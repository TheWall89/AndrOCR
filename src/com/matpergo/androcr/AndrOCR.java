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

import java.io.File;
import java.io.IOException;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class AndrOCR extends MainActivity implements MyDialogFragment.MyDialogListener{
	
	//private String mPath, mDataDir;
	//private final String mFileName = "OCRimage.jpg";
	private final int FROM_CAMERA = 0, FROM_GALLERY = 1, ABOUT_ID = 0, COPY_ERR_DIALOG = 1;
	//private Uri srcUri = null;
	private Button fromCamera, fromGallery;
	//private final String TAG = "AndrOCR.java";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);        
        setContentView(R.layout.androcr);
        
        fromCamera = (Button)findViewById(R.id.button_camera);
        fromGallery = (Button)findViewById(R.id.button_gallery);
        
        if(this.initPath() == false){
        	Toast.makeText(this, getString(R.string.sd_error), Toast.LENGTH_SHORT).show();
        	//If SD not available, disable buttons        	
        	fromCamera.setEnabled(false);        	
        	fromCamera.setBackgroundResource(R.drawable.holo_background_dark);
        	fromGallery.setEnabled(false);
        	fromGallery.setBackgroundResource(R.drawable.holo_background_dark);  
        }
        
        /*
        // The application needs to work with SD card. The code below checks if it is available.        
        if (Utilities.checkExternalStorage() == false){
        	Toast.makeText(this, getString(R.string.sd_error), Toast.LENGTH_SHORT).show();
        	//If SD not available, disable buttons        	
        	fromCamera.setEnabled(false);        	
        	fromCamera.setBackgroundResource(R.drawable.holo_background_dark);
        	fromGallery.setEnabled(false);
        	fromGallery.setBackgroundResource(R.drawable.holo_background_dark);        	
        }
        
        else{
        	mDataDir = this.getExternalFilesDir("data").getAbsolutePath();
        	//Log.i("This", mDataDir);
        	mPath = this.getExternalFilesDir("images").getAbsolutePath() + File.separator + mFileName;
        	//Log.i(TAG, mPath);
        	//Toast.makeText(this, "Picture Directory: " + mPath, Toast.LENGTH_LONG).show();
        }
        */
        
        // Checking for camera
        PackageManager pm = this.getPackageManager();
        if (!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
        	fromCamera.setEnabled(false);
        	fromCamera.setBackgroundResource(R.drawable.holo_background_dark);
        }

        
    }      
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        boolean cam = fromCamera.isEnabled();
        boolean gal = fromGallery.isEnabled();
    	super.onConfigurationChanged(newConfig);        
        setContentView(R.layout.androcr);
        fromCamera = (Button)findViewById(R.id.button_camera);
        fromGallery = (Button)findViewById(R.id.button_gallery);
        if (cam == false){
        	fromCamera.setEnabled(false);        	
        	fromCamera.setBackgroundResource(R.drawable.holo_background_dark);
        }
        if (gal == false){
        	fromGallery.setEnabled(false);
        	fromGallery.setBackgroundResource(R.drawable.holo_background_dark);
        }
    }
    
    //Method to be called when "From Camera" button is clicked
    public void getImageFromCamera(View v){
    	Uri targetUri = Uri.fromFile(new File(mPath));
    	Intent intent = new Intent (MediaStore.ACTION_IMAGE_CAPTURE);    	
    	intent.putExtra(MediaStore.EXTRA_OUTPUT, targetUri);
    	startActivityForResult(intent, FROM_CAMERA);
    }
    
    //Method to be called when "From Gallery" button is clicked
    public void getImageFromGallery(View v){
    	Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_PICK);		
		startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture)), FROM_GALLERY);
    }
    
    //This method runs when the Camera Activity or the Gallery Activity are ended. 
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
    	
    	//Log.i(TAG, "Request Code: "+requestCode);
    	//Log.i(TAG, "Result Code: "+resultCode);   	
    	
    	if (requestCode == FROM_CAMERA){
    		
        	switch (resultCode){
        	
        	case 0:
            	Toast.makeText(this, getString(R.string.camera_error), Toast.LENGTH_SHORT).show();
            	break;
            	
        	case -1:
        		//Image from camera is already stored in AndrOCR path
        		Intent intent = new Intent (this, CropImage.class);
        		//intent.putExtra("PATH", mPath);
            	//intent.putExtra("DATA", mDataDir);
            	startActivity(intent);  		
                                       		
        	}
    	} 
    	
    	if (requestCode == FROM_GALLERY) {
    		
    		//Result from gallery
    		switch (resultCode){
    		
        	case 0:
            	Toast.makeText(this, getString(R.string.gallery_error), Toast.LENGTH_SHORT).show();
            	break;
            	
        	case -1:        		
        		//Copying the image from gallery to the application path.        		            	
        		Uri srcUri = data.getData();
        		String srcPath = Utilities.getPathFromUri(srcUri, this);
        		try {
					Utilities.copyFile(new File(srcPath), new File(mPath));
				} catch (IOException e) {
					showDialogFragment(COPY_ERR_DIALOG, "copy_err_dialog");					
					e.printStackTrace();
				}
        		Intent intent = new Intent (this, CropImage.class);
        		//intent.putExtra("PATH", mPath);
            	//intent.putExtra("DATA", mDataDir);
            	startActivity(intent);        		
        	}
    	}   	
    	
    }
    
    /*
    ----------------------------- DIALOGS MANAGEMENT -----------------------------
    */
    
    public void onDialogPositiveClick(int dialog_id){
    	//Not needed
    }
    public void onDialogNegativeClick(int dialog_id){
    	//Not Needed
    }
    public void onDialogNeutralClick(int dialog_id){
    	switch(dialog_id){    	
    		case ABOUT_ID: 
    			removeDialogFragment("about_id");
    			break;
    		case COPY_ERR_DIALOG: 
    			removeDialogFragment("copy_err_dialog");
    			break;
    	}    	
    }
    public void onDialogSingleChoice(int dialog_id, int item){
    	//Not needed
    }
    
    /*
    ----------------------------- LIFE CYCLE METHODS -----------------------------
    */
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //super.onSaveInstanceState(outState);        
    }
    
    @Override
    protected void onPause() {
        super.onPause();        
    }
    
    @Override
    protected void onResume() {
        super.onResume();        
    }
    
}