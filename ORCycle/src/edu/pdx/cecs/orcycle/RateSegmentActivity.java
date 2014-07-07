package edu.pdx.cecs.orcycle;

import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
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
	public static final String EXTRA_START_INDEX = "start";
	public static final String EXTRA_END_INDEX = "end";


	private final long segmentId = -1;
	private long tripId = -1;
	private int rating;
	private int tripStartIndex = -1;
	private int tripEndIndex = -1;

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
			tripId         = myIntent.getLongExtra(RateSegmentActivity.EXTRA_TRIP_ID,     -1);
			tripStartIndex = myIntent.getIntExtra (RateSegmentActivity.EXTRA_START_INDEX, -1);
			tripEndIndex   = myIntent.getIntExtra (RateSegmentActivity.EXTRA_END_INDEX,   -1);
			rating = -1;

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
		Intent intent;
		try {
			// Handle presses on the action bar items
			switch (item.getItemId()) {

			case R.id.action_cancel_rate_segment:

				// Alert user
				Toast.makeText(getBaseContext(), "Segment rating discarded.", Toast.LENGTH_SHORT).show();

				// Cancel
				intent = new Intent(RateSegmentActivity.this, TripMapActivity.class);
				intent.putExtra("showtrip", tripId);
				finishActivity(intent);
				return true;

			case R.id.action_save_rate_segment:

				// Create segment entity
				SegmentData segment;
				if (null != (segment = SegmentData.createSegment(RateSegmentActivity.this))) {

					segment.updateSegment(tripId, rating, "", tripStartIndex, tripEndIndex);

					// Upload segment entity
					Uploader uploader = new Uploader(RateSegmentActivity.this);
					uploader.execute(segment.getSegmentId());
				}
				else {
					// Alert user
					Toast.makeText(getBaseContext(), "Unable to save data to local database.", Toast.LENGTH_SHORT).show();
				}

				// Create intent for next screen
				intent = new Intent(RateSegmentActivity.this, RateSegmentDetailActivity.class);
				intent.putExtra("showtrip", tripId);
				finishActivity(intent);
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
		finishActivity(null);
	}

	// Before 2.0
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finishActivity(null);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void finishActivity(Intent intent) {
		if (null != intent) {
			startActivity(intent);
		}
		RateSegmentActivity.this.finish();
		overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
	}

}
