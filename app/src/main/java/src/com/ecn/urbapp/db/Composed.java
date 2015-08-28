package src.com.ecn.urbapp.db;

import android.content.ContentValues;

public class Composed extends DataObject{

	//Attributes
	/**
	 * attribute that refers to the project_id that is relatated to the photo
	 */
	private long project_id;

	/**
	 * attribute that refers to the photo_id that is relatated to the project_id
	 */
	private long photo_id;
	
	//Getters
	/**
	 * getter of the project_id
	 * @return long id of the project
	 */
	public long getProject_id() {
		return project_id;
	}
	
	/**
	 * getter of the photo_id
	 * @return long id of the photo
	 */
	public long getPhoto_id() {
		return photo_id;
	}
	
	//Setters
	/**
	 * setter for the project_id
	 * @param project_id
	 */
	public void setProject_id(long project_id) {
		this.project_id = project_id;
	}

	/**
	 * setter for the photo_id
	 * @param photo_id
	 */
	public void setPhoto_id(long photo_id) {
		this.photo_id = photo_id;
	}
	
	//Override Methods
	@Override
	public String toString() {
		return "Composed [project_id=" + project_id + ", photo_id=" + photo_id
				+ "]";
	}

	/**
	 * save to local database
	 * @param datasource is the Localdatasource of which DataObject is a table
	 */
	@Override
	public void saveToLocal(LocalDataSource datasource) {
		ContentValues values = new ContentValues(); 
		values.put(MySQLiteHelper.COLUMN_PROJECTID, this.project_id);
		values.put(MySQLiteHelper.COLUMN_PHOTOID, this.photo_id);
		
		if(!this.registredInLocal){
			datasource.getDatabase().insert(MySQLiteHelper.TABLE_COMPOSED, null, values);
		}else
		{
			datasource.getDatabase().update(MySQLiteHelper.TABLE_COMPOSED,values,null,null);
		}
	}
}