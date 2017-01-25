package com.example.overlord.myapplication;

import android.*;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseError;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by overlord on 25/1/17.
 */

class LocationUtils {
    private static DataStash dataStash = DataStash.getDataStash();

    private static final int REQUEST_LOCATION_CODE = 0;

    private static final String[] mRequiredPermissions = new String[]{
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_NETWORK_STATE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.INTERNET
    };


    static LatLng getLatLng(GeoLocation location){
        return new LatLng(location.latitude, location.longitude);
    }

    static LatLng getLatLng(Location location){
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    static GeoLocation getGeoLocation(LatLng location){
        return new GeoLocation(location.latitude, location.longitude);
    }

    static GeoLocation getGeoLocation(Location location){
        return new GeoLocation(location.getLatitude(), location.getLongitude());
    }


    static boolean checkPermissions(Activity activity){
        boolean permissionsGranted = true;

        for(String permission : mRequiredPermissions)
            permissionsGranted = permissionsGranted &&
                    ContextCompat
                            .checkSelfPermission(
                                    activity,
                                    permission)
                            !=
                            PackageManager.PERMISSION_DENIED;

        return permissionsGranted;
    }

    static void requestPermissions(Activity activity){
        ActivityCompat.requestPermissions(
                activity,
                mRequiredPermissions,
                REQUEST_LOCATION_CODE
        );
    }

    /*
                RUN ONCE
     */
    static void addStaticGeoFireLocations(Map<String, GeoLocation> staticGeoLocations){
        for(Map.Entry<String, GeoLocation> locationEntry : staticGeoLocations.entrySet())
        dataStash.geoFire
                .setLocation(
                    locationEntry.getKey(),
                    locationEntry.getValue(),
                    //Success
                    new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            if (error != null)
                                Log.e("ADDING STATIC GEOFIRE",error.toString());
                        }});
    }

    static Map<String, GeoLocation> getInputGeoFireLocations(){
        Map<String, GeoLocation> locationMap = new ConcurrentHashMap<>();

        GeoLocation geoLocations[] = dataStash.getGeoLocations();

        int i = 0;
        for(GeoLocation geoLocation : geoLocations) {
            i += 1;
            locationMap.put("LOCATION>" + Integer.toString(i), geoLocation);
        }
        return locationMap;
    }


    /*
                            Google Map Styling
     */


    static void setGoogleMapStyle(GoogleMap googleMap, int resourceID){
        try{
            googleMap.getUiSettings().setMapToolbarEnabled(false);

            googleMap.setMaxZoomPreference(googleMap.getMaxZoomLevel());
            googleMap.setBuildingsEnabled(true);


//            if(!googleMap.setMapStyle(
//                    MapStyleOptions.loadRawResourceStyle(
//                            mActivity,
//                            resourceID
//                    )
//            ))
//                Log.e("STYLE", "FAILED");
        } catch (Resources.NotFoundException e){

            Log.e("STYLE", "NOT PRESENT");
        }
    }
}
