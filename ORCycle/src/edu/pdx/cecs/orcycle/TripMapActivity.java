/**  Cycle Atlanta, Copyright 2014 Georgia Institute of Technology
 *                                    Atlanta, GA. USA
 *
 *   @author Christopher Le Dantec <ledantec@gatech.edu>
 *   @author Anhong Guo <guoanhong@gatech.edu>
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
//
package edu.pdx.cecs.orcycle;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.VisibleRegion;

public class TripMapActivity extends Activity {

	private static final String MODULE_TAG = "TripMapActivity";

	private static final double NOTE_MIN_DISTANCE_FROM_TRIP = 100.0;

	GoogleMap map;
	ArrayList<CyclePoint> gpspoints;
	Polyline polyline;

	private LatLngBounds.Builder bounds;
	private boolean initialPositionSet = false;
	private Button buttonNote = null;
	private Button buttonRateStart = null;
	private Button buttonRateFinish = null;
	private boolean crosshairInRangeOfTrip = false;
	private LatLng crosshairLocation = null;
	private int indexOfClosestPoint = 0;
	private int segmentStartIndex = 0;
	private int segmentEndIndex = 0;
	private long tripid = -1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initialPositionSet = false;
		crosshairInRangeOfTrip = false;

		// getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_trip_map);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		// Toast.makeText(this, "trip map", Toast.LENGTH_LONG).show();

		try {
			// Set zoom controls
			map = ((MapFragment) getFragmentManager().findFragmentById(R.id.tripMap)).getMap();

			Bundle cmds = getIntent().getExtras();
			tripid = cmds.getLong("showtrip");

			TripData trip = TripData.fetchTrip(this, tripid);

			// Show trip details
			TextView t1 = (TextView) findViewById(R.id.TextViewMapPurpose);
			TextView t2 = (TextView) findViewById(R.id.TextViewMapInfo);
			TextView t3 = (TextView) findViewById(R.id.TextViewMapFancyStart);
			t1.setText(trip.purp);
			t2.setText(trip.info);
			t3.setText(trip.fancystart);
			buttonNote = (Button) findViewById(R.id.buttonNoteThis);
			buttonNote.setOnClickListener(new ButtonNote_OnClickListener());

			buttonRateStart = (Button) findViewById(R.id.buttonRateStart);
			buttonRateStart.setOnClickListener(new ButtonRateStart_OnClickListener());

			buttonRateFinish = (Button) findViewById(R.id.buttonRateFinish);
			buttonRateFinish.setOnClickListener(new ButtonRateFinish_OnClickListener());
			buttonRateFinish.setVisibility(View.GONE);

			gpspoints = trip.getPoints();

			if (trip.startpoint != null) {
				map.addMarker(new MarkerOptions()
						.icon(BitmapDescriptorFactory
								.fromResource(R.drawable.pingreen))
						.anchor(0.0f, 1.0f) // Anchors the marker on the bottom
											// left
						.position(
								new LatLng(trip.startpoint.latitude * 1E-6,
										trip.startpoint.longitude * 1E-6)));
			}
			if (trip.endpoint != null) {
				map.addMarker(new MarkerOptions()
						.icon(BitmapDescriptorFactory
								.fromResource(R.drawable.pinpurple))
						.anchor(0.0f, 1.0f) // Anchors the marker on the bottom
											// left
						.position(
								new LatLng(trip.endpoint.latitude * 1E-6,
										trip.endpoint.longitude * 1E-6)));
			}

			bounds = new LatLngBounds.Builder();

			PolylineOptions rectOptions = new PolylineOptions();
			rectOptions.geodesic(true).color(Color.BLUE);

			for (int i = 0; i < gpspoints.size(); i++) {
				LatLng point = new LatLng(gpspoints.get(i).latitude * 1E-6,
						gpspoints.get(i).longitude * 1E-6);
				bounds.include(point);
				rectOptions.add(point);
			}

			polyline = map.addPolyline(rectOptions);

			map.setOnCameraChangeListener(new OnCameraChangeListener() {

				@Override
				public void onCameraChange(CameraPosition cameraPosition) {

					Projection p;
					VisibleRegion vr;

					if (!initialPositionSet) {
						// Move camera.
						map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 50));
						// Remove listener to prevent position reset on camera move.
						initialPositionSet = true;
					}

					if (null != (p = map.getProjection())) {
						if (null != (vr = p.getVisibleRegion())) {

							crosshairLocation = new LatLng((vr.latLngBounds.northeast.latitude + vr.latLngBounds.southwest.latitude)/2.0,
													   (vr.latLngBounds.northeast.longitude + vr.latLngBounds.southwest.longitude)/2.0);

							double crosshairDistanceFromTrip = getCrosshairDistanceFromTrip(crosshairLocation);

							// textCrosshair.setText(String.valueOf(crosshairDistanceFromTrip)); // Keep for debugging

							if (null != buttonNote) {
								if (crosshairInRangeOfTrip) {
									//buttonNote.setText("  --> Note this... <--  ");
									buttonNote.setTextColor(Color.BLACK);
									buttonNote.setBackgroundColor(Color.GREEN);
									buttonRateStart.setTextColor(Color.BLACK);
									buttonRateStart.setBackgroundColor(Color.GREEN);
									buttonRateFinish.setTextColor(Color.BLACK);
									buttonRateFinish.setBackgroundColor(Color.GREEN);
								}
								else {
									//buttonNote.setText("  Note this...  ");
									buttonNote.setTextColor(Color.WHITE);
									buttonNote.setBackgroundColor(Color.RED);
									buttonRateStart.setTextColor(Color.WHITE);
									buttonRateStart.setBackgroundColor(Color.RED);
									buttonRateFinish.setTextColor(Color.WHITE);
									buttonRateFinish.setBackgroundColor(Color.RED);
								}
							}
						}
					}
				}
			});

			if (trip.status < TripData.STATUS_SENT && cmds != null
					&& cmds.getBoolean("uploadTrip", false)) {
				// And upload to the cloud database, too! W00t W00t!
				TripUploader uploader = new TripUploader(TripMapActivity.this);
				uploader.execute(trip.tripid);
			}

		} catch (Exception e) {
			Log.e(MODULE_TAG, e.toString());
		}
	}

	private double getCrosshairDistanceFromTrip(LatLng point) {

		float results[] = new float[1];

		LatLng tripPoint = new LatLng(gpspoints.get(0).latitude * 1E-6, gpspoints.get(0).longitude * 1E-6);
		Location.distanceBetween(point.latitude, point.longitude, tripPoint.latitude, tripPoint.longitude, results);
		indexOfClosestPoint = 0;

		double minDistance = results[0];

		for (int i = 1; i < gpspoints.size(); i++) {
			tripPoint = new LatLng(gpspoints.get(i).latitude * 1E-6, gpspoints.get(i).longitude * 1E-6);
			Location.distanceBetween(point.latitude, point.longitude, tripPoint.latitude, tripPoint.longitude, results);
			if (results[0] < minDistance) {
				minDistance = results[0];
				indexOfClosestPoint = i;
			}
		}

		crosshairInRangeOfTrip = (minDistance <= NOTE_MIN_DISTANCE_FROM_TRIP);

		return minDistance;
	}

	// Make sure overlays get zapped when we go BACK
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && map != null) {
			// map.getOverlays().clear();
			polyline.remove();
		}
		return super.onKeyDown(keyCode, event);
	}

	/* Creates the menu items */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.trip_map, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/* Handles item selections */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.action_close_trip_map:
			// close -> go back to FragmentMainInput
			if (map != null) {
				polyline.remove();
			}

			onBackPressed();

			// Intent i = new Intent(TripMapActivity.this, TabsConfig.class);
			// //i.putExtra("keepme", true);
			// startActivity(i);
			// overridePendingTransition(android.R.anim.fade_in,
			// R.anim.slide_out_down);
			// TripMapActivity.this.finish();

			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

    /**
     * Class: ButtonNote_OnClickListener
     *
     * Description: Callback to be invoked when buttonNote button is clicked
     */
	private final class ButtonNote_OnClickListener implements View.OnClickListener {

		/**
		 * Description: Handles onClick for view
		 */
		public void onClick(View v) {

			try {
				if (!crosshairInRangeOfTrip) {
					Toast.makeText(TripMapActivity.this, "Target must be within 100 meters of bike path.", Toast.LENGTH_SHORT).show();
				}
				else {
					Intent noteTypeIntent = new Intent(TripMapActivity.this, NoteTypeActivity.class);
					// update note entity
					NoteData note = NoteData.createNote(TripMapActivity.this);

					noteTypeIntent.putExtra("noteid", note.noteid);
					noteTypeIntent.putExtra("isRecording", 0);
					note.updateNoteStatus(NoteData.STATUS_INCOMPLETE);

					// Construct notes location
					Location noteLocation = new Location("");
					noteLocation.setLatitude(crosshairLocation.latitude);
					noteLocation.setLongitude(crosshairLocation.longitude);
					noteLocation.setAccuracy(gpspoints.get(indexOfClosestPoint).accuracy);
					noteLocation.setAltitude(gpspoints.get(indexOfClosestPoint).altitude);
					note.setLocationTime(noteLocation, System.currentTimeMillis());

					startActivity(noteTypeIntent);
					TripMapActivity.this.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
					// getActivity().finish();
				}
			}

			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

    /**
     * Class: ButtonRate_OnClickListener
     *
     * Description: Callback to be invoked when buttonRateSegment button is clicked
     */
	private final class ButtonRateStart_OnClickListener implements View.OnClickListener {

		/**
		 * Description: Handles onClick for view
		 */
		public void onClick(View v) {
			try {
				if (!crosshairInRangeOfTrip) {
					Toast.makeText(TripMapActivity.this, "Target must be within 100 meters of bike path.", Toast.LENGTH_SHORT).show();
				}
				else {
					segmentStartIndex = indexOfClosestPoint;

					buttonNote.setVisibility(View.GONE);
					buttonRateStart.setVisibility(View.GONE);
					buttonRateFinish.setVisibility(View.VISIBLE);
				}
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

    /**
     * Class: ButtonRateFinish_OnClickListener
     *
     * Description: Callback to be invoked when buttonRateFinish button is clicked
     */
	private final class ButtonRateFinish_OnClickListener implements View.OnClickListener {

		/**
		 * Description: Handles onClick for view
		 */
		public void onClick(View v) {

			try {
				if (!crosshairInRangeOfTrip) {
					Toast.makeText(TripMapActivity.this, "Target must be within 100 meters of bike path.", Toast.LENGTH_SHORT).show();
				}
				else {
					segmentEndIndex = indexOfClosestPoint;

					// The user may have selected the start and beginning indexes
					// in reverse order, so check and swap if necessary
					if (segmentStartIndex > segmentEndIndex) {
						int tmp = segmentStartIndex;
						segmentStartIndex = segmentEndIndex;
						segmentEndIndex = tmp;
					}

					buttonNote.setVisibility(View.GONE);
					buttonRateStart.setVisibility(View.GONE);
					buttonRateFinish.setVisibility(View.VISIBLE);

					Intent rateSegmentIntent = new Intent(TripMapActivity.this, RateSegmentActivity.class);
					rateSegmentIntent.putExtra(RateSegmentActivity.EXTRA_TRIP_ID, tripid);
					rateSegmentIntent.putExtra(RateSegmentActivity.EXTRA_START_INDEX, segmentStartIndex);
					rateSegmentIntent.putExtra(RateSegmentActivity.EXTRA_END_INDEX, segmentEndIndex);

					startActivity(rateSegmentIntent);
					TripMapActivity.this.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
				}
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}
}
