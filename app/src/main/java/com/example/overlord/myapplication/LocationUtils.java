package com.example.overlord.myapplication;

import android.*;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseError;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by overlord on 25/1/17.
 */

class LocationUtils {

    private static final int REQUEST_LOCATION_CODE = 0;

    private static final String[] mRequiredPermissions = new String[]{
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_NETWORK_STATE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.INTERNET
    };


    public static LatLng getLatLng(GeoLocation location){
        return new LatLng(location.latitude, location.longitude);
    }

    public static LatLng getLatLng(Location location){
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    public static GeoLocation getGeoLocation(LatLng location){
        return new GeoLocation(location.latitude, location.longitude);
    }


    public static boolean checkPermissions(Activity activity){
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

    public static void requestPermissions(Activity activity){
        ActivityCompat.requestPermissions(
                activity,
                mRequiredPermissions,
                REQUEST_LOCATION_CODE
        );
    }

    /*
                RUN ONCE
     */
    public static void addStaticGeofireLocations(GeoFire geofire,
                                                    Map<String, GeoLocation> staticGeolocations){
        for(Map.Entry<String, GeoLocation> locationEntry : staticGeolocations.entrySet())
            geofire.setLocation(
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

    public static Map<String, GeoLocation> getInputGeoFireLocations(){
        Map<String, GeoLocation> locationMap = new ConcurrentHashMap<>();

        GeoLocation geoLocations[] = new GeoLocation[]{
                new GeoLocation(22.253496, 84.902362),
                new GeoLocation(22.253449, 84.902495),
                new GeoLocation(22.253373, 84.902730),
                new GeoLocation(22.252920, 84.902039),
                new GeoLocation(22.253169, 84.902115),
                new GeoLocation(22.253423, 84.902204),
                new GeoLocation(22.253080, 84.902278),
                new GeoLocation(22.253148, 84.902558)
        };
        int i = 0;
        for(GeoLocation geoLocation : geoLocations) {
            i += 1;
            locationMap.put("LOCATION>" + Integer.toString(i), geoLocation);
        }
        return locationMap;
    }

}
