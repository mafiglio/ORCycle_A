package edu.pdx.cecs.orcycle;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class FragmentUserPreferences extends PreferenceFragment {

	@Override
	public void onCreate(Bundle saveInstanceState) {
		super.onCreate(saveInstanceState);
		addPreferencesFromResource(R.xml.user_preferences);

	}

}
