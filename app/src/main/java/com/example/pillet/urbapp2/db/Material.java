package com.example.pillet.urbapp2.db;

import android.content.ContentValues;

public class Material extends DataObject  {

	//Attributes
	/**
	 * long id of material
	 */
	private long material_id;
	/**
	 * String name of the material
	 */
	private String material_name;
	/**
	 * String value of conductivity 
	 */
	private double material_conduct;
	/**
	 * String value of heat capacity
	 */
	private long material_heat_capa;
	/**
	 * String value of mass density
	 */
	private long material_mass_density;
	
	//Getters
	/**
	 * getter for the attribute id of material
	 * @return long material_id
	 */
	public long getMaterial_id() {
		return material_id;
	}
	
	/**
	 * getter for the attribute name of material
	 * @return String material_name
	 */
	public String getMaterial_name() {
		return material_name;
	}
	
	/**
	 * getter for the attribute conductivity of material
	 * @return float material_conduct
	 */
	public double getMaterial_conduct() {
		return material_conduct;
	}
	
	/**
	 * getter for the attribute heat capacity of material
	 * @return long material_heat_capa
	 */
	public long getMaterial_heat_capa() {
		return material_heat_capa;
	}
	
	/**
	 * getter for the attribute mass density of material
	 * @return long material_conduct
	 */
	public long getMaterial_mass_density() {
		return material_mass_density;
	}
	
	
	//Setters
	/**
	 * setter for the id of material
	 * @param material_id long
	 */
	public void setMaterial_id(long material_id) {
		this.material_id = material_id;
	}

	/**
	 * setter for the name of material
	 * @param material_name String
	 */
	public void setMaterial_name(String material_name) {
		this.material_name = material_name;
	}
	
	/**
	 * setter for the conductivity of material
	 * @param material_conduct float
	 */
	public void setMaterial_conduct(double material_conduct) {
		this.material_conduct = material_conduct;
	}
	
	/**
	 * setter for the heat capacity of material
	 * @param material_heat_capa long
	 */
	public void setMaterial_heat_capa(long material_heat_capa) {
		this.material_heat_capa = material_heat_capa;
	}
	
	/**
	 * setter for the mass density of material
	 * @param material_mass_density long
	 */
	public void setMaterial_mass_density(long material_mass_density) {
		this.material_mass_density = material_mass_density;
	}
	
	//Override Methods
	@Override
	public String toString() {
		return "Material [material_id=" + material_id + ", material_name="
				+ material_name + ", material_conduct=" + material_conduct
				+ ", material_heat_capa=" + material_heat_capa
				+ ", material_mass_density=" + material_mass_density + "]";
	}

	/**
	 * Save to local database
	 * @param datasource is the Localdatasource of which DataObject is a table
	 */
	@Override
	public void saveToLocal(LocalDataSource datasource) {
		ContentValues values = new ContentValues();
		
		values.put(MySQLiteHelper.COLUMN_MATERIALNAME, this.material_name);
		
		if(this.registredInLocal){
			String[] s=new String[1];
			s[0]= ""+this.material_id;
			datasource.getDatabase().update(MySQLiteHelper.TABLE_MATERIAL, values, MySQLiteHelper.COLUMN_MATERIALID, s);
		}
		else{
			datasource.getDatabase().insert(MySQLiteHelper.TABLE_MATERIAL, null, values);
		}
	}
}