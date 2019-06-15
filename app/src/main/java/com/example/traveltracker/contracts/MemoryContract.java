package com.example.traveltracker.contracts;

import android.provider.BaseColumns;

public final class MemoryContract {
    private MemoryContract() {}

    public static class Entries implements BaseColumns {
        public final static String TABLE_NAME = "memory";
        public final static String COLUMN_MARKER_ID = "marker_id";
        public final static String COLUMN_IMAGE = "image";
    }

    public final static String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + Entries.TABLE_NAME +
                    " (" + Entries._ID + " INTEGER PRIMARY KEY, " +
                    Entries.COLUMN_IMAGE + " TEXT, " +
                    Entries.COLUMN_MARKER_ID + " INTEGER, " +
                    "FOREIGN KEY(" + Entries.COLUMN_MARKER_ID + ") REFERENCES " +
                    MarkerContract.Entries.TABLE_NAME + "(" + MarkerContract.Entries._ID + "))";

    public final static String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + Entries.TABLE_NAME;
}
