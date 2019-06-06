package com.lmu.alicenguyen.contextlock;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

/*restart alarm if device reboots*/
public class DeviceRebootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            try {
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                Intent alarmIntent = new Intent(context, RandomAlarmReceiver.class);
                Calendar c = Calendar.getInstance();
                c.set(Calendar.HOUR_OF_DAY, 1);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                if(alarmManager != null) {
                    alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), (24 * 1000 * 60 * 60), pendingIntent); //every 24 hours
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
