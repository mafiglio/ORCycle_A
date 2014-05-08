package edu.pdx.cecs.orcycle;

import java.text.SimpleDateFormat;

import edu.pdx.cecs.orcycle.R;
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
	private final Context context;
	private final String[] from;
	private final int[] to;
	Cursor cursor;

	public SavedNotesAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int flags) {
		super(context, R.layout.saved_notes_list_item, c, from, to, flags);
		this.context = context;
		this.from = from;
		this.to = to;
		this.cursor = c;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.saved_notes_list_item, parent,
				false);
		TextView textViewStart = (TextView) rowView
				.findViewById(R.id.TextViewStart);
		TextView textViewType = (TextView) rowView
				.findViewById(R.id.TextViewType);
		ImageView imageNoteType = (ImageView) rowView
				.findViewById(R.id.ImageNoteType);

		cursor.moveToPosition(position);

		SimpleDateFormat sdfStart = new SimpleDateFormat("MMMM d, y  HH:mm");
		// sdfStart.setTimeZone(TimeZone.getTimeZone("UTC"));
		Double startTime = cursor.getDouble(cursor
				.getColumnIndex("noterecorded"));
		String start = sdfStart.format(startTime);

		textViewStart.setText(start);

		String[] noteTypeText = new String[] { "Pavement issue",
				"Traffic signal", "Enforcement", "Bike parking",
				"Bike lane issue", "Note this issue", "Bike parking",
				"Bike shops", "Public restrooms", "Secret passage",
				"Water fountains", "Note this asset" };

		textViewType.setText(noteTypeText[cursor.getInt(cursor
				.getColumnIndex("notetype"))]);

		int status = cursor.getInt(cursor.getColumnIndex("notestatus"));
		Log.v("Jason", "Status: " + status);

		if (status == 2) {
			switch (cursor.getInt(cursor.getColumnIndex("notetype"))) {
			case 0:
				imageNoteType.setImageResource(R.drawable.noteissuepicker_high);
				break;
			case 1:
				imageNoteType.setImageResource(R.drawable.noteissuepicker_high);
				break;
			case 2:
				imageNoteType.setImageResource(R.drawable.noteissuepicker_high);
				break;
			case 3:
				imageNoteType.setImageResource(R.drawable.noteissuepicker_high);
				break;
			case 4:
				imageNoteType.setImageResource(R.drawable.noteissuepicker_high);
				break;
			case 5:
				imageNoteType.setImageResource(R.drawable.noteissuepicker_high);
				break;
			case 6:
				imageNoteType.setImageResource(R.drawable.noteassetpicker_high);
				break;
			case 7:
				imageNoteType.setImageResource(R.drawable.noteassetpicker_high);
				break;
			case 8:
				imageNoteType.setImageResource(R.drawable.noteassetpicker_high);
				break;
			case 9:
				imageNoteType.setImageResource(R.drawable.noteassetpicker_high);
				break;
			case 10:
				imageNoteType.setImageResource(R.drawable.noteassetpicker_high);
				break;
			case 11:
				imageNoteType.setImageResource(R.drawable.noteassetpicker_high);
				break;
			}
		} else if (status == 1) {
			imageNoteType.setImageResource(R.drawable.failedupload_high);
		}
		return rowView;
	}
}