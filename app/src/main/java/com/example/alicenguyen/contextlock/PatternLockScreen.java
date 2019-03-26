package com.example.alicenguyen.contextlock;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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
import android.support.v4.app.ActivityCompat;
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
import com.example.alicenguyen.contextlock.andrognito.pinlockview.PinLockView;
import com.example.alicenguyen.contextlock.fingerprint.FingerPrintListener;
import com.example.alicenguyen.contextlock.fingerprint.FingerprintHandler;
import com.example.alicenguyen.contextlock.util.Animate;
import com.example.alicenguyen.contextlock.util.Utils;
import com.google.android.gms.location.ActivityRecognitionClient;
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

public class PatternLockScreen extends AppCompatActivity {


    private PatternLockView mPatternLockView;

    public static final String TAG = "PatternLockscreen";
    public static final int RESULT_TOO_MANY_TRIES = RESULT_FIRST_USER + 1;
    private static final int PIN_LENGTH = 4;
    private static final String FINGER_PRINT_KEY = "FingerPrintKey";
    public static final String EXTRA_SET_PATTERN = "set_pattern";
    private static final String KEY_PATTERN = "pattern";

    private static final int MAX_ATTEMPS = 3;
    private static final String COOLDOWN_KEY = "cooldown_key";
    private static final String COUNTER_KEY = "counter_key";
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

    private boolean mSetPattern = false;
    private String mFirstPattern = "";
    private int mPatternTryCount = 0;
    private int mFingerprintTryCount = 0;


    private TextView mTextTitle;
    private TextView mTextAttempts;
    private TextView mTextFingerText;
    private AppCompatImageView mImageViewFingerView;


    private ImageView mContextIcon;
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
    private TextView patternTitle;
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
    private boolean patternUsed;


    private FirebaseAnalytics mFirebaseAnalytics;

    private PatternLockViewListener mPatternLockViewListener = new PatternLockViewListener() {
        @Override
        public void onStarted() {
            Log.d(getClass().getName(), "Pattern drawing started");
        }

        @Override
        public void onProgress(List<PatternLockView.Dot> progressPattern) {
            Log.d(getClass().getName(), "Pattern progress: " +
                    PatternLockUtils.patternToString(mPatternLockView, progressPattern));
        }

        @Override
        public void onComplete(List<PatternLockView.Dot> pattern) {
            Log.d(getClass().getName(), "Pattern complete: " +
                    PatternLockUtils.patternToString(mPatternLockView, pattern));
            String p =  PatternLockUtils.patternToString(mPatternLockView, pattern);
            if (mSetPattern) {
                setPattern(p);
            } else {
                checkPattern(p);
            }
        }

        @Override
        public void onCleared() {
            Log.d(getClass().getName(), "Pattern has been cleared");
        }
    };


    public static Intent getIntent(Context context, boolean setPattern) {
        Intent intent = new Intent(context, PatternLockScreen.class);
        intent.putExtra(EXTRA_SET_PATTERN, setPattern);
        return intent;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pattern_lock_screen);
        hideOnScreenButton();
        initElements();
        getUserLocation();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            showFingerprint = (AnimatedVectorDrawable) getDrawable(R.drawable.show_fingerprint);
            fingerprintToTick = (AnimatedVectorDrawable) getDrawable(R.drawable.fingerprint_to_tick);
            fingerprintToCross = (AnimatedVectorDrawable) getDrawable(R.drawable.fingerprint_to_cross);
        }
        mSetPattern = getIntent().getBooleanExtra(EXTRA_SET_PATTERN, false);

        if (mSetPattern) {
            changeLayoutForSetPattern();
        } else {
            String pattern = getPatternFromSharedPreferences();
            if (pattern.equals("")) {
                changeLayoutForSetPattern();
                mSetPattern = true;
            } else {
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                Log.e("tag", "This'll run 2 seconds later");
                                checkForFingerPrint();
                            }
                        },
                        3000);
            }
        }
        mPatternLockView = (PatternLockView) findViewById(R.id.pattern_lock_view);
        mPatternLockView.addPatternLockListener(mPatternLockViewListener);
        /*final PatternLockViewListener mPatternLockViewListener = new PatternLockViewListener() {
            @Override
            public void onStarted() {
                Log.e(getClass().getName(), "Pattern drawing started");
            }

            @Override
            public void onProgress(List<PatternLockView.Dot> progressPattern) {
                Log.e(getClass().getName(), "Pattern progress: " +
                        PatternLockUtils.patternToString(mPatternLockView, progressPattern));
            }

            @Override
            public void onComplete(List<PatternLockView.Dot> pattern) {
                Log.e(getClass().getName(), "Pattern complete: " +
                        PatternLockUtils.patternToString(mPatternLockView, pattern));
                String p =  PatternLockUtils.patternToString(mPatternLockView, pattern);
                if (mSetPattern) {
                    setPattern(p);
                } else {
                    checkPattern(p);
                }
            }

            @Override
            public void onCleared() {
                Log.e(getClass().getName(), "Pattern has been cleared");
            }
        };*/

    }


    private void checkPattern(String pattern) {
        Log.e(TAG, "checkPattern");
        if (Utils.sha256(pattern).equals(getPatternFromSharedPreferences())) {
            setResult(RESULT_OK);
            patternUsed = true;
            writeLockscreenDataToFirebase();
            openExperienceSampling();
            finish();
        } else {
            shake();
            mPatternTryCount++;
            mTextAttempts.setText(getString(R.string.patternlock_wrongpin));
            mPatternLockView.clearPattern();

            //mPinLockView.resetPinLockView();

            if (mPatternTryCount == 1) {
                mTextAttempts.setText(getString(R.string.patternlock_wrongpin));
                //mPinLockView.resetPinLockView();
            } else if (mPatternTryCount == 2) {
                mTextAttempts.setText(getString(R.string.patternlock_wrongpin));
                //mPinLockView.resetPinLockView();
            } else if (mPatternTryCount == MAX_ATTEMPS) {
                setResult(RESULT_TOO_MANY_TRIES);
                mPatternTryCount = 0;
                mTextAttempts.setText("");
                //finish();
            }
        }
    }

    private void hideOnScreenButton() {
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT )
        {
            getWindow().getDecorView().setSystemUiVisibility( View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                    | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY );
        }
    }

    private void getUserLocation() {
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                String lon = "" + location.getLongitude();
                String lat = "" + location.getLatitude();
                gpsAccuracy = location.getAccuracy();
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
            Toast.makeText(this, "Not Enough Permission", Toast.LENGTH_SHORT).show();
            return;
        }else{ //if permission enabled
            if(isNetworkEnabled) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Constants.LOCATION_INTERVAL, Constants.LOCATION_DISTANCE, locationListener);
            }
            if(isGPSEnabled)  {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Constants.LOCATION_INTERVAL, Constants.LOCATION_DISTANCE, locationListener);
            }else {
                setContextIcon();
            }
        }
    }

    private void getWeatherData(String longitude, String latitude) {
        RequestQueue queue = Volley.newRequestQueue(PatternLockScreen.this);
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
                    }
                });
        queue.add(jsonObjectRequest);
    }

    private boolean checkForOutdoor(){
        return isGPSEnabled;
    }

    private void setContextIcon() {

        if(mSetPattern){
            return;
        }
        Log.e(TAG, "set icon");
        Log.e(TAG, mainWeather);
        Log.d("user context icon hum", String.valueOf(humidity));
        Log.e(TAG,"activity " + userActivity);
        int compareAccuracy = Float.compare(gpsAccuracy, LOCATION_ACCURACY);

        contextIcon.setVisibility(View.VISIBLE);
        //mPatternLockView.setVisibility(View.VISIBLE);

        if(userActivity.equals("still") && mainWeather.contains("Drizzle")){
            Log.e(TAG, "tada");
            //contextIcon.setImageResource(R.drawable.running);
            //patternTitle.setText(getString(R.string.running) + " " + getString(R.string.pinlock_title));
        } else if(mainWeather.contains("Rain") && userActivity.equals(R.string.activity_walking)) {
            contextIcon.setImageResource(R.drawable.raindrop);
            patternTitle.setText(getString(R.string.rain) + " " + getString(R.string.pinlock_title));
            Log.d("mist", "it's wet outside!");
        }else if(mainWeather.contains("Drizzle")){ //Drizzle
            contextIcon.setImageResource(R.drawable.raindrop);
            patternTitle.setText(getString(R.string.running) + " " + getString(R.string.pinlock_title));
        }
        else if(mainWeather.contains("Snow")) { //compareAccuracy >= 0
            contextIcon.setImageResource(R.drawable.snowflake_white);
            patternTitle.setText(getString(R.string.snow) + " " + getString(R.string.pinlock_title));
        } else if(humidity > 75 && temperature.doubleValue() > 27.0) {
            contextIcon.setImageResource(R.drawable.raindrop);
            patternTitle.setText(getString(R.string.wet) + " " + getString(R.string.pinlock_title));
        }else if(temperature.doubleValue() > 27.0 ){
            contextIcon.setImageResource(R.drawable.humidity_white);
            patternTitle.setText(getString(R.string.muggy) + " " + getString(R.string.pinlock_title));
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
                patternTitle.setText(getString(R.string.default_humidity) + " " + getString(R.string.pinlock_title));
                break;
            default: //movement
                contextIcon.setImageResource(R.drawable.running);
                patternTitle.setText(getString(R.string.default_movement) + " " + getString(R.string.pinlock_title));
                break;
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
                    patternTitle.setText(getString(R.string.pinlock_title));
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
                    patternTitle.setText(R.string.pinlock_title);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Log.e(TAG, "error parse date");
        }
    }



    private String getPatternFromSharedPreferences() {
        SharedPreferences prefs = this.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE);
        return prefs.getString(KEY_PATTERN, "");
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
        Log.e(TAG, patternTitle.getText().toString());
        Log.e(TAG, String.valueOf(patternUsed));

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("logEvents").child(version).child(id);
        ref.child(currenttime.toString()).child("reason").setValue(patternTitle.getText().toString());
        ref.child(currenttime.toString()).child("pattern_used").setValue(patternUsed);
    }

    private void changeLayoutForSetPattern() {
        mImageViewFingerView.setVisibility(View.GONE);
        mTextFingerText.setVisibility(View.GONE);
        //mTextAttempts.setVisibility(View.GONE);
        mTextTitle.setText(getString(R.string.patternlock_settitle));
        mContextIcon.setVisibility(View.GONE);
    }

    private void writePatternToSharedPreferences(String pattern) {
        SharedPreferences prefs = this.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_PATTERN, Utils.sha256(pattern)).apply();
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
            //finish();

            //}
        }
        if(randomInt == 0 /*&& cooldown.equals("true")*/) {
            SharedPreferencesStorage.writeSharedPreference(this, Constants.PREFERENCES, Constants.COOLDOWN_KEY, "false");
            Log.e(TAG, "finish");
            //setResult(Activity.RESULT_CANCELED);
            setResult(RESULT_CANCELED);
            finishAffinity();

            //finishAffinity();
            //finish();
        }
    }

    private void setPattern(String pattern) {
        Log.e(TAG, "setPattern");
        if (mFirstPattern.equals("")) {
            mFirstPattern= pattern;
            //mTextTitle.setText(getString(com.amirarcane.lockscreen.R.string.pinlock_secondPin));
            mTextTitle.setText(getString(R.string.patternlock_secondPin));
            mPatternLockView.clearPattern();
            //mPatternLockView.resetPinLockView();
        } else {
            if (pattern.equals(mFirstPattern)) {
                writePatternToSharedPreferences(pattern);
                setResult(RESULT_OK);
                finish();
                Bundle extras = new Bundle();
                extras.putString("setup_finished", "Setup finished!");
                Intent i = new Intent(this, SetupActivity.class);
                i.putExtras(extras);
                startActivity(i);
                Log.e(TAG, "setPin finished");
            } else {
                shake();
                //mTextTitle.setText(getString(com.amirarcane.lockscreen.R.string.pinlock_tryagain));
                mTextTitle.setText(getString(R.string.patternlock_tryagain));
                mPatternLockView.clearPattern();

                //mPatternLockView.resetPinLockView();
                mFirstPattern = "";
            }
        }
    }

    private void shake() {
        ObjectAnimator objectAnimator = new ObjectAnimator().ofFloat(mPatternLockView, "translationX",
                0, 25, -25, 25, -25, 15, -15, 6, -6, 0).setDuration(1000);
        objectAnimator.start();
    }


    private void initElements() {

        mTextAttempts = (TextView) findViewById(R.id.attempts);
        mTextTitle = (TextView) findViewById(R.id.title);
        mImageViewFingerView = (AppCompatImageView) findViewById(R.id.fingerView);
        mTextFingerText = (TextView) findViewById(R.id.fingerText);
        mContextIcon = (ImageView) findViewById(R.id.context_icon);
        contextIcon = findViewById(R.id.context_icon);
        fingerprintIcon = findViewById(R.id.fingerView);
        patternTitle = findViewById(R.id.title);
        methodTitle = findViewById(R.id.fingerText);
    }


    //Create the generateKey method that we’ll use to gain access to the Android keystore and generate the encryption key//
    private void generateKey() throws PatternLockScreen.FingerprintException {
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
            throw new PatternLockScreen.FingerprintException(exc);

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
                        patternUsed = false;
                        //writeLogEventsToDB();
                        writeLockscreenDataToFirebase();
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
                Log.e(TAG, "fingerprint error");
                Toast.makeText(PatternLockScreen.this, errorString, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onHelp(CharSequence helpString) {
                Toast.makeText(PatternLockScreen.this, helpString, Toast.LENGTH_SHORT).show();
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
                    } catch (PatternLockScreen.FingerprintException e) {
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
        if(!mSetPattern){
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


}
