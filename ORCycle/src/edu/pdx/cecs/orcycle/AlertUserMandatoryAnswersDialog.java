package edu.pdx.cecs.orcycle;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class AlertUserMandatoryAnswersDialog {

	private final AlertDialog alertDialog;

	public AlertUserMandatoryAnswersDialog(Context context) {

		final AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(context.getResources().getString(R.string.aumad_answer_required_questions))
				.setCancelable(true)
				.setPositiveButton(context.getResources().getString(R.string.aumad_ok),
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog, final int id) {
								dialog.cancel();
							}
						});
		 alertDialog = builder.create();

	}
	public void show() {
		alertDialog.show();
	}
}
