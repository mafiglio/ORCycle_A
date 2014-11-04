package edu.pdx.cecs.orcycle;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.VisibleRegion;

public class CustomLocationActivity extends Activity {


	private enum TripMapView { info, map};

	private static final String MODULE_TAG = "CustomLocationActivity";

	public static final String EXTRA_NOTE_ID = "noteId";
	public static final String EXTRA_NOTE_SEVERITY = "noteSeverity";
	public static final String EXTRA_NOTE_SOURCE = "noteSource";
	public static final int EXTRA_NOTE_SOURCE_UNDEFINED = -1;
	public static final int EXTRA_NOTE_ID_UNDEFINED = -1;
	public static final int EXTRA_NOTE_TYPE_UNDEFINED = -1;
	public static final int EXTRA_NOTE_SOURCE_MAIN_INPUT = 0;
	public static final int EXTRA_NOTE_SOURCE_TRIP_MAP = 1;

	public static final String EXTRA_TRIP_ID = "EXTRA_TRIP_ID";
	private static final int EXTRA_TRIP_ID_UNDEFINED = -1;
	public static final String EXTRA_TRIP_SOURCE = "tripSource";
	private static final int EXTRA_TRIP_SOURCE_UNDEFINED = -1;

	private long tripId;
	private long noteId;
	private int noteSource;
	private int tripSource;


	GoogleMap mapView;
	ArrayList<CyclePoint> gpspoints;
	ArrayList<LatLng> mapPoints;
	Polyline polyline;
	Polyline segmentPolyline = null;

	private boolean initialPositionSet = false;

	private LatLng crosshairLocation = null;
	private Location customLocation;

	// *********************************************************************************
	// *
	// *********************************************************************************

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_custom_location);
		try {
			loadVars();

			initialPositionSet = false;
			customLocation = null;

			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

			// Set zoom controls
			mapView = ((MapFragment) getFragmentManager().findFragmentById(R.id.tripMap)).getMap();
			Bundle extras = getIntent().getExtras();

			mapView.setMyLocationEnabled(true);
			moveCameraToOregon();

			mapView.setOnCameraChangeListener(new OnCameraChangeListener() {

				@Override
				public void onCameraChange(CameraPosition cameraPosition) {

					Projection p;
					VisibleRegion vr;

					if (!initialPositionSet) {
						// Move camera.
						//mapView.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 50));
						moveCameraToOregon();
						// Remove listener to prevent position reset on camera move.
						initialPositionSet = true;
					}

					if (null != (p = mapView.getProjection())) {
						if (null != (vr = p.getVisibleRegion())) {

							crosshairLocation = new LatLng((vr.latLngBounds.northeast.latitude + vr.latLngBounds.southwest.latitude)/2.0,
													   (vr.latLngBounds.northeast.longitude + vr.latLngBounds.southwest.longitude)/2.0);
							if (null == customLocation) {
								customLocation = new Location("");
							}
							customLocation.setLatitude(crosshairLocation.latitude);
							customLocation.setLongitude(crosshairLocation.longitude);
							customLocation.setAccuracy((float)-1.0);
						}
					}
				}
			});


		} catch (Exception e) {
			Log.e(MODULE_TAG, e.toString());
		}
	}

	private void loadVars() {

		// get input values for this view
		Intent myIntent = getIntent();

		noteId = myIntent.getLongExtra(EXTRA_NOTE_ID, EXTRA_NOTE_ID_UNDEFINED);
		if (EXTRA_NOTE_ID_UNDEFINED == noteId) {
			throw new IllegalArgumentException(MODULE_TAG + ": EXTRA_NOTE_ID undefined.");
		}

		noteSource = myIntent.getIntExtra(EXTRA_NOTE_SOURCE, EXTRA_NOTE_SOURCE_UNDEFINED);
		if (!((noteSource == EXTRA_NOTE_SOURCE_MAIN_INPUT) ||(noteSource == EXTRA_NOTE_SOURCE_TRIP_MAP))) {
			throw new IllegalArgumentException(MODULE_TAG + ": EXTRA_NOTE_SOURCE invalid argument.");
		}

		// Note: these extras are used for transitioning back to the TripMapActivity if done
		if (EXTRA_TRIP_ID_UNDEFINED == (tripId = myIntent.getLongExtra(EXTRA_TRIP_ID, EXTRA_TRIP_ID_UNDEFINED))) {
			throw new IllegalArgumentException(MODULE_TAG + ": invalid extra - EXTRA_TRIP_ID");
		}

		if (EXTRA_TRIP_SOURCE_UNDEFINED == (tripSource = myIntent.getIntExtra(EXTRA_TRIP_SOURCE, EXTRA_TRIP_SOURCE_UNDEFINED))) {
			throw new IllegalArgumentException(MODULE_TAG + ": invalid extra - EXTRA_TRIP_SOURCE");
		}

	}

	private void moveCameraToMyLocation(Location location) {
		LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
		mapView.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 16));
	}

	private void moveCameraToOregon() {
		LatLng myLocation = new LatLng(43.8041334 , -120.55420119999996 );
		mapView.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 6));
	}

	@Override
	public void onResume() {
		super.onResume();

		try {
			UiSettings mUiSettings = mapView.getUiSettings();
			// Keep the UI Settings state in sync with the checkboxes.
			mUiSettings.setZoomControlsEnabled(true);
			mUiSettings.setCompassEnabled(true);
			mUiSettings.setMyLocationButtonEnabled(true);
			mapView.setMyLocationEnabled(true);
			mUiSettings.setScrollGesturesEnabled(true);
			mUiSettings.setZoomGesturesEnabled(true);
			mUiSettings.setTiltGesturesEnabled(true);
			mUiSettings.setRotateGesturesEnabled(true);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	@Override
	public void onBackPressed() {
		try {
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
			getMenuInflater().inflate(R.menu.custom_location, menu);
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

			case R.id.action_cla_done:
				setNoteLocation();
				transitionToNoteDetailActivity();
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

	private void setNoteLocation() {
		NoteData note = NoteData.fetchNote(this, noteId);
		note.setLocation(customLocation);
	}

	// *********************************************************************************
	// *
	// *********************************************************************************

	private void transitionToNoteDetailActivity() {

		// Create intent to go to the NoteDetailActivity
		Intent intent = new Intent(this, NoteDetailActivity.class);
		intent.putExtra(NoteDetailActivity.EXTRA_NOTE_ID, noteId);
		intent.putExtra(NoteDetailActivity.EXTRA_NOTE_SOURCE, noteSource);

		// the NoteType activity needs these when the back button
		// is pressed and we have to restart this activity
		intent.putExtra(NoteDetailActivity.EXTRA_TRIP_ID, tripId);
		intent.putExtra(NoteDetailActivity.EXTRA_TRIP_SOURCE, tripSource);

		// Exit this activity
		startActivity(intent);
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		finish();
	}
}
