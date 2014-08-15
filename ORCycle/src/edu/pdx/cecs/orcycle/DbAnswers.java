package edu.pdx.cecs.orcycle;

public final class DbAnswers {

	// Answers for UserInfoActivity
	public static final int[] userInfoAge             = { 2, 3, 4, 5, 6, 7, 8 };			// question_id = 1
	public static final int[] userInfoGender          = { 10, 11, 12 };						// 3
	public static final int[] userInfoEthnicity       = { 14, 15, 16, 17, 18, 19 };			    // 4
	public static final int[] userInfoOccupation      = { 21, 22, 23, 24, 25};				// 5
	public static final int[] userInfoIncome          = { 27, 28, 29, 30, 31, 32, 33, 34 };	// 6
	public static final int[] userInfoHHWorkers       = { 36, 37, 38, 39 };					// 7
	public static final int[] userInfoHHVehicles      = { 41, 42, 43, 44 };					// 8
	public static final int[] userInfoNumBikes        = { 46, 47, 48, 49, 50 };				// 9
	public static final int[] userInfoBikeTypes       = { 52, 53, 54, 55, 56, 57, 58 };		// 10
	public static final int[] userInfoCyclingFreq     = { 63, 62, 61, 60 };					// 14
	public static final int[] userInfoCyclingWeather  = { 65, 66, 67, 68 };					// 15
	public static final int[] userInfoRiderAbility    = { 74, 73, 72, 71, 70 };				// 16
	public static final int[] userInfoRiderType       = { 76, 77, 78, 79, 80, 81 };			// 17

	// Answers for TripQuestionsActivity
	public static final int[] tripFreq                = { 88, 89, 90, 91, 92};               // question_id = 19
	public static final int[] tripPurpose             = { 94, 95, 96, 97, 98, 99, 100, 101}; // question_id = 20
	public static final int[] routePrefs              = { 103, 104, 105, 106, 107, 108, 109, // question_id = 21
			                                              110, 111, 112, 113, 114, 115};
	public static final int[] tripComfort             = { 121, 120, 119, 118, 117};          // question_id = 22
	public static final int[] routeSafety             = { 123, 124, 125, 126, 127};          // question_id = 23
	public static final int[] passengers              = { 129, 130, 131, 132, 133, 134};     // question_id = 24
	public static final int[] bikeAccessories         = { 135, 136, 137, 138, 176};          // question_id = 25
	public static final int[] rideConflict            = { 139, 140, 141, 142 };              // question_id = 26
	public static final int[] routeStressors          = { 150, 143, 144, 145, 146, 147, 148, // question_id = 27
	                                                      149, 150 };

	public static final int[] noteSeverity = { 151, 152, 153, 154, 155};                // question_id = 28
	public static final int[] noteConflict = { 156, 157, 158, 159, 160, 161, 162, 163}; // question_id = 29
	public static final int[] noteIssue    = { 164, 165, 166, 167, 168, 169,               // question_id = 30
		                                    170, 171, 172, 173, 174, 175};

	public static final int[] userDetailedAnswers = { 12, 58, 81}; // These are answers specified as other
	public static final int[] tripDetailedAnswers = { 101, 115};   // These are answers specified as other
	public static final int[] noteDetailedAnswers = { 163, 175};   // These are answers specified as other

	public static final int userInfoGenderOther = 12;
	public static final int userInfoEthnicityOther = 19;
	public static final int userInfoOccupationOther = 25;
	public static final int userInfoBikeTypeOther = 58;
	public static final int userInfoRiderTypeOther = 81;
	public static final int noteConflictOther = 163;
	public static final int noteIssueOther = 175;
	public static final int noteBikeAccessoriesOther = 176;

	public static final String PURPOSE_COMMUTE = "Commute";
	public static final String PURPOSE_SCHOOL = "School";
	public static final String PURPOSE_WORK_RELATED = "Work-Related";
	public static final String PURPOSE_EXERCISE = "Exercise";
	public static final String PURPOSE_SOCIAL = "Social";
	public static final String PURPOSE_SHOPPING = "Shopping";
	public static final String PURPOSE_ERRAND = "Errand";
	public static final String PURPOSE_OTHER = "Other";

	public static final String getTextTripPurpose(int value)
			throws IllegalArgumentException {
		switch(value) {
		case 94: return PURPOSE_COMMUTE;
		case 95: return PURPOSE_SCHOOL;
		case 96: return PURPOSE_WORK_RELATED;
		case 97: return PURPOSE_EXERCISE;
		case 98: return PURPOSE_SOCIAL;
		case 99: return PURPOSE_SHOPPING;
		case 100: return PURPOSE_ERRAND;
		case 101: return PURPOSE_OTHER;
		default: throw new IllegalArgumentException();
		}
	}

	public static final int findIndex(int[] answers, int value) {
		int index;
		for (index = 0; index < answers.length; ++index) {
			if (answers[index] == value)
				return index;
		}
		return -1;
	}

}
