package edu.pdx.cecs.orcycle;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class ReminderReceiver extends BroadcastReceiver {

	public static final String ACTION_REMIND_USER = "edu.pdx.cecs.orcycle.ACTION_REMIND_USER";

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Toast.makeText(context, "Alarm occurring...", Toast.LENGTH_LONG).show();
	}
}
