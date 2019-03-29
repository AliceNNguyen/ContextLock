package com.example.alicenguyen.contextlock;

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

/*set random alarm to trigger lock screen*/
public class RandomAlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "RandomAlarmReceiver";
    private static final int REQUEST_CODE = 1;
    private DatabaseReference mDatabaseReference;

    //TODO set number auf calendars for number of lockscreen display
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences pref = context.getApplicationContext().getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE);
        String userid = pref.getString(Constants.KEY_ID, "0");
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("randomAlarm").child(userid).child(Calendar.getInstance().getTime().toString());


        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, LockscreenHelper.class);
        Random random = new Random();

        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, random.nextInt(11-8) + 8); //16
        //c.set(Calendar.HOUR_OF_DAY, 12); //16
        //c.set(Calendar.MINUTE, 20); //30
        //c.set(Calendar.SECOND, 0);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, i, 0);

        Calendar c1 = Calendar.getInstance();
        c1.set(Calendar.HOUR_OF_DAY, random.nextInt(14-11) + 11);
        PendingIntent pendingIntent1 = PendingIntent.getBroadcast(context, 2, i, 0); //flags 0


        Calendar c2 = Calendar.getInstance();
        c2.set(Calendar.HOUR_OF_DAY, random.nextInt(17-14) + 14);
        PendingIntent pendingIntent2 = PendingIntent.getBroadcast(context, 3, i, 0);


        Calendar c3= Calendar.getInstance();
        c3.set(Calendar.HOUR_OF_DAY, random.nextInt(20-17) + 17);
        PendingIntent pendingIntent3 = PendingIntent.getBroadcast(context, 4, i, 0);


        Calendar c4 = Calendar.getInstance();
        c4.set(Calendar.HOUR_OF_DAY, random.nextInt(23-20) + 20); //16
        /*c4.set(Calendar.HOUR_OF_DAY,12); //16
        c4.set(Calendar.MINUTE, 30);*/
        PendingIntent pendingIntent4 = PendingIntent.getBroadcast(context, 5, i, 0);


        Log.e(TAG, c1.toString());
        Log.e(TAG, c2.toString());
        Log.e(TAG, c3.toString());

        Log.e(TAG, c.getTime().toString());
        Log.e(TAG, c4.toString());

        mDatabaseReference.child("1").setValue(c.getTime().toString());
        mDatabaseReference.child("2").setValue(c1.getTime().toString());
        mDatabaseReference.child("3").setValue(c2.getTime().toString());
        mDatabaseReference.child("4").setValue(c3.getTime().toString());
        mDatabaseReference.child("5").setValue(c4.getTime().toString());

        alarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
        alarmManager.set(AlarmManager.RTC_WAKEUP, c1.getTimeInMillis(), pendingIntent1);
        alarmManager.set(AlarmManager.RTC_WAKEUP, c2.getTimeInMillis(),  pendingIntent2);
        alarmManager.set(AlarmManager.RTC_WAKEUP, c3.getTimeInMillis(),  pendingIntent3);
        alarmManager.set(AlarmManager.RTC_WAKEUP, c4.getTimeInMillis(), pendingIntent4);

    }
}
