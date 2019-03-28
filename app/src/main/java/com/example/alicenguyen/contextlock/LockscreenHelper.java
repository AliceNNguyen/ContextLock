package com.example.alicenguyen.contextlock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

//set value to trigger lock screen
public class LockscreenHelper extends BroadcastReceiver {
    private static final String TAG ="LockscreenHelper";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "onReceive alarm");
        SharedPreferencesStorage.writeSharedPreference(context, Constants.PREFERENCES, Constants.LOCKSCREEN_STORED_KEY, "true");
    }
}
