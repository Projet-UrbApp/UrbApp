package src.com.ecn.urbapp.utils;

import java.util.ArrayList;
import java.util.Arrays;

import android.graphics.Point;

import src.com.ecn.urbapp.db.GpsGeom;
import src.com.ecn.urbapp.db.PixelGeom;
import src.com.ecn.urbapp.zones.UtilCharacteristicsZone;
import src.com.ecn.urbapp.zones.Zone;
import org.osmdroid.util.GeoPoint;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class ConvertGeom{

	private static WKTReader wktr = new WKTReader();
	private static final String TAG = "ConvertGeom";

	//TODO test every function
	public static Zone pixelGeomToZone(PixelGeom the_geom){
			Zone temp = new Zone();
			Coordinate[] coords;
			try {
				Geometry geom = wktr.read(the_geom.getPixelGeom_the_geom());
				for (PixelGeom pg : UtilCharacteristicsZone.getPixelGeomsFromGeom(geom, false)) {
					Polygon poly = (Polygon) wktr.read(pg.getPixelGeom_the_geom());
					coords = poly.getCoordinates();
					for (Coordinate coord : coords) {
						temp.addPoint(new Point((int) coord.x, (int) coord.y));
					}
				}
				temp.actualizePolygon();
			} catch (ParseException e) {
				e.printStackTrace();
			}
			return temp;
	}
	
	public static String ZoneToPixelGeom(Zone zone){
		zone.actualizePolygon();
		return zone.getPolygon().toText();
	}

	/**
	 * build geopoint array from gpsgeom object
	 * @param the_geom
	 * @return
	 */
	public static ArrayList<GeoPoint> gpsGeomToGeoPoint(GpsGeom the_geom){
		ArrayList<GeoPoint> list = new ArrayList<GeoPoint>();

		String s = the_geom.getGpsGeomCoord().replace("srid=4326;LINESTRING(", "");

		s = s.replace(")", "");
		ArrayList<String> tab = new ArrayList<String>(Arrays.asList(s.split(",")));
		for(String str : tab){
			// int is needed for GeoPoint, double is for 1/1E6 degree format
			list.add(new GeoPoint(Double.parseDouble(str.split(" ")[1]), Double.parseDouble(str.split(" ")[0])));
		}
		return list;
	}

	/**
	 * build a gpsgeom from geopoint array
	 * @param list
	 * @return
	 */
	public static String GeoPointToGpsGeom(ArrayList<GeoPoint> list){
		String ret="LINESTRING(";
		String s="";
		for(GeoPoint ll : list){
			s+=ll.getLongitude()+" "+ll.getLatitude();
			if(list.get(list.size()-1)!=ll){
				s+=",";
			}
		}
		ret+=s;
		ret+=")";
		return ret;
	}
}