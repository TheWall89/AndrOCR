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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class MyDialogFragment extends DialogFragment {
	
	private final int ABOUT_ID = 0, COPY_ERR_DIALOG = 1, SEGMODE_DIALOG = 2, LANGUAGE_DIALOG = 3, LOADING_DIALOG = 4, NETWORK_ERR_DIALOG = 5,
						OCR_ERR_DIALOG = 6, EXTRACT_ERR_DIALOG = 7, CONTINUE_DIALOG = 8, SD_ERR_DIALOG = 9, PROGRESS_DIALOG = 10, TRANSLATE_DIALOG = 11, 
						TRANSLATE_ERR_DIALOG = 12, TTS_DATA_DIALOG = 13;
    private MyDialogListener mListener;
    private ProgressDialog mProgressDialog;
    
	public static MyDialogFragment newInstance(int dialog_id) {
		MyDialogFragment frag = new MyDialogFragment();
	        Bundle args = new Bundle();
	        args.putInt("dialog_id", dialog_id);
	        frag.setArguments(args);
	        return frag;
	    }
	
	/* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface MyDialogListener {
        public void onDialogPositiveClick(int dialog_id);
        public void onDialogNegativeClick(int dialog_id);
        public void onDialogNeutralClick(int dialog_id);
        public void onDialogSingleChoice(int dialog_id, int item);
    }   
    
    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (MyDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        
    	int dialog_id = getArguments().getInt("dialog_id");        
        Dialog dialog = null;
    	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    	ShowImage si;
    	
        switch(dialog_id) {
        
        	case ABOUT_ID:
        		builder.setTitle(getString(R.string.app_name));
        		builder.setMessage(getString(R.string.about_text));        		       		
        		builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
        			public void onClick(DialogInterface dialog, int id) {
        				mListener.onDialogNeutralClick(ABOUT_ID);
        			}
                });
        		dialog = builder.create();       		
        		break;
        	
        	case COPY_ERR_DIALOG:
        		builder.setTitle(getString(R.string.error));
        		builder.setMessage(getString(R.string.copy_error));        		
        		builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
        			public void onClick(DialogInterface dialog, int id) {
        				mListener.onDialogNeutralClick(COPY_ERR_DIALOG);
        			}
                });
        		dialog = builder.create();
        		break;
        		
        	case SEGMODE_DIALOG:   	
            	// Creating a choose dialog with radio buttons        	
            	builder.setTitle(getString(R.string.text_layout));            	            	
            	si = (ShowImage)this.getActivity();
            	builder.setSingleChoiceItems(si.getSegModeArray(), si.getSegmodeID(), new DialogInterface.OnClickListener() {
            	    public void onClick(DialogInterface dialog, int item) {
            	        mListener.onDialogSingleChoice(SEGMODE_DIALOG, item);
            	    }
            	});        	
            	dialog = builder.create();
                break;
                
        	case LANGUAGE_DIALOG:
            	// Creating a choose dialog with radio buttons        	
            	builder.setTitle(getString(R.string.language));
            	si = (ShowImage)this.getActivity();
            	builder.setSingleChoiceItems(si.getLangArray(), si.getLangID(), new DialogInterface.OnClickListener() {
            	    public void onClick(DialogInterface dialog, int item) {
            	        mListener.onDialogSingleChoice(LANGUAGE_DIALOG, item);        	    	        	        
            	    }
            	});
            	dialog = builder.create();
                break;
                
        	case LOADING_DIALOG:
        		ProgressDialog loadingDialog = new ProgressDialog(getActivity());
        		loadingDialog.setMessage(getString(R.string.please_wait));
            	return loadingDialog;           	
            	
        	case NETWORK_ERR_DIALOG:
            	builder.setTitle(getString(R.string.error));
            	builder.setMessage(getString(R.string.network_error));            	
            	builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    	mListener.onDialogNeutralClick(NETWORK_ERR_DIALOG);
                    }
                    });
            	dialog = builder.create();
            	break;
            	
        	case OCR_ERR_DIALOG:
            	builder.setTitle(getString(R.string.error));
            	builder.setMessage(getString(R.string.ocr_error));            	
            	builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogNeutralClick(OCR_ERR_DIALOG);
                    }
                    });
            	dialog = builder.create();
            	break;
            	
        	case EXTRACT_ERR_DIALOG:
            	builder.setTitle(getString(R.string.error));
            	builder.setMessage(getString(R.string.extraction_error));            	
            	builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    	mListener.onDialogNeutralClick(EXTRACT_ERR_DIALOG);
                    }
                    });
            	dialog = builder.create();
            	break;
            	
        	case CONTINUE_DIALOG:		
				builder.setTitle(getString(R.string.download));
	        	builder.setMessage(getString(R.string.info3));	        	
	        	builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {					
					public void onClick(DialogInterface dialog, int which) {
						mListener.onDialogPositiveClick(CONTINUE_DIALOG);
					}
				});
	        	builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {					
					public void onClick(DialogInterface dialog, int which) {
						mListener.onDialogNegativeClick(CONTINUE_DIALOG);
					}
				});
	        	dialog = builder.create();
	        	break;
	        	
        	case PROGRESS_DIALOG:
        		mProgressDialog = new ProgressDialog(getActivity());
        		mProgressDialog.setMessage(getString(R.string.language_download));
        		mProgressDialog.setIndeterminate(false);
        		mProgressDialog.setMax(100);
        		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);        		
        		mProgressDialog.setButton(ProgressDialog.BUTTON_NEUTRAL, getString(R.string.cancel), new DialogInterface.OnClickListener() {					
					public void onClick(DialogInterface dialog, int which) {
						mListener.onDialogNeutralClick(PROGRESS_DIALOG);
					}
				});
        		ShowImage showImage = (ShowImage)getActivity();
        		showImage.setProgressDialog(mProgressDialog);
        		return mProgressDialog;
        		
        	case SD_ERR_DIALOG:
            	builder.setTitle(getString(R.string.error));
            	builder.setMessage(getString(R.string.sd_error));            	
            	builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogNeutralClick(SD_ERR_DIALOG);
                    }
                    });
            	dialog = builder.create();
            	break;
            	
        	case TRANSLATE_DIALOG:
        		builder.setTitle(getString(R.string.translate_to));
        		UseText ut = (UseText)getActivity();
            	builder.setSingleChoiceItems(ut.getLangArray(), -1, new DialogInterface.OnClickListener() {
            		public void onClick(DialogInterface dialog, int item) {
            	        mListener.onDialogSingleChoice(TRANSLATE_DIALOG, item);
            		}
            	});
            	dialog = builder.create();
        		break;
        		
        	case TRANSLATE_ERR_DIALOG:
            	builder.setTitle(getString(R.string.error));
            	builder.setMessage(getString(R.string.translate_error));            	
            	builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogNeutralClick(TRANSLATE_ERR_DIALOG);
                    }
                    });
            	dialog = builder.create();
            	break;
            	
        	case TTS_DATA_DIALOG:
            	builder.setTitle(getString(R.string.tts_download));
            	builder.setMessage(getString(R.string.tts_message));
            	builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogPositiveClick(TTS_DATA_DIALOG);
                    }
                    });
            	builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogNegativeClick(TTS_DATA_DIALOG);
                    }
                    });
            	dialog = builder.create();
            	break;
        
        }        
        return dialog;   
	}     
	
}
