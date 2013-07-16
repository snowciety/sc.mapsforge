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
	"name": "snowciety", 
	"url": "http://{s}.tiles.cdn.snowcietyapp.com/snowciety/{z}/{x}/{y}.png",
	"subdomains": ["a", "b", "c"],
	"parallelrequests": 2,
	"maxzoom": "18",
	"minzoom": "12"
	});
mapView.startLayer("snowciety"); //All layers can be started with .startLayers()
//Draws to triangles on the map
mapView.drawPolyline({
	"coordinates": [[47.33,12.76],[47.33,12.78], [47.35, 12.77],[47.33,12.76]], 
	"color": "blue"});
mapView.drawPolyline({
	"coordinates": [[47.3345,12.7645],[47.3345,12.7845], [47.3545, 12.7745],[47.3345,12.7645]], 
	"color": "red"});