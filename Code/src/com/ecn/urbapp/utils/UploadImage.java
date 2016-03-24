package com.ecn.urbapp.utils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.ecn.urbapp.R;

public class UploadImage extends Activity {

	/** The captured image file. Get it's path from the starting intent */
	private File mImage;

	//TODO Adddescription for javadoc
	/** Progress dialog id */
	private static final int UPLOAD_PROGRESS_DIALOG = 0;
	private static final int UPLOAD_ERROR_DIALOG = 1;
	private static final int UPLOAD_SUCCESS_DIALOG = 2;


	/** Handler to confirm button */
	private Button mConfirm;

	/** Handler to cancel button */
	private Button mCancel;

	/** Uploading progress dialog */
	private ProgressDialog mDialog;
	
	/** Upload file to this url */
	private static final String UPLOAD_URL = "http://192.168.177.1//uploadImage.php";

	/** Send the file with this form name */
	private static final String FIELD_FILE = "file";

	/**
	 * Load the image from the cache data of the app and launch the upload
	 * @param imageToUploadUrl
	 */
	public UploadImage(String imageToUploadUrl) {
		super();
		mImage = new File(imageToUploadUrl);
		new UploadImageTask().execute(mImage);
	}

	//TODO Add description for javadoc
	public UploadImage() {
		super();
	}

	//TODO Add description for javadoc
	public void onStart()
	{
		super.onStart();

		//registerButtonCallbacks();
	}

	/**
	 * Called when the activity is created
	 *
	 * We load the captured image, and register button callbacks
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.layout_upload);

		mConfirm = (Button) findViewById(R.id.upload_send_confirm);
		mCancel = (Button) findViewById(R.id.upload_send_cancel);
		setResult(RESULT_CANCELED);

		Bundle b = getIntent().getExtras();
		String imageToUploadUrl = b.getString("imageUrl");
		mImage = new File(imageToUploadUrl);
		new UploadImageTask().execute(mImage);

	}

	//TODO Adddescription for javadoc
	@Override
	protected void onPause() {
		super.onPause();

	}

	/**
	 * Register callbacks for ui buttons
	 */
	protected void registerButtonCallbacks() {
		// Cancel button callback
		mCancel = (Button) findViewById(R.id.upload_send_cancel);
		mCancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				//UploadImage.UploadImageTask.finish();
			}
		});

		// Confirm button callback
		mConfirm = (Button) findViewById(R.id.upload_send_confirm);
		mConfirm.setEnabled(true);
		mConfirm.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				new UploadImageTask().execute(mImage);
			}
		});
	}



	/**
	 * Initialize the dialogs
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		switch(id) {
		case UPLOAD_PROGRESS_DIALOG:
			mDialog = new ProgressDialog(this);
			mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			mDialog.setCancelable(false);
			mDialog.setTitle(getString(R.string.progress_dialog_title_connecting));
			return mDialog;


		case UPLOAD_ERROR_DIALOG:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.upload_error_title)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setMessage(R.string.upload_error_message)
			.setCancelable(false)
			.setPositiveButton(getString(R.string.retry), new          DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					//UploadImageTask.this.finish();
				}
			});
			return builder.create();

		case UPLOAD_SUCCESS_DIALOG:
			AlertDialog.Builder success = new AlertDialog.Builder(this);
			success.setTitle(R.string.upload_success_title)
			.setIcon(android.R.drawable.ic_dialog_info)
			.setMessage(R.string.upload_success_message)
			.setCancelable(false)
			.setPositiveButton(getString(R.string.success), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					//UploadImageTask.this.finish();
				}
			});
			return success.create();

		default:
			return null;
		}
	}

	/**
	 * Prepare the progress dialog
	 */
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch(id) {
		case UPLOAD_PROGRESS_DIALOG:
			mDialog.setProgress(0);
			mDialog.setTitle(getString(R.string.progress_dialog_title_connecting));
		}
	}


	/**
	 * Asynchronous task to upload file to server
	 */
	class UploadImageTask extends AsyncTask<File, Integer, Boolean> {

		/**
		 * Prepare activity before upload
		 */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			setProgressBarIndeterminateVisibility(true);
			mConfirm.setEnabled(false);
			mCancel.setEnabled(false);
			//TODO change showDialog by a dialog fragment like it's recommended in the documentation
			showDialog(UPLOAD_PROGRESS_DIALOG);
		}

		/**
		 * Clean app state after upload is completed
		 */
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			setProgressBarIndeterminateVisibility(false);
			mConfirm.setEnabled(true);
			mDialog.dismiss();

			//TODO change showDialog by a dialog fragment like it's recommended in the documentation
			if (result) {
				showDialog(UPLOAD_SUCCESS_DIALOG);
				Log.w("DFHUPLOAD", "Bim, c'est bon ça");
			} else {
				showDialog(UPLOAD_ERROR_DIALOG);
				Log.e("DFHUPLOAD","Error fuck it");
			}
		}

		@Override
		protected Boolean doInBackground(File... image) {
			return doFileUpload(image[0], "UPLOAD_URL");
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);

			if (values[0] == 0) {
				mDialog.setTitle(getString(R.string.progress_dialog_title_uploading));
			}

			mDialog.setProgress(values[0]);
		}


		private boolean doFileUpload(File file, String uploadUrl) {
			HttpURLConnection connection = null;
			DataOutputStream outputStream = null;

			String pathToOurFile = file.getPath();
			String urlServer = UPLOAD_URL;
			String lineEnd = "\r\n";
			String twoHyphens = "--";
			String boundary =  "*****";

			// log path to our file
			Log.d("DFHUPLOAD", pathToOurFile);

			int bytesRead, bytesAvailable, bufferSize;
			byte[] buffer;
			int maxBufferSize = 1*1024*1024;
			int sentBytes = 0;
			long fileSize = file.length();

			// log filesize
			String files= String.valueOf(fileSize);
			String buffers= String.valueOf(maxBufferSize);
			Log.d("DFHUPLOAD",files);
			Log.d("DFHUPLOAD",buffers);

			try
			{
				FileInputStream fileInputStream = new FileInputStream(new File(pathToOurFile) );

				URL url = new URL(urlServer);
				connection = (HttpURLConnection) url.openConnection();

				// Allow Inputs & Outputs
				connection.setDoInput(true);
				connection.setDoOutput(true);
				connection.setUseCaches(false);

				// Enable POST method
				connection.setRequestMethod("POST");

				connection.setRequestProperty("Connection", "Keep-Alive");
				connection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);

				outputStream = new DataOutputStream( connection.getOutputStream() );
				outputStream.writeBytes(twoHyphens + boundary + lineEnd);
				outputStream.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + pathToOurFile +"\"" + lineEnd);
				outputStream.writeBytes(lineEnd);

				bytesAvailable = fileInputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				buffer = new byte[bufferSize];

				// Read file
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);

				while (bytesRead > 0)
				{
					outputStream.write(buffer, 0, bufferSize);
					bytesAvailable = fileInputStream.available();
					bufferSize = Math.min(bytesAvailable, maxBufferSize);
					bytesRead = fileInputStream.read(buffer, 0, bufferSize);

					sentBytes += bufferSize;
					publishProgress((int)(sentBytes * 100 / fileSize));

					bytesAvailable = fileInputStream.available();
					bufferSize = Math.min(bytesAvailable, maxBufferSize);
					bytesRead = fileInputStream.read(buffer, 0, bufferSize);
				}

				outputStream.writeBytes(lineEnd);
				outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

				// Responses from the server (code and message)
				connection.getResponseCode();

				connection.getResponseMessage();


				fileInputStream.close();
				outputStream.flush();
				outputStream.close();
				try {
					int responseCode = connection.getResponseCode();
					return responseCode == 200;
				} catch (IOException ioex) {
					Log.e("DFHUPLOAD", "Upload file failed: " + ioex.getMessage(), ioex);
					return false;
				} catch (Exception e) {
					Log.e("DFHUPLOAD", "Upload file failed: " + e.getMessage(), e);
					return false;
				}
			}
			catch (Exception ex)
			{
				String msg= ex.getMessage();
				Log.d("DFHUPLOAD", msg);
			}
			return true;
		}

	}
}