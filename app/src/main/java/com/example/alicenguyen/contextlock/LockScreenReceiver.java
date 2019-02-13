package com.example.alicenguyen.contextlock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.DetectedActivity;

public class LockScreenReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        /*Sent when the user is present after
         * device wakes up (e.g when the keyguard is gone)
         * */

        if(intent.getAction().equals(Intent.ACTION_USER_PRESENT)){
            //Intent i = new Intent(context, ExperienceSamplingActivity.class);
            //context.startActivity(i);

        }
        /*Device is shutting down. This is broadcast when the device
         * is being shut down (completely turned off, not sleeping)
         * */
        else if (intent.getAction().equals(Intent.ACTION_SHUTDOWN)) {

        }
    }
}
