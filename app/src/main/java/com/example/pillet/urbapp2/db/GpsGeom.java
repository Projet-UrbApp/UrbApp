package com.example.pillet.urbapp2.db;

import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;

import com.example.pillet.urbapp2.activities.MainActivity;
import com.example.pillet.urbapp2.syncToExt.Sync;

public class GpsGeom extends DataObject{
	
	
	//Attributes
	/**
	 * long id of the gpsGeom
	 */
	private long gpsGeom_id;
	/**
	 * String that describes a the_geom dataType in postgis
	 */
	private String gpsGeom_the_geom;
	/**
	 * Field containing the addressof the GpsGeom.
	 */
	private String gpsGeom_address;

	//Getters
	/**
	 * getter for gpsGeom_id
	 * @return long gpsGeom_id
	 */
	public long getGpsGeomsId(){
		return gpsGeom_id;
	}

	/**
	 * getter for gpsGeom geom
	 * @return String gpsGeom_the_geom;
	 */
	public String getGpsGeomCord() {
		return gpsGeom_the_geom;
	}
	
	/**
	 * getter for gpsGeom_address
	 * @return String gpsGeom_address;
	 */
	public String getAddress(){
		return gpsGeom_address;
	}	
	
	//Setters
	/**
	 * setter for gpsGeom_id
	 * @param id
	 */
	public void setGpsGeomId(long id) {
		this.gpsGeom_id = id;
	}

	/**
	 * setter for gpsGeom_the_geom
	 * @param str which will be gpsGeom_the_geom;
	 */
	public void setGpsGeomCoord(String str) {
		this.gpsGeom_the_geom = str;
	}
	
	/**
	 * setter for gpsGeom_address
	 * @param  s which will be gpsGeom_address;
	 */
	public void setAddress(String s){
		gpsGeom_address=s;
	}
	

	
	
	//Ovveride methods
	@Override
	public String toString() {
		return "gpsGeom_id =" + this.gpsGeom_id + "&" + "\n gpsGeom_the_geom =" + this.gpsGeom_the_geom  + "&" + "\n coord =" + this.gpsGeom_id;
		
	}

	@Override
	public void saveToLocal(LocalDataSource datasource) {
		ContentValues values = new ContentValues(); 
		
		values.put(MySQLiteHelper.COLUMN_GPSGEOMCOORD, this.gpsGeom_the_geom);
		
		if(this.registredInLocal){
			//String[] s=new String[1];
			//s[0]= ""+this.gpsGeom_id;
			
			String str = "gpsGeom_id "+"="+this.gpsGeom_id;
			//datasource.getDatabase().update(MySQLiteHelper.TABLE_GPSGEOM, values, MySQLiteHelper.COLUMN_GPSGEOMID,s );
			datasource.getDatabase().update(MySQLiteHelper.TABLE_GPSGEOM, values, str, null);
		}
		else{
			
			Cursor cursor = datasource.getDatabase().rawQuery(GETMAXGPSGEOMID, null);
			cursor.moveToFirst();
			if(!cursor.isAfterLast()){
				long old_id = this.getGpsGeomsId();
				//long new_id = 1+cursor.getLong(0);
				long new_id = 1+Sync.getMaxId().get("GpsGeom")+this.gpsGeom_id;
				this.setGpsGeomId(new_id);
				this.trigger(old_id, new_id, MainActivity.photo, MainActivity.project, MainActivity.element);
			}

			datasource.getDatabase().insert(MySQLiteHelper.TABLE_GPSGEOM, null, values);
			this.setRegistredInLocal(true);
		}
	}		
	
	/**
	 * query to get the biggest GpsGeom_id from local db
	 * 
	 */
	private static final String
		GETMAXGPSGEOMID = 
			"SELECT "+MySQLiteHelper.TABLE_GPSGEOM+"."+MySQLiteHelper.COLUMN_GPSGEOMID+" FROM "
			+ MySQLiteHelper.TABLE_GPSGEOM 
			+" ORDER BY "+ MySQLiteHelper.TABLE_GPSGEOM+"."+MySQLiteHelper.COLUMN_GPSGEOMID
			+" DESC LIMIT 1 ;"
		;
	
	/**
	 * trigger method is used to update foreign keys in the dataObjects
	 * this method is used before saving objects in database thank's to the "saved fragment"
	 * @param old_id represents the past id of our GpsGeom
	 * @param new_id represents the new id of our GpsGeom
	 * @param photo represents the photo which we are working on and therefore is related to this gpsGeom
	 * @param list_projet represents the projects that are related to this gpsGeom
	 * @param list_element represents the projects that are related to this gpsGeom
	 */
	public void trigger(long old_id, long new_id, Photo photo, ArrayList<Project> list_projet, ArrayList<Element> list_element ){
		/**
		 * we update gpsGeom_id from past to new in the photo that is geolocalised by this gpsGeom
		 */
		if (photo.getGpsGeom_id() == old_id){
			photo.setGpsGeom_id(new_id);
		}
		/**
		 * we update gpsGeom_id from past to new in the projects that are geolocalised by this gpsGeom
		 */
		if (list_projet!=null){
			for (Project p : list_projet){
				if(p.getGpsGeom_id()==old_id){
					p.setGpsGeom_id(new_id);
				}
			}
		}
		/**
		 * we update gpsGeom_id from past to new in the elements that are geolocalised by this gpsGeom
		 */
		if (list_element!=null){
			for (Element e : list_element){
				if(e.getGpsGeom_id()==old_id){
					e.setGpsGeom_id(new_id);
				}
			}
		}
	}
}