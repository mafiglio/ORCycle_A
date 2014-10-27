package edu.pdx.cecs.orcycle;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;

public class WebViewActivity extends Activity {

	public static final String MODULE_TAG = "WebViewActivity";
	public static final String EXTRA_URL = "url";
	public static final String EXTRA_TITLE = "title";
	WebView webView;

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			setContentView(R.layout.activity_web_view);

			Intent intent = getIntent();
			String url = intent.getStringExtra(EXTRA_URL);
			String title = intent.getStringExtra(EXTRA_TITLE);

			if (null != (title = getIntent().getStringExtra(EXTRA_TITLE))) {
				setTitle(title);
			}

			webView = (WebView) findViewById(R.id.web_view);
			//webView.setInitialScale(50);
			webView.getSettings().setJavaScriptEnabled(true);
			webView.getSettings().setBuiltInZoomControls(true);
			webView.getSettings().setDisplayZoomControls(true);
			webView.loadUrl(url);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	private void transitionToPreviousActivity() {
		Intent intent = new Intent(this, TabsConfig.class);
		setResult(RESULT_OK, intent);
		finish();
	}

	@Override
	public void onBackPressed() {
		try {
			transitionToPreviousActivity();
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

}
