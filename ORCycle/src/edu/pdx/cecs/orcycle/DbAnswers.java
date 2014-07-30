package edu.pdx.cecs.orcycle;

public final class DbAnswers {
	public static final int[] tripFreq        = { 88, 89, 90, 91, 92};               // question_id = 19
	public static final int[] tripPurpose     = { 94, 95, 96, 97, 98, 99, 100, 101}; // question_id = 20
	public static final int[] routePrefs      = { 103, 104, 105, 106, 107, 108, 109, // question_id = 21
			                                      110, 111, 112, 113, 114, 115};
	public static final int[] tripComfort     = { 117, 118, 119, 120, 121};          // question_id = 22
	public static final int[] routeSafety     = { 123, 124, 125, 126, 127};          // question_id = 23
	public static final int[] passengers      = { 129, 130, 131, 132, 133, 134};     // question_id = 24
	public static final int[] bikeAccessories = { 136, 137, 138};                    // question_id = 25


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
}
