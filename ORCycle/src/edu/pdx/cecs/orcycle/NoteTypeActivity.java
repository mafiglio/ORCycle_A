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

public class NoteTypeActivity extends Activity {

	private static final String MODULE_TAG = "NoteTypeActivity";

	public static final String EXTRA_NOTE_ID = "noteid";
	public static final String EXTRA_NOTE_TYPE = "noteType";
	public static final String EXTRA_NOTE_SOURCE = "noteSource";
	public static final int EXTRA_NOTE_ID_UNDEFINED = -1;
	public static final int EXTRA_NOTE_TYPE_UNDEFINED = -1;
	public static final int EXTRA_NOTE_SOURCE_UNDEFINED = -1;
	public static final int EXTRA_NOTE_SOURCE_MAIN_INPUT = 0;
	public static final int EXTRA_NOTE_SOURCE_TRIP_MAP = 1;

	public static final String EXTRA_TRIP_ID = "EXTRA_TRIP_ID";
	private static final int EXTRA_TRIP_ID_UNDEFINED = -1;
	public static final String EXTRA_TRIP_SOURCE = "tripSource";
	private static final int EXTRA_TRIP_SOURCE_UNDEFINED = -1;
	public static final int EXTRA_TRIP_SOURCE_MAIN_INPUT = 0;
	public static final int EXTRA_TRIP_SOURCE_SAVED_TRIPS = 1;

	private int noteType;
	private long noteid;
	private int noteSource = EXTRA_NOTE_SOURCE_UNDEFINED;
	private long tripId;
	private int tripSource = EXTRA_TRIP_SOURCE_UNDEFINED;

	private final HashMap<Integer, String> noteTypeDescriptions = new HashMap<Integer, String>();

	private String[] values;

	private MenuItem saveMenuItem;

	// Set up the purpose buttons to be one-click only
	private void prepareNoteTypeButtons() {
		// Note Issue
		noteTypeDescriptions.put(0, getResources().getString(R.string.note_pavement_issue_details));
		noteTypeDescriptions.put(1, getResources().getString(R.string.note_traffic_signal_details));
		noteTypeDescriptions.put(2, getResources().getString(R.string.note_enforcement_details));
		noteTypeDescriptions.put(3, getResources().getString(R.string.note_needs_bike_rack_details));
		noteTypeDescriptions.put(4, getResources().getString(R.string.note_bike_lane_issue_details));
		noteTypeDescriptions.put(5, getResources().getString(R.string.note_note_issue_details));

		// Note Asset
		noteTypeDescriptions.put(6,getResources().getString(R.string.note_bike_rack_details));
		noteTypeDescriptions.put(7,getResources().getString(R.string.note_bike_shop_details));
		noteTypeDescriptions.put(8,getResources().getString(R.string.note_public_restroom_details));
		noteTypeDescriptions.put(9,getResources().getString(R.string.note_secret_passage_details));
		noteTypeDescriptions.put(10,getResources().getString(R.string.note_water_fountain_details));
		noteTypeDescriptions.put(11,getResources().getString(R.string.note_note_asset_details));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_note_type);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		// get input values for this view
		Intent myIntent = getIntent();

		noteid = myIntent.getLongExtra(EXTRA_NOTE_ID, EXTRA_NOTE_ID_UNDEFINED);
		if (EXTRA_NOTE_ID_UNDEFINED == noteid) {
			throw new IllegalArgumentException(MODULE_TAG + ": EXTRA_NOTE_ID undefined.");
		}

		noteSource = myIntent.getIntExtra(EXTRA_NOTE_SOURCE, EXTRA_NOTE_SOURCE_UNDEFINED);
		if (!((noteSource == EXTRA_NOTE_SOURCE_MAIN_INPUT) ||(noteSource == EXTRA_NOTE_SOURCE_TRIP_MAP))) {
			throw new IllegalArgumentException(MODULE_TAG + ": EXTRA_NOTE_SOURCE invalid argument.");
		}

		noteType = myIntent.getIntExtra(EXTRA_NOTE_TYPE, EXTRA_NOTE_TYPE_UNDEFINED);

		// Note: these extras are used for transitioning back to the TripMapActivity if done
		if (EXTRA_TRIP_ID_UNDEFINED == (tripId = myIntent.getLongExtra(EXTRA_TRIP_ID, EXTRA_TRIP_ID_UNDEFINED))) {
			throw new IllegalArgumentException(MODULE_TAG + ": invalid extra - EXTRA_TRIP_ID");
		}

		if (EXTRA_TRIP_SOURCE_UNDEFINED == (tripSource = myIntent.getIntExtra(EXTRA_TRIP_SOURCE, EXTRA_TRIP_SOURCE_UNDEFINED))) {
			throw new IllegalArgumentException(MODULE_TAG + ": invalid extra - EXTRA_TRIP_SOURCE");
		}

		prepareNoteTypeButtons();

		final ListView listView = (ListView) findViewById(R.id.listViewNoteType);
		values = new String[] {
				getResources().getString(R.string.note_pavement_issue),
				getResources().getString(R.string.note_traffic_signal),
				getResources().getString(R.string.note_enforcement),
				getResources().getString(R.string.note_needs_bike_rack),
				getResources().getString(R.string.note_bike_lane_issue),
				getResources().getString(R.string.note_note_issue),
				getResources().getString(R.string.note_bike_rack),
				getResources().getString(R.string.note_bike_shop),
				getResources().getString(R.string.note_public_restroom),
				getResources().getString(R.string.note_secret_passage),
				getResources().getString(R.string.note_water_fountain),
				getResources().getString(R.string.note_note_asset)};
		// final ArrayList<String> list = new ArrayList<String>();
		// for (int i = 0; i < values.length; ++i) {
		// list.add(values[i]);
		// }
		NoteTypeAdapter adapter = new NoteTypeAdapter(this, values);
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
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				// view.setSelected(true);
				// view.setBackgroundDrawable(parent.getResources().getDrawable(R.drawable.bg_key));
				clearSelection();
				oldSelection = view;
				view.setBackgroundColor(Color.parseColor("#ff33b5e5"));
				// view.setBackgroundDrawable(parent.getResources().getDrawable(R.drawable.bg_key));
				noteType = position;
				// Log.v("Jason", purpose);
				((TextView) findViewById(R.id.textViewNoteTypeDesc))
						.setText(Html.fromHtml(noteTypeDescriptions
								.get(position)));
				saveMenuItem.setEnabled(true);
				// highlight
			}

		});

		this.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}

	/* Creates the menu items */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.note_type, menu);
		saveMenuItem = menu.getItem(1);
		saveMenuItem.setEnabled(false);
		return super.onCreateOptionsMenu(menu);
	}

	/* Handles item selections */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// Handle presses on the action bar items
		switch (item.getItemId()) {

		case R.id.action_cancel_note_type:

			// Cancel the note and move to the previous screen
			cancelNote();
			transitionToPreviousActivity();
			return true;

		case R.id.action_save_note_type:

			// move to the next view
			transitionToNoteDetailActivity();
			return true;

		default:

			return super.onOptionsItemSelected(item);
		}
	}

	// 2.0 and above
	@Override
	public void onBackPressed() {
		cancelNote();
		transitionToPreviousActivity();
	}

	private void cancelNote() {
		NoteData note;
		try {
			if (null != (note = NoteData.fetchNote(NoteTypeActivity.this, noteid))) {
				note.dropNote();
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	private void transitionToPreviousActivity() {
		// Cancel
		if (noteSource == EXTRA_NOTE_SOURCE_MAIN_INPUT) {
			transitionToTabsConfigActivity();
		}
		else if (noteSource == EXTRA_NOTE_SOURCE_TRIP_MAP) {
			transitionToTripMapActivity();
		}
	}

	private void transitionToNoteDetailActivity() {

		// Create intent to go to the NoteDetailActivity
		Intent intent = new Intent(this, NoteDetailActivity.class);
		intent.putExtra(NoteDetailActivity.EXTRA_NOTE_TYPE, noteType);
		intent.putExtra(NoteDetailActivity.EXTRA_NOTE_ID, noteid);
		intent.putExtra(NoteDetailActivity.EXTRA_NOTE_SOURCE, noteSource);

		// the NoteType activity needs these when the back button
		// is pressed and we have to restart this activity
		intent.putExtra(NoteDetailActivity.EXTRA_TRIP_ID, tripId);
		intent.putExtra(NoteDetailActivity.EXTRA_TRIP_SOURCE, tripSource);


		// Exit this activity
		startActivity(intent);
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		finish();
	}

	private void transitionToTripMapActivity() {

		// Create intent to go back to the TripMapActivity
		Intent intent = new Intent(this, TripMapActivity.class);
		intent.putExtra(TripMapActivity.EXTRA_TRIP_ID, tripId);
		intent.putExtra(TripMapActivity.EXTRA_TRIP_SOURCE, tripSource);

		// Exit this activity
		startActivity(intent);
		overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
		finish();
	}

	private void transitionToTabsConfigActivity() {

		// Create intent to go back to the recording screen in the Tabsconfig activity
		Intent intent = new Intent(this, TabsConfig.class);
		intent.putExtra(TabsConfig.EXTRA_SHOW_FRAGMENT, TabsConfig.FRAG_INDEX_MAIN_INPUT);

		// Exit this activity
		startActivity(intent);
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		finish();
	}
}
