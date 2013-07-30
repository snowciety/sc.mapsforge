var win = Ti.UI.createWindow({
	backgroundColor:'white'
});

var mf = require('sc.mapsforge');
var mapView = mf.createMapsforgeView({ 
	"scalebar": true,
	"minZoom": 12, //Min zoom level for map view
	"maxZoom": 18,  //Max zoom level for map view
	"centerLatlng": [48.32, 14.79], //Unknown location
	"zoomlevel": 18, //Bogus initial zoom level
	"debug": true });
win.add(mapView);
win.open();
//Set center and zoom level on map view using properties
mapView.centerLatlng = [47.32, 12.79]; //Center at Zell am See
mapView.zoomLevel = 12;
/* 	
 * Layers etc have to be initialized after the window has been opened
 * This is due to "createView" in MapsforgeViewProxy is not being called until
 * after the window has been opened.
 */
mapView.addLayer({ 
	"name": "osm", 
	"url": "http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png",
	"subdomains": ["a", "b"],
	"parallelRequests": 2,
	"maxZoom": "18",
	"minZoom": "12"
});

//Draw a blue line on the map...
var polyline = mapView.createPolyline({
	"latlngs": [
		[47.33,12.76], 
		[47.33,12.78], 
		[47.35, 12.77]
	], 
	"color": "blue",
	"strokeWidth": 5
});
Ti.API.info('Created polyline: ' + JSON.stringify(polyline));
//..and change its color to red...
polyline.color = "red";
//...and then update the layer.
polyline = mapView.updateLayer(polyline);
//Note, that your previous reference is invalid and has
//to be replaced with the new one returned from updateLayer()
Ti.API.info('Updated polyline to: ' + JSON.stringify(polyline));

//Draw a green polygon with a thick black stroke
mapView.createPolygon({
	"latlngs": [
		[47.3045, 12.7345], 
		[47.3045, 12.7545], 
		[47.3235, 12.7545], 
		[47.3235, 12.7345], 
		[47.3045, 12.7345]
	], 
	"fillColor": "green",
	"strokeColor": "black",
	"strokeWidth": 5}
);
	
//Draw a marker
mapView.createMarker({
	"iconPath": "http://www.google.com/mapfiles/marker.png",
	"latlng": [47.3100, 12.7300]
});
	
//Draw a marker at the same position as above but with offset
mapView.createMarker({
	"iconPath": "http://www.google.com/mapfiles/dd-start.png",
	"latlng": [47.3100, 12.7300],
	"hOffset": 5,
	"vOffset": 4
	});
	
//Draw a sized marker (of the Zuck)
//Original icon is 100x99 pixels
mapView.createMarker({
	"iconPath": "https://fbcdn-profile-a.akamaihd.net/hprofile-ak-prn2/202896_4_1782288297_s.jpg",
	"latlng": [47.3160, 12.7820],
	"iconSize": [64, 64] //New size in pixels
});

//Draw a circle
mapView.createCircle({
	"latlng": [47.2920, 12.7830],
	"fillColor": "blue",
	"strokeColor": "red",
	"radius": 500 //This is meters!
});