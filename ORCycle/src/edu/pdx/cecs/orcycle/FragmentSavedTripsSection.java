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
import android.graphics.Color;
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
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class FragmentSavedTripsSection extends Fragment {

	private static final String MODULE_TAG = "FragmentSavedTripsSection";

	public static final String ARG_SECTION_NUMBER = "section_number";

	ListView listSavedTrips;
	ActionMode mActionMode;
	ArrayList<Long> tripIdArray = new ArrayList<Long>();
	private MenuItem saveMenuItemDelete, saveMenuItemUpload;
	String[] values;

	Long storedID;

	Cursor allTrips;

	public SavedTripsAdapter sta;

	public FragmentSavedTripsSection() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		Log.v(MODULE_TAG, "Cycle: onCreateView()");

		View rootView = null;
		Intent intent;
		Bundle extras;

		try {
			rootView = inflater.inflate(R.layout.activity_saved_trips, null);
			setHasOptionsMenu(true);

			listSavedTrips = (ListView) rootView.findViewById(R.id.listViewSavedTrips);
			populateTripList(listSavedTrips);

			if (null != (intent = getActivity().getIntent())) {
				if (null != (extras = intent.getExtras())) {
					if (!extras.getBoolean(TabsConfig.EXTRA_KEEP_ME, false)) {
						cleanTrips();
					}
				}
			}

			tripIdArray.clear();
		}
		catch (Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}

		return rootView;
	}

	private void cleanTrips() {

		final DbAdapter mDb = new DbAdapter(getActivity());

		mDb.open();
		try {

			// Clean up any bad trips & coords from crashes
			int cleanedTrips = 0;
			// cleanedTrips = mDb.cleanTripsCoordsTables();
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

	private final ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

		// Called when the action mode is created; startActionMode() was called
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

		// Called each time the action mode is shown. Always called after
		// onCreateActionMode, but
		// may be called multiple times if the mode is invalidated.
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			try {
				Log.v(MODULE_TAG, "Prepare");
				saveMenuItemDelete = menu.getItem(0);
				saveMenuItemDelete.setEnabled(false);
				saveMenuItemUpload = menu.getItem(1);

				int flag = 1;
				for (int i = 0; i < listSavedTrips.getCount(); i++) {
					allTrips.moveToPosition(i);
					flag = flag
							* (allTrips.getInt(allTrips.getColumnIndex("status")) - 1);
					if (flag == 0) {
						storedID = allTrips.getLong(allTrips.getColumnIndex("_id"));
						Log.v(MODULE_TAG, "" + storedID);
						break;
					}
				}
				if (flag == 1) {
					saveMenuItemUpload.setEnabled(false);
				} else {
					saveMenuItemUpload.setEnabled(true);
				}

				mode.setTitle(tripIdArray.size() + " Selected");
				return false; // Return false if nothing is done
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
			return false; // Return false if nothing is done
		}

		// Called when the user selects a contextual menu item
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			try {
				switch (item.getItemId()) {
				case R.id.action_delete_saved_trips:
					// delete selected trips
					for (int i = 0; i < tripIdArray.size(); i++) {
						try {
							deleteTrip(tripIdArray.get(i));
						}
						catch(Exception ex) {
							Log.e(MODULE_TAG, ex.getMessage());
						}
					}
					mode.finish(); // Action picked, so close the CAB
					return true;
				case R.id.action_upload_saved_trips:
					// upload selected trips
					// for (int i = 0; i < tripIdArray.size(); i++) {
					// retryTripUpload(tripIdArray.get(i));
					// }
					// Log.v(MODULE_TAG, "" + storedID);
					try {
						retryTripUpload(storedID);
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

		// Called when the user exits the action mode
		@Override
		public void onDestroyActionMode(ActionMode mode) {
			try {
				int numListViewItems = listSavedTrips.getChildCount();
				mActionMode = null;
				tripIdArray.clear();

				// Reset all list items to their normal color
				for (int i = 0; i < numListViewItems; i++) {
					listSavedTrips.getChildAt(i).setBackgroundColor(Color.parseColor("#80ffffff"));
				}
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	};

	void populateTripList(ListView lv) {
		// Get list from the real phone database. W00t!
		final DbAdapter mDb = new DbAdapter(getActivity());
		mDb.open();

		try {
			allTrips = mDb.fetchAllTrips();

			String[] from = new String[] { "purp", "fancystart", "fancyinfo",
					"endtime", "start", "distance", "status" };
			int[] to = new int[] { R.id.TextViewPurpose, R.id.TextViewStart,
					R.id.TextViewInfo };

			sta = new SavedTripsAdapter(getActivity(),
					R.layout.saved_trips_list_item, allTrips, from, to,
					CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

			lv.setAdapter(sta);
		} catch (SQLException sqle) {
			// Do nothing, for now!
		}
		mDb.close();

		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
				try {
					allTrips.moveToPosition(pos);
					if (mActionMode == null) {
						if (allTrips.getInt(allTrips.getColumnIndex("status")) == 2) {
							transitionToTripMapActivity(id);
						} else if (allTrips.getInt(allTrips
								.getColumnIndex("status")) == 1) {
							// Toast.makeText(getActivity(), "Unsent",
							// Toast.LENGTH_SHORT).show();
							buildAlertMessageUnuploadedTripClicked(id);

							// Log.v(MODULE_TAG,
							// ""+allTrips.getLong(allTrips.getColumnIndex("_id")));
						}

					} else {
						// highlight
						if (tripIdArray.indexOf(id) > -1) {
							tripIdArray.remove(id);
							v.setBackgroundColor(Color.parseColor("#80ffffff"));
						} else {
							tripIdArray.add(id);
							v.setBackgroundColor(Color.parseColor("#ff33b5e5"));
						}
						// Toast.makeText(getActivity(), "Selected: " + tripIdArray,
						// Toast.LENGTH_SHORT).show();
						if (tripIdArray.size() == 0) {
							saveMenuItemDelete.setEnabled(false);
						} else {
							saveMenuItemDelete.setEnabled(true);
						}

						mActionMode.setTitle(tripIdArray.size() + " Selected");
					}
				}
				catch(Exception ex) {
					Log.e(MODULE_TAG, ex.getMessage());
				}
			}
		});

		registerForContextMenu(lv);
	}

	private void buildAlertMessageUnuploadedTripClicked(final long position) {
		try {
			final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle("Upload Trip");
			builder.setMessage("Do you want to upload this trip?");
			builder.setNegativeButton("Upload",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							try {
								dialog.cancel();
								retryTripUpload(position);
								// Toast.makeText(getActivity(),"Send Clicked: "+position,
								// Toast.LENGTH_SHORT).show();
							}
							catch(Exception ex) {
								Log.e(MODULE_TAG, ex.getMessage());
							}
						}
					});

			builder.setPositiveButton("Cancel",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							try {
								dialog.cancel();
								// continue
							}
							catch(Exception ex) {
								Log.e(MODULE_TAG, ex.getMessage());
							}
						}
					});
			final AlertDialog alert = builder.create();
			alert.show();
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	private void retryTripUpload(long tripId) {
		TripUploader uploader = new TripUploader(getActivity(), MyApplication.getInstance().getUserId());
		FragmentSavedTripsSection f2 = (FragmentSavedTripsSection) getActivity()
				.getSupportFragmentManager().findFragmentByTag(
						"android:switcher:" + R.id.pager + ":1");
		uploader.setSavedTripsAdapter(sta);
		uploader.setFragmentSavedTripsSection(f2);
		uploader.setListView(listSavedTrips);
		uploader.execute();
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
		listSavedTrips.invalidate();
		populateTripList(listSavedTrips);
	}

	// show edit button and hidden delete button
	@Override
	public void onResume() {
		super.onResume();
		try {
			Log.v(MODULE_TAG, "Cycle: SavedTrips onResume");
			populateTripList(listSavedTrips);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		try {
			Log.v(MODULE_TAG, "Cycle: SavedTrips onPause");
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		try {
			Log.v(MODULE_TAG, "Cycle: SavedTrips onDestroyView");
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	/* Creates the menu items */
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

	/* Handles item selections */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		try {
			// Handle presses on the action bar items
			switch (item.getItemId()) {
			case R.id.action_edit_saved_trips:
				// edit
				if (mActionMode != null) {
					return false;
				}

				// Start the CAB using the ActionMode.Callback defined above
				mActionMode = getActivity().startActionMode(mActionModeCallback);
				return true;
			default:
				return super.onOptionsItemSelected(item);
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		return false;
	}

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
