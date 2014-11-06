package edu.pdx.cecs.orcycle;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;
import android.widget.Toast;

public class ReportAccidentsActivity extends Activity {

	private static final String MODULE_TAG = "ReportAccidentsActivity";
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

	public static final String EXTRA_IS_BACK = "isBack";

	private long tripId;
	private long noteId;
	private int noteSource = EXTRA_NOTE_SOURCE_UNDEFINED;
	private int tripSource = EXTRA_TRIP_SOURCE_UNDEFINED;
	private boolean isBack;

	private static final String PREFS_ACCIDENT_QUESTIONS = "PREFS_ACCIDENT_QUESTIONS";
	private static final int PREF_SEVERITY = 1;
	private static final int PREF_ACCIDENT_OBJECT = 2;
	private static final int PREF_ACCIDENT_ACTIONS = 3;
	private static final int PREF_ACCIDENT_CONTRIB = 4;
	private static final int PREF_LOCATION = 5;

	private static final int PREF_ACCIDENT_OBJECT_OTHER = 1002;
	private static final int PREF_ACCIDENT_ACTIONS_OTHER = 1003;
	private static final int PREF_ACCIDENT_CONTRIB_OTHER = 1004;


	private Spinner spnSeverity;
	private MultiSelectionSpinner spnAccidentObject;
	private MultiSelectionSpinner spnAccidentActions;
	private MultiSelectionSpinner spnAccidentContrib;
	private Spinner spnLocation;
	private final CustomLocation_OnClickListener customLocation_OnClickListener =
			new CustomLocation_OnClickListener();
	private static final int USE_GPS_LOCATION_POS = 1;
	private static final int USE_CUSTOM_LOCATION_POS = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_report_accidents);

			loadVars(savedInstanceState);

			spnSeverity = (Spinner) findViewById(R.id.spn_ara_severity_of_problem);

			spnAccidentObject = (MultiSelectionSpinner) findViewById(R.id.spn_ara_object);
			spnAccidentObject.setItems(getResources().getStringArray(R.array.ara_a_object));
			spnAccidentObject.setTitle(getResources().getString(R.string.ara_q_object));
			spnAccidentObject.setOtherIndex(DbAnswers.findIndex(DbAnswers.accidentObject, DbAnswers.accidentObjectOther));

			spnAccidentActions = (MultiSelectionSpinner) findViewById(R.id.spn_ara_actions);
			spnAccidentActions.setItems(getResources().getStringArray(R.array.ara_a_actions));
			spnAccidentActions.setTitle(getResources().getString(R.string.ara_q_actions));
			spnAccidentActions.setOtherIndex(DbAnswers.findIndex(DbAnswers.accidentAction, DbAnswers.accidentActionOther));

			spnAccidentContrib = (MultiSelectionSpinner) findViewById(R.id.spn_ara_contributers);
			spnAccidentContrib.setItems(getResources().getStringArray(R.array.ara_a_contributers));
			spnAccidentContrib.setTitle(getResources().getString(R.string.ara_q_contributed));
			spnAccidentContrib.setOtherIndex(DbAnswers.findIndex(DbAnswers.accidentContrib, DbAnswers.accidentContribOther));

			spnLocation = (Spinner) findViewById(R.id.spn_ara_location);
			spnLocation.setOnItemSelectedListener(customLocation_OnClickListener);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	@Override
	public void onStart() {
		super.onStart();

		try {
			if (isBack) {
				recallUiSettings();
			}
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
			isBack = myIntent.getBooleanExtra(EXTRA_IS_BACK, false);
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
			inflater.inflate(R.menu.report_accidents, menu);
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

			case R.id.raa_menu_item_save_answers:

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
			saveUiSettings();
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
		if (null != (location = MyApplication.getInstance().getLastKnownLocation())) {
			NoteData note = NoteData.fetchNote(this, noteId);
			note.setLocation(location);
			return true;
		}
		return false;
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

		return ((spnSeverity.getSelectedItemPosition() > 0) &&
				(spnAccidentObject.getSelectedIndicies().size() > 0) &&
				(spnAccidentActions.getSelectedIndicies().size() > 0) &&
				(spnAccidentContrib.getSelectedIndicies().size() > 0) &&
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
			SpinnerPreferences prefs = new SpinnerPreferences(getSharedPreferences(PREFS_ACCIDENT_QUESTIONS, Context.MODE_PRIVATE));
			prefs.save(spnSeverity,  PREF_SEVERITY);
			prefs.save(spnAccidentObject,
					PREF_ACCIDENT_OBJECT, DbAnswers.accidentObject,
					PREF_ACCIDENT_OBJECT_OTHER, DbAnswers.accidentObjectOther);
			prefs.save(spnAccidentActions,
					PREF_ACCIDENT_ACTIONS, DbAnswers.accidentAction,
					PREF_ACCIDENT_ACTIONS_OTHER, DbAnswers.accidentActionOther);
			prefs.save(spnAccidentContrib,
					PREF_ACCIDENT_CONTRIB, DbAnswers.accidentContrib,
					PREF_ACCIDENT_CONTRIB_OTHER, DbAnswers.accidentContribOther);
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
			SpinnerPreferences prefs = new SpinnerPreferences(getSharedPreferences(PREFS_ACCIDENT_QUESTIONS, Context.MODE_PRIVATE) );

			prefs.recall(spnSeverity, PREF_SEVERITY);
			prefs.recall(spnAccidentObject, PREF_ACCIDENT_OBJECT, PREF_ACCIDENT_OBJECT_OTHER);
			prefs.recall(spnAccidentActions, PREF_ACCIDENT_ACTIONS, PREF_ACCIDENT_ACTIONS_OTHER);
			prefs.recall(spnAccidentContrib, PREF_ACCIDENT_CONTRIB, PREF_ACCIDENT_CONTRIB_OTHER);
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
			spAdapter.put(spnSeverity, DbQuestions.ACCIDENT_SEVERITY,
						  DbAnswers.accidentSeverity);

			spAdapter.put(spnAccidentObject, DbQuestions.ACCIDENT_OBJECT,
						  DbAnswers.accidentObject, DbAnswers.accidentObjectOther);

			spAdapter.put(spnAccidentActions, DbQuestions.ACCIDENT_ACTION,
						  DbAnswers.accidentAction, DbAnswers.accidentActionOther);

			spAdapter.put(spnAccidentContrib, DbQuestions.ACCIDENT_CONTRIB,
						  DbAnswers.accidentContrib, DbAnswers.accidentContribOther);

			setSeverity(spnSeverity, DbAnswers.accidentSeverity);
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
	// *
	// *********************************************************************************

	/**
     * Class: ButtonStart_OnClickListener
     *
     * Description: Callback to be invoked when startButton button is clicked
     */
	private final class CustomLocation_OnClickListener implements OnItemSelectedListener {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			try {
				if (position == 2) {
					ReportAccidentsActivity.this.alertCustomLocation();
				}
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			try {
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}

	}

	private void alertCustomLocation() {
		Toast.makeText(ReportAccidentsActivity.this,
				getResources().getString(R.string.fyi_custom_location),
				Toast.LENGTH_LONG).show();
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
		overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
		finish();
	}

	private void transitionToTabsConfigActivity() {

		Intent intent = new Intent(this, TabsConfig.class);

		intent.putExtra(TabsConfig.EXTRA_SHOW_FRAGMENT, TabsConfig.FRAG_INDEX_MAIN_INPUT);
		startActivity(intent);
		overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
		finish();
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
		intent.putExtra(ReportTypeActivity.EXTRA_REPORT_TYPE, ReportTypeActivity.EXTRA_REPORT_TYPE_ACCIDENT_REPORT);

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
		intent.putExtra(ReportTypeActivity.EXTRA_REPORT_TYPE, ReportTypeActivity.EXTRA_REPORT_TYPE_ACCIDENT_REPORT);

		// Exit this activity
		startActivity(intent);
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		finish();
	}
}
