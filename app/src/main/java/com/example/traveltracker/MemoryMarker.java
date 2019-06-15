package com.example.traveltracker;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MemoryMarker {
    private final Marker marker;
    private final long id;
    private final List<String> images = new ArrayList<>();

    public MemoryMarker(Marker marker, long id) {
        this.marker = marker;
        this.id = id;
    }

    public MemoryMarker(Marker marker, long id, String[] images) {
        this(marker, id);
        addImages(images);
    }

    public long getId() {
        return id;
    }

    public List<String> getImages() {
        return images;
    }

    public void addImages(String[] images) {
        if (images.length == 0) {
            return;
        }

        Collections.addAll(this.images, images);
    }

    public final LatLng getLocation() {
        return marker.getPosition();
    }

    public final String getMarkerId() {
        return marker.getId();
    }

    public boolean markerEquals(Marker marker) {
        return this.marker.equals(marker);
    }
}
