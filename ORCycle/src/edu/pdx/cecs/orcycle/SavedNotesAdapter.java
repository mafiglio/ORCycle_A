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
	private final String[] textNoteTypes;

	public SavedNotesAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int flags) {
		super(context, R.layout.saved_notes_list_item, c, from, to, flags);
		this.context = context;
		this.from = from;
		this.to = to;
		this.cursor = c;
		this.textNoteTypes = context.getResources().getStringArray(R.array.nqaIssueType);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = null;
		try {
			Log.v(MODULE_TAG, "getView: " + position);


			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			rowView = inflater.inflate(R.layout.saved_notes_list_item, parent,
					false);
			TextView textViewStart = (TextView) rowView.findViewById(R.id.TextViewStart);
			TextView textViewType = (TextView) rowView.findViewById(R.id.TextViewType);
			ImageView ivNoteIcon = (ImageView) rowView.findViewById(R.id.ImageNoteType);

			cursor.moveToPosition(position);

			SimpleDateFormat sdfStart = new SimpleDateFormat("MMMM d, y  h:mm a");
			// sdfStart.setTimeZone(TimeZone.getTimeZone("UTC"));
			Double startTime = cursor.getDouble(cursor
					.getColumnIndex("noterecorded"));
			String start = sdfStart.format(startTime);

			textViewStart.setText(start);

			int noteType = cursor.getInt(cursor.getColumnIndex(DbAdapter.K_NOTE_TYPE));
			int status = cursor.getInt(cursor.getColumnIndex(DbAdapter.K_NOTE_STATUS));

			textViewType.setText(getNoteTypeText(noteType));

			if (status == 1) {
				ivNoteIcon.setImageResource(R.drawable.failedupload_high);
			}
			else {
				ivNoteIcon.setImageResource(DbAnswers.getNoteTypeImageResourceId(noteType));
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		return rowView;
	}

	private String getNoteTypeText(int noteType) {

		int noteTypeIndex = DbAnswers.findIndex(DbAnswers.noteIssue, noteType);

		if (-1 != noteTypeIndex) {
			return textNoteTypes[noteTypeIndex + 1];
		}
		return "Unknown";
	}

}