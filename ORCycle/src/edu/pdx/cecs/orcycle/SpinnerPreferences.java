package edu.pdx.cecs.orcycle;

import android.content.SharedPreferences;
import android.widget.Spinner;

public class SpinnerPreferences {

	private final SharedPreferences settings;

	public SpinnerPreferences(SharedPreferences settings) {
		this.settings = settings;
	}

	/**
	 * Saves spinner selection to preferences editor
	 * @param editor
	 * @param spinner
	 * @param key
	 */
	public void save(Spinner spinner, int key) {
		settings.edit().putInt("" + key, spinner.getSelectedItemPosition());
	}

	/**
	 * Saves MultiSelectionSpinner selections to preferences editor
	 * @param editor
	 * @param spinner
	 * @param key
	 */
	public void save(MultiSelectionSpinner spinner, int key) {
		settings.edit().putString("" + key, spinner.getSelectedIndicesAsString());
	}

	public void commit() {
		settings.edit().commit();
	}

	public void recall(Spinner spinner, int key) {
		spinner.setSelection(settings.getInt(String.valueOf(key), 0));
	}

	public void recall(Spinner spinner, String key) {
		spinner.setSelection(settings.getInt(key, 0));
	}

	public void recall(MultiSelectionSpinner spinner, int key) {
		spinner.setSelection(settings.getString(String.valueOf(key), ""));
	}

	public void recall(MultiSelectionSpinner spinner, String key) {
		spinner.setSelection(settings.getString(key, ""));
	}

}
