package edu.pdx.cecs.orcycle;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class FragmentSettings extends Fragment {

	private static final String MODULE_TAG = "FragmentSettings";
	private static final String ORCYCLE_URI = "http://orcycle.cecs.pdx.edu/Web/";

	// UI Elements
	private Button buttonUserInfo = null;
	private Button buttonGetStarted = null;
	private Button buttonAbout = null;

	// *********************************************************************************
	// *                              Fragment Handlers
	// *********************************************************************************

	/**
	 * Handler: onCreateView
	 * Update distance and speed user interface elements
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		View rootView = null;

		try {
			Log.v(MODULE_TAG, "onCreateView");

			// Create main user interface window
			rootView = inflater.inflate(R.layout.activity_settings, container, false);

			// get references to view widgets
			buttonUserInfo = (Button) rootView.findViewById(R.id.buttonUserInfo);
			buttonUserInfo.setOnClickListener(new ButtonUserInfo_OnClickListener());

			buttonGetStarted = (Button) rootView.findViewById(R.id.buttonGetStarted);
			buttonGetStarted.setOnClickListener(new ButtonGetStarted_OnClickListener());

			buttonAbout = (Button) rootView.findViewById(R.id.buttonAbout);
			buttonAbout.setOnClickListener(new ButtonAbout_OnClickListener());
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}

		return rootView;
	}

    /**
     * Handler: onResume
     * Called when the <code>activity<code/> will start interacting with the user. At this point
     * the <code>activity<code/> is at the top of the <code>activity<code/> stack, with user
     * input going to it. Always followed by <code>onPause()<code/>.
     * @see <code>onPause<code/> class.
     */
	@Override
	public void onResume() {
		super.onResume();
		try {
			Log.v(MODULE_TAG, "onResume()");
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	/**
	 * Handler: onPause
	 */
	@Override
	public void onPause() {
		super.onPause();
	}

	// *********************************************************************************
	// *                              Button Handlers
	// *********************************************************************************

	/**
     * Class: ButtonUserInfo_OnClickListener
     *
     * Description: Callback to be invoked when startButton button is clicked
     */
	private final class ButtonUserInfo_OnClickListener implements View.OnClickListener {

		/**
		 * Description: Handles onClick for view
		 */
		public void onClick(View v) {
			try {
				transitionToUserInfoActivity();
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
			finally {
			}
		}
	}

	/**
     * Class: ButtonGetStarted_OnClickListener
     *
     * Description: Callback to be invoked when startButton button is clicked
     */
	private final class ButtonGetStarted_OnClickListener implements View.OnClickListener {

		/**
		 * Description: Handles onClick for view
		 */
		public void onClick(View v) {
			try {
				transitionToORcycle();
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
			finally {
			}
		}
	}

	/**
     * Class: ButtonAbout_OnClickListener
     *
     * Description: Callback to be invoked when startButton button is clicked
     */
	private final class ButtonAbout_OnClickListener implements View.OnClickListener {

		/**
		 * Description: Handles onClick for view
		 */
		public void onClick(View v) {
			try {
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
			finally {
			}
		}
	}

	private void transitionToORcycle() {
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ORCYCLE_URI));
		startActivity(browserIntent);
	}

	private void transitionToUserInfoActivity() {

		// Create intent to go back to the TripMapActivity
		Intent intent = new Intent(getActivity(), UserInfoActivity.class);

		// Exit this activity
		startActivity(intent);
		getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	}

}
