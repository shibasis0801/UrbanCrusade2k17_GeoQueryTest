package com.example.overlord.myapplication;

import android.support.v7.app.AppCompatActivity;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoQuery;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.database.DatabaseReference;

/**
 * Declare the following members and abstract creator methods :
 * > GoogleApiClient
 * > FireBase
 * > GeoFire
 * > GeoQuery
 *
 *
 *  Activity Lifecycle methods,
 *  onStart
 *  onStop
 *  depend on GoogleApiClient, GeoQuery member variables,
 *  Hence defined here.
 */

abstract class AbstractedGeofireActivity extends AppCompatActivity {
    /*
                                         Google Client Setup
     */
    protected GoogleApiClient mGoogleApiClient;

    protected GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    protected void setGoogleApiClient(GoogleApiClient googleApiClient) {
        mGoogleApiClient = googleApiClient;
    }

    //Override this function
    protected abstract GoogleApiClient createGoogleApiClient();

    /*
                                        Firebase Setup
     */

    protected DatabaseReference mFireBase;

    public DatabaseReference getFireBase() {
        return mFireBase;
    }

    public void setFireBase(DatabaseReference fireBase) {
        mFireBase = fireBase;
    }

    protected abstract DatabaseReference createFireBaseReference();

    /*
                                         GeoFire Setup
         */
    protected GeoFire mGeoFire;

    public GeoFire getGeoFire() {
        return mGeoFire;
    }

    public void setGeoFire(GeoFire geoFire) {
        mGeoFire = geoFire;
    }

    //Override this function
    protected abstract GeoFire createGeoFire();

    /*
                                         GeoQuery Setup
     */
    protected GeoQuery mGeoQuery;

    public GeoQuery getGeoQuery() {
        return mGeoQuery;
    }

    public void setGeoQuery(GeoQuery geoQuery) {
        mGeoQuery = geoQuery;
    }

    protected abstract GeoQuery createGeoQuery();
    /*
                                        Activity Lifecycle Methods
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
    /*
                                            INITIATOR
     */
    protected void initializeMembers(){
        setGoogleApiClient(createGoogleApiClient());
        setFireBase(createFireBaseReference());
        setGeoFire(createGeoFire());
        setGeoQuery(createGeoQuery());
    }
}
