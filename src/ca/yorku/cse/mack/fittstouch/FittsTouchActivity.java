package ca.yorku.cse.mack.fittstouch;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.StringTokenizer;

import com.qualcomm.snapdragon.sdk.sample.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.PointF;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * <h1>FittsTouch</h1>
 * 
 * Summary:
 * <p>
 * 
 * <ul>
 * <li>
 * An Android application for evaluating touch-based target selection using Fitts' law.
 * <p>
 * 
 * <li>Implements both the two-dimensional (2D) and one-dimensional (1D) Fitts' law tasks described
 * in ISO 9241-9 (updated in 2012 as ISO/TC 9241-411).
 * <p>
 * 
 * <li>User performance data gathered and saved in output files for follow-up analyses.
 * <p>
 * </ul>
 * 
 * <h3>Related references</h3>
 * 
 * The following publication presents research where this software was used.
 * <p>
 * 
 * <ul>
 * <li>Fitts' throughput and the remarkable case of touch-based target selection, by MacKenzie (in
 * press)
 * </ul>
 * 
 * The following publications provide background information on Fitts' law and experimental testing
 * using the Fitts' paradigm.
 * <p>
 * 
 * <ul>
 * <li><a href="http://www.yorku.ca/mack/ijhcs2004.pdf">Towards a standard for pointing device
 * evaluation: Perspectives on 27 years of Fitts' law research in HCI</a>, by Soukoreff and
 * MacKenzie (<i>IJHCS 2004</i>).
 * <p>
 * 
 * <li><a href="http://www.yorku.ca/mack/HCI.html">Fitts' law as a research and design tool in
 * human-computer interaction</a>, by MacKenzie (<i>HCI 1992</i>).
 * <p>
 * </ul>
 * <p>
 * 
 * <h3>Setup parameters</h3>
 * 
 * Upon launching, the program presents a setup dialog:
 * <p>
 * <center><a href="FittsTouch-1.jpg"><img src="FittsTouch-1.jpg" width="200"></a></center>
 * <p>
 * </center>
 * 
 * The parameters are embedded in the application. The default setting (shown) may be changed by
 * selecting the corresponding spinner. Changes may be saved. Saved changes become the default
 * settings when the application is next launched.
 * <p>
 * 
 * The setup parameters are as follows:
 * <p>
 * 
 * <blockquote>
 * <table border="1" cellspacing="0" cellpadding="6">
 * <tr bgcolor="#cccccc">
 * <th>Parameter
 * <th>Description
 * 
 * <tr>
 * <td valign="top">Participant code
 * <td>Identifies the current participant.
 * <p>
 * 
 * <tr>
 * <td valign="top">Session code
 * <td>Identifies the session. This code is useful if testing proceeds over multiple sessions to
 * gauge the progression of learning.
 * <p>
 * 
 * <tr>
 * <td valign="top">Block code (auto)
 * <td>Identifies the block of testing. This code is generated automatically. The first block of
 * testing is "B01", then "B02", and so on. Output data files include the block code in the
 * filename. The first available block code is used in opening data files for output. This prevents
 * overwriting data from an earlier block of testing.
 * <p>
 * 
 * <tr>
 * <td valign="top">Group code
 * <td>Identifies the group to which the participant was assigned. This code is needed if
 * counterbalancing was used (i.e., participants were assigned to groups to offset order effects).
 * This is common practice for testing the levels of a within-subjects independent variable.
 * <p>
 * 
 * <tr>
 * <td valign="top">Condition code
 * <td>An arbitrary code to associate a test condition with a block of trials. This parameter might
 * be useful if the user study includes conditions that are not inherently part of the application
 * (e.g., Gender &rarr; male, female; User stance &rarr; sitting, standing, walking).
 * <p>
 * 
 * <tr>
 * <td valign="top">Mode
 * <td>Set to either "1D" or "2D" to control whether the task is one-dimensional or two-dimensional.
 * <p>
 * 
 * <tr>
 * <td colspan=2 valign=center>
 * NOTE: The setup parameters above appear in the filename for the output data files (e.g.,
 * <code>FittsTouch-P01-S01-B01-G01-C01-1D.sd1</code>). They also appear as data columns in the
 * output data files.
 * 
 * <tr>
 * <td valign="top">Number of trials (1D)
 * <td>Specifies the number of back-and-forth selections in a block of trials. This setup parameter
 * is only relevant if Mode = 1D.
 * <p>
 * 
 * <tr>
 * <td valign="top">Number of targets (2D)
 * <td>Specifies the number of targets that appear in the layout circle. This setup parameter is
 * only relevant if Mode = 2D.
 * <p>
 * 
 * <tr>
 * <td valign="top">Target amplitude (A)
 * <td>Specifies either the diameter of the layout circle (2D) or the center-to-center distance
 * between targets (1D). The spinner offers three choices: "120, 240, 480", "250, 500", or "500"
 * (but see note 2 below).
 * <p>
 * 
 * <tr>
 * <td valign="top">Target width (W)
 * <td>Specifies the width of targets. This is either the diameter of the target circles (2D) or the
 * width of the rectangles (1D). The spinner offers three choices: "60, 100", "30, 60, 120", or
 * "100",
 * <p>
 * 
 * Notes:<br>
 * 1. The total number of <i>A-W</i> conditions (sequences) in a block is <i>n &times; m</i>, where
 * <i>n</i> is the number of target amplitudes and <i>m</i> is the number of target widths.<br>
 * 2. The <i>A-W</i> values are scaled such that the widest condition (largest A, largest W) spans
 * the device's display with minus 10 pixels on each side.
 * <p>
 * 
 * <tr>
 * <td valign="top">Vibrotactile feedback
 * <td>A checkbox parameter. If checked, a 10 ms vibrotactile pulse is emitted if a target is
 * selected in error (i.e., the finger tap is outside the target).
 * <p>
 * 
 * <tr>
 * <td valign="top">Audio feedback
 * <td>A checkbox parameter. If checked, an auditory beep is heard if a target is selected in error
 * (i.e., the finger tap is outside the target).
 * <p>
 * </table>
 * </blockquote>
 * 
 * <h3>Application screen snaps</h3>
 * 
 * Once the setup parameters are chosen, the testing begins by tapping OK. The first screen to
 * appear is a transition screen to ensure the participant is ready (below, left). Once the blue
 * circle is tapped, the first test condition appears. Example are shown below for the 2D task
 * (below, center) and the 1D task (below, right).
 * <p>
 * 
 * <center> <a href="FittsTouch-2.jpg"><img src="FittsTouch-2.jpg"
 * height=300></a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <a href="FittsTouch-3.jpg"><img
 * src="FittsTouch-3.jpg" height=300></a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <a
 * href="FittsTouch-4.jpg"><img src="FittsTouch-4.jpg" height=300></a> </center>
 * <p>
 * 
 * The participant selects targets by tapping with a finger. The target to tap is highlighted. Upon
 * selection, the highlight moves to a target on the opposite side of the layout circle (2D) or to
 * the opposing target (1D). Tapping continues until all targets are selected (2D) or until the
 * specified number of trials is completed (1D).
 * <p>
 * 
 * Errors are permitted. If the user attempts to tap a target and the selection coordinate on
 * finger-lift is outside the target, an error occurs. Vibrotactile or auditory feedback is emitted
 * if the corresponding setup parameter was checked. Whether the target was hit or missed, the
 * highlight moves to the next target.
 * <p>
 * 
 * A series of trials for a single <i>A-W</i> condition is called a "sequence". At the end of each
 * sequence, results appear on the display. See below:
 * <p>
 * 
 * <center><a href="FittsTouch-5.jpg"><img src="FittsTouch-5.jpg" height="400"></a></center>
 * <p>
 * 
 * Once all the <i>A-W</i> conditions in a block are finished, the application terminates. User
 * performance data are saved files for follow-up analyses. The data files are located in the
 * device's public storage directory in a folder named <code>FittsTouchData</code>.
 * <p>
 * 
 * 
 * <h3>Output data files</h3>
 * 
 * For each block of testing, two output data files are created: sd1 and sd2. ("sd" is for
 * "summary data".) The data are comma delimited for easy importing into a spreadsheet or statistics
 * program.
 * <p>
 * 
 * <h4>sd1 output file</h4>
 * 
 * The sd1 file contains the following summary data for each trial:
 * <p>
 * 
 * <pre>
 *      Participant - participant code
 *      Session - session code
 *      Block - block code
 *      Group - group code
 *      Condition - condition code
 *      Mode - mode code (1D or 2D)
 *      Trial - trial number
 *      A - target amplitude
 *      W - target width
 *      FromX - x coordinate of center of from-target
 *      FromY - y coordinate of center of from-target
 *      TargetX - x coordinate of center of to-target
 *      TargetY - y coordinate of center of to-target
 *      FingerDownX - x coordinate of finger-down event at end of trial
 *      FingerDownY - y coordinate of finger-down event at end of trial
 *      SelectX - x coordinate of finger-up event at end of trial (target selection)
 *      SelectY - y coordinate of finger-up event at end of trial (target selection)
 *      xDelta - (see below)
 *      FingerDownUpDelta - Pythagorean distance between finger-down and finger-up events
 *      FingerDownUpTime - time in ms between finger-down and finger-up events
 *      DistanceFromTargetCenter - Pythagorean distance from selection coordinate to target center
 *      Miss - 0 = target selected, 1 = target missed
 *      MT - movement time in ms for the trial
 * </pre>
 * 
 * Note: All sizes, distances, and coordinates are in pixel units for the test device.
 * <p>
 * 
 * <code>xDelta</code> is the <i>x</i>-distance from the selection coordinate to the center of the
 * target. It is normalized relative to the center of the target and to the task axis. For example,
 * <code>xDelta</code> = 1 is the equivalent of a one-pixel overshoot while <code>xDelta</code> =
 * &minus;1 is the equivalent of a one-pixel undershoot. Note that <code>xDelta</code> = 0 does not
 * mean selection was precisely at the centre of the target. It means the selection was on a line
 * orthogonal to the task axis going through the centre of the target. This is consistent with the
 * inherently one-dimensional nature of Fitts' law.
 * <p>
 * 
 * <code>xDelta</code> is important for calculating Fitts' throughput. The standard deviation in the
 * <code>xDelta</code> values collected over a sequence of trials is <i>SD</i><sub>x</sub>. This is
 * used in the calculation of throughput (<i>TP</i>) as follows:
 * <p>
 * 
 * <blockquote> <i>W</i><sub>e</sub> = 4.133 &times; <i>SD</i><sub>x</sub>
 * <p>
 * 
 * <i>ID</i><sub>e</sub> = log<sub>2</sub>(<i>A</i><sub>e</sub> / <i>W</i><sub>e</sub> + 1)
 * <p>
 * 
 * <i>TP</i> = <i>ID</i><sub>e</sub> / <i>MT</i>
 * <p>
 * </blockquote>
 * 
 * <h4>sd2 output file</h4>
 * 
 * The sd2 file contains summary data for a sequence of trials:
 * 
 * <pre>
 *      Participant - participant code
 *      Session - session code
 *      Block - block code
 *      Group - group code
 *      Condition - condition code
 *      Mode - mode code (1D or 2D)
 *      Trials - number of trials in the sequence
 *      SequenceRepeatCount - number of times the sequence was repeated (see below)
 *      A - specified target amplitude
 *      W - specified target width
 *      ID - specified index of difficulty
 *      Ae - actual or effective movement amplitude
 *      We - actual or effective target width
 *      IDe - actual or effective index of difficulty
 *      MT - mean movement time in ms over all trials in the sequence
 *      ErrorRate - error rate (%)
 *      TP - Fitts' throughput in bits per second
 * </pre>
 * 
 * 
 * The <code>SequenceRepeatCount</code> is used in conjunction with an outlier criterion. Any trial
 * where the actual distance traversed is less the &frac12; the specified amplitude is deemed an
 * outlier. The most likely cause of an outlier is either an inadvertent double-tap or a missed tap.
 * A sequence with 1 or more outliers is an outlier sequence and is repeated. If this occurs, an
 * alert popup appears at the end of the sequence to inform the participant. No data are saved for
 * an outlier sequence. However, the <code>SequenceRepeatCount</code> entry in the sd2 file
 * indicates the number of times the sequence was repeated due to the outlier criterion. Usually,
 * <code>SequenceRepeatCount</code> = 0 (hopefully!).
 * <p>
 * 
 * The last six entries in the sd2 file are user performance measures. These reflect how the user
 * actually performed while doing the sequence of trials. The measures most commonly used as
 * dependent variables in Fitts' law experiments are the last three: movement time, error rate, and
 * throughput.
 * <p>
 * 
 * The following are examples of "sd" (summary data) files:
 * <p>
 * 
 * <ul>
 * <li><a href="FittsTouch-sd1-example.txt">sd1 example</a>
 * <li><a href="FittsTouch-sd2-example.txt">sd2 example</a>
 * </ul>
 * 
 * Actual output files use "FittsTouch" as the base filename. This is followed by the participant
 * code, the session code, the block code, the group code, the condition code, and the mode, for
 * example, <code>FittsTouch-P01-S01-B01-G01-C01-1D.sd1</code>.
 * <p>
 * 
 * In most cases, the sd2 data files are the primary files used for data analyses in an experimental
 * evaluation. The data in the sd2 files are full-precision, comma-delimited, to facilitate
 * importing into a spreadsheet or statistics application. Below is an example for the sd2 file
 * above, after importing into Microsoft <i>Excel</i>: (click to enlarge)
 * <p>
 * 
 * <center> <a href="FittsTouch-6.jpg"><img src="FittsTouch-6.jpg" width=1000></a> </center>
 * <p>
 * 
 * When using this application in an experiment, it is a good idea to terminate all other
 * applications and to disable the system's WiFi and Bluetooth transceivers. This will maintain the
 * integrity of the data collected and ensure that the application runs without hesitations.
 * <p>
 * 
 * @author Scott MacKenzie, 2013-2015
 */

@SuppressLint({ "ClickableViewAccessibility", "DefaultLocale" })
public class FittsTouchActivity extends Activity implements View.OnTouchListener
{
	final String WORKING_DIRECTORY = "/FittsTouchData/";
	final String SD1_HEADER = "Participant,Session,Block,Group,Condition,Mode,"
			+ "Trial,A,W,FromX,FromY,TargetX,TargetY,FingerDownX,FingerDownY,SelectX,SelectY,xDelta,FingerDownUpDelta,"
			+ "FingerDownUpTime(ms),DistanceFromTargetCenter,Miss,MT(ms)\n";
	final String SD2_HEADER = "Participant,Session,Block,Group,Condition,Mode,"
			+ "Trials,SequenceRepeatCount,A,W,ID,Ae,We,IDe,MT(ms),ErrorRate(%),TP(bps)\n";
	final float TWO_TIMES_PI = 6.283185307f;
	final float LOG_TWO = 0.693147181f;
	final int VIBRATION_PULSE_DURATION = 10;

	ExperimentPanel ep;
	String participantCode, sessionCode, blockCode, groupCode, conditionCode;
	String dimensionMode;
	boolean vibrotactileFeedback, auditoryFeedback;
	int numberOfTrials, numberOfTargets, outlierSequenceCount;
	float[] amplitude, width;
	BufferedWriter sd1, sd2;
	File f1, f2;
	String s1, s2, s3;
	int screenOrientation;

	AmplitudeWidth[] aw; // task conditions (A-W pairs)
	float xCenter, yCenter, screenHeight;
	float xFingerDown, yFingerDown;
	long fingerDownTime, trialStartTime, now, sequenceStartTime;
	boolean even, sequenceStarted, waitTargetSelected, outlier;
	int awIdx, selectionCount, trialMiss;
	Vibrator vib;
	MediaPlayer missSound;
	StringBuilder sb1, sb2, results;

	// new stuff to streamline calculation of Throughput
	PointF[] from;
	PointF[] to;
	PointF[] select;
	float[] mtArray;

	// -----

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// init study parameters
		Bundle b = getIntent().getExtras();
		participantCode = b.getString("participantCode");
		sessionCode = b.getString("sessionCode");
		blockCode = "B01"; // always start here
		groupCode = b.getString("groupCode");
		conditionCode = b.getString("conditionCode");
		dimensionMode = b.getString("mode");
		numberOfTrials = b.getInt("numberOfTrials");
		numberOfTargets = b.getInt("numberOfTargets");
		amplitude = getValues(b.getString("amplitude"));
		width = getValues(b.getString("width"));
		vibrotactileFeedback = b.getBoolean("vibrotactileFeedback");
		auditoryFeedback = b.getBoolean("auditoryFeedback");
		screenOrientation = b.getInt("screenOrientation");

		// request the device to operate in its default orientation
		this.setRequestedOrientation(screenOrientation);

		// ===================
		// File initialization
		// ===================

		// make a working directory to store data files
		File dataDirectory = new File(Environment.getExternalStorageDirectory() + WORKING_DIRECTORY);
		if (!dataDirectory.exists() && !dataDirectory.mkdirs())
		{
			Log.i("MYDEBUG", "Failed to create directory: " + WORKING_DIRECTORY);
			super.onDestroy(); // cleanup
			this.finish(); // terminate
		}

		// base filename for output data files (find first available block code and use in filename)
		int blockNumber = 1;
		blockCode = "B01";
		String base = "FittsTouch-" + participantCode + "-" + sessionCode + "-" + blockCode + "-" + groupCode + "-"
				+ conditionCode + "-" + dimensionMode;

		f1 = new File(dataDirectory, base + ".sd1");
		f2 = new File(dataDirectory, base + ".sd2");

		// if file exists, increment the block number and try again
		while (f1.exists() || f2.exists())
		{
			++blockNumber;
			blockCode = blockNumber < 10 ? "B0" + blockNumber : "B" + blockNumber;
			base = "FittsTouch-" + participantCode + "-" + sessionCode + "-" + blockCode + "-" + groupCode + "-"
					+ conditionCode + "-" + dimensionMode;

			f1 = new File(dataDirectory, base + ".sd1");
			f2 = new File(dataDirectory, base + ".sd2");
		}

		try
		{
			sd1 = new BufferedWriter(new FileWriter(f1));
			sd2 = new BufferedWriter(new FileWriter(f2));

			// output header lines in data files
			sd1.write(SD1_HEADER, 0, SD1_HEADER.length());
			sd1.flush();
			sd2.write(SD2_HEADER, 0, SD2_HEADER.length());
			sd2.flush();
		} catch (IOException e)
		{
			Log.i("MYDEBUG", "Error opening data files! Exception: " + e.toString());
			super.onDestroy();
			this.finish();
		}

		ep = (ExperimentPanel)findViewById(R.id.experimentpanel);
		ep.setOnTouchListener(this);
		ep.waitStartCircleSelect = true;

		// determine screen width and height
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int screenWidth = dm.widthPixels;
		screenHeight = dm.heightPixels;
		xCenter = screenWidth / 2f;
		yCenter = screenHeight / 2f;

		ep.panelWidth = screenWidth;
		ep.panelHeight = screenHeight;
		ep.mode = dimensionMode;
		ep.waitStartCircleSelect = true;

		/*
		 * Scale target amplitudes and widths as appropriate for display size. The smaller of w or h
		 * constrains allowable space.
		 */
		float span = screenWidth < screenHeight ? screenWidth : screenHeight;

		// find largest amplitude
		float largestAmplitude = 0;
		for (int i = 0; i < amplitude.length; ++i)
			if (amplitude[i] > largestAmplitude)
				largestAmplitude = amplitude[i];

		// find widest target
		float largestWidth = 0;
		for (int i = 0; i < width.length; ++i)
			if (width[i] > largestWidth)
				largestWidth = width[i];

		// condition with largest amplitude and widest target will span the available display width
		// (minus 10 pixels)
		float scaleFactor = span / (largestAmplitude + largestWidth + 10f);

		// scale amplitudes
		for (int i = 0; i < amplitude.length; ++i)
			amplitude[i] *= scaleFactor;

		// scale widths
		for (int i = 0; i < width.length; ++i)
			width[i] *= scaleFactor;

		vib = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
		missSound = MediaPlayer.create(this, R.raw.miss);

		// tweaks needed to accommodate 1D vs. 2D modes
		if (dimensionMode.equals("1D"))
			numberOfTargets = 2; // reciprocal tapping
		if (dimensionMode.equals("2D"))
			numberOfTrials = numberOfTargets;

		// arrays needed for Throughput calculation (done on a per-sequence basis)
		from = new PointF[numberOfTrials];
		to = new PointF[numberOfTrials];
		select = new PointF[numberOfTrials];
		mtArray = new float[numberOfTrials];

		aw = getAmplitudeWidthArray(amplitude, width);
		awIdx = 0;
		waitTargetSelected = true;
	}

	// convert the amplitude/width string in the spinners to float array
	private float[] getValues(String valuesArg)
	{
		StringTokenizer st = new StringTokenizer(valuesArg, ", ");
		int i = 0;
		float[] values = new float[st.countTokens()];
		while (st.hasMoreTokens())
			values[i++] = Float.parseFloat(st.nextToken());
		return values;
	}

	@Override
	public boolean onTouch(View v, MotionEvent me)
	{
		float x = me.getX();
		float y = me.getY();

		if (me.getAction() == MotionEvent.ACTION_DOWN)
		{
			doFingerDown(x, y);
			return true;
		} else if (me.getAction() == MotionEvent.ACTION_UP)
		{
			if (ep.waitStartCircleSelect)
			{
				if (!ep.startCircle.inTarget(x, y))
					return true;
				else
					doStartCircleSelected();
			} else
				doTargetSelected(x, y);
		}
		return true;
	}

	private void configureTargets(int awIdx)
	{
		for (int i = 0; i < numberOfTargets; ++i)
		{
			float x = xCenter + (float)(aw[awIdx].a / 2f)
					* (float)Math.cos(TWO_TIMES_PI * ((float)i / numberOfTargets));
			float y = yCenter + (float)(aw[awIdx].a / 2f)
					* (float)Math.sin(TWO_TIMES_PI * ((float)i / numberOfTargets));
			if (ep.mode.equals("1D"))
				ep.targetSet[i] = new Target(Target.RECTANGLE, x, y, (float)aw[awIdx].w, screenHeight - 40f,
						Target.NORMAL);
			else
				ep.targetSet[i] = new Target(Target.CIRCLE, x, y, (float)aw[awIdx].w, (float)aw[awIdx].w, Target.NORMAL);
		}
		// Don't set target yet. This is done when start circle is selected.
	}

	private AmplitudeWidth[] getAmplitudeWidthArray(float[] aArray, float[] wArray)
	{
		AmplitudeWidth[] aw = new AmplitudeWidth[aArray.length * wArray.length];
		for (int i = 0; i < aw.length; ++i)
			aw[i] = new AmplitudeWidth(aArray[i / wArray.length], wArray[i % wArray.length]);

		// shuffle
		Random r = new Random();
		for (int i = 0; i < aw.length; ++i)
		{
			int idx = r.nextInt(aw.length);
			AmplitudeWidth temp = aw[idx];
			aw[idx] = aw[i];
			aw[i] = temp;
		}
		return aw;
	}

	public void doFingerDown(float xArg, float yArg)
	{
		xFingerDown = xArg;
		yFingerDown = yArg;
		fingerDownTime = System.nanoTime();
	}

	public void doStartCircleSelected()
	{
		if (ep.done) // start circle displayed after last sequence, select to finish
			doEndBlock();

		ep.waitStartCircleSelect = false;
		even = true;
		if (awIdx < aw.length)
		{
			ep.targetSet = new Target[numberOfTargets];
			configureTargets(awIdx);
		}

		ep.targetSet[0].status = Target.TARGET;
		ep.toTarget = ep.targetSet[0];
		selectionCount = 0;
	}

	// Done! close data files and exit
	private void doEndBlock()
	{
		try
		{
			sd1.close();
			sd2.close();

			// Make the saved data files visible in Windows Explorer
			// There seems to be bug doing this with Android 4.4. I'm using the following
			// code, instead of sendBroadcast. See...
			// http://code.google.com/p/android/issues/detail?id=38282
			MediaScannerConnection.scanFile(this, new String[] { f1.getAbsolutePath(), f2.getAbsolutePath() }, null,
					null);
		} catch (IOException e)
		{
			Log.d("MYDEBUG", "FILE CLOSE ERROR! e = " + e);
		}
		this.finish();
	}

	void doTargetSelected(float xSelect, float ySelect)
	{
		trialStartTime = now; // last "now" value is start of trial
		now = System.nanoTime(); // current "now" value is end of trial

		// hit or miss? (respond appropriately)
		trialMiss = ep.toTarget.inTarget(xSelect, ySelect) ? 0 : 1;
		if (trialMiss == 1)
		{
			// provide feedback (as per setup) but only if the user misses the target
			if (vibrotactileFeedback)
				vib.vibrate(VIBRATION_PULSE_DURATION);
			if (auditoryFeedback)
				missSound.start();
		}

		if (!sequenceStarted) // 1st target selection (beginning of sequence)
		{
			sequenceStarted = true;
			sequenceStartTime = now;
			advanceTarget();
			ep.fromTarget = ep.targetSet[0];
			sb1 = new StringBuilder();
			sb2 = new StringBuilder();
			results = new StringBuilder();
			return;
		}

		/*
		 * CALCULATION OF XDELTA: xDelta is the signed distance of the selection coordinate from the
		 * target center, as projected on the task axis. The projection onto the task axis is
		 * performed to respect the inherent one-dimensionality of Fitts' law. NOTE: xDelta is -ve
		 * if the selection is on the near side of the target center, +ve otherwise.
		 * 
		 * NOTE: Some of the calculations here are repeated in the Throughput class. They are also
		 * needed here to compute xDelta and ae at the end of each trial. Recall that the Throughput
		 * class is only given data at the end of a sequence.
		 */

		// a is the distance from the center of the "from" target to the center of the "to" target.
		float ax = (float)(ep.fromTarget.xCenter - ep.toTarget.xCenter);
		float ay = (float)(ep.fromTarget.yCenter - ep.toTarget.yCenter);
		float a = (float)Math.sqrt(ax * ax + ay * ay);

		// b is the distance from the selection coordinate to the center of the "to" target.
		float b = (float)Math.sqrt((xSelect - ep.toTarget.xCenter) * (xSelect - ep.toTarget.xCenter)
				+ (ySelect - ep.toTarget.yCenter) * (ySelect - ep.toTarget.yCenter));

		// c is the distance from the center of the "from" target to the selection coordinate.
		float c = (float)Math.sqrt((xSelect - ep.fromTarget.xCenter) * (xSelect - ep.fromTarget.xCenter)
				+ (ySelect - ep.fromTarget.yCenter) * (ySelect - ep.fromTarget.yCenter));

		// combine Pythagorean theorems to find xDelta
		float xDelta = -1.0f * (a * a + b * b - c * c) / (2 * a);

		/*
		 * ae is the "effective target amplitude" (the actual distance moved, as projected on the
		 * task axis). The calculation here isn't quite that same as in the Throughput class, where
		 * there is also an adjustment for the xDelta from the previous trial (for all trials after
		 * the first). The calculation here is good enough, since we are only using ae to determine
		 * if a trial is an outlier. The actual ae used in calculating throughput is calculated in
		 * the Throughput class.
		 */
		float ae = a + xDelta;

		/*
		 * DEFINITION OF OUTLIER: Any trial where the actual distance moved is less the 1/2 the
		 * specified amplitude is deemed an outlier. Any sequence with 1 or more outliers will be
		 * repeated. The
		 */

		if (ae < aw[awIdx].a / 2f)
			outlier = true;

		String trialTime = String.format("%.1f", (now - trialStartTime) / 1000000.0f);
		String fingerDownUpTime = String.format("%.1f", (now - fingerDownTime) / 1000000.0f);
		float fingerDownUpDelta = (float)Math.sqrt((xSelect - xFingerDown) * (xSelect - xFingerDown)
				+ (ySelect - yFingerDown) * (ySelect - yFingerDown));

		// ensure this is consistent with the sd1 header line defined at the top of
		// FittsTouchActivity
		sb1.append(participantCode + "," + sessionCode + "," + blockCode + "," + groupCode + "," + conditionCode + ","
				+ dimensionMode + "," + selectionCount + "," + aw[awIdx].a + "," + aw[awIdx].w + ","
				+ ep.fromTarget.xCenter + "," + ep.fromTarget.yCenter + "," + ep.toTarget.xCenter + ","
				+ ep.toTarget.yCenter + "," + xFingerDown + "," + yFingerDown + "," + xSelect + "," + ySelect + ","
				+ xDelta + "," + fingerDownUpDelta + "," + fingerDownUpTime + "," + b + "," + trialMiss + ","
				+ trialTime + "\n");

		/*
		 * These four arrays are added to at the end of each trial. At the end of a sequence, they
		 * are passed to the Throughput constructor. The Throughput object will compute throughput
		 * and other values based on the data in these arrays.
		 */
		from[selectionCount] = new PointF((float)ep.fromTarget.xCenter, (float)ep.fromTarget.yCenter);
		to[selectionCount] = new PointF((float)ep.toTarget.xCenter, (float)ep.toTarget.yCenter);
		select[selectionCount] = new PointF(xSelect, ySelect);
		mtArray[selectionCount] = (now - trialStartTime) / 1000000.0f;

		// prepare for next target selection
		++selectionCount;
		advanceTarget();

		if (selectionCount == numberOfTrials) // finished sequence
			doEndSequence();
	}

	void doEndSequence()
	{
		if (outlier)
		{
			++outlierSequenceCount;
			results.append("Oops! Outlier sequence!::Possible causes...:- missed tap:- double tap::Tap to try again");
			ep.resultsString = results.toString().split(":");
			outlier = false;
		} else
		{
			// give the relevant data to the Throughput object (where the serious calculations are
			// performed)
			int taskType = dimensionMode.equals("1D") ? Throughput.ONE_DIMENSIONAL : Throughput.TWO_DIMENSIONAL;
			int responseType = Throughput.SERIAL;
			Throughput t = new Throughput("nocode", aw[awIdx].a, aw[awIdx].w, taskType, responseType, from, to, select,
					mtArray);

			// CAUTION: ensure this is consistent with the sd2 header line defined at the top of
			// FittsTouchActivity
			sb2.append(participantCode + "," + sessionCode + "," + blockCode + "," + groupCode + "," + conditionCode
					+ "," + t.getTaskTypeString(taskType) + "," + t.getNumberOfTrials() + "," + outlierSequenceCount
					+ "," + t.getA() + "," + t.getW() + "," + t.getID() + "," + t.getAe() + "," + t.getWe() + ","
					+ t.getIDe() + "," + t.getMT() + "," + t.getErrorRate() + "," + t.getThroughput() + "\n");

			// write data to files at end of each sequence
			try
			{
				sd1.write(sb1.toString(), 0, sb1.length());
				sd1.flush();
				sd2.write(sb2.toString(), 0, sb2.length());
				sd2.flush();
			} catch (IOException e)
			{
				Log.d("MYDEBUG", "ERROR WRITING TO DATA FILES: e = " + e);
			}
			sb1.delete(0, sb1.length());
			sb2.delete(0, sb2.length());

			// prepare results for output on display
			results.append("Block " + Integer.parseInt(blockCode.substring(1)) + ":");
			results.append("Sequence " + (awIdx + 1) + " of " + aw.length + ":");
			results.append("Number of trials = " + t.getNumberOfTrials() + ":");
			results.append("A = " + Math.round(t.getA()) + " px (nominal):");
			results.append("W = " + Math.round(t.getW()) + " px:");
			results.append(String.format("ID = %.2f bits:", t.getID()));
			results.append("-----:");
			results.append(String.format("Ae = %.1f px:", t.getAe()));
			results.append(String.format("We = %.1f px:", t.getWe()));
			results.append(String.format("IDe = %.2f bits:", t.getIDe()));
			results.append("MT = " + Math.round(t.getMT()) + " ms (per trial):");
			results.append("Misses = " + t.getMisses() + ":");
			results.append(String.format("Throughput = %.2f bps:", t.getThroughput()));
			ep.resultsString = results.toString().split(":");

			++awIdx; // next A-W condition
			if (awIdx < aw.length)
				configureTargets(awIdx);
			else
				ep.done = true;
			outlierSequenceCount = 0;
		}
		ep.waitStartCircleSelect = true;
		sequenceStarted = false;
	}

	// advance to the next target (a bit complicated for the 2D task; see comment below)
	private void advanceTarget()
	{
		/*
		 * Find the current "target" then advance it to the circle on the opposite side of the
		 * layout circle. This is a bit tricky since every second advance requires the a target to
		 * be beside the target directly opposite the last target. This is needed to get the
		 * sequence of selections to advance around the layout circle. Of course, this only applies
		 * to the 2D task.
		 */
		int i;
		for (i = 0; i < ep.targetSet.length; ++i)
			if (ep.targetSet[i].status == Target.TARGET)
				break; // i is index of current target
		ep.targetSet[i].status = Target.NORMAL;
		int next;
		if (dimensionMode.equals("1D"))
		{
			next = (i + 1) % 2;
		} else
		{
			int halfWay = ep.targetSet.length / 2;
			next = even ? (i + halfWay) % ep.targetSet.length : (i + halfWay + 1) % ep.targetSet.length; // ouch!
		}
		ep.targetSet[next].status = Target.TARGET;
		ep.fromTarget = ep.targetSet[i];
		ep.toTarget = ep.targetSet[next];
		even = !even;
	}

	// simple class to hold the amplitude and width for a Fitts' law task
	private class AmplitudeWidth
	{
		float a, w;

		AmplitudeWidth(float aArg, float wArg)
		{
			a = aArg;
			w = wArg;
		}
	}
}