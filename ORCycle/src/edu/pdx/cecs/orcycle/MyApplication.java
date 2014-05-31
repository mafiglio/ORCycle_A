package edu.pdx.cecs.orcycle;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
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
	private RecordingService recordingService = null;
	private TripData trip;

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
	// *
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
	// *
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

		//recordingService.setListener((FragmentMainInput) getActivity().getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":0"));
    }

    /**
     * finishRecording
     */
    public void finishRecording() {

		if (trip.numpoints > 0) {
			// Handle pause time gracefully
			if (trip.pauseStartedAt > 0) {
				trip.totalPauseTime += (System.currentTimeMillis() - trip.pauseStartedAt);
			}
			if (trip.totalPauseTime > 0) {
				trip.endTime = System.currentTimeMillis() - trip.totalPauseTime;
			}
			trip.updateTrip("", "", "", "");
		}
		else {
			cancelRecording();
		}
   }

   /**
   * cancelRecording
   */
  public void cancelRecording() {
	  recordingService.cancelRecording();
  }
}
