package com.example.traveltracker;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class MapFragment extends Fragment implements OnMapReadyCallback{
    private final String TAG = "MapFragment";

    private final int PERMISSION_ACCESS_FINE_LOCATION = 1;
    private final int LOCATION_UPDATE_MIN_DISTANCE = 0;
    private final int LOCATION_UPDATE_MIN_TIME = 0;
    private final int DEFAULT_ZOOM = 12;

    private Context context;
    private DBHelper dbHelper;
    private GoogleMap googleMap;
    private LocationManager locationManager;
    private MarkerInfoWindowAdapter markerinfoWindowAdapter;
    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, "LocationListener.onLocationChanged");
            // we only need to get the current location once
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
        dbHelper = new DBHelper(context);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady");
        this.googleMap = googleMap;

        markerinfoWindowAdapter = new MarkerInfoWindowAdapter(context);

        // duration listener
        this.googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng position) {
                placeMarker(position);
                addMarker(position);
            }
        });



        this.googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            // TODO: there should be a 'are you sure?' warning
            @Override
            public void onInfoWindowClick(Marker marker) {
                //
                InfoData infodata = (InfoData) marker.getTag();

                //take input from user using popup after taking update inf object with newly entered value
                marker.setTag(infodata);

                openDialog(infodata, marker);
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

    private void openDialog(InfoData infodataobject, final Marker markerinstance) {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.layout_dialog);
        dialog.setTitle("Title...");

        // set the custom dialog components - text, image and button
        final EditText edtTxtName = (EditText) dialog.findViewById(R.id.edtTxtName);
        final EditText edtTxtStory = (EditText) dialog.findViewById(R.id.edtTxtStory);

        InfoData infoData = (InfoData)markerinstance.getTag();
        if(!infoData.Name.equals("<name>") && !infoData.Story.equals("<story>"))
        {
            edtTxtName.setText(infoData.Name);
            edtTxtStory.setText(infoData.Story);
        }

        ImageView imageView = (ImageView) dialog.findViewById(R.id.current_picture);
        imageView.setImageResource(R.drawable.lion);

        ImageButton dialogButton = (ImageButton) dialog.findViewById(R.id.btnOne);
        Button  btnSave = (Button) dialog.findViewById(R.id.btnSave);

        // if button is clicked, close the custom dialog
        dialogButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //dialog.dismiss();
                AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                alertDialog.setTitle("Alert");
                alertDialog.setMessage("Are you sure to delete this marker ?");
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface alert_dialog, int which) {
                                dbHelper.deleteMarker(markerinstance.getPosition());
                                markerinstance.remove();
                                alert_dialog.dismiss();
                                dialog.dismiss();
                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface alert_dialog, int which) {
                                alert_dialog.dismiss();
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                InfoData infoData = new InfoData();
                infoData.Name = edtTxtName.getText().toString();
                infoData.Story = edtTxtStory.getText().toString();
                markerinstance.setTag(infoData);
                markerinfoWindowAdapter.updateInfoWindow(markerinstance);
                dialog.dismiss();
            }
        });

        dialog.show();

    }


    private void placeMarker(LatLng position) {

        googleMap.setInfoWindowAdapter(markerinfoWindowAdapter);
        googleMap.setOnMarkerClickListener(markerinfoWindowAdapter);
        MarkerOptions markerOptions = new MarkerOptions()
                .position(position)
                // This is for Custom Marker icon logo..
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.mapplaceholder))
                .snippet("")
                .title("Marker Instance");

        Marker MarkerInstance = googleMap.addMarker(markerOptions);
        InfoData infoData = new InfoData();
        infoData.Name = "<name>";
        infoData.Story="<story>";
        MarkerInstance.setTag(infoData);
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
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ACCESS_FINE_LOCATION);
    }

    @SuppressWarnings({"MissingPermission"})

    private Location getCurrentLocation() {
        if (hasGpsPermission()) {
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
        List<LatLng> positions = dbHelper.getPositions();

        if (positions.isEmpty()) {
            return;
        }

        for (LatLng position : positions) {
            placeMarker(position);
        }
    }

    /*@Override
    public void applyTexts(String username, String password) {

    }*/
}