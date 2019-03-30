package com.example.alicenguyen.contextlock;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import java.util.Calendar;
import java.util.Date;

/*LockScreenReceiver contains lock screen triggering logic:
checks each time the screen turns off if a lock screen in stored to trigger*/
public class LockScreenReceiver extends BroadcastReceiver {
    private static final String TAG ="LockScreenReceiver";
    private Context ctx;
    private int unlockCounter;
    private KeyguardManager myKM;
    private int lockscreenShowCounter;

    /*Retrieve unlock/lock events */
    @Override
    public void onReceive(Context context, Intent intent) {
        ctx = context.getApplicationContext();
        unlockCounter = Integer.parseInt(SharedPreferencesStorage.readSharedPreference(context, Constants.PREFERENCES, Constants.UNLOCK_COUNTER_KEY));
        myKM = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        Log.e(TAG, "action");
        Log.e(TAG, intent.getAction());
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) { //intent.getAction().equals(Intent.ACTION_SCREEN_OFF) && myKM.isDeviceLocked()
            Log.e(TAG, "received alarm");
            int failCounter = Integer.parseInt(SharedPreferencesStorage.readSharedPreference(context, Constants.PREFERENCES, Constants.UNLOCK_FAILURE_COUNTER));
            String storedLockscreen = SharedPreferencesStorage.readSharedPreference(context, Constants.PREFERENCES, Constants.LOCKSCREEN_STORED_KEY);
            String storedPasswordUsed = SharedPreferencesStorage.readSharedPreference(context, Constants.PREFERENCES, Constants.PASSWORD_USED_KEY);
            Log.e(TAG, storedLockscreen);
            Log.e(TAG, String.valueOf(failCounter));
            Log.e(TAG, storedPasswordUsed);
            if((storedLockscreen.equals("true") && failCounter < 2) || (storedLockscreen.equals("true ") && !storedPasswordUsed.equals("true"))) {
                Log.e(TAG, "lockscreen open");
                setLockscreen(context);
            }
            context.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE).edit().remove(Constants.UNLOCK_FAILURE_COUNTER).apply();
            context.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE).edit().remove(Constants.PASSWORD_USED_KEY).apply();
        }
        if(!myKM.isDeviceLocked() && intent.getAction().equals(Intent.ACTION_USER_PRESENT)){
            writeUnlockEventsToDB(context);
        }
    }

    /*set lock screen if max number is not exceeded*/
    private void setLockscreen(Context context){
        lockscreenShowCounter = Integer.parseInt(SharedPreferencesStorage.readSharedPreference(context, Constants.PREFERENCES, Constants.LOCKSCREEN_SHOW_KEY));
        Log.e(TAG, String.valueOf(lockscreenShowCounter));
        if(lockscreenShowCounter < Constants.LOCKSCREEN_SHOW_COUNTER) {
            //Intent i = new Intent(context, Lockscreen.class);
            String fallback = SharedPreferencesStorage.readSharedPreference(context,Constants.PREFERENCES, Constants.UNLOCK_METHOD_KEY);
            if(fallback.equals("PIN")) {
                Intent i = new Intent(context, Lockscreen.class);
                context.startActivity(i);
            }else if(fallback.equals("Pattern")) {
                Intent i = new Intent(context, PatternLockScreen.class);
                context.startActivity(i);
            }
            lockscreenShowCounter++;
            SharedPreferencesStorage.writeSharedPreference(context, Constants.PREFERENCES, Constants.LOCKSCREEN_SHOW_KEY, String.valueOf(lockscreenShowCounter));
            context.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE).edit().remove(Constants.LOCKSCREEN_STORED_KEY).apply();
        }
    }

    private void writeUnlockEventsToDB(Context context) {
        SQLiteDB mDb = new SQLiteDB(context);
        Date currenttime = Calendar.getInstance().getTime();
        boolean isInserted = mDb.saveSUnlockSuccessEventsToDB(currenttime.toString());
        if(isInserted == true) {
            Log.e(TAG, "insertedToDB");
        }else{
            Log.e(TAG, "failed to insert DB");
        }
    }

    /*reset local storage for notification after it is send*/
    private void resetSharedPreferences() {
        ctx.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE).edit().remove(Constants.NOTIFICATION_MESSAGE_KEY).apply();
        ctx.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE).edit().remove(Constants.NOTIFICATION_ICON_KEY).apply();
        ctx.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE).edit().remove(Constants.NOTIFICATION_STORE_KEY).apply();
    }
}
