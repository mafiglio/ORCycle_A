package edu.pdx.cecs.orcycle;

import edu.pdx.cecs.orcycle.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TripPurposeAdapter extends ArrayAdapter<String> {
	private final Context context;
	private final String[] values;

	public TripPurposeAdapter(Context context, String[] values) {
		super(context, R.layout.trip_purpose_list_item, values);
		this.context = context;
		this.values = values;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.trip_purpose_list_item,
				parent, false);
		TextView textView = (TextView) rowView
				.findViewById(R.id.TextViewTripPurpose);
		ImageView imageView = (ImageView) rowView
				.findViewById(R.id.ImageViewTripPurpose);
		textView.setText(values[position]);
		// Change the icon for Windows and iPhone

		switch (position) {
		case 0:
			imageView.setImageResource(R.drawable.commute_high);
			break;
		case 1:
			imageView.setImageResource(R.drawable.school_high);
			break;
		case 2:
			imageView.setImageResource(R.drawable.workrel_high);
			break;
		case 3:
			imageView.setImageResource(R.drawable.exercise_high);
			break;
		case 4:
			imageView.setImageResource(R.drawable.social_high);
			break;
		case 5:
			imageView.setImageResource(R.drawable.shopping_high);
			break;
		case 6:
			imageView.setImageResource(R.drawable.errands_high);
			break;
		case 7:
			imageView.setImageResource(R.drawable.other_high);
			break;
		}
		return rowView;
	}
}