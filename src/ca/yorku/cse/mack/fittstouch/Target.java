package ca.yorku.cse.mack.fittstouch;

import android.graphics.RectF;
import android.util.Log;

@SuppressWarnings("unused")
public class Target
{
	public final static int NORMAL = 1;
	public final static int TARGET = 2;
	public final static int ALREADY_SELECTED = 3;
	public final static int OTHER = 4;

	public final static int RECTANGLE = 0;
	public final static int CIRCLE = 1;

	public float xCenter, yCenter, width, height;
	public RectF r;
	public int status;
	public int type;

	public Target(int typeArg, float xCenterArg, float yCenterArg, float widthArg, float heightArg, int statusArg)
	{
		type = typeArg;
		r = new RectF(xCenterArg - widthArg / 2f, yCenterArg - heightArg / 2f, xCenterArg + widthArg / 2f, yCenterArg
				+ heightArg / 2f);
		xCenter = xCenterArg;
		yCenter = yCenterArg;
		width = widthArg;
		height = heightArg;
		status = statusArg;
	}

	/**
	 * Returns true if the specified coordinate is inside the target.
	 */
	public boolean inTarget(float xTest, float yTest)
	{
		//Log.i("MYDEBUG", "type=" + type);
		if (type == CIRCLE)
			return distanceFromTargetCenter(xTest, yTest) <= (width / 2f);
		else
			return r.contains(xTest, yTest);
	}

	public float distanceFromTargetCenter(float xTest, float yTest)
	{
		return (float) Math.sqrt((xCenter - xTest) * (xCenter - xTest) + (yCenter - yTest) * (yCenter - yTest));
	}
}