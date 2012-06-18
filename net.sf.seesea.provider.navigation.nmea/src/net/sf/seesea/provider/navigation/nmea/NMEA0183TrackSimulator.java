/**
 * 
Copyright (c) 2010-2012, Jens Kübler
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
package net.sf.seesea.provider.navigation.nmea;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.ComponentInstance;

public class NMEA0183TrackSimulator implements Runnable {

	private final List<InputStream> inputStream;

	public NMEA0183TrackSimulator(List<InputStream> fileInputStream) {
		inputStream = fileInputStream;
	}

	public void run() {
		NMEA0183Parser parser = null;
		try {
			String currentLine = ""; //$NON-NLS-1$

			BundleContext bundleContext = NMEA0183Activator.getDefault()
					.getBundle().getBundleContext();
			ServiceReference<ComponentFactory> serviceReference = bundleContext
					.getServiceReference(ComponentFactory.class);
			ComponentFactory componentFactory = bundleContext
					.getService(serviceReference);
			Properties properties = new Properties();
			// properties.put(configuration, arg1)
			ComponentInstance instance = componentFactory
					.newInstance(properties);
			parser = (NMEA0183Parser) instance.getInstance();

			for (InputStream stream : inputStream) {
				InputStreamReader inputStreamReader = new InputStreamReader(stream);
				BufferedReader in = new BufferedReader(inputStreamReader);
				currentLine = ""; //$NON-NLS-1$
				while (currentLine != null) {
					currentLine = in.readLine();
					if (currentLine != null) {
						parser.receiveNMEAEvent(new NMEAEvent(currentLine));
						Thread.sleep(10);
					}
				}
			}
			parser.disable();
			instance.dispose();
		} catch (IOException e) {
			Logger.getLogger(NMEA0183TrackSimulator.class).debug("IO Exception while simulate track", e); //$NON-NLS-1$
		 } catch (InterruptedException e) {
			 if(parser != null) {
				 parser.disable();
			 }
			 Logger.getLogger(NMEA0183TrackSimulator.class).debug("Simulator interrupted", e); //$NON-NLS-1$
		 }

		// return track;
	}

}
