package com.example.traveltracker;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;


import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class MarkerInfoWindowAdapter implements GoogleMap.InfoWindowAdapter, GoogleMap.OnMarkerClickListener {
    private final static String TAG = "MarkerInfoWindowAdapter";
    private final int INFO_WINFOW_WIDTH = 300;
    private final int INFO_WINFOW_HEIGHT = 300;

    private final View window;
    private Context context;
    private EditText editTitle;
    private EditText editSnippet;
    private ImageView imageView;

    public MarkerInfoWindowAdapter(Context context) {
        this.context = context;
        window = LayoutInflater.from(context).inflate(R.layout.custom_info_window, null, false);
        editTitle = (EditText) window.findViewById(R.id.editTitle);
        editSnippet = (EditText) window.findViewById(R.id.editSnippet);
        imageView = (ImageView) window.findViewById(R.id.info_window_image);
    }

    public void updateInfoWindow(Marker marker) {
        editTitle.setText(marker.getTitle());
        editSnippet.setText(marker.getSnippet());
        marker.hideInfoWindow();
        marker.showInfoWindow();
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return window;
    }

    @Override
    public View getInfoContents(Marker marker) {
        editTitle.setText(marker.getTitle());
        editSnippet.setText(marker.getSnippet());
        return window;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        editTitle.setText(marker.getTitle());
        editSnippet.setText(marker.getSnippet());
        marker.showInfoWindow();
        return true;
    }

    public void setImageFromURI(Uri uri) {
        if (uri == null) {
            Log.e(TAG, "setImageFromURI null");
            return;
        }

        imageView.setLayoutParams(new LinearLayout.LayoutParams(INFO_WINFOW_WIDTH, INFO_WINFOW_HEIGHT));
        imageView.setImageURI(uri);
    }
}