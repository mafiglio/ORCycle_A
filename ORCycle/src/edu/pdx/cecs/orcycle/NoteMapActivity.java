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

import java.text.SimpleDateFormat;
import java.util.Locale;

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

	private static final SimpleDateFormat reportDateFormatter = new SimpleDateFormat("EEEE, MM/dd/yyyy", Locale.US);

	public GoogleMap map;
	private MenuItem mnuInfo;
	private MenuItem mnuImage;
	private MenuItem mnuMap;
	private ImageView imageView;
	private Bitmap photo;
	private View questionsView;
	private boolean noteHasImage;
	private NoteMapView currentView = NoteMapView.map;
	private String[] accidentSeverities;
	private String[] problemSeverity;

	// *********************************************************************************
	// *                              Fragment Handlers
	// *********************************************************************************

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		try {
			setContentView(R.layout.activity_note_map);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

			accidentSeverities = getResources().getStringArray(R.array.ara_a_severity_2);
			problemSeverity = getResources().getStringArray(R.array.arsi_a_urgency_2);

			imageView = (ImageView) findViewById(R.id.imageView);
			imageView.setVisibility(View.INVISIBLE);

			View accidentView = findViewById(R.id.report_accident_root_view);
			accidentView.setVisibility(View.INVISIBLE);

			View SafetyIssueView = findViewById(R.id.report_safety_issue_root_view);
			SafetyIssueView.setVisibility(View.INVISIBLE);

			// Set zoom controls
			map = ((MapFragment) getFragmentManager().findFragmentById(
					R.id.noteMap)).getMap();

			Bundle cmds = getIntent().getExtras();
			long noteId = cmds.getLong(EXTRA_NOTE_ID);

			NoteData note = NoteData.fetchNote(this, noteId);

			if (note.isAccident()) {
				questionsView = accidentView;
			}
			else {
				questionsView = SafetyIssueView;
			}


			// Show note details
			TextView tvHeaderSeverity = (TextView) findViewById(R.id.tvHeaderSeverity);
			TextView tvHeaderFancyStart = (TextView) findViewById(R.id.tvHeaderFancyStart);
			TextView tvNmComment = (TextView) findViewById(R.id.tvNmComment);
			TextView tvNmComment2 = (TextView) findViewById(R.id.tvNmComment2);

			tvHeaderSeverity.setText(getNoteSeverityText(note.getNoteSeverity()));
			tvHeaderFancyStart.setText(note.notefancystart);
			tvNmComment.setText(note.notedetails);
			tvNmComment2.setText(note.notedetails);

			TextView tvAccidentSeverity = (TextView) findViewById(R.id.tv_anm_a_severity_of_problem);
			TextView tvAccidentObject = (TextView) findViewById(R.id.tv_anm_a_object);
			TextView tvAccidentActions = (TextView) findViewById(R.id.tv_anm_a_actions);
			TextView tvAccidentContrib = (TextView) findViewById(R.id.tv_anm_a_contrib);
			TextView tvSafetyIssue = (TextView) findViewById(R.id.tv_anm_a_safety_issue);
			TextView tvSafetyUrgency = (TextView) findViewById(R.id.tv_anm_a_urgency);
			TextView tvIssueDate = (TextView) findViewById(R.id.tv_anm_a_issue_date);
			TextView tvCrashDate = (TextView) findViewById(R.id.tv_anm_a_crash_date);

			tvAccidentSeverity.setText("");
			tvAccidentObject.setText("");
			tvAccidentActions.setText("");
			tvAccidentContrib.setText("");
			tvSafetyIssue.setText("");
			tvSafetyUrgency.setText("");

			if (note.reportDate <= 0) {
				tvIssueDate.setText("Not specified");
				tvCrashDate.setText("Not specified");
			}
			else {
				tvIssueDate.setText(reportDateFormatter.format(note.reportDate));
				tvCrashDate.setText(reportDateFormatter.format(note.reportDate));
			}

			// Center & zoom the map
			LatLng center = new LatLng(note.getLatitude() * 1E-6, note.getLongitude() * 1E-6);

			try {
				map.animateCamera(CameraUpdateFactory.newLatLngZoom(center, 16));
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}

			// Add note marker to map
			noteHasImage = false;
			if (note != null) {

				LatLng notePosition = new LatLng(note.getLatitude() * 1E-6, note.getLongitude() * 1E-6);

				int noteDrawable = DbAnswers.getNoteSeverityMapImageResourceId(note.getNoteSeverity());

				map.addMarker(new MarkerOptions()
					.icon(BitmapDescriptorFactory.fromResource(noteDrawable))
					.anchor(0.0f, 1.0f) // Anchors the marker on the bottom left
					.position(notePosition));

				noteHasImage = note.hasImage();
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

	private String getNoteSeverityText(int noteSeverity) {

		int index;

		if (-1 != (index = DbAnswers.findIndex(DbAnswers.accidentSeverity, noteSeverity))) {
			return accidentSeverities[index + 1];
		}

		if (-1 != (index = DbAnswers.findIndex(DbAnswers.safetyUrgency, noteSeverity))) {
			return problemSeverity[index + 1];
		}

		return "Unknown";
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

			StringBuilder sbAccidentSeverity = new StringBuilder();
			StringBuilder sbAccidentObject = new StringBuilder();
			StringBuilder sbAccidentAction = new StringBuilder();
			StringBuilder sbAccidentContrib = new StringBuilder();
			StringBuilder sbSafetyIssue = new StringBuilder();
			StringBuilder sbSafetySeverity = new StringBuilder();

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

					case DbQuestions.ACCIDENT_SEVERITY:
						append(sbAccidentSeverity, R.array.ara_a_severity, DbAnswers.accidentSeverity, answerId);
						break;

					case DbQuestions.ACCIDENT_OBJECT:
						append(sbAccidentObject, R.array.ara_a_object, DbAnswers.accidentObject, answerId, otherText);
						break;

					case DbQuestions.ACCIDENT_ACTION:
						append(sbAccidentAction, R.array.ara_a_actions, DbAnswers.accidentAction, answerId, otherText);
						break;

					case DbQuestions.ACCIDENT_CONTRIB:
						append(sbAccidentContrib, R.array.ara_a_contributers, DbAnswers.accidentContrib, answerId, otherText);
						break;

					case DbQuestions.SAFETY_ISSUE:
						append(sbSafetyIssue, R.array.arsi_a_safety_issues, DbAnswers.safetyIssue, answerId, otherText);
						break;

					case DbQuestions.SAFETY_URGENCY:
						append(sbSafetySeverity, R.array.arsi_a_urgency, DbAnswers.safetyUrgency, answerId);
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
			TextView tvAccidentSeverity = (TextView) findViewById(R.id.tv_anm_a_severity_of_problem);
			TextView tvAccidentobject = (TextView) findViewById(R.id.tv_anm_a_object);
			TextView tvAccidentActions = (TextView) findViewById(R.id.tv_anm_a_actions);
			TextView tvAccidentContrib = (TextView) findViewById(R.id.tv_anm_a_contrib);
			TextView tvSafetyIssue = (TextView) findViewById(R.id.tv_anm_a_safety_issue);
			TextView tvSafetyUrgency = (TextView) findViewById(R.id.tv_anm_a_urgency);

			tvAccidentSeverity.setText(sbAccidentSeverity.toString());
			tvAccidentobject.setText(sbAccidentObject.toString());
			tvAccidentActions.setText(sbAccidentAction.toString());
			tvAccidentContrib.setText(sbAccidentContrib.toString());
			tvSafetyIssue.setText(sbSafetyIssue.toString());
			tvSafetyUrgency.setText(sbSafetySeverity.toString());
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		finally {
			mDb.close();
		}
	}

	private void append(StringBuilder sb, int textArrayId, int[] answers, int answerId) {
		if (sb.length() > 0)
			sb.append("\r\n");
		sb.append(DbAnswers.getAnswerText(this, textArrayId, answers, answerId));
	}

	private void append(StringBuilder sb, int textArrayId, int[] answers, int answerId, String otherText) {
		if ((null == otherText) || otherText.equals("")) {
			if (sb.length() > 0)
				sb.append("\r\n");
			sb.append(DbAnswers.getAnswerText(this, textArrayId, answers, answerId));
		}
		else {
			if (sb.length() > 0)
				sb.append("\r\n");
			sb.append(otherText);
		}
	}

}
