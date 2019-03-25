package com.example.alicenguyen.contextlock;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.hardware.fingerprint.FingerprintManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.alicenguyen.contextlock.andrognito.pinlockview.IndicatorDots;
import com.example.alicenguyen.contextlock.andrognito.pinlockview.PinLockListener;
import com.example.alicenguyen.contextlock.andrognito.pinlockview.PinLockView;
import com.example.alicenguyen.contextlock.fingerprint.FingerPrintListener;
import com.example.alicenguyen.contextlock.fingerprint.FingerprintHandler;
import com.example.alicenguyen.contextlock.util.Animate;
import com.example.alicenguyen.contextlock.util.Utils;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.DetectedActivity;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class Lockscreen extends AppCompatActivity {

    public static final String TAG = "Lockscreen";
    public static final int RESULT_TOO_MANY_TRIES = RESULT_FIRST_USER + 1;
    private static final int PIN_LENGTH = 4;
    private static final String FINGER_PRINT_KEY = "FingerPrintKey";
    public static final String EXTRA_SET_PIN = "set_pin";

    private static final String KEY_PIN = "pin";

    private static final int MAX_ATTEMPS = 3;
    //private static final int SURVEY_OPEN_NUMBER = 5;
    private static final String COOLDOWN_KEY = "cooldown_key";
    private static final String COUNTER_KEY = "counter_key";
    //private int survey_open_counter = 0;
    //private boolean cooldown = false;
    private String cooldown;
    private int opensurveycounter;


    private String userid;
    private Date current, switchDate;
    private String currentDate, switchVersionDate;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");


    private Cipher mCipher;
    private KeyStore mKeyStore;
    private KeyGenerator mKeyGenerator;
    private FingerprintManager.CryptoObject mCryptoObject;
    private FingerprintManager mFingerprintManager;
    private KeyguardManager mKeyguardManager;

    private boolean mSetPin = false;
    private String mFirstPin = "";
    private int mPinTryCount = 0;
    private int mFingerprintTryCount = 0;


    private TextView mTextTitle;
    private TextView mTextAttempts;
    private TextView mTextFingerText;
    private AppCompatImageView mImageViewFingerView;


    private IndicatorDots mIndicatorDots;
    private PinLockView mPinLockView;

    private ImageView mContextIcon;
    private ImageView settings;
    private ImageView showpinButton;

    private AnimatedVectorDrawable showFingerprint;
    private AnimatedVectorDrawable fingerprintToTick;
    private AnimatedVectorDrawable fingerprintToCross;

    private String mainWeather = "";
    private Number temperature = 0;
    private int humidity = 0;
    private String userActivity = "";


    protected LocationManager locationManager;
    protected LocationListener locationListener;

    private String OPEN_WEATHER_API_KEY = "18d997dfe947e33eb626ce588b9c7510";
    private String WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather?units=metric&lat="; //{lat}&lon={lon}"
    private String testURL= "http://api.openweathermap.org/data/2.5/weather?lat=48.366512&lon=10.894446&units=metric&appid=18d997dfe947e33eb626ce588b9c7510";

    private int LOCATION_ACCURACY = 20;
    private ImageView contextIcon;
    private ImageView fingerprintIcon;
    private PinLockView pinLockView;
    private TextView pinTitle;
    private TextView methodTitle;
    private float gpsAccuracy;

    private static final int LOCATION_INTERVAL = 10* 1000 * 60; // 1000 * 60 * 1; --> 1 minute// 5*60*1000
    private static final float LOCATION_DISTANCE = 10f; //10 meters

    //flag for gps status
    boolean isGPSEnabled = false;
    // flag for network status
    boolean isNetworkEnabled = false;


    private ActivityRecognitionClient mActivityRecognitionClient;
    private ActivityBroadcastReceiver mActivityBroadcastReceiver;
    private PendingIntent mPendingIntent;
    BroadcastReceiver broadcastReceiver;
    private boolean active;
    private boolean pinUsed;


    private FirebaseAnalytics mFirebaseAnalytics;




    public static Intent getIntent(Context context, boolean setPin) {
        Intent intent = new Intent(context, Lockscreen.class);
        intent.putExtra(EXTRA_SET_PIN, setPin);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lockscreen);

        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT )
        {
            getWindow().getDecorView().setSystemUiVisibility( View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                    | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY );
        }

        getPinlockViews();


        getUserLocation();
        /**startservice not for higher version**/
        //getUserActivity();
        //setContextIcon();
        //getLocationService();


        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);


        mSetPin = getIntent().getBooleanExtra(EXTRA_SET_PIN, false);

        //settings = (ImageView) findViewById(R.id.settings);

        //setSettingsDialog();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            showFingerprint = (AnimatedVectorDrawable) getDrawable(R.drawable.show_fingerprint);
            fingerprintToTick = (AnimatedVectorDrawable) getDrawable(R.drawable.fingerprint_to_tick);
            fingerprintToCross = (AnimatedVectorDrawable) getDrawable(R.drawable.fingerprint_to_cross);
        }

        if (mSetPin) {
            changeLayoutForSetPin();
        } else {
            String pin = getPinFromSharedPreferences();
            if (pin.equals("")) {
                changeLayoutForSetPin();
                mSetPin = true;
            } else {
                checkForFingerPrint();
            }
        }

        final PinLockListener pinLockListener = new PinLockListener() {

            @Override
            public void onComplete(String pin) {
                if (mSetPin) {
                    setPin(pin);
                } else {
                    checkPin(pin);
                }
            }

            @Override
            public void onEmpty() {
                Log.d(TAG, "Pin empty");
            }

            @Override
            public void onPinChange(int pinLength, String intermediatePin) {
                Log.d(TAG, "Pin changed, new length " + pinLength + " with intermediate pin " + intermediatePin);
            }
        };

        //mPinLockView   = findViewById(R.id.pinlockView);
        //mIndicatorDots = findViewById(R.id.indicator_dots);

        //startActivity(intent);
        mPinLockView.attachIndicatorDots(mIndicatorDots);
        mPinLockView.setPinLockListener(pinLockListener);

        mPinLockView.setPinLength(PIN_LENGTH);

        mIndicatorDots.setIndicatorType(IndicatorDots.IndicatorType.FILL_WITH_ANIMATION);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        //setDefaultView();

    }

    private void getPinlockViews() {
        mTextAttempts = (TextView) findViewById(R.id.attempts);
        mTextTitle = (TextView) findViewById(R.id.title);
        mIndicatorDots = (IndicatorDots) findViewById(R.id.indicator_dots);
        mImageViewFingerView = (AppCompatImageView) findViewById(R.id.fingerView);
        mTextFingerText = (TextView) findViewById(R.id.fingerText);
        mContextIcon = (ImageView) findViewById(R.id.context_icon);
        settings = (ImageView) findViewById(R.id.settings);
        mPinLockView   = findViewById(R.id.pinlockView);
        mIndicatorDots = findViewById(R.id.indicator_dots);
        contextIcon = findViewById(R.id.context_icon);
        fingerprintIcon = findViewById(R.id.fingerView);
        pinLockView = findViewById(R.id.pinlockView);
        pinTitle = findViewById(R.id.title);
        methodTitle = findViewById(R.id.fingerText);


        //showpinButton = findViewById(R.id.show_pin);
    }

    /*private void setSettingsDialog() {
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                mDialogBuilder.setCancelable(false);
                View mDialogView = getLayoutInflater().inflate(R.layout.dialog_settings, null);

                final EditText mUserId = (EditText) mDialogView.findViewById(R.id.setID);
                Button mSetPIN = (Button) mDialogView.findViewById(R.id.setPin);
                Button mCancelDialogButton = (Button) mDialogView.findViewById(R.id.cancel_dialog);
                Button mOKButton = (Button) mDialogView.findViewById(R.id.ok_dialog);
                Button mFeedbackButton= (Button) mDialogView.findViewById(R.id.open_survey);

                mDialogBuilder.setView(mDialogView);
                final AlertDialog dialog = mDialogBuilder.create();
                //set opacity of dialog
                //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white)));
                dialog.show();

                final String id = getIDfromSharedPreferences();
                Log.d("pref user id", id);
                if(!id.equals("")){
                    mUserId.setText(id, TextView.BufferType.EDITABLE);
                }

                mOKButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String userID = mUserId.getText().toString();
                        if(!userID.isEmpty()){
                            mFirebaseAnalytics.setUserId(userID);
                            writeIDtoSharedPreferences(userID);
                            dialog.cancel();

                        }else {
                            Toast.makeText(Lockscreen.this, "Please set a user id", Toast.LENGTH_LONG).show();
                        }
                    }
                });

                mCancelDialogButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                    }
                });

                mSetPIN.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // set pin instead of checking it
                        Intent intent = Lockscreen.getIntent(Lockscreen.this, true);
                        startActivity(intent);
                    }
                });

                mFeedbackButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                        startActivity(new Intent(Lockscreen.this, ExperienceSamplingActivity.class));
                    }
                });
            }
        });
    }*/

    /*private void writeIDtoSharedPreferences(String id) {
        SharedPreferences prefs = this.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_ID, id).apply();
    }

    public String getIDfromSharedPreferences(){
        SharedPreferences prefs = this.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE);
        return prefs.getString(KEY_ID, "");
    }*/


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(Lockscreen.this, "Permission denied to read your GPS location", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }
    //TODO
    private void handleUserLocation(String longitude, String latiude) {
    }

    /*private void getLocationService(){
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.e("on broadcast receive", "get location service");
                Log.e("broadcast", intent.getAction());
                if (intent.getAction().equals(Constants.BROADCAST_DETECTED_LOCATION)) {
                    String longitude = "" + intent.getDoubleExtra("longitude", 0);
                    String latitude = "" + intent.getDoubleExtra("latitude", 0);
                    Log.e("location service",intent.getExtras().get("longitude").toString());
                    Log.e("location service", longitude + " " + latitude);
                    //handleUserActivity(type, confidence);
                    handleUserLocation(longitude, latitude);
                }
            }
        };
        startTrackingLocation();
    }*/
    /*private void startTrackingLocation() {
        Intent locationIntent = new Intent(Lockscreen.this, LocationService.class);
        startService(locationIntent);
    }*/



    private void getUserActivity(){
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Constants.BROADCAST_DETECTED_ACTIVITY)) {
                    int type = intent.getIntExtra("type", -1);
                    int confidence = intent.getIntExtra("confidence", 0);
                    handleUserActivity(type, confidence);
                }
            }
        };
        startTracking();
    }

    private void handleUserActivity(int type, int confidence) {
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
        getUserLocation();
        if (confidence > Constants.CONFIDENCE) {
            //txtActivity.setText(label);
            //txtConfidence.setText("Confidence: " + confidence);
            //imgActivity.setImageResource(icon);

            //Toast.makeText(this, userActivity, Toast.LENGTH_LONG).show();
            setContextIcon();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(mActivityBroadcastReceiver,  new IntentFilter(Constants.BROADCAST_DETECTED_ACTIVITY));

        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);

        if(!keyguardManager.isDeviceLocked()){
            vibrate();
        }
        Log.e("Lockscreen", "lockscreen active");

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter(Constants.BROADCAST_DETECTED_ACTIVITY));


        /*LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter(Constants.BROADCAST_DETECTED_LOCATION));*/
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);

        activityManager.moveTaskToFront(getTaskId(), 0);


    }


    private void startTracking() {
        Intent intent1 = new Intent(Lockscreen.this, BackgroundDetectedActivitiesService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //startForegroundService(intent1);
        }else {
            startService(intent1);
        }
    }

    private void getUserLocation() {
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                String lon = "" + location.getLongitude();
                String lat = "" + location.getLatitude();

                gpsAccuracy = location.getAccuracy();

                Log.d("gps provider gps", location.getProvider());
                Log.e(TAG, lon);
                Log.e(TAG, lat);
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
            }else {
                setContextIcon();
            }
        }
    }

    private void getWeatherData(String longitude, String latitude) {
        RequestQueue queue = Volley.newRequestQueue(Lockscreen.this);
        String url = WEATHER_URL + latitude + "&lon=" + longitude + "&appid=" + OPEN_WEATHER_API_KEY;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e(TAG, response.toString());
                        try {
                            //Log.d("json humidity", response.getJSONObject("weather").toString());
                            JSONArray mainWeatherArray = response.getJSONArray("weather");
                            //JSONObject mainWeather = mainWeatherArray.getJSONObject(0);
                            //String main = mainWeather.getString("main");
                            Log.d(TAG, mainWeatherArray.getJSONObject(0).get("main").toString());
                            mainWeather = mainWeatherArray.getJSONObject(0).get("main").toString();
                            humidity = (int)response.getJSONObject("main").get("humidity");
                            temperature = (Number)response.getJSONObject("main").get("temp");
                            temperature = temperature.doubleValue();
                            setContextIcon();
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

    private boolean checkForOutdoor(){
        return isGPSEnabled;
    }

    private void setContextIcon() {

        if(mSetPin){
            return;
        }
        Log.e(TAG, "set icon");
        Log.e(TAG, mainWeather);
        Log.d("user context icon hum", String.valueOf(humidity));
        Log.e(TAG,"activity " + userActivity);
        int compareAccuracy = Float.compare(gpsAccuracy, LOCATION_ACCURACY);

        checkForOutdoor();
        contextIcon.setVisibility(View.VISIBLE);
        pinLockView.setVisibility(View.VISIBLE);
        //pinTitle.setText(R.string.pinlock_title);
        //methodTitle.setText(R.string.pinlock_pin);


       if(userActivity.equals("still") && mainWeather.contains("Drizzle")){
           Log.e(TAG, "tada");
            contextIcon.setImageResource(R.drawable.running);
            pinTitle.setText(getString(R.string.running) + " " + getString(R.string.pinlock_title));
            //methodTitle.setText(R.string.pinlock_pin);

       } else if(mainWeather.contains("Rain") && userActivity.equals(R.string.activity_walking)) {
            contextIcon.setImageResource(R.drawable.raindrop);
           pinTitle.setText(getString(R.string.rain) + " " + getString(R.string.pinlock_title));
           //methodTitle.setText(R.string.pinlock_pin);
            Log.d("mist", "it's wet outside!");
        }else if(mainWeather.contains("Drizzle")){ //Drizzle
           contextIcon.setImageResource(R.drawable.raindrop);
           pinTitle.setText(getString(R.string.running) + " " + getString(R.string.pinlock_title));
           //methodTitle.setText(R.string.pinlock_pin);
        }
        else if(mainWeather.contains("Snow")) { //compareAccuracy >= 0
           contextIcon.setImageResource(R.drawable.snowflake_white);
           pinTitle.setText(getString(R.string.snow) + " " + getString(R.string.pinlock_title));
           //methodTitle.setText(R.string.pinlock_pin);
        } else if(humidity > 75 && temperature.doubleValue() > 27.0) {
           contextIcon.setImageResource(R.drawable.raindrop);
           pinTitle.setText(getString(R.string.wet) + " " + getString(R.string.pinlock_title));
           //methodTitle.setText(R.string.pinlock_pin);
        }else if(temperature.doubleValue() > 27.0 ){
           contextIcon.setImageResource(R.drawable.humidity_white);
           pinTitle.setText(getString(R.string.muggy) + " " + getString(R.string.pinlock_title));
           //methodTitle.setText(R.string.pinlock_pin);
        } else{
            setRandomIcon();
        }
        setNotificationVersion();
        locationManager.removeUpdates(locationListener);
    }

    //TODO
    private void setRandomIcon() {
        Random generator = new Random();
        int number = generator.nextInt(2) + 1;
        switch (number) {
            case 1: //humdidity
                contextIcon.setImageResource(R.drawable.humidity_white);
                pinTitle.setText(getString(R.string.default_humidity) + " " + getString(R.string.pinlock_title));
                //methodTitle.setText(R.string.pinlock_pin);
                break;
            default: //movement
                contextIcon.setImageResource(R.drawable.running);
                pinTitle.setText(getString(R.string.default_movement) + " " + getString(R.string.pinlock_title));
                //methodTitle.setText(R.string.pinlock_pin);
                break;
        }

    }

    private void writeLockscreenDataToFirebase() {
        Log.e(TAG, "write to firebase");
        Date currenttime = Calendar.getInstance().getTime();
        String version = SharedPreferencesStorage.readSharedPreference(this, Constants.PREFERENCES, Constants.VERSION_KEY);

        SharedPreferences pref = this.getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE);
        String id = pref.getString(Constants.KEY_ID, "0");
        Log.e(TAG, userid);
        Log.e(TAG, currenttime.toString());
        Log.e(TAG, version);
        Log.e(TAG, pinTitle.getText().toString());
        Log.e(TAG, String.valueOf(pinUsed));

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("logEvents").child(version).child(id);
        ref.child(currenttime.toString()).child("reason").setValue(pinTitle.getText().toString());
        ref.child(currenttime.toString()).child("pin_used").setValue(pinUsed);
    }

    /*write weather data to local database*/
    private void writeLogEventsToDB() {
        SQLiteDB mDB = new SQLiteDB(this);
        Date currenttime = Calendar.getInstance().getTime();
        boolean isInserted = mDB.saveToDB(userid, currenttime.toString(), String.valueOf(pinUsed), pinTitle.getText().toString());
        if(isInserted == true) {
            Log.e(TAG, "insertedToDB");
        }else{
            Log.e(TAG, "failed to insert DB");
            //TODO send failed log to firebase

        }
    }


    /*handle notification version based on user id*/
    private void setNotificationVersion() {
        Log.e(TAG, "set version");
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE);
        userid = pref.getString(Constants.KEY_ID, "0");
        Date date = Calendar.getInstance().getTime();
        currentDate = simpleDateFormat.format(date);
        switchVersionDate = SharedPreferencesStorage.readSharedPreference(this, Constants.PREFERENCES, Constants.SWITCH_VERSION_KEY);
        Log.e(TAG, "version, userid");
        Log.e(TAG, userid);

        int id = Integer.parseInt(userid);
        try {
            current= simpleDateFormat.parse(currentDate);
            switchDate = simpleDateFormat.parse(switchVersionDate);

            Log.e(TAG, userid);
            Log.e(TAG, current.toString());
            Log.e(TAG, switchDate.toString());


            if(current.before(switchDate)){
                Log.e(TAG, current.toString());
                Log.e(TAG, switchDate.toString());
                if ((id % 2) == 0) {
                    // number is even
                    Log.e(TAG, "non condition notification");
                    SharedPreferencesStorage.writeSharedPreference(this, Constants.PREFERENCES, Constants.VERSION_KEY, Constants.VERSION_A );
                    contextIcon.setVisibility(View.INVISIBLE);
                    pinTitle.setText(getString(R.string.pinlock_title));

                    //setNonContextNotification();
                }
                else {
                    // number is odd
                    Log.e(TAG, "condition notification");

                    SharedPreferencesStorage.writeSharedPreference(this, Constants.PREFERENCES, Constants.VERSION_KEY, Constants.VERSION_B );
                    contextIcon.setVisibility(View.VISIBLE);
                    //setContextNotification();
                }
            }
            if(current.equals(switchDate) || current.after(switchDate)) {
                if ((id % 2) == 0) {
                    // number is even
                    SharedPreferencesStorage.writeSharedPreference(this, Constants.PREFERENCES, Constants.VERSION_KEY, Constants.VERSION_B );
                    contextIcon.setVisibility(View.VISIBLE);
                    //setContextNotification();
                }else {
                    // number is odd
                    SharedPreferencesStorage.writeSharedPreference(this, Constants.PREFERENCES, Constants.VERSION_KEY, Constants.VERSION_A );
                    contextIcon.setVisibility(View.INVISIBLE);
                    pinTitle.setText(R.string.pinlock_title);
                    //setNonContextNotification();
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Log.e(TAG, "error parse date");
        }
        //openSurvey();
    }



    private String getPinFromSharedPreferences() {
        SharedPreferences prefs = this.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE);
        return prefs.getString(KEY_PIN, "");
    }

    private void changeLayoutForSetPin() {
        mImageViewFingerView.setVisibility(View.GONE);
        mTextFingerText.setVisibility(View.GONE);
        mTextAttempts.setVisibility(View.GONE);
        mTextTitle.setText(getString(R.string.pinlock_settitle));
        mContextIcon.setVisibility(View.GONE);
        //mTextTitle.setText(getString(com.amirarcane.lockscreen.R.string.pinlock_settitle));
    }

    private void writePinToSharedPreferences(String pin) {
        SharedPreferences prefs = this.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_PIN, Utils.sha256(pin)).apply();
    }

    private void setPin(String pin) {
        if (mFirstPin.equals("")) {
            mFirstPin = pin;
            //mTextTitle.setText(getString(com.amirarcane.lockscreen.R.string.pinlock_secondPin));
            mTextTitle.setText(getString(R.string.pinlock_secondPin));
            mPinLockView.resetPinLockView();
        } else {
            if (pin.equals(mFirstPin)) {
                writePinToSharedPreferences(pin);
                setResult(RESULT_OK);
                finish();
                Bundle extras = new Bundle();
                extras.putString("setup_finished", "Setup finished!");
                Intent i = new Intent(this, SetupActivity.class);
                i.putExtras(extras);
                startActivity(i);
                Log.e(TAG, "setPin finished");
                //finish();
            } else {
                shake();
                //mTextTitle.setText(getString(com.amirarcane.lockscreen.R.string.pinlock_tryagain));
                mTextTitle.setText(getString(R.string.pinlock_tryagain));
                mPinLockView.resetPinLockView();
                mFirstPin = "";
            }
        }
    }

    private void writeCooldowntoSharedPreferences(boolean cooldown) {
        SharedPreferences prefs = this.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE);
        prefs.edit().putString(COOLDOWN_KEY, String.valueOf(cooldown));
    }

    public String getCooldownfromSharedPreferences(){
        SharedPreferences prefs = this.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE);
        return prefs.getString(COOLDOWN_KEY, "false");
    }

    private void writeSurveyOpenCountertoSharedPreferences(int opencounter) {
        SharedPreferences prefs = this.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE);
        prefs.edit().putString(COUNTER_KEY, String.valueOf(cooldown));
    }

    public String getSurveyOpenCounterfromSharedPreferences(){
        SharedPreferences prefs = this.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE);
        return prefs.getString(COUNTER_KEY, "0");
    }

    private void openExperienceSampling() {
        Random generator = new Random();
        int randomInt = generator.nextInt(Constants.SURVEY_COOLDOWN-0) + 0;
        Log.e("random", String.valueOf(randomInt));
        cooldown = SharedPreferencesStorage.readSharedPreference(this, Constants.PREFERENCES, Constants.COOLDOWN_KEY);
        Log.e("cooldown", String.valueOf(cooldown));
        //survey_open_counter = Integer.parseInt(getSurveyOpenCounterfromSharedPreferences());
        if(randomInt == 1) {
            //if(!cooldown.equals("true")) {
                opensurveycounter++;
                SharedPreferencesStorage.writeSharedPreference(this, Constants.PREFERENCES, Constants.COUNTER_KEY, String.valueOf(opensurveycounter));
                SharedPreferencesStorage.writeSharedPreference(this, Constants.PREFERENCES, Constants.COOLDOWN_KEY, "true");

                Log.e(TAG, "startActivity");
                Intent intent = new Intent(this, ExperienceSamplingActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            //}
        }
        if(randomInt == 0 /*&& cooldown.equals("true")*/) {
            SharedPreferencesStorage.writeSharedPreference(this, Constants.PREFERENCES, Constants.COOLDOWN_KEY, "false");
            finish();
        }
    }

    private void checkPin(String pin) {
        if (Utils.sha256(pin).equals(getPinFromSharedPreferences())) {
            setResult(RESULT_OK);
            pinUsed = true;
            writeLockscreenDataToFirebase();
            //writeLogEventsToDB();
            openExperienceSampling();
            finish();
        } else {
            shake();
            mPinTryCount++;
            mTextAttempts.setText(getString(R.string.pinlock_wrongpin));
            mPinLockView.resetPinLockView();

            if (mPinTryCount == 1) {
                mTextAttempts.setText(getString(R.string.pinlock_wrongpin));
                mPinLockView.resetPinLockView();
            } else if (mPinTryCount == 2) {
                mTextAttempts.setText(getString(R.string.pinlock_wrongpin));
                mPinLockView.resetPinLockView();
            } else if (mPinTryCount == MAX_ATTEMPS) {
                setResult(RESULT_TOO_MANY_TRIES);
                mPinTryCount = 0;
                mTextAttempts.setText("");
                //finish();
            }
        }
    }

    private void shake() {
        ObjectAnimator objectAnimator = new ObjectAnimator().ofFloat(mPinLockView, "translationX",
                0, 25, -25, 25, -25, 15, -15, 6, -6, 0).setDuration(1000);
        objectAnimator.start();
    }
    //Create the generateKey method that we’ll use to gain access to the Android keystore and generate the encryption key//
    private void generateKey() throws FingerprintException {
        try {
            // Obtain a reference to the Keystore using the standard Android keystore container identifier (“AndroidKeystore”)//
            mKeyStore = KeyStore.getInstance("AndroidKeyStore");

            //Generate the key//
            mKeyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

            //Initialize an empty KeyStore//
            mKeyStore.load(null);

            //Initialize the KeyGenerator//
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mKeyGenerator.init(new

                        //Specify the operation(s) this key can be used for//
                        KeyGenParameterSpec.Builder(FINGER_PRINT_KEY,
                        KeyProperties.PURPOSE_ENCRYPT |
                                KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_CBC)

                        //Configure this key so that the user has to confirm their identity with a fingerprint each time they want to use it//
                        .setUserAuthenticationRequired(true)
                        .setEncryptionPaddings(
                                KeyProperties.ENCRYPTION_PADDING_PKCS7)
                        .build());
            }
            //Generate the key//
            mKeyGenerator.generateKey();

        } catch (KeyStoreException
                | NoSuchAlgorithmException
                | NoSuchProviderException
                | InvalidAlgorithmParameterException
                | CertificateException
                | IOException exc) {
            throw new FingerprintException(exc);

        }
    }

    //Create a new method that we’ll use to initialize our mCipher//
    public boolean initCipher() {
        try {
            //Obtain a mCipher instance and configure it with the properties required for fingerprint authentication//
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mCipher = Cipher.getInstance(
                        KeyProperties.KEY_ALGORITHM_AES + "/"
                                + KeyProperties.BLOCK_MODE_CBC + "/"
                                + KeyProperties.ENCRYPTION_PADDING_PKCS7);
            }
        } catch (NoSuchAlgorithmException |
                NoSuchPaddingException e) {
            Log.e(TAG, "Failed to get Cipher");
            return false;
        }
        try {
            mKeyStore.load(null);
            SecretKey key = (SecretKey) mKeyStore.getKey(FINGER_PRINT_KEY,
                    null);
            mCipher.init(Cipher.ENCRYPT_MODE, key);
            //Return true if the mCipher has been initialized successfully//
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to init Cipher");
            return false;
        }
    }

    private void checkForFingerPrint() {
        Log.e(TAG, "checkForFingerprint");
        final FingerPrintListener fingerPrintListener = new FingerPrintListener() {

            @Override
            public void onSuccess() {
                Log.e(TAG, "fingerprint success");
                setResult(RESULT_OK);
                Animate.animate(mImageViewFingerView, fingerprintToTick);
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pinUsed = false;
                        //writeLogEventsToDB();
                        finish();
                        openExperienceSampling();
                    }
                }, 750);

            }

            @Override
            public void onFailed() {

                //mFingerprintTryCount++;
                Log.e(TAG, "fingerprint failed");
                Animate.animate(mImageViewFingerView, fingerprintToCross);
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Animate.animate(mImageViewFingerView, showFingerprint);
                    }
                }, 750);
            }

            @Override
            public void onError(CharSequence errorString) {
               /* Bundle bundle = new Bundle();
                bundle.putString("fingerprint_error", errorString.toString());
                mFirebaseAnalytics.logEvent("fingerprint_error", bundle);*/
               Log.e(TAG, "fingerprint error");

                Toast.makeText(Lockscreen.this, errorString, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onHelp(CharSequence helpString) {
                Bundle bundle = new Bundle();
                bundle.putString("fingerprint_help", helpString.toString());
                mFirebaseAnalytics.logEvent("fingerprint_help", bundle);
                Log.e(TAG, "fingerprint help");
                Toast.makeText(Lockscreen.this, helpString, Toast.LENGTH_SHORT).show();
            }

        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            FingerprintManager fingerprintManager = (FingerprintManager) getSystemService(Context.FINGERPRINT_SERVICE);
            if (fingerprintManager.isHardwareDetected()) {
                //Get an instance of KeyguardManager and FingerprintManager//
                mKeyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
                mFingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);

                //Check whether the user has granted your app the USE_FINGERPRINT permission//
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT)
                        != PackageManager.PERMISSION_GRANTED) {
                    // If your app doesn't have this permission, then display the following text//
//                Toast.makeText(EnterPinActivity.this, "Please enable the fingerprint permission", Toast.LENGTH_LONG).show();
                    mImageViewFingerView.setVisibility(View.GONE);
//                    mTextFingerText.setVisibility(View.GONE);
                    return;
                }

                //Check that the user has registered at least one fingerprint//
                if (!mFingerprintManager.hasEnrolledFingerprints()) {
                    // If the user hasn’t configured any fingerprints, then display the following message//
//                Toast.makeText(EnterPinActivity.this,
//                        "No fingerprint configured. Please register at least one fingerprint in your device's Settings",
//                        Toast.LENGTH_LONG).show();
                    mImageViewFingerView.setVisibility(View.GONE);
//                    mTextFingerText.setVisibility(View.GONE);
                    return;
                }

                //Check that the lockscreen is secured//
                if (!mKeyguardManager.isKeyguardSecure()) {
                    // If the user hasn’t secured their lockscreen with a PIN password or pattern, then display the following text//
//                Toast.makeText(EnterPinActivity.this, "Please enable lockscreen security in your device's Settings", Toast.LENGTH_LONG).show();
                    mImageViewFingerView.setVisibility(View.GONE);
//                    mTextFingerText.setVisibility(View.GONE);
                    return;
                } else {
                    try {
                        generateKey();
                        if (initCipher()) {
                            //If the mCipher is initialized successfully, then create a CryptoObject instance//
                            mCryptoObject = new FingerprintManager.CryptoObject(mCipher);

                            // Here, I’m referencing the FingerprintHandler class that we’ll create in the next section. This class will be responsible
                            // for starting the authentication process (via the startAuth method) and processing the authentication process events//
                            FingerprintHandler helper = new FingerprintHandler(this);
                            helper.startAuth(mFingerprintManager, mCryptoObject);
                            helper.setFingerPrintListener(fingerPrintListener);
                        }
                    } catch (FingerprintException e) {
                        Log.wtf(TAG, "Failed to generate key for fingerprint.", e);
                    }
                }
            } else {
                mImageViewFingerView.setVisibility(View.GONE);
//                mTextFingerText.setVisibility(View.GONE);
            }
        } else {
            mImageViewFingerView.setVisibility(View.GONE);
//            mTextFingerText.setVisibility(View.GONE);
        }
    }

    private void vibrate() {
        if(!mSetPin){
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            // Vibrate for 500 milliseconds
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                //deprecated in API 26
                v.vibrate(100);
            }
        }
    }


    private class FingerprintException extends Exception {
        public FingerprintException(Exception e) {
            super(e);
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        active = true;
        /*KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);

        if(!keyguardManager.isDeviceLocked()){
            vibrate();
        }

        Log.e("Lockscreen", "lockscreen active");*/
    }

    @Override
    public void onStop() {
        super.onStop();
        active = false;
    }


    //disable back button
    @Override
    public void onBackPressed() {
        if (!mSetPin) {
        } else {
            super.onBackPressed();
        }
    }
}

