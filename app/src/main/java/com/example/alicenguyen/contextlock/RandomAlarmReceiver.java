package com.example.alicenguyen.contextlock;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;
import java.util.Random;

public class RandomAlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "RandomAlarmReceiver";
    private static final int REQUEST_CODE = 1;


    //TODO set number auf calendars for number of lockscreen display
    @Override
    public void onReceive(Context context, Intent intent) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, LockscreenHelper.class);
        Random random = new Random();

        /*set random alarm three time each day between 5 and 23 o'clock*/
        Calendar c1 = Calendar.getInstance();
        c1.set(Calendar.HOUR_OF_DAY, random.nextInt(23-20) + 20);
        PendingIntent pendingIntent1 = PendingIntent.getBroadcast(context, 1, i, 0); //flags 0


        Calendar c2 = Calendar.getInstance();
        c2.set(Calendar.HOUR_OF_DAY, random.nextInt(20-17) + 17);
        PendingIntent pendingIntent2 = PendingIntent.getBroadcast(context, 2, i, 0);


        Calendar c3= Calendar.getInstance();
        c3.set(Calendar.HOUR_OF_DAY, random.nextInt(17-14) + 14);
        PendingIntent pendingIntent3 = PendingIntent.getBroadcast(context, 3, i, 0);


        Log.e(TAG, c1.toString());
        Log.e(TAG, c2.toString());
        Log.e(TAG, c3.toString());


        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, random.nextInt(14-11) + 11); //16
        c.set(Calendar.MINUTE, 30); //30
        c.set(Calendar.SECOND, 0);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 4, i, PendingIntent.FLAG_CANCEL_CURRENT);

        Calendar c5 = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, random.nextInt(11-8) + 8); //16
        c.set(Calendar.MINUTE, 30); //30
        c.set(Calendar.SECOND, 0);
        PendingIntent pendingIntent5 = PendingIntent.getBroadcast(context, 5, i, PendingIntent.FLAG_CANCEL_CURRENT);


        Log.e(TAG, c.toString());
        Log.e(TAG, c5.toString());


        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(),AlarmManager.INTERVAL_DAY, pendingIntent);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, c1.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent1);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, c2.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent2);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, c3.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent3);

        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, c5.getTimeInMillis(),AlarmManager.INTERVAL_DAY, pendingIntent5);

    }
}
