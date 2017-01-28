package com.example.overlord.myapplication;

import android.app.Activity;
import android.support.design.widget.BottomSheetBehavior;
import android.util.Log;
import android.widget.Toast;

import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.database.DatabaseError;

/**
 * Created by overlord on 26/1/17.
 */

class UrbanListeners {

    private static DataStash dataStash = DataStash.getDataStash();

    static void setupListeners(Activity activity){
        setFireBaseListeners(activity);
        setGeoQueryListeners(activity);
        setGoogleMapListeners(activity);
    }

    private static void setFireBaseListeners(Activity activity){}

    private static void setGeoQueryListeners(Activity activity){
        if(dataStash.geoQuery != null)
            dataStash.geoQuery
                    .addGeoQueryEventListener(new GeoQueryEventListener() {
                @Override
                public void onKeyEntered(String key, GeoLocation location) {
                    Log.i("onKeyEntered", key + ">" + location.toString());
                    if(!key.equals(CONSTANTS.PLAYER_LOCATION_TAG))
                        dataStash.enemyPlayerLocations.put(key, location);
                }

                @Override
                public void onKeyExited(String key) {
                    Log.i("onKeyExited", key);
                    dataStash.enemyPlayerLocations.remove(key);
                }

                @Override
                public void onKeyMoved(String key, GeoLocation location) {
                    Log.i("Moving", key);
                }

                @Override
                public void onGeoQueryReady() {
                    Log.i("GeoQuery", "READY");
                }

                @Override
                public void onGeoQueryError(DatabaseError error) {
                    if(error != null)
                        Log.e("GeoQueryError", error.toString());
                }
            });
    }


    private static void setGoogleMapListeners(final Activity activity){
        dataStash.googleMap
                .setOnMarkerClickListener(
                new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        if(dataStash.bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED)
                            dataStash.googleMap.setPadding(0,0,0,dataStash.bottomSheet.getHeight());
                            dataStash.bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        return false;
                    }
                }
        );

        dataStash.googleMap
                .setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        if(dataStash.bottomSheetBehavior.getState() ==
                                BottomSheetBehavior.STATE_EXPANDED){
                            dataStash.googleMap.setPadding(0,0,0,0);
                            dataStash.bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        }
                    }
                });

    }
}
