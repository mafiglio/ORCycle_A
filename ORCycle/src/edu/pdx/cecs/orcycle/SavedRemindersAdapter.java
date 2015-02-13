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

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
			// Inflate ui
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			// Reference widgets
			rowView = inflater.inflate(R.layout.saved_reminders_list_item, parent, false);
			TextView tvName = (TextView) rowView.findViewById(R.id.tv_reminder_name);
			TextView tvTime = (TextView) rowView.findViewById(R.id.tv_reminder_time);
			LinearLayout llSun = (LinearLayout) rowView.findViewById(R.id.tv_srli_sun_underline);
			LinearLayout llMon = (LinearLayout) rowView.findViewById(R.id.tv_srli_mon_underline);
			LinearLayout llTue = (LinearLayout) rowView.findViewById(R.id.tv_srli_tue_underline);
			LinearLayout llWed = (LinearLayout) rowView.findViewById(R.id.tv_srli_wed_underline);
			LinearLayout llThu = (LinearLayout) rowView.findViewById(R.id.tv_srli_thu_underline);
			LinearLayout llFri = (LinearLayout) rowView.findViewById(R.id.tv_srli_fri_underline);
			LinearLayout llSat = (LinearLayout) rowView.findViewById(R.id.tv_srli_sat_underline);

			// Move cursor to itemselected
			cursor.moveToPosition(position);

			// Get column values
			String name = cursor.getString(cursor.getColumnIndex(DbAdapter.K_REMINDER_NAME));
			int days = cursor.getInt(cursor.getColumnIndex(DbAdapter.K_REMINDER_DAYS));
			int hours = cursor.getInt(cursor.getColumnIndex(DbAdapter.K_REMINDER_HOURS));
			int minutes = cursor.getInt(cursor.getColumnIndex(DbAdapter.K_REMINDER_MINUTES));
			boolean enabled = (cursor.getInt(cursor.getColumnIndex(DbAdapter.K_REMINDER_ENABLED)) != 0);

			// Set value of AM/PM
			String ampm;
			if (hours > 12) {
				hours = hours - 12;
				ampm = "PM";
			}
			else {
				ampm = "AM";
			}

			// Set value for enabled / disabled
			if (enabled) {
				tvName.setText(name);
			}
			else {
				tvName.setText(name + " (disabled)");
			}
			tvTime.setText(String.format("%d:%02d %s", ((hours == 0) ? 12 : hours), minutes, ampm));

			// Get helper to help with setting day selections
			ReminderHelper rh = new ReminderHelper();
			rh.setDays(days);

			// Set day selections
			llSun.setVisibility(rh.getSunday()    ? View.VISIBLE : View.INVISIBLE);
			llMon.setVisibility(rh.getMonday()    ? View.VISIBLE : View.INVISIBLE);
			llTue.setVisibility(rh.getTuesday()   ? View.VISIBLE : View.INVISIBLE);
			llWed.setVisibility(rh.getWednesday() ? View.VISIBLE : View.INVISIBLE);
			llThu.setVisibility(rh.getThursday()  ? View.VISIBLE : View.INVISIBLE);
			llFri.setVisibility(rh.getFriday()    ? View.VISIBLE : View.INVISIBLE);
			llSat.setVisibility(rh.getSaturday()  ? View.VISIBLE : View.INVISIBLE);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		return rowView;
	}
}