package com.lmu.alicenguyen.contextlock;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Random;

import static android.content.Context.MODE_PRIVATE;

/*set random alarms to trigger lock screen*/
public class RandomAlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "RandomAlarmReceiver";

    //TODO set number auf calendars for number of lockscreen display
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences pref = context.getApplicationContext().getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE);
        String userid = pref.getString(Constants.KEY_ID, "0");
        DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("randomAlarm").child(userid).child(Calendar.getInstance().getTime().toString());

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, LockScreenHelper.class);
        Random random = new Random();

        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, random.nextInt(12-8) + 8);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, i, 0);

        Calendar c1 = Calendar.getInstance();
        c1.set(Calendar.HOUR_OF_DAY, random.nextInt(15-12) + 12);
        PendingIntent pendingIntent1 = PendingIntent.getBroadcast(context, 2, i, 0); //flags 0


        Calendar c2 = Calendar.getInstance();
        c2.set(Calendar.HOUR_OF_DAY, random.nextInt(18-15) + 15);
        PendingIntent pendingIntent2 = PendingIntent.getBroadcast(context, 3, i, 0);


        Calendar c3= Calendar.getInstance();
        c3.set(Calendar.HOUR_OF_DAY, random.nextInt(22-18) + 18);
        PendingIntent pendingIntent3 = PendingIntent.getBroadcast(context, 4, i, 0);



        Log.e(TAG, c1.toString());
        Log.e(TAG, c2.toString());
        Log.e(TAG, c3.toString());

        Log.e(TAG, c.getTime().toString());


        mDatabaseReference.child("1").setValue(c.getTime().toString());
        mDatabaseReference.child("2").setValue(c1.getTime().toString());
        mDatabaseReference.child("3").setValue(c2.getTime().toString());
        mDatabaseReference.child("4").setValue(c3.getTime().toString());


        if(alarmManager != null ) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
            alarmManager.set(AlarmManager.RTC_WAKEUP, c1.getTimeInMillis(), pendingIntent1);
            alarmManager.set(AlarmManager.RTC_WAKEUP, c2.getTimeInMillis(), pendingIntent2);
            alarmManager.set(AlarmManager.RTC_WAKEUP, c3.getTimeInMillis(), pendingIntent3);
        }
    }
}
