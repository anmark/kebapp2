package com.anmark.kebapp;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;


public class MainActivity extends Activity {

	// flag for Internet connection status
	Boolean isInternetPresent = false;

	// Connection detector class
	ConnectionDetector cd;

	// Alert Dialog Manager
	AlertDialogManager alert = new AlertDialogManager();

	// Google Places
	GooglePlaces googlePlaces;

	// Places List
	PlacesList nearPlaces;

	// GPS Location
	GPSTracker gps;

	// Button
	Button btnShowOnMap;

	// Progress dialog
	ProgressDialog pDialog;

	// Places Listview
	ListView lv;

	// ListItems data
	ArrayList<HashMap<String, String>> placesListItems = new ArrayList<HashMap<String,String>>();


	// KEY Strings
	public static String KEY_REFERENCE = "reference"; // id of the place
	public static String KEY_NAME = "name"; // name of the place
	public static String KEY_VICINITY = "vicinity"; // Place area name
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Getting listview
		lv = (ListView) findViewById(R.id.list);

		// button show on map
		btnShowOnMap = (Button) findViewById(R.id.btn_show_map);
		
		//TODO: Bad fix, pass arguments to activity instead
		nearPlaces = SplashScreenActivity.nearPlaces;
		gps = SplashScreenActivity.gps;
		
		// calling background Async task to load Google Places
		// After getting places from Google all the data is shown in listview
		new LoadPlaces().execute();
				
		/** Button click event for shown on map */
		btnShowOnMap.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(getApplicationContext(),
						PlacesMapActivityV2.class);
				// Sending user current geo location
				i.putExtra("user_latitude", Double.toString(gps.getLatitude()));
				i.putExtra("user_longitude", Double.toString(gps.getLongitude()));

				// passing near places to map activity
				i.putExtra("near_places", nearPlaces);
				// staring activity
				startActivity(i);
			}
		});


		/**
		 * ListItem click event
		 * On selecting a listitem SinglePlaceActivity is launched
		 * */
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// getting values from selected ListItem
				String reference = ((TextView) view.findViewById(R.id.reference)).getText().toString();

				/*
				DBAdapter db = new DBAdapter(getApplicationContext());
				db.open();
				db.deleteAllRows();
				db.close();
				
				*/
				// Starting new intent
				Intent in = new Intent(getApplicationContext(),
						SinglePlaceActivity.class);

				// Sending place refrence id to single place activity
				// place refrence id used to get "Place full details"
				in.putExtra(KEY_REFERENCE, reference);
				startActivity(in);
			}
		});
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	  
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
		}

		/**
		 * getting Places JSON
		 * */
		protected String doInBackground(String... args) {
			// creating Places class object
			googlePlaces = new GooglePlaces();
			//nearPlaces = SplashScreenActivity.nearPlaces;
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
			//pDialog.dismiss();
			// updating UI from Background Thread
			runOnUiThread(new Runnable() {
				public void run() {
					/**
					 * Updating parsed Places into LISTVIEW
					 * */
					// Get json response status
					System.out.println("nearplaces status " + nearPlaces.status);
					String status = nearPlaces.status;

					// Check for all possible status
					if(status.equals("OK")){
						// Successfully got places details
						if (nearPlaces.results != null) {
							// loop through each place
							for (Place p : nearPlaces.results) {
								HashMap<String, String> map = new HashMap<String, String>();

								// Place reference won't display in listview - it will be hidden
								// Place reference is used to get "place full details"
								map.put(KEY_REFERENCE, p.reference);

								// Place name
								map.put(KEY_NAME, p.name);


								// adding HashMap to ArrayList
								placesListItems.add(map);
							}
							// list adapter
							ListAdapter adapter = new SimpleAdapter(MainActivity.this, placesListItems,
									R.layout.list_item,
									new String[] { KEY_REFERENCE, KEY_NAME}, new int[] {
									R.id.reference, R.id.name });

							// Adding data into listview
							lv.setAdapter(adapter);
						}
					}
					else if(status.equals("ZERO_RESULTS")){
						// Zero results found
						alert.showAlertDialog(MainActivity.this, "Near kebab places",
								"Sorry no places found. Try to change the types of places",
								false);
					}
					else if(status.equals("UNKNOWN_ERROR"))
					{
						alert.showAlertDialog(MainActivity.this, "Places Error",
								"Sorry unknown error occured.",
								false);
					}
					else if(status.equals("OVER_QUERY_LIMIT"))
					{
						alert.showAlertDialog(MainActivity.this, "Places Error",
								"Sorry query limit to google places is reached",
								false);
					}
					else if(status.equals("REQUEST_DENIED"))
					{
						alert.showAlertDialog(MainActivity.this, "Places Error",
								"Sorry error occured. Request is denied",
								false);
					}
					else if(status.equals("INVALID_REQUEST"))
					{
						alert.showAlertDialog(MainActivity.this, "Places Error",
								"Sorry error occured. Invalid Request",
								false);
					}
					else
					{
						alert.showAlertDialog(MainActivity.this, "Places Error",
								"Sorry error occured.",
								false);
					}
				}
			});

		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}



}
