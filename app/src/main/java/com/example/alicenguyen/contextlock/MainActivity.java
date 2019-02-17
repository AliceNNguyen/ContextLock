package com.example.alicenguyen.contextlock;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;



//import classes from andrognito and amirarcane
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;


import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
//import com.google.firebase.analytics.FirebaseAnalytics;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";

    private static final String PREFERENCES = "com.example.alicenguyen.contextlock";
    private static final String KEY_PIN = "pin";
    private static final String KEY_ID = "user_id";

    private static final int MAX_ATTEMPS = 3;
    private static final int SURVEY_OPEN_NUMBER = 5;
    private static final String COOLDOWN_KEY = "cooldown_key";
    private static final String COUNTER_KEY = "counter_key";
    private static final String CHANNEL_ID = "channel-id";
    private int survey_open_counter = 0;
    private boolean cooldown = false;


    private SensorManager mSensorManager;
    private Sensor relativeHumidity;
    private Sensor light;
    private Sensor ambientTemperatureSensor;

    //context variables
    private int  relative_humidity = 0;
    private double ambient_temperature = 0;
    private String mainWeather = "";
    private Number temperature = 0;
    private int humidity = 0;
    private String userActivity = "unknown";


    protected LocationManager locationManager;
    protected LocationListener locationListener;

    private String OPEN_WEATHER_API_KEY = "18d997dfe947e33eb626ce588b9c7510";
    private String WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather?units=metric&lat="; //{lat}&lon={lon}"
    private String testURL= "http://api.openweathermap.org/data/2.5/weather?lat=48.366512&lon=10.894446&units=metric&appid=18d997dfe947e33eb626ce588b9c7510";

    private int LOCATION_ACCURACY = 20;
    private static final int LOCATION_INTERVAL = 10* 1000 * 60; // 1000 * 60 * 1; --> 1 minute// 5*60*1000
    private static final float LOCATION_DISTANCE = 10f; //10 meters

    private EditText userIdInput;
    private Button startTrackingButton, stopTrackingButton, enterIDButton;

    private String longitude, latitude, provider;

    //flag for gps status
    boolean isGPSEnabled = false;
    // flag for network status
    boolean isNetworkEnabled = false;

    private boolean userpermission = false;
    private boolean jobstarted = false;


    private ActivityRecognitionClient mActivityRecognitionClient;
    private PendingIntent mPendingIntent;
    private ActivityBroadcastReceiver mActivityBroadcastReceiver;
    private IntentFilter intentFilter;
    boolean isRegistered = false;

    private LocalBroadcastManager localBroadcastManager;
    private SharedPreferences sharedPreferences;

    //BroadcastReceiver broadcastReceiver;


    //private FirebaseAnalytics mFirebaseAnalytics;
    LockScreenReceiver broadcastReceiver = new LockScreenReceiver();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        setUserId();
        initTrackingButtons();
        /*mActivityBroadcastReceiver = new ActivityBroadcastReceiver();
        intentFilter = new IntentFilter();*/

        /*try {
            mActivityBroadcastReceiver = new ActivityBroadcastReceiver();
            intentFilter = new IntentFilter();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        mActivityBroadcastReceiver = new ActivityBroadcastReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.BROADCAST_DETECTED_ACTIVITY);
        registerReceiver(mActivityBroadcastReceiver, intentFilter);
       /* if(!isRegistered){
            registerReceiver(mActivityBroadcastReceiver, intentFilter);
            isRegistered = true;
        }*/
        /*if (mActivityBroadcastReceiver == null) {
            mActivityBroadcastReceiver = new ActivityBroadcastReceiver();
            intentFilter = new IntentFilter();
            //intentFilter.addAction(Constants.BROADCAST_DETECTED_ACTIVITY);
            registerReceiver(mActivityBroadcastReceiver, intentFilter);
            //Log.e(TAG, "onResume");
        }*/
    }



    private void writeIDtoSharedPreferences(String id) {
        SharedPreferences prefs = this.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_ID, id).apply();
    }

    public String getIDfromSharedPreferences(){
        SharedPreferences prefs = this.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        return prefs.getString(KEY_ID, "");
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
                    //mFirebaseAnalytics.setUserId(userID);
                    writeIDtoSharedPreferences(userID);
                    Toast.makeText(MainActivity.this, "ID gesetzt", Toast.LENGTH_LONG).show();
                }else {
                    Toast.makeText(MainActivity.this, "Bitte eine ID eingeben", Toast.LENGTH_LONG).show();
                }
            }
        });
    }



    private void checkAppPermissions() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
            return;
        } else {
            // Permission has already been granted
            Log.e("permission", "GPS permission granted");
            userpermission = true;
            final String id = getIDfromSharedPreferences();
            if(id.equals("")){
                Toast.makeText(this, "Bitte ID eingeben und bestätigen", Toast.LENGTH_LONG).show();
            }else {
                startBackgroundServices();

            }
            //getServices();
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
                        Toast.makeText(this, "Bitte ID eingeben und bestätigen", Toast.LENGTH_LONG).show();
                    }else {
                        startBackgroundServices();

                    }
                    //startBackgroundServices();
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(MainActivity.this, "Bitte GPS erlauben, um App nutzen zu können", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }



    //getLocationService()
    /*private void getServices(){
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.e("on broadcast receive", "get location service");
                Log.e("broadcast", intent.getAction());
                if (intent.getAction().equals(Constants.BROADCAST_DETECTED_LOCATION)) {
                    longitude = "" + intent.getDoubleExtra("longitude", 0);
                    latitude = "" + intent.getDoubleExtra("latitude", 0);
                    provider = intent.getStringExtra("provider");
                    Log.e("location service",intent.getExtras().get("longitude").toString());
                    Log.e("location service", longitude + " " + latitude);
                    Log.e("service provider", provider);
                    //handleUserActivity(type, confidence);
                    handleUserLocation(longitude, latitude);
                }
                if (intent.getAction().equals(Constants.BROADCAST_DETECTED_ACTIVITY)) {
                    int type = intent.getIntExtra("type", -1);
                    int confidence = intent.getIntExtra("confidence", 0);
                    Log.e("action abc", String.valueOf(type));

                    handleUserActivity(type, confidence);
                }
                if (intent.getAction().equals(Constants.BROADCAST_DETECTED_GOOGLE_LOCATION)) {

                   String latitude = intent.getStringExtra("latitude");
                   String longitude = intent.getStringExtra("longitude");
                   Log.e("googel service", latitude + " " + longitude);
                }

            }
        };
        startTrackingLocation();
    }*/

    /*private void startTrackingLocation() {
        Intent locationIntent = new Intent(MainActivity.this, LocationService.class);
        startService(locationIntent);
        //startForegroundService(locationIntent);

        Intent intent1 = new Intent(MainActivity.this, BackgroundDetectedActivitiesService.class);
        startService(intent1);

        Intent googleLocationIntent = new Intent(MainActivity.this, LocationJobService.class);
        startService(googleLocationIntent);
    }*/

    /*private void getUserActivity(){
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.e("activity broadcast", intent.getAction());
                if (intent.getAction().equals(Constants.BROADCAST_DETECTED_ACTIVITY)) {
                    int type = intent.getIntExtra("type", -1);
                    int confidence = intent.getIntExtra("confidence", 0);
                    Log.e("action abc", String.valueOf(type));

                    handleUserActivity(type, confidence);
                }
            }
        };
        startTracking();
    }*/



    /*@Override
    protected void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.BROADCAST_DETECTED_ACTIVITY);
        intentFilter.addAction(Constants.BROADCAST_DETECTED_LOCATION);
        intentFilter.addAction(Constants.BROADCAST_DETECTED_GOOGLE_LOCATION);

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);

        /*LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter(Constants.BROADCAST_DETECTED_ACTIVITY));

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter(Constants.BROADCAST_DETECTED_LOCATION));
    }

    @Override

    protected void onPause() {
        super.onPause();

        Log.e("on pause", "unregister");

        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);

    }


    private void startTracking() {
        Log.e("start tracking", "start");
        Intent intent1 = new Intent(MainActivity.this, BackgroundDetectedActivitiesService.class);
        startService(intent1);
    }*/


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

                //TextView locationView = findViewById(R.id.location);
                //locationView.setText("pressure: " + String.valueOf(temperature) );
            }
            if (event.sensor.getType() == Sensor.TYPE_RELATIVE_HUMIDITY) {
                float humidity = event.values[0];
                Log.d("sensor humidity", String.valueOf(humidity));
                relative_humidity = (int) humidity;

                //TextView locationView = findViewById(R.id.location);
                //locationView.setText("pressure: " + String.valueOf(temperature) );
            }

            if (event.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
                float temperature = event.values[0];
                Log.d("sensor temp", String.valueOf(temperature));
                ambient_temperature = (double) temperature;
            }
            //setContextIcon(mainWeather, temperature.doubleValue(), humidity, relative_humidity, ambient_temperature, userActivity);
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


    /*@Override
    protected void onStart() {
        super.onStart();
        Log.e(TAG, "onStart");
        //Log.e(TAG, localBroadcastManager.toString());

        if(mActivityBroadcastReceiver == null) {
            Log.e(TAG, "receiver registered");
            mActivityBroadcastReceiver = new ActivityBroadcastReceiver();
            intentFilter = new IntentFilter();
            registerReceiver(mActivityBroadcastReceiver, intentFilter);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG, "onStop");
        if (mActivityBroadcastReceiver != null) {
            Log.e(TAG, "receiver unregistered");
            unregisterReceiver(mActivityBroadcastReceiver);
            mActivityBroadcastReceiver = null;
        }
    }*/



    /*@Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
       try{
            registerReceiver(mActivityBroadcastReceiver,intentFilter);
            //sharedPreferences.registerOnSharedPreferenceChangeListener((SharedPreferences.OnSharedPreferenceChangeListener) this);
        }catch (Exception e){
            // already registered
        }

    }*/

   /*@Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "onPause");
        try {
            unregisterReceiver(mActivityBroadcastReceiver);
            //sharedPreferences.unregisterOnSharedPreferenceChangeListener((SharedPreferences.OnSharedPreferenceChangeListener) this);
        }catch (Exception e) {

        }
    }*/

    //receive activities from broadcast
    public void receiveUserActivity() {
        Intent serviceIntent = new Intent(this,UserActivityJobIntentService.class);
        //Context.sendBroadcast(ActivityBroadcastReceiver.getIntent(this, serviceIntent, 12345));
        mPendingIntent = PendingIntent.getBroadcast(getApplicationContext(),
                0,
                mActivityBroadcastReceiver.getIntent(this, serviceIntent, 1234),
                PendingIntent.FLAG_UPDATE_CURRENT);
        getUserActivity();
        //UserActivityJobIntentService.enqueueWork(this, serviceIntent);
    }




    /*public void registerBroadcastReceiver(View view) {

        this.registerReceiver(broadcastReceiver, new IntentFilter(
                "android.intent.action.TIME_TICK"));
        Toast.makeText(this, "Registered broadcast receiver", Toast.LENGTH_SHORT)
                .show();
    }


    public void unregisterBroadcastReceiver(View view) {

        this.unregisterReceiver(broadcastReceiver);

        Toast.makeText(this, "unregistered broadcst receiver", Toast.LENGTH_SHORT)
                .show();
    }*/

    private void initTrackingButtons(){
        startTrackingButton = findViewById(R.id.start_tracking_button);
        stopTrackingButton = findViewById(R.id.stop_tracking_button);
        startJobScheduler();
        stopJobScheduler();

    }

    public void startBackgroundServices(){
        startTrackingButton.setBackgroundColor(getResources().getColor(R.color.button_active_color));
        stopTrackingButton.setBackgroundResource(R.drawable.button_color_gradient);
        Toast.makeText(getApplicationContext(), "Tracking gestartet", Toast.LENGTH_SHORT).show();
        receiveUserActivity();
        //setReceiver();
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



    public void stopJobScheduler() {
        stopTrackingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                stopTrackingButton.setBackgroundColor(getResources().getColor(R.color.button_active_color));
                startTrackingButton.setBackgroundResource(R.drawable.button_color_gradient);

                JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
                scheduler.cancel(123);
                //scheduler.cancel(1234);
                Log.e(TAG, "Job cancelled");
                //removeActivityUpdatesHandler();
                Toast.makeText(getApplicationContext(), "Tracking gestoppt", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
       if(isRegistered){
           unregisterReceiver(mActivityBroadcastReceiver);
       }


        try {
           unregisterReceiver(mActivityBroadcastReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        /*try{

        }catch (Exception e) {

        }*/

    }
}
