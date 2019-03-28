package com.example.alicenguyen.contextlock;

import android.app.KeyguardManager;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
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

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.e(TAG, "onReceive");
        Log.e(TAG, intent.getAction());
    }

    @Override
    public void onPasswordFailed(Context context, Intent intent) {
        Log.e(TAG, "onFailed");
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        fail_count = devicePolicyManager.getCurrentFailedPasswordAttempts();
        Log.e(TAG, String.valueOf(fail_count)); /**value of failed authentication**/
        SharedPreferencesStorage.writeSharedPreference(context, Constants.PREFERENCES, Constants.UNLOCK_FAILURE_COUNTER, String.valueOf(fail_count));
    }

    @Override
    public void onPasswordFailed(Context context, Intent intent, UserHandle userHandle) {
        Log.e(TAG, "onFailed");
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        fail_count = devicePolicyManager.getCurrentFailedPasswordAttempts();
        Log.e(TAG, String.valueOf(fail_count)); /**value of failed authentication**/
        SharedPreferencesStorage.writeSharedPreference(context, Constants.PREFERENCES, Constants.UNLOCK_FAILURE_COUNTER, String.valueOf(fail_count));
    }

    private void writeUnlockEventsToDB (Context context) {
        SQLiteDB mDb = new SQLiteDB(context);
        Date currenttime = Calendar.getInstance().getTime();
        SharedPreferences pref = context.getApplicationContext().getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE);
        String userid = pref.getString(Constants.KEY_ID, "0");
        String failedCounter = SharedPreferencesStorage.readSharedPreference(context, Constants.PREFERENCES, Constants.UNLOCK_FAILURE_COUNTER);

        boolean isInserted = mDb.saveUnlockEventsToDB(userid, failedCounter, currenttime.toString());
        if(isInserted == true) {
            Log.e(TAG, "insertedToDB");
            context.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE).edit().remove(Constants.UNLOCK_FAILURE_COUNTER).apply();
        }else{
            Log.e(TAG, "failed to insert DB");
            //TODO send failed log to firebase
        }
    }


    @Override
    public void onPasswordSucceeded(Context context, Intent intent) {
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        Log.e(TAG, "success");
        Log.e(TAG, String.valueOf(fail_count));
        writeUnlockEventsToDB(context);

        if(fail_count < 1) {
            //vibrate(context);
            //setLockscreen(context);
        }
    }

    @Override
    public void onPasswordSucceeded(Context context, Intent intent, UserHandle userHandle) {
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        Log.e(TAG, "success");
        Log.e(TAG, String.valueOf(fail_count));
        writeUnlockEventsToDB(context);
    }
}
