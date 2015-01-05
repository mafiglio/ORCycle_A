package edu.pdx.cecs.orcycle;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;

public class BikeBell extends AsyncTask<Long, Integer, Boolean> {

	private final SoundPool soundpool = new SoundPool(1, AudioManager.STREAM_NOTIFICATION, 0);
	private final int bikebell;

	public BikeBell(Context context) {
		bikebell = soundpool.load(context, R.raw.bikebell, 1);
	}

	/**
	 * Plays the bikebell sound and posts notification
	 */
	public void ring() {
		this.execute(0L);
	}

	@Override
	protected Boolean doInBackground(Long... params) {
		soundpool.play(bikebell, 1.0f, 1.0f, 1, 0, 1.0f);
		return null;
	}
}
