package com.example.alicenguyen.contextlock;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
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
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
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
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.alicenguyen.contextlock.andrognito.pinlockview.IndicatorDots;
import com.example.alicenguyen.contextlock.andrognito.pinlockview.PinLockListener;
import com.example.alicenguyen.contextlock.andrognito.pinlockview.PinLockView;
import com.example.alicenguyen.contextlock.experience_sampling.ExperienceSamplingActivity;
import com.example.alicenguyen.contextlock.fingerprint.FingerPrintListener;
import com.example.alicenguyen.contextlock.fingerprint.FingerprintHandler;
import com.example.alicenguyen.contextlock.util.Animate;
import com.example.alicenguyen.contextlock.util.Utils;

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
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

/*pin lock screen
 * it also handles the lock screen version
 * main logic for pin and fingerprint functionality based on amirarcane (https://github.com/amirarcane/lock-screen) and are partly
 * modified for own purpose */
public class Lockscreen extends AppCompatActivity {

    public static final String TAG = "Lockscreen";
    public static final int RESULT_TOO_MANY_TRIES = RESULT_FIRST_USER + 1;
    private static final int PIN_LENGTH = 4;
    private static final String FINGER_PRINT_KEY = "FingerPrintKey";
    public static final String EXTRA_SET_PIN = "set_pin";

    private static final String KEY_PIN = "pin";

    private static final int MAX_ATTEMPS = 3;
    private static final String COOLDOWN_KEY = "cooldown_key";
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
    private TextView mTextAttempts;
    private TextView mTextFingerText;
    private AppCompatImageView mImageViewFingerView;


    private IndicatorDots mIndicatorDots;
    private PinLockView mPinLockView;

    private AnimatedVectorDrawable showFingerprint;
    private AnimatedVectorDrawable fingerprintToTick;
    private AnimatedVectorDrawable fingerprintToCross;

    private String mainWeather = "";
    private Number temperature = 0;
    private int humidity = 0;
    private String userActivity = "";


    protected LocationManager locationManager;
    protected LocationListener locationListener;

    /*credentials for OpenWeatherMap API*/
    private String OPEN_WEATHER_API_KEY = "18d997dfe947e33eb626ce588b9c7510";
    private String WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather?units=metric&lat="; //{lat}&lon={lon}"

    private ImageView contextIcon;
    private TextView pinTitle;

    //flag for gps status
    boolean isGPSEnabled = false;
    // flag for network status
    boolean isNetworkEnabled = false;

    BroadcastReceiver broadcastReceiver;

    private FirebaseAnalytics mFirebaseAnalytics;
    private FingerprintHandler helper;


    public static Intent getIntent(Context context, boolean setPin) {
        Intent intent = new Intent(context, Lockscreen.class);
        intent.putExtra(EXTRA_SET_PIN, setPin);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lockscreen);

        hideOnScreenButon();
        getPinlockViews();
        getUserLocation();
        getUserActivity();

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mSetPin = getIntent().getBooleanExtra(EXTRA_SET_PIN, false);

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
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                Log.e("tag", "This'll run 2 seconds later");
                                PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                                if(pm.isInteractive()) {
                                    Log.e(TAG, "is interactive");
                                    checkForFingerPrint();
                                }
                            }
                        },
                Constants.CHECK_FINGERPRINT_TIMEOUT);
            }
        }

        /*handles pin input: handles or check it*/
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

        mPinLockView.attachIndicatorDots(mIndicatorDots);
        mPinLockView.setPinLockListener(pinLockListener);
        mPinLockView.setPinLength(PIN_LENGTH);
        mIndicatorDots.setIndicatorType(IndicatorDots.IndicatorType.FILL_WITH_ANIMATION);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    private void hideOnScreenButon() {
        getWindow().getDecorView().setSystemUiVisibility( View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY );
    }

    /*get layout elements*/
    private void getPinlockViews() {
        mTextAttempts =  findViewById(R.id.attempts);
        mIndicatorDots = findViewById(R.id.indicator_dots);
        mImageViewFingerView = findViewById(R.id.fingerView);
        mTextFingerText = findViewById(R.id.fingerText);
        mPinLockView   = findViewById(R.id.pinlockView);
        mIndicatorDots = findViewById(R.id.indicator_dots);
        contextIcon = findViewById(R.id.context_icon);
        pinTitle = findViewById(R.id.title);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                //permission granted
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    // permission denied
                    // functionality that depends on this permission.
                    Toast.makeText(Lockscreen.this, "Permission denied to read your GPS location", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

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

    private void startTracking() {
        Intent intent1 = new Intent(Lockscreen.this, BackgroundDetectedActivitiesService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent1);
        }else {
            startService(intent1);
        }
    }

    private void stopTracking() {
        Intent serviceIntent = new Intent(this, BackgroundDetectedActivitiesService.class);
        stopService(serviceIntent);
        //Toast.makeText(this, "tracking stopped", Toast.LENGTH_LONG);
        Log.e(TAG, "stopTracking");
    }

    private void handleUserActivity(int type, int confidence) {
        userActivity = getString(R.string.activity_unknown);
        switch (type) {
            case DetectedActivity.IN_VEHICLE: {
                userActivity = getString(R.string.activity_in_vehicle);
                break;
            }
            case DetectedActivity.ON_BICYCLE: {
                userActivity = getString(R.string.activity_on_bicycle);
                break;
            }
            case DetectedActivity.ON_FOOT: {
                userActivity = getString(R.string.activity_on_foot);
                break;
            }
            case DetectedActivity.RUNNING: {
                userActivity = getString(R.string.activity_running);
                break;
            }
            case DetectedActivity.STILL: {
                userActivity = getString(R.string.activity_still);
                break;
            }
            case DetectedActivity.TILTING: {
                userActivity = getString(R.string.activity_tilting);
                break;
            }
            case DetectedActivity.WALKING: {
                userActivity = getString(R.string.activity_walking);
                break;
            }
            case DetectedActivity.UNKNOWN: {
                userActivity = getString(R.string.activity_unknown);
                break;
            }
        }
        Log.e(TAG, "User activity: " + userActivity + ", Confidence: " + confidence);
        getUserLocation();
        if (confidence > Constants.CONFIDENCE) {
            setContextIcon();
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

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter(Constants.BROADCAST_DETECTED_ACTIVITY));

        /*set vibration is lock screen is visible*/
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if(pm.isInteractive()) {
            Log.e(TAG, "isInteractive");
            vibrate();
        }
    }


    /*get current user's position to retrieve weather data*/
    private void getUserLocation() {
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                String lon = "" + location.getLongitude();
                String lat = "" + location.getLatitude();
                Log.e(TAG, "onLocationChanged");
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

        Log.e("network enabled", String.valueOf(isNetworkEnabled));
        Log.e("gps enabled", String.valueOf(isGPSEnabled));

        //check for permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
            Toast.makeText(this, "Not Enough Permission", Toast.LENGTH_SHORT).show();
            return;
        }else{ //if permission enabled
            if(isNetworkEnabled) {
                Log.e(TAG, "network enabled request");
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Constants.LOCATION_INTERVAL, Constants.LOCATION_DISTANCE, locationListener);
            }
            if(isGPSEnabled)  {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Constants.LOCATION_INTERVAL, Constants.LOCATION_DISTANCE, locationListener);
            }else {
                setContextIcon();
            }
        }
    }

    /*retrieve weather data from OpenWeatherMap API*/
    private void getWeatherData(String longitude, String latitude) {
        Log.e(TAG, "getWeatherdata");
        RequestQueue queue = Volley.newRequestQueue(Lockscreen.this);
        String url = WEATHER_URL + latitude + "&lon=" + longitude + "&appid=" + OPEN_WEATHER_API_KEY;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e(TAG, response.toString());
                        try {
                            JSONArray mainWeatherArray = response.getJSONArray("weather");
                            mainWeather = mainWeatherArray.getJSONObject(0).get("main").toString();
                            Log.e(TAG, mainWeather);
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
                    }
                });
        queue.add(jsonObjectRequest);
    }

    /*set context based icon and text based on retrieved data or random data*/
    private void setContextIcon() {
        if(mSetPin){
            return;
        }
        Log.e(TAG, "set icon");
        Log.e(TAG, "weather " + mainWeather);
        Log.d("user context icon hum", String.valueOf(humidity));
        Log.e(TAG,"activity " + userActivity);


        contextIcon.setVisibility(View.VISIBLE);
        mPinLockView.setVisibility(View.VISIBLE);

       if(userActivity.equals(getString(R.string.activity_walking)) && mainWeather.contains("Drizzle")){ //Drizzle
           Log.e(TAG, "tada");
            contextIcon.setImageResource(R.drawable.raindrop);
            pinTitle.setText(getString(R.string.rain) + " " + getString(R.string.pinlock_title));
       } else if(mainWeather.contains("Rain") && userActivity.equals(R.string.activity_walking)) {
            contextIcon.setImageResource(R.drawable.raindrop);
           pinTitle.setText(getString(R.string.rain) + " " + getString(R.string.pinlock_title));
            Log.d("mist", "it's wet outside!");
        }else if(mainWeather.contains("Drizzle")){ //Drizzle
           contextIcon.setImageResource(R.drawable.raindrop);
           pinTitle.setText(getString(R.string.rain) + " " + getString(R.string.pinlock_title));
        }
        else if(mainWeather.contains("Snow")) { //compareAccuracy >= 0
           contextIcon.setImageResource(R.drawable.snowflake_white);
           pinTitle.setText(getString(R.string.snow) + " " + getString(R.string.pinlock_title));
        }else if(userActivity.equals(getString(R.string.activity_in_vehicle))){
           contextIcon.setImageResource(R.drawable.public_transportation);
           pinTitle.setText(getString(R.string.default_movement) + " " + getString(R.string.pinlock_title));

        } else if(humidity > 75 && temperature.doubleValue() > 27.0) {
           contextIcon.setImageResource(R.drawable.raindrop);
           pinTitle.setText(getString(R.string.wet) + " " + getString(R.string.pinlock_title));
        }else if(temperature.doubleValue() > 27.0 ){
           contextIcon.setImageResource(R.drawable.humidity_white);
           pinTitle.setText(getString(R.string.muggy) + " " + getString(R.string.pinlock_title));
        } else{
            setRandomIcon();
        }
        setNotificationVersion();
        //locationManager.removeUpdates(locationListener);
    }

    //TODO
    private void setRandomIcon() {
        Random generator = new Random();
        int number = generator.nextInt(2) + 1;
        switch (number) {
            case 1: //humdidity
                contextIcon.setImageResource(R.drawable.humidity_white);
                pinTitle.setText(getString(R.string.default_humidity) + " " + getString(R.string.pinlock_title));
                break;
            default: //movement
                contextIcon.setImageResource(R.drawable.running);
                pinTitle.setText(getString(R.string.default_movement) + " " + getString(R.string.pinlock_title));
                break;
        }
    }

    /*write lock screen data to firebase
    * parameters are: if user used pin and displayed text*/
    private void writeLockscreenDataToFirebase(boolean pinUsed) {
        Log.e(TAG, "write to firebase");
        Date currenttime = Calendar.getInstance().getTime();
        String version = SharedPreferencesStorage.readSharedPreference(this, Constants.PREFERENCES, Constants.VERSION_KEY);
        SharedPreferences pref = this.getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE);
        String id = pref.getString(Constants.KEY_ID, "0");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("logEvents").child(version).child(id);
        ref.child(currenttime.toString()).child("reason").setValue(pinTitle.getText().toString());
        ref.child(currenttime.toString()).child("pin_used").setValue(pinUsed);
    }



    /*handle lock screen version based on user id and version switch date
    * user with even id number starts with non-context version
    * user with uneven id number starts with context version
    * after first half of study, versions are vice versa*/
    private void setNotificationVersion() {
        Log.e(TAG, "set version");
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE);
        userid = pref.getString(Constants.KEY_ID, "0");
        Date date = Calendar.getInstance().getTime();
        currentDate = simpleDateFormat.format(date);
        switchVersionDate = SharedPreferencesStorage.readSharedPreference(this, Constants.PREFERENCES, Constants.SWITCH_VERSION_KEY);
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
                }
                else {
                    // number is odd
                    Log.e(TAG, "condition notification");
                    SharedPreferencesStorage.writeSharedPreference(this, Constants.PREFERENCES, Constants.VERSION_KEY, Constants.VERSION_B );
                    contextIcon.setVisibility(View.VISIBLE);
                }
            }
            if(current.equals(switchDate) || current.after(switchDate)) {
                if ((id % 2) == 0) {
                    // number is even
                    SharedPreferencesStorage.writeSharedPreference(this, Constants.PREFERENCES, Constants.VERSION_KEY, Constants.VERSION_B );
                    contextIcon.setVisibility(View.VISIBLE);
                }else {
                    // number is odd
                    SharedPreferencesStorage.writeSharedPreference(this, Constants.PREFERENCES, Constants.VERSION_KEY, Constants.VERSION_A );
                    contextIcon.setVisibility(View.INVISIBLE);
                    pinTitle.setText(R.string.pinlock_title);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Log.e(TAG, "error parse date");
        }
    }


    private String getPinFromSharedPreferences() {
        SharedPreferences prefs = this.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE);
        return prefs.getString(KEY_PIN, "");
    }

    private void writePinToSharedPreferences(String pin) {
        SharedPreferences prefs = this.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_PIN, Utils.sha256(pin)).apply();
    }

    /*change the lock screen layout if pin is set*/
    private void changeLayoutForSetPin() {
        mImageViewFingerView.setVisibility(View.GONE);
        mTextFingerText.setVisibility(View.GONE);
        mTextAttempts.setVisibility(View.GONE);
        pinTitle.setText(getString(R.string.pinlock_settitle));
        contextIcon.setVisibility(View.GONE);
    }

    /*set pin and check if second input is correct*/
    private void setPin(String pin) {
        if (mFirstPin.equals("")) {
            mFirstPin = pin;
            pinTitle.setText(getString(R.string.pinlock_secondPin));
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
                pinTitle.setText(getString(R.string.pinlock_tryagain));
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

    /*open experience sampling based on random number (likelihood based on participation survey)*/
    private void openExperienceSampling() {
        Random generator = new Random();
        int randomInt = generator.nextInt(Constants.SURVEY_COOLDOWN-0) + 0;
        Log.e("random", String.valueOf(randomInt));
        cooldown = SharedPreferencesStorage.readSharedPreference(this, Constants.PREFERENCES, Constants.COOLDOWN_KEY);
        Log.e("cooldown", String.valueOf(cooldown));
        if(randomInt == 1) {
            //if(!cooldown.equals("true")) {
                opensurveycounter++;
                SharedPreferencesStorage.writeSharedPreference(this, Constants.PREFERENCES, Constants.COUNTER_KEY, String.valueOf(opensurveycounter));
                SharedPreferencesStorage.writeSharedPreference(this, Constants.PREFERENCES, Constants.COOLDOWN_KEY, "true");
                Log.e(TAG, "startActivity");
                Intent intent = new Intent(this, ExperienceSamplingActivity.class);
                startActivity(intent);
            //}
        }
        if(randomInt == 0 /*&& cooldown.equals("true")*/) {
            SharedPreferencesStorage.writeSharedPreference(this, Constants.PREFERENCES, Constants.COOLDOWN_KEY, "false");
            Log.e(TAG, "finish");
            setResult(RESULT_CANCELED);
            finishAffinity();
        }
    }

    /*check if entered pin is correct with deposited pin*/
    private void checkPin(String pin) {
        if (Utils.sha256(pin).equals(getPinFromSharedPreferences())) {
            setResult(RESULT_OK);
            writeLockscreenDataToFirebase(true);
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
                        writeLockscreenDataToFirebase(false);
                        openExperienceSampling();
                        finish();
                    }
                }, 750);
            }

            @Override
            public void onFailed() {
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
            public void onError(CharSequence errorString, int errMsgId) {
                if (errMsgId == FingerprintManager.FINGERPRINT_ERROR_CANCELED) {
                    Log.e(TAG,  errorString.toString());
                }else {
                    Toast.makeText(Lockscreen.this, errorString, Toast.LENGTH_SHORT).show();
                }
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
                    mImageViewFingerView.setVisibility(View.GONE);
                    return;
                }
                //Check that the user has registered at least one fingerprint//
                if (!mFingerprintManager.hasEnrolledFingerprints()) {
                    mImageViewFingerView.setVisibility(View.GONE);
                    return;
                }
                //Check that the lockscreen is secured//
                if (!mKeyguardManager.isKeyguardSecure()) {
                    mImageViewFingerView.setVisibility(View.GONE);
                    return;
                } else {
                    try {
                        generateKey();
                        if (initCipher()) {
                            //If the mCipher is initialized successfully, then create a CryptoObject instance//
                            mCryptoObject = new FingerprintManager.CryptoObject(mCipher);
                            // Here, I’m referencing the FingerprintHandler class that we’ll create in the next section. This class will be responsible
                            // for starting the authentication process (via the startAuth method) and processing the authentication process events//
                            helper = new FingerprintHandler(this);
                            helper.startAuth(mFingerprintManager, mCryptoObject);
                            helper.setFingerPrintListener(fingerPrintListener);
                        }
                    } catch (FingerprintException e) {
                        Log.wtf(TAG, "Failed to generate key for fingerprint.", e);
                    }
                }
            } else {
                mImageViewFingerView.setVisibility(View.GONE);
            }
        } else {
            mImageViewFingerView.setVisibility(View.GONE);
        }
    }

    private class FingerprintException extends Exception {
        public FingerprintException(Exception e) {
            super(e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy");
        locationManager.removeUpdates(locationListener);
        stopTracking(); //TODO check if this work


    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "onPause");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.moveTaskToFront(getTaskId(), 0);

        if(helper != null) {
            helper.stopListening();
        }
    }

    //disable back button of lock screen
    @Override
    public void onBackPressed() {
        if (!mSetPin) {
        } else {
            super.onBackPressed();
        }
    }
}

