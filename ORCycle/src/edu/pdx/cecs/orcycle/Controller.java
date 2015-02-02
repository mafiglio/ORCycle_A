package edu.pdx.cecs.orcycle;

import android.content.ComponentName;
import android.content.Intent;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.Log;

public class Controller {

	private static final String MODULE_TAG = "Controller";

	private static final String COM_ANDROID_SETTINGS = "com.android.settings";
	private static final String COM_ANDROID_SETTINGS_SECURITY_SETTINGS = "com.android.settings.SecuritySettings";

	public static final int DSA_ID_WELCOME_DIALOG_ID = 1000;
	public static final int DSA_ID_WELCOME_DIALOG_CONTINUE = 1001;
	public static final int DSA_ID_WELCOME_DIALOG_INSTRUCTIONS = 1002;
	public static final int DSA_ID_WELCOME_BACK_FROM_INSTRUCTIONS = 1003;

	public static final int DSA_ID_USER_PROFILE_DIALOG_ID = 2000;
	public static final int DSA_ID_USER_PROFILE_DIALOG_OK = 2001;
	public static final int DSA_ID_USER_PROFILE_DIALOG_LATER = 2002;

	public static final int DSA_ID_HOW_TO_DIALOG_ID = 3000;
	public static final int DSA_ID_HOW_TO_DIALOG_CONTINUE = 3001;

	private static int nextHowToScreen;
	private static final int LAST_HOW_TO_SCREEN = 2;

	public Controller() {
		nextHowToScreen = 0;
	}

	// *********************************************************************************
	// *                    FragmentMainInput Transitions
	// *********************************************************************************

	public void finish(FragmentMainInput f) {
		finish(f, -1, -1);
	}

	public void finish(FragmentMainInput f, long tripId) {
		finish(f, tripId, -1);
	}

	public void finish(FragmentMainInput f, long tripId, long noteId) {

		switch(f.getResult()) {

		case UNDEFINED:
			Log.e(MODULE_TAG, "Fragment result value not set");
			break;

		case SAVE_TRIP:
			transitionToTripQuestionsActivity(f, tripId);
			break;

		case REPORT:
			transitionToReportTypeActivity(f, tripId, noteId);
			break;

		case NO_GPS:
			transitionToLocationServices(f);
			break;

		case GET_USER_INFO:
			transitionToUserInfoActivity(f);
			break;

		case SHOW_INSTRUCTIONS:
			transitionToORcycle(f);
			break;

		case SHOW_WELCOME:
			transitionToDialogWelcome(f);
			break;

		case SHOW_DIALOG_USER_INFO:
			transitionToDialogUserInfo(f);
			break;

		case SHOW_TUTORIAL:
			transitionToTutorialActivity(f);
			break;
		}
	}

	public boolean setNextHowToScreen() {
		if (nextHowToScreen == LAST_HOW_TO_SCREEN) {
			return false;
		}
		else {
			++nextHowToScreen;
			return true;
		}
	}

	private void transitionToTripQuestionsActivity(FragmentMainInput f, long tripId) {

		Intent intent = new Intent(f.getActivity(), TripQuestionsActivity.class);
		intent.putExtra(TripQuestionsActivity.EXTRA_TRIP_ID, tripId);
		f.startActivity(intent);
		f.getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		f.getActivity().finish();
	}

	private void transitionToReportTypeActivity(FragmentMainInput f, long tripId, long noteId) {
		Intent intent = new Intent(f.getActivity(), ReportTypeActivity.class);
		intent.putExtra(NoteQuestionsActivity.EXTRA_NOTE_ID, noteId);
		intent.putExtra(NoteQuestionsActivity.EXTRA_NOTE_SOURCE, NoteQuestionsActivity.EXTRA_NOTE_SOURCE_MAIN_INPUT);
		intent.putExtra(NoteQuestionsActivity.EXTRA_TRIP_ID, tripId);
		intent.putExtra(NoteQuestionsActivity.EXTRA_TRIP_SOURCE, NoteQuestionsActivity.EXTRA_TRIP_SOURCE_MAIN_INPUT);
		f.startActivity(intent);
	}

	private void transitionToLocationServices(Fragment f) {
		final ComponentName toLaunch = new ComponentName(
				COM_ANDROID_SETTINGS,
				COM_ANDROID_SETTINGS_SECURITY_SETTINGS);
		final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setComponent(toLaunch);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		f.startActivityForResult(intent, 0);
	}

	private void transitionToUserInfoActivity(FragmentMainInput f) {

		// Create intent to come back to this activity
		Intent intent = new Intent(f.getActivity(), UserInfoActivity.class);
		intent.putExtra(UserInfoActivity.EXTRA_PREVIOUS_ACTIVITY, UserInfoActivity.EXTRA_FRAGMENT_MAIN_INPUT);

		// Exit this activity
		f.startActivity(intent);
		f.getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		f.getActivity().finish();
	}

	private void transitionToORcycle(FragmentMainInput f) {
		//Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(MyApplication.ORCYCLE_URI));
		//startActivityForResult(intent, DSA_ID_WELCOME_DIALOG_ID);
		String title = f.getResources().getString(R.string.title_orcycle_instructions);
		Intent intent = new Intent(f.getActivity(), WebViewActivity.class);
		intent.putExtra(WebViewActivity.EXTRA_URL, MyApplication.ORCYCLE_URI);
		intent.putExtra(WebViewActivity.EXTRA_TITLE, title);
		f.startActivityForResult(intent, DSA_ID_WELCOME_DIALOG_ID);
		f.getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	}

	private void transitionToDialogWelcome(FragmentMainInput f) {

		Intent intent = new Intent(f.getActivity(), DsaDialogActivity.class);

		String title = f.getResources().getString(R.string.fmi_welcome_title);
		String message = f.getResources().getString(R.string.fmi_welcome_message);
		String positiveText = f.getResources().getString(R.string.fmi_welcome_continue);
		String negativeText = f.getResources().getString(R.string.fmi_welcome_instructions);

		intent.putExtra(DsaDialogActivity.EXTRA_DIALOG_ID, DSA_ID_WELCOME_DIALOG_ID);
		intent.putExtra(DsaDialogActivity.EXTRA_TITLE, title);
		intent.putExtra(DsaDialogActivity.EXTRA_MESSAGE, message);
		intent.putExtra(DsaDialogActivity.EXTRA_POSITIVE_TEXT, positiveText);
		intent.putExtra(DsaDialogActivity.EXTRA_POSITIVE_ID, DSA_ID_WELCOME_DIALOG_CONTINUE);
		intent.putExtra(DsaDialogActivity.EXTRA_NEGATIVE_TEXT, negativeText);
		intent.putExtra(DsaDialogActivity.EXTRA_NEGATIVE_ID, DSA_ID_WELCOME_DIALOG_INSTRUCTIONS);
		f.startActivity(intent);
	}

	private void transitionToDialogHowTo(FragmentMainInput f) {

		Intent intent = new Intent(f.getActivity(), DsaDialogActivity.class);

		String title = f.getResources().getString(R.string.fmi_how_to_title);
		String message = f.getResources().getString(R.string.fmi_how_to_message);
		String positiveText = f.getResources().getString(R.string.fmi_how_to_continue);

		intent.putExtra(DsaDialogActivity.EXTRA_DIALOG_ID, DSA_ID_HOW_TO_DIALOG_ID);
		intent.putExtra(DsaDialogActivity.EXTRA_TITLE, title);
		intent.putExtra(DsaDialogActivity.EXTRA_MESSAGE, message);
		intent.putExtra(DsaDialogActivity.EXTRA_POSITIVE_TEXT, positiveText);
		intent.putExtra(DsaDialogActivity.EXTRA_POSITIVE_ID, DSA_ID_HOW_TO_DIALOG_CONTINUE);

		switch(nextHowToScreen) {
		default:
			intent.putExtra(DsaDialogActivity.EXTRA_IMAGE_ID, R.drawable.tutorial_start_trip);
			break;
		case 1:
			intent.putExtra(DsaDialogActivity.EXTRA_IMAGE_ID, R.drawable.tutorial_trips);
			break;
		case 2:
			intent.putExtra(DsaDialogActivity.EXTRA_IMAGE_ID, R.drawable.tutorial_reports);
			break;
		}

		f.startActivity(intent);
	}

	private void transitionToTutorialActivity(FragmentMainInput f) {
		Intent intent = new Intent(f.getActivity(), TutorialActivity.class);
		intent.putExtra(TutorialActivity.EXTRA_PREVIOUS_ACTIVITY, TutorialActivity.EXTRA_PREVIOUS_ACTIVITY_MAIN_INPUT);
		f.startActivity(intent);
	}


	private void transitionToDialogUserInfo(FragmentMainInput f) {

		Intent intent = new Intent(f.getActivity(), DsaDialogActivity.class);

		String title = f.getResources().getString(R.string.fmi_query_user_profile_title);
		String message = f.getResources().getString(R.string.fmi_query_user_profile);
		String positiveText = f.getResources().getString(R.string.fmi_qup_dialog_ok);
		String negativeText = f.getResources().getString(R.string.fmi_qup_dialog_later);

		intent.putExtra(DsaDialogActivity.EXTRA_DIALOG_ID, DSA_ID_USER_PROFILE_DIALOG_ID);
		intent.putExtra(DsaDialogActivity.EXTRA_TITLE, title);
		intent.putExtra(DsaDialogActivity.EXTRA_MESSAGE, message);
		intent.putExtra(DsaDialogActivity.EXTRA_POSITIVE_TEXT, positiveText);
		intent.putExtra(DsaDialogActivity.EXTRA_POSITIVE_ID, DSA_ID_USER_PROFILE_DIALOG_OK);
		intent.putExtra(DsaDialogActivity.EXTRA_NEGATIVE_TEXT, negativeText);
		intent.putExtra(DsaDialogActivity.EXTRA_NEGATIVE_ID, DSA_ID_USER_PROFILE_DIALOG_LATER);
		f.startActivity(intent);
	}
}
