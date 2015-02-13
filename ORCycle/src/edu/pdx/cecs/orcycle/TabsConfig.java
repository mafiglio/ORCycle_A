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

import java.util.Locale;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

public class TabsConfig extends FragmentActivity implements
		ActionBar.TabListener {

	private static final String MODULE_TAG = "TabsConfig";

	public static final String EXTRA_KEEP_ME = "EXTRA_KEEP_ME";
	public static final String EXTRA_SHOW_FRAGMENT = "EXTRA_SHOW_FRAGMENT";
	public static final String EXTRA_DSA_ACTIVITY = "EXTRA_DSA_ACTIVITY";
	public static final String EXTRA_DSA_BUTTON_PRESSED = "dsa_button_pressed";
	public static final String EXTRA_DSA_DIALOG_ID = "dsa_dialog_id";


	public static final int FRAG_INDEX_MAIN_INPUT = 0;
	public static final int FRAG_INDEX_SAVED_TRIPS = 1;
	public static final int FRAG_INDEX_SAVED_NOTES = 2;
	public static final int FRAG_INDEX_SETTINGS = 3;

	private int fragmentToShow = FRAG_INDEX_MAIN_INPUT;
	private MyApplication myApp;

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	Fragment fragment1;

	Fragment fragment2;

	Fragment fragment3;

	Fragment fragment4;

	// *********************************************************************************
	// *                            Fragment Implementation
	// *********************************************************************************

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			myApp = MyApplication.getInstance();
			myApp.setRunning(true);
			myApp.clearReminderNotifications();

			setContentView(R.layout.tabs_config);

			Log.v(MODULE_TAG, "Cycle: TabsConfig onCreate");

			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

			// Toast.makeText(this, "Tab Created", Toast.LENGTH_LONG).show();

			fragment1 = new FragmentMainInput();
			fragment2 = new FragmentSavedTripsSection();
			fragment3 = new FragmentSavedNotesSection();
			//fragment4 = new FragmentUserInfo();
			fragment4 = new FragmentSettings();

			// Set up the action bar.
			final ActionBar actionBar = getActionBar();
			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
			actionBar.setBackgroundDrawable(getResources().getDrawable(R.color.psu_green));

			// Create the adapter that will return a fragment for each of the four
			// primary sections of the app.
			mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

			// Set up the ViewPager with the sections adapter.
			mViewPager = (ViewPager) findViewById(R.id.pager);
			mViewPager.setAdapter(mSectionsPagerAdapter);

			// When swiping between different sections, select the corresponding
			// tab. We can also use ActionBar.Tab#select() to do this if we have
			// a reference to the Tab.
			mViewPager
					.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
						@Override
						public void onPageSelected(int position) {
							actionBar.setSelectedNavigationItem(position);
						}
					});

			// For each of the sections in the app, add a tab to the action bar.
			for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
				// Create a tab with text corresponding to the page title defined by
				// the adapter. Also specify this Activity object, which implements
				// the TabListener interface, as the callback (listener) for when
				// this tab is selected.

				actionBar.addTab(actionBar.newTab()
						.setText(mSectionsPagerAdapter.getPageTitle(i))
						.setTabListener(this));
			}

			mViewPager.setOffscreenPageLimit(4);

			Intent intent;
			Bundle bundle;
			if (null != (intent = getIntent())) {
				if (null != (bundle = intent.getExtras())) {
					fragmentToShow = bundle.getInt(TabsConfig.EXTRA_SHOW_FRAGMENT, FRAG_INDEX_MAIN_INPUT);
				}
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		Log.v(MODULE_TAG, "Cycle: TabsConfig onResume");

		try {
			final ActionBar actionBar = getActionBar();
			actionBar.selectTab(actionBar.getTabAt(fragmentToShow));
			myApp.ResumeNotification();
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.v(MODULE_TAG, "Cycle: TabsConfig onPause");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.v(MODULE_TAG, "Cycle: TabsConfig onDestroy");
		try {
			myApp.clearRecordingNotifications();
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	// 2.0 and above
	@Override
	public void onBackPressed() {
	    moveTaskToBack(true);
	}

	// Before 2.0
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	        moveTaskToBack(true);
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}

	// *********************************************************************************
	// *                    ActionBar.TabListener Implementation
	// *********************************************************************************

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		try {
			// When the given tab is selected, switch to the corresponding page in
			// the ViewPager.
			// Toast.makeText(this, "TabSelected", Toast.LENGTH_LONG).show();
			mViewPager.setCurrentItem(tab.getPosition());
			final ActionBar actionBar = getActionBar();
			switch (tab.getPosition()) {
			case 0:
				actionBar.setDisplayShowTitleEnabled(false);
				actionBar.setDisplayShowHomeEnabled(false);
				break;
			case 1:
				actionBar.setDisplayShowTitleEnabled(true);
				actionBar.setDisplayShowHomeEnabled(true);
				actionBar.setTitle(R.string.app_name);
				break;
			case 2:
				actionBar.setDisplayShowTitleEnabled(true);
				actionBar.setDisplayShowHomeEnabled(true);
				actionBar.setTitle(R.string.app_name);
				break;
			case 3:
				actionBar.setDisplayShowTitleEnabled(true);
				actionBar.setDisplayShowHomeEnabled(true);
				actionBar.setTitle(R.string.app_name);
				break;
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

		mViewPager.startActionMode(new ActionMode.Callback() {
			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void onDestroyActionMode(ActionMode mode) {
				// TODO Auto-generated method stub

			}

			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
				// TODO Auto-generated method stub
				return false;
			}
		});
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	// *********************************************************************************
	// *                           SectionsPagerAdapter
	// *********************************************************************************

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {

			switch (position) {
			case FRAG_INDEX_MAIN_INPUT:
				return fragment1;
			case FRAG_INDEX_SAVED_TRIPS:
				return fragment2;
			case FRAG_INDEX_SAVED_NOTES:
				return fragment3;
			case FRAG_INDEX_SETTINGS:
				return fragment4;
			}
			return null;
		}

		@Override
		public int getCount() {
			// Show 4 total pages.
			return 4;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section2).toUpperCase(l);
			case 2:
				return getString(R.string.title_section3).toUpperCase(l);
			case 3:
				return getString(R.string.title_section4).toUpperCase(l);
			}
			return null;
		}
	}
}
