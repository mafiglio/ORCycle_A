package edu.pdx.cecs.orcycle;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

public class FragmentSettingsController {

	private final Activity activity;

	public FragmentSettingsController(Activity activity) {
		this.activity = activity;
	}

	public void setResult(int result) {

		switch(result) {

		case FragmentSettings.RESULT_USER_INFO_PRESSED:
			transitionToUserInfoActivity();
			break;

		case FragmentSettings.RESULT_GET_STARTED_PRESSED:
			transitionToWebViewActivity(R.string.ats_webview_title_orcycle, MyApplication.ORCYCLE_URI);
			break;

		case FragmentSettings.RESULT_FEEDBACK_PRESSED:
			transitionToUserFeedbackActivity();
			break;

		case FragmentSettings.RESULT_PRIVACY_POLICY_PRESSED:
			transitionToWebViewActivity(R.string.ats_webview_Title_privacy_policy, MyApplication.URI_PRIVACY_POLICY);
			break;

		case FragmentSettings.RESULT_REMINDERS_PRESSED:
			transitionToReminders();
			break;

		case FragmentSettings.RESULT_TUTORIAL_PRESSED:
			transitionToTutorial();
			break;

		case FragmentSettings.RESULT_MAPS_LINK_PRESSED:
			transitionToWebViewActivity(R.string.ats_webview_title_maps, MyApplication.URI_ORCYCLE_MAPS);
			break;

		case FragmentSettings.RESULT_REPORT_HAZARDS_LINK_PRESSED:
			transitionToWebViewActivity(R.string.ats_webview_title_hazards, MyApplication.URI_REPORT_ROAD_HAZARDS);
			break;

		}
	}

	private void transitionToReminders() {
		Intent intent = new Intent(activity, SavedRemindersActivity.class);
		activity.startActivity(intent);
		activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		//activity.finish();
	}

	private void transitionToUserFeedbackActivity() {
		Intent intent = new Intent(activity, UserFeedbackActivity.class);
		activity.startActivity(intent);
		activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		activity.finish();
	}

	private void transitionToUserInfoActivity() {

		// Create intent to go back to the TripMapActivity
		Intent intent = new Intent(activity, UserInfoActivity.class);
		intent.putExtra(UserInfoActivity.EXTRA_PREVIOUS_ACTIVITY, UserInfoActivity.EXTRA_FRAGMENT_SETTINGS);

		// Exit this activity
		activity.startActivity(intent);
		activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		activity.finish();
	}

	private void transitionToTutorial() {
		Intent intent = new Intent(activity, TutorialActivity.class);
		intent.putExtra(TutorialActivity.EXTRA_PREVIOUS_ACTIVITY, TutorialActivity.EXTRA_PREVIOUS_ACTIVITY_USER_SETTINGS);
		activity.startActivity(intent);
		activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	}

	private void transitionToWebViewActivity(int titleId, String uri) {
		String title = activity.getResources().getString(titleId);
		Intent intent = new Intent(activity, WebViewActivity.class);
		intent.putExtra(WebViewActivity.EXTRA_URL, uri);
		intent.putExtra(WebViewActivity.EXTRA_TITLE, title);
		activity.startActivity(intent);
		activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	}

	private void transitionToExternalWebSite(String url) {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		activity.startActivity(intent);
		activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	}
}
