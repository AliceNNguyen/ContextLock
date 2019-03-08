package com.example.alicenguyen.contextlock;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LocalDatabase extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "logEvents_database";
    public static final String TABLE_NAME = "logevents_table";
    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_USERID = "user_id";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_DETECTED_ACTIVITY= "detected_activity";
    public static final String COLUMN_DETECTED_WEATHER = "detected_weather";
    public static final String COLUMN_SEND = "send";


    LocalDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USERID + " TEXT, " +
                COLUMN_TIMESTAMP + " TEXT, " +
                COLUMN_DETECTED_ACTIVITY + " TEXT, " +
                COLUMN_DETECTED_WEATHER + " TEXT, " +
                COLUMN_SEND + " TEXT" + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);

    }

    public boolean saveToDB(String userid, String timestamp, String detectedActivity,
                            String detectedWeather, String send) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(LocalDatabase.COLUMN_USERID, userid);
        values.put(LocalDatabase.COLUMN_TIMESTAMP, timestamp);
        values.put(LocalDatabase.COLUMN_DETECTED_ACTIVITY, detectedActivity);
        values.put(LocalDatabase.COLUMN_DETECTED_WEATHER, detectedWeather);
        values.put(LocalDatabase.COLUMN_SEND, send);
        //values.put(LocalDatabase.COLUMN_SURVEY_CANCELED, surveyCanceled);
        long newRowId = database.insert(LocalDatabase.TABLE_NAME, null, values);
        if(newRowId == -1) {
            return false;
        }else{
            return true;
        }
    }

    public Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+ TABLE_NAME,null);
        return res;
    }

    public void deleteAllData() {
        SQLiteDatabase db= this.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
    }

}
