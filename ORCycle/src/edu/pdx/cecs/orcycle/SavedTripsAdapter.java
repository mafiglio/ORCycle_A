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
import java.util.ArrayList;
import java.util.TimeZone;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

@SuppressLint("SimpleDateFormat")
public class SavedTripsAdapter extends SimpleCursorAdapter {

	private final static String MODULE_TAG = "SavedTripsAdapter";

	private final static String[] from = new String[] { DbAdapter.K_TRIP_ROWID };
	private final static SimpleDateFormat sdfStart = new SimpleDateFormat("MMMM d, y  h:mm a");
	private final static SimpleDateFormat sdfDuration = new SimpleDateFormat("HH:mm:ss");

	private static class ViewHolder {
		public TextView tvStartTime = null;
		public TextView tvTripPurpose = null;
		public TextView tvTripDuration = null;
		public ImageView ivIcon = null;
	}

	private final Cursor cursor;
	private final int listItemLayout;
	private final int defaultColor;
	private final int selectedColor;
	private final LayoutInflater layoutInflater;
	private final ArrayList<Long> selectedItems = new ArrayList<Long>();

	public SavedTripsAdapter(Context context, int listItemLayout, Cursor cursor,
			int defaultColor, int selectedColor) {
		super(context, listItemLayout, cursor, from, null, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

		SavedTripsAdapter.sdfDuration.setTimeZone(TimeZone.getTimeZone("UTC"));

		this.cursor = cursor;
		this.listItemLayout = listItemLayout;
		this.defaultColor = defaultColor;
		this.selectedColor = selectedColor;
		this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public ArrayList<Long> getSelectedItems() {
		return selectedItems;
	}

	public long[] getSelectedItemsArray() {

		long[] selectedItemsArray = new long[selectedItems.size()];

		for(int i = 0; i < selectedItems.size(); ++i) {
			selectedItemsArray[i] = selectedItems.get(i);
		}

		return selectedItemsArray;
	}

	public void setSelectedItems(long[] selectedItemsArray) {
		selectedItems.clear();
		for (long tripId: selectedItemsArray) {
			selectedItems.add(tripId);
		}
	}

	public boolean isSelected(long id) {
		return selectedItems.indexOf(id) >= 0;
	}

	public void select(long id, boolean select) {
		if (select) {
			selectedItems.add(id);
		}
		else {
			selectedItems.remove(id);
		}
	}

	public int numSelectedItems() {
		return selectedItems.size();
	}

	public void toggleSelection(long id) {
		if (isSelected(id)) {
			select(id, false);
		}
		else {
			select(id, true);
		}
	}

	public void clearSelectedItems() {
		selectedItems.clear();
	}

	public ArrayList<Long> getSelectedTrips() {

		ArrayList<Long> selectedTripIds = new ArrayList<Long>(selectedItems);

		return selectedTripIds;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		try {
			// move cursor to item to be displayed
			cursor.moveToPosition(position);

			// get list item data
			long tripId = cursor.getLong(cursor.getColumnIndex(DbAdapter.K_TRIP_ROWID));
			Double startTime = cursor.getDouble(cursor.getColumnIndex(DbAdapter.K_TRIP_START));
			String formattedStartTime = sdfStart.format(startTime);
			Double endTime = cursor.getDouble(cursor.getColumnIndex(DbAdapter.K_TRIP_END));
			String formattedDuration = sdfDuration.format(endTime - startTime);
			int uploadStatus = cursor.getInt(cursor.getColumnIndex(DbAdapter.K_TRIP_STATUS));
			String purpose = cursor.getString(cursor.getColumnIndex(DbAdapter.K_TRIP_PURP));
			int imageResource = getImageResource(uploadStatus);

			// Create view holder
			ViewHolder holder = null;
			if (convertView == null) { // then this is the first time this item is being drawn

				// Inflate the standard list item layout
				convertView = layoutInflater.inflate(listItemLayout, null);

				// populate the view data
				holder = new ViewHolder();
				holder.tvStartTime = (TextView) convertView.findViewById(R.id.tv_start_time);
				holder.tvTripPurpose = (TextView) convertView.findViewById(R.id.tv_trip_purpose);
				holder.tvTripDuration = (TextView) convertView.findViewById(R.id.tv_trip_duration);
				holder.ivIcon = (ImageView) convertView.findViewById(R.id.image_trip_purpose);

				// Optimization: Tag the row with it's child views, so we don't have to
				// call findViewById() later when we reuse the row.
				convertView.setTag(holder);
			} else { // this list item's view already exist
				holder = (ViewHolder) convertView.getTag();
			}

			if ((uploadStatus == TripData.STATUS_COMPLETE /* 1 */) || (uploadStatus == TripData.STATUS_SENT /* 2 */)) {
				holder.tvStartTime.setText(formattedStartTime);
				holder.tvTripPurpose.setText(purpose);
				holder.tvTripDuration.setText(formattedDuration);
				holder.ivIcon.setImageResource(imageResource);
			}
			else {
				holder.tvStartTime.setText(formattedStartTime);
				holder.tvTripPurpose.setText("Invalid");
				holder.tvTripDuration.setText("00:00:00");
				holder.ivIcon.setImageResource(imageResource);
			}
			convertView.setBackgroundColor(isSelected(tripId) ? selectedColor : defaultColor);
			return convertView;
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		return convertView;
	}

	private int getImageResource(int status) {

		int imageResource = -1;
		int columnIndex;
		String value;

		if (status == TripData.STATUS_SENT /* 2 */) {
			if (-1 != (columnIndex = cursor.getColumnIndex("purp"))) {
				if (null != (value = cursor.getString(columnIndex))) {
					if (value.equals(DbAnswers.PURPOSE_COMMUTE)) {
						imageResource = R.drawable.commute_high;
					}
					else if (value.equals(DbAnswers.PURPOSE_SCHOOL)) {
						imageResource = R.drawable.school_high;
					}
					else if (value.equals(DbAnswers.PURPOSE_WORK_RELATED)) {
						imageResource = R.drawable.workrel_high;
					}
					else if (value.equals(DbAnswers.PURPOSE_EXERCISE)) {
						imageResource = R.drawable.exercise_high;
					}
					else if (value.equals(DbAnswers.PURPOSE_SOCIAL)) {
						imageResource = R.drawable.social_high;
					}
					else if (value.equals(DbAnswers.PURPOSE_SHOPPING)) {
						imageResource = R.drawable.shopping_high;
					}
					else if (value.equals(DbAnswers.PURPOSE_TRANSIT_OR_VEHICLE)) {
						imageResource = R.drawable.errands_high;
					}
					else if (value.equals(DbAnswers.PURPOSE_OTHER)) {
						imageResource = R.drawable.other_high;
					}
				}
			}
		} else if (status == TripData.STATUS_COMPLETE /* 1 */) {
			imageResource = R.drawable.failedupload_high;
		}
		else {
			Log.e(MODULE_TAG, "No icon for upload status:" + status);
		}
		return imageResource == -1 ? R.drawable.invalidtrip : imageResource;
	}
}