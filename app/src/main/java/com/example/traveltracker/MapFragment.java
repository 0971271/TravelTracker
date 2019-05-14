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

public class MapFragment extends Fragment implements OnMapReadyCallback {
    private final String TAG = "MapFragment";

    private final int PERMISSION_ACCESS_FINE_LOCATION = 1;
    private final int LOCATION_UPDATE_MIN_DISTANCE = 0;
    private final int LOCATION_UPDATE_MIN_TIME = 0;
    private final int DEFAULT_ZOOM = 12;

    private Context context;
    private GoogleMap googleMap;
    private LocationManager locationManager;
    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, "LocationListener.onLocationChanged");
            locationManager.removeUpdates(locationListener);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };


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
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady");
        this.googleMap = googleMap;

        this.googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                addMarker(latLng);
            }
        });

        this.googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            // TODO: there should be a 'are you sure?' warning
            @Override
            public void onInfoWindowClick(Marker marker) {
                marker.remove();
            }
        });

        if (hasGpsPermission()) {
            showCurrentLocation();
        }
        else {
            askGpsPermission();
        }
    }

    private void addMarker(LatLng position) {
        MarkerOptions markerOptions = new MarkerOptions()
            .position(position)
            .snippet("Tap here to remove this marker")
            .title("Marker Instance");

        googleMap.addMarker(markerOptions);
    }

    private void showCurrentLocation() {
        Location currentLocation = getCurrentLocation();

        if (currentLocation != null) {
            LatLng position = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            moveCamera(position);
            addMarker(position);
        }
    }

    private boolean hasGpsPermission() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void askGpsPermission() {
        ActivityCompat.requestPermissions((Activity) context,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ACCESS_FINE_LOCATION);
    }

    private Location getCurrentLocation() {
        if (hasGpsPermission()) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    LOCATION_UPDATE_MIN_TIME, LOCATION_UPDATE_MIN_DISTANCE, locationListener);

            return locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        return null;
    }

    private void moveCamera(LatLng position) {
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, DEFAULT_ZOOM));
    }
}
