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
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import net.sf.seesea.data.io.IDataWriter;
import net.sf.seesea.data.io.WriterException;
import net.sf.seesea.model.core.geo.Depth;
import net.sf.seesea.model.core.geo.MeasuredPosition3D;
import net.sf.seesea.model.core.physx.CompositeMeasurement;
import net.sf.seesea.model.core.physx.Measurement;

/**
 * 
 * This class writes data points to multiple tables
 * 
 * Schema to write to is;
 * 
 * <pre>
 * gid serial NOT NULL,
 * lat numeric,
 * lon numeric,
 * dbs numeric,
 * the_geom geometry,
 * datasetid character varying(100) NOT NULL DEFAULT 'default'::character varying,
 * CONSTRAINT trackpoints_cor1_pkey PRIMARY KEY (gid),
 * CONSTRAINT enforce_dims_the_geom CHECK (st_ndims(the_geom) = 2),
 * CONSTRAINT enforce_geotype_the_geom CHECK (geometrytype(the_geom) = 'POINT'::text OR the_geom IS NULL),
 *  CONSTRAINT enforce_srid_the_geom CHECK (st_srid(the_geom) = 4326)
 * </pre>
 * 
 */
@Component(configurationPolicy = ConfigurationPolicy.REQUIRE, property = "type=postgis")
public class PostInsertGISWriter implements IDataWriter {

	private List<PreparedStatement> insertStatements;
	private int batchSizeCounter;
	private List<String> tableNames;
	private Map<Integer, Long> counters;
	private DataSource outputDataSource;
	private Connection connection;

	public void activate(Map<String, Object> properties) throws SQLException {
		tableNames = Arrays.asList(((String) properties.get("outputTables")).split(","));
		connection = outputDataSource.getConnection();
		// for (String tableName : tableNames) {
		// try(Statement statement = connection.createStatement()) {
		// statement.execute("CREATE SEQUENCE IF NOT EXISTS " + tableName
		// +"_gid_seq");
		// }
		// }
		try (Statement statement = connection.createStatement()) {// NOT NULL
																	// DEFAULT
																	// nextval('"
																	// +
																	// tableNames.get(0)
																	// +
																	// "_gid_seq'::regclass)
			boolean execute = statement.execute("CREATE TABLE IF NOT EXISTS " + tableNames.get(0)
					+ " ( lat numeric, lon numeric(11, 8), dbs numeric(8, 2), valid boolean, gid serial , datasetid integer, the_geom geometry,latvar double precision DEFAULT 0, lonvar double precision DEFAULT 0, depthvar double precision DEFAULT 0,recordingdate timestamp with time zone, CONSTRAINT "
					+ tableNames.get(0)
					+ "_pkey PRIMARY KEY (gid), CONSTRAINT enforce_srid_the_geom CHECK (st_srid(the_geom) = 4326), CONSTRAINT enforce_geotype_the_geom CHECK (geometrytype(the_geom) = 'POINT'::text OR the_geom IS NULL),CONSTRAINT enforce_dims_the_geom CHECK (st_ndims(the_geom) = 2))");
		} catch (SQLException e) {
			e.getNextException().printStackTrace();
		}
		for (String tableName : tableNames.subList(1, tableNames.size())) {
			try (Statement statement = connection.createStatement()) {// NOT
																		// NULL
																		// DEFAULT
																		// nextval('"
																		// +
																		// tableName
																		// +
																		// "_gid_seq'::regclass)
				statement.execute("CREATE TABLE IF NOT EXISTS " + tableName
						+ " ( lat numeric, lon numeric(11, 8), dbs numeric(8, 2), valid boolean, gid serial , datasetid integer, the_geom geometry, recordingdate timestamp with time zone, CONSTRAINT "
						+ tableName
						+ "_pkey PRIMARY KEY (gid), CONSTRAINT enforce_srid_the_geom CHECK (st_srid(the_geom) = 4326), CONSTRAINT enforce_geotype_the_geom CHECK (geometrytype(the_geom) = 'POINT'::text OR the_geom IS NULL),CONSTRAINT enforce_dims_the_geom CHECK (st_ndims(the_geom) = 2))");
			}
		}
		insertStatements = new ArrayList<PreparedStatement>(tableNames.size());
		prepareInsertStatement();
		batchSizeCounter = 0;
		counters = new HashMap<Integer, Long>();
		for (int i = 0; i < tableNames.size(); i++) {
			counters.put(i, 100L);
		}
	}

	public void deactivate(Map<String, Object> properties) throws SQLException {
		connection.close();
	}

	private void prepareInsertStatement() throws SQLException {
		for (int i = 0; i < tableNames.size(); i++) {
			String insert = null;
			if (i == 0) {
				insert = "INSERT INTO " //$NON-NLS-1$
						+ tableNames.get(i)
						+ " (lat, lon , dbs, datasetid, valid, recordingdate, latvar, lonvar, depthvar, the_geom) VALUES (?::numeric(8,6), ?::numeric(11,8), ?::numeric(8,2), ?, ?, ?, ?, ?, ?, ST_SetSRID(ST_MakePoint(?, ?), 4326))"; //$NON-NLS-1$
			} else {
				insert = "INSERT INTO " //$NON-NLS-1$
						+ tableNames.get(i)
						+ " (lat, lon , dbs, datasetid, valid, recordingdate, the_geom) VALUES (?::numeric(8,6), ?::numeric(11,8), ?::numeric(8,2), ?, ?, ?,  ST_SetSRID(ST_MakePoint(?, ?), 4326))"; //$NON-NLS-1$
			}
			PreparedStatement prepareStatement = connection.prepareStatement(insert);
			insertStatements.add(prepareStatement);
		}
	}

	@Override
	public void closeOutput() throws WriterException {
		try {
			for (PreparedStatement insertStatement : insertStatements) {
				insertStatement.executeBatch();
			}
		} catch (SQLException e) {
			throw new WriterException(e);
		}
	}

	@Override
	public void write(Measurement measurement, boolean valid, long sourceTrackIdentifier) throws WriterException {
		if (measurement instanceof CompositeMeasurement) {
			CompositeMeasurement compositeMeasurement = (CompositeMeasurement) measurement;
			write(compositeMeasurement.getMeasurements(), valid, sourceTrackIdentifier);
		}
	}

	@Override
	public void write(Collection<Measurement> measurements, boolean valid, long sourceTrackIdentifier)
			throws WriterException {
		MeasuredPosition3D geoPosition = null;
		Depth depth = null;

		for (Measurement measurement2 : measurements) {
			if (measurement2 instanceof MeasuredPosition3D) {
				geoPosition = (MeasuredPosition3D) measurement2;
			} else if (measurement2 instanceof Depth) {
				depth = (Depth) measurement2;
			}
		}
		if (geoPosition != null && depth != null) {
			if (valid && (float) depth.getDepth() > 0.01) {
				double latitude = geoPosition.getLatitude().getDecimalDegree();
				double longitude = geoPosition.getLongitude().getDecimalDegree();
				double depth2 = depth.getDepth();
				Date time = geoPosition.getTime();
				write(latitude, longitude, depth2, sourceTrackIdentifier, 0, 0, 0, time);
			}
		}
	}

	// @Override
	public void write(double lat, double lon, double depth, long sourceTrackIdentifier, double latVariance,
			double lonVariance, double depthVariance, Date time) throws WriterException {
		try {
			for (int i = 0; i < insertStatements.size(); i++) {
				PreparedStatement insertStatement = insertStatements.get(i);
				if (i == 0) {
					insertStatement.setDouble(1, truncateDecimals(lat,8));
					insertStatement.setDouble(2, truncateDecimals(lon,8));
					insertStatement.setDouble(3, truncateDecimals(depth,2));
					insertStatement.setLong(4, sourceTrackIdentifier);
					insertStatement.setBoolean(5, true);
					if (time == null) {
						insertStatement.setTimestamp(6, new Timestamp(0));
					} else {
						insertStatement.setTimestamp(6, new Timestamp(time.getTime()));
					}
					insertStatement.setDouble(7, latVariance);
					insertStatement.setDouble(8, lonVariance);
					insertStatement.setDouble(9, depthVariance);
					insertStatement.setDouble(10, lon);
					insertStatement.setDouble(11, lat);
					insertStatement.addBatch();
					batchSizeCounter++;
					if (batchSizeCounter > 5000) {
						insertStatement.executeBatch();
						batchSizeCounter = 0;
						insertStatements.remove(i);
						String insert = "INSERT INTO " //$NON-NLS-1$
								+ tableNames.get(i)
								+ " (lat, lon , dbs, datasetid, valid,recordingdate, latvar, lonvar, depthvar, the_geom) VALUES (?::numeric(8,6), ?::numeric(11,8), ?::numeric(8,2), ?,?,?,?,?,?, ST_SetSRID(ST_MakePoint(?, ?), 4326))"; //$NON-NLS-1$
						PreparedStatement prepareStatement = connection.prepareStatement(insert);
						insertStatements.add(i, prepareStatement);
					}
				} else if (i > 0) {
					Long counter = counters.get(i);
					counters.put(i, counter += 1L);
					if (((i == 1) && (counter % 32 == 0)) || ((i == 2) && (counter % 128 == 0))
							|| ((i == 3) && (counter % 512 == 0))) {
						insertStatement.setDouble(1, truncateDecimals(lat,8));
						insertStatement.setDouble(2, truncateDecimals(lon,8));
						insertStatement.setDouble(3, truncateDecimals(depth,2));
						insertStatement.setLong(4, sourceTrackIdentifier);
						insertStatement.setBoolean(5, true);
						if (time == null) {
							insertStatement.setTimestamp(6, new Timestamp(0));
						} else {
							insertStatement.setTimestamp(6, new Timestamp(time.getTime()));
						}
						insertStatement.setDouble(7, lon);
						insertStatement.setDouble(8, lat);
						insertStatement.addBatch();
						batchSizeCounter++;
						if (batchSizeCounter > 5000) {
							insertStatement.executeBatch();
							batchSizeCounter = 0;
							insertStatements.remove(i);
							String insert = "INSERT INTO " //$NON-NLS-1$
									+ tableNames.get(i)
									+ " (lat, lon , dbs, datasetid, valid,recordingdate, the_geom) VALUES (?::numeric(8,6), ?::numeric(11,8), ?::numeric(8,2), ?, ?,?,  ST_SetSRID(ST_MakePoint(?, ?), 4326))"; //$NON-NLS-1$
							PreparedStatement prepareStatement = connection.prepareStatement(insert);
							insertStatements.add(i, prepareStatement);
						}
						counters.put(i, 1L);
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			SQLException nextException = e.getNextException();
			if (nextException != null) {
				nextException.printStackTrace();
			}
			throw new WriterException("Failed to insert data into table", e); //$NON-NLS-1$
		}
	}

	public static double truncateDecimals(double d, int len) {
	    long p = pow(10, len);
	    long l = (long)(d * p);
	    return (double)l / (double)p;
	}

	public static long pow(long a, int b) {
	    long result = 1;
	    for (int i = 1; i <= b; i++) {
	       result *= a;
	    }
	    return result;
	}
	
	@Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.DYNAMIC, target = "(db=depth)")
	public synchronized void bindOutputConnection(DataSource outputDataSource) {
		this.outputDataSource = outputDataSource;
	}

	public synchronized void unbindOutputConnection(DataSource outputDataSource) {
		this.outputDataSource = null;
	}

	@Override
	public void clearAllOutput() throws WriterException {
		for (String table : tableNames) {
			try(PreparedStatement deletePostprocessStatement = connection.prepareStatement("DELETE FROM " + table)) {
				int executeUpdate = deletePostprocessStatement.executeUpdate();
			 } catch (SQLException e) {
				 throw new WriterException("Failed to delete from table", e);
			}
		}
	}

	@Override
	public void clearOutput(Long trackId) throws WriterException {
		if (trackId == null) {
			throw new IllegalArgumentException("Track Id must not be null");
		}
		for (String table : tableNames) {
			 try(PreparedStatement deletePostprocessStatement = connection.prepareStatement("DELETE FROM " + table + " WHERE datasetid = ?")) {
				 deletePostprocessStatement.setLong(1, trackId);
				 int executeUpdate = deletePostprocessStatement.executeUpdate();
			 } catch (SQLException e) {
				 throw new WriterException("Failed to delete from table", e);
			}
		 }

	}
	
}
