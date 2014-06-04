package edu.pdx.cecs.orcycle;

import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
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
	private TextView txtCurSpeed = null;

	Intent fi;
	boolean isRecording = false;
	boolean isPaused = false;
	Timer timer;
	Timer timerWaitForServiceConnection;
	float curDistance;

	int zoomFlag = 1;

	Location currentLocation = new Location("");

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
			txtCurSpeed = (TextView) rootView.findViewById(R.id.textViewSpeed);
		}
		catch(Exception ex) {
			Log.e("Jason", ex.getMessage());
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

		Log.v(MODULE_TAG, "Cycle: MainInput onResume");

		// Use a timer to update the trip duration.
		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				mHandler.post(mUpdateTimer);
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

	/**
	 * Handler: onPause
	 */
	@Override
	public void onPause() {
		super.onPause();

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

	/**
	 * Handler: onDestroyView
	 */
	@Override
	public void onDestroyView() {
		super.onDestroyView();

		Log.v(MODULE_TAG, "Cycle: MainInput onDestroyView");

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
			updateTimer();
		}
	};

	/**
	 * Update the duration label
	 */
	private void updateTimer() {

		if (null != recordingService) {
			ApplicationStatus appStatus = myApp.getStatus();

			TripData tripData = appStatus.getTripData();

			boolean isRecording =
					((RecordingService.STATE_RECORDING == recordingService.getState()) ||
					(RecordingService.STATE_PAUSED == recordingService.getState()));

			if ((null != tripData) && isRecording) {
				txtDuration.setText(sdf.format(tripData.getDuration()));
			}
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
							startActivity(new Intent(getActivity(), TripPurposeActivity.class));
							getActivity().finish();
						}
					}
				}
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
					buildAlertMessageNoGps();
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
			buildAlertMessageSaveClicked();
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

			if (!myApp.getStatus().isProviderEnabled()) {
				buildAlertMessageNoGps();
			}
			else {
				fi = new Intent(getActivity(), NoteTypeActivity.class);
				// update note entity
				NoteData note;
				note = NoteData.createNote(getActivity());

				fi.putExtra("noteid", note.noteid);

				Log.v("Jason", "Note ID in MainInput: " + note.noteid);

				if (isRecording == true) {
					fi.putExtra("isRecording", 1);
				} else {
					fi.putExtra("isRecording", 0);
				}

				note.updateNoteStatus(NoteData.STATUS_INCOMPLETE);

				double currentTime = System.currentTimeMillis();

				if (currentLocation != null) {
					note.addPointNow(currentLocation, currentTime);

					// Log.v("Jason", "Note ID: "+note);

					startActivity(fi);
					getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
					// getActivity().finish();
				}
				else {
					Toast.makeText(getActivity(), "No GPS data acquired; nothing to submit.", Toast.LENGTH_SHORT).show();
				}
			}
		}
	}

	// *********************************************************************************
	// *
	// *********************************************************************************

	/**
	 * Update distance and speed user interface elements
	 */
	public void updateStatus(int points, float distance, float spdCurrent, float spdMax) {

		this.curDistance = distance;

		txtCurSpeed.setText(String.format("%1.1f mph", spdCurrent));

		float miles = 0.0006212f * distance;
		txtDistance.setText(String.format("%1.1f miles", miles));
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

			txtCurSpeed = (TextView) getActivity().findViewById(R.id.textViewSpeed);
			txtCurSpeed.setText("0.0 mph");

			setupButtons();
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	// *********************************************************************************
	// *                            Supporting Dialogs
	// *********************************************************************************

	/**
	 * Build dialog telling user that the GPS is not available
	 */
	private void buildAlertMessageNoGps() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(
				getActivity());
		builder.setMessage(
				"Your phone's GPS is disabled. ORCycle needs GPS to determine your location.\n\nGo to System Settings now to enable GPS?")
				.setCancelable(false)
				.setPositiveButton("GPS Settings...",
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog,
									final int id) {
								final ComponentName toLaunch = new ComponentName(
										"com.android.settings",
										"com.android.settings.SecuritySettings");
								final Intent intent = new Intent(
										Settings.ACTION_LOCATION_SOURCE_SETTINGS);
								intent.addCategory(Intent.CATEGORY_LAUNCHER);
								intent.setComponent(toLaunch);
								intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								startActivityForResult(intent, 0);
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

	/**
	 * Build dialog telling user to save this trip
	 */
	private void buildAlertMessageSaveClicked() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Save Trip");
		builder.setMessage("Do you want to save this trip?");
		builder.setNegativeButton("Save",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
						// save
						// If we have points, go to the save-trip activity
						// trip.numpoints > 0

						myApp.finishRecording();
						int numTripPoints = myApp.getStatus().getTripData().numpoints;
						if (numTripPoints > 0) {
							// Save trip so far (points and extent, but no purpose or notes)
							fi = new Intent(getActivity(), TripPurposeActivity.class);

							startActivity(fi);
							getActivity().overridePendingTransition(
									R.anim.slide_in_right,
									R.anim.slide_out_left);
							getActivity().finish();
						}
						// Otherwise, cancel and go back to main screen
						else {
							Toast.makeText(getActivity(),
									"No GPS data acquired; nothing to submit.",
									Toast.LENGTH_SHORT).show();

							cancelRecording();
						}
						setupButtons();
					}
				});

		builder.setNeutralButton("Discard",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
						// discard
						cancelRecording();
						setupButtons();
					}
				});

		builder.setPositiveButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
						// continue
						setupButtons();
					}
				});
		final AlertDialog alert = builder.create();
		alert.show();
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

	/**
	 * Callback called when connected to GCore. Implementation of
	 * {@link ConnectionCallbacks}.
	 */
	@Override
	public void onConnected(Bundle connectionHint) {
		mLocationClient.requestLocationUpdates(REQUEST, this); // LocationListener
	}

	/**
	 * Callback called when disconnected from GCore. Implementation of
	 * {@link ConnectionCallbacks}.
	 */
	@Override
	public void onDisconnected() {
		// Do nothing
	}

	/**
	 * Implementation of {@link OnConnectionFailedListener}.
	 */
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// Do nothing
	}

	@Override
	public boolean onMyLocationButtonClick() {
		// Toast.makeText(getActivity(), "MyLocation button clicked",
		// Toast.LENGTH_SHORT).show();
		// Return false so that we don't consume the event and the default
		// behavior still occurs
		// (the camera animates to the user's current position).
		return false;
	}

	// *********************************************************************************
	// * Dead Code
	// *********************************************************************************

	/**
	 * Start recording
	 */
	private void XXXstartRecording() {

		// Query the RecordingService to figure out what to do.
		final Button startButton = (Button) getActivity().findViewById(R.id.buttonStart);
		Intent rService = new Intent(getActivity(), RecordingService.class);
		getActivity().startService(rService);
		ServiceConnection sc = new ServiceConnection() {
			public void onServiceDisconnected(ComponentName name) {
			}

			public void onServiceConnected(ComponentName name, IBinder service) {
				IRecordService rs = (IRecordService) service;
				TripData trip = myApp.getStatus().getTripData();

				switch (rs.getState()) {
				case RecordingService.STATE_IDLE:
					trip = TripData.createTrip(getActivity());
					rs.startRecording(trip);
					isRecording = true;
					startButton.setText("Save");
					// startButton.setBackgroundColor(0xFF0000);
					// MainInputActivity.this.pauseButton.setEnabled(true);
					// MainInputActivity.this
					// .setTitle("Cycle Atlanta - Recording...");
					break;
				case RecordingService.STATE_RECORDING:
					long id = rs.getCurrentTripID();
					trip = TripData.fetchTrip(getActivity(), id);
					isRecording = true;
					startButton.setText("Save");
					// startButton.setBackgroundColor(0xFF0000);
					// MainInputActivity.this.pauseButton.setEnabled(true);
					// MainInputActivity.this
					// .setTitle("Cycle Atlanta - Recording...");
					break;
				// case RecordingService.STATE_PAUSED:
				// long tid = rs.getCurrentTrip();
				// isRecording = false;
				// trip = TripData.fetchTrip(MainInputActivity.this, tid);
				// // MainInputActivity.this.pauseButton.setEnabled(true);
				// // MainInputActivity.this.pauseButton.setText("Resume");
				// // MainInputActivity.this
				// // .setTitle("Cycle Atlanta - Paused...");
				// break;
				case RecordingService.STATE_FULL:
					// Should never get here, right?
					break;
				}
				rs.setListener((FragmentMainInput) getActivity()
						.getSupportFragmentManager().findFragmentByTag(
								"android:switcher:" + R.id.pager + ":0"));
				getActivity().unbindService(this);
			}
		};
		getActivity().bindService(rService, sc, Context.BIND_AUTO_CREATE);

		isRecording = true;
	}


}