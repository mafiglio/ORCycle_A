package edu.pdx.cecs.orcycle;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

public class UserFeedbackUploader extends AsyncTask<Long, Integer, Boolean> {

	private static final String MODULE_TAG = "UserFeedbackUploader";
	private static final int kSaveProtocolVersion4 = 4;
	private static final String boundary = "cycle*******notedata*******atlanta";
	private static final String fieldSep = "--cycle*******notedata*******atlanta\r\n";

	public static final String USER_FEEDBACK = "feedback";
	private final String userId;

	private Context mCtx = null;

	public UserFeedbackUploader(Context ctx, String userId) {
		super();
		this.mCtx = ctx;
		this.userId = userId;
	}

	/**
	 *
	 * @return
	 * @throws JSONException
	 */
	private JSONObject getUserJSON() throws JSONException {

		String userFeedback = null;
		JSONObject userJson = null;

		SharedPreferences settings = mCtx.getSharedPreferences(UserFeedbackActivity.PREFS_USER_FEEDBACK_UPLOAD, Context.MODE_PRIVATE);
		Map<String, ?> prefs = settings.getAll();

		for (Entry<String, ?> p : prefs.entrySet()) {
			int key = Integer.parseInt(p.getKey());
			// CharSequence value = (CharSequence) p.getValue();

			switch (key) {

			case UserFeedbackActivity.PREF_FEEDBACK:
				userFeedback = (String) p.getValue();
				break;
			}
		}

		if (null != userFeedback) {
			if (null != (userJson = new JSONObject())) {
				userJson.put(USER_FEEDBACK, userFeedback);
			}
		}
		return userJson;
	}

	public String getAppVersion() {
		String versionName = "";
		int versionCode = 0;

		try {
			PackageInfo pInfo = mCtx.getPackageManager().getPackageInfo(
					mCtx.getPackageName(), 0);
			versionName = pInfo.versionName;
			versionCode = pInfo.versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}

		String systemVersion = Build.VERSION.RELEASE;

		String manufacturer = Build.MANUFACTURER;
		String model = Build.MODEL;
		if (model.startsWith(manufacturer)) {
			return versionName + " (" + versionCode + ") on Android "
					+ systemVersion + " " + capitalize(model);
		} else {
			return versionName + " (" + versionCode + ") on Android "
					+ systemVersion + " " + capitalize(manufacturer) + " "
					+ model;
		}
	}

	private String capitalize(String s) {
		if (s == null || s.length() == 0) {
			return "";
		}
		char first = s.charAt(0);
		if (Character.isUpperCase(first)) {
			return s;
		} else {
			return Character.toUpperCase(first) + s.substring(1);
		}
	}

	boolean uploadUserInfoV4() {
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

			DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
			JSONObject jsonUser;
			if (null != (jsonUser = getUserJSON())) {
				try {
					String deviceId = userId;

					dos.writeBytes(fieldSep + ContentField("user") + jsonUser.toString() + "\r\n");
					dos.writeBytes(fieldSep + ContentField("version") + String.valueOf(kSaveProtocolVersion4) + "\r\n");
					dos.writeBytes(fieldSep + ContentField("device") + deviceId + "\r\n");
					dos.writeBytes(fieldSep);
					dos.flush();
				}
				catch(Exception ex) {
					Log.e(MODULE_TAG, ex.getMessage());
					return false;
				}
				finally {
					dos.close();
				}
				int serverResponseCode = conn.getResponseCode();
				String serverResponseMessage = conn.getResponseMessage();
				// JSONObject responseData = new JSONObject(serverResponseMessage);
				Log.v("Jason", "HTTP Response is : " + serverResponseMessage + ": "
						+ serverResponseCode);
				if (serverResponseCode == 201 || serverResponseCode == 202) {
					// TODO: Record somehow that data was uploaded successfully
					result = true;
				}
			}
			else {
				result = false;
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

	private String ContentField(String type) {
		return "Content-Disposition: form-data; name=\"" + type + "\"\r\n\r\n";
	}

	@Override
	protected Boolean doInBackground(Long... dummy) {

		Boolean result = false;

		try {
			result = uploadUserInfoV4();
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}

		return result;
	}

	@Override
	protected void onPreExecute() {
		Toast.makeText(mCtx.getApplicationContext(),
				"Submitting feedback. Thanks for using ORcycle!",
				Toast.LENGTH_LONG).show();
	}

	@Override
	protected void onPostExecute(Boolean result) {
		try {
			if (result) {
				Toast.makeText(mCtx.getApplicationContext(),
						"User feedback uploaded successfully.", Toast.LENGTH_SHORT)
						.show();
			} else {
				Toast.makeText(
						mCtx.getApplicationContext(),
						"ORcycle couldn't upload the information.",
						Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {
			// Just don't toast if the view has gone out of context
		}
	}
}
