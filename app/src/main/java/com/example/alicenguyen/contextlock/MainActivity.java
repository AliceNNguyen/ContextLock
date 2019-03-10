package com.example.alicenguyen.contextlock;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;

import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;


public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";

    private static final String PREFERENCES = "com.example.alicenguyen.contextlock";

    private SensorManager mSensorManager;
    private Sensor relativeHumidity;
    private Sensor light;
    private Sensor ambientTemperatureSensor;

    //context variables
    private int  relative_humidity = 0;
    private double ambient_temperature = 0;

    private EditText userIdInput;
    private Button startTrackingButton, stopTrackingButton, enterIDButton;

    private boolean userpermission = false;
    private boolean jobstarted = false;
    //private boolean permissionAgreed = false;

    private ActivityRecognitionClient mActivityRecognitionClient;
    private PendingIntent mPendingIntent;
    private ActivityBroadcastReceiver mActivityBroadcastReceiver;
    private LockScreenReceiver mLockScreenReceiver;
    private IntentFilter intentFilter;
    boolean isRegistered = false;
    private SharedPreferences prefs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        notificationSettingsRequest();
        showPermissionDialog();
        setUserId();
        setRandomAlarmReceiver();
        //startAlarm();
        setAlarmForLogEventsExport();
        initTrackingButtons();

        /*register broadcast receiver for UserActivityClient*/
        mActivityBroadcastReceiver = new ActivityBroadcastReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.BROADCAST_DETECTED_ACTIVITY);
        registerReceiver(mActivityBroadcastReceiver, intentFilter);
        registerLockScreenReceiver();
    }

    private void registerLockScreenReceiver() {
        mLockScreenReceiver = new LockScreenReceiver();

        final IntentFilter intentFilter = new IntentFilter();
        /** System Defined Broadcast */
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(mLockScreenReceiver, intentFilter);
        //registerReceiver(mLockScreenReceiver, new IntentFilter("android.intent.action.USER_PRESENT"));

    }


    private void writeIDtoSharedPreferences(String id) {
        SharedPreferences prefs = this.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        prefs.edit().putString(Constants.KEY_ID, id).apply();
    }

    public String getIDfromSharedPreferences(){
        SharedPreferences prefs = this.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        return prefs.getString(Constants.KEY_ID, "");
    }

    private void setUserId() {
        userIdInput = findViewById(R.id.set_id_input);
        final String id = getIDfromSharedPreferences();
        if(!id.equals("")){
            userIdInput.setText(id, TextView.BufferType.EDITABLE);
        }
        enterIDButton = findViewById(R.id.set_user_id_button);
        enterIDButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userID = userIdInput.getText().toString();
                if(!userID.isEmpty()){
                    writeIDtoSharedPreferences(userID);
                    Toast.makeText(MainActivity.this, "ID set", Toast.LENGTH_LONG).show();
                    openInitialSurvey();
                }else {
                    Toast.makeText(MainActivity.this, "Please enter ID", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /*open initial survey when user clicks on confirm id button the first time*/
    private void openInitialSurvey() {
        boolean previouslyStarted = prefs.getBoolean(Constants.FIRST_OPEN_SURVEY, false);
        if(!previouslyStarted) {
            Intent i = new Intent(this, InitialSurvey.class);
            startActivity(i);
            SharedPreferences.Editor edit = prefs.edit();
            edit.putBoolean(Constants.FIRST_OPEN_SURVEY, Boolean.TRUE);
            edit.commit();
        }
    }

    /*if permission are granted, background services will be started after user starts the tracking (on start tracking clicked)*/
    private void checkAppPermissions() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
            return;
        } else {
            /*Permission has already been granted*/
            Log.e("permission", "GPS permission granted");
            userpermission = true;
            final String id = getIDfromSharedPreferences();
            if(id.equals("")){
                Toast.makeText(this, "Please enter ID and submit", Toast.LENGTH_LONG).show();
            }else {
                startBackgroundServices();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    final String id = getIDfromSharedPreferences();
                    if(id.equals("")){
                        Toast.makeText(this, "Please enter ID and submit", Toast.LENGTH_LONG).show();
                    }else {
                        startBackgroundServices();
                    }
                } else {
                    // permission denied
                    Toast.makeText(MainActivity.this, "Please accept GPS permission to user the App", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }


    private final SensorEventListener mSensorEventListener
            = new SensorEventListener() {
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub
        }
        @Override
        public void onSensorChanged(SensorEvent event) {
            Log.d("sensor changed", "in method");
            if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
                float light = event.values[0];
                Log.d("sensor light", String.valueOf(light));
            }
            if (event.sensor.getType() == Sensor.TYPE_RELATIVE_HUMIDITY) {
                float humidity = event.values[0];
                Log.d("sensor humidity", String.valueOf(humidity));
                relative_humidity = (int) humidity;
            }
            if (event.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
                float temperature = event.values[0];
                Log.d("sensor temp", String.valueOf(temperature));
                ambient_temperature = (double) temperature;
            }
        }
    };

    private void getSensors() {
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        relativeHumidity = mSensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        light = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        ambientTemperatureSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        List<Sensor> list = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        //Toast.makeText(MainActivity.this, list.toString(), Toast.LENGTH_LONG).show();
        Log.d("sensor list sensor", list.toString());

        if (mSensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY) != null) {
            // Success! There's a magnetometer.
            Toast.makeText(MainActivity.this, "humidity sensor available", Toast.LENGTH_SHORT).show();

            mSensorManager.registerListener(mSensorEventListener, relativeHumidity,
                    SensorManager.SENSOR_DELAY_UI);
        } else {
            //getUserLocation();
            Toast.makeText(MainActivity.this, "no humidity sensor", Toast.LENGTH_SHORT).show();
        }
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null) {
            // Success! There's a magnetometer.
            Toast.makeText(MainActivity.this, "light sensor available", Toast.LENGTH_SHORT).show();

            mSensorManager.registerListener(mSensorEventListener, light,
                    SensorManager.SENSOR_DELAY_UI);
        } else {
            Toast.makeText(MainActivity.this, "no light sensor", Toast.LENGTH_SHORT).show();
        }
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE) != null) {
            // Success! There's a magnetometer.
            mSensorManager.registerListener(mSensorEventListener, ambientTemperatureSensor,
                    SensorManager.SENSOR_DELAY_UI);
        } else {
            //getLocation();
            Toast.makeText(MainActivity.this, "no light sensor", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkGPS() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Log.e(TAG, "checkGPS");

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Log.e(TAG, "GPS enabled");
            //Toast.makeText(this, "GPS is Enabled in your devide", Toast.LENGTH_SHORT).show();
        }else{
            Log.e(TAG, "no GPS");
            buildAlertMessageNoGps();
        }
    }

    /*Inform the user which data are tracked for the user study and ask for permission*/
    private void showPermissionDialog() {
        boolean permissionAgreed= prefs.getBoolean(Constants.PERMISSION_AGREE, false);
        AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(this, android.R.style.Theme_Holo_Light_Dialog));
        View mDialogView = getLayoutInflater().inflate(R.layout.permission_dialog, null);
        TextView permissionTitle = (TextView) mDialogView.findViewById(R.id.permission_title);
        final CheckBox permissionCheckbox = (CheckBox) mDialogView.findViewById(R.id.checkbox_permission_agree);
        Button mAgreeButton = (Button) mDialogView.findViewById(R.id.agree_permission_button);
        Button mCancelButton= (Button) mDialogView.findViewById(R.id.cancel_permission_button);
        mDialogBuilder.setView(mDialogView);
        final AlertDialog dialog = mDialogBuilder.create();
        //set opacity of dialog
        //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white)));

        if(!permissionAgreed) {
            dialog.show();
        }
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!permissionCheckbox.isChecked()) {
                    Toast.makeText(getApplicationContext(),getString(R.string.agree_permission_request), Toast.LENGTH_LONG).show();
                }
            }
        });
        mAgreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(permissionCheckbox.isChecked()) {
                    //permissionAgreed = true;
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putBoolean(Constants.PERMISSION_AGREE, Boolean.TRUE);
                    edit.commit();
                    dialog.cancel();
                }else {
                    Toast.makeText(getApplicationContext(), getString(R.string.agree_permission_request), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /*set study duration when app starts for the first time*/
    private void setSwitchVersionDate() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, Constants.STUDY_LENGTH);
        Date date = c.getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String switchVersionDate = simpleDateFormat.format(date);
        SharedPreferencesStorage.writeSharedPreference(this, Constants.PREFERENCES, Constants.SWITCH_VERSION_KEY, switchVersionDate);
    }

    /*helper dialog to support user setting up notification permission*/
    private void notificationSettingsRequest() {
        boolean previouslyStarted = prefs.getBoolean(Constants.FIRST_OPEN, false);
        if(!previouslyStarted) {
            setSwitchVersionDate();
            SharedPreferences.Editor edit = prefs.edit();
            edit.putBoolean(Constants.FIRST_OPEN, Boolean.TRUE);
            edit.commit();
            final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, android.R.style.Theme_Holo_Light_Dialog));
            builder.setMessage("Notifications on your lock screen needs to be enabled for using the App.")
                    .setCancelable(false)
                    .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                        public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            Intent settingsIntent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    //for Android 5-7
                                    .putExtra("app_package", getPackageName())
                                    .putExtra("app_uid", getApplicationInfo().uid)
                                    //for Android 8 and above
                                    .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                            startActivity(settingsIntent);
                        }
                    })
                    .setNegativeButton("no", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            dialog.cancel();
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(" Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("ّyes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("no", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }


    private void turnGPSOff() {
        String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if (provider.contains("gps")) { //if gps is enabled
            final Intent poke = new Intent();
            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3"));
            sendBroadcast(poke);
        }
    }

    /*This method schedule an alarm to send locally stored data to firebase DB.
    Function is called each day at nighttime
    */
    private void setAlarmForLogEventsExport() {
        Log.e(TAG, "start export");
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, ExportDBHelper.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, 0);

        //TODO set alarm to midnight/after midnight after testing done
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 24 );
        c.set(Calendar.MINUTE, 0);

        // alarm will fire the next day if alarm time is before current time
        /*if (c.before(Calendar.getInstance())) {
            c.add(Calendar.DATE, 1);
        }*/

        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(),AlarmManager.INTERVAL_DAY, pendingIntent);
    }

    private void setRandomAlarmReceiver() {
        Log.e(TAG, "startAlarm");
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, RandomAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, 0);

        Calendar c = Calendar.getInstance();
        c.add(Calendar.MINUTE, 5);

        Log.e(TAG, c.toString());
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
    }

    /*Backup alarm scheduler to send notification. It makes sure that user will receive
    notification even if no context conditions are met and/or background tracking is enabled/ not working
    Alarm is set randomly
    */
    private void startAlarm() {
        Log.e(TAG, "startAlarm");
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, 0);
        Random random = new Random();

        /*
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MINUTE, 30);*/
        /*c.set(Calendar.HOUR_OF_DAY, random.nextInt(14-8) + 8);
        c.set(Calendar.MINUTE, random.nextInt(60));
        c.set(Calendar.SECOND, random.nextInt(999999999 + 1));*/

        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 9);
        c.set(Calendar.MINUTE, 15);
        c.set(Calendar.SECOND, 0);


        Log.e(TAG, c.toString());

        //alarm will fire the next day if alarm time is before current time
        /*if (c.before(Calendar.getInstance())) {
            c.add(Calendar.DATE, 1);
        }*/

        alarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);

        //alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), AlarmManager.INTERVAL_DAY / 3, pendingIntent);
    }


    private void getUserActivity() {
        mActivityRecognitionClient = new ActivityRecognitionClient(this);
        requestActivityUpdatesHandler();
    }

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
                Toast.makeText(getApplicationContext(),
                        "Removed activity updates successfully!",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Failed to remove activity updates!",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }



    /*receive activities from broadcast*/
    public void receiveUserActivity() {
        Intent serviceIntent = new Intent(this,UserActivityJobIntentService.class);
        mPendingIntent = PendingIntent.getBroadcast(getApplicationContext(),
                0,
                mActivityBroadcastReceiver.getIntent(this, serviceIntent, 1234),
                PendingIntent.FLAG_UPDATE_CURRENT);
        getUserActivity();
        //UserActivityJobIntentService.enqueueWork(this, serviceIntent);
    }

    /*ob button pressed: tracking of user location for weather and user activity are started*/
    private void initTrackingButtons(){
        startTrackingButton = findViewById(R.id.start_tracking_button);
        stopTrackingButton = findViewById(R.id.stop_tracking_button);
        startJobScheduler();
        stopJobScheduler();
    }

    /*init Job Schedulars*/
    public void startBackgroundServices(){
        startTrackingButton.setBackgroundColor(ContextCompat.getColor(this,R.color.button_active_color));
        stopTrackingButton.setBackgroundResource(R.drawable.button_color_gradient);
        Toast.makeText(getApplicationContext(), "Tracking gestartet", Toast.LENGTH_SHORT).show();
        receiveUserActivity();
        ComponentName componentName = new ComponentName(MainActivity.this, LocationJobService.class);
        JobInfo info = new JobInfo.Builder(123, componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)
                .setPeriodic(Constants.JOB_SERVICE_INTERVAL)
                .build();
        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        int resultCode = scheduler.schedule(info);
        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            Log.e(TAG, "Job scheduled");
        } else {
            Log.e(TAG, "Job scheduling failed");
        }
    }


    private void startJobScheduler() {
        startTrackingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAppPermissions();
            }
        });
    }


    /*on button pressed: background tracking will be stopped*/
    public void stopJobScheduler() {
        stopTrackingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTrackingButton.setBackgroundColor(getResources().getColor(R.color.button_active_color));
                startTrackingButton.setBackgroundResource(R.drawable.button_color_gradient);
                JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
                scheduler.cancel(123);
                Log.e(TAG, "Job cancelled");
                Toast.makeText(getApplicationContext(), "Tracking gestoppt", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /*unregister receiver*/
    @Override
    protected void onDestroy() {
        super.onDestroy();
       if(isRegistered){
           unregisterReceiver(mActivityBroadcastReceiver);
           unregisterReceiver(mLockScreenReceiver);
       }
        try {
           unregisterReceiver(mActivityBroadcastReceiver);
           unregisterReceiver(mLockScreenReceiver);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
