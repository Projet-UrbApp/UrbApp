package com.example.pillet.urbapp2.db;

import android.content.ContentValues;

public class ElementType extends DataObject {

	
	//Attributes
	/**
	 * long id of the elementType
	 */
	private long elementType_id;
	/**
	 * name that describes the elementType (for instance grass, glass ...)
	 */
	private String elementType_name;
	
	
	
	
	//Getters
	/**
	 * getter for the elementType_id
	 * @return long elementType_id
	 */
	public long getElementType_id() {
		return elementType_id;
	}
	
	/**
	 * getter for the elementType_name
	 * @return String elementType_name
	 */
	public String getElementType_name() {
		return elementType_name;
	}

	
	
	
	
	//Setters
	/**
	 * setter for the elementType_id
	 * @param long elementType_id
	 */
	public void setElementType_id(long elementType_id) {
		this.elementType_id = elementType_id;
	}
	
	/**
	 * setter for the elementType_name
	 * @param String elementType_name
	 */
	public void setElementType_name(String elementType_name) {
		this.elementType_name = elementType_name;
	}
	
	
	//Override Methods
	@Override
	public String toString() {
		return "ElementType [elementType_id=" + elementType_id
				+ ", elementType_name=" + elementType_name + "]";
	}

	@Override
	public void saveToLocal(LocalDataSource datasource) {
		ContentValues values = new ContentValues(); 

		values.put(MySQLiteHelper.COLUMN_ELEMENTTYPENAME, this.elementType_name);
		
		if(this.registredInLocal){
			String[] s=new String[1];
			s[0]= ""+this.elementType_id;
			datasource.getDatabase().update(MySQLiteHelper.TABLE_ELEMENTTYPE, values, MySQLiteHelper.COLUMN_ELEMENTTYPEID,s );
		}
		else{
			values.put(MySQLiteHelper.COLUMN_ELEMENTTYPEID, this.elementType_id);
			datasource.getDatabase().insert(MySQLiteHelper.TABLE_ELEMENTTYPE, null, values);
		}
	}
	
}