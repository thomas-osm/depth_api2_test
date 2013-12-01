package org.osm.depth.upload;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import junit.framework.TestCase;

import org.eclipse.persistence.jaxb.rs.MOXyJsonProvider;
import org.glassfish.jersey.client.filter.HttpBasicAuthFilter;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.media.multipart.Boundary;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;
import org.osm.depth.upload.messages.Track;

public class TrackResourceTest extends TestCase {

	public void testX() {
		Client client = ClientBuilder.newClient();
		client.register(new HttpBasicAuthFilter(TestConstants.TESTUSER, TestConstants.TESTPASSWORD));
		WebTarget basePath = client.target(TestConstants.HOST).path(TestConstants.PATH).path(TestConstants.APIPATH);
		WebTarget target = basePath.path("track");
		
		Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_XML);
		 
		Response response = invocationBuilder.get();
		Set<Link> links = response.getLinks();
//		URI uri = links.iterator().next().getUri();
		System.out.println(response.getStatus());
		System.out.println(response.readEntity(String.class));
//		System.out.println(uri);

	}
	
	public void testTrackUploadRoundTrip() throws FileNotFoundException {
		Client client = ClientBuilder.newClient();
		client.register(MOXyJsonProvider.class);
		client.register(MultiPartFeature.class);
		LoggingFilter loggingFilter = new LoggingFilter();
		client.register(loggingFilter);

		client.register(new HttpBasicAuthFilter(TestConstants.TESTUSER, TestConstants.TESTPASSWORD));
		WebTarget basePath = client.target(TestConstants.HOST).path(TestConstants.PATH).path(TestConstants.APIPATH);
		
		WebTarget trackPath = basePath.path("track");
		Track track3 = new Track();
		track3.vesselconfigid = 1;
		track3.license = 1;
		Response response = trackPath.request(MediaType.APPLICATION_JSON).post(Entity.entity(track3, MediaType.APPLICATION_JSON));
		assertEquals(200, response.getStatus());
		
		Track track = response.readEntity(Track.class);
		
		FormDataMultiPart multipart = new FormDataMultiPart();

//        multipart.field("track", "text.txt");
        multipart.field("id", new Long(track.id).toString());
        
        File file = new File("res/Test.txt");
		// 
        multipart.bodyPart(new FormDataBodyPart("track", file,MediaType.MULTIPART_FORM_DATA_TYPE)).bodyPart(new StreamDataBodyPart("track", new FileInputStream(file))).type(new MediaType("multipart", "form-data",
        		Collections.singletonMap(Boundary.BOUNDARY_PARAMETER, Boundary.createBoundary())));
        
//        form.bodyPart(new FileDataBodyPart("file", file, MediaType.MULTIPART_FORM_DATA_TYPE));
        response = trackPath.request(MediaType.APPLICATION_JSON).put(Entity.entity(multipart, multipart.getMediaType()));
        assertEquals(200, response.getStatus());
        
        response = trackPath.request(MediaType.APPLICATION_JSON).get();
        assertEquals(200, response.getStatus());
        List<Track> tracks = response.readEntity(new GenericType<List<Track>>(){});
        for (Track track2 : tracks) {
			if(track2.id == track.id) {
//				assertEquals(track.file_ref,"test.txt");
				assertEquals(track2.upload_state,1);
				assertEquals(track2.fileName,"track");
				assertTrue(track2.uploadDate.before(Calendar.getInstance().getTime()));
				break;
			}
		}
        
        WebTarget deletePath = trackPath.path("/" + new Long(track.id).toString()); //$NON-NLS-1$
        response = deletePath.request().delete();
        assertEquals(204, response.getStatus());
        
		return;
		
	}
}
