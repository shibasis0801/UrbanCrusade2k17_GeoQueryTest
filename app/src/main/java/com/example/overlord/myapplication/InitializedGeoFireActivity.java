package com.example.overlord.myapplication;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

/**
 * Created by overlord on 26/1/17.
 */

abstract class InitializedGeoFireActivity extends AppCompatActivity {

    private static DataStash dataStash = DataStash.getDataStash();
    private LocationRequest mLocationRequest;


    protected abstract Activity getPresentActivity();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        createGoogleApiClient();
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            /**
             * Manipulates the map once available.
             * This callback is triggered when the map is ready to be used.
             * This is where we can add markers or lines, add listeners or move the camera.
             * If Google Play services is not installed on the device, the user will be prompted to install
             * it inside the SupportMapFragment. This method will only be triggered once the user has
             * installed Google Play services and returned to the app.
             */

            @Override
            public void onMapReady(GoogleMap googleMap) {
                dataStash.googleMap = googleMap;
                LocationUtils.setGoogleMapStyle(dataStash.googleMap, R.raw.defender_style);
                LocationUtils.addStaticGeoFireLocations(LocationUtils.getInputGeoFireLocations());
            }
        });
    }

    protected GoogleApiClient createGoogleApiClient() {
        return new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        mLocationRequest = LocationRequest
                                .create()
                                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                                .setInterval(1000);

                        if(LocationUtils.checkPermissions(getPresentActivity()))
                            try {
                                LocationServices
                                        .FusedLocationApi
                                        .requestLocationUpdates(
                                                dataStash.googleApiClient,
                                                mLocationRequest,
                                                new LocationListener() {
                                                    @Override
                                                    public void onLocationChanged(Location location) {
                                                        WarMap.updateCentralLocation(
                                                                getPresentActivity(),
                                                                location
                                                        );
                                                    }
                                                });
                            }
                            catch (SecurityException se) {
                                Log.d("Security exception", se.toString());
                            }
                        else{
                            LocationUtils.requestPermissions(getPresentActivity());
                        }
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        dataStash.googleApiClient.connect();
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                })
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(dataStash.googleApiClient != null)
            dataStash.googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        if(dataStash.googleApiClient != null)
            dataStash.googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if(dataStash.geoQuery != null)
            dataStash.geoQuery.removeAllListeners();
        super.onDestroy();
    }
}
