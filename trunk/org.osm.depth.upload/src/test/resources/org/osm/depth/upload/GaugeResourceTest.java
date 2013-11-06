package org.osm.deoth.upload;

import java.io.FileNotFoundException;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import junit.framework.TestCase;

import org.eclipse.persistence.jaxb.rs.MOXyJsonProvider;
import org.glassfish.jersey.client.filter.HttpBasicAuthFilter;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.osm.depth.upload.messages.Gauge;
import org.osm.depth.upload.messages.GaugeType;
import org.osm.depth.upload.messages.License;

public class GaugeResourceTest extends TestCase {
	
	public void testCRUDRoundTrip() throws FileNotFoundException {
		Client client = ClientBuilder.newClient();
		client.register(new HttpBasicAuthFilter("x", "x"));
		client.register(MOXyJsonProvider.class);
		client.register(MultiPartFeature.class);

		LoggingFilter loggingFilter = new LoggingFilter();
		client.register(loggingFilter);
		
		WebTarget basePath = client.target("http://localhost:8080").path("org.osm.depth.upload").path("api2");
		WebTarget path = basePath.path("gauge");
		
		Gauge gauge = new Gauge();
		gauge.name = "Maxau";
		gauge.latitude = 49.0;
		gauge.longitude = 8.0;
		gauge.gaugeType = GaugeType.RIVER;
		
		Response response = path.request(MediaType.APPLICATION_JSON).post(Entity.entity(gauge, MediaType.APPLICATION_JSON));
		assertEquals(200, response.getStatus());
		
        response = path.request(MediaType.APPLICATION_JSON).get();
        assertEquals(200, response.getStatus());
        List<Gauge> gauges = response.readEntity(new GenericType<List<Gauge>>(){});
        long gaugeId = 0;
        for (Gauge gauge2 : gauges) {
			assertEquals(gauge2.name,gauge.name);
			assertEquals(gauge2.latitude,gauge.latitude);
			assertEquals(gauge2.longitude,gauge.longitude);
			assertEquals(gauge2.gaugeType,gauge.gaugeType);
			assertNotSame(0, gauge2.id);
			gaugeId = gauge2.id;
		}
        
        WebTarget deletePath = path.path("/" + new Long(gaugeId).toString()); //$NON-NLS-1$
        response = deletePath.request().delete();
        assertEquals(200, response.getStatus());

        response = path.request(MediaType.APPLICATION_JSON).get();
        assertEquals(200, response.getStatus());
        gauges = response.readEntity(new GenericType<List<Gauge>>(){});
        assertTrue(gauges.isEmpty());
        
		return;
		
	}
}
