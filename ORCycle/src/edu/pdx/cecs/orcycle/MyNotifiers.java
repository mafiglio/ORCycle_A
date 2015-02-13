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

import java.util.Locale;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class MyNotifiers {

	private static final int RECORDING_ID = 1;
	private static final int REMINDER_ID = 2;

	public static void setReminderNotification(Context context, String reminderName) {

		CharSequence tickerText = "Reminder: " + reminderName + "Would you like to start ORcycle";
		CharSequence contentTitle = "ORcycle Reminder";
		CharSequence contentText = "Tap to start ORcycle";

		//| Notification.FLAG_ONGOING_EVENT
		//| Notification.FLAG_SHOW_LIGHTS | Notification.FLAG_INSISTENT
		//| Notification.FLAG_NO_CLEAR;


		// Define intent to be executed when notification is clicked
		Intent intent = new Intent(context, TabsConfig.class);
		intent.putExtra(TabsConfig.EXTRA_SHOW_FRAGMENT, TabsConfig.FRAG_INDEX_MAIN_INPUT);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

		// Build notification
		Notification.Builder builder = new Notification.Builder(context);
		builder.setSmallIcon(R.drawable.ic_launcher)
			   .setTicker(tickerText)
			   .setWhen(System.currentTimeMillis())
			   .setLights(0xffff00ff, 300, 3000)
			   .setContentTitle(contentTitle)
			   .setContentText(contentText)
			   .setContentIntent(pendingIntent)
			   .setAutoCancel(true);
		Notification notification = builder.getNotification();

		// Execute notification
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(REMINDER_ID, notification);
	}

	public static void setRecordingNotification(Context context) {

		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		int icon = R.drawable.ic_launcher;
		CharSequence tickerText = "Recording...";
		long when = System.currentTimeMillis();

		Notification notification = new Notification(icon, tickerText, when);

		notification.ledARGB = 0xffff00ff;
		notification.ledOnMS = 300;
		notification.ledOffMS = 3000;
		notification.flags = notification.flags
				| Notification.FLAG_ONGOING_EVENT
				| Notification.FLAG_SHOW_LIGHTS | Notification.FLAG_INSISTENT
				| Notification.FLAG_NO_CLEAR;

		CharSequence contentTitle = "ORcycle recording";
		CharSequence contentText = "Tap to see your ongoing trip";

		Intent intent = new Intent(context, TabsConfig.class);
		intent.putExtra(TabsConfig.EXTRA_SHOW_FRAGMENT, TabsConfig.FRAG_INDEX_MAIN_INPUT);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
		notification.setLatestEventInfo(context, contentTitle, contentText, pendingIntent);

		mNotificationManager.notify(RECORDING_ID, notification);
	}

	public static void cancelReminderNotification(Context context) {

		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(REMINDER_ID);
	}

	public static void cancelRecordingNotification(Context context) {

		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(RECORDING_ID);
	}

	public static void setNotification(Context context, double startTime) {

		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		int icon = R.drawable.ic_launcher;
		long when = System.currentTimeMillis();
		int minutes = (int) (when - startTime) / 60000;
		CharSequence tickerText = String.format(Locale.US, "Still recording (%d min)", minutes);

		Notification notification = new Notification(icon, tickerText, when);
		notification.flags |= Notification.FLAG_ONGOING_EVENT
				| Notification.FLAG_SHOW_LIGHTS;
		notification.ledARGB = 0xffff00ff;
		notification.ledOnMS = 300;
		notification.ledOffMS = 3000;

		CharSequence contentTitle = "ORcycle recording";
		CharSequence contentText = "Tap to see your ongoing trip";

		Intent intent = new Intent(context, FragmentMainInput.class);
		intent.putExtra(TabsConfig.EXTRA_SHOW_FRAGMENT, TabsConfig.FRAG_INDEX_MAIN_INPUT);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
		notification.setLatestEventInfo(context, contentTitle, contentText, pendingIntent);

		mNotificationManager.notify(RECORDING_ID, notification);
	}

}
