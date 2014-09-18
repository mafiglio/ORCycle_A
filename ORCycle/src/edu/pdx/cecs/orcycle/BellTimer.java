package edu.pdx.cecs.orcycle;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;

public class BellTimer {

	private double startTime;
	private Context context;

	// Bike bell variables
	private static int BELL_FIRST_INTERVAL = 20;
	private static int BELL_NEXT_INTERVAL = 5;
	private Timer timer;
	private SoundPool soundpool;
	private int bikebell;

	/**
	 *  Handler for calling ringBell on the UI thread
	 */
	final Handler ringBellHandler = new Handler();

	/**
	 *  Runnable to ring bell (to be executed on UI thread)
	 */
	final Runnable ringBellRunnable = new Runnable() {
		public void run() {
			ringBell();
		}
	};

	/**
	 * Plays the bikebell sound and posts notification
	 */
	private void ringBell() {
		soundpool.play(bikebell, 1.0f, 1.0f, 1, 0, 1.0f);
		MyNotifiers.setNotification(context, startTime);
	}

	public void init(Context context) {
		this.context = context;
		soundpool = new SoundPool(1, AudioManager.STREAM_NOTIFICATION, 0);
		bikebell = soundpool.load(context, R.raw.bikebell, 1);
	}

	public void cancel() {
		if (timer != null) {
			timer.cancel();
			timer.purge();
		}
	}

	public void start(double startTime) {
		this.cancel();
		this.startTime = startTime;
		timer = new Timer();
		timer.schedule(new RingBellTask(), BELL_FIRST_INTERVAL * 60000, BELL_NEXT_INTERVAL * 60000);
	}

	private final class RingBellTask extends TimerTask {
		@Override
		public void run() {
			ringBellHandler.post(ringBellRunnable);
		}
	}


}
