package org.mapsforge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.view.TiUIView;
import org.mapsforge.core.graphics.Color;
import org.mapsforge.core.model.LatLong;

import android.app.Activity;

@Kroll.proxy(creatableInModule = MapsforgeModule.class)
public class MapsforgeViewProxy extends TiViewProxy {
	
	private static final String KEY_URL = "url";
	private static final String KEY_NAME = "name";
	private static final String KEY_SUBDOMAINS = "subdomains";
	private static final String KEY_REQUESTS = "parallelRequests";
	private static final String KEY_MAXZOOM = "maxZoom";
	private static final String KEY_MINZOOM = "minZoom";
	private static final String KEY_COLOR = "color";
	private static final String KEY_COORDINATES = "coordinates";
	private static final String KEY_FILLCOLOR = "fillColor";
	private static final String KEY_STROKECOLOR = "strokeColor";
	private static final String KEY_STROKEWIDTH = "strokeWidth";
	private static final String KEY_HOFFSET = "hOffset";
	private static final String KEY_VOFFSET = "vOffset";
	private static final String KEY_ICONPATH = "iconPath";
	private static final String KEY_RADIUS = "radius";
	
	private static final String TAG = "MapsforgeProxy";
	private MapsforgeView mView;

	/**
	 * Overrides
	 */
	
	@Override
	public TiUIView createView(Activity activity) {
		Log.d(TAG, "createView called");
		mView = new MapsforgeView(this);
		mView.getLayoutParams().autoFillsHeight = true;
		mView.getLayoutParams().autoFillsWidth = true;
		
		return mView;
	}
	
	@Override
	public void handleCreationDict(KrollDict options) {
		super.handleCreationDict(options);
	}
	
	@Override
	public void handleCreationArgs(KrollModule createdInModule, Object[] args) {
		super.handleCreationArgs(createdInModule, args);
	}
	
	/**
	 * Exposed Kroll methods
	 */
	
	@Kroll.method
	public void setCenter(double lat, double lon) {
		mView.setCenter(lat,lon);
	}
	
	@Kroll.method
	public void setZoomLevel(String zoomlevel) {
		mView.setZoomLevel(Byte.valueOf(zoomlevel));
	}
	
	@Kroll.method
	public void addLayer(HashMap<String,Object> args) {
		KrollDict dict = new KrollDict(args);
		/*
		 * Arguments:
		 * name String (used as cached identifier)
		 * url String
		 * subdomains String[]
		 * parallelrequests int
		 * maxzoom byte
		 * minzoom byte
		 *
		 */
		String name = null, url = null;
		String[] subdomains = null;
		int parallelrequests = 0;
		byte maxzoom = 18;
		byte minzoom = 12;
		
		if (dict.containsKey(KEY_NAME)) {
			name = dict.getString(KEY_NAME);
		} else {
			Log.e(TAG, "argument '" + KEY_NAME + "' must be supplied!");
			return;
		}
		
		if (dict.containsKey(KEY_URL)) {
			url = dict.getString(KEY_URL);
			if (!validUrl(url)) {
				Log.e(TAG, "url must contain {z} {x} {y} identifiers!");
			}
		} else {
			Log.e(TAG, "argument '" + KEY_URL + "' must be supplied!");
			return;
		}
		
		if (dict.containsKey(KEY_SUBDOMAINS)) {
			subdomains = dict.getStringArray(KEY_SUBDOMAINS);
		} else {
			Log.e(TAG, "argument '" + KEY_SUBDOMAINS + "' must be supplied!");
			return;
		}
		
		if (dict.containsKey(KEY_REQUESTS)) {
			parallelrequests = dict.getInt(KEY_REQUESTS);
		} else {
			Log.e(TAG, "argument '" + KEY_REQUESTS + "' must be supplied!");
			return;
		}
		
		if (dict.containsKey(KEY_MAXZOOM)) {
			try {
				maxzoom = Byte.valueOf(dict.getString(KEY_MAXZOOM));
			} catch (NumberFormatException e) {
				Log.e(TAG, "maxzoom was not defined as a number!");
			}
		} else {
			Log.e(TAG, "argument '" + KEY_MAXZOOM + "' must be supplied!");
			return;
		}
		
		if (dict.containsKey(KEY_MINZOOM)) {
			try {
				minzoom = Byte.valueOf(dict.getString(KEY_MINZOOM));
			} catch(NumberFormatException e) {
				Log.e(TAG, "minzoom was not defined as a number!");
			}
		} else {
			Log.e(TAG, "argument '" + KEY_MINZOOM + "' must be supplied!");
			return;
		}
		
		mView.addLayer(getActivity(), name, url, subdomains, parallelrequests, maxzoom, minzoom);
	}
	
	@Kroll.method
	public void startLayers() {
		mView.startLayers();
	}
	
	@Kroll.method
	public void startLayer(String name) {
		mView.startLayer(name);
	}
	
	@Kroll.method
	public void drawPolyline(KrollDict dict) {
		if (!dict.containsKey(KEY_COORDINATES)) {
			Log.e(TAG, "Required parameter 'coordinates' is missing! Aborting...");
			return;
		}
		Object[] coordinates = (Object[]) dict.get(KEY_COORDINATES);
		List<LatLong> geom = coordinatesToList(coordinates);
		Color color = Color.RED;
		float strokeWidth = 0;
		
		if (dict.containsKey(KEY_COLOR)) {
			color = Color.valueOf(dict.get(KEY_COLOR).toString().toUpperCase());
		}
		if (dict.containsKey(KEY_STROKEWIDTH)) {
			strokeWidth = TiConvert.toFloat(dict.get(KEY_STROKEWIDTH));
		}
				
		mView.drawPolyline(geom, color, strokeWidth);
	}
	
	@Kroll.method
	public void drawPolygon(KrollDict dict) {
		if (!dict.containsKey(KEY_COORDINATES)) {
			Log.e(TAG, "Required parameter 'coordinates' is missing! Aborting...");
			return;
		}
		Object[] coordinates = (Object[]) dict.get(KEY_COORDINATES);
		List<LatLong> geom = coordinatesToList(coordinates);
		Color fillColor = Color.TRANSPARENT;
		Color strokeColor = Color.BLACK;
		float strokeWidth = 0;
		if (dict.containsKey(KEY_FILLCOLOR)) {
			fillColor = Color.valueOf(dict.get(KEY_FILLCOLOR).toString().toUpperCase());
		}
		if (dict.containsKey(KEY_STROKECOLOR)) {
			strokeColor = Color.valueOf(dict.get(KEY_STROKECOLOR).toString().toUpperCase());
		}
		if (dict.containsKey(KEY_STROKEWIDTH)) {
			strokeWidth = TiConvert.toFloat(dict.get(KEY_STROKEWIDTH));
		}
		
		mView.drawPolygon(geom, fillColor, strokeColor, strokeWidth);
	}
	
	@Kroll.method
	public void drawMarker(KrollDict dict) {
		if (!dict.containsKey(KEY_COORDINATES)) {
			Log.e(TAG, "Required parameter 'coordinates' is missing! Aborting...");
			return;
		}
		String iconPath = null;
		int hoffset = 0;
		int voffset = 0;
		
		Object[] coordinates = (Object[]) dict.get(KEY_COORDINATES);
		double lat = TiConvert.toDouble(coordinates[0]);
		double lon = TiConvert.toDouble(coordinates[1]);
		LatLong pos = new LatLong(lat, lon);
				
		if (dict.containsKey(KEY_ICONPATH)) {
			iconPath = dict.get(KEY_ICONPATH).toString();
			iconPath = iconPath.replaceAll("file://", "");
			iconPath = iconPath.trim();
		} else {
			Log.e(TAG, "Required parameter iconPath could not be found! Aborting...");
			return;
		}
		
		if (iconPath.isEmpty()) {
			Log.e(TAG, "Required parameter iconPath has no value! Aborting...");
			return;
		}
				
		if (dict.containsKey(KEY_HOFFSET)) {
			hoffset = TiConvert.toInt(dict.get(KEY_HOFFSET));
		}
		if (dict.containsKey(KEY_VOFFSET)) {
			voffset = TiConvert.toInt(dict.get(KEY_VOFFSET));
		}

		mView.drawMarker(pos, iconPath, hoffset, voffset);
	}
	
	@Kroll.method
	public void drawCircle(KrollDict dict) {
		if (!dict.containsKey(KEY_COORDINATES)) {
			Log.e(TAG, "Required parameter 'coordinates' is missing! Aborting...");
			return;
		}
		Object[] coordinates = (Object[]) dict.get(KEY_COORDINATES);
		double lat = TiConvert.toDouble(coordinates[0]);
		double lon = TiConvert.toDouble(coordinates[1]);
		LatLong latLong = new LatLong(lat, lon);
		
		Color fillColor = Color.RED;
		Color strokeColor = Color.BLACK;
		float strokeWidth = 0;
		float radius = 0;
		if (dict.containsKey(KEY_FILLCOLOR)) {
			fillColor = Color.valueOf(dict.get(KEY_FILLCOLOR).toString().toUpperCase());
		}
		if (dict.containsKey(KEY_STROKECOLOR)) {
			strokeColor = Color.valueOf(dict.get(KEY_STROKECOLOR).toString().toUpperCase());
		}
		if (dict.containsKey(KEY_STROKEWIDTH)) {
			strokeWidth = TiConvert.toFloat(dict.get(KEY_STROKEWIDTH));
		}
		if (dict.containsKey(KEY_RADIUS)) {
			radius = TiConvert.toFloat(dict.get(KEY_RADIUS));
		}
		
		if (radius < 0) {
			Log.e(TAG, "Parameter radius can not be <0! Aborting...");
			return;
		}
		
		mView.drawCircle(latLong, radius, fillColor, strokeColor, strokeWidth);
	}
	
	/**
	 * Private methods
	 */
	
	private boolean validUrl(String url) {
		return url.matches(".*\\{z\\}.*\\{x\\}.*\\{y\\}.*");
	}
	
	private List<LatLong> coordinatesToList(Object[] coordinates) {
		List<LatLong> geom = new ArrayList<LatLong>();
		for(int i = 0; i < coordinates.length; i++) {
			Object[] pair = (Object[]) coordinates[i];
			double lat = TiConvert.toDouble(pair[0]);
			double lon = TiConvert.toDouble(pair[1]);
			geom.add(new LatLong(lat, lon));
		}
		
		return geom;
	}

}
