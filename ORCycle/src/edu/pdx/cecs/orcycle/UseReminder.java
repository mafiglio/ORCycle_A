package edu.pdx.cecs.orcycle;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class UseReminder {

	private static final String MODULE_TAG = "UseReminder";

	public static final int REMINDER_PERIOD = 3; // In months

	/**
	 * Creates an alarm intent for reminder.
	 * with action: ACTION_USE_REMINDER
	 * @param context
	 * @return
	 */
	private static PendingIntent getUseAlarm(Context context) {

		Intent intent = new Intent(ReminderReceiver.ACTION_USE_REMINDER);

		// Construct and return the pending intent for broadcasting the alarm
		return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
	}

	/**
	 * Creates an alarm intent for 1 week from now.
	 * with action: ACTION_ONE_WEEK_REMINDER
	 * @param context
	 * @return
	 */
	private static PendingIntent getOneWeekAlarm(Context context) {

		Intent intent = new Intent(ReminderReceiver.ACTION_ONE_WEEK_USE_REMINDER);

		// Construct and return the pending intent for broadcasting the alarm
		return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
	}

	/**
	 * Cancels the current use reminder, and schedules a new one.
	 * @param context
	 */
	public static void rescheduleAlarm(Context context) {

		try {
			AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

			// Cancel all previous alarms for this reminder
			alarmMgr.cancel(getUseAlarm(context));

			// Create a calendar set to the current time
			Calendar alarmCalendar = Calendar.getInstance();
			alarmCalendar.setTimeInMillis(System.currentTimeMillis());

			// Add REMINDER_PERIOD months to the calendar
			alarmCalendar.add(Calendar.MONTH, REMINDER_PERIOD);
			// alarmCalendar.add(Calendar.MINUTE, 1); // For debugging

			Log.i(MODULE_TAG, "Alarm set for: " + alarmCalendar.getTime().toString());

			PendingIntent alarmIntent = getUseAlarm(context);

			// Set alarm to go off at the time set by the calendar
			alarmMgr.set(AlarmManager.RTC, alarmCalendar.getTimeInMillis(), alarmIntent);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	/**
	 * Cancels the current 1 week reminder, and schedules a new one for 6 months from now
	 * @param context
	 */
	public static void scheduleOneWeekAlarm(Context context) {

		try {
			AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

			// Cancel all previous alarms for this reminder
			alarmMgr.cancel(getOneWeekAlarm(context));

			// Create a calendar set to the current time
			Calendar alarmCalendar = Calendar.getInstance();
			alarmCalendar.setTimeInMillis(System.currentTimeMillis());

			// Add six months to the calendar
			alarmCalendar.add(Calendar.HOUR, 24 * 7);
			// alarmCalendar.add(Calendar.SECOND, 30); // For debugging

			Log.i(MODULE_TAG, "Alarm set for: " + alarmCalendar.getTime().toString());

			PendingIntent alarmIntent = getOneWeekAlarm(context);

			// Set alarm to go off at the time set by the calendar
			alarmMgr.set(AlarmManager.RTC, alarmCalendar.getTimeInMillis(), alarmIntent);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	/**
	 * Cancels the 1 week alarm.
	 * @param context
	 */
	public static void cancelOneWeekAlarm(Context context) {
		AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		// Cancel all previous alarms for this reminder
		alarmMgr.cancel(getOneWeekAlarm(context));
	}

}
