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
package net.sf.seesea.osm.map;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.sf.seesea.tileservice.IAsynchronouslyUpdateable;
import net.sf.seesea.tileservice.ITileCacheProcessor;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.graphics.Point;

public class OSMCacheProcessor implements ITileCacheProcessor {

	private OpenStreetMapHardDiskTileSource openStreetMapHardDiskTileSource;

	public OSMCacheProcessor(File storageLocation) {
		OpenStreetmapServerTileSource openStreetmapServerTileSource;
		try {
			openStreetmapServerTileSource = new OpenStreetmapServerTileSource(new URL("http://tile.openstreetmap.org/"),0,18); //$NON-NLS-1$
			openStreetMapHardDiskTileSource = new OpenStreetMapHardDiskTileSource(storageLocation, openStreetmapServerTileSource);
		} catch (MalformedURLException e) {
			Logger.getLogger(OSMCacheProcessor.class).error("Failed URL", e); //$NON-NLS-1$
		} 
	}

	public void cacheTiles(Point mercatorPosition, int requiredXTiles, int requiredYTiles, int zoomLevel, IAsynchronouslyUpdateable updateable, IProgressMonitor progressMonitor) {
		List<TileDesription> failedTiles = new ArrayList<TileDesription>(); 
		for (int i = 0; i < requiredXTiles; i++) {
			for (int j = 0; j < requiredYTiles; j++) {
				int xTile = (mercatorPosition.x / 256) + i;
				int yTile = (mercatorPosition.y / 256) + j;
				try {
					if(progressMonitor.isCanceled()) {
						return;
					}
					openStreetMapHardDiskTileSource.getTileURL(xTile, yTile, zoomLevel);
					progressMonitor.worked(1);
				} catch (IOException e) {
					TileDesription tileDesription = new TileDesription(i, j, zoomLevel);
					failedTiles.add(tileDesription);
					Logger logger = Logger.getLogger(OSMCacheProcessor.class);
					logger.warn("Failed to retrieve tile " + tileDesription,  e); //$NON-NLS-1$
				}
			}
		}
		
	}

	@Override
	public IStatus clearCachedTiles() {
		return openStreetMapHardDiskTileSource.clearCache();
	}
	

	
}
