package edu.pdx.cecs.orcycle;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.util.Log;

public class SpeedMonitor {

	private static final String MODULE_TAG = "SpeedMonitor";
	private static final long ONE_MINUTE_MS = 60000;
	private static final long TWO_MINUTES_MS = 120000;
	private static final long THREE_MINUTES_MS = 180000; // For faster debugging, set this value to 18000
	private static final float METERS_PER_SECOND_TO_MILES_PER_HOUR = 2.2369f;
	private static final String MESSAGE_TOO_SLOW = "You are going slower than 3 mph, if you are not biking anymore, please stop recording the trip. Thanks!";
	private static final String MESSAGE_TOO_FAST = "You are going faster than 20 mph, if you are not biking anymore, please stop recording the trip. Thanks!"";


	private final LinkedList<ListItem> speeds = new LinkedList<ListItem>();
	private final SoundPool soundpool = new SoundPool(1, AudioManager.STREAM_NOTIFICATION, 0);
	private final Handler speedCheckHandler = new Handler();
	private final Context context;
	private final int bikebell;

	private Timer speedCheckTimer;

	// *********************************************************************************
	// *
	// *********************************************************************************

	/**
	 * TimerTask for calculating the average speed
	 */
	private final class CalculateAverageSpeedTask extends TimerTask {
		@Override
		public void run() {
			try {
				if (null == speedCheckHandler) {
					Log.e(MODULE_TAG, "speedCheckHandler == null");
				}
				else {
					speedCheckHandler.post(new Runnable() {
						public void run() {
							try {
								speedCheck();
							}
							catch(Exception ex) {
								Log.e(MODULE_TAG, ex.getMessage());
							}
						}
					});
				}
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	/**
	 * Item for storing an instance of time and speed
	 */
	private final class ListItem {
		long time;
		float speed;
		public ListItem(long time, float speed) {
			this.time = time;
			this.speed = speed;
		}
	}

	// *********************************************************************************
	// *
	// *********************************************************************************

	/**
	 * Constructor
	 * @param context
	 */
	public SpeedMonitor(Context context) {
		this.context = context;
		this.bikebell = soundpool.load(context, R.raw.bikebell, 1);
	}

	/**
	 * Start monitoring of speed
	 */
	public void start() {
		try {
			cancel();
			speeds.clear();
			startTimer();
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	/**
	 * Stop monitoring of speed
	 */
	public void cancel() {
		if (null != speedCheckTimer) {
			speedCheckTimer.cancel();
			speedCheckTimer = null;
		}
	}

	/**
	 * Stores a time and speed measurement
	 * @param currentTimeMillis
	 * @param speed
	 */
	public void recordSpeed(long currentTimeMillis, float speed) {
		speeds.addFirst(new ListItem(currentTimeMillis, speed));
	}

	// *********************************************************************************
	// *
	// *********************************************************************************

	/**
	 * Starts timer for checking speed list
	 */
	private void startTimer() {
		speedCheckTimer = new Timer();
		speedCheckTimer.schedule(new CalculateAverageSpeedTask(), THREE_MINUTES_MS, ONE_MINUTE_MS);
	}

	/**
	 * Checks speed running average and if outside of range, alerts the user
	 */
	private void speedCheck() {

		float [] runningAverage = new float[1];

		if (getAverage(THREE_MINUTES_MS, runningAverage)) {

			float mph = runningAverage[0] * METERS_PER_SECOND_TO_MILES_PER_HOUR;

			// Note: if we get here then there is 3 minutes
			// or more data in speed buffer
			if (mph < 3.0f) {
				notifyUser(MESSAGE_TOO_SLOW);
				truncateBufferTo(0);
			}
			else if (mph > 20.0f) {
				notifyUser(MESSAGE_TOO_FAST);
				truncateBufferTo(0);
			}
			else {
				// The timer will schedule the next check in a minute, so
				// remove all but last two minutes of data from buffer
				truncateBufferTo(TWO_MINUTES_MS);
			}
		}
	}

	/**
	 * Get the average speed over the specified time period ()
	 * @param timePeriod time to take average over (specified in milliseconds)
	 * @param runningAverage returns average running speed (expressed in meters/second)
	 * @return true if calculation made, false otherwise
	 */
	private boolean getAverage(long timePeriod, float [] runningAverage) {

			try {
				long currentTime = System.currentTimeMillis();

				// Determine if buffer contains at least 3 minutes of buffered speed data
				if(speeds.size() == 0) {
					return false;
				}
				else if ((currentTime - speeds.getLast().time) < timePeriod) {
					return false;
				}

				// Calculate average speed
				Iterator<ListItem> entries = speeds.iterator();
				ListItem entry;
				float speedSummation = 0.0f;

				while (entries.hasNext()) {
					entry = entries.next();
					speedSummation += entry.speed;
				}

				runningAverage[0] = speedSummation / speeds.size();
				return true;
			}
			catch(Exception ex) {
				Log.v(MODULE_TAG, ex.getMessage());
			}
		return false;
	}

	/**
	 * Removes entries from list if recorded more than timePeriod
	 * ago.  If timePeriod <= 0, all entries are removed.
	 * @param timePeriod
	 */
	private void truncateBufferTo(long timePeriod) {

		try {
			if (timePeriod > 0) {

				// Get current time
				long currentTime = System.currentTimeMillis();

				// Iterate over speeds
				Iterator<ListItem> entries = speeds.iterator();
				while (entries.hasNext()) {
					// Remove speed if recorded more than timePeriod ago
					if ((currentTime - entries.next().time) > timePeriod) {
						entries.remove();
					}
				}
			}
			else {
				speeds.clear();
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	/**
	 * Plays bell sound and presents message to user via toast
	 * @param message
	 */
	private void notifyUser(String message) {
		try {
			soundpool.play(bikebell, 1.0f, 1.0f, 1, 0, 1.0f);

			CustomToast toast = new CustomToast(context, message, 30);
			toast.show();
		}
		catch(Exception ex) {
			Log.v(MODULE_TAG, ex.getMessage());
		}
	}

}
