package com.lmu.alicenguyen.contextlock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.Calendar;

import static android.content.Context.MODE_PRIVATE;

/*Export data from local SQLiteDB to online Firebase*/
public class ExportDBHelper extends BroadcastReceiver {
    private static final String TAG = "ExportDBHelper";
    private SQLiteDB db;
    private Cursor cursor;
    private Context ctx;
    private String exportTime;


    @Override
    public void onReceive(Context context, Intent intent) {
        ctx = context.getApplicationContext();
        exportTime = Calendar.getInstance().getTime().toString();
        db = new SQLiteDB(context);
        exportUnlockEvents();
        exportUnlockSuccessEvents(context);
        resetLockscreenShowCounter();
    }

    /*export the number of failed unlock events
    * for some version this event only works if user switch to fallback unlock method, not for failed fingerprint*/
    private void exportUnlockEvents() {
        cursor = db.getUnlockData();
        if (cursor != null && cursor.moveToFirst()) {
            do {
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

    /*export local database to firebase
    * table show time of successful unlock and corresponding unlock number*/
    private void exportUnlockSuccessEvents(Context context) {
        Log.e(TAG, "get success unlock data");
        SharedPreferences pref = context.getApplicationContext().getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE);
        String userid = pref.getString(Constants.KEY_ID, "0");
        cursor = db.getSuccessUnlockData();
        String version = SharedPreferencesStorage.readSharedPreference(ctx, Constants.PREFERENCES, Constants.VERSION_KEY);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("unlockEvents").child(version).child(userid);
        if (cursor.moveToFirst()) {
            do {
                String key = cursor.getString(cursor.getColumnIndex(SQLiteDB.COLUMN_ID));
                String unlockTime = cursor.getString(cursor.getColumnIndex(SQLiteDB.COLUMN_ON_UNLOCK));
                ref.child(exportTime).child(unlockTime).setValue(key);
            } while (cursor.moveToNext());
        }
    }

    /*reset lock screen trigger counter*/
    private void resetLockscreenShowCounter() {
        Log.e(TAG, "reset lockscreen counter");
        ctx.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE).edit().remove(Constants.LOCKSCREEN_SHOW_KEY).apply();
        Log.e(TAG, SharedPreferencesStorage.readSharedPreference(ctx, Constants.PREFERENCES, Constants.LOCKSCREEN_SHOW_KEY));
    }
}


