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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TimePicker;

public class EditReminderActivity extends Activity {

	private static final String MODULE_TAG = "EditReminderActivity";

	public static final String EXTRA_NEW_REMINDER = "new_reminder";
	public static final String EXTRA_REMINDER_ID = "reminder_id";
	public static final String EXTRA_REMINDER_NAME = "reminder_name";
	public static final String EXTRA_REMINDER_DAYS = "reminder_days";
	public static final String EXTRA_REMINDER_HOURS = "reminder_hours";
	public static final String EXTRA_REMINDER_MINUTES = "reminder_minutes";
	public static final String EXTRA_REMINDER_ENABLED = "reminder_enabled";

	// UI Elements
	private EditText etName;
	private Button buttonSave;
	private Button buttonCancel;
	private CheckBox chkSunday;
	private CheckBox chkMonday;
	private CheckBox chkTuesday;
	private CheckBox chkWednesday;
	private CheckBox chkThursday;
	private CheckBox chkFriday;
	private CheckBox chkSaturday;
	private TimePicker timePicker;
	private RelativeLayout rlEnabled;
	private CheckBox chkEnabled;

	private long reminderId;
	private String reminderName;
	private int reminderDays;
	private boolean reminderEnabled;
	private int reminderHours;
	private int reminderMinutes;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			setContentView(R.layout.activity_edit_reminder);

			etName = (EditText) findViewById(R.id.et_aer_name);

			// Setup the save button
			buttonSave = (Button) findViewById(R.id.btn_aer_save);
			buttonSave.setOnClickListener(new ButtonSave_OnClickListener());

			// Setup the cancel button
			buttonCancel = (Button) findViewById(R.id.btn_aer_cancel);
			buttonCancel.setOnClickListener(new ButtonCancel_OnClickListener());

			// Get references to the checkboxes
			chkSunday = (CheckBox) findViewById(R.id.chk_aer_sun);
			chkMonday = (CheckBox) findViewById(R.id.chk_aer_mon);
			chkTuesday = (CheckBox) findViewById(R.id.chk_aer_tue);
			chkWednesday = (CheckBox) findViewById(R.id.chk_aer_wed);
			chkThursday = (CheckBox) findViewById(R.id.chk_aer_thu);
			chkFriday = (CheckBox) findViewById(R.id.chk_aer_fri);
			chkSaturday = (CheckBox) findViewById(R.id.chk_aer_sat);

			timePicker = (TimePicker) findViewById(R.id.time_picker_aer);
			rlEnabled = (RelativeLayout) findViewById(R.id.rl_aer_enabled);
			chkEnabled = (CheckBox) findViewById(R.id.chk_aer_enabled);

			LoadReminderVars(savedInstanceState);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		try {
			LoadReminderVars(savedInstanceState);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		try {
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		try {
			SaveReminderVars(savedInstanceState);
			super.onSaveInstanceState(savedInstanceState);
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
     * Description: Callback to be invoked when saveButton button is clicked
     */
	private final class ButtonSave_OnClickListener implements View.OnClickListener {

		/**
		 * Description: Handles onClick for view
		 */
		public void onClick(View v) {
			try {
				if (EntriesValidated()) {
					saveReminder();
					finish();
				}
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

    /**
     * Class: ButtonCancel_OnClickListener
     *
     * Description: Callback to be invoked when cancelButton button is clicked
     */
	private final class ButtonCancel_OnClickListener implements View.OnClickListener {

		/**
		 * Description: Handles onClick for view
		 */
		public void onClick(View v) {
			try {
				finish();
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	// *********************************************************************************
	// *                              Misc Methods
	// *********************************************************************************

	private void LoadReminderVars(Bundle savedInstanceState) {

		// Default reminder values for a new reminder
		reminderId = -1;
		reminderName = "";
		reminderDays = 0;
		reminderHours = 0;
		reminderMinutes = 0;
		reminderEnabled = true;

		// Searched the bundle for the values
		if (null == savedInstanceState) { // then this is being called from OnCreate

			Bundle extras = getIntent().getExtras();
			if ((null != extras) && (extras.containsKey(EXTRA_REMINDER_ID))) {
				reminderId = extras.getLong(EXTRA_REMINDER_ID);
			}

			if(reminderId > 0) {
				ReminderHelper rh = new ReminderHelper(this, reminderId);
				reminderName = rh.getName();
				reminderDays = rh.getDays();
				reminderHours = rh.getHours();
				reminderMinutes = rh.getMinutes();
				reminderEnabled = rh.getEnabled();
			}
		}
		else { // Search then extras for the values
			if (savedInstanceState.containsKey(EXTRA_REMINDER_ID)) {
				reminderId = savedInstanceState.getLong(EXTRA_REMINDER_ID);
			}
			if (savedInstanceState.containsKey(EXTRA_REMINDER_NAME)) {
				reminderName = savedInstanceState.getString(EXTRA_REMINDER_NAME);
			}
			if (savedInstanceState.containsKey(EXTRA_REMINDER_DAYS)) {
				reminderDays = savedInstanceState.getInt(EXTRA_REMINDER_DAYS, 0);
			}
			if (savedInstanceState.containsKey(EXTRA_REMINDER_HOURS)) {
				reminderHours = savedInstanceState.getInt(EXTRA_REMINDER_HOURS);
			}
			if (savedInstanceState.containsKey(EXTRA_REMINDER_MINUTES)) {
				reminderMinutes = savedInstanceState.getInt(EXTRA_REMINDER_MINUTES);
			}
			if (savedInstanceState.containsKey(EXTRA_REMINDER_ENABLED)) {
				reminderEnabled = (savedInstanceState.getInt(EXTRA_REMINDER_ENABLED) != 0);
			}
		}

		// Set reminder name
		etName.setText(reminderName);

		// Set reminder days selected
		ReminderHelper rh = new ReminderHelper();
		rh.setDays(reminderDays);
		chkSunday.setChecked(rh.getSunday());
		chkMonday.setChecked(rh.getMonday());
		chkTuesday.setChecked(rh.getTuesday());
		chkWednesday.setChecked(rh.getWednesday());
		chkThursday.setChecked(rh.getThursday());
		chkFriday.setChecked(rh.getFriday());
		chkSaturday.setChecked(rh.getSaturday());

		// Set reminder time
		timePicker.setCurrentHour(reminderHours);
		timePicker.setCurrentMinute(reminderMinutes);

		// Set enabled value
		chkEnabled.setChecked(reminderEnabled);

		// Only show enabled checkbox when editing an existing reminder
		if(reminderId > 0) {
			rlEnabled.setVisibility(View.VISIBLE);
		}
		else {
			rlEnabled.setVisibility(View.GONE);
		}
	}

	private void SaveReminderVars(Bundle savedInstanceState) {

		ReminderHelper rh = new ReminderHelper();
		rh.setSunday(chkSunday.isChecked())
		  .setMonday(chkMonday.isChecked())
		  .setTuesday(chkTuesday.isChecked())
		  .setWednesday(chkWednesday.isChecked())
		  .setThursday(chkThursday.isChecked())
		  .setFriday(chkFriday.isChecked())
		  .setSaturday(chkSaturday.isChecked());

		savedInstanceState.putLong(EXTRA_REMINDER_ID, reminderId);
		savedInstanceState.putString(EXTRA_REMINDER_NAME, reminderName);
		savedInstanceState.putInt(EXTRA_REMINDER_DAYS, rh.getDays());
		savedInstanceState.putInt(EXTRA_REMINDER_HOURS, reminderHours);
		savedInstanceState.putInt(EXTRA_REMINDER_MINUTES, reminderMinutes);
		savedInstanceState.putInt(EXTRA_REMINDER_ENABLED, reminderEnabled ? 1 : 0);
	}

	private boolean EntriesValidated() {

		// Get the name in the edit text entry field
		reminderName = etName.getText().toString().trim();

		// Check the name for a blank entry
		if ((null == reminderName) || (reminderName.equalsIgnoreCase(""))) {
			AlertUserMandatoryAnswers(R.string.aer_error_no_name);
			return false;
		}

		// Check that at least one day has been selected
		if (!(chkSunday.isChecked() || chkMonday.isChecked() || chkTuesday.isChecked() ||
			  chkWednesday.isChecked() || chkThursday.isChecked() || chkFriday.isChecked() ||
			  chkSaturday.isChecked())) {
			AlertUserMandatoryAnswers(R.string.aer_error_no_days);
			return false;
		}

		return true;
	}

	private void AlertUserMandatoryAnswers(int messageId) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getResources().getString(messageId))
				.setCancelable(true)
				.setTitle(R.string.aer_error_dialog_title)
				.setPositiveButton(getResources().getString(R.string.aer_alert_OK),
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog, final int id) {
								dialog.cancel();
							}
						});
		final AlertDialog alert = builder.create();
		alert.show();
	}

	private void saveReminder() {

		ReminderHelper rh = new ReminderHelper(this);

		rh.setId(reminderId)
		  .setName(reminderName)
		  .setSunday(chkSunday.isChecked())
		  .setMonday(chkMonday.isChecked())
		  .setTuesday(chkTuesday.isChecked())
		  .setWednesday(chkWednesday.isChecked())
		  .setThursday(chkThursday.isChecked())
		  .setFriday(chkFriday.isChecked())
		  .setSaturday(chkSaturday.isChecked())
		  .setHours(timePicker.getCurrentHour())
		  .setMinutes(timePicker.getCurrentMinute())
		  .setEnabled(chkEnabled.isChecked())
		  .update();

		Reminder reminder = new Reminder(this, rh);
		if(chkEnabled.isChecked()) {
			reminder.schedule();
		}
		else {
			reminder.cancel();
		}
	}
}