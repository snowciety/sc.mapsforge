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
	
	private static final String KEY_DEBUG = "debug";
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
	
	private static boolean sDebug = false;
	
	private MapsforgeView mView;
	
	private static void debugMsg(String msg) {
		if (sDebug) {
			Log.d(TAG, msg);
		}
	}

	/**
	 * Overrides
	 */
	
	@Override
	public TiUIView createView(Activity activity) {
		mView = new MapsforgeView(this);
		mView.getLayoutParams().autoFillsHeight = true;
		mView.getLayoutParams().autoFillsWidth = true;
		
		return mView;
	}
	
	@Override
	public void handleCreationDict(KrollDict options) {
		super.handleCreationDict(options);
		if (options.containsKey(KEY_DEBUG)) {
			sDebug = options.getBoolean(KEY_DEBUG);
		}
		debugMsg("options: " + options.toString());
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
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Kroll.method
	public void addLayer(HashMap args) {
		KrollDict dict = new KrollDict(args);
		
		String name = null, url = null;
		String[] subdomains = null;
		int parallelrequests = 0;
		byte maxzoom = 18;
		byte minzoom = 12;

		if (dict.containsKey(KEY_NAME)) {
			name = dict.get(KEY_NAME).toString();
		} else {
			throw new IllegalArgumentException("Argument '" + KEY_NAME + "' must be supplied!");
		}

		if (dict.containsKey(KEY_URL)) {
			url = dict.getString(KEY_URL);
			if (!validUrl(url)) {
				throw new IllegalArgumentException("URL must contain {z} {x} {y} identifiers!");
			}
		} else {
			throw new IllegalArgumentException("Argument '" + KEY_URL + "' must be supplied!");
		}
		
		if (dict.containsKey(KEY_SUBDOMAINS)) {
			subdomains = dict.getStringArray(KEY_SUBDOMAINS);
		} else {
			throw new IllegalArgumentException("Argument '" + KEY_SUBDOMAINS + "' must be supplied!");
		}
		
		if (dict.containsKey(KEY_REQUESTS)) {
			parallelrequests = dict.getInt(KEY_REQUESTS);
		} else {
			throw new IllegalArgumentException("Argument '" + KEY_REQUESTS + "' must be supplied!");
		}
		
		if (dict.containsKey(KEY_MAXZOOM)) {
			try {
				maxzoom = Byte.valueOf(dict.getString(KEY_MAXZOOM));
			} catch (NumberFormatException e) {
				Log.e(TAG, "maxzoom was not defined as a number!");
				return;
			}
		} else {
			throw new IllegalArgumentException("Argument '" + KEY_MAXZOOM + "' must be supplied!");
		}
		
		if (dict.containsKey(KEY_MINZOOM)) {
			try {
				minzoom = Byte.valueOf(dict.getString(KEY_MINZOOM));
			} catch(NumberFormatException e) {
				Log.e(TAG, "minzoom was not defined as a number!");
				return;
			}
		} else {
			throw new IllegalArgumentException("Argument '" + KEY_MINZOOM + "' must be supplied!");
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
		checkForCoordinates(dict);
		
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
		checkForCoordinates(dict);
		
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
		checkForCoordinates(dict);
		
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
			throw new IllegalArgumentException("Required parameter iconPath could not be found! Aborting...");
		}
		
		if (iconPath.isEmpty()) {
			throw new IllegalArgumentException("Required parameter iconPath has no value! Aborting...");
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
		checkForCoordinates(dict);
		
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
			throw new IllegalArgumentException("Parameter radius can not be <0! Aborting...");
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
	
	private void checkForCoordinates(KrollDict dict) {
		if (!dict.containsKey(KEY_COORDINATES)) {
			throw new IllegalArgumentException("Required parameter 'coordinates' is missing! Aborting...");
		}
	}

}
