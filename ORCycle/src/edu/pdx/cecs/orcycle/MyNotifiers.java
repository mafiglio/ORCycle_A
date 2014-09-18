package edu.pdx.cecs.orcycle;

import java.util.Locale;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class MyNotifiers {

	private static final int RECORDING_ID = 1;

	public static void setNotification(Context context) {

		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		int icon = R.drawable.icon25;
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

		Intent intent = new Intent(context, FragmentMainInput.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
		notification.setLatestEventInfo(context, contentTitle, contentText, pendingIntent);

		mNotificationManager.notify(RECORDING_ID, notification);
	}

	public static void cancelAll(Context context) {

		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancelAll();
	}

	public static void setNotification(Context context, double startTime) {

		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		int icon = R.drawable.icon48;
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

		Intent notificationIntent = new Intent(context, FragmentMainInput.class);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);

		mNotificationManager.notify(RECORDING_ID, notification);
	}

}
