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
package net.sf.seesea.provider.navigation.nmea.v2000;

import java.util.Calendar;

import net.sf.seesea.model.core.geo.Depth;
import net.sf.seesea.model.core.geo.GeoFactory;
import net.sf.seesea.model.core.geo.GeoPosition3D;
import net.sf.seesea.model.core.geo.RelativeDepthMeasurementPosition;
import net.sf.seesea.provider.navigation.nmea.NMEAParser;
import net.sf.seesea.provider.navigation.nmea.v2000.data.Integrety;
import net.sf.seesea.provider.navigation.nmea.v2000.data.MethodGNSS;
import net.sf.seesea.provider.navigation.nmea.v2000.data.TypeOfSystem;

public class NMEA2000Parser extends NMEAParser {

	@Override
	protected String getProviderName() {
		return "NMEA2000"; //$NON-NLS-1$
	}
	
	public NMEA2000Parser() {
	}
	
	private Depth readWaterDepth(byte[] frameData) {
		long sequenceID = byteToUnsignedIntBE(frameData, 0, 1);
		long waterDepthTransducer = byteToUnsignedIntBE(frameData, 1, 5);
		long transducerOffset = byteToLong(frameData, 5, 7);
		
		Depth depth = GeoFactory.eINSTANCE.createDepth();
		depth.setTime(Calendar.getInstance().getTime()); // may be read it from can
		if(transducerOffset == 0) {
			depth.setMeasurementPosition(RelativeDepthMeasurementPosition.TRANSDUCER);
		} else if(transducerOffset > 0) {
			depth.setMeasurementPosition(RelativeDepthMeasurementPosition.SURFACE);
		} else {
			depth.setMeasurementPosition(RelativeDepthMeasurementPosition.KEEL);
		}
		
		depth.setDepth(((double)waterDepthTransducer) / 100 + ((double)transducerOffset) / 1000);
		
		return depth;
	}

	private GeoPosition3D readGNSSPositionData(byte[] frameData) {
		long sequenceID = byteToUnsignedIntBE(frameData, 0, 1);
		long positionDate = byteToUnsignedIntBE(frameData, 1, 3);
		long positionTimeInSeconds = byteToUnsignedIntBE(frameData, 3, 7);
		long latitude = byteToLong(frameData, 7, 15);
		long longitude = byteToLong(frameData, 15, 23);
		long altitude = byteToLong(frameData, 15, 23);
		int typeOfSystemIndex = extractHighNibbleBE(frameData[23]);
		TypeOfSystem typeOfSystem = (TypeOfSystem.values())[typeOfSystemIndex];
		int methodIndex = extractLowNibble(frameData[23]);;
		MethodGNSS methodGNSS = MethodGNSS.getByIndex(methodIndex);

		int integretyIndex = extractBitField(frameData[24], 0, 2);
		Integrety integrety = (Integrety.values())[integretyIndex];
		
		long numberOfSV = byteToUnsignedIntBE(frameData, 24, 25);

		long hdop = byteToLong(frameData, 25, 27);
		long pdop = byteToLong(frameData, 27, 29);
		long geoidalSeparation = byteToLong(frameData, 29, 33);

		long numberOfReferenceStations = byteToUnsignedIntBE(frameData, 33, 34);
		for (int i = 0; i < numberOfReferenceStations; i ++) {
			// FIXME multiple reference stations
//		int referenceStationIndex = extractHighNibble(frameData[34]);
//		// TODO
//		// TODO 2
//		long ageOfDGNSSCorrections = byteToUnsignedInt(frameData, 36, 38);
//
//		int referenceStationIndex = extractHighNibble(frameData[34]);
		}
		
		return null;
	}

	private int extractBitField(byte b, int fromIndex, int toIndex) {

		int value =  b >> (8 - toIndex);
		return value;
//		for(int i = fromIndex; i < toIndex; i++) {
//			1 << i - fromIndex;
//		}
		
//		0x00000000;
//		0x00000001;
//		0x00000002;
//		0x00000003;
//		0x00000007;
//		0x0000000F;
//		0x00000010;
//		0x00000020;
//		0x00000030;
//		0x00000070;
//		0x000000F0;
		
	}
	
	private int extractLowNibble(byte b) {
		return b & 0x0000000F;
	}
	
	private int extractHighNibbleBE(byte b) {
		return b & 0x000000F0;  
	}

	private long byteToUnsignedIntBE(byte[] buf, int fromIndex, int toIndex) {
        long[] bytesAsInt = new long[toIndex - fromIndex];
        long anUnsignedInt = 0;
        
        for (int i = fromIndex ; i < toIndex; i++) {
        	bytesAsInt[i - fromIndex] = (0x000000FF & ((int)buf[i])) << (8 * (toIndex - i - 1));
        	anUnsignedInt |= bytesAsInt[i - fromIndex]; 
		}
        
        anUnsignedInt &= 0xFFFFFFFFL;
        
//		firstByte = (0x000000FF & ((int)buf[index]));
//        secondByte = (0x000000FF & ((int)buf[index+1]));
//        thirdByte = (0x000000FF & ((int)buf[index+2]));
//        fourthByte = (0x000000FF & ((int)buf[index+3]));
//	        index = index+4;
//		long anUnsignedInt = ((long) (firstByte << 24
//		                | secondByte << 16
//	                        | thirdByte << 8
//	                        | fourthByte))
//	                       & 0xFFFFFFFFL;
		
		return anUnsignedInt;
	}
	
	/**
     * Convert the byte array to an int starting from the given offset.
     *
     * @param b The byte array
     * @param offset The array offset
     * @return The integer
     */
    public long byteToLong(byte[] b, int fromIndex, int toIndex) {
    	long value = 0;
        for (int i = fromIndex; i < toIndex; i++) {
            int shift = (4 - 1 - i - fromIndex) * 8;
            value += (b[i + fromIndex] & 0x000000FF) << shift;
        }
        return value;
    }
	
	
	
}
