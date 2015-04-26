package ca.yorku.cse.mack.fittstouch;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * ExperimentPanel -- panel to present and sequence the targets
 * <p>
 * 
 * @author Scott MacKenzie
 * 
 */
public class ExperimentPanel extends View
{
	public final int START_TEXT_SIZE = 40; // may need to fiddle with this, depending on device
	public final int GAP = 12; // gap between lines

	public Target[] targetSet;
	public Target toTarget; // the target to select
	public Target fromTarget; // the source target from where the trial began
	public Target startCircle;
	
	public Cursor cursor;

	public float panelWidth;
	public float panelHeight;

	public boolean waitStartCircleSelect, done;
	public boolean debug;
	public String mode;
	public Paint targetPaint, targetRimPaint, normalPaint, startPaint;
	public Paint originPaint;
	public String[] resultsString = { "" };

	public ExperimentPanel(Context contextArg)
	{
		super(contextArg);
		initialize();
	}

	public ExperimentPanel(Context contextArg, AttributeSet attrs)
	{
		super(contextArg, attrs);
		initialize();
	}

	public ExperimentPanel(Context contextArg, AttributeSet attrs, int defStyle)
	{
		super(contextArg, attrs, defStyle);
		initialize();
	}

	// things that can be initialized from within this View
	private void initialize()
	{
		targetPaint = new Paint();
		targetPaint.setColor(0xffff5331);
		targetPaint.setStyle(Paint.Style.FILL);
		targetPaint.setAntiAlias(true);

		targetRimPaint = new Paint();
		targetRimPaint.setColor(Color.TRANSPARENT);
		targetRimPaint.setStyle(Paint.Style.STROKE);
		targetRimPaint.setStrokeWidth(2);
		targetRimPaint.setAntiAlias(true);

		normalPaint = new Paint();
		normalPaint.setColor(0xff67c6f2); // lighter red (to minimize distraction)
//		normalPaint.setStyle(Paint.Style.STROKE);
//		normalPaint.setStrokeWidth(2);
		normalPaint.setAntiAlias(true);

		startPaint = new Paint();
		startPaint.setColor(0xff36D7B7);
		startPaint.setStyle(Paint.Style.FILL);
		startPaint.setAntiAlias(true);
		startPaint.setTextSize(START_TEXT_SIZE);
		Log.i("bundle", "debugPanel=" + debug);
		

		this.setBackgroundColor(Color.argb(255, 68, 161, 204));
		
		this.cursor = new Cursor(0, 0, 20);
		
		originPaint = new Paint();
		originPaint.setColor(0xff36D7B7);
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		canvas.drawCircle(panelWidth/2, panelHeight/2, 50, originPaint);
		if (waitStartCircleSelect) // draw start circle and prompt/results string
		{
			canvas.drawCircle(startCircle.xCenter, startCircle.yCenter, startCircle.width / 2f, startPaint);
			for (int i = 0; i < resultsString.length; ++i)
				canvas.drawText(resultsString[i], 20, 25 + 2 * startCircle.width / 2f + (i + 1)
						* (START_TEXT_SIZE + GAP), startPaint);
		} else if (!done) // draw task targets
		{
			for (int i = 0; i < targetSet.length; ++i)
			{
				if (mode.equals("1D"))
					canvas.drawRect(targetSet[i].r, normalPaint);
				else
					// 2D
					canvas.drawOval(targetSet[i].r, normalPaint);
			}

			// draw target to select last (so it is on top of any overlapping targets)
			if (mode.equals("1D"))
			{
				canvas.drawRect(toTarget.r, targetPaint);
				canvas.drawRect(toTarget.r, targetRimPaint);
			} else
			// 2D
			{
				canvas.drawOval(toTarget.r, targetPaint);
				canvas.drawOval(toTarget.r, targetRimPaint);
			}
		}
		
		canvas.drawCircle(cursor.x, cursor.y, cursor.radius, cursor.paintbrush);
		
		invalidate(); // will cause onDraw to run again immediately
	}

	@SuppressLint("DrawAllocation") @Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		setMeasuredDimension((int)panelWidth, (int)panelHeight);
		startCircle = new Target(Target.CIRCLE, panelWidth/2, panelHeight/2, 100f, 100f, Target.NORMAL);
	}
	
	public class Cursor {
		int x,y;
		int radius;
		Paint paintbrush;
		
		Cursor(int x, int y, int radius) {
			this.x = x;
			this.y = y;
			this.radius = radius;
			
			paintbrush = new Paint();
			paintbrush.setColor(0xffECECEC);
		}

		public float getX() {
			return cursor.x;
		}

		public float getY() {
			return cursor.y;
		}
	}
	
	public void placeCursor(double d, double e) {
		this.cursor.x = (int) d;
		this.cursor.y = (int) e;
	}
	
	public void resetBackground() {
		if (debug) {
			this.setBackgroundColor(Color.argb(100, 68, 161, 204));
		} else {
			this.setBackgroundColor(Color.argb(255, 68, 161, 204));
		}
	}
}