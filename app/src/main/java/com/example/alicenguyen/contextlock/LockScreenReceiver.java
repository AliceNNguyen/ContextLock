package com.example.alicenguyen.contextlock;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import java.util.Calendar;
import java.util.Date;

public class LockScreenReceiver extends BroadcastReceiver {
    private static final String TAG ="LockScreenReceiver";
    private Context ctx;
    private String userid;
    private int unlockCounter;
    private KeyguardManager myKM;
    private boolean isScreenOn;
    private int lockscreenShowCounter;

    /*Remove notification when user unlocked the device
    * Retrieve unlock/lock events */
    @Override
    public void onReceive(Context context, Intent intent) {
        ctx = context.getApplicationContext();
        unlockCounter = Integer.parseInt(SharedPreferencesStorage.readSharedPreference(context, Constants.PREFERENCES, Constants.UNLOCK_COUNTER_KEY));

        myKM = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        Log.e(TAG, "action");
        Log.e(TAG, intent.getAction());
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF) && myKM.isDeviceLocked()) {
            Log.e(TAG, "received alarm");
            int failCounter = Integer.parseInt(SharedPreferencesStorage.readSharedPreference(context, Constants.PREFERENCES, Constants.UNLOCK_FAILURE_COUNTER));
            String storedLockscreen = SharedPreferencesStorage.readSharedPreference(context, Constants.PREFERENCES, Constants.LOCKSCREEN_STORED_KEY);
            Log.e(TAG, storedLockscreen);
            Log.e(TAG, String.valueOf(failCounter));
            if(storedLockscreen.equals("true") && failCounter < 1) {
                Log.e(TAG, "lockscreen open");
                //vibrate(context);
                setLockscreen(context);
            }
            context.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE).edit().remove(Constants.UNLOCK_FAILURE_COUNTER).apply();
        }
        //checkIsDeviceLocked();
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
            context.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE).edit().remove(Constants.LOCKSCREEN_STORED_KEY).apply();
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



    private void checkScreenState() {
        PowerManager pm = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
        isScreenOn = pm.isInteractive();
        Log.e(TAG, "screen state");
        Log.e(TAG, String.valueOf(isScreenOn));
    }

    private void checkIsDeviceLocked() {
        checkScreenState();
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
    private void setLockscreen(){
        lockscreenShowCounter = Integer.parseInt(SharedPreferencesStorage.readSharedPreference(ctx, Constants.PREFERENCES, Constants.LOCKSCREEN_SHOW_KEY));
        Log.e(TAG, String.valueOf(lockscreenShowCounter));
        if(lockscreenShowCounter < Constants.LOCKSCREEN_SHOW_COUNTER) {
            Intent i = new Intent(ctx, Lockscreen.class);
            ctx.startActivity(i);
            lockscreenShowCounter++;
            SharedPreferencesStorage.writeSharedPreference(ctx, Constants.PREFERENCES, Constants.LOCKSCREEN_SHOW_KEY, String.valueOf(lockscreenShowCounter));
            //checkIsDeviceSecure();
        }
    }


    private void writeUnlockEventsToDB(Context context) {
        LocalDatabase mDb = new LocalDatabase(context);
        Date currenttime = Calendar.getInstance().getTime();
        boolean isInserted = mDb.saveUnlockEventsToDB(currenttime.toString());
        if(isInserted == true) {
            Log.e(TAG, "insertedToDB");
        }else{
            Log.e(TAG, "failed to insert DB");
            //TODO send failed log to firebase
        }
    }

    private void openSurvey(Context context) {
        Intent intent = new Intent(context, ExperienceSamplingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /*reset local storage for notification after it is send*/
    private void resetSharedPreferences() {
        ctx.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE).edit().remove(Constants.NOTIFICATION_MESSAGE_KEY).apply();
        ctx.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE).edit().remove(Constants.NOTIFICATION_ICON_KEY).apply();
        ctx.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE).edit().remove(Constants.NOTIFICATION_STORE_KEY).apply();
    }

    /*if device is locked and user is present, send notification if there is one stored*/
    private void checkForNotification(Context context) {
        String message = SharedPreferencesStorage.readSharedPreference(context, Constants.PREFERENCES, Constants.NOTIFICATION_MESSAGE_KEY);
        String icon = SharedPreferencesStorage.readSharedPreference(context, Constants.PREFERENCES, Constants.NOTIFICATION_ICON_KEY);
        String notificationStored = SharedPreferencesStorage.readSharedPreference(context, Constants.PREFERENCES, Constants.NOTIFICATION_STORE_KEY);
        Log.e(TAG, "checkForNotification");
        Log.e(TAG, message);
        Log.e(TAG, icon);
        if(notificationStored.equals("true")) {
            NotificationHelper notificationHelper = new NotificationHelper(context);
            NotificationCompat.Builder nb = notificationHelper.getChannelNotification(Integer.parseInt(icon), message);
            notificationHelper.getManager().notify(Constants.NOTIFICATION_ID, nb.build());
            openSurvey(context);
            resetSharedPreferences();
        }
    }
}
