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
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
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

	private enum TripMapView { info, map};

	private static final String MODULE_TAG = "TripMapActivity";

	private static final double NOTE_MIN_DISTANCE_FROM_TRIP = 45.7247; // meters is approximate 150 feet;

	public static final String EXTRA_TRIP_ID = "tripId";
	private static final int EXTRA_TRIP_ID_UNDEFINED = -1;
	public static final String EXTRA_IS_NEW_TRIP = "isNewTrip";
	public static final String EXTRA_TRIP_SOURCE = "tripSource";
	private static final int EXTRA_TRIP_SOURCE_UNDEFINED = -1;
	public static final int EXTRA_TRIP_SOURCE_MAIN_INPUT = 0;
	public static final int EXTRA_TRIP_SOURCE_SAVED_TRIPS = 1;
	public static final int EXTRA_TRIP_SOURCE_TRIP_QUESTIONS = 2;

	GoogleMap mapView;
	ArrayList<CyclePoint> gpspoints;
	ArrayList<LatLng> mapPoints;
	Polyline polyline;
	Polyline segmentPolyline = null;

	private LatLngBounds.Builder bounds;
	private boolean initialPositionSet = false;
	private Button buttonNote = null;
	private Button buttonRateStart = null;
	private Button buttonRateFinish = null;
	private boolean crosshairInRangeOfTrip = false;
	private LatLng crosshairLocation = null;
	private int indexOfClosestPoint = 0;
	private int segmentStartIndex = -1;
	private int segmentEndIndex = -1;
	private long tripId = -1;
	private boolean isNewTrip = false;
	private int tripSource = EXTRA_TRIP_SOURCE_UNDEFINED;
	private boolean selectingSegment = false;
	private com.google.android.gms.maps.model.Marker segmentStartMarker = null;
	private View questionsView;
	private View llTmButtons;

	private MenuItem mnuInfo;
	private MenuItem mnuMap;
	private TripMapView currentView = TripMapView.map;

	private TextView tvTmTripFrequency;
	private TextView tvTmTripPurpose;
	private TextView tvTmRouteChoice;
	private TextView tvTmTripComfort;
	//private TextView tvTmRouteSafety;
	//private TextView tvTmPassengers;
	//private TextView tvTmBikeAccessories;
	//private TextView tvTmRideConflict;
	private TextView tvTmRouteStressor;
	private TextView tvTmComment;

	private TextView tvAtmMoveCloser;

	// *********************************************************************************
	// *
	// *********************************************************************************

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		try {

			initialPositionSet = false;
			crosshairInRangeOfTrip = false;
			selectingSegment = false;

			// getWindow().requestFeature(Window.FEATURE_NO_TITLE);
			setContentView(R.layout.activity_trip_map);

			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

			// Set zoom controls
			mapView = ((MapFragment) getFragmentManager().findFragmentById(R.id.tripMap)).getMap();
			llTmButtons = findViewById(R.id.llTmButtons);
			llTmButtons.setVisibility(View.VISIBLE);

			questionsView = findViewById(R.id.tripQuestionsRootView);
			questionsView.setVisibility(View.INVISIBLE);

			tvAtmMoveCloser = (TextView) findViewById(R.id.tvAtmMoveCloser);


			Bundle extras = getIntent().getExtras();
			isNewTrip = extras.getBoolean(EXTRA_IS_NEW_TRIP, false);

			if (EXTRA_TRIP_ID_UNDEFINED == (tripId = extras.getLong(EXTRA_TRIP_ID, EXTRA_TRIP_ID_UNDEFINED))) {
				throw new IllegalArgumentException(MODULE_TAG + ": invalid extra - EXTRA_TRIP_ID");
			}

			if (EXTRA_TRIP_SOURCE_UNDEFINED == (tripSource = extras.getInt(EXTRA_TRIP_SOURCE, EXTRA_TRIP_SOURCE_UNDEFINED))) {
				throw new IllegalArgumentException(MODULE_TAG + ": invalid extra - EXTRA_TRIP_SOURCE");
			}

			TripData trip = TripData.fetchTrip(this, tripId);

			// Show trip details
			TextView t1 = (TextView) findViewById(R.id.TextViewMapPurpose);
			TextView t2 = (TextView) findViewById(R.id.TextViewMapInfo);
			TextView t3 = (TextView) findViewById(R.id.TextViewMapFancyStart);
			t1.setText(trip.purp);
			t2.setText(trip.info);
			t3.setText(trip.fancystart);

			// Trip questions
			tvTmTripFrequency   = (TextView) findViewById(R.id.tvTmTripFrequency);
			tvTmTripPurpose     = (TextView) findViewById(R.id.tvTmTripPurpose);
			tvTmRouteChoice     = (TextView) findViewById(R.id.tvTmRouteChoice);
			tvTmTripComfort     = (TextView) findViewById(R.id.tvTmTripComfort);
			//tvTmRouteSafety     = (TextView) findViewById(R.id.tvTmRouteSafety);
			//tvTmPassengers      = (TextView) findViewById(R.id.tvTmPassengers);
			//tvTmBikeAccessories = (TextView) findViewById(R.id.tvTmBikeAccessories);
			//tvTmRideConflict    = (TextView) findViewById(R.id.tvTmRideConflict);
			tvTmRouteStressor   = (TextView) findViewById(R.id.tvTmRouteStressor);
			tvTmComment         = (TextView) findViewById(R.id.tvTmComment);
			tvTmComment.setText(trip.getNoteComment());

			getTripResponses(tripId);

			buttonNote = (Button) findViewById(R.id.buttonNoteThis);
			buttonNote.setOnClickListener(new ButtonNote_OnClickListener());

			buttonRateStart = (Button) findViewById(R.id.buttonRateStart);
			buttonRateStart.setOnClickListener(new ButtonRateStart_OnClickListener());

			// the next two lines will temporarily disable the RateSegment functionality
			//buttonRateStart.setOnClickListener(new ButtonRateStart_OnClickListener());
			buttonRateStart.setVisibility(View.GONE);

			buttonRateFinish = (Button) findViewById(R.id.buttonRateFinish);
			buttonRateFinish.setOnClickListener(new ButtonRateFinish_OnClickListener());
			buttonRateFinish.setVisibility(View.GONE);

			gpspoints = trip.getPoints();
			mapPoints = new ArrayList<LatLng>();

			LatLng point;
			bounds = new LatLngBounds.Builder();
			for (int i = 0; i < gpspoints.size(); i++) {
				mapPoints.add(point = new LatLng(gpspoints.get(i).latitude * 1E-6, gpspoints.get(i).longitude * 1E-6));
				bounds.include(point);
			}

			if (trip.startpoint != null) {
				addMarker(trip.startpoint, R.drawable.trip_start);
			}

			if (trip.endpoint != null) {
				addMarker(trip.endpoint, R.drawable.trip_end);
			}

			polyline = drawMap(0, mapPoints.size() - 1, Color.BLUE);

			mapView.setOnCameraChangeListener(new OnCameraChangeListener() {

				@Override
				public void onCameraChange(CameraPosition cameraPosition) {

					Projection p;
					VisibleRegion vr;

					if (!initialPositionSet) {
						// Move camera.
						mapView.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 50));
						// Remove listener to prevent position reset on camera move.
						initialPositionSet = true;
					}

					if (null != (p = mapView.getProjection())) {
						if (null != (vr = p.getVisibleRegion())) {

							crosshairLocation = new LatLng((vr.latLngBounds.northeast.latitude + vr.latLngBounds.southwest.latitude)/2.0,
													   (vr.latLngBounds.northeast.longitude + vr.latLngBounds.southwest.longitude)/2.0);

							double crosshairDistanceFromTrip = getCrosshairDistanceFromTrip(crosshairLocation);

							// textCrosshair.setText(String.valueOf(crosshairDistanceFromTrip)); // Keep for debugging

							if (null != buttonNote) {
								if (crosshairInRangeOfTrip) {
									//buttonNote.setText("  --> Note this... <--  ");
									buttonNote.setTextColor(getResources().getColor(R.color.user_button_text));
									buttonNote.setBackgroundColor(getResources().getColor(R.color.user_button_background));
									buttonRateStart.setTextColor(getResources().getColor(R.color.user_button_text));
									buttonRateStart.setBackgroundColor(getResources().getColor(R.color.user_button_background));
									buttonRateFinish.setTextColor(getResources().getColor(R.color.user_button_text));
									buttonRateFinish.setBackgroundColor(getResources().getColor(R.color.user_button_background));
									tvAtmMoveCloser.setVisibility(View.GONE);


									if ((segmentStartIndex != -1) && (segmentEndIndex == -1)) {
										// Remove previously drawn line
										if (null != segmentPolyline)
											segmentPolyline.remove();
										// draw the new line
										segmentPolyline = drawMap(segmentStartIndex, indexOfClosestPoint, Color.MAGENTA);
									}

								}
								else {
									//buttonNote.setText("  Note this...  ");
									buttonNote.setTextColor(Color.WHITE);
									buttonNote.setBackgroundColor(Color.RED);
									buttonRateStart.setTextColor(Color.WHITE);
									buttonRateStart.setBackgroundColor(Color.RED);
									buttonRateFinish.setTextColor(Color.WHITE);
									buttonRateFinish.setBackgroundColor(Color.RED);
									tvAtmMoveCloser.setVisibility(View.VISIBLE);
								}
							}
						}
					}
				}
			});

			if ((trip.getStatus() < TripData.STATUS_SENT) && (extras != null)
					&& isNewTrip) {
				// And upload to the cloud database, too! W00t W00t!
				TripUploader uploader = new TripUploader(TripMapActivity.this);
				uploader.execute(trip.tripid);
			}

		} catch (Exception e) {
			Log.e(MODULE_TAG, e.toString());
		}
		currentView = TripMapView.map;
	}

	private void getTripResponses(long tripId) {

		DbAdapter mDb = new DbAdapter(this);

		mDb.openReadOnly();
		try {
			Cursor answers = mDb.fetchTripAnswers(tripId);

			int questionCol = answers.getColumnIndex(DbAdapter.K_ANSWER_QUESTION_ID);
			int answerCol = answers.getColumnIndex(DbAdapter.K_ANSWER_ANSWER_ID);
			int otherCol = answers.getColumnIndex(DbAdapter.K_ANSWER_OTHER_TEXT);

			// Variables for collecting responses
			StringBuilder sbTripFrequency = new StringBuilder();
			StringBuilder sbTripPurpose = new StringBuilder();
			StringBuilder sbRoutePrefs = new StringBuilder();
			StringBuilder sbTripComfort = new StringBuilder();
			//StringBuilder sbRouteSafety = new StringBuilder();
			//StringBuilder sbPassengers = new StringBuilder();
			//StringBuilder sbBikeAccessories = new StringBuilder();
			//StringBuilder sbRideConflict = new StringBuilder();
			StringBuilder sbRouteStressor = new StringBuilder();

			int questionId;
			int answerId;
			String otherText;

			// Cycle thru the database entries
			while (!answers.isAfterLast()) {

				questionId = answers.getInt(questionCol);
				answerId = answers.getInt(answerCol);
				if (-1 == otherCol) {
					otherText = null;
				}
				else if (null != (otherText = answers.getString(otherCol))) {
					otherText = otherText.trim();
				}
				else {
					otherText = null;
				}

				try {
					switch(questionId) {

					case DbQuestions.TRIP_FREQUENCY:
						append(sbTripFrequency, R.array.qa_19_routeFrequency, DbAnswers.tripFreq, answerId);
						break;

					case DbQuestions.TRIP_PURPOSE:
						append(sbTripPurpose, R.array.qa_20_tripPurpose, DbAnswers.tripPurpose, answerId, otherText);
						break;

					case DbQuestions.ROUTE_PREFS:
						append(sbRoutePrefs, R.array.qa_21_routePreferences, DbAnswers.routePrefs, answerId, otherText);
						break;

					case DbQuestions.TRIP_COMFORT:
						append(sbTripComfort, R.array.qa_22_routeComfort, DbAnswers.tripComfort, answerId);
						break;

					//case DbQuestions.ROUTE_SAFETY:
					//	append(sbRouteSafety, R.array.qa_23_RouteSafety, DbAnswers.routeSafety, answerId);
					//	break;

					//case DbQuestions.PASSENGERS:
					//	append(sbPassengers, R.array.qa_24_ridePassengers, DbAnswers.passengers, answerId);
					//	break;

					//case DbQuestions.BIKE_ACCESSORIES:
					//	append(sbBikeAccessories, R.array.qa_25_bikeAccessories, DbAnswers.bikeAccessories, answerId, otherText);
					//	break;

					//case DbQuestions.RIDE_CONFLICT:
					//	append(sbRideConflict, R.array.qa_26_rideConflict, DbAnswers.rideConflict, answerId);
					//	break;

					case DbQuestions.ROUTE_STRESSORS:
						append(sbRouteStressor, R.array.qa_27_routeStressors, DbAnswers.routeStressors, answerId, otherText);
						break;
					}
				}
				catch(Exception ex) {
					Log.e(MODULE_TAG, ex.getMessage());
				}
				// Move to next row
				answers.moveToNext();
			}
			answers.close();

			// Show trip details
			tvTmTripFrequency.setText(sbTripFrequency.toString());
			tvTmTripPurpose.setText(sbTripPurpose.toString());
			tvTmRouteChoice.setText(sbRoutePrefs.toString());
			tvTmTripComfort.setText(sbTripComfort.toString());
			//tvTmRouteSafety.setText(sbRouteSafety.toString());
			//tvTmPassengers.setText(sbPassengers.toString());
			//tvTmBikeAccessories.setText(sbBikeAccessories.toString());
			//tvTmRideConflict.setText(sbRideConflict.toString());
			tvTmRouteStressor.setText(sbRouteStressor.toString());
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		finally {
			mDb.close();
		}
	}

	private void append(StringBuilder sb, int textArrayId, int[] answers, int answerId) {
		sb.append(DbAnswers.getAnswerText(this, textArrayId, answers, answerId)).append("\r\n");
	}

	private void append(StringBuilder sb, int textArrayId, int[] answers, int answerId, String otherText) {
		if ((null == otherText) || otherText.equals("")) {
			sb.append(DbAnswers.getAnswerText(this, textArrayId, answers, answerId)).append("\r\n");
		}
		else {
			sb.append(otherText).append("\r\n");
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		try {
			setCurrentView(currentView);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	private void setSelectingSegment(boolean value) {
		if (true == (selectingSegment = value)) {
			buttonNote.setVisibility(View.GONE);
			buttonRateStart.setVisibility(View.GONE);
			buttonRateFinish.setVisibility(View.VISIBLE);
		}
		else {
			buttonNote.setVisibility(View.VISIBLE);

			// this line will temporarily disable the RateSegment functionality
			//buttonRateStart.setVisibility(View.VISIBLE);
			buttonRateStart.setVisibility(View.GONE);

			buttonRateFinish.setVisibility(View.GONE);

			if (null != segmentPolyline)
				segmentPolyline.remove();

			if (null != segmentStartMarker) {
				segmentStartMarker.remove();
			}
			segmentStartIndex = -1;
			segmentEndIndex = -1;
		}
	}

	private boolean getSelectingSegment() {
		return selectingSegment;
	}

	private com.google.android.gms.maps.model.Marker
	addMarker(CyclePoint cyclePoint, int resourceId) {

		MarkerOptions markerOptions = new MarkerOptions();
		markerOptions.icon(BitmapDescriptorFactory.fromResource(resourceId));
		markerOptions.anchor(0.0f, 1.0f); // Anchors the marker on the bottom left
		markerOptions.position(new LatLng(cyclePoint.latitude * 1E-6, cyclePoint.longitude * 1E-6));

		return mapView.addMarker(markerOptions);
	}

	private Polyline drawMap(int start, int end, int color) {

		// swap order if out of order
		if (start > end) {
			int tmp = start;
			start = end;
			end = tmp;
		}

		PolylineOptions polylineOptions = new PolylineOptions();
		polylineOptions.geodesic(true).color(color);

		for (int i = start; i <= end; i++) {
			polylineOptions.add(mapPoints.get(i));
		}

		return mapView.addPolyline(polylineOptions);
	}

	/**
	 * Get shortest distance from trip to crosshairs, and mark index of that point
	 * @param crosshairs
	 * @return
	 */
	private double getCrosshairDistanceFromTrip(LatLng crosshairs) {

		float distance[] = new float[1];

		// Get the initial point
		LatLng tripPoint = mapPoints.get(0);
		// get distance to initial point
		Location.distanceBetween(crosshairs.latitude, crosshairs.longitude, tripPoint.latitude, tripPoint.longitude, distance);
		// set index of closest point to initial point
		indexOfClosestPoint = 0;

		// Set minimum distance = distance to initial point
		double minDistance = distance[0];

		// now cycle through remaining points
		for (int i = 1; i < mapPoints.size(); i++) {
			tripPoint = mapPoints.get(i);
			Location.distanceBetween(crosshairs.latitude, crosshairs.longitude, tripPoint.latitude, tripPoint.longitude, distance);
			if (distance[0] < minDistance) {
				minDistance = distance[0];
				indexOfClosestPoint = i;
			}
		}

		crosshairInRangeOfTrip = (minDistance <= NOTE_MIN_DISTANCE_FROM_TRIP);

		return minDistance;
	}

	@Override
	public void onBackPressed() {
		try {

			if(getSelectingSegment()) {
				setSelectingSegment(false);
			}
			else {
				// Remove polylines if they exist
				if ((mapView != null) && (polyline != null)) {
					polyline.remove();
				}

				//  Transition to
				transitionToTabsConfigActivity();
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	/* Creates the menu items */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		try {
			// Inflate the menu items for use in the action bar
			getMenuInflater().inflate(R.menu.trip_map, menu);

			mnuInfo = menu.getItem(0);
			mnuMap = menu.getItem(1);
			setCurrentView(currentView);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		return super.onCreateOptionsMenu(menu);
	}

	/* Handles item selections */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		try {
			// Handle presses on the action bar items
			switch (item.getItemId()) {

			case R.id.action_trip_map_view_info:
				setCurrentView(TripMapView.info);
				return true;

			case R.id.action_trip_map_view_map:
				setCurrentView(TripMapView.map);
				return true;

			case R.id.action_trip_map_close:

				// close -> go back to FragmentMainInput
				if ((mapView != null) && (polyline != null)) {
					polyline.remove();
				}

				transitionToTabsConfigActivity();
				return true;

			default:
				return super.onOptionsItemSelected(item);
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		return super.onOptionsItemSelected(item);
	}

	private void setCurrentView(TripMapView tripMapView) {

		switch (tripMapView) {

		case info:

			questionsView.setVisibility(View.VISIBLE);
			llTmButtons.setVisibility(View.INVISIBLE);
			tvAtmMoveCloser.setVisibility(View.INVISIBLE);
			if ((null != mnuInfo) && (null != mnuMap)) {
				mnuInfo.setVisible(false);
				mnuMap.setVisible(true);
			}
			break;

		case map:

			questionsView.setVisibility(View.INVISIBLE);
			llTmButtons.setVisibility(View.VISIBLE);
			if (!crosshairInRangeOfTrip) {
				tvAtmMoveCloser.setVisibility(View.VISIBLE);
			}
			if ((null != mnuInfo) && (null != mnuMap)) {
				mnuInfo.setVisible(true);
				mnuMap.setVisible(false);
			}
			break;
		}
		currentView = tripMapView;
	}

	// *********************************************************************************
	// *
	// *********************************************************************************

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
					Toast.makeText(TripMapActivity.this, "Please move target closer to path.", Toast.LENGTH_SHORT).show();
				}
				else {
					// update note entity
					NoteData note = NoteData.createNote(TripMapActivity.this, tripId);
					note.updateNoteStatus(NoteData.STATUS_INCOMPLETE);

					// Construct notes location
					Location noteLocation = new Location("");
					noteLocation.setLatitude(crosshairLocation.latitude);
					noteLocation.setLongitude(crosshairLocation.longitude);
					noteLocation.setAccuracy(gpspoints.get(indexOfClosestPoint).accuracy);
					noteLocation.setAltitude(gpspoints.get(indexOfClosestPoint).altitude);
					note.setLocation(noteLocation);

					transitionToNoteQuestionsActivity(note, tripId);
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
					setSelectingSegment(true);
					segmentStartIndex = indexOfClosestPoint;
					segmentStartMarker = addMarker(gpspoints.get(segmentStartIndex), R.drawable.trip_start);
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
					Toast.makeText(TripMapActivity.this,
							"Target must be within 100 meters of bike path.",
							Toast.LENGTH_SHORT).show();
				}
				else if (indexOfClosestPoint == segmentStartIndex) {
					Toast.makeText(TripMapActivity.this,
							"Ending position must be different than starting position.",
							Toast.LENGTH_SHORT).show();
				}
				else {
					segmentEndIndex = indexOfClosestPoint;
					addMarker(gpspoints.get(segmentEndIndex), R.drawable.trip_end);

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

					transitionToRateSegmentActivity();
				}
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	// *********************************************************************************
	// *
	// *********************************************************************************

	private void transitionToTabsConfigActivity() {

		Intent intent = new Intent(TripMapActivity.this, TabsConfig.class);

		if ((tripSource == EXTRA_TRIP_SOURCE_MAIN_INPUT) ||
			(tripSource == EXTRA_TRIP_SOURCE_TRIP_QUESTIONS)) {
			intent.putExtra(TabsConfig.EXTRA_SHOW_FRAGMENT, TabsConfig.FRAG_INDEX_MAIN_INPUT);
		}
		else if (tripSource == EXTRA_TRIP_SOURCE_SAVED_TRIPS) {
			intent.putExtra(TabsConfig.EXTRA_SHOW_FRAGMENT, TabsConfig.FRAG_INDEX_SAVED_TRIPS);
		}
		else {
			throw new IllegalArgumentException(MODULE_TAG + ": tripSource contains invalid value");
		}
		startActivity(intent);
		finish();
		overridePendingTransition(android.R.anim.fade_in, R.anim.slide_out_down);
	}

	private void transitionToNoteQuestionsActivity(NoteData note, long tripId) {

		Intent intent = new Intent(this, NoteQuestionsActivity.class);
		intent.putExtra(NoteQuestionsActivity.EXTRA_NOTE_ID, note.noteId);
		intent.putExtra(NoteQuestionsActivity.EXTRA_NOTE_TYPE, NoteQuestionsActivity.EXTRA_NOTE_TYPE_UNDEFINED);
		intent.putExtra(NoteQuestionsActivity.EXTRA_NOTE_SOURCE, NoteQuestionsActivity.EXTRA_NOTE_SOURCE_TRIP_MAP);
		intent.putExtra(NoteQuestionsActivity.EXTRA_TRIP_ID, tripId);
		intent.putExtra(NoteQuestionsActivity.EXTRA_TRIP_SOURCE, tripSource);
		startActivity(intent);
		finish();
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	}

	private void transitionToRateSegmentActivity() {

		Intent intent = new Intent(this, RateSegmentActivity.class);

		intent.putExtra(RateSegmentActivity.EXTRA_TRIP_ID, tripId);
		intent.putExtra(RateSegmentActivity.EXTRA_TRIP_SOURCE, tripSource);
		intent.putExtra(RateSegmentActivity.EXTRA_SEGMENT_START_INDEX, segmentStartIndex);
		intent.putExtra(RateSegmentActivity.EXTRA_SEGMENT_END_INDEX, segmentEndIndex);

		startActivity(intent);
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		finish();
	}
}
