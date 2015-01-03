package edu.pdx.cecs.orcycle;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class Reminder {

	private static final String MODULE_TAG = "Reminder";

	public static final String EXTRA_REMINDER_NAME = "REMINDER_NAME";

    /**
     * Value specifying one week in milliseconds
     * milliseconds/sec * sec/min * min/hr * hr/day * days/week.
     */
	private static final long WEEKLY_INTERVAL = 1000 * 60 * 60 * 24 * 7;

	/**
     * Value of the {@code DAY_OF_WEEK} field indicating Sunday.
     */
	private static final int SUNDAY_ID = 0;

    /**
     * Value of the {@code DAY_OF_WEEK} field indicating Monday.
     */
	private static final int MONDAY_ID = 1;

    /**
     * Value of the {@code DAY_OF_WEEK} field indicating Tuesday.
     */
	private static final int TUESDAY_ID = 2;

    /**
     * Value of the {@code DAY_OF_WEEK} field indicating Wednesday.
     */
	private static final int WEDNESDAY_ID = 3;

    /**
     * Value of the {@code DAY_OF_WEEK} field indicating Thursday.
     */
	private static final int THURSDAY_ID = 4;

    /**
     * Value of the {@code DAY_OF_WEEK} field indicating Friday.
     */
	private static final int FRIDAY_ID = 5;

    /**
     * Value of the {@code DAY_OF_WEEK} field indicating Saturday.
     */
	private static final int SATURDAY_ID = 6;

	private final  ReminderHelper rh;

	private final Context context;

	public Reminder(Context context, ReminderHelper rh) {
		this.context = context;
		this.rh = new ReminderHelper(rh);
	}

	public void schedule() {

		AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

		// Cancel all previous alarms for this reminder
		cancel(context, alarmMgr, rh.getId());

		if (rh.getSunday())    schedule(context, rh, alarmMgr, Calendar.SUNDAY    );
		if (rh.getMonday())    schedule(context, rh, alarmMgr, Calendar.MONDAY    );
		if (rh.getTuesday())   schedule(context, rh, alarmMgr, Calendar.TUESDAY   );
		if (rh.getWednesday()) schedule(context, rh, alarmMgr, Calendar.WEDNESDAY );
		if (rh.getThursday())  schedule(context, rh, alarmMgr, Calendar.THURSDAY  );
		if (rh.getFriday())    schedule(context, rh, alarmMgr, Calendar.FRIDAY    );
		if (rh.getSaturday())  schedule(context, rh, alarmMgr, Calendar.SATURDAY  );
	}

	public void cancel() {
		AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		cancel(context, alarmMgr, rh.getId());
	}

	private static void schedule(Context context, ReminderHelper rh, AlarmManager alarmMgr, int dayOfWeek) {

		try {
			long now = System.currentTimeMillis();

			// Get a calendar set to now to later use as a time reference
			Calendar nowCalendar = Calendar.getInstance();
			nowCalendar.setTimeInMillis(now);

			Calendar alarmCalendar = Calendar.getInstance();
			alarmCalendar.setTimeInMillis(now);

			alarmCalendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
			alarmCalendar.set(Calendar.HOUR_OF_DAY, rh.getHours());
			alarmCalendar.set(Calendar.MINUTE, rh.getMinutes());
			alarmCalendar.set(Calendar.SECOND, 0);

			// When setting the alarm's day of the week value, the calendar may set a day
			// prior to today. Inorder to stop alarm from firing immediately in this
			// case, we set the day to fire a week from today
			if (alarmCalendar.before(nowCalendar)) {
				alarmCalendar.add(Calendar.DAY_OF_WEEK, 7);			// Log.i(MODULE_TAG, alarmCalendar.getTime().toString());
			}

			Log.i(MODULE_TAG, "Alarm set for: " + alarmCalendar.getTime().toString());

			PendingIntent alarmIntent = getAlarmIntent(context, dayOfWeek, rh.getId(), rh.getName());

			// Set Repeating interval
			alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, alarmCalendar.getTimeInMillis(),
					WEEKLY_INTERVAL, alarmIntent);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	public static void cancel(Context context, long reminderId) {
		AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		cancel(context, alarmMgr, reminderId);
	}

	private static void cancel(Context context, AlarmManager alarmMgr, long reminderId) {
		cancel(context, alarmMgr, Calendar.SUNDAY, reminderId);
		cancel(context, alarmMgr, Calendar.MONDAY, reminderId);
		cancel(context, alarmMgr, Calendar.TUESDAY, reminderId);
		cancel(context, alarmMgr, Calendar.WEDNESDAY, reminderId);
		cancel(context, alarmMgr, Calendar.THURSDAY, reminderId);
		cancel(context, alarmMgr, Calendar.FRIDAY, reminderId);
		cancel(context, alarmMgr, Calendar.SATURDAY, reminderId);
	}

	private static void cancel(Context context, AlarmManager alarmMgr, int dayOfWeek, long reminderId) {
		try {
			PendingIntent alarmIntent = getAlarmIntent(context, dayOfWeek, reminderId);
			alarmMgr.cancel(alarmIntent);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	private static int getAlarmId(int dayOfWeek, long reminderId) throws Exception {

		int alarmId = 7 * (int) reminderId;

		if (alarmId <= 0) {
			throw new Exception("Invalid reminder ID encountered: " + dayOfWeek);
		}

		switch(dayOfWeek) {
		case Calendar.SUNDAY:    alarmId += SUNDAY_ID; break;
		case Calendar.MONDAY:    alarmId += MONDAY_ID; break;
		case Calendar.TUESDAY:   alarmId += TUESDAY_ID; break;
		case Calendar.WEDNESDAY: alarmId += WEDNESDAY_ID; break;
		case Calendar.THURSDAY:  alarmId += THURSDAY_ID; break;
		case Calendar.FRIDAY:    alarmId += FRIDAY_ID; break;
		case Calendar.SATURDAY:  alarmId += SATURDAY_ID; break;
		default: throw new Exception("Invalid dayOfWeek encountered: " + dayOfWeek);
		}

		return alarmId;
	}

	private static PendingIntent getAlarmIntent(Context context, int dayOfWeek, long reminderId) throws Exception {
		return getAlarmIntent(context, dayOfWeek, reminderId, null);
	}

	private static PendingIntent getAlarmIntent(Context context, int dayOfWeek, long reminderId, String name) throws Exception {

		PendingIntent alarmIntent;

		String ALARM_ACTION = ReminderReceiver.ACTION_REMIND_USER;

		Intent intent = new Intent(ALARM_ACTION);
		if (null != name) {
			intent.putExtra(EXTRA_REMINDER_NAME, name);
		}

		// Construct the pending intent for broadcasting the alarm
		alarmIntent = PendingIntent.getBroadcast(context, getAlarmId(dayOfWeek, reminderId), intent, PendingIntent.FLAG_CANCEL_CURRENT);

		return alarmIntent;
	}
}
