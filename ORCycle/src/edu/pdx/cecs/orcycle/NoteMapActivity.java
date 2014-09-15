package edu.pdx.cecs.orcycle;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class NoteMapActivity extends Activity {

	private enum NoteMapView { info, image, map};

	public static final String EXTRA_NOTE_ID = "shownote";
	private static final String MODULE_TAG = "NoteMapActivity";

	public GoogleMap map;
	private MenuItem mnuInfo;
	private MenuItem mnuImage;
	private MenuItem mnuMap;
	private ImageView imageView;
	private Bitmap photo;
	private View questionsView;
	private boolean noteHasImage;
	private NoteMapView currentView = NoteMapView.map;

	// *********************************************************************************
	// *                              Fragment Handlers
	// *********************************************************************************

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		try {
			setContentView(R.layout.activity_note_map);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

			imageView = (ImageView) findViewById(R.id.imageView);
			imageView.setVisibility(View.INVISIBLE);

			questionsView = findViewById(R.id.noteQuestionsRootView);
			questionsView.setVisibility(View.INVISIBLE);

			// Set zoom controls
			map = ((MapFragment) getFragmentManager().findFragmentById(
					R.id.noteMap)).getMap();

			Bundle cmds = getIntent().getExtras();
			long noteid = cmds.getLong(EXTRA_NOTE_ID);

			NoteData note = NoteData.fetchNote(this, noteid);

			// Show note details
			TextView tvHeaderSeverity = (TextView) findViewById(R.id.tvHeaderSeverity);
			TextView tvHeaderFancyStart = (TextView) findViewById(R.id.tvHeaderFancyStart);
			TextView tvNmComment = (TextView) findViewById(R.id.tvNmComment);

			tvHeaderSeverity.setText(DbAnswers.getAnswerText(this, R.array.nmaSeverityOfProblem, DbAnswers.noteSeverity, note.noteSeverity));
			tvHeaderFancyStart.setText(note.notefancystart);
			tvNmComment.setText(note.notedetails);

			// Center & zoom the map
			LatLng center = new LatLng(note.latitude * 1E-6, note.longitude * 1E-6);

			map.animateCamera(CameraUpdateFactory.newLatLngZoom(center, 16));

			// Add note marker to map
			noteHasImage = false;
			if (note != null) {

				LatLng notePosition = new LatLng(note.latitude * 1E-6, note.longitude * 1E-6);

				int noteDrawable = DbAnswers.getNoteSeverityMapImageResourceId(note.noteSeverity);

				map.addMarker(new MarkerOptions()
					.icon(BitmapDescriptorFactory.fromResource(noteDrawable))
					.anchor(0.0f, 1.0f) // Anchors the marker on the bottom left
					.position(notePosition));

				noteHasImage = !note.noteimageurl.equals("") && (note.noteimagedata != null);
			}

			// If image exist, add it to the imageView widget
			if (noteHasImage) {
				// Store photo error, retrieve error
				photo = BitmapFactory.decodeByteArray(note.noteimagedata, 0,
						note.noteimagedata.length);
				if (photo.getHeight() > photo.getWidth()) {
					imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				} else {
					imageView.setScaleType(ImageView.ScaleType.FIT_START);
				}
				imageView.setImageBitmap(photo);
			}

			getNoteResponses(note.noteId);

		} catch (Exception ex) {
			Log.e(MODULE_TAG, ex.toString());
		}
		currentView = NoteMapView.map;
	}

	@Override
	public void onBackPressed() {
		try {
			transitionToTabsConfigActivity();
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		try {
			// Inflate the menu; this adds items to the action bar if it is present.
			getMenuInflater().inflate(R.menu.note_map, menu);

			mnuInfo = menu.getItem(0);
			mnuImage = menu.getItem(1);
			mnuMap = menu.getItem(2);
			setCurrentView(currentView);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}

		return true;
	}

	@Override
	public void onResume() {
		super.onResume();

		try {
			setCurrentView(currentView);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	private void setCurrentView(NoteMapView noteMapView) {

		switch (noteMapView) {

		case info:

			if (noteHasImage) {
				if (NoteMapView.image == currentView) {
					Animation animFadeOut = AnimationUtils.loadAnimation(
							getApplicationContext(), android.R.anim.fade_out);
					imageView.setAnimation(animFadeOut);
				}
				imageView.setVisibility(View.INVISIBLE);
			}

			questionsView.setVisibility(View.VISIBLE);

			if ((null != mnuInfo) && (null != mnuImage) && (null != mnuMap)) {
				mnuInfo.setVisible(false);
				mnuImage.setVisible(noteHasImage);
				mnuMap.setVisible(true);
			}
			break;

		case image:

			questionsView.setVisibility(View.INVISIBLE);

			Animation animFadeIn = AnimationUtils.loadAnimation(
					getApplicationContext(), android.R.anim.fade_in);
			imageView.setAnimation(animFadeIn);
			imageView.setVisibility(View.VISIBLE);

			if ((null != mnuInfo) && (null != mnuImage) && (null != mnuMap)) {
				mnuInfo.setVisible(true);
				mnuImage.setVisible(false);
				mnuMap.setVisible(true);
			}
			break;

		case map:

			questionsView.setVisibility(View.INVISIBLE);

			if (noteHasImage) {
				if (NoteMapView.image == currentView) {
					Animation animFadeOut = AnimationUtils.loadAnimation(
							getApplicationContext(), android.R.anim.fade_out);
					imageView.setAnimation(animFadeOut);
				}
				imageView.setVisibility(View.INVISIBLE);
			}

			if ((null != mnuInfo) && (null != mnuImage) && (null != mnuMap)) {
				mnuInfo.setVisible(true);
				mnuImage.setVisible(noteHasImage);
				mnuMap.setVisible(false);
			}
			break;
		}
		currentView = noteMapView;
	}

	/* Handles item selections */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		try {
			// Handle presses on the action bar items
			switch (item.getItemId()) {

			case R.id.action_note_map_view_info:
				setCurrentView(NoteMapView.info);
				return true;

			case R.id.action_note_map_view_image:
				setCurrentView(NoteMapView.image);
				return true;

			case R.id.action_note_map_view_map:
				setCurrentView(NoteMapView.map);
				return true;

			case R.id.action_note_map_close:
				// close -> go back to FragmentMainInput
				onBackPressed();
				return true;

			default:
				return super.onOptionsItemSelected(item);
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		return super.onOptionsItemSelected(item);
	}

	// *********************************************************************************
	// *                             Transition Functions
	// *********************************************************************************

	private void transitionToTabsConfigActivity() {

		Intent intent = new Intent(NoteMapActivity.this, TabsConfig.class);

		intent.putExtra(TabsConfig.EXTRA_SHOW_FRAGMENT, TabsConfig.FRAG_INDEX_SAVED_NOTES);

		startActivity(intent);
		overridePendingTransition(android.R.anim.fade_in, R.anim.slide_out_down);
		finish();
	}

	// *********************************************************************************
	// *                               Helper Functions
	// *********************************************************************************

	private void getNoteResponses(long noteId) {

		DbAdapter mDb = new DbAdapter(this);

		mDb.openReadOnly();
		try {
			Cursor answers = mDb.fetchNoteAnswers(noteId);

			int questionCol = answers.getColumnIndex(DbAdapter.K_NOTE_ANSWER_QUESTION_ID);
			int answerCol = answers.getColumnIndex(DbAdapter.K_NOTE_ANSWER_ANSWER_ID);
			int otherCol = answers.getColumnIndex(DbAdapter.K_NOTE_ANSWER_OTHER_TEXT);

			StringBuilder sbNoteSeverity = new StringBuilder();
			StringBuilder sbNoteConflict = new StringBuilder();
			StringBuilder sbNoteIssue = new StringBuilder();

			int questionId;
			int answerId;
			String otherText;

			// Cycle thru the database entries
			while (!answers.isAfterLast()) {

				questionId = answers.getInt(questionCol);
				answerId = answers.getInt(answerCol);
				if (null != (otherText = answers.getString(otherCol))) {
					otherText = otherText.trim();
				}

				try {
					switch(questionId) {

					case DbQuestions.NOTE_SEVERITY:
						append(sbNoteSeverity, R.array.nqaSeverityOfProblem, DbAnswers.noteSeverity, answerId);
						break;

					case DbQuestions.NOTE_CONFLICT:
						append(sbNoteConflict, R.array.nqaConflictType, DbAnswers.noteConflict, answerId, otherText);
						break;

					case DbQuestions.NOTE_ISSUE:
						append(sbNoteIssue, R.array.nqaIssueType, DbAnswers.noteIssue, answerId, otherText);
						break;
					}
				}
				catch(Exception ex) {
					Log.e(MODULE_TAG, ex.getMessage());
				}
				// Move to next row
				answers.moveToNext();
			}
			answers.close();

			// Show note details
			TextView tvNmSeverityOfProblem = (TextView) findViewById(R.id.tvNmSeverityOfProblem);
			TextView tvNmConflictType = (TextView) findViewById(R.id.tvNmConflictType);
			TextView tvNmIssueType = (TextView) findViewById(R.id.tvNmIssueType);

			tvNmSeverityOfProblem.setText(sbNoteSeverity.toString());
			tvNmConflictType.setText(sbNoteConflict.toString());
			tvNmIssueType.setText(sbNoteIssue.toString());

		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		finally {
			mDb.close();
		}
	}

	private void append(StringBuilder sb, int textArrayId, int[] answers, int answerId) {
		sb.append(DbAnswers.getAnswerText(this, textArrayId, answers, answerId)).append("\r\n");
	}

	private void append(StringBuilder sb, int textArrayId, int[] answers, int answerId, String otherText) {
		if ((null == otherText) || otherText.equals("")) {
			sb.append(DbAnswers.getAnswerText(this, textArrayId, answers, answerId)).append("\r\n");
		}
		else {
			sb.append(otherText).append("\r\n");
		}
	}

}
