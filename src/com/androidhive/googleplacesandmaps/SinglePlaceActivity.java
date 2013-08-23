package com.androidhive.googleplacesandmaps;

import com.androidhive.googleplacesandmaps.DBAdapter.Row;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.TextView;

public class SinglePlaceActivity extends Activity {
	// flag for Internet connection status
	Boolean isInternetPresent = false;

	// Connection detector class
	ConnectionDetector cd;

	// Alert Dialog Manager
	AlertDialogManager alert = new AlertDialogManager();

	// Google Places
	GooglePlaces googlePlaces;

	// Place Details
	PlaceDetails placeDetails;

	// Progress dialog
	ProgressDialog pDialog;



	// KEY Strings
	public static String KEY_REFERENCE = "reference"; // id of the place

	private RatingBar ratingBar;
	private TextView txtRatingValue;
	private DBAdapter db;
	private boolean placeExist;
	private String reference;
	
	private String name;
	private String address;
	private String phone;
	private String latitude;
	private String longitude;
	private String placeID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.single_place);

		Intent i = getIntent();

		// Place referece id
		reference = i.getStringExtra(KEY_REFERENCE);

		// Calling a Async Background thread
		new LoadSinglePlaceDetails().execute(reference);

		ratingBar = (RatingBar) findViewById(R.id.ratingBar);
		txtRatingValue = (TextView) findViewById(R.id.txtRatingValue);

		db = new DBAdapter(getApplicationContext());
		//db.open();
		//db.createRow("hallåaaa", "vad", 0, "");
		
		
		
		
		//System.out.println(reference+ " " + name);
		
		//db.createRow(reference, name, 0, "");
		/*
		for(Row row : db.getAllRows()){
			System.out.println("placeID: "+ row.placeID + " Name: " + row.getName()+ " Rating: " + row.getRating() +" Comment: " + row.getComment());
		}
		db.close();

		
		placeExist = db.rowExists(placeDetails.result.getId());	
		if(!placeExist){							
			db.createRow(placeDetails.result.getId(), placeDetails.result.getName(), 0, "");
			txtRatingValue.setText(0);
			//TODO:comment
		}

		txtRatingValue.setText(Double.toString(db.getRow(placeDetails.result.getId()).getRating()));
		 */
		addListenerOnRatingBar();
	}

	public void addListenerOnRatingBar() {

		//if rating value is changed,
		//display the current rating value in the result (textview) automatically
		ratingBar.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {
			public void onRatingChanged(RatingBar ratingBar, float rating,
					boolean fromUser) {

					db.open();
					db.updateRowRatingById(placeID, (double) rating);
					db.close();
				txtRatingValue.setText(String.valueOf(rating));

			}
		});
	}


	/**
	 * Background Async Task to Load Google places
	 * */
	class LoadSinglePlaceDetails extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(SinglePlaceActivity.this);
			pDialog.setMessage("Loading profile ...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		/**
		 * getting Profile JSON
		 * */
		protected String doInBackground(String... args) {
			String reference = args[0];

			// creating Places class object
			googlePlaces = new GooglePlaces();
			//TODO:
			// Check if used is connected to Internet
			try {
				placeDetails = googlePlaces.getPlaceDetails(reference);

			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(String file_url) {
			// dismiss the dialog after getting all products
			pDialog.dismiss();
			// updating UI from Background Thread
			runOnUiThread(new Runnable() {
				public void run() {
					/**
					 * Updating parsed Places into LISTVIEW
					 * */
					if(placeDetails != null){
						String status = placeDetails.status;

						// check place details status
						// Check for all possible status
						if(status.equals("OK")){
							if (placeDetails.result != null) {
								name = placeDetails.result.name;
								address = placeDetails.result.formatted_address;
								phone = placeDetails.result.formatted_phone_number;
								latitude = Double.toString(placeDetails.result.geometry.location.lat);
								longitude = Double.toString(placeDetails.result.geometry.location.lng);
								placeID = placeDetails.result.getId();

								//System.out.println(placeID);
								//if place exist query and display rating else create place in db
								DBAdapter db = new DBAdapter(getApplicationContext());
								db.open();
								//accessDB( placeID,  name);
								if(!db.rowExists(placeID)){
									db.createRow(placeID, name, 0, "");
									txtRatingValue.setText("0");
								}
								Double rating = db.getRow(placeID).getRating();
								txtRatingValue.setText(Double.toString(rating));
								
								//float convert workaround
								Float ratingf = (float) (rating / 1.0);
								ratingBar.setRating(ratingf);
								//TODO: remove print								
								db.printAllRows();
								db.close();
								
								txtRatingValue = (TextView) findViewById(R.id.txtRatingValue);

								Log.d("Place ", name + address + phone + latitude + longitude);

								// Displaying all the details in the view
								// single_place.xml
								TextView lbl_name = (TextView) findViewById(R.id.name);
								TextView lbl_address = (TextView) findViewById(R.id.address);
								TextView lbl_phone = (TextView) findViewById(R.id.phone);
								TextView lbl_location = (TextView) findViewById(R.id.location);

								// Check for null data from google
								// Sometimes place details might missing
								name = name == null ? "Not present" : name; // if name is null display as "Not present"
								address = address == null ? "Not present" : address;
								phone = phone == null ? "Not present" : phone;
								latitude = latitude == null ? "Not present" : latitude;
								longitude = longitude == null ? "Not present" : longitude;

								lbl_name.setText(name);
								lbl_address.setText(address);
								lbl_phone.setText(Html.fromHtml("<b>Phone:</b> " + phone));
								lbl_location.setText(Html.fromHtml("<b>Latitude:</b> " + latitude + ", <b>Longitude:</b> " + longitude));

								//TODO:	set rating 
								//txtRatingValue = (TextView) findViewById(R.id.txtRatingValue);
								//txtRatingValue.setText(String.valueOf(rating));
							}
						}
						else if(status.equals("ZERO_RESULTS")){
							alert.showAlertDialog(SinglePlaceActivity.this, "Near Places",
									"Sorry no place found.",
									false);
						}
						else if(status.equals("UNKNOWN_ERROR"))
						{
							alert.showAlertDialog(SinglePlaceActivity.this, "Places Error",
									"Sorry unknown error occured.",
									false);
						}
						else if(status.equals("OVER_QUERY_LIMIT"))
						{
							alert.showAlertDialog(SinglePlaceActivity.this, "Places Error",
									"Sorry query limit to google places is reached",
									false);
						}
						else if(status.equals("REQUEST_DENIED"))
						{
							alert.showAlertDialog(SinglePlaceActivity.this, "Places Error",
									"Sorry error occured. Request is denied",
									false);
						}
						else if(status.equals("INVALID_REQUEST"))
						{
							alert.showAlertDialog(SinglePlaceActivity.this, "Places Error",
									"Sorry error occured. Invalid Request",
									false);
						}
						else
						{
							alert.showAlertDialog(SinglePlaceActivity.this, "Places Error",
									"Sorry error occured.",
									false);
						}
					}else{
						alert.showAlertDialog(SinglePlaceActivity.this, "Places Error",
								"Sorry error occured.",
								false);
					}


				}
			});

		}

	}
	public void accessDB(String id, String name){
		DBAdapter dba = new DBAdapter(getApplicationContext());
		dba.open();
		dba.createRow(id, name, 0, "");
		for(Row row : db.getAllRows()){
			System.out.println("placeID: "+ row.placeID + " Name: " + row.getName()+ " Rating: " + row.getRating() +" Comment: " + row.getComment());
		}
		db.close();
	}

}
