var win = Ti.UI.createWindow({
	backgroundColor:'white'
});

var mf = require('org.mapsforge');
var mapView = mf.createMapsforgeView({ 
	"scalebar": true, 
	"center": [47.32, 12.79], 
	"zoomlevel": "12" }); //Create map view
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
	"parallelrequests": 2,
	"maxzoom": "18",
	"minzoom": "12"
	});
mapView.startLayer("osm"); //All layers can be started with .startLayers()
//Draw a blue triangle on the map
mapView.drawPolyline({
	"coordinates": [[47.33,12.76],[47.33,12.78], [47.35, 12.77],[47.33,12.76]], 
	"color": "blue"});
//Draw a green polygon with a thick black stroke
mapView.drawPolygon({
	"coordinates": [[47.3045, 12.7345], [47.3045, 12.7545], [47.3235, 12.7545], [47.3235, 12.7345], [47.3045, 12.7345]], 
	"fillcolor": "green",
	"strokecolor": "black",
	"strokewidth": 5});