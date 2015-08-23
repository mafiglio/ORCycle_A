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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class UserInfoActivity extends Activity {

	private static final String MODULE_TAG = "UserInfoActivity";

	private static final String PREFS_USER_INFO = "PREFS_USER_INFO";
	public static final String PREFS_USER_INFO_UPLOAD = "PREFS_USER_INFO_UPLOAD";

	public static final String EXTRA_PREVIOUS_ACTIVITY = "previousActivity";
	public static final int EXTRA_PREVIOUS_ACTIVITY_UNDEFINED = -1;
	public static final int EXTRA_FRAGMENT_MAIN_INPUT = 0;
	public static final int EXTRA_FRAGMENT_SETTINGS = 1;

	public static final int PREF_EMAIL           = 0;
	public static final int PREF_RIDER_ABILITY   = 1;
	public static final int PREF_RIDER_TYPE      = 2;
	public static final int PREF_CYCLE_FREQUENCY = 3;
	public static final int PREF_CYCLE_WEATHER   = 4;
	public static final int PREF_NUM_BIKES       = 5;
	public static final int PREF_BIKE_TYPES      = 6;
	public static final int PREF_OCCUPATION      = 7;
	public static final int PREF_AGE             = 8;
	public static final int PREF_GENDER          = 9;
	public static final int PREF_VEHICLES        = 10;
	public static final int PREF_WORKERS         = 11;
	public static final int PREF_ETHNICITY       = 12;
	public static final int PREF_INCOME          = 13;
	public static final int PREF_INSTALLED       = 14;
	public static final int PREF_DEVICE_MODEL    = 15;
	public static final int PREF_APP_VERSION     = 16;
	public static final int PREF_EMAIL_NAME      = 17;
	public static final int PREF_EMAIL_PHONE     = 18;
	public static final int PREF_RIDER_TYPE_OTHER = 1002;
	public static final int PREF_BIKE_TYPE_OTHER  = 1006;
	public static final int PREF_OCCUPATION_OTHER = 1007;
	public static final int PREF_GENDER_OTHER     = 1009;
	public static final int PREF_ETHNICITY_OTHER  = 1012;

	private enum ExitTransition { EXIT_BACK, EXIT_SEND };
	private MultiSelectionSpinner spnrBikeTypes;
	private Spinner spnrRiderType;
	private Spinner spnrOccupation;
	private Spinner spnrGender;
	private Spinner spnrEthnicity;
	private Button btnSave;
	private Button btnPrivacyPolicy1;
	private Button btnPrivacyPolicy2;

	private OnItemWithOtherSelectedListener spnrRiderType_OnItemSelected = null;
	private OnItemWithOtherSelectedListener spnrOccupation_OnItemSelected = null;
	private OnItemWithOtherSelectedListener spnrGender_OnItemSelected = null;
	private OnItemWithOtherSelectedListener spnrEthnicity_OnItemSelected = null;

	private int previousActivity;

	// *********************************************************************************
	// *                              Activity Handlers
	// *********************************************************************************

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			Log.v(MODULE_TAG, "Cycle: onCreate()");

			// load root view
			setContentView(R.layout.activity_user_info);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

			// get input values for this view
			Intent myIntent = getIntent();

			previousActivity = myIntent.getIntExtra(EXTRA_PREVIOUS_ACTIVITY, EXTRA_PREVIOUS_ACTIVITY_UNDEFINED);
			if (!((previousActivity == EXTRA_FRAGMENT_SETTINGS) ||(previousActivity == EXTRA_FRAGMENT_MAIN_INPUT))) {
				throw new IllegalArgumentException(MODULE_TAG + ": EXTRA_NOTE_SOURCE invalid argument.");
			}

			// Create listeners for the spinner setOnItemSelectedListener
			spnrRiderType_OnItemSelected = new OnItemWithOtherSelectedListener(this);
			spnrRiderType_OnItemSelected.setOtherIndex(DbAnswers.findIndex(DbAnswers.userInfoRiderType, DbAnswers.userInfoRiderTypeOther));
			spnrRiderType_OnItemSelected.setItems(getResources().getStringArray(R.array.qa_17_ridertype));
			spnrRiderType_OnItemSelected.ignoreNextOtherSelection();

			spnrOccupation_OnItemSelected = new OnItemWithOtherSelectedListener(this);
			spnrOccupation_OnItemSelected.setOtherIndex(DbAnswers.findIndex(DbAnswers.userInfoOccupation, DbAnswers.userInfoOccupationOther));
			spnrOccupation_OnItemSelected.setItems(getResources().getStringArray(R.array.qa_05_occupation));
			spnrOccupation_OnItemSelected.ignoreNextOtherSelection();

			spnrGender_OnItemSelected = new OnItemWithOtherSelectedListener(this);
			spnrGender_OnItemSelected.setOtherIndex(DbAnswers.findIndex(DbAnswers.userInfoGender, DbAnswers.userInfoGenderOther));
			spnrGender_OnItemSelected.setItems(getResources().getStringArray(R.array.qa_03_gender));
			spnrGender_OnItemSelected.ignoreNextOtherSelection();

			spnrEthnicity_OnItemSelected = new OnItemWithOtherSelectedListener(this);
			spnrEthnicity_OnItemSelected.setOtherIndex(DbAnswers.findIndex(DbAnswers.userInfoEthnicity, DbAnswers.userInfoEthnicityOther));
			spnrEthnicity_OnItemSelected.setItems(getResources().getStringArray(R.array.qa_04_ethnicity));
			spnrEthnicity_OnItemSelected.ignoreNextOtherSelection();

			// Set the listeners for the spinner setOnItemSelectedListener
			spnrRiderType = (Spinner) findViewById(R.id.spnrRiderType);
			spnrRiderType.setOnItemSelectedListener(spnrRiderType_OnItemSelected);

			spnrOccupation = (Spinner) findViewById(R.id.spnrOccupation);
			spnrOccupation.setOnItemSelectedListener(spnrOccupation_OnItemSelected);

			spnrGender = (Spinner) findViewById(R.id.spnrGender);
			spnrGender.setOnItemSelectedListener(spnrGender_OnItemSelected);

			spnrEthnicity = (Spinner) findViewById(R.id.spnrEthnicity);
			spnrEthnicity.setOnItemSelectedListener(spnrEthnicity_OnItemSelected);

			// Since the selection array cannot currently be set from the resource
			// file for the MultiSelectionSpinner, it must be done here
			spnrBikeTypes = (MultiSelectionSpinner) findViewById(R.id.spnrBikeTypes);
			spnrBikeTypes.setItems(getResources().getStringArray(R.array.qa_10_bikeTypes));
			spnrBikeTypes.setTitle(getResources().getString(R.string.q10_bikeTypes));
			spnrBikeTypes.setOtherIndex(DbAnswers.findIndex(DbAnswers.userInfoBikeTypes, DbAnswers.userInfoBikeTypeOther));

			RecallPreferences();

			((EditText) findViewById(R.id.editEmail)).setImeOptions(EditorInfo.IME_ACTION_DONE);
			((EditText) findViewById(R.id.editEmailName)).setImeOptions(EditorInfo.IME_ACTION_DONE);
			((EditText) findViewById(R.id.editEmailPhone)).setImeOptions(EditorInfo.IME_ACTION_DONE);

			btnSave = (Button) findViewById(R.id.btn_aui_save_user_info);
			btnSave.setOnClickListener(new ButtonSave_OnClickListener());

			btnPrivacyPolicy1 = (Button) findViewById(R.id.btn_aui_privacy_policy_1);
			btnPrivacyPolicy1.setOnClickListener(new PrivacyPolicy_OnClickListener());

			btnPrivacyPolicy2 = (Button) findViewById(R.id.btn_aui_privacy_policy_2);
			btnPrivacyPolicy2.setOnClickListener(new PrivacyPolicy_OnClickListener());
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	@Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
		try {
			Log.v(MODULE_TAG, "Cycle: onRestoreInstanceState()");
			RecallPreferences();
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
    }

	@Override
    protected void onSaveInstanceState(Bundle outState) {
		try {
			Log.v(MODULE_TAG, "Cycle: onSaveInstanceState()");
			savePreferences(false);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
    }

	/**
	 * Inflates the UserInfo menu items
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		try {
			// Inflate the menu items for use in the action bar
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.user_info, menu);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * Handles item selections
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// Handle presses on the action bar items
		switch (item.getItemId()) {

		case R.id.action_user_info_send:

			saveAndFinish();
			return true;

		default:

			return super.onOptionsItemSelected(item);
		}
	}

	private void saveAndFinish() {
		try {
			savePreferences(true);
			// this extra call to savePreferences is absolutely necessary.  It
			// allows changes to be stored for later return to this activity.
			savePreferences(false);
			UserInfoUploader uploader = new UserInfoUploader(this, MyApplication.getInstance().getUserId());
			uploader.execute();
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}

		try {
			transitionToTabsConfigActivity(ExitTransition.EXIT_SEND);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	/**
	 * Handles OnBackPressed key event
	 */
	@Override
	public void onBackPressed() {
		try {
			// I found that if I make selections in this activity, I
			// expect them to be saved even if I press the <back> button
			savePreferences(false);
			transitionToTabsConfigActivity(ExitTransition.EXIT_BACK);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
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
				saveAndFinish();
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

    /**
     * Class: PrivacyPolicy_OnClickListener
     *
     * Description: Callback to be invoked when btnPrivacyPolicy button is clicked
     */
	private final class PrivacyPolicy_OnClickListener implements View.OnClickListener {

		/**
		 * Description: Handles onClick for view
		 */
		public void onClick(View v) {

			try {
				transitionToWebViewActivity(R.string.ats_webview_Title_privacy_policy, MyApplication.URI_PRIVACY_POLICY);
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	// *********************************************************************************
	// *                               Preferences
	// *********************************************************************************

	/**
	 * Recall UserInfo preferences
	 */
	private void RecallPreferences() {

		SharedPreferences settings = getSharedPreferences(PREFS_USER_INFO, MODE_PRIVATE);
		Map<String, ?> prefs = settings.getAll();

		for (Entry<String, ?> p : prefs.entrySet()) {

			int key = Integer.parseInt(p.getKey());

			switch (key) {
			case PREF_EMAIL:           recallPref( (EditText) findViewById(R.id.editEmail         ),  p); break;
			case PREF_EMAIL_NAME:      recallPref( (EditText) findViewById(R.id.editEmailName     ),  p); break;
			case PREF_EMAIL_PHONE:     recallPref( (EditText) findViewById(R.id.editEmailPhone    ),  p); break;
			case PREF_RIDER_ABILITY:   recallPref( (Spinner) findViewById(R.id.spnrRiderAbility   ),  p); break;
			case PREF_RIDER_TYPE:      recallPref( (Spinner) findViewById(R.id.spnrRiderType      ),  p); break;
			case PREF_CYCLE_FREQUENCY: recallPref( (Spinner) findViewById(R.id.spnrCycleFrequency ),  p); break;
			case PREF_CYCLE_WEATHER:   recallPref( (Spinner) findViewById(R.id.spnrCycleWeather   ),  p); break;
			case PREF_NUM_BIKES:       recallPref( (Spinner) findViewById(R.id.spnrNumBikes       ),  p); break;
			case PREF_BIKE_TYPES:      recallPref( (MultiSelectionSpinner) findViewById(R.id.spnrBikeTypes),  p); break;
			case PREF_OCCUPATION:      recallPref( (Spinner) findViewById(R.id.spnrOccupation     ),  p); break;
			case PREF_AGE:             recallPref( (Spinner) findViewById(R.id.spnrAge            ),  p); break;
			case PREF_GENDER:          recallPref( (Spinner) findViewById(R.id.spnrGender         ),  p); break;
			case PREF_VEHICLES:        recallPref( (Spinner) findViewById(R.id.spnrVehicles       ),  p); break;
			case PREF_WORKERS:         recallPref( (Spinner) findViewById(R.id.spnrWorkers        ),  p); break;
			case PREF_ETHNICITY:       recallPref( (Spinner) findViewById(R.id.spnrEthnicity      ),  p); break;
			case PREF_INCOME:          recallPref( (Spinner) findViewById(R.id.spnrIncome         ),  p); break;

			case PREF_RIDER_TYPE_OTHER: spnrRiderType_OnItemSelected.setOtherText((String)p.getValue());  break;
			case PREF_BIKE_TYPE_OTHER:  spnrBikeTypes.setOtherText((String)p.getValue()); break;
			case PREF_OCCUPATION_OTHER: spnrOccupation_OnItemSelected.setOtherText((String)p.getValue()); break;
			case PREF_GENDER_OTHER:     spnrGender_OnItemSelected.setOtherText((String)p.getValue());     break;
			case PREF_ETHNICITY_OTHER:  spnrEthnicity_OnItemSelected.setOtherText((String)p.getValue());  break;
			}
		}
	}

	/**
	 * Save UserInfo preferences
	 */
	private void savePreferences(boolean forUpload) {

		// Save user preferences. We need an Editor object to
		// make changes. All objects are from android.context.Context
		SharedPreferences settings;
		if (forUpload) {
			settings = getSharedPreferences(PREFS_USER_INFO_UPLOAD, MODE_PRIVATE);
		}
		else {
			settings = getSharedPreferences(PREFS_USER_INFO, MODE_PRIVATE);
		}
		SharedPreferences.Editor editor = settings.edit();

		savePref( editor, PREF_EMAIL,           (EditText) findViewById(R.id.editEmail          ));
		savePref( editor, PREF_EMAIL_NAME,      (EditText) findViewById(R.id.editEmailName      ));
		savePref( editor, PREF_EMAIL_PHONE,     (EditText) findViewById(R.id.editEmailPhone     ));

		savePref( editor, PREF_RIDER_ABILITY,   (Spinner)  findViewById(R.id.spnrRiderAbility   ), forUpload);

		savePref( editor, PREF_RIDER_TYPE,      (Spinner)  findViewById(R.id.spnrRiderType      ),
				DbAnswers.userInfoRiderType,
				DbAnswers.userInfoRiderTypeOther,
				spnrRiderType_OnItemSelected.getOtherText(),
				PREF_RIDER_TYPE_OTHER, forUpload);

		savePref( editor, PREF_CYCLE_FREQUENCY, (Spinner)  findViewById(R.id.spnrCycleFrequency ), forUpload);
		savePref( editor, PREF_CYCLE_WEATHER,   (Spinner)  findViewById(R.id.spnrCycleWeather   ), forUpload);
		savePref( editor, PREF_NUM_BIKES,       (Spinner)  findViewById(R.id.spnrNumBikes       ), forUpload);
		savePref( editor, PREF_BIKE_TYPES,      spnrBikeTypes,
				DbAnswers.userInfoBikeTypes,
				DbAnswers.userInfoBikeTypeOther,
				spnrBikeTypes.getOtherText(),
				PREF_BIKE_TYPE_OTHER);

		savePref( editor, PREF_OCCUPATION,      (Spinner)  findViewById(R.id.spnrOccupation     ),
				DbAnswers.userInfoOccupation,
				DbAnswers.userInfoOccupationOther,
				spnrRiderType_OnItemSelected.getOtherText(),
				PREF_OCCUPATION_OTHER, forUpload);

		savePref( editor, PREF_AGE,             (Spinner)  findViewById(R.id.spnrAge            ), forUpload);

		savePref( editor, PREF_GENDER,          (Spinner)  findViewById(R.id.spnrGender         ),
				DbAnswers.userInfoGender,
				DbAnswers.userInfoGenderOther,
				spnrGender_OnItemSelected.getOtherText(),
				PREF_GENDER_OTHER, forUpload);

		savePref( editor, PREF_VEHICLES,        (Spinner)  findViewById(R.id.spnrVehicles       ), forUpload);
		savePref( editor, PREF_WORKERS,         (Spinner)  findViewById(R.id.spnrWorkers        ), forUpload);

		savePref( editor, PREF_ETHNICITY,       (Spinner)  findViewById(R.id.spnrEthnicity      ),
				DbAnswers.userInfoEthnicity,
				DbAnswers.userInfoEthnicityOther,
				spnrRiderType_OnItemSelected.getOtherText(),
				PREF_ETHNICITY_OTHER, forUpload);

		savePref( editor, PREF_INCOME,          (Spinner)  findViewById(R.id.spnrIncome         ), forUpload);
		savePref( editor, PREF_INSTALLED, MyApplication.getInstance().getFirstUseString());
		savePref( editor, PREF_DEVICE_MODEL, MyApplication.getInstance().getDeviceModel());
		savePref( editor, PREF_APP_VERSION, MyApplication.getInstance().getAppVersion());

		// Don't forget to commit your edits!!!
		editor.commit();

		// Set the flag indicating user info has been filled out and scheduled for upload
		if (forUpload && desiredQuestionsAnswered()) {
			MyApplication.getInstance().setUserProfileUploaded(true);
		}
	}

	/**
	 * Returns true if desired user questions have been answered, false otherwise
	 * @return true if desired user questions have been answered, false otherwise
	 */
	private boolean desiredQuestionsAnswered() {
		if ((((Spinner) findViewById(R.id.spnrRiderAbility  )).getSelectedItemPosition() > 0) &&
			(((Spinner) findViewById(R.id.spnrRiderType     )).getSelectedItemPosition() > 0) &&
			(((Spinner) findViewById(R.id.spnrCycleFrequency)).getSelectedItemPosition() > 0) &&
			(((Spinner) findViewById(R.id.spnrCycleWeather  )).getSelectedItemPosition() > 0) &&
			(((Spinner) findViewById(R.id.spnrNumBikes      )).getSelectedItemPosition() > 0) &&
			(((MultiSelectionSpinner) findViewById(R.id.spnrBikeTypes)).getSelectedIndicies().size() > 0)) {
			return true;
		}
		return false;
	}


	/**
	 * Insert the position of the spinner into an int preference in the editor
	 * @param editor Preference editor
	 * @param prefIndex Named index where preference is stored
	 * @param spinner Instance of a Spinner widget
	 */
	private static final void savePref(SharedPreferences.Editor editor, int prefIndex, Spinner spinner,
			int[] answer_ids, int other_id, String other_text, int otherPrefIndex, boolean forUpload) {

		if (forUpload) {
			editor.putInt("" + prefIndex, spinner.getSelectedItemPosition() - 1);
		}
		else {
			editor.putInt("" + prefIndex, spinner.getSelectedItemPosition());
		}

		int answerIndex = spinner.getSelectedItemPosition() - 1;
		if ((answerIndex >= 0) && (answer_ids[answerIndex] == other_id)) {
			editor.putString("" + otherPrefIndex, other_text);
		}
	}

	/**
	 * Insert the position of the spinner into an int preference in the editor
	 * @param editor Preference editor
	 * @param prefIndex Named index where preference is stored
	 * @param spinner Instance of a Spinner widget
	 */
	private static final void savePref(SharedPreferences.Editor editor, int prefIndex, Spinner spinner, boolean forUpload) {
		if (forUpload) {
			editor.putInt("" + prefIndex, spinner.getSelectedItemPosition() - 1);
		}
		else {
			editor.putInt("" + prefIndex, spinner.getSelectedItemPosition());
		}
	}

	/**
	 * Insert the long value into the preference in the editor
	 * @param editor Preference editor
	 * @param prefIndex Named index where preference is stored
	 * @param spinner Instance of a Spinner widget
	 */
	private static final void savePref(SharedPreferences.Editor editor, int prefIndex, String value) {
		editor.putString("" + prefIndex, value);
	}

	/**
	 * Insert the position of the spinner into an int preference in the editor
	 * @param editor Preference editor
	 * @param prefIndex Named index where preference is stored
	 * @param spinner Instance of a Spinner widget
	 */
	private static final void savePref(SharedPreferences.Editor editor, int prefIndex, MultiSelectionSpinner spinner) {
		editor.putString("" + prefIndex, spinner.getSelectedIndicesAsString());
	}

	/**
	 * Insert the position of the spinner into an int preference in the editor
	 * @param editor Preference editor
	 * @param prefIndex Named index where preference is stored
	 * @param spinner Instance of a Spinner widget
	 */
	private static final void savePref(SharedPreferences.Editor editor, int prefIndex, MultiSelectionSpinner spinner,
			int[] answers, int otherId, String other_text, int otherPrefIndex) {

		editor.putString("" + prefIndex, spinner.getSelectedIndicesAsString());

		// If other is one of the selections, save the text inormation
		List<Integer> selectedIndicies = spinner.getSelectedIndicies();
		for (int index : selectedIndicies) {
			if ((otherId >= 0) && (answers[index] == otherId)) {
				editor.putString("" + otherPrefIndex, other_text);
			}
		}
	}

	/**
	 * Insert the text of the EditText widget into an int preference in the editor
	 * @param editor Preference editor
	 * @param prefIndex Named index where preference is stored
	 * @param editText Instance of an EditText widget
	 */
	private static final void savePref(SharedPreferences.Editor editor, int prefIndex, EditText editText) {

		String text = editText.getText().toString();
		editor.putString("" + prefIndex, text);
	}

	/**
	 * Select the spinner position from the given preference mapped entry
	 * @param spinner Instance of a Spinner widget
	 * @param entry an instance of a mapped entry
	 */
	private static final void recallPref(Spinner spinner, Entry<String, ?> entry) {
		try {
			spinner.setSelection(((Integer) entry.getValue()).intValue());
		}
		catch (Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	/**
	 * Sets MultiSelectionSpinner settings from a map entry
	 * @param spinner Instance of a Spinner widget
	 * @param entry an instance of a mapped entry
	 */
	private static final void recallPref(MultiSelectionSpinner spinner, Entry<String, ?> p) {
		try {
			// Retireve entry value
			String entry = (String) p.getValue();

			if ((null != entry) && !entry.equals("")) {
				// Set MultiSelectionSpinner spinner control values
				spinner.setSelection(entry);
			}
		}
		catch (Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	/**
	 * Insert the text from the given preference mapped entry into
	 * an instance of an EditText widget
	 * @param editText
	 * @param entry an instance of a mapped entry
	 */
	private static final void recallPref(EditText editText, Entry<String, ?> entry) {
		try {
			String text = entry.getValue().toString();
			editText.setText(text);
		}
		catch (Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	// *********************************************************************************
	// *                                 Transitions
	// *********************************************************************************

	private void transitionToTabsConfigActivity(ExitTransition exitTransition) {

		Intent intent = new Intent(this, TabsConfig.class);

		if (previousActivity == EXTRA_FRAGMENT_MAIN_INPUT) {
			intent.putExtra(TabsConfig.EXTRA_SHOW_FRAGMENT, TabsConfig.FRAG_INDEX_MAIN_INPUT);
		}
		else {
			intent.putExtra(TabsConfig.EXTRA_SHOW_FRAGMENT, TabsConfig.FRAG_INDEX_SETTINGS);
		}
		startActivity(intent);
		finish();
		if (exitTransition == ExitTransition.EXIT_BACK) {
			overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
		}
		else {
			overridePendingTransition(android.R.anim.fade_in, R.anim.slide_out_down);
		}
	}

	private void transitionToWebViewActivity(int titleId, String uri) {
		String title = getResources().getString(titleId);
		Intent intent = new Intent(this, WebViewActivity.class);
		intent.putExtra(WebViewActivity.EXTRA_URL, uri);
		intent.putExtra(WebViewActivity.EXTRA_TITLE, title);
		startActivity(intent);
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	}
}
