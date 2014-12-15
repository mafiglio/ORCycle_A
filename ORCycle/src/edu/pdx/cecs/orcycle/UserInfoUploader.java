package edu.pdx.cecs.orcycle;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPOutputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class UserInfoUploader extends AsyncTask<Long, Integer, Boolean> {

	private static final String MODULE_TAG = "UserInfoUploader";
	private static final int kSaveProtocolVersion3 = 3;
	private static final int kSaveProtocolVersion4 = 4;
	private static final String boundary = "cycle*******notedata*******atlanta";
	private static final String lineEnd = "\r\n";
	private static final String fieldSep = "--cycle*******notedata*******atlanta\r\n";

	private static final String FID_QUESTION_ID = "question_id";
	private static final String FID_ANSWER_ID = "answer_id";
	private static final String FID_ANSWER_OTHER_TEXT = "other_text";

	public static final String USER_EMAIL = "email";
	public static final String USER_INSTALLED = "installed";
	public static final String USER_DEVICE_MODEL = "deviceModel";
	public static final String USER_APP_VERSION = "app_version";

	private String email;
	private String installed; // DateTime
	private String deviceModel;
	private String appVersion;

	private Context mCtx = null;
	private String userId = null;

	public UserInfoUploader(Context ctx, String userId) {
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

		JSONObject userJson = new JSONObject();

		userJson.put(USER_EMAIL, email);
		userJson.put(USER_INSTALLED, installed);
		userJson.put(USER_DEVICE_MODEL, deviceModel);
		userJson.put(USER_APP_VERSION, appVersion);

		return userJson;
	}

	/**
	 *
	 * @return
	 * @throws JSONException
	 */
	private JSONArray getUserResponsesJSON() throws JSONException {

		// Create a JSON array to hold all of the answers
		JSONArray jsonAnswers = new JSONArray();

		SharedPreferences settings = mCtx.getSharedPreferences(UserInfoActivity.PREFS_USER_INFO_UPLOAD, Context.MODE_PRIVATE);
		Map<String, ?> prefs = settings.getAll();

		String riderTypeOther = null;
		String occupationOther = null;
		String bikeTypeOther = null;
		String genderOther = null;
		String ethnicityOther = null;

		for (Entry<String, ?> p : prefs.entrySet()) {
			int key = Integer.parseInt(p.getKey());
			// CharSequence value = (CharSequence) p.getValue();

			switch (key) {
			case UserInfoActivity.PREF_RIDER_TYPE_OTHER: riderTypeOther  = (String) p.getValue(); break;
			case UserInfoActivity.PREF_OCCUPATION_OTHER: occupationOther = (String) p.getValue(); break;
			case UserInfoActivity.PREF_BIKE_TYPE_OTHER:  bikeTypeOther   = (String) p.getValue(); break;
			case UserInfoActivity.PREF_GENDER_OTHER:     genderOther     = (String) p.getValue(); break;
			case UserInfoActivity.PREF_ETHNICITY_OTHER:  ethnicityOther  = (String) p.getValue(); break;
			}
		}

		for (Entry<String, ?> p : prefs.entrySet()) {
			int key = Integer.parseInt(p.getKey());
			// CharSequence value = (CharSequence) p.getValue();

			switch (key) {

			case UserInfoActivity.PREF_EMAIL:
				email = (String) p.getValue();
				break;

			case UserInfoActivity.PREF_RIDER_ABILITY:
				putInt(jsonAnswers, DbQuestions.USER_INFO_RIDER_ABILITY,
						DbAnswers.userInfoRiderAbility, -1, null, p);
				break;

			case UserInfoActivity.PREF_RIDER_TYPE:
				putInt(jsonAnswers, DbQuestions.USER_INFO_RIDER_TYPE,
						DbAnswers.userInfoRiderType,
						DbAnswers.userInfoRiderTypeOther, riderTypeOther, p);
				break;

			case UserInfoActivity.PREF_CYCLE_FREQUENCY:
				putInt(jsonAnswers, DbQuestions.USER_INFO_CYCLING_FREQ,
						DbAnswers.userInfoCyclingFreq, -1, null, p);
				break;

			case UserInfoActivity.PREF_CYCLE_WEATHER:
				putInt(jsonAnswers, DbQuestions.USER_INFO_CYCLING_WEATHER,
						DbAnswers.userInfoCyclingWeather, -1, null, p);
				break;

			case UserInfoActivity.PREF_NUM_BIKES:
				putInt(jsonAnswers, DbQuestions.USER_INFO_NUM_BIKES,
						DbAnswers.userInfoNumBikes, -1, null, p);
				break;

			case UserInfoActivity.PREF_BIKE_TYPES:
				putMultiInt(jsonAnswers, DbQuestions.USER_INFO_BIKE_TYPES,
						DbAnswers.userInfoBikeTypes,
						DbAnswers.userInfoBikeTypeOther, bikeTypeOther, p);
				break;

			case UserInfoActivity.PREF_OCCUPATION:
				putInt(jsonAnswers, DbQuestions.USER_INFO_OCCUPATION,
						DbAnswers.userInfoOccupation,
						DbAnswers.userInfoOccupationOther, occupationOther, p);
				break;

			case UserInfoActivity.PREF_AGE:
				putInt(jsonAnswers, DbQuestions.USER_INFO_AGE,
						DbAnswers.userInfoAge, -1, null, p);
				break;

			case UserInfoActivity.PREF_GENDER:
				putInt(jsonAnswers, DbQuestions.USER_INFO_GENDER,
						DbAnswers.userInfoGender,
						DbAnswers.userInfoGenderOther, genderOther, p);
				break;

			case UserInfoActivity.PREF_VEHICLES:
				putInt(jsonAnswers, DbQuestions.USER_INFO_VEHICLES,
						DbAnswers.userInfoHHVehicles, -1, null, p);
				break;

			case UserInfoActivity.PREF_WORKERS:
				putInt(jsonAnswers, DbQuestions.USER_INFO_WORKERS,
						DbAnswers.userInfoHHWorkers, -1, null, p);
				break;

			case UserInfoActivity.PREF_ETHNICITY:
				putInt(jsonAnswers, DbQuestions.USER_INFO_ETHNICITY,
						DbAnswers.userInfoEthnicity,
						DbAnswers.userInfoEthnicityOther, ethnicityOther, p);
				break;

			case UserInfoActivity.PREF_INCOME:
				putInt(jsonAnswers, DbQuestions.USER_INFO_INCOME,
						DbAnswers.userInfoIncome, -1, null, p);
				break;

			case UserInfoActivity.PREF_INSTALLED:
				installed = (String) p.getValue();
				break;

			case UserInfoActivity.PREF_DEVICE_MODEL:
				deviceModel = (String) p.getValue();
				break;

			case UserInfoActivity.PREF_APP_VERSION:
				appVersion = (String) p.getValue();
				break;
			}
		}

		return jsonAnswers;
	}

	/**
	 *
	 * @param jsonAnswers
	 * @param questionId
	 * @param answers
	 * @param entry
	 */
	private void putInt(JSONArray jsonAnswers, int questionId, int[] answers, int otherId, String otherText, Entry<String, ?> entry) {

		try {
			int index = ((Integer) entry.getValue()).intValue();
			if (index >= 0) {
				int answer = answers[index];
				JSONObject json = new JSONObject();

				// Place values into the JSON object
				json.put(FID_QUESTION_ID, questionId);
				json.put(FID_ANSWER_ID, answer);

				// Assure that whenever other is selected, there is an entry for other text
				if ((-1 != otherId) && (answers[index] == otherId) ) {
					if (null == otherText) {
						json.put(FID_ANSWER_OTHER_TEXT, "");
					}
					else {
						json.put(FID_ANSWER_OTHER_TEXT, otherText);
					}
				}

				// Place JSON objects into the JSON array
				jsonAnswers.put(json);
			}
		}
		catch (Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	/**
	 *
	 * @param jsonAnswers
	 * @param questionId
	 * @param answers
	 * @param entry
	 */
	private void putMultiInt(JSONArray jsonAnswers, int questionId, int[] answers, int otherId, String otherText, Entry<String, ?> entry) {

		try {
			String text = (String) entry.getValue();
			String[] selections = text.split(",");
			int index;

			for (String selection : selections) {
				try {
					index = Integer.parseInt(selection);
					if (index >= 0 && index < answers.length) {
						int answer = answers[index];
						JSONObject json = new JSONObject();

						// Place values into the JSON object
						json.put(FID_QUESTION_ID, questionId);
						json.put(FID_ANSWER_ID, answer);
						// Assure that whenever other is selected, there is an entry for other text
						if ((-1 != otherId) && (answers[index] == otherId) ) {
							if (null == otherText) {
								json.put(FID_ANSWER_OTHER_TEXT, "");
							}
							else {
								json.put(FID_ANSWER_OTHER_TEXT, otherText);
							}
						}

						// Place JSON objects into the JSON array
						jsonAnswers.put(json);
					} else {
						Log.e(MODULE_TAG, "Index " + index + " is out of bounds.");
					}
				} catch (Exception ex) {
					Log.e(MODULE_TAG, ex.getMessage());
				}
			}
		}
		catch (Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	private void putString(JSONArray jsonAnswers, int questionId, int[] answers, Entry<String, ?> entry) {

		try {
			JSONObject json = new JSONObject();
			String text = (String) entry.getValue();
			int answer_id = answers[0];

			if (null != text) {
				text = text.trim();
				if (!text.equals("")) {
					// Place values into the JSON object
					json.put(FID_QUESTION_ID, questionId);
					json.put(FID_ANSWER_ID, answer_id);
					json.put(FID_ANSWER_OTHER_TEXT, text);
					// Place JSON objects into the JSON array
					jsonAnswers.put(json);
				}
			}
		}
		catch (Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}


	private String getPostDataV3() throws JSONException {

		JSONArray userResponses = getUserResponsesJSON();
		JSONObject user = getUserJSON();
		String deviceId = userId;

		String codedPostData =
				"&user=" + user.toString() +
				"&userRespones=" + userResponses.toString() +
				"&version=" + String.valueOf(kSaveProtocolVersion3) +
				"&device=" + deviceId;

		return codedPostData;
	}

	private static String convertStreamToString(InputStream is) {
		/*
		 * To convert the InputStream to String we use the
		 * BufferedReader.readLine() method. We iterate until the BufferedReader
		 * return null which means there's no more data to read. Each line will
		 * appended to a StringBuilder and returned as String.
		 */
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	public static byte[] compress(String string) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream(string.length());
		GZIPOutputStream gos = new GZIPOutputStream(os);
		gos.write(string.getBytes());
		gos.close();
		byte[] compressed = os.toByteArray();
		os.close();
		return compressed;
	}

	boolean uploadUserInfoV3() {
		boolean result = false;

		byte[] postBodyDataZipped;

		String postBodyData;
		try {
			postBodyData = getPostDataV3();
		} catch (JSONException e) {
			e.printStackTrace();
			return result;
		}

		HttpClient client = new DefaultHttpClient();
		final String postUrl = mCtx.getResources().getString(R.string.post_url);
		HttpPost postRequest = new HttpPost(postUrl);

		try {
			// Zip Upload!!!
			Log.v(MODULE_TAG, "postBodyData: " + postBodyData.toString());
			Log.v(MODULE_TAG, "postBodyData Length: " + postBodyData.length());

			postBodyDataZipped = compress(postBodyData);

			Log.v(MODULE_TAG, "postBodyDataZipped: " + postBodyDataZipped);
			Log.v(MODULE_TAG, "postBodyDataZipped  Length: " + String.valueOf(postBodyDataZipped.length));

			Log.v(MODULE_TAG, "Initializing HTTP POST request to " + postUrl
					+ " of size " + String.valueOf(postBodyDataZipped.length)
					+ " orig size " + postBodyData.length());

			postRequest.setHeader("Cycleatl-Protocol-Version", "3");
			postRequest.setHeader("Content-Encoding", "gzip");
			postRequest.setHeader("Content-Type",
					"application/vnd.cycleatl.trip-v3+form");
			// postRequest.setHeader("Content-Length",String.valueOf(postBodyDataZipped.length));

			postRequest.setEntity(new ByteArrayEntity(postBodyDataZipped));

			HttpResponse response = client.execute(postRequest);
			String responseString = convertStreamToString(response.getEntity().getContent());
			// Log.v("httpResponse", responseString);
			JSONObject responseData = new JSONObject(responseString);
			if (responseData.getString("status").equals("success")) {
				// TODO: Record somehow that data was uploaded successfully
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

	boolean uploadUserInfoV4() {
		boolean result = false;
		final String postUrl = mCtx.getResources().getString(R.string.post_url);

		try {
			JSONArray userResponses = getUserResponsesJSON();

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
					dos.writeBytes(fieldSep + ContentField("userResponses") + userResponses.toString() + "\r\n");
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
			//result = uploadUserInfoV4();
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
				"Submitting. Thanks for using ORcycle!",
				Toast.LENGTH_LONG).show();
	}

	@Override
	protected void onPostExecute(Boolean result) {
		try {
			if (result) {
				Toast.makeText(mCtx.getApplicationContext(),
						"User information uploaded successfully.", Toast.LENGTH_SHORT)
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

