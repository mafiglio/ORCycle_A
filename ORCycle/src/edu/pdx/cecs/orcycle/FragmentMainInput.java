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

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
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

	private static final int FMI_USER_PAUSED = 1;
	private static final int FMI_NOTE_PAUSED = 2;
	private static final int FMI_FINISH_PAUSED = 3;

	public enum Result {UNDEFINED, SAVE_TRIP, REPORT, NO_GPS, GET_USER_INFO, SHOW_INSTRUCTIONS,
		SHOW_WELCOME, SHOW_DIALOG_USER_INFO, SHOW_TUTORIAL };

	private Result result;

	// Reference to Global application object
	private MyApplication myApp = null;
	private Controller controller = null;

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

	private boolean backFromInstructions;

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
		result = Result.UNDEFINED;

		try {
			// Convenient pointer to global application object
			myApp = MyApplication.getInstance();
			controller = myApp.getController();

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
			buttonNote.setVisibility(View.GONE);
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
			if (backFromInstructions) {
				backFromInstructions = false;
				if (myApp.getUserWelcomeEnabled()) {
					controller.finish(setResult(Result.SHOW_WELCOME));
					return;
				}
			}
			// Check for responses from User dialogs
			else if (null != (intent = getActivity().getIntent())) {
				if (null != (bundle = intent.getExtras())) {
					String src = bundle.getString(TabsConfig.EXTRA_DSA_ACTIVITY);
					if (null != src) {
						// Respond to the dialog button that was pressed
						int dialogId = bundle.getInt(TabsConfig.EXTRA_DSA_DIALOG_ID, -1);
						int buttonPressed = bundle.getInt(TabsConfig.EXTRA_DSA_BUTTON_PRESSED, -1);
						boolean ischecked = bundle.getBoolean(DsaDialogActivity.EXTRA_IS_CHECKED, false);

						// Check for Welcome Dialog
						if (Controller.DSA_ID_WELCOME_DIALOG_ID == dialogId) {
							if (ischecked) {
								myApp.setUserWelcomeEnabled(false);
							}
							if (Controller.DSA_ID_WELCOME_DIALOG_INSTRUCTIONS == buttonPressed) {
								controller.finish(setResult(Result.SHOW_INSTRUCTIONS));
								backFromInstructions = false;
								return;
							}
						}
						// Check for HowTo dialog
						else if (Controller.DSA_ID_HOW_TO_DIALOG_ID == dialogId) {
							if (ischecked) {
								myApp.setTutorialEnabled(false);
							}
							else if (controller.setNextHowToScreen()){
								controller.finish(setResult(Result.SHOW_TUTORIAL));
								return;
							}
						}
						// Check for UserInfo dialog
						else if (Controller.DSA_ID_USER_PROFILE_DIALOG_ID == dialogId) {
							if (ischecked) {
								myApp.setUserProfileUploaded(true);
							}
							if (Controller.DSA_ID_USER_PROFILE_DIALOG_OK == buttonPressed) {
								controller.finish(setResult(Result.GET_USER_INFO));
								return;
							}
						}
						else {
							// got a bundle, from unknown dialog?
						}
					}
				}
			}

			// Show next dialog in sequence
			if (!myApp.getCheckedForUserWelcome() && myApp.getUserWelcomeEnabled()) {
				myApp.setCheckedForUserWelcome(true);
				controller.finish(setResult(Result.SHOW_WELCOME));
			}
			else if (!myApp.getCheckedForTutorial() && myApp.getTutorialEnabled()) {
				myApp.setCheckedForTutorial(true);
				controller.finish(setResult(Result.SHOW_TUTORIAL));
			}
			else if (!myApp.getCheckedForUserProfile()
					&& !myApp.getUserProfileUploaded()
					&& (myApp.getFirstTripCompleted())) {
				myApp.setCheckedForUserProfile(true);
				controller.finish(setResult(Result.SHOW_DIALOG_USER_INFO));
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

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch(requestCode) {
		case Controller.DSA_ID_WELCOME_DIALOG_ID:
			backFromInstructions = true;
			Log.e(MODULE_TAG, "back from ORcycle");
			break;
		case Controller.DSA_ID_USER_PROFILE_DIALOG_ID:
			break;
		}
	}

	/**
	 * Set up buttons, and enable status updates
	 */
	private void syncDisplayToRecordingState() {

		switch(recordingService.getState()) {

		case RecordingService.STATE_IDLE:
			setupButtons();
			break;

		case RecordingService.STATE_RECORDING:
			setupButtons();
			break;

		case RecordingService.STATE_PAUSED:
			if (recordingService.pauseId() == FMI_NOTE_PAUSED) {
				recordingService.resumeRecording();
			}
			setupButtons();
			break;

		case RecordingService.STATE_FULL:
			setupButtons();
			dialogTripFinish(true);
			break;

		default:
			Log.e(MODULE_TAG, "Undefined recording state encountered");
			break;
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
				recordingService.pauseRecording(FMI_USER_PAUSED);
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
				recordingService.pauseRecording(FMI_FINISH_PAUSED);
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
				if (null == recordingService) {
					Log.e(MODULE_TAG, "Connection to recording service not yet established");
					return;
				}

				long tripId;

				switch(recordingService.getState()) {

				case RecordingService.STATE_IDLE:
					tripId = 0;
					break;

				case RecordingService.STATE_RECORDING:
					recordingService.pauseRecording(FMI_NOTE_PAUSED);
					tripId = recordingService.getCurrentTripID();
					break;

				case RecordingService.STATE_PAUSED:
					tripId = recordingService.getCurrentTripID();
					break;

				case RecordingService.STATE_FULL:
					tripId = recordingService.getCurrentTripID();
					break;

				default:
					Log.e(MODULE_TAG, "Invalid recording service state encountered.");
					return;
				}

				NoteData note = NoteData.createNote(getActivity(), tripId);
				note.updateNoteStatus(NoteData.STATUS_INCOMPLETE);
				controller.finish(setResult(Result.REPORT), tripId, note.getNoteId());
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	private FragmentMainInput setResult(Result result) {
		this.result = result;
		return this;
	}

	public FragmentMainInput.Result getResult() {
		return this.result;
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
			controller.finish(setResult(Result.NO_GPS));
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
					controller.finish(setResult(Result.SAVE_TRIP), tripData.tripid);
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

}