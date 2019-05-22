package com.example.traveltracker.contracts;

import android.provider.BaseColumns;

public final class MarkerContract {
    private MarkerContract() {}

    public static class MarkerEntries implements BaseColumns {
        public final static String TABLE_NAME = "marker";
        public final static String COLUMN_LATITUDE = "latitude";
        public final static String COLUMN_LONGITUDE = "longitude";
    }

    public final static String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + MarkerEntries.TABLE_NAME +
            " (" + MarkerEntries._ID + " INTEGER PRIMARY KEY," +
            MarkerEntries.COLUMN_LATITUDE + " REAL," +
            MarkerEntries.COLUMN_LONGITUDE + " REAL)";

    public final static String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + MarkerEntries.TABLE_NAME;
}
