package com.example.pillet.urbapp2.utils;

import java.util.ArrayList;

import com.google.android.gms.maps.model.LatLng;

/**
 * Many static methods useful 
 * @author Sebastien
 *
 */
public class MathOperation {

	public static LatLng barycenter(ArrayList<LatLng> GPSPoints) {
		int numberPoint = GPSPoints.size();
		Double x = Double.valueOf(0);
		Double y = Double.valueOf(0);
		for (LatLng GPSinCase:GPSPoints){
			x += GPSinCase.latitude;
			y += GPSinCase.longitude;
		}
		
		LatLng GPSCentered = new LatLng(x/numberPoint,y/numberPoint);
		
		return GPSCentered;
	}
	
}