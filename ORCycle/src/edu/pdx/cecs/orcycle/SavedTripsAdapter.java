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
import java.util.TimeZone;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class SavedTripsAdapter extends SimpleCursorAdapter {

	private final static String MODULE_TAG = "SavedTripsAdapter";

	private final Context context;
	private final String[] from;
	private final int[] to;
	Cursor cursor;

	public SavedTripsAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int flags) {
		super(context, R.layout.saved_trips_list_item, c, from, to, flags);
		this.context = context;
		this.from = from;
		this.to = to;
		this.cursor = c;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = null;
		try {
			//Log.v(MODULE_TAG, "getView(Position): " + position);

			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			rowView = inflater.inflate(R.layout.saved_trips_list_item, parent, false);
			TextView textViewStart = (TextView) rowView.findViewById(R.id.TextViewStart);
			TextView textViewPurpose = (TextView) rowView.findViewById(R.id.TextViewPurpose);
			TextView textViewInfo = (TextView) rowView.findViewById(R.id.TextViewInfo);
			ImageView imageTripPurpose = (ImageView) rowView.findViewById(R.id.ImageTripPurpose);
			//TextView textViewCO2 = (TextView) rowView.findViewById(R.id.TextViewCO2);
			//TextView textViewCalory = (TextView) rowView.findViewById(R.id.TextViewCalory);
			//View llCaloryCo2 = rowView.findViewById(R.id.RelativeLayout2);

			cursor.moveToPosition(position);

			SimpleDateFormat sdfStart = new SimpleDateFormat("MMMM d, y  h:mm a");
			Double startTime = cursor.getDouble(cursor.getColumnIndex("start"));
			String start = sdfStart.format(startTime);

			textViewStart.setText(start);
			textViewPurpose.setText(cursor.getString(cursor.getColumnIndex("purp")));

			SimpleDateFormat sdfDuration = new SimpleDateFormat("HH:mm:ss");
			sdfDuration.setTimeZone(TimeZone.getTimeZone("UTC"));
			Double endTime = cursor.getDouble(cursor.getColumnIndex("endtime"));
			String duration = sdfDuration.format(endTime - startTime);

			//Log.v(MODULE_TAG, "Duration: " + duration);

			textViewInfo.setText(duration);

			//Double CO2 = cursor.getFloat(cursor.getColumnIndex("distance")) * 0.0006212 * 0.93;
			//DecimalFormat df = new DecimalFormat("0.#");
			//String CO2String = df.format(CO2);
			//textViewCO2.setText("CO2 Saved: " + CO2String + " lbs");

			//Double calory = cursor.getFloat(cursor.getColumnIndex("distance")) * 0.0006212 * 49 - 1.69;
			//String caloryString = df.format(calory);
			//if (calory <= 0) {
			//	textViewCalory.setText("Calories Burned: " + 0 + " kcal");
			//} else {
			//	textViewCalory
			//			.setText("Calories Burned: " + caloryString + " kcal");
			//}

			// ----------------------------------------------------------
			// For the moment, these elements will be invisible until we
			// have a more accurate solution for calories burned
			// ----------------------------------------------------------

			//llCaloryCo2.setVisibility(View.GONE);

			//textViewCO2.setVisibility(View.GONE);
			//textViewCalory.setVisibility(View.GONE);

			// -------------------------------------------------------
			//
			// -------------------------------------------------------

			int status = cursor.getInt(cursor.getColumnIndex("status"));

			//Log.v(MODULE_TAG, "Status: " + status);

			if (status == 0){
				//textViewPurpose.setText("In Progress");
				rowView.setVisibility(View.GONE);
				rowView = inflater.inflate(R.layout.saved_trips_list_item_null, parent, false);
			}
			else {
				rowView.setVisibility(View.VISIBLE);
			}

			int columnIndex;
			String value;

			if (status == 2) {
				if (-1 != (columnIndex = cursor.getColumnIndex("purp"))) {
					if (null != (value = cursor.getString(columnIndex))) {
						if (value.equals(DbAnswers.PURPOSE_COMMUTE)) {
							imageTripPurpose.setImageResource(R.drawable.commute_high);
						}
						else if (value.equals(DbAnswers.PURPOSE_SCHOOL)) {
							imageTripPurpose.setImageResource(R.drawable.school_high);
						}
						else if (value.equals(DbAnswers.PURPOSE_WORK_RELATED)) {
							imageTripPurpose.setImageResource(R.drawable.workrel_high);
						}
						else if (value.equals(DbAnswers.PURPOSE_EXERCISE)) {
							imageTripPurpose.setImageResource(R.drawable.exercise_high);
						}
						else if (value.equals(DbAnswers.PURPOSE_SOCIAL)) {
							imageTripPurpose.setImageResource(R.drawable.social_high);
						}
						else if (value.equals(DbAnswers.PURPOSE_SHOPPING)) {
							imageTripPurpose.setImageResource(R.drawable.shopping_high);
						}
						else if (value.equals(DbAnswers.PURPOSE_TRANSIT_OR_VEHICLE)) {
							imageTripPurpose.setImageResource(R.drawable.errands_high);
						}
						else if (value.equals(DbAnswers.PURPOSE_OTHER)) {
							imageTripPurpose.setImageResource(R.drawable.other_high);
						}
					}
				}
			} else if (status == 1) {
				imageTripPurpose.setImageResource(R.drawable.failedupload_high);
			}
			return rowView;
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		return rowView;
	}
}