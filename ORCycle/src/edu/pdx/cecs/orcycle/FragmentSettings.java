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
import android.widget.TextView;

public class FragmentSettings extends Fragment {

	private static final String MODULE_TAG = "FragmentSettings";
	private static final String ORCYCLE_URI = "http://www.pdx.edu/transportation-lab/android-instructions";
	private static final String PRIVACY_POLICY_URI = "http://www.pdx.edu/transportation-lab/privacy-policy";

	// UI Elements
	private Button btnUserInfo = null;
	private Button btnGetStarted = null;
	private Button btnFeedback = null;
	private Button btnPrivacyPolicy = null;
	private TextView tvVersionCode = null;

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
			btnUserInfo = (Button) rootView.findViewById(R.id.btnUserInfo);
			btnUserInfo.setOnClickListener(new UserInfo_OnClickListener());

			btnGetStarted = (Button) rootView.findViewById(R.id.btnGetStarted);
			btnGetStarted.setOnClickListener(new GetStarted_OnClickListener());

			btnFeedback = (Button) rootView.findViewById(R.id.btnFeedback);
			btnFeedback.setOnClickListener(new Feedback_OnClickListener());

			btnPrivacyPolicy = (Button) rootView.findViewById(R.id.btnPrivacyPolicy);
			btnPrivacyPolicy.setOnClickListener(new PrivacyPolicy_OnClickListener());

			tvVersionCode = (TextView) rootView.findViewById(R.id.as_version_code);
			tvVersionCode.setText(getResources().getString(R.string.about_version_code) +
					String.valueOf(MyApplication.getInstance().getVersionCode()));
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
	private final class UserInfo_OnClickListener implements View.OnClickListener {

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
	private final class GetStarted_OnClickListener implements View.OnClickListener {

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
	private final class Feedback_OnClickListener implements View.OnClickListener {

		/**
		 * Description: Handles onClick for view
		 */
		public void onClick(View v) {
			try {
				transitionToUserFeedbackActivity();
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
	private final class PrivacyPolicy_OnClickListener implements View.OnClickListener {

		/**
		 * Description: Handles onClick for view
		 */
		public void onClick(View v) {
			try {
				transitionToPrivacyPolicy();
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
			finally {
			}
		}
	}

	private void transitionToORcycle() {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(ORCYCLE_URI));
		startActivity(intent);
		getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	}

	private void transitionToPrivacyPolicy() {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_POLICY_URI));
		startActivity(intent);
		getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	}

	private void transitionToUserFeedbackActivity() {
		Intent intent = new Intent(getActivity(), UserFeedbackActivity.class);
		startActivity(intent);
		getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		getActivity().finish();
	}

	private void transitionToUserInfoActivity() {

		// Create intent to go back to the TripMapActivity
		Intent intent = new Intent(getActivity(), UserInfoActivity.class);
		intent.putExtra(UserInfoActivity.EXTRA_PREVIOUS_ACTIVITY, UserInfoActivity.EXTRA_FRAGMENT_SETTINGS);

		// Exit this activity
		startActivity(intent);
		getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		getActivity().finish();
	}
}
