package com.example.overlord.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.facebook.login.LoginResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

/**
 * Handles Boiler Plate. Meant to be inherited by only MapsActivity.
 *
 * Creates the GoogleApiClient, Issues location request centered around player's current location.
 *
 */

abstract class BoilerplateMapsActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS    = 0;
    private final static int REQUEST_CHECK_SETTINGS = 1;

    private static final String TAG = "BoilerplateMapsActivity";

    private GoogleApiClient mGoogleApiClient;

    private static DataStash dataStash = DataStash.getDataStash();

    protected abstract Activity getPresentActivity();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);


        mGoogleApiClient = createGoogleApiClient();
        createView();
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
                Log.d("BPMAonCreate","getMapAsync");

                dataStash.googleMap = googleMap;

                LocationUtils.setGoogleMapStyle(dataStash.googleMap, R.raw.defender_style);
                LocationUtils.addStaticGeoFireLocations(dataStash.geoFire,
                        LocationUtils.getInputGeoFireLocations());

                UrbanListeners.setupListeners(getPresentActivity());
            }
        });
    }

     protected void createView(){
        CoordinatorLayout coordinatorLayout = (CoordinatorLayout)findViewById(R.id.mainContent);
        View bottomSheet = coordinatorLayout.findViewById(R.id.bottomSheet);
        dataStash.bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        dataStash.bottomSheetBehavior
                .setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            boolean first = true;
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                Log.d(TAG, "LocationChanged" + newState);
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                Log.d(TAG, "Slide");
                if(first){
                    first = false;
                    bottomSheet.setTranslationY(0);
                }
            }
        });
    }

    protected LocationRequest createLocationRequest(){
        return LocationRequest
                .create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(2000);//ms

    }
    protected void issueLocationRequest(LocationRequest locationRequest){
        try {
            LocationServices
                    .FusedLocationApi
                    .requestLocationUpdates(
                            mGoogleApiClient,
                            locationRequest,
                            new LocationListener() {
                                @Override
                                public void onLocationChanged(Location location) {
                                    WarMap.updateCentralLocation(
                                            getPresentActivity(),
                                            location
                                    );
                                }
                            });
        } catch (SecurityException se) {
            Log.d("SECURITY_API_CLIENT", se.toString());
        }
    }


    protected synchronized GoogleApiClient createGoogleApiClient() {
        return new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        if(LocationUtils.checkPermissions(getPresentActivity())) {

                            final LocationRequest locationRequest = createLocationRequest();

                            LocationUtils.requestSettings(locationRequest, mGoogleApiClient)
                                    .setResultCallback(
                                    new ResultCallback<LocationSettingsResult>() {
                                        @Override
                                        public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                                            final Status status = locationSettingsResult.getStatus();

                                            switch (status.getStatusCode()){
                                                case LocationSettingsStatusCodes.SUCCESS:
                                                    //Actual Location Request Call
                                                    issueLocationRequest(locationRequest);
                                                    break;

                                                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                                    try{
                                                        status.startResolutionForResult(
                                                                getPresentActivity(),
                                                                REQUEST_CHECK_SETTINGS
                                                        );
                                                    } catch (IntentSender.SendIntentException e) {}
                                                    break;
                                                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                                    Toast.makeText(getPresentActivity(),
                                                            "IRREVOCABLY FUCKED, RESTART", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                            );
                        }
                        else{
                            LocationUtils.requestPermissions(getPresentActivity(),
                                    REQUEST_PERMISSIONS);
                        }
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        mGoogleApiClient.connect();
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
    public void onBackPressed() {
        if(dataStash.bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED){
            dataStash.bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        Log.e("LogicalError", "onStopCalled");
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if(dataStash.geoQuery != null)
            dataStash.geoQuery.removeAllListeners();
        super.onDestroy();
    }

}
