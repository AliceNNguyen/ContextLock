package com.example.alicenguyen.contextlock;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;

import org.w3c.dom.Text;

import java.util.Calendar;
import java.util.Date;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "AlarmReceiver";
    private boolean isLocked;
    private Context context;
    private String userid;
    private Date currenttime;
    private FirebaseAnalytics mFirebaseAnalytics;


    @Override
    public void onReceive(Context ctx, Intent intent) {
        //open experience sampling 3 TODO
        Log.e(TAG, "onReceive");

        context = ctx.getApplicationContext();
        createNotificationChannel();
        SharedPreferences pref = context.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE);
        userid = pref.getString(Constants.KEY_ID, "no id");
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
        mFirebaseAnalytics.setUserId(userid);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);

        checkIfScreenLocked();


        if(isLocked) {
            sendNotification();
            openSurvey();
        }

    }

    private void openSurvey() {
        Intent intent = new Intent(context, ExperienceSamplingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }



    private void createNotificationChannel() {

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name =  "notification_channel";
            String description = "use pin";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(Constants.CHANNEL_ID_DEFAULT, name, importance);
            channel.setDescription(description);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void sendNotification() {
        Log.e("AlarmReceiver", "send");

        int notificationId = 1;
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),
                R.mipmap.humidity_ic);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, Constants.CHANNEL_ID_DEFAULT)
                .setSmallIcon(R.drawable.ic_fingerprint)
                .setLargeIcon(bitmap)
                .setContentTitle("Use PIN/Pattern")
                .setContentText("Humidity recognized! Your fingerprint might not working")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Humidity recognized! Your fingerprint might not working"))
                // Set the intent that will fire when the user taps the notification
                //.setContentIntent(pendingIntent)
                .setAutoCancel(true);


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(notificationId, mBuilder.build());

        currenttime = Calendar.getInstance().getTime();
        Bundle params = new Bundle();
        params.putString("send", "true" );
        params.putString("timestamp", currenttime.toString());
        mFirebaseAnalytics.logEvent("default_notification_event", params);
    }


    private void checkIfScreenLocked() {
        KeyguardManager myKM = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        if( myKM.isKeyguardLocked()) {
            isLocked = true;
            Log.e(TAG, "device is locked");
            //it is locked
        } else {
            isLocked = false;
            Log.e(TAG, "device not locked");
            //it is not locked
        }
    }


}
