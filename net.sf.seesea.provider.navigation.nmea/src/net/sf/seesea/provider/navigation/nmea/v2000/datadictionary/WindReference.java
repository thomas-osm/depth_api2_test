/**
 * 
Copyright (c) 2010-2012, Jens K�bler
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
package net.sf.seesea.provider.navigation.nmea.v2000.datadictionary;

import net.sf.seesea.provider.navigation.nmea.v2000.data.TypeOfSystem;

public enum WindReference {
	
	TheoreticalWindRefTrueNorth(0), 
	TheoreticalWindRefMagneticNorth(1), 
	ApparentWind(2), 
	TheoreticalRefGround(3), 
	TheoreticalRefWater(4), 
	Error(6), 
	Null(7);
	
	private final int value;

	private WindReference(int value) {
		this.value = value;
	}

	public static WindReference of(int linkType) {

	    switch (linkType) {
	        case 0: return TheoreticalWindRefTrueNorth;
	        case 1: return TheoreticalWindRefMagneticNorth;
	        case 2: return ApparentWind;
	        case 3: return TheoreticalRefGround;
	        case 4: return TheoreticalRefWater;
	        case 6: return Error;
	        case 7: return Null;

	        //ETC....

	        default: return Null;

	    }
	}


}
