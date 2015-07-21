/**
 *  ORcycle, Copyright 2014, 2015, PSU Transportation, Technology, and People Lab.
 *
 *  @author Robin Murray <robin5@pdx.edu>    (code)
 *  @author Miguel Figliozzi <figliozzi@pdx.edu> and ORcycle team (general app
 *  design and features, report questionnaires and new ORcycle features)
 *
 *  For more information on the project, go to
 *  http://www.pdx.edu/transportation-lab/orcycle and http://www.pdx.edu/transportation-lab/app-development
 *
 *  Updated/modified for Oregon pilot study and app deployment.
 *
 *  ORcycle is free software: you can redistribute it and/or modify it under the
 *  terms of the GNU General Public License as published by the Free Software
 *  Foundation, either version 3 of the License, or any later version.
 *  ORcycle is distributed in the hope that it will be useful, but WITHOUT ANY
 *  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 *  A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *  You should have received a copy of the GNU General Public License along with
 *  ORcycle. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package edu.pdx.cecs.orcycle;

import java.text.SimpleDateFormat;
import java.util.Locale;

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
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

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

	public static final String EXTRA_IS_BACK = "isBack";

	private long tripId;
	private long noteId;
	private int noteSource = EXTRA_NOTE_SOURCE_UNDEFINED;
	private int tripSource = EXTRA_TRIP_SOURCE_UNDEFINED;
	private boolean isBack;

	private static final String PREFS_SAFETY_ISSUE_QUESTIONS = "PREFS_SAFETY_ISSUE_QUESTIONS";
	private static final int PREF_SAFETY_ISSUES = 1;
	private static final int PREF_URGENCY = 2;
	private static final int PREF_LOCATION = 3;
	private static final int PREF_REPORT_DATE = 4;
	private static final int PREF_REPORT_DATE_TAG = 5;
	private static final int PREF_SAFETY_ISSUES_OTHER = 1001;

	private Button btnSave;
	private MultiSelectionSpinner spnSafetyIssues;
	private Spinner spnUrgency;
	private Spinner spnLocation;
	private Button btnReportDate;
	private final SimpleDateFormat reportDateFormatter = new SimpleDateFormat("EEEE, MM/dd/yyyy", Locale.US);
	private final CustomLocation_OnClickListener customLocation_OnClickListener =
			new CustomLocation_OnClickListener();
	private static final int USE_GPS_LOCATION_POS = 1;
	private static final int USE_CUSTOM_LOCATION_POS = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_report_safety_issues);

			loadVars(savedInstanceState);

			spnSafetyIssues = (MultiSelectionSpinner) findViewById(R.id.spn_arsi_problem);
			spnSafetyIssues.setItems(getResources().getStringArray(R.array.arsi_a_safety_issues));
			spnSafetyIssues.setTitle(getResources().getString(R.string.arsi_q_problem_type_title));
			spnSafetyIssues.setOtherIndex(DbAnswers.findIndex(DbAnswers.safetyIssue, DbAnswers.safetyIssueOther));

			spnUrgency = (Spinner) findViewById(R.id.spn_arsi_urgency);
			spnLocation = (Spinner) findViewById(R.id.spn_arsi_location);
			spnLocation.setOnItemSelectedListener(customLocation_OnClickListener);
			if (noteSource == EXTRA_NOTE_SOURCE_TRIP_MAP) {
				RelativeLayout rl = (RelativeLayout) findViewById(R.id.rl_arsi_location);
				rl.setVisibility(View.GONE);
			}

			btnSave = (Button) findViewById(R.id.btn_arsi_save_report);
			btnSave.setOnClickListener(new ButtonSave_OnClickListener());

			btnReportDate = (Button) findViewById(R.id.btn_issue_date);
			btnReportDate.setTag(0L);
			btnReportDate.setOnClickListener(new ReportDate_OnClickListener());
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
			saveUiSettings();
			if (noteSource == EXTRA_NOTE_SOURCE_TRIP_MAP) {
				transitionToNoteDetailActivity();
			}
			else if (useCustomLocation()) {
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
		builder.setTitle(R.string.ara_no_gps_title);
		builder.setMessage(R.string.ara_no_gps_message);
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

		if (noteSource == EXTRA_NOTE_SOURCE_TRIP_MAP) {
			return ((spnSafetyIssues.getSelectedIndicies().size() > 0) &&
					(spnUrgency.getSelectedItemPosition() > 0) &&
					(btnReportDate.getText().length() > 0));
		}
		else {
			return ((spnSafetyIssues.getSelectedIndicies().size() > 0) &&
					(spnUrgency.getSelectedItemPosition() > 0) &&
					(btnReportDate.getText().length() > 0) &&
					(spnLocation.getSelectedItemPosition() > 0));
		}
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
			prefs.save(spnSafetyIssues,
					PREF_SAFETY_ISSUES, DbAnswers.safetyIssue,
					PREF_SAFETY_ISSUES_OTHER, DbAnswers.safetyIssueOther);
			prefs.save(spnUrgency,  PREF_URGENCY);
			prefs.save(spnLocation, PREF_LOCATION);
			prefs.save(btnReportDate, PREF_REPORT_DATE);
			prefs.saveTag(btnReportDate, PREF_REPORT_DATE_TAG);
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

			prefs.recall(spnSafetyIssues, PREF_SAFETY_ISSUES);
			prefs.recall(spnUrgency, PREF_URGENCY);
			prefs.recall(spnLocation, PREF_LOCATION);
			prefs.recall(btnReportDate, PREF_REPORT_DATE);
			prefs.recallTag(btnReportDate, PREF_REPORT_DATE_TAG);
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

		// Update report date
		long reportDate = (Long)btnReportDate.getTag();
		dbAdapter.updateNoteReportDate(noteId, reportDate);

		// This activity can be entered into a number of times with
		// the same noteId so we always delete any previous submissions
		dbAdapter.deleteNoteAnswers(noteId);

		SpinnerAdapter spAdapter = new SpinnerAdapter(dbAdapter, noteId);

		// Enter the user selections into the local database
		try {
			spAdapter.put(spnSafetyIssues,  DbQuestions.SAFETY_ISSUE,
							   DbAnswers.safetyIssue, DbAnswers.safetyIssueOther);

			spAdapter.put(spnUrgency , DbQuestions.SAFETY_URGENCY,
					   DbAnswers.safetyUrgency);

			// This is utterly bad programming!!! We are setting the severity
			// column in the database with urgency values.  Later we key off this value
			// to determine if the report is a safety or accident report
			setSeverity(spnUrgency, DbAnswers.safetyUrgency);
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
	// *                              Button Handlers
	// *********************************************************************************

	/**
     * Class: ButtonSave_OnClickListener
     *
     * Description: Callback to be invoked when btnSave button is clicked
     */
	private final class ButtonSave_OnClickListener implements View.OnClickListener {

		/**
		 * Description: Handles onClick for view
		 */
		public void onClick(View v) {

			try {
				finishReport();
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

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
					ReportSafetyIssuesActivity.this.alertCustomLocation();
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
		Toast.makeText(ReportSafetyIssuesActivity.this,
				getResources().getString(R.string.fyi_custom_location),
				Toast.LENGTH_LONG).show();
	}

	/**
     * Class: ReportDate_OnClickListener
     *
     * Description: Callback to be invoked when btnReportDate button is clicked
     */
	private final class ReportDate_OnClickListener implements View.OnClickListener {

		/**
		 * Description: Handles onClick for view
		 */
		public void onClick(View v) {

			try {
				showDatePicker();
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	 /**
     * Builds a custom dialog based on the defined layout
     * 'res/layout/datepicker_layout.xml' and shows it
     */
    public void showDatePicker() {

    	DatePickerDialog datePickerDialog = new DatePickerDialog(this, btnReportDate, reportDateFormatter);

    	datePickerDialog.show();
        // Finish
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
		intent.putExtra(ReportTypeActivity.EXTRA_REPORT_TYPE, ReportTypeActivity.EXTRA_REPORT_TYPE_SAFETY_ISSUE_REPORT);

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
		intent.putExtra(ReportTypeActivity.EXTRA_REPORT_TYPE, ReportTypeActivity.EXTRA_REPORT_TYPE_SAFETY_ISSUE_REPORT);

		// Exit this activity
		startActivity(intent);
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		finish();
	}
}
