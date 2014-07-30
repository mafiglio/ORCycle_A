package edu.pdx.cecs.orcycle;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;

public class TripQuestionsActivity extends Activity {

	private static final String MODULE_TAG = "TripQuestionsActivity";

	private MultiSelectionSpinner routePrefs;
	private MultiSelectionSpinner passengers;
	private MultiSelectionSpinner bikeAccessories;
	private Spinner tripFrequency;
	private Spinner tripPurpose;
	private Spinner tripComfort;
	private Spinner routeSafety;

	public static final String EXTRA_TRIP_ID = "TRIP_ID";

	private static final String PREFS_TRIP_QUESTIONS = "PREFS_TRIP_QUESTIONS";

	private static final int PREF_TRIP_FREQUENCY = 1;
	private static final int PREF_TRIP_PURPOSE = 2;
	private static final int PREF_ROUTE_PREFS = 3;
	private static final int PREF_TRIP_COMFORT = 4;
	private static final int PREF_ROUTE_SAFETY = 5;
	private static final int PREF_PARTICIPANTS = 6;
	private static final int PREF_BIKE_ACCESSORY = 7;

	private final Answer_OnClickListener answer_OnClickListener = new Answer_OnClickListener();

	private long tripId = -1;

	// *********************************************************************************
	// *                              Activity Handlers
	// *********************************************************************************

	/**
	 * Handler: onCreate
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);

			tripId = getIntent().getExtras().getLong(EXTRA_TRIP_ID);

			Log.v(MODULE_TAG, "Cycle: onCreate() - trip_id = " + tripId);

			setContentView(R.layout.activity_trip_questions);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

			tripFrequency = (Spinner) findViewById(R.id.spinnerTripFrequency);
			tripFrequency.setOnItemSelectedListener(answer_OnClickListener);

			tripPurpose = (Spinner) findViewById(R.id.spinnerTripPurpose);
			tripPurpose.setOnItemSelectedListener(answer_OnClickListener);

			routePrefs = (MultiSelectionSpinner) findViewById(R.id.spinnerRouteChoice);
			routePrefs.setItems(getResources().getStringArray(R.array.tripRouteChoiceArray));
			routePrefs.setOnItemSelectedListener(answer_OnClickListener);

			tripComfort = (Spinner) findViewById(R.id.spinnerTripComfort);
			tripComfort.setOnItemSelectedListener(answer_OnClickListener);

			routeSafety = (Spinner) findViewById(R.id.spinnerRouteSafety);
			routeSafety.setOnItemSelectedListener(answer_OnClickListener);

			passengers = (MultiSelectionSpinner) findViewById(R.id.spinnerParticipants);
			passengers.setItems(getResources().getStringArray(R.array.tripParticipantsArray));
			passengers.setOnItemSelectedListener(answer_OnClickListener);

			bikeAccessories = (MultiSelectionSpinner) findViewById(R.id.spinnerBikeAccessory);
			bikeAccessories.setItems(getResources().getStringArray(R.array.tripBikeAccessoryArray));
			bikeAccessories.setOnItemSelectedListener(answer_OnClickListener);
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
			inflater.inflate(R.menu.trip_questions, menu);
			//menu.getItem(0).setEnabled(true);
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

			case R.id.action_save_trip_questions:

				if (MandatoryQuestionsAnswered()) {
					submitAnswers();
					transitionToTripDetailActivity();
				}
				else {
					AlertUserMandatoryAnswers();
				}
				return true;
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		return super.onOptionsItemSelected(item);
	}

	private boolean MandatoryQuestionsAnswered() {
		return tripPurpose.getSelectedItemPosition() > 0;
	}

	private void AlertUserMandatoryAnswers() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Please answer required questions")
				.setCancelable(true)
				.setPositiveButton("OK",
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog, final int id) {
								dialog.cancel();
							}
						});
		final AlertDialog alert = builder.create();
		alert.show();
	}

	// 2.0 and above
	@Override
	public void onBackPressed() {
		try {
			transitionToPreviousActivity();
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	// Before 2.0
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			try {
				transitionToPreviousActivity();
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	// *********************************************************************************
	// *                              Button Handlers
	// *********************************************************************************

    /**
     * Class: ButtonStart_OnClickListener
     *
     * Description: Callback to be invoked when startButton button is clicked
     */
	private final class Answer_OnClickListener implements OnItemSelectedListener {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			try {
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

	private boolean questionAnswered() {

		if ((tripFrequency.getSelectedItemPosition()       > 0) ||
			(tripPurpose.getSelectedItemPosition()         > 0) ||
			(routePrefs.getSelectedIndicies().size()       > 0) ||
			(tripComfort.getSelectedItemPosition()         > 0) ||
			(routeSafety.getSelectedItemPosition()         > 0) ||
			(passengers.getSelectedIndicies().size()       > 0) ||
			(bikeAccessories.getSelectedIndicies().size()  > 0)) {
			return true;
		}

		return false;
	}

	// *********************************************************************************
	// *                      Saving & Recalling UI Settings
	// *********************************************************************************

	/**
	 * Saves UI settings to preferences file
	 */
	private void saveUiSettings() {

		SharedPreferences settings;
		SharedPreferences.Editor editor;

		if (null != (settings = getSharedPreferences(PREFS_TRIP_QUESTIONS, MODE_PRIVATE))) {
			if (null != (editor = settings.edit())) {
				saveSpinnerPosition(editor, tripFrequency,   PREF_TRIP_FREQUENCY );
				saveSpinnerPosition(editor, tripPurpose,     PREF_TRIP_PURPOSE   );
				saveSpinnerPosition(editor, routePrefs,      PREF_ROUTE_PREFS    );
				saveSpinnerPosition(editor, tripComfort,     PREF_TRIP_COMFORT   );
				saveSpinnerPosition(editor, routeSafety,     PREF_ROUTE_SAFETY   );
				saveSpinnerPosition(editor, passengers,      PREF_PARTICIPANTS   );
				saveSpinnerPosition(editor, bikeAccessories, PREF_BIKE_ACCESSORY );
				editor.commit();
			}
		}
	}

	private void saveSpinnerPosition(SharedPreferences.Editor editor, Spinner spinner, int key) {
		editor.putInt("" + key, spinner.getSelectedItemPosition());
	}

	private void saveSpinnerPosition(SharedPreferences.Editor editor, MultiSelectionSpinner spinner, int key) {
		editor.putString("" + key, spinner.getSelectedIndicesAsString());
	}

	/**
	 * Recalls UI settings from preferences file
	 */
	private void recallUiSettings() {

		SharedPreferences settings;
		Map<String, ?> prefs;

		try {
			if (null != (settings = getSharedPreferences(PREFS_TRIP_QUESTIONS, MODE_PRIVATE))) {
				if (null != (prefs = settings.getAll())) {
					for (Entry<String, ?> entry : prefs.entrySet()) {
						try {
							switch (Integer.parseInt(entry.getKey())) {
							case PREF_TRIP_FREQUENCY: setSpinnerSetting(tripFrequency,   entry); break;
							case PREF_TRIP_PURPOSE:   setSpinnerSetting(tripPurpose,     entry); break;
							case PREF_ROUTE_PREFS:    setSpinnerSetting(routePrefs,      entry); break;
							case PREF_TRIP_COMFORT:   setSpinnerSetting(tripComfort,     entry); break;
							case PREF_ROUTE_SAFETY:   setSpinnerSetting(routeSafety,     entry); break;
							case PREF_PARTICIPANTS:   setSpinnerSetting(passengers,      entry); break;
							case PREF_BIKE_ACCESSORY: setSpinnerSetting(bikeAccessories, entry); break;
							}
						}
						catch(Exception ex) {
							Log.e(MODULE_TAG, ex.getMessage());
						}
					}
				}
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	/**
	 * Sets spinner setting from a map entry
	 * @param spinner
	 * @param p
	 */
	private void setSpinnerSetting(Spinner spinner, Entry<String, ?> p) {
		spinner.setSelection(((Integer) p.getValue()).intValue());
	}

	/**
	 * Sets MultiSelectionSpinner settings from a map entry
	 * @param spinner
	 * @param p
	 */
	private void setSpinnerSetting(MultiSelectionSpinner spinner, Entry<String, ?> p) {

		// Retireve entry value
		String entry = (String) p.getValue();

		// Check that values exist
		if ((null == entry) || entry.equals(""))
			return;

		// Split values apart
		String[] entries;
		entries = entry.split(",");

		// Check that values exist
		if (entries.length < 1)
			return;

		// Setting multiple spinner selections require an array of ints
		int[] selections = new int[entries.length];

		// Fill array of ints with settings from entry
		for (int i = 0; i < entries.length; ++i) {
			selections[i] = Integer.valueOf(entries[i]);
		}

		// Set MultiSelectionSpinner spinner control values
		spinner.setSelection(selections);
	}

	// *********************************************************************************
	// *                         Submitting Answers
	// *********************************************************************************

	/**
	 * Saves UI settings to database
	 */
	private void submitAnswers() {

		DbAdapter dbAdapter = new DbAdapter(this);
		dbAdapter.open();

		// Remove any previous answers from the local database
		dbAdapter.deleteAnswers(tripId);

		// Enter the user selections into the local database
		try {
			submitSpinnerSelection(tripFrequency,    dbAdapter, DbQuestions.TRIP_FREQUENCY,   DbAnswers.tripFreq       );
			submitSpinnerSelection(tripPurpose,      dbAdapter, DbQuestions.TRIP_PURPOSE,     DbAnswers.tripPurpose    );
			submitSpinnerSelection(routePrefs,       dbAdapter, DbQuestions.ROUTE_PREFS,      DbAnswers.routePrefs     );
			submitSpinnerSelection(tripComfort,      dbAdapter, DbQuestions.TRIP_COMFORT,     DbAnswers.tripComfort    );
			submitSpinnerSelection(routeSafety,      dbAdapter, DbQuestions.ROUTE_SAFETY,     DbAnswers.routeSafety    );
			submitSpinnerSelection(passengers,       dbAdapter, DbQuestions.PASSENGERS,       DbAnswers.passengers     );
			submitSpinnerSelection(bikeAccessories,  dbAdapter, DbQuestions.BIKE_ACCESSORIES, DbAnswers.bikeAccessories);

			updateTripPurpose(tripPurpose, dbAdapter,  DbAnswers.tripPurpose);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		finally {
			dbAdapter.close();
		}
	}

	/**
	 * Enters the spinner selection into the database
	 * @param spinner
	 * @param dbAdapter
	 * @param question_id
	 * @param answers
	 */
	private void submitSpinnerSelection(Spinner spinner, DbAdapter dbAdapter, int question_id, int[] answer_ids) {
		// Note: The first entry is always blank, the array of answers displayed
		// by the UI is one greater than the number of answers in the database.
		int answerIndex = spinner.getSelectedItemPosition() - 1;
		if (answerIndex > -1) {
			dbAdapter.addAnswerToTrip(tripId, question_id, answer_ids[answerIndex]);
		}
	}

	/**
	 * Enters the MultiSelectionSpinner selections into the database
	 * @param spinner
	 * @param dbAdapter
	 * @param question_id
	 * @param answers
	 */
	private void submitSpinnerSelection(MultiSelectionSpinner spinner, DbAdapter dbAdapter, int question_id, int[] answers) {
		List<Integer> selectedIndicies = spinner.getSelectedIndicies();
		for (int index : selectedIndicies) {
			dbAdapter.addAnswerToTrip(tripId, question_id, answers[index]);
		}
	}

	/**
	 * Enters the trips purpose into an additional
	 * tripPurpose field that's in the database
	 * @param spinner  The spinner widget to obtain the trip purpose selection
	 * @param dbAdapter The adapter connected to the local database
	 * @param answer_ids The TripPurpose values corresponding to the spinner selections
	 */
	private void updateTripPurpose(Spinner spinner, DbAdapter dbAdapter, int[] answer_ids) {
		int tripPurposeId = DbAnswers.tripPurpose[spinner.getSelectedItemPosition() - 1];
		String tripPurposeText = DbAnswers.getTextTripPurpose(tripPurposeId);
		dbAdapter.updateTripPurpose(tripId, tripPurposeText);
	}

	// *********************************************************************************
	// *                    Transitioning to other activities
	// *********************************************************************************

	private void transitionToTripDetailActivity() {
		Intent intent = new Intent(TripQuestionsActivity.this, TripDetailActivity.class);
		intent.putExtra(TripDetailActivity.EXTRA_TRIP_ID, tripId);
		startActivity(intent);
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		TripQuestionsActivity.this.finish();
	}

	private void transitionToPreviousActivity() {
		Intent intent = new Intent(TripQuestionsActivity.this, TabsConfig.class);
		// tell the TabsConfig activities to not delete this trip
		intent.putExtra(TabsConfig.EXTRA_KEEP_ME, true);
		intent.putExtra(TabsConfig.EXTRA_SHOW_FRAGMENT, TabsConfig.FRAG_INDEX_MAIN_INPUT);
		startActivity(intent);
		overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
		TripQuestionsActivity.this.finish();
	}
}
