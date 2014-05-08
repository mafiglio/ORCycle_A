package edu.pdx.cecs.orcycle;

import java.util.HashMap;

import edu.pdx.cecs.orcycle.R;
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

public class NoteTypeActivity extends Activity {
	// HashMap<Integer, ToggleButton> purpButtons = new HashMap<Integer,
	// ToggleButton>();
	int noteType;

	long noteid;

	int isRecording;

	HashMap<Integer, String> noteTypeDescriptions = new HashMap<Integer, String>();

	String[] values;

	private MenuItem saveMenuItem;

	// Set up the purpose buttons to be one-click only
	void prepareNoteTypeButtons() {
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

		// Set up note type buttons
		noteType = -1;
		prepareNoteTypeButtons();

		Intent myIntent = getIntent(); // gets the previously created intent
		noteid = myIntent.getLongExtra("noteid", 0);

		isRecording = myIntent.getIntExtra("isRecording", -1);

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
			// cancel
			Toast.makeText(getBaseContext(), "Note discarded.",
					Toast.LENGTH_SHORT).show();

			// Cancel
			NoteData note = NoteData.fetchNote(NoteTypeActivity.this, noteid);
			Log.v("Jason", "Note id: " + noteid);
			note.dropNote();

			NoteTypeActivity.this.finish();
			
			overridePendingTransition(android.R.anim.slide_in_left,
					android.R.anim.slide_out_right);
			return true;
		case R.id.action_save_note_type:
			// move to next view
			Intent intentToNoteDetail = new Intent(NoteTypeActivity.this,
					NoteDetailActivity.class);
			intentToNoteDetail.putExtra("noteType", noteType);

			intentToNoteDetail.putExtra("noteid", noteid);

			Log.v("Jason", "Note ID in NoteType: " + noteid);

			if (isRecording == 1) {
				intentToNoteDetail.putExtra("isRecording", 1);
			} else {
				intentToNoteDetail.putExtra("isRecording", 0);
			}

			startActivity(intentToNoteDetail);
			overridePendingTransition(R.anim.slide_in_right,
					R.anim.slide_out_left);
			NoteTypeActivity.this.finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// 2.0 and above
	@Override
	public void onBackPressed() {
		// cancel
		Toast.makeText(getBaseContext(), "Note discarded.", Toast.LENGTH_SHORT)
				.show();

		// Cancel
		NoteData note = NoteData.fetchNote(NoteTypeActivity.this, noteid);
		Log.v("Jason", "Note id: " + noteid);
		note.dropNote();

		NoteTypeActivity.this.finish();
		
		overridePendingTransition(android.R.anim.slide_in_left,
				android.R.anim.slide_out_right);
	}

	// Before 2.0
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// cancel
			Toast.makeText(getBaseContext(), "Note discarded.",
					Toast.LENGTH_SHORT).show();

			// Cancel
			NoteData note = NoteData.fetchNote(NoteTypeActivity.this, noteid);
			Log.v("Jason", "Note id: " + noteid);
			note.dropNote();

			NoteTypeActivity.this.finish();
			
			overridePendingTransition(android.R.anim.slide_in_left,
					android.R.anim.slide_out_right);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}
