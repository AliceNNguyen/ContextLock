package com.lmu.alicenguyen.contextlock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

//set value to trigger lock screen
public class LockScreenHelper extends BroadcastReceiver {
    private static final String TAG ="LockScreenHelper";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "onReceive alarm");
        SharedPreferencesStorage.writeSharedPreference(context, Constants.PREFERENCES, Constants.LOCKSCREEN_STORED_KEY, "true");
    }
}
