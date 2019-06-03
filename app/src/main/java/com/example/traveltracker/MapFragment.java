package com.example.traveltracker;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment implements OnMapReadyCallback {
    private final String TAG = "MapFragment";

    private final int PERMISSION_ACCESS_FINE_LOCATION = 1;
    private final int LOCATION_UPDATE_MIN_DISTANCE = 0;
    private final int LOCATION_UPDATE_MIN_TIME = 0;
    private final int DEFAULT_ZOOM = 12;

    private Context context;
    private DBHelper dbHelper;
    private GoogleMap googleMap;
    private LocationManager locationManager;
    private final List<MemoryMarker> memories = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.map_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated");

        // https://stackoverflow.com/a/26598640
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);

        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        dbHelper = new DBHelper(context);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady");
        this.googleMap = googleMap;

        this.googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng position) {
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(position)
                        .snippet("Tap here to remove this marker")
                        .title("Marker Instance");
                Marker marker = createMarker(markerOptions);
                MemoryMarker memory = new MemoryMarker(marker, memories.size() + 1);
                memories.add(memory);
                addMarker(memory.getLocation());
            }
        });

        this.googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            // TODO: there should be a 'are you sure?' warning
            @Override
            public void onInfoWindowClick(Marker marker) {
                Log.d(TAG, memories.size() + " saved memories");

                if (memories.size() == 0) {
                    return;
                }

                String clickId = marker.getId();

                for (MemoryMarker memory : memories) {
                    if (clickId.equals(memory.markerId())) {
                        dbHelper.deleteMarker(memory.getId());
                        // FIXME: marker stays on the map untill the map gets refreshed
                        marker.remove();
                        Log.d(TAG, "removed memory #" + memory.getId());
                        memories.remove(memory);
                        return;
                    }
                }

                Log.d(TAG, "memory not found");
            }
        });

        if (hasGpsPermission()) {
            showCurrentLocation();
        }
        else {
            // TODO: show current location after getting permission
            askGpsPermission();
        }

        showSavedMarkers();
    }

    private final Marker createMarker(MarkerOptions markerOptions) {
        return googleMap.addMarker(markerOptions);
    }

    private void placeMarker(LatLng position) {
        MarkerOptions markerOptions = new MarkerOptions()
                .position(position)
                .snippet("Tap here to remove this marker")
                .title("Marker Instance");

        googleMap.addMarker(markerOptions);
    }

    private void addMarker(LatLng position) {
        dbHelper.addMarker(position.latitude, position.longitude);
    }

    private void showCurrentLocation() {
        Location currentLocation = getCurrentLocation();

        if (currentLocation != null) {
            LatLng position = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            moveCamera(position);
            placeMarker(position);
        }
    }

    private boolean hasGpsPermission() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void askGpsPermission() {
        ActivityCompat.requestPermissions((Activity) context,
                new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, PERMISSION_ACCESS_FINE_LOCATION);
    }

    @SuppressWarnings({"MissingPermission"})
    private Location getCurrentLocation() {
        if (hasGpsPermission()) {
            LocationListener locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    Log.d(TAG, "LocationListener.onLocationChanged");
                    // we only need to get the current location once
                    locationManager.removeUpdates(this);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {}

                @Override
                public void onProviderEnabled(String provider) {}

                @Override
                public void onProviderDisabled(String provider) {}
            };
            // GPS_PROVIDER shows incorrect location? Use NETWORK_PROVIDER for now
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    LOCATION_UPDATE_MIN_TIME, LOCATION_UPDATE_MIN_DISTANCE, locationListener);

            return locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        return null;
    }

    private void moveCamera(LatLng position) {
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, DEFAULT_ZOOM));
    }

    private void showSavedMarkers() {
        List<List<Object>> markerContents = dbHelper.getMarkerContents();

        if (markerContents.size() == 0) {
            return;
        }

        for (List<Object> markerContent : markerContents) {
            // 0: id    1: postion    2: title     3: snippet
            Long id = (Long) markerContent.get(0);
            LatLng position = (LatLng) markerContent.get(1);
            String title = (String) markerContent.get(2);
            String snippet = (String) markerContent.get(3);

            title = title == null ? "Marker instance" : title;
            snippet = snippet == null ? "Tap here to remove this marker." : snippet;

            MarkerOptions markerOptions = new MarkerOptions()
                    .position(position)
                    .snippet(snippet)
                    .title(title);
            Marker marker = createMarker(markerOptions);
            MemoryMarker memory = new MemoryMarker(marker, id);
            memories.add(memory);
            placeMarker(memory.getLocation());
        }
    }
}
