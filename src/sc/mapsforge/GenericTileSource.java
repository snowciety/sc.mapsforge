/*
 * Copyright 2013 Snowciety
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
 package sc.mapsforge;

import java.net.MalformedURLException;
import java.net.URL;

import org.mapsforge.core.model.Tile;
import org.mapsforge.map.layer.download.tilesource.TileSource;

public class GenericTileSource implements TileSource {
	
	private String[] mSubdomains = { "a", "b", "c" };
	private int mSubdomainPointer = 0;
	private String mUrl;
	private int mRequests;
	private byte mMaxzoom;
	private byte mMinzoom;
	
	public GenericTileSource(String url, String[] subdomains, int parallelrequests, byte maxzoom, byte minzoom) {
		if (subdomains != null ) {
			this.mSubdomains = subdomains;
		}
		
		this.mUrl = url;
		this.mMaxzoom = maxzoom;
		this.mMinzoom = minzoom;
		this.mRequests = parallelrequests;
	}

	@Override
	public int getParallelRequestsLimit() {
		return mRequests;
	}

	@Override
	public URL getTileUrl(Tile tile) throws MalformedURLException {
		String fUrl = this.mUrl;
		//Match to see if URL has subdomain identifier
		//If so, replace is with a subdomain
		if (fUrl.matches(".*\\{s\\}.*")) {
			int position = (mSubdomainPointer) % (mSubdomains.length);
			mSubdomainPointer++;
			fUrl = fUrl.replaceAll("\\{s\\}", mSubdomains[position]);
		}

		fUrl = fUrl.replaceAll("\\{z\\}", Byte.toString(tile.zoomLevel));
		fUrl = fUrl.replaceAll("\\{x\\}", Long.toString(tile.tileX));
		fUrl = fUrl.replaceAll("\\{y\\}", Long.toString(tile.tileY));
		
		return new URL(fUrl);
	}

	@Override
	public byte getZoomLevelMax() {
		return mMaxzoom;
	}

	@Override
	public byte getZoomLevelMin() {
		return mMinzoom;
	}
	
	@Override
	public int hashCode() {
	         final int prime = 31;
	         int result = 1;
	         result = prime * result + this.mUrl.hashCode();
	         return result;
	 }
}
