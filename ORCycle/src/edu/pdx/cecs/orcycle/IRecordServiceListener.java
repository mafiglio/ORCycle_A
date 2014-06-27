package edu.pdx.cecs.orcycle;

public interface IRecordServiceListener {

	/**
	 * Updates the status of a trip being recorded.
	 * @param distance Distance travelled in meters
	 * @param avgSpeedMps Average speed in meters per second
	 */
	public void updateStatus(float distance, float avgSpeedMps);
}
