package com.example.mapdemo;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Toast;

import com.example.mapdemo.util.MapUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


/**
 * Uses a map view to show the user's location.
 * A button ("Get new points") generates three random points within the map's current view. 
 * Those three points are represented by pins on the map. Each time the button is pressed, the
 * previous points are removed and three new ones are added.
 * <p>
 * Pressing one of the points shows a simple overlay with the longitude and latitude of the selected point.
 * Uses the default pins and overlay provided by the map framework.
 * </p>
 */
public class MainActivity extends FragmentActivity implements LocationListener {

	/**
	 * Zoom level for the map
	 */
	private static final int ZOOM_LEVEL = 15;

	/**
	 * The number of random points to generate
	 */
	private static final int NUM_OF_RAND_PTS = 3;

	/**
	 * Thickness of line for location accuracy radius circle
	 */
	private static final int CIRCLE_LINE_WEIGHT = 3;

	/**
	 * Frequency of location updates in milliseconds.
	 */
	private static final long MIN_UPDATE_LOC_TIME_MS = 1000;

	/**
	 * The minimum distance in meters between location updates.
	 */
	private static final float MIN_UPDATE_LOC_DIST_METERS = 1;
	
	/**
	 * The instance of the map displayed by the map fragment.
	 */
	private GoogleMap mMap;

	/**
	 * Location manager to get maps API data
	 */
	private LocationManager mLocationManager;

	/**
	 * Provider for map geolocation data
	 */
	private String mProvider;

	/**
	 * Device/user's current location
	 */
	private LatLng mCurrentLocation;

	/**
	 * Boundaries for current marker
	 */
	private LatLngBounds mBounds;

	/**
	 * Current random points being displayed
	 */
	private List<LatLng> mRandomPoints = null;

	/**
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		// Hide the action bar
		getActionBar().hide();

		// Init random points
		mRandomPoints = new ArrayList<LatLng>();

		mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
				.getMap();
		mMap.setMyLocationEnabled(true);
		mMap.getUiSettings().setZoomControlsEnabled(false);
		mMap.getUiSettings().setZoomGesturesEnabled(false);

		mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

			@Override
			public boolean onMarkerClick(Marker marker) {
				// Show the lat/long info for the point
				marker.showInfoWindow();
				return false;
			}
		});

		mMap.setOnCameraChangeListener(new OnCameraChangeListener() {
			
			@Override
			public void onCameraChange(CameraPosition position) {
				mBounds = mMap.getProjection().getVisibleRegion().latLngBounds;

				if(mRandomPoints.isEmpty())
				{
					addRandomPointsToMap();
				}
			}
		});

		// Get the location manager
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		// Define the criteria how to select the location provider -> use default 
		Criteria criteria = new Criteria();
		mProvider = mLocationManager.getBestProvider(criteria, false);
		mLocationManager.requestLocationUpdates(mProvider, MIN_UPDATE_LOC_TIME_MS, MIN_UPDATE_LOC_DIST_METERS, this);


		findViewById(R.id.gen_new_pts_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mMap.clear();

				addAccuracyCircleToMap(mLocationManager.getLastKnownLocation(mProvider));

				mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mCurrentLocation, ZOOM_LEVEL));

				addRandomPointsToMap();
			}
		});
	}

	/** 
	 * Request updates at startup
	 * @see android.support.v4.app.FragmentActivity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		mLocationManager.requestLocationUpdates(mProvider, MIN_UPDATE_LOC_TIME_MS, MIN_UPDATE_LOC_DIST_METERS, this);
	}

	/** 
	 * Remove the LocationManager updates when Activity is paused 
	 * @see android.support.v4.app.FragmentActivity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		mLocationManager.removeUpdates(this);
	}

	/**
	 * @see android.location.LocationListener#onLocationChanged(android.location.Location)
	 */
	@Override
	public void onLocationChanged(Location location) {
		if(location != null)
		{
			mMap.clear();
			mCurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());
			addAccuracyCircleToMap(location);

			// Move the camera instantly to user's loc with a zoom of 15.
			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mCurrentLocation, ZOOM_LEVEL));
			
			if(mBounds != null && mRandomPoints.isEmpty())
			{
				addRandomPointsToMap();
			}
			else if(!mRandomPoints.isEmpty())
			{
				for(LatLng point: mRandomPoints)
				{
					mMap.addMarker(new MarkerOptions().title("Marker").snippet("lat: " + point.latitude + " long: " + point.longitude).position(new LatLng(point.latitude, point.longitude)));
				}
			}
		}
		
	}

	/**
	 * @see android.location.LocationListener#onStatusChanged(java.lang.String, int, android.os.Bundle)
	 */
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// Unused 
	}

	/**
	 * @see android.location.LocationListener#onProviderEnabled(java.lang.String)
	 */
	@Override
	public void onProviderEnabled(String provider) {
		Toast.makeText(this, "Enabled new provider " + provider,
				Toast.LENGTH_SHORT).show();
	}

	/**
	 * @see android.location.LocationListener#onProviderDisabled(java.lang.String)
	 */
	@Override
	public void onProviderDisabled(String provider) {
		Toast.makeText(this, "Disabled provider " + provider,
				Toast.LENGTH_SHORT).show();
	}

	/**
	 * Adds the randomly generated points to the map.
	 */
	private void addRandomPointsToMap()
	{
		// Check that bounds have been set to a valid value.
		if(MapUtils.isBoundsValid(mBounds))
		{
			mRandomPoints = MapUtils.getRandPts(mCurrentLocation, mBounds, NUM_OF_RAND_PTS);

			for(LatLng point: mRandomPoints)
			{
				mMap.addMarker(new MarkerOptions().title("Marker").snippet("lat: " + point.latitude + " long: " + point.longitude).position(new LatLng(point.latitude, point.longitude)));
			}
		}
	}
	
	/**
	 * Draw the "accuracy radius" circle on the map around the device location point.
	 * @param location the current location
	 */
	private void addAccuracyCircleToMap(Location location)
	{
		CircleOptions circleOptions = new CircleOptions()
		.center(mCurrentLocation)
		.radius(location.getAccuracy() *ZOOM_LEVEL * ZOOM_LEVEL)
		.fillColor(getResources().getColor(R.color.transparent_blue))
		.strokeColor(getResources().getColor(R.color.blue))
		.strokeWidth(CIRCLE_LINE_WEIGHT);

		mMap.addCircle(circleOptions);
	}
}
