package edu.pdx.cecs.orcycle;

import java.util.List;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.util.Log;

public abstract class SensorRecorder implements SensorEventListener {

	private static final String MODULE_TAG = "AntDeviceRecorder";

	protected enum State { IDLE, RUNNING, PAUSED, FAILED };
	protected final String sensorName;
	protected final int type;
	protected State state;
	private final int rate;

	public abstract void reset();

	public static SensorRecorder create(Context context, int sensorType) {

		SensorManager sensorManager;
		Sensor sensor;
		SensorRecorder recorder = null;

		// Get reference to sensor manager
    	if (null != (sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE))) {
	    	if (null != (sensor = sensorManager.getDefaultSensor(sensorType))) {
	    		recorder = new VectorSensorRecorder(sensor.getName(), sensorType, SensorManager.SENSOR_DELAY_NORMAL);
	    	}
    	}
		return recorder;
	}

	public SensorRecorder(String name, int type, int rate) {
		this.sensorName = name;
		this.type = type;
		this.rate = rate;
		this.state = State.IDLE;
	}

	public String getSensorName() {
		return this.sensorName;
	}

	synchronized public void start(Context context) {

		SensorManager sensorManager;

		// Get reference to sensor manager
    	if (null != (sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE))) {
    		List<Sensor> sensorList;
    		// Get list of hardware sensors
	    	if (null != (sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL))) {
		    	for (Sensor sensor : sensorList) {
		    		// Search for the first matching sensor name and register to receive events
	    			if (sensorName.equals(sensor.getName())) {
	    				sensorManager.registerListener(this, sensor, rate);
	    				this.state = State.RUNNING;
	    				return;
	    			}
		    	}
	    	}
    	}
		this.state = State.FAILED;
	}

	synchronized public void pause() {
		if (state == State.RUNNING) {
			state = State.PAUSED;
		}
		else {
			Log.e(MODULE_TAG, "Invalid state");
		}
	}

	synchronized public void resume() {
		if (state == State.PAUSED) {
			state = State.RUNNING;
		}
		else {
			Log.e(MODULE_TAG, "Invalid state");
		}
	}

	synchronized public void unregister(Context context) {

		SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		sensorManager.unregisterListener(this);
		this.state = State.IDLE;
	}

	abstract void addSample(float[] values);

	abstract void writeResult(TripData tripData, long currentTimeMillis, Location location);

	@Override
	synchronized public void onSensorChanged(SensorEvent event) {
		addSample(event.values);
	}

	@Override
	synchronized public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}
}
