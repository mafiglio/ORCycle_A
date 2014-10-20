package edu.pdx.cecs.orcycle;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

public class DsaDialog {

	private final AlertDialog alertDialog;

	public DsaDialog(Activity activity, int titleId, int textId,
			CompoundButton.OnCheckedChangeListener onCheckedChangeListener,
			int positiveTextId, DialogInterface.OnClickListener positiveListener,
			int neutralTextId, DialogInterface.OnClickListener neutralListener,
			int negativeTextId, DialogInterface.OnClickListener negativeListener
			) {

		String title;
		String text;
		String positiveText = null;
		String neutralText = null;
		String negativeText = null;

		if (-1 == titleId) {
			title = "";
		}
		else {
			title = activity.getResources().getString(titleId);
		}

		if (-1 == textId) {
			text = "";
		}
		else {
			text = activity.getResources().getString(textId);
		}

		if (null != positiveListener) {
			positiveText = activity.getResources().getString(positiveTextId);
		}
		if (null != neutralListener) {
			neutralText = activity.getResources().getString(neutralTextId);
		}
		if (null != negativeListener) {
			negativeText = activity.getResources().getString(negativeTextId);
		}

		alertDialog = createDialog(activity, title, text, onCheckedChangeListener,
									positiveText, positiveListener,
									neutralText, neutralListener,
									negativeText, negativeListener);
	}

	public DsaDialog(Activity activity, int titleId, int textId, String parameter,
		CompoundButton.OnCheckedChangeListener onCheckedChangeListener,
		int positiveTextId, DialogInterface.OnClickListener positiveListener,
		int neutralTextId, DialogInterface.OnClickListener neutralListener,
		int negativeTextId, DialogInterface.OnClickListener negativeListener
		) {

		String title;
		String text;
		String positiveText = null;
		String neutralText = null;
		String negativeText = null;

		if (-1 == titleId) {
			title = "";
		}
		else {
			title = activity.getResources().getString(titleId, parameter);
		}

		if (-1 == textId) {
			text = "";
		}
		else {
			text = activity.getResources().getString(textId, parameter);
		}

		if (null != positiveListener) {
			positiveText = activity.getResources().getString(positiveTextId);
		}

		if (null != neutralListener) {
			neutralText = activity.getResources().getString(neutralTextId);
		}

		if (null != negativeListener) {
			negativeText = activity.getResources().getString(negativeTextId);
		}

		alertDialog = createDialog(activity, title, text, onCheckedChangeListener,
									positiveText, positiveListener,
									neutralText, neutralListener,
									negativeText, negativeListener);
	}

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
		textbox.setText(text);

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
}
