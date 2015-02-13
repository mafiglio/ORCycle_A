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

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;

public class SavedRemindersActivity extends Activity {

	private final class ReminderClickListener implements
			AdapterView.OnItemClickListener {
		public void onItemClick(AdapterView<?> parent, View v, int pos, long reminderId) {
			Log.v(MODULE_TAG, "onItemClick (id = " + String.valueOf(reminderId) + ", pos = " + String.valueOf(pos) + ")");
			try {
				if (mActionModeNote == null) {
					transitionToEditReminderActivity(reminderId);
				} else {
					// highlight
					if (remindersToDelete.indexOf(reminderId) > -1) {
						remindersToDelete.remove(reminderId);
						v.setBackgroundColor(Color.parseColor("#80ffffff"));
					} else {
						remindersToDelete.add(reminderId);
						v.setBackgroundColor(Color.parseColor("#ff33b5e5"));
					}
					// Toast.makeText(this, "Selected: " + noteIdArray,
					// Toast.LENGTH_SHORT).show();
					if (remindersToDelete.size() == 0) {
						mnuDelete.setEnabled(false);
					} else {
						mnuDelete.setEnabled(true);
					}

					mActionModeNote.setTitle(remindersToDelete.size() + " Selected");
				}
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	private static final String MODULE_TAG = "RemindersActivity";

	ListView lvReminders;
	ActionMode mActionModeNote;
	ArrayList<Long> remindersToDelete = new ArrayList<Long>();
	private MenuItem mnuDelete;
	private MenuItem mnuAdd;
	private Cursor reminders;
	private SavedRemindersAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			setContentView(R.layout.activity_saved_reminders);

			Log.v(MODULE_TAG, "Cycle: RemindersActivity onCreateView");

			//setHasOptionsMenu(true);

			lvReminders = (ListView) findViewById(R.id.lvReminders);
			populateRemindersList(lvReminders);
			remindersToDelete.clear();
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	private final ActionMode.Callback mActionModeCallbackNote = new ActionMode.Callback() {

		// Called when the action mode is created; startActionMode() was called
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			try {
			// Inflate a menu resource providing context menu items
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.saved_reminders_context_menu, menu);
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
			return true;
		}

		// Called each time the action mode is shown. Always called after
		// onCreateActionMode, but
		// may be called multiple times if the mode is invalidated.
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			try {
				mnuDelete = menu.getItem(0);
				mnuDelete.setEnabled(false);
				mnuAdd = menu.getItem(1);
				mnuAdd.setEnabled(true);

				mode.setTitle(remindersToDelete.size() + " Selected");
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

				case R.id.action_delete_reminders:
					deleteReminders();
					mode.finish(); // Action picked, so close the CAB
					return true;

				case R.id.action_cancel_delete_reminders:
					mode.finish();
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
				int numListViewItems = lvReminders.getChildCount();
				mActionModeNote = null;
				remindersToDelete.clear();

				// Reset all list items to their normal color
				for (int i = 0; i < numListViewItems; i++) {
					lvReminders.getChildAt(i).setBackgroundColor(Color.parseColor("#80ffffff"));
				}
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	};

	void populateRemindersList(ListView lv) {

		// Get list from the real phone database. W00t!
		final DbAdapter mDb = new DbAdapter(this);
		mDb.openReadOnly();

		try {
			reminders = mDb.fetchAllReminders();

			String[] from = new String[] { DbAdapter.K_REMINDER_DAYS, DbAdapter.K_REMINDER_HOURS,
					DbAdapter.K_REMINDER_MINUTES, DbAdapter.K_REMINDER_ENABLED };

			int[] to = new int[] { 0, 0, 0, 0 };

			adapter = new SavedRemindersAdapter(this,
					R.layout.saved_reminders_list_item, reminders, from, to,
					CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

			lv.setAdapter(adapter);
		} catch (SQLException ex) {
			Log.e(MODULE_TAG, ex.getMessage());
			// Do nothing, for now!
		} catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		mDb.close();

		lv.setOnItemClickListener(new ReminderClickListener());

		registerForContextMenu(lv);
	}

	private void deleteReminders() {
		try {
			for (int i = 0; i < remindersToDelete.size(); i++) {
				deleteReminder(remindersToDelete.get(i));
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	private void deleteReminder(long reminderId) {

		Reminder.cancel(this, reminderId);

		DbAdapter mDbHelper = new DbAdapter(this);
		mDbHelper.open();
		try {
			if (!mDbHelper.deleteReminder(reminderId)) {
				Log.e(MODULE_TAG, "Could not delete reminder(" + String.valueOf(reminderId) + ")");
			}
		}
		finally {
			mDbHelper.close();
		}
		lvReminders.invalidate();
		populateRemindersList(lvReminders);
	}

	// show edit button and hidden delete button
	@Override
	public void onResume() {
		super.onResume();
		try {
			Log.v(MODULE_TAG, "Cycle: RemindersActivity onResume");
			lvReminders.invalidate();
			populateRemindersList(lvReminders);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		try {
			Log.v(MODULE_TAG, "Cycle: RemindersActivity onPause");
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
			Log.v(MODULE_TAG, "Cycle: RemindersActivity onDestroy");
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	/* Creates the menu items */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		try {
			// Inflate the menu items for use in the action bar
			getMenuInflater().inflate(R.menu.saved_reminders, menu);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		return super.onCreateOptionsMenu(menu);
	}

	/* Handles item selections */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		try {
			// Handle presses on the action bar items
			switch (item.getItemId()) {

			case R.id.action_edit_saved_reminders:
				// edit
				if (mActionModeNote != null) {
					return false;
				}

				// Start the CAB using the ActionMode.Callback defined above
				mActionModeNote = startActionMode(mActionModeCallbackNote);
				return true;

			case R.id.action_add_reminder:
				transitionToEditReminderActivity(-1);
				return true;

			case R.id.action_done_reminder:
				transitionToTabsConfigActivity();
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

	@Override
	public void onBackPressed() {
		try {
			transitionToTabsConfigActivity();
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	/**
	 * Setup transition to the TabsConfigActivity
	 */
	private void transitionToTabsConfigActivity() {

		Intent intent = new Intent(this, TabsConfig.class);

		intent.putExtra(TabsConfig.EXTRA_SHOW_FRAGMENT, TabsConfig.FRAG_INDEX_SETTINGS);
		startActivity(intent);
		finish();
		overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
	}

	/**
	 * Setup transition to the TabsConfigActivity
	 */
	private void transitionToEditReminderActivity(long reminderId) {

		Intent intent = new Intent(this, EditReminderActivity.class);

		if (reminderId > 0) {
			intent.putExtra(EditReminderActivity.EXTRA_REMINDER_ID, reminderId);
		}
		startActivity(intent);
		//finish();
		overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
	}
}
