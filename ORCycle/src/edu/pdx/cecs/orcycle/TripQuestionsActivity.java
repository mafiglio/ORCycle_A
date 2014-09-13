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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

public class TripQuestionsActivity extends Activity {

	private static final String MODULE_TAG = "TripQuestionsActivity";

	private MultiSelectionSpinner routePrefs;
	private MultiSelectionSpinner routeStressors;
	private Spinner tripFrequency;
	private Spinner tripPurpose;
	private Spinner tripComfort;
	private EditText tripComment;
	//private MultiSelectionSpinner passengers;
	//private MultiSelectionSpinner bikeAccessories;
	//private Spinner routeSafety;
	//private Spinner rideConflict;

	public static final String EXTRA_TRIP_ID = "TRIP_ID";

	private static final String PREFS_TRIP_QUESTIONS = "PREFS_TRIP_QUESTIONS";

	private static final int PREF_TRIP_FREQUENCY   = 1;
	private static final int PREF_TRIP_PURPOSE     = 2;
	private static final int PREF_ROUTE_PREFS      = 3;
	private static final int PREF_TRIP_COMFORT     = 4;
	private static final int PREF_ROUTE_SAFETY     = 5;
	private static final int PREF_PASSENGERS       = 6;
	private static final int PREF_BIKE_ACCESSORIES = 7;
	private static final int PREF_RIDE_CONFLICT    = 8;
	private static final int PREF_ROUTE_STRESSORS  = 9;
	private static final int PREF_TRIP_COMMENT     = 10;

	private TripPurpose_OnClickListener tripPurpose_OnClick = null;



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


			tripFrequency = (Spinner) findViewById(R.id.spnrTripFrequency);

			tripPurpose = (Spinner) findViewById(R.id.spnrTripPurpose);
			tripPurpose_OnClick = new TripPurpose_OnClickListener();
			tripPurpose_OnClick.setOtherIndex(DbAnswers.findIndex(DbAnswers.tripPurpose, DbAnswers.tripPurposeOther));
			tripPurpose_OnClick.setItems(getResources().getStringArray(R.array.qa_20_tripPurpose));
			tripPurpose.setOnItemSelectedListener(tripPurpose_OnClick);

			routePrefs = (MultiSelectionSpinner) findViewById(R.id.spnrRouteChoice);
			routePrefs.setTitle(getResources().getString(R.string.q21_routePreferences));
			routePrefs.setItems(getResources().getStringArray(R.array.qa_21_routePreferences));
			routePrefs.setOtherIndex(DbAnswers.findIndex(DbAnswers.routePrefs, DbAnswers.tripRoutePrefsOther));

			tripComfort = (Spinner) findViewById(R.id.spnrTripComfort);

			//routeSafety = (Spinner) findViewById(R.id.spnrRouteSafety);
			//routeSafety.setOnItemSelectedListener(answer_OnClickListener);

			//passengers = (MultiSelectionSpinner) findViewById(R.id.spnrPassengers);
			//passengers.setItems(getResources().getStringArray(R.array.qa_24_ridePassengers));
			//passengers.setOverrideIndex(DbAnswers.findIndex(DbAnswers.passengers, DbAnswers.passengersAlone));
			//passengers.setOnItemSelectedListener(answer_OnClickListener);

			//bikeAccessories = (MultiSelectionSpinner) findViewById(R.id.spnrBikeAccessories);
			//bikeAccessories.setItems(getResources().getStringArray(R.array.qa_25_bikeAccessories));
			//bikeAccessories.setOverrideIndex(DbAnswers.findIndex(DbAnswers.bikeAccessories, DbAnswers.bikeAccessoriesNone));
			//bikeAccessories.setOnItemSelectedListener(answer_OnClickListener);

			//rideConflict = (Spinner) findViewById(R.id.spnrRideConflict);
			//rideConflict.setOnItemSelectedListener(answer_OnClickListener);

			routeStressors = (MultiSelectionSpinner) findViewById(R.id.spnrRouteStressor);
			routeStressors.setItems(getResources().getStringArray(R.array.qa_27_routeStressors));
			routeStressors.setTitle(getResources().getString(R.string.q27_routeStressors));
			routeStressors.setOverrideIndex(DbAnswers.findIndex(DbAnswers.routeStressors, DbAnswers.routeStressorsNotConcerned));
			routeStressors.setOtherIndex(DbAnswers.findIndex(DbAnswers.routeStressors, DbAnswers.tripRouteStressorsOther));

			tripComment = (EditText) findViewById(R.id.editTextTripComment);

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
					querySafetyMarker();
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

    /**
     * Class: ButtonStart_OnClickListener
     *
     * Description: Callback to be invoked when startButton button is clicked
     */
	private final class TripPurpose_OnClickListener implements OnItemSelectedListener {

		private int otherIndex = -1;
		private String otherText = null;
		private AlertDialog.Builder inputDialog;
		private EditText editText;
		private ArrayAdapter<String> myArrayAdapter = null;
		private String[] items;
		private final String DEFAULT_OTHER_TEXT = "Other...";
		private AdapterView<ArrayAdapter<String>> myAdapterView;

		public TripPurpose_OnClickListener() {
		}

		private void initDialog() {
			inputDialog = new AlertDialog.Builder(TripQuestionsActivity.this);
			editText = new EditText(TripQuestionsActivity.this);
			inputDialog.setView(editText);
			inputDialog.setTitle("Please specify other:");
			inputDialog.setPositiveButton("OK", new OtherPositiveButton_OnClickListener());
			inputDialog.setNegativeButton("Cancel", new OtherNegativeButton_OnClickListener());
			inputDialog.setCancelable(true);
		}

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			try {
				Log.v(MODULE_TAG, "Item selected(" + position + ") = " + id);

				if (otherIndex == position - 1) {
					myArrayAdapter = null;
					try {
						myAdapterView = (AdapterView<ArrayAdapter<String>>) parent;
						myArrayAdapter = myAdapterView.getAdapter();
						String item = myArrayAdapter.getItem(position);
						Log.v(MODULE_TAG, "Item(" + position + ") = <" + item + ">");
						initDialog();
						inputDialog.show();
					}
					catch(ClassCastException ex) {
						Log.e(MODULE_TAG, ex.getMessage());
					}
				}
				else {
					otherText = "";
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

		private final class OtherPositiveButton_OnClickListener implements
				DialogInterface.OnClickListener {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				try {
					setOtherText(editText.getText().toString());
				}
				catch (Exception ex) {
					Log.e(MODULE_TAG, ex.getMessage());
				}
				finally {
					dialog.dismiss();
				}
			}
		}

		private final class OtherNegativeButton_OnClickListener implements
				DialogInterface.OnClickListener {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				try {
					dialog.dismiss();
				}
				catch (Exception ex) {
					Log.e(MODULE_TAG, ex.getMessage());
				}
			}
		}

		public void setOtherIndex(int index) {
			this.otherIndex = index;
		}

		private void setOtherText(String text) {

			if (null == text) {
				otherText="";
			}
			else {
				otherText = text.trim();
			}

			// Pick something sane to show the user
			if (otherText == "") {
				this.items[otherIndex + 1] = DEFAULT_OTHER_TEXT;
			}
			else {
				this.items[otherIndex + 1] = "Other( \"" +  text + "\" )";
			}

			// First try
			//myArrayAdapter.clear();
			//myArrayAdapter.addAll(this.items);
			//myArrayAdapter.notifyDataSetChanged();

			// Second try
			//ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(NoteQuestionsActivity.this, otherIndex);
			//myAdapterView.setAdapter(arrayAdapter);
		}

		public String getOtherText() {
			return otherText;
		}

		private void setItems(String[] items) {
			this.items = new String[items.length];
			for (int i = 0; i < items.length; ++i) {
				this.items[i] = new String(items[i]);
			}
		}
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
				saveSpinnerSelection(editor, tripFrequency,   PREF_TRIP_FREQUENCY   );
				saveSpinnerSelection(editor, tripPurpose,     PREF_TRIP_PURPOSE     );
				saveSpinnerSelections(editor, routePrefs,      PREF_ROUTE_PREFS      );
				saveSpinnerSelection(editor, tripComfort,     PREF_TRIP_COMFORT     );
				//saveSpinnerPosition(editor, routeSafety,     PREF_ROUTE_SAFETY     );
				//saveSpinnerPosition(editor, passengers,      PREF_PASSENGERS       );
				//saveSpinnerPosition(editor, bikeAccessories, PREF_BIKE_ACCESSORIES );
				//saveSpinnerPosition(editor, rideConflict,    PREF_RIDE_CONFLICT    );
				saveSpinnerSelections(editor, routeStressors,  PREF_ROUTE_STRESSORS  );
				saveEditText(editor, tripComment, PREF_TRIP_COMMENT);
				editor.commit();
			}
		}
	}

	private void saveSpinnerSelection(SharedPreferences.Editor editor, Spinner spinner, int key) {
		editor.putInt("" + key, spinner.getSelectedItemPosition());
	}

	private void saveSpinnerSelections(SharedPreferences.Editor editor, MultiSelectionSpinner spinner, int key) {
		editor.putString("" + key, spinner.getSelectedIndicesAsString());
	}

	private void saveEditText(SharedPreferences.Editor editor, EditText editText, int key) {
		editor.putString("" + key, editText.getEditableText().toString());
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
							case PREF_TRIP_FREQUENCY:   setSpinnerSelection(tripFrequency,   entry); break;
							case PREF_TRIP_PURPOSE:     setSpinnerSelection(tripPurpose,     entry); break;
							case PREF_ROUTE_PREFS:      setSpinnerSelections(routePrefs,      entry); break;
							case PREF_TRIP_COMFORT:     setSpinnerSelection(tripComfort,     entry); break;
							//case PREF_ROUTE_SAFETY:     setSpinnerSetting(routeSafety,     entry); break;
							//case PREF_PASSENGERS:       setSpinnerSetting(passengers,      entry); break;
							//case PREF_BIKE_ACCESSORIES: setSpinnerSetting(bikeAccessories, entry); break;
							//case PREF_RIDE_CONFLICT:    setSpinnerSetting(rideConflict,    entry); break;
							case PREF_ROUTE_STRESSORS:  setSpinnerSelections(routeStressors,  entry); break;
							case PREF_TRIP_COMMENT:     setEditText(tripComment,           entry); break;
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
	private void setSpinnerSelection(Spinner spinner, Entry<String, ?> p) {
		spinner.setSelection(((Integer) p.getValue()).intValue());
	}

	/**
	 * Sets MultiSelectionSpinner settings from a map entry
	 * @param spinner
	 * @param p
	 */
	private void setSpinnerSelections(MultiSelectionSpinner spinner, Entry<String, ?> p) {

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

	private void setEditText(EditText editText, Entry<String, ?> p) {
		editText.setText((String) p.getValue());
	}

	// *********************************************************************************
	// *                         Submitting Answers
	// *********************************************************************************

	/**
	 * Saves UI settings to database
	 */
	private void submitAnswers() {

		DbAdapter dbAdapter = null;

		try {
			dbAdapter = new DbAdapter(this);
			dbAdapter.open();

			// Remove any previous answers from the local database
			dbAdapter.deleteAnswers(tripId);

			// Enter the user selections into the local database
			// Update answer table
			submitSpinnerSelection(dbAdapter, tripFrequency,
					DbQuestions.TRIP_FREQUENCY, DbAnswers.tripFreq);
			submitSpinnerSelection(dbAdapter, tripPurpose,
					DbQuestions.TRIP_PURPOSE, DbAnswers.tripPurpose,
					DbAnswers.tripPurposeOther, tripPurpose_OnClick.getOtherText());
			submitSpinnerSelection(routePrefs, dbAdapter,
					DbQuestions.ROUTE_PREFS, DbAnswers.routePrefs,
					DbAnswers.tripRoutePrefsOther);
			submitSpinnerSelection(dbAdapter, tripComfort,
					DbQuestions.TRIP_COMFORT, DbAnswers.tripComfort);
			// submitSpinnerSelection( dbAdapter, routeSafety,
			// DbQuestions.ROUTE_SAFETY, DbAnswers.routeSafety );
			// submitSpinnerSelection( dbAdapter, passengers,
			// DbQuestions.PASSENGERS, DbAnswers.passengers );
			// submitSpinnerSelection( dbAdapter, bikeAccessories,
			// DbQuestions.BIKE_ACCESSORIES, DbAnswers.bikeAccessories );
			// submitSpinnerSelection( dbAdapter, rideConflict,
			// DbQuestions.RIDE_CONFLICT, DbAnswers.rideConflict );
			submitSpinnerSelection(routeStressors, dbAdapter,
					DbQuestions.ROUTE_STRESSORS, DbAnswers.routeStressors,
					DbAnswers.tripRouteStressorsOther);

			// Update trip table
			updateTripPurpose(dbAdapter, tripPurpose, DbAnswers.tripPurpose);

		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		finally {
			if (null != dbAdapter)
				dbAdapter.close();
		}

		// get reference to recording service
		IRecordService recordingService = MyApplication.getInstance().getRecordingService();
		// finish recording process
		recordingService.finishRecording();

		// -------------------------------------
		// gather final trip data
		// -------------------------------------

		TripData tripData = TripData.fetchTrip(this, tripId);

		// Save the trip details to the phone database. W00t!
		tripData.updateTrip(tripData.getStartTime(), tripData.getEndTime(),
				tripData.distance, tripComment.getEditableText().toString());
		tripData.updateTripStatus(TripData.STATUS_COMPLETE);

		// ----------------
		// Upload trip data
		// ----------------

		TripUploader uploader = new TripUploader(this);
		uploader.execute(tripData.tripid);

		try {
			recordingService.reset();
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
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
	 * Enters the spinner selection into the database
	 * @param dbAdapter
	 * @param spinner
	 * @param question_id
	 * @param answers
	 */
	private void submitSpinnerSelection(DbAdapter dbAdapter, Spinner spinner,
			int question_id, int[] answer_ids) {
		submitSpinnerSelection(dbAdapter, spinner, question_id, answer_ids, -1, null);
	}
	/**
	 * Enters the spinner selection into the database
	 * @param spinner
	 * @param dbAdapter
	 * @param question_id
	 * @param answers
	 */
	private void submitSpinnerSelection(DbAdapter dbAdapter, Spinner spinner,
			int question_id, int[] answer_ids, int other_id, String other_text) {

		// Note: The first entry is always blank, the array of answers displayed
		// by the UI is one greater than the number of answers in the database.
		int answerIndex = spinner.getSelectedItemPosition() - 1;

		if (answerIndex >= 0) {
			if (answer_ids[answerIndex] == other_id) {
				dbAdapter.addAnswerToTrip(tripId, question_id, other_id, other_text);
			}
			else {
				dbAdapter.addAnswerToTrip(tripId, question_id, answer_ids[answerIndex]);
			}
		}
	}





	/**
	 * Enters the MultiSelectionSpinner selections into the database
	 * @param spinner
	 * @param dbAdapter
	 * @param question_id
	 * @param answers
	 */
	private void submitSpinnerSelection(DbAdapter dbAdapter, MultiSelectionSpinner spinner, int question_id, int[] answers) {
		List<Integer> selectedIndicies = spinner.getSelectedIndicies();
		for (int index : selectedIndicies) {
			dbAdapter.addAnswerToTrip(tripId, question_id, answers[index]);
		}
	}

	/**
	 * Enters the MultiSelectionSpinner selections into the database
	 * @param spinner
	 * @param dbAdapter
	 * @param question_id
	 * @param answers
	 */
	private void submitSpinnerSelection(MultiSelectionSpinner spinner, DbAdapter dbAdapter,
			int question_id, int[] answers, int answerOther) {
		List<Integer> selectedIndicies = spinner.getSelectedIndicies();
		for (int index : selectedIndicies) {
			if ((answerOther >= 0) && (answers[index] == answerOther)) {
				dbAdapter.addAnswerToTrip(tripId, question_id, answers[index], spinner.getOtherText());
			}
			else {
				dbAdapter.addAnswerToTrip(tripId, question_id, answers[index]);
			}
		}
	}

	/**
	 * Enters the trips purpose into an additional
	 * tripPurpose field that's in the database
	 * @param spinner  The spinner widget to obtain the trip purpose selection
	 * @param dbAdapter The adapter connected to the local database
	 * @param answer_ids The TripPurpose values corresponding to the spinner selections
	 */
	private void updateTripPurpose(DbAdapter dbAdapter, Spinner spinner, int[] answer_ids) {
		int tripPurposeId = DbAnswers.tripPurpose[spinner.getSelectedItemPosition() - 1];
		String tripPurposeText = DbAnswers.getTextTripPurpose(tripPurposeId);
		dbAdapter.updateTripPurpose(tripId, tripPurposeText);
	}

	// *********************************************************************************
	// *                         querySafetyMarker Dialog
	// *********************************************************************************

	/**
	 * Build dialog telling user that they can add safety markers to the trip
	 */
	private void querySafetyMarker() {

		final AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setMessage(getResources().getString(R.string.tqi_qsm_dialog_message));
		builder.setCancelable(false);
		builder.setPositiveButton(getResources().getString(R.string.tqi_qsm_dialog_ok),
				new QuerySafetyMarker_OkListener());
		builder.setNegativeButton(getResources().getString(R.string.tqi_qsm_dialog_later),
				new QuerySafetyMarker_CancelListener());
		final AlertDialog alert = builder.create();
		alert.show();
	}

	class QuerySafetyMarker_OkListener implements DialogInterface.OnClickListener {
		public void onClick(final DialogInterface dialog, final int id) {
			transitionToTripMapActivity();
		}
	}

	class QuerySafetyMarker_CancelListener implements DialogInterface.OnClickListener {
		public void onClick(final DialogInterface dialog, final int id) {
			dialog.cancel();
			AlertUserAboutLater();
		}
	}

	// *********************************************************************************
	// *                       AlertUserAboutLater Dialog
	// *********************************************************************************

	private void AlertUserAboutLater() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getResources().getString(R.string.tqa_alert_user_about_later))
				.setCancelable(true)
				.setPositiveButton("OK", new AlertUserAboutLater_OkListener());
		final AlertDialog alert = builder.create();
		alert.show();
	}

    private final class AlertUserAboutLater_OkListener implements
			DialogInterface.OnClickListener {
		public void onClick(final DialogInterface dialog, final int id) {
			dialog.cancel();
			transitionToTabsConfigActivity();
		}
	}

    // *********************************************************************************
	// *                    Transitioning to other activities
	// *********************************************************************************

	private void transitionToPreviousActivity() {
		Intent intent = new Intent(TripQuestionsActivity.this, TabsConfig.class);
		// tell the TabsConfig activities to not delete this trip
		intent.putExtra(TabsConfig.EXTRA_KEEP_ME, true);
		intent.putExtra(TabsConfig.EXTRA_SHOW_FRAGMENT, TabsConfig.FRAG_INDEX_MAIN_INPUT);
		startActivity(intent);
		overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
		TripQuestionsActivity.this.finish();
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
}
