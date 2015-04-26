/*
 * ======================================================================
 * This class is responsible for creating the Game layer on top of DrawView and CameraSurface
 * =====================================================================
 * @file: GameDrawView.java
 */

package com.qualcomm.snapdragon.sdk.sample;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.hardware.Camera;
import android.view.SurfaceView;

import com.qualcomm.snapdragon.sdk.face.FaceData;

public class GameDrawView extends SurfaceView {

    private final Paint leftEyeBrush = new Paint();
    private final Paint rightEyeBrush = new Paint();
    private final Paint mouthBrush = new Paint();
    private final Paint rectBrush = new Paint();
    public Point leftEye, rightEye, mouth;
    Rect mFaceRect;
    public FaceData[] mFaceArray;
    boolean _inFrame;            // Boolean to see if there is any faces in the frame
    int mSurfaceWidth;
    int mSurfaceHeight;
    int mRadius;
    int drawRadius;
    int cameraPreviewWidth;
    int cameraPreviewHeight;
    boolean mLandScapeMode;
    float scaleX = 1.0f;
    float scaleY = 1.0f;
    
    private int DEGREE_INTERVAL = 45;
    private int TARGET_RADIUS = 45;
    private int OFFSET = TARGET_RADIUS + TARGET_RADIUS/2;

    public GameDrawView(Context context, FaceData[] faceArray, boolean inFrame, int surfaceWidth, int surfaceHeight,
            Camera cameraObj, boolean landScapeMode) {
        super(context);

        setWillNotDraw(false);                    // This call is necessary, or else the draw method will not be called.
        mFaceArray = faceArray;
        _inFrame = inFrame;
        mSurfaceWidth = surfaceWidth;
        mSurfaceHeight = surfaceHeight;
        mRadius = (mSurfaceWidth / 2) - (OFFSET);
        
        
        mLandScapeMode = landScapeMode;
        if (cameraObj != null) {
            cameraPreviewWidth = cameraObj.getParameters().getPreviewSize().width;
            cameraPreviewHeight = cameraObj.getParameters().getPreviewSize().height;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (_inFrame)                // If the face detected is in frame.
        {
            for (int i = 0; i < mFaceArray.length; i++) {
                rectBrush.setColor(Color.YELLOW);
                rectBrush.setStyle(Paint.Style.FILL);
//                canvas.drawRect(mFaceArray[i].rect.left * scaleX, mFaceArray[i].rect.top * scaleY,
//                        mFaceArray[i].rect.right * scaleX, mFaceArray[i].rect.bottom * scaleY, rectBrush);
            }
        	double rad = (Math.PI)/180;
        	for (int i = 0; i <= 360; i += DEGREE_INTERVAL) {
	        	rectBrush.setColor(Color.GREEN);
				rectBrush.setColor(Color.argb(200, (int)(255*Math.cos(i)), 200, 0));
	        	rectBrush.setStyle(Paint.Style.FILL);
	        	canvas.drawCircle((float) (mSurfaceWidth/2 + (Math.cos(rad*i) * mRadius)), (float)(mSurfaceHeight/2 + (Math.sin(rad*i) * mRadius)), TARGET_RADIUS, rectBrush);
        	}
        	

        } else {
            canvas.drawColor(0, Mode.CLEAR);
        }
    }
}
