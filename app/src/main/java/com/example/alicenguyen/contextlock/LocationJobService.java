package com.example.alicenguyen.contextlock;

import android.Manifest;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
//import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;

public class LocationJobService extends JobService {
    private static final String TAG = "LocationJobService";
    private boolean jobCancelled = false;

    private FusedLocationProviderClient mFusedLocationClient;


    private boolean isGPSEnabled = false;
    private boolean isNetworkEnabled = false;

    private LocationManager locationManager;
    private LocationListener locationListener;
    //private Location location;
    private ActivityRecognitionClient mActivityRecognitionClient;
    private Intent mIntentService;
    private PendingIntent mPendingIntent;
    private BroadcastReceiver broadcastReceiver;
    //private FirebaseAnalytics mFirebaseAnalytics;


    private String OPEN_WEATHER_API_KEY = "18d997dfe947e33eb626ce588b9c7510";
    private String WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather?units=metric&lat=";
    private int  relative_humidity = 0;
    private double ambient_temperature = 0;
    private String mainWeather = "";
    private Number temperature = 0;
    private int humidity = 0;
    //private String userActivity = "";
    private String provider, latitude, longitude;
    private LockScreenReceiver lockScreenReceiver = new LockScreenReceiver();
    private boolean isLocked = false;


    public LocationJobService() {
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.e(TAG, "job started");
        createNotificationChannel();
        doBackgroundWork(params);

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        jobCancelled = true;
        return true;
    }

    private void doBackgroundWork(final JobParameters params) {
        if (jobCancelled) {
            return;
        }


        getUserLocation();
        //getUserActivity();
        Log.d(TAG, "Job finished");
        jobFinished(params, false);
       /* new Thread(new Runnable() {
            @Override
            public void run() {
                getUserLocation();
                Log.d(TAG, "Job finished");
                jobFinished(params, false);
            }
        }).start();*/
    }



    private void checkContextForNotification() {
        Log.d("weather description", mainWeather);
        Log.d("gps enabled", String.valueOf(isGPSEnabled));
        checkIfScreenLocked();

        if(isLocked) {
            Log.e(TAG, String.valueOf(isLocked));
            if(mainWeather.equals("Rain") && provider.equals("gps")) {
                sendNotification(getString(R.string.rain), R.mipmap.raindrop_ic);
            }else if(mainWeather.equals("Snow") && provider.equals("gps")) {
                sendNotification(getString(R.string.snow), R.mipmap.snow_ic);
            }else if(mainWeather.contains("Drizzle") && provider.equals("gps")){
                sendNotification(getString(R.string.rain), R.mipmap.raindrop_ic);
            }else {
                Log.e(TAG, "Alles gut");
                //Toast.makeText(this, "Alles gut!", Toast.LENGTH_LONG).show();
            }
            Intent intent = new Intent(this, ExperienceSamplingActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }


    }

    private void checkIfScreenLocked() {
        KeyguardManager myKM = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        if( myKM.inKeyguardRestrictedInputMode()) {
            isLocked = true;
            Log.e(TAG, "device is locked");
            //it is locked
        } else {
            isLocked = false;
            Log.e(TAG, "device not locked");
            //it is not locked
        }
    }

    private void getWeatherData() {
        RequestQueue queue = Volley.newRequestQueue(this);
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
                            Log.e("main weather main", mainWeatherArray.getJSONObject(0).get("main").toString());
                            mainWeather = mainWeatherArray.getJSONObject(0).get("main").toString();
                            humidity = (int)response.getJSONObject("main").get("humidity");
                            temperature = (Number)response.getJSONObject("main").get("temp");
                            temperature = temperature.doubleValue();
                            //TODO
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
    }


    private void getUserLocation() {
        Log.e(TAG, "get location method");
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                longitude = "" + location.getLongitude();
                latitude = "" + location.getLatitude();
                provider = location.getProvider();
                getWeatherData();
                Log.e("gps provider gps", location.getProvider());
                Log.e(TAG, longitude + " " + latitude);
                Toast.makeText(getApplicationContext(),
                        "Successfully requested location updates " + latitude + " " + longitude, Toast.LENGTH_SHORT).show();
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

        Log.e("network enabled", String.valueOf(isNetworkEnabled));
        Log.e("gps enabled", String.valueOf(isGPSEnabled));

        //check for permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                return;
            }
            if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Constants.LOCATION_INTERVAL, Constants.LOCATION_DISTANCE, locationListener);
            }
            if (isGPSEnabled) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Constants.LOCATION_INTERVAL, Constants.LOCATION_DISTANCE, locationListener);
            }
    }

    /*private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isSuccess = false;
            Log.e(TAG, "receiver");
            Log.e(TAG, intent.getAction());
            if(intent.getAction().equals(Constants.BROADCAST_DETECTED_ACTIVITY)) {
                int type = intent.getIntExtra("type", -1);
                int confidence = intent.getIntExtra("confidence", 0);
                Log.e(TAG, "detected activity " + String.valueOf(type));
                handleUserActivity(type, confidence);

            }
            /*if(intent.hasExtra(IS_SUCCESS)) {
                isSuccess = intent.getBooleanExtra(IS_SUCCESS, false);
            }*/
            //LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
            //jobFinished(mParams, !isSuccess);
    /*    }
    };*/

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
        //mFirebaseAnalytics.logEvent("user_activity", bundle);

        if (confidence > Constants.CONFIDENCE) {
            //txtActivity.setText(label);
            //txtConfidence.setText("Confidence: " + confidence);
            //imgActivity.setImageResource(icon);

            Toast.makeText(this, userActivity, Toast.LENGTH_LONG).show();
            checkContextForNotification();
            //setContextIcon(mainWeather, temperature.doubleValue(), humidity, relative_humidity, ambient_temperature, userActivity);
        }
    }*/


    private void getUserActivity() {
        //mActivityRecognitionClient = new ActivityRecognitionClient(this);
        //mIntentService = new Intent(this, DetectedActivitiesIntentService.class);
        /*LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter(Constants.BROADCAST_DETECTED_ACTIVITY));*/

        /*broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.e(TAG, "on receive");
                Log.e(TAG, intent.getAction());
                if (intent.getAction().equals(Constants.BROADCAST_DETECTED_ACTIVITY)) {
                    int type = intent.getIntExtra("type", -1);
                    int confidence = intent.getIntExtra("confidence", 0);
                    Log.e(TAG, String.valueOf(type));

                    //handleUserActivity(type, confidence);
                }
            }
        };
        startTracking();*/
        /*IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.BROADCAST_DETECTED_ACTIVITY);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, intentFilter);

        Intent intent = new Intent(this, BackgroundDetectedActivitiesService.class);
        startService(intent);*/
        //startService(new Intent(this, DetectedActivitiesIntentService.class));
    }

    /*private void startTracking() {
        Log.e("start tracking", "start");
        Intent intent1 = new Intent(this, BackgroundDetectedActivitiesService.class);
        startService(intent1);
    }*/


    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(Constants.CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void sendNotification(String contextDesscription, int icon) {
        Log.e("notfication", "send");

        int notificationId = 1;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                icon);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, Constants.CHANNEL_ID)
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
    }



}
