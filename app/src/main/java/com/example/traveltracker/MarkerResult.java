package com.example.traveltracker;

import com.google.android.gms.maps.model.LatLng;

public class MarkerResult {
    private long id;
    private String title;
    private String snippet;
    private LatLng location;

    public MarkerResult(long id, String title, String snippet, LatLng location) {
        this.id = id;
        this.title = title;
        this.snippet = snippet;
        this.location = location;
    }

    public long getId() { return id; }

    public String getTitle() { return title; }

    public String getSnippet() { return snippet; }

    public LatLng getLocation() { return location; }
}