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

public final class DbQuestions {

	public static final int USER_INFO_AGE             = 1;  // age
	public static final int USER_INFO_EMAIL           = 2;  // email
	public static final int USER_INFO_GENDER          = 3;  // gender
	public static final int USER_INFO_ETHNICITY       = 4;  //
	public static final int USER_INFO_OCCUPATION      = 5;  //
	public static final int USER_INFO_INCOME          = 6;  //
	public static final int USER_INFO_WORKERS         = 7;  //
	public static final int USER_INFO_VEHICLES        = 8;  //
	public static final int USER_INFO_NUM_BIKES       = 9;  //
	public static final int USER_INFO_BIKE_TYPES      = 10; //
	public static final int USER_INFO_HOME_ZIP        = 11; //
	public static final int USER_INFO_WORK_ZIP        = 12; //
	public static final int USER_INFO_SCHOOL_ZIP      = 13; //
	public static final int USER_INFO_CYCLING_FREQ    = 14; //
	public static final int USER_INFO_CYCLING_WEATHER = 15; //
	public static final int USER_INFO_RIDER_ABILITY   = 16; //
	public static final int USER_INFO_RIDER_TYPE      = 17; //
	public static final int USER_INFO_RIDER_HISTORY   = 18; //


	// Trip related questions
	public static final int TRIP_FREQUENCY   = 19; // routeFreq
	public static final int TRIP_PURPOSE     = 20; // purpose
	public static final int ROUTE_PREFS      = 21; // routePrefs
	public static final int TRIP_COMFORT     = 22; // routeComfort
	//public static final int ROUTE_SAFETY     = 23; // routeSafety
	//public static final int PASSENGERS       = 24; // ridePassengers
	//public static final int BIKE_ACCESSORIES = 25; // rideSpecial
	//public static final int RIDE_CONFLICT    = 26; // rideConflict
	public static final int ROUTE_STRESSORS  = 27; // routeStressors

	// Note related questions
	public static final int NOTE_SEVERITY    = 28; // severity
	public static final int NOTE_CONFLICT    = 29; // conflictWith
	public static final int NOTE_ISSUE       = 30; // issueType

	public static final int ACCIDENT_SEVERITY = 28;
	public static final int ACCIDENT_OBJECT   = 29;
	public static final int ACCIDENT_ACTION   = 32;
	public static final int ACCIDENT_CONTRIB  = 33;
	public static final int SAFETY_ISSUE      = 30;
	public static final int SAFETY_URGENCY   = 31;

}
