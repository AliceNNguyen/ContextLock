package com.example.alicenguyen.contextlock.jobservices;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.util.Log;

import com.example.alicenguyen.contextlock.Constants;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

/*Helper class to integrate ActivityRecognitionClient since PendingIntent is needed and normal background services
 * is deprecated since Android Oreo
 * Code snippet based by https://stackoverflow.com/questions/46675242/issue-moving-from-intentservice-to-jobintentservice-for-android-o*/

public class ActivityBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "BroadcastReceiver";
    public static final String EXTRA_SERVICE_CLASS = "extra_service_class";
    public static final String EXTRA_JOB_ID = "job_id";
    private ActivityRecognitionClient mActivityRecognitionClient;
    private PendingIntent mPendingIntent;

    /**
     * @param intent an Intent meant for a {@link android.support.v4.app.JobIntentService}
     * @return a new Intent intended for use by this receiver based off the passed intent
     */
    public static Intent getIntent(Context context, Intent intent, int job_id) {
        ComponentName component = intent.getComponent();
        if (component == null)
            throw new RuntimeException("Missing intent component");
        Intent new_intent = new Intent(intent)
                .putExtra(EXTRA_SERVICE_CLASS, component.getClassName())
                .putExtra(EXTRA_JOB_ID, job_id);

        new_intent.setClass(context, ActivityBroadcastReceiver.class);
        return new_intent;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (intent.getExtras() == null)
                throw new Exception("No extras found");
            // change intent's class to its intended service's class
            String service_class_name = intent.getStringExtra(EXTRA_SERVICE_CLASS);
            Log.e("broadcastreceiver", service_class_name);

            if (service_class_name == null)
                throw new Exception("No service class found in extras");

            Class service_class = Class.forName(service_class_name);

            if (!JobIntentService.class.isAssignableFrom(service_class))
                throw new Exception("Service class found is not a JobIntentService: " + service_class.getName());

            intent.setClass(context, service_class);
            // get job id
            if (!intent.getExtras().containsKey(EXTRA_JOB_ID))
                throw new Exception("No job ID found in extras");

            int job_id = intent.getIntExtra(EXTRA_JOB_ID, 0);

            Intent i =new Intent(context, ActivityBroadcastReceiver.class);
            mPendingIntent= PendingIntent.getBroadcast(context, 0, i, 0);
            mActivityRecognitionClient = new ActivityRecognitionClient(context);
            requestActivityUpdatesHandler();

            // start the service
            JobIntentService.enqueueWork(context, service_class, job_id, intent);
            Log.e("broadcastreceiver", "enqueue work");
        } catch (Exception e) {
            System.err.println("Error starting service from receiver: " + e.getMessage());
        }
    }

    /*request user activity updates with given pending intent*/
    public void requestActivityUpdatesHandler() {
        Task<Void> task = mActivityRecognitionClient.requestActivityUpdates(
                Constants.DETECTION_INTERVAL_IN_MILLISECONDS,
                mPendingIntent);

        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                Log.e(TAG, "sucess update activities");
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "failed update activities");
            }
        });
    }

    public void removeActivityUpdatesHandler() {
        Task<Void> task = mActivityRecognitionClient.removeActivityUpdates(
                mPendingIntent);
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                Log.e(TAG, "successful update activity");
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Failed to remove activity updates!");

            }
        });
    }

}

