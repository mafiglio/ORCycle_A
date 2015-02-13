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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

public class NoteDetailActivity extends Activity {

	public static final String MODULE_TAG = "NoteDetailActivity";

	public static final String EXTRA_NOTE_ID = "noteId";
	public static final String EXTRA_NOTE_SEVERITY = "noteSeverity";
	public static final String EXTRA_NOTE_SOURCE = "noteSource";
	public static final int EXTRA_NOTE_SOURCE_UNDEFINED = -1;
	public static final int EXTRA_NOTE_ID_UNDEFINED = -1;
	public static final int EXTRA_NOTE_TYPE_UNDEFINED = -1;
	public static final int EXTRA_NOTE_SOURCE_MAIN_INPUT = 0;
	public static final int EXTRA_NOTE_SOURCE_TRIP_MAP = 1;

	public static final String EXTRA_TRIP_ID = "EXTRA_TRIP_ID";
	private static final int EXTRA_TRIP_ID_UNDEFINED = -1;
	public static final String EXTRA_TRIP_SOURCE = "tripSource";
	private static final int EXTRA_TRIP_SOURCE_UNDEFINED = -1;

	private static final int CAMERA_REQUEST = 1888;
	private static final int IMAGE_REQUEST = 1889;
	private static final int WEB_VIEW_REQUEST = 1890;

	long noteId;
	int noteSource;
	private long tripId;
	private int tripSource;
	private int reportType;

	private EditText noteDetails;
	private ImageButton imageButton;
	private ImageView imageView;
	private Bitmap photo;
	private Uri uri;
	private Dialog attachDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);

			loadVars();

			// setup main view
			setContentView(R.layout.activity_note_detail);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

			// get references to view widgets
			noteDetails = (EditText) findViewById(R.id.et_and_note_detail);
			imageView = (ImageView) findViewById(R.id.iv_and_image_view);
			// imageView.setVisibility(4);
			imageButton = (ImageButton) findViewById(R.id.ib_and_image_button);

			// show input keyboard
			this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

			// setup photo button
			Button addPhotoButton = (Button) findViewById(R.id.btn_and_add_photo);
			addPhotoButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					try {
						showAttachDialog();
					}
					catch(Exception ex) {
						Log.e(MODULE_TAG, ex.getMessage());
					}
				}
			});
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	private void loadVars() {

		// get input values for this view
		Intent myIntent = getIntent();

		noteId = myIntent.getLongExtra(EXTRA_NOTE_ID, EXTRA_NOTE_ID_UNDEFINED);
		if (EXTRA_NOTE_ID_UNDEFINED == noteId) {
			throw new IllegalArgumentException(MODULE_TAG + ": EXTRA_NOTE_ID undefined.");
		}

		noteSource = myIntent.getIntExtra(EXTRA_NOTE_SOURCE, EXTRA_NOTE_SOURCE_UNDEFINED);
		if (!((noteSource == EXTRA_NOTE_SOURCE_MAIN_INPUT) ||(noteSource == EXTRA_NOTE_SOURCE_TRIP_MAP))) {
			throw new IllegalArgumentException(MODULE_TAG + ": EXTRA_NOTE_SOURCE invalid argument.");
		}

		// Note: these extras are used for transitioning back to the TripMapActivity if done
		if (EXTRA_TRIP_ID_UNDEFINED == (tripId = myIntent.getLongExtra(EXTRA_TRIP_ID, EXTRA_TRIP_ID_UNDEFINED))) {
			throw new IllegalArgumentException(MODULE_TAG + ": invalid extra - EXTRA_TRIP_ID");
		}

		if (EXTRA_TRIP_SOURCE_UNDEFINED == (tripSource = myIntent.getIntExtra(EXTRA_TRIP_SOURCE, EXTRA_TRIP_SOURCE_UNDEFINED))) {
			throw new IllegalArgumentException(MODULE_TAG + ": invalid extra - EXTRA_TRIP_SOURCE");
		}

		reportType = myIntent.getIntExtra(ReportTypeActivity.EXTRA_REPORT_TYPE, ReportTypeActivity.EXTRA_REPORT_TYPE_UNDEFINED);
		if (ReportTypeActivity.EXTRA_REPORT_TYPE_UNDEFINED == reportType) {
			throw new IllegalArgumentException(MODULE_TAG + ": invalid extra - EXTRA_REPORT_TYPE");
		}
	}

	// *********************************************************************************
	// *                                Attach Dialog
	// *********************************************************************************

	private void showAttachDialog() {

		attachDialog = new Dialog(this);
		attachDialog.setContentView(R.layout.dialog_attach);
		attachDialog.setTitle("Add Photo");
		attachDialog.setCancelable(true);

		((ImageButton) attachDialog.findViewById(R.id.attach_stored_image))
			.setOnClickListener(new AttachStoredImage_OnClickListener());

		((ImageButton) attachDialog.findViewById(R.id.attach_camera_photo))
			.setOnClickListener(new AttachCameraPhoto_OnClickListener());

		attachDialog.show();
	}

    /**
     * Class: AttachStoredImage_OnClickListener
     *
     * Description: Callback to be invoked when AttachStoredImage button is clicked
     */
	private final class AttachStoredImage_OnClickListener implements View.OnClickListener {

		/**
		 * Description: Handles onClick for view
		 */
		public void onClick(View v) {
			try {
				// initialize global result variables
				photo = null; uri = null;

				transitionToGetImageContentActivity();

				attachDialog.dismiss();
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

    /**
     * Class: AttachCameraPhoto_OnClickListener
     *
     * Description: Callback to be invoked when AttachCameraPhoto button is clicked
     */
	private final class AttachCameraPhoto_OnClickListener implements View.OnClickListener {

		/**
		 * Description: Handles onClick for view
		 */
		public void onClick(View v) {
			try {
				transitionToGetCameraContentActivity();
				attachDialog.dismiss();
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	// *********************************************************************************
	// *                           Image and Camera Results
	// *********************************************************************************

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		try {
			if (resultCode == RESULT_OK) {
				if (requestCode == CAMERA_REQUEST) {
					uri = null;
					photo = (Bitmap) data.getExtras().get("data");
					imageView.setImageBitmap(photo);
				}
				else if (requestCode == IMAGE_REQUEST) {
					photo = null;
					uri = data.getData();
					imageView.setImageURI(uri);
				}
				else if (requestCode == WEB_VIEW_REQUEST) {
					transitionToSourceActivity();
				}
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	public static byte[] getBitmapAsByteArray(Bitmap bitmap) {

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		bitmap.compress(CompressFormat.JPEG, 100, outputStream);
		return outputStream.toByteArray();
	}

	public static byte[] getBitmapAsByteArray(Context context, Uri uri) {

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 8;

	    try {
	    	InputStream inStream = context.getContentResolver().openInputStream(uri);
	    	Bitmap bitmap = BitmapFactory.decodeStream(inStream, null, options);
	    	bitmap.compress(CompressFormat.JPEG, 100, outputStream);
			return outputStream.toByteArray();
	    }
	    catch(FileNotFoundException ex) {
	    	Log.e(MODULE_TAG, ex.getMessage());
	    }
    	return null;
	}

	@SuppressLint("SimpleDateFormat")
	void submit(String noteDetailsToUpload) {

		byte[] noteImage;

		NoteData note = NoteData.fetchNote(NoteDetailActivity.this, noteId);

		// Start time format displayed in note list
		String fancyStartTime = (new SimpleDateFormat("MMMM d, y  HH:mm a")).format(note.startTime);
		float[] latLng = null;

		if (photo != null) {
			latLng = getLatLng(photo);
			noteImage = getBitmapAsByteArray(photo);
		}
		else if (uri != null) {
			latLng = getLatLng(this, uri);
			noteImage = getBitmapAsByteArray(this, uri);
		}
		else {
			noteImage = null;
		}

		// store note details in local database
		if (null != latLng) {
			note.updateNoteLatLng(latLng[0], latLng[1]);
		}
		note.updateNote(fancyStartTime, noteDetailsToUpload, noteImage);
		note.updateNoteStatus(NoteData.STATUS_COMPLETE);

		if (note.noteStatus < NoteData.STATUS_SENT) {
			// And upload to the cloud database, too! W00t W00t!
			NoteUploader uploader = new NoteUploader(NoteDetailActivity.this, MyApplication.getInstance().getUserId());
			NoteUploader.setPending(note.noteId, true);
			uploader.execute(note.noteId);
		}

		dialogMaps();
	}

	/**
	 * Creates the menu items
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		try {
			// Inflate the menu items for use in the action bar
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.note_detail, menu);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * Handles item selections
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {

		case R.id.action_skip_note_detail: // skip

			try {
				submit("");
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
			return true;

		case R.id.action_save_note_detail: // save

			try {
				submit(noteDetails.getEditableText().toString());
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
			return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * 2.0 and above
	 */
	@Override
	public void onBackPressed() {
		try {
			transitionToPreviousActivity();
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	// *********************************************************************************
	// *                         Image helper functions
	// *********************************************************************************

	/**
	 * Retrieves Exif metadata from an image file
	 * @param uri
	 * @return
	 */
	private float[] getLatLng(Context context, Uri uri) {

		InputStream inStream = null;
		String fileName;

		try {
	    	if (null != (inStream = context.getContentResolver().openInputStream(uri))) {
	    		if (null != (fileName = downloadContent(context, inStream))) {
	    			return getLatLng(fileName);
	    		}
	    	}
	    }
		catch(FileNotFoundException ex) {
			Log.e(MODULE_TAG, ex.getMessage());
			return null;
		}
		finally {
			if (null != inStream) {
				try {
					inStream.close();
			    }
				catch(IOException ex) {
					Log.e(MODULE_TAG, ex.getMessage());
				}
			}
		}
	    	return null;
	}

	private String downloadContent(Context context, InputStream input) {

		byte[] buffer = new byte[4096];
		int bytesRead;
		FileOutputStream fos = null;

		DbAdapter dbAdapter = new DbAdapter(this);
		String noteImagesDirName = dbAdapter.getNoteImageDirectory();
		String localFileName = noteImagesDirName + "/tmp.jpg";

		File file = new File(localFileName);
		if (file.exists()) {
			file.delete();
		}

		try {
			fos = new FileOutputStream(localFileName);
			while ((bytesRead = input.read(buffer, 0, buffer.length)) > -1) {
				fos.write(buffer, 0, bytesRead);
			}
			return localFileName;
		}
		catch(IOException ex) {
			Log.e(MODULE_TAG, ex.getMessage());
			return null;
		}
		finally {
			if (null != fos) {
				try {
					fos.close();
				}
				catch(Exception ex) {
					Log.e(MODULE_TAG, ex.getMessage());
				}
			}
		}
	}

	/**
	 * Retrieves Exif metadata from a photo bitmap
	 * @param photo
	 * @return
	 */
	private float[] getLatLng(Bitmap photo) {
		return null;
	}

	private float[] getLatLng(String fileName) {

		float[] latLng = new float[2];

		try {
			File file = new File(fileName);
			if (file.exists()) {
				ExifInterface exif = new ExifInterface(fileName);

				if (exif.getLatLong(latLng)) {
			    	return latLng;
				}
			}
		}
		catch(IOException ex) {
	    	Log.e(MODULE_TAG, ex.getMessage());
		}
		return null;
	}

	// *********************************************************************************
	// *                            ORcycle Maps Dialog
	// *********************************************************************************

	/**
	 * Build dialog drecting user to ORcycle maps website
	 */
	private void dialogMaps() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.nda_dialog_maps_title);
		builder.setMessage(getResources().getString(R.string.nda_dialog_maps_message));
		builder.setCancelable(false);
		builder.setPositiveButton(getResources().getString(R.string.nda_dialog_maps_button_now),
				new DialogMaps_NowListener());
		builder.setNegativeButton(getResources().getString(R.string.nda_dialog_maps_button_later),
				new DialogMaps_LaterListener());
		final AlertDialog alert = builder.create();
		alert.show();
	}

	private final class DialogMaps_NowListener implements DialogInterface.OnClickListener {
		public void onClick(final DialogInterface dialog, final int id) {
			transitionToWebViewActivity();
			dialog.cancel();
		}
	}

	private final class DialogMaps_LaterListener implements DialogInterface.OnClickListener {
		public void onClick(final DialogInterface dialog, final int id) {
			transitionToSourceActivity();
			dialog.cancel();
		}
	}

	// *********************************************************************************
	// *                    Transitioning to other activities
	// *********************************************************************************

	private void transitionToSourceActivity() {
		if (noteSource == EXTRA_NOTE_SOURCE_MAIN_INPUT) {
			transitionToTabsConfigActivity();
		} else {
			transitionToTripMapActivity();
		}
	}

	private void transitionToPreviousActivity() {
		// Cancel
		if (reportType == ReportTypeActivity.EXTRA_REPORT_TYPE_ACCIDENT_REPORT) {
			transitionToReportAccidentsActivity();
		}
		else if (reportType == ReportTypeActivity.EXTRA_REPORT_TYPE_SAFETY_ISSUE_REPORT) {
			transitionToReportSafetyIssuesActivity();
		}
	}

	private void transitionToReportSafetyIssuesActivity() {

		// Create intent to go back to the recording screen in the Tabsconfig activity
		Intent intent = new Intent(this, ReportSafetyIssuesActivity.class);
		intent.putExtra(NoteQuestionsActivity.EXTRA_NOTE_ID, noteId);
		intent.putExtra(NoteQuestionsActivity.EXTRA_NOTE_SOURCE, noteSource);
		intent.putExtra(NoteQuestionsActivity.EXTRA_TRIP_ID, tripId);
		intent.putExtra(NoteQuestionsActivity.EXTRA_TRIP_SOURCE, tripSource);
		intent.putExtra(ReportAccidentsActivity.EXTRA_IS_BACK, true);

		// Exit this activity
		startActivity(intent);
		overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
		finish();
	}

	private void transitionToReportAccidentsActivity() {

		// Create intent to go back to the recording screen in the Tabsconfig activity
		Intent intent = new Intent(this, ReportAccidentsActivity.class);
		intent.putExtra(NoteQuestionsActivity.EXTRA_NOTE_ID, noteId);
		intent.putExtra(NoteQuestionsActivity.EXTRA_NOTE_SOURCE, noteSource);
		intent.putExtra(NoteQuestionsActivity.EXTRA_TRIP_ID, tripId);
		intent.putExtra(NoteQuestionsActivity.EXTRA_TRIP_SOURCE, tripSource);
		intent.putExtra(ReportSafetyIssuesActivity.EXTRA_IS_BACK, true);

		// Exit this activity
		startActivity(intent);
		overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
		finish();
	}

	private void transitionToTripMapActivity() {

		// Create intent to go back to the TripMapActivity
		Intent intent = new Intent(this, TripMapActivity.class);
		intent.putExtra(TripMapActivity.EXTRA_TRIP_ID, tripId);
		intent.putExtra(TripMapActivity.EXTRA_TRIP_SOURCE, tripSource);

		// Exit this activity
		startActivity(intent);
		overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
		finish();
	}

	private void transitionToTabsConfigActivity() {

		// Create intent to go back to the recording screen in the Tabsconfig activity
		Intent intent = new Intent(this, TabsConfig.class);
		intent.putExtra(TabsConfig.EXTRA_SHOW_FRAGMENT, TabsConfig.FRAG_INDEX_MAIN_INPUT);

		// Exit this activity
		startActivity(intent);
		overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
		finish();
	}

	private void transitionToGetImageContentActivity() {

		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		// assures chooser picks a stream that can be opened
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		// assures that the image is local to the device (not internet)
		intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);

		startActivityForResult(
				Intent.createChooser(intent, getResources().getText(R.string.chooser_image)),
				IMAGE_REQUEST);
	}

	private void transitionToGetCameraContentActivity() {
		Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(cameraIntent, CAMERA_REQUEST);
	}

	private void transitionToWebViewActivity() {
		String title = getResources().getString(R.string.webview_title_orcycle_maps);
		Intent intent = new Intent(this, WebViewActivity.class);
		intent.putExtra(WebViewActivity.EXTRA_URL, MyApplication.URI_ORCYCLE_MAPS);
		intent.putExtra(WebViewActivity.EXTRA_TITLE, title);
		startActivityForResult(intent, WEB_VIEW_REQUEST);
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	}
}
