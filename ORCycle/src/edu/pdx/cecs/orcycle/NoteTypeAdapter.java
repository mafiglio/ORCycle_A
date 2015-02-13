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