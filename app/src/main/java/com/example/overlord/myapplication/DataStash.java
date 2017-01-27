package com.example.overlord.myapplication;

import android.location.Location;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton
 * Holds dynamic data
 *
 */

class DataStash {

    private static DataStash sDataStash;
    private DataStash(){
        fireBase= FirebaseDatabase.getInstance().getReference();
        geoFire = new GeoFire(fireBase.child(CONSTANTS.FIREBASE.GEOFIRE));

    }
    static DataStash getDataStash() {
        if(sDataStash == null)
            sDataStash = new DataStash();
        return sDataStash;
    }

    /*
            All Team Bases
    */
    private GeoLocation geoLocations[] = new GeoLocation[]{
            new GeoLocation(22.253496, 84.902362),
            new GeoLocation(22.253449, 84.902495),
            new GeoLocation(22.253373, 84.902730),
            new GeoLocation(22.252920, 84.902039),
            new GeoLocation(22.253169, 84.902115),
            new GeoLocation(22.253423, 84.902204),
            new GeoLocation(22.253080, 84.902278),
            new GeoLocation(22.253148, 84.902558)
    };

    final GeoLocation[] getGeoLocations() {
        return geoLocations;
    }

    /**
     * Fuck Getter/Setters
     *
     */

    double querySize = 9.1;//KM
    Map<String, GeoLocation> enemyPlayerLocations = new ConcurrentHashMap<>();
    Location playerLocation;
    GoogleApiClient googleApiClient;
    GoogleMap googleMap;
    DatabaseReference fireBase;
    GeoFire geoFire;
    GeoQuery geoQuery;
}
