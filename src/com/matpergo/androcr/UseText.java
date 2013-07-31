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
import java.util.HashMap;
import java.util.Locale;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class UseText extends MainActivity implements OnInitListener, OnUtteranceCompletedListener, MyDialogFragment.MyDialogListener{
	
	private String recognizedText, textToUse;
	private String mFromLang, mToLang, mCurrentLang, mTranslatedText = "";
	private EditText mEditText;
	private Button mTranslateButton, mTTSButton, mSaveButton;	
	private TextToSpeech mTTS;	
	private HashMap<String, String> myHash;
	private boolean mIsTTSinitialized = false, mOnCreateTTS, mUserWantsTTS, mTTSAlreadyChecked = false, mFirstExec = true;
	private final int ABOUT_ID = 0, LOADING_DIALOG = 4, NETWORK_ERR_DIALOG = 5, SD_ERR_DIALOG = 9, TRANSLATE_DIALOG = 11, TRANSLATE_ERR_DIALOG = 12, TTS_DATA_DIALOG = 13;
	private final int TTS_CHECK = 0, TTS_INSTALLATION = 1;
	
	private final String[] mLangArray = {AndrOCRApp.myGetString(R.string.english), AndrOCRApp.myGetString(R.string.bulgarian), AndrOCRApp.myGetString(R.string.catalan), 
			AndrOCRApp.myGetString(R.string.chinese), AndrOCRApp.myGetString(R.string.chinese_tra), AndrOCRApp.myGetString(R.string.czech), 
			AndrOCRApp.myGetString(R.string.danish), AndrOCRApp.myGetString(R.string.dutch),
			AndrOCRApp.myGetString(R.string.german), AndrOCRApp.myGetString(R.string.greek),
			AndrOCRApp.myGetString(R.string.finnish), AndrOCRApp.myGetString(R.string.french), AndrOCRApp.myGetString(R.string.hebrew), AndrOCRApp.myGetString(R.string.hungarian), 
			AndrOCRApp.myGetString(R.string.indonesian), AndrOCRApp.myGetString(R.string.italian), AndrOCRApp.myGetString(R.string.japanese),
			AndrOCRApp.myGetString(R.string.korean), AndrOCRApp.myGetString(R.string.latvian), AndrOCRApp.myGetString(R.string.lithuanian),
			AndrOCRApp.myGetString(R.string.norwegian), AndrOCRApp.myGetString(R.string.polish), 
			AndrOCRApp.myGetString(R.string.portuguese), AndrOCRApp.myGetString(R.string.romanian), AndrOCRApp.myGetString(R.string.russian),
			AndrOCRApp.myGetString(R.string.slovakian), AndrOCRApp.myGetString(R.string.slovenian),
			AndrOCRApp.myGetString(R.string.spanish), AndrOCRApp.myGetString(R.string.swedish),
			AndrOCRApp.myGetString(R.string.turkish), AndrOCRApp.myGetString(R.string.ukrainian), AndrOCRApp.myGetString(R.string.vietnamese)};
	
	private final String [] mBingLangCodeArray = {"en", "bg", "ca", "zh-CHS", "zh-CHT", "cs", "da", "nl", "de", "el", "fi", "fr", "he", "hu", "id", "it", "ja", "ko", "lv", "lt",  
			"no", "pl", "pt", "ro", "rus", "sk", "sl", "es", "sv", "tr", "uk", "vi"};
	
	//private final String TAG = "UseText.java";
	
	//Useful method to call after rotations
	private void setupUI(){        
		// Getting buttons (we need to disable them sometimes)
        mTranslateButton = (Button)findViewById(R.id.button_translate);
        mTTSButton = (Button)findViewById(R.id.button_tts);
        mSaveButton = (Button)findViewById(R.id.button_file);
        
        // Setting up the textbox
        mEditText = (EditText)findViewById(R.id.text);
        mEditText.setText(textToUse);
		
		// If language is Serbian or Tagalog, Bing can't translate it
        enableTranslateButton(true);
        
        // Enabling TTS button or not
        if(isTTSLangSupported())        	
        	enableTTSButton(true);        
        else
        	enableTTSButton(false);
        
        // Enabling SaveButton
        enableSaveButton(this.initPath());
        
        //Changing TTS button text accordingly even when rotating screen
        if(mTTS != null && mTTS.isSpeaking()){			
			setTTSButton(getString(R.string.stop));
        }
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.usetext);       
        
        // Getting the path of the image to show        
        Bundle extras = this.getIntent().getExtras();        
        recognizedText = extras.getString("TEXT");
        textToUse = recognizedText;
        
        // Getting the language used for text recognition
        mFromLang = extras.getString("LANG");
        mCurrentLang = mFromLang;
        //Log.i(TAG, mFromLang);
         
        setupUI();
        
        // Trying to initialize TTS
        mUserWantsTTS = true;
        mOnCreateTTS = true;
        if (mFirstExec == true){
        	checkTTS();
        	mFirstExec = false;
        }       
        
        /*
        // Setting the TTS button (checking language)
        enableTTSButton(true);
        
        // Initializing TTS service        
        if (!isTTSinitialized && userWantsTTS){
        	isTTSinitialized = true;        	
        	//Log.w(TAG, "onCreate: mTTS instantiated");
        	initTTS();
        }*/
        
        // Preparing TTS with some options
		myHash = new HashMap<String, String>();
		myHash.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,"end of speak");       
		
		//Log.i(TAG, "oncreateexecuted");
	}
	
	@Override
    public void onConfigurationChanged(Configuration newConfig) {
		textToUse = mEditText.getText().toString();
		super.onConfigurationChanged(newConfig);
		setContentView(R.layout.usetext);		
        setupUI();
        //Log.i(TAG, "onConfigChanged");
    }
	
	/* -------------------- Methods to translate text -------------------- */
	public void onTranslate(View v){
		
		// Checking if Network is available
		if (!Utilities.isNetworkAvailable(this)){    			
			showDialogFragment(NETWORK_ERR_DIALOG, "network_error_dialog");
		}
		else{		
			// Obtaining text shown by the TextEdit (it could be different from the recognized one cause user can modify it)
			textToUse = mEditText.getText().toString();		
			showDialogFragment(TRANSLATE_DIALOG, "translate_dialog");
		}
	}
	
	public void translateText(){		
		new Translate().execute("");		
	}
	
	public void enableTranslateButton(boolean value){
		// Setting button enabled or disabled
		if(mFromLang.equals("sr") || mFromLang.equals("tgl")){
        	mTranslateButton.setEnabled(false);        	       	
        }
		else
			mTranslateButton.setEnabled(value);		
		
		// Setting button background
		if(mTranslateButton.isEnabled()){
			mTranslateButton.setBackgroundResource(R.drawable.holo_button_anim);
		}
		else 
			mTranslateButton.setBackgroundResource(R.drawable.holo_background_dark);
	}
	
	/* -------------------- Method to rewrite text -------------------- */
	public void onRewrite(View v){
		mEditText.setText(recognizedText);		
		mCurrentLang = mFromLang;
		enableTranslateButton(true);
		if(isTTSLangSupported() && mIsTTSinitialized)       	
        	enableTTSButton(true);
        else
        	enableTTSButton(false);
	}
	
	/* -------------------- Methods to use TTS -------------------- */	
	public void onTTS(View v){
		
		if(mTTS.isSpeaking()){
			mTTS.stop();
			setTTSButton(getString(R.string.tts));
		}
		else{
			// Obtaining text shown by the TextEdit (it could be different from the recognized one cause user can modify it)
			textToUse = mEditText.getText().toString();			
		
				// To determine the language we use mCurrentLang. We are sure it is a supported language.	
				if (mCurrentLang.equals("en")){
					if(mTTS.isLanguageAvailable(Locale.ENGLISH) == TextToSpeech.LANG_AVAILABLE){
						mTTS.setLanguage(Locale.ENGLISH);
						// Speak!
						mTTS.speak(textToUse, TextToSpeech.QUEUE_ADD, myHash);
						setTTSButton(getString(R.string.stop));
					}
					else{
						Toast.makeText(this, getString(R.string.language_error), Toast.LENGTH_SHORT).show();
					}					
				}
				else if (mCurrentLang.equals("it")){
					if(mTTS.isLanguageAvailable(Locale.ITALIAN) == TextToSpeech.LANG_AVAILABLE){
						mTTS.setLanguage(Locale.ITALIAN);
						// Speak!
						mTTS.speak(textToUse, TextToSpeech.QUEUE_ADD, myHash);
						setTTSButton(getString(R.string.stop));
					}
					else{
						Toast.makeText(this, getString(R.string.language_error), Toast.LENGTH_SHORT).show();
					}	
				}
				else if (mCurrentLang.equals("fr")){
					if(mTTS.isLanguageAvailable(Locale.FRENCH) == TextToSpeech.LANG_AVAILABLE){
						mTTS.setLanguage(Locale.FRENCH);
						// Speak!
						mTTS.speak(textToUse, TextToSpeech.QUEUE_ADD, myHash);
						setTTSButton(getString(R.string.stop));
					}
					else{
						Toast.makeText(this, getString(R.string.language_error), Toast.LENGTH_SHORT).show();
					}	
				}
				else if (mCurrentLang.equals("de")){
					if(mTTS.isLanguageAvailable(Locale.GERMAN) == TextToSpeech.LANG_AVAILABLE){
						mTTS.setLanguage(Locale.GERMAN);
						// Speak!
						mTTS.speak(textToUse, TextToSpeech.QUEUE_ADD, myHash);
						setTTSButton(getString(R.string.stop));
					}
					else{
						Toast.makeText(this, getString(R.string.language_error), Toast.LENGTH_SHORT).show();
					}	
				}
				else if (mCurrentLang.equals("es")){
					if(mTTS.isLanguageAvailable(new Locale("es")) == TextToSpeech.LANG_AVAILABLE){
						mTTS.setLanguage(new Locale("es"));
						// Speak!
						mTTS.speak(textToUse, TextToSpeech.QUEUE_ADD, myHash);
						setTTSButton(getString(R.string.stop));
					}
					else{
						Toast.makeText(this, getString(R.string.language_error), Toast.LENGTH_SHORT).show();
					}	
				}
		}
		
	}
	
	private void checkTTS(){		
		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, TTS_CHECK);
	}
	
	private void initTTS(){
		mTTS = new TextToSpeech(this, this);
        mTTS.setOnUtteranceCompletedListener(this);                
	}
	
	public void onInit(int status) {		
		mIsTTSinitialized = true;
	}
	
	private void enableTTSButton(boolean value){
		mTTSButton.setEnabled(value);
				
		// Setting button background
		if(mTTSButton.isEnabled()){
			mTTSButton.setBackgroundResource(R.drawable.holo_button_anim);
		}
		else
			mTTSButton.setBackgroundResource(R.drawable.holo_background_dark);
			
	}
	
	private boolean isTTSLangSupported(){
		if (!(mCurrentLang.equals("en") || mCurrentLang.equals("it") || mCurrentLang.equals("fr") || mCurrentLang.equals("de") || mCurrentLang.equals("es")))
			return false;
		else
			return true;
	}
	
	private void setTTSButton(String value){
		mTTSButton.setText(value);
	}
	
	public void onUtteranceCompleted(String uttId) {
	    if (uttId.equals("end of speak")) {
	    	//Log.i(TAG, "Utterance catched");
	    	
	    	// Only the UI thread can change the views.
	    	runOnUiThread(new Runnable() {
                public void run() {
                	setTTSButton(getString(R.string.tts));
                }
            });
	    } 
	}	
	
	/* -------------------- Methods to use Maps -------------------- */
	public void onMaps(View v){
		
		// Checking if Network is available
		if (!Utilities.isNetworkAvailable(this)){    			
			showDialogFragment(NETWORK_ERR_DIALOG, "network_err_dialog");
		}
		else{
			// Obtaining text shown by the TextEdit (it could be different from the recognized one cause user can modify it)
			textToUse = mEditText.getText().toString();
			textToUse = textToUse.replace(' ', '+');
			try {			
				Intent geoIntent = new Intent (android.content.Intent.ACTION_VIEW, Uri.parse ("geo:0,0?q=" + textToUse));
				startActivity(geoIntent);
			} catch (Exception e){
				Toast.makeText(this, getString(R.string.maps_error), Toast.LENGTH_SHORT).show();			
			}
		}
	}
	
	
	/* -------------------- Method to save on file -------------------- */
	public void onSaveFile(View v){
		textToUse = mEditText.getText().toString();
		try{
			File out = new File(Environment.getExternalStorageDirectory(), "OCRtext.txt");
						
			Utilities.writeTextFile(out, textToUse);
	        
	        Toast.makeText(this, getString(R.string.file_saved) + Environment.getExternalStorageDirectory().toString() + 
	        		File.separator + "OCRtext.txt", Toast.LENGTH_SHORT).show();
		}
		catch (IOException ioe){
			showDialogFragment(SD_ERR_DIALOG, "sd_err_dialog");
			//ioe.printStackTrace();
		}
	}
	
	private void enableSaveButton(boolean value){
		mSaveButton.setEnabled(value);
				
		// Setting button background
		if(mSaveButton.isEnabled()){
			mSaveButton.setBackgroundResource(R.drawable.holo_button_anim);
		}
		else
			mSaveButton.setBackgroundResource(R.drawable.holo_background_dark);			
	}
	
	/* -------------------- Method to start from new image -------------------- */
	public void onNewImage(View v){
		Intent intent = new Intent(this, AndrOCR.class);    	
    	startActivity(intent);
	}
	
	/*
    ----------------------------- DIALOGS MANAGEMENT -----------------------------
    */
	
	public void onDialogPositiveClick(int dialog_id){
		switch(dialog_id){
		case TTS_DATA_DIALOG:
			removeDialogFragment("tts_data_dialog");
            Intent installIntent = new Intent();
            installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
            startActivityForResult(installIntent, TTS_INSTALLATION);
            break;
		}
	}
    public void onDialogNegativeClick(int dialog_id){
    	switch(dialog_id){
		case TTS_DATA_DIALOG:
			removeDialogFragment("tts_data_dialog");
            // Disabling TTS button
            enableTTSButton(false);
            mUserWantsTTS = false;
            break;
		}
    }
    public void onDialogNeutralClick(int dialog_id){
    	switch(dialog_id){
    	
    	case ABOUT_ID:
			removeDialogFragment("about_id");
			break;
		
		case NETWORK_ERR_DIALOG: 	
        	removeDialogFragment("network_err_dialog");        	
        	break;
        	
		case SD_ERR_DIALOG:
			removeDialogFragment("sd_err_dialog");            
            break;      
		
		case TRANSLATE_ERR_DIALOG:
			removeDialogFragment("translate_err_dialog");
			break;
    		
    	}    	
    }
    public void onDialogSingleChoice(int dialog_id, int item){
    	switch(dialog_id){
    	case TRANSLATE_DIALOG:
			int langID = item;	        
	        // Change the translation language
	        mToLang = mBingLangCodeArray[langID];        	    	
	    	removeDialogFragment("translate_dialog");        	    	
	    	translateText();
	    	break;
    	}
    }
	
    public String[] getLangArray(){
    	return mLangArray;
    }    
    	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
		
		if (requestCode == TTS_CHECK) {
	        if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {	            
	            initTTS();	            
	        } else {
	            if (mTTSAlreadyChecked == false){	        	
	            	showDialogFragment(TTS_DATA_DIALOG, "tts_data_dialog");
	            	mTTSAlreadyChecked = true;
	            }
	            else{
	            	enableTTSButton(false);
	            	mTTSAlreadyChecked = false;
	            	mUserWantsTTS = false;
	            }   
	            	
	        }
	    }
		
		if (requestCode == TTS_INSTALLATION){
			checkTTS();
		}
	}
	
	
    /*
    ----------------------------- LIFE CYCLE METHODS -----------------------------
    */
    
	@Override
	protected void onSaveInstanceState(Bundle outState) {
	    //outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE");
	    //super.onSaveInstanceState(outState);
		//Log.i(TAG, "osaveinstancestate executed");
	}
    
    @Override
    protected void onPause() {    	
    	super.onPause();
    	//Log.i(TAG, "onpause executed");
    }
    
    @Override
    protected void onStop(){    	
    	if (mIsTTSinitialized){    		
    		mIsTTSinitialized = false;    		
    		mTTS.stop();
    		mTTS.shutdown();
        }
    	mOnCreateTTS = false;
    	super.onStop();
    	//Log.i(TAG, "onstop executed");
    }
    
    @Override
    protected void onResume() {    	    	
        if (!mOnCreateTTS && mUserWantsTTS){        	
        	mOnCreateTTS = true;
        	checkTTS();        	
        }
        super.onResume();
        //Log.i(TAG, "onresume executed");
    }
    
    protected void onDestroy(){
    	mTTS = null;    	
    	super.onDestroy();
    	//Log.i(TAG, "ondestroy executed");
    }
    
    
    
    /*---------- Private inner class to translate text ----------*/
    private class Translate extends AsyncTask<String, Integer, String> {
        
    	private boolean success = false;
    	
    	@Override
        protected String doInBackground(String... sUrl) {
    		
    		try{
    			// Do translation 	    
			    mTranslatedText = TranslatorBing.translate(mFromLang, mToLang, textToUse);
			    success=true;
			}
			catch (Exception e){				
				success=false;
				}    			           
            
            return null;
        }
    	
    	@Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialogFragment(LOADING_DIALOG, "loading_dialog");
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
        		// Show translated text
        		mEditText.setText(mTranslatedText);                		
        		// Text language is changed, we disable the TTS button in case                		
    	    	mCurrentLang = mToLang;
    	    	enableTranslateButton(false);
    	    	if(isTTSLangSupported() && mIsTTSinitialized)       	
    	        	enableTTSButton(true);
    	        else
    	        	enableTTSButton(false);
        	}
        	else{
        		removeDialogFragment("loading_dialog");
				showDialogFragment(TRANSLATE_ERR_DIALOG, "translate_err_dialog");
        	}			
        }
    }

}
