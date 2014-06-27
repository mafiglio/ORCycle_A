/**  Cycle Altanta, Copyright 2012 Georgia Institute of Technology
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

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.util.Log;

public class TripData {

	private static final String MODULE_TAG = "TripData";

	long tripid;

	private final static double RESET_START_TIME = 0.0;

	private double tripStartTime = RESET_START_TIME;
	private double startTime = RESET_START_TIME;
	private double endTime = RESET_START_TIME;

	private double pauseStartedTime = RESET_START_TIME;

	private double totalTravelTime = 0.0f;
	private boolean isPaused = false;
	private boolean isFinished = false;


	int numpoints = 0;
	int lathigh, lgthigh, latlow, lgtlow, latestlat, latestlgt;
	int status;
	float distance;
	String purp, fancystart, info;

	private ArrayList<CyclePoint> gpspoints = new ArrayList<CyclePoint>();
	CyclePoint startpoint, endpoint;

	DbAdapter mDb;

	public static int STATUS_INCOMPLETE = 0;
	public static int STATUS_COMPLETE = 1;
	public static int STATUS_SENT = 2;

	public static TripData createTrip(Context c) {
		TripData t = new TripData(c.getApplicationContext(), 0);
		t.createTripInDatabase(c);
		t.initializeData();
		return t;
	}

	public static TripData fetchTrip(Context c, long tripid) {
		TripData t = new TripData(c.getApplicationContext(), tripid);
		t.populateDetails();
		return t;
	}

	public TripData(Context ctx, long tripid) {
		Context context = ctx.getApplicationContext();
		this.tripid = tripid;
		mDb = new DbAdapter(context);
	}

	void initializeData() {
		endTime = (startTime = (tripStartTime = System.currentTimeMillis()));
		pauseStartedTime = RESET_START_TIME;
		totalTravelTime = 0.0f;
		isPaused = false;

		numpoints = 0;
		latestlat = 800;
		latestlgt = 800;
		distance = 0;

		lathigh = (int) (-100 * 1E6);
		latlow = (int) (100 * 1E6);
		lgtlow = (int) (180 * 1E6);
		lgthigh = (int) (-180 * 1E6);
		purp = fancystart = info = "";

		updateTrip();
	}

	// Get lat/long extremes, etc, from trip record
	void populateDetails() {

		pauseStartedTime = RESET_START_TIME; // TODO: maybe put into database.
		isPaused = false;

		mDb.openReadOnly();

		Cursor tripdetails = mDb.fetchTrip(tripid);
		tripStartTime = tripdetails.getDouble(tripdetails.getColumnIndex("start"));
		startTime = System.currentTimeMillis();
		lathigh = tripdetails.getInt(tripdetails.getColumnIndex("lathi"));
		latlow = tripdetails.getInt(tripdetails.getColumnIndex("latlo"));
		lgthigh = tripdetails.getInt(tripdetails.getColumnIndex("lgthi"));
		lgtlow = tripdetails.getInt(tripdetails.getColumnIndex("lgtlo"));
		status = tripdetails.getInt(tripdetails.getColumnIndex("status"));
		endTime = tripdetails.getDouble(tripdetails.getColumnIndex("endtime"));
		distance = tripdetails.getFloat(tripdetails.getColumnIndex("distance"));

		purp = tripdetails.getString(tripdetails.getColumnIndex("purp"));
		fancystart = tripdetails.getString(tripdetails
				.getColumnIndex("fancystart"));
		info = tripdetails.getString(tripdetails.getColumnIndex("fancyinfo"));

		tripdetails.close();

		Cursor points = mDb.fetchAllCoordsForTrip(tripid);
		if (points != null) {
			numpoints = points.getCount();
			points.close();
		}

		mDb.close();
	}

	void createTripInDatabase(Context c) {
		mDb.open();
		tripid = mDb.createTrip();
		mDb.close();
	}

	void dropTrip() {
		mDb.open();
		mDb.deleteAllCoordsForTrip(tripid);
		mDb.deletePauses(tripid);
		mDb.deleteTrip(tripid);
		mDb.close();
	}

	public ArrayList<CyclePoint> getPoints() {
		// If already built, don't build again!
		if (gpspoints != null && gpspoints.size() > 0) {
			return gpspoints;
		}

		// Otherwise, we need to query DB and build points from scratch.
		gpspoints = new ArrayList<CyclePoint>();

		try {
			mDb.openReadOnly();

			Cursor points = mDb.fetchAllCoordsForTrip(tripid);
			int COL_LAT = points.getColumnIndex(DbAdapter.K_POINT_LAT);
			int COL_LGT = points.getColumnIndex(DbAdapter.K_POINT_LGT);
			int COL_TIME = points.getColumnIndex(DbAdapter.K_POINT_TIME);
			int COL_ACC = points.getColumnIndex(DbAdapter.K_POINT_ACC);
			int COL_ALT = points.getColumnIndex(DbAdapter.K_POINT_ALT);

			numpoints = points.getCount();

			points.moveToLast();
			this.endpoint = new CyclePoint(points.getInt(COL_LAT),
					points.getInt(COL_LGT), points.getDouble(COL_TIME),
					points.getFloat(COL_ACC), points.getDouble(COL_ALT));

			points.moveToFirst();
			this.startpoint = new CyclePoint(points.getInt(COL_LAT),
					points.getInt(COL_LGT), points.getDouble(COL_TIME),
					points.getFloat(COL_ACC), points.getDouble(COL_ALT));

			while (!points.isAfterLast()) {
				int lat = points.getInt(COL_LAT);
				int lgt = points.getInt(COL_LGT);
				double time = points.getDouble(COL_TIME);
				float acc = (float) points.getDouble(COL_ACC);
				double alt = points.getDouble(COL_ALT);
				CyclePoint pt = new CyclePoint(lat, lgt, time, acc, alt);
				gpspoints.add(pt);
				// addPointToSavedMap(lat, lgt, time, acc);
				points.moveToNext();
			}
			points.close();
			mDb.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		// gpspoints.repopulate();

		return gpspoints;
	}

	public double getStartTime() {
		return tripStartTime;
	}

	public double getEndTime() {
		return endTime;
	}

	public void startPause() {

		double currentTime = System.currentTimeMillis();
		// record the beginning of pause time
		pauseStartedTime = currentTime;
		totalTravelTime += (currentTime - startTime);
		startTime = RESET_START_TIME;
		isPaused = true;
	}

	public void finishPause() {

		double currentTime = System.currentTimeMillis();

		// TODO: Assert(pauseStartedTime > RESET_START_TIME);

		// Insert pause data into database
		try {
			mDb.open();
			mDb.addPauseToTrip(tripid, pauseStartedTime, currentTime);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		finally {
			mDb.close();
		}

		// Re-initialize start time counters
		startTime = currentTime;
		isPaused = false;
	}

	// TODO: Verify this calculation.  Should be tied to whether trip is done or in progress
	public double getDuration () {

		if (isPaused) {
			return totalTravelTime;
		}
		else {
			return  totalTravelTime + (System.currentTimeMillis() - startTime);
		}
	}

	public float getDistance() {
		return distance;
	}

	boolean addPointNow(Location loc, double currentTime, float dst) {
		int lat = (int) (loc.getLatitude() * 1E6);
		int lgt = (int) (loc.getLongitude() * 1E6);

		// Skip duplicates
		if (latestlat == lat && latestlgt == lgt)
			return true;

		float accuracy = loc.getAccuracy();
		double altitude = loc.getAltitude();
		float speed = loc.getSpeed();

		CyclePoint pt = new CyclePoint(lat, lgt, currentTime, accuracy, altitude, speed);

		numpoints++;

		if (isPaused ) {
			endTime = totalTravelTime;
		}
		else {
			endTime = totalTravelTime + currentTime - startTime;
		}

		distance = dst;

		latlow = Math.min(latlow, lat);
		lathigh = Math.max(lathigh, lat);
		lgtlow = Math.min(lgtlow, lgt);
		lgthigh = Math.max(lgthigh, lgt);

		latestlat = lat;
		latestlgt = lgt;

		mDb.open();
		boolean rtn = mDb.addCoordToTrip(tripid, pt);
		rtn = rtn
				&& mDb.updateTrip(tripid, "", startTime, "", "", "", lathigh,
						latlow, lgthigh, lgtlow, distance);
		mDb.close();

		if (!rtn) {
			Log.e(MODULE_TAG, "Couldn't write trip point to database");
		}

		return rtn;
	}

	/**
	 * Makes final calculation of endTime and
	 * push trip data to the database
	 */
	public void finish() {
		if (!isFinished) {
			totalTravelTime += System.currentTimeMillis() - startTime;
			endTime = totalTravelTime;
			isFinished = true;
			updateTrip("", "", "", "");
		}
	}

	public boolean updateTripStatus(int tripStatus) {
		boolean rtn;
		mDb.open();
		rtn = mDb.updateTripStatus(tripid, tripStatus);
		mDb.close();
		return rtn;
	}

	public void updateTrip() {
		updateTrip("", "", "", "");
	}

	public void updateTrip(String purpose, String fancyStart, String fancyInfo,
			String notes) {
		// Save the trip details to the phone database. W00t!
		mDb.open();
		mDb.updateTrip(tripid, purpose, startTime, fancyStart, fancyInfo,
				notes, lathigh, latlow, lgthigh, lgtlow, distance);
		mDb.close();
	}
}
