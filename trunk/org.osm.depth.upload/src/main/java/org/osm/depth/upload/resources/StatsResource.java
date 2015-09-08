package org.osm.depth.upload.resources;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.osm.depth.upload.exceptions.DatabaseException;
import org.osm.depth.upload.messages.Stats;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Path("/stats")
@Api(value ="/stats")
public class StatsResource {
	
	@ApiOperation(value = "Get database statistics", response = Stats.class)
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Stats getStats() {
		Stats stats = new Stats();
		Context initContext;
		try {
			initContext = new InitialContext();
			DataSource ds = (DataSource) initContext.lookup("java:/comp/env/jdbc/postgres"); //$NON-NLS-1$
			DataSource dsDepth = (DataSource)initContext.lookup("java:/comp/env/jdbc/depth"); //$NON-NLS-1$
			Connection connection = ds.getConnection();
			try {
				Statement statement = connection.createStatement();
				try {
					statement.execute("SELECT count(user_name) FROM user_profiles"); //$NON-NLS-1$
					ResultSet resultSet = statement.getResultSet();
					while(resultSet.next()) {
						stats.usercount = resultSet.getInt(1);
					}
				} finally {
					statement.close();
				}
				Connection depthConn = dsDepth.getConnection();
				try {
					statement = depthConn.createStatement();
					try {
						statement.execute("SELECT count(datasetid) FROM trackpoints_raw_16"); //$NON-NLS-1$
						ResultSet resultSet = statement.getResultSet();
						while(resultSet.next()) {
							stats.contributedpoints = resultSet.getInt(1);
						}
					} finally {
						statement.close();
					}
					statement = depthConn.createStatement();
					try {
						statement.execute("SELECT COUNT(datasetid) FROM (SELECT DISTINCT datasetid FROM trackpoints_raw_16) AS temp"); //$NON-NLS-1$
						ResultSet resultSet = statement.getResultSet();
						while(resultSet.next()) {
							stats.trackscount = resultSet.getInt(1);
						}
					} finally {
						statement.close();
					}
				} finally {
					depthConn.close();
				}
			} finally {
				connection.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DatabaseException("Internal SQL Error");
		} catch (NamingException e) {
			e.printStackTrace();
			throw new DatabaseException("Database unavailable");
		}
		return stats;
	}

}
