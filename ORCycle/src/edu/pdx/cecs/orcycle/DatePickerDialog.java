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
import android.widget.DatePicker;
import android.widget.TextView;

public class DatePickerDialog {

	private static final String MODULE_TAG = "DatePickerDialog";

	private final TextView tvCrashDate;
	private final AlertDialog dialog;

	public DatePickerDialog(Activity activity, TextView tvCrashDate) {

		this.tvCrashDate = tvCrashDate;

        // Initializiation
        LayoutInflater inflater = activity.getLayoutInflater();
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        View customView = inflater.inflate(R.layout.dialog_datepicker, null);
        dialogBuilder.setView(customView);
        final Calendar now = Calendar.getInstance();

        final DatePicker datePicker = (DatePicker) customView.findViewById(R.id.dialog_datepicker);
        final TextView dateTextView = (TextView) customView.findViewById(R.id.dialog_dateview);
        final SimpleDateFormat dialogFormatter = new SimpleDateFormat("EEEE, MM/dd/yyyy", Locale.US);
        final SimpleDateFormat resultFormatter = new SimpleDateFormat("MM/dd/yyyy", Locale.US);

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
        	String initDate = tvCrashDate.getText().toString();
        	if (!initDate.equals("")) {
	            cal.setTime(resultFormatter.parse(initDate));
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
        dialogBuilder.setNegativeButton("Today", new Dialog_OnNegativeButtonClicked(now, resultFormatter));

        dialogBuilder.setPositiveButton("OK", new Dialog_OnPositiveButtonClicked(datePicker, resultFormatter));

        dialog = dialogBuilder.create();
        // Initialize datepicker in dialog atepicker
        datePicker.init(year, month, day, new DatePicker_OnDateChangedListener(dateTextView, dialog, now,
					dialogFormatter));
	}

	public void show() {
        dialog.show();
	}

	private final class Dialog_OnPositiveButtonClicked implements
			DialogInterface.OnClickListener {
		private final DatePicker datePicker;
		private final SimpleDateFormat resultFormatter;

		private Dialog_OnPositiveButtonClicked(DatePicker datePicker,
				SimpleDateFormat dateViewFormatter) {
			this.datePicker = datePicker;
			this.resultFormatter = dateViewFormatter;
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
		    Calendar cal = Calendar.getInstance();
		    cal.set(
		        datePicker.getYear(),
		        datePicker.getMonth(),
		        datePicker.getDayOfMonth()
		    );
		    DatePickerDialog.this.tvCrashDate.setText(resultFormatter.format(cal.getTime()));
		    dialog.dismiss();
		}
	}

	private final class Dialog_OnNegativeButtonClicked implements
			DialogInterface.OnClickListener {
		private final Calendar now;
		private final SimpleDateFormat resultFormatter;

		private Dialog_OnNegativeButtonClicked(Calendar now, SimpleDateFormat formatter) {
			this.now = now;
			this.resultFormatter = formatter;
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			DatePickerDialog.this.tvCrashDate.setText(resultFormatter.format(now.getTime()));
		    dialog.dismiss();
		}
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

}
