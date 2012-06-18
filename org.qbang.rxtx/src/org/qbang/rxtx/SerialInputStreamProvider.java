/**
 * 
 Copyright (c) 2010-2012, Jens K�bler All rights reserved.
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
package org.qbang.rxtx;

import gnu.io.SerialPort;

import java.io.IOException;
import java.io.InputStream;

import net.sf.seesea.services.navigation.provider.INMEAStreamProvider;

/**
 * 
 */
public class SerialInputStreamProvider implements INMEAStreamProvider {

	private final SerialPort serialPort;

	/**
	 * 
	 */
	public SerialInputStreamProvider(SerialPort commPort) {
		this.serialPort = commPort;
	}
	
	/* (non-Javadoc)
	 * @see net.sf.seesea.services.navigation.provider.INMEAStreamProvider#getInputStream()
	 */
	@Override
	public InputStream getInputStream() throws IOException {
		return serialPort.getInputStream();
	}

	@Override
	public String getName() {
		return serialPort.getName();
	}

	@Override
	public void close() throws IOException {
		serialPort.getInputStream().close();
		serialPort.close();
	}

}
