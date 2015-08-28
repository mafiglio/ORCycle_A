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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class ReminderReceiver extends BroadcastReceiver {

	// TODO: Move messages to resource file
	public static final String MODULE_TAG = "ReminderReceiver";
	public static final String ACTION_REMIND_USER = "edu.pdx.cecs.orcycle.ACTION_REMIND_USER";
	public static final String ACTION_USE_REMINDER = "edu.pdx.cecs.orcycle.ACTION_USE_REMINDER";
	public static final String ACTION_ONE_WEEK_USE_REMINDER = "edu.pdx.cecs.orcycle.ACTION_ONE_WEEK_USE_REMINDER";

	/**
	 * Process an alarm event
	 */
	@Override
	public void onReceive(Context context, Intent intent) {

		String reminderName;
		BikeBell bell;
		String toastMessage;
		String tickerText;
		String contentTitle;
		String contentText;

		try {
			// Ring bell
			if (null != (bell = new BikeBell(context))) {
				bell.ring();
			}

			// Determine action to take
			String action = intent.getAction();

			if (action.equals(ACTION_USE_REMINDER)) {

				// Display toast message
				toastMessage = context.getString(R.string.rr_use_toast_message);
				longToastMessage(context, toastMessage);

				// Display notification message
				tickerText = context.getString(R.string.rr_use_ticker_text, UseReminder.REMINDER_PERIOD);
				contentTitle = context.getString(R.string.rr_use_content_title, UseReminder.REMINDER_PERIOD);
				contentText = context.getString(R.string.rr_use_content_text);
				MyNotifiers.setReminderNotification(context, tickerText, contentTitle, contentText);

				UseReminder.scheduleOneWeekAlarm(context);
			}
			else if (action.equals(ACTION_ONE_WEEK_USE_REMINDER)) {

				// Display toast message
				toastMessage = context.getString(R.string.rr_use_toast_message);
				longToastMessage(context, toastMessage);

				// Display notification message
				tickerText = context.getString(R.string.rr_use_ticker_text, UseReminder.REMINDER_PERIOD);
				contentTitle = context.getString(R.string.rr_use_content_title, UseReminder.REMINDER_PERIOD);
				contentText = context.getString(R.string.rr_use_content_text);
				MyNotifiers.setReminderNotification(context, tickerText, contentTitle, contentText);

				UseReminder.rescheduleAlarm(context);
			}
			else if (action.equals(ACTION_REMIND_USER)) {

				// Display toast message
				toastMessage = context.getString(R.string.rr_orcycle_reminder);
				if (null != (reminderName = intent.getStringExtra(Reminder.EXTRA_REMINDER_NAME))) {
					toastMessage = toastMessage + ": " + reminderName;
				}
				else {
					reminderName = "";
					toastMessage = toastMessage + "!";
				}
				longToastMessage(context, toastMessage);

				// If application is not running
				if (!MyApplication.getInstance().isRunning()) {

					// Display query start dialog
					Intent startIntent = new Intent(context, QueryStartActivity.class);
					startIntent.putExtra(QueryStartActivity.EXTRA_REMINDER_NAME, reminderName);
					startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(startIntent);

					// display notification message
					tickerText = context.getString(R.string.rr_query_start, reminderName);
					contentTitle = context.getString(R.string.rr_orcycle_reminder);
					contentText = context.getString(R.string.rr_tap_to_start);
					MyNotifiers.setReminderNotification(context, tickerText, contentTitle, contentText);
				}
			}

		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	private void longToastMessage(Context context, String message) {
		Toast.makeText(context, message, Toast.LENGTH_LONG).show();
		Toast.makeText(context, message, Toast.LENGTH_LONG).show();
		Toast.makeText(context, message, Toast.LENGTH_LONG).show();
	}
}
