package src.com.ecn.urbapp.db;

import java.util.ArrayList;

import android.content.ContentValues;

import src.com.ecn.urbapp.activities.MainActivity;
import src.com.ecn.urbapp.syncToExt.Sync;


public class Project extends DataObject {

	private static final String TAG = "projet";
	//Attributes
	/**
	 * long project id attributes 
	 */
	private long project_id;
	/**
	 * String project name attributes
	 */
	private String project_name;
	/**
	 * long id of the gpsgeom that locates to the project
	 */
	private long gpsGeom_id;
	/**
	 * value String that contains the value of the GpsGeom designed by the previous attribute
	 */
	private String Ext_GpsGeomCoord;

	
	//Getters
	
	/**
	 * getter for gpsgeom id
	 * @return the id of the gpsgeom
	 */
	public long getGpsGeom_id() {
		return gpsGeom_id;
	}
	
	/**
	 * getter for the project id
	 * @return the id of the project
	 */
	public long getProjectId(){
		return project_id;
	}
	
	/**
	 * getter for the name of the project
	 * @return the name of the project
	 */
	public String getProjectName() {
		return project_name;
	}
	
	/**
	 * get the value of the gpsgeom
	 * @return String gpsgeom
	 */
	public String getExt_GpsGeomCoord() {
		return Ext_GpsGeomCoord;
	}

	//Setters
	/**
	 * setter for the gpsgeomcoord
	 * @param ext_GpsGeomCoord
	 */
	public void setExt_GpsGeomCoord(String ext_GpsGeomCoord) {
		Ext_GpsGeomCoord = ext_GpsGeomCoord;
	}

	/**
	 * setter for the gpsgeom id
	 * @param gpsGeom_id
	 */
	public void setGpsGeom_id(long gpsGeom_id) {
		this.gpsGeom_id = gpsGeom_id;
	}

	/**
	 * setter for the name of the project
	 * @param str
	 */
	public void setProjectName(String str) {
		this.project_name = str;
	}

	/**
	 * setter for the project id
	 * @param id
	 */
	public void setProjectId(long id) {
		this.project_id = id;
	}


	//Override methods
	@Override
	public String toString() {
		return project_name + " " +project_id;
	}

	@Override
	public void saveToLocal(LocalDataSource datasource) {
		ContentValues values = new ContentValues(); 
		
		values.put(MySQLiteHelper.COLUMN_PROJECTNAME, this.project_name);
		
			
		if(this.registredInLocal){
			/*String[] s=new String[1];
			s[0]= ""+this.project_id;
			datasource.getDatabase().update(MySQLiteHelper.TABLE_PROJECT, values, MySQLiteHelper.COLUMN_PROJECTID,s );
			*/

			String str = "project_id "+"="+this.project_id;
			datasource.getDatabase().update(MySQLiteHelper.TABLE_PROJECT, values, str, null);
		}
		else{
			//Cursor cursor = datasource.getDatabase().rawQuery(GETMAXPROJECTID, null);
			//cursor.moveToFirst();
			long old_id = this.getProjectId();
			//long new_id = 1+cursor.getLong(0);
			Sync maxSync = new Sync();
			long new_id = (long) maxSync.getMaxId().get("Project")+1; //1+this.project_id+Sync.getMaxId().get("Project");
			this.setProjectId(new_id);
			this.trigger(old_id, new_id, MainActivity.composed);

			values.put(MySQLiteHelper.COLUMN_PROJECTID, new_id);
			values.put(MySQLiteHelper.COLUMN_GPSGEOMID, this.gpsGeom_id);
			datasource.getDatabase().insert(MySQLiteHelper.TABLE_PROJECT, null, values);
			this.setRegistredInLocal(true);
		}
	}	

	/**
	 * query to get the biggest photo_id from local db
	 * 
	 */
	private static final String
		GETMAXPROJECTID = 
			"SELECT "+MySQLiteHelper.TABLE_PROJECT+"."+MySQLiteHelper.COLUMN_PROJECTID+" FROM "
			+ MySQLiteHelper.TABLE_PROJECT
			+" ORDER BY "+MySQLiteHelper.TABLE_PROJECT+"."+MySQLiteHelper.COLUMN_PROJECTID
			+" DESC LIMIT 1 ;"
		;
	/**
	 * trigger method is used to update foreign keys in the dataObjects
	 * this method is used before saving objects in database thank's to the "saved fragment"
	 * @param old_id
	 * @param new_id
	 * @param list_composed
	 */
	public void trigger(long old_id, long new_id, ArrayList<Composed> list_composed ){

		if (list_composed!=null){
			for (Composed c : list_composed){
				if(c.getProject_id()==old_id){
					c.setProject_id(new_id);
				}
			}
		}
	}
}