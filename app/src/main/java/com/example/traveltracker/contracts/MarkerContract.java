package com.example.traveltracker.contracts;

import android.provider.BaseColumns;

public final class MarkerContract {
    private MarkerContract() {}

    public static class Entries implements BaseColumns {
        public final static String TABLE_NAME = "marker";
        public final static String COLUMN_LATITUDE = "latitude";
        public final static String COLUMN_LONGITUDE = "longitude";
        public final static String COLUMN_SNIPPET = "snippet";
        public final static String COLUMN_TITLE = "title";
        public final static String COLUMN_IMAGE = "image";
    }

    public final static String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + Entries.TABLE_NAME +
                    " (" + Entries._ID + " INTEGER PRIMARY KEY," +
                    Entries.COLUMN_LATITUDE + " REAL," +
                    Entries.COLUMN_LONGITUDE + " REAL," +
                    Entries.COLUMN_SNIPPET + " TEXT, " +
                    Entries.COLUMN_TITLE + " TEXT, " +
                    Entries.COLUMN_IMAGE + " TEXT)";

    public final static String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + Entries.TABLE_NAME;
}
