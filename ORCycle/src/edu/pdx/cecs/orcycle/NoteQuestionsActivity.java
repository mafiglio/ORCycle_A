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

public class NoteQuestionsActivity extends Activity {

	private static final String MODULE_TAG = "NoteQuestionsActivity";

	private Spinner spnSeverity;
	private Spinner spnIssueType;
	private MultiSelectionSpinner spnConflict;

	private String issueTypeOtherText = null;

	public static final String EXTRA_NOTE_ID = "noteId";
	public static final String EXTRA_NOTE_TYPE = "noteType";
	public static final String EXTRA_NOTE_SEVERITY = "noteSeverity";
	public static final String EXTRA_NOTE_SOURCE = "noteSource";
	public static final int EXTRA_NOTE_ID_UNDEFINED = -1;
	public static final int EXTRA_NOTE_TYPE_UNDEFINED = -1;
	public static final int EXTRA_NOTE_SEVERITY_UNDEFINED = -1;
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

	private SpnIssueType_OnClickListener spnIssueType_OnClick = null;

	private int noteType;
	private int noteSeverity;
	private long noteId = -1;
	private int noteSource = EXTRA_NOTE_SOURCE_UNDEFINED;
	private long tripId;
	private int tripSource = EXTRA_TRIP_SOURCE_UNDEFINED;

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

			noteType = myIntent.getIntExtra(EXTRA_NOTE_TYPE, EXTRA_NOTE_TYPE_UNDEFINED);
			noteSeverity = myIntent.getIntExtra(EXTRA_NOTE_SEVERITY, EXTRA_NOTE_SEVERITY_UNDEFINED);

			// Note: these extras are used for transitioning back to the TripMapActivity if done
			if (EXTRA_TRIP_ID_UNDEFINED == (tripId = myIntent.getLongExtra(EXTRA_TRIP_ID, EXTRA_TRIP_ID_UNDEFINED))) {
				throw new IllegalArgumentException(MODULE_TAG + ": invalid extra - EXTRA_TRIP_ID");
			}

			if (EXTRA_TRIP_SOURCE_UNDEFINED == (tripSource = myIntent.getIntExtra(EXTRA_TRIP_SOURCE, EXTRA_TRIP_SOURCE_UNDEFINED))) {
				throw new IllegalArgumentException(MODULE_TAG + ": invalid extra - EXTRA_TRIP_SOURCE");
			}

			Log.v(MODULE_TAG, "Cycle: onCreate() - note_id = " + noteId);

			setContentView(R.layout.activity_note_questions);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

			spnIssueType_OnClick = new SpnIssueType_OnClickListener();


			spnSeverity = (Spinner) findViewById(R.id.spnSeverityOfProblem);

			spnConflict = (MultiSelectionSpinner) findViewById(R.id.spnConflictType);
			spnConflict.setItems(getResources().getStringArray(R.array.nqaConflictType));
			spnConflict.setTitle(getResources().getString(R.string.nqaConflictTypeTitle));
			spnConflict.setOtherIndex(DbAnswers.findIndex(DbAnswers.noteConflict, DbAnswers.noteConflictOther));

			spnIssueType = (Spinner) findViewById(R.id.spnIssueType);
			spnIssueType_OnClick.setOtherIndex(DbAnswers.findIndex(DbAnswers.noteIssue, DbAnswers.noteIssueOther));
			spnIssueType_OnClick.setItems(getResources().getStringArray(R.array.nqaIssueType));
			spnIssueType.setOnItemSelectedListener(spnIssueType_OnClick);
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
			inflater.inflate(R.menu.note_questions, menu);
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

			case R.id.action_save_note_questions:

				if (MandatoryQuestionsAnswered()) {
					submitAnswers();
					transitionToNoteDetailActivity();
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

		return ((spnIssueType.getSelectedItemPosition() > 0) &&
				(spnSeverity.getSelectedItemPosition() > 0));
	}

	private void AlertUserMandatoryAnswers() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Please answer all required questions.")
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
	private final class SpnIssueType_OnClickListener implements OnItemSelectedListener {

		private int otherIndex = -1;
		private final AlertDialog.Builder inputDialog;
		private final EditText editText;
		private ArrayAdapter<String> myArrayAdapter = null;
		private String[] items;
		private final String DEFAULT_OTHER_TEXT = "Other...";
		private AdapterView<ArrayAdapter<String>> myAdapterView;

		public SpnIssueType_OnClickListener() {

			inputDialog = new AlertDialog.Builder(NoteQuestionsActivity.this);
			editText = new EditText(NoteQuestionsActivity.this);
			inputDialog.setView(editText);
			inputDialog.setTitle("Specify other...");
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
						inputDialog.show();
					}
					catch(ClassCastException ex) {
						Log.e(MODULE_TAG, ex.getMessage());
					}
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
				issueTypeOtherText="";
			}
			else {
				issueTypeOtherText = text.trim();
			}

			// Pick something sane to show the user
			if (issueTypeOtherText == "") {
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

		private void setItems(String[] items) {
			this.items = new String[items.length];
			for (int i = 0; i < items.length; ++i) {
				this.items[i] = new String(items[i]);
			}
		}
	}

	private boolean questionAnswered() {

		if ((spnSeverity.getSelectedItemPosition()    > 0) ||
			(spnConflict.getSelectedIndicies().size() > 0) ||
			(spnIssueType.getSelectedItemPosition()   > 0)) {
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

		if (null != (settings = getSharedPreferences(PREFS_NOTE_QUESTIONS, MODE_PRIVATE))) {
			if (null != (editor = settings.edit())) {
				saveSpinnerPosition(editor, spnSeverity,  PREF_SEVERITY);
				saveSpinnerPosition(editor, spnConflict,  PREF_CONFLICT);
				saveSpinnerPosition(editor, spnIssueType, PREF_ISSUE   );
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
			if (null != (settings = getSharedPreferences(PREFS_NOTE_QUESTIONS, MODE_PRIVATE))) {
				if (null != (prefs = settings.getAll())) {
					for (Entry<String, ?> entry : prefs.entrySet()) {
						try {
							switch (Integer.parseInt(entry.getKey())) {
							case PREF_SEVERITY: setSpinnerSetting(spnSeverity,  entry); break;
							case PREF_CONFLICT: setSpinnerSetting(spnConflict,  entry); break;
							case PREF_ISSUE:    setSpinnerSetting(spnIssueType, entry); break;
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
		dbAdapter.deleteNoteAnswers(noteId);

		// Enter the user selections into the local database
		try {
			submitSpinnerSelection(spnSeverity,  dbAdapter, DbQuestions.NOTE_SEVERITY,
					DbAnswers.noteSeverity);

			submitSpinnerSelection(spnConflict,  dbAdapter, DbQuestions.NOTE_CONFLICT,
					DbAnswers.noteConflict, DbAnswers.noteConflictOther );

			submitSpinnerSelection(spnIssueType, dbAdapter, DbQuestions.NOTE_ISSUE,
					DbAnswers.noteIssue, DbAnswers.noteIssueOther, issueTypeOtherText);

			recordNoteType(spnIssueType, DbAnswers.noteIssue);
			recordNoteSeverity(spnSeverity, DbAnswers.noteSeverity);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		finally {
			dbAdapter.close();
		}
	}

	private void submitSpinnerSelection(Spinner spinner, DbAdapter dbAdapter,
			int question_id, int[] answer_ids) {
		submitSpinnerSelection(spinner, dbAdapter, question_id, answer_ids, -1, null);
	}
	/**
	 * Enters the spinner selection into the database
	 * @param spinner
	 * @param dbAdapter
	 * @param question_id
	 * @param answers
	 */
	private void submitSpinnerSelection(Spinner spinner, DbAdapter dbAdapter,
			int question_id, int[] answer_ids, int other_id, String other_text) {

		// Note: The first entry is always blank, the array of answers displayed
		// by the UI is one greater than the number of answers in the database.
		int answerIndex = spinner.getSelectedItemPosition() - 1;

		if (answerIndex >= 0) {
			if (answer_ids[answerIndex] == other_id) {
				dbAdapter.addAnswerToNote(noteId, question_id, other_id, other_text);
			}
			else {
				dbAdapter.addAnswerToNote(noteId, question_id, answer_ids[answerIndex]);
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
	private void submitSpinnerSelection(MultiSelectionSpinner spinner, DbAdapter dbAdapter,
			int question_id, int[] answers, int answerOther) {
		List<Integer> selectedIndicies = spinner.getSelectedIndicies();
		for (int index : selectedIndicies) {
			if ((answerOther >= 0) && (answers[index] == answerOther)) {
				dbAdapter.addAnswerToNote(noteId, question_id, answers[index], spinner.getOtherText());
			}
			else {
				dbAdapter.addAnswerToNote(noteId, question_id, answers[index]);
			}
		}
	}

	private void recordNoteType(Spinner spinner, int[] answerIds) {
		int answerIndex = spinner.getSelectedItemPosition() - 1;

		if (answerIndex >= 0) {
			noteType = answerIds[answerIndex];
		}
		else {
			noteType = -1;
		}
	}

	private void recordNoteSeverity(Spinner spinner, int[] answerIds) {
		int answerIndex = spinner.getSelectedItemPosition() - 1;

		if (answerIndex >= 0) {
			noteSeverity = answerIds[answerIndex];
		}
		else {
			noteSeverity = -1;
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

	private void transitionToNoteDetailActivity() {

		// Create intent to go to the NoteDetailActivity
		Intent intent = new Intent(this, NoteDetailActivity.class);
		intent.putExtra(NoteDetailActivity.EXTRA_NOTE_TYPE, noteType);
		intent.putExtra(NoteDetailActivity.EXTRA_NOTE_SEVERITY, noteSeverity);
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
