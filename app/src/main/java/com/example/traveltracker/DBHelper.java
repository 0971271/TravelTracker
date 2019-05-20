package com.example.traveltracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.traveltracker.contracts.MarkerContract;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {
    private final static String TAG = "DBHelper";
    public final static int DATABASE_VERSION = 1;
    public final static String DATABASE_NAME = "traveltracker.db";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(MarkerContract.SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(MarkerContract.SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void addMarker(double latitude, double longitude) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(MarkerContract.MarkerEntries.COLUMN_LATITUDE, latitude);
        values.put(MarkerContract.MarkerEntries.COLUMN_LONGITUDE, longitude);
        long id = db.insert(MarkerContract.MarkerEntries.TABLE_NAME, null, values);
        Log.d(TAG, "addMarker #" + id);
    }

    // get the positions of the saved markers
    public List<LatLng> getPositions() {
        SQLiteDatabase db = getReadableDatabase();

        String[] projection = {
                MarkerContract.MarkerEntries.COLUMN_LATITUDE,
                MarkerContract.MarkerEntries.COLUMN_LONGITUDE
        };

        Cursor cursor = db.query(
                MarkerContract.MarkerEntries.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null
        );

        List<LatLng> positions = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                double latitude = cursor.getDouble(
                        cursor.getColumnIndex(MarkerContract.MarkerEntries.COLUMN_LATITUDE)
                );
                double longitude = cursor.getDouble(
                        cursor.getColumnIndex(MarkerContract.MarkerEntries.COLUMN_LONGITUDE)
                );
                positions.add(new LatLng(latitude, longitude));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return positions;
    }

    // TODO: a markers should be found with it's id instead of it's position
    public boolean deleteMarker(LatLng position) {
        SQLiteDatabase db = getWritableDatabase();
        String selection = MarkerContract.MarkerEntries.COLUMN_LATITUDE + " = ? AND " +
                MarkerContract.MarkerEntries.COLUMN_LONGITUDE + " = ?";
        String[] selectionArgs = { Double.toString(position.latitude), Double.toString(position.longitude) };
        return db.delete(MarkerContract.MarkerEntries.TABLE_NAME, selection, selectionArgs) == 1;
    }
}