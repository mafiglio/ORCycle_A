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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

public class TutorialActivity extends Activity {

	private static final String MODULE_TAG = "TutorialActivity";
	public static final String EXTRA_PREVIOUS_ACTIVITY = "previous_activity";
	public static final int EXTRA_PREVIOUS_ACTIVITY_MAIN_INPUT = 1;
	public static final int EXTRA_PREVIOUS_ACTIVITY_USER_SETTINGS = 2;
	private static final int NUM_TUTORIAL_PAGES = 9;
	private static final int LAST_TUTORIAL_PAGE = 9;

	private int previousActivity;

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
	 * derivative, which will keep every loaded fragment in memory. If this
	 * becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v13.app.FragmentStatePagerAdapter}.
	 */
	private SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	private ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tutorial);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		//mViewPager.setOffscreenPageLimit(0);
		mSectionsPagerAdapter.setViewPager(mViewPager);

		// We need maximum space on this screen so hide action bar
		getActionBar().hide();

		if (null != savedInstanceState)
			LoadExtras(savedInstanceState);
		else
			LoadExtras(getIntent().getExtras());
	}

	private void LoadExtras(Bundle extras) {
		previousActivity = extras.getInt(EXTRA_PREVIOUS_ACTIVITY, EXTRA_PREVIOUS_ACTIVITY_MAIN_INPUT);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
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

	public void transitionToTabsConfigActivity() {

		Intent intent = new Intent(this, TabsConfig.class);

		if (previousActivity == EXTRA_PREVIOUS_ACTIVITY_MAIN_INPUT) {
			intent.putExtra(TabsConfig.EXTRA_SHOW_FRAGMENT, TabsConfig.FRAG_INDEX_MAIN_INPUT);
		}
		else {
			intent.putExtra(TabsConfig.EXTRA_SHOW_FRAGMENT, TabsConfig.FRAG_INDEX_SETTINGS);
		}
		startActivity(intent);
		finish();
		overridePendingTransition(android.R.anim.fade_in, R.anim.slide_out_down);
	}

	// *********************************************************************************
	// *                        SectionsPagerAdapter Class
	// *********************************************************************************

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		private ViewPager mViewPager;

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
			mViewPager = null;
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a PlaceholderFragment (defined as a static inner class
			// below).
			return TutorialFragment.newInstance(position + 1, mViewPager);
		}

		@Override
		public int getCount() {
			return NUM_TUTORIAL_PAGES;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			default: return getString(R.string.tutorial_title_add_report);
			case 1: return getString(R.string.tutorial_title_reports);
			case 2: return getString(R.string.tutorial_title_start_trip);
			case 3: return getString(R.string.tutorial_title_trips);
			case 4: return getString(R.string.tutorial_title_trip_map);
			case 5: return getString(R.string.tutorial_title_biking_habits);
			case 6: return getString(R.string.tutorial_title_add_reminder);
			case 7: return getString(R.string.tutorial_title_user);
			case 8: return getString(R.string.tutorial_title_links);
			}
		}

		public void setViewPager(ViewPager viewPager) {
			mViewPager = viewPager;
		}

		public ViewPager getViewPager() {
			return mViewPager;
		}
	}

	// *********************************************************************************
	// *                        Placeholder Fragment Class
	// *********************************************************************************

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class TutorialFragment extends Fragment {

		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";

		private int section;
		private Button btnDone;
		private int previousActivity;

		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static TutorialFragment newInstance(int sectionNumber, ViewPager viewPager) {
			TutorialFragment fragment = new TutorialFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		/**
		 * Default constructor initializes internal variables
		 */
		public TutorialFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

			View rootView = null;
			try {
				rootView = inflater.inflate(R.layout.fragment_tutorial, container, false);

				section = getArguments().getInt(ARG_SECTION_NUMBER);

				TutorialActivity tutorialActivity = (TutorialActivity) getActivity();
				previousActivity = tutorialActivity.previousActivity;

				btnDone = (Button) rootView.findViewById(R.id.btn_tutorial_done);
				btnDone.setOnClickListener(new ButtonDone_OnClickListener());

				if (previousActivity == TutorialActivity.EXTRA_PREVIOUS_ACTIVITY_MAIN_INPUT)
					btnDone.setText(R.string.turorial_btn_done_main_input);
				else
					btnDone.setText(R.string.turorial_btn_done_user_settings);

				ImageView imageView = (ImageView) rootView.findViewById(R.id.iv_tutorial);
				//imageView.setImageResource(getTutorialImage(section));

				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inSampleSize = 2;
				Bitmap bMap = BitmapFactory.decodeResource(getResources(), getTutorialImage(section), options);
				imageView.setImageBitmap(bMap);


				// set button visibilities
				if (section == LAST_TUTORIAL_PAGE) {
					btnDone.setVisibility(View.VISIBLE);
				}
				else {
					btnDone.setVisibility(View.GONE);
				}
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
			return rootView;
		}

		@Override
		public void onStart() {
			super.onStart();

			try {
				Log.v(MODULE_TAG, "Cycle: onStart("+section+")");
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}

		@Override
		public void onResume() {
			super.onResume();

			try {
				Log.v(MODULE_TAG, "Cycle: onResume("+section+")");
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}

		@Override
		public void onPause() {

			try {
				Log.v(MODULE_TAG, "Cycle: onPause("+section+")");
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
			super.onPause();
		}

		@Override
		public void onStop() {

			try {
				Log.v(MODULE_TAG, "Cycle: onStop("+section+")");
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
			super.onStop();
		}

		@Override
		public void onDestroyView() {
			try {
				Log.v(MODULE_TAG, "Cycle: onDestroyView("+section+")");
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
			super.onDestroyView();
		}

		private int getTutorialImage(int section) {
			switch(section) {
			default: return R.drawable.tutorial_add_report;
			case 2: return R.drawable.tutorial_reports;
			case 3: return R.drawable.tutorial_start_trip;
			case 4: return R.drawable.tutorial_trips;
			case 5: return R.drawable.tutorial_trip_map;
			case 6: return R.drawable.tutorial_biking_habits;
			case 7: return R.drawable.tutorial_add_reminder;
			case 8: return R.drawable.tutorial_user;
			case 9: return R.drawable.tutorial_links;
			}
		}

		// *********************************************************************************
		// *                              Button Handlers
		// *********************************************************************************

		/**
	     * Class: ButtonDone_OnClickListener
	     *
	     * Description: Callback to be invoked when the previousButton button is clicked
	     */
		private final class ButtonDone_OnClickListener implements View.OnClickListener {

			/**
			 * Description: Handles onClick for view
			 */
			public void onClick(View v) {
				try {
					if (previousActivity == TutorialActivity.EXTRA_PREVIOUS_ACTIVITY_MAIN_INPUT) {
						dialogRepeatTutorial();
					}
					else {
						((TutorialActivity) getActivity()).transitionToTabsConfigActivity();
					}
				}
				catch(Exception ex) {
					Log.e(MODULE_TAG, ex.getMessage());
				}
				finally {
				}
			}
		}

		// *********************************************************************************
		// *                            Repeat Tutorial Dialog
		// *********************************************************************************

		/**
		 * Build dialog telling user to save this trip
		 */
		private void dialogRepeatTutorial() {
			final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(getResources().getString(R.string.turorial_drt_title));
			builder.setMessage(getResources().getString(R.string.turorial_drt_message));
			builder.setPositiveButton(getResources().getString(R.string.turorial_drt_yes_button),
					new DialogRepeatTutorial_OnPositiveButtonClicked());
			builder.setNegativeButton(getResources().getString(R.string.turorial_drt_no_button),
					new DialogRepeatTutorial_OnNegativeButtonClicked());
			final AlertDialog alert = builder.create();
			alert.show();
		}

		/**
		 * Listener for "No" button in dialog
		 */
		private final class DialogRepeatTutorial_OnNegativeButtonClicked implements
				DialogInterface.OnClickListener {
			public void onClick(DialogInterface dialog, int id) {
				try {
					MyApplication.getInstance().setTutorialEnabled(false);
					((TutorialActivity) getActivity()).transitionToTabsConfigActivity();
				} catch (Exception ex) {
					Log.e(MODULE_TAG, ex.getMessage());
				}
			}
		}

		/**
		 * Listener for "Yes" button in dialog
		 */
		private final class DialogRepeatTutorial_OnPositiveButtonClicked implements
				DialogInterface.OnClickListener {
			public void onClick(DialogInterface dialog, int id) {
				try {
					MyApplication.getInstance().setTutorialEnabled(true);
					((TutorialActivity) getActivity()).transitionToTabsConfigActivity();
				} catch (Exception ex) {
					Log.e(MODULE_TAG, ex.getMessage());
				}
			}
		}
	}
}
