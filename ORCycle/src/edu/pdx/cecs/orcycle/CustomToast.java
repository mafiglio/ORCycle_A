package edu.pdx.cecs.orcycle;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class CustomToast {

	private final Context context;
	private final String message;
	private final int numShows;

	public CustomToast(Context context, String message, int numShows) {
		this.context = context;
		this.message = message;
		this.numShows = numShows;
	}

	public void show() {
		for (int i = 0; i < numShows; ++i) {
			makeCustomToast().show();
		}
	}

	private Toast makeCustomToast() {

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		// View layout = inflater.inflate(R.layout.custom_toast, (ViewGroup) findViewById(R.id.toast_layout_root));
		View layout = inflater.inflate(R.layout.custom_toast, null);
		TextView text = (TextView) layout.findViewById(R.id.custom_toast_text);
		text.setText(message);
		Toast toast = new Toast(context);
		toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, 0);
		toast.setDuration(Toast.LENGTH_LONG);
		toast.setView(layout);
		return toast;
	}
}
