package com.example.pillet.urbapp2.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteHelper extends SQLiteOpenHelper {

		/**
		 * declaration of tables
		 */
		public static final String TABLE_PROJECT = "Project";
		public static final String TABLE_GPSGEOM = "GpsGeom";
		public static final String TABLE_PIXELGEOM = "PixelGeom";
		public static final String TABLE_PHOTO = "Photo";
		public static final String TABLE_MATERIAL = "Material";
		public static final String TABLE_ELEMENTTYPE = "ElementType";
		public static final String TABLE_COMPOSED = "Composed";
		public static final String TABLE_ELEMENT = "Element";

		//creation of tables getters
		/**
		 * get the name of Table project
		 * @return String which is the name of table project
		 */
		public static String getTableProject() {
			return TABLE_PROJECT;
		}

		/**
		 * get the name of Table pixelgeom
		 * @return String which is the name of table pixelgeom
		 */
		public static String getTablePixelgeom() {
			return TABLE_PIXELGEOM;
		}

		/**
		 * get the name of Table photo
		 * @return String which is the name of table photo
		 */
		public static String getTablePhoto() {
			return TABLE_PHOTO;
		}

		/**
		 * get the name f Table material
		 * @return String which is the name of table material
		 */
		public static String getTableMaterial() {
			return TABLE_MATERIAL;
		}

		/**
		 * get the name f Table elementType
		 * @return String which is the name of table elementType
		 */
		public static String getTableElementtype() {
			return TABLE_ELEMENTTYPE;
		}

		/**
		 * get the name f Table composed
		 * @return String which is the name of table composed
		 */
		public static String getTableComposed() {
			return TABLE_COMPOSED;
		}

		/**
		 * get the name f Table element
		 * @return String which is the name of table element
		 */
		public static String getTableElement() {
			return TABLE_ELEMENT;
		}

		/**
		 * get the name f Table gpsgeom
		 * @return String which is the name of table gpsgeom
		 */
		public static String getTableGpsgeom() {
			return TABLE_GPSGEOM;
		}

		/**
		 * names of the columns of the whole database
		 */
		
		public static final String COLUMN_PROJECTID = "project_id";
		public static final String COLUMN_PROJECTNAME = "project_name";
		//PROJECTGPSGEOM refers to GPSGEOM
		
		public static final String COLUMN_GPSGEOMID = "gpsGeom_id";
		public static final String COLUMN_GPSGEOMCOORD = "gpsGeom_the_geom";
		
		public static final String COLUMN_PIXELGEOMID = "pixelGeom_id";
		public static final String COLUMN_PIXELGEOMCOORD = "pixelGeom_the_geom";
		
		public static final String COLUMN_PHOTOID = "photo_id";
		public static final String COLUMN_PHOTODESCRIPTION = "photo_description";
		public static final String COLUMN_PHOTOAUTHOR = "photo_author";
		public static final String COLUMN_PHOTOURL = "photo_url";
		public static final String COLUMN_PHOTOADRESSE = "photo_adresse";
		public static final String COLUMN_PHOTONBRPOINTS = "photo_nbrPoints";
		public static final String COLUMN_PHOTODERNIEREMODIF = "photo_derniereModif";
		//PHOTOGPSGEOM refers to GPSGEOM
		
		public static final String COLUMN_MATERIALID = "material_id";
		public static final String COLUMN_MATERIALNAME = "material_name";
		public static final String COLUMN_MATERIALCONDUCT = "material_conduct";
		public static final String COLUMN_MATERIALHEAT = "material_heat_capa";
		public static final String COLUMN_MATERIALMASS = "material_mass_density";
		
		public static final String COLUMN_ELEMENTTYPEID = "elementType_id";
		public static final String COLUMN_ELEMENTTYPENAME = "elementType_name";
		
		public static final String COLUMN_ELEMENTID = "element_id";
		public static final String COLUMN_ELEMENTCOLOR = "element_color";
		
		/**
		 * name of the local database
		 * upgrading the version force the database to be deleted and recreated
		 */
		public static final String DATABASE_NAME = "local3.db";
		public static final int DATABASE_VERSION = 3;
		
		
		/**
		 * query to create table GPSGEOM
		 */
		private static final String 
					DATABASE_CREATE = 		
						"create table "
						+ TABLE_GPSGEOM + " (" 
						+ COLUMN_GPSGEOMID   + " INTEGER PRIMARY KEY AUTOINCREMENT, "
						+ COLUMN_GPSGEOMCOORD + " text not null" 
						+"); "
		;
		
		/**
		 * query to create table PIXELGEOM
		 */
		private static final String 
				DATABASE_CREATE2 = 	
					"create table "
					+ TABLE_PIXELGEOM + " (" 
					+ COLUMN_PIXELGEOMID   + " INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ COLUMN_PIXELGEOMCOORD + " text not null" 
					+"); "
		;
		
		/**
		 * query to create table PROJECT
		 */
		private static final String 
				DATABASE_CREATE3 = 	
				    "create table "
					    + TABLE_PROJECT + " (" 
					    + COLUMN_PROJECTID   + " INTEGER PRIMARY KEY AUTOINCREMENT, "
					    + COLUMN_PROJECTNAME + " text not null, " 
					    + COLUMN_GPSGEOMID + " INTEGER, " 
					    + "FOREIGN KEY("+ COLUMN_GPSGEOMID +") REFERENCES "+TABLE_GPSGEOM+" ("+COLUMN_GPSGEOMID+")"
					    +");"
		;
		/**
		 * query to create table PHOTO
		 */
		private static final String 
				DATABASE_CREATE4 = 	
				    "create table "
					    + TABLE_PHOTO + " (" 
					    + COLUMN_PHOTOID   + " INTEGER PRIMARY KEY AUTOINCREMENT, "
					    + COLUMN_PHOTODESCRIPTION + " text not null, " 
					    + COLUMN_PHOTOAUTHOR + " text not null, " 
					    + COLUMN_PHOTOURL + " text not null, " 
					    + COLUMN_PHOTOADRESSE+ " text not null, " 
					    + COLUMN_PHOTONBRPOINTS + " INTEGER, " 
					    + COLUMN_PHOTODERNIEREMODIF + " DATETIME, "
					    + COLUMN_GPSGEOMID + " INTEGER, " 
					    + "FOREIGN KEY("+ COLUMN_GPSGEOMID +") REFERENCES "+TABLE_GPSGEOM+" ("+COLUMN_GPSGEOMID+")"
					    +");"
		;
		/**
		 * query to create table MATERIAL
		 */
		private static final String 
				DATABASE_CREATE5 = 	
				    "create table "
					    + TABLE_MATERIAL + " (" 
					    + COLUMN_MATERIALID   + " INTEGER PRIMARY KEY AUTOINCREMENT, "
					    + COLUMN_MATERIALNAME + " text not null "
					    +");"
	    ;
		/**
		 * query to create table ELEMENTTYPE
		 */
		private static final String 
				DATABASE_CREATE6 = 	
				    "create table "
					    + TABLE_ELEMENTTYPE + " (" 
					    + COLUMN_ELEMENTTYPEID   + " INTEGER PRIMARY KEY AUTOINCREMENT, "
					    + COLUMN_ELEMENTTYPENAME + " text not null " 
					    +");"
		;
		/**
		 * query to create table COMPOSED
		 */
		private static final String 
				DATABASE_CREATE7 = 	
				    "create table "
					    + TABLE_COMPOSED + " (" 
					    + COLUMN_PROJECTID   + " INTEGER, "
					    + COLUMN_PHOTOID + " INTEGER, " 
					    +" PRIMARY KEY ( "+COLUMN_PROJECTID+", "+COLUMN_PHOTOID+" ),"
					    + " FOREIGN KEY( "+ COLUMN_PROJECTID +" ) REFERENCES "+TABLE_PROJECT+" ( "+COLUMN_PROJECTID+" ),"
					    + " FOREIGN KEY( "+ COLUMN_PHOTOID +" ) REFERENCES "+TABLE_PHOTO+" ( "+COLUMN_PHOTOID+" )"
					    +");"
		;
		/**
		 * query to create table ELEMENT
		 */
		private static final String 
				DATABASE_CREATE8 = 	
				    "create table "
					    + TABLE_ELEMENT + " (" 
					    + COLUMN_ELEMENTID   + " INTEGER PRIMARY KEY AUTOINCREMENT, "
					    + COLUMN_PHOTOID + " INTEGER, " 
					    + COLUMN_MATERIALID + " INTEGER, " 
					    + COLUMN_GPSGEOMID + " INTEGER, " 
					    + COLUMN_PIXELGEOMID + " INTEGER, " 
					    + COLUMN_ELEMENTTYPEID + " INTEGER, "
					    + COLUMN_ELEMENTCOLOR + " text not null, "
					    + "FOREIGN KEY("+ COLUMN_PHOTOID +") REFERENCES "+TABLE_PHOTO+" ("+COLUMN_PHOTOID+"),"
					    + "FOREIGN KEY("+ COLUMN_MATERIALID +") REFERENCES "+TABLE_MATERIAL+" ("+COLUMN_MATERIALID+"),"
					    + "FOREIGN KEY("+ COLUMN_GPSGEOMID +") REFERENCES "+TABLE_GPSGEOM+" ("+COLUMN_GPSGEOMID+"),"
					    + "FOREIGN KEY("+ COLUMN_PIXELGEOMID +") REFERENCES "+TABLE_PIXELGEOM+" ("+COLUMN_PIXELGEOMID+"),"
					    + "FOREIGN KEY("+ COLUMN_ELEMENTTYPEID +") REFERENCES "+TABLE_ELEMENTTYPE+" ("+COLUMN_ELEMENTTYPEID+")"
					    +");"
		;
			     
		/**
		 * constructor
		 * @param context of our activity
		 */
		public MySQLiteHelper(Context context){
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}


		
		@Override
		public void onCreate(SQLiteDatabase database) {
			/**
			 * we call each methods to create every table
			 */
			database.execSQL(getDatabaseCreate());	
			database.execSQL(getDatabaseCreate2());
			database.execSQL(getDatabaseCreate3());
			database.execSQL(getDatabaseCreate4());
			database.execSQL(getDatabaseCreate5());	
			database.execSQL(getDatabaseCreate6());
			database.execSQL(getDatabaseCreate7());
			database.execSQL(getDatabaseCreate8());
		}


		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(MySQLiteHelper.class.getName(), 
			"Upgrading database from version"+oldVersion+" to "+newVersion+", which will destroy all your data");
			db.execSQL(
					"DROP TABLE IF EXISTS" 
							+ TABLE_PROJECT +", "
							+ TABLE_GPSGEOM +", "
							+ TABLE_PIXELGEOM +", "
							+ TABLE_PHOTO 
							+"; "
					);
			onCreate(db);		
		}

		/**
		 * getter for createquery of related number
		 * @return the query as String
		 */
		public static String getDatabaseCreate() {
			return DATABASE_CREATE;
		}
		/**
		 * getter for createquery of related number
		 * @return the query as String
		 */
		public static String getDatabaseCreate2() {
			return DATABASE_CREATE2;
		}
		/**
		 * getter for createquery of related number
		 * @return the query as String
		 */
		public static String getDatabaseCreate3() {
			return DATABASE_CREATE3;
		}
		/**
		 * getter for createquery of related number
		 * @return the query as String
		 */
		public static String getDatabaseCreate4() {
			return DATABASE_CREATE4;
		}
		/**
		 * getter for createquery of related number
		 * @return the query as String
		 */
		public static String getDatabaseCreate5() {
			return DATABASE_CREATE5;
		}
		/**
		 * getter for createquery of related number
		 * @return the query as String
		 */
		public static String getDatabaseCreate6() {
			return DATABASE_CREATE6;
		}
		/**
		 * getter for createquery of related number
		 * @return the query as String
		 */
		public static String getDatabaseCreate7() {
			return DATABASE_CREATE7;
		}
		/**
		 * getter for createquery of related number
		 * @return the query as String
		 */
		public static String getDatabaseCreate8() {
			return DATABASE_CREATE8;
		}
}