package com.lmu.alicenguyen.contextlock;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.lmu.alicenguyen.contextlock.initial_survey.InitialSurvey;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SetupActivity extends AppCompatActivity {

    public static final String TAG = "SetupActivity";

    private static final String PREFERENCES = "com.example.alicenguyen.contextlock";
    private SharedPreferences prefs;
    private String userId;
    private DatabaseReference mDatabaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        setUserIdDialog();
        showPermissionDialog();
        setID();
        setFinished();
        setAlarmForLogEventsExport();
        setupDeviceAdministratorPermissions(prefs);
    }


    private void setFinished() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        if (extras != null) {
            String string= extras.getString("setup_finished");
            TextView finishedSetup = findViewById(R.id.setup_finished);
            finishedSetup.setText(string);
        } else {
            Log.e(TAG, "no setup message");
        }
    }

    private void setID() {
        String id = getIDfromSharedPreferences();
        Log.e(TAG, id);
        TextView idTextView = findViewById(R.id.user_id);
        if(!id.equals("")) {
            idTextView.setText(id);
        }
    }

    private void writeIDtoSharedPreferences(String id) {
        SharedPreferences prefs = this.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        prefs.edit().putString(Constants.KEY_ID, id).apply();
    }

    public String getIDfromSharedPreferences() {
        SharedPreferences prefs = this.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        return prefs.getString(Constants.KEY_ID, "");
    }

    /*handles setup process if user opens app for the first time*/
    private void setUserIdDialog() {
        boolean previouslyStarted = prefs.getBoolean(Constants.FIRST_SETUP, false);

            final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
            builder.setCancelable(false);
            builder.setTitle("Enter your received ID");

            final EditText input = new EditText(this);
            input.setTextAppearance(R.style.EditTextStyle);

            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            builder.setView(input);
            builder.setPositiveButton("Submit", null);
            final AlertDialog alert = builder.create();
            alert.setOnShowListener(new DialogInterface.OnShowListener() {

                @Override
                public void onShow(final DialogInterface dialog) {
                    Button b = alert.getButton(AlertDialog.BUTTON_POSITIVE);
                    b.setTextColor(ContextCompat.getColor(SetupActivity.this, R.color.teal));
                    b.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            // TODO Do something
                            userId = input.getText().toString();
                            if(!userId.isEmpty()) {
                                Log.e(TAG, userId);
                                writeIDtoSharedPreferences(userId);
                                SharedPreferences.Editor edit = prefs.edit();
                                edit.putBoolean(Constants.FIRST_SETUP, Boolean.TRUE);
                                edit.apply();
                                Log.e(TAG, String.valueOf(prefs.getBoolean(Constants.FIRST_SETUP, false)));
                                Toast.makeText(SetupActivity.this, "ID set", Toast.LENGTH_LONG).show();
                                setSwitchVersionDate();
                                setStudyEndDate();
                                startService();
                                //setRandomAlarmReceiver(); //TODO wieder rein
                                setRepeatingSync(SetupActivity.this);
                                dialog.dismiss();
                                openInitialSurvey();

                            }else {
                                Toast.makeText(SetupActivity.this, "Please enter ID", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            });
            if (!previouslyStarted) {
                alert.show();
            }
    }


    private void saveInitialVersion() {
        String userid = getIDfromSharedPreferences();
        Log.e(TAG, "init version id: " + userid);
        if (!userid.equals("")) {
            Log.e(TAG, "version init");
            int id = Integer.parseInt(userid);
            if ((id % 2) == 0) {
                SharedPreferencesStorage.writeSharedPreference(this, Constants.PREFERENCES, Constants.VERSION_KEY, Constants.VERSION_A);
            } else {
                SharedPreferencesStorage.writeSharedPreference(this, Constants.PREFERENCES, Constants.VERSION_KEY, Constants.VERSION_B);
            }
        }
    }

    /*open initial survey when user clicks on confirm id button the first time*/
    private void openInitialSurvey() {
        saveInitialVersion();
        Intent i = new Intent(this, InitialSurvey.class);
        startActivity(i);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean(Constants.FIRST_OPEN_SURVEY, Boolean.TRUE);
        edit.apply();
    }

    private void setPin() {
        Log.e(TAG, "setPin");
        String fallback = SharedPreferencesStorage.readSharedPreference(this, Constants.PREFERENCES, Constants.UNLOCK_METHOD_KEY);
        switch (fallback) {
            case "PIN": {
                Intent intent = PinLockScreen.getIntent(this, true);
                startActivity(intent);
                break;
            }
            case "Pattern": {
                Intent intent = PatternLockScreen.getIntent(this, true);
                startActivity(intent);
                break;
            }
            default:
                Log.e(TAG, "not found");
                break;
        }
    }

    /*if permission are granted, background services will be started after user starts the tracking (on start tracking clicked)*/
    private void checkAppPermissions() {
        if (ContextCompat.checkSelfPermission(SetupActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SetupActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
            return;
        } else {
            /*Permission has already been granted*/
            Log.e("permission", "GPS permission granted");
            final String id = getIDfromSharedPreferences();
            if (id.equals("")) {
                Toast.makeText(this, "Please enter ID and submit", Toast.LENGTH_LONG).show();
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
                    if (id.equals("")) {
                        Toast.makeText(this, "Please enter ID and submit", Toast.LENGTH_LONG).show();
                    } else {
                        //startBackgroundServices();
                    }
                } else {
                    // permission denied
                    Toast.makeText(SetupActivity.this, "Please accept GPS permission to user the App", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }


    /*Inform the user which data are tracked for the user study and ask for permission*/
    private void showPermissionDialog() {
        boolean permissionAgreed = prefs.getBoolean(Constants.PERMISSION_AGREE, false);
        AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(this, android.R.style.Theme_Holo_Light_Dialog));
        mDialogBuilder.setCancelable(false);

        View mDialogView = getLayoutInflater().inflate(R.layout.permission_dialog, null);
        final CheckBox permissionCheckbox =  mDialogView.findViewById(R.id.checkbox_permission_agree);
        Button mAgreeButton = mDialogView.findViewById(R.id.agree_permission_button);
        Button mCancelButton = mDialogView.findViewById(R.id.cancel_permission_button);
        mDialogBuilder.setView(mDialogView);
        final AlertDialog dialog = mDialogBuilder.create();
        //set opacity of dialog
        //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white)));

        if (!permissionAgreed) {
            dialog.show();
        }
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!permissionCheckbox.isChecked()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.agree_permission_request), Toast.LENGTH_LONG).show();
                }
            }
        });
        mAgreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (permissionCheckbox.isChecked()) {
                    //permissionAgreed = true;
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putBoolean(Constants.PERMISSION_AGREE, Boolean.TRUE);
                    edit.commit();
                    dialog.cancel();
                    checkAppPermissions();
                } else {
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
        Log.e(TAG, switchVersionDate);
        SharedPreferencesStorage.writeSharedPreference(this, Constants.PREFERENCES, Constants.SWITCH_VERSION_KEY, switchVersionDate);
        mDatabaseReference.child("Users").child(userId).child("start-date").setValue(Calendar.getInstance().getTime().toString());
        mDatabaseReference.child("Users").child(userId).child("switch-date").setValue(switchVersionDate);
    }

    /*set study end date when app starts for the first time*/
    private void setStudyEndDate() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, Constants.STUDY_LENGTH * 2);
        Date date = c.getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String studyEndDate = simpleDateFormat.format(date);
        SharedPreferencesStorage.writeSharedPreference(this, Constants.PREFERENCES, Constants.STUDY_END_KEY, studyEndDate);
        Log.e(TAG, "study end: " + studyEndDate);
        mDatabaseReference.child("Users").child(userId).child("end-date").setValue(studyEndDate);
    }


    /*This method schedule an alarm to send locally stored data to firebase DB.
    Function is called each day at nighttime
    */
    private void setAlarmForLogEventsExport() {
        Log.e(TAG, "start export");
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, ExportDBHelper.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 20, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 24);
        c.set(Calendar.MINUTE, 0);

        if(alarmManager!= null) {
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        }
    }

    /*set RandomAlarmReceiver to set new random lock screen triggering alarms each night*/
    public static void setRepeatingSync(Context context) {
        try {
            AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, RandomAlarmReceiver.class);
            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, 1);
            PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                    c.getTimeInMillis(), (24 * 1000 * 60 * 60), alarmIntent); //every 24 hours
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupDeviceAdministratorPermissions(SharedPreferences sharedPreferences) {
        DevicePolicyManager mgr = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName cn = new ComponentName(this, AdminReceiver.class);
        if ( !mgr.isAdminActive(cn)) {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, cn);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, getString(R.string.admin_explanation));
            //startActivity(intent);
            startActivityForResult(intent, RESULT_OK);
        }
    }

    /*settings dialog to change pin/pattern, start and stop foreground services*/
    public void openSettings(View view) {
        AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(SetupActivity.this);
        mDialogBuilder.setCancelable(true);
        View mDialogView = getLayoutInflater().inflate(R.layout.settings_dialog, null);

        Button mSetPIN = mDialogView.findViewById(R.id.setPin);
        Button mCancelDialogButton = mDialogView.findViewById(R.id.cancel_dialog);
        Button mstopTrackingButton = mDialogView.findViewById(R.id.stop_tracking);
        Button mstartTrackingButton = mDialogView.findViewById(R.id.start_tracking);

        mDialogBuilder.setView(mDialogView);
        final AlertDialog dialog = mDialogBuilder.create();
        dialog.show();

        mCancelDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        mSetPIN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPin();
            }
        });
        mstopTrackingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(SetupActivity.this, R.style.AlertDialogStyle)
                        .setMessage("Are you sure you want to stop tracking?"
                                + " The app won't receive relevant study data anymore")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                stopService();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });
        mstartTrackingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService();
            }
        });
    }


    /*unregister receiver*/
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void startService() {
        Intent serviceIntent = new Intent(this, LockScreenService.class);
        ContextCompat.startForegroundService(this, serviceIntent);
        setRepeatingSync(this);
        Toast.makeText(this, "tracking started", Toast.LENGTH_LONG).show();
    }

    private void stopActivityTracking() {
        Intent serviceIntent = new Intent(this, BackgroundDetectedActivitiesService.class);
        stopService(serviceIntent);
        Log.e(TAG, "stopActivityTracking");
    }

    public void stopService() {
        Intent serviceIntent = new Intent(this, LockScreenService.class);
        stopService(serviceIntent);
        //stopActivityTracking();
        Toast.makeText(this, "tracking stopped", Toast.LENGTH_LONG).show();
    }
}
