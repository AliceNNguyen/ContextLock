package com.example.alicenguyen.contextlock;

import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "AlarmReceiver";
    private boolean isLocked;
    private Context context;
    private String userid, message;
    private Date currenttime, current, switchDate;
    private SharedPreferencesStorage sharedPreferencesStorage;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
    private String currentDate, switchVersionDate;
    private String cooldown, notificationmessage;
    private int opensurveycounter, icon;
    private int notificationSendCounter = 0;



    @Override
    public void onReceive(Context ctx, Intent intent) {
        //open experience sampling 3 TODO
        Log.e(TAG, "onReceive");

        context = ctx;
        SharedPreferences pref = context.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE);
        userid = pref.getString(Constants.KEY_ID, "no id");


        notificationSendCounter = Integer.parseInt(SharedPreferencesStorage.readSharedPreference(context, Constants.PREFERENCES, Constants.NOTIFICATION_SEND_KEY));

        Date date = Calendar.getInstance().getTime();
        currentDate =  simpleDateFormat.format(date);
        switchVersionDate = SharedPreferencesStorage.readSharedPreference(ctx, Constants.PREFERENCES, Constants.SWITCH_VERSION_KEY);

        Log.e(TAG, currentDate);
        //Log.e(TAG, storedDate);
        Log.e(TAG, switchVersionDate);
        checkIfScreenLocked();

        Log.e(TAG, "send counter: " + notificationSendCounter);
        if(notificationSendCounter < Constants.NOTIFICATION_SEND_MAX_NUMBER) {
            //sendNotification();
            int id = Integer.parseInt(userid);
            Log.e(TAG, "ABC");
            Log.e(TAG, String.valueOf(id));
            Log.e(TAG, "send counter: " + notificationSendCounter);

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

                        if(isLocked) {
                            setNonContextNotification();
                            openSurvey();
                        }else {
                            bufferNotification(" ", R.mipmap.fingerprint_ic);
                            updateSendCounter();
                        }
                        SharedPreferencesStorage.writeSharedPreference(context, Constants.PREFERENCES, Constants.VERSION_KEY, Constants.VERSION_A );
                    }
                    else {
                        // number is odd
                        Log.e(TAG, "condition notification");
                        SharedPreferencesStorage.writeSharedPreference(context, Constants.PREFERENCES, Constants.VERSION_KEY, Constants.VERSION_B);
                        if(isLocked){
                            setContextNotification();
                            openSurvey();

                        }else {
                            setNotificationMessage();
                            bufferNotification(message, icon);
                            updateSendCounter();
                        }

                    }
                }
                if(current.equals(switchDate) || current.after(switchDate)) {

                    if ((id % 2) == 0) {
                        // number is even
                        SharedPreferencesStorage.writeSharedPreference(context, Constants.PREFERENCES, Constants.VERSION_KEY, Constants.VERSION_B );
                        if(isLocked) {
                            setContextNotification();
                            openSurvey();

                        }else {
                            setNotificationMessage();
                            bufferNotification(message, icon);
                            updateSendCounter();
                        }

                    }
                    else {
                        // number is odd
                        SharedPreferencesStorage.writeSharedPreference(context, Constants.PREFERENCES, Constants.VERSION_KEY, Constants.VERSION_A );
                        if(isLocked) {
                            setNonContextNotification();
                            openSurvey();
                        }else {
                            bufferNotification(" ", R.mipmap.fingerprint_ic);
                            updateSendCounter();
                        }
                    }
                }

            } catch (ParseException e) {
                e.printStackTrace();
                Log.e(TAG, "error parse date");
            }
            //openExperienceSampling();
            //openSurvey();
        }else {
            Log.e(TAG, "counter max reached");
        }

    }

    private void updateSendCounter() {
        notificationSendCounter++;
        SharedPreferencesStorage.writeSharedPreference(context, Constants.PREFERENCES, Constants.NOTIFICATION_SEND_KEY, String.valueOf(notificationSendCounter));
    }

    private void bufferNotification(String message, int icon) {
        Log.e(TAG, "bufferNotification");
        Log.e(TAG, String.valueOf(icon));
        SharedPreferencesStorage.writeSharedPreference(context, Constants.PREFERENCES, Constants.NOTIFICATION_STORE_KEY, "true");
        SharedPreferencesStorage.writeSharedPreference(context, Constants.PREFERENCES, Constants.NOTIFICATION_MESSAGE_KEY, message);
        SharedPreferencesStorage.writeSharedPreference(context, Constants.PREFERENCES, Constants.NOTIFICATION_ICON_KEY, String.valueOf(icon));

    }

    private void setContextNotification() {
        Log.e(TAG, "send");
        setNotificationMessage();
        notificationSendCounter++;
        Log.e(TAG, String.valueOf(notificationSendCounter));
        SharedPreferencesStorage.writeSharedPreference(context, Constants.PREFERENCES, Constants.NOTIFICATION_SEND_KEY, String.valueOf(notificationSendCounter));
        NotificationHelper notificationHelper = new NotificationHelper(context);
        NotificationCompat.Builder nb = notificationHelper.getChannelNotification(icon, message);
        notificationHelper.getManager().notify(Constants.NOTIFICATION_ID, nb.build());
    }


    private void setNonContextNotification() {
        Log.e(TAG, "send");
        notificationSendCounter++;
        Log.e(TAG, String.valueOf(notificationSendCounter));
        icon = R.mipmap.fingerprint_ic;
        SharedPreferencesStorage.writeSharedPreference(context, Constants.PREFERENCES, Constants.NOTIFICATION_SEND_KEY, String.valueOf(notificationSendCounter));
        NotificationHelper notificationHelper = new NotificationHelper(context);
        NotificationCompat.Builder nb = notificationHelper.getChannelNotification(icon, "");
        notificationHelper.getManager().notify(Constants.NOTIFICATION_ID, nb.build());
    }

    private void setNotificationMessage() {
        Random generator = new Random();
        int number = generator.nextInt(2) + 1;
        switch(number) {
            case 1:
                message = context.getString(R.string.default_humidity);
                icon = R.mipmap.humidity_ic;
                break;
            default:
                message = context.getString(R.string.default_movement);
                icon = R.mipmap.running_ic;
                break;
        }
        Log.e(TAG, String.valueOf(number));
    }

    private void openExperienceSampling() {

                Log.e(TAG, "notification is still open");
                // Do something.
                Random generator = new Random();
                int randomInt = generator.nextInt(2-0) + 0;
                Log.d("random", String.valueOf(randomInt));

                cooldown = SharedPreferencesStorage.readSharedPreference(context, Constants.PREFERENCES, Constants.COOLDOWN_KEY);

                Log.e("cooldown", String.valueOf(cooldown));

                if(randomInt == 1) {
                    if(!cooldown.equals("true")) {
                        opensurveycounter++;
                        SharedPreferencesStorage.writeSharedPreference(context, Constants.PREFERENCES, Constants.COUNTER_KEY, String.valueOf(opensurveycounter));
                        SharedPreferencesStorage.writeSharedPreference(context, Constants.PREFERENCES, Constants.COOLDOWN_KEY, "true");

                        Intent intent = new Intent(context, ExperienceSamplingActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }
                }
                if(randomInt == 0 && cooldown.equals("true")) {
                    SharedPreferencesStorage.writeSharedPreference(context, Constants.PREFERENCES, Constants.COOLDOWN_KEY, "false");
                }
    }

    private void openSurvey() {
        Intent intent = new Intent(context, ExperienceSamplingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
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
