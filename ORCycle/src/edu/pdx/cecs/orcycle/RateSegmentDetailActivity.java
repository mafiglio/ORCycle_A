package edu.pdx.cecs.orcycle;

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

public class RateSegmentDetailActivity extends Activity {

	private static final String MODULE_TAG = "RateSegmentDetailActivity";

	public static final String EXTRA_SEGMENT_ID = "segmentid";
	public static final String EXTRA_TRIP_ID = "tripid";
	private static final int EXTRA_TRIP_ID_UNDEFINED = -1;
	public static final String EXTRA_TRIP_SOURCE = "tripSource";
	private static final int EXTRA_TRIP_SOURCE_UNDEFINED = -1;
	public static final String EXTRA_RATING = "rating";
	public static final String EXTRA_SEGMENT_START_INDEX = "start";
	public static final String EXTRA_SEGMENT_END_INDEX = "end";
	public static final String EXTRA_FROM_TRIP_DETAILS = "fromTripDetails";

	private long tripId = EXTRA_TRIP_ID_UNDEFINED;
	private int tripSource = EXTRA_TRIP_SOURCE_UNDEFINED;
	private int rating;
	private int segmentStartIndex;
	private int segmentEndIndex;
	private final boolean fromTripDetails = false;

	private EditText txtRateSegmentDetails;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			setContentView(R.layout.activity_rate_segment_detail);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

			// gets the previously created intent
			Intent intent = getIntent();

			// Note: these extras are used for transitioning back to the TripMapActivity if done
			if (EXTRA_TRIP_ID_UNDEFINED == (tripId = intent.getLongExtra(EXTRA_TRIP_ID, EXTRA_TRIP_ID_UNDEFINED))) {
				throw new IllegalArgumentException(MODULE_TAG + ": invalid extra - EXTRA_TRIP_ID");
			}

			if (EXTRA_TRIP_SOURCE_UNDEFINED == (tripSource = intent.getIntExtra(EXTRA_TRIP_SOURCE, EXTRA_TRIP_SOURCE_UNDEFINED))) {
				throw new IllegalArgumentException(MODULE_TAG + ": invalid extra - EXTRA_TRIP_SOURCE");
			}

			rating          = intent.getIntExtra (RateSegmentDetailActivity.EXTRA_RATING,      -1);
			segmentStartIndex  = intent.getIntExtra (RateSegmentDetailActivity.EXTRA_SEGMENT_START_INDEX, -1);
			segmentEndIndex    = intent.getIntExtra (RateSegmentDetailActivity.EXTRA_SEGMENT_END_INDEX,   -1);

			// get reference to edit text widget
			txtRateSegmentDetails = (EditText) findViewById(R.id.editTextRateSegmentDetail);

			this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	void submit(String details) {
		try {
			// Store rate segment data to database
			SegmentData segment;
			if (null != (segment = SegmentData.createSegment(RateSegmentDetailActivity.this))) {

				segment.updateSegment(tripId, rating, details, segmentStartIndex, segmentEndIndex);

				// Upload segment entity
				Uploader uploader = new Uploader(RateSegmentDetailActivity.this);
				uploader.execute(segment.getSegmentId());
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}

		try {
			// Transition to the TripMapActivity
			transitionToTripMapActivity();
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	/**
	 * Creates the menu items
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		try {
			// Inflate the menu items for use in the action bar
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.rate_segment_detail, menu);
			return super.onCreateOptionsMenu(menu);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		return false;
	}

	/**
	 *  Handles item selections
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// Handle presses on the action bar items
		switch (item.getItemId()) {

		case R.id.action_skip_rate_segment_detail: // Skip

			submit("");
			return true;

		case R.id.action_save_rate_segment_detail: // Save

			submit(txtRateSegmentDetails.getEditableText().toString());
			return true;

		default:

			return super.onOptionsItemSelected(item);
		}
	}

	// 2.0 and above
	@Override
	public void onBackPressed() {
		// skip
		transitionToRateSegmentActivity();
	}

	private void transitionToTripMapActivity() {

		// Create intent to go to the TripMapActivity
		Intent intent = new Intent(this, TripMapActivity.class);
		intent.putExtra(TripMapActivity.EXTRA_TRIP_ID, tripId);
		intent.putExtra(TripMapActivity.EXTRA_TRIP_SOURCE, tripSource);

		// Exit this activity
		startActivity(intent);
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		finish();
	}

	private void transitionToRateSegmentActivity() {

		Intent intent = new Intent(this, RateSegmentActivity.class);
		intent.putExtra(RateSegmentActivity.EXTRA_TRIP_ID, tripId);
		intent.putExtra(RateSegmentActivity.EXTRA_TRIP_SOURCE, tripSource);
		intent.putExtra(RateSegmentActivity.EXTRA_SEGMENT_START_INDEX, segmentStartIndex);
		intent.putExtra(RateSegmentActivity.EXTRA_SEGMENT_END_INDEX, segmentEndIndex);

		startActivity(intent);
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		finish();
	}
}
