/*
 * Copyright 2013 Matteo Pergolesi (matpergo@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.matpergo.androcr;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.ImageView;

public class Cropper extends ImageView {
	
	private CropImage mContext;
	
	// Drawable used to draw cropper
	private ShapeDrawable mDrawable;
	
	// Drawable used to draw corners
	private Drawable[] cornerArray;
	
	// Original Bitmap dimensions
	private int origWidth, origHeight;
	
	// Scaled Bitmap dimensions
	//private int scaledWidth, scaledHeight;
	
	// Cropper dimensions
	private int mCropX, mCropY, mCropW, mCropH;
	
	// Scaled Image Bounds
	private int boundX, boundY, boundW, boundH;
	
	// Useful touch variables
	private boolean isInside = false, topLeft = false, topRight = false, bottomLeft = false, bottomRight = false;  
	
	// Touch gaps	
	private int mGap;
	
	// Strokes Width
	private int mStrokeW;
	
	private boolean mFirstStart = true;
	
	private AccessibilityManager mAccessibilityManager;
		
	//private final String TAG = "Cropper.java";	
	
	// Constructor
	public Cropper (Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = (CropImage)context;
        mAccessibilityManager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);        
    }
	
	@Override
	public void onDraw (Canvas canvas){
		super.onDraw(canvas);
		
		//Log.i(TAG, "ImageView dimensions: " + this.getWidth() + ", " + this.getHeight());
		
		// Setting initial cropper dimensions according to ImageView
		if (mFirstStart == true){				
			// Getting display density to adjust dimensions for each type of screen
			DisplayMetrics metrics = new DisplayMetrics();
			mContext.getWindowManager().getDefaultDisplay().getMetrics(metrics);
			//Log.d(TAG, "Density: "+metrics.densityDpi);
			if (metrics.densityDpi == DisplayMetrics.DENSITY_LOW){
				mGap = 32;
		    	mStrokeW = 3;
			}
			else if (metrics.densityDpi == DisplayMetrics.DENSITY_MEDIUM){
				mGap = 40;
		    	mStrokeW = 3;
			}
			else if (metrics.densityDpi == DisplayMetrics.DENSITY_HIGH || metrics.densityDpi == DisplayMetrics.DENSITY_TV){
				mGap = 56;
		    	mStrokeW = 5;
			}			
			else if (metrics.densityDpi >= 320){
				mGap = 64;
		    	mStrokeW = 7;
			}					
			defaultPosition();
			mFirstStart = false;
		}		
		// Drawing cropper
		mDrawable.draw(canvas);
		// Drawing corners on cropper
		for (int i = 0; i<4; i++){
			cornerArray[i].draw(canvas);
		}
	}
	
	public void defaultPosition(){
		setImgBox();
		setCropperDefault();
		initCropper();
		initCorners();			
		setNewLayout();
		//Log.w("Cropper", "DefaultPosition executed");
	}
	
	// Sets the box from where the cropper can't exit
	private void setImgBox(){	
		boundX = 0;
		boundW = this.getWidth();
		boundY = 0;
		boundH = this.getHeight();			
		
		origWidth = mContext.getOrigWidth();
		origHeight = mContext.getOrigHeight();
	}
	
	private void setCropperDefault(){
		mCropW = boundW / 2;		
		mCropH = boundH / 2;
		mCropX = boundX + boundW / 4;
		mCropY = boundY + boundH / 4;	
	}
	
	// Sets cropper default dimensions, according on the scaled image width and height
	private void initCropper(){	
		mDrawable = new ShapeDrawable(new RectShape());
        mDrawable.getPaint().setColor(0xFF0099CC);
        mDrawable.getPaint().setStyle(Paint.Style.STROKE);
        mDrawable.getPaint().setStrokeWidth(mStrokeW);      
	}
	
	// Sets corners
	private void initCorners(){
		cornerArray = new Drawable[4];
		for (int i = 0; i < 4; i++){
			cornerArray[i] = mContext.getResources().getDrawable(R.drawable.crop_corners);
		}
	}
	
	private void setNewLayout(){
		// Changing cropper position and dimensions
		mDrawable.setBounds(mCropX, mCropY, mCropX + mCropW, mCropY + mCropH);
		// Changing corners positions
		cornerArray[0].setBounds(mCropX - mGap/4, mCropY - mGap/4, mCropX + mGap/4 , mCropY + mGap/4);
		cornerArray[1].setBounds(mCropX + mCropW - mGap/4, mCropY - mGap/4, mCropX + mCropW + mGap/4 , mCropY + mGap/4);
		cornerArray[2].setBounds(mCropX - mGap/4, mCropY + mCropH - mGap/4, mCropX + mGap/4 , mCropY + mCropH + mGap/4);
		cornerArray[3].setBounds(mCropX + mCropW - mGap/4, mCropY + mCropH - mGap/4, mCropX + mCropW + mGap/4 , mCropY + mCropH + mGap/4);				
	}
	
	private void keepCropperInside(){
		// Checks if the cropper is out of scaled image margins
		if (mCropX < boundX)
			mCropX = boundX;
		
		if (mCropY < boundY)
			mCropY = boundY;
		
		if (mCropX + mCropW > boundW)
			mCropX = boundW - mCropW;
		
		if (mCropY + mCropH > boundH)
			mCropY = boundH - mCropH;
	}
	
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
	public boolean onHoverEvent(MotionEvent event) {
	    if (mAccessibilityManager.isTouchExplorationEnabled()) {	    	
	        return this.onTouchEvent(event);
	    } else {
	        return super.onHoverEvent(event);
	    }
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event){
		
		int touchX = (int)event.getX();
		int touchY = (int)event.getY();		
		
		int action = event.getAction();
		
		//Log.w(TAG, "Touch coordinates: " + event.getX() + ", " + event.getY());
		//Log.w(TAG, "Raw coordinates: " + event.getRawX() + ", " + event.getRawY());
		//Log.w(TAG, "Touch precision: " + event.getXPrecision() + ", " + event.getYPrecision());
		
		switch (action){
			
			case MotionEvent.ACTION_DOWN: case MotionEvent.ACTION_HOVER_ENTER:
				
				// Checking if touching inside cropper
				if(touchX >= mCropX + mGap && touchX <= mCropX + mCropW - mGap 
					&& touchY >= mCropY + mGap && touchY <= mCropY + mCropH - mGap)
					isInside = true;
				
				// Checking if touching on top-left corner
				else if(Math.abs(touchX - mCropX) < mGap/2 && Math.abs(touchY - mCropY) < mGap/2)
					topLeft = true;
				
				// Checking if touching on top-right corner
				else if(Math.abs(touchX - (mCropX + mCropW)) < mGap/2 && Math.abs(touchY - mCropY) < mGap/2)
					topRight = true;
				
				// Checking if touching on bottom-left corner
				else if(Math.abs(touchX - mCropX) < mGap/2 && Math.abs (touchY - (mCropY + mCropH)) < mGap/2)
					bottomLeft = true;
				
				// Checking if touching on bottom-right corner
				else if(Math.abs(touchX - (mCropX + mCropW)) < mGap/2 && Math.abs(touchY - (mCropY + mCropH)) < mGap/2)
					bottomRight = true;
				break;		
				
				
			case MotionEvent.ACTION_MOVE: case MotionEvent.ACTION_HOVER_MOVE:
				
				// If cropper is too little we block dimensions			
				if (mCropW < mGap/2 || mCropH < mGap/2){
					resetTouchVars();
					// Setting the smallest dimension to a good value
					if(mCropW < mGap/2)
						mCropW = mGap/2 + mStrokeW;
					else
						mCropH = mGap/2 + mStrokeW;					
				}
				else{
				
					if (isInside){
					
						//Log.i(TAG, "Dragging inside...");					
					
						// Centering the cropper on the touch position. No need to modify dimensions.
						mCropX = (int) event.getX() - mCropW/2;
						mCropY = (int) event.getY() - mCropH/2;
					
						// Checking if cropper goes out of view
						keepCropperInside();
					}				
				
					else if (topLeft == true && touchX >= boundX && touchY >= boundY){
										
						//Log.i(TAG, "Top Left Corner...");
					
						mCropW = mCropW - (touchX - mCropX);
						mCropH = mCropH - (touchY - mCropY);					
						mCropX = touchX;
						mCropY = touchY;					
					}
				
					else if (topRight == true && touchX <= boundW && touchY >= boundY){
					
						//Log.i(TAG, "Top Right Corner...");
					
						mCropW = mCropW + (touchX - mCropX - mCropW);
						mCropH = mCropH - (touchY - mCropY);
						//X is the same
						mCropY = touchY;					
					}
				
					else if (bottomLeft == true && touchX >= boundX && touchY <= boundH){
					
						//Log.i(TAG, "Bottom Left Corner...");
					
						mCropW = mCropW - (touchX - mCropX);
						mCropH = mCropH + (touchY - mCropY - mCropH);
						mCropX = touchX;
						mCropY = touchY - mCropH;
					}
					else if (bottomRight == true && touchX <= boundW && touchY <= boundH){
						
						//Log.i(TAG, "Bottom Right Corner...");
					
						mCropW = mCropW + (touchX - mCropX - mCropW);
						mCropH = mCropH + (touchY - mCropY - mCropH);
						//X is the same
						mCropY = touchY - mCropH;
					}									
				}
				setNewLayout();
				invalidate();
				//Log.w(TAG, "Cropper dimensions: " + mCropX + "," + mCropY + "," + mCropW + "," + mCropH);
				break;
				
			case MotionEvent.ACTION_UP: case MotionEvent.ACTION_HOVER_EXIT:
				resetTouchVars();
				break;
			
		}
		super.onTouchEvent(event);
		return true;
	}
	
	private void resetTouchVars(){
		// Reset touch variables
		isInside = false;
		topLeft = false;
		topRight = false;
		bottomLeft = false;
		bottomRight = false;
	}
	
	public void setFirstStart(boolean value){
		mFirstStart = value;
	}
	
	public int getCropWidth(){
		return mCropW * origWidth / boundW;
	}
	
	public int getCropHeight(){
		return mCropH * origHeight / boundH;
	}
	
	public int getCropX(){		
		return mCropX * origWidth / boundW;
	}
	
	public int getCropY(){
		return mCropY * origHeight / boundH;
	}
}
