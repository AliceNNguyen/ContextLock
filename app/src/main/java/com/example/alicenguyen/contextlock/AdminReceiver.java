package com.example.alicenguyen.contextlock;

import android.app.KeyguardManager;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.PowerManager;
import android.os.UserHandle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.locks.Lock;

import static android.content.Context.MODE_PRIVATE;

public class AdminReceiver extends DeviceAdminReceiver {
    private static final String TAG = "AdminReceiver";
    private int fail_count = 0;
    private int lockscreenShowCounter;
    private boolean isScreenOn;
    private KeyguardManager myKM;
    //private Context ctx;
    private int success_count;

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.e(TAG, intent.getAction());
        myKM = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        //checkIsDeviceLocked(context);

        /*if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
            Log.e(TAG, "received alarm");
            int failCounter = Integer.parseInt(SharedPreferencesStorage.readSharedPreference(context, Constants.PREFERENCES, Constants.UNLOCK_FAILURE_COUNTER));
            String storedLockscreen = SharedPreferencesStorage.readSharedPreference(context, Constants.PREFERENCES, Constants.LOCKSCREEN_STORED_KEY);
            Log.e(TAG, storedLockscreen);
            Log.e(TAG, String.valueOf(failCounter));
            if(storedLockscreen.equals("true") && failCounter < 1) {
                Log.e(TAG, "lockscreen open");
                vibrate(context);
                setLockscreen(context);
            }
            context.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE).edit().remove(Constants.UNLOCK_FAILURE_COUNTER).apply();
        }*/
    }

    @Override
    public void onPasswordFailed(Context context, Intent intent) {
        Log.e(TAG, "onFailed");
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        fail_count = devicePolicyManager.getCurrentFailedPasswordAttempts();
        Log.e(TAG, String.valueOf(fail_count)); /**value of failed authentication**/
        SharedPreferencesStorage.writeSharedPreference(context, Constants.PREFERENCES, Constants.UNLOCK_FAILURE_COUNTER, String.valueOf(fail_count));


        /*if (no >= 3) {
            context.startActivity(new Intent(context, MyActivity.class));
        }*/
    }

    @Override
    public void onPasswordFailed(Context context, Intent intent, UserHandle userHandle) {
        Log.e(TAG, "onFailed");
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        fail_count = devicePolicyManager.getCurrentFailedPasswordAttempts();
        Log.e(TAG, String.valueOf(fail_count)); /**value of failed authentication**/
        SharedPreferencesStorage.writeSharedPreference(context, Constants.PREFERENCES, Constants.UNLOCK_FAILURE_COUNTER, String.valueOf(fail_count));


        /*if (no >= 3) {
            context.startActivity(new Intent(context, MyActivity.class));
        }*/
    }

    private void checkScreenState(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        isScreenOn = pm.isInteractive();
        Log.e(TAG, "screen state");
        Log.e(TAG, String.valueOf(isScreenOn));
    }

    private void checkIsDeviceLocked(Context context) {
        checkScreenState(context);
        if(!myKM.isDeviceLocked() && isScreenOn) { //device not locked and screen on
            Log.e(TAG, String.valueOf(myKM.isDeviceLocked()));
            Log.e(TAG, "screen on");
            //setLockscreen();
        }else if(!myKM.isDeviceLocked() && !isScreenOn) { //device is not locked and screen off
            Log.e(TAG, "is not device locked");
            Log.e(TAG, "screen is not on");
        }else if(myKM.isDeviceLocked() && !isScreenOn){ //device is locked and screen off
            Log.e(TAG, "device is locked and screen of");
            //setLockscreen();
        }else{
            Log.e(TAG, "device is locked");
            //setLockscreen();

        }
    }

    /*set lock screen if max number is not exceeded*/
    private void setLockscreen(Context context){
        lockscreenShowCounter = Integer.parseInt(SharedPreferencesStorage.readSharedPreference(context, Constants.PREFERENCES, Constants.LOCKSCREEN_SHOW_KEY));
        Log.e(TAG, String.valueOf(lockscreenShowCounter));
        Intent i = new Intent(context, Lockscreen.class);
        if(lockscreenShowCounter < Constants.LOCKSCREEN_SHOW_COUNTER) {
            //Intent i = new Intent(context, Lockscreen.class);
            context.startActivity(i);
            lockscreenShowCounter++;
            SharedPreferencesStorage.writeSharedPreference(context, Constants.PREFERENCES, Constants.LOCKSCREEN_SHOW_KEY, String.valueOf(lockscreenShowCounter));
            context.getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE).edit().remove(Constants.LOCKSCREEN_STORED_KEY).apply();
            //checkIsDeviceSecure();
        }
    }

    private void vibrate(Context context) {
        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            v.vibrate(100);
        }
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

        if(fail_count < 1) {
            //vibrate(context);
            //setLockscreen(context);
        }
    }
}
