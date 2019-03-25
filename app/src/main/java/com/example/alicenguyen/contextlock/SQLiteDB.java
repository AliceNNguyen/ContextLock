package com.example.alicenguyen.contextlock;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteDB extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "eventslog_database";
    public static final String TABLE_NAME = "eventslog_table";
    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_USERID = "user_id";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_REASON = "displayed_reason";
    public static final String COLUMN_PIN_USED = "pin_used";

    public static final String TABLE_NAME_UNLOCK = "unlock_events_table";
    public static final String COLUMN_UNLOCK_FIRST_TIMESTAMP = "unlock_time";
    public static final String COLUMN_UNLOCK_FAILED_COUNTER = "unlock_failed_counter";
    public static final String COLUMN_UNLOCK_SUCCESS_TIMESTAMP = "unlock_success_time";

    public static final String TABLE_NAME_UNLOCKEVENTS_SUCCESS = "unlock_successevents_table";
    public static final String COLUMN_ON_UNLOCK = "on_unlock_success";




    SQLiteDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USERID + " TEXT, " +
                COLUMN_TIMESTAMP + " TEXT, " +
                COLUMN_REASON + " TEXT, " +
                COLUMN_PIN_USED + " TEXT" + ")");

        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_NAME_UNLOCK + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USERID + " TEXT, " +
                COLUMN_UNLOCK_FAILED_COUNTER + " TEXT, " +
                COLUMN_UNLOCK_SUCCESS_TIMESTAMP + " TEXT" + ")");

        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_NAME_UNLOCKEVENTS_SUCCESS+ " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_ON_UNLOCK + " TEXT" + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_UNLOCK);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_UNLOCKEVENTS_SUCCESS);

        onCreate(sqLiteDatabase);

    }

    public boolean saveToDB(String userid, String timestamp, String pinUsed,
                            String reason) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SQLiteDB.COLUMN_USERID, userid);
        values.put(SQLiteDB.COLUMN_TIMESTAMP, timestamp);
        values.put(SQLiteDB.COLUMN_REASON, reason);
        values.put(SQLiteDB.COLUMN_PIN_USED, pinUsed);
        long newRowId = database.insert(SQLiteDB.TABLE_NAME, null, values);
        if (newRowId == -1) {
            return false;
        } else {
            return true;
        }
    }

    public boolean saveUnlockEventsToDB(String userid, String failedCounter,String unlockSuccessTimestamp) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SQLiteDB.COLUMN_USERID, userid);
        values.put(SQLiteDB.COLUMN_UNLOCK_FAILED_COUNTER, failedCounter);
        values.put(SQLiteDB.COLUMN_UNLOCK_SUCCESS_TIMESTAMP, unlockSuccessTimestamp);

        long newRowId = database.insert(SQLiteDB.TABLE_NAME_UNLOCK, null, values);
        if (newRowId == -1) {
            return false;
        } else {
            return true;
        }
    }

    public boolean saveSUnlockSuccessEventsToDB(String timestamp) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SQLiteDB.COLUMN_ON_UNLOCK, timestamp);
        long newRowId = database.insert(SQLiteDB.TABLE_NAME_UNLOCKEVENTS_SUCCESS, null, values);
        if (newRowId == -1) {
            return false;
        } else {
            return true;
        }
    }

    public Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE_NAME, null);
        return res;
    }

    public Cursor getUnlockData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE_NAME_UNLOCK, null);
        return res;
    }

    public Cursor getSuccessUnlockData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE_NAME_UNLOCK, null);
        return res;
    }



    public void deleteAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
    }

}
