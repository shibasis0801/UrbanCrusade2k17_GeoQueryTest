package com.example.overlord.myapplication;

import android.app.Activity;
import android.location.Location;
import android.util.Log;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseError;
import java.util.Map;

/**
 * Real time Google Map Updates when WAR starts.
 */

class WarMap {
    private static DataStash dataStash = DataStash.getDataStash();

    static void updateCentralLocation(Activity activity, Location location){
        dataStash.playerLocation = location;

        if(dataStash.geoQuery == null)
            dataStash.geoQuery = dataStash.geoFire
                    .queryAtLocation(
                            LocationUtils
                                    .getGeoLocation(location),
                            dataStash.querySize
                    );
        else
            dataStash.geoQuery.setCenter(LocationUtils.getGeoLocation(location));

        dataStash.geoFire
                .setLocation(
                CONSTANTS.PLAYER_LOCATION_TAG,
                LocationUtils.getGeoLocation(location),
                new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        if(error != null)
                            Log.e("updateCentralLocation", error.toString());
                    }
                }
        );

        updateMapUI(activity ,location);
    }

    private static void updateMapUI(final Activity activity , final Location location){
        activity.runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        if(LocationUtils.checkPermissions(activity)){

                            dataStash.googleMap.clear();//markers

                            MarkerOptions options = new MarkerOptions()
                                    .rotation(location.getBearing())
                                    .flat(true);

                            Log.d("enemySize", "" + dataStash.enemyPlayerLocations.size());
                            //Enemies
                            for(Map.Entry<String, GeoLocation> enemy : dataStash.enemyPlayerLocations.entrySet())
                                dataStash.googleMap
                                        .addMarker(
                                        options.position(LocationUtils.getLatLng(enemy.getValue()))
                                                .title(enemy.getKey())
                                                .icon(BitmapDescriptorFactory
                                                        .defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                                );

                            //Your Location
                            dataStash.googleMap
                                    .addMarker(
                                    options.position(LocationUtils.getLatLng(location))
                                            .title(CONSTANTS.PLAYER_LOCATION_TAG)
                                            .icon(BitmapDescriptorFactory
                                                    .defaultMarker(BitmapDescriptorFactory.HUE_RED))
                            );

                            CameraPosition cameraPosition = CameraPosition
                                    .builder(dataStash.googleMap.getCameraPosition())
                                    .target(LocationUtils.getLatLng(location))
                                    .zoom(19)
                                    .tilt(25)
                                    .bearing(location.getBearing())
                                    .build();

                            dataStash.googleMap
                                    .animateCamera(CameraUpdateFactory
                                    .newCameraPosition(cameraPosition));
                        }
                    }
                }
        );
    }
}
