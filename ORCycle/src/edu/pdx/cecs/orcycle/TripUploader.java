/**
 *  ORcycle, Copyright 2014, 2015, PSU Transportation, Technology, and People Lab.
 *
 *  ORcycle 2.2.0 has introduced new app features: safety focus with new buttons
 *  to report safety issues and crashes (new questionnaires), expanded trip
 *  questionnaire (adding questions besides trip purpose), app utilization
 *  reminders, app tutorial, and updated font and color schemes.
 *
 *  @author Bryan.Blanc <bryanpblanc@gmail.com>    (code)
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
 *************************************************************************************
 *
 *	 Cycle Altanta, Copyright 2012 Georgia Institute of Technology
 *                                    Atlanta, GA. USA
 *
 *   @author Christopher Le Dantec <ledantec@gatech.edu>
 *   @author Anhong Guo <guoanhong15@gmail.com>
 *
 *   Updated/Modified for Atlanta's app deployment. Based on the
 *   CycleTracks codebase for SFCTA.
 *
 *   CycleTracks, Copyright 2009,2010 San Francisco County Transportation Authority
 *                                    San Francisco, CA, USA
 *
 * 	 @author Billy Charlton <billy.charlton@sfcta.org>
 *
 *   This file is part of CycleTracks.
 *
 *   CycleTracks is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   CycleTracks is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with CycleTracks.  If not, see <http://www.gnu.org/licenses/>.
 */

package edu.pdx.cecs.orcycle;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.zip.GZIPOutputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

public class TripUploader extends AsyncTask<Long, Integer, Boolean> {

	private static final String MODULE_TAG = "TripUploader";
	public static final int kSaveProtocolVersion = 3;
	private static final String POST_URL = "http://orcycle2.cecs.pdx.edu/post/";

	// Saving protocol version 3
	public static final String TRIP_COORDS_TIME = "r";
	public static final String TRIP_COORDS_LAT = "l";
	public static final String TRIP_COORDS_LON = "n";
	public static final String TRIP_COORDS_ALT = "a";
	public static final String TRIP_COORDS_SPEED = "s";
	public static final String TRIP_COORDS_HACCURACY = "h";
	public static final String TRIP_COORDS_VACCURACY = "v";
	public static final String TRIP_COORDS_SENSOR_READINGS = "sr";

	public static final String PAUSE_START = "ps";
	public static final String PAUSE_END = "pe";

	public static final String TRIP_COORD_SENSOR_ID = "s_id";
	public static final String TRIP_COORD_SENSOR_TYPE = "s_t";
	public static final String TRIP_COORD_SENSOR_SAMPLES = "s_ns";
	public static final String TRIP_COORD_SENSOR_NUM_VALS = "s_nv";
	public static final String TRIP_COORD_SENSOR_AVG_0 = "s_a0";
	public static final String TRIP_COORD_SENSOR_AVG_1 = "s_a1";
	public static final String TRIP_COORD_SENSOR_AVG_2 = "s_a2";
	public static final String TRIP_COORD_SENSOR_SSD_0 = "s_s0";
	public static final String TRIP_COORD_SENSOR_SSD_1 = "s_s1";
	public static final String TRIP_COORD_SENSOR_SSD_2 = "s_s2";

	public static final String USER_AGE = "age";
	public static final String USER_EMAIL = "email";
	public static final String USER_GENDER = "gender";
	public static final String USER_ZIP_HOME = "homeZIP";
	public static final String USER_ZIP_WORK = "workZIP";
	public static final String USER_ZIP_SCHOOL = "schoolZIP";
	public static final String USER_CYCLING_FREQUENCY = "cyclingFreq";
	public static final String APP_VERSION = "app_version";

	public static final String USER_ETHNICITY = "ethnicity";
	public static final String USER_INCOME = "income";
	public static final String USER_HHSIZE = "hhSize";
	public static final String USER_HHVEHICLES = "hhVehicles";
	public static final String USER_RIDERTYPE = "rider_type";
	public static final String USER_RIDERHISTORY = "rider_history";


	private static final String FID_QUESTION_ID = "question_id";
	private static final String FID_ANSWER_ID = "answer_id";
	private static final String FID_ANSWER_OTHER_TEXT = "other_text";

	private final Context mCtx;
	private final String userId;
	private final DbAdapter mDb;

	private Map<String, Integer> sensorColumn = null;

	public TripUploader(Context ctx, String userId) {
		super();
		this.mCtx = ctx;
		this.userId = userId;
		this.mDb = new DbAdapter(this.mCtx);
	}

	@SuppressLint("SimpleDateFormat")
	private JSONObject getCoordsJSON(long tripId) throws JSONException {

		JSONObject jsonTripCoords = null;
		JSONArray jsonSensorReadings = null;

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		mDb.openReadOnly();

		try {
			Cursor cursorTripCoords = mDb.fetchAllCoordsForTrip(tripId);

			// Build the map between JSON field name and phone db field name:
			Map<String, Integer> fieldMap = new HashMap<String, Integer>();

			fieldMap.put(TRIP_COORDS_TIME,      cursorTripCoords.getColumnIndex(DbAdapter.K_POINT_TIME));
			fieldMap.put(TRIP_COORDS_LAT,       cursorTripCoords.getColumnIndex(DbAdapter.K_POINT_LAT));
			fieldMap.put(TRIP_COORDS_LON,       cursorTripCoords.getColumnIndex(DbAdapter.K_POINT_LGT));
			fieldMap.put(TRIP_COORDS_ALT,       cursorTripCoords.getColumnIndex(DbAdapter.K_POINT_ALT));
			fieldMap.put(TRIP_COORDS_SPEED,     cursorTripCoords.getColumnIndex(DbAdapter.K_POINT_SPEED));
			fieldMap.put(TRIP_COORDS_HACCURACY, cursorTripCoords.getColumnIndex(DbAdapter.K_POINT_ACC));
			fieldMap.put(TRIP_COORDS_VACCURACY, cursorTripCoords.getColumnIndex(DbAdapter.K_POINT_ACC));

			// Build JSON objects for each coordinate:
			jsonTripCoords = new JSONObject();
			double coordTime;
			while (!cursorTripCoords.isAfterLast()) {

				// *****************
				// * Get coordinates
				// *****************

				coordTime = cursorTripCoords.getDouble(fieldMap.get(TRIP_COORDS_TIME));

				JSONObject jsonCoord = new JSONObject();

				jsonCoord.put(TRIP_COORDS_TIME,      df.format(coordTime));
				jsonCoord.put(TRIP_COORDS_LAT,       cursorTripCoords.getDouble(fieldMap.get(TRIP_COORDS_LAT)) / 1E6);
				jsonCoord.put(TRIP_COORDS_LON,       cursorTripCoords.getDouble(fieldMap.get(TRIP_COORDS_LON)) / 1E6);
				jsonCoord.put(TRIP_COORDS_ALT,       cursorTripCoords.getDouble(fieldMap.get(TRIP_COORDS_ALT)));
				jsonCoord.put(TRIP_COORDS_SPEED,     cursorTripCoords.getDouble(fieldMap.get(TRIP_COORDS_SPEED)));
				jsonCoord.put(TRIP_COORDS_HACCURACY, cursorTripCoords.getDouble(fieldMap.get(TRIP_COORDS_HACCURACY)));
				jsonCoord.put(TRIP_COORDS_VACCURACY, cursorTripCoords.getDouble(fieldMap.get(TRIP_COORDS_VACCURACY)));

				// **********************************************************
				// * Get all sensor readings corresponding to this time index
				// **********************************************************

				jsonSensorReadings = getJsonSensorReadings(coordTime);

				if ((null != jsonSensorReadings) && (jsonSensorReadings.length() > 0)) {
					jsonCoord.put(TRIP_COORDS_SENSOR_READINGS, jsonSensorReadings);
				}

				// ****************************************************
				// * Insert sensor readings into jSON coordinate object
				// ****************************************************

				jsonTripCoords.put(jsonCoord.getString("r"), jsonCoord);

				// move to next coordinate
				cursorTripCoords.moveToNext();
			}
			cursorTripCoords.close();
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		finally {
			mDb.close();
		}
		return jsonTripCoords;
	}

	/**
	 * Get all sensor readings corresponding to this time index
	 * @return
	 */
	private JSONArray getJsonSensorReadings(double coordTime) {
		JSONArray jsonSensorReadings = null;
		JSONObject jsonSensorReading;
		int numVals;
		Cursor cursorSV = null;

		try {
			if (null != (cursorSV = mDb.fetchSensorValues(coordTime))) {

				// Collect sensor readings into a json object
				jsonSensorReadings = new JSONArray();

				// Construct a map between cursor column index
				if (null == sensorColumn) {
					sensorColumn = new HashMap<String, Integer>();
					sensorColumn.put(TRIP_COORD_SENSOR_ID,       cursorSV.getColumnIndex(DbAdapter.K_SENSOR_ID));
					sensorColumn.put(TRIP_COORD_SENSOR_TYPE,     cursorSV.getColumnIndex(DbAdapter.K_SENSOR_TYPE));
					sensorColumn.put(TRIP_COORD_SENSOR_SAMPLES,  cursorSV.getColumnIndex(DbAdapter.K_SENSOR_SAMPLES));
					sensorColumn.put(TRIP_COORD_SENSOR_NUM_VALS, cursorSV.getColumnIndex(DbAdapter.K_SENSOR_NUM_VALS));
					sensorColumn.put(TRIP_COORD_SENSOR_AVG_0,    cursorSV.getColumnIndex(DbAdapter.K_SENSOR_AVG_0));
					sensorColumn.put(TRIP_COORD_SENSOR_AVG_1,    cursorSV.getColumnIndex(DbAdapter.K_SENSOR_AVG_1));
					sensorColumn.put(TRIP_COORD_SENSOR_AVG_2,    cursorSV.getColumnIndex(DbAdapter.K_SENSOR_AVG_2));
					sensorColumn.put(TRIP_COORD_SENSOR_SSD_0,    cursorSV.getColumnIndex(DbAdapter.K_SENSOR_SSD_0));
					sensorColumn.put(TRIP_COORD_SENSOR_SSD_1,    cursorSV.getColumnIndex(DbAdapter.K_SENSOR_SSD_1));
					sensorColumn.put(TRIP_COORD_SENSOR_SSD_2,    cursorSV.getColumnIndex(DbAdapter.K_SENSOR_SSD_2));
				}

				while (!cursorSV.isAfterLast()) {

					jsonSensorReading = new JSONObject();
					jsonSensorReading.put(TRIP_COORD_SENSOR_ID,      cursorSV.getString(sensorColumn.get(TRIP_COORD_SENSOR_ID)));
					jsonSensorReading.put(TRIP_COORD_SENSOR_TYPE,    cursorSV.getInt   (sensorColumn.get(TRIP_COORD_SENSOR_TYPE)));
					jsonSensorReading.put(TRIP_COORD_SENSOR_SAMPLES, cursorSV.getInt   (sensorColumn.get(TRIP_COORD_SENSOR_SAMPLES)));

					numVals = cursorSV.getInt(sensorColumn.get(TRIP_COORD_SENSOR_NUM_VALS));

					switch(numVals) {
					case 1:
						jsonSensorReading.put(TRIP_COORD_SENSOR_AVG_0, cursorSV.getDouble(sensorColumn.get(TRIP_COORD_SENSOR_AVG_0)));
						jsonSensorReading.put(TRIP_COORD_SENSOR_SSD_0, cursorSV.getDouble(sensorColumn.get(TRIP_COORD_SENSOR_SSD_0)));
						break;
					case 3:
						jsonSensorReading.put(TRIP_COORD_SENSOR_AVG_0, cursorSV.getDouble(sensorColumn.get(TRIP_COORD_SENSOR_AVG_0)));
						jsonSensorReading.put(TRIP_COORD_SENSOR_AVG_1, cursorSV.getDouble(sensorColumn.get(TRIP_COORD_SENSOR_AVG_1)));
						jsonSensorReading.put(TRIP_COORD_SENSOR_AVG_2, cursorSV.getDouble(sensorColumn.get(TRIP_COORD_SENSOR_AVG_2)));
						jsonSensorReading.put(TRIP_COORD_SENSOR_SSD_0, cursorSV.getDouble(sensorColumn.get(TRIP_COORD_SENSOR_SSD_0)));
						jsonSensorReading.put(TRIP_COORD_SENSOR_SSD_1, cursorSV.getDouble(sensorColumn.get(TRIP_COORD_SENSOR_SSD_1)));
						jsonSensorReading.put(TRIP_COORD_SENSOR_SSD_2, cursorSV.getDouble(sensorColumn.get(TRIP_COORD_SENSOR_SSD_2)));
						break;
					}

					jsonSensorReadings.put(jsonSensorReading);
					cursorSV.moveToNext();
				}
			}
		}
		catch (Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		finally {
			if (null != cursorSV) {
				cursorSV.close();
			}
		}
		return jsonSensorReadings;
	}

	@SuppressLint("SimpleDateFormat")
	private JSONArray getPausesJSON(long tripId) throws JSONException {

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		mDb.openReadOnly();

		Cursor pausesCursor = mDb.fetchPauses(tripId);

		// Build the map between JSON fieldname and phone db fieldname:
		Map<String, Integer> fieldMap = new HashMap<String, Integer>();
		fieldMap.put(PAUSE_START, pausesCursor.getColumnIndex(DbAdapter.K_PAUSE_START_TIME));
		fieldMap.put(PAUSE_END, pausesCursor.getColumnIndex(DbAdapter.K_PAUSE_END_TIME));

		// Build JSON objects for each coordinate:
		JSONArray jsonPauses = new JSONArray();
		while (!pausesCursor.isAfterLast()) {

			JSONObject json = new JSONObject();

			try {
				json.put(PAUSE_START, df.format(pausesCursor.getDouble(fieldMap.get(PAUSE_START))));
				json.put(PAUSE_END, df.format(pausesCursor.getDouble(fieldMap.get(PAUSE_END))));

				jsonPauses.put(json);
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
			pausesCursor.moveToNext();
		}
		pausesCursor.close();
		mDb.close();
		return jsonPauses;
	}

	private JSONArray getTripResponsesJSON(long tripId) throws JSONException {

		// Create a JSON array to hold all of the answers
		JSONArray jsonAnswers = new JSONArray();

		mDb.openReadOnly();
		try {
			Cursor answers = mDb.fetchTripAnswers(tripId);

			int questionColumn = answers.getColumnIndex(DbAdapter.K_ANSWER_QUESTION_ID);
			int answerColumn = answers.getColumnIndex(DbAdapter.K_ANSWER_ANSWER_ID);
			int otherText = answers.getColumnIndex(DbAdapter.K_ANSWER_OTHER_TEXT);
			String text;

			// Cycle thru the database entries
			while (!answers.isAfterLast()) {

				// For each row, construct a JSON object
				JSONObject json = new JSONObject();

				try {
					// Place values into the JSON object
					json.put(FID_QUESTION_ID, answers.getInt(questionColumn));
					json.put(FID_ANSWER_ID, answers.getInt(answerColumn));

					if (null != (text = answers.getString(otherText))) {
						text = text.trim();
						if (!text.equals("")) {
							json.put(FID_ANSWER_OTHER_TEXT, text);
						}
					}
					// Place JSON objects into the JSON array
					jsonAnswers.put(json);
				}
				catch(Exception ex) {
					Log.e(MODULE_TAG, ex.getMessage());
				}
				// Move to next row
				answers.moveToNext();
			}
			answers.close();
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		finally {
			mDb.close();
		}
		return jsonAnswers;
	}

	@SuppressLint("SimpleDateFormat")
	private Vector<String> getTripData(long tripId) {
		Vector<String> tripData = new Vector<String>();
		mDb.openReadOnly();
		Cursor tripCursor = mDb.fetchTrip(tripId);

		String note = tripCursor.getString(tripCursor.getColumnIndex(DbAdapter.K_TRIP_NOTE));
		String purpose = tripCursor.getString(tripCursor.getColumnIndex(DbAdapter.K_TRIP_PURP));
		Double startTime = tripCursor.getDouble(tripCursor.getColumnIndex(DbAdapter.K_TRIP_START));
		Double endTime = tripCursor.getDouble(tripCursor.getColumnIndex(DbAdapter.K_TRIP_END));
		tripCursor.close();
		mDb.close();

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		tripData.add(note);
		tripData.add(purpose);
		tripData.add(df.format(startTime));
		tripData.add(df.format(endTime));

		return tripData;
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

	private String getPostData(long tripId) throws JSONException {
		JSONObject coords = getCoordsJSON(tripId);
		JSONArray pauses = getPausesJSON(tripId);
		//JSONObject user = getUserJSON();
		JSONArray tripResponses = getTripResponsesJSON(tripId);
		String deviceId = userId;
		Vector<String> tripData = getTripData(tripId);
		String notes = tripData.get(0);
		String purpose = tripData.get(1);
		String startTime = tripData.get(2);

		String codedPostData =
				"purpose=" + purpose +
				"&tripid=" + String.valueOf(tripId) +
				// "&user=" + user.toString() +
				"&notes=" + notes +
				"&coords=" + coords.toString() +
				"&pauses=" + pauses.toString() +
				"&version=" + String.valueOf(kSaveProtocolVersion) +
				"&start=" + startTime +
				"&device=" + deviceId +
				"&tripResponses=" + tripResponses.toString();
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

	boolean uploadOneTrip(long currentTripId) {
		boolean result = false;

		byte[] postBodyDataZipped;

		String postBodyData;
		try {
			postBodyData = getPostData(currentTripId);
		} catch (JSONException ex) {
			ex.printStackTrace();
			return result;
		}

		HttpClient client = new DefaultHttpClient();
		HttpPost postRequest = new HttpPost(POST_URL);

		try {
			// Zip Upload!!!
			postBodyDataZipped = compress(postBodyData);
			postRequest.setHeader("Cycleatl-Protocol-Version", "3");
			postRequest.setHeader("Content-Encoding", "gzip");
			postRequest.setHeader("Content-Type", "application/vnd.cycleatl.trip-v3+form");
			postRequest.setEntity(new ByteArrayEntity(postBodyDataZipped));

			HttpResponse response = client.execute(postRequest);
			String responseString = convertStreamToString(response.getEntity().getContent());

			JSONObject responseData = new JSONObject(responseString);

			String responseStatus = responseData.getString("status");

			if (responseStatus.equals("success")) {
				mDb.open();
				mDb.updateTripStatus(currentTripId, TripData.STATUS_SENT);
				mDb.close();
				result = true;
			}
			else {
				Log.e(MODULE_TAG, "Server response status: " + responseStatus);
			}
		} catch (IllegalStateException ex) {
			ex.printStackTrace();
			return false;
		} catch (IOException ex) {
			ex.printStackTrace();
			return false;
		} catch (JSONException ex) {
			ex.printStackTrace();
			return false;
		}
		return result;
	}

	@Override
	protected Boolean doInBackground(Long... tripid) {
		// First, send the trip user asked for:
		Boolean result = true;
		if (tripid.length != 0) {
			result = uploadOneTrip(tripid[0]);
		}

		// Then, automatically try and send previously-completed trips
		// that were not sent successfully.
		Vector<Long> unsentTrips = new Vector<Long>();

		mDb.openReadOnly();
		Cursor cur = mDb.fetchUnsentTrips();
		if (cur != null && cur.getCount() > 0) {
			// pd.setMessage("Sent. You have previously unsent trips; submitting those now.");
			while (!cur.isAfterLast()) {
				unsentTrips.add(Long.valueOf(cur.getLong(0)));
				cur.moveToNext();
			}
			cur.close();
		}
		mDb.close();

		for (Long trip : unsentTrips) {
			result &= uploadOneTrip(trip);
		}
		return result;
	}

	@Override
	protected void onPreExecute() {
		Toast.makeText(mCtx.getApplicationContext(),
				"Submitting. Thanks for using ORcycle!",
				Toast.LENGTH_LONG).show();
	}

	private SavedTripsAdapter mSavedTripsAdapter;

	public SavedTripsAdapter setSavedTripsAdapter(
			SavedTripsAdapter mSavedTripsAdapter) {
		this.mSavedTripsAdapter = mSavedTripsAdapter;
		return mSavedTripsAdapter;
	}

	private FragmentSavedTripsSection fragmentSavedTripsSection;

	public FragmentSavedTripsSection setFragmentSavedTripsSection(
			FragmentSavedTripsSection fragmentSavedTripsSection) {
		this.fragmentSavedTripsSection = fragmentSavedTripsSection;
		return fragmentSavedTripsSection;
	}

	private ListView listSavedTrips;

	public ListView setListView(ListView listSavedTrips) {
		this.listSavedTrips = listSavedTrips;
		return listSavedTrips;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		try {
			if (mSavedTripsAdapter != null) {
				mSavedTripsAdapter.notifyDataSetChanged();
			}

			if (fragmentSavedTripsSection != null) {
				listSavedTrips.invalidate();
				fragmentSavedTripsSection.populateTripList(listSavedTrips);
			}
			if (result) {
				Toast.makeText(mCtx.getApplicationContext(),
						"Trip uploaded successfully.", Toast.LENGTH_SHORT)
						.show();
			} else {
				Toast.makeText(
						mCtx.getApplicationContext(),
						"ORcycle couldn't upload the trip, and will retry when your next trip is completed.",
						Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {
			// Just don't toast if the view has gone out of context
		}
	}
}
