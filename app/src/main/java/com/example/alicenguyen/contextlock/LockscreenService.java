package com.example.alicenguyen.contextlock;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;


/*starts foreground service to track user's lock screen*/
public class LockscreenService extends Service {
    private static final String TAG = "LockscreenService";
    public static final String CHANNEL_ID = "channelID";
    private LockScreenReceiver lockScreenReceiver;
    private AdminReceiver adminReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        registerLockscreenReceiver();
        //registerAdmin();
    }

    private void registerAdmin() {
        DevicePolicyManager mgr = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName cn = new ComponentName(this, AdminReceiver.class);
        if ( !mgr.isAdminActive(cn)) {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, cn);
            //intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, getString(R.string.admin_explanation));
            //startActivity(intent);
            //startActivityForResult(intent, RESULT_OK);
        }else {
            Log.e(TAG, "admin is active");
        }
    }

    private void registerLockscreenReceiver() {
        lockScreenReceiver = new LockScreenReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(lockScreenReceiver, filter);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String input = intent.getStringExtra("inputExtra");

        Intent notificationIntent = new Intent(this, SetupActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "LockScreen Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }


        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Study is running")
                .setContentText(input)
                .setSmallIcon(R.mipmap.ic_fingerprint_white)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);
        return START_NOT_STICKY;
    }

    private void unregisterLockscreenReceiver() {
        try {
            unregisterReceiver(lockScreenReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterLockscreenReceiver();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
