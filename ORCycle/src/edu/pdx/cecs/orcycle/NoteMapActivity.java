package edu.pdx.cecs.orcycle;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
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

	public static final String EXTRA_NOTE_ID = "shownote";
	private static final String MODULE_TAG = "NoteMapActivity";

	public GoogleMap map;
	private MenuItem saveMenuItem;
	private ImageView imageView;
	private Bitmap photo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_note_map);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		imageView = (ImageView) findViewById(R.id.imageView);

		imageView.setVisibility(View.INVISIBLE);

		try {
			// Set zoom controls
			map = ((MapFragment) getFragmentManager().findFragmentById(
					R.id.noteMap)).getMap();

			Bundle cmds = getIntent().getExtras();
			long noteid = cmds.getLong(EXTRA_NOTE_ID);

			NoteData note = NoteData.fetchNote(this, noteid);

			// Show note details
			TextView t1 = (TextView) findViewById(R.id.TextViewMapNoteType);
			TextView t2 = (TextView) findViewById(R.id.TextViewMapNoteDetails);
			TextView t3 = (TextView) findViewById(R.id.TextViewMapNoteFancyStart);

			t1.setText(getNoteTypeText(note.notetype));
			t2.setText(note.notedetails);
			t3.setText(note.notefancystart);

			// Center & zoom the map
			LatLng center = new LatLng(note.latitude * 1E-6, note.longitude * 1E-6);

			map.animateCamera(CameraUpdateFactory.newLatLngZoom(center, 16));

			if (note != null) {
				if (note.notetype >= 0 && note.notetype <= 5) {
					map.addMarker(new MarkerOptions()
							.icon(BitmapDescriptorFactory
									.fromResource(R.drawable.noteissuemapglyph_high))
							.anchor(0.0f, 1.0f) // Anchors the marker on the
												// bottom left
							.position(
									new LatLng(note.latitude * 1E-6,
											note.longitude * 1E-6)));
				} else if (note.notetype >= 6 && note.notetype <= 11) {
					map.addMarker(new MarkerOptions()
							.icon(BitmapDescriptorFactory
									.fromResource(R.drawable.noteassetmapglyph_high))
							.anchor(0.0f, 1.0f) // Anchors the marker on the
												// bottom left
							.position(
									new LatLng(note.latitude * 1E-6,
											note.longitude * 1E-6)));
				}
			}

			Log.v("Jason", "Image Photo: " + note.noteimagedata);
			Log.v("Jason", "Image Photo: " + note.noteimageurl);

			if (note.noteimageurl.equals("")) {
			} else {
				// Store photo error, retrieve error
				photo = BitmapFactory.decodeByteArray(note.noteimagedata, 0,
						note.noteimagedata.length);
				if (photo.getHeight() > photo.getWidth()) {
					imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				} else {
					imageView.setScaleType(ImageView.ScaleType.FIT_START);
				}
				imageView.setImageBitmap(photo);
				Log.v("Jason", "Image Photo: " + photo);
			}

		} catch (Exception e) {
			Log.e("GOT!", e.toString());
		}
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
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.note_map, menu);
		saveMenuItem = menu.getItem(0);
		Bundle cmds = getIntent().getExtras();
		long noteid = cmds.getLong(EXTRA_NOTE_ID);
		NoteData note = NoteData.fetchNote(this, noteid);
		if (note.noteimageurl.equals("")) {
			saveMenuItem.setVisible(false);
		} else {
			saveMenuItem.setVisible(true);
		}
		return true;
	}

	/* Handles item selections */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.action_close_note_map:
			// close -> go back to FragmentMainInput
			onBackPressed();
		case R.id.action_switch_note_view:
			// animation for map and image..
			if (saveMenuItem.getTitle().equals("image")) {
				saveMenuItem.setTitle("map");
				Animation animFadeIn = AnimationUtils.loadAnimation(
						getApplicationContext(), android.R.anim.fade_in);
				imageView.setAnimation(animFadeIn);
				imageView.setVisibility(View.VISIBLE);
			} else if (saveMenuItem.getTitle().equals("map")) {
				saveMenuItem.setTitle("image");
				Animation animFadeOut = AnimationUtils.loadAnimation(
						getApplicationContext(), android.R.anim.fade_out);
				imageView.setAnimation(animFadeOut);
				imageView.setVisibility(View.INVISIBLE);
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void transitionToTabsConfigActivity() {

		Intent intent = new Intent(NoteMapActivity.this, TabsConfig.class);

		intent.putExtra(TabsConfig.EXTRA_SHOW_FRAGMENT, TabsConfig.FRAG_INDEX_SAVED_NOTES);

		startActivity(intent);
		overridePendingTransition(android.R.anim.fade_in, R.anim.slide_out_down);
		finish();
	}

	private String getNoteTypeText(int noteType) {

		String[] noteTypes = getResources().getStringArray(R.array.nqaIssueType);

		int index = DbAnswers.findIndex(DbAnswers.noteIssue, noteType);

		if (-1 != index) {
			return noteTypes[index + 1];
		}
		return "Unknown";
	}

}
