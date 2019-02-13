package com.example.alicenguyen.contextlock;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;

public class UserActivityJobIntentService extends JobIntentService {
    private static final String TAG = "UserActivityJobIntent";
    private static int JOB_ID = 12345;

    private ActivityRecognitionClient mActivityRecognitionClient;
    private String userActivity = "unknown";
    private boolean isLocked = false;

    static void enqueueWork(Context context, Intent activity) {
        enqueueWork(context, UserActivityJobIntentService.class, JOB_ID, activity);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {

        createNotificationChannel();
        mActivityRecognitionClient = new ActivityRecognitionClient(this);
        if (ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

            // Get the list of the probable activities associated with the current state of the
            // device. Each activity is associated with a confidence level, which is an int between
            // 0 and 100.
            ArrayList<DetectedActivity> detectedActivities = (ArrayList) result.getProbableActivities();

            for (DetectedActivity activity : detectedActivities) {
                Log.e(TAG, "Detected activity: " + activity.getType() + ", " + activity.getConfidence());
                //broadcastActivity(activity);
                handleUserActivity(activity.getType(), activity.getConfidence());

            }
        }else {
            Log.e(TAG, "no activities recognized");
        }

    }


    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(Constants.CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void sendNotification(String contextDesscription, int icon) {
        Log.e("notfication", "send");

        int notificationId = 1;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                icon);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, Constants.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_fingerprint)
                .setLargeIcon(bitmap)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(contextDesscription +  " " + getString(R.string.notification_description))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(contextDesscription + " " +  getString(R.string.notification_description)))
                //.setStyle(new NotificationCompat.BigPictureStyle()
                //.bigPicture(bitmap).setSummaryText("message"))
                // Set the intent that will fire when the user taps the notification
                //.setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(notificationId, mBuilder.build());
    }

    private void handleUserActivity(int type, int confidence) {
        userActivity = getString(R.string.activity_unknown);
        //int icon = R.drawable.ic_still;
        switch (type) {
            case DetectedActivity.IN_VEHICLE: {
                userActivity = getString(R.string.activity_in_vehicle);
                //icon = R.drawable.ic_driving;
                break;
            }
            case DetectedActivity.ON_BICYCLE: {
                userActivity = getString(R.string.activity_on_bicycle);
                //icon = R.drawable.ic_on_bicycle;
                break;
            }
            case DetectedActivity.ON_FOOT: {
                userActivity = getString(R.string.activity_on_foot);
                //icon = R.drawable.ic_walking;
                break;
            }
            case DetectedActivity.RUNNING: {
                userActivity = getString(R.string.activity_running);
                //icon = R.drawable.ic_running;
                break;
            }
            case DetectedActivity.STILL: {
                userActivity = getString(R.string.activity_still);
                break;
            }
            case DetectedActivity.TILTING: {
                userActivity = getString(R.string.activity_tilting);
                //icon = R.drawable.ic_tilting;
                break;
            }
            case DetectedActivity.WALKING: {
                userActivity = getString(R.string.activity_walking);
                //icon = R.drawable.ic_walking;
                break;
            }
            case DetectedActivity.UNKNOWN: {
                userActivity = getString(R.string.activity_unknown);
                break;
            }
        }

        Log.d(TAG, "User activity: " + userActivity + ", Confidence: " + confidence);
        //Bundle bundle = new Bundle();
        //bundle.putString("user_activity", String.valueOf(userActivity));
        //mFirebaseAnalytics.logEvent("user_activity", bundle);

        if (confidence > Constants.CONFIDENCE) {
            //txtActivity.setText(label);
            //txtConfidence.setText("Confidence: " + confidence);
            //imgActivity.setImageResource(icon);

            //Toast.makeText(this, userActivity, Toast.LENGTH_LONG).show();
            checkContextForNotification();
            //setContextIcon(mainWeather, temperature.doubleValue(), humidity, relative_humidity, ambient_temperature, userActivity);
        }
    }

    private void checkContextForNotification() {
        Log.e(TAG, userActivity);
        checkIfScreenLocked();
        if(isLocked) {
            if (userActivity.equals("running")) {
                sendNotification(getString(R.string.running), R.mipmap.running_ic);
            } else if (userActivity.equals("in_vehicle")) {
                sendNotification(getString(R.string.in_vehicle), R.mipmap.publictransport_ic);
            } else {
                Log.e(TAG, "no conditions met");
                //Toast.makeText(this, "Alles gut!", Toast.LENGTH_LONG).show();
            }
            Intent intent = new Intent(this, ExperienceSamplingActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    private void checkIfScreenLocked() {
        KeyguardManager myKM = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        if( myKM.inKeyguardRestrictedInputMode()) {
            isLocked = true;
            Log.e(TAG, "device is locked");
            //it is locked
        } else {
            isLocked = false;
            Log.e(TAG, "device not locked");
            //it is not locked
        }
    }




    private void broadcastActivity(DetectedActivity activity) {
        Intent intent = new Intent(Constants.BROADCAST_DETECTED_ACTIVITY);
        intent.putExtra("type", activity.getType());
        intent.putExtra("confidence", activity.getConfidence());
        Log.e("action", String.valueOf(activity.getType()));
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onStopCurrentWork() {
        return super.onStopCurrentWork();
    }
}
