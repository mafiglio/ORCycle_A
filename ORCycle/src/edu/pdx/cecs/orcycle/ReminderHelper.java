package edu.pdx.cecs.orcycle;

public class ReminderHelper {

	private final byte SUNDAY_MASK = 1;
	private final byte MONDAY_MASK = 2;
	private final byte TUESDAY_MASK = 4;
	private final byte WEDNESDAY_MASK = 8;
	private final byte THURSDAY_MASK = 16;
	private final byte FRIDAY_MASK = 32;
	private final byte SATURDAY_MASK = 64;
	private byte days;

	public ReminderHelper() {
		days = 0;
	}

	public ReminderHelper(int days) {
		this.days = (byte) days;
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

	public int days() {
		return this.days;
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

}
