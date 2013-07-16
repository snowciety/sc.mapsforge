var win = Ti.UI.createWindow({
	backgroundColor:'white'
});

var mf = require('org.mapsforge');
var mapProxy = mf.createMapsforgeView({ "scalebar": true });
win.add(mapProxy);
win.open();
mapProxy.addLayer({ 
	"name": "snowciety", 
	"url": "http://{s}.tiles.cdn.snowcietyapp.com/snowciety/{z}/{x}/{y}.png",
	"subdomains": ["a", "b", "c"],
	"parallelrequests": 2,
	"maxzoom": "18",
	"minzoom": "12"
	});
mapProxy.setCenter(47.32, 12.79);
mapProxy.setZoomLevel("12"); //Byte value
mapProxy.startLayer("snowciety"); //All layers can be started with .startLayers()
mapProxy.drawPolyline({"coordinates": [[47.33,12.76],[47.33,12.78], [47.35, 12.77],[47.33,12.76]], "color": "blue"});