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
    private final int PERMISSION_CAMERA = 3;

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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                captureImage();
            }
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


    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) ==PackageManager.PERMISSION_GRANTED;
    }

    private void askCameraPermission() {
        ActivityCompat.requestPermissions(this,
                new String[] { Manifest.permission.CAMERA }, PERMISSION_CAMERA);
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