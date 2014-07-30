package edu.pdx.cecs.orcycle;

import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class RateSegmentActivity extends Activity {

	private static final String MODULE_TAG = "RateSegmentActivity";

	public static final String EXTRA_TRIP_ID = "tripid";
	private static final int EXTRA_TRIP_ID_UNDEFINED = -1;
	public static final String EXTRA_TRIP_SOURCE = "tripSource";
	private static final int EXTRA_TRIP_SOURCE_UNDEFINED = -1;
	public static final String EXTRA_SEGMENT_START_INDEX = "start";
	public static final String EXTRA_SEGMENT_END_INDEX = "end";
	private static final int EXTRA_SEGMENT_INDEX_UNDEFINED = -1;
	private static final int RATING_UNDEFINED = -1;

	private long tripId = EXTRA_TRIP_ID_UNDEFINED;
	private int tripSource = EXTRA_TRIP_SOURCE_UNDEFINED;
	private int rating;
	private int segmentStartIndex = EXTRA_SEGMENT_INDEX_UNDEFINED;
	private int segmentEndIndex = EXTRA_SEGMENT_INDEX_UNDEFINED;

	private final HashMap<Integer, String> rateSegmentDescriptions = new HashMap<Integer, String>();

	private String[] ratings;

	private MenuItem saveMenuItem;

	// Set up the purpose buttons to be one-click only
	void prepareSegmentRatingButtons() {
		rateSegmentDescriptions.put(0, getResources().getString(R.string.segment_rating_details_a));
		rateSegmentDescriptions.put(1, getResources().getString(R.string.segment_rating_details_b));
		rateSegmentDescriptions.put(2, getResources().getString(R.string.segment_rating_details_c));
		rateSegmentDescriptions.put(3, getResources().getString(R.string.segment_rating_details_d));
		rateSegmentDescriptions.put(4, getResources().getString(R.string.segment_rating_details_e));
		rateSegmentDescriptions.put(5, getResources().getString(R.string.segment_rating_details_f));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		try {
			setContentView(R.layout.activity_rate_segment);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

			// Set up note type buttons
			prepareSegmentRatingButtons();

			// gets the previously created intent
			Intent myIntent = getIntent();

			// Note: these extras are used for transitioning back to the TripMapActivity if done
			if (EXTRA_TRIP_ID_UNDEFINED == (tripId = myIntent.getLongExtra(EXTRA_TRIP_ID, EXTRA_TRIP_ID_UNDEFINED))) {
				throw new IllegalArgumentException(MODULE_TAG + ": invalid extra - EXTRA_TRIP_ID");
			}

			if (EXTRA_TRIP_SOURCE_UNDEFINED == (tripSource = myIntent.getIntExtra(EXTRA_TRIP_SOURCE, EXTRA_TRIP_SOURCE_UNDEFINED))) {
				throw new IllegalArgumentException(MODULE_TAG + ": invalid extra - EXTRA_TRIP_SOURCE");
			}

			if (EXTRA_SEGMENT_INDEX_UNDEFINED == (segmentStartIndex = myIntent.getIntExtra (RateSegmentActivity.EXTRA_SEGMENT_START_INDEX, EXTRA_SEGMENT_INDEX_UNDEFINED))) {
				throw new IllegalArgumentException(MODULE_TAG + ": invalid extra - EXTRA_SEGMENT_START_INDEX");
			}

			if (EXTRA_SEGMENT_INDEX_UNDEFINED == (segmentEndIndex = myIntent.getIntExtra (RateSegmentActivity.EXTRA_SEGMENT_END_INDEX,   EXTRA_SEGMENT_INDEX_UNDEFINED))) {
				throw new IllegalArgumentException(MODULE_TAG + ": invalid extra - EXTRA_SEGMENT_END_INDEX");
			}
			rating = RATING_UNDEFINED;

			final ListView listView = (ListView) findViewById(R.id.listViewRateSegment);
			ratings = new String[] {
					getResources().getString(R.string.segment_rating_a),
					getResources().getString(R.string.segment_rating_b),
					getResources().getString(R.string.segment_rating_c),
					getResources().getString(R.string.segment_rating_d),
					getResources().getString(R.string.segment_rating_e),
					getResources().getString(R.string.segment_rating_f)};

			RateSegmentAdapter adapter = new RateSegmentAdapter(this, ratings);
			listView.setAdapter(adapter);
			// set default
			listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				private View oldSelection = null;

				public void clearSelection() {
					if (oldSelection != null) {
						oldSelection.setBackgroundColor(Color.parseColor("#ffffff"));
					}
				}

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					clearSelection();
					oldSelection = view;
					view.setBackgroundColor(Color.parseColor("#ff33b5e5"));
					rating = position;
					((TextView) findViewById(R.id.textViewRateSegmentDesc)).setText(Html.fromHtml(rateSegmentDescriptions.get(position)));
					saveMenuItem.setEnabled(true);
				}

			});

			this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
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
		inflater.inflate(R.menu.rate_segment, menu);
		saveMenuItem = menu.getItem(1);
		saveMenuItem.setEnabled(false);
		return super.onCreateOptionsMenu(menu);
	}

	/* Handles item selections */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		try {
			// Handle presses on the action bar items
			switch (item.getItemId()) {

			case R.id.action_cancel_rate_segment:

				// Cancel segment rating
				transitionToTripMapActivity();
				return true;

			case R.id.action_save_rate_segment:

				// Save segment rating
				SegmentData segment;
				if (null != (segment = SegmentData.createSegment(RateSegmentActivity.this))) {

					segment.updateSegment(tripId, rating, "", segmentStartIndex, segmentEndIndex);

					// Upload segment entity
					Uploader uploader = new Uploader(RateSegmentActivity.this);
					uploader.execute(segment.getSegmentId());
				}
				else {
					// Alert user
					Toast.makeText(getBaseContext(), "Unable to save data to local database.", Toast.LENGTH_SHORT).show();
				}

				// Create intent for next screen
				transitionToRateSegmentDetailActivity();
				return true;

			default:
				return super.onOptionsItemSelected(item);
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
			return false;
		}
	}

	// 2.0 and above
	@Override
	public void onBackPressed() {
		try {
			transitionToTripMapActivity();
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	private void transitionToTripMapActivity() {

		// Create intent to go to the TripMapActivity
		Intent intent = new Intent(this, TripMapActivity.class);
		intent.putExtra(TripMapActivity.EXTRA_TRIP_ID, tripId);
		intent.putExtra(TripMapActivity.EXTRA_TRIP_SOURCE, tripSource);

		// Exit this activity
		startActivity(intent);
		finish();
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	}

	private void transitionToRateSegmentDetailActivity() {

		// Create intent for next screen
		Intent intent = new Intent(this, RateSegmentDetailActivity.class);
		intent.putExtra(RateSegmentDetailActivity.EXTRA_TRIP_ID, tripId);
		intent.putExtra(RateSegmentDetailActivity.EXTRA_TRIP_SOURCE, tripSource);
		intent.putExtra(RateSegmentDetailActivity.EXTRA_RATING, rating);
		intent.putExtra(RateSegmentDetailActivity.EXTRA_SEGMENT_START_INDEX, segmentStartIndex);
		intent.putExtra(RateSegmentDetailActivity.EXTRA_SEGMENT_END_INDEX, segmentEndIndex);

		// Exit this activity
		startActivity(intent);
		finish();
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	}
}
