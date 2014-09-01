/**	 Cycle Altanta, Copyright 2012 Georgia Institute of Technology
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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.media.ExifInterface;
import android.util.Log;

/**
 * Simple database access helper class. Defines the basic CRUD operations, and
 * gives the ability to list all trips as well as retrieve or modify a specific
 * trip.
 *
 * This has been improved from the first version of this tutorial through the
 * addition of better error handling and also using returning a Cursor instead
 * of using a collection of inner classes (which is less scalable and not
 * recommended).
 *
 * **This code borrows heavily from Google demo app "Notepad" in the Android
 * SDK**
 */
public class DbAdapter {

    private static final String NOTE_IMAGES_DIR_NAME = "note_images";
	// Database versions
	private static final int DATABASE_VERSION_PAUSES = 22;
	private static final int DATABASE_VERSION_SEGMENTS = 23;
	private static final int DATABASE_VERSION_NOTES_V3 = 25;
	private static final int DATABASE_VERSION_ANSWERS = 26;
	private static final int DATABASE_VERSION_NOTE_ANSWERS = 27;
	private static final int DATABASE_VERSION_NOTE_SEVERITY = 28;
	private static final int DATABASE_VERSION = DATABASE_VERSION_NOTE_SEVERITY;

	// Trips Table columns
	public static final String K_TRIP_ROWID = "_id";
	public static final String K_TRIP_PURP = "purp";
	public static final String K_TRIP_START = "start";
	public static final String K_TRIP_END = "endtime";
	public static final String K_TRIP_FANCYSTART = "fancystart";
	public static final String K_TRIP_FANCYINFO = "fancyinfo";
	public static final String K_TRIP_NOTE = "note";
	public static final String K_TRIP_DISTANCE = "distance";
	public static final String K_TRIP_LATHI = "lathi";
	public static final String K_TRIP_LATLO = "latlo";
	public static final String K_TRIP_LGTHI = "lgthi";
	public static final String K_TRIP_LGTLO = "lgtlo";
	public static final String K_TRIP_STATUS = "status";

	// Coords Table columns
	public static final String K_POINT_ROWID = "_id";
	public static final String K_POINT_TRIP = "trip";
	public static final String K_POINT_TIME = "time";
	public static final String K_POINT_LAT = "lat";
	public static final String K_POINT_LGT = "lgt";
	public static final String K_POINT_ACC = "acc";
	public static final String K_POINT_ALT = "alt";
	public static final String K_POINT_SPEED = "speed";

	// Note Table columns
	public static final String K_NOTE_ROWID = "_id";
	public static final String K_NOTE_TRIP_ID = "tripid";
	public static final String K_NOTE_RECORDED = "noterecorded";
	public static final String K_NOTE_FANCYSTART = "notefancystart";
	public static final String K_NOTE_LAT = "notelat";
	public static final String K_NOTE_LGT = "notelgt";
	public static final String K_NOTE_ACC = "noteacc";
	public static final String K_NOTE_ALT = "notealt";
	public static final String K_NOTE_SPEED = "notespeed";
	public static final String K_NOTE_TYPE = "notetype";
	public static final String K_NOTE_SEVERITY = "noteseverity";
	public static final String K_NOTE_DETAILS = "notedetails";
	public static final String K_NOTE_IMGURL = "noteimageurl";
	public static final String K_NOTE_STATUS = "notestatus";

	// Pauses Table columns
	public static final String K_PAUSE_TRIP_ID = "_id";
	public static final String K_PAUSE_START_TIME = "starttime";
	public static final String K_PAUSE_END_TIME = "endtime";

	// Trip Answers Table columns
	public static final String K_ANSWER_TRIP_ID = "trip_id";
	public static final String K_ANSWER_QUESTION_ID = "question_id";
	public static final String K_ANSWER_ANSWER_ID = "answer_id";
	public static final String K_ANSWER_OTHER_TEXT = "other_text";

	// Note Answers Table columns
	public static final String K_NOTE_ANSWER_NOTE_ID = "note_id";
	public static final String K_NOTE_ANSWER_QUESTION_ID = "question_id";
	public static final String K_NOTE_ANSWER_ANSWER_ID = "answer_id";
	public static final String K_NOTE_ANSWER_OTHER_TEXT = "other_text";

	// Segments Table columns
	public static final String K_SEGMENT_ID = "_id";
	public static final String K_SEGMENT_TRIP_ID = "tripid";
	public static final String K_SEGMENT_RATING = "rating";
	public static final String K_SEGMENT_DETAILS = "details";
	public static final String K_SEGMENT_START_INDEX = "startindex";
	public static final String K_SEGMENT_END_INDEX = "endindex";
	public static final String K_SEGMENT_STATUS = "status";

	private static final String MODULE_TAG = "DbAdapter";
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

	/**
	 * Database creation sql statement
	 */
	private static final String TABLE_CREATE_TRIPS = "create table trips "
			+ "(_id integer primary key autoincrement, purp text, start double, endtime double, "
			+ "fancystart text, fancyinfo text, distance float, note text,"
			+ "lathi integer, latlo integer, lgthi integer, lgtlo integer, status integer);";

	private static final String TABLE_CREATE_COORDS = "create table coords "
			+ "(_id integer primary key autoincrement, "
			+ "trip integer, lat int, lgt int, "
			+ "time double, acc float, alt double, speed float);";

	private static final String TABLE_CREATE_NOTES = "create table notes "
			+ "(_id integer primary key autoincrement, tripid int, notetype integer, noteseverity integer, noterecorded double, "
			+ "notefancystart text, notedetails text, noteimageurl text, "
			+ "notelat int, notelgt int, noteacc float, notealt double, notespeed float, notestatus integer);";

	private static final String TABLE_CREATE_PAUSES = "create table pauses "
			+ "(_id integer, starttime double, endtime double, "
			+ "PRIMARY KEY(_id, startTime), "
			+ "FOREIGN KEY(_id) REFERENCES TRIPS(_id));";

	private static final String TABLE_CREATE_SEGMENTS = "create table segments "
			+ "(_id integer primary key autoincrement, tripid int, rating int, "
			+ "details text, startindex int, endindex int, status int, "
			+ "FOREIGN KEY(tripid) REFERENCES TRIPS(_id));";

	private static final String TABLE_CREATE_ANSWERS = "create table answers "
			+ "(trip_id integer, question_id integer, answer_id integer, "
			+ "FOREIGN KEY(trip_id) REFERENCES TRIPS(_id));";

	private static final String TABLE_CREATE_NOTE_ANSWERS = "create table note_answers "
			+ "(note_id integer, question_id integer, answer_id integer, other_text text,"
			+ "FOREIGN KEY(note_id) REFERENCES NOTES(_id));";

	private static final String TABLE_DROP_TRIPS = "drop table trips;";
	private static final String TABLE_DROP_SEGMENTS = "drop table segments;";
	private static final String TABLE_DROP_NOTES = "drop table notes;";
	private static final String TABLE_DROP_PAUSES = "drop table pauses;";
	private static final String TABLE_DROP_ANSWERS = "drop table answers;";
	private static final String TABLE_DROP_NOTE_ANSWERS = "drop table note_answers;";
	private static final String TABLE_NOTES_ADD_COLUMN = "ALTER TABLE notes ADD COLUMN tripid int;";

	private static final String DATABASE_NAME = "data";
	private static final String DATA_TABLE_TRIPS = "trips";
	private static final String DATA_TABLE_COORDS = "coords";
	private static final String DATA_TABLE_NOTES = "notes";
	private static final String DATA_TABLE_PAUSES = "pauses";
	private static final String DATA_TABLE_SEGMENTS = "segments";
	private static final String DATA_TABLE_ANSWERS = "answers";
	private static final String DATA_TABLE_NOTE_ANSWERS = "note_answers";

	private final Context mCtx;
	private String noteImagesDirName = null;

	// ************************************************************************
	// *                         DatabaseHelper
	// ************************************************************************

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(TABLE_CREATE_TRIPS);
			db.execSQL(TABLE_CREATE_COORDS);
			db.execSQL(TABLE_CREATE_NOTES);
			db.execSQL(TABLE_CREATE_PAUSES);
			db.execSQL(TABLE_CREATE_SEGMENTS);
			db.execSQL(TABLE_CREATE_ANSWERS);
			db.execSQL(TABLE_CREATE_NOTE_ANSWERS);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

			Log.w(MODULE_TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);

			if (oldVersion < DATABASE_VERSION_PAUSES)
				try {
					db.execSQL(TABLE_CREATE_PAUSES);
				}
				catch(Exception ex) {
					Log.e(MODULE_TAG, ex.getMessage());
				}

			if (oldVersion < DATABASE_VERSION_SEGMENTS)
				try {
					db.execSQL(TABLE_CREATE_SEGMENTS);
				}
				catch(Exception ex) {
					Log.e(MODULE_TAG, ex.getMessage());
				}

			if (oldVersion < DATABASE_VERSION_NOTES_V3) {
				try {
					db.execSQL(TABLE_DROP_NOTES);
				}
				catch(Exception ex) {
					Log.e(MODULE_TAG, ex.getMessage());
				}
				try {
					db.execSQL(TABLE_CREATE_NOTES);
				}
				catch(Exception ex) {
					Log.e(MODULE_TAG, ex.getMessage());
				}
			}

			if (oldVersion < DATABASE_VERSION_ANSWERS) {
				try {
					db.execSQL(TABLE_CREATE_ANSWERS);
				}
				catch(Exception ex) {
					Log.e(MODULE_TAG, ex.getMessage());
				}
			}

			if (oldVersion < DATABASE_VERSION_NOTE_ANSWERS) {
				try {
					db.execSQL(TABLE_CREATE_NOTE_ANSWERS);
				}
				catch(Exception ex) {
					Log.e(MODULE_TAG, ex.getMessage());
				}
			}
			if (oldVersion < DATABASE_VERSION_NOTE_SEVERITY) {
				try {
					db.execSQL(TABLE_DROP_NOTES);
					db.execSQL(TABLE_CREATE_NOTES);
				}
				catch(Exception ex) {
					Log.e(MODULE_TAG, ex.getMessage());
				}
			}
		}

		@Override
		public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		}
	}

	// ************************************************************************
	// *                    DbAdapter table methods
	// ************************************************************************

	/**
	 * Constructor - takes the context to allow the database to be
	 * opened/created
	 *
	 * @param ctx
	 *            the Context within which to work
	 */
	public DbAdapter(Context ctx) {
		mCtx = ctx;
		try {
			noteImagesDirName = makeNoteImageDirectory();
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	private String makeNoteImageDirectory() throws Exception {
        File dir;

        if (null == (dir = mCtx.getFilesDir())) {
            throw new Exception("Unable to retrieve root directory for storing note images");
        }

        String dirName = dir.getAbsolutePath() + "/"+ NOTE_IMAGES_DIR_NAME;
        File imagesDir = new File(dirName);
        if (!imagesDir.exists())
            if (!imagesDir.mkdirs())
                throw new Exception("Unable to create directory for storing note images");
        return dirName;
	}

	/**
	 * Open the database. If it cannot be opened, try to create a new instance
	 * of the database. If it cannot be created, throw an exception to signal
	 * the failure
	 *
	 * @return this (self reference, allowing this to be chained in an
	 *         initialization call)
	 * @throws SQLException
	 *             if the database could be neither opened or created
	 */
	public DbAdapter open() throws SQLException {
		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}

	public DbAdapter openReadOnly() throws SQLException {
		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getReadableDatabase();
		return this;
	}

	public void close() {
		mDbHelper.close();
	}

	// ************************************************************************
	// *                    Coordinate table methods
	// ************************************************************************

	public boolean addCoordToTrip(long tripid, CyclePoint pt) {

		// Add the latest point
		ContentValues coordValues = new ContentValues();
		coordValues.put(K_POINT_TRIP, tripid);
		coordValues.put(K_POINT_LAT, pt.latitude);
		coordValues.put(K_POINT_LGT, pt.longitude);
		coordValues.put(K_POINT_TIME, pt.time);
		coordValues.put(K_POINT_ACC, pt.accuracy);
		coordValues.put(K_POINT_ALT, pt.altitude);
		coordValues.put(K_POINT_SPEED, pt.speed);

		long rowId = mDb.insert(DATA_TABLE_COORDS, null, coordValues);

		if (rowId == -1) {
			Log.e(MODULE_TAG, "Insert " + DATA_TABLE_COORDS + ": failed");
		}
		else {
			Log.i(MODULE_TAG, "Insert " + DATA_TABLE_COORDS + "[" + String.valueOf(rowId) + "]("
					+ K_POINT_TRIP + ", " + K_POINT_LAT + ", " + K_POINT_LGT + ", "
					+ K_POINT_TIME + ", " + K_POINT_ACC + ", " + K_POINT_ALT + ", "
					+ K_POINT_SPEED +")");
		}

		// And update the trip stats
		ContentValues tripValues = new ContentValues();
		tripValues.put(K_TRIP_END, pt.time);

		int numRows = mDb.update(DATA_TABLE_TRIPS, tripValues, K_TRIP_ROWID + "=" + tripid, null);

		Log.i(MODULE_TAG, "Updated " + DATA_TABLE_TRIPS + "[" + String.valueOf(tripid) +"]("
				+ K_TRIP_END +"): " + String.valueOf(numRows) + " rows.");

		boolean success = ((rowId != -1) && (numRows > 0));

		return success;
	}

	public boolean deleteAllCoordsForTrip(long tripid) {

		int numRows = mDb.delete(DATA_TABLE_COORDS, K_POINT_TRIP + "=" + tripid, null);

		Log.i(MODULE_TAG, "Deleted " + DATA_TABLE_COORDS + "[" + String.valueOf(tripid) +"]: " + String.valueOf(numRows) + " rows.");

		return numRows > 0;
	}

	public Cursor fetchAllCoordsForTrip(long tripid) {
		try {
			Cursor mCursor = mDb.query(true, DATA_TABLE_COORDS, new String[] {
					K_POINT_LAT, K_POINT_LGT, K_POINT_TIME, K_POINT_ACC,
					K_POINT_ALT, K_POINT_SPEED }, K_POINT_TRIP + "=" + tripid,
					null, null, null, K_POINT_TIME, null);

			if (mCursor != null) {
				mCursor.moveToFirst();
			}
			return mCursor;
		} catch (Exception e) {
			Log.e(MODULE_TAG, e.toString());
			return null;
		}
	}

	// ************************************************************************
	// *                       Trip table methods
	// ************************************************************************

	/**
	 * Create a new trip using the data provided. If the trip is successfully
	 * created return the new rowId for that trip, otherwise return a -1 to
	 * indicate failure.
	 */
	public long createTrip(String purp, double starttime, String fancystart, String note) {

		ContentValues initialValues = new ContentValues();
		initialValues.put(K_TRIP_PURP, purp);
		initialValues.put(K_TRIP_START, starttime);
		initialValues.put(K_TRIP_FANCYSTART, fancystart);
		initialValues.put(K_TRIP_NOTE, note);
		initialValues.put(K_TRIP_STATUS, TripData.STATUS_INCOMPLETE);

		Long rowId = mDb.insert(DATA_TABLE_TRIPS, null, initialValues);

		Log.i(MODULE_TAG, "Insert " + DATA_TABLE_TRIPS + "[" + String.valueOf(rowId) +"]");

		return rowId;
	}

	public long createTrip() {
		return createTrip("", System.currentTimeMillis(), "", "");
	}

	/**
	 * Delete the trip with the given rowId
	 *
	 * @param rowId
	 *            id of note to delete
	 * @return true if deleted, false otherwise
	 */
	public boolean deleteTrip(long rowId) {

		int numRows = mDb.delete(DATA_TABLE_TRIPS, K_TRIP_ROWID + "=" + rowId, null);

		Log.i(MODULE_TAG, "Deleted " + DATA_TABLE_TRIPS + "[" + String.valueOf(rowId) +"]: " + String.valueOf(numRows) + " rows.");

		return  numRows > 0;
	}

	/**
	 * Return a Cursor over the list of all notes in the database
	 *
	 * @return Cursor over all trips
	 */
	public Cursor fetchAllTrips() {
		Cursor c = mDb.query(DATA_TABLE_TRIPS, new String[] { K_TRIP_ROWID,
				K_TRIP_PURP, K_TRIP_START, K_TRIP_FANCYSTART, K_TRIP_NOTE,
				K_TRIP_FANCYINFO, K_TRIP_END, K_TRIP_DISTANCE, K_TRIP_STATUS },
				null, null, null, null, "-" + K_TRIP_START);
		if (c != null && c.getCount() > 0) {
			c.moveToFirst();
		}
		return c;
	}

	public Cursor fetchUnsentTrips() {
		Cursor c = mDb.query(DATA_TABLE_TRIPS, new String[] { K_TRIP_ROWID },
				K_TRIP_STATUS + "=" + TripData.STATUS_COMPLETE, null, null,
				null, null);
		if (c != null && c.getCount() > 0) {
			c.moveToFirst();
		}
		return c;
	}

	public int cleanTripsCoordsTables() {
		int badTrips = 0;

		Cursor c = mDb.query(DATA_TABLE_TRIPS, new String[] { K_TRIP_ROWID,
				K_TRIP_STATUS }, K_TRIP_STATUS + "="
				+ TripData.STATUS_INCOMPLETE, null, null, null, null);

		if (c != null && c.getCount() > 0) {
			c.moveToFirst();
			badTrips = c.getCount();

			while (!c.isAfterLast()) {
				long tripid = c.getInt(0);
				deleteAllCoordsForTrip(tripid);
				deletePauses(tripid);
				deleteAnswers(tripid);
				c.moveToNext();
			}
		}
		c.close();
		if (badTrips > 0) {
			mDb.delete(DATA_TABLE_TRIPS, K_TRIP_STATUS + "="
					+ TripData.STATUS_INCOMPLETE, null);
		}
		return badTrips;
	}

	/**
	 * Return a Cursor positioned at the trip that matches the given rowId
	 *
	 * @param rowId
	 *            id of trip to retrieve
	 * @return Cursor positioned to matching trip, if found
	 * @throws SQLException
	 *             if trip could not be found/retrieved
	 */
	public Cursor fetchTrip(long rowId) throws SQLException {
		Cursor mCursor = mDb.query(true, DATA_TABLE_TRIPS, new String[] {
				K_TRIP_ROWID, K_TRIP_PURP, K_TRIP_START, K_TRIP_FANCYSTART,
				K_TRIP_NOTE, K_TRIP_LATHI, K_TRIP_LATLO, K_TRIP_LGTHI,
				K_TRIP_LGTLO, K_TRIP_STATUS, K_TRIP_END, K_TRIP_FANCYINFO,
				K_TRIP_DISTANCE },

		K_TRIP_ROWID + "=" + rowId,

		null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	public boolean updateTrip(long tripid, String fancystart, String fancyinfo, String note) {

		int numRows;

		ContentValues contentValues = new ContentValues();

		contentValues.put(K_TRIP_FANCYSTART, fancystart);
		contentValues.put(K_TRIP_FANCYINFO, fancyinfo);
		contentValues.put(K_TRIP_NOTE, note);

		numRows = mDb.update(DATA_TABLE_TRIPS, contentValues, K_TRIP_ROWID + "=" + tripid, null);

		Log.i(MODULE_TAG, "Updated " + DATA_TABLE_TRIPS + "[" + String.valueOf(tripid)
				+ "](" + K_TRIP_FANCYSTART + ", " + K_TRIP_FANCYINFO + ", " + K_TRIP_NOTE +"): "
				+ String.valueOf(numRows) + " rows.");

		return numRows > 0;
	}

	public void updateTripPurpose(long tripid, String purpose) {

		int numRows;

		ContentValues contentValues = new ContentValues();

		contentValues.put(K_TRIP_PURP, purpose);

		numRows = mDb.update(DATA_TABLE_TRIPS, contentValues, K_TRIP_ROWID + "=" + tripid, null);

		Log.i(MODULE_TAG, "Updated " + DATA_TABLE_TRIPS + "[" + String.valueOf(tripid)
				+ "](" + K_TRIP_PURP + "): " + String.valueOf(numRows) + " rows.");

		return;
	}

	public boolean updateTrip(long tripid, int lathigh, int latlow, int lgthigh, int lgtlow, float distance) {

		int numRows;

		ContentValues contentValues = new ContentValues();

		contentValues.put(K_TRIP_LATHI, lathigh);
		contentValues.put(K_TRIP_LATLO, latlow);
		contentValues.put(K_TRIP_LGTHI, lgthigh);
		contentValues.put(K_TRIP_LGTLO, lgtlow);
		contentValues.put(K_TRIP_DISTANCE, distance);

		numRows = mDb.update(DATA_TABLE_TRIPS, contentValues, K_TRIP_ROWID + "=" + tripid, null);

		Log.i(MODULE_TAG, "Updated " + DATA_TABLE_TRIPS + "[" + String.valueOf(tripid)
				+ "](" + K_TRIP_LATHI + ", " + K_TRIP_LATLO + ", " + K_TRIP_LGTHI + ", " + K_TRIP_LGTLO + ", " + K_TRIP_DISTANCE +"): " + String.valueOf(numRows) + " rows.");

		return numRows > 0;
	}

	public boolean updateTripStatus(long tripid, int tripStatus) {

		int numRows;

		ContentValues initialValues = new ContentValues();
		initialValues.put(K_TRIP_STATUS, tripStatus);

		numRows = mDb.update(DATA_TABLE_TRIPS, initialValues, K_TRIP_ROWID + "=" + tripid, null);

		Log.i(MODULE_TAG, "Updated " + DATA_TABLE_TRIPS + "[" + String.valueOf(tripid)
				+ "](" + K_TRIP_STATUS +"): " + String.valueOf(numRows) + " rows.");

		return numRows > 0;
	}

	// ************************************************************************
	// *                       Notes table methods
	// ************************************************************************

	/**
	 * Create a new note using the data provided. If the note is successfully
	 * created return the new rowId for that note, otherwise return a -1 to
	 * indicate failure.
	 */

	public long createNote(long tripId, int noteType, int noteSeverity, double noteRecorded,
			String noteFancyStart, String noteDetails, String imageUrl) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(K_NOTE_TRIP_ID, tripId);
		initialValues.put(K_NOTE_TYPE, noteType);
		initialValues.put(K_NOTE_SEVERITY, noteSeverity);
		initialValues.put(K_NOTE_RECORDED, noteRecorded);
		initialValues.put(K_NOTE_FANCYSTART, noteFancyStart);
		initialValues.put(K_NOTE_DETAILS, noteDetails);
		initialValues.put(K_NOTE_IMGURL, imageUrl);

		initialValues.put(K_NOTE_LAT, 0);
		initialValues.put(K_NOTE_LGT, 0);
		initialValues.put(K_NOTE_ACC, 0);
		initialValues.put(K_NOTE_ALT, 0);
		initialValues.put(K_NOTE_SPEED, 0);

		initialValues.put(K_NOTE_STATUS, NoteData.STATUS_INCOMPLETE);

		return mDb.insert(DATA_TABLE_NOTES, null, initialValues);
	}

	public long createNote(long tripId) {
		return createNote(tripId, -1, -1, System.currentTimeMillis(), "", "", "");
	}

	/**
	 * Delete the note with the given rowId
	 *
	 * @param rowId
	 *            id of note to delete
	 * @return true if deleted, false otherwise
	 */
	public boolean deleteNote(long rowId) {

		deleteNoteImageFile(rowId);

		return mDb.delete(DATA_TABLE_NOTES, K_NOTE_ROWID + "=" + rowId, null) > 0;
	}




	/**
	 * Return a Cursor over the list of all notes in the database
	 *
	 * @return Cursor over all notes
	 */
	public Cursor fetchAllNotes() {

		Cursor cursor = mDb.query(DATA_TABLE_NOTES,
				new String[] { K_NOTE_ROWID, K_NOTE_TRIP_ID, K_NOTE_TYPE, K_NOTE_SEVERITY, K_NOTE_RECORDED,
						K_NOTE_FANCYSTART, K_NOTE_DETAILS, K_NOTE_IMGURL,
						K_NOTE_LAT, K_NOTE_LGT, K_NOTE_ACC,
						K_NOTE_ALT, K_NOTE_SPEED, K_NOTE_STATUS }, null, null,
				null, null, "-" + K_NOTE_RECORDED);

		if ((cursor != null) && (cursor.getCount() > 0)) {
			cursor.moveToFirst();
		}
		return cursor;
	}

	public Cursor fetchUnsentNotes() {
		Cursor c = mDb.query(DATA_TABLE_NOTES, new String[] { K_NOTE_ROWID },
				K_NOTE_STATUS + "=" + NoteData.STATUS_COMPLETE, null, null,
				null, null);
		if (c != null && c.getCount() > 0) {
			c.moveToFirst();
		}
		return c;
	}

	public int cleanNoteTables() {
		int badNotes = 0;

		Cursor c = mDb.query(DATA_TABLE_NOTES, new String[] { K_NOTE_ROWID,
				K_NOTE_STATUS }, K_NOTE_STATUS + "="
				+ NoteData.STATUS_INCOMPLETE, null, null, null, null);

		if (c != null && c.getCount() > 0) {
			c.moveToFirst();
			badNotes = c.getCount();

			while (!c.isAfterLast()) {
				c.moveToNext();
			}
		}
		c.close();
		if (badNotes > 0) {
			mDb.delete(DATA_TABLE_NOTES, K_NOTE_STATUS + "="
					+ NoteData.STATUS_INCOMPLETE, null);
		}
		return badNotes;
	}

	/**
	 * Return a Cursor positioned at the note that matches the given rowId
	 *
	 * @param rowId
	 *            id of note to retrieve
	 * @return Cursor positioned to matching note, if found
	 * @throws SQLException
	 *             if note could not be found/retrieved
	 */
	public Cursor fetchNote(long rowId) throws SQLException {
		Cursor mCursor = mDb.query(true, DATA_TABLE_NOTES,
				new String[] { K_NOTE_ROWID, K_NOTE_TRIP_ID, K_NOTE_TYPE, K_NOTE_SEVERITY, K_NOTE_RECORDED,
						K_NOTE_FANCYSTART, K_NOTE_DETAILS, K_NOTE_IMGURL,
						K_NOTE_LAT, K_NOTE_LGT, K_NOTE_ACC,
						K_NOTE_ALT, K_NOTE_SPEED, K_NOTE_STATUS },

				K_NOTE_ROWID + "=" + rowId,

				null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	public boolean updateNote(long noteid, int latitude, int longitude,
			float accuracy, double altitude, float speed) {

		ContentValues contentValues = new ContentValues();

		contentValues.put(K_NOTE_LAT, latitude);
		contentValues.put(K_NOTE_LGT, longitude);
		contentValues.put(K_NOTE_ACC, accuracy);
		contentValues.put(K_NOTE_ALT, altitude);
		contentValues.put(K_NOTE_SPEED, speed);

		int numRows = mDb.update(DATA_TABLE_NOTES, contentValues, K_NOTE_ROWID + "=" + noteid, null);

		Log.i(MODULE_TAG, "Updated " + DATA_TABLE_NOTES + "[" + String.valueOf(noteid) + "]("
				+ K_NOTE_LAT + ", " + K_NOTE_LGT + ", " + K_NOTE_ACC + ", " + K_NOTE_ALT + ", "
				+ K_NOTE_SPEED +"): " + String.valueOf(numRows) + " rows.");

		return numRows > 0;
	}

	public boolean updateNote(long noteId, String noteFancyStart, int noteType, int noteSeverity,
			 String noteDetails, byte[] imageBytes) {

		ContentValues contentValues = new ContentValues();

		contentValues.put(K_NOTE_FANCYSTART, noteFancyStart);
		contentValues.put(K_NOTE_TYPE, noteType);
		contentValues.put(K_NOTE_SEVERITY, noteSeverity);
		contentValues.put(K_NOTE_DETAILS, noteDetails);

		if (null == imageBytes) {
			contentValues.put(K_NOTE_IMGURL, "");
		}
		else {
			contentValues.put(K_NOTE_IMGURL, getNoteImageFileName(noteId));
			saveToFile(noteId, imageBytes);

			float[] latLng = new float[2];

			if (getNoteImageLatLng(noteId, latLng)) {
				contentValues.put(K_NOTE_LAT, (int) latLng[0]);
				contentValues.put(K_NOTE_LGT, (int) latLng[1]);
			}
		}

		int numRows = mDb.update(DATA_TABLE_NOTES, contentValues, K_NOTE_ROWID + "=" + noteId, null);

		return numRows > 0;
	}

	public String getNoteImageFileName(long noteId) {
		return noteImagesDirName + "/note_image_" + noteId + ".jpg";
	}

	private boolean getNoteImageLatLng(long noteId, float[] latLng) {

		String latitude;
		String latitudeRef;
		String longitude;
		String longitudeRef;

		String fileName = getNoteImageFileName(noteId);
		try {
			File file = new File(fileName);
			if (file.exists()) {
				//ExifInterface exif = new ExifInterface(fileName);
				ExifInterface exif = new ExifInterface("&^%^%$%^)_+++----");
		    	Log.v(MODULE_TAG, "Filename: " + fileName);

				//if (exif.getLatLong(latLng)) {
			    //	Log.v(MODULE_TAG, "Latitude: " + latLng[0]);
			    //	Log.v(MODULE_TAG, "Longitude: " + latLng[1]);
			    //	return true;
				//}
				if (null != (latitude = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE)))
			    	Log.v(MODULE_TAG, "Latitude: " + latitude);
				if (null != (latitudeRef = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF)))
			    	Log.v(MODULE_TAG, "latitudeRef: " + latitudeRef);
				if (null != (longitude = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE)))
			    	Log.v(MODULE_TAG, "longitude: " + longitude);
				if (null != (longitudeRef = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF)))
			    	Log.v(MODULE_TAG, "longitudeRef: " + longitudeRef);
			}
		}
		catch(IOException ex) {
	    	Log.e(MODULE_TAG, ex.getMessage());
		}
		return false;
	}

	private void saveToFile(long noteId, byte[] imageBytes) {

		String fileName = getNoteImageFileName(noteId);

		File file = new File(fileName);
		OutputStream out = null;
		try {
			out = new BufferedOutputStream(new FileOutputStream(file));
			out.write(imageBytes);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		finally {
			if (out != null) {
				try {
					out.close();
				}
				catch(Exception ex) {
					Log.e(MODULE_TAG, ex.getMessage());
				}
			}
		}
	}

	private void deleteNoteImageFile(long noteId) {

		String fileName = getNoteImageFileName(noteId);
		File file;

		try {
			file = new File(fileName);
			if (file.exists()) {
				file.delete();
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	public byte[] getNoteImageData(long noteId) {

		float[] latLng = new float[2];

		if (getNoteImageLatLng(noteId, latLng)) {
			Log.v(MODULE_TAG, (int) latLng[0]);
			Log.v(MODULE_TAG, , (int) latLng[1]);
		}


		String fileName = getNoteImageFileName(noteId);
		File file = new File(fileName);
		InputStream in = null;
		byte[] bytes = null;
		try {
			if (file.exists()) {
				in = new FileInputStream(file);
				bytes = new byte[(int) file.length()];
				in.read(bytes);
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
			bytes = null;
		}
		finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException ex) {
					Log.e(MODULE_TAG, ex.getMessage());
				}
			}
		}
		return bytes;
	}

	public boolean updateNoteStatus(long noteid, int noteStatus) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(K_NOTE_STATUS, noteStatus);

		int numRows = mDb.update(DATA_TABLE_NOTES, initialValues, K_NOTE_ROWID + "=" + noteid, null);

		Log.i(MODULE_TAG, "Updated " + DATA_TABLE_NOTES + "[" + String.valueOf(noteid)
				+ "](" + K_NOTE_STATUS +"): " + String.valueOf(numRows) + " rows.");

		return numRows > 0;
	}

	// ************************************************************************
	// *                       Pauses table methods
	// ************************************************************************

	/**
	 * Insert a row into the 'pauses' table
	 * @param tripId Trip ID of associated trip
	 * @param startTime Starting date-time of the pause
	 * @param endTime Ending date-time of the pause
	 * @throws SQLException
	 */
	public void addPauseToTrip(long tripId, double startTime, double endTime) throws SQLException{

		// Assemble row data
		ContentValues rowValues = new ContentValues();
		rowValues.put(K_PAUSE_TRIP_ID, tripId);
		rowValues.put(K_PAUSE_START_TIME, startTime);
		rowValues.put(K_PAUSE_END_TIME, endTime);

		// Insert row in table
		mDb.insertOrThrow(DATA_TABLE_PAUSES, null, rowValues);
	}

	/**
	 * Delete pauses with the given trip ID
	 * @param tripId id of the pauses to delete
	 */
	public void deletePauses(long tripId) {
		mDb.delete(DATA_TABLE_PAUSES, K_PAUSE_TRIP_ID + "=" + tripId, null);
	}

	/**
	 * Return a Cursor positioned at the pause that matches the given rowId
	 *
	 * @param rowId
	 *            id of trip to retrieve
	 * @return Cursor positioned to matching trip, if found
	 * @throws SQLException
	 *             if trip could not be found/retrieved
	 */
	public Cursor fetchPauses(long tripId) throws SQLException {

		Cursor cursor;

		String[] columns = new String[] { K_PAUSE_START_TIME, K_PAUSE_END_TIME };
		String whereClause = K_PAUSE_TRIP_ID + "=" + tripId;

		if (null != (cursor = mDb.query(true, DATA_TABLE_PAUSES, columns, whereClause, null, null, null, null, null))) {
			cursor.moveToFirst();
		}

		return cursor;
	}

	// ************************************************************************
	// *                       Answers table methods
	// ************************************************************************

	/**
	 * Insert a row into the 'answers' table
	 * @param trip_id Trip ID of associated trip
	 * @param question_id ID of question being answered
	 * @param answer_id ID of answer
	 * @throws SQLException
	 */
	public void addAnswerToTrip(long trip_id, int question_id, int answer_id) throws SQLException{

		// Assemble row data
		ContentValues rowValues = new ContentValues();
		rowValues.put(K_ANSWER_TRIP_ID, trip_id);
		rowValues.put(K_ANSWER_QUESTION_ID, question_id);
		rowValues.put(K_ANSWER_ANSWER_ID, answer_id);

		// Insert row in table
		mDb.insertOrThrow(DATA_TABLE_ANSWERS, null, rowValues);
	}

	/**
	 * Delete answers with the given trip ID
	 * @param trip_id id of the pauses to delete
	 */
	public void deleteAnswers(long trip_id) {
		mDb.delete(DATA_TABLE_ANSWERS, K_ANSWER_TRIP_ID + "=" + trip_id, null);
	}

	/**
	 * Return a Cursor positioned at the answers that matches the given trip_id
	 *
	 * @param trip_id ID of trip to retrieve
	 * @return Cursor positioned to matching trip, if found
	 * @throws SQLException if trip could not be found/retrieved
	 */
	public Cursor fetchAnswers(long trip_id) throws SQLException {

		Cursor cursor;

		String[] columns = new String[] { K_ANSWER_QUESTION_ID, K_ANSWER_ANSWER_ID };
		String whereClause = K_ANSWER_TRIP_ID + "=" + trip_id;

		if (null != (cursor = mDb.query(true, DATA_TABLE_ANSWERS, columns,
				whereClause, null, null, null, null, null))) {
			cursor.moveToFirst();
		}

		return cursor;
	}

	// ************************************************************************
	// *                     Note Answers table methods
	// ************************************************************************

	/**
	 * Insert a row into the 'answers' table
	 * @param note_id Note ID of associated trip
	 * @param question_id ID of question being answered
	 * @param answer_id ID of answer
	 * @throws SQLException
	 */
	public void addAnswerToNote(long note_id, int question_id, int answer_id) throws SQLException{

		// Assemble row data
		ContentValues rowValues = new ContentValues();
		rowValues.put(K_NOTE_ANSWER_NOTE_ID, note_id);
		rowValues.put(K_NOTE_ANSWER_QUESTION_ID, question_id);
		rowValues.put(K_NOTE_ANSWER_ANSWER_ID, answer_id);
		rowValues.put(K_NOTE_ANSWER_OTHER_TEXT, "");

		// Insert row in table
		mDb.insertOrThrow(DATA_TABLE_NOTE_ANSWERS, null, rowValues);
	}

	/**
	 * Insert a row into the 'answers' table
	 * @param note_id Note ID of associated trip
	 * @param question_id ID of question being answered
	 * @param answer_id ID of answer
	 * @throws SQLException
	 */
	public void addAnswerToNote(long note_id, int question_id, int answer_id, String other_text) throws SQLException{

		// Assemble row data
		ContentValues rowValues = new ContentValues();
		rowValues.put(K_NOTE_ANSWER_NOTE_ID, note_id);
		rowValues.put(K_NOTE_ANSWER_QUESTION_ID, question_id);
		rowValues.put(K_NOTE_ANSWER_ANSWER_ID, answer_id);
		rowValues.put(K_NOTE_ANSWER_OTHER_TEXT, other_text);

		// Insert row in table
		mDb.insertOrThrow(DATA_TABLE_NOTE_ANSWERS, null, rowValues);
	}

	/**
	 * Delete answers with the given note ID
	 * @param note_id id of the pauses to delete
	 */
	public void deleteNoteAnswers(long note_id) {
		mDb.delete(DATA_TABLE_NOTE_ANSWERS, K_NOTE_ANSWER_NOTE_ID + "=" + note_id, null);
	}

	/**
	 * Return a Cursor positioned at the answers that matches the given note_id
	 *
	 * @param note_id ID of trip to retrieve
	 * @return Cursor positioned to matching trip, if found
	 * @throws SQLException if trip could not be found/retrieved
	 */
	public Cursor fetchNoteAnswers(long note_id) throws SQLException {

		Cursor cursor;

		String[] columns = new String[] { K_NOTE_ANSWER_QUESTION_ID, K_NOTE_ANSWER_ANSWER_ID, K_NOTE_ANSWER_OTHER_TEXT };
		String whereClause = K_NOTE_ANSWER_NOTE_ID + "=" + note_id;

		if (null != (cursor = mDb.query(true, DATA_TABLE_NOTE_ANSWERS, columns,
				whereClause, null, null, null, null, null))) {
			cursor.moveToFirst();
		}

		return cursor;
	}

	// ************************************************************************
	// *                       Segments table methods
	// ************************************************************************

	/**
	 * Return a Cursor positioned at the Segment that matches the given segmentId
	 *
	 * @param segmentId id of note to retrieve
	 * @return Cursor positioned to matching note, if found
	 * @throws SQLException
	 *             if note could not be found/retrieved
	 */
	public Cursor fetchSegment(long segmentId) throws SQLException {
		Cursor mCursor = mDb.query(true, DATA_TABLE_SEGMENTS,
				new String[] { K_SEGMENT_ID, K_SEGMENT_TRIP_ID, K_SEGMENT_RATING,
				K_SEGMENT_DETAILS, K_SEGMENT_START_INDEX, K_SEGMENT_END_INDEX,
				K_SEGMENT_STATUS}, K_SEGMENT_ID + "=" + segmentId,
				null, null, null, null, null);

		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	public long createSegment() {
		return createSegment(-1, -1, "", -1, -1);
	}

	/**
	 * Create a new segment using the data provided. If the segment is successfully
	 * created return the new rowId for that segment, otherwise return a -1 to
	 * indicate failure.
	 */

	public long createSegment(long tripId, int rating, String details, int startIndex, int endIndex) {

		ContentValues initialValues = new ContentValues();
		initialValues.put(K_SEGMENT_TRIP_ID, tripId);
		initialValues.put(K_SEGMENT_RATING, rating);
		initialValues.put(K_SEGMENT_DETAILS, details);
		initialValues.put(K_SEGMENT_START_INDEX, startIndex);
		initialValues.put(K_SEGMENT_END_INDEX, endIndex);
		initialValues.put(K_SEGMENT_STATUS, SegmentData.STATUS_INCOMPLETE);

		long segmentId = mDb.insert(DATA_TABLE_SEGMENTS, null, initialValues);
		return segmentId;
	}

	public boolean updateSegmentStatus(long segmentId, int status) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(K_SEGMENT_STATUS, status);

		return mDb.update(DATA_TABLE_SEGMENTS, initialValues, K_SEGMENT_ID + "=" + segmentId, null) > 0;
	}

	public boolean updateSegment(long segmentId, long tripId,
			int rating, String details, int startIndex, int endIndex) {

		ContentValues initialValues = new ContentValues();
		initialValues.put(K_SEGMENT_TRIP_ID, tripId);
		initialValues.put(K_SEGMENT_RATING , rating);
		initialValues.put(K_SEGMENT_DETAILS, details);
		initialValues.put(K_SEGMENT_START_INDEX, startIndex);
		initialValues.put(K_SEGMENT_END_INDEX, endIndex);

		return mDb.update(DATA_TABLE_SEGMENTS, initialValues, K_SEGMENT_ID + "=" + segmentId, null) > 0;
	}

	public Cursor fetchUnsentSegmentIds() {
		Cursor c = mDb.query(DATA_TABLE_SEGMENTS, new String[] { K_SEGMENT_ID },
				K_SEGMENT_STATUS + "=" + SegmentData.STATUS_COMPLETE, null, null,
				null, null);
		if (c != null && c.getCount() > 0) {
			c.moveToFirst();
		}
		return c;
	}

}
