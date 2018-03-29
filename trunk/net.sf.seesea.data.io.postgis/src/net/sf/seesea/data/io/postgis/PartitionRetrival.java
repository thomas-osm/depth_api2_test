/**
Copyright (c) 2013-2015, Jens Kübler
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
 * Neither the name of the <organization> nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package net.sf.seesea.data.io.postgis;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import net.sf.seesea.data.io.IParitionRetrival;
import net.sf.seesea.geometry.IIdentifiablePolygon;
import net.sf.seesea.geometry.impl.IdentifiablePolygon;
import net.sf.seesea.model.core.geo.GeoBoundingBox;

@Component
public class PartitionRetrival implements IParitionRetrival {

	public void getHitPartitionizedPolygons(Set<Long> trackIds, String trackpointTable, Set<String> inshoreOSMids,
			Set<String> offshoreIds) throws SQLException {
		for (Long trackId : trackIds) {
			// the bounding box of the points for the track
			try (PreparedStatement boundaryStatement = triangulationConnection.prepareStatement(
					"SELECT st_xmin(ST_Extent(the_geom)), st_xmax(ST_Extent(the_geom)), st_ymin(ST_Extent(the_geom)), st_ymax(ST_Extent(the_geom)) FROM "
							+ trackpointTable + " WHERE datasetid = ?")) {
				boundaryStatement.setLong(1, trackId);
				try (ResultSet query = boundaryStatement.executeQuery()) {
					if (query.next()) {
						double xmin = query.getDouble(1);
						double xmax = query.getDouble(2);
						double ymin = query.getDouble(3);
						double ymax = query.getDouble(4);
						if (xmin == 0.0 && xmax == 0.0 || ymin == 0.0 && ymax == 0.0) {
							Logger.getLogger(getClass())
									.info("Skipping track id because no bounding box exists : " + trackId);
							continue;
						}

						getHitPartitionizedPolygons(inshoreOSMids, offshoreIds, trackId, xmin, xmax, ymin, ymax);

						// ResultSet offshorePartionizedPolygonResultSet =
						// offshoreStatement.executeQuery("SELECT path,
						// ST_X(geom) AS lon, ST_Y(geom) as lat, gid FROM
						// (SELECT (ST_DUMPPOINTS(geom)).*, gid FROM
						// gebco_poly_100 WHERE ST_Overlaps(geom," + envelope +
						// ") OR ST_Contains(" + envelope +", geom) OR
						// ST_Contains(geom," + envelope +") ) AS g");
					}
				}
			}
		}
	}

	public void getHitPartitionizedPolygons(GeoBoundingBox boundingBox, Set<String> inshoreOSMids,
			Set<String> offshoreIds) throws SQLException {
		getHitPartitionizedPolygons(inshoreOSMids, offshoreIds, 0L, boundingBox.getLeft(), boundingBox.getRight(),
				boundingBox.getBottom(), boundingBox.getTop());
	}

	public void getHitPartitionizedPolygons(Set<String> inshoreOSMids, Set<String> offshoreIds, Long trackId,
			double xmin, double xmax, double ymin, double ymax) throws SQLException {
		// select all polygons of that bounding box
		try (Statement inshoreStatement = osmConnectionReference.createStatement();
				Statement offshoreStatement = triangulationConnection.createStatement()) {
			// formatter:off
			NumberFormat format = DecimalFormat.getNumberInstance(Locale.ENGLISH);
			format.setMaximumFractionDigits(Integer.MAX_VALUE);
			String envelope = MessageFormat.format("ST_MakeEnvelope({0}, {1}, {2}, {3}, 4326)", format.format(xmin),
					format.format(ymin), format.format(xmax), format.format(ymax));
			try (ResultSet inshorePartionizedPolygonResultSet = inshoreStatement.executeQuery(
					"SELECT osm_id FROM " + "(SELECT * FROM (" + "SELECT osm_id FROM planet_osm_polygon AS poly WHERE "
							+ "(ST_Overlaps(way, ST_Transform(" + envelope + ", 900913)) OR ST_Contains(ST_Transform("
							+ envelope + ", 900913), way) OR ST_Contains(way, ST_Transform(" + envelope
							+ ", 900913)))  AND (\"natural\" = 'water' OR waterway IS NOT NULL OR water IS NOT NULL) ) AS xx  "
							+ ") as g ")) {
				while (inshorePartionizedPolygonResultSet.next()) {
					inshoreOSMids.add(inshorePartionizedPolygonResultSet.getString(1));
				}
			}
			try (ResultSet offshorePartionizedPolygonResultSet = offshoreStatement
					.executeQuery("SELECT gid FROM (SELECT gid FROM gebco_poly_100 WHERE ST_Overlaps(geom," + envelope
							+ ") OR ST_Contains(" + envelope + ", geom) OR ST_Contains(geom," + envelope
							+ ") ) AS g")) {
				while (offshorePartionizedPolygonResultSet.next()) {
					offshoreIds.add(offshorePartionizedPolygonResultSet.getString(1));
				}
				Logger.getLogger(getClass()).info((inshoreOSMids.size() + offshoreIds.size())
						+ " areas are in the given bounding box for track id " + trackId);
			}
			// formatter:on
		}
	}

	public IIdentifiablePolygon getHitPartitionizedPolygon(Set<String> inshoreOSMids, Set<String> offshoreIds,
			double lat, double lon) throws SQLException {
		// select all polygons of that bounding box
		try (Statement inshoreStatement = osmConnectionReference.createStatement();
				Statement offshoreStatement = triangulationConnection.createStatement()) {
			// formatter:off
			NumberFormat format = DecimalFormat.getNumberInstance(Locale.ENGLISH);
			format.setMaximumFractionDigits(Integer.MAX_VALUE);
			String envelope = MessageFormat.format("ST_GEOMFROM_TEXT('POINT ({0} {1})', 4326)", format.format(lon),
					format.format(lat));
			try (ResultSet inshorePartionizedPolygonResultSet = inshoreStatement.executeQuery(
					"SELECT osm_id FROM " + "(SELECT * FROM (" + "SELECT osm_id FROM planet_osm_polygon AS poly WHERE "
							+ "(ST_Contains(way, ST_Transform(" + envelope
							+ ", 900913)))  AND (\"natural\" = 'water' OR waterway IS NOT NULL OR water IS NOT NULL) ) AS xx  "
							+ ") as g ")) {
				while (inshorePartionizedPolygonResultSet.next()) {
					inshoreOSMids.add(inshorePartionizedPolygonResultSet.getString(1));
				}
			}
			try (ResultSet offshorePartionizedPolygonResultSet = offshoreStatement
					.executeQuery("SELECT gid FROM (SELECT gid FROM gebco_poly_100 WHERE ST_Overlaps(geom," + envelope
							+ ") OR ST_Contains(" + envelope + ", geom) OR ST_Contains(geom," + envelope
							+ ") ) AS g")) {
				while (offshorePartionizedPolygonResultSet.next()) {
					offshoreIds.add(offshorePartionizedPolygonResultSet.getString(1));
				}
			}
			// formatter:on
		}
		// FIXME: add a real polygon
		return new IdentifiablePolygon();
	}

	@Reference(policy = ReferencePolicy.DYNAMIC, target = "(db=userData)", cardinality = ReferenceCardinality.MANDATORY)
	public synchronized void bindUserDataConnection(DataSource connection) {
		userDataDataSourceReference.set(connection);
	}

	public synchronized void unbindUserDataConnection(DataSource connection) {
		userDataDataSourceReference.compareAndSet(connection, null);
	}
	
	private AtomicReference<DataSource> userDataDataSourceReference = new AtomicReference<DataSource>();
	
	private AtomicReference<DataSource> triangulationDataSourceReference = new AtomicReference<DataSource>();

	private AtomicReference<DataSource> osmDataSourceReference = new AtomicReference<DataSource>();
	
	@Activate
	public synchronized void actviate() throws SQLException {
		DataSource ds = triangulationDataSourceReference.get();
		triangulationConnection = ds.getConnection();
		DataSource ds2 = userDataDataSourceReference.get();
		userDataConnectionReference = ds2.getConnection();
		DataSource ds3 = osmDataSourceReference.get();
		osmConnectionReference = ds3.getConnection();

	}
	
	@Deactivate
	public synchronized void deactivate() {
		try {
			triangulationConnection.close();
			userDataConnectionReference.close();
			osmConnectionReference.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


	private Connection userDataConnectionReference;

	private Connection triangulationConnection;

	private Connection osmConnectionReference;

	@Reference(policy = ReferencePolicy.DYNAMIC, target = "(db=coastline)", cardinality = ReferenceCardinality.MANDATORY)
	public synchronized void bindGaugeConnection(DataSource connection) {
		triangulationDataSourceReference.set(connection);
	}

	public synchronized void unbindGaugeConnection(DataSource connection) {
		triangulationDataSourceReference.compareAndSet(connection, null);
	}

	@Reference(policy = ReferencePolicy.DYNAMIC, target = "(db=osm)", cardinality = ReferenceCardinality.MANDATORY)
	public synchronized void bindOSMConnection(DataSource connection) {
		osmDataSourceReference.set(connection);
	}

	public synchronized void unbindOSMConnection(DataSource connection) {
		osmDataSourceReference.compareAndSet(connection, null);
	}

}
