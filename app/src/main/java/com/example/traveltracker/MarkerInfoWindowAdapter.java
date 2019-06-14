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

    public void updateInfoWindow(Marker m)
    {
        EditText t1_Name = (EditText) window.findViewById(R.id.edtTxtName);
        EditText t1_Story = (EditText) window.findViewById(R.id.edtTxtStory);
        InfoData infoData = (InfoData) m.getTag();
        t1_Name.setText(infoData.Name);
        t1_Story.setText(infoData.Story);
        m.hideInfoWindow();
        m.showInfoWindow();
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return window;
    }

    @Override
    public View getInfoContents(Marker marker) {
        EditText t1_Name = (EditText) window.findViewById(R.id.edtTxtName);
        EditText t1_Story = (EditText) window.findViewById(R.id.edtTxtStory);
        InfoData infoData = (InfoData) marker.getTag();
        t1_Name.setText(infoData.Name);
        t1_Story.setText(infoData.Story);

        return window;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        EditText t1_Name = (EditText) window.findViewById(R.id.edtTxtName);
        EditText t1_Story = (EditText) window.findViewById(R.id.edtTxtStory);
        InfoData infodata = (InfoData) marker.getTag();
        t1_Name.setText(infodata.Name);
        t1_Story.setText(infodata.Story);
        marker.showInfoWindow();
        return true;
    }
}