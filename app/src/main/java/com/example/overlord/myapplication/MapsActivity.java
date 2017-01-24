package com.example.overlord.myapplication;
import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
/*
    Map Styles used

    Defender Style
    MidnightCommander > https://twitter.com/adamkrogh

    Attacker Style

 */

public class MapsActivity extends FragmentActivity {

    private static final int REQUEST_LOCATION_CODE = 0;
    private static final String MY_LOCATION_TAG = "MY_LOCATION";
    private static final String GEOFIRE_NODE_TAG="LOCATIONS_GEOFIRE";
    private static final GeoLocation GEO_LOCATION_NESCAFE = new GeoLocation(22.2533788, 84.9025932);

    private static final String[] mRequiredPermissions = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET
    };

    private Activity mActivity = MapsActivity.this;
    private GeoFire mGeoFire;
    private GeoQuery mGeoQuery;
    private UiSettings mUiSettings;
    private Location mMyLocation;
    private GoogleMap mGoogleMap;
    private DatabaseReference mFirebase;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Map<String, GeoLocation> mTeamLocations = new ConcurrentHashMap<>();

    protected void initializeMembers() {
        mFirebase = FirebaseDatabase.getInstance().getReference();
        mGeoFire = new GeoFire(mFirebase.child(GEOFIRE_NODE_TAG));
        mGeoQuery = mGeoFire.queryAtLocation(GEO_LOCATION_NESCAFE, 0.1);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        mLocationRequest = LocationRequest
                                .create()
                                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                                .setInterval(1000);

                        if(appHasRequiredPermissions())
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
                            ActivityCompat.requestPermissions(
                                    mActivity,
                                    mRequiredPermissions,
                                    REQUEST_LOCATION_CODE
                            );
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

    protected void updateMapUI(final Location location){
        runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        if(appHasRequiredPermissions()){

                            for(Map.Entry<String, GeoLocation> team : mTeamLocations.entrySet())
                                mGoogleMap.addMarker(
                                        new MarkerOptions()
                                                .position(getLatLng(team.getValue()))
                                                .title(team.getKey())
                                                .flat(true)
                                                .rotation(mMyLocation.getBearing())
                                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher))
                                );

                            CameraPosition cameraPosition = CameraPosition
                                    .builder(mGoogleMap.getCameraPosition())
                                    .target(getLatLng(mMyLocation))
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

    protected void setupListeners(){
        mGeoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                Log.i("onKeyEntered", key + ">" + location.toString());
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
        mGoogleMap.setOnMarkerClickListener(
                new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        Toast.makeText(mActivity, marker.getTitle(), Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }
        );
        mGoogleMap.setInfoWindowAdapter(
                new GoogleMap.InfoWindowAdapter() {
                    @Override
                    public View getInfoWindow(Marker marker) {
                        TextView textView = new TextView(mActivity);
                        textView.setText(marker.getTitle());
                        return textView;
                    }

                    @Override
                    public View getInfoContents(Marker marker) {
                        TextView textView = new TextView(mActivity);
                        textView.setText(marker.getTitle());
                        return textView;
                    }
                }
        );
    }

    protected void setGoogleMapStyle(int resourceID){
        try{
            mUiSettings = mGoogleMap.getUiSettings();
            mUiSettings.setMapToolbarEnabled(false);

            mGoogleMap.setMaxZoomPreference(mGoogleMap.getMaxZoomLevel());
            mGoogleMap.setBuildingsEnabled(true);


            if(!mGoogleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            mActivity,
                            resourceID
                    )
            ))
                Log.e("STYLE", "FAILED");
        } catch (Resources.NotFoundException e){

            Log.e("STYLE", "NOT PRESENT");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);


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

                addStaticGeofireLocations(getInputGeoFireLocations());
                setupListeners();
            }
        });

    }
    /*
                                    UTILITY FUNCTIONS
     */
    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mGeoQuery.removeAllListeners();
        super.onDestroy();
    }

    protected LatLng getLatLng(GeoLocation location){
        return new LatLng(location.latitude, location.longitude);
    }

    protected LatLng getLatLng(Location location){
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    protected GeoLocation getGeoLocation(LatLng location){
        return new GeoLocation(location.latitude, location.longitude);
    }


    protected boolean appHasRequiredPermissions(){
        boolean permissionsGranted = true;

        for(String permission : mRequiredPermissions)
            permissionsGranted = permissionsGranted &&
                    ContextCompat
                            .checkSelfPermission(
                                    getApplicationContext(),
                                    permission)
                            !=
                            PackageManager.PERMISSION_DENIED;

        return permissionsGranted;
    }
    /*
                RUN ONCE
     */
    protected void addStaticGeofireLocations(Map<String, GeoLocation> staticGeolocations){
        for(Map.Entry<String, GeoLocation> locationEntry : staticGeolocations.entrySet())

            mGeoFire.setLocation(
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

    protected Map<String, GeoLocation> getInputGeoFireLocations(){
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
