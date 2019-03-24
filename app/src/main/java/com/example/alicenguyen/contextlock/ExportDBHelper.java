package com.example.alicenguyen.contextlock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.Calendar;

public class ExportDBHelper extends BroadcastReceiver {
    private static final String TAG = "ExportDBHelper";
    //private LocalDatabase db;
    private SQLiteDB db;
    private Cursor cursor;
    private Context ctx;
    private String userid, version, timestamp;
    private String exportTime;


    @Override
    public void onReceive(Context context, Intent intent) {
        ctx = context.getApplicationContext();
        exportTime = Calendar.getInstance().getTime().toString();
        db = new SQLiteDB(context);
        exportUnlockEvents();
        //db = new LocalDatabase(context);
        //readDataFromDB();
        //readDataFromSQliteDB();
        //resetNotificationCounter();
        resetLockscreenShowCounter();
    }

    private void readDataFromSQliteDB() {
        cursor = db.getAllData();
        if (cursor.moveToFirst() && cursor.getCount() >0) {
            Log.e(TAG, String.valueOf(cursor.moveToFirst()));
            Log.e(TAG, String.valueOf(cursor.getCount()));
            do {
                version = SharedPreferencesStorage.readSharedPreference(ctx, Constants.PREFERENCES, Constants.VERSION_KEY);
                userid = cursor.getString(cursor.getColumnIndex(SQLiteDB.COLUMN_USERID));
                timestamp = cursor.getString(cursor.getColumnIndex(SQLiteDB.COLUMN_TIMESTAMP));
                String reason = cursor.getString(cursor.getColumnIndex(SQLiteDB.COLUMN_REASON));
                String pinUsed = cursor.getString(cursor.getColumnIndex(SQLiteDB.COLUMN_PIN_USED));
                exportDataToFirebase(version, userid, timestamp, reason, pinUsed);
                Log.e(TAG, userid);
                Log.e(TAG, version);
                Log.e(TAG, timestamp);
                Log.e(TAG, pinUsed);
                Log.e(TAG, reason);
            } while (cursor.moveToNext());
            //exportUnlockCounter(userid, version);
        }
        exportUnlockEvents();
    }

    private void exportDataToFirebase(String version, String userid, String timestamp, String reason, String pinUsed){
        if(cursor.getCount() > 0) {
            Log.e(TAG, userid);
            Log.e(TAG, version);
            Log.e(TAG, timestamp);
            Log.e(TAG, pinUsed);
            Log.e(TAG, reason);
            //export sql lite database to firebase when there is data in the table
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("logEvents").child(version).child(userid);
            ref.child(timestamp).child("reason").setValue(reason);
            ref.child(timestamp).child("pin_used").setValue(pinUsed);
            Log.e(TAG, "exported");
            //TODO
            //delete database entries after exported?
            //db.deleteAllData();
        } else {
            Log.e(TAG, "database empty");
        }
    }

    private void exportUnlockEvents() {
        cursor = db.getUnlockData();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                //String version = SharedPreferencesStorage.readSharedPreference(ctx, Constants.PREFERENCES, Constants.VERSION_KEY);
                String version = SharedPreferencesStorage.readSharedPreference(ctx, Constants.PREFERENCES, Constants.VERSION_KEY);
                String userid = cursor.getString(cursor.getColumnIndex(SQLiteDB.COLUMN_USERID));
                String failCounter = cursor.getString(cursor.getColumnIndex(SQLiteDB.COLUMN_UNLOCK_FAILED_COUNTER));
                String successUnlockTimestamp = cursor.getString(cursor.getColumnIndex(SQLiteDB.COLUMN_UNLOCK_SUCCESS_TIMESTAMP));

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("unlockEvents").child(version).child(userid);
                ref.child(exportTime).child("failed_counter").setValue(failCounter);
                ref.child(exportTime).child("success_time").setValue(successUnlockTimestamp);
            } while (cursor.moveToNext());
        }
    }

    /*private void readDataFromDB() {
        cursor = db.getAllData();
        if (cursor.moveToFirst()) {
            do {
                version = SharedPreferencesStorage.readSharedPreference(ctx, Constants.PREFERENCES, Constants.VERSION_KEY);
                userid = cursor.getString(cursor.getColumnIndex(LocalDatabase.COLUMN_USERID));
                timestamp = cursor.getString(cursor.getColumnIndex(LocalDatabase.COLUMN_TIMESTAMP));
                String detectedActivity = cursor.getString(cursor.getColumnIndex(LocalDatabase.COLUMN_DETECTED_ACTIVITY));
                String detectedWeather = cursor.getString(cursor.getColumnIndex(LocalDatabase.COLUMN_DETECTED_WEATHER));
                String send = cursor.getString(cursor.getColumnIndex(LocalDatabase.COLUMN_ISLOCKED));

                exportDataToFirebase(version, userid, timestamp, detectedActivity, detectedWeather, send);
            } while (cursor.moveToNext());
            exportUnlockCounter(userid, version);

        }
        exportUnlockEvents();

    }*/

    /*
    private void exportUnlockEvents() {
        cursor = db.getUnlockData();
        if (cursor.moveToFirst()) {
            do {
                String key = cursor.getString(cursor.getColumnIndex(LocalDatabase.COLUMN_ID));
                String unlockTime = cursor.getString(cursor.getColumnIndex(LocalDatabase.COLUMN_ON_UNLOCK));
                String version = SharedPreferencesStorage.readSharedPreference(ctx, Constants.PREFERENCES, Constants.VERSION_KEY);
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("unlockEvents").child(version).child(userid);
                ref.child(exportTime).child(unlockTime).setValue(key);
            } while (cursor.moveToNext());
        }
    }*/

    /*private void exportDataToFirebase(String version, String userid,String timestamp,String detectedActivity, String detectedWeather, String send ) {
        if(cursor.getCount() > 0) {
            //export sql lite database to firebase when there is data in the table
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("logEvents").child(version).child(userid);
            ref.child(timestamp).child("detectedActivity").setValue(detectedActivity);
            ref.child(timestamp).child("detectedWeather").setValue(detectedWeather);
            ref.child(timestamp).child("isLocked").setValue(send);
            Log.e(TAG, "exported");
            //TODO
            //delete database entries after exported?
            //db.deleteAllData();
        } else {
            Log.e(TAG, "database empty");
        }
    }*/

    private void exportUnlockCounter(String userid, String version) {
        String unlockCounter = SharedPreferencesStorage.readSharedPreference(ctx, Constants.PREFERENCES, Constants.UNLOCK_COUNTER_KEY);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("unlockEvents").child(version).child(userid);
        ref.child(exportTime).child("unlockCounter").setValue(unlockCounter);

        if(Integer.parseInt(unlockCounter) > 0) {
            ctx.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE).edit().remove(Constants.UNLOCK_COUNTER_KEY).apply();
        }
    }

    private void resetLockscreenShowCounter() {
        Log.e(TAG, "reset lockscreen counter");
        ctx.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE).edit().remove(Constants.LOCKSCREEN_SHOW_KEY).apply();
        Log.e(TAG, SharedPreferencesStorage.readSharedPreference(ctx, Constants.PREFERENCES, Constants.LOCKSCREEN_SHOW_KEY));

    }

    /*daily reset notification send counter*/
    private void resetNotificationCounter() {
        Log.e(TAG, "reset counter");
        ctx.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE).edit().remove(Constants.NOTIFICATION_SEND_KEY).apply();
        Log.e(TAG, SharedPreferencesStorage.readSharedPreference(ctx, Constants.PREFERENCES, Constants.NOTIFICATION_SEND_KEY));
    }
}


