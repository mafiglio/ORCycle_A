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
