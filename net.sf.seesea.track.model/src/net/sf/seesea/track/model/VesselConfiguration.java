/**
Copyright (c) 2013-2015, Jens Kübler
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
package net.sf.seesea.track.model;

import java.util.HashMap;
import java.util.Map;

import net.sf.seesea.geometry.impl.Point3D;

public class VesselConfiguration {
	
	private Map<String, Point3D> gpsSensorOffsets;
	
	private Map<String, Point3D> depthSensorOffsets;
	
	public VesselConfiguration() {
		gpsSensorOffsets = new HashMap<String, Point3D>();
		depthSensorOffsets = new HashMap<String, Point3D>();
	}
	
	public VesselConfiguration(Map<String, Point3D> gpsSensorOffsets,
			Map<String, Point3D> depthSensorOffsets) {
		super();
		this.gpsSensorOffsets = gpsSensorOffsets;
		this.depthSensorOffsets = depthSensorOffsets;
	}



	public Map<String, Point3D> getGpsSensorOffsets() {
		return gpsSensorOffsets;
	}

	public Map<String, Point3D> getDepthSensorOffsets() {
		return depthSensorOffsets;
	}
	
}
