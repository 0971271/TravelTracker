package com.example.traveltracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.traveltracker.contracts.MarkerContract;
import com.example.traveltracker.contracts.MemoryContract;
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
        db.execSQL(MemoryContract.SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(MarkerContract.SQL_DELETE_ENTRIES);
        db.execSQL(MemoryContract.SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void addMarker(double latitude, double longitude) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(MarkerContract.Entries.COLUMN_LATITUDE, latitude);
        values.put(MarkerContract.Entries.COLUMN_LONGITUDE, longitude);
        long id = db.insert(MarkerContract.Entries.TABLE_NAME, null, values);
        Log.d(TAG, "addMarker #" + id);
    }

    public List<List<Object>> getMarkerContents() {
        SQLiteDatabase db = getReadableDatabase();

        String[] projection = {
                MarkerContract.Entries._ID,
                MarkerContract.Entries.COLUMN_LATITUDE,
                MarkerContract.Entries.COLUMN_LONGITUDE,
                MarkerContract.Entries.COLUMN_SNIPPET,
                MarkerContract.Entries.COLUMN_TITLE
        };

        Cursor cursor = db.query(
                MarkerContract.Entries.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null
        );

        List<List<Object>> memories = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                List<Object> values = new ArrayList<>();

                Long id = cursor.getLong(
                        cursor.getColumnIndex(MarkerContract.Entries._ID)
                );
                double latitude = cursor.getDouble(
                        cursor.getColumnIndex(MarkerContract.Entries.COLUMN_LATITUDE)
                );
                double longitude = cursor.getDouble(
                        cursor.getColumnIndex(MarkerContract.Entries.COLUMN_LONGITUDE)
                );
                String title = cursor.getString(
                        cursor.getColumnIndex(MarkerContract.Entries.COLUMN_TITLE)
                );
                String snippet = cursor.getString(
                        cursor.getColumnIndex(MarkerContract.Entries.COLUMN_SNIPPET)
                );

                values.add(id);
                values.add(new LatLng(latitude, longitude));
                values.add(title);
                values.add(snippet);
                memories.add(values);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return memories;
    }

    public boolean deleteMarker(long id) {
        SQLiteDatabase db = getWritableDatabase();
        String selection = MarkerContract.Entries._ID + " = ?";
        String[] selectionArgs = { Long.toString(id) };
        return db.delete(MarkerContract.Entries.TABLE_NAME, selection, selectionArgs) == 1;
    }

    public boolean addMemory(long memoryId, String imageName) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(MemoryContract.Entries.COLUMN_IMAGE, imageName);
        String selection = MemoryContract.Entries.COLUMN_MARKER_ID + " LIKE ?";
        String[] selectionArgs = { Long.toString(memoryId) };
        return db.update(MemoryContract.Entries.TABLE_NAME, values, selection, selectionArgs) == 1;
    }
}