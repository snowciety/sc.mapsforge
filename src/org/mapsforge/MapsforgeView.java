package org.mapsforge;

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

public class MapsforgeView extends TiUIView {
	
	private static final String TAG = "MapsforgeView";
	private static final String KEY_DEBUG = "debug";
	private static final String KEY_SCALEBAR = "scalebar";
	private static final String KEY_CENTER = "center";
	private static final String KEY_ZOOMLEVEL = "zoomlevel";

    private static final int TIMEOUT_CONNECT = 5000;
    private static final int TIMEOUT_READ = 10000;
    
	private static boolean sDebug = false;

	private MapView mMap;
	private GraphicFactory mGraphicFactory;
	private HashMap<String, TileDownloadLayer> mLayers = new HashMap<String, TileDownloadLayer>();
	
	private static void debugMsg(String msg) {
		if (sDebug) {
			Log.d(TAG, msg);
		}
	}

	public MapsforgeView(TiViewProxy proxy) {
		super(proxy);		
		this.mMap = new MapView(proxy.getActivity());
		this.mGraphicFactory = AndroidGraphicFactory.INSTANCE;
		setNativeView(mMap);
		debugMsg("MapView created");
	}
	
	@Override
	public void processProperties(KrollDict props) {
		super.processProperties(props);
				
		if (props.containsKey(KEY_DEBUG)) {
			sDebug = props.getBoolean(KEY_DEBUG);
		}
		
		debugMsg("processProperties " + props);
		
		if (props.containsKey(KEY_SCALEBAR)) {
			mMap.getMapScaleBar().setVisible(props.getBoolean(KEY_SCALEBAR));
			debugMsg("scalebar set to " + (props.getBoolean(KEY_SCALEBAR) ? "visible" : "hidden"));
		}
		
		if (props.containsKey(KEY_CENTER)) {
			Object[] coords = (Object[]) props.get(KEY_CENTER);
			double lat = TiConvert.toDouble(coords[0]);
			double lon = TiConvert.toDouble(coords[1]);
			setCenter(lat, lon);
		}
		
		if (props.containsKey(KEY_ZOOMLEVEL)) {
			byte zoomlevel = Byte.valueOf(props.getString(KEY_ZOOMLEVEL));
			setZoomLevel(zoomlevel);
		}
	}
	
	@Override
	public void propertyChanged(String key, Object oldValue, Object newValue,
			KrollProxy proxy) {
		super.propertyChanged(key, oldValue, newValue, proxy);
	}
	
	/**
	 * Adds a tile layer to the map view
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
		GenericTileSource tileSource = new GenericTileSource(url, subdomains, parallelrequests, maxzoom, minzoom);
		TileDownloadLayer downloadLayer = new TileDownloadLayer(createTileCache(activity, name), mMap.getModel().mapViewPosition, tileSource, mGraphicFactory);
		mMap.getLayerManager().getLayers().add(downloadLayer);
		mLayers.put(name, downloadLayer);
		debugMsg("Added layer " + name + " with url " + url);
	}
	
	
	/**
	 * Removes a specific layer from the map view.
	 * @param name	Layer identifier as specified in addLayer().
	 */
	public void removeLayer(String name) {
		Layer l = mLayers.get(name);
		if (l != null) {
			mMap.getLayerManager().getLayers().remove(l);
			mLayers.remove(name);
		} else {
			Log.e(TAG, "Layer with name " + name + " could not be found!");
		}
	}
    
    /**
     * Starts all tile layers.
     */
    public void startLayers() {
    	Iterator<Entry<String, TileDownloadLayer>> it = mLayers.entrySet().iterator();
    	while (it.hasNext()) {
    		Entry<String, TileDownloadLayer> pairs = (Entry<String, TileDownloadLayer>) it.next();
    		pairs.getValue().start();
			debugMsg("Started layer " + pairs.getKey());
    	}
    }
    
    /**
     * Starts a specific tile layer using its identifier.
     * @param name
     */
    public void startLayer(String name) {
    	Iterator<Entry<String, TileDownloadLayer>> it = mLayers.entrySet().iterator();
    	while (it.hasNext()) {
    		Entry<String, TileDownloadLayer> pairs = (Entry<String, TileDownloadLayer>) it.next();
    		if (pairs.getKey().equals(name)) {
    			pairs.getValue().start();
    			debugMsg("Started layer " + pairs.getKey());
    			return;
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
    	mMap.getModel().mapViewPosition.setCenter(new LatLong(lat, lon));
		debugMsg("center set to " + Double.toString(lat) + " " + Double.toString(lon));
    }
    
    /**
     * Sets the zoom level of the map view.
     * @param zoomlevel
     */
    public void setZoomLevel(byte zoomlevel) {
    	mMap.getModel().mapViewPosition.setZoomLevel(zoomlevel);
		debugMsg("zoomlevel set to " + Byte.toString(zoomlevel));
    }
    
    /**
     * Draws a polyline on the map view.
     * @param coordinates
     * @param color
     * @param strokeWidth
     */
    public void drawPolyline(List<LatLong> coordinates, Color color, float strokeWidth) {
		Paint paintStroke = mGraphicFactory.createPaint();
		paintStroke.setStyle(Style.STROKE);
		paintStroke.setColor(color);
		paintStroke.setStrokeWidth(strokeWidth);

		Polyline pl = new Polyline(paintStroke,mGraphicFactory);
		pl.getLatLongs().addAll(coordinates);
		mMap.getLayerManager().getLayers().add(pl);
    }
    
    /**
     * Draws a polygon on the map view.
     * @param coordinates
     * @param fillColor
     * @param strokeColor
     * @param strokeWidth
     */
    public void drawPolygon(List<LatLong> coordinates, Color fillColor, Color strokeColor, float strokeWidth) {
    	Paint paintFill = mGraphicFactory.createPaint();
    	paintFill.setStyle(Style.FILL);
    	paintFill.setColor(fillColor);
    	
    	Paint paintStroke = mGraphicFactory.createPaint();
    	paintStroke.setStyle(Style.STROKE);
    	paintStroke.setColor(strokeColor);
    	paintStroke.setStrokeWidth(strokeWidth);
    	
    	Polygon pg = new Polygon(paintFill, paintStroke, mGraphicFactory);
    	pg.getLatLongs().addAll(coordinates);
    	mMap.getLayerManager().getLayers().add(pg);
    }
    
    /**
     * Puts a marker on the map view.
     * @param pos
     * @param iconPath	Must be a URL or file system path on the device(i.e '/sdcard/marker.png').
     * @param horizontalOffset
     * @param verticalOffset
     */
    public void drawMarker(LatLong pos, String iconPath, int horizontalOffset, int verticalOffset) {
    	InputStream is = createInputStream(iconPath);
    	if (is == null) {
    		Log.e(TAG, "Unable to retrieve marker image. No marker drawn.");
    		return;
    	}
    	
    	Bitmap icon = null;
		try {
			icon = mGraphicFactory.createBitmap(is);
		} catch (IOException e) {
			Log.e(TAG, "Unable to create bitmap. No marker drawn.");
			Log.e(TAG, e.getMessage());
			return;
		} finally {
			IOUtils.closeQuietly(is);
		}
		
		Marker m = new Marker(pos, icon, horizontalOffset, verticalOffset);
		mMap.getLayerManager().getLayers().add(m);
    }
    
    /**
     * Draws a circle on the map view.
     * @param latLong
     * @param radius	Note: The radius is in meters!
     * @param fillColor
     * @param strokeColor
     * @param strokeWidth
     */
    public void drawCircle(LatLong latLong, float radius, Color fillColor, Color strokeColor, float strokeWidth) {
    	Paint paintFill = mGraphicFactory.createPaint();
    	paintFill.setColor(fillColor);
    	paintFill.setStyle(Style.FILL);
    	
    	Paint paintStroke = mGraphicFactory.createPaint();
    	paintStroke.setColor(strokeColor);
    	paintStroke.setStrokeWidth(strokeWidth);
    	paintStroke.setStyle(Style.STROKE);
    	
    	Circle c = new Circle(latLong, radius, paintFill, paintStroke);
    	mMap.getLayerManager().getLayers().add(c);
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
}
