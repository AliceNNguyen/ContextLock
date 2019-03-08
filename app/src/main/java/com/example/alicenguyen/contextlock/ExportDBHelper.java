package com.example.alicenguyen.contextlock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.telecom.Call;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.security.acl.LastOwnerException;
import java.util.ArrayList;
import java.util.List;

public class ExportDBHelper extends BroadcastReceiver {
    private static final String TAG = "ExportDBHelper";
    private LocalDatabase db;
    private Cursor cursor;
    private Context ctx;
    private String userid, version;


    @Override
    public void onReceive(Context context, Intent intent) {
        ctx = context.getApplicationContext();
        db = new LocalDatabase(context);
        readDataFromDB();
        resetNotificationCounter();
    }

    private void readDataFromDB() {
        cursor = db.getAllData();
        if (cursor.moveToFirst()) {
            do {
                version = SharedPreferencesStorage.readSharedPreference(ctx, Constants.PREFERENCES, Constants.VERSION_KEY);
                userid = cursor.getString(cursor.getColumnIndex(LocalDatabase.COLUMN_USERID));
                String timestamp = cursor.getString(cursor.getColumnIndex(LocalDatabase.COLUMN_TIMESTAMP));
                String detectedActivity = cursor.getString(cursor.getColumnIndex(LocalDatabase.COLUMN_DETECTED_ACTIVITY));
                String detectedWeather = cursor.getString(cursor.getColumnIndex(LocalDatabase.COLUMN_DETECTED_WEATHER));
                String send = cursor.getString(cursor.getColumnIndex(LocalDatabase.COLUMN_SEND));

                exportDataToFirebase(version, userid, timestamp, detectedActivity, detectedWeather, send);
            } while (cursor.moveToNext());
        }
        exportUnlockCounter(userid, version);
    }

    private void exportDataToFirebase(String version, String userid,String timestamp,String detectedActivity, String detectedWeather, String send ) {
        if(cursor.getCount() > 0) {
            //do some stuff when there is data in the table

            //FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("logEvents").child(version).child(userid);
            ref.child(timestamp).child("detectedActivity").setValue(detectedActivity);
            ref.child(timestamp).child("detectedWeather").setValue(detectedWeather);
            ref.child(timestamp).child("send").setValue(send);
            Log.e(TAG, "exported");
            //TODO
            //db.deleteAllData();
            /*delete database after exported*/
        } else {
            //do some stuff when there is no data
            Log.e(TAG, "database empty");
        }
    }

    private void exportUnlockCounter(String userid, String version) {
        String unlockCounter = SharedPreferencesStorage.readSharedPreference(ctx, Constants.PREFERENCES, Constants.UNLOCK_COUNTER_KEY);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("unlockEvents").child(version).child(userid);
        ref.child("unlockCounter").setValue(unlockCounter);
    }

    private void resetNotificationCounter() {
        Log.e(TAG, "reset counter");
        ctx.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE).edit().remove(Constants.NOTIFICATION_SEND_KEY).apply();
        Log.e(TAG, SharedPreferencesStorage.readSharedPreference(ctx, Constants.PREFERENCES, Constants.NOTIFICATION_SEND_KEY));

    }
}


