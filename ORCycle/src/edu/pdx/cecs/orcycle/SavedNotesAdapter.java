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

import java.text.SimpleDateFormat;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class SavedNotesAdapter extends SimpleCursorAdapter {

	private final static String MODULE_TAG = "SavedNotesAdapter";

	private final Context context;
	private final String[] from;
	private final int[] to;
	Cursor cursor;
	private final String[] accidentSeverities;
	private final String[] problemSeverity;

	public SavedNotesAdapter(Context context, int layout, Cursor c,
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
			rowView = inflater.inflate(R.layout.saved_notes_list_item, parent, false);
			TextView tvNoteRecorded = (TextView) rowView.findViewById(R.id.tvSnliRecorded);
			TextView tvNoteSeverity = (TextView) rowView.findViewById(R.id.tvSnliNoteSeverity);
			ImageView ivNoteIcon = (ImageView) rowView.findViewById(R.id.ivNoteSeverity);

			cursor.moveToPosition(position);

			SimpleDateFormat sdfStart = new SimpleDateFormat("MMMM d, y  h:mm a");
			// sdfStart.setTimeZone(TimeZone.getTimeZone("UTC"));
			Double startTime = cursor.getDouble(cursor.getColumnIndex("noterecorded"));
			String start = sdfStart.format(startTime);

			tvNoteRecorded.setText(start);

			int noteSeverity = cursor.getInt(cursor.getColumnIndex(DbAdapter.K_NOTE_SEVERITY));
			int status = cursor.getInt(cursor.getColumnIndex(DbAdapter.K_NOTE_STATUS));

			tvNoteSeverity.setText(getNoteSeverityText(noteSeverity));

			if (status == 1) {
				ivNoteIcon.setImageResource(R.drawable.failedupload_high);
			}
			else {
				int iconResourceId = DbAnswers.getNoteSeverityImageResourceId(noteSeverity);
				if (-1 != iconResourceId) {
					ivNoteIcon.setImageResource(iconResourceId);
				}
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		return rowView;
	}

	private String getNoteSeverityText(int noteSeverity) {

		int index;

		if (-1 != (index = DbAnswers.findIndex(DbAnswers.accidentSeverity, noteSeverity))) {
			return accidentSeverities[index + 1];
		}

		if (-1 != (index = DbAnswers.findIndex(DbAnswers.safetyUrgency, noteSeverity))) {
			return problemSeverity[index + 1];
		}

		return "Unknown";
	}

}