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
package net.sf.seesea.waterlevel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import net.sf.seesea.data.io.IParitionRetrival;
import net.sf.seesea.gauge.GaugeUpdateException;
import net.sf.seesea.gauge.IGaugeProvider;
import net.sf.seesea.gauge.IGaugeValueUpdater;
import net.sf.seesea.track.api.data.ITrackFile;

@Component(immediate = true)
public class GaugeValueUpdater implements IGaugeValueUpdater {

	public GaugeValueUpdater() {
		gaugeProviderRefereneces = new CopyOnWriteArrayList<ServiceReference<IGaugeProvider>>();
	}
	
	public void updateGaugeValues4Track(List<ITrackFile> clusterOfTrackFiles) throws GaugeUpdateException {
		ITrackFile startTrackFile = clusterOfTrackFiles.get(0);
		Date startTime = startTrackFile.getStartTime();
		ITrackFile endTrackFile = clusterOfTrackFiles.get(clusterOfTrackFiles.size() - 1);
		Date endTime = endTrackFile.getEndTime();
		// offshore onshore ids : getHitPartitionizedPolygonIds()
		
		// maybe we hit more polygons than required but thats ok
		Set<Long> candidatePolygonIds = new HashSet<>();
		// for all water polygons in the bounding box update the gauge values
		
		Set<String> inshoreOSMids = new HashSet<String>();
		Set<String> offshoreIds = new HashSet<String>();;

		for (ITrackFile trackFile : clusterOfTrackFiles) {
			try {
				if(trackFile.getBoundingBox() != null && (Math.abs(trackFile.getBoundingBox().getRight() - trackFile.getBoundingBox().getLeft()) > 2.0 || Math.abs(trackFile.getBoundingBox().getTop() - trackFile.getBoundingBox().getBottom()) > 2.0)) {
					throw new GaugeUpdateException("Bounding box of track " + trackFile.getTrackId() + " is larger than 2 degrees. Skipping gauge determination.");
				}
				partitionizeReference.get().getHitPartitionizedPolygons(trackFile.getBoundingBox(), inshoreOSMids, offshoreIds);
				for (String string : inshoreOSMids) {
					candidatePolygonIds.add(Long.parseLong(string));
				}
			} catch (SQLException e1) {
				throw new GaugeUpdateException(e1);
			}
		}
		if(candidatePolygonIds.isEmpty()) {
			return;
		}

//		Map<Long, Long> gaugeContainingPolygonIds2gaugeId = getGaugePoly();
		
		Set<Long> gaugeIds = new HashSet<>();
		for (Long polygonIds : candidatePolygonIds) {
			Set<Long> trackPolygonId = new HashSet<Long>();
//		trackPolygonId.add(27425090L);
			trackPolygonId.add(polygonIds);
			Long nearestGaugePolygon;
			try {
				nearestGaugePolygon = getNearestGaugePolygon(trackPolygonId, 0, gaugeContainingPolygonIds2gaugeId.keySet(), new HashSet<Long>());
				if(debug) {
					Logger.getLogger(GaugeValueUpdater.class).debug("Gauge Polygon found:" + nearestGaugePolygon);
				}
				if(nearestGaugePolygon != null) {
					gaugeIds.add(gaugeContainingPolygonIds2gaugeId.get(nearestGaugePolygon));
				}
			} catch (SQLException e) {
				throw new GaugeUpdateException("Failed to determine gauge id to update", e);
			}
		}

		
		for (Long gaugeId : gaugeIds) {
			try {
				retrieveLatestGaugeValues(startTime, endTime, gaugeId);
			} catch (GaugeUpdateException e) {
				Logger.getLogger(getClass()).error(MessageFormat.format("Failed to update gauge with id {0} start:{1} end:{2}", gaugeId, startTime, endTime) ,e);
			}
		}
	}
	
	public Long getGaugeId(long polygonId) throws GaugeUpdateException {
		Set<Long> trackPolygonId = new HashSet<Long>();
		trackPolygonId.add(polygonId);
		try {
			return getNearestGaugePolygon(trackPolygonId, 0, gaugeContainingPolygonIds2gaugeId.keySet(), new HashSet<Long>());
		} catch (SQLException e) {
			throw new GaugeUpdateException(e);
		}
	}

	public void retrieveLatestGaugeValues(Date startTime, Date endTime, long gaugeId) throws GaugeUpdateException {
		Logger.getLogger(getClass()).info(MessageFormat.format("Update gauge {0} between start {1} and end {2}", gaugeId, startTime, endTime));
		// which gauge provider is responsible for that location ?
		try (PreparedStatement gaugeProviderStatement = gaugeconnection.prepareStatement("SELECT remoteid, provider FROM gauge WHERE id = ?");) {
			gaugeProviderStatement.setLong(1, gaugeId);
			try(ResultSet queryRemoteGauge = gaugeProviderStatement.executeQuery()) {
				while(queryRemoteGauge.next()) {
					String remoteId = queryRemoteGauge.getString(1);
					String provider = queryRemoteGauge.getString(2);
					boolean providerFound = false;
					for (ServiceReference<IGaugeProvider> gaugeProviderReference : gaugeProviderRefereneces) {
						if(gaugeProviderReference.getProperty("provider").equals(provider)) {
							providerFound = true;
							IGaugeProvider gaugeProvider = (IGaugeProvider) componentContext.locateService("GAUGE_PROVIDERS", gaugeProviderReference);
							gaugeProvider.updateSingleGaugeMeasurements(Long.toString(gaugeId), remoteId, startTime, endTime);
						}
					}
					if(!providerFound) {
						Logger.getLogger(getClass()).info("No gauge provider found for gauge id " + gaugeId + " and provider " + provider);
					}
				}
			}
		} catch (SQLException e) {
			throw new GaugeUpdateException(e);
		}
	}

    private int maxRecursionDepth;
    
    private boolean debug; 
    
    /**
     * TODO: area could be better restricted
     * 
     * @return the gauge id to be updated for the given osm polygon id  
     * 
     * @throws GaugeUpdateException
     */
	public Map<Long, Long> getGaugePoly() throws GaugeUpdateException {
		// either mark all polygons beginning from the gauges or mark the polygons from the tracks (and hence the areas)
		
		Map<Long, Long> gaugeContainingPolygonIds2gaugeId = new HashMap<Long, Long>();
		Map<Long, String> gaugeId2Name = new HashMap<Long, String>();
		
		// get all polygons for the given gauge locations
		try (Statement statement = gaugeconnection.createStatement()) {
			try (ResultSet executeQuery = statement.executeQuery("SELECT id, name, ST_X(ST_ASTEXT(geom)), ST_Y(ST_ASTEXT(geom)) FROM gauge")) {
				while(executeQuery.next()) {
					String lon = executeQuery.getString(3);
					String lat = executeQuery.getString(4);
					String name = executeQuery.getString(2);
					Long gaugeId = executeQuery.getLong(1);
					gaugeId2Name.put(gaugeId, name);
					try (Statement statementGaugePolygon = osmConnection.createStatement()) {
						// get the polygons which are very close to the gauges, assuming that they are very close to the shore
						try(ResultSet execute = statementGaugePolygon.executeQuery("SELECT osm_id FROM planet_osm_polygon AS b WHERE ST_DWithin(way, ST_Transform(ST_GeomFromText( 'POINT(" + lon + " " + lat + ")', 4326), 900913), 100) AND (water is not null OR waterway is not null OR \"natural\" = 'water')")) {
							while(execute.next()) {
								long id = execute.getLong(1);
								gaugeContainingPolygonIds2gaugeId.put(id, gaugeId);
								if(debug) {
									Logger.getLogger(GaugeValueUpdater.class).debug("Gauge added for search" + id + ":" + lat + ":" + lon + ":" + name);
								}
							}
						}
					}
				}
			}
				
		} catch (SQLException e) {
			throw new GaugeUpdateException("Failed to determine viable gauge id for given location", e);
		}
		return gaugeContainingPolygonIds2gaugeId;
	}

	/**
	 * recursively determine the first osm polygon for the given osm node. This is a breadth first search 
	 * 
	 * @param nodes
	 * @param recursionDepth
	 * @param gaugeContainingPolygonIds
	 * @param visitedNodes
	 * @return
	 * @throws SQLException
	 */
	private Long getNearestGaugePolygon(Set<Long> nodes, int recursionDepth, Set<Long> gaugeContainingPolygonIds , Set<Long> visitedNodes) throws SQLException {
		try (Statement statement = osmConnection.createStatement()) {
			Set<Long> osmIds = new LinkedHashSet<Long>();
			visitedNodes.addAll(nodes);
			// this is a breadth first search. It there fore is related to the distance of reachability and may not always yield the closest point but a first good approximation
			for (Long node : nodes) {
				StringBuffer out = new StringBuffer();
				if(debug) {
					for (int i = 0 ; i < recursionDepth; i++) {
						out.append(">");
					}
				}
				try(ResultSet execute = statement.executeQuery("SELECT (b.osm_id) FROM planet_osm_polygon as a,planet_osm_polygon as b WHERE ST_Touches(a.way, b.way) AND a.osm_id != b.osm_id AND (b.water is not null OR b.waterway is not null OR b.natural = 'water') AND a.osm_id = " + node)) {
					while(execute.next()) {
						long osmId = execute.getLong(1);
						if(visitedNodes.contains(osmId)) {
							continue;
						}
						if(gaugeContainingPolygonIds.contains(osmId)) {
							return osmId;
						} else {
							osmIds.add(osmId);
						}
						if(debug) {
							Logger.getLogger(GaugeValueUpdater.class).debug(out.toString() + node + "->" + osmId);
						}
					}
				}
			}
			if(recursionDepth < maxRecursionDepth) {
				Long neighbors = getNearestGaugePolygon(osmIds, ++recursionDepth, gaugeContainingPolygonIds, visitedNodes);
				if(neighbors != null) {
					return neighbors;
				}
			} else {
				if(debug) {
					Logger.getLogger(GaugeValueUpdater.class).debug("recursion depth reached - no gauge found for node id ");
				}
			}
		}
		return null;
	}

	
	private AtomicReference<DataSource> gaugeConnectionReference = new AtomicReference<DataSource>();

	@Reference(policy = ReferencePolicy.DYNAMIC, target = "(db=osm)", cardinality = ReferenceCardinality.MANDATORY)
	public synchronized void bindOSMConnection(DataSource connection) {
		osmConnectionReference.set(connection);
	}

	public synchronized void unbindOSMConnection(DataSource connection) {
		osmConnectionReference.compareAndSet(connection, null);
	}

	private AtomicReference<DataSource> osmConnectionReference = new AtomicReference<DataSource>();

	@Reference(policy = ReferencePolicy.DYNAMIC, target = "(db=gauge)", cardinality = ReferenceCardinality.MANDATORY)
	public synchronized void bindGaugeConnection(DataSource connection) {
		gaugeConnectionReference.set(connection);
	}

	public synchronized void unbindGaugeConnection(DataSource connection) {
		gaugeConnectionReference.compareAndSet(connection, null);
	}

	private AtomicReference<IParitionRetrival> partitionizeReference = new AtomicReference<IParitionRetrival>();

	@Reference(policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MANDATORY)
	public void bindPartitionizer(IParitionRetrival partitionize) {
		partitionizeReference.set(partitionize);
	}

	public void unbindPartitionizer(IParitionRetrival partitionize) {
		partitionizeReference.compareAndSet(partitionize, null);
	}


	private ComponentContext componentContext;

	private Connection osmConnection;
	
	@Activate
	public synchronized void actviate(ComponentContext componentContext) throws GaugeUpdateException {
    	maxRecursionDepth = 10;
    	debug = false;
		this.componentContext = componentContext;
		DataSource dataSource = gaugeConnectionReference.get();
		DataSource openStreetmapDatasource = osmConnectionReference.get();
		try {
			gaugeconnection = dataSource.getConnection();
			osmConnection = openStreetmapDatasource.getConnection();
		} catch (SQLException e) {
			throw new GaugeUpdateException(e);
		}
		gaugeContainingPolygonIds2gaugeId = getGaugePoly();

	}
	
	@Deactivate
	public synchronized void deactivate(ComponentContext componentContext) {
		this.componentContext = null;
		try {
			gaugeconnection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private List<ServiceReference<IGaugeProvider>> gaugeProviderRefereneces;

	private Map<Long, Long> gaugeContainingPolygonIds2gaugeId;

	private Connection gaugeconnection;

	
	@Reference(policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MULTIPLE, service = IGaugeProvider.class, name = "GAUGE_PROVIDERS")
	public void bindGaugeProvider(ServiceReference<IGaugeProvider> serviceReference) {
		gaugeProviderRefereneces.add(serviceReference);
	}
	
	public void unbindGaugeProvider(ServiceReference<IGaugeProvider> serviceReference) {
		gaugeProviderRefereneces.remove(serviceReference);
	}

}
