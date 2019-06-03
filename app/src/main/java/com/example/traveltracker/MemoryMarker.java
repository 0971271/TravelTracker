package com.example.traveltracker;

import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.List;

public class MemoryMarker {
    private final Marker marker;
    private final long id;
    private List<String> images;

    public MemoryMarker(Marker marker, long id) {
        this.marker = marker;
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setText(String text) {
        marker.setSnippet(text);
    }

    public void setTitle(String title) {
        marker.setTitle(title);
    }

    public String getText() {
        return marker.getSnippet();
    }

    public String getTitle() {
        return marker.getTitle();
    }

    public List<String> getImages() {
        return images;
    }

    public void addImages(@NonNull String[] images) {
        if (images.length == 0) {
            return;
        }

        for (String image : images) {
            this.images.add(image);
        }
    }

    public final LatLng getLocation() {
        return marker.getPosition();
    }

    public final String markerId() {
        return marker.getId();
    }
}
