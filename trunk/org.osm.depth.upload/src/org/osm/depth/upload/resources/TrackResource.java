/**
 * 
 Copyright (c) 2010-2013, Jens K�bler All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution. Neither the name of the <organization> nor the names
 * of its contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */
package org.osm.depth.upload.resources;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.osm.depth.upload.exceptions.DatabaseException;
import org.osm.depth.upload.messages.Track;

@Path("/track")
public class TrackResource {

	@javax.ws.rs.core.Context
    UriInfo uriInfo;
	
	@GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public List<Track> getAllTracks(@javax.ws.rs.core.Context SecurityContext context) {
		String username = context.getUserPrincipal().getName();
		Context initContext;
		try {
			initContext = new InitialContext();
			DataSource ds = (DataSource)initContext.lookup("java:/comp/env/jdbc/postgres"); //$NON-NLS-1$
			Connection conn = ds.getConnection();
			Statement statement = conn.createStatement();
			ResultSet executeQuery;
			if(context.isUserInRole("ADMIN")) { //$NON-NLS-1$
				executeQuery = statement.executeQuery("SELECT * FROM user_tracks u LEFT OUTER JOIN vesselconfiguration v ON u.vesselconfigid = v.id ORDER BY id"); //$NON-NLS-1$
			} else {
				executeQuery = statement.executeQuery("SELECT * FROM user_tracks u LEFT OUTER JOIN vesselconfiguration v ON u.vesselconfigid = v.id WHERE u.user_name='" + username + "' ORDER BY id"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			
			List<Track> list = new ArrayList<Track>(2);
			while(executeQuery.next()) {
				Track track = new Track(executeQuery.getLong("track_id"), executeQuery.getString("file_ref"), executeQuery.getInt("upload_state"), executeQuery.getString("fileType"), executeQuery.getString("compression"), executeQuery.getLong("containertrack")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				UriBuilder ub = uriInfo.getBaseUriBuilder();
//				track.delete = ub.path("/track/" + track.id).build().toString(); //$NON-NLS-1$
				list.add(track);
			}
//			GenericEntity<List<Track>> entity = new GenericEntity<List<Track>>(list) {/* */};
//			Link self = Link.fromMethod(TrackResource.class,"delete").build(); //$NON-NLS-1$
//			Response response = Response.ok().entity(entity).links(self).build();
			return list;
			
			
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DatabaseException("Internal SQL Error");
		} catch (NamingException e) {
			e.printStackTrace();
			throw new DatabaseException("Database unavailable");
		}
	}
	
//	@Path("newid")
	@POST
	@Consumes({MediaType.MULTIPART_FORM_DATA, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
	public Track newTrack(@javax.ws.rs.core.Context SecurityContext context) {
		String username = context.getUserPrincipal().getName();
		Context initContext;
		try {
			
			initContext = new InitialContext();
			DataSource ds = (DataSource)initContext.lookup("java:/comp/env/jdbc/postgres"); //$NON-NLS-1$
			Connection conn = ds.getConnection();

			Statement statement = conn.createStatement();
			Statement createIDStatement = conn.createStatement();
			
			ResultSet executeQuery = createIDStatement.executeQuery("SELECT nextval('user_tracks_track_id_seq')"); //$NON-NLS-1$
			if(executeQuery.next()) {
				Long id = executeQuery.getLong(1);
				statement.executeUpdate("INSERT INTO user_tracks (track_id, user_name) VALUES (" + id + ",'" + username + "')"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				Track track = new Track();
				track.id = id;
				return track;
			} else {
				// failed to create id
			}
			throw new DatabaseException("Database unavailable"); //$NON-NLS-1$
			
		} catch (SQLException e) {
			throw new DatabaseException("Internal SQL Error"); //$NON-NLS-1$
		} catch (NamingException e) {
			e.printStackTrace();
			throw new DatabaseException("Database unavailable"); //$NON-NLS-1$
		}
		
	}
	
//	@Path("upload")
	@PUT
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public String uploadDone(@javax.ws.rs.core.Context SecurityContext context, 
			@FormDataParam("track") InputStream uploadedInputStream,
			@FormDataParam("track") FormDataContentDisposition fileDetail,
			@FormDataParam("id") String trackID) {
		
//		FileUploadProgressMonitor progressMonitor = new FileUploadProgressMonitor();
		
		Long bytesRead = 0L;
		String username = context.getUserPrincipal().getName();
		Context initContext;
		try {
			File file = getFile(Long.parseLong(trackID));
			FileOutputStream outputStream = new FileOutputStream(file);
			byte[] buffer = new byte[16384];
			int len = uploadedInputStream.read(buffer);
			while (len != -1) {
				outputStream.write(buffer, 0, len);
			    len = uploadedInputStream.read(buffer);
			    bytesRead += len;
			}
			
			initContext = new InitialContext();
			DataSource ds = (DataSource)initContext.lookup("java:/comp/env/jdbc/postgres"); //$NON-NLS-1$
			Connection conn = ds.getConnection();

			Statement selectstatement = conn.createStatement();
			selectstatement.executeQuery(MessageFormat
					.format("SELECT upload_state FROM user_tracks WHERE track_id = {0} AND user_name = ''{1}''", //$NON-NLS-1$
							trackID, username));

			
			
			Statement statement = conn.createStatement();
			statement.execute(MessageFormat
					.format("UPDATE user_tracks SET file_ref = ''{0}'', upload_state = 1 WHERE track_id = {1}", //$NON-NLS-1$
							fileDetail.getFileName(), trackID));
			selectstatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DatabaseException("Internal SQL Error"); //$NON-NLS-1$
		} catch (NamingException e) {
			e.printStackTrace();
			throw new DatabaseException("Database unavailable"); //$NON-NLS-1$
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new DatabaseException("Uploaded file not processable"); //$NON-NLS-1$
		} catch (IOException e) {
			e.printStackTrace();
			throw new DatabaseException("Uploaded file not processable"); //$NON-NLS-1$
		}
		return bytesRead.toString();
	}
	
	@DELETE
	@Path("{id}")
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public void delete(@javax.ws.rs.core.Context SecurityContext context, @PathParam(value = "id") String id) {
		String username = context.getUserPrincipal().getName();
		Context initContext;
		try {
			long trackId = Long.parseLong(id);
			initContext = new InitialContext();
			DataSource ds = (DataSource)initContext.lookup("java:/comp/env/jdbc/postgres"); //$NON-NLS-1$
			Connection conn = ds.getConnection();

			Statement selectstatement = conn.createStatement();
			
			if(context.isUserInRole("ADMIN")) { //$NON-NLS-1$
				selectstatement.execute(MessageFormat
					.format("DELETE FROM user_tracks WHERE track_id = {0}", //$NON-NLS-1$
							id));
			} else {
				selectstatement.execute(MessageFormat
						.format("DELETE FROM user_tracks WHERE track_id = {0} AND user_name = ''{1}''", //$NON-NLS-1$
								id, username));
			}

			selectstatement.close();
			getFile(trackId).delete();
			
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DatabaseException("Internal SQL Error"); //$NON-NLS-1$
		} catch (NamingException e) {
			e.printStackTrace();
			throw new DatabaseException("Database unavailable"); //$NON-NLS-1$
		} catch (IOException e) {
			e.printStackTrace();
			throw new DatabaseException("Delete file not possible"); //$NON-NLS-1$
		}
	}
	
//	@GET
//	@Path("status/{id}")
//    @Produces({ MediaType.APPLICATION_JSON })
//	public long getById(@PathParam("id") long id) {
//		// return filesize
//		return 10000;
//	}
	
	/**
	 * 
	 * @param trackId
	 * @return the file to create or null if the directory could not be created
	 * @throws IOException 
	 */
	private File getFile(Long trackId) throws IOException {
		Long dirNumber = Math.round(trackId / 100L) * 100L;
		String fileDirectoryConfig = ""; //$NON-NLS-1$
		File fileDirectory = new File(fileDirectoryConfig + dirNumber.toString());
		String trackIDString = trackId.toString();
		if(!fileDirectory.exists()) {
			boolean mkdirs = fileDirectory.mkdirs();
			if(!mkdirs) {
				throw new IOException("Failed to create directory" + fileDirectory.getAbsolutePath()); //$NON-NLS-1$
			}
		}
		if(fileDirectory.exists()) {
			return new File(fileDirectory, trackIDString.substring(trackIDString.length() - 3, trackIDString.length()));
		}
		throw new IOException("Failed to create directory" + fileDirectory.getAbsolutePath()); //$NON-NLS-1$
	}
	
}
