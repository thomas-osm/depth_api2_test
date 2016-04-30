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

/**
 * A validator removes valid checksums from nmea strings  
 */
public class NMEAMessageValidator {

	/**
	 * checks the checksum and additionally cuts off the checksum. This is not a sideffect free method
	 * 
	 * @param rawContent
	 * @return
	 */
	protected boolean xremoveValidChecksum(String nmeaMessageContent) {
		if(!nmeaMessageContent.startsWith("$")) { //$NON-NLS-1$
			return false;
		}
		try {
			String rawContent = nmeaMessageContent.substring(1,
					nmeaMessageContent.length());
			int checksumIndex = rawContent.lastIndexOf("*"); //$NON-NLS-1$
			if(checksumIndex != -1) {
				String checksumString = rawContent.substring(checksumIndex + 1, checksumIndex + 3);
				try {
					int checkSum = Integer.parseInt(checksumString, 16);
					
					rawContent = rawContent.substring(0, checksumIndex);
					char calculatedChecksum = 0;
					for (int i = 0; i < rawContent.length(); i++) {
						calculatedChecksum ^= rawContent.charAt(i);
					}
					if(((char)checkSum) == calculatedChecksum) {
						return true;
					}
				} catch (NumberFormatException e) {
					return false;
				}
			}
			return false;
		} catch (StringIndexOutOfBoundsException e) {
			return false;
		}
	}

	
}
