package com.example.pillet.urbapp2.utils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.util.Log;

import com.example.pillet.urbapp2.activities.MainActivity;

public class ConnexionCheck {

	//TODO Adddescription for javadoc
	private static String url=MainActivity.CONNECTIVITY_URL;

	//TODO Adddescription for javadoc
	public void Connectivity(){
		ConnectivityManager con=(ConnectivityManager)MainActivity.baseContext.getSystemService(Activity.CONNECTIVITY_SERVICE);
		boolean wifi=con.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();
		boolean mobile = false;

		try {
			mobile=con.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
		}
		catch (NullPointerException e){
			mobile=false;
		}
		boolean internet=wifi|mobile;
		if (internet){
			ConnectivityRequestTask task = new ConnectivityRequestTask(url);
			task.execute(url);
		}

	}
}
//TODO Adddescription for javadoc
class ConnectivityRequestTask extends AsyncTask<String, Void, Boolean> {

	private String url;

	public ConnectivityRequestTask(String url){
		this.url = url;
	}

	@Override
	// Actual download method, run in the task thread
	protected Boolean doInBackground(String... params) {
		HttpURLConnection urlConnection = null;
		try{
			URL url = new URL(MainActivity.CONNECTIVITY_URL); // "http://clients3.google.com/generate_204"
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setInstanceFollowRedirects(false);
			urlConnection.setConnectTimeout(3000);
			urlConnection.setReadTimeout(5000);
			urlConnection.setUseCaches(false);
			urlConnection.getInputStream();
			// We got a valid response, but not from the real google
			return urlConnection.getResponseCode() == 204;
		} catch (IOException e) {

				Log.e("Connect","Walled garden check - probably not a portal: exception "
						+ e);
			return false;
		} finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}
		}
	}


	protected void onPostExecute(Boolean isConnect) {
		if(!isConnect){
			MainActivity.errorConnect();
		}
	}
}