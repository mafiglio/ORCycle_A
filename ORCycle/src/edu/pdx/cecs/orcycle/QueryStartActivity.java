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
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class QueryStartActivity extends Activity {

	private static final String MODULE_TAG = "QueryStartActivity";

	public static final String EXTRA_REMINDER_NAME = "REMINDER_NAME";

	private static final int POSITIVE_BUTTON_ID = 0;
	private static final int NEGATIVE_BUTTON_ID = 1;

	/**
     * Callback to be invoked when a dialog button is pressed
     */
	private final class DsaButton_OnClickListener implements View.OnClickListener {

		private final int buttonId;

		/**
		 * Constructor for DsaButton_OnClickListener
		 * @param id
		 */
		public DsaButton_OnClickListener(int id) {
			buttonId = id;
		}

		/**
		 * Description: Handles onClick for view
		 */
		public void onClick(View v) {
			try {
				if (buttonId == POSITIVE_BUTTON_ID) {
					transitionToTabsConfigActivity();
				}
				else {
					finish();
				}
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	private Button btnPositive;
	private Button btnNegative;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			setContentView(R.layout.activity_query_start);

			String reminderName = getIntent().getStringExtra(EXTRA_REMINDER_NAME);
			String title = getResources().getString(R.string.qsa_title, reminderName);
			setTitle(title);

			btnPositive = (Button) this.findViewById(R.id.btn_qsa_positive);
			btnPositive.setOnClickListener(new DsaButton_OnClickListener(POSITIVE_BUTTON_ID));

			btnNegative = (Button) this.findViewById(R.id.btn_qsa_negative);
			btnNegative.setOnClickListener(new DsaButton_OnClickListener(NEGATIVE_BUTTON_ID));
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	// *********************************************************************************
	// *
	// *********************************************************************************

	private void transitionToTabsConfigActivity() {

		Intent intent = new Intent(this, TabsConfig.class);

		intent.putExtra(TabsConfig.EXTRA_SHOW_FRAGMENT, TabsConfig.FRAG_INDEX_MAIN_INPUT);
		startActivity(intent);
		finish();
		overridePendingTransition(android.R.anim.fade_in, R.anim.slide_out_down);
	}
}
