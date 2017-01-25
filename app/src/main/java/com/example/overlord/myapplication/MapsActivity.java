package com.example.overlord.myapplication;

import android.app.Activity;
import android.content.res.Resources;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.*;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
    Map Styles used

    Defender Style
    MidnightCommander > https://twitter.com/adamkrogh

    Attacker Style

 */
/*
 *  Function Hierarchy
 *
 *  1   onCreate, creator
 *  2   Location update
 *  3   Listener
 *  4   Styling
 */
public class MapsActivity extends AbstractedGeofireActivity {

    private static final String MY_LOCATION_TAG ="MY_LOCATION";
    private static final GeoLocation GEO_LOCATION_NESCAFE = new GeoLocation(22.2533788, 84.9025932);

    private Activity mActivity = MapsActivity.this;
    private Location mMyLocation;
    private GoogleMap mGoogleMap;
    private Map<String, GeoLocation> mTeamLocations = new ConcurrentHashMap<>();

    private LocationRequest mLocationRequest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*
            onStop, onStart, initializeMembers are inherited.
            initializeMembers calls all 4 create methods
         */

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        //Creates all required member variables
        initializeMembers();

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            /**
             * Manipulates the map once available.
             * This callback is triggered when the map is ready to be used.
             * This is where we can add markers or lines, add listeners or move the camera. In this case,
             * we just add a marker near Sydney, Australia.
             * If Google Play services is not installed on the device, the user will be prompted to install
             * it inside the SupportMapFragment. This method will only be triggered once the user has
             * installed Google Play services and returned to the app.
             */

            @Override
            public void onMapReady(GoogleMap googleMap) {
                mGoogleMap = googleMap;
                setGoogleMapStyle(R.raw.defender_style);

                LocationUtils.addStaticGeofireLocations(mGeoFire, LocationUtils.getInputGeoFireLocations());
                setupListeners();
            }
        });
    }

    @Override
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

                        if(LocationUtils.checkPermissions(mActivity))
                            try {
                                LocationServices
                                        .FusedLocationApi
                                        .requestLocationUpdates(
                                                mGoogleApiClient,
                                                mLocationRequest,
                                                new LocationListener() {
                                                    @Override
                                                    public void onLocationChanged(Location location) {
                                                        updateCentralLocation(location);
                                                    }
                                                });
                            }
                            catch (SecurityException se) {
                                Log.d("Security exception", se.toString());
                            }
                        else{
                            LocationUtils.requestPermissions(mActivity);
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
    protected DatabaseReference createFireBaseReference() {
        return FirebaseDatabase.getInstance().getReference();
    }

    @Override
    protected GeoFire createGeoFire() {
        return new GeoFire(mFireBase.child(CONSTANTS.FIREBASE.GEOFIRE));
    }

    @Override
    protected GeoQuery createGeoQuery() {
        return mGeoFire.queryAtLocation(GEO_LOCATION_NESCAFE, 0.1);
    }

    /*
                                  Location Updates
     */

    protected void updateCentralLocation(Location location){
        double latitude = location.getLatitude();
        double longitude= location.getLongitude();

        mMyLocation = location;

        mGeoQuery.setCenter(new GeoLocation(
                latitude,
                longitude)
        );

        mGeoFire.setLocation(
                MY_LOCATION_TAG,
                new GeoLocation(latitude, longitude),
                new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        if(error != null)
                            Log.e("updateCentralLocation", error.toString());
                    }
                }
        );

        updateMapUI(location);
    }

    protected void updateMapUI(final Location location){
        runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        if(LocationUtils.checkPermissions(mActivity)){
                            mGoogleMap.clear();
                            MarkerOptions options = new MarkerOptions()
                                    .rotation(location.getBearing())
                                    .flat(true);

                            //Enemies
                            for(Map.Entry<String, GeoLocation> team : mTeamLocations.entrySet())
                                mGoogleMap.addMarker(
                                        options.position(LocationUtils.getLatLng(team.getValue()))
                                                .title(team.getKey())
                                                .icon(BitmapDescriptorFactory
                                                        .defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                                );

                            //Your Location
                            mGoogleMap.addMarker(
                                    options.position(LocationUtils.getLatLng(location))
                                            .title(MY_LOCATION_TAG)
                                            .icon(BitmapDescriptorFactory
                                                    .defaultMarker(BitmapDescriptorFactory.HUE_RED))
                            );

                            CameraPosition cameraPosition = CameraPosition
                                    .builder(mGoogleMap.getCameraPosition())
                                    .target(LocationUtils.getLatLng(mMyLocation))
                                    .zoom(19)
                                    .tilt(25)
                                    .bearing(location.getBearing())
                                    .build();

                            mGoogleMap.animateCamera(CameraUpdateFactory
                                    .newCameraPosition(cameraPosition));
                        }
                    }
                }
    );
    }

    /*
                                    Listener Setup
     */

    protected void setupListeners(){
        setFirebaseListeners();
        setGeoQueryListeners();
        setGoogleMapListeners();
    }

    protected void setFirebaseListeners(){}

    protected void setGeoQueryListeners(){
        mGeoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                Log.i("onKeyEntered", key + ">" + location.toString());
                if(!key.equals(MY_LOCATION_TAG))
                    mTeamLocations.put(key, location);
            }

            @Override
            public void onKeyExited(String key) {
                Log.i("onKeyExited", key);
                mTeamLocations.remove(key);
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                Log.d("LOCATION CHANGES", "MOVING");
            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }


    protected void setGoogleMapListeners(){
        mGoogleMap.setOnMarkerClickListener(
                new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {

                        return false;
                    }
                }
        );

        mGoogleMap.setInfoWindowAdapter(new TeamInfoAdapter(mActivity));

        mGoogleMap.setOnInfoWindowClickListener(
                new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        Toast.makeText(mActivity, "YO", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    /*
                            Google Map Styling
     */

    protected void setGoogleMapStyle(int resourceID){
        try{
            mGoogleMap.getUiSettings().setMapToolbarEnabled(false);

            mGoogleMap.setMaxZoomPreference(mGoogleMap.getMaxZoomLevel());
            mGoogleMap.setBuildingsEnabled(true);


//            if(!mGoogleMap.setMapStyle(
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
