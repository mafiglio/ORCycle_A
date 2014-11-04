package edu.pdx.cecs.orcycle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;

public class ReportTypeActivity extends Activity {

	private static final String MODULE_TAG = "ReportTypeActivity";

	public static final String EXTRA_NOTE_ID = "noteId";
	public static final String EXTRA_NOTE_SOURCE = "noteSource";
	public static final int EXTRA_NOTE_ID_UNDEFINED = -1;
	public static final int EXTRA_NOTE_SOURCE_UNDEFINED = -1;
	public static final int EXTRA_NOTE_SOURCE_MAIN_INPUT = 0;
	public static final int EXTRA_NOTE_SOURCE_TRIP_MAP = 1;

	public static final String EXTRA_TRIP_ID = "EXTRA_TRIP_ID";
	private static final int EXTRA_TRIP_ID_UNDEFINED = -1;
	public static final String EXTRA_TRIP_SOURCE = "tripSource";
	private static final int EXTRA_TRIP_SOURCE_UNDEFINED = -1;
	public static final int EXTRA_TRIP_SOURCE_MAIN_INPUT = 0;
	public static final int EXTRA_TRIP_SOURCE_SAVED_TRIPS = 1;

	private static final String PREFS_NOTE_QUESTIONS = "PREFS_NOTE_QUESTIONS";

	private static final int PREF_SEVERITY = 1;
	private static final int PREF_CONFLICT = 2;
	private static final int PREF_ISSUE    = 3;

	private long noteId = -1;
	private int noteSource = EXTRA_NOTE_SOURCE_UNDEFINED;
	private long tripId;
	private int tripSource = EXTRA_TRIP_SOURCE_UNDEFINED;

	private Button btnReportAccident;
	private Button btnReportSafetyIssue;
	private Button btnCancel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);

			Log.v(MODULE_TAG, "Cycle: onCreate() - note_id = " + noteId);

			getWindow().requestFeature(Window.FEATURE_NO_TITLE);
			setContentView(R.layout.activity_report_type);

			loadVars();

			btnReportAccident = (Button) findViewById(R.id.btnArtReportAccident);
			btnReportAccident.setOnClickListener(new RtaReportAccident_OnClickListener());

			btnReportSafetyIssue = (Button) findViewById(R.id.btnArtReportSafetyIssue);
			btnReportSafetyIssue.setOnClickListener(new RtaReportSafetyIssue_OnClickListener());

			btnCancel = (Button) findViewById(R.id.btnArtCancel);
			btnCancel.setOnClickListener(new RtaCancel_OnClickListener());
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	/**
	 * Loads extras into local variables
	 */
	private void loadVars() {
		// get input values for this view
		Intent myIntent = getIntent();

		noteId = myIntent.getLongExtra(EXTRA_NOTE_ID, EXTRA_NOTE_ID_UNDEFINED);
		if (EXTRA_NOTE_ID_UNDEFINED == noteId) {
			throw new IllegalArgumentException(MODULE_TAG + ": EXTRA_NOTE_ID undefined.");
		}

		noteSource = myIntent.getIntExtra(EXTRA_NOTE_SOURCE, EXTRA_NOTE_SOURCE_UNDEFINED);
		if (!((noteSource == EXTRA_NOTE_SOURCE_MAIN_INPUT) ||(noteSource == EXTRA_NOTE_SOURCE_TRIP_MAP))) {
			throw new IllegalArgumentException(MODULE_TAG + ": EXTRA_NOTE_SOURCE invalid argument.");
		}

		// Note: these extras are used for transitioning back to the TripMapActivity if done
		tripId = myIntent.getLongExtra(EXTRA_TRIP_ID, EXTRA_TRIP_ID_UNDEFINED);
		if (EXTRA_TRIP_ID_UNDEFINED == tripId) {
			throw new IllegalArgumentException(MODULE_TAG + ": invalid extra - EXTRA_TRIP_ID");
		}

		tripSource = myIntent.getIntExtra(EXTRA_TRIP_SOURCE, EXTRA_TRIP_SOURCE_UNDEFINED);
		if (EXTRA_TRIP_SOURCE_UNDEFINED == tripSource) {
			throw new IllegalArgumentException(MODULE_TAG + ": invalid extra - EXTRA_TRIP_SOURCE");
		}
	}

	@Override
	public void onBackPressed() {
		try {
			transitionToPreviousActivity();
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	// *********************************************************************************
	// *                              Button Handlers
	// *********************************************************************************

	private final class RtaReportAccident_OnClickListener implements View.OnClickListener {

		/**
		 * Description: Handles onClick for view
		 */
		public void onClick(View v) {
			try {
				transitionToReportAccidentsActivity();
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	private final class RtaReportSafetyIssue_OnClickListener implements View.OnClickListener {

		/**
		 * Description: Handles onClick for view
		 */
		public void onClick(View v) {
			try {
				transitionToReportSafetyIssuesActivity();
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	private final class RtaCancel_OnClickListener implements View.OnClickListener {

		/**
		 * Description: Handles onClick for view
		 */
		public void onClick(View v) {
			try {
				transitionToPreviousActivity();
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	// *********************************************************************************
	// *                    Transitioning to other activities
	// *********************************************************************************

	private void transitionToReportSafetyIssuesActivity() {

		// Create intent to go back to the recording screen in the Tabsconfig activity
		Intent intent = new Intent(this, ReportSafetyIssuesActivity.class);
		intent.putExtra(NoteQuestionsActivity.EXTRA_NOTE_ID, noteId);
		intent.putExtra(NoteQuestionsActivity.EXTRA_NOTE_SOURCE, noteSource);
		intent.putExtra(NoteQuestionsActivity.EXTRA_TRIP_ID, tripId);
		intent.putExtra(NoteQuestionsActivity.EXTRA_TRIP_SOURCE, tripSource);

		// Exit this activity
		startActivity(intent);
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		finish();
	}

	private void transitionToReportAccidentsActivity() {

		// Create intent to go back to the recording screen in the Tabsconfig activity
		Intent intent = new Intent(this, ReportAccidentsActivity.class);
		intent.putExtra(NoteQuestionsActivity.EXTRA_NOTE_ID, noteId);
		intent.putExtra(NoteQuestionsActivity.EXTRA_NOTE_SOURCE, noteSource);
		intent.putExtra(NoteQuestionsActivity.EXTRA_TRIP_ID, tripId);
		intent.putExtra(NoteQuestionsActivity.EXTRA_TRIP_SOURCE, tripSource);

		// Exit this activity
		startActivity(intent);
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		finish();
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

	private void transitionToTabsConfigActivity() {

		// Create intent to go back to the recording screen in the Tabsconfig activity
		Intent intent = new Intent(this, TabsConfig.class);
		intent.putExtra(TabsConfig.EXTRA_SHOW_FRAGMENT, TabsConfig.FRAG_INDEX_MAIN_INPUT);

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
}
