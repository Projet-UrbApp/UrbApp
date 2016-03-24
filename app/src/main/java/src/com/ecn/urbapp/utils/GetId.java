package src.com.ecn.urbapp.utils;

import src.com.ecn.urbapp.activities.MainActivity;
import src.com.ecn.urbapp.db.Element;
import src.com.ecn.urbapp.db.GpsGeom;
import src.com.ecn.urbapp.db.PixelGeom;
import src.com.ecn.urbapp.db.Project;

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