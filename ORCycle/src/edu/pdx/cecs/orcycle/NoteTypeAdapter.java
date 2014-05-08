package edu.pdx.cecs.orcycle;

import edu.pdx.cecs.orcycle.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class NoteTypeAdapter extends ArrayAdapter<String> {
	private final Context context;
	private final String[] values;

	public NoteTypeAdapter(Context context, String[] values) {
		super(context, R.layout.note_type_list_item, values);
		this.context = context;
		this.values = values;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.note_type_list_item, parent,
				false);
		TextView textView = (TextView) rowView
				.findViewById(R.id.TextViewNoteType);
		ImageView imageView = (ImageView) rowView
				.findViewById(R.id.ImageViewNoteType);
		textView.setText(values[position]);
		// Change the icon for Windows and iPhone

		switch (position) {
		case 0:
			imageView.setImageResource(R.drawable.noteissuepicker_high);
			break;
		case 1:
			imageView.setImageResource(R.drawable.noteissuepicker_high);
			break;
		case 2:
			imageView.setImageResource(R.drawable.noteissuepicker_high);
			break;
		case 3:
			imageView.setImageResource(R.drawable.noteissuepicker_high);
			break;
		case 4:
			imageView.setImageResource(R.drawable.noteissuepicker_high);
			break;
		case 5:
			imageView.setImageResource(R.drawable.noteissuepicker_high);
			break;
		case 6:
			imageView.setImageResource(R.drawable.noteassetpicker_high);
			break;
		case 7:
			imageView.setImageResource(R.drawable.noteassetpicker_high);
			break;
		case 8:
			imageView.setImageResource(R.drawable.noteassetpicker_high);
			break;
		case 9:
			imageView.setImageResource(R.drawable.noteassetpicker_high);
			break;
		case 10:
			imageView.setImageResource(R.drawable.noteassetpicker_high);
			break;
		case 11:
			imageView.setImageResource(R.drawable.noteassetpicker_high);
			break;
		}
		return rowView;
	}
}