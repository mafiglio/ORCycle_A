package edu.pdx.cecs.orcycle;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
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

	public static final String EXTRA_NOTE_ID = "noteid";
	public static final String EXTRA_NOTE_TYPE = "noteType";
	public static final String EXTRA_IS_RECORDING = "isRecording";

	long noteid;
	int noteType = 0;
	boolean isRecording;
	EditText noteDetails;
	ImageButton imageButton;
	ImageView imageView;
	String imageURL = "";
	byte[] noteImage;
	Bitmap photo;
	private static final int CAMERA_REQUEST = 1888;

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
				Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				startActivityForResult(cameraIntent, CAMERA_REQUEST);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
			photo = (Bitmap) data.getExtras().get("data");
			imageView.setImageBitmap(photo);
			Log.v("Jason", "Image Photo: " + photo);
		}
	}

	public static byte[] getBitmapAsByteArray(Bitmap bitmap) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.JPEG ,100, outputStream);
		return outputStream.toByteArray();
	}

	void submit(String noteDetailsToUpload, byte[] noteImage) {

		NoteData note = NoteData.fetchNote(NoteDetailActivity.this, noteid);
		//note.populateDetails();

		// date format for displaying in lists
		SimpleDateFormat sdfStart = new SimpleDateFormat("MMMM d, y  HH:mm a");
		String fancyStartTime = sdfStart.format(note.startTime);
		Log.v("Jason", "Start: " + fancyStartTime);

		// date format for creating image filenames
		SimpleDateFormat sdfStart2 = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		String date = sdfStart2.format(note.startTime);

		// Save the note details to the phone database. W00t!

		String deviceId = MyApplication.getInstance().getDeviceId();
		if (photo != null) {
			noteImage = getBitmapAsByteArray(photo);
			imageURL = deviceId + "-" + date + "-type-" + noteType;
		} else {
			noteImage = null;
			imageURL = "";
		}

		// finalize note values and store in local database
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
			intent = new Intent(this, TabsConfig.class);
			this.finish();
			this.startActivity(intent);
		} else {
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
