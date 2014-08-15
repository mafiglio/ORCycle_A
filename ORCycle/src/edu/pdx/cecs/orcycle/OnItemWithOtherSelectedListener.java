package edu.pdx.cecs.orcycle;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;

public class OnItemWithOtherSelectedListener implements OnItemSelectedListener {

	private static final String MODULE_TAG = "OnItemWithOtherSelectedListener";
	private static final String DEFAULT_OTHER_TEXT = "Other...";

	private int otherIndex = -1;
	private String otherText = "";
	private AlertDialog.Builder inputDialog;
	private EditText editText;
	private ArrayAdapter<String> myArrayAdapter = null;
	private String[] items;
	private AdapterView<ArrayAdapter<String>> myAdapterView;
	private boolean ignoreNextOtherSelection;
	private final Context context;

	public OnItemWithOtherSelectedListener(Context context) {
		this.context = context;
		ignoreNextOtherSelection = false;
	}

	public void ignoreNextOtherSelection() {
		this.ignoreNextOtherSelection = true;
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		try {
			Log.v(MODULE_TAG, "Item selected(" + position + ") = " + id);

			if (otherIndex == position - 1) {
				myArrayAdapter = null;
				try {
					myAdapterView = (AdapterView<ArrayAdapter<String>>) parent;
					myArrayAdapter = myAdapterView.getAdapter();
					String item = myArrayAdapter.getItem(position);
					Log.v(MODULE_TAG, "Item(" + position + ") = <" + item + ">");
					if (!ignoreNextOtherSelection) {
						inputDialog = new AlertDialog.Builder(context);
						editText = new EditText(context);
						inputDialog.setView(editText);
						inputDialog.setTitle("Specify other...");
						inputDialog.setPositiveButton("OK", new OtherPositiveButton_OnClickListener());
						inputDialog.setNegativeButton("Cancel", new OtherNegativeButton_OnClickListener());
						inputDialog.setCancelable(true);
						inputDialog.show();
					}
				}
				catch(ClassCastException ex) {
					Log.e(MODULE_TAG, ex.getMessage());
				}
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		ignoreNextOtherSelection = false;
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		try {
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	private final class OtherPositiveButton_OnClickListener implements
			DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int id) {
			try {
				setOtherText(editText.getText().toString());
			}
			catch (Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
			finally {
				dialog.dismiss();
			}
		}
	}

	private final class OtherNegativeButton_OnClickListener implements
			DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int id) {
			try {
				dialog.dismiss();
			}
			catch (Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	public void setOtherIndex(int index) {
		this.otherIndex = index;
	}

	public void setOtherText(String text) {

		if (null == text) {
			otherText="";
		}
		else {
			otherText = text.trim();
		}

		// Pick something sane to show the user
		if (otherText == "") {
			this.items[otherIndex + 1] = DEFAULT_OTHER_TEXT;
		}
		else {
			this.items[otherIndex + 1] = "Other( \"" +  text + "\" )";
		}

		// First try
		//myArrayAdapter.clear();
		//myArrayAdapter.addAll(this.items);
		//myArrayAdapter.notifyDataSetChanged();

		// Second try
		//ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(NoteQuestionsActivity.this, otherIndex);
		//myAdapterView.setAdapter(arrayAdapter);
	}

	public String getOtherText() {
		return otherText;
	}

	public void setItems(String[] items) {
		this.items = new String[items.length];
		for (int i = 0; i < items.length; ++i) {
			this.items[i] = new String(items[i]);
		}
	}
}
