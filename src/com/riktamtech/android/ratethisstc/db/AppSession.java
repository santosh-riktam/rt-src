package com.riktamtech.android.ratethisstc.db;

import java.util.ArrayList;
import java.util.Arrays;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.riktamtech.android.ratethisstc.R;
import com.riktamtech.android.ratethisstc.dao.AchievementDAO;
import com.riktamtech.android.ratethisstc.dao.AppUser;
import com.riktamtech.android.ratethisstc.dao.MyRatesDAO;
import com.riktamtech.android.ratethisstc.dao.NewRateDAO;

public class AppSession {
	public static String appUrlString = "android.resource://com.riktamtech.android.ratethisstc/";

	public static final int NEW = 0, UPLOAD_IN_PROGRESS = 3;

	public static NewRateDAO newRateDAO;
	public static int newRatePosted = NEW;
	public static NewRateDAO cachedRateDAO; // for myrates to display
											// immediately after uploading

	public static boolean isMyRates;// for passing frm rates to details screen.
									// better way to preserve value since
									// activity id being recreated after
									// returning frm gallery

	public static int DEVICE_SCREEN_WIDTH, DEVICE_SCREEN_HEIGHT, DEVICE_DENSITY;

	public static String EXTERNAL_CACHE_DIR_PATH;

	public static String DEVICE_TOKEN;

	public static AppUser signedInUser;

	public static AchievementDAO signnedInUserAchievements;

	public static ArrayList<MyRatesDAO> myRatesArrayList, ratedArrayList;
	// public static boolean hasMoreMyRates=true,hasMoreRatedRates=true;

	public static Location currentUserLocation;

	public static ArrayList<String> ageGroupArrayList, votingDurationArrayList, primaryTagsArrayList;

	//for new rate and other screens
	public static String newRateTempImage1Name = "NewRateImage1.jpg", newRateTempImage2Name = "NewRateImage2.jpg";

	static LocationManager locationManager;
	static LocationListener locationListener;
	static int minDistance = 0; //meters
	static int timeMillis = 0; //milliseconds

	public static void listenToLocationChanges(Context ctx) {

		log("listen location called");
		if (locationManager == null) {
			locationManager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
			currentUserLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (currentUserLocation == null) {
				currentUserLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			}
		}

		if (locationListener == null) {
			locationListener = new LocationListener() {
				public void onLocationChanged(Location location) {

					currentUserLocation = location;
					log("now current location is " + location);
				}

				public void onStatusChanged(String provider, int status, Bundle extras) {
				}

				public void onProviderEnabled(String provider) {
				}

				public void onProviderDisabled(String provider) {
				}
			};
		}
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, timeMillis, minDistance, locationListener);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, timeMillis, minDistance, locationListener);

	}

	public static void stopListeningToLocationUpdates() {

		locationManager.removeUpdates(locationListener);
		log("remove update called");
	}

	public static void initArrays(Context ctx) {
		ageGroupArrayList = new ArrayList<String>(Arrays.asList(ctx.getResources().getStringArray(R.array.NewRateFormVotersAgeSp)));
		votingDurationArrayList = new ArrayList<String>(Arrays.asList(ctx.getResources().getStringArray(R.array.NewRateFormVotingTime)));
		primaryTagsArrayList = new ArrayList<String>(Arrays.asList(ctx.getResources().getStringArray(R.array.NewRateFormPrimaryTagSp)));
	}

	public void clear() {
		myRatesArrayList = null;
		ratedArrayList = null;
		signedInUser = null;
		// TODO clear all variables
	}

	private static void log(String s) {
		Log.d("AppSession", s);
	}
}
