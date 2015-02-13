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
import android.content.DialogInterface;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

public class DsaDialog {

	private final AlertDialog alertDialog;

	public DsaDialog(Activity activity, String title, String text,
		CompoundButton.OnCheckedChangeListener onCheckedChangeListener,
		String positiveText, DialogInterface.OnClickListener positiveListener,
		String neutralText, DialogInterface.OnClickListener neutralListener,
		String negativeText, DialogInterface.OnClickListener negativeListener
		) {

		alertDialog = createDialog(activity, title, text, onCheckedChangeListener,
									positiveText, positiveListener,
									neutralText, neutralListener,
									negativeText, negativeListener);
	}

	private static final AlertDialog createDialog(Activity activity, String title, String text,
			CompoundButton.OnCheckedChangeListener onCheckedChangeListener,
			String positiveText, DialogInterface.OnClickListener positiveListener,
			String neutralText, DialogInterface.OnClickListener neutralListener,
			String negativeText, DialogInterface.OnClickListener negativeListener
			) {

		// Load custom layout for alert dialog
		LayoutInflater inflater = activity.getLayoutInflater();
		View rootView = inflater.inflate(R.layout.dialog_text_checkbox, null);

		// Reference custom layout's textbox and set text value
		TextView textbox = (TextView) rootView.findViewById(R.id.tv_dtc_text);
		textbox.setText(Html.fromHtml(text));

		CheckBox cbDontShowAgain = (CheckBox) rootView.findViewById(R.id.cb_dtc_checkbox);
		if (null != onCheckedChangeListener)
			cbDontShowAgain.setOnCheckedChangeListener(onCheckedChangeListener);

		// Create alert dialog
		final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setView(rootView);

		if (null != title)
			builder.setTitle(title);

		if (null != positiveListener)
			builder.setPositiveButton(positiveText, positiveListener);

		if (null != neutralListener)
			builder.setNeutralButton(neutralText, neutralListener);

		if (null != negativeListener)
			builder.setPositiveButton(negativeText, negativeListener);

		return builder.create();
	}

	public void show() {
		alertDialog.show();
	}

	public void dismiss() {
		alertDialog.dismiss();
	}
}
