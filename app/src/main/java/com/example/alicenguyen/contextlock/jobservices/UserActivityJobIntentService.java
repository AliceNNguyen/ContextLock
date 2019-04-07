package com.example.alicenguyen.contextlock.jobservices;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.alicenguyen.contextlock.Constants;
import com.example.alicenguyen.contextlock.NotificationHelper;
import com.example.alicenguyen.contextlock.R;
import com.example.alicenguyen.contextlock.SharedPreferencesStorage;
import com.example.alicenguyen.contextlock.experience_sampling.ExperienceSamplingActivity;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class UserActivityJobIntentService extends JobIntentService {
    private static final String TAG = "UserActivityJobIntent";
    private static int JOB_ID = 1234;
    private ActivityRecognitionClient mActivityRecognitionClient;
    private String userActivity = "unknown";
    private boolean isLocked = false;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
    private int opensurveycounter;
    private FirebaseAnalytics mFirebaseAnalytics;
    private DatabaseReference mDatabaseReference;
    private String cooldown;
    private String userid;
    private Date current, switchDate;
    private String currentDate, switchVersionDate;
    private String message;
    private int icon;
    private int notificationSendCounter = 0;


    static void enqueueWork(Context context, Intent activity) {
        enqueueWork(context, UserActivityJobIntentService.class, JOB_ID, activity);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Log.e(TAG, "onhandlework");
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE);
        userid = pref.getString(Constants.KEY_ID, "no id");
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        Date date = Calendar.getInstance().getTime();
        currentDate = simpleDateFormat.format(date);
        switchVersionDate = SharedPreferencesStorage.readSharedPreference(this, Constants.PREFERENCES, Constants.SWITCH_VERSION_KEY);
        notificationSendCounter = Integer.parseInt(SharedPreferencesStorage.readSharedPreference(this, Constants.PREFERENCES, Constants.NOTIFICATION_SEND_KEY));
        registerActivityRecognitionClient(intent);
    }

    private void registerActivityRecognitionClient(Intent intent) {
        mActivityRecognitionClient = new ActivityRecognitionClient(this);
        if (ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

            // Get the list of the probable activities associated with the current state of the
            // device. Each activity is associated with a confidence level, which is an int between
            // 0 and 100.
            ArrayList<DetectedActivity> detectedActivities = (ArrayList) result.getProbableActivities();

            for (DetectedActivity activity : detectedActivities) {
                Log.e(TAG, "Detected activity: " + activity.getType() + ", " + activity.getConfidence());
                handleUserActivity(activity.getType(), activity.getConfidence());
            }
        }else {
            Log.e(TAG, "no activities recognized");
        }
    }


    private void handleUserActivity(int type, int confidence) {
        userActivity = getString(R.string.activity_unknown);
        switch (type) {
            case DetectedActivity.IN_VEHICLE: {
                userActivity = getString(R.string.activity_in_vehicle);
                break;
            }
            case DetectedActivity.ON_BICYCLE: {
                userActivity = getString(R.string.activity_on_bicycle);
                break;
            }
            case DetectedActivity.ON_FOOT: {
                userActivity = getString(R.string.activity_on_foot);
                break;
            }
            case DetectedActivity.RUNNING: {
                userActivity = getString(R.string.activity_running);
                break;
            }
            case DetectedActivity.STILL: {
                userActivity = getString(R.string.activity_still);
                break;
            }
            case DetectedActivity.TILTING: {
                userActivity = getString(R.string.activity_tilting);
                break;
            }
            case DetectedActivity.WALKING: {
                userActivity = getString(R.string.activity_walking);
                break;
            }
            case DetectedActivity.UNKNOWN: {
                userActivity = getString(R.string.activity_unknown);
                break;
            }
        }
        Log.d(TAG, "User activity: " + userActivity + ", Confidence: " + confidence);
        /*handle user activity if confidence is greater than defined value*/
        if (confidence > Constants.CONFIDENCE) {
            checkContextForNotification();
        }
    }

    private void writeLogEventsToDB() {
        LocalDatabase mDb = new LocalDatabase(this);
        Date currenttime = Calendar.getInstance().getTime();
        boolean isInserted = mDb.saveToDB(userid, currenttime.toString(), userActivity, "", String.valueOf(isLocked));
        if(isInserted == true) {
            Log.e(TAG, "insertedToDB");
        }else{
            Log.e(TAG, "failed to insert DB");
        }
    }

    private void openSurvey() {
        Intent intent = new Intent(this, ExperienceSamplingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    /*notification with given reason*/
    private void setContextNotification() {
        notificationSendCounter++;
        Log.e(TAG, String.valueOf(notificationSendCounter));
        SharedPreferencesStorage.writeSharedPreference(this, Constants.PREFERENCES, Constants.NOTIFICATION_SEND_KEY, String.valueOf(notificationSendCounter));
        NotificationHelper notificationHelper = new NotificationHelper(this);
        NotificationCompat.Builder nb = notificationHelper.getChannelNotification(icon, message);
        notificationHelper.getManager().notify(Constants.NOTIFICATION_ID, nb.build());
        Log.e(TAG, "send");
    }

    /*short notification without given reason*/
    private void setNonContextNotification() {
        Log.e(TAG, "send");
        notificationSendCounter++;
        Log.e(TAG, String.valueOf(notificationSendCounter));
        SharedPreferencesStorage.writeSharedPreference(this, Constants.PREFERENCES, Constants.NOTIFICATION_SEND_KEY, String.valueOf(notificationSendCounter));
        NotificationHelper notificationHelper = new NotificationHelper(this);
        NotificationCompat.Builder nb = notificationHelper.getChannelNotification(R.mipmap.fingerprint_ic, "");
        notificationHelper.getManager().notify(Constants.NOTIFICATION_ID, nb.build());

    }

    /*handle notification version based on user id*/
    private void setNotificationVersion() {
        int id = Integer.parseInt(userid);
        try {
            current= simpleDateFormat.parse(currentDate);
            switchDate = simpleDateFormat.parse(switchVersionDate);

            Log.e(TAG, current.toString());
            Log.e(TAG, switchDate.toString());

            if(current.before(switchDate)){
                Log.e(TAG, current.toString());
                Log.e(TAG, switchDate.toString());
                if ((id % 2) == 0) {
                    // number is even
                    Log.e(TAG, "non condition notification");
                    SharedPreferencesStorage.writeSharedPreference(this, Constants.PREFERENCES, Constants.VERSION_KEY, Constants.VERSION_A );
                    setNonContextNotification();
                }
                else {
                    // number is odd
                    Log.e(TAG, "condition notification");
                    Log.e(TAG, message);
                    SharedPreferencesStorage.writeSharedPreference(this, Constants.PREFERENCES, Constants.VERSION_KEY, Constants.VERSION_B );
                    setContextNotification();
                }
            }
            if(current.equals(switchDate) || current.after(switchDate)) {
                if ((id % 2) == 0) {
                    // number is even
                    SharedPreferencesStorage.writeSharedPreference(this, Constants.PREFERENCES, Constants.VERSION_KEY, Constants.VERSION_B );
                    setContextNotification();
                }else {
                    // number is odd
                    SharedPreferencesStorage.writeSharedPreference(this, Constants.PREFERENCES, Constants.VERSION_KEY, Constants.VERSION_A );
                    setNonContextNotification();
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Log.e(TAG, "error parse date");
        }
        openSurvey();
    }

    /*set values to context parameters*/
    private void checkContextForNotification() {
        Log.e(TAG, userActivity);
        Log.e(TAG, "send notification counter:" + notificationSendCounter);
        checkIfScreenLocked();
        String storedWeather = SharedPreferencesStorage.readSharedPreference(this, Constants.PREFERENCES, Constants.WEATHER_KEY);
        Log.e(TAG, "storedWeather");
        Log.e(TAG, storedWeather);
        if(isLocked && notificationSendCounter < Constants.NOTIFICATION_SEND_MAX_NUMBER) {
            if (userActivity.equals(getString(R.string.activity_running))) {
                message = getString(R.string.running);
                icon = R.mipmap.running_ic;
                setNotificationVersion();
            } else if (userActivity.equals(getString(R.string.activity_in_vehicle))) { //getString(R.string.activity_in_vehicle)
                message = getString(R.string.in_vehicle);
                icon = R.mipmap.publictransport_ic;
                setNotificationVersion();
            } else if(userActivity.equals(getString(R.string.activity_walking )) && storedWeather.contains("Rain")) {
                Log.e(TAG, "walk and rain");
                message = getString(R.string.rain);
                icon = R.mipmap.raindrop_ic;
                setNotificationVersion();
            } else if(userActivity.equals(getString(R.string.activity_walking)) && storedWeather.contains("Snow")) {
                message = getString(R.string.snow);
                icon = R.mipmap.snow_ic;
                setNotificationVersion();
            } else{
                message = getString(R.string.no_condition_message) + ": " + userActivity;
                Log.e(TAG, "no conditions met");
            }
        }
        writeLogEventsToDB();
    }


    /*check if screen is locked to send notification*/
    private void checkIfScreenLocked() {
        KeyguardManager myKM = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onStopCurrentWork() {
        return super.onStopCurrentWork();
    }
}
