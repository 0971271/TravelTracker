package com.example.traveltracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;


import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class MarkerInfoWindowAdapter implements GoogleMap.InfoWindowAdapter, GoogleMap.OnMarkerClickListener
{

    private final View window;
    private Context context;

    public MarkerInfoWindowAdapter(Context context) {
        this.context = context;
        window = LayoutInflater.from(context).inflate(R.layout.custom_info_window, null, false);
    }

    public void updateInfoWindow(Marker marker)
    {
        EditText t1_Name = (EditText) window.findViewById(R.id.editTitle);
        EditText t1_Story = (EditText) window.findViewById(R.id.editSnippet);
        t1_Name.setText(marker.getTitle());
        t1_Story.setText(marker.getSnippet());
        marker.hideInfoWindow();
        marker.showInfoWindow();
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return window;
    }

    @Override
    public View getInfoContents(Marker marker) {
        EditText t1_Name = (EditText) window.findViewById(R.id.editTitle);
        EditText t1_Story = (EditText) window.findViewById(R.id.editSnippet);
        t1_Name.setText(marker.getTitle());
        t1_Story.setText(marker.getSnippet());
        return window;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        EditText t1_Name = (EditText) window.findViewById(R.id.editTitle);
        EditText t1_Story = (EditText) window.findViewById(R.id.editSnippet);
        t1_Name.setText(marker.getTitle());
        t1_Story.setText(marker.getSnippet());
        marker.showInfoWindow();
        return true;
    }
}