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
import android.provider.Settings.System;
import android.util.Log;
import android.widget.Toast;

public class Uploader extends AsyncTask<Long, Integer, Boolean> {

	public static final String MODULE_TAG = "Uploader";
	public static final int kSaveNoteProtocolVersion = 4;

	private final Context mCtx;
	DbAdapter mDb;

	String twoHyphens = "--";
	String boundary = "cycle*******notedata*******atlanta";
	String lineEnd = "\r\n";

	public Uploader(Context ctx) {
		super();
		this.mCtx = ctx;
		this.mDb = new DbAdapter(this.mCtx);
	}

	public String getDeviceId() {
		String androidId = System.getString(this.mCtx.getContentResolver(), System.ANDROID_ID);
		String androidBase = "androidDeviceId-";

		if (androidId == null) { // This happens when running in the Emulator
			final String emulatorId = "android-RunningAsTestingDeleteMe";
			return emulatorId;
		}
		String deviceId = androidBase.concat(androidId);

		// Fix String Length
		int a = deviceId.length();
		if (a < 32) {
			for (int i = 0; i < 32 - a; i++) {
				deviceId = deviceId.concat("0");
			}
		} else {
			deviceId = deviceId.substring(0, 32);
		}

		return deviceId;
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
			String deviceId = getDeviceId();

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
