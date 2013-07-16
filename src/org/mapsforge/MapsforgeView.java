package org.mapsforge;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.view.TiUIView;
import org.mapsforge.core.graphics.Color;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.cache.FileSystemTileCache;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.download.TileDownloadLayer;
import org.mapsforge.map.layer.overlay.Polyline;

import android.app.Activity;

public class MapsforgeView extends TiUIView {
	
	private static final String TAG = "MapsforgeView";
	private static final String KEY_SCALEBAR = "scalebar";
	private MapView mMap; //TODO Destroy this reference when View is destroyed!
	private HashMap<String, TileDownloadLayer> mLayers = new HashMap<String, TileDownloadLayer>();

	public MapsforgeView(TiViewProxy proxy) {
		super(proxy);		
		mMap = new MapView(proxy.getActivity());
		
		setNativeView(mMap);
	}
	
	@Override
	public void processProperties(KrollDict props) {
		super.processProperties(props);
		
		Log.d(TAG, "processProperties " + props);
				
		if (props.containsKey(KEY_SCALEBAR)) {
			mMap.getMapScaleBar().setVisible(props.getBoolean(KEY_SCALEBAR));
			Log.d(TAG, "scalebar set to " + (props.getBoolean(KEY_SCALEBAR) ? "visible" : "hidden"));
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
		TileDownloadLayer downloadLayer = new TileDownloadLayer(createTileCache(activity, name), mMap.getModel().mapViewPosition, tileSource, AndroidGraphicFactory.INSTANCE);
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
        return new FileSystemTileCache(1024, cacheDirectory, AndroidGraphicFactory.INSTANCE);
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
    }
    
    public void setZoomLevel(byte zoomlevel) {
    	mMap.getModel().mapViewPosition.setZoomLevel(zoomlevel);
    }
    
    public void drawPolyline(List<LatLong> coordinates, Color color) {
		Paint paintStroke = AndroidGraphicFactory.INSTANCE
				.createPaint();
		paintStroke.setStyle(Style.STROKE);
		paintStroke.setColor(color);
		paintStroke.setStrokeWidth(5);

		Polyline pl = new Polyline(paintStroke,
				AndroidGraphicFactory.INSTANCE);
		pl.getLatLongs().addAll(coordinates);
		mMap.getLayerManager().getLayers().add(pl);
    }
}
