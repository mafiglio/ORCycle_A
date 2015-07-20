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

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

public class NoteUploader extends AsyncTask<Long, Integer, Boolean> {

	private byte[] imageData;

	private static final String MODULE_TAG = "NoteUploader";
	private static final int kSaveNoteProtocolVersion = 4;

	private static final String NOTE_TRIP_ID = "p";
	private static final String NOTE_RECORDED = "r";
	private static final String NOTE_LAT = "l";
	private static final String NOTE_LGT = "n";
	private static final String NOTE_HACC = "h";
	private static final String NOTE_VACC = "v";
	private static final String NOTE_ALT = "a";
	private static final String NOTE_SPEED = "s";
	private static final String NOTE_TYPE = "t";
	private static final String NOTE_DETAILS = "d";
	private static final String NOTE_IMGURL = "i";
	private static final String NOTE_REPORT_DATE = "reportDate";
	private static final String NOTE_EMAIL_SENT = "e";

	private static final String FID_QUESTION_ID = "question_id";
	private static final String FID_ANSWER_ID = "answer_id";
	private static final String FID_ANSWER_OTHER_TEXT = "other_text";

	private static final String twoHyphens = "--";
	private static final String boundary = "cycle*******notedata*******atlanta";
	private static final String lineEnd = "\r\n";
	private static final String notesep = "--cycle*******notedata*******atlanta\r\n";

	private static final Map<Long, Boolean> pendingUploads = new HashMap<Long, Boolean>();

	private final SimpleDateFormat reportDateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

	private final Context mCtx;
	private final String userId;
	private final DbAdapter mDb;

	public NoteUploader(Context ctx, String userId) {
		super();
		this.mCtx = ctx;
		this.userId = userId;
		this.mDb = new DbAdapter(this.mCtx);
	}

	public static void setPending(long noteId, boolean value) {
		Log.v(MODULE_TAG, "setPending: [" + String.valueOf(noteId) + "] = " + String.valueOf(value));
		if (value == false) {
			if (pendingUploads.containsKey(noteId)) {
				pendingUploads.remove(noteId);
			}
		}
		else {
			pendingUploads.put(noteId, true);
		}
	}

	public static boolean isPending(long noteId) {

		boolean value = false;
		if (pendingUploads.containsKey(noteId)) {
			value = pendingUploads.get(noteId);
		}
		return value;
	}

	private JSONObject getNoteJSON(long noteId) throws JSONException {
		try {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String noteImageFileName;

			mDb.openReadOnly();
			try {
				Cursor noteCursor = mDb.fetchNote(noteId);

				try {
					Map<String, Integer> fieldMap = new HashMap<String, Integer>();
					fieldMap.put(NOTE_TRIP_ID, noteCursor.getColumnIndex(DbAdapter.K_NOTE_TRIP_ID));
					fieldMap.put(NOTE_RECORDED, noteCursor.getColumnIndex(DbAdapter.K_NOTE_RECORDED));
					fieldMap.put(NOTE_LAT, noteCursor.getColumnIndex(DbAdapter.K_NOTE_LAT));
					fieldMap.put(NOTE_LGT, noteCursor.getColumnIndex(DbAdapter.K_NOTE_LGT));
					fieldMap.put(NOTE_HACC, noteCursor.getColumnIndex(DbAdapter.K_NOTE_ACC));
					fieldMap.put(NOTE_VACC, noteCursor.getColumnIndex(DbAdapter.K_NOTE_ACC));
					fieldMap.put(NOTE_ALT, noteCursor.getColumnIndex(DbAdapter.K_NOTE_ALT));
					fieldMap.put(NOTE_SPEED, noteCursor.getColumnIndex(DbAdapter.K_NOTE_SPEED));
					fieldMap.put(NOTE_DETAILS, noteCursor.getColumnIndex(DbAdapter.K_NOTE_DETAILS));
					fieldMap.put(NOTE_IMGURL, noteCursor.getColumnIndex(DbAdapter.K_NOTE_IMGURL));
					fieldMap.put(NOTE_REPORT_DATE, noteCursor.getColumnIndex(DbAdapter.K_NOTE_REPORT_DATE));
					fieldMap.put(NOTE_EMAIL_SENT, noteCursor.getColumnIndex(DbAdapter.K_NOTE_EMAIL_SENT));

					JSONObject note = new JSONObject();

					note.put(NOTE_TRIP_ID, noteCursor.getInt(fieldMap.get(NOTE_TRIP_ID)));
					note.put(NOTE_RECORDED, df.format(noteCursor.getDouble(fieldMap.get(NOTE_RECORDED))));
					note.put(NOTE_LAT, noteCursor.getDouble(fieldMap.get(NOTE_LAT)) / 1E6);
					note.put(NOTE_LGT, noteCursor.getDouble(fieldMap.get(NOTE_LGT)) / 1E6);
					note.put(NOTE_HACC, noteCursor.getDouble(fieldMap.get(NOTE_HACC)));
					note.put(NOTE_VACC, noteCursor.getDouble(fieldMap.get(NOTE_VACC)));
					note.put(NOTE_ALT, noteCursor.getDouble(fieldMap.get(NOTE_ALT)));
					note.put(NOTE_SPEED, noteCursor.getDouble(fieldMap.get(NOTE_SPEED)));
					note.put(NOTE_DETAILS, noteCursor.getString(fieldMap.get(NOTE_DETAILS)));
					note.put(NOTE_IMGURL, noteImageFileName = noteCursor.getString(fieldMap.get(NOTE_IMGURL)));

					long reportDate = noteCursor.getLong(fieldMap.get(NOTE_REPORT_DATE));
					String formattedDate = reportDateFormatter.format(reportDate);
					note.put(NOTE_REPORT_DATE, formattedDate);

					boolean emailSent = (noteCursor.getInt(fieldMap.get(NOTE_EMAIL_SENT)) == 1 ? true : false);
					note.put(NOTE_EMAIL_SENT, emailSent);

					if ((null != noteImageFileName) && (!noteImageFileName.equals("")))
						imageData = mDb.getNoteImageData(noteId);
					else
						imageData = null;

					return note;
				}
				catch(Exception ex) {
					Log.e(MODULE_TAG, ex.getMessage());
				}
				finally {
					noteCursor.close();
				}
				return null;
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
			finally {
				mDb.close();
			}
			return null;
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		return null;
	}

	private JSONArray getNoteResponsesJSON(long noteId) throws JSONException {

		// Create a JSON array to hold all of the answers
		JSONArray jsonAnswers = new JSONArray();

		mDb.openReadOnly();
		try {
			Cursor answers = mDb.fetchNoteAnswers(noteId);

			int questionId = answers.getColumnIndex(DbAdapter.K_NOTE_ANSWER_QUESTION_ID);
			int answerId = answers.getColumnIndex(DbAdapter.K_NOTE_ANSWER_ANSWER_ID);
			int otherText = answers.getColumnIndex(DbAdapter.K_NOTE_ANSWER_OTHER_TEXT);
			String text;

			// Cycle thru the database entries
			while (!answers.isAfterLast()) {

				// For each row, construct a JSON object
				JSONObject json = new JSONObject();

				try {
					// Place values into the JSON object
					json.put(FID_QUESTION_ID, answers.getInt(questionId));
					json.put(FID_ANSWER_ID, answers.getInt(answerId));

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

	boolean uploadOneNote(long noteId) {
		boolean result = false;
		final String postUrl = mCtx.getResources().getString(R.string.post_url);

		try {
			JSONArray jsonNoteResponses = getNoteResponsesJSON(noteId);

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
			JSONObject jsonNote;
			if (null != (jsonNote = getNoteJSON(noteId))) {
				try {
					String deviceId = userId;

					dos.writeBytes(notesep + ContentField("note") + jsonNote.toString() + "\r\n");
					dos.writeBytes(notesep + ContentField("version") + String.valueOf(kSaveNoteProtocolVersion) + "\r\n");
					dos.writeBytes(notesep + ContentField("device") + deviceId + "\r\n");
					dos.writeBytes(notesep + ContentField("noteResponses") + jsonNoteResponses.toString() + "\r\n");

					if (null != imageData) {
						dos.writeBytes(notesep
								+ "Content-Disposition: form-data; name=\"file\"; filename=\""
								+ deviceId + ".jpg\"\r\n"
								+ "Content-Type: image/jpeg\r\n\r\n");
						dos.write(imageData);
						dos.writeBytes("\r\n");
					}

					dos.writeBytes(notesep);
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
					mDb.open();
					mDb.updateNoteStatus(noteId, NoteData.STATUS_SENT);
					mDb.close();
					result = true;
				}
			}
			else {
				result = false;
			}
		} catch (IllegalStateException ex) {
			Log.e(MODULE_TAG, ex.getMessage());
			return false;
		} catch (IOException ex) {
			Log.e(MODULE_TAG, ex.getMessage());
			return false;
		} catch (JSONException ex) {
			Log.e(MODULE_TAG, ex.getMessage());
			return false;
		}
		finally {
			NoteUploader.setPending(noteId, false);
		}
		return result;
	}

	private String ContentField(String type) {
		return "Content-Disposition: form-data; name=\"" + type + "\"\r\n\r\n";
	}

	@Override
	protected Boolean doInBackground(Long... noteIds) {
		// First, send the note user asked for:
		Boolean result = true;
		if (noteIds.length != 0) {
			result = uploadOneNote(noteIds[0]);
		}

		// Then, automatically try and send previously-completed notes
		// that were not sent successfully.
		Vector<Long> unsentNotes = new Vector<Long>();

		mDb.openReadOnly();
		Cursor cur = mDb.fetchUnsentNotes();
		if (cur != null && cur.getCount() > 0) {
			// pd.setMessage("Sent. You have previously unsent notes; submitting those now.");
			while (!cur.isAfterLast()) {
				unsentNotes.add(Long.valueOf(cur.getLong(0)));
				cur.moveToNext();
			}
			cur.close();
		}
		mDb.close();

		for (Long note : unsentNotes) {
			result &= uploadOneNote(note);
		}
		return result;
	}

	@Override
	protected void onPreExecute() {
		Toast.makeText(mCtx.getApplicationContext(),
				"Submitting. Thanks for using ORCycle!",
				Toast.LENGTH_LONG).show();
	}

	private SavedNotesAdapter mSavedNotesAdapter;

	public SavedNotesAdapter setSavedNotesAdapter(
			SavedNotesAdapter mSavedNotesAdapter) {
		this.mSavedNotesAdapter = mSavedNotesAdapter;
		return mSavedNotesAdapter;
	}

	private FragmentSavedNotesSection fragmentSavedNotesSection;

	public FragmentSavedNotesSection setFragmentSavedNotesSection(
			FragmentSavedNotesSection fragmentSavedNotesSection) {
		this.fragmentSavedNotesSection = fragmentSavedNotesSection;
		return fragmentSavedNotesSection;
	}

	private ListView listSavedNotes;

	public ListView setListView(ListView listSavedNotes) {
		this.listSavedNotes = listSavedNotes;
		return listSavedNotes;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		try {
			if (mSavedNotesAdapter != null) {
				mSavedNotesAdapter.notifyDataSetChanged();
			}

			if (fragmentSavedNotesSection != null) {
				listSavedNotes.invalidate();
				fragmentSavedNotesSection.populateNoteList(listSavedNotes);
			}

			if (result) {
				Toast.makeText(mCtx.getApplicationContext(),
						"Note uploaded successfully.", Toast.LENGTH_SHORT)
						.show();
			} else {
				Toast.makeText(
						mCtx.getApplicationContext(),
						"ORCycle couldn't upload the note, and will retry when your next note is completed.",
						Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {
			// Just don't toast if the view has gone out of context
		}
	}
}
