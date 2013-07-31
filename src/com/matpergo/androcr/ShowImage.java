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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

public class ShowImage extends MainActivity implements MyDialogFragment.MyDialogListener {
	
	private ImageView mImageView;
	private Bitmap mBitmap;
	private ProgressDialog mProgressDialog;
	private String mTrainedDataPath, mLang;
	private int mLangID;
	private int mSegMode, mSegModeID;
	private Activity mThis;
	private TessBaseAPI mBaseApi;
	private final int ABOUT_ID = 0, SEGMODE_DIALOG = 2, LANGUAGE_DIALOG = 3, LOADING_DIALOG = 4, NETWORK_ERR_DIALOG = 5, OCR_ERR_DIALOG = 6, EXTRACT_ERR_DIALOG = 7, CONTINUE_DIALOG = 8, SD_ERR_DIALOG = 9, PROGRESS_DIALOG = 10;
	
	private final String[] mSegModeArray = {AndrOCRApp.myGetString(R.string.PSM_AUTO), AndrOCRApp.myGetString(R.string.PSM_SINGLE_BLOCK), AndrOCRApp.myGetString(R.string.PSM_SINGLE_LINE), AndrOCRApp.myGetString(R.string.PSM_SINGLE_WORD),
			AndrOCRApp.myGetString(R.string.PSM_SINGLE_CHAR), AndrOCRApp.myGetString(R.string.PSM_SINGLE_BLOCK_VERT_TEXT)};
	
	private final String[] mLangArray = {AndrOCRApp.myGetString(R.string.english), AndrOCRApp.myGetString(R.string.bulgarian), AndrOCRApp.myGetString(R.string.catalan), 
										AndrOCRApp.myGetString(R.string.chinese), AndrOCRApp.myGetString(R.string.chinese_tra), AndrOCRApp.myGetString(R.string.czech), 
										AndrOCRApp.myGetString(R.string.danish), AndrOCRApp.myGetString(R.string.danish_frak), AndrOCRApp.myGetString(R.string.dutch),
										AndrOCRApp.myGetString(R.string.german), AndrOCRApp.myGetString(R.string.german_frak), AndrOCRApp.myGetString(R.string.greek),
										AndrOCRApp.myGetString(R.string.finnish), AndrOCRApp.myGetString(R.string.french), AndrOCRApp.myGetString(R.string.hebrew), AndrOCRApp.myGetString(R.string.hungarian), 
										AndrOCRApp.myGetString(R.string.indonesian), AndrOCRApp.myGetString(R.string.italian), AndrOCRApp.myGetString(R.string.japanese),
										AndrOCRApp.myGetString(R.string.korean), AndrOCRApp.myGetString(R.string.latvian), AndrOCRApp.myGetString(R.string.lithuanian),
										AndrOCRApp.myGetString(R.string.norwegian), AndrOCRApp.myGetString(R.string.polish), 
										AndrOCRApp.myGetString(R.string.portuguese), AndrOCRApp.myGetString(R.string.romanian), AndrOCRApp.myGetString(R.string.russian),
										AndrOCRApp.myGetString(R.string.serbian), AndrOCRApp.myGetString(R.string.slovakian), AndrOCRApp.myGetString(R.string.slovenian),
										AndrOCRApp.myGetString(R.string.spanish), AndrOCRApp.myGetString(R.string.swedish), AndrOCRApp.myGetString(R.string.swedish_frak),
										AndrOCRApp.myGetString(R.string.tagalog), AndrOCRApp.myGetString(R.string.turkish), AndrOCRApp.myGetString(R.string.ukrainian),
										AndrOCRApp.myGetString(R.string.vietnamese)};
	
	private final String [] mTessLangArray = {"eng", "bul", "cat", "chi_sim", "chi_tra", "ces",  "dan", "dan-frak", "nld", "deu", "deu-frak", "ell", "fin", "fra", "heb", "hun", 
												"ind", "ita", "jpn", "kor", "lav", "lit", "nor", "pol", "por", "ron", "rus", "srp", "slk", "slv", "spa", "swe", "swe-frak",
												"tgl", "tur", "ukr", "vie"};
	
	private final String [] mBingLangCodeArray = {"en", "bg", "ca", "zh-CHS", "zh-CHT", "cs", "da", "da", "nl", "de", "de", "el", "fi", "fr", "he", "hu", "id", "it", "ja", "ko", "lv", "lt",  
													"no", "pl", "pt", "ro", "rus", "sr", "sk", "sl", "es", "sv", "sv", "tgl", "tr", "uk", "vi"};
	
	private String recognizedText = "";
	
	private DownloadExtractFile def;
	
	//private final String TAG = "ShowImage.java";

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	//Log.w(TAG, "onCreate");
        super.onCreate(savedInstanceState);        
        setContentView(R.layout.showimage);
        
        // Keeping a variable referencing the activity
        mThis=this;
        
        // Getting the path of the image to show        
        //Bundle extras = this.getIntent().getExtras();        
        //mPath = extras.getString("PATH");
        //mDataDir = extras.getString("DATA");
        if(this.initPath() == true){
        	mTrainedDataPath = mDataDir + File.separator + "tessdata";
                
        	//Log.i(TAG, mTrainedDataPath);
        
        	// Creating a bitmap from the image path 
        	//Log.w(TAG, "Sample Size: " + Utilities.calculateSampleSize(mPath));
        	mBitmap = Utilities.createBitmapFromPath(mPath, Utilities.calculateSampleSize(mPath));
        
        	// Restore preferences (last used language and segmentation mode)        
        	restorePreferences();
        
        	// Copying Language file
        	setTessData();
        
        	// Showing the selected PSM
        	Toast.makeText(mThis, getString(R.string.selected_text_layout) + " " + mSegModeArray[mSegModeID], Toast.LENGTH_SHORT).show();
        
        	// Setting up the UI
        	mImageView = (ImageView) findViewById(R.id.imageView);        
        	mImageView.setImageBitmap(mBitmap);
        }
        else{
        	//Show error dialog
        	showDialogFragment(SD_ERR_DIALOG, "sd_err_dialog");
        }
      
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);        
        //Do something?                                                                                    
    }

    // Downloading and copying language data to data folder
    private void setTessData(){
    	
    	// Checking if language file already exist inside data folder
    	File dir = new File(mTrainedDataPath);
		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				showDialogFragment(SD_ERR_DIALOG, "sd_err_dialog");
				return;
			} else {				
			}
		}
    	
    	if (!(new File(mTrainedDataPath + File.separator + mLang + ".traineddata")).exists()) {
        	
    		// If English or Hebrew, we just copy the file from assets
    		if (mLang.equals("eng") || mLang.equals("heb")){
    			try {
    				AssetManager assetManager = getAssets();   				
    				InputStream in = assetManager.open(mLang + ".traineddata");				
    				OutputStream out = new FileOutputStream(mTrainedDataPath + File.separator + mLang + ".traineddata");				
    				Utilities.copyFile(in, out);
    				Toast.makeText(mThis, getString(R.string.selected_language) + " " + mLangArray[mLangID], Toast.LENGTH_SHORT).show();
    				//Log.v(TAG, "Copied " + mLang + " traineddata");
    			} catch (IOException e) {
    				showDialogFragment(SD_ERR_DIALOG, "sd_err_dialog");
    			}
    		}
    		
    		else{
    		
    			// Checking if Network is available
    			if (!Utilities.isNetworkAvailable(this)){    			
    				showDialogFragment(NETWORK_ERR_DIALOG, "network_err_dialog");    				
    			}
    			else {    				
    				// Shows a dialog with File dimension. When user click on OK download starts. If he press Cancel revert to english language (like NETWORK ERROR)
    				showDialogFragment(CONTINUE_DIALOG, "continue_dialog");    				
    			}
    		}		
        }
    	
    	else {
    		Toast.makeText(mThis, getString(R.string.selected_language) + " " + mLangArray[mLangID], Toast.LENGTH_SHORT).show();
    	}
    }
    
    //Library initialization
    public void initTess(){    	
    	if (mBaseApi != null)
    		mBaseApi.end();    	
    	
    	mBaseApi = new TessBaseAPI();
		mBaseApi.setDebug(false);
		mBaseApi.setPageSegMode(mSegMode);
		mBaseApi.init(mDataDir + File.separator, mLang, TessBaseAPI.OEM_TESSERACT_ONLY);
		//Log.w(TAG, "initTess executed: " + mSegMode + "," + mLang);
    }
    
    public void changeLanguage(View v){    	
    	showDialogFragment(LANGUAGE_DIALOG, "language_dialog");    	
    }
    
    public void changeSegMode(View v){    	
    	showDialogFragment(SEGMODE_DIALOG, "segmode_dialog");
    }
    
    public void startOCR(View v){   	
    	// Start the OCR on the image. Shows the text in a new Activity.
    	new OCR().execute("");    		
    }
    
    public void onCancel(View v){
    	cleanMemory();
    	this.finish();
    }
    
    private void showText(){
    	//Log.i(TAG, "ShowText");    	
    	Intent intent = new Intent(this, UseText.class);
    	intent.putExtra("TEXT", recognizedText);
    	intent.putExtra("LANG", mBingLangCodeArray[mLangID]);    	
    	startActivity(intent);
    }
    
    public void rotateImageRight(View v){    	
    	// Rotating the image   		
		mBitmap = Utilities.rotateBitmap(mBitmap, 90);
		
		// Setting the new bitmap in the imageView
		mImageView.setImageBitmap(mBitmap);
    }
    
    public void rotateImageLeft(View v){
    	// Rotating the image   		
		mBitmap = Utilities.rotateBitmap(mBitmap, -90);
		
		// Setting the new bitmap in the imageView
		mImageView.setImageBitmap(mBitmap);
    }
    
    private void cleanMemory(){
    	// Unbinding drawable from ImageView
    	if(mImageView.getDrawable() != null)
    		mImageView.getDrawable().setCallback(null);
    	
    	// Telling DVM to free memory
    	if(mBitmap != null){    		
    		mBitmap.recycle();
    		mBitmap = null;
    		System.gc();
    	}   	
    }
    
    /*
    ----------------------------- DIALOGS MANAGEMENT -----------------------------
    */
    
    public void onDialogPositiveClick(int dialog_id){
    	switch(dialog_id){
    	
    		case CONTINUE_DIALOG:
    			removeDialogFragment("continue_dialog");
				def = new DownloadExtractFile();
				// Download the file from website	    				
				def.execute("http://tesseract-ocr.googlecode.com/files/" + mLang + ".traineddata.gz");
    			break;
    	}
    }
    public void onDialogNegativeClick(int dialog_id){
    	switch(dialog_id){
    	
			case CONTINUE_DIALOG:								
				mLangID = 0;
            	mLang = mTessLangArray[0];
            	removeDialogFragment("continue_dialog");
            	Toast.makeText(mThis, getString(R.string.selected_language) + " " + mLangArray[mLangID], Toast.LENGTH_SHORT).show();
				break;
		}
    }
    public void onDialogNeutralClick(int dialog_id){
    	switch(dialog_id){
    	
    		case ABOUT_ID:
    			removeDialogFragment("about_id");
    			break;
    		
    		case NETWORK_ERR_DIALOG:    			                	
            	mLangID = 0;
            	mLang = mTessLangArray[0];
            	removeDialogFragment("network_err_dialog");
            	Toast.makeText(mThis, getString(R.string.selected_language) + " " + mLangArray[mLangID], Toast.LENGTH_SHORT).show();
            	break;
    		
    		case OCR_ERR_DIALOG:
    			removeDialogFragment("ocr_err_dialog");
    			break;
    		
    		case EXTRACT_ERR_DIALOG:
    			// Resetting language to default
            	mLangID = 0;
            	mLang = mTessLangArray[0];
            	removeDialogFragment("extract_err_dialog");
            	Toast.makeText(mThis, getString(R.string.selected_language) + " " + mLangArray[mLangID], Toast.LENGTH_SHORT).show();
            	break;
    		
    		case PROGRESS_DIALOG:
    			def.cancel(true);								                	
            	mLangID = 0;
            	mLang = mTessLangArray[0];
				Toast.makeText(mThis, getString(R.string.selected_language) + " " + mLangArray[mLangID], Toast.LENGTH_SHORT).show();
				break;
				
    		case SD_ERR_DIALOG:
    			removeDialogFragment("sd_err_dialog");
                this.finish();
                break;
    	}    	
    }
    public void onDialogSingleChoice(int dialog_id, int item){
    	switch(dialog_id){
    	
    		case SEGMODE_DIALOG:
    			mSegModeID = item;
    	        // Change the OCR page segmentation mode
    	    	switch (mSegModeID){
    	    	case 0:
    	    		mSegMode = TessBaseAPI.PSM_AUTO;        	    		
    	    		break;
    	    	case 1:    		
    	    		mSegMode = TessBaseAPI.PSM_SINGLE_BLOCK;        	    		
    	    		break;
    	    	case 2:
    	    		mSegMode = TessBaseAPI.PSM_SINGLE_LINE;        	    		
    	    		break;
    	    	case 3:
    	    		mSegMode = TessBaseAPI.PSM_SINGLE_WORD;        	    		
    	    		break;
    	    	case 4:
    	    		mSegMode = TessBaseAPI.PSM_SINGLE_CHAR;        	    		
    	    		break;
    	    	case 5:
    	    		mSegMode = TessBaseAPI.PSM_SINGLE_BLOCK_VERT_TEXT;        	    		
    	    		break;        	    	
    	    	}        	    	
    	    	// It's not needed to restart the whole library here
    	    	removeDialogFragment("segmode_dialog");
    	    	Toast.makeText(mThis, getString(R.string.selected_text_layout) + " " + mSegModeArray[mSegModeID], Toast.LENGTH_SHORT).show();
    	    	break;
    	    	
    		case LANGUAGE_DIALOG:
    			mLangID = item;
    	        // Change the OCR language recognition
    	        mLang = mTessLangArray[mLangID];
    	        removeDialogFragment("language_dialog");
    	    	setTessData();    	    	
    	    	break;
    	}
    }
    
    public String[] getSegModeArray(){
    	return mSegModeArray;
    }
    
    public int getSegmodeID(){
    	return mSegModeID;
    }
    
    public String[] getLangArray(){
    	return mLangArray;
    }
    
    public int getLangID(){
    	return mLangID;
    }
    /*
    ----------------------------- SAVE/RESTORE PREFERENCES -----------------------------
    */
    
    private void savePreferences(){
    	// We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();             
        editor.putString("language", mLang);
        editor.putInt("langID", mLangID);
        editor.putInt("segmode", mSegMode);
        editor.putInt("segmodeID", mSegModeID);
        // Commit the edits!
        editor.commit();      	
    }
    
    private void restorePreferences(){
    	SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        mLang = settings.getString("language", "eng");
        mLangID = settings.getInt("langID", 0);
        mSegMode = settings.getInt("segmode", TessBaseAPI.PSM_AUTO);
        mSegModeID = settings.getInt("segmodeID", 0);               
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
    	//Log.w(TAG, "onPause");      
        savePreferences();
        super.onPause();
    }
    
    @Override
    protected void onResume() {
    	//Log.w(TAG, "onResume");    	
        restorePreferences();        
        super.onResume();
    }
    
    @Override
    protected void onStop(){
    	//Log.w(TAG, "onStop");       
        super.onStop(); 
    }
    
    @Override
    protected void onDestroy(){
    	//Log.w(TAG, "onDestroy");
    	cleanMemory();
    	super.onDestroy();
    }
    
    @Override
    protected void onStart(){
    	//Log.w(TAG, "onStart");
    	super.onStart();
    }
    
    @Override
    protected void onRestart(){
    	//Log.w(TAG, "onRestart");    	
    	super.onRestart();
    }
    
    /*---------- Private inner class to download a file in the background ----------*/
    private class DownloadExtractFile extends AsyncTask<String, Integer, String> {
        
    	private boolean success = false;
    	private String downloadFilePath = mTrainedDataPath + File.separator + mLang + ".traineddata.gz";    	
    	
    	@Override
        protected String doInBackground(String... sUrl) {
            try {
                URL url = new URL(sUrl[0]);
                URLConnection connection = url.openConnection();
                connection.connect();
                // This will be useful so that you can show a typical 0-100% progress bar
                int fileLength = connection.getContentLength();

                // Download the file
                InputStream input = new BufferedInputStream(url.openStream());                
                OutputStream output = new FileOutputStream(downloadFilePath);

                byte data[] = new byte[1024];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1 && !isCancelled()) {
                    total += count;
                    // Publishing the progress....
                    publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
                output.flush();
                output.close();
                input.close();
                success = true;                
                //Log.i(TAG, "Download completed succesfully");
            } catch (Exception e) {
            	//Log.e(TAG, "Problems during download");
            	success = false;            	
            }            
            return null;
        }
    	
    	@Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialogFragment(PROGRESS_DIALOG, "progress_dialog");            
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);            
            mProgressDialog.setProgress(progress[0]);
        }
        
        @Override
        protected void onPostExecute(String result) {
        	super.onPostExecute(result);        	
            if (success){
            	// Extracting the tar file    		
            	try {
            		//Log.i(TAG, "Src file to extract: " + mTrainedDataPath + File.separator + mLang + ".traineddata.gz");
            		Utilities.unZip(mTrainedDataPath + File.separator + mLang + ".traineddata.gz", mTrainedDataPath + File.separator + mLang + ".traineddata");
            		Toast.makeText(mThis, getString(R.string.selected_language) + " " + mLangArray[mLangID], Toast.LENGTH_SHORT).show();
            		removeDialogFragment("progress_dialog");
            	} catch (IOException e) {
            		removeDialogFragment("progress_dialog");
            		showDialogFragment(EXTRACT_ERR_DIALOG, "extract_err_dialog");
            		File file = new File(downloadFilePath);
                	if (file.exists())
                		file.delete();
            	}
            }
            else{
            	removeDialogFragment("progress_dialog");
            	showDialogFragment(NETWORK_ERR_DIALOG, "network_err_dialog");
            	File file = new File(downloadFilePath);
            	if (file.exists())
            		file.delete();
            }            
        }
        
        @Override
        protected void onCancelled (){
        	super.onCancelled();       	
        	File file = new File(downloadFilePath);
        	if (file.exists())
        		file.delete();
        	removeDialogFragment("progress_dialog");
        	//mProgressDialog.dismiss();
        }
    }
    
    public void setProgressDialog(ProgressDialog pd){
    	mProgressDialog = pd;
    }
    
    /*---------- Private inner class to perform OCR on the image ----------*/
    private class OCR extends AsyncTask<String, Integer, String> {
        
    	private boolean success = false;    	
    	
    	@Override
        protected String doInBackground(String... sUrl) {
    		
    		try{
				//Log.i(TAG, "STARTING OCR");
	    	
				// Image is ready for recognition   			
				mBaseApi.setImage(mBitmap);
				
				// Text recognition: can take a bit...    				
				recognizedText = mBaseApi.getUTF8Text();
				success = true;
			}
			catch (Exception e){				
				success=false;
				e.printStackTrace();
				}    			           
            
            return null;
        }
    	
    	@Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialogFragment(LOADING_DIALOG, "loading_dialog");
            initTess();        	
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);            
        }
        
        @Override
        protected void onPostExecute(String result) {
        	super.onPostExecute(result);        	
        	if (success){
        		removeDialogFragment("loading_dialog");        		  
        		showText();
        	}
        	else{
        		removeDialogFragment("loading_dialog");
        		showDialogFragment(OCR_ERR_DIALOG, "ocr_err_dialog");
        	}			
        }
    }


}
