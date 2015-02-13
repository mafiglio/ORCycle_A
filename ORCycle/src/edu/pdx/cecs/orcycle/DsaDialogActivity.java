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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

public class DsaDialogActivity extends Activity {

	private static final String MODULE_TAG = "DsaDialogActivity";

	public static final String EXTRA_TITLE = "title";
	public static final String EXTRA_MESSAGE = "message";
	public static final String EXTRA_DIALOG_ID = "dialog_id";
	public static final String EXTRA_POSITIVE_TEXT = "positive_text";
	public static final String EXTRA_POSITIVE_ID = "positive_id";
	public static final String EXTRA_NEUTRAL_TEXT = "neutral_text";
	public static final String EXTRA_NEUTRAL_ID = "neutral_id";
	public static final String EXTRA_NEGATIVE_TEXT = "negative_text";
	public static final String EXTRA_NEGATIVE_ID = "negative_id";
	public static final String EXTRA_IS_CHECKED = "is_checked";
	public static final int EXTRA_UNSET_ID = -1;
	public static final String EXTRA_IMAGE_ID = "image_id";

	private int dialogId;
	private int positiveId;
	private int neutralId;
	private int negativeId;
	private int imageId;
	private Button btnPositive;
	private Button btnNeutral;
	private Button btnNegative;
	private CheckBox cbDontShowAgain;
	private ImageView imageView;

	private final CheckboxChangeListener onCheckboxChangeListener = new CheckboxChangeListener();
	private final class CheckboxChangeListener implements CompoundButton.OnCheckedChangeListener {

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		}
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			setContentView(R.layout.activity_dsa_dialog);

			Bundle extras = (null == savedInstanceState) ? getIntent().getExtras() : savedInstanceState;

			String title = extras.getString(EXTRA_TITLE);
			String message = extras.getString(EXTRA_MESSAGE);
			dialogId = extras.getInt(EXTRA_DIALOG_ID);
			String positiveText = extras.getString(EXTRA_POSITIVE_TEXT);
			positiveId = extras.getInt(EXTRA_POSITIVE_ID, EXTRA_UNSET_ID);
			String neutralText = extras.getString(EXTRA_NEUTRAL_ID);
			neutralId = extras.getInt(EXTRA_NEUTRAL_ID, EXTRA_UNSET_ID);
			String negativeText = extras.getString(EXTRA_NEGATIVE_TEXT);
			negativeId = extras.getInt(EXTRA_NEGATIVE_ID, EXTRA_UNSET_ID);
			imageId = extras.getInt(EXTRA_IMAGE_ID, EXTRA_UNSET_ID);

			// Set title
			TextView tvTitle = (TextView) findViewById(R.id.tvDsaTitle);
			if (null == title) {
				tvTitle.setVisibility(View.GONE);
			}
			else {
				tvTitle.setVisibility(View.GONE);
				//tvTitle.setText(title);
				this.setTitle(title);
			}

			// Set image
			imageView = (ImageView) findViewById(R.id.imageDsa);
			if (EXTRA_UNSET_ID == imageId) {
				imageView.setVisibility(View.GONE);
			}
			else {
				imageView.setImageResource(imageId);
				imageView.setVisibility(View.VISIBLE);
			}

			// Set message
			TextView tvMessage = (TextView) findViewById(R.id.tvDsaMessage);
			if (null == message) {
				tvMessage.setVisibility(View.GONE);
			}
			else {
				tvMessage.setText(message);
			}

			// Set positive button
			btnPositive = (Button) findViewById(R.id.btnDsaPositive);
			if ((null != positiveText) && (EXTRA_UNSET_ID != positiveId)) {
				btnPositive.setText(positiveText);
				btnPositive.setOnClickListener(new DsaButton_OnClickListener(positiveId));
			}
			else {
				btnPositive.setVisibility(View.GONE);
			}

			// Set neutral button
			btnNeutral = (Button) findViewById(R.id.btnDsaNeutral);
			if ((null != neutralText) && (EXTRA_UNSET_ID != neutralId)) {
				btnNeutral.setText(neutralText);
				btnNeutral.setOnClickListener(new DsaButton_OnClickListener(neutralId));
			}
			else {
				btnNeutral.setVisibility(View.GONE);
			}

			// Set negative button
			btnNegative = (Button) findViewById(R.id.btnDsaNegative);
			if ((null != negativeText) && (EXTRA_UNSET_ID != negativeId)) {
				btnNegative.setText(negativeText);
				btnNegative.setOnClickListener(new DsaButton_OnClickListener(negativeId));
			}
			else {
				btnNegative.setVisibility(View.GONE);
			}

			cbDontShowAgain = (CheckBox) findViewById(R.id.chkDsaCheckbox);
			cbDontShowAgain.setOnCheckedChangeListener(onCheckboxChangeListener);

		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}



	}
	// *********************************************************************************
	// *                              Button Handlers
	// *********************************************************************************

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
				transitionTo(buttonId);
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	private void transitionTo(int buttonPressed) {
		Intent intent = new Intent(this, TabsConfig.class);
		intent.putExtra(TabsConfig.EXTRA_SHOW_FRAGMENT, TabsConfig.FRAG_INDEX_MAIN_INPUT);
		intent.putExtra(TabsConfig.EXTRA_DSA_ACTIVITY, TabsConfig.EXTRA_DSA_ACTIVITY);
		intent.putExtra(TabsConfig.EXTRA_DSA_DIALOG_ID, dialogId);
		intent.putExtra(TabsConfig.EXTRA_DSA_BUTTON_PRESSED, buttonPressed);
		intent.putExtra(EXTRA_IS_CHECKED, cbDontShowAgain.isChecked());
		startActivity(intent);
		finish();
	}
}
