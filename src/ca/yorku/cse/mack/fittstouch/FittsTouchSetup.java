package ca.yorku.cse.mack.fittstouch;

import com.qualcomm.snapdragon.sdk.sample.CameraFittsActivity;
import com.qualcomm.snapdragon.sdk.sample.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

@SuppressWarnings("unused")
public class FittsTouchSetup extends Activity
{
	private Spinner spinParticipant, spinSession, spinBlock, spinGroup, spinCondition;
	private Spinner spinMode, spinNumTrials;
	private Spinner spinNumTargets, spinAmplitude, spinWidth;
	private Spinner spinNavigationType, spinSelectionType;
	private CheckBox checkVibrotactileFeedback;
	private CheckBox checkAuditoryFeedback;
	private CheckBox checkDebug;

	/*
	 * The following arrays are used for the spinners in the setup dialog. The first entry is the
	 * default value. It will be replaced from the app's shared preferences if a corresponding entry
	 * was saved in a previous invocation.
	 */
	String[] navigationType = { "Positional", "Rotational", "Positional" };
	String[] selectionType = { "Dwell", "Dwell", "Blink", "Smile" };
	String[] participantCode = { "P99", "P01", "P02", "P03", "P04", "P05", "P06", "P07", "P08", "P09", "P10", "P11",
			"P12", "P13", "P14", "P15", "P16", "P17", "P18", "P19", "P20", "P21", "P22", "P23", "P24", "P25" };
	String[] sessionCode = { "S99", "S01", "S02", "S03", "S04", "S05", "S06", "S07", "S08", "S09", "S10", "S11", "S12",
			"S13", "S14", "S15", "S16", "S17", "S18", "S19", "S20", "S21", "S22", "S23", "S24", "S25" };
	String[] blockCode = { "(auto)" }; // effectively disable spinner for block
										// code
	String[] groupCode = { "G99", "G01", "G02", "G03", "G04", "G05", "G06", "G07", "G08", "G09", "G10", "G11", "G12",
			"G13", "G14", "G15", "G16", "G17", "G18", "G19", "G20", "G21", "G22", "G23", "G24", "G25" };
	String[] conditionCode = { "C99", "C01", "C02", "C03", "C04", "C05", "C06", "C07", "C08", "C09", "C10", "C11",
			"C12", "C13", "C14", "C15", "C16", "C17", "C18", "C19", "C20", "C21", "C22", "C23", "C24", "C25" };
	String[] dimensionMode = { "1D", "1D", "2D" };
	String[] numberOfTrials1D = { "20", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17",
			"18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30" };
	String[] numberOfTargets2D = { "20", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17",
			"18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30" };
	String[] amplitudes = { "120, 240, 480", "120, 240, 480", "250, 500", "500" };
	String[] widths = { "60, 100", "60, 100", "30, 60, 120", "100" };

	boolean vibrotactileFeedback = false;
	boolean auditoryFeedback = true;
	boolean debug = false;

	int screenOrientation;

	SharedPreferences sp;
	SharedPreferences.Editor spe;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setup);

		/*
		 * Initialize reference to shared preferences. NOTE: The values saved are the default
		 * values.
		 */
		sp = this.getPreferences(MODE_PRIVATE);

		/*
		 * Overwrite 1st entry from shared preferences, if corresponding value exits.
		 */
		participantCode[0] = sp.getString("participantCode", participantCode[0]);
		sessionCode[0] = sp.getString("sessionCode", sessionCode[0]);
		// block code initialized in main activity (based on existing filenames)
		groupCode[0] = sp.getString("groupCode", groupCode[0]);
		conditionCode[0] = sp.getString("conditionCode", conditionCode[0]);
		dimensionMode[0] = sp.getString("dimensionMode", dimensionMode[0]);
		numberOfTrials1D[0] = sp.getString("numberOfTrials", numberOfTrials1D[0]);
		numberOfTargets2D[0] = sp.getString("numberOfTargets", numberOfTargets2D[0]);
		amplitudes[0] = sp.getString("amplitudes", amplitudes[0]);
		widths[0] = sp.getString("widths", widths[0]);
		vibrotactileFeedback = sp.getBoolean("vibrotactileFeedback", false);
		auditoryFeedback = sp.getBoolean("auditoryFeedback", true);
		
		navigationType[0] = sp.getString("navigationType", navigationType[0]);
		selectionType[0] = sp.getString("selectionType", selectionType[0]);

		// get references to widget elements
		spinNavigationType = (Spinner)findViewById(R.id.paramNav);
		spinSelectionType = (Spinner)findViewById(R.id.paramSelect);
		spinParticipant = (Spinner)findViewById(R.id.paramPart);
		spinSession = (Spinner)findViewById(R.id.paramSess);
		spinBlock = (Spinner)findViewById(R.id.paramBlock);
		spinGroup = (Spinner)findViewById(R.id.paramGroup);
		spinCondition = (Spinner)findViewById(R.id.paramCondition);
		spinMode = (Spinner)findViewById(R.id.paramMode);
		spinNumTrials = (Spinner)findViewById(R.id.paramTrials);
		spinNumTargets = (Spinner)findViewById(R.id.paramTargets);
		spinAmplitude = (Spinner)findViewById(R.id.paramAmplitude);
		spinWidth = (Spinner)findViewById(R.id.paramWidth);
		checkVibrotactileFeedback = (CheckBox)findViewById(R.id.paramVibrotactileFeedback);
		checkAuditoryFeedback = (CheckBox)findViewById(R.id.paramAuditoryFeedback);
		checkDebug = (CheckBox)findViewById(R.id.paramDebug);

		// initialise spinner adapters
		ArrayAdapter<CharSequence> adapterPC = new ArrayAdapter<CharSequence>(this, R.layout.spinnerstyle,
				participantCode);
		spinParticipant.setAdapter(adapterPC);

		ArrayAdapter<CharSequence> adapterSS = new ArrayAdapter<CharSequence>(this, R.layout.spinnerstyle, sessionCode);
		spinSession.setAdapter(adapterSS);

		ArrayAdapter<CharSequence> adapterB = new ArrayAdapter<CharSequence>(this, R.layout.spinnerstyle, blockCode);
		spinBlock.setAdapter(adapterB);

		ArrayAdapter<CharSequence> adapterG = new ArrayAdapter<CharSequence>(this, R.layout.spinnerstyle, groupCode);
		spinGroup.setAdapter(adapterG);

		ArrayAdapter<CharSequence> adapterC = new ArrayAdapter<CharSequence>(this, R.layout.spinnerstyle, conditionCode);
		spinCondition.setAdapter(adapterC);

		ArrayAdapter<CharSequence> adapterM = new ArrayAdapter<CharSequence>(this, R.layout.spinnerstyle, dimensionMode);
		spinMode.setAdapter(adapterM);

		ArrayAdapter<CharSequence> adapterTR = new ArrayAdapter<CharSequence>(this, R.layout.spinnerstyle,
				numberOfTrials1D);
		spinNumTrials.setAdapter(adapterTR);

		ArrayAdapter<CharSequence> adapterNP = new ArrayAdapter<CharSequence>(this, R.layout.spinnerstyle,
				numberOfTargets2D);
		spinNumTargets.setAdapter(adapterNP);

		ArrayAdapter<CharSequence> adapterA = new ArrayAdapter<CharSequence>(this, R.layout.spinnerstyle, amplitudes);
		spinAmplitude.setAdapter(adapterA);

		ArrayAdapter<CharSequence> adapterW = new ArrayAdapter<CharSequence>(this, R.layout.spinnerstyle, widths);
		spinWidth.setAdapter(adapterW);
		
		ArrayAdapter<CharSequence> adapterNav = new ArrayAdapter<CharSequence>(this, R.layout.spinnerstyle,
				navigationType);
		spinNavigationType.setAdapter(adapterNav);
		
		ArrayAdapter<CharSequence> adapterSel = new ArrayAdapter<CharSequence>(this, R.layout.spinnerstyle,
				selectionType);
		spinSelectionType.setAdapter(adapterSel);

		checkVibrotactileFeedback.setChecked(vibrotactileFeedback);
		checkAuditoryFeedback.setChecked(auditoryFeedback);
		checkDebug.setChecked(debug);

		/*
		 * Determine if the device is naturally portrait or landscape. This is passed on to the
		 * activity in the variable screenOrienation.
		 */
		Display display = getWindowManager().getDefaultDisplay();
		int width = display.getWidth();
		int height = display.getHeight();

		/*
		 * Get the default screen orientation. Change, if necessary (see below).
		 */
		screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

		switch (display.getRotation())
		{
			case Surface.ROTATION_0:
			case Surface.ROTATION_180:
				if (width > height)
					screenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
				break;
			case Surface.ROTATION_270:
			case Surface.ROTATION_90:
				if (width < height)
					screenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
				break;
		}
	}

	/** Called when the "OK" button is pressed. */
	public void clickOK(View view)
	{
		// get user's choices
		String part = participantCode[spinParticipant.getSelectedItemPosition()];
		String sess = sessionCode[spinSession.getSelectedItemPosition()];
		// String block = blockCode[spinBlock.getSelectedItemPosition()];
		String group = groupCode[spinGroup.getSelectedItemPosition()];
		String condition = conditionCode[spinCondition.getSelectedItemPosition()];
		String mode = dimensionMode[spinMode.getSelectedItemPosition()];
		int numTrials = Integer.parseInt(numberOfTrials1D[spinNumTrials.getSelectedItemPosition()]);
		int numTargets = Integer.parseInt(numberOfTargets2D[spinNumTargets.getSelectedItemPosition()]);
		String amplitude = amplitudes[spinAmplitude.getSelectedItemPosition()];
		String width = widths[spinWidth.getSelectedItemPosition()];
		boolean vibrotactileFeedback = checkVibrotactileFeedback.isChecked();
		boolean debug = checkDebug.isChecked();
		boolean auditoryFeedback = checkAuditoryFeedback.isChecked();
		
		String navigationTypeSelected = navigationType[spinNavigationType.getSelectedItemPosition()];
		String selectionTypeSelected = selectionType[spinSelectionType.getSelectedItemPosition()];
		
		/*
		 * NOTE: Activities are created by calling zero-parameter constructors. It is not possible
		 * to directly pass arguments to Activities. Instead, primitive values can be "bundled" and
		 * passed.
		 */
		Bundle b = new Bundle();
		b.putString("navigationType", navigationTypeSelected);
		b.putString("selectionType", selectionTypeSelected);
		b.putString("participantCode", part);
		b.putString("sessionCode", sess);
		// b.putString("blockCode", block);
		b.putString("groupCode", group);
		b.putString("conditionCode", condition);
		b.putString("mode", mode);
		b.putInt("numberOfTrials", numTrials);
		b.putInt("numberOfTargets", numTargets);
		b.putString("amplitude", amplitude);
		b.putString("width", width);
		b.putBoolean("vibrotactileFeedback", vibrotactileFeedback);
		b.putBoolean("auditoryFeedback", auditoryFeedback);
		b.putBoolean("debug", debug);
		b.putInt("screenOrientation", screenOrientation);

		// start experiment activity
		Intent i = new Intent(getApplicationContext(), CameraFittsActivity.class);
		i.putExtras(b);
		startActivity(i);
		finish();
	}

	/** Called when the "Save" button is pressed. */
	public void clickSave(View view)
	{
		spe = sp.edit();
		spe.putString("participantCode", participantCode[spinParticipant.getSelectedItemPosition()]);
		spe.putString("sessionCode", sessionCode[spinSession.getSelectedItemPosition()]);
		spe.putString("groupCode", groupCode[spinGroup.getSelectedItemPosition()]);
		spe.putString("conditionCode", conditionCode[spinCondition.getSelectedItemPosition()]);
		spe.putString("dimensionMode", dimensionMode[spinMode.getSelectedItemPosition()]);
		spe.putString("numberOfTrials", numberOfTrials1D[spinNumTrials.getSelectedItemPosition()]);
		spe.putString("numberOfTargets", numberOfTargets2D[spinNumTargets.getSelectedItemPosition()]);
		spe.putString("amplitudes", amplitudes[spinAmplitude.getSelectedItemPosition()]);
		spe.putString("widths", widths[spinWidth.getSelectedItemPosition()]);
		spe.putBoolean("vibrotactileFeedback", checkVibrotactileFeedback.isChecked());
		spe.putBoolean("auditoryFeedback", checkAuditoryFeedback.isChecked());
		spe.commit();
		Toast.makeText(this, "Preferences saved!", Toast.LENGTH_SHORT).show();
	}

	/** Called when the "Exit" button is pressed. */
	public void clickExit(View view)
	{
		super.onDestroy(); // cleanup
		this.finish(); // terminate
	}
}
