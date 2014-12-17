package edu.pdx.cecs.orcycle;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class SavedRemindersAdapter extends SimpleCursorAdapter {
	private final static String MODULE_TAG = "SavedNotesAdapter";

	private final Context context;
	private final String[] from;
	private final int[] to;
	Cursor cursor;
	private final String[] accidentSeverities;
	private final String[] problemSeverity;

	public SavedRemindersAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int flags) {
		super(context, R.layout.saved_notes_list_item, c, from, to, flags);
		this.context = context;
		this.from = from;
		this.to = to;
		this.cursor = c;
		this.accidentSeverities = context.getResources().getStringArray(R.array.ara_a_severity_2);
		this.problemSeverity = context.getResources().getStringArray(R.array.arsi_a_urgency_2);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = null;
		try {
			//Log.v(MODULE_TAG, "getView: " + position);

			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			rowView = inflater.inflate(R.layout.saved_reminders_list_item, parent, false);
			TextView tvName = (TextView) rowView.findViewById(R.id.tv_reminder_name);
			TextView tvTime = (TextView) rowView.findViewById(R.id.tv_reminder_time);

			LinearLayout tvSun = (LinearLayout) rowView.findViewById(R.id.tv_srli_sun_underline);
			LinearLayout tvMon = (LinearLayout) rowView.findViewById(R.id.tv_srli_mon_underline);
			LinearLayout tvTue = (LinearLayout) rowView.findViewById(R.id.tv_srli_tue_underline);
			LinearLayout tvWed = (LinearLayout) rowView.findViewById(R.id.tv_srli_wed_underline);
			LinearLayout tvThu = (LinearLayout) rowView.findViewById(R.id.tv_srli_thu_underline);
			LinearLayout tvFri = (LinearLayout) rowView.findViewById(R.id.tv_srli_fri_underline);
			LinearLayout tvSat = (LinearLayout) rowView.findViewById(R.id.tv_srli_sat_underline);

			CheckBox chkEnabled = (CheckBox) rowView.findViewById(R.id.chk_reminder_enabled);

			cursor.moveToPosition(position);

			String name = cursor.getString(cursor.getColumnIndex(DbAdapter.K_REMINDER_NAME));
			int days = cursor.getInt(cursor.getColumnIndex(DbAdapter.K_REMINDER_DAYS));
			int hours = cursor.getInt(cursor.getColumnIndex(DbAdapter.K_REMINDER_HOURS));
			int minutes = cursor.getInt(cursor.getColumnIndex(DbAdapter.K_REMINDER_MINUTES));
			int enabled = cursor.getInt(cursor.getColumnIndex(DbAdapter.K_REMINDER_ENABLED));


			String ampm;
			if (hours > 12) {
				hours = hours - 12;
				ampm = "PM";
			}
			else {
				ampm = "AM";
			}

			tvName.setText(name);
			tvTime.setText(String.format("%d:%02d %s", hours, minutes, ampm));
			chkEnabled.setChecked(enabled != 0);

			ReminderHelper rh = new ReminderHelper(days);

			tvSun.setVisibility(rh.getSunday()    ? View.VISIBLE : View.INVISIBLE);
			tvMon.setVisibility(rh.getMonday()    ? View.VISIBLE : View.INVISIBLE);
			tvTue.setVisibility(rh.getTuesday()   ? View.VISIBLE : View.INVISIBLE);
			tvWed.setVisibility(rh.getWednesday() ? View.VISIBLE : View.INVISIBLE);
			tvThu.setVisibility(rh.getThursday()  ? View.VISIBLE : View.INVISIBLE);
			tvFri.setVisibility(rh.getFriday()    ? View.VISIBLE : View.INVISIBLE);
			tvSat.setVisibility(rh.getSaturday()  ? View.VISIBLE : View.INVISIBLE);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		return rowView;
	}
}