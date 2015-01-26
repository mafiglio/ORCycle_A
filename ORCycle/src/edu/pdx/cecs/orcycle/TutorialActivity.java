package edu.pdx.cecs.orcycle;

import java.util.Locale;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class TutorialActivity extends Activity {

	private static final String MODULE_TAG = "TutorialActivity";

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
	 * derivative, which will keep every loaded fragment in memory. If this
	 * becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v13.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	public ViewPager mViewPager;

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
		mSectionsPagerAdapter.setViewPager(mViewPager);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.tutorial, menu);
		//return true;
		return false;
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
			// Show 3 total pages.
			return 3;
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
			}
			return null;
		}

		public void setViewPager(ViewPager viewPager) {
			mViewPager = viewPager;
		}

		public ViewPager getViewPager() {
			return mViewPager;
		}
	}

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
		private Button btnPrev;
		private Button btnNext;
		private Button btnDone;

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

				TextView textView = (TextView) rootView.findViewById(R.id.section_label);
				textView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));

				btnPrev = (Button) rootView.findViewById(R.id.btn_tutorial_previous);
				btnPrev.setOnClickListener(new ButtonPrevious_OnClickListener(section));

				btnNext = (Button) rootView.findViewById(R.id.btn_tutorial_next);
				btnNext.setOnClickListener(new ButtonNext_OnClickListener(section));

				btnDone = (Button) rootView.findViewById(R.id.btn_tutorial_done);
				btnDone.setOnClickListener(new ButtonDone_OnClickListener(section));

				ImageView imageView = (ImageView) rootView.findViewById(R.id.iv_tutorial);

				switch(section) {
				case 1:

					imageView.setImageResource(R.drawable.how_to_1);
					btnPrev.setVisibility(View.GONE);
					btnNext.setVisibility(View.VISIBLE);
					btnDone.setVisibility(View.GONE);
					break;

				case 2:

					imageView.setImageResource(R.drawable.how_to_2);
					btnPrev.setVisibility(View.VISIBLE);
					btnNext.setVisibility(View.VISIBLE);
					btnDone.setVisibility(View.GONE);
					break;

				case 3:

					imageView.setImageResource(R.drawable.how_to_3);
					btnPrev.setVisibility(View.VISIBLE);
					btnNext.setVisibility(View.GONE);
					btnDone.setVisibility(View.VISIBLE);
					break;

				default:

					imageView.setImageResource(R.drawable.how_to_3);
					btnPrev.setVisibility(View.VISIBLE);
					btnNext.setVisibility(View.GONE);
					btnDone.setVisibility(View.VISIBLE);
					break;
				}

				getActivity().getActionBar().setDisplayShowTitleEnabled(true);
				getActivity().getActionBar().setDisplayShowHomeEnabled(false);
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
			return rootView;
		}

		/**
	     * Class: ButtonPrevious_OnClickListener
	     *
	     * Description: Callback to be invoked when the previousButton button is clicked
	     */
		private final class ButtonPrevious_OnClickListener implements View.OnClickListener {

			private final int mSection;

			public ButtonPrevious_OnClickListener(int section) {
				super();
				mSection = section;
			}

			/**
			 * Description: Handles onClick for view
			 */
			public void onClick(View v) {
				try {
					ViewPager viewPager = (ViewPager) TutorialFragment.this.getActivity().findViewById(R.id.pager);
					viewPager.setCurrentItem(mSection - 2, true);
				}
				catch(Exception ex) {
					Log.e(MODULE_TAG, ex.getMessage());
				}
			}
		}

		/**
	     * Class: ButtonNext_OnClickListener
	     *
	     * Description: Callback to be invoked when the previousButton button is clicked
	     */
		private final class ButtonNext_OnClickListener implements View.OnClickListener {

			private final int mSection;

			public ButtonNext_OnClickListener(int section) {
				super();
				mSection = section;
			}

			/**
			 * Description: Handles onClick for view
			 */
			public void onClick(View v) {
				try {
					ViewPager viewPager = (ViewPager) TutorialFragment.this.getActivity().findViewById(R.id.pager);
					viewPager.setCurrentItem(mSection, true);
				}
				catch(Exception ex) {
					Log.e(MODULE_TAG, ex.getMessage());
				}
				finally {
				}
			}
		}

		/**
	     * Class: ButtonDone_OnClickListener
	     *
	     * Description: Callback to be invoked when the previousButton button is clicked
	     */
		private final class ButtonDone_OnClickListener implements View.OnClickListener {

			private final int mSection;

			public ButtonDone_OnClickListener(int section) {
				super();
				mSection = section;
			}

			/**
			 * Description: Handles onClick for view
			 */
			public void onClick(View v) {
				try {
					TutorialFragment.this.getActivity().finish();
				}
				catch(Exception ex) {
					Log.e(MODULE_TAG, ex.getMessage());
				}
				finally {
				}
			}
		}
	}


}
