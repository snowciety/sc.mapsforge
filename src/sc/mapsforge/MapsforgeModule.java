/*
 * Copyright 2013 Snowciety
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
 package sc.mapsforge;


import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;

import org.appcelerator.titanium.TiApplication;
import org.appcelerator.kroll.common.Log;

import android.app.Activity;

@Kroll.module(name="Mapsforge", id="sc.mapsforge")
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
		// put module init code that needs to run when the application is created
	}
	
	@Override
	public void onResume(Activity activity) {
		super.onResume(activity);
	}
	
	@Override
	public void onDestroy(Activity activity) {
		super.onDestroy(activity);
	}
}

