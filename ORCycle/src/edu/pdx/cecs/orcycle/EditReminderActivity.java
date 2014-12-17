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
import android.widget.TimePicker;

public class EditReminderActivity extends Activity {

	private static final String MODULE_TAG = "EditReminderActivity";

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
	private String reminderName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
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

		final DbAdapter mDb = new DbAdapter(this);

		ReminderHelper rh = new ReminderHelper();
		rh.setSunday(chkSunday.isChecked())
		  .setMonday(chkMonday.isChecked())
		  .setTuesday(chkTuesday.isChecked())
		  .setWednesday(chkWednesday.isChecked())
		  .setThursday(chkThursday.isChecked())
		  .setFriday(chkFriday.isChecked())
		  .setSaturday(chkSaturday.isChecked());

		try {
			mDb.open();
			mDb.createReminder(reminderName, rh.days(),
					timePicker.getCurrentHour(), timePicker.getCurrentMinute(), true);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		finally {
			if (null != mDb) {
				mDb.close();
			}
		}
	}
}
