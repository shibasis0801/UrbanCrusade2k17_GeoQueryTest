package com.example.overlord.myapplication;

import android.app.Activity;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
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
/**
 *  Function Hierarchy
 *
 *  1   onCreate, creator
 *  2   Location update
 *  3   Listener
 *  4   Styling
 */
public class MapsActivity extends InitializedGeoFireActivity {

    @Override
    protected Activity getPresentActivity() {
        return MapsActivity.this;
    }

    private static final String MY_LOCATION_TAG ="MY_LOCATION";

    private Activity mActivity = MapsActivity.this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
}
