package com.ecn.urbapp.db;

import java.util.ArrayList;
import java.util.Vector;

import android.content.ContentValues;
import android.database.Cursor;

import com.ecn.urbapp.activities.MainActivity;
import com.ecn.urbapp.syncToExt.Sync;

public class PixelGeom extends DataObject  {

	
	//Attributes
	/**
	 * long id attributes of pixelGeom
	 */
	private long pixelGeom_id;
	/**
	 * String polygon attributes of pixelGeom
	 */
	private String pixelGeom_the_geom;
	/**
	 * boolean attributes of pixelGeom that determines if a pixelGeom is selected or not
	 */
	public boolean selected;

	/**
	 * linked list of pixelGeom
	 */
	private Vector<PixelGeom> linkedPixelGeom = new Vector<PixelGeom>();
	
	//Getters
	/**
	 * getter for pixelGeom id
	 * @return long pixelGeom_id
	 */
	public long getPixelGeomId(){
		return pixelGeom_id;
	}
	/**
	 * getter for pixelGeom isSelected
	 * @return boolean isSelected
	 */
	public boolean isSelected() {
		return selected;
	}
	
	/**
	 * getter for the pixelGeom polygon geometry
	 * @return String geom
	 */
	public String getPixelGeom_the_geom() {
		return pixelGeom_the_geom;
	}
		
	//Setters
	/**
	 * setter PixelGeom Id
	 * @param id
	 */
	public void setPixelGeomId(long id) {
		this.pixelGeom_id = id;
	}
	/**
	 * setter PixelGeom Selected
	 * @param selected
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	/**
	 * setter of pixelgeom polygon
	 * @param pixelGeom_the_geom
	 */
	public void setPixelGeom_the_geom(String pixelGeom_the_geom) {
		this.pixelGeom_the_geom = pixelGeom_the_geom;
	}

	
	
	//Override methods
	@Override
	public String toString() {
		return "pixelGeom_id =" + this.pixelGeom_id + "&" + "\ncoord =" + this.pixelGeom_the_geom ;
		
	}

	@Override
	public void saveToLocal(LocalDataSource datasource) {
		ContentValues values = new ContentValues(); 
		
		values.put(MySQLiteHelper.COLUMN_PIXELGEOMCOORD, this.pixelGeom_the_geom);
		
		if(this.registredInLocal){
			/*String[] s=new String[1];
			s[0]= ""+this.pixelGeom_id;
			datasource.getDatabase().update(MySQLiteHelper.TABLE_PIXELGEOM, values, MySQLiteHelper.COLUMN_PIXELGEOMID,s );
*/
			String str = "pixelGeom_id "+"="+this.pixelGeom_id;
			datasource.getDatabase().update(MySQLiteHelper.TABLE_PIXELGEOM, values, str, null);
		}
		else{
			Cursor cursor = datasource.getDatabase().rawQuery(GETMAXPIXELGEOMID, null);
			cursor.moveToFirst();
			if(!cursor.isAfterLast()){
				long old_id = this.getPixelGeomId();
				//long new_id = 1+cursor.getLong(0);
				long new_id = 1+this.pixelGeom_id+Sync.getMaxId().get("PixelGeom");
				this.setPixelGeomId(new_id);
				this.trigger(old_id, new_id, MainActivity.element);
				
			}
			values.put(MySQLiteHelper.COLUMN_PIXELGEOMID, this.pixelGeom_id);
			datasource.getDatabase().insert(MySQLiteHelper.TABLE_PIXELGEOM, null, values);
			this.setRegistredInLocal(true);
		}
	}
	
	/**
	 * query to get the biggest photo_id from local db
	 * 
	 */
	private static final String
		GETMAXPIXELGEOMID = 
			"SELECT "+MySQLiteHelper.TABLE_PIXELGEOM+"."+MySQLiteHelper.COLUMN_PIXELGEOMID+" FROM "
			+ MySQLiteHelper.TABLE_PIXELGEOM 
			+" ORDER BY "+MySQLiteHelper.TABLE_PIXELGEOM+"."+MySQLiteHelper.COLUMN_PIXELGEOMID
			+" DESC LIMIT 1 ;"
		;

	/**
	 * trigger method is used to update foreign keys in the dataObjects
	 * this method is used before saving objects in database thank's to the "saved fragment"
	 * @param old_id represents the past id of our GpsGeom
	 * @param new_id represents the new id of our GpsGeom
	 * @param list_element represents the projects that are related to this gpsGeom
	 */
	public void trigger(long old_id, long new_id,  ArrayList<Element> list_element){
		if (list_element!=null){
			for (Element e : list_element){
				if(e.getPixelGeom_id()==old_id){
					e.setPixelGeom_id(new_id);
				}
			}
			
		}
		
	}
	/**
	 * setter for the list of pixelGeom
	 * @param selectedPixelGeom
	 */
	public void setLinkedPixelGeom(Vector<PixelGeom> selectedPixelGeom) {
		linkedPixelGeom = selectedPixelGeom;
	}
	
	/**
	 * getter for the list of pixelGeom
	 * @return
	 */
	public Vector<PixelGeom> getLinkedPixelGeom() {
		return linkedPixelGeom;
	}
}