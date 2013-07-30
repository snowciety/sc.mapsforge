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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.view.TiUIView;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.graphics.Color;
import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.util.IOUtils;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.Layer;
import org.mapsforge.map.layer.cache.FileSystemTileCache;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.download.TileDownloadLayer;
import org.mapsforge.map.layer.overlay.Circle;
import org.mapsforge.map.layer.overlay.Marker;
import org.mapsforge.map.layer.overlay.Polygon;
import org.mapsforge.map.layer.overlay.Polyline;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class MapsforgeView extends TiUIView {
	
	private static final String TAG = "MapsforgeView";
	private static final String KEY_DEBUG = "debug";
	private static final String KEY_SCALEBAR = "scalebar";
	private static final String KEY_CENTER = "centerLatlng";
	private static final String KEY_ZOOMLEVEL = "zoomlevel";
	private static final String KEY_MINZOOM = "minZoom";
	private static final String KEY_MAXZOOM = "maxZoom";

    private static final int TIMEOUT_CONNECT = 5000;
    private static final int TIMEOUT_READ = 10000;
    
	private static boolean sDebug = false;

	private GraphicFactory mGraphicFactory;
	private HashMap<String, Layer> mLayers = new HashMap<String, Layer>();
	
	private static void debugMsg(String msg) {
		if (sDebug) {
			Log.d(TAG, msg);
		}
	}

	public MapsforgeView(TiViewProxy proxy) {
		super(proxy);		
		MapView mapView = new MapView(proxy.getActivity());
		this.mGraphicFactory = AndroidGraphicFactory.INSTANCE;
		setNativeView(mapView);
	}
	
	@Override
	public void processProperties(KrollDict props) {
		super.processProperties(props);
				
		if (props.containsKey(KEY_DEBUG)) {
			sDebug = props.getBoolean(KEY_DEBUG);
		}
		debugMsg("processProperties " + props);
		
		MapView mapView = (MapView) getNativeView();
		if (props.containsKey(KEY_SCALEBAR)) {
			mapView.getMapScaleBar().setVisible(props.getBoolean(KEY_SCALEBAR));
			debugMsg("scalebar set to " + (props.getBoolean(KEY_SCALEBAR) ? "visible" : "hidden"));
		}
		
		if (props.containsKey(KEY_MINZOOM)) {
			int zoom = props.getInt(KEY_MINZOOM);
			mapView.getModel().mapViewPosition.setZoomLevelMin((byte) zoom);
			debugMsg("Min zoom level for map view set to " + Integer.toString(zoom));
		}
		
		if (props.containsKey(KEY_MAXZOOM)) {
			int zoom = props.getInt(KEY_MAXZOOM);
			mapView.getModel().mapViewPosition.setZoomLevelMax((byte) zoom);
			debugMsg("Max zoom level for map view set to " + Integer.toString(zoom));
		}
		
		if (props.containsKey(KEY_CENTER)) {
			Object[] coords = (Object[]) props.get(KEY_CENTER);
			double lat = TiConvert.toDouble(coords[0]);
			double lon = TiConvert.toDouble(coords[1]);
			setCenter(lat, lon);
		}
		
		if (props.containsKey(KEY_ZOOMLEVEL)) {
			int zoomlevel = props.getInt(KEY_ZOOMLEVEL);
			setZoomLevel(zoomlevel);
		}
	}
	
	@Override
	public void propertyChanged(String key, Object oldValue, Object newValue,
			KrollProxy proxy) {
		super.propertyChanged(key, oldValue, newValue, proxy);
	}
	
	/**
	 * Adds a tile layer using a FileSystemTileCache to the map view
	 * @param activity	Context of the map view.
	 * @param name	Identifier for the layer.
	 * @param url	URL to the tile source. Must have {z},{x} and {y} place holders.
	 * @param subdomains	Sub domains, if any, for the tile source.
	 * @param parallelrequests	Number of parallel requests the tile source can handle.
	 * @param maxzoom	Highest zoom level for the tile source.
	 * @param minzoom	Lowest zoom level for the tile source.
	 */
	public void addLayer(Activity activity, String name, String url, String[] subdomains,
			int parallelrequests, byte maxzoom, byte minzoom){
		MapView mapView = (MapView) getNativeView();
		GenericTileSource tileSource = new GenericTileSource(url, subdomains, parallelrequests, maxzoom, minzoom);
		TileDownloadLayer downloadLayer = new TileDownloadLayer(createTileCache(activity, name), mapView.getModel().mapViewPosition, tileSource, mGraphicFactory);
		mapView.getLayerManager().getLayers().add(downloadLayer);
		mLayers.put(name, downloadLayer);
		debugMsg("Added layer " + name + " with url " + url);
	}
	
	
	/**
	 * Removes a specific layer from the map view.
	 * @param name	Layer identifier as specified in addLayer().
	 */
	public boolean removeLayer(String name) {
		Layer l = mLayers.get(name);
		if (l != null) {
			MapView mapView = (MapView) getNativeView();
			mapView.getLayerManager().getLayers().remove(l);
			mLayers.remove(name);
			return true;
		} else {
			Log.e(TAG, "Layer with name " + name + " could not be found!");
			return false;
		}
	}
    
    /**
     * Starts all tile layers.
     */
    public void startLayers() {
    	Iterator<Entry<String, Layer>> it = mLayers.entrySet().iterator();
    	while (it.hasNext()) {
    		Entry<String, Layer> pairs = (Entry<String, Layer>) it.next();
    		if (pairs.getValue() instanceof TileDownloadLayer) {
    			((TileDownloadLayer) pairs.getValue()).start();
				debugMsg("Started layer " + pairs.getKey());
    		}
    	}
    }
    
    /**
     * Starts a specific tile layer using its identifier.
     * @param name
     */
    public void startLayer(String name) {
    	Iterator<Entry<String, Layer>> it = mLayers.entrySet().iterator();
    	while (it.hasNext()) {
    		Entry<String, Layer> pairs = (Entry<String, Layer>) it.next();
    		if (pairs.getKey().equals(name)) {
    			if (pairs.getValue() instanceof TileDownloadLayer) {
    				((TileDownloadLayer) pairs.getValue()).start();
    				debugMsg("Started layer " + pairs.getKey());
    				return;
    			}
    		}
    	}
    	Log.e(TAG, "Could not find any layer named " + name + " to start!");
    }
    
    /**
     * Sets the center of the map view.
     * @param lat
     * @param lon
     */
    public void setCenter(double lat, double lon) {
		MapView mapView = (MapView) getNativeView();
    	mapView.getModel().mapViewPosition.setCenter(new LatLong(lat, lon));
		debugMsg("Center for map view set to " + Double.toString(lat) + " " + Double.toString(lon));
    }
    
    /**
     * Sets the zoom level of the map view.
     * @param zoomlevel
     */
    public void setZoomLevel(int zoomlevel) {
		MapView mapView = (MapView) getNativeView();
    	mapView.getModel().mapViewPosition.setZoomLevel((byte) zoomlevel);
		debugMsg("Zoom level for map view set to " + Integer.toString(zoomlevel));
    }
    
    /**
     * Draws a polyline on the map view.
     * @param coordinates
     * @param color
     * @param strokeWidth
     * @return identifier for the object.
     */
    public int createPolyline(List<LatLong> coordinates, Color color, float strokeWidth) {
		Paint paintStroke = mGraphicFactory.createPaint();
		paintStroke.setStyle(Style.STROKE);
		paintStroke.setColor(color);
		paintStroke.setStrokeWidth(strokeWidth);

		Polyline pl = new Polyline(paintStroke,mGraphicFactory);
		pl.getLatLongs().addAll(coordinates);
		MapView mapView = (MapView) getNativeView();
		mapView.getLayerManager().getLayers().add(pl);
		mLayers.put(Integer.toString(pl.hashCode()), pl);
		mapView.getLayerManager().redrawLayers();
		
		return pl.hashCode();
    }
    
    /**
     * Draws a polygon on the map view.
     * @param coordinates
     * @param fillColor
     * @param strokeColor
     * @param strokeWidth
     * @return identifier for the object.
     */
    public int createPolygon(List<LatLong> coordinates, Color fillColor, Color strokeColor, float strokeWidth) {
    	Paint paintFill = mGraphicFactory.createPaint();
    	paintFill.setStyle(Style.FILL);
    	paintFill.setColor(fillColor);
    	
    	Paint paintStroke = mGraphicFactory.createPaint();
    	paintStroke.setStyle(Style.STROKE);
    	paintStroke.setColor(strokeColor);
    	paintStroke.setStrokeWidth(strokeWidth);
    	
    	Polygon pg = new Polygon(paintFill, paintStroke, mGraphicFactory);
    	pg.getLatLongs().addAll(coordinates);
		MapView mapView = (MapView) getNativeView();
    	mapView.getLayerManager().getLayers().add(pg);
    	mLayers.put(Integer.toString(pg.hashCode()), pg);
		mapView.getLayerManager().redrawLayers();

    	return pg.hashCode();
    }
    
    /**
     * Puts a marker on the map view.
     * @param pos
     * @param iconPath	Must be a URL or file system path on the device(i.e '/sdcard/marker.png').
     * @param horizontalOffset
     * @param verticalOffset
     * @param iconWidth
     * @param iconHeight
     * @return identifier for the object.
     */
    public int createMarker(LatLong pos, String iconPath, int horizontalOffset, int verticalOffset, int iconWidth, int iconHeight) {
    	InputStream is = createInputStream(iconPath);
    	if (is == null) {
    		Log.e(TAG, "Unable to retrieve marker image. No marker drawn.");
    		return -1;
    	}
    	
    	Bitmap icon = null;
		try {
			icon = mGraphicFactory.createBitmap(is);
		} catch (IOException e) {
			Log.e(TAG, "Unable to create bitmap. No marker drawn.");
			Log.e(TAG, e.getMessage());
			return -1;
		} finally {
			IOUtils.closeQuietly(is);
		}
		
		if ((iconWidth > 0 && iconHeight > 0) && (iconWidth != icon.getWidth() || iconHeight != icon.getHeight())) {
			icon = resizeBitmap(icon, iconWidth, iconHeight);
		}
		
		Marker m = new Marker(pos, icon, horizontalOffset, verticalOffset);
		MapView mapView = (MapView) getNativeView();
		mapView.getLayerManager().getLayers().add(m);
    	mLayers.put(Integer.toString(m.hashCode()), m);
		mapView.getLayerManager().redrawLayers();

    	return m.hashCode();
    }
    
    /**
     * Draws a circle on the map view.
     * @param latLong
     * @param radius	Note: The radius is in meters!
     * @param fillColor
     * @param strokeColor
     * @param strokeWidth
     * @return identifier for the object.
     */
    public int createCircle(LatLong latLong, float radius, Color fillColor, Color strokeColor, float strokeWidth) {
    	Paint paintFill = mGraphicFactory.createPaint();
    	paintFill.setColor(fillColor);
    	paintFill.setStyle(Style.FILL);
    	
    	Paint paintStroke = mGraphicFactory.createPaint();
    	paintStroke.setColor(strokeColor);
    	paintStroke.setStrokeWidth(strokeWidth);
    	paintStroke.setStyle(Style.STROKE);
    	
    	Circle c = new Circle(latLong, radius, paintFill, paintStroke);
		MapView mapView = (MapView) getNativeView();
    	mapView.getLayerManager().getLayers().add(c);
    	mLayers.put(Integer.toString(c.hashCode()), c);
		mapView.getLayerManager().redrawLayers();

    	return c.hashCode();
    }
    
    private TileCache createTileCache(Activity activity, String name) {
        String cacheDirectoryName = activity.getExternalCacheDir().getAbsolutePath() + File.separator + name;
        File cacheDirectory = new File(cacheDirectoryName);
        if (!cacheDirectory.exists()) {
                cacheDirectory.mkdir();
        }
        return new FileSystemTileCache(1024, cacheDirectory, mGraphicFactory);
    }
    
    private static URLConnection getURLConnection(URL url) throws IOException {
        URLConnection urlConnection = url.openConnection();
        urlConnection.setConnectTimeout(TIMEOUT_CONNECT);
        urlConnection.setReadTimeout(TIMEOUT_READ);
        return urlConnection;
    }
    
    private static InputStream createInputStream(String iconPath) {
    	InputStream is = null;
     	if (iconPath.startsWith("www")) {
    		//Add "http://"
    		iconPath = "http://" + iconPath;
    	}
    	
    	if (iconPath.startsWith("http")) {
    		URL url = null;
			try {
				url = new URL(iconPath);
    			URLConnection conn = getURLConnection(url);
    			is = conn.getInputStream();
			} catch (MalformedURLException e1) {
				Log.e(TAG, "The URL is malformed.");
				Log.e(TAG, e1.getMessage());
				is = null;
			} catch (IOException e) {
    			Log.e(TAG, "Could not load file from " + url.toString());
    			Log.e(TAG, e.getMessage());
    			is = null;
    		}
    	} else {
    		//Should be a file system path
    		File f = new File(iconPath);
    		try {
    			is = new FileInputStream(f);
    		} catch (FileNotFoundException e) {
    			Log.e(TAG, "File not found.");
    			Log.e(TAG, e.getMessage());
    			is = null;
    		}
    	}
    	return is;
    }
    
    private static Bitmap resizeBitmap(Bitmap bitmap, int width, int height) {
		/*
		 * TODO Is this really the easiest way to resize a bitmap
		 *  when using mapsforge?
		 */
		debugMsg(String.format("Resizing bitmap from %dx%d to %dx%d", bitmap.getWidth(), bitmap.getHeight(), width, height));
		android.graphics.Bitmap aBitmap = AndroidGraphicFactory.getBitmap(bitmap);
		android.graphics.Bitmap scaled = android.graphics.Bitmap.createScaledBitmap(aBitmap, width, height, false);
		Drawable d = new BitmapDrawable(scaled);
		bitmap = AndroidGraphicFactory.convertToBitmap(d);
		//Bitmaps are evil, null references to ensure they get GC'd
		d = null;
		scaled = null;
		aBitmap = null;
		return bitmap;
    }
}
