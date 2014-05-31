package edu.pdx.cecs.orcycle;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.EditText;

public class TripDetailActivity extends Activity {

	// Reference to Global application object
	private MyApplication myApp = null;

	// Reference to recording service;
	private IRecordService recordingService = null;

	long tripid;
	String purpose = "";
	EditText notes;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Convenient pointer to global application object
		myApp = MyApplication.getInstance();
		recordingService = myApp.getRecordingService();

		setContentView(R.layout.activity_trip_detail);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		finishRecording();
		purpose = "";
		Intent myIntent = getIntent(); // gets the previously created intent
		purpose = myIntent.getStringExtra("purpose");
		notes = (EditText) findViewById(R.id.editTextTripDetail);
		this.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
	}

	// submit btn is only activated after the service.finishedRecording() is
	// completed.
	void submit(String notesToUpload) {
		final Intent xi = new Intent(this, TripMapActivity.class);

		TripData trip = TripData.fetchTrip(TripDetailActivity.this, tripid);
		trip.populateDetails();

		SimpleDateFormat sdfStart = new SimpleDateFormat("MMMM d, y  HH:mm");
		String fancyStartTime = sdfStart.format(trip.startTime);
		Log.v("Jason", "Start: " + fancyStartTime);

		// "3.5 miles in 26 minutes"
		/*
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		char[] tripStart = sdf.format(trip.startTime).toCharArray();
		char[] tripEnd = sdf.format(trip.endTime).toCharArray();
		int minStart = (tripStart[0]*10 + tripStart[1])*60 + tripStart[3]*10+tripStart[4];
		int minEnd = (tripEnd[0]*10 + tripEnd[1])*60 + tripEnd[3]*10+tripEnd[4];
		int minutes = minEnd - minStart;*/

		SimpleDateFormat sdfDuration = new SimpleDateFormat("HH:mm:ss");
		sdfDuration.setTimeZone(TimeZone.getTimeZone("UTC"));
		Double endTime = trip.endTime;
		Double startTime = trip.startTime;
		String duration = sdfDuration.format(endTime - startTime);

		String fancyEndInfo = String.format("%1.1f miles, %s,  %s",
				(0.0006212f * trip.distance), duration, notesToUpload);

		// Save the trip details to the phone database. W00t!
		trip.updateTrip(purpose, fancyStartTime, fancyEndInfo, notesToUpload);
		trip.updateTripStatus(TripData.STATUS_COMPLETE);
		resetService();

		// Now create the MainInput Activity so BACK btn works properly
		Intent i = new Intent(getApplicationContext(), TabsConfig.class);
		startActivity(i);

		// And, show the map!
		xi.putExtra("showtrip", trip.tripid);
		xi.putExtra("uploadTrip", true);
		Log.v("Jason", "Tripid: " + String.valueOf(trip.tripid));
		startActivity(xi);
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		TripDetailActivity.this.finish();

	}

	void finishRecording() {
		tripid = recordingService.finishRecording();
	}

	void resetService() {
		recordingService.reset();
	}

	/* Creates the menu items */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.trip_detail, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/* Handles item selections */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.action_skip_trip_detail:
			// skip
			submit("");
			return true;
		case R.id.action_save_trip_detail:
			// save
			submit(notes.getEditableText().toString());
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// 2.0 and above
	@Override
	public void onBackPressed() {
		// skip
		submit("");
	}

	// Before 2.0
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// skip
			submit("");
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}
