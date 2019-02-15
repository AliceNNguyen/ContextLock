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

    //BroadcastReceiver broadcastReceiver;


    //private FirebaseAnalytics mFirebaseAnalytics;
    LockScreenReceiver broadcastReceiver = new LockScreenReceiver();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //registerScreenReceiver();

        //getSensors();
        //getUserLocation();
        //createNotificationChannel();
        //getUserActivity();
        //getLocationService();
        //getServices();
        //getLocation();



        //mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        setUserId();
        initTrackingButtons();
    }

    /*private void registerScreenReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        //intentFilter.addAction("android.intent.action.NEW_OUTGOING_CALL");
        intentFilter.addAction("android.intent.action.USER_PRESENT");
        intentFilter.addAction("android.intent.action.ACTION_SHUTDOWN");
        this.registerReceiver(broadcastReceiver, intentFilter);
    }*/



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
            if(userIdInput.getText().toString().matches("")){
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
                    startBackgroundServices();
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


    //TODO
    /*private void handleUserLocation(String longitude, String latitude) {
        if(!longitude.equals("") && !latitude.equals("")) {
            Log.d("server", "weather");
            getWeatherData(longitude, latitude);
        }

    }*/

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

    /*private void handleUserActivity(int type, int confidence) {
        userActivity = getString(R.string.activity_unknown);
        //int icon = R.drawable.ic_still;
        switch (type) {
            case DetectedActivity.IN_VEHICLE: {
                userActivity = getString(R.string.activity_in_vehicle);
                //icon = R.drawable.ic_driving;
                break;
            }
            case DetectedActivity.ON_BICYCLE: {
                userActivity = getString(R.string.activity_on_bicycle);
                //icon = R.drawable.ic_on_bicycle;
                break;
            }
            case DetectedActivity.ON_FOOT: {
                userActivity = getString(R.string.activity_on_foot);
                //icon = R.drawable.ic_walking;
                break;
            }
            case DetectedActivity.RUNNING: {
                userActivity = getString(R.string.activity_running);
                //icon = R.drawable.ic_running;
                break;
            }
            case DetectedActivity.STILL: {
                userActivity = getString(R.string.activity_still);
                break;
            }
            case DetectedActivity.TILTING: {
                userActivity = getString(R.string.activity_tilting);
                //icon = R.drawable.ic_tilting;
                break;
            }
            case DetectedActivity.WALKING: {
                userActivity = getString(R.string.activity_walking);
                //icon = R.drawable.ic_walking;
                break;
            }
            case DetectedActivity.UNKNOWN: {
                userActivity = getString(R.string.activity_unknown);
                break;
            }
        }

        Log.d(TAG, "User activity: " + userActivity + ", Confidence: " + confidence);
        Bundle bundle = new Bundle();
        bundle.putString("user_activity", String.valueOf(userActivity));
        mFirebaseAnalytics.logEvent("user_activity", bundle);

        if (confidence > Constants.CONFIDENCE) {
            //txtActivity.setText(label);
            //txtConfidence.setText("Confidence: " + confidence);
            //imgActivity.setImageResource(icon);

            Toast.makeText(this, userActivity, Toast.LENGTH_LONG).show();
            checkContextForNotification();
            //setContextIcon(mainWeather, temperature.doubleValue(), humidity, relative_humidity, ambient_temperature, userActivity);
        }
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

    /*private void getUserLocation() {
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                String lon = "" + location.getLongitude();
                String lat = "" + location.getLatitude();


                Log.d("gps provider gps", location.getProvider());
                getWeatherData(lon, lat);

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // getting GPS status
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // getting network status
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        Log.d("network enabled", String.valueOf(isNetworkEnabled));
        Log.d("gps enabled", String.valueOf(isGPSEnabled));

        //check for permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);

            // ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            // public void onRequestPermissionsResult(int requestCode, String[] permissions int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            Toast.makeText(this, "Not Enough Permission", Toast.LENGTH_SHORT).show();
            return;
        }else{ //if permission enabled
            if(isNetworkEnabled) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, locationListener);
            }
            if(isGPSEnabled)  {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, locationListener);
            }
        }
    }*/

    /*private void getWeatherData(String longitude, String latitude) {
        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        String url = WEATHER_URL + latitude + "&lon=" + longitude + "&appid=" + OPEN_WEATHER_API_KEY;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("response json", response.toString());
                        try {
                            //Log.d("json humidity", response.getJSONObject("weather").toString());
                            JSONArray mainWeatherArray = response.getJSONArray("weather");
                            //JSONObject mainWeather = mainWeatherArray.getJSONObject(0);
                            //String main = mainWeather.getString("main");
                            Log.d("main weather main", mainWeatherArray.getJSONObject(0).get("main").toString());
                            mainWeather = mainWeatherArray.getJSONObject(0).get("main").toString();
                            humidity = (int)response.getJSONObject("main").get("humidity");
                            temperature = (Number)response.getJSONObject("main").get("temp");
                            temperature = temperature.doubleValue();
                            checkContextForNotification();
                            //setContextIcon(mainWeather, temperature.doubleValue(), humidity, 0, ambient_temperature, userActivity);
                        } catch (JSONException e) {
                            Log.d("json exception","json exception log");
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        //Toast toast = Toast.makeText(MainActivity.this, "no response", Toast.LENGTH_LONG);
                        //toast.show();
                    }
                });
        queue.add(jsonObjectRequest);
    }*/

    /*private void checkContextForNotification() {
        Log.d("weather description", mainWeather);
        Log.d("gps enabled", String.valueOf(isGPSEnabled));
        Log.e("user activity", userActivity);

        if(mainWeather.equals("Rain") && provider.equals("gps")) {
            Toast.makeText(MainActivity.this, "Regen", Toast.LENGTH_LONG).show();
            sendNotification(getString(R.string.rain), R.mipmap.raindrop_ic);
        }else if(mainWeather.equals("Snow") && provider.equals("gps")) {
            Toast.makeText(MainActivity.this, "Schnee", Toast.LENGTH_LONG).show();
            sendNotification(getString(R.string.snow), R.mipmap.snow_ic);
        }else if(mainWeather.contains("Drizzle") && provider.equals("gps")){
            sendNotification(getString(R.string.rain), R.mipmap.raindrop_ic);
        } else if(userActivity.equals("still")) {
            Toast.makeText(MainActivity.this, "still", Toast.LENGTH_LONG).show();
            sendNotification(getString(R.string.running), R.mipmap.running_ic);
        }else if(userActivity.equals("in_vehicle")){
            sendNotification(getString(R.string.in_vehicle), R.mipmap.publictransport_ic);

        }else {
            Toast.makeText(MainActivity.this, "Alles gut!", Toast.LENGTH_LONG).show();
        }
    }*/


    /*private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }*/

    /*private void sendNotification(String contextDesscription, int icon) {
        Log.e("notfication", "send");

        int notificationId = 1;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                icon);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_fingerprint)
                .setLargeIcon(bitmap)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(contextDesscription +  " " + getString(R.string.notification_description))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setStyle(new NotificationCompat.BigTextStyle()
                .bigText(contextDesscription + " " +  getString(R.string.notification_description)))
                //.setStyle(new NotificationCompat.BigPictureStyle()
                //.bigPicture(bitmap).setSummaryText("message"))
                // Set the intent that will fire when the user taps the notification
                //.setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(notificationId, mBuilder.build());
    }*/


    //Latitude: 37.4220
    //Longitude: -122.0840

    //Amsterdam
    //Latitude: 52.3792
    //Longitude: 4.8994


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
            //getLocation();
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


    private String getPinFromSharedPreferences() {
        SharedPreferences prefs = this.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        return prefs.getString(KEY_PIN, "");
    }


    private void writeCooldowntoSharedPreferences(boolean cooldown) {
        SharedPreferences prefs = this.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        prefs.edit().putString(COOLDOWN_KEY, String.valueOf(cooldown));
    }

    public String getCooldownfromSharedPreferences(){
        SharedPreferences prefs = this.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        return prefs.getString(COOLDOWN_KEY, "false");
    }

    public void writeSurveyOpenCountertoSharedPreferences(int opencounter) {
        SharedPreferences prefs = this.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        prefs.edit().putString(COUNTER_KEY, String.valueOf(cooldown));
    }

    public String getSurveyOpenCounterfromSharedPreferences(){
        SharedPreferences prefs = this.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        return prefs.getString(COUNTER_KEY, "0");
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
                Log.e("broadcast updates", "sucess update activities");

            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("broadcas updates", "failed update activities");
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


    //receive activities from broadcast
    public void receiveUserActivity() {
        Intent serviceIntent = new Intent(this,UserActivityJobIntentService.class);
        //Context.sendBroadcast(ActivityBroadcastReceiver.getIntent(this, serviceIntent, 12345));
        mPendingIntent = PendingIntent.getBroadcast(getApplicationContext(),
                1,
                ActivityBroadcastReceiver.getIntent(this, serviceIntent, 12345),
                PendingIntent.FLAG_UPDATE_CURRENT);
        getUserActivity();
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
        receiveUserActivity();

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
                scheduler.cancel(1234);
                Log.e(TAG, "Job cancelled");
                Toast.makeText(getApplicationContext(), "Tracking gestoppt", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeActivityUpdatesHandler();
    }
}
