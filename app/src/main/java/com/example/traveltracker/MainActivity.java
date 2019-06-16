package com.example.traveltracker;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import android.content.Intent;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";

    public final int PERMISSION_CAMERA = 3;
    public final int PERMISSION_READ_EXTERNAL_STORAGE = 4;
    public final int PERMISSION_WRITE_EXTERNAL_STORAGE = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        //I added this if statement to keep the selected fragment when rotating the device
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new MapFragment())
                    .commit();
        }
        if (!hasCameraPermission()) {
            askCameraPermission();
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;

                    switch (item.getItemId()){
                        case R.id.nav_home:
                            selectedFragment = new MapFragment();
                            break;
                        case R.id.nav_travels:
                            selectedFragment = new TravelsFragment();
                            break;
                        case R.id.nav_camera:
                            captureImage();
                            // false so camera doesn't get highlighted
                            return false;
                        case R.id.nav_profile:
                            selectedFragment = new ProfileFragment();
                            break;
                    }

                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment)
                            .commit();

                    return true;
                }
            };

    public boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    public void askPermission(String permission, int requestCode) {
        if (hasPermission(permission)) {
            return;
        }

        ActivityCompat.requestPermissions(this,
                new String[] { permission }, requestCode);
    }

    public boolean hasCameraPermission() {
        return hasPermission(Manifest.permission.CAMERA);
    }

    public boolean hasReadExternalStoragePermission() {
        return hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    public void askCameraPermission() {
        askPermission(Manifest.permission.CAMERA, PERMISSION_CAMERA);
    }

    public void askReadExternalStoragePermission() {
        askPermission(Manifest.permission.READ_EXTERNAL_STORAGE, PERMISSION_READ_EXTERNAL_STORAGE);
    }

    public void askWriteExternalStoragePermission() {
        askPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_WRITE_EXTERNAL_STORAGE);
    }

    private void captureImage() {
        if (!hasCameraPermission()) {
            return;
        }

        Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        try {
            File image = createImage();

            if (image != null) {
                Uri imageUri = FileProvider.getUriForFile(this,
                        getPackageName() + ".fileprovider", image);

                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(captureImage, PERMISSION_CAMERA);
                Log.d(TAG, "created " + image.getName());
            }
        }
        catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private File createImage() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(timestamp, ".jpg", storageDir);
    }
}