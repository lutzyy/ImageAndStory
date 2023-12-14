package com.example.cw4;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DHelper extends SQLiteOpenHelper {
    public DHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "mydatabase";

    public static final String IMG_TABLE =
            "CREATE TABLE IMGAGES(" + "_id INTEGER PRIMARY KEY AUTOINCREMENT, " + "NAME TEXT, " + "IMAGE BLOB, " + "IMAGE_DATE TEXT);";

    public static final String SKETCH_TABLE =
            "CREATE TABLE SKETCH(" + "_id INTEGER PRIMARY KEY AUTOINCREMENT, " + "SKETCH_NAME TEXT, " + "SKETCH_IMAGE BLOB, " + "SKETCH_DATE TEXT);";

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(IMG_TABLE);
        db.execSQL(SKETCH_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS IMGAGES");
        db.execSQL("DROP TABLE IF EXISTS SKETCH");
        onCreate(db);
    }
}

