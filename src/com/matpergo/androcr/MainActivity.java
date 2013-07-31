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

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MainActivity extends FragmentActivity {
	
	protected final int ABOUT_ID = 0;
	protected String mPath, mDataDir;
	protected final String mFileName = "OCRimage.jpg";
	
	// Creating an "Options menu"
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.about:
                showDialogFragment(ABOUT_ID, "about_id");
                return true;
            case R.id.rate:
            	Intent intent1 = new Intent(Intent.ACTION_VIEW);
            	intent1.setData(Uri.parse("market://details?id=com.matpergo.androcr"));
            	startActivity(intent1);
                return true;
            case R.id.calcpro:
            	Intent intent2 = new Intent(Intent.ACTION_VIEW);
            	intent2.setData(Uri.parse("market://details?id=it.RiccardoP.CalculatorPro"));
            	startActivity(intent2);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    // Method to show dialogs
    void showDialogFragment(int dialog_id, String dialog_tag) {
        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        
    	FragmentTransaction ft = getSupportFragmentManager().beginTransaction();        
        ft.addToBackStack(null);       

        // Create and show the dialog.
        DialogFragment newFragment = MyDialogFragment.newInstance(dialog_id);
        newFragment.setCancelable(false);
        newFragment.show(ft, dialog_tag);        
    }
    
    void removeDialogFragment(String dialog_tag){
    	//FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
    	DialogFragment dialogFragment = (DialogFragment)getSupportFragmentManager().findFragmentByTag(dialog_tag);
    	if (dialogFragment != null){
    		dialogFragment.dismiss();
    		//ft.remove(dialogFragment);
    	}
    	
    }
    
    //Methods to correctly initialize paths
    public boolean initPath(){
    	boolean allOk = false;
    	// The application needs to work with SD card. The code below checks if it is available.        
        if (Utilities.checkExternalStorage() == true && this.getExternalFilesDir(null) != null){
        	mDataDir = this.getExternalFilesDir("data").getAbsolutePath();        	
        	mPath = this.getExternalFilesDir("images").getAbsolutePath() + File.separator + mFileName;
        	allOk = true;
        	//Log.i("Utilities.java", mDataDir);
        	//Log.i("This", mDataDir);
        	//Toast.makeText(this, "Picture Directory: " + mPath, Toast.LENGTH_LONG).show();
        }    	
    	return allOk;
    }

}
