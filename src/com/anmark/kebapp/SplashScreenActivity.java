package com.anmark.kebapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;


public class SplashScreenActivity extends Activity {

	// flag for Internet connection status
	Boolean isInternetPresent = false;

	// Connection detector class
	ConnectionDetector cd;

	// Alert Dialog Manager
	AlertDialogManager alert = new AlertDialogManager();

	// Google Places
	GooglePlaces googlePlaces;

	// Places List
	public static PlacesList nearPlaces;

	// GPS Location
	public static GPSTracker gps;

	// Progress dialog
	ProgressDialog pDialog;


	public static boolean noGo = true;
	// Splash screen timer
	private static int SPLASH_TIME_OUT = 2000;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash_screen);

		cd = new ConnectionDetector(getApplicationContext());

		// Check if Internet present
		isInternetPresent = cd.isConnectingToInternet();
		if (!isInternetPresent) {
			// Internet Connection is not present

			alert.showAlertDialog(SplashScreenActivity.this, "Internet Connection Error",
					"Please connect to working Internet connection", false);
			// stop executing code by return
			return;
		}

		// creating GPS Class object
		gps = new GPSTracker(this);

		// check if GPS location can get
		if (gps.canGetLocation()) {
			Log.d("Your Location", "latitude:" + gps.getLatitude() + ", longitude: " + gps.getLongitude());
		} else {
			// Can't get user's current location
			alert.showAlertDialog(SplashScreenActivity.this, "GPS Status",
					"Couldn't get location information. Please enable GPS",
					false);
			// stop executing code by return
			return;
		}

		//TODO:
		noGo = false;

		new Handler().postDelayed(new Runnable() {

			// Showing splash screen with a timer
			@Override
			public void run() {
				// This method will be executed once the timer is over
				// calling background Async task to load Google Places
				new LoadPlaces().execute();
			}
		}, SPLASH_TIME_OUT);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		setContentView(R.layout.activity_splash_screen);
	}

	/**
	 * Background Async Task to Load Google places
	 * */
	class LoadPlaces extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(SplashScreenActivity.this);
			pDialog.setMessage(Html.fromHtml("<b>Search</b><br/>Loading kebab places..."));
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.getWindow().setGravity(Gravity.BOTTOM);
			pDialog.show();
		}

		/**
		 * getting Places JSON
		 * */
		protected String doInBackground(String... args) {
			// creating Places class object
			googlePlaces = new GooglePlaces();

			try {
				// Separeate your place types by PIPE symbol "|"
				// If you want all types places make it as null
				// Check list of types supported by google
				// 
				String types = "restaurant"; // Listing places only restaurants
				String keyword = "pizza";// Listing places with keyword pizza
				// Radius in meters - increase this value if you don't find any places
				//double radius = 1000; // 1000 meters 
				String rankby = "distance";

				// get nearest places ranked by distance
				nearPlaces = googlePlaces.search(gps.getLatitude(),
						gps.getLongitude(), types, keyword,  rankby);


			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * and show the data in UI
		 * Always use runOnUiThread(new Runnable()) to update UI from background
		 * thread, otherwise you will get error
		 * **/
		protected void onPostExecute(String file_url) {
			// dismiss the dialog after getting all products
			pDialog.dismiss();

			// After completing http call
			// will close this activity and lauch main activity
			Intent i = new Intent(SplashScreenActivity.this, MainActivity.class);
			startActivity(i);

			// close this activity
			finish();

		}
	}



}
