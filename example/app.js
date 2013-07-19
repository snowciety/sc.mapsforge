var win = Ti.UI.createWindow({
	backgroundColor:'white'
});

var mf = require('org.mapsforge');
var mapView = mf.createMapsforgeView({ 
	"scalebar": true, 
	"center": [47.32, 12.79], 
	"zoomlevel": "12",
	"debug": true }); //Create map view
win.add(mapView);
win.open();
/* 	
 * Layers etc have to be initialized after the window has been opened
 * This is due to "createView" in MapsforgeViewProxy is not being called until
 * after the window has been opened.
 */

//mapView.setCenter(47.32, 12.79); //Change current center
//mapView.setZoomLevel("12"); //Change current zoom level
mapView.addLayer({ 
	"name": "osm", 
	"url": "http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png",
	"subdomains": ["a", "b"],
	"parallelRequests": 2,
	"maxZoom": "18",
	"minZoom": "12"
	});
mapView.startLayer("osm"); //All layers can be started with .startLayers()
//Draw a blue triangle on the map
mapView.drawPolyline({
	"coordinates": [
		[47.33,12.76], 
		[47.33,12.78], 
		[47.35, 12.77], 
		[47.33,12.76]
	], 
	"color": "blue"});
//Draw a green polygon with a thick black stroke
mapView.drawPolygon({
	"coordinates": [
		[47.3045, 12.7345], 
		[47.3045, 12.7545], 
		[47.3235, 12.7545], 
		[47.3235, 12.7345], 
		[47.3045, 12.7345]
	], 
	"fillColor": "green",
	"strokeColor": "black",
	"strokeWidth": 5});
//Draw a marker
mapView.drawMarker({
	"iconPath": "http://www.google.com/mapfiles/marker.png",
	"coordinates": [47.3100, 12.7300]
	});
//Draw a marker at the same position as above but with offset
mapView.drawMarker({
	"iconPath": "http://www.google.com/mapfiles/dd-start.png",
	"coordinates": [47.3100, 12.7300],
	"hOffset": 5,
	"vOffset": 4
	});
//Draw a circle
mapView.drawCircle({
	"coordinates": [47.3320, 12.7230],
	"fillColor": "blue",
	"strokeColor": "red",
	"radius": 500
	});