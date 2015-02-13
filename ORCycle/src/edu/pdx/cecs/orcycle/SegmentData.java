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

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;

public class SegmentData {
	private long segmentId;
	private long tripId;
	private int rating;
	private String details;
	private int startIndex;
	private int endIndex;
	private int segmentStatus;

	private final DbAdapter mDb;

	public static int STATUS_INCOMPLETE = 0;
	public static int STATUS_COMPLETE = 1;
	public static int STATUS_SENT = 2;

	private static final String MODULE_TAG = "SegmentData";

	public static SegmentData createSegment(Context c) {
		SegmentData t = new SegmentData(c.getApplicationContext(), 0);
		t.createSegmentInDatabase(c);
		t.initializeData();
		return t;
	}

	public static SegmentData fetchSegment(Context c, long segment_id) {
		SegmentData t = new SegmentData(c.getApplicationContext(), segment_id);
		t.populateDetails();
		return t;
	}

	private SegmentData(Context ctx, long segment_id) {
		Context context = ctx.getApplicationContext();
		this.segmentId = segment_id;
		mDb = new DbAdapter(context);
	}

	private void initializeData() {
		tripId = -1;
		rating = -1;
		details = "";
		startIndex = -1;
		endIndex = -1;
	}

	// Get lat/long extremes, etc, from note record
	void populateDetails() {

		mDb.openReadOnly();
		try {
			Cursor cursor = mDb.fetchSegment(segmentId);

			try {
				tripId = cursor.getLong(cursor.getColumnIndex("tripid"));
				rating = cursor.getInt(cursor.getColumnIndex("rating"));
				details = cursor.getString(cursor.getColumnIndex("details"));
				startIndex = cursor.getInt(cursor.getColumnIndex("startindex"));
				endIndex = cursor.getInt(cursor.getColumnIndex("endindex"));
				segmentStatus = cursor.getInt(cursor.getColumnIndex("status"));
			}
			finally {
				cursor.close();
			}
		}
		finally {
			mDb.close();
		}
	}

	void createSegmentInDatabase(Context c) {
		mDb.open();
		segmentId = mDb.createSegment();
		mDb.close();
	}

	public boolean updateSegmentStatus(int segmentStatus) {
		boolean rtn;
		mDb.open();
		if (rtn = mDb.updateSegmentStatus(segmentId, segmentStatus)) {
			this.segmentStatus = segmentStatus;
		}
		mDb.close();
		return rtn;
	}

	public void updateSegment() {
		updateSegment(-1, -1, "", -1, -1);
	}

	public void updateSegment(long tripId, int rating,
			String details, int startIndex, int endIndex) {
		// Save the segment details to the database.
		mDb.open();
		try {
			if (mDb.updateSegment(segmentId, tripId, rating, details, startIndex, endIndex)) {
				this.tripId = tripId;
				this.rating = rating;
				this.details = details;
				this.startIndex = startIndex;
				this.endIndex = endIndex;
			}
		}
		finally {
			mDb.close();
		}
	}

	public long getSegmentId() {
		return segmentId;
	}

	// JSON field identifiers
	public static final String JFID_TRIP_ID = "t";
	public static final String JFID_RATING = "r";
	public static final String JFID_DETAILS = "d";
	public static final String JFID_START_INDEX = "s";
	public static final String JFID_END_INDEX = "e";

	public JSONObject getJSON() throws JSONException {

		JSONObject json = new JSONObject();

		json.put(JFID_TRIP_ID, tripId);
		json.put(JFID_RATING, rating);
		json.put(JFID_DETAILS, details);
		json.put(JFID_START_INDEX, startIndex);
		json.put(JFID_END_INDEX, endIndex);

		return json;
	}



}
