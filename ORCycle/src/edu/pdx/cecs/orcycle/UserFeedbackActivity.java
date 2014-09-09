package edu.pdx.cecs.orcycle;

import android.app.Activity;
import android.content.Intent;
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

	private enum ExitTransition { EXIT_BACK, EXIT_SEND };

	private EditText etUserFeedback;

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

	private void submit(String userFeedback) {

		try {
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}

		transitionToTabsConfigActivity(ExitTransition.EXIT_SEND);
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
		try {
			// Handle presses on the action bar items
			switch (item.getItemId()) {

			case R.id.action_send_user_feedback:
				// send
				submit(etUserFeedback.getEditableText().toString());
				return true;

			default:
				return super.onOptionsItemSelected(item);
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		return false;
	}

	/**
	 *  2.0 and above
	 */
	@Override
	public void onBackPressed() {
		try {
			// skip
			transitionToTabsConfigActivity(ExitTransition.EXIT_BACK);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
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
