package com.ecn.urbapp.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

/**
 * Download a picture from the web and prints it on screen
 * @author Sebastien
 *
 */
public class ImageDownloader {
	/** Path of the directory where image is going to be registered	 */
	private final static String path=Environment.getExternalStorageDirectory()+"/featureapp/";
	/** Name of the file to be registered */
	private static String name;

	/**
	 * Method to launch the download of picture from an url
	 * @param url
	 * @param name name of the file to registered in path
	 * @return path of image
	 */
    public String download(String url, String name) {
    		this.name=name;
            BitmapDownloaderTask task = new BitmapDownloaderTask();
            
            //check if file already exists. If so, displays it !
            File imgFile = new  File(path+name);
        	if(!imgFile.exists())
				try {
					task.execute(url+name).get();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} //download image !
            return path+name;
        }

	/**
	 * The task which get the picture on web and save it on memory
	 * @param url
	 * @return
	 */
static void downloadBitmap(String url) {
    final AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
    final HttpGet getRequest = new HttpGet(url);

    try {
        HttpResponse response = client.execute(getRequest);
        final int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != HttpStatus.SC_OK) { 
            Log.w("ImageDownloader", "Error " + statusCode + " while retrieving bitmap from " + url); 
        }
        
        final HttpEntity entity = response.getEntity();
        if (entity != null) {
        	
        	File file = new File(path, name);
            file.createNewFile();
            
            InputStream inputStream = null;
            try {
                inputStream = entity.getContent(); 
                /*
                 * Read bytes to the Buffer until there is nothing more to read(-1) and write on the fly in the file.
                 */
                FileOutputStream fos = new FileOutputStream(file);
                final int BUFFER_SIZE = 5 * 1024;
                BufferedInputStream bis = new BufferedInputStream(inputStream, BUFFER_SIZE);
                byte[] baf = new byte[BUFFER_SIZE];
                int actual = 0;
                while (actual != -1) {
                    fos.write(baf, 0, actual);
                    actual = bis.read(baf, 0, BUFFER_SIZE);
                }

                fos.close();
                
            } finally {
                if (inputStream != null) {
                    inputStream.close();  
                }
                entity.consumeContent();
            }
        }
    } catch (Exception e) {
        // Could provide a more explicit error message for IOException or IllegalStateException
        getRequest.abort();
        Log.w("ImageDownloader", "Error while retrieving bitmap from " + url);
    } finally {
        if (client != null) {
            client.close();
        }
    }
}

/**
 * The AsyncTask to download picture
 * @author Sebastien
 *
 */
	class BitmapDownloaderTask extends AsyncTask<String, Void, Void> {
	
	    public BitmapDownloaderTask() {
	    }
	
	    @Override
	    // Actual download method, run in the task thread
	    protected Void doInBackground(String... params) {
	         // params comes from the execute() call: params[0] is the url.
	          downloadBitmap(params[0]);
	          return null;
	    }
	
	    
	    // Once the image is download, associates it to the imageView
	    protected void onPostExecute() {
	    	            
	    	//save file on cache data of the app
	    	//BitmapToFile(bitmap);

	    }
	}
}