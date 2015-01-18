package edu.pdx.cecs.orcycle;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

public class DatePickerDialog {

	private static final String MODULE_TAG = "DatePickerDialog";

	private final Button btnReportDate;
	private final AlertDialog dialog;

	public DatePickerDialog(Activity activity, Button btnReportDate, SimpleDateFormat resultFormatter) {

		this.btnReportDate = btnReportDate;

        // Initializiation
        LayoutInflater inflater = activity.getLayoutInflater();
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        View customView = inflater.inflate(R.layout.dialog_datepicker, null);
        dialogBuilder.setView(customView);
        final Calendar now = Calendar.getInstance();

        final DatePicker datePicker = (DatePicker) customView.findViewById(R.id.dialog_datepicker);
        final TextView dateTextView = (TextView) customView.findViewById(R.id.dialog_dateview);
        final SimpleDateFormat dialogFormatter = new SimpleDateFormat("EEEE, MM/dd/yyyy", Locale.US);

        // Minimum date
        Calendar maxDate = Calendar.getInstance();
        datePicker.setMaxDate(maxDate.getTimeInMillis());

        // View settings
        dialogBuilder.setTitle("Choose a date");

        // Init date to today
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        // Set UI to passed in date
        try {
        	long initDate = (Long) btnReportDate.getTag();
        	if (initDate > 0) {
	            cal.setTimeInMillis(initDate);
		        year = cal.get(Calendar.YEAR);
		        month = cal.get(Calendar.MONTH);
		        day = cal.get(Calendar.DAY_OF_MONTH);
        	}
        } catch (Exception e) {
            Log.e(MODULE_TAG, e.getMessage());
        }

        Calendar dateToDisplay = Calendar.getInstance();
        dateToDisplay.set(year, month, day);
        dateTextView.setText(dialogFormatter.format(dateToDisplay.getTime()));

        // Buttons
        dialogBuilder.setPositiveButton("OK", new PositiveButton_OnClickListener(datePicker, resultFormatter));

        dialogBuilder.setNegativeButton("Today", new NegativeButton_OnClickListener(now, resultFormatter));

        dialog = dialogBuilder.create();

        // Initialize datepicker in dialog atepicker
        datePicker.init(year, month, day, new DatePicker_OnDateChangedListener(dateTextView, dialog, now, dialogFormatter));
	}

	public void show() {
        dialog.show();
	}

	private final class DatePicker_OnDateChangedListener implements
			DatePicker.OnDateChangedListener {

		private final TextView tvDate;
		private final AlertDialog dialog;
		private final Calendar now;
		private final SimpleDateFormat dateViewFormatter;

		private DatePicker_OnDateChangedListener(TextView tvDate,
				AlertDialog dialog, Calendar now,
				SimpleDateFormat dateViewFormatter) {
			this.tvDate = tvDate;
			this.dialog = dialog;
			this.now = now;
			this.dateViewFormatter = dateViewFormatter;
		}

		public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

			Calendar cal = Calendar.getInstance();
			cal.set(year, monthOfYear, dayOfMonth);

			tvDate.setText(dateViewFormatter.format(cal.getTime()));
		}
	}

	private final class PositiveButton_OnClickListener implements
	DialogInterface.OnClickListener {

	private final DatePicker datePicker;
	private final SimpleDateFormat viewFormatter;

	public PositiveButton_OnClickListener(DatePicker datePicker, SimpleDateFormat viewFormatter) {
		this.datePicker = datePicker;
		this.viewFormatter = viewFormatter;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		Calendar cal = Calendar.getInstance();
		cal.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
		DatePickerDialog.this.btnReportDate.setText(viewFormatter.format(cal.getTime()));
		DatePickerDialog.this.btnReportDate.setTag(cal.getTimeInMillis());
		dialog.dismiss();
	}
}

	private final class NegativeButton_OnClickListener implements
			DialogInterface.OnClickListener {
		private final Calendar now;
		private final SimpleDateFormat viewFormatter;

		public NegativeButton_OnClickListener(Calendar now, SimpleDateFormat viewFormatter) {
			this.now = now;
			this.viewFormatter = viewFormatter;
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			DatePickerDialog.this.btnReportDate.setText(viewFormatter.format(now.getTime()));
			DatePickerDialog.this.btnReportDate.setTag(now.getTimeInMillis());
			dialog.dismiss();
		}
	}

}
