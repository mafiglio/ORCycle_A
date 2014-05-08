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
import android.location.LocationManager;
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

import edu.pdx.cecs.orcycle.R;

public class FragmentMainInput extends Fragment implements ConnectionCallbacks,
		OnConnectionFailedListener, LocationListener,
		OnMyLocationButtonClickListener {

	public static final String ARG_SECTION_NUMBER = "section_number";

	Intent fi;
	TripData trip;
	NoteData note;
	boolean isRecording = false;
	Timer timer;
	float curDistance;

	TextView txtDuration;
	TextView txtDistance;
	TextView txtCurSpeed;

	int zoomFlag = 1;

	Location currentLocation = new Location("");

	final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

	// Need handler for callbacks to the UI thread
	final Handler mHandler = new Handler();
	final Runnable mUpdateTimer = new Runnable() {
		public void run() {
			updateTimer();
		}
	};

	private final static int MENU_USER_INFO = 0;
	private final static int MENU_HELP = 1;

	private final static int CONTEXT_RETRY = 0;
	private final static int CONTEXT_DELETE = 1;

	DbAdapter mDb;
	GoogleMap map;
	UiSettings mUiSettings;
	private LocationClient mLocationClient;

	private static final LocationRequest REQUEST = LocationRequest.create()
			.setInterval(5000) // 5 seconds
			.setFastestInterval(16) // 16ms = 60fps
			.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

	public FragmentMainInput() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.v("Jason", "Cycle: MainInput onCreateView");

		// Toast.makeText(getActivity(), "Record Created",
		// Toast.LENGTH_LONG).show();

		View rootView = inflater.inflate(R.layout.activity_main_input,
				container, false);
		setUpMapIfNeeded();

		// LatLng myLocation = new
		// LatLng(mLocationClient.getLastLocation().getLatitude(),
		// mLocationClient.getLastLocation().getLongitude());
		// map.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 13));

		// map.moveCamera(CameraUpdateFactory.newLatLngZoom(atlanta, 13));

		// map = ((SupportMapFragment)
		// getActivity().getSupportFragmentManager().findFragmentById(R.id.map)).getMap();

		// LatLng atlanta = new LatLng(33.749038, -84.388068);

		// map.setMyLocationEnabled(true);
		// map.moveCamera(CameraUpdateFactory.newLatLngZoom(atlanta, 13));

		// Log.d("Jason", "Start");

		// Hide action bar title on Main Screen
		// getActivity().getActionBar().setDisplayShowTitleEnabled(true);
		// getActivity().getActionBar().setDisplayShowHomeEnabled(true);

		Intent rService = new Intent(getActivity(), RecordingService.class);
		ServiceConnection sc = new ServiceConnection() {
			public void onServiceDisconnected(ComponentName name) {
			}

			public void onServiceConnected(ComponentName name, IBinder service) {
				IRecordService rs = (IRecordService) service;
				int state = rs.getState();
				if (state > RecordingService.STATE_IDLE) {
					if (state == RecordingService.STATE_FULL) {
						startActivity(new Intent(getActivity(),
								TripPurposeActivity.class));
					} else { // RECORDING OR PAUSED:
						// startActivity(new Intent(MainInput.this,
						// RecordingActivity.class));
					}
					getActivity().finish();
				} else {
					// Idle. First run? Switch to user prefs screen if there are
					// no prefs stored yet
					// SharedPreferences settings =
					// getSharedPreferences("PREFS", 0);
					// if (settings.getAll().isEmpty()) {
					// showWelcomeDialog();
					// }
					// // Not first run - set up the list view of saved trips
					// ListView listSavedTrips = (ListView)
					// findViewById(R.id.ListSavedTrips);
					// populateList(listSavedTrips);
				}
				getActivity().unbindService(this); // race? this says
													// we no longer care
			}
		};
		// This needs to block until the onServiceConnected (above) completes.
		// Thus, we can check the recording status before continuing on.
		getActivity().bindService(rService, sc, Context.BIND_AUTO_CREATE);

		// Log.d("Jason", "Start2");

		// And set up the record button
		Button startButton = (Button) rootView.findViewById(R.id.buttonStart);
		startButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (isRecording == false) {
					// Before we go to record, check GPS status
					final LocationManager manager = (LocationManager) getActivity()
							.getSystemService(Context.LOCATION_SERVICE);
					if (!manager
							.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
						buildAlertMessageNoGps();
					} else {
						// startActivity(i);
						// call function in Recording Activity
						// Toast.makeText(getApplicationContext(),
						// "Start Clicked",Toast.LENGTH_LONG).show();
						startRecording();
						// MainInputActivity.this.finish();
					}
				} else if (isRecording == true) {
					// pop up: save, discard, cancel
					buildAlertMessageSaveClicked();
				}
			}
		});

		Button noteThisButton = (Button) rootView
				.findViewById(R.id.buttonNoteThis);
		noteThisButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				final LocationManager manager = (LocationManager) getActivity()
						.getSystemService(Context.LOCATION_SERVICE);
				if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
					buildAlertMessageNoGps();
				} else {
					fi = new Intent(getActivity(), NoteTypeActivity.class);
					// update note entity
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
						getActivity().overridePendingTransition(
								R.anim.slide_in_right, R.anim.slide_out_left);
						// getActivity().finish();
					} else {
						Toast.makeText(getActivity(),
								"No GPS data acquired; nothing to submit.",
								Toast.LENGTH_SHORT).show();
					}
				}
			}
		});

		// copy from Recording Activity
		txtDuration = (TextView) rootView
				.findViewById(R.id.textViewElapsedTime);
		txtDistance = (TextView) rootView.findViewById(R.id.textViewDistance);
		txtCurSpeed = (TextView) rootView.findViewById(R.id.textViewSpeed);

		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

		return rootView;
	}

	// @Override
	// public View onCreateView(LayoutInflater inflater, ViewGroup container,
	// Bundle savedInstanceState) {
	// View rootView = inflater.inflate(
	// R.layout.activity_main_input, container, false);
	// return rootView;
	// }

	public void updateStatus(int points, float distance, float spdCurrent,
			float spdMax) {
		this.curDistance = distance;

		// fix GPS Issue to ensure this
		// // TODO: check task status before doing this?
		// if (points > 0) {
		// txtStat.setText("" + points + " data points received...");
		// } else {
		// txtStat.setText("Waiting for GPS fix...");
		// }

		txtCurSpeed.setText(String.format("%1.1f mph", spdCurrent));

		float miles = 0.0006212f * distance;
		txtDistance.setText(String.format("%1.1f miles", miles));
	}

	void cancelRecording() {
		final Button startButton = (Button) getActivity().findViewById(
				R.id.buttonStart);
		startButton.setText("Start");
		// startButton.setBackgroundColor(0x4d7d36);
		Intent rService = new Intent(getActivity(), RecordingService.class);
		ServiceConnection sc = new ServiceConnection() {
			public void onServiceDisconnected(ComponentName name) {
			}

			public void onServiceConnected(ComponentName name, IBinder service) {
				IRecordService rs = (IRecordService) service;
				rs.cancelRecording();
				getActivity().unbindService(this);
			}
		};
		// This should block until the onServiceConnected (above) completes.
		getActivity().bindService(rService, sc, Context.BIND_AUTO_CREATE);

		isRecording = false;

		txtDuration = (TextView) getActivity().findViewById(
				R.id.textViewElapsedTime);
		txtDuration.setText("00:00:00");
		txtDistance = (TextView) getActivity().findViewById(
				R.id.textViewDistance);
		txtDistance.setText("0.0 miles");

		txtCurSpeed = (TextView) getActivity().findViewById(R.id.textViewSpeed);
		txtCurSpeed.setText("0.0 mph");
	}

	void startRecording() {
		// Query the RecordingService to figure out what to do.
		final Button startButton = (Button) getActivity().findViewById(
				R.id.buttonStart);
		Intent rService = new Intent(getActivity(), RecordingService.class);
		getActivity().startService(rService);
		ServiceConnection sc = new ServiceConnection() {
			public void onServiceDisconnected(ComponentName name) {
			}

			public void onServiceConnected(ComponentName name, IBinder service) {
				IRecordService rs = (IRecordService) service;

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
					long id = rs.getCurrentTrip();
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

	private void buildAlertMessageSaveClicked() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(
				getActivity());
		builder.setTitle("Save Trip");
		builder.setMessage("Do you want to save this trip?");
		builder.setNegativeButton("Save",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
						// save
						// If we have points, go to the save-trip activity
						// trip.numpoints > 0
						if (trip.numpoints > 0) {
							// Handle pause time gracefully
							if (trip.pauseStartedAt > 0) {
								trip.totalPauseTime += (System
										.currentTimeMillis() - trip.pauseStartedAt);
							}
							if (trip.totalPauseTime > 0) {
								trip.endTime = System.currentTimeMillis()
										- trip.totalPauseTime;
							}
							// Save trip so far (points and extent, but no
							// purpose or
							// notes)
							fi = new Intent(getActivity(),
									TripPurposeActivity.class);
							trip.updateTrip("", "", "", "");

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
					}
				});

		builder.setNeutralButton("Discard",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
						// discard
						cancelRecording();
					}
				});

		builder.setPositiveButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
						// continue
					}
				});
		final AlertDialog alert = builder.create();
		alert.show();
	}

	void updateTimer() {
		if (trip != null && isRecording) {
			double dd = System.currentTimeMillis() - trip.startTime
					- trip.totalPauseTime;

			txtDuration.setText(sdf.format(dd));

			// double avgSpeed = 3600.0 * 0.6212 * this.curDistance / dd;
			// txtAvgSpeed.setText(String.format("%1.1f mph", avgSpeed));
		}
	}

	// onResume is called whenever this activity comes to foreground.
	// Use a timer to update the trip duration.
	@Override
	public void onResume() {
		super.onResume();

		Log.v("Jason", "Cycle: MainInput onResume");

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
	}

	// Don't do pointless UI updates if the activity isn't being shown.
	@Override
	public void onPause() {
		super.onPause();
		Log.v("Jason", "Cycle: MainInput onPause");
		// Background GPS.
		if (timer != null)
			timer.cancel();
		if (mLocationClient != null) {
			mLocationClient.disconnect();
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		Log.v("Jason", "Cycle: MainInput onDestroyView");
		// Toast.makeText(getActivity(), "Record Destroyed",
		// Toast.LENGTH_LONG).show();
		// Fragment fragment =
		// (getFragmentManager().findFragmentById(R.id.map));
		// FragmentTransaction ft = getActivity().getSupportFragmentManager()
		// .beginTransaction();
		// ft.remove(fragment);
		// ft.commit();

		// cancelRecording();
	}

	private void setUpMapIfNeeded() {
		// Do a null check to confirm that we have not already instantiated the
		// map.
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

	// private void centerMapOnMyLocation() {
	// // Toast.makeText(getActivity(), "Center", Toast.LENGTH_LONG).show();
	//
	// map.setMyLocationEnabled(true);
	//
	// LocationManager locationManager = (LocationManager) getActivity()
	// .getSystemService(Context.LOCATION_SERVICE);
	//
	// // Creating a criteria object to retrieve provider
	// Criteria criteria = new Criteria();
	//
	// // Getting the name of the best provider
	// String provider = locationManager.getBestProvider(criteria, true);
	//
	// // Getting Current Location
	// Location location = locationManager.getLastKnownLocation(provider);
	//
	// if (location != null) {
	// onLocationChanged(location);
	// }
	//
	// LatLng myLocation;
	//
	// if (location != null) {
	// myLocation = new LatLng(location.getLatitude(),
	// location.getLongitude());
	// map.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 16));
	// }
	// }

	private void setUpLocationClientIfNeeded() {
		if (mLocationClient == null) {
			mLocationClient = new LocationClient(getActivity(), this, // ConnectionCallbacks
					this); // OnConnectionFailedListener
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
}