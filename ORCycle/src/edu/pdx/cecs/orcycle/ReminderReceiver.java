package edu.pdx.cecs.orcycle;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class ReminderReceiver extends BroadcastReceiver {

	public static final String MODULE_TAG = "ReminderReceiver";
	public static final String ACTION_REMIND_USER = "edu.pdx.cecs.orcycle.ACTION_REMIND_USER";

	@Override
	public void onReceive(Context context, Intent intent) {

		String reminderName;
		BikeBell bell;

		try {
			// Ring bell
			if (null != (bell = new BikeBell(context))) {
				bell.ring();
			}

			// Show reminder message
			String message = "ORcycle reminder";
			if (null != (reminderName = intent.getStringExtra(Reminder.EXTRA_REMINDER_NAME))) {
				message = message + ": " + reminderName;
			}
			else {
				message = message + "!";
			}
			Toast.makeText(context, message, Toast.LENGTH_LONG).show();

			// If application is not running, display ORcycle start dialog
			if (!MyApplication.getInstance().isRunning()) {
				Intent startIntent = new Intent(context, QueryStartActivity.class);
				startIntent.putExtra(QueryStartActivity.EXTRA_REMINDER_NAME, reminderName);
				startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(startIntent);

				MyNotifiers.setReminderNotification(context, reminderName);
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}
}
