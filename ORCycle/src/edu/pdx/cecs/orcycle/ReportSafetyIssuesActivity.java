package edu.pdx.cecs.orcycle;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Spinner;

public class ReportSafetyIssuesActivity extends Activity {

	private static final String MODULE_TAG = "ReportSafetyIssuesActivity";
	public static final String EXTRA_NOTE_ID = "noteId";
	public static final String EXTRA_NOTE_TYPE = "noteType";
	public static final String EXTRA_NOTE_SEVERITY = "noteSeverity";
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

	private long tripId;
	private long noteId;
	private int noteSource = EXTRA_NOTE_SOURCE_UNDEFINED;
	private int tripSource = EXTRA_TRIP_SOURCE_UNDEFINED;

	private static final String PREFS_SAFETY_ISSUE_QUESTIONS = "PREFS_SAFETY_ISSUE_QUESTIONS";
	private static final int PREF_PROBLEMS = 1;
	private static final int PREF_URGENCY = 2;
	private static final int PREF_LOCATION = 3;

	private MultiSelectionSpinner spnProblems;
	private Spinner spnUrgency;
	private Spinner spnLocation;
	private static final int USE_GPS_LOCATION_POS = 1;
	private static final int USE_CUSTOM_LOCATION_POS = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_report_safety_issues);

			loadVars(savedInstanceState);

			spnProblems = (MultiSelectionSpinner) findViewById(R.id.spn_arsi_problem);
			spnProblems.setItems(getResources().getStringArray(R.array.arsi_a_problem_type));
			spnProblems.setTitle(getResources().getString(R.string.arsi_q_problem_type_title));
			spnProblems.setOtherIndex(DbAnswers.findIndex(DbAnswers.problemType, DbAnswers.problemTypeOther));

			spnUrgency = (Spinner) findViewById(R.id.spn_arsi_urgency);
			spnLocation = (Spinner) findViewById(R.id.spn_arsi_location);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	private void loadVars(Bundle savedInstanceState) {

		if (null == savedInstanceState) {
			Intent myIntent = getIntent();
			noteId = myIntent.getLongExtra(EXTRA_NOTE_ID, EXTRA_NOTE_ID_UNDEFINED);
			noteSource = myIntent.getIntExtra(EXTRA_NOTE_SOURCE, EXTRA_NOTE_SOURCE_UNDEFINED);
			tripId = myIntent.getLongExtra(EXTRA_TRIP_ID, EXTRA_TRIP_ID_UNDEFINED);
			tripSource = myIntent.getIntExtra(EXTRA_TRIP_SOURCE, EXTRA_TRIP_SOURCE_UNDEFINED);
		}
		else {
			noteId = savedInstanceState.getLong(EXTRA_NOTE_ID, EXTRA_NOTE_ID_UNDEFINED);
			noteSource = savedInstanceState.getInt(EXTRA_NOTE_SOURCE, EXTRA_NOTE_SOURCE_UNDEFINED);
			tripId = savedInstanceState.getLong(EXTRA_TRIP_ID, EXTRA_TRIP_ID_UNDEFINED);
			tripSource = savedInstanceState.getInt(EXTRA_TRIP_SOURCE, EXTRA_TRIP_SOURCE_UNDEFINED);
		}

		if (EXTRA_NOTE_ID_UNDEFINED == noteId) {
			throw new IllegalArgumentException(MODULE_TAG + ": EXTRA_NOTE_ID undefined.");
		}

		if (!((noteSource == EXTRA_NOTE_SOURCE_MAIN_INPUT) ||(noteSource == EXTRA_NOTE_SOURCE_TRIP_MAP))) {
			throw new IllegalArgumentException(MODULE_TAG + ": EXTRA_NOTE_SOURCE invalid argument.");
		}

		// Note: these extras are used for transitioning back to the TripMapActivity if done
		if (EXTRA_TRIP_ID_UNDEFINED == tripId) {
			throw new IllegalArgumentException(MODULE_TAG + ": invalid extra - EXTRA_TRIP_ID");
		}

		if (EXTRA_TRIP_SOURCE_UNDEFINED == tripSource) {
			throw new IllegalArgumentException(MODULE_TAG + ": invalid extra - EXTRA_TRIP_SOURCE");
		}
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		try {
			super.onRestoreInstanceState(savedInstanceState);
			Log.v(MODULE_TAG, "Cycle: onRestoreInstanceState()");
			recallUiSettings();
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		try {
			Log.v(MODULE_TAG, "Cycle: onSaveInstanceState()");
			saveUiSettings();
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		finally {
			super.onSaveInstanceState(savedInstanceState);
		}
	}

	/* Creates the menu items */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		try {
			// Inflate the menu items for use in the action bar
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.report_safety_issues, menu);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		return super.onCreateOptionsMenu(menu);
	}

	/* Handles item selections */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		try {
			switch (item.getItemId()) {

			case R.id.rsia_menu_item_save_answers:

				finishReport();
				return true;
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		return super.onOptionsItemSelected(item);
	}

	private void finishReport() {

		if (MandatoryQuestionsAnswered()) {
			submitAnswers();
			if (useCustomLocation()) {
				transitionToCustomLocationActivity();
			}
			else if (setNoteLocation()) {
				transitionToNoteDetailActivity();
			}
			else {
				dialogNoGPS();
			}
		}
		else {
			AlertUserMandatoryAnswers();
		}
	}

	private boolean setNoteLocation() {
		Location location;
		if (null != (location = getLastKnownLocation())) {
			NoteData note = NoteData.fetchNote(this, noteId);
			note.setLocation(location);
			return true;
		}
		return false;
	}

	public Location getLastKnownLocation() {

		LocationManager lm = null;
		List<String> providers = null;
		Location location = null;

		if (null != (lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE))) {
			if (null != (providers = lm.getProviders(true))) {
				/* Loop over the array backwards, and if you get a location, then break out the loop*/
				for (int i = providers.size() - 1;  i >= 0; --i) {
					if (null != (location = lm.getLastKnownLocation(providers.get(i)))) {
						location.setLatitude(location.getLatitude() * 1e6);
						location.setLongitude(location.getLongitude() * 1e6);
						break;
					}
				}
			}
		}
		return location;
	}

	private boolean useCustomLocation() {
		return spnLocation.getSelectedItemPosition() == USE_CUSTOM_LOCATION_POS;
	}

	private boolean useGPSLocation() {
		return spnLocation.getSelectedItemPosition() == USE_GPS_LOCATION_POS;
	}

	/**
	 * Build dialog telling user that the GPS is not available
	 */
	private void dialogNoGPS() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getResources().getString(R.string.ara_no_gps));
		builder.setCancelable(false);
		builder.setPositiveButton(getResources().getString(R.string.ara_ok),
				new DialogNoGPS_OkListener());
		final AlertDialog alert = builder.create();
		alert.show();
	}

	private final class DialogNoGPS_OkListener implements DialogInterface.OnClickListener {
		public void onClick(final DialogInterface dialog, final int id) {
			dialog.cancel();
		}
	}

	private boolean MandatoryQuestionsAnswered() {

		return ((spnProblems.getSelectedIndicies().size() > 0) &&
				(spnUrgency.getSelectedItemPosition() > 0) &&
				(spnLocation.getSelectedItemPosition() > 0));
	}

	private void AlertUserMandatoryAnswers() {
		AlertUserMandatoryAnswersDialog dialog = new AlertUserMandatoryAnswersDialog(this);
		dialog.show();
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
	// *                      Saving & Recalling UI Settings
	// *********************************************************************************

	/**
	 * Saves UI settings to preferences file
	 */
	private void saveUiSettings() {

		try {
			SpinnerPreferences prefs = new SpinnerPreferences(getSharedPreferences(PREFS_SAFETY_ISSUE_QUESTIONS, Context.MODE_PRIVATE));
			prefs.save(spnProblems,  PREF_PROBLEMS);
			prefs.save(spnUrgency,  PREF_URGENCY);
			prefs.save(spnLocation, PREF_LOCATION);
			prefs.commit();
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	/**
	 * Recalls UI settings from preferences file
	 */
	private void recallUiSettings() {

		try {
			SpinnerPreferences prefs = new SpinnerPreferences(getSharedPreferences(PREFS_SAFETY_ISSUE_QUESTIONS, Context.MODE_PRIVATE) );

			prefs.recall(spnProblems, PREF_PROBLEMS);
			prefs.recall(spnUrgency, PREF_URGENCY);
			prefs.recall(spnLocation, PREF_LOCATION);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	// *********************************************************************************
	// *                   Saving Spinner Settings to Database
	// *********************************************************************************

	/**
	 * Saves UI settings to database
	 */
	private void submitAnswers() {

		DbAdapter dbAdapter = new DbAdapter(this);
		dbAdapter.open();

		// This activity can be entered into a number of times with
		// the same noteId so we always delete any previous submissions
		dbAdapter.deleteNoteAnswers(noteId);

		SpinnerAdapter spAdapter = new SpinnerAdapter(dbAdapter, noteId);

		// Enter the user selections into the local database
		try {
			spAdapter.put(spnProblems,  DbQuestions.NOTE_CONFLICT,
							   DbAnswers.problemType, DbAnswers.problemTypeOther);

			spAdapter.put(spnUrgency , DbQuestions.NOTE_SEVERITY,
					   DbAnswers.problemSeverity);

			setSeverity(spnUrgency, DbAnswers.problemSeverity);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		finally {
			dbAdapter.close();
		}
	}

	private void setSeverity(Spinner spinner, int[] answerIds) {
		int answerIndex = spinner.getSelectedItemPosition() - 1;

		if (answerIndex >= 0) {
			int noteSeverity = answerIds[answerIndex];
			DbAdapter mDb = null;
			try {
				mDb = new DbAdapter(this);
				mDb.open();
				mDb.updateNoteSeverity(noteId, noteSeverity);
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
			finally {
				if (null != mDb)
					mDb.close();
			}
		}
	}

    // *********************************************************************************
	// *                    Transitioning to other activities
	// *********************************************************************************

	private void transitionToPreviousActivity() {
		// Cancel
		if (noteSource == EXTRA_NOTE_SOURCE_MAIN_INPUT) {
			transitionToTabsConfigActivity();
		}
		else if (noteSource == EXTRA_NOTE_SOURCE_TRIP_MAP) {
			transitionToTripMapActivity();
		}
	}

	private void transitionToTripMapActivity() {
		Intent intent = new Intent(this, TripMapActivity.class);
		intent.putExtra(TripMapActivity.EXTRA_TRIP_ID, tripId);
		intent.putExtra(TripMapActivity.EXTRA_IS_NEW_TRIP, true);
		intent.putExtra(TripMapActivity.EXTRA_TRIP_SOURCE, TripMapActivity.EXTRA_TRIP_SOURCE_TRIP_QUESTIONS);
		startActivity(intent);
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		finish();
	}

	private void transitionToTabsConfigActivity() {

		Intent intent = new Intent(this, TabsConfig.class);

		intent.putExtra(TabsConfig.EXTRA_SHOW_FRAGMENT, TabsConfig.FRAG_INDEX_MAIN_INPUT);
		startActivity(intent);
		finish();
		overridePendingTransition(android.R.anim.fade_in, R.anim.slide_out_down);
	}

	private void transitionToNoteDetailActivity() {

		// Create intent to go to the NoteDetailActivity
		Intent intent = new Intent(this, NoteDetailActivity.class);
		intent.putExtra(NoteDetailActivity.EXTRA_NOTE_ID, noteId);
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

	private void transitionToCustomLocationActivity() {

		// Create intent to go to the NoteDetailActivity
		Intent intent = new Intent(this, CustomLocationActivity.class);
		intent.putExtra(NoteDetailActivity.EXTRA_NOTE_ID, noteId);
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
}
