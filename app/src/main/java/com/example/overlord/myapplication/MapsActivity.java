package com.example.overlord.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

/*
    Map Styles used

    Defender Style
    MidnightCommander > https://twitter.com/adamkrogh

    Attacker Style

 */

public class MapsActivity extends BoilerplateMapsActivity {


    @Override
    protected Activity getPresentActivity() {
        return MapsActivity.this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
