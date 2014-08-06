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

public class FragmentSavedNotesSection extends Fragment {

	private static final String MODULE_TAG = "FragmentSavedNotesSection";

	public static final String ARG_SECTION_NUMBER = "section_number";

	ListView listSavedNotes;
	ActionMode mActionModeNote;
	ArrayList<Long> noteIdArray = new ArrayList<Long>();
	private MenuItem saveMenuItemDelete, saveMenuItemUpload;
	String[] values;

	Long storedID;

	Cursor allNotes;

	public SavedNotesAdapter sna;

	public FragmentSavedNotesSection() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = null;

		try {
			rootView = inflater.inflate(R.layout.activity_saved_notes, null);

			Log.v(MODULE_TAG, "Cycle: SavedNotes onCreateView");

			setHasOptionsMenu(true);

			listSavedNotes = (ListView) rootView
					.findViewById(R.id.listViewSavedNotes);
			populateNoteList(listSavedNotes);

			final DbAdapter mDb = new DbAdapter(getActivity());
			mDb.open();
			try {
				// Clean up any bad notes from crashes
				int cleanedNotes = mDb.cleanNoteTables();
				if (cleanedNotes > 0) {
					Toast.makeText(getActivity(),
							"" + cleanedNotes + " bad notes(s) removed.",
							Toast.LENGTH_SHORT).show();
				}
			}
			finally {
				mDb.close();
			}

			noteIdArray.clear();
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}

		return rootView;
	}

	private final ActionMode.Callback mActionModeCallbackNote = new ActionMode.Callback() {

		// Called when the action mode is created; startActionMode() was called
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			try {
			// Inflate a menu resource providing context menu items
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.saved_notes_context_menu, menu);
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
				Log.v(MODULE_TAG, "Prepare");
				saveMenuItemDelete = menu.getItem(0);
				saveMenuItemDelete.setEnabled(false);
				saveMenuItemUpload = menu.getItem(1);

				int flag = 1;
				for (int i = 0; i < listSavedNotes.getCount(); i++) {
					allNotes.moveToPosition(i);
					flag = flag
							* (allNotes.getInt(allNotes
									.getColumnIndex("notestatus")) - 1);
					if (flag == 0) {
						storedID = allNotes.getLong(allNotes.getColumnIndex("_id"));
						Log.v(MODULE_TAG, "" + storedID);
						break;
					}
				}
				if (flag == 1) {
					saveMenuItemUpload.setEnabled(false);
				} else {
					saveMenuItemUpload.setEnabled(true);
				}

				mode.setTitle(noteIdArray.size() + " Selected");
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
				case R.id.action_delete_saved_notes:
					try {
						// delete selected notes
						for (int i = 0; i < noteIdArray.size(); i++) {
							deleteNote(noteIdArray.get(i));
						}
					}
					catch(Exception ex) {
						Log.e(MODULE_TAG, ex.getMessage());
					}
					mode.finish(); // Action picked, so close the CAB
					return true;
				case R.id.action_upload_saved_notes:
					try {
						// upload selected notes
						// for (int i = 0; i < noteIdArray.size(); i++) {
						// retryNoteUpload(noteIdArray.get(i));
						// }
						// Log.v("Jason", "" + storedID);
						retryNoteUpload(storedID);
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
				int numListViewItems = listSavedNotes.getChildCount();
				mActionModeNote = null;
				noteIdArray.clear();

				// Reset all list items to their normal color
				for (int i = 0; i < numListViewItems; i++) {
					listSavedNotes.getChildAt(i).setBackgroundColor(Color.parseColor("#80ffffff"));
				}
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	};

	void populateNoteList(ListView lv) {
		// Get list from the real phone database. W00t!
		final DbAdapter mDb = new DbAdapter(getActivity());
		//mDb.open();
		mDb.openReadOnly();
		try {
			allNotes = mDb.fetchAllNotes();

			String[] from = new String[] { "notetype", "noterecorded",
					"notestatus" };
			int[] to = new int[] { R.id.TextViewType, R.id.TextViewStart };

			sna = new SavedNotesAdapter(getActivity(),
					R.layout.saved_notes_list_item, allNotes, from, to,
					CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

			lv.setAdapter(sna);
		} catch (SQLException ex) {
			Log.e(MODULE_TAG, ex.getMessage());
			// Do nothing, for now!
		} catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		mDb.close();

		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
				try {
					allNotes.moveToPosition(pos);
					if (mActionModeNote == null) {
						if (allNotes.getInt(allNotes.getColumnIndex("notestatus")) == 2) {
							Intent i = new Intent(getActivity(),
									NoteMapActivity.class);
							i.putExtra("shownote", id);
							startActivity(i);
						} else if (allNotes.getInt(allNotes
								.getColumnIndex("notestatus")) == 1) {
							// Toast.makeText(getActivity(), "Unsent",
							// Toast.LENGTH_SHORT).show();
							buildAlertMessageUnuploadedNoteClicked(id);

							// Log.v("Jason",
							// ""+allNotes.getLong(allNotes.getColumnIndex("_id")));
						}

					} else {
						// highlight
						if (noteIdArray.indexOf(id) > -1) {
							noteIdArray.remove(id);
							v.setBackgroundColor(Color.parseColor("#80ffffff"));
						} else {
							noteIdArray.add(id);
							v.setBackgroundColor(Color.parseColor("#ff33b5e5"));
						}
						// Toast.makeText(getActivity(), "Selected: " + noteIdArray,
						// Toast.LENGTH_SHORT).show();
						if (noteIdArray.size() == 0) {
							saveMenuItemDelete.setEnabled(false);
						} else {
							saveMenuItemDelete.setEnabled(true);
						}

						mActionModeNote.setTitle(noteIdArray.size() + " Selected");
					}
				}
				catch(Exception ex) {
					Log.e(MODULE_TAG, ex.getMessage());
				}
			}
		});

		registerForContextMenu(lv);
	}

	private void buildAlertMessageUnuploadedNoteClicked(final long position) {
		try {
			final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle("Upload Note");
			builder.setMessage("Do you want to upload this note?");
			builder.setNegativeButton("Upload",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							try {
								dialog.cancel();
								retryNoteUpload(position);
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

	private void retryNoteUpload(long noteId) {
		NoteUploader uploader = new NoteUploader(getActivity());
		FragmentSavedNotesSection f3 = (FragmentSavedNotesSection) getActivity()
				.getSupportFragmentManager().findFragmentByTag(
						"android:switcher:" + R.id.pager + ":2");
		uploader.setSavedNotesAdapter(sna);
		uploader.setFragmentSavedNotesSection(f3);
		uploader.setListView(listSavedNotes);
		uploader.execute();
	}

	private void deleteNote(long noteId) {
		DbAdapter mDbHelper = new DbAdapter(getActivity());
		mDbHelper.open();
		try {
			mDbHelper.deleteNote(noteId);
		}
		finally {
			mDbHelper.close();
		}
		listSavedNotes.invalidate();
		populateNoteList(listSavedNotes);
	}

	// show edit button and hidden delete button
	@Override
	public void onResume() {
		super.onResume();
		try {
			Log.v(MODULE_TAG, "Cycle: SavedNotes onResume");
			populateNoteList(listSavedNotes);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		try {
			Log.v(MODULE_TAG, "Cycle: SavedNotes onPause");
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		try {
			Log.v(MODULE_TAG, "Cycle: SavedNotes onDestroyView");
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
			inflater.inflate(R.menu.saved_notes, menu);
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

			case R.id.action_edit_saved_notes:
				// edit
				if (mActionModeNote != null) {
					return false;
				}

				// Start the CAB using the ActionMode.Callback defined above
				mActionModeNote = getActivity().startActionMode(
						mActionModeCallbackNote);
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
}
