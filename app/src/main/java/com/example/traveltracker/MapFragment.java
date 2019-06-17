package com.example.traveltracker;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment implements OnMapReadyCallback{
    private final String TAG = "MapFragment";

    private final String DEFAULT_MARKER_TITLE = "Give a Title marker here!";
    private final String DEFAULT_MARKER_SNIPPET = "Whats your story?";
    private final String MARKER_TITLE_HINT = "Hi, please add your name.";
    private final String MARKER_SNIPPET_HINT = "Would you mind sharing your story?";

    private final int PERMISSION_ACCESS_FINE_LOCATION = 1;
    private final int LOCATION_UPDATE_MIN_DISTANCE = 0;
    private final int LOCATION_UPDATE_MIN_TIME = 0;
    private final int DEFAULT_ZOOM = 12;

    private final int DIALOG_IMAGE_WIDTH = 500;
    private final int DIALOG_IMAGE_HEIGTH = 500;

    private Context context;
    private DBHelper dbHelper;
    private GoogleMap googleMap;
    private LocationManager locationManager;
    private MarkerInfoWindowAdapter markerInfoWindowAdapter;
    private MainActivity main;
    private Uri selectedImageUri;
    private ImageView dialogImage;

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
        main = (MainActivity) context;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady");
        this.googleMap = googleMap;

        markerInfoWindowAdapter = new MarkerInfoWindowAdapter(context);

        this.googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng position) {
                createMarker(position);
                addMarker(position);
                Marker marker = createMarker(position);
                MemoryMarker memory = new MemoryMarker(marker, memories.size() + 1);
                memories.add(memory);
                Log.d(TAG, "#memories" + memories.size());
            }
        });

        this.googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                openDialog(marker);
            }
        });

        if (hasGpsPermission()) {
            showCurrentLocation();
        }
        else {
            // TODO: show current location after getting permission
            askGpsPermission();
        }

        googleMap.setInfoWindowAdapter(markerInfoWindowAdapter);
        googleMap.setOnMarkerClickListener(markerInfoWindowAdapter);
        showSavedMarkers();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == main.PERMISSION_READ_EXTERNAL_STORAGE && resultCode == Activity.RESULT_OK) {
            if (dialogImage == null) {
                Log.e(TAG, "dialogImage == null");
                return;
            }

            selectedImageUri = data.getData();
            Log.d(TAG, "onActivityResult() select image: " + getImagePathFromUri(selectedImageUri));
            dialogImage.setImageURI(selectedImageUri);
            markerInfoWindowAdapter.setImageFromURI(selectedImageUri);
        }
    }

    private void openDialog(final Marker marker) {
        final Dialog dialog = new Dialog(context);
        final long memoryId = findMemoryId(marker);

        dialog.setContentView(R.layout.layout_dialog);
        dialog.setTitle("Title...");

        Log.d(TAG, "open dialog for memory " + memoryId);

        // set the custom dialog components - text, image and button
        final EditText editTitle = (EditText) dialog.findViewById(R.id.editTitle);
        final EditText editSnippet = (EditText) dialog.findViewById(R.id.editSnippet);
        final ImageView imageView = (ImageView) dialog.findViewById(R.id.current_picture);

        editTitle.setHint(MARKER_TITLE_HINT);
        editSnippet.setHint(MARKER_SNIPPET_HINT);

        if(!marker.getTitle().equals(DEFAULT_MARKER_TITLE) && !marker.getSnippet().equals(DEFAULT_MARKER_SNIPPET)) {
            editTitle.setText(marker.getTitle());
            editSnippet.setText(marker.getSnippet());
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(DIALOG_IMAGE_WIDTH, DIALOG_IMAGE_HEIGTH);
        imageView.setLayoutParams(params);

        if (selectedImageUri == null) {
            imageView.setImageResource(R.drawable.lion);
        }
        else {
            imageView.setImageURI(selectedImageUri);
        }
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
                                if (memoryId == -1) {
                                    Log.d(TAG, "id " + memoryId + " doesn't belong to a memory");
                                    return;
                                }

                                dbHelper.deleteMarker(memoryId);
                                marker.remove();
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
            public void onClick(View view) {
                String name = editTitle.getText().toString();
                String story = editSnippet.getText().toString();
                marker.setTitle(name);
                marker.setSnippet(story);
                if (memoryId != -1) {
                    dbHelper.updateMarker(memoryId, name, story);
                }

                dialog.dismiss();
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (main.hasReadExternalStoragePermission()) {
                    dialogImage = imageView;
                    Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    galleryIntent.setType("image/*");
                    startActivityForResult(galleryIntent, main.PERMISSION_READ_EXTERNAL_STORAGE);
                }
                else {
                    main.askReadExternalStoragePermission();
                }
            }
        });

        dialog.show();
    }

    private Marker createMarker(LatLng position) {
        MarkerOptions markerOptions = new MarkerOptions()
                .position(position)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons8))
                .title(DEFAULT_MARKER_TITLE)
                .snippet(DEFAULT_MARKER_SNIPPET);

        return googleMap.addMarker(markerOptions);
    }

    private Marker createMarker(MarkerResult markerResult) {
        googleMap.setInfoWindowAdapter(markerInfoWindowAdapter);
        googleMap.setOnMarkerClickListener(markerInfoWindowAdapter);
        String title = markerResult.getTitle();
        String snippet = markerResult.getSnippet();

        title = title == null ? DEFAULT_MARKER_TITLE : title;
        snippet = snippet == null ? DEFAULT_MARKER_SNIPPET :snippet;

        MarkerOptions markerOptions = new MarkerOptions()
                .position(markerResult.getLocation())
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons8))
                .title(title)
                .snippet(snippet);

        return googleMap.addMarker(markerOptions);
    }

    private void addMarker(LatLng position) {
        dbHelper.addMarker(position.latitude, position.longitude);
    }

    private void showCurrentLocation() {
        Location currentLocation = getCurrentLocation();

        if (currentLocation != null) {
            LatLng position = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            moveCamera(position);
            // show a standard marker for the current location
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(position)
                    .title("You are here");
            googleMap.addMarker(markerOptions);
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
        List<MarkerResult> markerResults = dbHelper.getMarkers();

        if (markerResults.size() == 0) {
            return;
        }

        for (MarkerResult markerResult : markerResults) {
            Marker marker = createMarker(markerResult);
            MemoryMarker memoryMarker = new MemoryMarker(marker, markerResult.getId());
            memories.add(memoryMarker);
        }
    }

    // returns -1 if the given marker doesn't have a memory
    private long findMemoryId(Marker marker) {
        if (memories.size() == 0) {
            Log.d(TAG, "memories.size() == 0");
            return -1;
        }

        for (MemoryMarker memory : memories) {
            if (memory.markerEquals(marker)) {
                return memory.getId();
            }
        }

        return -1;
    }

    // get the path to save into the database
    private String getImagePathFromUri(Uri uri) {
        if (uri == null) {
            return "";
        }

        Cursor cursor = main.getContentResolver()
                .query(uri, null, null, null, null, null);
        String path;

        if (cursor == null) {
            path = uri.getPath();
        }
        else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            path = cursor.getString(index);
        }

        cursor.close();
        return path;
    }

    // returns null if no uri is found
    private Uri getImageURIForMemory(long id) {
        for (MemoryMarker memory : memories) {
            if (memory.getId() == id) {
                return Uri.parse(memory.getImages().get(0));
            }
        }

        return null;
    }
}