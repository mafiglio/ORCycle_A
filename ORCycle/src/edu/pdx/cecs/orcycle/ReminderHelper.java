package edu.pdx.cecs.orcycle;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class ReminderHelper {

	private static final String MODULE_TAG = "ReminderHelper";

	private static final byte SUNDAY_MASK = 1;
	private static final byte MONDAY_MASK = 2;
	private static final byte TUESDAY_MASK = 4;
	private static final byte WEDNESDAY_MASK = 8;
	private static final byte THURSDAY_MASK = 16;
	private static final byte FRIDAY_MASK = 32;
	private static final byte SATURDAY_MASK = 64;

	private long id;
	private String name;
	private byte days;
	private int hours;
	private int minutes;
	private boolean enabled;
	private Context context;

	public ReminderHelper() {
		init(null);
	}

	public ReminderHelper(Context context) {
		init(context);
	}

	public ReminderHelper(ReminderHelper rh) {
		this.id = rh.id;
		this.name = rh.name;
		this.days = rh.days;
		this.hours = rh.hours;
		this.minutes = rh.minutes;
		this.enabled = rh.enabled;
		this.context = rh.context;
	}

	public ReminderHelper(Context context, long reminderId) {
		init(context);
		final DbAdapter mDb = new DbAdapter(context);
		try {
			mDb.open();
			Cursor reminder;
			if (null != (reminder = mDb.fetchReminder(reminderId))) {
				this.name = reminder.getString(reminder.getColumnIndex(DbAdapter.K_REMINDER_NAME ));
				this.days = (byte) reminder.getInt(reminder.getColumnIndex(DbAdapter.K_REMINDER_DAYS));
				this.hours = reminder.getInt(reminder.getColumnIndex(DbAdapter.K_REMINDER_HOURS));
				this.minutes = reminder.getInt(reminder.getColumnIndex(DbAdapter.K_REMINDER_MINUTES));
				this.enabled = (reminder.getInt(reminder.getColumnIndex(DbAdapter.K_REMINDER_ENABLED)) != 0);
			}
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

	private void init(Context context) {
		this.context = context;
		id = -1;
		name = "";
		days = 0;
		enabled = false;
		hours = 0;
		minutes = 0;
	}

	public ReminderHelper setId(long id) {
		this.id = id;
		return this;
	}

	public long getId() {
		return id;
	}

	public ReminderHelper setName(String name) {
		this.name = name;
		return this;
	}

	public String getName() {
		return name;
	}

	public ReminderHelper setSunday(boolean enabled) {

		if (enabled) {
			days |= SUNDAY_MASK;
		}
		else {
			days &= ~SUNDAY_MASK;
		}
		return this;
	}

	public ReminderHelper setMonday(boolean enabled) {

		if (enabled) {
			days |= MONDAY_MASK;
		}
		else {
			days &= ~MONDAY_MASK;
		}
		return this;
	}

	public ReminderHelper setTuesday(boolean enabled) {

		if (enabled) {
			days |= TUESDAY_MASK;
		}
		else {
			days &= ~TUESDAY_MASK;
		}
		return this;
	}

	public ReminderHelper setWednesday(boolean enabled) {

		if (enabled) {
			days |= WEDNESDAY_MASK;
		}
		else {
			days &= ~WEDNESDAY_MASK;
		}
		return this;
	}

	public ReminderHelper setThursday(boolean enabled) {

		if (enabled) {
			days |= THURSDAY_MASK;
		}
		else {
			days &= ~THURSDAY_MASK;
		}
		return this;
	}

	public ReminderHelper setFriday(boolean enabled) {

		if (enabled) {
			days |= FRIDAY_MASK;
		}
		else {
			days &= ~FRIDAY_MASK;
		}
		return this;
	}

	public ReminderHelper setSaturday(boolean enabled) {

		if (enabled) {
			days |= SATURDAY_MASK;
		}
		else {
			days &= ~SATURDAY_MASK;
		}
		return this;
	}

	public int getDays() {
		return this.days;
	}

	public void setDays(int days) {
		this.days = (byte) days;
	}

	public boolean getSunday() {
		return (days & SUNDAY_MASK) != 0;
	}

	public boolean getMonday() {
		return (days & MONDAY_MASK) != 0;
	}

	public boolean getTuesday() {
		return (days & TUESDAY_MASK) != 0;
	}

	public boolean getWednesday() {
		return (days & WEDNESDAY_MASK) != 0;
	}

	public boolean getThursday() {
		return (days & THURSDAY_MASK) != 0;
	}

	public boolean getFriday() {
		return (days & FRIDAY_MASK) != 0;
	}

	public boolean getSaturday() {
		return (days & SATURDAY_MASK) != 0;
	}

	public ReminderHelper setHours(int hours) {
		this.hours = hours;
		return this;
	}

	public int getHours() {
		return hours;
	}

	public ReminderHelper setMinutes(int minutes) {
		this.minutes = minutes;
		return this;
	}

	public int getMinutes() {
		return minutes;
	}

	public ReminderHelper setEnabled(boolean enabled) {
		this.enabled = enabled;
		return this;
	}

	public ReminderHelper setEnabled(int enabled) {
		this.enabled = (enabled != 0);
		return this;
	}

	public boolean getEnabled() {
		return enabled;
	}

	public void update() {

		final DbAdapter mDb = new DbAdapter(context);

		try {
			mDb.open();
			if (id > 0) {
				mDb.updateReminder(id, name, days, hours, minutes, enabled);
			}
			else {
				id = mDb.createReminder(name, days, hours, minutes, enabled);
			}
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
