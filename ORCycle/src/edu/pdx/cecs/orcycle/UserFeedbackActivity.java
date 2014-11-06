package edu.pdx.cecs.orcycle;

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
import android.view.WindowManager;
import android.widget.EditText;

public class UserFeedbackActivity extends Activity {

	private static final String MODULE_TAG = "UserFeedbackActivity";
	private static final String PREFS_USER_FEEDBACK = "PREFS_USER_FEEDBACK";
	public static final String PREFS_USER_FEEDBACK_UPLOAD = "PREFS_USER_FEEDBACK_UPLOAD";
	public static final int PREF_FEEDBACK = 0;

	private enum ExitTransition { EXIT_BACK, EXIT_SEND };

	private EditText etUserFeedback;

	// *********************************************************************************
	// *                              Activity Handlers
	// *********************************************************************************

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		try {
			setContentView(R.layout.activity_user_feedback);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

			etUserFeedback = (EditText) findViewById(R.id.userFeedback);
			this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
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
	 *  Creates the menu items
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		try {
			// Inflate the menu items for use in the action bar
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.user_feedback, menu);
			return super.onCreateOptionsMenu(menu);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		return false;
	}

	/**
	 *  Handles item selections
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// Handle presses on the action bar items
		switch (item.getItemId()) {

		case R.id.action_send_user_feedback:

			submit();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void submit() {

		String text = etUserFeedback.getText().toString().trim();
		if (text.equals("")) {
			showNoBlankInputDialog();
		}
		else if (text.equals("orcycle:reset-all-dialogs")) {
			try {
				MyApplication.getInstance().setDefaultApplicationSettings();
				savePreferences(false);
				transitionToTabsConfigActivity(ExitTransition.EXIT_SEND);
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
		else {
			try {
				savePreferences(true);
				// this extra call to savePreferences is absolutely necessary.  It
				// allows changes to be stored for later return to this activity.
				savePreferences(false);
				UserFeedbackUploader uploader = new UserFeedbackUploader(this, MyApplication.getInstance().getUserId());
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
	}

	/**
	 *  2.0 and above
	 */
	@Override
	public void onBackPressed() {
		try {
			transitionToTabsConfigActivity(ExitTransition.EXIT_BACK);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	// *********************************************************************************
	// *                               Preferences
	// *********************************************************************************

	/**
	 * Recall UserInfo preferences
	 */
	private void RecallPreferences() {

		SharedPreferences settings = getSharedPreferences(PREFS_USER_FEEDBACK, MODE_PRIVATE);
		Map<String, ?> prefs = settings.getAll();

		for (Entry<String, ?> p : prefs.entrySet()) {

			int key = Integer.parseInt(p.getKey());

			switch (key) {
			case PREF_FEEDBACK:
				recallPref(etUserFeedback,  p);
				break;
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
			settings = getSharedPreferences(PREFS_USER_FEEDBACK_UPLOAD, MODE_PRIVATE);
		}
		else {
			settings = getSharedPreferences(PREFS_USER_FEEDBACK, MODE_PRIVATE);
		}
		SharedPreferences.Editor editor = settings.edit();

		savePref(editor, PREF_FEEDBACK, etUserFeedback);

		// Don't forget to commit your edits!!!
		editor.commit();
	}

	/**
	 * Insert the text of the EditText widget into an int preference in the editor
	 * @param editor Preference editor
	 * @param prefIndex Named index where preference is stored
	 * @param editText Instance of an EditText widget
	 */
	private static final void savePref(SharedPreferences.Editor editor, int prefIndex, EditText editText) {

		String text = editText.getText().toString().trim();
		editor.putString("" + prefIndex, text);
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
	// *                              Miscellaneous
	// *********************************************************************************

	/**
	 * Build dialog telling user enter feedback text
	 */
	private void showNoBlankInputDialog() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(
			"Please enter feedback text before pressing send.")
			.setCancelable(true)
			.setTitle("ORcycle")
			.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
						public void onClick(final DialogInterface dialog,
								final int id) {
							dialog.cancel();
						}
					});
		final AlertDialog alert = builder.create();
		alert.show();
	}

	// *********************************************************************************
	// *                    Transitioning to other activities
	// *********************************************************************************

	/**
	 * Setup transition to the TabsConfigActivity
	 */
	private void transitionToTabsConfigActivity(ExitTransition exitTransition) {

		Intent intent = new Intent(this, TabsConfig.class);

		intent.putExtra(TabsConfig.EXTRA_SHOW_FRAGMENT, TabsConfig.FRAG_INDEX_SETTINGS);
		startActivity(intent);
		finish();
		if (exitTransition == ExitTransition.EXIT_BACK) {
			overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
		}
		else {
			overridePendingTransition(android.R.anim.fade_in, R.anim.slide_out_down);
		}
	}
}
