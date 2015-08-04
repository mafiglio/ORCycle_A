package edu.pdx.cecs.orcycle;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SixMonthReminder {

	private static final String MODULE_TAG = "SixMonthReminder";

	public static final String EXTRA_REMINDER_NAME = "REMINDER_NAME";  // TODO: place in reciever

	public static void reschedule(Context context) {

		try {
			AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

			// Cancel all previous alarms for this reminder
			alarmMgr.cancel(getAlarmIntent(context));

			// Create a calendar set to the current time
			Calendar alarmCalendar = Calendar.getInstance();
			alarmCalendar.setTimeInMillis(System.currentTimeMillis());

			// Add six months to the calendar
			alarmCalendar.add(Calendar.MONTH, 6);
			//alarmCalendar.add(Calendar.MINUTE, 1);

			Log.i(MODULE_TAG, "Alarm set for: " + alarmCalendar.getTime().toString());

			PendingIntent alarmIntent = getAlarmIntent(context);

			// Set alarm to go off at the time set by the calendar
			alarmMgr.set(AlarmManager.RTC, alarmCalendar.getTimeInMillis(), alarmIntent);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	private static PendingIntent getAlarmIntent(Context context) {

		Intent intent = new Intent(ReminderReceiver.ACTION_SIX_MONTH_REMINDER);

		// Construct and return the pending intent for broadcasting the alarm
		return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
	}
}
