package edu.pdx.cecs.orcycle;

import java.util.Map;
import java.util.Map.Entry;

import android.app.Activity;
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
import android.widget.Toast;

public class TripQuestionsActivity extends Activity {

	private static final String MODULE_TAG = "TripQuestionsActivity";
	// Reference to Global application object
	private final MyApplication myApp = null;
	private MenuItem saveMenuItem;

	private MultiSelectionSpinner mspinRouteChoice;
	private MultiSelectionSpinner mspinParticipants;
	private MultiSelectionSpinner mspinAccessories;

	public static final String PREFS_TRIP_QUESTIONS = "PREFS_TRIP_QUESTIONS";

	public static final int PREF_TRIP_FREQUENCY = 1;
	public static final int PREF_TRIP_PURPOSE = 2;
	public static final int PREF_ROUTE_PREFS = 3;
	public static final int PREF_TRIP_COMFORT = 4;
	public static final int PREF_ROUTE_SAFETY = 5;
	public static final int PREF_PARTICIPANTS = 6;
	public static final int PREF_BIKE_ACCESSORY = 7;

	private final int[] tripFreqAnswers = {-1, 88, 89, 90, 91, 92};                  // question_id = 19
	private final int[] tripPurposeAnswers = {-1, 94, 95, 96, 97, 98, 99, 100, 101}; // question_id = 20
	private final int[] routeChoiceAnswers = {103, 104, 105, 106, 107, 108, 109,     // question_id = 21
			                                  110, 111, 112, 113, 114, 115};
	private final int[] tripComfortAnswers = {-1, 117, 118, 119, 120, 121};          // question_id = 22
	private final int[] experRiderAnswers = {-1, 123, 124, 125, 126, 127};           // question_id = 23
	private final int[] participantsAnswers = {129, 130, 131, 132, 133, 134};        // question_id = 24
	private final int[] bikeAccessoryAnswers = {136, 137, 138};                      // question_id = 25

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

			Log.v(MODULE_TAG, "Cycle: onCreate()");

			setContentView(R.layout.activity_trip_questions);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

			((Spinner) findViewById(R.id.spinnerTripFrequency))    .setOnItemSelectedListener(new Answer_OnClickListener());
			((Spinner) findViewById(R.id.spinnerTripPurpose))      .setOnItemSelectedListener(new Answer_OnClickListener());

			mspinRouteChoice = (MultiSelectionSpinner) findViewById(R.id.spinnerRouteChoice);
			mspinRouteChoice.setItems(getResources().getStringArray(R.array.tripRouteChoiceArray));
			mspinRouteChoice.setOnItemSelectedListener(new Answer_OnClickListener());

			((Spinner) findViewById(R.id.spinnerTripComfort))      .setOnItemSelectedListener(new Answer_OnClickListener());
			((Spinner) findViewById(R.id.spinnerExperiencedRider)) .setOnItemSelectedListener(new Answer_OnClickListener());

			mspinParticipants = (MultiSelectionSpinner) findViewById(R.id.spinnerParticipants);
			mspinParticipants.setItems(getResources().getStringArray(R.array.tripParticipantsArray));
			mspinParticipants.setOnItemSelectedListener(new Answer_OnClickListener());

			mspinAccessories = (MultiSelectionSpinner) findViewById(R.id.spinnerBikeAccessory);
			mspinAccessories.setItems(getResources().getStringArray(R.array.tripBikeAccessoryArray));
			mspinAccessories.setOnItemSelectedListener(new Answer_OnClickListener());
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
			loadPreferences();
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		try {
			Log.v(MODULE_TAG, "Cycle: onSaveInstanceState()");
			savePreferences();
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		finally {
			super.onSaveInstanceState(savedInstanceState);
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
	private final class Answer_OnClickListener implements OnItemSelectedListener {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			try {
				if (null != saveMenuItem)
					saveMenuItem.setEnabled(atleastOneQuestionAnswered());
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			try {
				if (null != saveMenuItem)
					saveMenuItem.setEnabled(atleastOneQuestionAnswered());
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	private boolean atleastOneQuestionAnswered() {

		if ((((Spinner) this.findViewById(R.id.spinnerTripFrequency)).getSelectedItemPosition()) > 0) {
			return true;
		}
		if ((((Spinner) this.findViewById(R.id.spinnerTripPurpose)).getSelectedItemPosition()) > 0) {
			return true;
		}
		if (mspinRouteChoice.getSelectedIndicies().size() > 0) {
			return true;
		}
		if ((((Spinner) this.findViewById(R.id.spinnerTripComfort)).getSelectedItemPosition()) > 0) {
			return true;
		}
		if ((((Spinner) this.findViewById(R.id.spinnerExperiencedRider)).getSelectedItemPosition()) > 0) {
			return true;
		}
		if (mspinParticipants.getSelectedIndicies().size() > 0) {
			return true;
		}
		if (mspinAccessories.getSelectedIndicies().size() > 0) {
			return true;
		}

		return false;
	}

	/* Creates the menu items */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.trip_questions, menu);
		saveMenuItem = menu.getItem(1);
		saveMenuItem.setEnabled(atleastOneQuestionAnswered());
		return super.onCreateOptionsMenu(menu);
	}

	/* Handles item selections */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		Intent intent;
		// Handle presses on the action bar items
		switch (item.getItemId()) {

		case R.id.action_skip_trip_questions:

			Toast.makeText(getBaseContext(), "Trip discarded.", Toast.LENGTH_SHORT).show();

			intent = new Intent(TripQuestionsActivity.this, TabsConfig.class);
			intent.putExtra("keepme", true);
			startActivity(intent);
			overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
			TripQuestionsActivity.this.finish();
			return true;

		case R.id.action_save_trip_questions:

			savePreferences(); // loadPreferences();

			// move to next view
			// send purpose with intent
			intent = new Intent(TripQuestionsActivity.this, TripDetailActivity.class);
			//intent.putExtra("purpose", purpose);
			startActivity(intent);
			overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			TripQuestionsActivity.this.finish();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// 2.0 and above
	@Override
	public void onBackPressed() {

		Toast.makeText(getBaseContext(), "Trip discarded.", Toast.LENGTH_SHORT).show();

		//cancelRecording();

		Intent intent = new Intent(TripQuestionsActivity.this, TabsConfig.class);
		intent.putExtra("keepme", true);
		startActivity(intent);
		overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
		this.finish();
	}

	// Before 2.0
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {

			Toast.makeText(getBaseContext(), "Trip discarded.", Toast.LENGTH_SHORT).show();

			//cancelRecording();

			Intent intent = new Intent(TripQuestionsActivity.this, TabsConfig.class);
			//i.putExtra("keepme", true);
			startActivity(intent);
			overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
			this.finish();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	// *********************************************************************************
	// *                         Saving & Recalling Answers
	// *********************************************************************************

	/**
	 * Recalls UI settings from preferences file
	 */
	private void loadPreferences() {

		SharedPreferences settings;
		Map<String, ?> prefs;

		try {
			if (null != (settings = getSharedPreferences(PREFS_TRIP_QUESTIONS, 0))) {
				if (null != (prefs = settings.getAll())) {
					for (Entry<String, ?> p : prefs.entrySet()) {
						try {
							int key = Integer.parseInt(p.getKey());
							// CharSequence value = (CharSequence) p.getValue();

							switch (key) {

							case PREF_TRIP_FREQUENCY:
								loadPrefSpinner(R.id.spinnerTripFrequency, p, tripFreqAnswers);
								break;

							case PREF_TRIP_PURPOSE:
								loadPrefSpinner(R.id.spinnerTripPurpose, p, tripPurposeAnswers);
								break;

							case PREF_ROUTE_PREFS:
								loadPrefSpinner(mspinRouteChoice, p, routeChoiceAnswers);
								break;

							case PREF_TRIP_COMFORT:
								loadPrefSpinner(R.id.spinnerTripComfort, p, tripComfortAnswers);
								break;

							case PREF_ROUTE_SAFETY:
								loadPrefSpinner(R.id.spinnerExperiencedRider, p, experRiderAnswers);
								break;

							case PREF_PARTICIPANTS:
								loadPrefSpinner(mspinParticipants, p, participantsAnswers);
								break;

							case PREF_BIKE_ACCESSORY:
								loadPrefSpinner(mspinAccessories, p, bikeAccessoryAnswers);
								break;
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
	 * Saves UI settings to preferences file
	 */
	private void savePreferences() {

		SharedPreferences settings;
		SharedPreferences.Editor editor;

		if (null != (settings = getSharedPreferences(PREFS_TRIP_QUESTIONS, MODE_PRIVATE))) {
			if (null != (editor = settings.edit())) {
				savePrefSpinner(editor, PREF_TRIP_FREQUENCY,    R.id.spinnerTripFrequency,    tripFreqAnswers      );
				savePrefSpinner(editor, PREF_TRIP_PURPOSE,      R.id.spinnerTripPurpose,      tripPurposeAnswers   );
				savePrefSpinner(editor, PREF_ROUTE_PREFS,      mspinRouteChoice,             routeChoiceAnswers   );
				savePrefSpinner(editor, PREF_TRIP_COMFORT,      R.id.spinnerTripComfort,      tripComfortAnswers   );
				savePrefSpinner(editor, PREF_ROUTE_SAFETY, R.id.spinnerExperiencedRider, experRiderAnswers    );
				savePrefSpinner(editor, PREF_PARTICIPANTS,      mspinParticipants,            participantsAnswers  );
				savePrefSpinner(editor, PREF_BIKE_ACCESSORY,    mspinAccessories,             bikeAccessoryAnswers );
				editor.commit();
			}
		}
	}

	private void savePrefSpinner(SharedPreferences.Editor editor, int ikey, int spinnerId, int[] answers) {

		Spinner spinner;
		int selectedAnswer;
		String key = "" + ikey;

		if (null != (spinner = (Spinner) findViewById(spinnerId))) {
			if (-1 != (selectedAnswer = spinner.getSelectedItemPosition())) {
				editor.putInt(key, answers[selectedAnswer]);
			}
		}
	}

	private void savePrefSpinner(SharedPreferences.Editor editor, int ikey, MultiSelectionSpinner spinner, int[] answers) {
		String key = "" + ikey;
		String selectedItems = spinner.getSelectedIndicesAsString(answers);
		editor.putString(key, selectedItems);
	}

	private void loadPrefSpinner(int spinnerId, Entry<String, ?> p, int[] answers) {
		Spinner spinner = (Spinner) findViewById(spinnerId);
		int answer = ((Integer) p.getValue()).intValue();

		if (null != spinner) {
			for (int i = 0; i < answers.length; ++i) {
				if (answer == answers[i]) {
					spinner.setSelection(i);
					break;
				}
			}
		}
	}

	private void loadPrefSpinner(MultiSelectionSpinner spinner, Entry<String, ?> p, int[] answers) {

		String values = (String) p.getValue();

		if (null != values) {
			int[] selections = mapAnswers(values, answers);
			if ((null != selections) && (selections.length != 0))
				spinner.setSelection(selections);
		}
	}

	private int[] mapAnswers(String text, int[] answers) {

			int[] selectedIndexes;
			String[] selectedValues;

			if ((null == text) || text.equals(""))
				return null;

			selectedValues = text.split(",");
			selectedIndexes = new int[selectedValues.length];

			if (selectedIndexes.length > 0) {

				for (int i = 0; i < selectedIndexes.length; ++i) {
					// get value we are trying to match against
					int matchValue = Integer.valueOf(selectedValues[i]);
					// cycle thru all of the answers
					for (int answerIndex = 0; answerIndex < answers.length; ++answerIndex) {
						if (answers[answerIndex] == matchValue) {
							selectedIndexes[i] = answerIndex;
							break;
						}
					}
				}
			}
			return selectedIndexes;
		}

}
