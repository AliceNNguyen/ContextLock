package com.example.alicenguyen.contextlock;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.DetectedActivity;

import java.util.Calendar;
import java.util.Date;

import static android.content.Context.MODE_PRIVATE;

public class LockScreenReceiver extends BroadcastReceiver {
    private static final String TAG ="LockScreenReceiver";
    private Context ctx;
    private String userid;
    private int unlockCounter = 0;

    /*remove notification when user unlocked the device*/
    @Override
    public void onReceive(Context context, Intent intent) {
        ctx = context.getApplicationContext();
        unlockCounter = Integer.parseInt(SharedPreferencesStorage.readSharedPreference(context, Constants.PREFERENCES, Constants.UNLOCK_COUNTER_KEY));


        KeyguardManager myKM = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        if( myKM.isKeyguardLocked()) {
            Log.e(TAG, "device is locked");
            checkForNotification(context);
            //it is locked
        } else {
            Log.e(TAG, "device not locked");
            Log.e(TAG, String.valueOf(unlockCounter));
            //it is not locked
            SharedPreferencesStorage.writeSharedPreference(context, Constants.PREFERENCES, Constants.UNLOCK_COUNTER_KEY, String.valueOf(unlockCounter));
            NotificationHelper.cancelNotification(context, Constants.NOTIFICATION_ID);
        }
    }

    private void openSurvey(Context context) {
        Intent intent = new Intent(context, ExperienceSamplingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private void resetSharedPreferences() {
        ctx.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE).edit().remove(Constants.NOTIFICATION_MESSAGE_KEY).apply();
        ctx.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE).edit().remove(Constants.NOTIFICATION_ICON_KEY).apply();
        ctx.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE).edit().remove(Constants.NOTIFICATION_STORE_KEY).apply();

    }

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
