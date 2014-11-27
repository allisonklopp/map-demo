/**
 * 
 */
package com.example.mapdemo.util;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

/**
 * Utility class containing functions for mapping points.
 * @author aklopp
 */
public class MapUtils {
	
	/**
	 * Function to generate the specified number of random points within the screen bounds indicated.
	 * @param point the center point
	 * @param bounds the screen bounds
	 * @param numberOfPoints the number of points to generate
	 * @return list of generated points
	 */
	public static List<LatLng> getRandPts(LatLng point, LatLngBounds bounds, int numberOfPoints)
	{
		int plusMinusSign1 = 0;
		int plusMinusSign2 = 0;
		List<LatLng> generatedPoints = new ArrayList<LatLng>();
		
		while(generatedPoints.size() < numberOfPoints)
		{
			// Randomly determine positive/negative for each
			if(Math.random() > 0.5)
				plusMinusSign1 = 1;
			else
				plusMinusSign1 = -1;
			if(Math.random() > 0.5)
				plusMinusSign2 = 1;
			else
				plusMinusSign2 = -1;
				
			// Add or subtract a random number * corresponding screen dimension / 2
			double randLat = point.latitude + plusMinusSign1 * (Math.random())*getWidthOfBounds(bounds)*0.5;
			double randLong = point.longitude + plusMinusSign2 * ( Math.random())*getHeightOfBounds(bounds)*0.5;
			LatLng temp = new LatLng(randLat, randLong);
			
			// Check the ultra-rare outlier cases where the generated point matches an already added point or the center point
			if(!generatedPoints.contains(temp) && !(point.longitude == temp.longitude && point.latitude == temp.latitude))
			{
				generatedPoints.add(temp);
			}
		}
			
		return generatedPoints;
	}
	
	/**
	 * Check if lat-long bounds for screen are accurate.
	 * @param bounds
	 * @return true if bounds are valid
	 */
	public static boolean isBoundsValid(LatLngBounds bounds)
	{
		return (Math.abs(bounds.northeast.latitude) - Math.abs(bounds.southwest.latitude)) !=0 && (Math.abs(bounds.northeast.longitude) - Math.abs(bounds.southwest.longitude))!=0;
	}
	
	/**
	 * Get the distance across current map view in degrees latitude.
	 * @param bounds
	 * @return width in degrees of latitude
	 */
	public static double getWidthOfBounds(LatLngBounds bounds)
	{
		return bounds.northeast.latitude - bounds.southwest.latitude;
	}
	
	/**
	 * Get the distance across current map in degrees longitude.
	 * @param bounds
	 * @return height in degrees of longitude
	 */
	public static double getHeightOfBounds(LatLngBounds bounds)
	{
		return bounds.northeast.longitude - bounds.southwest.longitude;
	}
}
