package com.example.pillet.urbapp2.utils;

import java.util.ArrayList;

import org.osmdroid.util.GeoPoint;

/**
 * Many static methods useful 
 * @author Sebastien
 *
 */
public class MathOperation {

	public static GeoPoint barycenter(ArrayList<GeoPoint> GPSPoints) {
		int numberPoint = GPSPoints.size();
		Double x = Double.valueOf(0);
		Double y = Double.valueOf(0);
		for (GeoPoint GPSinCase:GPSPoints){
			x += GPSinCase.getLatitude();
			y += GPSinCase.getLongitude();
		}
		
		GeoPoint GPSCentered = new GeoPoint(x/numberPoint,y/numberPoint);
		
		return GPSCentered;
	}
	
}