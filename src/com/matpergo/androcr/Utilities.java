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
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.zip.GZIPInputStream;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

public abstract class Utilities {
	
	// Method to check if external storage is available for reading/writing	
	public static boolean checkExternalStorage(){
		
		boolean mExternalStorageWriteable = false;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
		    // We can read and write the media 
		    mExternalStorageWriteable = true;
		}
		
		return (mExternalStorageWriteable);
	}
	
	// Method to check if internet connection is available
	public static boolean isNetworkAvailable(Activity activity) {
	    ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
	}

	
	/*
	 ********** Providing simple operations on files **********
	 */
	
	public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!sourceFile.exists()) {
            return;
        }

        FileChannel source = null;
        FileChannel destination = null;
        FileInputStream fis = new FileInputStream(sourceFile);
        source = fis.getChannel();
        FileOutputStream fos = new FileOutputStream(destFile);
        destination = fos.getChannel();
        if (destination != null && source != null) {
            destination.transferFrom(source, 0, source.size());
        }
        if (source != null) {
             source.close();
             fis.close();
        }
        if (destination != null) {
             destination.close();
             fos.close();
        }
        source.close();
	}
	
	public static void copyFile(InputStream in, OutputStream out) throws IOException {
        
		// Transfer bytes from in to out
		byte[] buf = new byte[1024];
		int len;
		
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();		
		out.close();
	}
	
	public static void writeTextFile(File out, String text) throws IOException {
		FileWriter writer = new FileWriter(out);
		writer.append(text);
        writer.flush();
        writer.close();
	}
	
	public static String getPathFromUri(Uri uri, Activity activity){
    	
    	String[] projection = {MediaStore.Images.Media.DATA};
		Cursor cursor = activity.managedQuery(uri, projection, null, null, null);
		activity.startManagingCursor(cursor);
		int column_index = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);		
				
    }
	
	
	public static void unZip (String tarFile, String destFile) throws IOException{
			
			File zippedFile = new File (tarFile);
			File outFilePath = new File (destFile);
			
			GZIPInputStream gzipInputStream = new GZIPInputStream(new BufferedInputStream(new FileInputStream(zippedFile)));
		    OutputStream outputStream = new FileOutputStream(outFilePath);
		    BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);

		    final int BUFFER = 8192;
		    byte[] data = new byte[BUFFER];
		    int len;
		    while ((len = gzipInputStream.read(data, 0, BUFFER)) > 0) {
		      bufferedOutputStream.write(data, 0, len);
		    }
		    gzipInputStream.close();
		    bufferedOutputStream.flush();
		    bufferedOutputStream.close();
		    
		    if (zippedFile.exists())
		        zippedFile.delete();
    }	
	
	/*
	 ********** Providing simple operations on Bitmaps **********
	 */
	
	// Calculating sample size. We choose 2 if the image is 5mpx or more.
	public static int calculateSampleSize(String path){
		// Getting Bitmap original dimensions
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);
		int height = options.outHeight;
		int width = options.outWidth;
		
		int inSampleSize = 1;
		float constant = 1024000;
		int megapixels = Math.round(((float)width*(float)height)/constant);
		//Log.e("MPX", ""+megapixels);
		// If image quality is good, we can use a scaled image
		if(megapixels > 5){
			inSampleSize = 2;
		}	    
	    return inSampleSize;
	}
	
	
	public static Bitmap createBitmapFromPath(String path, int sampleSize){	
		
		// Creating a bitmap from the given path
		Bitmap bitmap = null;
		try{			
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = false;
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;
			options.inSampleSize = sampleSize;
			options.inPurgeable = true;
			bitmap = BitmapFactory.decodeFile(path, options);
			
		}
		catch(OutOfMemoryError e){
			//Memory error occured.
		}
		return bitmap;
	}
	
	public static Bitmap rotateBitmap(Bitmap bitmap, int angle){
		// Getting width & height of the given image.
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		
		// Setting pre rotate
		Matrix mtx = new Matrix();
		mtx.preRotate(angle);

		// Rotating Bitmap
		bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, false);
		return bitmap;
	}

}
