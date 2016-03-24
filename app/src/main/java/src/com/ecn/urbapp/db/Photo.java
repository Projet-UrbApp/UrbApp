package src.com.ecn.urbapp.db;

import java.util.ArrayList;

import android.content.ContentValues;

import src.com.ecn.urbapp.activities.MainActivity;
import src.com.ecn.urbapp.syncToExt.Sync;

public class Photo extends DataObject  {
	
	/**
	 * contains the id of the photo
	 */
	private long photo_id;
	/**
	 * contains the description of the photo
	 */
	private String photo_description;
	/**
	 * contains the author of the photo
	 */
	private String photo_author;
	/**
	 * attributes that declare the name of the picture for instance : img1.png
	 */
	private String photo_url;
	/**
	 * contains the address of the photo
	 */
	private String photo_adresse;
	/**
	 * contains the number of points that have to been set to geolocalized the photo
	 * set to 2 by default
	 */
	private long photo_nbrPoints=2;
	/**
	 * contains the last modification
	 * set to 0 by default
	 */
	
	private int photo_derniereModif=0;
	
	/**
	 * contains the tempurl of the photo
	 */
	private String photo_urlTemp;
	/**
	 * contains the gpsgeom_id
	 */
	private long gpsGeom_id;

	//Getters
	/**
	 * contains the localization 
	 */
	private String Ext_GpsGeomCoord;

	/**
	 * getter for the temp url
	 * @return
	 */
	public String getUrlTemp() {
		return photo_urlTemp;
	}
	/**
	 * setter for the tempurl
	 * @param s
	 */
	public void setUrlTemp(String s) {
		photo_urlTemp=s;
	}
	/**
	 * getter for the photo id
	 * @return
	 */
	public long getPhoto_id() {
		return photo_id;
	}

	/**
	 * getter for the description
	 * @return
	 */
	public String getPhoto_description() {
		return photo_description;
	}

	/**
	 * getter for the author
	 * @return
	 */
	public String getPhoto_author() {
		return photo_author;
	}
	public String getPhoto_url() {
		return photo_url;
	}

	
	/**
	 * getter for the gpsgeom id
	 * @return
	 */
	public long getGpsGeom_id() {
		return gpsGeom_id;
	}

	/**
	 * setter for the id of the gpsgeom
	 * @param gpsGeom_id
	 */
	public void setGpsGeom_id(long gpsGeom_id) {
		this.gpsGeom_id = gpsGeom_id;
	}


	/**
	 * getter for localisation fo the photo
	 * @return
	 */
	public String getExt_GpsGeomCoord() {
		return Ext_GpsGeomCoord;
	}


	/**
	 * getter for the date of last modification
	 * @return
	 */
	public int getPhoto_derniereModif() {
		return photo_derniereModif;
	}

	/**
	 * getter for the number of points
	 * @return
	 */
	public long getPhoto_nbrPoints() {
		return photo_nbrPoints;
	}

	/**
	 * getter fr the address
	 * @return
	 */
	public String getPhoto_adresse() {
		return photo_adresse;
	}
	
		
		
		
	//Setters
	/**
	 * setter for the localization of the photo
	 * @param ext_GpsGeomCoord
	 */
	public void setExt_GpsGeomCoord(String ext_GpsGeomCoord) {
		Ext_GpsGeomCoord = ext_GpsGeomCoord;
	}

	
	/**
	 * setter for the url of the photo
	 * @param photo_url
	 */
	public void setPhoto_url(String photo_url) {
		this.photo_url = photo_url;
	}

	/**
	 * setter for the id of the photo
	 * @param photo_id
	 */
	public void setPhoto_id(long photo_id) {
		this.photo_id = photo_id;
	}

	/**
	 * setter for the description
	 * @param photo_description
	 */
	public void setPhoto_description(String photo_description) {
		this.photo_description = photo_description;
	}

	/**
	 * setter for the author
	 * @param photo_author
	 */
	public void setPhoto_author(String photo_author) {
		this.photo_author = photo_author;
	}
	
	/**
	 * setter for the date of last modification
	 * @param d
	 */
	public void setPhoto_derniereModif(int d) {
		this.photo_derniereModif=d;
	}
	
	/**
	 * setter for the number of points that will be geolocalized
	 * @param nbr
	 */
	public void setPhoto_nbrPoints(long nbr) {
		this.photo_nbrPoints=nbr;
	}
	
	/**
	 * setter for the adresse of the Photo
	 * @param adresse
	 */
	public void setPhoto_adresse(String adresse) {
		this.photo_adresse=adresse;
	}



	
	

	//Override methods
	@Override
	public String toString() {
		return "Photo [photo_id=" + photo_id + ", photo_description="
				+ photo_description + ", photo_author=" + photo_author
				+ ", photo_url=" + photo_url + ", gps_Geom_id=" + gpsGeom_id +"&" + "  position =" + this.Ext_GpsGeomCoord
				+ "]";
	}

	@Override
	public void saveToLocal(LocalDataSource datasource) {
		ContentValues values = new ContentValues(); 
		
		values.put(MySQLiteHelper.COLUMN_PHOTOURL, this.photo_url);
		values.put(MySQLiteHelper.COLUMN_PHOTODESCRIPTION,this.photo_description);
		values.put(MySQLiteHelper.COLUMN_PHOTOAUTHOR, this.photo_author);
		values.put(MySQLiteHelper.COLUMN_PHOTOADRESSE, this.photo_adresse);
		values.put(MySQLiteHelper.COLUMN_PHOTONBRPOINTS,this.photo_nbrPoints);
		values.put(MySQLiteHelper.COLUMN_PHOTODERNIEREMODIF, this.photo_derniereModif);
		
		if(this.registredInLocal){
			String str = "photo_id "+"="+this.photo_id;
			datasource.getDatabase().update(MySQLiteHelper.TABLE_PHOTO, values, str, null);
		}
		else{
			//Cursor cursor = datasource.getDatabase().rawQuery(GETMAXPHOTOID, null);
			//cursor.moveToFirst();

			long old_id = this.getPhoto_id();
			//long new_id = 1+cursor.getLong(0);
			long new_id = Sync.getMaxId().get("Photo")+1;
			this.setPhoto_id(new_id);
			this.trigger(old_id, new_id, MainActivity.element, MainActivity.composed);

			values.put(MySQLiteHelper.COLUMN_GPSGEOMID, this.gpsGeom_id);
			datasource.getDatabase().insert(MySQLiteHelper.TABLE_PHOTO, null, values);
		}
	}

	/**
	 * query to get the biggest photo_id from local db
	 * 
	 */
	private static final String
		GETMAXPHOTOID = 
			"SELECT "+MySQLiteHelper.TABLE_PHOTO+"."+MySQLiteHelper.COLUMN_PHOTOID+" FROM "
			+ MySQLiteHelper.TABLE_PHOTO 
			+" ORDER BY "+MySQLiteHelper.TABLE_PHOTO+"."+MySQLiteHelper.COLUMN_PHOTOID
			+" DESC LIMIT 1 ;"
		;

	/**
	 * trigger method is used to update foreign keys in the dataObjects
	 * this method is used before saving objects in database thank's to the "saved fragment"
	 * @param old_id
	 * @param new_id
	 * @param list_element
	 * @param list_composed
	 */
	public void trigger(long old_id, long new_id, ArrayList<Element> list_element, ArrayList<Composed> list_composed ){

		if (list_element!=null){
			for (Element e : list_element){
				if(e.getPhoto_id()==old_id){
					e.setPhoto_id(new_id);
				}
			}
			
		}
		if (list_composed!=null){
			for (Composed c : list_composed){
				if(c.getPhoto_id()==old_id){
					c.setPhoto_id(new_id);
				}
			}
			
		}
		
	}
}