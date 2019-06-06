package com.lmu.alicenguyen.contextlock;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;


/*starts foreground service to track user's lock screen*/
public class LockScreenService extends Service {
    public static final String CHANNEL_ID = "channelID";
    private LockScreenReceiver lockScreenReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        registerLockscreenReceiver();
    }

    private void registerLockscreenReceiver() {
        lockScreenReceiver = new LockScreenReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(lockScreenReceiver, filter);
    }


    /*create permanent notification for foreground service*/
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "LockScreen Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if(manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
        startForeground(NotificationBuilder.getNotificationId(), NotificationBuilder.getNotification(this));
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
