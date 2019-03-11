package com.example.alicenguyen.contextlock;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class NotificationHelper extends ContextWrapper {
    private static final String TAG = "NotificationHelper";
    public static final String channelID = "channelID";
    public static final String channelName = "Channel Name";
    private NotificationManager mManager;


    public NotificationHelper(Context base) {
        super(base);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
        }
    }

    /*creating notification channel for Android Oreo or greater*/
    @TargetApi(Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationChannel channel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH);
        getManager().createNotificationChannel(channel);
    }

    public NotificationManager getManager() {
        if (mManager == null) {
            mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mManager;
    }

    /*public method to cancel notification*/
    public static void cancelNotification(Context context, int notifyId) {
        Log.e(TAG, "cancel Notification");
        NotificationManager mManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mManager.cancel(notifyId);
    }

    /*public method to create notification with given icon and message*/
    public NotificationCompat.Builder getChannelNotification(int icon, String message) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                icon);
        return new NotificationCompat.Builder(getApplicationContext(), channelID)
                .setSmallIcon(R.drawable.ic_fingerprint)
                .setLargeIcon(bitmap)
                .setContentTitle(getString(R.string.notification_title) + " " + message)
                .setContentText(getString(R.string.default_message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(getString(R.string.default_message)))
                // Set the intent that will fire when the user taps the notification
                //.setContentIntent(pendingIntent)
                //.setTimeoutAfter(Constants.NOTIFICATION_TIMEOUT)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setAutoCancel(true);
    }
}
