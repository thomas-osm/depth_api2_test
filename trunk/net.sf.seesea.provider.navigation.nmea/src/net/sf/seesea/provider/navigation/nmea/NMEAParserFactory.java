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

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import net.sf.seesea.services.navigation.provider.INMEAStreamProvider;

import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.ComponentInstance;

/**
 * This factory creates new NMEA readers as soon as a {@link INMEAStreamProvider} becomes available.
 * The input for the readers is processed in another thread that is stopped when the stream provider becomes unavailable
 * 
 * @author jens
 *
 */
public class NMEAParserFactory {
	
	private ComponentFactory componentFactory;

	private final Map<INMEAStreamProvider,Future<Void>> map;
	private final Map<INMEAStreamProvider,NMEAEventListener> nmeaEventListeners;
	
	public NMEAParserFactory() {
		map = Collections.synchronizedMap(new HashMap<INMEAStreamProvider, Future<Void>>());
		nmeaEventListeners = Collections.synchronizedMap(new HashMap<INMEAStreamProvider, NMEAEventListener>());
	}
	
	public synchronized void bind(ComponentFactory componentFactory) {
		this.componentFactory = componentFactory;
	}

	public synchronized void unbind(ComponentFactory componentFactory) {
		this.componentFactory = componentFactory;
	}

	/**
	 * a new stream source becomes available. a reader consumes this one 
	 * 
	 * @param nmeaStreamProvider
	 */
	public synchronized void bind(final INMEAStreamProvider nmeaStreamProvider) {
		 FutureTask<Void> futureTask = new FutureTask<Void>(new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				ComponentInstance componentInstance = componentFactory.newInstance(new Properties());
				final NMEA0183Reader nmeaReader = new NMEA0183Reader(nmeaStreamProvider.getInputStream());
				NMEAEventListener instance = (NMEAEventListener) componentInstance.getInstance();
				nmeaEventListeners.put(nmeaStreamProvider, instance);
				nmeaReader.addNMEAEventListener(instance);
				try {
					nmeaReader.readLine();
				} catch (Exception e) {
					try {
						nmeaReader.unregisterService();
						instance.disable();
						componentInstance.dispose();
//						nmeaReader.removeNMEAEventListener(instance);
						Logger.getRootLogger().error("Failed to process nmea data", e);
						nmeaStreamProvider.close();
						Logger.getRootLogger().error("Stream provider successfully closed", e);
					} catch (IOException e1) {
						Logger.getRootLogger().error("Failed to close input stream", e);
					}
					throw new NMEAProcessingException(e);
				}
				return null;
			}
		});
			
		 ExecutorService es = Executors.newSingleThreadExecutor ();
		 Future<Void> submit = (Future<Void>) es.submit (futureTask);
		map.put(nmeaStreamProvider, submit);
	}
	
	public synchronized void unbind(INMEAStreamProvider nmeaStreamProvider) {
		Future<Void> task = map.get(nmeaStreamProvider);
		if(task != null) {
			task.cancel(true);
		}
		if(nmeaEventListeners.get(nmeaStreamProvider) != null) {
			nmeaEventListeners.get(nmeaStreamProvider).disable();
		}
 		map.remove(nmeaStreamProvider);
	}
	
		
}
