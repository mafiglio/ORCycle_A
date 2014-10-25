package edu.pdx.cecs.orcycle;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
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
	private static final String COM_ANDROID_SETTINGS = "com.android.settings";
	private static final String COM_ANDROID_SETTINGS_SECURITY_SETTINGS = "com.android.settings.SecuritySettings";

	private static final int DSA_ID_WELCOME_DIALOG_ID = 1000;
	private static final int DSA_ID_WELCOME_DIALOG_CONTINUE = 1001;
	private static final int DSA_ID_WELCOME_DIALOG_OK = 1002;

	private static final int DSA_ID_USER_PROFILE_DIALOG_ID = 2000;
	private static final int DSA_ID_USER_PROFILE_DIALOG_OK = 2001;
	private static final int DSA_ID_USER_PROFILE_DIALOG_LATER = 2002;

	// Reference to Global application object
	private MyApplication myApp = null;

	// Reference to recording service;
	private IRecordService recordingService;

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

	private Timer statusUpdateTimer;
	final Handler serviceConnectionHandler = new Handler();
	private Timer serviceConnectionTimer;
	final Handler taskHandler = new Handler();
	private Location currentLocation = null;

	// Format used to show elapsed time to user when recording trips
	private final SimpleDateFormat tripDurationFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);

	// *********************************************************************************
	// *
	// *********************************************************************************

	private GoogleMap map;
	private LocationClient mLocationClient;

	private static final LocationRequest REQUEST = LocationRequest.create()
			.setInterval(5000) // 5 seconds
			.setFastestInterval(16) // 16ms = 60fps
			.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

	// *********************************************************************************
	// *                                Constructor
	// *********************************************************************************

	public FragmentMainInput() {
		tripDurationFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
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

		Log.v(MODULE_TAG, "Cycle: onCreateView()");

		View rootView = null;

		try {
			// Convenient pointer to global application object
			myApp = MyApplication.getInstance();

			// Folloing line for debugging only
			//myApp.setUserProfileUploaded(false);

			// Create main user interface window
			rootView = inflater.inflate(R.layout.activity_main_input, container, false);

			// Initialize map parameters
			setUpMapIfNeeded();

			// Setup the button to start recording
			buttonStart = (Button) rootView.findViewById(R.id.buttonStart);
			buttonStart.setVisibility(View.GONE);
			buttonStart.setOnClickListener(new ButtonStart_OnClickListener());

			// Setup the button to pause recording
			buttonPause = (Button) rootView.findViewById(R.id.buttonPause);
			buttonPause.setVisibility(View.GONE);
			buttonPause.setOnClickListener(new ButtonPause_OnClickListener());

			// Setup the button to resume recording
			buttonResume = (Button) rootView.findViewById(R.id.buttonResume);
			buttonResume.setVisibility(View.GONE);
			buttonResume.setOnClickListener(new ButtonResume_OnClickListener());

			// Setup the button to finish recording
			buttonFinish = (Button) rootView.findViewById(R.id.buttonFinish);
			buttonFinish.setVisibility(View.GONE);
			buttonFinish.setOnClickListener(new ButtonFinish_OnClickListener());

			// Setup the button to add a note to the trip
			buttonNote = (Button) rootView.findViewById(R.id.btn_ami_note_this);
			buttonNote.setVisibility(View.VISIBLE);
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

			buttonNote.setVisibility(View.VISIBLE);
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
			Log.v(MODULE_TAG, "Cycle: onResume()");

			setUpMapIfNeeded();
			if (map != null) {
				UiSettings mUiSettings = map.getUiSettings();
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
			enableLocationClient();

			Intent intent;
			Bundle bundle;
			if (null != (intent = getActivity().getIntent())) {
				if (null != (bundle = intent.getExtras())) {
					String src = bundle.getString(TabsConfig.EXTRA_DSA_ACTIVITY);
					if (null != src) {
						int dialogId = bundle.getInt(TabsConfig.EXTRA_DSA_DIALOG_ID, -1);
						int buttonPressed = bundle.getInt(TabsConfig.EXTRA_DSA_BUTTON_PRESSED, -1);
						if (DSA_ID_WELCOME_DIALOG_ID == dialogId) {
							if (DSA_ID_WELCOME_DIALOG_OK == buttonPressed) {
								transitionToORcycle();
								getActivity().finish();
								return;
							}
						}
						else if (DSA_ID_USER_PROFILE_DIALOG_ID == dialogId) {
							if (DSA_ID_USER_PROFILE_DIALOG_OK == buttonPressed) {
								transitionToUserInfoActivity();
								getActivity().finish();
								return;
							}
						}
						else {
							// got a bundle, from unknown dialog?
						}
					}
				}
			}

			if (!myApp.getCheckedForUserWelcome() && myApp.getUserWelcomeEnabled()) {
				myApp.setCheckedForUserWelcome(true);
				transitionToDialogWelcome();
			}
			else if (!myApp.getCheckedForUserProfile()
					&& !myApp.getUserProfileUploaded()
					&& (myApp.getFirstTripCompleted())) {
				myApp.setCheckedForUserProfile(true);
				transitionToDialogUserInfo();
			}
			else if (null == recordingService) {
				scheduleServiceConnect();
			}
			else {
				syncDisplayToRecordingState();
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	/**
	 * Set up buttons, and enable status updates
	 */
	private void syncDisplayToRecordingState() {
		// Setup the UI buttons according to current state
		setupButtons();

		// If the recorder is has completed a recording, switch
		// to activity for uploading the trip data
		int state = recordingService.getState();
		if (state > RecordingService.STATE_IDLE) {
			if (state == RecordingService.STATE_FULL) {
				dialogTripFinish(true);
				//scheduleTask(TASK_WAIT_SERVICE_CONNECT);
			}
		}
		scheduleStatusUpdates(); // every second
	}

	/**
	 * Creates and schedules a timer to update the trip duration.
	 */
	private void scheduleStatusUpdates() {
		statusUpdateTimer = new Timer();
		statusUpdateTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				try {
					tripStatusHandler.post(tripStatusUpdateTask);
				}
				catch(Exception ex) {
					Log.e(MODULE_TAG, ex.getMessage());
				}
			}
		}, 0, 1000); // every second
	}

	private void scheduleServiceConnect() {
		serviceConnectionTimer = new Timer();
		serviceConnectionTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				serviceConnectionHandler.post(doServiceConnection);
			}
		}, 2000, 2000); // 2 second delay, at 2 second intervals
	}

	// *********************************************************************************
	// *
	// *********************************************************************************

	/**
	 * Handler: onPause
	 */
	@Override
	public void onPause() {
		super.onPause();

		try {
			Log.v(MODULE_TAG, "Cycle: onPause()");

			if (statusUpdateTimer != null)
				statusUpdateTimer.cancel();

			if (serviceConnectionTimer != null)
				serviceConnectionTimer.cancel();

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
			Log.v(MODULE_TAG, "Cycle: onDestroyView()");
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
				moveCameraToOregon();
			}
		}
	}

	// *********************************************************************************
	// *                   Trip Status Update Timers and Runnables
	// *********************************************************************************

	// Need handler for callbacks to the UI thread
	final Handler tripStatusHandler = new Handler();
	final Runnable tripStatusUpdateTask = new Runnable() {
		public void run() {
			try {
				updateTripStatus();
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	};

	/**
	 * Update the duration label
	 */
	private void updateTripStatus() {

		try {
			if (null != recordingService) {

				ApplicationStatus appStatus = myApp.getStatus();

				boolean isRecording =
						((RecordingService.STATE_RECORDING == recordingService.getState()) ||
						(RecordingService.STATE_PAUSED == recordingService.getState()));

				if (isRecording) {

					TripData tripData = appStatus.getTripData();

					if (null != tripData) {
						txtDuration.setText(tripDurationFormat.format(tripData.getDuration(true)));
						this.updateStatus(tripData.getDistance(), tripData.getAvgSpeedMps(true));
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

	/**
	 * Class: doServiceConnection
	 * This task is to be executed after onResume has occurred to assure we still
	 * have a reference to the recording service.  This would happen if the OS
	 * kicked the service out of memory while the owning activity was dormant.
	 */
	final Runnable doServiceConnection = new Runnable() {
		public void run() {

			try {

				if (null == recordingService) {
					Log.v(MODULE_TAG, "doServiceConnection(): Service not yet connected.");

					// See if a service connection has been established
					if (null != (recordingService = myApp.getRecordingService())) {
						Log.v(MODULE_TAG, "doServiceConnection(): got connection!");

						// We now have connection to the service so cancel the timer
						serviceConnectionTimer.cancel();

						recordingService.setListener(FragmentMainInput.this);
						//scheduleTask(TASK_SERVICE_CONNECT_COMPLETE);
						syncDisplayToRecordingState();
					}
				}
				else {
					Log.v(MODULE_TAG, "doServiceConnection(): Service already connected.");
					serviceConnectionTimer.cancel();
					syncDisplayToRecordingState();
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
					dialogNoGps();
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
				boolean allowSave = (tripData.getNumPoints() > 0);
				dialogTripFinish(allowSave);
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
					dialogNoGps();
				}
				else if (currentLocation == null) {
					alertUserNoGPSData();
				}
				else {
					int state = recordingService.getState();
					long tripId;
					if (state > RecordingService.STATE_IDLE) {
						// pause recording of trip data
						recordingService.pauseRecording();
						// update note entity
						tripId = recordingService.getCurrentTripID();
					}
					else {
						tripId = 0;
					}
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

			txtDuration = (TextView) getActivity().findViewById(R.id.textViewElapsedTime);
			txtDuration.setText(getResources().getString(R.string.fmi_reset_duration));

			txtDistance = (TextView) getActivity().findViewById(R.id.textViewDistance);
			txtDistance.setText(getResources().getString(R.string.fmi_reset_distance));

			txtAvgSpeed = (TextView) getActivity().findViewById(R.id.textViewAvgSpeed);
			txtAvgSpeed.setText(getResources().getString(R.string.fmi_reset_avg_speed));

			txtCalories = (TextView) getActivity().findViewById(R.id.textViewCalories);
			txtCalories.setText(getResources().getString(R.string.fmi_reset_calories));

			txtCO2 = (TextView) getActivity().findViewById(R.id.textViewCO2);
			txtCO2.setText(getResources().getString(R.string.fmi_reset_co2));

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
	private void dialogNoGps() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(getResources().getString(R.string.fmi_no_gps));
		builder.setCancelable(false);
		builder.setPositiveButton(getResources().getString(R.string.fmi_no_gps_dialog_ok),
				new DialogNoGps_OkListener());
		builder.setNegativeButton(getResources().getString(R.string.fmi_no_gps_dialog_cancel),
				new DialogNoGps_CancelListener());
		final AlertDialog alert = builder.create();
		alert.show();
	}

	private final class DialogNoGps_OkListener implements DialogInterface.OnClickListener {
		public void onClick(final DialogInterface dialog, final int id) {
			transitionToLocationServices();
		}
	}

	private final class DialogNoGps_CancelListener implements DialogInterface.OnClickListener {
		public void onClick(final DialogInterface dialog, final int id) {
			dialog.cancel();
		}
	}

	// *********************************************************************************
	// *                            Trip Finished Dialogs
	// *********************************************************************************

	/**
	 * Build dialog telling user to save this trip
	 */
	private void dialogTripFinish(boolean allowSave) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(getResources().getString(R.string.fmi_dtf_save_trip));
		if (allowSave) {
			builder.setMessage(getResources().getString(R.string.fmi_dtf_query_save));
			builder.setNegativeButton(getResources().getString(R.string.fmi_dtf_save),
					new DialogTripFinish_OnSaveTripClicked());
		}
		else {
			builder.setMessage(getResources().getString(R.string.fmi_no_gps_data));
		}
		builder.setNeutralButton(getResources().getString(R.string.fmi_dtf_discard),
				new DialogTripFinish_OnDiscardTripClicked());
		builder.setPositiveButton(getResources().getString(R.string.fmi_dtf_resume),
				new DialogTripFinish_OnContinueTripClicked());
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

				if (tripData.getNumPoints() > 0) {
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
	 * Creates a new location client and connects to it
	 */
	private void enableLocationClient() {
		if (mLocationClient == null) {
			mLocationClient = new LocationClient(getActivity(), this, this); // OnConnectionFailedListener // ConnectionCallbacks
		}
		mLocationClient.connect();
	}

	/**
	 * Implementation of {@link LocationListener}.
	 */
	@Override
	public void onLocationChanged(Location location) {
		try {
			currentLocation = new Location(location);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	private void moveCameraToMyLocation(Location location) {
		LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
		map.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 16));
	}

	private void moveCameraToOregon() {
		LatLng myLocation = new LatLng(43.8041334 , -120.55420119999996 );
		map.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 6));
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
		return false;
	}

	// *********************************************************************************
	// *                            Misc & Helper Functions
	// *********************************************************************************

	private void alertUserNoGPSData() {
		Toast.makeText(getActivity(),
			getResources().getString(R.string.fmi_no_gps_data),
			Toast.LENGTH_SHORT).show();
	}

	// *********************************************************************************
	// *                            Transitions
	// *********************************************************************************

	private void transitionToTripQuestionsActivity(long tripId) {

		Intent intent = new Intent(getActivity(), TripQuestionsActivity.class);
		intent.putExtra(TripQuestionsActivity.EXTRA_TRIP_ID, tripId);
		startActivity(intent);
		getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		getActivity().finish();
	}

	private void transitionToNoteQuestionsActivity(NoteData note, long tripId) {

		Intent intent = new Intent(getActivity(), NoteQuestionsActivity.class);
		intent.putExtra(NoteQuestionsActivity.EXTRA_NOTE_ID, note.noteId);
		intent.putExtra(NoteQuestionsActivity.EXTRA_NOTE_TYPE, NoteQuestionsActivity.EXTRA_NOTE_TYPE_UNDEFINED);
		intent.putExtra(NoteQuestionsActivity.EXTRA_NOTE_SOURCE, NoteQuestionsActivity.EXTRA_NOTE_SOURCE_MAIN_INPUT);
		intent.putExtra(NoteQuestionsActivity.EXTRA_TRIP_ID, tripId);
		intent.putExtra(NoteQuestionsActivity.EXTRA_TRIP_SOURCE, NoteQuestionsActivity.EXTRA_TRIP_SOURCE_MAIN_INPUT);
		startActivity(intent);
		getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		getActivity().finish();
	}

	private void transitionToLocationServices() {
		final ComponentName toLaunch = new ComponentName(
				COM_ANDROID_SETTINGS,
				COM_ANDROID_SETTINGS_SECURITY_SETTINGS);
		final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setComponent(toLaunch);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivityForResult(intent, 0);
	}

	private void transitionToUserInfoActivity() {

		// Create intent to come back to this activity
		Intent intent = new Intent(getActivity(), UserInfoActivity.class);
		intent.putExtra(UserInfoActivity.EXTRA_PREVIOUS_ACTIVITY, UserInfoActivity.EXTRA_FRAGMENT_MAIN_INPUT);

		// Exit this activity
		startActivity(intent);
		getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		getActivity().finish();
	}

	private void transitionToORcycle() {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(MyApplication.ORCYCLE_URI));
		startActivity(intent);
		getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	}

	private void transitionToDialogWelcome() {

		Intent intent = new Intent(getActivity(), DsaDialogActivity.class);

		String title = getResources().getString(R.string.fmi_welcome_title);
		String message = getResources().getString(R.string.fmi_welcome_message);
		String positiveText = getResources().getString(R.string.fmi_welcome_continue);
		String negativeText = getResources().getString(R.string.fmi_welcome_instructions);

		intent.putExtra(DsaDialogActivity.EXTRA_DIALOG_ID, DSA_ID_WELCOME_DIALOG_ID);
		intent.putExtra(DsaDialogActivity.EXTRA_TITLE, title);
		intent.putExtra(DsaDialogActivity.EXTRA_MESSAGE, message);
		intent.putExtra(DsaDialogActivity.EXTRA_POSITIVE_TEXT, positiveText);
		intent.putExtra(DsaDialogActivity.EXTRA_POSITIVE_ID, DSA_ID_WELCOME_DIALOG_CONTINUE);
		intent.putExtra(DsaDialogActivity.EXTRA_NEGATIVE_TEXT, negativeText);
		intent.putExtra(DsaDialogActivity.EXTRA_NEGATIVE_ID, DSA_ID_WELCOME_DIALOG_OK);
		startActivity(intent);
	}

	private void transitionToDialogUserInfo() {

		Intent intent = new Intent(getActivity(), DsaDialogActivity.class);

		String title = getResources().getString(R.string.fmi_query_user_profile_title);
		String message = getResources().getString(R.string.fmi_query_user_profile);
		String positiveText = getResources().getString(R.string.fmi_qup_dialog_ok);
		String negativeText = getResources().getString(R.string.fmi_qup_dialog_later);

		intent.putExtra(DsaDialogActivity.EXTRA_DIALOG_ID, DSA_ID_USER_PROFILE_DIALOG_ID);
		intent.putExtra(DsaDialogActivity.EXTRA_TITLE, title);
		intent.putExtra(DsaDialogActivity.EXTRA_MESSAGE, message);
		intent.putExtra(DsaDialogActivity.EXTRA_POSITIVE_TEXT, positiveText);
		intent.putExtra(DsaDialogActivity.EXTRA_POSITIVE_ID, DSA_ID_USER_PROFILE_DIALOG_OK);
		intent.putExtra(DsaDialogActivity.EXTRA_NEGATIVE_TEXT, negativeText);
		intent.putExtra(DsaDialogActivity.EXTRA_NEGATIVE_ID, DSA_ID_USER_PROFILE_DIALOG_LATER);
		startActivity(intent);
	}
}