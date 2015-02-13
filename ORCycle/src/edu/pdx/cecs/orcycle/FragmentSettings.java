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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class FragmentSettings extends Fragment {

	// Module name
	public static final String MODULE_TAG = "FragmentSettings";

	// Activity results
	public static final int RESULT_USER_INFO_PRESSED = 1;
	public static final int RESULT_GET_STARTED_PRESSED = 2;
	public static final int RESULT_FEEDBACK_PRESSED = 3;
	public static final int RESULT_PRIVACY_POLICY_PRESSED = 4;
	public static final int RESULT_REMINDERS_PRESSED = 5;
	public static final int RESULT_TUTORIAL_PRESSED = 6;
	public static final int RESULT_MAPS_LINK_PRESSED = 7;
	public static final int RESULT_REPORT_HAZARDS_LINK_PRESSED = 8;

	private FragmentSettingsController controller;

	// UI Elements
	private Button btnUserInfo = null;
	private Button btnGetStarted = null;
	private Button btnFeedback = null;
	private Button btnPrivacyPolicy = null;
	private Button btnReminders = null;
	private Button btnTutorial = null;
	private Button btnMapsLink = null;
	private Button btnReportHazardsLink = null;
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
		controller = new FragmentSettingsController(getActivity());

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

			btnReminders = (Button) rootView.findViewById(R.id.btnReminders);
			btnReminders.setOnClickListener(new Reminders_OnClickListener());

			btnTutorial = (Button) rootView.findViewById(R.id.btnTutorial);
			btnTutorial.setOnClickListener(new Tutorial_OnClickListener());

			btnMapsLink = (Button) rootView.findViewById(R.id.btn_ats_maps_link);
			btnMapsLink.setOnClickListener(new MapsLink_OnClickListener());

			btnReportHazardsLink = (Button) rootView.findViewById(R.id.btn_ats_report_hazards_link);
			btnReportHazardsLink.setOnClickListener(new ReportHazardsLink_OnClickListener());

			// Create version string
			StringBuilder version = new StringBuilder();
			version.append(getResources().getString(R.string.about_version));
			version.append(MyApplication.getInstance().getVersionName());
			version.append(getResources().getString(R.string.about_version_code));
			version.append(MyApplication.getInstance().getVersionCode());
			tvVersionCode = (TextView) rootView.findViewById(R.id.as_version_code);
			tvVersionCode.setText(version);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}

		return rootView;
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
				controller.setResult(RESULT_USER_INFO_PRESSED);
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
				//transitionToORcycle();
				controller.setResult(RESULT_GET_STARTED_PRESSED);
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
				controller.setResult(RESULT_FEEDBACK_PRESSED);
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
				controller.setResult(RESULT_PRIVACY_POLICY_PRESSED);
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
	private final class Reminders_OnClickListener implements View.OnClickListener {

		/**
		 * Description: Handles onClick for view
		 */
		public void onClick(View v) {
			try {
				controller.setResult(RESULT_REMINDERS_PRESSED);
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
			finally {
			}
		}
	}

	/**
     * Class: Tutorial_OnClickListener
     *
     * Description: Callback to be invoked when Tutorial button is clicked
     */
	private final class Tutorial_OnClickListener implements View.OnClickListener {

		/**
		 * Description: Handles onClick for view
		 */
		public void onClick(View v) {
			try {
				controller.setResult(RESULT_TUTORIAL_PRESSED);
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
			finally {
			}
		}
	}

	/**
     * Class: MapsLink_OnClickListener
     *
     * Description: Callback to be invoked when mapsLink button is clicked
     */
	private final class MapsLink_OnClickListener implements View.OnClickListener {

		/**
		 * Description: Handles onClick for view
		 */
		public void onClick(View v) {
			try {
				//transitionToORcycle();
				controller.setResult(RESULT_MAPS_LINK_PRESSED);
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
			finally {
			}
		}
	}

	/**
     * Class: ReportHazardsLink_OnClickListener
     *
     * Description: Callback to be invoked when hazardsLink button is clicked
     */
	private final class ReportHazardsLink_OnClickListener implements View.OnClickListener {

		/**
		 * Description: Handles onClick for view
		 */
		public void onClick(View v) {
			try {
				//transitionToORcycle();
				controller.setResult(RESULT_REPORT_HAZARDS_LINK_PRESSED);
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
			finally {
			}
		}
	}

}
