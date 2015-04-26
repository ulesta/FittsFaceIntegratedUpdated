package ca.yorku.cse.mack.fittstouch;

import android.graphics.PointF;
import android.util.Log;

/**
 * Throughput -- class to provide a simple and accurate calculation of Fitts' throughput.
 * 
 * This is an Android version of the Throughput class included with FittsTaskTwo.
 */
@SuppressWarnings("unused")
public class Throughput
{
	public final static float LOG_TWO = 0.693147181f;
	public final static float SQRT_2_PI_E = 4.132731354f;

	// int constants for response type
	public final static int SERIAL = 100;
	public final static int DISCRETE = 101;

	// int constants for tasks type
	public final static int ONE_DIMENSIONAL = 200;
	public final static int TWO_DIMENSIONAL = 201;

	/*
	 * The following are the core set of data values needed to compute throughput and other measures provided in this
	 * class.
	 */
	String code;
	float amplitude, width;
	PointF[] from, to, select;
	float[] mt;
	int numberOfTrials;
	boolean serialTask;
	int responseType, taskType;

	/*
	 * The following arrays are populated with values calculated from the data in the arrays above.
	 */
	float[] deltaX;
	float[] ae;
	int[] miss;

	// Blank constructor. Must be followed with setData
	Throughput()
	{
	}

	// Constructor with data
	public Throughput(String codeArg, float amplitudeArg, float widthArg, int taskTypeArg, int responseTypeArg,
			PointF[] fromArg, PointF[] toArg, PointF[] selectArg, float[] mtArg)
	{
		setData(codeArg, amplitudeArg, widthArg, taskTypeArg, responseTypeArg, fromArg, toArg, selectArg, mtArg);
	}

	/**
	 * Set the data for this Throughput object. This method can be used to provide a new set of the data to the
	 * Throughput object (without instantiated a new object).
	 * 
	 */
	private void setData(String codeArg, float amplitudeArg, float widthArg, int taskTypeArg, int responseTypeArg,
			PointF[] fromArg, PointF[] toArg, PointF[] selectArg, float[] mtArg)
	{
		// load core requisite data
		code = codeArg;
		amplitude = amplitudeArg;
		width = widthArg;
		taskType = taskTypeArg;
		responseType = responseTypeArg;
		from = fromArg;
		to = toArg;
		select = selectArg;
		mt = mtArg;

		// calculate data that depend on the core data loaded above (begin by initializing arrays)
		numberOfTrials = mt.length;
		deltaX = new float[mt.length];
		ae = new float[mt.length];
		miss = new int[mt.length];

		// fill deltaX, ae, and miss arrays
		for (int i = 0; i < to.length; ++i)
		{
			// start-of-trial coordinate (centre of the "from" target)
			float x1 = from[i].x;
			float y1 = from[i].y;

			// centre coordinate of the target to select (center of the "to" target)
			float x2 = to[i].x;
			float y2 = to[i].y;

			// actual selection coordinate ("select")
			float x = select[i].x;
			float y = select[i].y;

			// compute length of the sides of the triangle formed by the three points above
			float a = (float)Math.hypot(x1 - x2, y1 - y2); // a: the specified amplitude
			float b = (float)Math.hypot(x - x2, y - y2); // b: distance from the selection point to the target center
			float c = (float)Math.hypot(x1 - x, y1 - y); // c: distance from "from" to "to"

			verifyAmplitudeData(amplitude, a, i, taskType, numberOfTrials);

			/*
			 * Compute dx. This calculation is correct, but a diagram helps to visualize the geometry. dx is negative
			 * for a selection on the "near side" of the target center (undershoot) and positive for a selection on the
			 * "far side" of the target center (overshoot). For a near-side selection, the a-b-c triangle is acute
			 * (i.e., a^2 + b^2 > c^2). For a far-side selection the a-b-c triangle is obtuse (i.e., a^2 + b^2 < c^2).
			 */
			deltaX[i] = (c * c - b * b - a * a) / (2.0f * a); // Pythagorean identity

			// debug/demo code...
			// String type = (a * a + b * b) > (c * c) ? "ACUTE" : "obtuse";
			// System.out.printf("a=%7.2f   b=%7.2f   c=%7.2f   type=%6s   dx=%7.2f\n", a, b, c, type, deltaX[i]);

			/*
			 * Compute the effective movement amplitude. The computed amplitude, a, is adjusted by adding dx at the end
			 * of the trial to give the actual amplitude moved (as projected on the task axis).
			 * 
			 * For serial tasks, we also adjust for the starting position by adding dx from the previous trial (if i >
			 * 0).
			 */
			ae[i] = a + deltaX[i];
			if (serialTask && i > 0)
				ae[i] += deltaX[i - 1];

			/*
			 * Compute whether or not the target was missed. This information is not explicitly provided to the
			 * Throughput class, so we need to calculate it. For the 1D case, the target is missed if dx is more than
			 * half the target width. For the 2D case, we assume the targets are circular, which is typically the case.
			 * The target is missed if b (the distance from the selection coordinate to the center of the target; see
			 * above) is greater than half the diameter (i.e., width) of the target circle.
			 */
			if (taskType == Throughput.ONE_DIMENSIONAL)
				miss[i] = Math.abs(deltaX[i]) > width / 2.0 ? 1 : 0;
			else if (taskType == Throughput.TWO_DIMENSIONAL)
				miss[i] = b > width / 2.0 ? 1 : 0;
			else
				miss[i] = -1;
		}
	}

	/*
	 * Verify the amplitude data. The Throughput object receives as arguments the "amplitude" for the sequence of trials
	 * as well as two Point2D.Double arrays specifying the center of the "from" and "to" targets for each trial. This
	 * method verifies the correctness of these data. The process and calculations are different for 1D tasks and 2D
	 * tasks. The 1D case is simple: The amplitude passed is the distance between the center of the two targets -- the
	 * "from" and "to" targets. This is identical for each trial in the sequence. The 2D case is more complicated.
	 * 
	 * For the 2D case, the amplitude passed to the Throughput object is the diameter of the layout circle. This value
	 * is not necessarily the amplitude of movement for the trials, even if the movements are perfectly executed. The
	 * movement amplitudes for perfect movements are calculated here, and given the label "taskAdjustedAmplitude". The
	 * calculations are different depending on whether there is an even number of targets or an odd number of targets.
	 * 
	 * 2D - Even Number of Targets. If there is an even number of targets, the first trial (index = 0) begins by
	 * selecting the start target and then moving directly across the layout circle to the target on the opposite side.
	 * The movement distance equals the diameter of the layout circle. However, the next trial (index = 1) is to the
	 * target beside the start target. The movement distance in this case is less than the diameter of the layout
	 * circle. The movement distance is depends on the number of targets.
	 * 
	 * 2D - Odd Number of Targets. If there is an odd number of targets, the movement distance is the same for every
	 * trial. The distance is less than the diameter of the layout circle, because the target is slightly displaced from
	 * the location directly across the layout circle.
	 * 
	 * The calculations below account for the peculiarities of the tasks, as just described.
	 * 
	 * When all the calculations are done, we compare the taskAdjustedAmplitude to the value "a" passed here as an
	 * argument. The taskAdjustedAmplitude and a should be the same. Because we are dealing with floating point
	 * arithmetic and there may be some integer rounding issues for the location of targets as rendered on the screen,
	 * the comparison only requires that taskAdjustedAmplitude and a are within WIGGLE units of each other (see below).
	 * If they are, all is well. If they aren't, there's a problem.
	 */
	private void verifyAmplitudeData(float amplitude, float a, int trialIndex, int taskType, int numberOfTrials)
	{
		// allow this much difference when verifying amplitudes
		final float WIGGLE = 2.0f;

		// calculate the adjusted amplitude
		float taskAdjustedAmplitude = -1.0f;

		if (taskType == Throughput.TWO_DIMENSIONAL)
		{
			// even number of trials (taskAdjustedAmplitude is different for even- and odd-numbered trials)
			if (numberOfTrials % 2 == 0)
				if (trialIndex % 2 == 0) // even-indexed trials
					taskAdjustedAmplitude = amplitude;
				else
				// odd-indexed trials
				{
					float b = amplitude * (float)Math.sin(Math.PI / numberOfTrials);
					float theta = 0.5f * (float)Math.PI * (numberOfTrials - 2) / numberOfTrials;
					float c = b * (float)Math.sin(theta);
					float x = b * (float)Math.cos(theta);
					taskAdjustedAmplitude = (float)Math.sqrt((amplitude - x) * (amplitude - x) + c * c);
				}
			else
			// odd number of trials (taskAdjustedAmplitude is the same for every trial in the sequence)
			{
				float b = amplitude * (float)Math.sin(Math.PI / numberOfTrials);
				float m = 2.0f * numberOfTrials;
				float theta = 0.5f * (((float)Math.PI * (m - 2.0f)) / m);
				float x = (b / 2.0f) / (float)Math.tan(theta);
				float h = amplitude - x;
				taskAdjustedAmplitude = (float)Math.sqrt(h * h + (b / 2.0f) * (b / 2.0f));
			}
		} else if (taskType == Throughput.ONE_DIMENSIONAL)
		{
			taskAdjustedAmplitude = amplitude; // the 1D case is simple (but still worth checking)
		}

		if (Math.abs(a - taskAdjustedAmplitude) > WIGGLE)
			System.out.printf("Oops! amplitude=%1.1f, task_adjusted_amplitude= %1.2f, computed_amplitude= %1.2f\n",
					amplitude, taskAdjustedAmplitude, a);
	}

	/**
	 * Returns the code associated with this sequence of trials. The code is the string assigned to the sequence to
	 * associate text conditions (e.g., participant code, device code, etc.) with the sequence.
	 */
	public String getCode()
	{
		return code;
	}

	/**
	 * Returns the Throughput for the sequence of trials.
	 */
	public float getThroughput()
	{
		float aeMean = mean(ae);
		float sdx = sd(deltaX);
		float we = SQRT_2_PI_E * sdx;
		float ide = (float)Math.log(aeMean / we + 1.0f) / LOG_TWO; // bits
		float mtMean = mean(mt) / 1000.0f; // seconds
		return ide / mtMean; // bits per second
	}

	/**
	 * Returns the mean movement time (ms) for the sequence of trials.
	 */
	public float getMT()
	{
		return mean(mt); // milliseconds
	}

	/**
	 * Returns the number of trials in this sequence.
	 */
	public int getNumberOfTrials()
	{
		return numberOfTrials;
	}

	/**
	 * Returns the task type for this sequence. The return value is the int given to the Throughput object (via the
	 * constructor of setData). Should be Throughput.ONE_DIMENSIONAL or Throughout.TWO_DIMENSIONAL.
	 */
	public int getTaskType()
	{
		return taskType;
	}

	/**
	 * Returns a string representing the task type for this sequence. The string returned is "1D", "2D", or "?" (if the
	 * task type is unknown).
	 * 
	 * @return
	 */

	public String getTaskTypeString(int taskType)
	{
		if (taskType == ONE_DIMENSIONAL)
			return "1D";
		else if (taskType == TWO_DIMENSIONAL)
			return "2D";
		else
			return "?";
	}

	/**
	 * Returns the response type for this sequence. The value returned is the int constant passed to the Throughput
	 * object in the first place (via the constructor or setData). Should be Throughput.SERIAL or Throughput.DISCRETE.
	 */
	public int getResponseType()
	{
		return responseType;
	}

	/**
	 * Returns a string representing the response type for this sequence. The string returned is "Serial", "Discrete",
	 * of "?" (if the response type is unknown).
	 */
	public String getResponseTypeString(int responseType)
	{
		if (responseType == SERIAL)
			return "Serial";
		else if (responseType == DISCRETE)
			return "Discrete";
		else
			return "?";
	}

	/**
	 * Returns a point array containing the "from" points for the trials in this sequence. The "from" points are the
	 * coordinates of the center of the target from which each trial begins.
	 */
	public PointF[] getFrom()
	{
		return from;
	}

	/**
	 * Returns a point array containing the "to" points for the trials in this sequence. The "to" points are the
	 * coordinates of the center of the target to which each trial proceeds.
	 */
	public PointF[] getTo()
	{
		return to;
	}

	/**
	 * Returns a point array containing the "select" points for the trials in this sequence. The "select" points are the
	 * coordinates of the point of selection where each trial terminated.
	 */
	public PointF[] getSelect()
	{
		return select;
	}

	/**
	 * Returns the float array holding the mt (movement time) values for the trials in this sequence.
	 * 
	 * @return
	 */
	public float[] getMTArray()
	{
		return mt;
	}

	/**
	 * Returns the standard deviation in the selection coordinates for this sequence of trials. The coordinates are
	 * projected onto the task axis.
	 */
	public float getSDx()
	{
		return sd(deltaX);
	}

	/**
	 * Returns the mean of the selection coordinates for this sequence of trials. The coordinates are projected onto the
	 * task axis.
	 */
	public float getX()
	{
		return mean(getDeltaX());
	}

	/**
	 * Returns the array of selection coordinates for this sequence of trials. The coordinates are projected onto the
	 * task axis.
	 */
	public float[] getDeltaX()
	{
		return deltaX;
	}

	/**
	 * Returns the specified amplitude for the trials in this sequence.
	 * 
	 * NOTE: This value is not used in calculating Throughput. It is provided only as a convenience.
	 */
	public float getA()
	{
		return amplitude;
	}

	/**
	 * Returns the effective amplitude for the trials in this sequence. The effective amplitude is the mean of the
	 * actual movement amplitudes for the sequence of trials, as projected on the task axis.
	 */
	public float getAe()
	{
		return mean(ae);
	}

	/**
	 * Returns the specified target width for this sequence of trials.
	 * 
	 * NOTE: This value is not used in calculating Throughput. It is provided only as a convenience.
	 */
	public float getW()
	{
		return width;
	}

	/**
	 * Returns the effective target width for this sequence of trials. The effective target width is 4.133 x SDx, where
	 * SDx is the standard deviation in the selection coordinates, as projected onto the task axis.
	 */
	public float getWe()
	{
		return SQRT_2_PI_E * getSDx();
	}

	/**
	 * Returns the specified index of difficulty for this sequence of trials. The specified index of difficulty is ID =
	 * log2(A/W + 1).
	 * 
	 * NOTE: This value is not used in calculating Throughput. It is provided only as a convenience.
	 */
	public float getID()
	{
		return (float)Math.log(getA() / getW() + 1.0f) / LOG_TWO;
	}

	/**
	 * Returns the effective index of difficulty for this sequence of trials. The effective index of difficulty, IDe =
	 * log2(Ae/We + 1).
	 */
	public float getIDe()
	{
		return (float)Math.log(getAe() / (SQRT_2_PI_E * getSDx()) + 1.0f) / LOG_TWO;
	}

	/**
	 * Returns the number of misses for this sequence.
	 */
	public int getMisses()
	{
		int count = 0;
		for (int i = 0; i < getNumberOfTrials(); ++i)
			count += miss[i];
		return count;
	}

	/**
	 * Returns the error rate as a percentage.
	 */
	public float getErrorRate()
	{
		return (float)getMisses() / getNumberOfTrials() * 100.0f;
	}

	// Calculate the mean of the values in a double array.
	private float mean(float n[])
	{
		float mean = 0.0f;
		for (int j = 0; j < n.length; j++)
			mean += n[j];
		return mean / n.length;
	}

	// Calculate the standard deviation of values in a double array.
	private float sd(float[] n)
	{
		float m = mean(n);
		float t = 0.0f;
		for (int j = 0; j < n.length; j++)
			t += (m - n[j]) * (m - n[j]);
		return (float)Math.sqrt(t / (n.length - 1.0f));
	}
}
