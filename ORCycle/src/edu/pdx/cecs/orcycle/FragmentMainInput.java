package edu.pdx.cecs.orcycle;

import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;

/**
 * @author Robin
 *
 */
public class FragmentMainInput extends Fragment
	implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener, IRecordServiceListener,
		OnMyLocationButtonClickListener {

	private static final String MODULE_TAG = "FragmentMainInput";
	private static final float CF_METERS_TO_MILES = 0.00062137f;  	// Conversion factor meters to miles
	private static final float CF_MILES_TO_LBS_CO2 = 0.93f;  		// Conversion factor miles to pounds CO2 saved
	private static final float CF1_MILES_TO_KCALORIES = 49.0f;  	// Conversion factor part 1 miles to Kcalories burned
	private static final float CF2_MILES_TO_KCALORIES = -1.69f;  	// Conversion factor part 2 miles to Kcalories burned
	private static final float METERS_PER_SECOND_TO_MILES_PER_HOUR = 2.2369f;

	// Reference to Global application object
	private MyApplication myApp = null;

	// Reference to recording service;
	private IRecordService recordingService = null;

	// UI Elements
	private Button buttonStart = null;
	private Button buttonPause = null;
	private Button buttonResume = null;
	private Button buttonFinish = null;
	private Button buttonNote = null;
	private TextView txtDuration = null;
	private TextView txtDistance = null;
	private TextView txtAvgSpeed = null;
	private TextView txtCO2 = null;
	private TextView txtCalories = null;

	boolean isRecording = false;
	boolean isPaused = false;
	Timer timer;
	Timer timerWaitForServiceConnection;
	float curDistance;
	int zoomFlag = 1;

	Location currentLocation = new Location("");

	// Format used to show elapsed time to user when recording trips
	private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

	// *********************************************************************************
	// *
	// *********************************************************************************

	private GoogleMap map;
	private UiSettings mUiSettings;
	private LocationClient mLocationClient;

	private static final LocationRequest REQUEST = LocationRequest.create()
			.setInterval(5000) // 5 seconds
			.setFastestInterval(16) // 16ms = 60fps
			.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

	// *********************************************************************************
	// *                                Constructor
	// *********************************************************************************

	public FragmentMainInput() {

		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

	}

	// *********************************************************************************
	// *                              Fragment Handlers
	// *********************************************************************************

	/**
	 * Handler: onCreateView
	 * Update distance and speed user interface elements
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.v(MODULE_TAG, "onCreateView");

		View rootView = null;

		try {
			// Convenient pointer to global application object
			myApp = MyApplication.getInstance();

			// Create main user interface window
			rootView = inflater.inflate(R.layout.activity_main_input, container, false);

			// Initialize map parameters
			setUpMapIfNeeded();

			// Setup the button to start recording
			buttonStart = (Button) rootView.findViewById(R.id.buttonStart);
			buttonStart.setVisibility(View.GONE);
			//buttonStart.setWidth(140);
			buttonStart.setOnClickListener(new ButtonStart_OnClickListener());

			// Setup the button to pause recording
			buttonPause = (Button) rootView.findViewById(R.id.buttonPause);
			buttonPause.setVisibility(View.GONE);
			//buttonPause.setWidth(140);
			buttonPause.setOnClickListener(new ButtonPause_OnClickListener());

			// Setup the button to resume recording
			buttonResume = (Button) rootView.findViewById(R.id.buttonResume);
			buttonResume.setVisibility(View.GONE);
			//buttonResume.setWidth(140);
			buttonResume.setOnClickListener(new ButtonResume_OnClickListener());

			// Setup the button to finish recording
			buttonFinish = (Button) rootView.findViewById(R.id.buttonFinish);
			buttonFinish.setVisibility(View.GONE);
			//buttonFinish.setWidth(140);
			buttonFinish.setOnClickListener(new ButtonFinish_OnClickListener());

			// Setup the button to add a note to the trip
			buttonNote = (Button) rootView.findViewById(R.id.buttonNoteThis);
			buttonNote.setVisibility(View.GONE);
			//buttonNote.setWidth(140);
			buttonNote.setOnClickListener(new ButtonNote_OnClickListener());

			// Copy from Recording Activity
			txtDuration = (TextView) rootView.findViewById(R.id.textViewElapsedTime);
			txtDistance = (TextView) rootView.findViewById(R.id.textViewDistance);
			txtAvgSpeed = (TextView) rootView.findViewById(R.id.textViewAvgSpeed);
			txtCO2 = (TextView) rootView.findViewById(R.id.textViewCO2);
			txtCalories = (TextView) rootView.findViewById(R.id.textViewCalories);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}

		return rootView;
	}

	/**
	 * Set buttons according to application state
	 * @param appState
	 */
	private void setupButtons() {

		switch(recordingService.getState()) {

		case RecordingService.STATE_IDLE:

			buttonNote.setVisibility(View.GONE);
			buttonStart.setVisibility(View.VISIBLE);
			buttonPause.setVisibility(View.GONE);
			buttonResume.setVisibility(View.GONE);
			buttonFinish.setVisibility(View.GONE);
			break;

		case RecordingService.STATE_RECORDING:

			buttonNote.setVisibility(View.VISIBLE);
			buttonStart.setVisibility(View.GONE);
			buttonPause.setVisibility(View.VISIBLE);
			buttonResume.setVisibility(View.GONE);
			buttonFinish.setVisibility(View.VISIBLE);
			break;

		case RecordingService.STATE_PAUSED:

			buttonNote.setVisibility(View.VISIBLE);
			buttonStart.setVisibility(View.GONE);
			buttonPause.setVisibility(View.GONE);
			buttonResume.setVisibility(View.VISIBLE);
			buttonFinish.setVisibility(View.VISIBLE);
			break;

		case RecordingService.STATE_FULL:

			buttonNote.setVisibility(View.VISIBLE);
			buttonStart.setVisibility(View.GONE);
			buttonPause.setVisibility(View.GONE);
			buttonResume.setVisibility(View.GONE);
			buttonFinish.setVisibility(View.GONE);
			break;
		}
	}

    /**
     * Handler: onResume
     * Called when the <code>activity<code/> will start interacting with the user. At this point
     * the <code>activity<code/> is at the top of the <code>activity<code/> stack, with user
     * input going to it. Always followed by <code>onPause()<code/>.
     * @see <code>onPause<code/> class.
     */
	@Override
	public void onResume() {
		super.onResume();

		try {

			Log.v(MODULE_TAG, "onResume()");

			// Use a timer to update the trip duration.
			timer = new Timer();
			timer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					try {
						mHandler.post(mUpdateTimer);
					}
					catch(Exception ex) {
						Log.e(MODULE_TAG, ex.getMessage());
					}
				}
			}, 0, 1000); // every second

			setUpMapIfNeeded();
			if (map != null) {
				// Keep the UI Settings state in sync with the checkboxes.
				mUiSettings.setZoomControlsEnabled(true);
				mUiSettings.setCompassEnabled(true);
				mUiSettings.setMyLocationButtonEnabled(true);
				map.setMyLocationEnabled(true);
				mUiSettings.setScrollGesturesEnabled(true);
				mUiSettings.setZoomGesturesEnabled(true);
				mUiSettings.setTiltGesturesEnabled(true);
				mUiSettings.setRotateGesturesEnabled(true);
			}
			setUpLocationClientIfNeeded();
			mLocationClient.connect();

			// Setup wait for service connection timer

			if (null == recordingService) {
				timerWaitForServiceConnection = new Timer();
				timerWaitForServiceConnection.scheduleAtFixedRate(new TimerTask() {
					@Override
					public void run() {
						handlerWaitForServiceConnection.post(doServiceConnection);
					}
				}, 0, 1000); // every second
			}
			else {
				setupButtons();
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	/**
	 * Handler: onPause
	 */
	@Override
	public void onPause() {
		super.onPause();

		try {
			Log.v(MODULE_TAG, "Cycle: MainInput onPause");

			// Background GPS.
			if (timer != null)
				timer.cancel();

			// Background GPS.
			if (timerWaitForServiceConnection != null)
				timerWaitForServiceConnection.cancel();

			if (mLocationClient != null) {
				mLocationClient.disconnect();
			}

			if (recordingService != null) {
				recordingService.setListener(null);
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	/**
	 * Handler: onDestroyView
	 */
	@Override
	public void onDestroyView() {
		super.onDestroyView();

		try {
			Log.v(MODULE_TAG, "Cycle: MainInput onDestroyView");
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	/**
	 * setUpMapIfNeeded: Instantiate the map
	 */
	private void setUpMapIfNeeded() {

		// Do a null check to confirm that we have not already instantiated the map.
		if (map == null) {

			// Try to obtain the map from the SupportMapFragment.
			map = ((SupportMapFragment) getActivity()
					.getSupportFragmentManager().findFragmentById(R.id.map))
					.getMap();

			// Check if we were successful in obtaining the map.
			if (map != null) {
				map.setMyLocationEnabled(true);
				map.setOnMyLocationButtonClickListener(this);
				mUiSettings = map.getUiSettings();
				// centerMapOnMyLocation();
			}
		}
	}

	// *********************************************************************************
	// *                                Timers
	// *********************************************************************************

	// Need handler for callbacks to the UI thread
	final Handler mHandler = new Handler();
	final Runnable mUpdateTimer = new Runnable() {
		public void run() {
			try {
				updateTimer();
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	};

	/**
	 * Update the duration label
	 */
	private void updateTimer() {

		try {
			if (null != recordingService) {

				ApplicationStatus appStatus = myApp.getStatus();

				boolean isRecording =
						((RecordingService.STATE_RECORDING == recordingService.getState()) ||
						(RecordingService.STATE_PAUSED == recordingService.getState()));

				if (isRecording) {

					TripData tripData = appStatus.getTripData();

					if (null != tripData) {
						txtDuration.setText(sdf.format(tripData.getDuration()));
						double duration = tripData.getDuration() / 1000.0f;
						float distance = tripData.getDistance();
						float avgSpeedMps = (float) ((duration > 1.0f) ? (distance / duration): 0);
						this.updateStatus(distance, avgSpeedMps);
					}
				}
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	// *********************************************************************************
	// *                     WaitForServiceConnection Tasking
	// *********************************************************************************

	// Need handler for callbacks to the UI thread
	final Handler handlerWaitForServiceConnection = new Handler();

	/**
	 * Class: doServiceConnection
	 * This task is to be executed after onResume has occurred to assure we still
	 * have a reference to the recording service.  This would happen if the OS
	 * kicked the service out of memory while the owning activity was dormant.
	 */
	final Runnable doServiceConnection = new Runnable() {
		public void run() {

			try {
				Log.v(MODULE_TAG, "doServiceConnection");

				if (null == recordingService) {

					// See if a service connection has been established
					if (null != (recordingService = myApp.getRecordingService())) {

						// We now have connection to the service so cancel the timer
						timerWaitForServiceConnection.cancel();

						Toast.makeText(getActivity(), "Recording service connected...",
								Toast.LENGTH_SHORT).show();

						recordingService.setListener(FragmentMainInput.this);

						// Setup the UI buttons according to current state
						setupButtons();

						// If the recorder is has completed a recording, switch
						// to activity for uploading the trip data
						int state = recordingService.getState();
						if (state > RecordingService.STATE_IDLE) {
							if (state == RecordingService.STATE_FULL) {
								//TripData tripData = myApp.getStatus().getTripData();
								//boolean allowSave = (tripData.numpoints > 0);
								dialogTripFinish(true);
								//dialogTripFinish();
							}
						}
					}
				}
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		} // end of run
	};

	// *********************************************************************************
	// *                              Button Handlers
	// *********************************************************************************

	/**
     * Class: ButtonStart_OnClickListener
     *
     * Description: Callback to be invoked when startButton button is clicked
     */
	private final class ButtonStart_OnClickListener implements View.OnClickListener {

		/**
		 * Description: Handles onClick for view
		 */
		public void onClick(View v) {
			try {
				// Before we go to record, check GPS status
				if (!myApp.getStatus().isProviderEnabled()) {
					// Alert user GPS not available
					showDialogNoGps();
				}
				else {
					myApp.startRecording(getActivity());
				}
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
			finally {
				setupButtons();
			}
		}
	}

    /**
     * Class: ButtonPause_OnClickListener
     *
     * Description: Callback to be invoked when pauseButton button is clicked
     */
	private final class ButtonPause_OnClickListener implements View.OnClickListener {

		/**
		 * Description: Handles onClick for view
		 */
		public void onClick(View v) {
			try {
				recordingService.pauseRecording();
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
			finally {
				setupButtons();
			}
		}
	}

    /**
     * Class: ButtonResume_OnClickListener
     *
     * Description: Callback to be invoked when resumeButton button is clicked
     */
	private final class ButtonResume_OnClickListener implements View.OnClickListener {

		/**
		 * Description: Handles onClick for view
		 */
		public void onClick(View v) {
			try {
				recordingService.resumeRecording();
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
			finally {
				setupButtons();
			}
		}
	}

    /**
     * Class: ButtonFinish_OnClickListener
     *
     * Description: Callback to be invoked when ButtonFinish button is clicked
     */
	private final class ButtonFinish_OnClickListener implements View.OnClickListener {

		/**
		 * Description: Handles onClick for view
		 */
		public void onClick(View v) {
			try {
				recordingService.pauseRecording();
				setupButtons();

				TripData tripData = myApp.getStatus().getTripData();
				boolean allowSave = (tripData.numpoints > 0);
				dialogTripFinish(allowSave);

				if (tripData.numpoints > 0) {
				}
				else {
					// Otherwise, cancel and go back to main screen
					//alertUserNoGPSData();
					//cancelRecording();
					//setupButtons();
				}
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
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
				if (!myApp.getStatus().isProviderEnabled()) {
					showDialogNoGps();
				}
				else if (currentLocation == null) {
					alertUserNoGPSData();
				}
				else {
					// pause recording of trip data
					recordingService.pauseRecording();

					// update note entity
					long tripId = recordingService.getCurrentTripID();

					NoteData note = NoteData.createNote(getActivity(), tripId);
					note.updateNoteStatus(NoteData.STATUS_INCOMPLETE);
					note.setLocation(currentLocation);
					transitionToNoteQuestionsActivity(note, tripId);
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

	/**
	 * Updates the status of a trip being recorded.
	 * @param distanceMeters Distance travelled in meters
	 * @param avgSpeedMps Average speed in meters per second
	 */
	public void updateStatus(float distanceMeters, float avgSpeedMps) {

		this.curDistance = distanceMeters;

		float distanceMiles = distanceMeters * CF_METERS_TO_MILES;
		txtDistance.setText(String.format("%1.1f miles", distanceMiles));

		float avgSpeedMph = avgSpeedMps * METERS_PER_SECOND_TO_MILES_PER_HOUR;
		txtAvgSpeed.setText(String.format("%1.1f mph", avgSpeedMph));

		float calories = distanceMiles * CF1_MILES_TO_KCALORIES + CF2_MILES_TO_KCALORIES;
		if (calories < 0.0f) {
			calories = 0.0f;
		}
		txtCalories.setText(String.format("%1.1f kcal", calories));

		float lbsCO2 = distanceMiles * CF_MILES_TO_LBS_CO2;
		txtCO2.setText(String.format("%1.1f lbs", lbsCO2));
	}

	/**
	 * Cancels recording
	 */
	private void cancelRecording() {

		try {
			myApp.cancelRecording();

			isRecording = false;

			txtDuration = (TextView) getActivity().findViewById(R.id.textViewElapsedTime);
			txtDuration.setText("00:00:00");

			txtDistance = (TextView) getActivity().findViewById(R.id.textViewDistance);
			txtDistance.setText("0.0 miles");

			txtAvgSpeed = (TextView) getActivity().findViewById(R.id.textViewAvgSpeed);
			txtAvgSpeed.setText("0.0 mph");

			txtCalories = (TextView) getActivity().findViewById(R.id.textViewCalories);
			txtCalories.setText("0.0 kcal");

			txtCO2 = (TextView) getActivity().findViewById(R.id.textViewCO2);
			txtCO2.setText("0.0 lbs");

			setupButtons();
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	// *********************************************************************************
	// *                            No GPS Dialog
	// *********************************************************************************

	/**
	 * Build dialog telling user that the GPS is not available
	 */
	private void showDialogNoGps() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(
				getActivity());
		builder.setMessage(
				"Your phone's GPS is disabled. ORCycle needs GPS to determine your location.\n\nGo to System Settings now to enable GPS?")
				.setCancelable(false)
				.setPositiveButton("GPS Settings...",
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog,
									final int id) {
								transitionToLocationServices();
							}
						})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog,
									final int id) {
								dialog.cancel();
							}
						});
		final AlertDialog alert = builder.create();
		alert.show();
	}

	// *********************************************************************************
	// *                            Trip Finished Dialogs
	// *********************************************************************************

	/**
	 * Build dialog telling user to save this trip
	 */
	private void dialogTripFinish(boolean allowSave) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Save Trip");
		if (allowSave) {
			builder.setMessage("Do you want to save this trip?");
			builder.setNegativeButton("Save", new DialogTripFinish_OnSaveTripClicked());
		}
		else {
			builder.setMessage("No GPS data acquired; nothing to save.");
		}
		builder.setNeutralButton("Discard", new DialogTripFinish_OnDiscardTripClicked());
		builder.setPositiveButton("Resume", new DialogTripFinish_OnContinueTripClicked());
		final AlertDialog alert = builder.create();
		alert.show();
	}

	private final class DialogTripFinish_OnSaveTripClicked implements
			DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int id) {

			try {
				dialog.cancel();

				myApp.finishRecording();

				TripData tripData = myApp.getStatus().getTripData();

				if (tripData.numpoints > 0) {
					transitionToTripQuestionsActivity(tripData.tripid);
				}
				// Otherwise, cancel and go back to main screen
				else {
					alertUserNoGPSData();
					cancelRecording();
				}
				setupButtons();
			}
			catch (Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	private final class DialogTripFinish_OnDiscardTripClicked implements
			DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int id) {
			try {
				dialog.cancel();
				cancelRecording();
			}
			catch (Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			} finally {
				setupButtons();
			}
		}
	}

	private final class DialogTripFinish_OnContinueTripClicked implements
			DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int id) {
			try {
				dialog.cancel();
				recordingService.resumeRecording();
			} catch (Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			} finally {
				setupButtons();
			}
		}
	}

	// *********************************************************************************
	// *                            Map Location Tracking
	// *********************************************************************************

	/**
	 * Creates a new location client and ???
	 */
	private void setUpLocationClientIfNeeded() {
		if (mLocationClient == null) {
			mLocationClient = new LocationClient(getActivity(), this, this); // OnConnectionFailedListener // ConnectionCallbacks
		}
	}

	/**
	 * Implementation of {@link LocationListener}.
	 */
	@Override
	public void onLocationChanged(Location location) {
		try {
			// onMyLocationButtonClick();
			currentLocation = location;

			// Log.v("Jason", "Current Location: "+currentLocation);

			if (zoomFlag == 1) {
				LatLng myLocation;

				if (location != null) {
					myLocation = new LatLng(location.getLatitude(),
							location.getLongitude());
					map.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation,
							16));
					zoomFlag = 0;
				}
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	/**
	 * Callback called when connected to GCore. Implementation of
	 * {@link ConnectionCallbacks}.
	 */
	@Override
	public void onConnected(Bundle connectionHint) {
		try {
			mLocationClient.requestLocationUpdates(REQUEST, this); // LocationListener
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	/**
	 * Callback called when disconnected from GCore. Implementation of
	 * {@link ConnectionCallbacks}.
	 */
	@Override
	public void onDisconnected() {
		try {
		// Do nothing
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	/**
	 * Implementation of {@link OnConnectionFailedListener}.
	 */
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		try {
		// Do nothing
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	@Override
	public boolean onMyLocationButtonClick() {
		try {
		// Toast.makeText(getActivity(), "MyLocation button clicked",
		// Toast.LENGTH_SHORT).show();
		// Return false so that we don't consume the event and the default
		// behavior still occurs
		// (the camera animates to the user's current position).
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		return false;
	}

	// *********************************************************************************
	// *                            Misc & Helper Functions
	// *********************************************************************************

	private void alertUserNoGPSData() {
		Toast.makeText(getActivity(), "No GPS data acquired; nothing to submit.", Toast.LENGTH_SHORT).show();
	}

	private void transitionToTripQuestionsActivity(long tripId) {

		Intent intent = new Intent(getActivity(), TripQuestionsActivity.class);
		intent.putExtra(TripQuestionsActivity.EXTRA_TRIP_ID, tripId);
		startActivity(intent);
		getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		getActivity().finish();
	}

	private void transitionToNoteTypeActivity(NoteData note, long tripId) {

		// Setup intent to move to next activity
		Intent intent = new Intent(getActivity(), NoteTypeActivity.class);
		intent.putExtra(NoteTypeActivity.EXTRA_NOTE_ID, note.noteid);
		intent.putExtra(NoteTypeActivity.EXTRA_NOTE_TYPE, NoteTypeActivity.EXTRA_NOTE_TYPE_UNDEFINED);
		intent.putExtra(NoteTypeActivity.EXTRA_NOTE_SOURCE, NoteTypeActivity.EXTRA_NOTE_SOURCE_MAIN_INPUT);
		intent.putExtra(NoteTypeActivity.EXTRA_TRIP_ID, tripId);
		intent.putExtra(NoteTypeActivity.EXTRA_TRIP_SOURCE, NoteTypeActivity.EXTRA_TRIP_SOURCE_MAIN_INPUT);

		startActivity(intent);
		getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		// getActivity().finish();
	}

	private void transitionToNoteQuestionsActivity(NoteData note, long tripId) {

		Intent intent = new Intent(getActivity(), NoteQuestionsActivity.class);
		intent.putExtra(NoteQuestionsActivity.EXTRA_NOTE_ID, note.noteid);
		intent.putExtra(NoteQuestionsActivity.EXTRA_NOTE_TYPE, NoteTypeActivity.EXTRA_NOTE_TYPE_UNDEFINED);
		intent.putExtra(NoteQuestionsActivity.EXTRA_NOTE_SOURCE, NoteTypeActivity.EXTRA_NOTE_SOURCE_MAIN_INPUT);
		intent.putExtra(NoteQuestionsActivity.EXTRA_TRIP_ID, tripId);
		intent.putExtra(NoteQuestionsActivity.EXTRA_TRIP_SOURCE, NoteTypeActivity.EXTRA_TRIP_SOURCE_MAIN_INPUT);
		startActivity(intent);
		getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		getActivity().finish();
	}

	private void transitionToLocationServices() {
		final ComponentName toLaunch = new ComponentName(
				"com.android.settings",
				"com.android.settings.SecuritySettings");
		final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setComponent(toLaunch);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivityForResult(intent, 0);
	}

}