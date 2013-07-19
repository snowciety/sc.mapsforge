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
	private static final String KEY_SCALEBAR = "scalebar";
	private static final String KEY_CENTER = "center";
	private static final String KEY_ZOOMLEVEL = "zoomlevel";

    private static final int TIMEOUT_CONNECT = 5000;
    private static final int TIMEOUT_READ = 10000;

	private MapView mMap; //TODO Destroy this reference when View is destroyed!
	private GraphicFactory mGraphicFactory;
	private HashMap<String, TileDownloadLayer> mLayers = new HashMap<String, TileDownloadLayer>();

	public MapsforgeView(TiViewProxy proxy) {
		super(proxy);		
		this.mMap = new MapView(proxy.getActivity());
		this.mGraphicFactory = AndroidGraphicFactory.INSTANCE;
		setNativeView(mMap);
		Log.d(TAG, "MapView created");
	}
	
	@Override
	public void processProperties(KrollDict props) {
		super.processProperties(props);
		
		Log.d(TAG, "processProperties " + props);
				
		if (props.containsKey(KEY_SCALEBAR)) {
			mMap.getMapScaleBar().setVisible(props.getBoolean(KEY_SCALEBAR));
			Log.d(TAG, "scalebar set to " + (props.getBoolean(KEY_SCALEBAR) ? "visible" : "hidden"));
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
		
		if (key.equals(KEY_SCALEBAR)) {
			mMap.getMapScaleBar().setVisible((Boolean)newValue);
			Log.d(TAG, "scalebar set to " + ((Boolean)newValue ? "visible" : "hidden"));
		}
	}
	
	public void addLayer(Activity activity, String name, String url, String[] subdomains, int parallelrequests, byte maxzoom, byte minzoom){
		GenericTileSource tileSource = new GenericTileSource(url, subdomains, parallelrequests, maxzoom, minzoom);
		TileDownloadLayer downloadLayer = new TileDownloadLayer(createTileCache(activity, name), mMap.getModel().mapViewPosition, tileSource, mGraphicFactory);
		mMap.getLayerManager().getLayers().add(downloadLayer);
		mLayers.put(name, downloadLayer);
		Log.d(TAG, "Added layer " + name + " with url " + url);
	}
	
    private TileCache createTileCache(Activity activity, String name) {
        String cacheDirectoryName = activity.getExternalCacheDir().getAbsolutePath() + File.separator + name;
        File cacheDirectory = new File(cacheDirectoryName);
        if (!cacheDirectory.exists()) {
                cacheDirectory.mkdir();
        }
        return new FileSystemTileCache(1024, cacheDirectory, mGraphicFactory);
    }
    
    public void startLayers() {
    	Iterator<Entry<String, TileDownloadLayer>> it = mLayers.entrySet().iterator();
    	while (it.hasNext()) {
    		Entry<String, TileDownloadLayer> pairs = (Entry<String, TileDownloadLayer>) it.next();
    		pairs.getValue().start();
			Log.d(TAG, "Started layer " + pairs.getKey());
    	}
    }
    
    public void startLayer(String name) {
    	Iterator<Entry<String, TileDownloadLayer>> it = mLayers.entrySet().iterator();
    	while (it.hasNext()) {
    		Entry<String, TileDownloadLayer> pairs = (Entry<String, TileDownloadLayer>) it.next();
    		if (pairs.getKey().equals(name)) {
    			pairs.getValue().start();
    			Log.d(TAG, "Started layer " + pairs.getKey());
    			return;
    		}
    	}
    	Log.e(TAG, "Could not find any layer named " + name + " to start!");
    }
    
    public void setCenter(double lat, double lon) {
    	mMap.getModel().mapViewPosition.setCenter(new LatLong(lat, lon));
		Log.d(TAG, "center set to " + Double.toString(lat) + " " + Double.toString(lon));
    }
    
    public void setZoomLevel(byte zoomlevel) {
    	mMap.getModel().mapViewPosition.setZoomLevel(zoomlevel);
		Log.d(TAG, "zoomlevel set to " + Byte.toString(zoomlevel));
    }
    
    public void drawPolyline(List<LatLong> coordinates, Color color, float strokeWidth) {
		Paint paintStroke = mGraphicFactory.createPaint();
		paintStroke.setStyle(Style.STROKE);
		paintStroke.setColor(color);
		paintStroke.setStrokeWidth(strokeWidth);

		Polyline pl = new Polyline(paintStroke,mGraphicFactory);
		pl.getLatLongs().addAll(coordinates);
		mMap.getLayerManager().getLayers().add(pl);
    }
    
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
    		//Should be path on file system
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
