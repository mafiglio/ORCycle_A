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

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

public class FragmentSavedTripsSection extends Fragment {

	private static final String MODULE_TAG = "FragmentSavedTripsSection";

	public static final String ARG_SECTION_NUMBER = "section_number";

	private SavedTripsAdapter savedTripsAdapter;
	private ListView lvSavedTrips;
	private MenuItem menuDelete;
	private MenuItem menuUpload;

	private ActionMode editMode;
	private final ActionMode.Callback editModeCallback = new EditModeCallback();

	Long tripIdToUpload;

	Cursor cursorTrips;

	// *********************************************************************************
	// *                          Fragment Life Cycle
	// *********************************************************************************

	public FragmentSavedTripsSection() {
	}

	/**
	 * Called once the fragment has been created in order for it
	 * to create it's user interface.
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		Log.v(MODULE_TAG, "Cycle: onCreateView()");

		View rootView = null;
		Intent intent;
		Bundle extras;

		try {
			if (null != (rootView = inflater.inflate(R.layout.activity_saved_trips, null))) {

				lvSavedTrips = (ListView) rootView.findViewById(R.id.listViewSavedTrips);
				lvSavedTrips.setOnItemClickListener(new SavedTrips_OnItemClickListener());

				setHasOptionsMenu(true);

				if (null != (intent = getActivity().getIntent())) {
					if (null != (extras = intent.getExtras())) {
						if (!extras.getBoolean(TabsConfig.EXTRA_KEEP_ME, false)) {
							cleanTrips();
						}
					}
				}
				//tripIdArray.clear();
			}
		}
		catch (Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}

		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		try {
			Log.v(MODULE_TAG, "Cycle: SavedTrips onResume");
			populateTripList(lvSavedTrips);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	/**
	 * Creates menu items
	 */
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		try {
			// Inflate the menu items for use in the action bar
			inflater.inflate(R.menu.saved_trips, menu);
			super.onCreateOptionsMenu(menu, inflater);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	/**
	 * Handles menu item selections
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		try {
			// Handle presses on the action bar items
			switch (item.getItemId()) {

			case R.id.action_edit_saved_trips:
				return startActionModeEdit();

			default:
				return super.onOptionsItemSelected(item);
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		return false;
	}

	// *********************************************************************************
	// *                             Fragment Actions
	// *********************************************************************************

	void populateTripList(ListView lv) {
		// Get list from the real phone database. W00t!
		final DbAdapter mDb = new DbAdapter(getActivity());
		mDb.open();

		try {
			cursorTrips = mDb.fetchAllTrips();

			savedTripsAdapter = new SavedTripsAdapter(getActivity(),
					R.layout.saved_trips_list_item, cursorTrips,
					getResources().getColor(R.color.default_color),
					getResources().getColor(R.color.pressed_color));

			lv.setAdapter(savedTripsAdapter);
			lv.invalidate();
		} catch (SQLException sqle) {
			// Do nothing, for now!
		}
		finally {
			mDb.close();
		}
		// registerForContextMenu(lvSavedTrips); TODO: Validate this isn't needed
	}

	private void cleanTrips() {

		final DbAdapter mDb = new DbAdapter(getActivity());

		mDb.open();
		try {
			// Clean up any bad trips & coords from crashes
			int cleanedTrips = 0;
			// cleanedTrips = mDb.cleanTripsCoordsTables(); TODO: Why was this taken out?
			if (cleanedTrips > 0) {
				Toast.makeText(getActivity(), "" + cleanedTrips + " bad trip(s) removed.",
						Toast.LENGTH_SHORT).show();
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		finally {
			mDb.close();
		}
	}

	private void retryTripUpload(long tripId) {
		TripUploader uploader = new TripUploader(getActivity(), MyApplication.getInstance().getUserId());
		FragmentSavedTripsSection f2 = (FragmentSavedTripsSection) getActivity()
				.getSupportFragmentManager().findFragmentByTag(
						"android:switcher:" + R.id.pager + ":1");
		uploader.setSavedTripsAdapter(savedTripsAdapter);
		uploader.setFragmentSavedTripsSection(f2);
		uploader.setListView(lvSavedTrips);
		uploader.execute();
	}

	private void actionDeleteSelectedTrips(ArrayList<Long> tripIds) {
		try {
			// delete selected trips
			for (long tripId: tripIds) {
				try {
					deleteTrip(tripId);
				}
				catch(Exception ex) {
					Log.e(MODULE_TAG, ex.getMessage());
				}
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	private void deleteTrip(long tripId) {
		DbAdapter mDbHelper = new DbAdapter(getActivity());
		mDbHelper.open();
		try {
			mDbHelper.deleteAllCoordsForTrip(tripId);
			mDbHelper.deletePauses(tripId);
			mDbHelper.deleteAnswers(tripId);
			mDbHelper.deleteTrip(tripId);
		}
		finally {
			mDbHelper.close();
		}
		lvSavedTrips.invalidate();
		populateTripList(lvSavedTrips);
	}

	private void clearSelections() {

		int numListViewItems = lvSavedTrips.getChildCount();

		savedTripsAdapter.clearSelectedItems();

		// Reset all list items to their normal color
		for (int i = 0; i < numListViewItems; i++) {
			lvSavedTrips.getChildAt(i).setBackgroundColor(getResources().getColor(R.color.default_color));
		}
	}

	// *********************************************************************************
	// *                           Item Click Listener
	// *********************************************************************************

	private final class SavedTrips_OnItemClickListener implements
			AdapterView.OnItemClickListener {
		public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
			int uploadStatus;

			try {
				cursorTrips.moveToPosition(pos);
				if (editMode == null) {
					uploadStatus = cursorTrips.getInt(cursorTrips.getColumnIndex("status"));
					if (uploadStatus == TripData.STATUS_SENT /* 2 */) {
						transitionToTripMapActivity(id);
					}
					else if (uploadStatus == TripData.STATUS_COMPLETE /* 1 */) {
						dialogTripNotUploaded(id);
					}
				}
				else {

					savedTripsAdapter.toggleSelection(id);
					if (savedTripsAdapter.isSelected(id)) {
						v.setBackgroundColor(getResources().getColor(R.color.pressed_color));
					} else {
						v.setBackgroundColor(getResources().getColor(R.color.default_color));
					}

					menuDelete.setEnabled(savedTripsAdapter.numSelectedItems() > 0);
					editMode.setTitle(savedTripsAdapter.numSelectedItems() + " Selected");
				}
			} catch (Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	// *********************************************************************************
	// *                              Edit Action Mode
	// *********************************************************************************

	/**
	 * Starts the edit action mode.
	 * @return true if new action mode was started, false otherwise.
	 */
	private boolean startActionModeEdit() {
		if (editMode != null) {
			return false;
		}
		// Start the CAB using the ActionMode.Callback defined above
		editMode = getActivity().startActionMode(editModeCallback);
		return true;
	}

	private final class EditModeCallback implements ActionMode.Callback {

		/**
		 * Called when the action mode is created; startActionMode() was called
		 */
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			try {
				// Inflate a menu resource providing context menu items
				MenuInflater inflater = mode.getMenuInflater();
				inflater.inflate(R.menu.saved_trips_context_menu, menu);
				return true;
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
			return false;
		}

		/**
		 * Called each time the action mode is shown. Always
		 * called after onCreateActionMode, but may be called
		 * multiple times if the mode is invalidated.
		 */
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			try {
				int numSelectedItems = savedTripsAdapter.getSelectedItems().size();

				menuDelete = menu.findItem(R.id.action_delete_saved_trips);
				menuDelete.setEnabled(numSelectedItems > 0);
				menuUpload = menu.findItem(R.id.action_upload_saved_trips);

				// Search the saved trips, and look for the first completed trip
				int flag = 1;
				for (int i = 0; i < lvSavedTrips.getCount(); i++) {
					cursorTrips.moveToPosition(i);

					// Calculate flag value
					flag = flag * (cursorTrips.getInt(cursorTrips.getColumnIndex(DbAdapter.K_TRIP_STATUS)) - 1);

					// if the status was 0 then  flag = flag * -1      (STATUS_INCOMPLETE)
					// if the status was 1 then  flag = flag * 0 = 0   (STATUS_COMPLETE)
					// if the status was 2 then  flag = flag * 1       (STATUS_SENT)
					// Note: Once the flag becomes 0 it will stay 0

					if (flag == 0) { // then a completed entry was found
						tripIdToUpload = cursorTrips.getLong(cursorTrips.getColumnIndex(DbAdapter.K_TRIP_ROWID));
						Log.v(MODULE_TAG, "Next trip ID to upload" + tripIdToUpload);
						break;
					}
				}

				// Enable the upload menu item if any trips to be uploaded
				menuUpload.setEnabled(flag == 0);

				// Set title bar number of selections
				mode.setTitle(numSelectedItems + " Selected");
				return true;
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
			return false; // Return false if nothing is done
		}

		/**
		 *  Called when the user selects a contextual menu item
		 */
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			try {
				switch (item.getItemId()) {

				case R.id.action_delete_saved_trips:
					// delete selected trips
					actionDeleteSelectedTrips(savedTripsAdapter.getSelectedItems());
					mode.finish(); // Action picked, so close the CAB
					return true;

				case R.id.action_upload_saved_trips:
					try {
						retryTripUpload(tripIdToUpload);
					}
					catch(Exception ex) {
						Log.e(MODULE_TAG, ex.getMessage());
					}
					mode.finish(); // Action picked, so close the CAB
					return true;

				default:
					return false;
				}
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
			return false;
		}

		/**
		 * Called when the user exits the action mode
		 */
		@Override
		public void onDestroyActionMode(ActionMode mode) {
			try {
				editMode = null;
				clearSelections();
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	// *********************************************************************************
	// *                            Dialog Trip not Uploaded
	// *********************************************************************************

	private void dialogTripNotUploaded(final long tripId) {
		try {
			final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle("Upload Trip");
			builder.setMessage("Do you want to upload this trip?");
			builder.setNegativeButton("Upload",
					new dialogTripNotUploaded_UploadButton(tripId));

			builder.setPositiveButton("Cancel",
					new dialogTripNotUploaded_CancelButton());
			final AlertDialog alert = builder.create();
			alert.show();
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	private final class dialogTripNotUploaded_CancelButton implements
			DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int id) {
			try {
				dialog.cancel();
				// continue
			} catch (Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	private final class dialogTripNotUploaded_UploadButton implements
			DialogInterface.OnClickListener {
		private final long tripId;

		private dialogTripNotUploaded_UploadButton(long tripId) {
			this.tripId = tripId;
		}

		public void onClick(DialogInterface dialog, int id) {
			try {
				dialog.cancel();
				retryTripUpload(tripId);
			} catch (Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	// *********************************************************************************
	// *                                       Transitions
	// *********************************************************************************

	private void transitionToTripMapActivity(long tripId) {
		Intent intent = new Intent(getActivity(), TripMapActivity.class);
		intent.putExtra(TripMapActivity.EXTRA_TRIP_ID, tripId);
		intent.putExtra(TripMapActivity.EXTRA_IS_NEW_TRIP, false);
		intent.putExtra(TripMapActivity.EXTRA_TRIP_SOURCE, TripMapActivity.EXTRA_TRIP_SOURCE_SAVED_TRIPS);
		startActivity(intent);
		getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		getActivity().finish();
	}
}
