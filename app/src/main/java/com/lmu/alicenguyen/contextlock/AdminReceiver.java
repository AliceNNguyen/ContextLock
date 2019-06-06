package com.lmu.alicenguyen.contextlock;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.UserHandle;
import android.util.Log;
import java.util.Calendar;
import java.util.Date;

import static android.content.Context.MODE_PRIVATE;

public class AdminReceiver extends DeviceAdminReceiver {
    private static final String TAG = "AdminReceiver";
    private int fail_count = 0;


    /*get current lock screen failed attempts for Android Version lower 27*/
    @Override
    public void onPasswordFailed(Context context, Intent intent) {
        Log.e(TAG, "onFailed");
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        if(devicePolicyManager != null) {
            fail_count = devicePolicyManager.getCurrentFailedPasswordAttempts();
            Log.e(TAG, String.valueOf(fail_count));
        }
        SharedPreferencesStorage.writeSharedPreference(context, Constants.PREFERENCES, Constants.UNLOCK_FAILURE_COUNTER, String.valueOf(fail_count));
    }

    /*get current lock screen password failed attempts for Android version higher 26*/
    @Override
    public void onPasswordFailed(Context context, Intent intent, UserHandle userHandle) {
        Log.e(TAG, "onFailed");
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        if(devicePolicyManager != null) {
            fail_count = devicePolicyManager.getCurrentFailedPasswordAttempts();
            Log.e(TAG, String.valueOf(fail_count));
        }
        SharedPreferencesStorage.writeSharedPreference(context, Constants.PREFERENCES, Constants.UNLOCK_FAILURE_COUNTER, String.valueOf(fail_count));
    }

    /*write number of failed system screen lock to local database*/
    private void writeUnlockEventsToDB (Context context) {
        SQLiteDB mDb = new SQLiteDB(context);
        Date currenttime = Calendar.getInstance().getTime();
        SharedPreferences pref = context.getApplicationContext().getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE);
        String userid = pref.getString(Constants.KEY_ID, "0");
        String failedCounter = SharedPreferencesStorage.readSharedPreference(context, Constants.PREFERENCES, Constants.UNLOCK_FAILURE_COUNTER);

        boolean isInserted = mDb.saveUnlockEventsToDB(userid, failedCounter, currenttime.toString());
        if(isInserted) {
            Log.e(TAG, "insertedToDB");
            context.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE).edit().remove(Constants.UNLOCK_FAILURE_COUNTER).apply();
        }else{
            Log.e(TAG, "failed to insert DB");
        }
    }

    @Override
    public void onPasswordSucceeded(Context context, Intent intent) {
        Log.e(TAG, "success");
        writeUnlockEventsToDB(context);
    }

    @Override
    public void onPasswordSucceeded(Context context, Intent intent, UserHandle userHandle) {
        Log.e(TAG, "success");
        SharedPreferencesStorage.writeSharedPreference(context, Constants.PREFERENCES, Constants.PASSWORD_USED_KEY, "true");
        writeUnlockEventsToDB(context);
    }
}
