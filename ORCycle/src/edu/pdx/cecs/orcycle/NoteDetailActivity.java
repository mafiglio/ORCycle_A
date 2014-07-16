package edu.pdx.cecs.orcycle;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
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

	public static final String EXTRA_NOTE_ID = "noteid";
	public static final String EXTRA_NOTE_TYPE = "noteType";
	public static final String EXTRA_IS_RECORDING = "isRecording";

	private static final int CAMERA_REQUEST = 1888;
	private static final int IMAGE_REQUEST = 1889;

	private Dialog attachDialog;

	long noteid;
	int noteType = 0;
	boolean isRecording;
	EditText noteDetails;
	ImageButton imageButton;
	ImageView imageView;
	String imageURL = "";
	byte[] noteImage;
	Bitmap photo;
	Uri uri;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// get input values for this view
		Intent myIntent = getIntent();
		noteType = myIntent.getIntExtra(EXTRA_NOTE_TYPE, -1);
		noteid = myIntent.getLongExtra(EXTRA_NOTE_ID, -1);
		isRecording = myIntent.getBooleanExtra(EXTRA_IS_RECORDING, false);

		// TODO: Assert that intent values are not set to -1

		// setup main view
		setContentView(R.layout.activity_note_detail);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		// get references to view widgets
		noteDetails = (EditText) findViewById(R.id.editTextNoteDetail);
		imageView = (ImageView) findViewById(R.id.imageView);
		// imageView.setVisibility(4);
		imageButton = (ImageButton) findViewById(R.id.imageButton);

		// show input keyboard
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

		// setup photo button
		Button addPhotoButton = (Button) findViewById(R.id.addPhotoButton);
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

	// *********************************************************************************
	// *                                Attach Dialog
	// *********************************************************************************

	private void showAttachDialog() {

		attachDialog = new Dialog(this);
		attachDialog.setContentView(R.layout.dialog_attach);
		attachDialog.setTitle("chooser_image");
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

				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.setType("image/*");
				// assures chooser picks a stream that can be opened
				intent.addCategory(Intent.CATEGORY_OPENABLE);
				// assures that the image is local to the device (not internet)
				intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);

				startActivityForResult(
						Intent.createChooser(intent, getResources().getText(R.string.chooser_image)),
						IMAGE_REQUEST);
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
				Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				startActivityForResult(cameraIntent, CAMERA_REQUEST);
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
		options.inSampleSize = 16;

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
	void submit(String noteDetailsToUpload, byte[] noteImage) {

		int byteCount;

		NoteData note = NoteData.fetchNote(NoteDetailActivity.this, noteid);

		// format start time displayed in note list
		String fancyStartTime = (new SimpleDateFormat("MMMM d, y  HH:mm a")).format(note.startTime);

		// format date for creating image filename
		String date = (new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")).format(note.startTime);

		// Save the note details to the phone database. W00t!

		String deviceId = MyApplication.getInstance().getDeviceId();
		if (photo != null) {
			noteImage = getBitmapAsByteArray(photo);
			imageURL = deviceId + "-" + date + "-type-" + noteType;
		}
		else if (uri != null) {
			noteImage = getBitmapAsByteArray(this, uri);
			imageURL = uri.toString();
			imageURL = uri.getPath();
		}
		else {
			noteImage = null;
			imageURL = "";
		}

		byteCount = noteImage.length;


		// store note details in local database
		note.updateNote(noteType, fancyStartTime, noteDetailsToUpload, imageURL, noteImage);
		note.updateNoteStatus(NoteData.STATUS_COMPLETE);

		// Now create the MainInput Activity so BACK btn works properly
		// Should not use this.

		// TODO: note uploader
		if (note.notestatus < NoteData.STATUS_SENT) {
			// And upload to the cloud database, too! W00t W00t!
			NoteUploader uploader = new NoteUploader(NoteDetailActivity.this);
			uploader.execute(note.noteid);
		}

		Intent intent;

		if (isRecording) {
			// Intent to go back to recording tab
			intent = new Intent(this, TabsConfig.class);
			this.finish();
			this.startActivity(intent);
		} else {
			// Intent to go to Note map
			intent = new Intent(this, NoteMapActivity.class);
			intent.putExtra(NoteMapActivity.EXTRA_NOTE_ID, note.noteid);
			this.startActivity(intent);
			this.finish();
			this.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		}
	}

	/**
	 * Sets up a transition to the NoteTypeActivity.
	 */
	private void TransitionBackToNoteTypeActivity() {

		// Get intent for transition to NoteTypeActivity.
		Intent intent = new Intent(this, NoteTypeActivity.class);

		// Set values for input to NoteTypeActivity
		intent.putExtra(NoteTypeActivity.EXTRA_NOTE_ID, noteid);
		intent.putExtra(NoteTypeActivity.EXTRA_NOTE_TYPE, noteType);
		intent.putExtra(NoteTypeActivity.EXTRA_IS_RECORDING, isRecording);

		// Initiate transition to NoteTypeActivity
		this.startActivity(intent);
		this.finish();
		this.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	}

	/* Creates the menu items */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.note_detail, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/* Handles item selections */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.action_skip_note_detail:
			// skip
			submit("", null);
			return true;
		case R.id.action_save_note_detail:
			// save
			submit(noteDetails.getEditableText().toString(), noteImage);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// 2.0 and above
	@Override
	public void onBackPressed() {
		// Go back to NoteTypeActivity.
		TransitionBackToNoteTypeActivity();
	}

	// Before 2.0
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// Go back to NoteTypeActivity.
			TransitionBackToNoteTypeActivity();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}
