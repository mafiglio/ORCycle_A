package edu.pdx.cecs.orcycle;

import java.util.List;
import java.util.UUID;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

/**
 * This class extends the <code>Application<code/> class, and implements it as a singleton.
 * This class is used to maintain global application state.
 * @author robin5 (Robin Murray)
 * @version 1.0
 * @see <code>Application<code/> class.
 * created 5/22/2014
 */
public class MyApplication extends android.app.Application {

	private final String MODULE_TAG = "MyApplication";

	public static final String PREFS_APPLICATION = "PREFS_APPLICATION";

	private static final String SETTING_USER_INFO_UPLOADED = "USER_INFO_UPLOADED";
	private static final String SETTING_FIRST_TRIP_COMPLETED = "SETTING_FIRST_TRIP_COMPLETED";
	private static final String SETTING_USER_ID = "SETTING_USER_ID";
	private static final double RESET_START_TIME = 0.0;
	private static final String ANDROID_USER = "android-";

	private String userId = null;
	private RecordingService recordingService = null;
	private TripData trip;
	private boolean checkedForUserProfile = false;
	private final BellTimer bellTimer = new BellTimer();
	private double lastTripStartTime = RESET_START_TIME;


    /**
    * Reference to class instance
    */
    private static MyApplication myApp = null;

    /**
     * Returns the class instance of the MyApplication object
     */
    public static MyApplication getInstance() {
        return myApp;
    }

	// *********************************************************************************
	// *                   			Application Interface
	// *********************************************************************************

    /**
     * Called when the application is starting, before any activity, service,
     * or receiver objects (excluding content providers) have been created.
     * @throws java.lang.OutOfMemoryError
     */
    @Override
    public final void onCreate() {
        super.onCreate();

        // Set reference to this instance
        myApp = this;

        ConnectRecordingService();

		bellTimer.init(this.getBaseContext());

		initUserId();
    }

    /**
     * This is called when the overall system is running low on memory, and
     * actively running processes should trim their memory usage.
     */
    @Override
    public final void onLowMemory() {
        super.onLowMemory();
    }

    /**
     * TODO: onTrimMemory() requires minimum API of 14
     * Called when the operating system has determined that it is a good
     * time for a process to trim unneeded memory from its process.
    @Override
    public final void onTrimMemory(int level) {
        super.onTrimMemory(level);
    }
     */

    /**
     * Called by the system when the device configuration changes while
     * the component is running.
     */
    @Override
    public final void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

	// *********************************************************************************
	// *
	// *********************************************************************************

    private boolean isProviderEnabled() {
		final LocationManager manager = (LocationManager) getSystemService(LOCATION_SERVICE);
		return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public ApplicationStatus getStatus() {

    	ApplicationStatus applicationStatus = new ApplicationStatus(
				this.isProviderEnabled(),
				this.trip);

    	return applicationStatus;
    }

	// *********************************************************************************
	// *              RecordingService ServiceConnection Interface
	// *********************************************************************************

    /**
     * Connects the recording service to the Application object
     */
    private void ConnectRecordingService() {

    	try {
        Intent intent = new Intent(this, RecordingService.class);
        bindService(intent, recordingServiceServiceConnection, Context.BIND_AUTO_CREATE);
    	}
        catch(SecurityException ex) {
			Log.d(MODULE_TAG, ex.getMessage());
        }
    }

    /**
     * Connection to the RecordingService
     */
    public final ServiceConnection recordingServiceServiceConnection = new ServiceConnection() {

        /**
         * Called when a connection to the Service has been established, with
         * the {@link android.os.IBinder} of the communication channel to the
         * Service.
         *
         * @param name The concrete component name of the service that has
         * been connected.
         *
         * @param service The IBinder of the Service's communication channel,
         * which you can now make calls on.
         */
        public void onServiceConnected(ComponentName name, IBinder service) {

        	Log.v(MODULE_TAG, "Connecting to RecordingService");

        	if (null == recordingService) {
        		recordingService = ((RecordingService.MyServiceBinder)service).getService();
        	}
        }

        /**
         * Called when a connection to the Service has been lost. This typically
         * happens when the process hosting the service has crashed or been killed.
         * This does <em>not</em> remove the ServiceConnection itself -- this
         * binding to the service will remain active, and you will receive a call
         * to {@link #onServiceConnected} when the Service is next running.
         *
         * @param name The concrete component name of the service whose
         * connection has been lost.
         */
        public void onServiceDisconnected(ComponentName name) {

        	Log.v(MODULE_TAG, "Disconnecting from RecordingService");

        	recordingService = null;
        }
    };

    public IRecordService getRecordingService() {
    	return recordingService;
    }

    /**
     * startRecording
     */
    public void startRecording(FragmentActivity activity) {
		switch (recordingService.getState()) {

		case RecordingService.STATE_IDLE:
			trip = TripData.createTrip(activity);
			recordingService.startRecording(trip);
			break;

		case RecordingService.STATE_RECORDING:
			long id = recordingService.getCurrentTripID();
			trip = TripData.fetchTrip(activity, id);
			break;
		}

		startNotification(lastTripStartTime = trip.getStartTime());
    }

    /**
     * finishRecording
     */
    public void finishRecording() {
    	recordingService.finishRecording();
		clearNotifications();
		lastTripStartTime = RESET_START_TIME;
   }

    /**
     * cancelRecording
     */
    public void cancelRecording() {
    	recordingService.cancelRecording();
		clearNotifications();
		lastTripStartTime = RESET_START_TIME;
    }

    public boolean isRecording() {
    	if (recordingService == null) {
    		return false;
    	}
    	else {
    		return ((RecordingService.STATE_RECORDING == recordingService.getState()) ||
					(RecordingService.STATE_PAUSED == recordingService.getState()));
    	}
    }

	public void setCheckedForUserProfile(boolean value) {
		checkedForUserProfile = value;
	}

	public boolean getCheckedForUserProfile() {
		return checkedForUserProfile;
	}

	public void setUserProfileUploaded(boolean value) {
		SharedPreferences settings;
		settings = getSharedPreferences(PREFS_APPLICATION, MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor = settings.edit();
		editor.putBoolean(SETTING_USER_INFO_UPLOADED, value);
		editor.apply();
	}

	public boolean getUserProfileUploaded() {
		SharedPreferences settings = getSharedPreferences(PREFS_APPLICATION, MODE_PRIVATE);
		boolean value = settings.getBoolean(SETTING_USER_INFO_UPLOADED, false);
		return value;
	}

	public boolean getFirstTripCompleted() {
		SharedPreferences settings = getSharedPreferences(PREFS_APPLICATION, MODE_PRIVATE);
		boolean value = settings.getBoolean(SETTING_FIRST_TRIP_COMPLETED, false);
		return value;
	}

	public void setFirstTripCompleted(boolean value) {
		SharedPreferences settings = getSharedPreferences(PREFS_APPLICATION, MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor = settings.edit();
		editor.putBoolean(SETTING_FIRST_TRIP_COMPLETED, value);
		editor.apply();
	}

	private void initUserId() {
		// generateNewUserId();  // For resetting while debugging
		SharedPreferences settings = getSharedPreferences(PREFS_APPLICATION, MODE_PRIVATE);
		userId = settings.getString(SETTING_USER_ID, null);
		if ((null == userId) || (userId.equals(""))) {
			generateNewUserId();
		}
	}

	public String getUserId() {
		return userId;
	}

	public void generateNewUserId() {
		userId = ANDROID_USER + UUID.randomUUID().toString();
		SharedPreferences settings = getSharedPreferences(PREFS_APPLICATION, MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor = settings.edit();
		editor.putString(SETTING_USER_ID, userId);
		editor.apply();
	}

	public void ResumeNotification() {
		if (isRecording()) {
			startNotification(lastTripStartTime);
		}
	}

	public void startNotification(double startTime) {

		if (startTime != RESET_START_TIME) {
			bellTimer.cancel();
			// Set up timer for bike bell
			bellTimer.start(startTime);
		}
		// Add the notify bar and blinking light
		MyNotifiers.setNotification(this);
	}

	public void clearNotifications() {
		MyNotifiers.cancelAll(this.getBaseContext());
		bellTimer.cancel();
	}

	public double[] getLastKnownLocation() {

		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		List<String> providers = lm.getProviders(true);

		/* Loop over the array backwards, and if you get an accurate location, then break out the loop*/
		Location l = null;

		for (int i=providers.size()-1; i>=0; i--) {
		l = lm.getLastKnownLocation(providers.get(i));
		if (l != null) break;
		}

		double[] gps = new double[2];
		if (l != null) {
		gps[0] = l.getLatitude();
		gps[1] = l.getLongitude();
		}
		return gps;
	}

	public int getAppVersion() {
		try {
			PackageInfo pInfo;
			if (null != (pInfo = this.getPackageManager().getPackageInfo(this.getPackageName(), 0))) {
				return pInfo.versionCode;
			}
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}

		return -1;
	}
}
