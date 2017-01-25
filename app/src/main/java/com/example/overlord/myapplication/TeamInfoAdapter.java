package com.example.overlord.myapplication;

import android.app.Activity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by overlord on 25/1/17.
 */

public class TeamInfoAdapter implements GoogleMap.InfoWindowAdapter {
    private final CardView mCardView;
    private final ImageView mTeamIcon;
    private final TextView mTeamName;
    private final TextView  mTeamPower;

    public TeamInfoAdapter(Activity activity){
        mCardView = (CardView)  activity.getLayoutInflater().inflate(R.layout.team_card_view, null);
        mTeamIcon = (ImageView) mCardView.findViewById(R.id.team_icon_image_view);
        mTeamName = (TextView)  mCardView.findViewById(R.id.team_name_text_view);
        mTeamPower= (TextView)  mCardView.findViewById(R.id.team_power_text_view);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        mTeamName.setText(marker.getTitle());
        return mCardView;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}

