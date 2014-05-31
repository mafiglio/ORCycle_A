package edu.pdx.cecs.orcycle;

public class ApplicationStatus {

	public ApplicationStatus(
			boolean isProviderEnabled,
			TripData tripData) {

		this.isProviderEnabled = isProviderEnabled;
		this.tripData = tripData;
	}

	public boolean isProviderEnabled() {
		return this.isProviderEnabled;
	}

	public TripData getTripData() {
		return this.tripData;
	}

	private final boolean isProviderEnabled;
	private final TripData tripData;
}
