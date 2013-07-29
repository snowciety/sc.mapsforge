package sc.mapsforge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.appcelerator.kroll.KrollDict;
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
	private static final String KEY_ID = "id";
	private static final String KEY_URL = "url";
	private static final String KEY_NAME = "name";
	private static final String KEY_SUBDOMAINS = "subdomains";
	private static final String KEY_REQUESTS = "parallelRequests";
	private static final String KEY_MAXZOOM = "maxZoom";
	private static final String KEY_MINZOOM = "minZoom";
	private static final String KEY_COLOR = "color";
	private static final String KEY_COORDINATE = "latlng";
	private static final String KEY_COORDINATES = "latlngs";
	private static final String KEY_FILLCOLOR = "fillColor";
	private static final String KEY_STROKECOLOR = "strokeColor";
	private static final String KEY_STROKEWIDTH = "strokeWidth";
	private static final String KEY_HOFFSET = "hOffset";
	private static final String KEY_VOFFSET = "vOffset";
	private static final String KEY_ICONPATH = "iconPath";
	private static final String KEY_ICONSIZE = "iconSize";
	private static final String KEY_RADIUS = "radius";
	private static final String TAG = "MapsforgeProxy";
	
	private static boolean sDebug = false;
	
	private Double[] mCenterLatlng;
	private int mZoomLevel;
	
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
		debugMsg("handleCreationDict " + options.toString());
	}
	
	/*
	 * Kroll methods
	 */
	
	/**
	 * Sets the current center of the map view.
	 * @param center	a pair of latitude and longitude coordinates
	 */
	@Kroll.setProperty @Kroll.method
	public void setCenterLatlng(Double[] center) {
		if (center.length != 2) {
			throw new IllegalArgumentException("setCenter needs a latitude and longitude: [lat, lon]");
		}
		mCenterLatlng = center;
		mView.setCenter(mCenterLatlng[0], mCenterLatlng[1]);
	}
	
	/**
	 * Returns the current center position as a double pair. [lat, lon]
	 */
	@Kroll.getProperty @Kroll.method
	public Double[] getCenterLatlng() {
		return mCenterLatlng;
	}
	
	/**
	 * Sets the current zoom level for the map view.
	 * @param zoomlevel	zoom level.
	 */
	@Kroll.setProperty @Kroll.method
	public void setZoomLevel(int zoomLevel) {
		if (zoomLevel < 0 ) {
			throw new IllegalArgumentException("Zoom level has to be greater than 0!");
		}
		mZoomLevel = zoomLevel;
		mView.setZoomLevel(mZoomLevel);
	}
	
	/**
	 * Returns the current zoom level of the map view.
	 * @return	the current set zoom level
	 */
	public int getZoomLevel() {
		return mZoomLevel;
	}
	
	/**
	 * Adds a bitmap tile layer to the map view.
	 * Supported parameters:
	 * name String
	 * url String Must contain {z},{x} and {y} place holders.
	 * subdomains StringArray
	 * parallelRequests Integer
	 * maxZoom	Integer
	 * minZoom Integer
	 * @param args	dictionary with key-value pairs: {key:value}.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Kroll.method
	public void addLayer(HashMap args) {
		KrollDict dict = new KrollDict(args);
		
		String name = null, url = null;
		String[] subdomains = null;
		int parallelrequests = 0;
		byte maxzoom = 18;
		byte minzoom = 12;

		if (containsKey(dict, KEY_NAME)) {
			name = dict.get(KEY_NAME).toString();
		}

		if (containsKey(dict, KEY_URL)) {
			url = dict.getString(KEY_URL);
			if (!validUrl(url)) {
				throw new IllegalArgumentException("URL must contain {z} {x} {y} identifiers!");
			}
		}
		
		if (containsKey(dict, KEY_SUBDOMAINS)) {
			subdomains = dict.getStringArray(KEY_SUBDOMAINS);
		}
		
		if (containsKey(dict, KEY_REQUESTS)) {
			parallelrequests = dict.getInt(KEY_REQUESTS);
		}
		
		if (containsKey(dict, KEY_MAXZOOM)) {
			try {
				maxzoom = Byte.valueOf(dict.getString(KEY_MAXZOOM));
			} catch (NumberFormatException e) {
				Log.e(TAG, "maxzoom was not defined as a number!");
				return;
			}
		}
		
		if (containsKey(dict, KEY_MINZOOM)) {
			try {
				minzoom = Byte.valueOf(dict.getString(KEY_MINZOOM));
			} catch(NumberFormatException e) {
				Log.e(TAG, "minzoom was not defined as a number!");
				return;
			}
		}
		
		mView.addLayer(getActivity(), name, url, subdomains, parallelrequests, maxzoom, minzoom);
	}
	
	/**
	 * Remove a layer from the LayerManager using an identifier
	 * @param id	the layer identifier.
	 */
	@Kroll.method
	public void removeLayer(String id) {
		if (mView.removeLayer(id)) {
			debugMsg("Layer with id "+ id +" was removed");
		} else {
			Log.e(TAG, "Layer with id " + id + " could not bew removed!");
		}
	}
	
	/**
	 * Remove a layer from the LayerManager using an identifier
	 * @param id	the layer identifier.
	 */
	@Kroll.method
	public void removeLayer(int id) {
		removeLayer(Integer.toString(id));
	}
	
	/**
	 * Activates all tile layers.
	 */
	@Kroll.method
	public void startLayers() {
		mView.startLayers();
	}
	
	/**
	 * Activate a specific tile layer by its identifier 'name'.
	 * @param name
	 */
	@Kroll.method
	public void startLayer(String name) {
		mView.startLayer(name);
	}
	
	/**
	 * Draws a polyline on the map view.
	 * Supported parameters:
	 * latlngs Array<Array<Integer> Like [ [45,12], [45,13] ]
	 * color String Supported colors are: black, blue, green, red, transparent, white.
	 * strokeWidth Integer
	 * @param dict	dictionary with key-value pairs: {key:value}.
	 */
	@Kroll.method
	public HashMap createPolyline(KrollDict dict) {
		containsKey(dict, KEY_COORDINATES);
		
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
				
		int id = mView.createPolyline(geom, color, strokeWidth);
		dict.put(KEY_ID, id);
		
		return dict;
	}
	
	/**
	 * Draws a polygon on the map view.
	 * Supported parameters:
	 * latlngs Array<Array<Integer>> Like [ [45,12], [45,13] ]
	 * fillColor String Supported colors are: black, blue, green, red, transparent, white.
	 * strokeColor String Supported colors are: black, blue, green, red, transparent, white.
	 * strokeWidth Integer
	 * @param dict	dictionary with key-value pairs: {key:value}.
	 */
	@Kroll.method
	public HashMap createPolygon(KrollDict dict) {
		containsKey(dict, KEY_COORDINATES);
		
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
		
		int id = mView.createPolygon(geom, fillColor, strokeColor, strokeWidth);
		dict.put(KEY_ID, id);

		return dict;
	}
	
	/**
	 * Puts a marker on the map at a given position.
	 * Supported parameters:
	 * latlng Array<Integer> Like [45,13]
	 * iconPath String Either a URL or a file system path on the device (i.e '/sdcard/myfile.png')
	 * hOffset Integer Horizontal offset from position in pixels.
	 * vOffset Integer Vertical offset from position in pixels.
	 * @param dict	dictionary with key-value pairs: {key:value}.
	 */
	@Kroll.method
	public HashMap createMarker(KrollDict dict) {
		containsKey(dict, KEY_COORDINATE);
		
		Object[] coordinate = (Object[]) dict.get(KEY_COORDINATE);
		if (coordinate.length != 2 ) {
			throw new IllegalArgumentException("A coordinate pair was not given!");
		}
		
		String iconPath = null;
		int hoffset = 0;
		int voffset = 0;
		int iconWidth = 0;
		int iconHeight = 0;
		double lat = TiConvert.toDouble(coordinate[0]);
		double lon = TiConvert.toDouble(coordinate[1]);
		LatLong pos = new LatLong(lat, lon);
				
		if (containsKey(dict, KEY_ICONPATH)) {
			iconPath = dict.get(KEY_ICONPATH).toString();
			iconPath = iconPath.replaceAll("file://", "");
			iconPath = iconPath.trim();
		}
		
		if (iconPath.isEmpty()) {
			throw new IllegalArgumentException("Required parameter iconPath has no value!");
		}
				
		if (dict.containsKey(KEY_HOFFSET)) {
			hoffset = TiConvert.toInt(dict.get(KEY_HOFFSET));
		}
		if (dict.containsKey(KEY_VOFFSET)) {
			voffset = TiConvert.toInt(dict.get(KEY_VOFFSET));
		}
		
		if (dict.containsKey(KEY_ICONSIZE)) {
			Object[] iconSize = (Object[]) dict.get(KEY_ICONSIZE);
			if (iconSize.length != 2) {
				throw new IllegalArgumentException("Parameter iconSize takes two and only two values!");
			}
			iconWidth 	= TiConvert.toInt(iconSize[0]);
			iconHeight 	= TiConvert.toInt(iconSize[1]);
		}

		int id = mView.createMarker(pos, iconPath, hoffset, voffset, iconWidth, iconHeight);
		dict.put(KEY_ID, id);

		return dict;
	}
	
	/**
	 * Draws a circle on the map view.
	 * Supported parameters:
	 * latlng Array<Integer> Like [45,12]
	 * fillColor String Supported colors are: black, blue, green, red, transparent, white.
	 * strokeColor String Supported colors are: black, blue, green, red, transparent, white.
	 * strokeWidth Integer
	 * radius Integer Radius of the circle in meters.
	 * @param dict	dictionary with key-value pairs: {key:value}.
	 */
	@Kroll.method
	public HashMap createCircle(KrollDict dict) {
		containsKey(dict, KEY_COORDINATE);
		
		Object[] coordinate = (Object[]) dict.get(KEY_COORDINATE);
		double lat = TiConvert.toDouble(coordinate[0]);
		double lon = TiConvert.toDouble(coordinate[1]);
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
			throw new IllegalArgumentException("Parameter 'radius' can not be <0!");
		}
		
		int id = mView.createCircle(latLong, radius, fillColor, strokeColor, strokeWidth);
		dict.put(KEY_ID, id);

		return dict;
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
			if (pair.length < 2) {
				throw new IllegalArgumentException("A coordinate pair was not given!");
			}
			double lat = TiConvert.toDouble(pair[0]);
			double lon = TiConvert.toDouble(pair[1]);
			geom.add(new LatLong(lat, lon));
		}
		
		return geom;
	}
	
	private boolean containsKey(KrollDict dict, String key) {
		if (!dict.containsKey(key)) {
			throw new IllegalArgumentException("Required parameter '"+ key +"' is missing!");
		} else{
			return true;
		}
	}

}
