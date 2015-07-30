package edu.pdx.cecs.orcycle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

public class NoteEmail {

	private static final String MODULE_TAG = "NoteEmail";

	private static final String NL2 = "\n\n";
	private static final String NL = "\n";
	private static final String TAB = "    ";

	private final Context context;
	private final String subject;
	private final StringBuilder text = new StringBuilder();;
	private String imageFileName = null;
	private Uri imageUri = null;

	private final StringBuilder sbAccidentSeverity = new StringBuilder();
	private final StringBuilder sbAccidentObject = new StringBuilder();
	private final StringBuilder sbAccidentAction = new StringBuilder();
	private final StringBuilder sbAccidentContrib = new StringBuilder();
	private final StringBuilder sbSafetyIssue = new StringBuilder();
	private final StringBuilder sbSafetySeverity = new StringBuilder();
	private final StringBuilder sbSubjectLineUrgency = new StringBuilder();

	private final boolean hasImage;

	/**
	 * Instantiates an object containing note information to be emailed to application owner
	 * @param context
	 * @param noteData
	 * @param imageHasLatLng
	 * @param reportLat
	 * @param reportLng
	 * @param imageLat
	 * @param imageLng
	 */
	public NoteEmail(Context context, NoteData noteData,
			boolean imageHasLatLng, double reportLat, double reportLng,
			double imageLat, double imageLng) {

		this.context = context;

		// Start time format displayed in note list
		String recordedDate = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)).format(noteData.getRecorded());
		String reportDate = (new SimpleDateFormat("yyyy-MM-dd", Locale.US)).format(noteData.getReportDate());

		// get filename of image.
		imageFileName = noteData.getImageFileName();
		this.hasImage = ((null != imageFileName) && (!imageFileName.equals("")));

		// Assemble answers to note questions
		getNoteResponses(noteData.getNoteId());

		// Generate note text
		text.append("Phone number for contact: <enter here>\n\n");
		text.append("E-mail address: <enter here>\n\n");

		if (noteData.isAccident()) {

			subject = "Crash or near miss report";

			text.append("Report Date:\n\n");
			text.append(TAB);
			text.append(recordedDate);
			text.append(NL2);
			text.append("Severity of the crash event:\n\n");
			text.append(sbAccidentSeverity.toString());
			text.append(NL2);
			text.append("Object (vehicle) related to the event:\n\n ");
			text.append(sbAccidentObject);
			text.append(NL2);
			text.append("Actions related to the event:\n\n ");
			text.append(sbAccidentAction);
			text.append(NL2);
			text.append("What contributed to the event:\n\n");
			text.append(sbAccidentContrib);
			text.append(NL2);
			text.append("Date crash occurred: ");
			text.append(NL2);
			text.append(TAB);
			text.append(reportDate);
			text.append(NL2);
		}
		else if (noteData.isSafetyIssue()){

			subject = sbSubjectLineUrgency.toString();

			text.append("Report Date:\n\n");
			text.append(TAB);
			text.append(recordedDate);
			text.append(NL2);
			text.append("Issue Type(s):\n\n");
			text.append(sbSafetyIssue);
			text.append(NL2);
			text.append("Urgency Level:\n\n");
			text.append(sbSafetySeverity);
			text.append(NL2);
			text.append("Date issue encountered: ");
			text.append(NL2);
			text.append(TAB);
			text.append(reportDate);
			text.append(NL2);
		}
		else {
			subject = "Unknown Report: " + reportDate;
		}

		if (imageHasLatLng) {
			appendLocation("Report Location", reportLat / 1E6, reportLng / 1E6);
			appendLocation("Image Location", imageLat / 1E6, imageLng / 1E6);
		}
		else {
			appendLocation("Report Location", reportLat / 1E6, reportLng / 1E6);
		}

		// Final note comment
		text.append("Additional Details: ");
		text.append(NL2);
		text.append(TAB);
		text.append(noteData.getNotedetails());
		text.append(NL2);

		// Generate URI to image file
		if (hasImage){
			File inFile = new File(imageFileName);
			File outFile = null;
	        try {
	            outFile = new File(Environment.getExternalStorageDirectory() + File.separator + "image.jpg");
	            outFile.createNewFile();
	            copyFile(inFile, outFile);
		        imageUri = Uri.fromFile(outFile);
	        } catch (IOException ex) {
				Log.e(MODULE_TAG, ex.getMessage());
	        }
		}
	}

	private void appendLocation(String caption, double lat, double lng) {
		// Google maps link
		text.append(caption);
		text.append(":");
		text.append(NL2);
		text.append(TAB);
		//text.append("<a href=");
		text.append("http://maps.google.com/maps?q=");
		text.append(String.valueOf(lat));
		text.append(",");
		text.append(String.valueOf(lng));
		text.append("&ll=");
		text.append(String.valueOf(lat));
		text.append(",");
		text.append(String.valueOf(lng));
		text.append("&z=16");
		//text.append("/>");
		text.append(NL2);
	}


	/**
	 *
	 * @param src
	 * @param dst
	 * @throws IOException
	 */
	 void copyFile(File src, File dst) throws IOException {
		    FileChannel inChannel = null;
		    FileChannel outChannel = null;
		    try {
			    inChannel = new FileInputStream(src).getChannel();
			    outChannel = new FileOutputStream(dst).getChannel();
		        inChannel.transferTo(0, inChannel.size(), outChannel);
		    } finally {
		        if (inChannel != null)
		            inChannel.close();
		        if (outChannel != null)
		            outChannel.close();
		    }
		}

	 public Uri getAttachment() {
		return imageUri;
	}

	public String getSubject() {
		return subject;
	}

	public String getText() {

		//String s = Html.toHtml(Html.fromHtml(text.toString()));

		return text.toString();
	}

	private void getNoteResponses(long noteId) {

		DbAdapter mDb = new DbAdapter(context);

		mDb.openReadOnly();

		try {
			Cursor answers = mDb.fetchNoteAnswers(noteId);

			int questionCol = answers.getColumnIndex(DbAdapter.K_NOTE_ANSWER_QUESTION_ID);
			int answerCol = answers.getColumnIndex(DbAdapter.K_NOTE_ANSWER_ANSWER_ID);
			int otherCol = answers.getColumnIndex(DbAdapter.K_NOTE_ANSWER_OTHER_TEXT);

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
						append(sbSubjectLineUrgency, R.array.email_subject_line_urgency, DbAnswers.safetyUrgency, answerId);
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
			sb.append(NL);
		sb.append(TAB);
		sb.append(DbAnswers.getAnswerText(context, textArrayId, answers, answerId));
	}

	private void append(StringBuilder sb, int textArrayId, int[] answers, int answerId, String otherText) {
		if ((null == otherText) || otherText.equals("")) {
			if (sb.length() > 0)
				sb.append(NL);
			sb.append(TAB);
			sb.append(DbAnswers.getAnswerText(context, textArrayId, answers, answerId));
		}
		else {
			if (sb.length() > 0)
				sb.append(NL);
			sb.append(TAB);
			sb.append("Other(");
			sb.append(otherText);
			sb.append(")");
		}
	}
}
