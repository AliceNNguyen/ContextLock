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


    //TODO set number auf calendars for number of lockscreen display
    @Override
    public void onReceive(Context context, Intent intent) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, LockscreenHelper.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, i, 0);
        Random random = new Random();

        /*set random alarm three time each day between 5 and 23 o'clock*/
        Calendar c1 = Calendar.getInstance();
        c1.set(Calendar.HOUR_OF_DAY, random.nextInt(23-17) + 23);

        Calendar c2 = Calendar.getInstance();
        c2.set(Calendar.HOUR_OF_DAY, random.nextInt(17-11) + 11);

        Calendar c3= Calendar.getInstance();
        c3.set(Calendar.HOUR_OF_DAY, random.nextInt(11-8) + 8);

        Log.e(TAG, c1.toString());
        Log.e(TAG, c2.toString());
        Log.e(TAG, c3.toString());


        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 15);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);

        Log.e(TAG, c.toString());

        //alarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, c1.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, c2.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, c3.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
    }
}
