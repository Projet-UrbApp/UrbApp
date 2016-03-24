package src.com.ecn.urbapp.syncToExt;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import src.com.ecn.urbapp.activities.MainActivity;
import src.com.ecn.urbapp.db.Composed;
import src.com.ecn.urbapp.db.Element;
import src.com.ecn.urbapp.db.ElementType;
import src.com.ecn.urbapp.db.GpsGeom;
import src.com.ecn.urbapp.db.Material;
import src.com.ecn.urbapp.db.Photo;
import src.com.ecn.urbapp.db.PixelGeom;
import src.com.ecn.urbapp.db.Project;
import src.com.ecn.urbapp.fragments.HomeFragment;
import src.com.ecn.urbapp.fragments.SaveFragment;
import com.google.gson.Gson;

/**
 * All the maters of 
 * @author Sebastien
 */
public class Sync
{
	private static final String TAG = "sync";
	/**
	 * MaxId and timestamp
	 */
	public static HashMap<String, Integer> maxId = new HashMap<String, Integer>();

	/**
	 * Contains all the projects on server
	 */
	public static ArrayList<Project> refreshedValues = new ArrayList<Project>();

	/**
	 * Contains all the relative photos on server of a project type
	 */
	public static ArrayList<Photo> refreshedValuesPhoto;

	/**
	 * Contains all the GpsGeom from Server
	 */
	public static ArrayList<GpsGeom> allGpsGeom = new ArrayList<GpsGeom>();

	/**
	 * Contains all Composed from Server
	 */
	public static ArrayList<Composed> allComposed = new ArrayList<Composed>();

	/**
	 * Contains all Elements from Server
	 */
	public static ArrayList<Element> allElement = new ArrayList<Element>();

	/**
	 * Contains all PixelGeom from Server
	 */
	public static ArrayList<PixelGeom> allPixelGeom = new ArrayList<PixelGeom>();

	/**
	 * Launch the sync to external DB (export mode)
	 * @param upload_photo
	 * @return Boolean if success of not
	 */
	public Boolean doSyncToExt(Boolean upload_photo)
	{
		Boolean success = false;
		try
		{
			BackTaskExportToExt export = new BackTaskExportToExt(upload_photo);
			export.execute();
			success = true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return success;
	}

	/**
	 *
	 * @return Boolean if success of not
	 */
	public Boolean getProjectsFromExt()
	{
		Boolean success = false;
		try
		{
			BackTaskImportProject BaProjectSync = new BackTaskImportProject();
			BaProjectSync.execute().get();
			success = true;
		}
		catch (Exception e)
		{}
		return success;
	}

	/**
	 *
	 * @return Boolean if success of not
	 */
	public Boolean getPhotosFromExt(long project_id)
	{
		Boolean success = false;
		refreshedValuesPhoto = new ArrayList<Photo>();
		try
		{
			BackTaskImportPhoto BaPhotoSync = new BackTaskImportPhoto(project_id);
			BaPhotoSync.execute().get();
			success = true;
		}
		catch (Exception e)
		{}
		return success;
	}

	/**
	 *
	 * @return Boolean if success of not
	 */
	public Boolean getTypeAndMaterialsFromExt()
	{
		Boolean success = false;
		try
		{
			BackTaskImportTypeMaterial BaTypeMaterialSync = new BackTaskImportTypeMaterial();
			BaTypeMaterialSync.execute().get();
			success = true;
		}
		catch (Exception e)
		{}
		return success;
	}

	/**
	 * Get the max id of each critical tables in external DB AND get current timestamp from database
	 * @return Hashmap of all max id
	 */
	public static HashMap<String, Integer> getMaxId() {
		maxId.clear();
		try {
			maxId = new BackTastMaxId().execute().get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return maxId;
	}

	public String projectID(int id){
		IsProjectIdExist projectIdExist = new IsProjectIdExist(id);
		try{
			Integer res = projectIdExist.execute().get();
			boolean res2 = MainActivity.datasource.existProjectWithId((long)id);
			String str  = null;
			if(res==1 && res2)
			{
				str = "External : Ok | Local : Ok";
			}else if(res==0 && res2)
			{
				str = "External : No | Local : Ok";
			} else if(res==1)
			{
				str = "External : Ok | Local : No";
			}else if (res==0)
			{
				str = "External : No | Local : No";
			}
			return str;
		}catch (InterruptedException | ExecutionException e){
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * The additional threat to upload data to the server
	 * @author Sebastien
	 *
	 */
	public class BackTaskExportToExt extends AsyncTask<Void, String, String> {

		private Context mContext;
		protected Boolean upload_photo=true;

		/**
		 * Constructor
		 * @param upload_photo
		 */
		public BackTaskExportToExt(Boolean upload_photo){
			super();
			this.mContext = MainActivity.baseContext;
			this.upload_photo = upload_photo;
		}

		/**
		 * Pre Execution orders
		 */
		protected void onPreExecute(){
			super.onPreExecute();
			Toast.makeText(MainActivity.baseContext,  "Début de l'exportation", Toast.LENGTH_SHORT).show();
		}

		/**
		 * The transformation in json and the sending to server
		 */
		protected String doInBackground(Void... params) {
			Gson gson = new Gson();
			String dataJson = gson.toJson(MainActivity.gpsGeom);
			String jSonComplete = "[{\"gpsgeom\":"+dataJson+"},";

			dataJson = gson.toJson(MainActivity.pixelGeom);
			jSonComplete += "{\"pixelgeom\":"+dataJson+"},";

			ArrayList<Photo> photos = new ArrayList<Photo>();
			photos.add(MainActivity.photo);
			dataJson = gson.toJson(photos);
			jSonComplete += "{\"photo\":"+dataJson+"},";

			dataJson = gson.toJson(MainActivity.project);
			jSonComplete += "{\"project\":"+dataJson+"},";

			dataJson = gson.toJson(MainActivity.composed);
			jSonComplete += "{\"composed\":"+dataJson+"},";

			dataJson = gson.toJson(MainActivity.element);
			jSonComplete += "{\"element\":"+dataJson+"}]";

			if (upload_photo){
				/**
				 * File upload request
				 */
				File mImage = new File(Environment.getExternalStorageDirectory(), "featureapp/"+MainActivity.photo.getPhoto_url());
				doFileUpload(mImage);
			}
			return postData(jSonComplete);
		}

		/**
		 * The posting method to server
		 * @param param the json data to send
		 * @return the string of the server response
		 */
		public String postData(String param) {
			HttpClient httpclient = new DefaultHttpClient();
			// specify the URL you want to post to
			HttpPost httppost = new HttpPost(MainActivity.serverURL+"registerDB.php");
			try {
				// create a list to store HTTP variables and their values
				List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>();
				// add an HTTP variable and value pair
				nameValuePairs.add(new BasicNameValuePair("myHttpData", param));
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,"UTF-8"));
				// send the variable and value, in other words post, to the URL
				HttpResponse response = httpclient.execute(httppost);

				StringBuilder sb = new StringBuilder();
				try {
					BufferedReader reader =
							new BufferedReader(new InputStreamReader(response.getEntity().getContent()), 65728);
					String line = null;
					while ((line = reader.readLine()) != null) {
						sb.append(line);
					}
				} catch (Exception e) { e.printStackTrace(); }
				return sb.toString();
			} catch (IOException e) {
				e.printStackTrace();
			} ;
			return null;
		}

		private boolean doFileUpload(File file) {
			HttpURLConnection connection = null;
			DataOutputStream outputStream = null;

			String pathToOurFile = file.getPath();
			String urlServer = MainActivity.serverURL+"uploadImage.php";
			String lineEnd = "\r\n";
			String twoHyphens = "--";
			String boundary = "*****";

			// log path to our file
			Log.i("DFHUPLOAD", pathToOurFile);

			int bytesRead, bytesAvailable, bufferSize;
			byte[] buffer;
			int maxBufferSize = 4*1024*1024;
			int sentBytes = 0;
			long fileSize = file.length();

			// log filesize
			String files= String.valueOf(fileSize);
			String buffers= String.valueOf(maxBufferSize);
			Log.i("DFHUPLOAD",files);
			Log.i("DFHUPLOAD",buffers);

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
					//for progress bar publishProgress((int)(sentBytes * 100 / fileSize));

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
		/**
		 * The things to execute after the backTask 
		 */
		protected void onPostExecute(String result) {
			if (result.equals("OK")){
				Toast.makeText(mContext, "Synchronisation avec la base : SUCCES", Toast.LENGTH_SHORT).show();
			}
			else {
				Toast.makeText(mContext, result, Toast.LENGTH_SHORT).show();
			}
			SaveFragment.dialog.dismiss();
		}
	}

	public class IsProjectIdExist extends AsyncTask<Void,Integer,Integer>{
		private Context mContext;
		protected Integer project_id;

		public IsProjectIdExist(int id){
			super();
			this.mContext = MainActivity.baseContext;
			project_id =id;
		}

		@Override
		protected Integer doInBackground(Void... params) {
			String JSON = getData();
			try{
				JSONObject jObj = new JSONObject(JSON);
				Integer exist;
				try{
					exist = jObj.getInt("exist");
					return exist;
				}catch(Exception e){
					return null;
				}
			}catch(JSONException e){
				Log.e("JSON Parser", "Error parsing data " + e.toString());
				return null;
			}
		}

		public String getData(){
			HttpClient httpclient = new DefaultHttpClient();
			// specify the URL you want to post to
			HttpPost httppost = new HttpPost(MainActivity.serverURL+"projectID.php");
			try {
				List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>();
				nameValuePairs.add(new BasicNameValuePair("project_id", project_id.toString()));
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,"UTF-8"));
				// send the variable and value, in other words post, to the URL
				HttpResponse response = httpclient.execute(httppost);

				StringBuilder sb = new StringBuilder();
				try {
					BufferedReader reader =
							new BufferedReader(new InputStreamReader(response.getEntity().getContent()), 65728);
					String line = null;

					while ((line = reader.readLine()) != null) {
						sb.append(line);
					}
				} catch (Exception e) { e.printStackTrace(); }

				return sb.toString();

			} catch (IOException e) {
				e.printStackTrace();
			} ;
			return "error";
		}
	}

	/**
	 * The additional threat to get the Max id of each tables on server AND the current timestamp for synchronise purpose
	 * @author Sebastien
	 *
	 */
	public static class BackTastMaxId extends AsyncTask<Void, HashMap<String, Integer>, HashMap<String, Integer>> {

		private Context mContext;

		/**
		 * Constructor
		 */
		public BackTastMaxId(){
			super();
			this.mContext = MainActivity.baseContext;
		}

		/**
		 * Ask the server and transform them to HashMap
		 */
		protected HashMap<String, Integer> doInBackground(Void... params) {

			String JSON = getData();
			try {
				JSONObject jObj = new JSONObject(JSON);
				HashMap<String, Integer> maxID = new HashMap<String, Integer>();
				try {
					maxID.put("Photo", jObj.getInt("photo"));
					maxID.put("GpsGeom", jObj.getInt("gpsgeom"));
					maxID.put("Element", jObj.getInt("element"));
					maxID.put("PixelGeom", jObj.getInt("pixelgeom"));
					maxID.put("Project", jObj.getInt("project"));
					maxID.put("date", jObj.getInt("date"));
				} catch (Exception e) {
					maxID.put("Photo", 0);
					maxID.put("GpsGeom", 0);
					maxID.put("Element", 0);
					maxID.put("PixelGeom", 0);
					maxID.put("Project", 0);
					maxID.put("date", jObj.getInt("date"));
				} finally {
					return maxID;
				}

			} catch (JSONException e) {
				Log.e("JSON Parser", "Error parsing data " + e.toString());
				return null;
			}
		}

		/**
		 * The request method to server
		 * @return the string of the server response
		 */
		public String getData() {
			HttpClient httpclient = new DefaultHttpClient();
			// specify the URL you want to post to
			HttpPost httppost = new HttpPost(MainActivity.serverURL+"maxID.php");
			try {
				// send the variable and value, in other words post, to the URL
				HttpResponse response = httpclient.execute(httppost);

				StringBuilder sb = new StringBuilder();
				try {
					BufferedReader reader =
							new BufferedReader(new InputStreamReader(response.getEntity().getContent()), 65728);
					String line = null;

					while ((line = reader.readLine()) != null) {
						sb.append(line);
					}
				} catch (Exception e) { e.printStackTrace(); }

				return sb.toString();

			} catch (IOException e) {
				e.printStackTrace();
			} ;
			return "error";
		}

		/**
		 * The things to execute after the backTask 
		 */
		protected void onPostExecute(HashMap<String, Integer> result) {
			try{
				Toast.makeText(mContext, "Connexion au serveur : OK", Toast.LENGTH_SHORT).show();
			}
			catch (Exception e) {
				Toast.makeText(mContext, "Erreur dans la communication avec le serveur", Toast.LENGTH_SHORT).show();
			}
		}
	}

	/**
	 * The additional threat to get projects and gpsgeom data from server
	 * @author Sebastien
	 *
	 */
	public static class BackTaskImportProject extends AsyncTask<Void, Void, Void> {

		private Context mContext;

		/**
		 * To get the informations of project and gpsGeom for all the projects on external DB server
		 */
		public BackTaskImportProject(){
			this.mContext = MainActivity.baseContext;
		}

		/**
		 * Ask the server and save project and gpsGeom on the var
		 *
		 */
		protected Void doInBackground(Void... params) {

			String JSON = getData();
			try {
				JSONArray jArr = new JSONArray(JSON);
				refreshedValues = new ArrayList<Project>();
				allGpsGeom = new ArrayList<GpsGeom>();

				JSONObject projects = jArr.getJSONObject(0);
				JSONArray projectsInner = projects.getJSONArray("Project");
				JSONObject gpsGeom = jArr.getJSONObject(1);
				JSONArray gpsGeomInner = gpsGeom.getJSONArray("GpsGeom");

				for(int i=0;i<projectsInner.length();i++)
				{
					JSONObject project = projectsInner.getJSONObject(i);
					long project_id = project.getLong("project_id");
					String project_name = project.getString("project_name");
					long gpsgeom_id = project.getLong("gpsgeom_id");

					Project projectEnCours = new Project();
					projectEnCours.setProjectId(project_id);
					projectEnCours.setProjectName(project_name);
					projectEnCours.setGpsGeom_id(gpsgeom_id);

					refreshedValues.add(projectEnCours);
				}
				for(int i=0;i<gpsGeomInner.length();i++)
				{
					JSONObject gpsgeom = gpsGeomInner.getJSONObject(i);
					long gpsGeom_id = gpsgeom.getLong("gpsGeom_id");
					String gpsGeom_the_geom = gpsgeom.getString("gpsGeom_the_geom");

					GpsGeom gpsGeomEnCours = new GpsGeom();
					gpsGeomEnCours.setGpsGeomId(gpsGeom_id);
					gpsGeomEnCours.setGpsGeomCoord(gpsGeom_the_geom);

					allGpsGeom.add(gpsGeomEnCours);
				}

			} catch (JSONException e) {
				Log.e("JSON Parser", "Error parsing data " + e.toString());
			}
			return null;
		}

		/**
		 * The request method to server
		 * @return the string of the server response
		 */
		public String getData() {
			HttpClient httpclient = new DefaultHttpClient();
			// specify the URL you want to post to
			HttpPost httppost = new HttpPost(MainActivity.serverURL+"import.php");
			try {

				// create a list to store HTTP variables and their values
				List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>();
				// add an HTTP variable and value pair
				nameValuePairs.add(new BasicNameValuePair("project", "all"));
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,"UTF-8"));
				// send the variable and value, in other words post, to the URL
				HttpResponse response = httpclient.execute(httppost);

				StringBuilder sb = new StringBuilder();
				try {
					BufferedReader reader =
							new BufferedReader(new InputStreamReader(response.getEntity().getContent()), 65728);
					String line = null;

					while ((line = reader.readLine()) != null) {
						sb.append(line);
					}
				} catch (Exception e) { e.printStackTrace(); }

				return sb.toString();

			} catch (IOException e) {
				e.printStackTrace();
			} ;
			return "error";
		}

	}

	/**
	 * The additional threat to get projects and gpsgeom data from server
	 * @author Sebastien
	 *
	 */
	public static class BackTaskImportPhoto extends AsyncTask<Void, Void, Void> {

		private Context mContext;
		private long project_id = 1;

		/**
		 * Default constructor
		 */
		public BackTaskImportPhoto(long project_id){
			this.mContext = MainActivity.baseContext;
			this.project_id = project_id;
		}

		/**
		 * Ask the server and save all data on the specific var
		 */
		protected Void doInBackground(Void... params) {

			String JSON = getData();
			try{
				JSONArray jArr = new JSONArray(JSON);
				refreshedValues = new ArrayList<Project>();
				allGpsGeom = new ArrayList<GpsGeom>();
				try {
					JSONObject projects = jArr.getJSONObject(0);
					JSONArray projectsInner = projects.getJSONArray("Project");

					for(int i=0;i<projectsInner.length();i++)
					{
						JSONObject project = projectsInner.getJSONObject(i);
						long project_id = project.getLong("project_id");
						String project_name = project.getString("project_name");
						long gpsgeom_id = project.getLong("gpsgeom_id");

						Project projectEnCours = new Project();
						projectEnCours.setProjectId(project_id);
						projectEnCours.setProjectName(project_name);
						projectEnCours.setGpsGeom_id(gpsgeom_id);

						refreshedValues.add(projectEnCours);
					}
				} catch (JSONException e) {
					Log.e("JSON Parser", "Error parsing data " + e.toString());
				}

				try{
					JSONObject photos = jArr.getJSONObject(1);
					JSONArray photoInner = photos.getJSONArray("Photo");

					for(int i=0;i<photoInner.length();i++)
					{
						JSONObject photo = photoInner.getJSONObject(i);
						long photo_id = photo.getLong("photo_id");
						String photo_descript = photo.getString("photo_description");
						String photo_url = photo.getString("photo_url");
						String photo_author = photo.getString("photo_author");
						long gpsgeom_id = photo.getLong("gpsGeom_id");
						int photo_nbr = photo.getInt("photo_nbrPoint");
						String photo_adresse = photo.getString("photo_adresse");
						int photo_date = photo.getInt("photo_derniereModif");

						Photo photoEnCours = new Photo();
						photoEnCours.setPhoto_id(photo_id);
						photoEnCours.setPhoto_author(photo_author);
						photoEnCours.setPhoto_description(photo_descript);
						photoEnCours.setPhoto_url(photo_url);
						photoEnCours.setGpsGeom_id(gpsgeom_id);
						photoEnCours.setPhoto_nbrPoints(photo_nbr);
						photoEnCours.setPhoto_adresse(photo_adresse);
						photoEnCours.setPhoto_derniereModif(photo_date);

						refreshedValuesPhoto.add(photoEnCours);
					}
				} catch (JSONException e) {
					Log.e("JSON Parser", "Error parsing data " + e.toString());
				}

				try{
					JSONObject composeds = jArr.getJSONObject(2);
					JSONArray composedInner = composeds.getJSONArray("Composed");

					for(int i=0;i<composedInner.length();i++)
					{
						JSONObject composed = composedInner.getJSONObject(i);
						long project_id = composed.getLong("project_id");
						long photo_id = composed.getLong("photo_id");

						Composed composedEnCours = new Composed();
						composedEnCours.setProject_id(project_id);
						composedEnCours.setPhoto_id(photo_id);

						allComposed.add(composedEnCours);
					}
				} catch (JSONException e) {
					Log.e("JSON Parser", "Error parsing data " + e.toString());
				}

				try{
					JSONObject gpsGeom = jArr.getJSONObject(3);
					JSONArray gpsGeomInner = gpsGeom.getJSONArray("GpsGeom");

					for(int i=0;i<gpsGeomInner.length();i++)
					{
						JSONObject gpsgeom = gpsGeomInner.getJSONObject(i);
						long gpsGeom_id = gpsgeom.getLong("gpsGeom_id");
						String gpsGeom_the_geom = gpsgeom.getString("gpsGeom_the_geom");

						GpsGeom gpsGeomEnCours = new GpsGeom();
						gpsGeomEnCours.setGpsGeomId(gpsGeom_id);
						gpsGeomEnCours.setGpsGeomCoord(gpsGeom_the_geom);

						allGpsGeom.add(gpsGeomEnCours);
					}
				} catch (JSONException e) {
					Log.e("JSON Parser", "Error parsing data " + e.toString());
				}

				try{
					JSONObject elements = jArr.getJSONObject(4);
					JSONArray elementInner = elements.getJSONArray("Element");

					for(int i=0;i<elementInner.length();i++)
					{
						JSONObject element = elementInner.getJSONObject(i);
						long element_id = element.getLong("element_id");
						long photo_id = element.getLong("photo_id");
						long material_id = element.getLong("material_id");
						long gpsGeom_id = element.getLong("gpsGeom_id");
						long pixelGeom_id = element.getLong("pixelGeom_id");
						long elementType_id = element.getLong("elementType_id");
						String element_color = element.getString("element_color");

						Element elementEnCours = new Element();
						elementEnCours.setElement_id(element_id);
						elementEnCours.setPhoto_id(photo_id);
						elementEnCours.setMaterial_id(material_id);
						elementEnCours.setGpsGeom_id(gpsGeom_id);
						elementEnCours.setPixelGeom_id(pixelGeom_id);
						elementEnCours.setElementType_id(elementType_id);
						elementEnCours.setElement_color(element_color);

						allElement.add(elementEnCours);
					}
				} catch (JSONException e) {
					Log.e("JSON Parser", "Error parsing data " + e.toString());
				}

				try{
					JSONObject pixelGeom = jArr.getJSONObject(5);
					JSONArray pixelInner = pixelGeom.getJSONArray("PixelGeom");

					for(int i=0;i<pixelInner.length();i++)
					{
						JSONObject pixel = pixelInner.getJSONObject(i);
						long pixelGeom_id = pixel.getLong("pixelGeom_id");
						String pixelGeom_the_geom = pixel.getString("pixelGeom_the_geom");

						PixelGeom pixelEnCours = new PixelGeom();
						pixelEnCours.setPixelGeomId(pixelGeom_id);
						pixelEnCours.setPixelGeom_the_geom(pixelGeom_the_geom);

						allPixelGeom.add(pixelEnCours);
					}

				} catch (JSONException e) {
					Log.e("JSON Parser", "Error parsing data " + e.toString());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			return null;
		}

		/**
		 * The request method to server
		 * @return the string of the server response
		 */
		public String getData() {
			HttpClient httpclient = new DefaultHttpClient();
			// specify the URL you want to post to
			HttpPost httppost = new HttpPost(MainActivity.serverURL+"import.php");
			try {

				// create a list to store HTTP variables and their values
				List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>();
				// add an HTTP variable and value pair
				nameValuePairs.add(new BasicNameValuePair("project_id", String.valueOf(project_id)));
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,"UTF-8"));
				// send the variable and value, in other words post, to the URL
				HttpResponse response = httpclient.execute(httppost);

				StringBuilder sb = new StringBuilder();
				try {
					BufferedReader reader =
							new BufferedReader(new InputStreamReader(response.getEntity().getContent()), 65728);
					String line = null;

					while ((line = reader.readLine()) != null) {
						Log.i(TAG,line);
						sb.append(line);
					}
				} catch (Exception e) { e.printStackTrace(); }

				return sb.toString();

			} catch (IOException e) {
				e.printStackTrace();
			} ;
			return "error";
		}

	}

	/**
	 * The additional threat to get projects and gpsgeom data from server
	 * @author Sebastien
	 *
	 */
	public static class BackTaskImportTypeMaterial extends AsyncTask<Void, Void, Void> {

		private Context mContext;

		/**
		 * Default constructor
		 */
		public BackTaskImportTypeMaterial(){
			this.mContext = MainActivity.baseContext;
		}

		/**
		 * Pre Execution orders
		 */
		protected void onPreExecute(){
			super.onPreExecute();
			Toast.makeText(MainActivity.baseContext,  "Début de la synchro", Toast.LENGTH_SHORT).show();
		}

		/**
		 * Ask the server and save all data on the specific var
		 */
		protected Void doInBackground(Void... params) {
			String JSON = getData();
			try {
				JSONArray jArr = new JSONArray(JSON);

				JSONObject materials = jArr.getJSONObject(0);
				JSONArray materialsInner = materials.getJSONArray("Material");

				for(int i=0;i<materialsInner.length();i++)
				{
					JSONObject project = materialsInner.getJSONObject(i);
					long material_id = project.getLong("material_id");
					String material_name = project.getString("material_name");
					long material_heat = project.getInt("material_heat_capa");
					long material_mass = project.getInt("material_mass_density");
					Double material_conductivity = project.getDouble("material_conduct");

					Material materialEnCours = new Material();
					materialEnCours.setMaterial_id(material_id);
					materialEnCours.setMaterial_name(material_name);
					materialEnCours.setMaterial_conduct(material_conductivity);
					materialEnCours.setMaterial_heat_capa(material_heat);
					materialEnCours.setMaterial_mass_density(material_mass);
					MainActivity.material.add(materialEnCours);
				}

				JSONObject types = jArr.getJSONObject(1);
				JSONArray typeInner = types.getJSONArray("ElementType");

				for(int i=0;i<typeInner.length();i++)
				{
					JSONObject photo = typeInner.getJSONObject(i);
					long elementType_id = photo.getLong("elementType_id");
					String elementType_name = photo.getString("elementType_name");

					ElementType elmtTypeEnCours = new ElementType();
					elmtTypeEnCours.setElementType_id(elementType_id );
					elmtTypeEnCours.setElementType_name(elementType_name);

					MainActivity.elementType.add(elmtTypeEnCours);
				}
			} catch (JSONException e) {
				Log.e("JSON Parser", "Error parsing data " + e.toString());
			}
			return null;
		}

		/**
		 * The request method to server
		 * @return the string of the server response
		 */
		public String getData() {
			HttpClient httpclient = new DefaultHttpClient();
			// specify the URL you want to post to
			HttpPost httppost = new HttpPost(MainActivity.serverURL+"importMaterial.php");
			try {

				// create a list to store HTTP variables and their values
				List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>();
				// add an HTTP variable and value pair
				nameValuePairs.add(new BasicNameValuePair("material", "coincoin"));
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,"UTF-8"));
				// send the variable and value, in other words post, to the URL
				HttpResponse response = httpclient.execute(httppost);

				StringBuilder sb = new StringBuilder();
				try {
					BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()), 65728);
					String line = null;

					while ((line = reader.readLine()) != null) {
						sb.append(line);
					}
				} catch (Exception e) { e.printStackTrace(); }

				return sb.toString();

			} catch (IOException e) {
				e.printStackTrace();
			} ;
			return "error";
		}

		/**
		 * The things to execute after the backTask 
		 */
		protected void onPostExecute() {
			Toast.makeText(MainActivity.baseContext, "Elements chargées dans la base de données", Toast.LENGTH_SHORT).show();
			HomeFragment.dialogMater.dismiss();
		}
	}
}