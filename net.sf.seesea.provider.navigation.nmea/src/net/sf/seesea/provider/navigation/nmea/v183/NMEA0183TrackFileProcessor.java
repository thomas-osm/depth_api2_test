package net.sf.seesea.provider.navigation.nmea.v183;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;

import net.sf.seesea.model.core.physx.Measurement;
import net.sf.seesea.provider.navigation.nmea.NMEA0183MessageTypes;
import net.sf.seesea.track.api.IMeasurmentProcessor;
import net.sf.seesea.track.api.ITrackFileProcessor;
import net.sf.seesea.track.api.data.ITrackFile;
import net.sf.seesea.track.api.data.SensorDescription;
import net.sf.seesea.track.api.data.SensorDescriptionUpdateRate;
import net.sf.seesea.track.api.exception.InputStreamNotFoundException;
import net.sf.seesea.track.api.exception.ProcessingException;

@Component(factory = "trackfile.x-nmea0183")
public class NMEA0183TrackFileProcessor implements ITrackFileProcessor {

	private NMEA0183Reader nmea0183Reader;
	private IMeasurmentProcessor measurmentProcessor;

	public NMEA0183TrackFileProcessor() {
		EnumSet<NMEA0183MessageTypes> filterForMessageTypes = EnumSet.of(NMEA0183MessageTypes.RMC, NMEA0183MessageTypes.GLL, NMEA0183MessageTypes.GGA, NMEA0183MessageTypes.DBK, NMEA0183MessageTypes.DBT, NMEA0183MessageTypes.DBS, NMEA0183MessageTypes.DPT, NMEA0183MessageTypes.ZDA);
		Map<NMEA0183MessageTypes,Set<String>> filter = new HashMap<NMEA0183MessageTypes, Set<String>>();
		for (NMEA0183MessageTypes nmea0183MessageTypes : filterForMessageTypes) {
			filter.put(nmea0183MessageTypes, Collections.<String>emptySet());
		}
		nmea0183Reader = new NMEA0183Reader(filter, true);
	}
	
	@Override
	public void processFile(ITrackFile recordedFile) throws FileNotFoundException,
			IOException, ProcessingException {
//		System.out.println("Reading file " + recordedFile.getFileReference());
		try (			InputStream inputStream = recordedFile.getInputStream();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "ISO646-US"));){
			
			String line = null;
			while((line = bufferedReader.readLine()) != null) {
				List<Measurement> results = null;
				List<Measurement> extractedMeasurments = new ArrayList<Measurement>(1);
				try {
					results = nmea0183Reader.extractMeasurementsFromNMEA(line, extractedMeasurments);
				} catch (IllegalArgumentException e) {
					Logger.getLogger(getClass()).error("Failed to parse " + line + " from " + recordedFile.getTrackId());
				} catch (StringIndexOutOfBoundsException e) {
					Logger.getLogger(getClass()).error("Failed to parse " + line + " from " + recordedFile.getTrackId());
				}
				if(results !=null && !results.isEmpty()) {
					int startOfMessage = line.indexOf("$"); //$NON-NLS-1$
					measurmentProcessor.processMeasurements(results, line.substring(startOfMessage + 3, startOfMessage + 6), recordedFile);
				}
				int i = 0;
			}
		} catch (EOFException e) {
			throw new ProcessingException(e);
		} catch (InputStreamNotFoundException e) {
			throw new ProcessingException(e);
		}
	}

	@Override
	public void setMeasurementProcessor(IMeasurmentProcessor measurmentProcessor) {
		this.measurmentProcessor = measurmentProcessor;
	}

	@Override
	public void setFilter(Set<SensorDescriptionUpdateRate<Measurement>> bestSensors) throws ProcessingException {
		try {
			Map<NMEA0183MessageTypes,Set<String>> filter = new HashMap<NMEA0183MessageTypes, Set<String>>();
			for (SensorDescription<Measurement> sensorDescription : bestSensors) {
				Set<String> sensorIds = new HashSet<String>();
				sensorIds.add(sensorDescription.getSensorID());
				filter.put(NMEA0183MessageTypes.valueOf(sensorDescription.getMessageType()), sensorIds);
			}
			nmea0183Reader = new NMEA0183Reader(filter, true);
		} catch (IllegalArgumentException e) {
			throw new ProcessingException("Failed to set filter", e);
		}
	}

	@Override
	public boolean hasRelativeTime() {
		return true;
	}

	@Override
	public boolean hasAbsoluteTime() {
		return true;
	}

}
