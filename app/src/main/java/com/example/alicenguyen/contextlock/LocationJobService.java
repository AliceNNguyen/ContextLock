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
import android.content.SharedPreferences;
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
import com.google.firebase.analytics.FirebaseAnalytics;
//import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;

public class LocationJobService extends JobService {
    private static final String TAG = "LocationJobService";
    private boolean jobCancelled = false;

    private FirebaseAnalytics mFirebaseAnalytics;

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
    private String city = "";
    private Number temperature = 0;
    private int humidity = 0;
    //private String userActivity = "";
    private String provider, latitude, longitude;
    private LockScreenReceiver lockScreenReceiver = new LockScreenReceiver();
    private boolean isLocked = false;
    private int opensurveycounter;
    private SharedPreferencesStorage sharedPreferencesStorage;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
    private String currentDate;




    public LocationJobService() {
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.e(TAG, "job started");
        createNotificationChannel();
        Date date = Calendar.getInstance().getTime();
        String currentDate = simpleDateFormat.format(date);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        //SharedPreferencesStorage.writeSharedPreference(this, Constants.PREFERENCES, Constants.DATE_KEY, currentDate);
        String storedDate = SharedPreferencesStorage.readSharedPreference(this, Constants.PREFERENCES, Constants.DATE_KEY);

        if(currentDate.equals(storedDate)) {
            String counter = SharedPreferencesStorage.readSharedPreference(this, Constants.PREFERENCES, Constants.COUNTER_KEY);
            opensurveycounter = Integer.parseInt(counter);
        }else {
            getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE).edit().remove(Constants.COUNTER_KEY).apply();
            String counter = SharedPreferencesStorage.readSharedPreference(this, Constants.PREFERENCES, Constants.COUNTER_KEY);
            opensurveycounter = Integer.parseInt(counter);
            SharedPreferencesStorage.writeSharedPreference(this, Constants.PREFERENCES, Constants.DATE_KEY, currentDate);
        }
        doBackgroundWork(params);

        Log.e(TAG, "counter " + opensurveycounter);
        Log.e(TAG, currentDate);
        Log.e(TAG, storedDate);

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

    private void openExperienceSampling() {
        opensurveycounter++;
        SharedPreferencesStorage.writeSharedPreference(this, Constants.PREFERENCES, Constants.COUNTER_KEY, String.valueOf(opensurveycounter));
        Intent intent = new Intent(this, ExperienceSamplingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    private void checkContextForNotification() {
        Log.d("weather description", mainWeather);
        Log.d("gps enabled", String.valueOf(isGPSEnabled));
        checkIfScreenLocked();
        Log.e(TAG, "check counter" + opensurveycounter);

        if(isLocked && opensurveycounter < 6) {
            Log.e(TAG, String.valueOf(isLocked));
            if(mainWeather.contains("Rain") && provider.equals("gps")) {
                sendNotification(getString(R.string.rain), R.mipmap.raindrop_ic);
                openExperienceSampling();
            }else if(mainWeather.contains("Snow") && provider.equals("gps")) {
                sendNotification(getString(R.string.snow), R.mipmap.snow_ic);
                openExperienceSampling();
            }else if(mainWeather.contains("Drizzle") && provider.equals("gps")){
                sendNotification(getString(R.string.rain), R.mipmap.raindrop_ic);
                openExperienceSampling();
            }else {
                Log.e(TAG, "Alles gut");
                Bundle params = new Bundle();
                params.putString("user_weather", "no notification conditions" );
                mFirebaseAnalytics.logEvent("user_activity_notification", params);
                //Toast.makeText(this, "Alles gut!", Toast.LENGTH_LONG).show();
            }


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
                            /*city = response.getJSONObject("name").toString();
                            Log.d(TAG, "city");
                            Log.d(TAG, city);*/
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

    private void sendNotification(String contextDescription, int icon) {
        Log.e("notfication", "send");

        int notificationId = 1;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                icon);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, Constants.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_fingerprint)
                .setLargeIcon(bitmap)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(contextDescription +  " " + getString(R.string.notification_description))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(contextDescription + " " +  getString(R.string.notification_description)))
                //.setStyle(new NotificationCompat.BigPictureStyle()
                //.bigPicture(bitmap).setSummaryText("message"))
                // Set the intent that will fire when the user taps the notification
                //.setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(notificationId, mBuilder.build());

        Bundle params = new Bundle();
        params.putString("weather", contextDescription );
        mFirebaseAnalytics.logEvent("user_location_notification", params);
    }

    private void setAlarmManagerForNotification() {

    }



}
