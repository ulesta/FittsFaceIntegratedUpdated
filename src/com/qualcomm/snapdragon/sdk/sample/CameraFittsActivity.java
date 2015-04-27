/*
 * ======================================================================
 * Copyright � 2014 Qualcomm Technologies, Inc. All Rights Reserved.
 * QTI Proprietary and Confidential.
 * =====================================================================
 * @file: CameraPreviewActivity.java
 */

package com.qualcomm.snapdragon.sdk.sample;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Random;
import java.util.StringTokenizer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff.Mode;
import android.hardware.Camera;
import android.hardware.Camera.Face;
import android.hardware.Camera.FaceDetectionListener;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import ca.yorku.cse.mack.fittstouch.ExperimentPanel;
import ca.yorku.cse.mack.fittstouch.Target;
import ca.yorku.cse.mack.fittstouch.Throughput;

import com.qualcomm.snapdragon.sdk.face.FaceData;
import com.qualcomm.snapdragon.sdk.face.FacialProcessing;
import com.qualcomm.snapdragon.sdk.face.FacialProcessing.FP_MODES;
import com.qualcomm.snapdragon.sdk.face.FacialProcessing.PREVIEW_ROTATION_ANGLE;

@SuppressLint("NewApi")
public class CameraFittsActivity extends Activity implements
		Camera.PreviewCallback, View.OnTouchListener {

	/**
	 * ========================== FITTS FIELDS ==========================
	 */
	final String WORKING_DIRECTORY = "/FittsFaceData/";
	final String SD1_HEADER = "Participant,Session,Block,Group,Condition,Mode,Navigation,Selection,"
			+ "Trial,A,W,FromX,FromY,TargetX,TargetY,FingerDownX,FingerDownY,SelectX,SelectY,xDelta,FingerDownUpDelta,"
			+ "FingerDownUpTime(ms),DistanceFromTargetCenter,Miss,MT(ms)\n";
	final String SD2_HEADER = "Participant,Session,Block,Group,Condition,Mode,Navigation,Selection,"
			+ "Trials,SequenceRepeatCount,A,W,ID,Ae,We,IDe,MT(ms),ErrorRate(%),TP(bps),Re-entries,Timeouts,Tracking drop\n";
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

	String navigationType, selectionType;

	AmplitudeWidth[] aw; // task conditions (A-W pairs)
	float xCenter, yCenter, screenHeight;
	float xFingerDown, yFingerDown;
	long fingerDownTime, trialStartTime, now, sequenceStartTime;
	boolean even, sequenceStarted, waitTargetSelected, outlier;
	int awIdx, selectionCount, trialMiss;
	Vibrator vib;
	MediaPlayer missSound, selectSound;
	StringBuilder sb1, sb2, results;

	double runningEstimatedX;
	double runningEstimatedY;
	double runningX;
	double runningY;
	
	// new stuff to streamline calculation of Throughput
	PointF[] from;
	PointF[] to;
	PointF[] select;
	float[] mtArray;

	/**
	 * ========================== FACIAL TRACKER FIELDS
	 * ==========================
	 */

	// Global Variables Required

	boolean drawViewAdded;

	Camera cameraObj;
	FrameLayout preview;
	FacialProcessing faceProc;
	FaceData[] faceArray = null;// Array in which all the face data values will
								// be returned for each face detected.
	View myView;
	Canvas canvas = new Canvas();
	Paint rectBrush = new Paint();
	private CameraSurfacePreview mPreview;
	private DrawView drawView;
	private GameDrawView gameDrawView;
	private final int FRONT_CAMERA_INDEX = 1;
	private final int BACK_CAMERA_INDEX = 0;

	// boolean clicked = false;
	boolean fpFeatureSupported = false;
	boolean cameraPause = false; // Boolean to check if the "pause" button is
									// pressed or no.
	static boolean cameraSwitch = false; // Boolean to check if the camera is
											// switched to back camera or no.
	boolean info = false; // Boolean to check if the face data info is displayed
							// or no.
	boolean landScapeMode = false; // Boolean to check if the phone orientation
									// is in landscape mode or portrait mode.

	int cameraIndex;// Integer to keep track of which camera is open.
	int smileValue = 0;
	int leftEyeBlink = 0;
	int rightEyeBlink = 0;
	int faceRollValue = 0;
	int pitch = 0;
	int yaw = 0;
	int horizontalGaze = 0;
	int verticalGaze = 0;
	PointF gazePointValue = null;
	private final String TAG = "CameraPreviewActivity";

	private double[] runningArrayX;
	private double[] runningArrayY;
	private int runningCounter = 0;

	// Constants
	private int RUNNING_LENGTH = 5; // Num of running avg elements
	private int BLINK_THRESHOLD = 55; // Blink values that go over this
										// threshold will be considered
	private int SMILE_THRESHOLD = 65; // Smile values that go over this
										// threshold will be considered
	float ALPHA = 0.05f; // Used for lowPassFilter

	// TextView Variables
	TextView numFaceText, smileValueText, leftBlinkText, rightBlinkText,
			gazePointText, faceRollText, faceYawText, facePitchText,
			horizontalGazeText, verticalGazeText;

	int surfaceWidth = 0;
	int surfaceHeight = 0;

	OrientationEventListener orientationEventListener;
	int deviceOrientation;
	int presentOrientation;
	float rounded;
	Display display;
	int displayAngle;

	float yawMapped = 0;
	float pitchMapped = 0;

	// Timers as flags
	float selectActionCooldown;
	float elapsedActionTime;
	float oldActionTime;
	float BLINK_TIME_THRESHOLD = 550;

	float DWELL_COOLDOWN = 500;
	float DWELL_TIME_THRESHOLD = 2500;
	float DWELL_TIMEOUT = 60000;
	boolean dwelling;
	int countReentry;
	float timeOutTimer;
	boolean timerActive;
	int countTimeout;

	boolean trackingFace;
	int countTrackLost;

	int SCALE_UNIT_CIRCLE_FOR_ROTATION = 1500;

	boolean debug;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fitts_camera);
		myView = new View(CameraFittsActivity.this);
		// Create our Preview view and set it as the content of our activity.
		preview = (FrameLayout) findViewById(R.id.camera_preview);
		numFaceText = (TextView) findViewById(R.id.numFaces);
		smileValueText = (TextView) findViewById(R.id.smileValue);
		rightBlinkText = (TextView) findViewById(R.id.rightEyeBlink);
		leftBlinkText = (TextView) findViewById(R.id.leftEyeBlink);
		faceRollText = (TextView) findViewById(R.id.faceRoll);
		gazePointText = (TextView) findViewById(R.id.gazePoint);
		faceYawText = (TextView) findViewById(R.id.faceYawValue);
		facePitchText = (TextView) findViewById(R.id.facePitchValue);
		horizontalGazeText = (TextView) findViewById(R.id.horizontalGazeAngle);
		verticalGazeText = (TextView) findViewById(R.id.verticalGazeAngle);

		// Check to see if the FacialProc feature is supported in the device or
		// no.
		fpFeatureSupported = FacialProcessing
				.isFeatureSupported(FacialProcessing.FEATURE_LIST.FEATURE_FACIAL_PROCESSING);

		if (fpFeatureSupported && faceProc == null) {
			Log.e("TAG", "Feature is supported");
			faceProc = FacialProcessing.getInstance(); // Calling the Facial
														// Processing
														// Constructor.
			faceProc.setProcessingMode(FP_MODES.FP_MODE_VIDEO);
		} else {
			Log.e("TAG", "Feature is NOT supported");
			return;
		}

		cameraIndex = Camera.getNumberOfCameras() - 1;// Start with front Camera

		try {
			cameraObj = Camera.open(cameraIndex); // attempt to get a Camera
													// instance
		} catch (Exception e) {
			Log.d("TAG", "Camera Does Not exist");// Camera is not available (in
													// use or does not exist)
		}

		// Change the sizes according to phone's compatibility.
		mPreview = new CameraSurfacePreview(CameraFittsActivity.this,
				cameraObj, faceProc);
		preview.removeView(mPreview);
		preview = (FrameLayout) findViewById(R.id.camera_preview);
		preview.addView(mPreview);
		cameraObj.setPreviewCallback(CameraFittsActivity.this);

		// Action listener for the screen touch to display the face data info.
		touchScreenListener();

		// Action listener for the Pause Button.
		pauseActionListener();

		// Action listener for the Switch Camera Button.
		cameraSwitchActionListener();

		orientationListener();

		display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
				.getDefaultDisplay();

		// initialize running array
		runningArrayX = new double[RUNNING_LENGTH];
		runningArrayY = new double[RUNNING_LENGTH];

		/**
		 * ================== Fitts' Init ==================
		 */

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
		debug = b.getBoolean("debug");
		Log.i("bundle", "debug=" + debug);
		screenOrientation = b.getInt("screenOrientation");

		navigationType = b.getString("navigationType");
		selectionType = b.getString("selectionType");

		// request the device to operate in its default orientation
		this.setRequestedOrientation(screenOrientation);

		// ===================
		// File initialization
		// ===================

		// make a working directory to store data files
		File dataDirectory = new File(Environment.getExternalStorageDirectory()
				+ WORKING_DIRECTORY);
		if (!dataDirectory.exists() && !dataDirectory.mkdirs()) {
			Log.i("MYDEBUG", "Failed to create directory: " + WORKING_DIRECTORY);
			super.onDestroy(); // cleanup
			this.finish(); // terminate
		}

		// base filename for output data files (find first available block code
		// and use in filename)
		int blockNumber = 1;
		blockCode = "B01";
		String base = "FittsTouch-" + participantCode + "-" + sessionCode + "-"
				+ blockCode + "-" + groupCode + "-" + conditionCode + "-"
				+ dimensionMode;

		f1 = new File(dataDirectory, base + ".sd1");
		f2 = new File(dataDirectory, base + ".sd2");

		// if file exists, increment the block number and try again
		while (f1.exists() || f2.exists()) {
			++blockNumber;
			blockCode = blockNumber < 10 ? "B0" + blockNumber : "B"
					+ blockNumber;
			base = "FittsTouch-" + participantCode + "-" + sessionCode + "-"
					+ blockCode + "-" + groupCode + "-" + conditionCode + "-"
					+ dimensionMode;

			f1 = new File(dataDirectory, base + ".sd1");
			f2 = new File(dataDirectory, base + ".sd2");
		}

		try {
			sd1 = new BufferedWriter(new FileWriter(f1));
			sd2 = new BufferedWriter(new FileWriter(f2));

			// output header lines in data files
			sd1.write(SD1_HEADER, 0, SD1_HEADER.length());
			sd1.flush();
			sd2.write(SD2_HEADER, 0, SD2_HEADER.length());
			sd2.flush();
		} catch (IOException e) {
			Log.i("MYDEBUG",
					"Error opening data files! Exception: " + e.toString());
			super.onDestroy();
			this.finish();
		}

		ep = (ExperimentPanel) findViewById(R.id.experimentpanel);
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
		ep.debug = debug;
		ep.resetBackground();
		ep.waitStartCircleSelect = true;

		/*
		 * Scale target amplitudes and widths as appropriate for display size.
		 * The smaller of w or h constrains allowable space.
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

		// condition with largest amplitude and widest target will span the
		// available display width
		// (minus 10 pixels)
		float scaleFactor = span / (largestAmplitude + largestWidth + 10f);

		// scale amplitudes
		for (int i = 0; i < amplitude.length; ++i)
			amplitude[i] *= scaleFactor;

		// scale widths
		for (int i = 0; i < width.length; ++i)
			width[i] *= scaleFactor;

		vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		missSound = MediaPlayer.create(this, R.raw.miss);
		selectSound = MediaPlayer.create(this, R.raw.pop);

		// tweaks needed to accommodate 1D vs. 2D modes
		if (dimensionMode.equals("1D"))
			numberOfTargets = 2; // reciprocal tapping
		if (dimensionMode.equals("2D"))
			numberOfTrials = numberOfTargets;

		// arrays needed for Throughput calculation (done on a per-sequence
		// basis)
		from = new PointF[numberOfTrials];
		to = new PointF[numberOfTrials];
		select = new PointF[numberOfTrials];
		mtArray = new float[numberOfTrials];

		aw = getAmplitudeWidthArray(amplitude, width);
		awIdx = 0;
		waitTargetSelected = true;

	}

	FaceDetectionListener faceDetectionListener = new FaceDetectionListener() {

		@Override
		public void onFaceDetection(Face[] faces, Camera camera) {
			Log.e(TAG, "Faces Detected through FaceDetectionListener = "
					+ faces.length);
		}
	};

	private void orientationListener() {
		orientationEventListener = new OrientationEventListener(this,
				SensorManager.SENSOR_DELAY_NORMAL) {
			@Override
			public void onOrientationChanged(int orientation) {
				deviceOrientation = orientation;
			}
		};

		if (orientationEventListener.canDetectOrientation()) {
			orientationEventListener.enable();
		}

		presentOrientation = 90 * (deviceOrientation / 360) % 360;
	}

	/*
	 * Function for the screen touch action listener. On touching the screen,
	 * the face data info will be displayed.
	 */
	private void touchScreenListener() {
		preview.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:

					if (!info) {
						LayoutParams layoutParams = preview.getLayoutParams();

						if (CameraFittsActivity.this.getResources()
								.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
							int oldHeight = preview.getHeight();
							layoutParams.height = oldHeight * 3 / 4;
						} else {
							int oldHeight = preview.getHeight();
							layoutParams.height = oldHeight * 80 / 100;
						}
						preview.setLayoutParams(layoutParams);// Setting the
																// changed
																// parameters
																// for the
																// layout.
						info = true;
					} else {
						LayoutParams layoutParams = preview.getLayoutParams();
						layoutParams.height = LayoutParams.WRAP_CONTENT;
						preview.setLayoutParams(layoutParams);// Setting the
																// changed
																// parameters
																// for the
																// layout.
						info = false;
					}
					break;

				case MotionEvent.ACTION_MOVE:
					break;

				case MotionEvent.ACTION_UP:
					break;
				}

				return true;
			}
		});

	}

	/*
	 * Function for switch camera action listener. Switches camera from front to
	 * back and vice versa.
	 */
	private void cameraSwitchActionListener() {
		ImageView switchButton = (ImageView) findViewById(R.id.switchCameraButton);

		switchButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				if (!cameraSwitch)// If the camera is facing front then do this
				{
					stopCamera();
					cameraObj = Camera.open(BACK_CAMERA_INDEX);
					mPreview = new CameraSurfacePreview(
							CameraFittsActivity.this, cameraObj, faceProc);
					preview = (FrameLayout) findViewById(R.id.camera_preview);
					preview.addView(mPreview);
					cameraSwitch = true;
					cameraObj.setPreviewCallback(CameraFittsActivity.this);
				} else // If the camera is facing back then do this.
				{
					stopCamera();
					cameraObj = Camera.open(FRONT_CAMERA_INDEX);
					preview.removeView(mPreview);
					mPreview = new CameraSurfacePreview(
							CameraFittsActivity.this, cameraObj, faceProc);
					preview = (FrameLayout) findViewById(R.id.camera_preview);
					preview.addView(mPreview);
					cameraSwitch = false;
					cameraObj.setPreviewCallback(CameraFittsActivity.this);
				}

			}

		});
	}

	/*
	 * Function for pause button action listener to pause and resume the
	 * preview.
	 */
	private void pauseActionListener() {
		ImageView pause = (ImageView) findViewById(R.id.pauseButton);
		pause.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				if (!cameraPause) {
					cameraObj.stopPreview();
					cameraPause = true;
				} else {
					cameraObj.startPreview();
					cameraObj.setPreviewCallback(CameraFittsActivity.this);
					cameraPause = false;
				}

			}
		});
	}

	/*
	 * This function will update the TextViews with the new values that come in.
	 */

	public void setUI(int numFaces, int smileValue, int leftEyeBlink,
			int rightEyeBlink, int faceRollValue, int faceYawValue,
			int facePitchValue, PointF gazePointValue, int horizontalGazeAngle,
			int verticalGazeAngle) {

		/*
		 * numFaceText.setText("Number of Faces: " + numFaces);
		 * smileValueText.setText("Smile Value: " + smileValue);
		 * leftBlinkText.setText("Left Eye Blink Value: " + leftEyeBlink);
		 * rightBlinkText.setText("Right Eye Blink Value " + rightEyeBlink);
		 * faceRollText.setText("Face Roll Value: " + faceRollValue);
		 * faceYawText.setText("Face Yaw Value: " + faceYawValue);
		 * facePitchText.setText("Face Pitch Value: " + facePitchValue);
		 * horizontalGazeText.setText("Horizontal Gaze: " +
		 * horizontalGazeAngle); verticalGazeText.setText("VerticalGaze: " +
		 * verticalGazeAngle);
		 */

		/*
		 * if (gazePointValue != null) { double x = Math.round(gazePointValue.x
		 * * 100.0) / 100.0;// Rounding // the gaze // point // value. double y
		 * = Math.round(gazePointValue.y * 100.0) / 100.0;
		 * gazePointText.setText("Gaze Point: (" + x + "," + y + ")"); } else {
		 * gazePointText.setText("Gaze Point: ( , )"); }
		 */
	}

	@Override
	protected void onPause() {
		super.onPause();
		stopCamera();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (cameraObj != null) {
			stopCamera();
		}
		if (!cameraSwitch)
			startCamera(FRONT_CAMERA_INDEX);
		else
			startCamera(BACK_CAMERA_INDEX);
	}

	/*
	 * This is a function to stop the camera preview. Release the appropriate
	 * objects for later use.
	 */
	public void stopCamera() {
		if (cameraObj != null) {
			cameraObj.stopPreview();
			cameraObj.setPreviewCallback(null);
			preview.removeView(drawView);
			preview.removeView(mPreview);
			cameraObj.release();
			faceProc.release();
			faceProc = null;
		}

		cameraObj = null;
	}

	/*
	 * This is a function to start the camera preview. Call the appropriate
	 * constructors and objects.
	 * 
	 * @param-cameraIndex: Will specify which camera (front/back) to start.
	 */
	public void startCamera(int cameraIndex) {

		if (fpFeatureSupported && faceProc == null) {

			Log.e("TAG", "Feature is supported");
			faceProc = FacialProcessing.getInstance();// Calling the Facial
														// Processing
														// Constructor.
		}

		try {
			cameraObj = Camera.open(cameraIndex);// attempt to get a Camera
													// instance
		} catch (Exception e) {
			Log.d("TAG", "Camera Does Not exist");// Camera is not available (in
													// use or does not exist)
		}

		mPreview = new CameraSurfacePreview(CameraFittsActivity.this,
				cameraObj, faceProc);
		preview.removeView(mPreview);
		preview = (FrameLayout) findViewById(R.id.camera_preview);
		preview.addView(mPreview);
		cameraObj.setPreviewCallback(CameraFittsActivity.this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.camera_preview, menu);
		return true;
	}

	/*
	 * Detecting the face according to the new Snapdragon SDK. Face detection
	 * will now take place in this function. 1) Set the Frame 2) Detect the
	 * Number of faces. 3) If(numFaces > 0) then do the necessary processing.
	 */
	@Override
	public void onPreviewFrame(byte[] data, Camera arg1) {

		presentOrientation = (90 * Math.round(deviceOrientation / 90)) % 360;
		int dRotation = display.getRotation();
		PREVIEW_ROTATION_ANGLE angleEnum = PREVIEW_ROTATION_ANGLE.ROT_0;

		switch (dRotation) {
		case 0:
			displayAngle = 90;
			angleEnum = PREVIEW_ROTATION_ANGLE.ROT_90;
			break;

		case 1:
			displayAngle = 0;
			angleEnum = PREVIEW_ROTATION_ANGLE.ROT_0;
			break;

		case 2:
			// This case is never reached.
			break;

		case 3:
			displayAngle = 180;
			angleEnum = PREVIEW_ROTATION_ANGLE.ROT_180;
			break;
		}

		if (faceProc == null) {
			faceProc = FacialProcessing.getInstance();
		}

		Parameters params = cameraObj.getParameters();
		Size previewSize = params.getPreviewSize();
		surfaceWidth = mPreview.getWidth();
		surfaceHeight = mPreview.getHeight();

		// Landscape mode - front camera
		if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE
				&& !cameraSwitch) {
			faceProc.setFrame(data, previewSize.width, previewSize.height,
					true, angleEnum);
			cameraObj.setDisplayOrientation(displayAngle);
			landScapeMode = true;
		}
		// landscape mode - back camera
		else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE
				&& cameraSwitch) {
			faceProc.setFrame(data, previewSize.width, previewSize.height,
					false, angleEnum);
			cameraObj.setDisplayOrientation(displayAngle);
			landScapeMode = true;
		}
		// Portrait mode - front camera
		else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT
				&& !cameraSwitch) {
			faceProc.setFrame(data, previewSize.width, previewSize.height,
					true, angleEnum);
			cameraObj.setDisplayOrientation(displayAngle);
			landScapeMode = false;
		}
		// Portrait mode - back camera
		else {
			faceProc.setFrame(data, previewSize.width, previewSize.height,
					false, angleEnum);
			cameraObj.setDisplayOrientation(displayAngle);
			landScapeMode = false;
		}

		int numFaces = faceProc.getNumFaces();

		if (numFaces == 0) {
			if (trackingFace) {
				countTrackLost++;
				trackingFace = false;
			}
			Log.d("TAG", "No Face Detected");
			if (drawView != null) {
				preview.removeView(drawView);

				drawView = DrawView.getInstance(this, null, false, 0, 0, null,
						landScapeMode, ep, navigationType);
				preview.addView(drawView);
			}
			if (gameDrawView != null) {
				preview.removeView(gameDrawView);

				gameDrawView = new GameDrawView(this, null, false, 0, 0, null,
						landScapeMode);
				preview.addView(gameDrawView);
			}
			canvas.drawColor(0, Mode.CLEAR);
			setUI(0, 0, 0, 0, 0, 0, 0, null, 0, 0);
		} else {
			if (!trackingFace) {
				trackingFace = true;
			}

			Log.d("TAG", "Face Detected");
			faceArray = faceProc.getFaceData(EnumSet.of(
					FacialProcessing.FP_DATA.FACE_RECT,
					FacialProcessing.FP_DATA.FACE_COORDINATES,
					FacialProcessing.FP_DATA.FACE_CONTOUR,
					FacialProcessing.FP_DATA.FACE_SMILE,
					FacialProcessing.FP_DATA.FACE_ORIENTATION,
					FacialProcessing.FP_DATA.FACE_BLINK));
			// faceArray = faceProc.getFaceData(); // Calling getFaceData()
			// alone will give you all facial data except the
			// face
			// contour. Face Contour might be a heavy operation, it is
			// recommended that you use it only when you need it.
			if (faceArray == null) {
				Log.e("TAG", "Face array is null");
			} else {
				if (faceArray[0].leftEyeObj == null) {
					Log.e(TAG, "Eye Object NULL");
				} else {
					Log.e(TAG, "Eye Object not NULL");
				}

				faceProc.normalizeCoordinates(surfaceWidth, surfaceHeight);

				preview.removeView(gameDrawView);
				drawView = DrawView.getInstance(this, faceArray, true,
						surfaceWidth, surfaceHeight, cameraObj, landScapeMode,
						ep, navigationType);
				gameDrawView = new GameDrawView(this, faceArray, true,
						surfaceWidth, surfaceHeight, cameraObj, landScapeMode);
				preview.removeView(drawView);// Remove the previously created
				// view to avoid unnecessary
				// stacking of Views.
				preview.addView(drawView);

				for (int j = 0; j < numFaces; j++) {
					smileValue = faceArray[j].getSmileValue();
					leftEyeBlink = faceArray[j].getLeftEyeBlink();
					rightEyeBlink = faceArray[j].getRightEyeBlink();
					// faceRollValue = faceArray[j].getRoll();
					// gazePointValue = faceArray[j].getEyeGazePoint();
					pitch = faceArray[j].getPitch();
					yaw = faceArray[j].getYaw();
					// horizontalGaze =
					// faceArray[j].getEyeHorizontalGazeAngle();
					// verticalGaze = faceArray[j].getEyeVerticalGazeAngle();
				}

				runningX = runningArrayX[runningCounter] = Math.sin(yaw * (Math.PI / 180))
						* SCALE_UNIT_CIRCLE_FOR_ROTATION;
				runningY = runningArrayY[runningCounter] = Math.sin(pitch
						* (Math.PI / 180))
						* SCALE_UNIT_CIRCLE_FOR_ROTATION;

				// Log.d("running", "xthetha=" + yaw * (Math.PI / 180) + "\tx="
				// + runningArrayX[runningCounter] + "\tytheta="
				// + (pitch + 15) * (Math.PI / 180) + "\ty="
				// + runningArrayY[runningCounter]);

				runningCounter = (runningCounter + 1) % RUNNING_LENGTH;

				if (selectionType.equals("Blink")) {
					if (leftEyeBlink > BLINK_THRESHOLD
							|| rightEyeBlink > BLINK_THRESHOLD) {
						elapsedActionTime += System.nanoTime() - oldActionTime;
						oldActionTime = System.nanoTime();
						// Log.d("blink", "elapsedTime=" +
						// elapsedActionTime/1000000);
						if ((elapsedActionTime / 1000000) > BLINK_TIME_THRESHOLD
								&& ((System.nanoTime() - selectActionCooldown) / 1000000) > 3500) {
							selectActionCooldown = System.nanoTime();
							if (ep.waitStartCircleSelect) {
								if (ep.startCircle.inTarget(ep.cursor.getX(),
										ep.cursor.getY())) {
									doStartCircleSelected();
								}
							} else {
								doTargetSelected(ep.cursor.getX(),
										ep.cursor.getY());
							}
						}
					} else {
						// reset elapsed time
						elapsedActionTime = 0;
						oldActionTime = System.nanoTime();
					}
				} else if (selectionType.equals("Smile")) {
					if (smileValue > SMILE_THRESHOLD) {
						if (((System.nanoTime() - selectActionCooldown) / 1000000) > 3500) {
							selectActionCooldown = System.nanoTime();
							if (ep.waitStartCircleSelect) {
								if (ep.startCircle.inTarget(ep.cursor.getX(),
										ep.cursor.getY())) {
									doStartCircleSelected();
								}
							} else {
								doTargetSelected(ep.cursor.getX(),
										ep.cursor.getY());
							}
						}
					}
				} else if (selectionType.equals("Dwell")) {
					if (inProximity()) {
						elapsedActionTime += System.nanoTime() - oldActionTime;
						oldActionTime = System.nanoTime();
						if (!timerActive && !ep.waitStartCircleSelect) {
							timerActive = true;
							timeOutTimer = System.nanoTime();
						}
						dwelling = true;
						if (elapsedActionTime / 1000000 >= DWELL_TIME_THRESHOLD
								&& ((System.nanoTime() - selectActionCooldown) / 1000000) >= DWELL_COOLDOWN) {
							selectActionCooldown = System.nanoTime();
							if (ep.waitStartCircleSelect) {
								if (ep.startCircle.inTarget(ep.cursor.getX(),
										ep.cursor.getY())) {
									doStartCircleSelected();
								}
							} else {
								doTargetSelected(ep.cursor.getX(),
										ep.cursor.getY());
							}
						}
					} else {
						if (dwelling && !ep.waitStartCircleSelect) {
							countReentry++;
							dwelling = false;

						}
						// reset elapsed time
						elapsedActionTime = 0;
						oldActionTime = System.nanoTime();
					}

					// Log.d("dwell", "elapsed=" + (System.nanoTime() -
					// timeOutTimer)/1000000);
					if (timerActive
							&& (System.nanoTime() - timeOutTimer) / 1000000 > DWELL_TIMEOUT
							&& !ep.waitStartCircleSelect) {
						Log.d("timeout", "time=" + (System.nanoTime() - timeOutTimer) / 1000000 + "\ttimerActive?=" + timerActive);
						doTargetSelected(ep.cursor.getX(), ep.cursor.getY());
						timerActive = false;
						countTimeout++;
						timeOutTimer = System.nanoTime();
					}
				}

				runningEstimatedX = (1 - ALPHA) * runningEstimatedX + ALPHA * runningX;
				runningEstimatedY = (1 - ALPHA) * runningEstimatedY + ALPHA * runningY;
				
				drawView.setPointer(runningEstimatedX, -runningEstimatedY);
				setUI(numFaces, smileValue, leftEyeBlink, rightEyeBlink,
						faceRollValue, yaw, pitch, gazePointValue,
						horizontalGaze, verticalGaze);
			}
		}
	}

	private double computeRunningAvg(double[] arr) {
		double sum = 0.0;
		for (int i = 0; i < arr.length; i++) {
			sum += arr[i];
		}
		return sum / arr.length;
	}

	protected float lowPass(float input, float output) {
		if (output == 0)
			return input;

		output = output + ALPHA * (input - output);
		return output;
	}

	/**
	 * =========================================== FITTS METHODS
	 * ===========================================
	 */

	// convert the amplitude/width string in the spinners to float array
	private float[] getValues(String valuesArg) {
		StringTokenizer st = new StringTokenizer(valuesArg, ", ");
		int i = 0;
		float[] values = new float[st.countTokens()];
		while (st.hasMoreTokens())
			values[i++] = Float.parseFloat(st.nextToken());
		return values;
	}

	private void configureTargets(int awIdx) {
		for (int i = 0; i < numberOfTargets; ++i) {
			float x = xCenter
					+ (float) (aw[awIdx].a / 2f)
					* (float) Math.cos(TWO_TIMES_PI
							* ((float) i / numberOfTargets));
			float y = yCenter
					+ (float) (aw[awIdx].a / 2f)
					* (float) Math.sin(TWO_TIMES_PI
							* ((float) i / numberOfTargets));
			if (ep.mode.equals("1D"))
				ep.targetSet[i] = new Target(Target.RECTANGLE, x, y,
						(float) aw[awIdx].w, screenHeight - 40f, Target.NORMAL);
			else
				ep.targetSet[i] = new Target(Target.CIRCLE, x, y,
						(float) aw[awIdx].w, (float) aw[awIdx].w, Target.NORMAL);
		}
		// Don't set target yet. This is done when start circle is selected.
	}

	private AmplitudeWidth[] getAmplitudeWidthArray(float[] aArray,
			float[] wArray) {
		AmplitudeWidth[] aw = new AmplitudeWidth[aArray.length * wArray.length];
		for (int i = 0; i < aw.length; ++i)
			aw[i] = new AmplitudeWidth(aArray[i / wArray.length], wArray[i
					% wArray.length]);

		// shuffle
		Random r = new Random();
		for (int i = 0; i < aw.length; ++i) {
			int idx = r.nextInt(aw.length);
			AmplitudeWidth temp = aw[idx];
			aw[idx] = aw[i];
			aw[i] = temp;
		}
		return aw;
	}

	public void doFingerDown(float xArg, float yArg) {
		xFingerDown = xArg;
		yFingerDown = yArg;
		fingerDownTime = System.nanoTime();
	}

	public void doStartCircleSelected() {
		if (ep.done) // start circle displayed after last sequence, select to
						// finish
			doEndBlock();

		ep.waitStartCircleSelect = false;
		even = true;
		if (awIdx < aw.length) {
			ep.targetSet = new Target[numberOfTargets];
			configureTargets(awIdx);
		}

		ep.targetSet[0].status = Target.TARGET;
		ep.toTarget = ep.targetSet[0];
		selectionCount = 0;
	}

	// Done! close data files and exit
	private void doEndBlock() {
		try {
			sd1.close();
			sd2.close();

			// Make the saved data files visible in Windows Explorer
			// There seems to be bug doing this with Android 4.4. I'm using the
			// following
			// code, instead of sendBroadcast. See...
			// http://code.google.com/p/android/issues/detail?id=38282
			MediaScannerConnection
					.scanFile(
							this,
							new String[] { f1.getAbsolutePath(),
									f2.getAbsolutePath() }, null, null);
		} catch (IOException e) {
			Log.d("MYDEBUG", "FILE CLOSE ERROR! e = " + e);
		}
		this.finish();
	}

	void doTargetSelected(float xSelect, float ySelect) {
		trialStartTime = now; // last "now" value is start of trial
		now = System.nanoTime(); // current "now" value is end of trial

		// hit or miss? (respond appropriately)
		trialMiss = ep.toTarget.inTarget(xSelect, ySelect) ? 0 : 1;
		if (trialMiss == 1) {
			// provide feedback (as per setup) but only if the user misses the
			// target
			if (vibrotactileFeedback)
				vib.vibrate(VIBRATION_PULSE_DURATION);
			if (auditoryFeedback)
				missSound.start();
		} else {
			if (auditoryFeedback)
				selectSound.start();
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
		 * CALCULATION OF XDELTA: xDelta is the signed distance of the selection
		 * coordinate from the target center, as projected on the task axis. The
		 * projection onto the task axis is performed to respect the inherent
		 * one-dimensionality of Fitts' law. NOTE: xDelta is -ve if the
		 * selection is on the near side of the target center, +ve otherwise.
		 * 
		 * NOTE: Some of the calculations here are repeated in the Throughput
		 * class. They are also needed here to compute xDelta and ae at the end
		 * of each trial. Recall that the Throughput class is only given data at
		 * the end of a sequence.
		 */

		// a is the distance from the center of the "from" target to the center
		// of the "to" target.
		float ax = (float) (ep.fromTarget.xCenter - ep.toTarget.xCenter);
		float ay = (float) (ep.fromTarget.yCenter - ep.toTarget.yCenter);
		float a = (float) Math.sqrt(ax * ax + ay * ay);

		// b is the distance from the selection coordinate to the center of the
		// "to" target.
		float b = (float) Math.sqrt((xSelect - ep.toTarget.xCenter)
				* (xSelect - ep.toTarget.xCenter)
				+ (ySelect - ep.toTarget.yCenter)
				* (ySelect - ep.toTarget.yCenter));

		// c is the distance from the center of the "from" target to the
		// selection coordinate.
		float c = (float) Math.sqrt((xSelect - ep.fromTarget.xCenter)
				* (xSelect - ep.fromTarget.xCenter)
				+ (ySelect - ep.fromTarget.yCenter)
				* (ySelect - ep.fromTarget.yCenter));

		// combine Pythagorean theorems to find xDelta
		float xDelta = -1.0f * (a * a + b * b - c * c) / (2 * a);

		/*
		 * ae is the "effective target amplitude" (the actual distance moved, as
		 * projected on the task axis). The calculation here isn't quite that
		 * same as in the Throughput class, where there is also an adjustment
		 * for the xDelta from the previous trial (for all trials after the
		 * first). The calculation here is good enough, since we are only using
		 * ae to determine if a trial is an outlier. The actual ae used in
		 * calculating throughput is calculated in the Throughput class.
		 */
		float ae = a + xDelta;

		/*
		 * DEFINITION OF OUTLIER: Any trial where the actual distance moved is
		 * less the 1/2 the specified amplitude is deemed an outlier. Any
		 * sequence with 1 or more outliers will be repeated. The
		 */

		if (ae < aw[awIdx].a / 2f)
			outlier = true;

		String trialTime = String.format("%.1f",
				(now - trialStartTime) / 1000000.0f);
		String fingerDownUpTime = String.format("%.1f",
				(now - fingerDownTime) / 1000000.0f);
		float fingerDownUpDelta = (float) Math.sqrt((xSelect - xFingerDown)
				* (xSelect - xFingerDown) + (ySelect - yFingerDown)
				* (ySelect - yFingerDown));

		// ensure this is consistent with the sd1 header line defined at the top
		// of
		// FittsTouchActivity
		sb1.append(participantCode + "," + sessionCode + "," + blockCode + ","
				+ groupCode + "," + conditionCode + "," + dimensionMode + ","
				+ navigationType + "," + selectionCount + ","
				+ selectionCount + "," + aw[awIdx].a + "," + aw[awIdx].w + ","
				+ ep.fromTarget.xCenter + "," + ep.fromTarget.yCenter + ","
				+ ep.toTarget.xCenter + "," + ep.toTarget.yCenter + ","
				+ xFingerDown + "," + yFingerDown + "," + xSelect + ","
				+ ySelect + "," + xDelta + "," + fingerDownUpDelta + ","
				+ fingerDownUpTime + "," + b + "," + trialMiss + ","
				+ trialTime + "\n");

		/*
		 * These four arrays are added to at the end of each trial. At the end
		 * of a sequence, they are passed to the Throughput constructor. The
		 * Throughput object will compute throughput and other values based on
		 * the data in these arrays.
		 */
		from[selectionCount] = new PointF((float) ep.fromTarget.xCenter,
				(float) ep.fromTarget.yCenter);
		to[selectionCount] = new PointF((float) ep.toTarget.xCenter,
				(float) ep.toTarget.yCenter);
		select[selectionCount] = new PointF(xSelect, ySelect);
		mtArray[selectionCount] = (now - trialStartTime) / 1000000.0f;

		// prepare for next target selection
		++selectionCount;
		advanceTarget();

		if (selectionCount == numberOfTrials) // finished sequence
			doEndSequence();
	}

	void doEndSequence() {
		if (outlier) {
			++outlierSequenceCount;
			results.append("Oops! Outlier sequence!::Possible causes...:- missed tap:- double tap::Tap to try again");
			ep.resultsString = results.toString().split(":");
			outlier = false;
		} else {

			// give the relevant data to the Throughput object (where the
			// serious calculations are
			// performed)
			int taskType = dimensionMode.equals("1D") ? Throughput.ONE_DIMENSIONAL
					: Throughput.TWO_DIMENSIONAL;
			int responseType = Throughput.SERIAL;
			Throughput t = new Throughput("nocode", aw[awIdx].a, aw[awIdx].w,
					taskType, responseType, from, to, select, mtArray);

			// CAUTION: ensure this is consistent with the sd2 header line
			// defined at the top of
			// FittsTouchActivity
			sb2.append(participantCode + "," + sessionCode + "," + blockCode
					+ "," + groupCode + "," + conditionCode + ","
					+ t.getTaskTypeString(taskType) + ","
					+ navigationType + "," + selectionCount + ","
					+ t.getNumberOfTrials() + "," + outlierSequenceCount + ","
					+ t.getA() + "," + t.getW() + "," + t.getID() + ","
					+ t.getAe() + "," + t.getWe() + "," + t.getIDe() + ","
					+ t.getMT() + "," + t.getErrorRate() + ","
					+ t.getThroughput() + "," + countReentry + ","
					+ countTimeout + "," + countTrackLost + "\n");

			// write data to files at end of each sequence
			try {
				sd1.write(sb1.toString(), 0, sb1.length());
				sd1.flush();
				sd2.write(sb2.toString(), 0, sb2.length());
				sd2.flush();
			} catch (IOException e) {
				Log.d("MYDEBUG", "ERROR WRITING TO DATA FILES: e = " + e);
			}
			sb1.delete(0, sb1.length());
			sb2.delete(0, sb2.length());

			// prepare results for output on display
			results.append("Block " + Integer.parseInt(blockCode.substring(1))
					+ ":");
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
			results.append(String.format("Throughput = %.2f bps:",
					t.getThroughput()));
			results.append("-----:");
			results.append("# Tracking drop = " + countTrackLost + ":");
			results.append("# Re-entries(dwell) " + countReentry + ":");
			results.append("# Timeouts (dwell) " + countTimeout + ":");
			ep.resultsString = results.toString().split(":");

			++awIdx; // next A-W condition
			if (awIdx < aw.length)
				configureTargets(awIdx);
			else
				ep.done = true;
			outlierSequenceCount = 0;

			resetValues();
		}
		ep.waitStartCircleSelect = true;
		sequenceStarted = false;
	}

	private void resetValues() {
		// reset drop
		countTrackLost = 0;
		// reset re-entry count
		countReentry = 0;
		timerActive = false;
		// reset track lost count
		countTrackLost = 0;
	}

	// simple class to hold the amplitude and width for a Fitts' law task
	private class AmplitudeWidth {
		float a, w;

		AmplitudeWidth(float aArg, float wArg) {
			a = aArg;
			w = wArg;
		}
	}

	// advance to the next target (a bit complicated for the 2D task; see
	// comment below)
	private void advanceTarget() {
		timerActive = false;
		/*
		 * Find the current "target" then advance it to the circle on the
		 * opposite side of the layout circle. This is a bit tricky since every
		 * second advance requires the a target to be beside the target directly
		 * opposite the last target. This is needed to get the sequence of
		 * selections to advance around the layout circle. Of course, this only
		 * applies to the 2D task.
		 */
		int i;
		for (i = 0; i < ep.targetSet.length; ++i)
			if (ep.targetSet[i].status == Target.TARGET)
				break; // i is index of current target
		ep.targetSet[i].status = Target.NORMAL;
		int next;
		if (dimensionMode.equals("1D")) {
			next = (i + 1) % 2;
		} else {
			int halfWay = ep.targetSet.length / 2;
			if (ep.targetSet.length % 2 != 0) { // odd length
				next = (i + halfWay) % (ep.targetSet.length);
			} else { // even length
				next = even ? (i + halfWay) % ep.targetSet.length : (i
						+ halfWay + 1)
						% (ep.targetSet.length); // ouch!
			}
		}
		ep.targetSet[next].status = Target.TARGET;
		ep.fromTarget = ep.targetSet[i];
		ep.toTarget = ep.targetSet[next];
		even = !even;
	}

	@Override
	public boolean onTouch(View v, MotionEvent me) {
		float x = me.getX();
		float y = me.getY();
		if (debug) {
			if (me.getAction() == MotionEvent.ACTION_DOWN) {
				doFingerDown(x, y);
				return true;
			} else if (me.getAction() == MotionEvent.ACTION_UP) {
				if (ep.waitStartCircleSelect) {
					if (!ep.startCircle.inTarget(x, y))
						return true;
					else
						doStartCircleSelected();
				} else
					doTargetSelected(x, y);
			}
		}
		return true;
	}

	public String getNavType() {
		return navigationType;
	}

	/* Helper method to see if cursor is within a target circle */
	private Boolean inProximity() {
		float diffX = (ep.waitStartCircleSelect) ? ep.startCircle.xCenter
				- ep.cursor.getX() : ep.toTarget.xCenter - ep.cursor.getX();
		float diffY = (ep.waitStartCircleSelect) ? ep.startCircle.yCenter
				- ep.cursor.getY() : ep.toTarget.yCenter - ep.cursor.getY();
		float targetWidth = (ep.waitStartCircleSelect) ? ep.startCircle.width
				: ep.toTarget.width;
		return Math.sqrt((diffX * diffX) + (diffY * diffY)) <= targetWidth / 2;
	}

}
