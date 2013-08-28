package com.anmark.test;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class SinglePlaceCommentActivity extends Activity {

	private EditText txtCommentValue;
	private DBAdapter db;
	private String placeID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_single_place_comment);

		Intent i = getIntent();
		placeID = i.getStringExtra("placeID");

		txtCommentValue = (EditText) findViewById(R.id.Single_Place_CommentValue);

		db = new DBAdapter(getApplicationContext());
		db.open();
		txtCommentValue.setText(db.getRow(placeID).getComment());
		db.close();

	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	   
	}

	public void saveComment(View view) {
		String tempComment = txtCommentValue.getText().toString();
		db.open();
		System.out.println("update placeid :"+placeID);
		System.out.println("update: " +db.updateRowCommentById(placeID, tempComment));
		db.close();

		Intent returnIntent = new Intent();
		returnIntent.putExtra("placeID", placeID);
		setResult(RESULT_OK, returnIntent);     
		finish();
	}

}
