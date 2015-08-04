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

	public static final String MODULE_TAG = "ReminderReceiver";
	public static final String ACTION_REMIND_USER = "edu.pdx.cecs.orcycle.ACTION_REMIND_USER";
	public static final String ACTION_SIX_MONTH_REMINDER = "edu.pdx.cecs.orcycle.ACTION_SIX_MONTH_REMINDER";
	public static final String SIX_MONTH_TOAST_MESSAGE = "Please use ORcycle to log your typical commute, social, recreational, etc. trip";

	@Override
	public void onReceive(Context context, Intent intent) {

		String reminderName;
		BikeBell bell;
		CharSequence tickerText;
		CharSequence contentTitle;
		CharSequence contentText;


		try {
			// Ring bell
			if (null != (bell = new BikeBell(context))) {
				bell.ring();
			}

			String action = intent.getAction();
			if (action.equals(ACTION_SIX_MONTH_REMINDER)) {
				Toast.makeText(context, SIX_MONTH_TOAST_MESSAGE, Toast.LENGTH_LONG).show();
				Toast.makeText(context, SIX_MONTH_TOAST_MESSAGE, Toast.LENGTH_LONG).show();
				Toast.makeText(context, SIX_MONTH_TOAST_MESSAGE, Toast.LENGTH_LONG).show();
				tickerText = "Reminder: It's been 6 months!  Would you like to start ORcycle";
				contentTitle = "It's been 6 months!";
				contentText = "Tap to start ORcycle";
				MyNotifiers.setReminderNotification(context, tickerText, contentTitle, contentText);
			}
			else if (action.equals(ACTION_REMIND_USER)) {
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

					tickerText = "Reminder: " + reminderName + "Would you like to start ORcycle";
					contentTitle = "ORcycle Reminder";
					contentText = "Tap to start ORcycle";

					MyNotifiers.setReminderNotification(context, tickerText, contentTitle, contentText);
				}
			}

		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}
}
