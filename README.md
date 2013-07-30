#mapsforge Titanium module
This module wraps parts of the, not yet released, [mapsforge](https://code.google.com/p/mapsforge/) 0.4.0 (0.4.0-SNAPSHOT) API. Specifically it is using the ["rewrite"](https://code.google.com/p/mapsforge/source/browse/?name=rewrite) branch of the mapsforge API. Note, that is API is **not** production ready and is unsupported.
Just as mapsforge, this module is in development.

#Building
Clone this repository.
Copy "build.properties.example" to "build.properties" and change the following variables to match your environment:
* titanium.sdk
* titanium.os
* titanium.version
* android.sdk

Then, run "ant" in the repository to build the module. If successful, the finished module can be found in the folder "dist" named "sc.mapsforge-android-x.x.zip".

#The mapsforge library
This repo includes a module built from the mapsforge repository on 2013-07-16. Since mapsforge is under development new features might be added that is not supported by this module yet.
If you want to upgrade the included library (and add new features) you need to build mapsforge as described [here](https://code.google.com/p/mapsforge/wiki/GettingStartedDevelopers) and then replace the jar in the "lib" directory with the new one. You then need to rebuild this module as described above.
Note that upgrading the mapsforge library might break existing functionality in this module, so inexperienced users are discouraged to do this!

#Usage
See the example app in "/example" for an overview of how this module can be used.

#Documentation
JavaDoc documentation for the proxy can be found [here](http://snowciety.github.io/sc.mapsforge/sc/mapsforge/MapsforgeViewProxy.html#method_summary). This documentation together with the example application should hopefully give some insight on how the module is to be used.
