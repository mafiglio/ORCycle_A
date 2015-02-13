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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

public class MultiSelectionSpinner extends Spinner implements
		OnMultiChoiceClickListener {

	private static final String MODULE_TAG = "MultiSelectionSpinner";
	private static final String DEFAULT_OTHER_CAPTION = "Other...";

	private String[] _items = null;
	private boolean[] mSelection = null;
	private boolean[] prevSelections = null;
	private String title = null;
	private int indexOfOther = -1;
	private int indexOfOverride = -1;
	private String otherText = "";
	private EditText etOther = null;
	private AlertDialog alertDialog = null;

	private final ArrayAdapter<String> simple_adapter;

	public MultiSelectionSpinner(Context context) {
		super(context);

		simple_adapter = new ArrayAdapter<String>(context,
				android.R.layout.simple_spinner_item);
		super.setAdapter(simple_adapter);
	}

	public MultiSelectionSpinner(Context context, AttributeSet attrs) {
		super(context, attrs);

		simple_adapter = new ArrayAdapter<String>(context,
				android.R.layout.simple_spinner_item);
		super.setAdapter(simple_adapter);
	}

	public void onClick(DialogInterface dialog, int index, boolean isChecked) {

		if (mSelection != null && index < mSelection.length) {
			mSelection[index] = isChecked;

			if (indexOfOverride >= 0) { // then other checkbox was clicked
				if (isChecked) {
					if (index == indexOfOverride) { // then show dialog to get other text value
						performResetAllOthers();
					}
					else {
						performResetOverride();
					}
				}
			}

			if ((indexOfOther >= 0) && (indexOfOther == index)) { // then other checkbox was clicked
				if (isChecked) { // then show dialog to get other text value
					performGetOtherText();
				}
				else { // other was deselected so clear value
					setOtherText(null);
				}
			}
			else {
				simple_adapter.clear();
				simple_adapter.add(buildSelectedItemString());
			}

		} else {
			throw new IllegalArgumentException(
					"Argument 'index' is out of bounds.");
		}
	}

	private void performResetAllOthers() {
		for (int index = 0; index < mSelection.length; ++index) {
			if (indexOfOverride != index) {
				mSelection[index] = false;
			}
		}
		simple_adapter.clear();
		String selectedItems = buildSelectedItemString();
		simple_adapter.add(selectedItems);

		if (null != alertDialog) {
			// get a reference to the ListView widget in the dialog
			ListView list = alertDialog.getListView();

			for (int index = 0; index < mSelection.length; ++index) {
				list.setItemChecked(index, mSelection[index]);
			}
		}
	}

	private void performResetOverride() {

		mSelection[indexOfOverride] = false;

		simple_adapter.clear();
		simple_adapter.add(buildSelectedItemString());

		if (null != alertDialog) {
			// get a reference to the ListView widget in the dialog
			ListView list = alertDialog.getListView();

			list.setItemChecked(indexOfOverride, false);
		}
	}

	private void performResetChanges() {

		for (int index = 0; index < mSelection.length; ++index) {
			mSelection[index] = prevSelections[index];
		}
		simple_adapter.clear();
		simple_adapter.add(buildSelectedItemString());
	}

	public void setOtherText(String text) {

		// Set and trim value of other
		if (null == text) {
			otherText = "";
		}
		else {
			otherText = text.trim();
		}

		// change the text in the item array
		if (otherText.equals("")) {
			_items[indexOfOther] = DEFAULT_OTHER_CAPTION;
		}
		else {
			_items[indexOfOther] = "Other( \"" +  otherText + "\" )";
		}

		//
		simple_adapter.clear();
		simple_adapter.add(buildSelectedItemString());

		if (null != alertDialog) {
			// get a reference to the ListView widget in the dialog
			ListView list = alertDialog.getListView();

			// get a reference to the arrayadapter held by the ListView widget
			ArrayAdapter adapter = (ArrayAdapter)list.getAdapter();

			// Notify the adapter that the underlying dataset has changed
			adapter.notifyDataSetChanged();
		}
	}

	private void performGetOtherText() {

		// Create an EditText view to get user input
		etOther = new EditText(getContext());

		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle("Specify other:");
		builder.setView(etOther); // Here the variable alert is the instance of
		builder.setPositiveButton("OK", new OtherPositiveButton_OnClickListener());
		builder.setNegativeButton("Cancel", new OtherNegativeButton_OnClickListener());
		builder.show();
	}

	private final class PositiveButton_OnClickListener implements
			DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int id) {
			// ...
		}
	}

	private final class NegativeButton_OnClickListener implements
			DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int id) {
			performResetChanges();
		}
	}

	private final class OtherPositiveButton_OnClickListener implements
			DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int id) {
			try {
				setOtherText(etOther.getEditableText().toString());
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	private final class OtherNegativeButton_OnClickListener implements
			DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int id) {
			// ...
		}
	}

	@Override
	public boolean performClick() {

		prevSelections = new boolean[mSelection.length];

		for (int i = 0; i < mSelection.length; ++i) {
			prevSelections[i] = mSelection[i];
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setMultiChoiceItems(_items, mSelection, this);
		builder.setTitle(title);
		builder.setPositiveButton("OK", new PositiveButton_OnClickListener());
		builder.setNegativeButton("Cancel", new NegativeButton_OnClickListener());

		alertDialog = builder.create();
		alertDialog.show();
		return true;

	}

	public void setTitle(String value) {
		this.title = value;
	}

	public String getTitle() {
		return this.title;
	}

	public void setOtherIndex(int value) {
		if (value < 0)
			value = -1;
		indexOfOther = value;
	}

	public void setOverrideIndex(int value) {
		if (value < 0)
			value = -1;
		indexOfOverride = value;
	}

	@Override
	public void setAdapter(SpinnerAdapter adapter) {
		throw new RuntimeException(
				"setAdapter is not supported by MultiSelectSpinner.");
	}

	public void setItems(String[] items) {
		_items = items;
		mSelection = new boolean[_items.length];
		simple_adapter.clear();
		// simple_adapter.add(_items[0]);
		Arrays.fill(mSelection, false);
	}

	public void setItems(List<String> items) {
		_items = items.toArray(new String[items.size()]);
		mSelection = new boolean[_items.length];
		simple_adapter.clear();
		// simple_adapter.add(_items[0]);
		Arrays.fill(mSelection, false);
	}

	public void setSelection(String[] selection) {
		for (String cell : selection) {
			for (int j = 0; j < _items.length; ++j) {
				if (_items[j].equals(cell)) {
					mSelection[j] = true;
				}
			}
		}
	}

	public void setSelection(List<String> selection) {
		for (int i = 0; i < mSelection.length; i++) {
			mSelection[i] = false;
		}
		for (String sel : selection) {
			for (int j = 0; j < _items.length; ++j) {
				if (_items[j].equals(sel)) {
					mSelection[j] = true;
				}
			}
		}
		simple_adapter.clear();
		simple_adapter.add(buildSelectedItemString());
	}

	@Override
	public void setSelection(int index) {
		for (int i = 0; i < mSelection.length; i++) {
			mSelection[i] = false;
		}
		if (index >= 0 && index < mSelection.length) {
			mSelection[index] = true;
		} else {
			throw new IllegalArgumentException("Index " + index
					+ " is out of bounds.");
		}
		simple_adapter.clear();
		simple_adapter.add(buildSelectedItemString());
	}

	public void setSelection(int[] selectedIndicies) {
		for (int i = 0; i < mSelection.length; i++) {
			mSelection[i] = false;
		}
		for (int index : selectedIndicies) {
			if (index >= 0 && index < mSelection.length) {
				mSelection[index] = true;
			} else {
				throw new IllegalArgumentException("Index " + index
						+ " is out of bounds.");
			}
		}
		simple_adapter.clear();
		simple_adapter.add(buildSelectedItemString());
	}

	public void setSelection(String text) {

		int index;

		// Initialize all entries to false
		for (int i = 0; i < mSelection.length; i++) {
			mSelection[i] = false;
		}

		// Set the selections
		String[] selections = text.split(",");

		for (String selection : selections) {
			try {
				index = Integer.parseInt(selection);
				if (index >= 0 && index < mSelection.length) {
					mSelection[index] = true;
				} else {
					throw new IllegalArgumentException("Index " + index
							+ " is out of bounds.");
				}
			} catch (Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
		simple_adapter.clear();
		simple_adapter.add(buildSelectedItemString());
	}

	public List<String> getSelectedStrings() {
		List<String> selection = new LinkedList<String>();
		for (int i = 0; i < _items.length; ++i) {
			if (mSelection[i]) {
				selection.add(_items[i]);
			}
		}
		return selection;
	}

	public List<Integer> getSelectedIndicies() {
		List<Integer> selection = new LinkedList<Integer>();
		for (int i = 0; i < _items.length; ++i) {
			if (mSelection[i]) {
				selection.add(i);
			}
		}
		return selection;
	}

	private String buildSelectedItemString() {
		StringBuilder sb = new StringBuilder();
		boolean foundOne = false;

		for (int i = 0; i < _items.length; ++i) {
			if (mSelection[i]) {
				if (foundOne) {
					sb.append(", ");
				}
				foundOne = true;
				sb.append(_items[i]);
			}
		}
		return sb.toString();
	}

	public String getSelectedItemsAsString() {
		StringBuilder sb = new StringBuilder();
		boolean foundOne = false;

		for (int i = 0; i < _items.length; ++i) {
			if (mSelection[i]) {
				if (foundOne) {
					sb.append(", ");
				}
				foundOne = true;
				sb.append(_items[i]);
			}
		}
		return sb.toString();
	}

	public String getSelectedIndicesAsString() {

		StringBuilder sb = new StringBuilder();
		boolean foundOne = false;

		for (int i = 0; i < _items.length; ++i) {
			if (mSelection[i]) {
				if (foundOne) {
					sb.append(",");
				}
				foundOne = true;
				sb.append(i);
			}
		}
		return sb.toString();
	}

	public String getOtherText() {
		return otherText;
	}

	/**
	 * <p>
	 * Callback method to be invoked when an item in this view has been
	 * selected. This callback is invoked only when the newly selected position
	 * is different from the previously selected position or if there was no
	 * selected item.
	 * </p>
	 *
	 * Impelmenters can call getItemAtPosition(position) if they need to access
	 * the data associated with the selected item.
	 *
	 * @param parent
	 *            The AdapterView where the selection happened
	 * @param view
	 *            The view within the AdapterView that was clicked
	 * @param position
	 *            The position of the view in the adapter
	 * @param id
	 *            The row id of the item that is selected
	 */
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		Log.v(MODULE_TAG, "_onItemSelected: Item selected(" + position + ") = "
				+ id);
	}

	/**
	 * Callback method to be invoked when the selection disappears from this
	 * view. The selection can disappear for instance when touch is activated or
	 * when the adapter becomes empty.
	 *
	 * @param parent
	 *            The AdapterView that now contains no selected item.
	 */
	public void onNothingSelected(AdapterView<?> parent) {
		Log.v(MODULE_TAG, "_onNothingSelected()");
	}

}