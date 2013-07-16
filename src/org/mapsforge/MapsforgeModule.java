package org.mapsforge;


import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;

import org.appcelerator.titanium.TiApplication;
import org.appcelerator.kroll.common.Log;

import android.app.Activity;

@Kroll.module(name="Mapsforge", id="org.mapsforge")
public class MapsforgeModule extends KrollModule
{

	// Standard Debugging variables
	private static final String TAG = "MapsforgeModule";

	// You can define constants with @Kroll.constant, for example:
	// @Kroll.constant public static final String EXTERNAL_NAME = value;
	
	public MapsforgeModule()
	{
		super();
	}

	@Kroll.onAppCreate
	public static void onAppCreate(TiApplication app)
	{
		Log.d(TAG, "inside onAppCreate");
		// put module init code that needs to run when the application is created
	}
	
	@Override
	public void onResume(Activity activity) {
		super.onResume(activity);
	}
}

