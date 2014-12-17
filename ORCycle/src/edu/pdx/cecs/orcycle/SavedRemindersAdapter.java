package edu.pdx.cecs.orcycle;

import java.text.SimpleDateFormat;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
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
			TextView tvDays = (TextView) rowView.findViewById(R.id.tv_reminder_days);
			TextView tvTime = (TextView) rowView.findViewById(R.id.tv_reminder_time);
			CheckBox chkEnabled = (CheckBox) rowView.findViewById(R.id.chk_reminder_enabled);

			cursor.moveToPosition(position);

			String name = cursor.getString(cursor.getColumnIndex(DbAdapter.K_REMINDER_NAME));
			int days = cursor.getInt(cursor.getColumnIndex(DbAdapter.K_REMINDER_DAYS));
			double time = cursor.getDouble(cursor.getColumnIndex(DbAdapter.K_REMINDER_TIME));
			int enabled = cursor.getInt(cursor.getColumnIndex(DbAdapter.K_REMINDER_ENABLED));

			tvName.setText(name);
			tvDays.setText(Integer.valueOf(days));
			SimpleDateFormat sdfStart = new SimpleDateFormat("MMMM d, y  h:mm a");
			tvTime.setText(sdfStart.format(time));
			chkEnabled.setEnabled(enabled != 0);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		return rowView;
	}
}