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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

public class NoteDetailActivity extends Activity {

	public static final String MODULE_TAG = "NoteDetailActivity";

	public static final String ORCYCLE_EMAIL_ADDRESS = "askodot@odot.state.or.us";
	public static final String ORCYCLE_CC_ADDRESS = "orcycle@pdx.edu";
	//public static final String ORCYCLE_EMAIL_ADDRESS = "robin5@pdx.edu";

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
	private static final int EMAIL_REQUEST = 1891;

	private long noteId;
	private int noteSource;
	private NoteData note;
	private long tripId;
	private int tripSource;
	private int reportType;
	private double emailReportLat;  // original latitude location of report set by user
	private double emailReportLng;	// original longitude location of report set by user
	private double emailImageLat;  	// latitude location set by image
	private double emailImageLng;	// longitude location set by image
	private boolean imageHasLatLng = false;

	private EditText noteDetails;
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
			if (requestCode == EMAIL_REQUEST) {
				note.updateEmailSent(true);
				uploadNote();
				dialogEmailEpilog();
			}
			else if (resultCode == RESULT_OK) {
				if (requestCode == CAMERA_REQUEST) {
					uri = null;
					photo = (Bitmap) data.getExtras().get("data");
					imageView.setImageBitmap(photo);
				}
				else if (requestCode == IMAGE_REQUEST) {
					photo = null;
					uri = data.getData();
					imageView.setImageBitmap(getReducedBitmap(uri));
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

	private Bitmap getReducedBitmap(Uri uri) {

		InputStream inStream = null;
		Bitmap thumbnail = null;
		try {
			float scale = -1;
			int width = imageView.getWidth();
			int height = imageView.getHeight();

			if ((scale = getScale(uri, width, height)) <= 0) {
				return null;
			}

			// sampleSize must be a power of 2
			int sampleSize = 1;
			while (sampleSize < scale) {
			    sampleSize *= 2;
			}

			Options bitmapOptions = new Options();
			// this is why you can not have an image scaled as you would like to have
			bitmapOptions.inSampleSize = sampleSize;
			// now we want to load the image
			bitmapOptions.inJustDecodeBounds = false;
			// Let's load just the part of the image necessary for creating the thumbnail, not the whole image
			inStream = getContentResolver().openInputStream(uri);
			thumbnail = BitmapFactory.decodeStream(inStream, null, bitmapOptions);
		}
		catch(FileNotFoundException ex) {
			Log.e(MODULE_TAG, ex.getMessage());
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
		return thumbnail;
	}

	private float getScale(Uri uri, int desiredWidth, int desiredHeight) {
		InputStream inStream = null;
		float scale = -1;

		try {
			inStream = getContentResolver().openInputStream(uri);

			Options bitmapOptions = new Options();
			// obtain the size of the image, without loading it in memory
			bitmapOptions.inJustDecodeBounds = true;

			BitmapFactory.decodeStream(inStream, null, bitmapOptions);
			// find the best scaling factor for the desired dimensions
			float widthScale = (float)bitmapOptions.outWidth/desiredWidth;
			float heightScale = (float)bitmapOptions.outHeight/desiredHeight;
			scale = Math.min(widthScale, heightScale);
		}
		catch(FileNotFoundException ex) {
			Log.e(MODULE_TAG, ex.getMessage());
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
		return scale;
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

		note = NoteData.fetchNote(NoteDetailActivity.this, noteId);
		emailReportLat = note.getLatitude();
		emailReportLng = note.getLongitude();

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

		imageHasLatLng = (null != latLng);
		// store note details in local database
		if (imageHasLatLng) {
			// replace report location with the location from the image
			note.updateNoteLatLng(latLng[0], latLng[1]);

			int iLat = (int) (latLng[0] * 1E6);
			int iLng = (int) (latLng[1] * 1E6);
			emailImageLat = iLat;
			emailImageLng = iLng;
		}

		note.updateNote(noteDetailsToUpload, noteImage);
		note.updateNoteStatus(NoteData.STATUS_COMPLETE);

		// Query user to upload an email

		if (note.isSafetyIssue()) {
			dialogEmail();
		}
		else {
			uploadNote();
			transitionToSourceActivity();
		}
	}

	/**
	 * Instantiates NoteUploader and launches upload thread.
	 */
	private void uploadNote() {

		if (note.getNoteStatus() < NoteData.STATUS_SENT) {
			// And upload to the cloud database, too! W00t W00t!
			NoteUploader uploader = new NoteUploader(NoteDetailActivity.this, MyApplication.getInstance().getUserId());
			NoteUploader.setPending(note.getNoteId(), true);
			uploader.execute(note.getNoteId());
		}
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
	 * @param photo bitmap
	 * @return always null
	 */
	private float[] getLatLng(Bitmap photo) {
		return null;
	}

	/**
	 * Retrieves Exif metadata from a bitmap file
	 * @param fileName file name of bitmap file
	 * @return an array of floats with first entry latitude, and second entry longitude
	 */
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
	// *                            ORcycle E-mail Dialog
	// *********************************************************************************

	/**
	 * Build dialog asking user to send email
	 */
	private void dialogEmail() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.nda_dialog_email_title);
		builder.setMessage(R.string.nda_dialog_email_message);
		builder.setCancelable(false);
		builder.setPositiveButton(R.string.nda_dialog_email_button_yes,
				new DialogEmail_YesListener());
		builder.setNegativeButton(R.string.nda_dialog_email_button_no,
				new DialogEmail_NoListener());
		final AlertDialog alert = builder.create();
		alert.show();
	}

	private final class DialogEmail_YesListener implements DialogInterface.OnClickListener {
		public void onClick(final DialogInterface dialog, final int id) {
			try {
				SharedPreferences settings = getSharedPreferences(UserInfoActivity.PREFS_USER_INFO_UPLOAD, Context.MODE_PRIVATE);
				String userName = settings.getString(String.valueOf(UserInfoActivity.PREF_EMAIL_NAME), "");
				String userPhone = settings.getString(String.valueOf(UserInfoActivity.PREF_EMAIL_PHONE), "");
				String userEmail = settings.getString(String.valueOf(UserInfoActivity.PREF_EMAIL), "");

				NoteEmail noteEmail = new NoteEmail(NoteDetailActivity.this, note, imageHasLatLng,
						emailReportLat, emailReportLng, emailImageLat, emailImageLng,
						userName, userPhone, userEmail);

				transitionToEmailActivity(noteEmail);
			}
			catch (Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
			finally {
				dialog.cancel();
			}
		}
	}

	private final class DialogEmail_NoListener implements DialogInterface.OnClickListener {
		public void onClick(final DialogInterface dialog, final int id) {
			try {
				uploadNote();
				transitionToSourceActivity();
			}
			catch (Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
			finally {
				dialog.cancel();
			}
		}
	}

	// *********************************************************************************
	// *                            ORcycle E-mail Epilog Dialog
	// *********************************************************************************

	/**
	 * Build dialog telling user reporting process
	 */
	private void dialogEmailEpilog() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.nda_dialog_epilog_title);
		builder.setMessage(R.string.nda_dialog_epilog_message);
		builder.setCancelable(false);
		builder.setPositiveButton(R.string.nda_dialog_epilog_button_ok,
				new DialogEmailEpilog_OkListener());
		final AlertDialog alert = builder.create();
		alert.show();
	}

	private final class DialogEmailEpilog_OkListener implements DialogInterface.OnClickListener {
		public void onClick(final DialogInterface dialog, final int id) {
			try {
				if (MyApplication.getInstance().getHintEmailNameAndNumber()) {
					dialogEmailNameAndNumber();
				}
				else {
					transitionToSourceActivity();
				}
			}
			catch (Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
			finally {
				dialog.cancel();
			}
		}
	}

	// *********************************************************************************
	// *                       Email Name And Number Dialog
	// *********************************************************************************

	private void dialogEmailNameAndNumber() {

		// Create dialog
		DsaDialog dsaDialog = new DsaDialog(this, null,				// context and title
			R.string.nda_email_message,								// message text
			new dialogEmailNameAndNumber_CheckedChangeListener(),	// don't show again checkbox listener
			R.string.nda_dialog_button_ok, 							// positive button text
			new dialogEmailNameAndNumber_OkListener(),				// positive button listener
			-1, null, 												// neutral button text and listener
			-1, null); 												// negative button text and listener

		// Show dialog
		dsaDialog.show();
	}

    private final class dialogEmailNameAndNumber_OkListener implements
			DialogInterface.OnClickListener {
		public void onClick(final DialogInterface dialog, final int id) {
			try {
				transitionToSourceActivity();
			}
			catch (Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
			finally {
				dialog.cancel();
			}
		}
	}

    private final class dialogEmailNameAndNumber_CheckedChangeListener implements
    CompoundButton.OnCheckedChangeListener {
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			try {
				MyApplication.getInstance().setHintEmailNameAndNumber(!isChecked);
			}
			catch (Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
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

	private void transitionToEmailActivity(NoteEmail noteEmail) {
		try {
			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_EMAIL, new String[] { ORCYCLE_EMAIL_ADDRESS });
			intent.putExtra(Intent.EXTRA_CC, new String[] { ORCYCLE_CC_ADDRESS });
			intent.putExtra(Intent.EXTRA_SUBJECT, noteEmail.getSubject());
			intent.putExtra(Intent.EXTRA_TEXT, noteEmail.getText());

			// Add the image attachment to the email if one exists
			Uri attachment = null;
		    if (null != (attachment = noteEmail.getAttachment())) {
		    	intent.putExtra(Intent.EXTRA_STREAM, attachment);
		    }

		    // launch the email chooser activity and return the result to this activity
		    startActivityForResult(Intent.createChooser(intent, ""), EMAIL_REQUEST);

		    // use a slide out transition
			overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

			// Note that this activity is left on the activity stack.
		}
		catch (Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}
}
