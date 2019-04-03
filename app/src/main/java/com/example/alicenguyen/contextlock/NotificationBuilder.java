package com.example.alicenguyen.contextlock;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

public class NotificationBuilder {
    private static final int NOTIFICATION_ID = 1094;
    public static final String CHANNEL_ID = "channelID";


    private static Notification notification;

    public static Notification getNotification(Context context) {

        if(notification == null) {
            Intent notificationIntent = new Intent(context, SetupActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context,
                    0, notificationIntent, 0);


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel serviceChannel = new NotificationChannel(
                        CHANNEL_ID,
                        "LockScreen Service Channel",
                        NotificationManager.IMPORTANCE_DEFAULT
                );
                NotificationManager manager = context.getSystemService(NotificationManager.class);
                manager.createNotificationChannel(serviceChannel);
            }

            notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setContentTitle("Study is running")
                    //.setContentText(input)
                    .setSmallIcon(R.mipmap.ic_fingerprint_white)
                    .setContentIntent(pendingIntent)
                    .build();
        }

        return notification;
    }

    public static int getNotificationId() {
        return NOTIFICATION_ID;
    }
}
