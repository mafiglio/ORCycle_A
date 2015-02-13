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

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class Uploader extends AsyncTask<Long, Integer, Boolean> {

	private static final String MODULE_TAG = "Uploader";
	private static final int kSaveNoteProtocolVersion = 4;
	private static final String boundary = "cycle*******notedata*******atlanta";

	private final Context mCtx;
	private final String userId;
	private final DbAdapter mDb;

	public Uploader(Context ctx, String userId) {
		super();
		this.mCtx = ctx;
		this.userId = userId;
		this.mDb = new DbAdapter(this.mCtx);
	}

	private static final String contentFieldPrefix = "--cycle*******notedata*******atlanta\r\n";

	private String makeContentField(String name, String value) {
		return contentFieldPrefix + "Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n"
				+ value + "\r\n";
	}

	private boolean uploadOneSegment(long currentNoteId) {
		boolean result = false;
		final String postUrl = mCtx.getResources().getString(R.string.post_url);

		try {
			URL url = new URL(postUrl);

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoInput(true); // Allow Inputs
			conn.setDoOutput(true); // Allow Outputs
			conn.setUseCaches(false); // Don't use a Cached Copy
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("ENCTYPE", "multipart/form-data");
			conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
			conn.setRequestProperty("Cycleatl-Protocol-Version", "4");

			SegmentData segmentData = SegmentData.fetchSegment(mCtx, currentNoteId);
			JSONObject json = segmentData.getJSON();
			String deviceId = userId;

			DataOutputStream stream = new DataOutputStream(conn.getOutputStream());
			stream.writeBytes(makeContentField("ratesegment", json.toString()));
			stream.writeBytes(makeContentField("version", String.valueOf(kSaveNoteProtocolVersion)));
			stream.writeBytes(makeContentField("device", deviceId));
			stream.writeBytes(contentFieldPrefix);
			stream.flush();
			stream.close();

			int serverResponseCode = conn.getResponseCode();
			String serverResponseMessage = conn.getResponseMessage();
			Log.v(MODULE_TAG, "HTTP Response is : " + serverResponseMessage + ": " + serverResponseCode);
			if (serverResponseCode == 201 || serverResponseCode == 202) {
				segmentData.updateSegmentStatus(SegmentData.STATUS_SENT);
				result = true;
			}
		} catch (IllegalStateException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		}
		return result;
	}

	@Override
	protected Boolean doInBackground(Long... noteid) {
		// First, send the note user asked for:
		Boolean result = true;
		if (noteid.length != 0) {
			result = uploadOneSegment(noteid[0]);
		}

		// Then, automatically try and send previously-completed notes
		// that were not sent successfully.
		return result && SendAllSegments();
	}

	private boolean SendAllSegments() {

		boolean result = true;

		Vector<Long> unsentSegmentIds = new Vector<Long>();

		mDb.openReadOnly();
		try {
			Cursor cursor = mDb.fetchUnsentSegmentIds();
			try {
				if (cursor != null && cursor.getCount() > 0) {
					// pd.setMessage("Sent. You have previously unsent notes; submitting those now.");
					while (!cursor.isAfterLast()) {
						unsentSegmentIds.add(Long.valueOf(cursor.getLong(0)));
						cursor.moveToNext();
					}
				}
			}
			finally {
				cursor.close();
			}
		}
		finally {
			mDb.close();
			}

		for (Long segmentId : unsentSegmentIds) {
			result &= uploadOneSegment(segmentId);
		}
		return result;

	}

	@Override
	protected void onPreExecute() {
		Toast.makeText(mCtx.getApplicationContext(),
				"Submitting. Thanks for using ORCycle!",
				Toast.LENGTH_LONG).show();
	}

	@Override
	protected void onPostExecute(Boolean result) {
	}
}
