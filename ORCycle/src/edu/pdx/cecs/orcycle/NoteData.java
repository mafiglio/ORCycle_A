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
 *  Cycle Altanta, Copyright 2012 Georgia Institute of Technology
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
 *   @author Billy Charlton <billy.charlton@sfcta.org>
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

import android.content.Context;
import android.database.Cursor;
import android.location.Location;

public class NoteData {

	public static final String MODULE_TAG = "NoteData";
	public static int STATUS_INCOMPLETE = 0;
	public static int STATUS_COMPLETE = 1;
	public static int STATUS_SENT = 2;

	private final DbAdapter mDb;

	private long noteId;
	private double recorded = 0;
	private int severity;
	private String fancyStart;

	private String details;
	private byte[] image;
	private int noteStatus;
	private long reportDate;

	private boolean emailSent;
	private int isSafetyIssue;
	private int isAccident;

	private int latitude;
	private int longitude;

	private float accuracy;
	private float speed;
	private double altitude;
	private String imageFileName;

	public long getNoteId() {
		return noteId;
	}

	public int getNoteStatus() {
		return noteStatus;
	}

	public String getNotedetails() {
		return details;
	}

	public double getRecorded() {
		return recorded;
	}

	public long getReportDate() {
		return reportDate;
	}

	public int getLatitude() {
		return latitude;
	}

	public int getLongitude() {
		return longitude;
	}

	public int getNoteSeverity() {
		return severity;
	}

	public float getAccuracy() {
		return accuracy;
	}

	public float getSpeed() {
		return speed;
	}

	public double getAltitude() {
		return altitude;
	}

	public String getFancyStart() {
		return fancyStart;
	}

	public byte[] getImage() {
		return image;
	}

	public boolean isEmailSent() {
		return emailSent;
	}

	public static NoteData createNote(Context context, long tripId) {
		NoteData noteData = new NoteData(context.getApplicationContext(), 0);
		long recorded = System.currentTimeMillis();
		noteData.createNoteInDatabase(tripId, recorded);
		noteData.initializeData(recorded);
		return noteData;
	}

	public static NoteData fetchNote(Context context, long noteId) {
		NoteData noteData = new NoteData(context.getApplicationContext(), noteId);
		noteData.populateDetails();
		return noteData;
	}

	private NoteData(Context ctx, long noteid) {
		Context context = ctx.getApplicationContext();
		this.noteId = noteid;
		mDb = new DbAdapter(context);
	}

	private void initializeData(long recorded) {
		this.recorded = recorded;
		severity = -1;
		fancyStart = details = "";
		latitude = 0;
		longitude = 0;
		accuracy = 0;
		altitude = 0;
		speed = 0;
		reportDate = 0;
		isSafetyIssue = -1;
		isAccident = -1;
	}

	public boolean hasImage() {
		return !imageFileName.equals("") && (image != null);
	}

	public String getImageFileName() {
		return imageFileName;
	}

	/**
	 * load values from database using global noteId
	 */
	private void populateDetails() {

		mDb.openReadOnly();
		try {
			Cursor noteDetails = mDb.fetchNote(noteId);
			try {
				recorded       = noteDetails.getDouble(noteDetails.getColumnIndex(DbAdapter.K_NOTE_RECORDED   ));
				fancyStart = noteDetails.getString(noteDetails.getColumnIndex(DbAdapter.K_NOTE_FANCYSTART ));
				latitude       = noteDetails.getInt   (noteDetails.getColumnIndex(DbAdapter.K_NOTE_LAT        ));
				longitude      = noteDetails.getInt   (noteDetails.getColumnIndex(DbAdapter.K_NOTE_LGT        ));
				accuracy       = noteDetails.getFloat (noteDetails.getColumnIndex(DbAdapter.K_NOTE_ACC        ));
				altitude       = noteDetails.getDouble(noteDetails.getColumnIndex(DbAdapter.K_NOTE_ALT        ));
				speed          = noteDetails.getFloat (noteDetails.getColumnIndex(DbAdapter.K_NOTE_SPEED      ));
				severity       = noteDetails.getInt   (noteDetails.getColumnIndex(DbAdapter.K_NOTE_SEVERITY   ));
				details        = noteDetails.getString(noteDetails.getColumnIndex(DbAdapter.K_NOTE_DETAILS    ));
				noteStatus     = noteDetails.getInt   (noteDetails.getColumnIndex(DbAdapter.K_NOTE_STATUS     ));
				imageFileName  = noteDetails.getString(noteDetails.getColumnIndex(DbAdapter.K_NOTE_IMGURL     ));
				reportDate     = noteDetails.getLong  (noteDetails.getColumnIndex(DbAdapter.K_NOTE_REPORT_DATE));
				emailSent      = (noteDetails.getInt  (noteDetails.getColumnIndex(DbAdapter.K_NOTE_EMAIL_SENT)) == 1 ? true : false);

				if ((null != imageFileName) && (!imageFileName.equals("")))
					image = mDb.getNoteImageData(noteId);
				else
					image = null;

				if (DbAnswers.isAccidentSeverity(severity)) {
					isAccident = 1;    // true
					isSafetyIssue = 0; // false
				}
				else if (DbAnswers.isSafetyUrgency(severity)) {
					isAccident = 0;    // false
					isSafetyIssue = 1; // true
				}
			}
			finally {
				noteDetails.close();
			}
		}
		finally {
			mDb.close();
		}
	}

	/**
	 * Create a note in the local database
	 * @param tripId
	 * @param recorded
	 */
	private void createNoteInDatabase(long tripId, long recorded) {
		mDb.open();
		try {
			noteId = mDb.createNote(tripId, recorded);
		}
		finally {
			mDb.close();
		}
	}

	/**
	 * Add location point
	 * @param loc
	 * @return
	 */
	public boolean setLocation(Location loc) {

		boolean rtn;

		int lat = (int) (loc.getLatitude() * 1E6);
		int lgt = (int) (loc.getLongitude() * 1E6);
		float accuracy = loc.getAccuracy();
		double altitude = loc.getAltitude();
		float speed = loc.getSpeed();

		mDb.open();
		try {
			rtn = mDb.updateNote(noteId, lat, lgt, accuracy, altitude, speed);
		}
		finally {
			mDb.close();
		}

		return rtn;
	}

	public boolean updateNoteStatus(int noteStatus) {
		boolean rtn;
		mDb.open();
		try {
			rtn = mDb.updateNoteStatus(noteId, noteStatus);
		}
		finally {
			mDb.close();
		}
		return rtn;
	}

	public void updateNoteLatLng(float latitude, float longitude) {

		this.latitude = (int) (latitude * 1E6);
		this.longitude = (int) (longitude * 1E6);

		mDb.open();
		try {
			mDb.updateNote(noteId, this.latitude, this.longitude, 0, 0, 0);
		}
		finally {
			mDb.close();
		}
	}

	/**
	 * Pushes the following note data to the database
	 * @param noteFancyStart
	 * @param noteDetails
	 * @param image
	 */
	public void updateNote(String noteDetails, byte[] image) {
		mDb.open();
		try {
			mDb.updateNote(noteId, noteDetails, image);
			this.details = noteDetails;
			this.imageFileName = (null == image) ? "" : mDb.getNoteImageFileName(noteId);
		}
		finally {
			mDb.close();
		}
	}

	public void updateEmailSent(boolean value) {
		mDb.open();
		try {
			mDb.updateNoteEmailSent(noteId, value);
			this.emailSent = value;
		}
		finally {
			mDb.close();
		}
	}

	/**
	 * Returns true if the report data is from a safety issue
	 * @return true if safety issue, false otherwise
	 * @throws IllegalStateException if the value was not set from the database
	 */
	public boolean isSafetyIssue() throws IllegalStateException {

		if (0 == this.isSafetyIssue)
			return false;
		else if (1 == this.isSafetyIssue)
			return true;

		throw new IllegalStateException(MODULE_TAG + ": isSafetyIssue value not set");
	}

	/**
	 * Returns true if the report data is from an accident
	 * @return true if accident, false otherwise
	 * @throws IllegalStateException if the value was not set from the database
	 */
	public boolean isAccident() throws IllegalStateException {

		if (0 == this.isAccident)
			return false;
		else if (1 == this.isAccident)
			return true;

		throw new IllegalStateException(MODULE_TAG + ": isAccident value not set");
	}
}
