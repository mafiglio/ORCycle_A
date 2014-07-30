package edu.pdx.cecs.orcycle;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.EditText;

public class TripDetailActivity extends Activity {

	private static final String MODULE_TAG = "TripDetailActivity";

	public static final String EXTRA_TRIP_ID = "EXTRA_TRIP_ID";
	private static final int EXTRA_TRIP_ID_UNDEFINED = -1;

	// Reference to recording service;
	private IRecordService recordingService = null;

	private long tripId = EXTRA_TRIP_ID_UNDEFINED;
	private EditText tripComment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		try {
			setContentView(R.layout.activity_trip_detail);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

			// Note: these extras are used for transitioning back to the TripMapActivity if done
			if (EXTRA_TRIP_ID_UNDEFINED == (tripId = getIntent().getLongExtra(EXTRA_TRIP_ID, EXTRA_TRIP_ID_UNDEFINED))) {
				throw new IllegalArgumentException(MODULE_TAG + ": invalid extra - EXTRA_TRIP_ID");
			}

			// get reference to recording service
			recordingService = MyApplication.getInstance().getRecordingService();

			tripComment = (EditText) findViewById(R.id.editTextTripDetail);
			this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	// submit btn is only activated after the service.finishedRecording() is
	// completed.
	void submit(String notesToUpload) {

		try {
			recordingService.finishRecording();

			TripData trip = TripData.fetchTrip(TripDetailActivity.this, tripId);

			SimpleDateFormat sdfStart = new SimpleDateFormat("MMMM d, y  HH:mm a");
			String fancyStartTime = sdfStart.format(trip.getStartTime());
			Log.v(MODULE_TAG, "Start: " + fancyStartTime);

			SimpleDateFormat sdfDuration = new SimpleDateFormat("HH:mm:ss");
			sdfDuration.setTimeZone(TimeZone.getTimeZone("UTC"));
			Double endTime = trip.getEndTime();
			Double startTime = trip.getStartTime();
			String duration = sdfDuration.format(endTime - startTime);

			String fancyEndInfo = String.format("%1.1f miles, %s,  %s",
					(0.0006212f * trip.distance), duration, notesToUpload);

			// Save the trip details to the phone database. W00t!
			trip.updateTrip(fancyStartTime, fancyEndInfo, notesToUpload);
			trip.updateTripStatus(TripData.STATUS_COMPLETE);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		try {
			recordingService.reset();
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}

		// Show the map
		transitionToTripMapActivity();
	}

	/* Creates the menu items */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		try {
			// Inflate the menu items for use in the action bar
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.trip_detail, menu);
			return super.onCreateOptionsMenu(menu);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		return false;
	}

	/* Handles item selections */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		try {
			// Handle presses on the action bar items
			switch (item.getItemId()) {
			case R.id.action_skip_trip_detail:
				// skip
				submit("");
				return true;
			case R.id.action_save_trip_detail:
				// save
				submit(tripComment.getEditableText().toString());
				return true;
			default:
				return super.onOptionsItemSelected(item);
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		return false;
	}

	// 2.0 and above
	@Override
	public void onBackPressed() {
		try {
			// skip
			transitionToTripQuestionActivity();
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	private void transitionToTripMapActivity() {
		Intent intent = new Intent(this, TripMapActivity.class);
		intent.putExtra(TripMapActivity.EXTRA_TRIP_ID, tripId);
		intent.putExtra(TripMapActivity.EXTRA_IS_NEW_TRIP, true);
		intent.putExtra(TripMapActivity.EXTRA_TRIP_SOURCE, TripMapActivity.EXTRA_TRIP_SOURCE_MAIN_INPUT);
		startActivity(intent);
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		finish();
	}

	private void transitionToTripQuestionActivity() {

		Intent intent = new Intent(this, TripQuestionsActivity.class);
		intent.putExtra(TripQuestionsActivity.EXTRA_TRIP_ID, tripId);
		startActivity(intent);
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		finish();
	}

}
