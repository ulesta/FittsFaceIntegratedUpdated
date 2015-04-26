/*
 * ======================================================================
 * Copyright � 2014 Qualcomm Technologies, Inc. All Rights Reserved.
 * QTI Proprietary and Confidential.
 * =====================================================================
 * @file: DrawView.java
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
import android.util.Log;
import android.view.SurfaceView;

import ca.yorku.cse.mack.fittstouch.ExperimentPanel;

import com.qualcomm.snapdragon.sdk.face.FaceData;

public class DrawView extends SurfaceView {

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
    int cameraPreviewWidth;
    int cameraPreviewHeight;
    boolean mLandScapeMode;
    float scaleX = 1.0f;
    float scaleY = 1.0f;
    
    int mRadius;
    int drawRadius;
    
    int pointerX;
    int pointerY;
    
    int sampleCount = 0;
    float lastNoseTipSampleX;
    float lastNoseTipSampleY;
    float lastResultNoseTipX;
    float lastResultNoseTipY;
    
    // Constant scale factor offset for nose tip
    float NS_SCALE_X = 3.8f;
    float NS_SCALE_Y = 3.8f;
    
    private int DEGREE_INTERVAL = 45;
    private int TARGET_RADIUS = 70;
    private int OFFSET = TARGET_RADIUS + TARGET_RADIUS/2;
    
    float[] runningArrayX;
    float[] runningArrayY;
    int counter = 0;
    float xTotal = 0;
    float yTotal = 0;
    float avgX = 0;
    float avgY = 0;
    
    public int RUNNING_ARR_LEN = 5;
    
    static final float ALPHA = 0.001f;
    
    private static DrawView instance = null;
    
    ExperimentPanel epRef = null;
    
    String mNavType;

    private DrawView(Context context, FaceData[] faceArray, boolean inFrame, int surfaceWidth, int surfaceHeight,
            Camera cameraObj, boolean landScapeMode, ExperimentPanel ep, String navigationType) {
        super(context);
        Log.d("const", "New one drawn!");
        setWillNotDraw(false);                    // This call is necessary, or else the draw method will not be called.
        mFaceArray = faceArray;
        _inFrame = inFrame;
        mSurfaceWidth = surfaceWidth;
        mSurfaceHeight = surfaceHeight;
        mLandScapeMode = landScapeMode;
        
        pointerX = mSurfaceWidth/2;
        pointerY = mSurfaceHeight/2;
        
        mRadius = (mSurfaceWidth / 2) - (OFFSET);
        
        epRef = ep;
        mNavType = navigationType;
        
        if (cameraObj != null) {
            cameraPreviewWidth = cameraObj.getParameters().getPreviewSize().width;
            cameraPreviewHeight = cameraObj.getParameters().getPreviewSize().height;
        }
        
        runningArrayX = new float[RUNNING_ARR_LEN];
        runningArrayY = new float[RUNNING_ARR_LEN];
        
        for (int i = 0; i <= runningArrayX.length - 1; i++) {
        	runningArrayX[i] = 0;
        	runningArrayY[i] = 0;
        }
        
    }
    
    public static DrawView getInstance(Context context, FaceData[] faceArray, boolean inFrame, int surfaceWidth, int surfaceHeight,
            Camera cameraObj, boolean landScapeMode, ExperimentPanel ep, String navigationType) {
    	if (instance == null) {
    		instance = new DrawView(context, faceArray, inFrame, surfaceWidth, surfaceHeight, cameraObj, landScapeMode, ep, navigationType);
    	} else {
    		instance.reinit(context, faceArray, inFrame, surfaceWidth, surfaceHeight, cameraObj, landScapeMode, ep, navigationType);
    	}
    	return instance;
    }
    
    public void reinit(Context context, FaceData[] faceArray, boolean inFrame, int surfaceWidth, int surfaceHeight,
            Camera cameraObj, boolean landScapeMode, ExperimentPanel ep, String navigationType) {
    	mFaceArray = faceArray;
    	
    	mSurfaceWidth = surfaceWidth;
        mSurfaceHeight = surfaceHeight;
        mLandScapeMode = landScapeMode;
        
//        sampleCount = 0;
        
        pointerX = mSurfaceWidth/2;
        pointerY = mSurfaceHeight/2;
        
        mRadius = (mSurfaceWidth / 2) - (OFFSET);
        
        epRef = ep;
        mNavType = navigationType;
        
        if (cameraObj != null) {
            cameraPreviewWidth = cameraObj.getParameters().getPreviewSize().width;
            cameraPreviewHeight = cameraObj.getParameters().getPreviewSize().height;
        }
    	
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (_inFrame && mFaceArray != null)                // If the face detected is in frame.
        {
        	leftEyeBrush.setColor(Color.WHITE);
            /*canvas.drawCircle(pointerX, pointerY, 20f,
                    leftEyeBrush);*/

            for (int i = 0; i < mFaceArray.length; i++) {
                if (mFaceArray[i].leftEye != null) {
                    leftEyeBrush.setColor(Color.RED);
                    canvas.drawCircle(mFaceArray[i].leftEye.x * scaleX, mFaceArray[i].leftEye.y * scaleY, 5f,
                            leftEyeBrush);

                    rightEyeBrush.setColor(Color.GREEN);
                    canvas.drawCircle(mFaceArray[i].rightEye.x * scaleX, mFaceArray[i].rightEye.y * scaleY, 5f,
                            rightEyeBrush);

                    mouthBrush.setColor(Color.WHITE);
                    canvas.drawCircle(mFaceArray[i].mouth.x * scaleX, mFaceArray[i].mouth.y * scaleY, 5f, mouthBrush);
                }
                if (mFaceArray[i].leftEyeObj != null) {
                    mouthBrush.setColor(Color.CYAN);
                    canvas.drawCircle(mFaceArray[i].mouthObj.left.x, mFaceArray[i].mouthObj.left.y, 5f, mouthBrush);
                    canvas.drawCircle(mFaceArray[i].mouthObj.right.x, mFaceArray[i].mouthObj.right.y, 5f, mouthBrush);
                    canvas.drawCircle(mFaceArray[i].mouthObj.upperLipTop.x, mFaceArray[i].mouthObj.upperLipTop.y, 5f,
                            mouthBrush);
                    canvas.drawCircle(mFaceArray[i].mouthObj.upperLipBottom.x, mFaceArray[i].mouthObj.upperLipBottom.y,
                            5f, mouthBrush);
                    canvas.drawCircle(mFaceArray[i].mouthObj.lowerLipTop.x, mFaceArray[i].mouthObj.lowerLipTop.y, 5f,
                            mouthBrush);
                    canvas.drawCircle(mFaceArray[i].mouthObj.lowerLipBottom.x, mFaceArray[i].mouthObj.lowerLipBottom.y,
                            5f, mouthBrush);

                    canvas.drawCircle(mFaceArray[i].leftEyebrow.left.x, mFaceArray[i].leftEyebrow.left.y, 5f,
                            mouthBrush);
                    canvas.drawCircle(mFaceArray[i].leftEyebrow.right.x, mFaceArray[i].leftEyebrow.right.y, 5f,
                            mouthBrush);
                    canvas.drawCircle(mFaceArray[i].leftEyebrow.top.x, mFaceArray[i].leftEyebrow.top.y, 5f, mouthBrush);
                    canvas.drawCircle(mFaceArray[i].leftEyebrow.bottom.x, mFaceArray[i].leftEyebrow.bottom.y, 5f,
                            mouthBrush);

                    canvas.drawCircle(mFaceArray[i].rightEyebrow.left.x, mFaceArray[i].rightEyebrow.left.y, 5f,
                            mouthBrush);
                    canvas.drawCircle(mFaceArray[i].rightEyebrow.right.x, mFaceArray[i].rightEyebrow.right.y, 5f,
                            mouthBrush);
                    canvas.drawCircle(mFaceArray[i].rightEyebrow.top.x, mFaceArray[i].rightEyebrow.top.y, 5f,
                            mouthBrush);
                    canvas.drawCircle(mFaceArray[i].rightEyebrow.bottom.x, mFaceArray[i].rightEyebrow.bottom.y, 5f,
                            mouthBrush);

                    canvas.drawCircle(mFaceArray[i].leftEar.top.x * scaleX, mFaceArray[i].leftEar.top.y * scaleY, 5f,
                            mouthBrush);
                    canvas.drawCircle(mFaceArray[i].leftEar.bottom.x * scaleX, mFaceArray[i].leftEar.bottom.y * scaleY,
                            5f, mouthBrush);
                    canvas.drawCircle(mFaceArray[i].rightEar.top.x * scaleX, mFaceArray[i].rightEar.top.y * scaleY, 5f,
                            mouthBrush);
                    canvas.drawCircle(mFaceArray[i].rightEar.bottom.x * scaleX, mFaceArray[i].rightEar.bottom.y
                            * scaleY, 5f, mouthBrush);

                    canvas.drawCircle(mFaceArray[i].leftEyeObj.left.x * scaleX, mFaceArray[i].leftEyeObj.left.y
                            * scaleY, 5f, mouthBrush);
                    canvas.drawCircle(mFaceArray[i].leftEyeObj.right.x * scaleX, mFaceArray[i].leftEyeObj.right.y
                            * scaleY, 5f, mouthBrush);
                    canvas.drawCircle(mFaceArray[i].leftEyeObj.top.x * scaleX, mFaceArray[i].leftEyeObj.top.y * scaleY,
                            5f, mouthBrush);
                    canvas.drawCircle(mFaceArray[i].leftEyeObj.bottom.x * scaleX, mFaceArray[i].leftEyeObj.bottom.y
                            * scaleY, 5f, mouthBrush);
                    canvas.drawCircle(mFaceArray[i].leftEyeObj.centerPupil.x * scaleX,
                            mFaceArray[i].leftEyeObj.centerPupil.y * scaleY, 5f, mouthBrush);

                    canvas.drawCircle(mFaceArray[i].rightEyeObj.left.x * scaleX, mFaceArray[i].rightEyeObj.left.y
                            * scaleY, 5f, mouthBrush);
                    canvas.drawCircle(mFaceArray[i].rightEyeObj.right.x * scaleX, mFaceArray[i].rightEyeObj.right.y
                            * scaleY, 5f, mouthBrush);
                    canvas.drawCircle(mFaceArray[i].rightEyeObj.top.x * scaleX, mFaceArray[i].rightEyeObj.top.y
                            * scaleY, 5f, mouthBrush);
                    canvas.drawCircle(mFaceArray[i].rightEyeObj.bottom.x * scaleX, mFaceArray[i].rightEyeObj.bottom.y
                            * scaleY, 5f, mouthBrush);
                    canvas.drawCircle(mFaceArray[i].rightEyeObj.centerPupil.x * scaleX,
                            mFaceArray[i].rightEyeObj.centerPupil.y * scaleY, 5f, mouthBrush);

                    canvas.drawCircle(mFaceArray[i].chin.left.x * scaleX, mFaceArray[i].chin.left.y * scaleY, 5f,
                            mouthBrush);
                    canvas.drawCircle(mFaceArray[i].chin.right.x * scaleX, mFaceArray[i].chin.right.y * scaleY, 5f,
                            mouthBrush);
                    canvas.drawCircle(mFaceArray[i].chin.center.x * scaleX, mFaceArray[i].chin.center.y * scaleY, 5f,
                            mouthBrush);

                    canvas.drawCircle(mFaceArray[i].nose.noseBridge.x * scaleX, mFaceArray[i].nose.noseBridge.y
                            * scaleY, 5f, mouthBrush);
                    canvas.drawCircle(mFaceArray[i].nose.noseCenter.x * scaleX, mFaceArray[i].nose.noseCenter.y
                            * scaleY, 5f, mouthBrush);
                    canvas.drawCircle(mFaceArray[i].nose.noseTip.x * scaleX, mFaceArray[i].nose.noseTip.y * scaleY, 5f,
                            mouthBrush);
                    
                    /** ------- [START - NOSE TIP COORD PROCEDURES] ------- */
                    // Change scale offset
                    canvas.drawCircle((mFaceArray[i].nose.noseTip.x * scaleX), (mFaceArray[i].nose.noseTip.y * scaleY), 15f,
                            mouthBrush);
                    canvas.drawLine((mFaceArray[i].nose.noseTip.x), (mFaceArray[i].nose.noseTip.y), 
                    		mFaceArray[i].nose.noseTip.x * scaleX, mFaceArray[i].nose.noseTip.y * scaleY, mouthBrush);
                    
                    float noseTipX = mFaceArray[i].nose.noseTip.x * scaleX;
                    float noseTipY = mFaceArray[i].nose.noseTip.y * scaleY;
                    
                    lastNoseTipSampleX = noseTipX;
                    lastNoseTipSampleY = noseTipY;
                    
                    Log.d("Nose", "(" + noseTipX + ", " + noseTipY + ")\t" + "Sample: " + sampleCount);
                    
                    float noseTipXnormalized = noseTipX - mSurfaceWidth/2;
                    float noseTipYnormalized = noseTipY - mSurfaceHeight/2;
                    
                    // apply scale factor
                    float noseTipXscaled = noseTipXnormalized * NS_SCALE_X;
                    float noseTipYscaled = noseTipYnormalized * NS_SCALE_Y;
                    
                    float resultNoseTipX = mSurfaceWidth/2 + noseTipXscaled;
                    float resultNoseTipY = mSurfaceHeight/2 + noseTipYscaled;
                    
                    runningArrayX[sampleCount] = resultNoseTipX;
                    runningArrayY[sampleCount] = resultNoseTipY;
                    
                    sampleCount = (sampleCount + 1) % RUNNING_ARR_LEN;
                    
                    resultNoseTipX = computeRunningAverage(runningArrayX);
                    resultNoseTipY = computeRunningAverage(runningArrayY);
                   
                    lastResultNoseTipX = resultNoseTipX;
                    lastResultNoseTipY = resultNoseTipY;
                    
                    mouthBrush.setColor(Color.MAGENTA);
                    /*canvas.drawCircle(resultNoseTipX, resultNoseTipY, 20f, mouthBrush);*/
                    
                    if (epRef != null && mNavType.equals("Positional")) {
                    	epRef.placeCursor(resultNoseTipX, resultNoseTipY);
                    }
                    
                    /** ------- [END - NOSE TIP COORD PROCEDURES] ------- */
                    
                    mouthBrush.setColor(Color.CYAN);
                    canvas.drawCircle(mFaceArray[i].nose.noseLowerLeft.x * scaleX, mFaceArray[i].nose.noseLowerLeft.y
                            * scaleY, 5f, mouthBrush);
                    canvas.drawCircle(mFaceArray[i].nose.noseLowerRight.x * scaleX, mFaceArray[i].nose.noseLowerRight.y
                            * scaleY, 5f, mouthBrush);
                    canvas.drawCircle(mFaceArray[i].nose.noseMiddleLeft.x * scaleX, mFaceArray[i].nose.noseMiddleLeft.y
                            * scaleY, 5f, mouthBrush);
                    canvas.drawCircle(mFaceArray[i].nose.noseMiddleRight.x * scaleX,
                            mFaceArray[i].nose.noseMiddleRight.y * scaleY, 5f, mouthBrush);
                    canvas.drawCircle(mFaceArray[i].nose.noseUpperLeft.x * scaleX, mFaceArray[i].nose.noseUpperLeft.y
                            * scaleY, 5f, mouthBrush);
                    canvas.drawCircle(mFaceArray[i].nose.noseUpperRight.x * scaleX, mFaceArray[i].nose.noseUpperRight.y
                            * scaleY, 5f, mouthBrush);
                }
                rectBrush.setColor(Color.YELLOW);
                rectBrush.setStyle(Paint.Style.STROKE);
                canvas.drawRect(mFaceArray[i].rect.left * scaleX, mFaceArray[i].rect.top * scaleY,
                        mFaceArray[i].rect.right * scaleX, mFaceArray[i].rect.bottom * scaleY, rectBrush);
            }
        } else {
            canvas.drawColor(0, Mode.CLEAR);
        }
    }
    
	private float computeRunningAverage(float[] arr) {
    	float sum = 0.0f;
		for (int i = 0; i < arr.length; i++) {
			sum += arr[i];
		}
		return sum/arr.length;
	}

	public void setPointer( double d, double e) {
    	this.pointerX = (int) ((mSurfaceWidth/2) + d);
    	this.pointerY = (int) ((mSurfaceHeight/2) + e);
    	Log.d("nav", "Reached 1/2");
    	Log.d("nav", "navType=" + mNavType);
    	if (epRef != null && mNavType.equals("Rotational")) {
    		Log.d("nav", "Reached 2/2");
    		epRef.placeCursor(pointerX, pointerY);
    	}
    }
    
    /**
     * @see http://en.wikipedia.org/wiki/Low-pass_filter#Algorithmic_implementation
     * @see http://en.wikipedia.org/wiki/Low-pass_filter#Simple_infinite_impulse_response_filter
     */
    protected float lowPass( float input, float output ) {
        if ( output == 0 ) return input;
            
        output = output + ALPHA * (input - output);
        return output;
    }
}
