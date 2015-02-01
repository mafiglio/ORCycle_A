package edu.pdx.cecs.orcycle;

import java.text.SimpleDateFormat;
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
import android.os.Build;
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
	public static final String ORCYCLE_URI = "http://www.pdx.edu/transportation-lab/android-instructions";
	public static final String URI_PRIVACY_POLICY = "http://www.pdx.edu/transportation-lab/privacy-policy";
	public static final String URI_REPORT_ROAD_HAZARDS = "http://www.pdx.edu/transportation-lab/reporting-road-hazards";
	public static final String URI_ORCYCLE_MAPS = "http://www.pdx.edu/transportation-lab/orcycle-maps";

	private static final String SETTING_USER_WELCOME_ENABLED = "USER_WELCOME_ENABLED";
	private static final String SETTING_TUTORIAL_ENABLED = "TUTORIAL_ENABLED";
	private static final String SETTING_USER_INFO_UPLOADED = "USER_INFO_UPLOADED";
	private static final String SETTING_FIRST_TRIP_COMPLETED = "SETTING_FIRST_TRIP_COMPLETED";
	private static final String SETTING_WARN_REPEAT_TRIPS = "SETTING_WARN_REPEAT_TRIPS";
	private static final String SETTING_USER_ID = "SETTING_USER_ID";
	private static final String SETTING_FIRST_USE = "SETTING_FIRST_USE";
	private static final double RESET_START_TIME = 0.0;
	private static final String ANDROID_USER = "android";

	private String userId = null;
	private int versionCode;
	private String versionName;
	private String appVersion;
	private String deviceModel;
	private long firstUse = -1;
	private boolean warnRepeatTrips;
	private boolean firstTripCompleted;
	private RecordingService recordingService = null;
	private TripData trip;

	private boolean running;

	private boolean checkedForUserProfile = false;
	private boolean userProfileUploaded;

	private boolean checkedForUserWelcome = false;
	private boolean userWelcomeEnabled;

	private boolean checkedForTutorial = false;
	private boolean tutorialEnabled;

	private double lastTripStartTime = RESET_START_TIME;


    /**
    * Reference to class instance
    */
    private static MyApplication myApp = null;

    private static final Controller controller = new Controller();

    /**
     * Returns the class instance of the MyApplication object
     */
    public static MyApplication getInstance() {
        return myApp;
    }

	public Controller getController() {
		return controller;
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
		loadApplicationSettings();
    }

	private void loadApplicationSettings() {

		//generateUserId();  // For resetting while debugging
		SharedPreferences settings = getSharedPreferences(PREFS_APPLICATION, MODE_PRIVATE);
		userId = settings.getString(SETTING_USER_ID, null);
		if ((null == userId) || (userId.equals(""))) {
			userId = generateUserId();
		}

		firstUse = settings.getLong(SETTING_FIRST_USE, -1);
		if (-1 == firstUse) {
			firstUse = generateFirstUse();
		}

		// setDefaultApplicationSettings();

		firstTripCompleted = settings.getBoolean(SETTING_FIRST_TRIP_COMPLETED, true);

		userProfileUploaded = settings.getBoolean(SETTING_USER_INFO_UPLOADED, false);

		//userWelcomeEnabled = settings.getBoolean(SETTING_USER_WELCOME_ENABLED, true);
		// We are purposely disabling the welcome screen for now
		userWelcomeEnabled = false;

		tutorialEnabled = settings.getBoolean(SETTING_TUTORIAL_ENABLED, true);

		warnRepeatTrips = settings.getBoolean(SETTING_WARN_REPEAT_TRIPS, true);

		loadDeviceInfo();
	}

	public void setDefaultApplicationSettings() {
		setFirstTripCompleted(true); // for debugging
		//setFirstTripCompleted(false);
		setUserProfileUploaded(false);
		setUserWelcomeEnabled(false);
		setTutorialEnabled(true);
		setWarnRepeatTrips(true);
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
     * Called when the operating system has determined that it is a good
     * time for a process to trim unneeded memory from its process.
     */
    @Override
    public final void onTrimMemory(int level) {
        super.onTrimMemory(level);
    }

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

    public boolean isRunning() {
		return this.running;
    }

    public void setRunning(boolean value) {
		running = value;
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

		startRecordingNotification(lastTripStartTime = trip.getStartTime());
    }

    /**
     * finishRecording
     */
    public void finishRecording() {
    	recordingService.finishRecording();
		clearRecordingNotifications();
		lastTripStartTime = RESET_START_TIME;
   }

    /**
     * cancelRecording
     */
    public void cancelRecording() {
    	recordingService.cancelRecording();
		clearRecordingNotifications();
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

	// *********************************************************************************
	// *                              UserProfile
	// *********************************************************************************

	public void setCheckedForUserProfile(boolean value) {
		checkedForUserProfile = value;
	}

	public boolean getCheckedForUserProfile() {
		return checkedForUserProfile;
	}

	public void setUserProfileUploaded(boolean value) {
		userProfileUploaded = value;
		SharedPreferences settings;
		settings = getSharedPreferences(PREFS_APPLICATION, MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor = settings.edit();
		editor.putBoolean(SETTING_USER_INFO_UPLOADED, userProfileUploaded);
		editor.apply();
	}

	public boolean getUserProfileUploaded() {
		return userProfileUploaded;
	}

	public void setFirstTripCompleted(boolean value) {
		firstTripCompleted = value;
		SharedPreferences settings = getSharedPreferences(PREFS_APPLICATION, MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor = settings.edit();
		editor.putBoolean(SETTING_FIRST_TRIP_COMPLETED, firstTripCompleted);
		editor.apply();
	}

	public boolean getFirstTripCompleted() {
		return firstTripCompleted;
	}

	// *********************************************************************************
	// *                                UserWelcome
	// *********************************************************************************

	public void setCheckedForUserWelcome(boolean value) {
		checkedForUserWelcome = value;
	}

	public boolean getCheckedForUserWelcome() {
		return checkedForUserWelcome;
	}

	public void setUserWelcomeEnabled(boolean value) {
		userWelcomeEnabled = value;
		SharedPreferences settings;
		settings = getSharedPreferences(PREFS_APPLICATION, MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor = settings.edit();
		editor.putBoolean(SETTING_USER_WELCOME_ENABLED, userWelcomeEnabled);
		editor.apply();
	}

	public boolean getUserWelcomeEnabled() {
		return userWelcomeEnabled;
	}

	// *********************************************************************************
	// *                                HowTo
	// *********************************************************************************

	public void setCheckedForTutorial(boolean value) {
		checkedForTutorial = value;
	}

	public boolean getCheckedForTutorial() {
		return checkedForTutorial;
	}

	public void setTutorialEnabled(boolean value) {
		tutorialEnabled = value;
		SharedPreferences settings;
		settings = getSharedPreferences(PREFS_APPLICATION, MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor = settings.edit();
		editor.putBoolean(SETTING_TUTORIAL_ENABLED, tutorialEnabled);
		editor.apply();
	}

	public boolean getTutorialEnabled() {
		return tutorialEnabled;
	}

	// *********************************************************************************
	// *                                WarnRepeatTrips
	// *********************************************************************************

	public void setWarnRepeatTrips(boolean value) {
		warnRepeatTrips = value;
		SharedPreferences settings = getSharedPreferences(PREFS_APPLICATION, MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor = settings.edit();
		editor.putBoolean(SETTING_WARN_REPEAT_TRIPS, warnRepeatTrips);
		editor.apply();
	}

	public boolean getWarnRepeatTrips() {
		return warnRepeatTrips;
	}

	// *********************************************************************************
	// *                                FirstUse
	// *********************************************************************************

	/**
	 * Sets SETTING_FIRST_USE, and SETTING_VERSION_CODE settings
	 * @return new value of SETTING_FIRST_USE
	 */
	private long generateFirstUse() {
		long value = System.currentTimeMillis();
		SharedPreferences settings = getSharedPreferences(PREFS_APPLICATION, MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor = settings.edit();
		editor.putLong(SETTING_FIRST_USE, value);
		editor.apply();
		return value;
	}

	public long getFirstUse() {
		return firstUse;
	}

	public String getFirstUseString() {
		return (new SimpleDateFormat("MMMM d, y  h:mm a")).format(firstUse);
	}

	public String getUserId() {
		return userId;
	}

	private String generateUserId() {
		String value = ANDROID_USER + UUID.randomUUID().toString();
		SharedPreferences settings = getSharedPreferences(PREFS_APPLICATION, MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor = settings.edit();
		editor.putString(SETTING_USER_ID, value);
		editor.apply();
		return value;
	}

	// *********************************************************************************
	// *
	// *********************************************************************************

	public void ResumeNotification() {
		if (isRecording()) {
			startRecordingNotification(lastTripStartTime);
		}
	}

	public void startRecordingNotification(double startTime) {

		// Add the notify bar and blinking light
		MyNotifiers.setRecordingNotification(this);
	}

	public void clearRecordingNotifications() {
		MyNotifiers.cancelRecordingNotification(this.getBaseContext());
	}

	public void clearReminderNotifications() {
		MyNotifiers.cancelReminderNotification(this.getBaseContext());
	}

	// *********************************************************************************
	// *
	// *********************************************************************************

	public Location getLastKnownLocation() {

		LocationManager lm = null;
		List<String> providers = null;
		Location location = null;

		if (null != (lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE))) {
			if (null != (providers = lm.getProviders(true))) {
				/* Loop over the array backwards, and if you get a location, then break out the loop*/
				for (int i = providers.size() - 1;  i >= 0; --i) {
					if (null != (location = lm.getLastKnownLocation(providers.get(i)))) {
						break;
					}
				}
			}
		}
		return location;
	}

	private void loadDeviceInfo() {

		// Determine application information
		versionName = "";
		versionCode = 0;
		try {
			Context context = this.getBaseContext();
			PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			versionName = pInfo.versionName;
			versionCode = pInfo.versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		appVersion = versionName + " (" + versionCode + ") on Android " + Build.VERSION.RELEASE;

		// Determine model information
		String manufacturer = Build.MANUFACTURER;
		String model = Build.MODEL;

		if (model.startsWith(manufacturer)) {
			deviceModel = capitalize(model);
		} else {
			deviceModel = capitalize(manufacturer) + " " + model;
		}
	}

	public String getVersionName() {
		return versionName;
	}

	public int getVersionCode() {
		return versionCode;
	}

	public String getAppVersion() {
		return appVersion;
	}

	public String getDeviceModel() {
		return (deviceModel);
	}

	private String capitalize(String s) {
		if (s == null || s.length() == 0) {
			return "";
		}
		char first = s.charAt(0);
		if (Character.isUpperCase(first)) {
			return s;
		} else {
			return Character.toUpperCase(first) + s.substring(1);
		}
	}
}
