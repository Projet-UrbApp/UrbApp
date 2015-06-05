package com.example.pillet.urbapp2.utils;

import android.util.Log;

import java.util.ArrayList;

import org.osmdroid.util.GeoPoint;

/**
 * Many static methods useful 
 * @author Sebastien
 *
 */
public class MathOperation {
	private static final String TAG = "MathOperation";
	public static GeoPoint barycenter(ArrayList<GeoPoint> GPSPoints) {
		int numberPoint = GPSPoints.size();
		Double x = (double) 0;
		Double y = (double) 0;
		for (GeoPoint GPSinCase:GPSPoints){
			x += GPSinCase.getLatitude();
			y += GPSinCase.getLongitude();
		}
		return new GeoPoint(x/numberPoint,y/numberPoint);
	}
}