package com.example.alicenguyen.contextlock;

import android.Manifest;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;


public class LocationJobService extends JobService {
    private static final String TAG = "LocationJobService";
    private boolean jobCancelled = false;
    private boolean isGPSEnabled = false;
    private boolean isNetworkEnabled = false;

    private LocationManager locationManager;
    private LocationListener locationListener;

    /*URL and API key to receive weather data*/
    private String OPEN_WEATHER_API_KEY = "18d997dfe947e33eb626ce588b9c7510";
    private String WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather?units=metric&lat=";

    private String mainWeather = "";
    private Number temperature = 0;
    private int humidity = 0;
    private String provider, latitude, longitude;
    private boolean isLocked = false;
    private int opensurveycounter;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
    private String currentDate, switchVersionDate;
    private String cooldown;
    private String userid;
    private Date current, switchDate;
    private String message;
    private int icon;
    private int notificationSendCounter = 0;



    public LocationJobService() {
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.e(TAG, "job started");
        //createNotificationChannel();
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE);
        userid = pref.getString(Constants.KEY_ID, "no id");
        Date date = Calendar.getInstance().getTime();
        currentDate = simpleDateFormat.format(date);
        switchVersionDate = SharedPreferencesStorage.readSharedPreference(this, Constants.PREFERENCES, Constants.SWITCH_VERSION_KEY);

        notificationSendCounter = Integer.parseInt(SharedPreferencesStorage.readSharedPreference(this, Constants.PREFERENCES, Constants.NOTIFICATION_SEND_KEY));
        Log.e(TAG, currentDate);
        Log.e(TAG, switchVersionDate);
        doBackgroundWork(params);
        Log.e(TAG, currentDate);
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
        Log.d(TAG, "Job finished");
        jobFinished(params, false);
    }

    private void openSurvey() {
        Intent intent = new Intent(this, ExperienceSamplingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    private void openExperienceSampling() {
        Random generator = new Random();
        int randomInt = generator.nextInt(2-0) + 0;
        Log.d("random", String.valueOf(randomInt));

        cooldown = SharedPreferencesStorage.readSharedPreference(this, Constants.PREFERENCES, Constants.COOLDOWN_KEY);

        Log.e("cooldown", String.valueOf(cooldown));
        if(randomInt == 1) {
            if(!cooldown.equals("true")) {
                opensurveycounter++;
                SharedPreferencesStorage.writeSharedPreference(this, Constants.PREFERENCES, Constants.COUNTER_KEY, String.valueOf(opensurveycounter));
                SharedPreferencesStorage.writeSharedPreference(this, Constants.PREFERENCES, Constants.COOLDOWN_KEY, "true");

                Intent intent = new Intent(this, ExperienceSamplingActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
        if(randomInt == 0 && cooldown.equals("true")) {
            SharedPreferencesStorage.writeSharedPreference(this, Constants.PREFERENCES, Constants.COOLDOWN_KEY, "false");
        }
    }

    private void setContextNotification() {
        Log.e(TAG, "send");
        //setNotificationMessage();
        notificationSendCounter++;
        Log.e(TAG, String.valueOf(notificationSendCounter));
        SharedPreferencesStorage.writeSharedPreference(this, Constants.PREFERENCES, Constants.NOTIFICATION_SEND_KEY, String.valueOf(notificationSendCounter));

        NotificationHelper notificationHelper = new NotificationHelper(this);
        NotificationCompat.Builder nb = notificationHelper.getChannelNotification(icon, message);
        notificationHelper.getManager().notify(Constants.NOTIFICATION_ID, nb.build());
    }

    private void setNonContextNotification() {
        Log.e(TAG, "send");
        notificationSendCounter++;
        Log.e(TAG, String.valueOf(notificationSendCounter));
        SharedPreferencesStorage.writeSharedPreference(this, Constants.PREFERENCES, Constants.NOTIFICATION_SEND_KEY, String.valueOf(notificationSendCounter));

        NotificationHelper notificationHelper = new NotificationHelper(this);
        NotificationCompat.Builder nb = notificationHelper.getChannelNotification(R.mipmap.fingerprint_ic, getString(R.string.default_message));
        notificationHelper.getManager().notify(Constants.NOTIFICATION_ID, nb.build());
    }


    /*notification version is handled by user id:
    if user id is even: start with non-context notification than switch to context notification when first part of study duration is over
    if user id is uneven: start with context notification than switch to non-context notification
    */
   private void setNotificationVersion () {
       int id = Integer.parseInt(userid);
       Log.e(TAG, String.valueOf(id));
       try {
           current= simpleDateFormat.parse(currentDate);
           switchDate = simpleDateFormat.parse(switchVersionDate);

           Log.e(TAG, current.toString());
           Log.e(TAG, switchDate.toString());

           if(current.before(switchDate)){
               Log.e(TAG, current.toString());
               Log.e(TAG, switchDate.toString());
               if ((id % 2) == 0) {
                   // number is even
                   Log.e(TAG, "non condition notification");
                   SharedPreferencesStorage.writeSharedPreference(this, Constants.PREFERENCES, Constants.VERSION_KEY, Constants.VERSION_A);
                   setNonContextNotification();
               }
               else {
                   // number is odd
                   Log.e(TAG, "condition notification");
                   SharedPreferencesStorage.writeSharedPreference(this, Constants.PREFERENCES, Constants.VERSION_KEY, Constants.VERSION_B );
                   setContextNotification();
               }
           }
           if(current.after(switchDate)) {
               if ((id % 2) == 0) {
                   // number is even
                   SharedPreferencesStorage.writeSharedPreference(this, Constants.PREFERENCES, Constants.VERSION_KEY, Constants.VERSION_B );
                   setContextNotification();
               }
               else {
                   // number is odd
                   SharedPreferencesStorage.writeSharedPreference(this, Constants.PREFERENCES, Constants.VERSION_KEY, Constants.VERSION_A );
                   setNonContextNotification();
               }
           }
       } catch (ParseException e) {
           e.printStackTrace();
           Log.e(TAG, "error parse date");
       }
       //saveToDB();
       //openExperienceSampling();
       openSurvey();
   }

   /*write weather data to local database*/
    private void writeLogEventsToDB() {
        LocalDatabase mDb = new LocalDatabase(this);
        Date currenttime = Calendar.getInstance().getTime();
        Log.e(TAG, "send is locked: " + String.valueOf(isLocked));
        boolean isInserted = mDb.saveToDB(userid, currenttime.toString(), "", mainWeather, String.valueOf(isLocked));
        if(isInserted == true) {
            Log.e(TAG, "insertedToDB");
        }else{
            Log.e(TAG, "failed to insert DB");
            //TODO send failed log to firebase
        }
    }

    /*notification will be send only if lock screen is enabled*/
    private void checkContextForNotification() {
        Log.e("weather description", mainWeather);
        Log.d("gps enabled", String.valueOf(isGPSEnabled));
        checkIfScreenLocked();
        Log.e(TAG, String.valueOf(isLocked));

        if(isLocked && notificationSendCounter < Constants.NOTIFICATION_SEND_MAX_NUMBER) {
            Log.e(TAG, String.valueOf(isLocked));
            if(mainWeather.contains("Rain") /*&& provider.equals("gps")*/) {
                //sendNotification(getString(R.string.rain), R.mipmap.raindrop_ic);
                message = getString(R.string.rain);
                icon = R.mipmap.raindrop_ic;
                setNotificationVersion();

                //openExperienceSampling();
            }else if(mainWeather.contains("Snow")) {
                //sendNotification(getString(R.string.snow), R.mipmap.snow_ic);
                message = getString(R.string.snow);
                icon = R.mipmap.snow_ic;
                setNotificationVersion();

                //openExperienceSampling();
            }else if(mainWeather.contains("Drizzle") /*&& provider.equals("gps")*/) {
                //sendNotification(getString(R.string.rain), R.mipmap.raindrop_ic);
                message = getString(R.string.rain);
                icon = R.mipmap.raindrop_ic;
                setNotificationVersion();

                //openExperienceSampling();
            }else if(humidity > 75 && temperature.intValue() > 27){
                //sendNotification(getString(R.string.humdidity), R.mipmap.humidity_ic);
                message = getString(R.string.humdidity);
                icon = R.mipmap.humidity_ic;
                setNotificationVersion();
            }else {
                Log.e(TAG, "Alles gut");
            }
        }
        writeLogEventsToDB();
    }

    /*check if screen is locked to send notification*/
    private void checkIfScreenLocked() {
        KeyguardManager myKM = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        if( myKM.isKeyguardLocked()) {
            isLocked = true;
            Log.e(TAG, "device is locked");
        } else {
            isLocked = false;
            Log.e(TAG, "device not locked");
        }
    }

    /*retrieve weather from OpenweatherMap API (https://openweathermap.org/api) from detected latitude and longitude*/
    private void getWeatherData() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = WEATHER_URL + latitude + "&lon=" + longitude + "&appid=" + OPEN_WEATHER_API_KEY;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("response json", response.toString());
                        try {
                            JSONArray mainWeatherArray = response.getJSONArray("weather");
                            Log.e("main weather main", mainWeatherArray.getJSONObject(0).get("main").toString());
                            mainWeather = mainWeatherArray.getJSONObject(0).get("main").toString();
                            humidity = (int)response.getJSONObject("main").get("humidity");
                            temperature = (Number)response.getJSONObject("main").get("temp");
                            temperature = temperature.doubleValue();
                            checkContextForNotification();
                        } catch (JSONException e) {
                            Log.d("json exception","json exception log");
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Log.e(TAG, "api weather error");
                        //Toast toast = Toast.makeText(MainActivity.this, "no response", Toast.LENGTH_LONG);
                        //toast.show();
                    }
                });
        queue.add(jsonObjectRequest);
    }

    /*get latitude and longitude to retrieve weather data if GPS permission in enabled*/
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
            }
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.e(TAG, provider);
                Log.e(TAG,  String.valueOf(status));
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
                return;
            }
            if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Constants.LOCATION_INTERVAL, Constants.LOCATION_DISTANCE, locationListener);
            }
            if (isGPSEnabled) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Constants.LOCATION_INTERVAL, Constants.LOCATION_DISTANCE, locationListener);
            }
    }
}
