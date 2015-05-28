package com.example.pillet.urbapp2.utils;

import com.example.pillet.urbapp2.activities.MainActivity;
import com.example.pillet.urbapp2.db.Element;
import com.example.pillet.urbapp2.db.GpsGeom;
import com.example.pillet.urbapp2.db.PixelGeom;
import com.example.pillet.urbapp2.db.Project;
/**
 * 
 * We suppose that the elementplace in the list is equal to their ID
 *
 */
public class GetId{
	
	public static long Element(){
		long ret = 1;
		for(Element el : MainActivity.element){
			if(el.getElement_id()==ret){
				ret++;
			}
		}
		return ret;
	}
	
	public static long GpsGeom(){
		long ret = 1;
		for(GpsGeom gg : MainActivity.gpsGeom){
			if(gg.getGpsGeomsId()==ret){
				ret++;
			}
		}
		return ret;
	}
	
	public static long PixelGeom(){
		long ret = 1;
		for(PixelGeom pg : MainActivity.pixelGeom){
			if(pg.getPixelGeomId()==ret){
				ret++;
			}
		}
		return ret;
	}
	
	public static long Project(){
		long ret = 1;
		for(Project p : MainActivity.project){
			if(p.getProjectId()==ret){
				ret++;
			}
		}
		return ret;
	}
}