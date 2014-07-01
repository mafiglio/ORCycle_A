package edu.pdx.cecs.orcycle;

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
import android.widget.Toast;

public class RateSegmentDetailActivity extends Activity {

	private static final String MODULE_TAG = "RateSegmentDetailActivity";

	public static final String EXTRA_SEGMENT_ID = "segmentid";
	public static final String EXTRA_TRIP_ID = "tripid";
	public static final String EXTRA_RATING = "rating";
	public static final String EXTRA_START_INDEX = "start";
	public static final String EXTRA_END_INDEX = "end";

	private EditText txtRateSegmentDetails;

	private long tripId;
	private int rating;
	private int tripStartIndex;
	private int tripEndIndex;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_rate_segment_detail);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		// gets the previously created intent
		Intent myIntent = getIntent();
		tripId         = myIntent.getLongExtra(RateSegmentDetailActivity.EXTRA_TRIP_ID,     -1);
		rating         = myIntent.getIntExtra (RateSegmentDetailActivity.EXTRA_RATING,      -1);
		tripStartIndex = myIntent.getIntExtra (RateSegmentDetailActivity.EXTRA_START_INDEX, -1);
		tripEndIndex   = myIntent.getIntExtra (RateSegmentDetailActivity.EXTRA_END_INDEX,   -1);

		// get reference to edit text widget
		txtRateSegmentDetails = (EditText) findViewById(R.id.editTextRateSegmentDetail);

		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
	}

	void submit(String details) {

		try {
			// Store rate segment data to database
			SegmentData segment;
			if (null != (segment = SegmentData.createSegment(RateSegmentDetailActivity.this))) {

				segment.updateSegment(tripId, rating, details, tripStartIndex, tripEndIndex);

				// Upload segment entity
				Uploader uploader = new Uploader(RateSegmentDetailActivity.this);
				uploader.execute(segment.getSegmentId());
			}
			else {
				// Alert user that we couldn't store rating information
				Toast.makeText(getBaseContext(), "Unable to rating to local database.", Toast.LENGTH_SHORT).show();
			}

			// Create intent for next screen
			Intent intent = new Intent(RateSegmentDetailActivity.this, TabsConfig.class);
			startActivity(intent);
			overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			RateSegmentDetailActivity.this.finish();
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	/* Creates the menu items */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.rate_segment_detail, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/* Handles item selections */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.action_skip_rate_segment_detail:
			// skip
			submit("");
			return true;
		case R.id.action_save_rate_segment_detail:
			// save
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
		RateSegmentDetailActivity.this.finish();
	}

	// Before 2.0
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// skip
			RateSegmentDetailActivity.this.finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
