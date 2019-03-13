package com.example.alicenguyen.contextlock;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ExperienceSamplingActivity extends AppCompatActivity {

    private static final String PREFERENCES = "com.example.alicenguyen.contextlock";

    public static View seekbarContainer;
    public static View checkBoxContainer;

    private Button notNowButton;
    private ImageView sendButton;
    private SeekBar predictionSeekbar;
    private SeekBar reasonableSeekbar;
    private SeekBar annoyanceSeekbar;

    private TextView predictionStronglyAgree, predictionAgree, predictionNeutral, predictionDisagree, predictionStronglyDisagree;
    private TextView annoyanceStronglyAgree, annoyanceAgree, annoyanceNeutral, annoyanceDisagree, annoyanceStronglyDisagree;
    //private TextView reasonableStronglyAgree, reasonableAgree, reasonableNeutral, reasonableDisagree, reasonableStronglyDisagree;

    private EditText reasonEditext, reasonEditextVersionA;

    private int annoyanceValue, predictionValue, reasonableValue;
    private String locationValue;
    private String reasonFreeText = "";
    private String reasonFreeTextVersionA = "";
    private FirebaseAnalytics mFirebaseAnalytics;

    private DatabaseReference mDatabaseReference;
    private String userid, version;
    private String switchValue, fingerErrorValue, reasonValue;
    private Bundle extras = new Bundle();
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");

    private Date currenttime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_experience_sampling);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        currenttime = Calendar.getInstance().getTime();

        Log.d("survey", "survey open");
        SharedPreferences pref = getApplicationContext().getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        userid = pref.getString("user_id", "no id");


        Log.e("user id", userid);

        initNotNowButton();
        initViewElements();
        version = SharedPreferencesStorage.readSharedPreference(this, Constants.PREFERENCES, Constants.VERSION_KEY);
        Log.e("version", version);

        /*handle survey version based on notification version*/
        if(version.equals(Constants.VERSION_A)){
            seekbarContainer.setVisibility(View.GONE);
            checkBoxContainer.setVisibility(View.VISIBLE);
        }else if(version.equals(Constants.VERSION_B)) {
            seekbarContainer.setVisibility(View.VISIBLE);
            checkBoxContainer.setVisibility(View.GONE);
        }
        setPredictionSeekbarListener();
        setAnnoyanceSeekbarListener();
        //setReasonableSeekbarListener();
        //getEditText();

        initNextButton();

        //getLocationEditText();
        //initSendButton();
    }

    //TODO
    private void getReasonEditText() {

    }

    private void initNextButton() {
        Button nextButton = findViewById(R.id.next_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                extras.putInt("predictionValue", predictionValue);
                extras.putInt("annoyanceValue", annoyanceValue);
                //extras.putInt("reasonableValue", reasonableValue);
                extras.putString("switchValue", switchValue);
                extras.putString("fingerErrorValue", fingerErrorValue);
                extras.putString("reasonFreeText", reasonEditext.getText().toString());
                extras.putString("reasonFreeTextVersionA", reasonEditextVersionA.getText().toString());
                //startActivity(new Intent(ExperienceSamplingActivity.this, ExperienceSamplingActivity2.class));
                Intent i = new Intent(ExperienceSamplingActivity.this, ExperienceSamplingActivity2.class);
                i.putExtras(extras);
                Log.e("bundle", extras.toString());
                startActivity(i);
            }
        });
    }



    /*private void initSendButton(){
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                currenttime = Calendar.getInstance().getTime();

                //TODO set locationValue to database
                mDatabaseReference.child("users").child(userid).child(currenttime.toString()).child("prediction-rate").setValue(predictionValue);
                mDatabaseReference.child("users").child(userid).child(currenttime.toString()).child("annoyance-rate").setValue(annoyanceValue);
                mDatabaseReference.child("users").child(userid).child(currenttime.toString()).child("reasonable-rate").setValue(reasonableValue);

                Toast.makeText(ExperienceSamplingActivity.this, "gesendet", Toast.LENGTH_SHORT).show();
                finishAffinity();

            }
        });
    }*/

    /*private void getLocationEditText() {
        locationFreeText = locationEditText.getText().toString();
    }*/

    /*private void setReasonableSeekbarListener() {
        reasonableSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                reasonableValue = progress;
                //Toast.makeText(ExperienceSamplingActivity.this, String.valueOf(progress), Toast.LENGTH_SHORT).show();
                switch (progress) {
                    case (0):
                        reasonableStronglyDisagree.setTextColor(getResources().getColor(R.color.teal));
                        reasonableDisagree.setTextColor(getResources().getColor(R.color.dark_grey));
                        reasonableNeutral.setTextColor(getResources().getColor(R.color.dark_grey));
                        reasonableAgree.setTextColor(getResources().getColor(R.color.dark_grey));
                        reasonableStronglyAgree.setTextColor(getResources().getColor(R.color.dark_grey));
                        break;
                    case (1):
                        reasonableStronglyDisagree.setTextColor(getResources().getColor(R.color.dark_grey));
                        reasonableDisagree.setTextColor(getResources().getColor(R.color.teal));
                        reasonableNeutral.setTextColor(getResources().getColor(R.color.dark_grey));
                        reasonableAgree.setTextColor(getResources().getColor(R.color.dark_grey));
                        reasonableStronglyAgree.setTextColor(getResources().getColor(R.color.dark_grey));
                        break;
                    case (2):
                        reasonableStronglyDisagree.setTextColor(getResources().getColor(R.color.dark_grey));
                        reasonableDisagree.setTextColor(getResources().getColor(R.color.dark_grey));
                        reasonableNeutral.setTextColor(getResources().getColor(R.color.teal));
                        reasonableAgree.setTextColor(getResources().getColor(R.color.dark_grey));
                        reasonableStronglyAgree.setTextColor(getResources().getColor(R.color.dark_grey));
                        break;
                    case (3):
                        reasonableStronglyDisagree.setTextColor(getResources().getColor(R.color.dark_grey));
                        reasonableDisagree.setTextColor(getResources().getColor(R.color.dark_grey));
                        reasonableNeutral.setTextColor(getResources().getColor(R.color.dark_grey));
                        reasonableAgree.setTextColor(getResources().getColor(R.color.teal));
                        reasonableStronglyAgree.setTextColor(getResources().getColor(R.color.dark_grey));
                        break;
                    case (4):
                        reasonableStronglyDisagree.setTextColor(getResources().getColor(R.color.dark_grey));
                        reasonableDisagree.setTextColor(getResources().getColor(R.color.dark_grey));
                        reasonableNeutral.setTextColor(getResources().getColor(R.color.dark_grey));
                        reasonableAgree.setTextColor(getResources().getColor(R.color.dark_grey));
                        reasonableStronglyAgree.setTextColor(getResources().getColor(R.color.teal));
                        break;
                    default:
                        //This code is executed when value of variable 'day'
                        //doesn't match with any of case above
                        reasonableDisagree.setTextColor(getResources().getColor(R.color.dark_grey));
                        reasonableDisagree.setTextColor(getResources().getColor(R.color.dark_grey));
                        reasonableNeutral.setTextColor(getResources().getColor(R.color.dark_grey));
                        reasonableAgree.setTextColor(getResources().getColor(R.color.dark_grey));
                        reasonableStronglyAgree.setTextColor(getResources().getColor(R.color.dark_grey));
                        break;
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekBar.getThumb().setColorFilter(getResources().getColor(R.color.teal), PorterDuff.Mode.SRC);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }*/

    private void setAnnoyanceSeekbarListener() {
        annoyanceSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                annoyanceValue = progress;
                switch (progress) {
                    case (0):
                        annoyanceStronglyDisagree.setTextColor(getResources().getColor(R.color.teal));
                        annoyanceDisagree.setTextColor(getResources().getColor(R.color.dark_grey));
                        annoyanceNeutral.setTextColor(getResources().getColor(R.color.dark_grey));
                        annoyanceAgree.setTextColor(getResources().getColor(R.color.dark_grey));
                        annoyanceStronglyAgree.setTextColor(getResources().getColor(R.color.dark_grey));
                        break;
                    case (1):
                        annoyanceStronglyDisagree.setTextColor(getResources().getColor(R.color.dark_grey));
                        annoyanceDisagree.setTextColor(getResources().getColor(R.color.teal));
                        annoyanceNeutral.setTextColor(getResources().getColor(R.color.dark_grey));
                        annoyanceAgree.setTextColor(getResources().getColor(R.color.dark_grey));
                        annoyanceStronglyAgree.setTextColor(getResources().getColor(R.color.dark_grey));
                        break;
                    case (2):
                        annoyanceStronglyDisagree.setTextColor(getResources().getColor(R.color.dark_grey));
                        annoyanceDisagree.setTextColor(getResources().getColor(R.color.dark_grey));
                        annoyanceNeutral.setTextColor(getResources().getColor(R.color.teal));
                        annoyanceAgree.setTextColor(getResources().getColor(R.color.dark_grey));
                        annoyanceStronglyAgree.setTextColor(getResources().getColor(R.color.dark_grey));
                        break;
                    case (3):
                        annoyanceStronglyDisagree.setTextColor(getResources().getColor(R.color.dark_grey));
                        annoyanceDisagree.setTextColor(getResources().getColor(R.color.dark_grey));
                        annoyanceNeutral.setTextColor(getResources().getColor(R.color.dark_grey));
                        annoyanceAgree.setTextColor(getResources().getColor(R.color.teal));
                        annoyanceStronglyAgree.setTextColor(getResources().getColor(R.color.dark_grey));
                        break;
                    case (4):
                        annoyanceStronglyDisagree.setTextColor(getResources().getColor(R.color.dark_grey));
                        annoyanceDisagree.setTextColor(getResources().getColor(R.color.dark_grey));
                        annoyanceNeutral.setTextColor(getResources().getColor(R.color.dark_grey));
                        annoyanceAgree.setTextColor(getResources().getColor(R.color.dark_grey));
                        annoyanceStronglyAgree.setTextColor(getResources().getColor(R.color.teal));
                        break;
                    default:
                        //This code is executed when value of variable 'day'
                        //doesn't match with any of case above
                        annoyanceStronglyDisagree.setTextColor(getResources().getColor(R.color.teal));
                        annoyanceDisagree.setTextColor(getResources().getColor(R.color.dark_grey));
                        annoyanceNeutral.setTextColor(getResources().getColor(R.color.dark_grey));
                        annoyanceAgree.setTextColor(getResources().getColor(R.color.dark_grey));
                        annoyanceStronglyAgree.setTextColor(getResources().getColor(R.color.dark_grey));
                        break;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekBar.getThumb().setColorFilter(getResources().getColor(R.color.teal), PorterDuff.Mode.SRC);

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void setPredictionSeekbarListener() {
        predictionSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                predictionValue = progress;
                //Toast.makeText(ExperienceSamplingActivity.this, String.valueOf(progress), Toast.LENGTH_SHORT).show();
                switch (progress) {
                    case (0):
                        predictionStronglyDisagree.setTextColor(getResources().getColor(R.color.teal));
                        predictionDisagree.setTextColor(getResources().getColor(R.color.dark_grey));
                        predictionNeutral.setTextColor(getResources().getColor(R.color.dark_grey));
                        predictionAgree.setTextColor(getResources().getColor(R.color.dark_grey));
                        predictionStronglyAgree.setTextColor(getResources().getColor(R.color.dark_grey));
                        break;
                    case (1):
                        predictionStronglyDisagree.setTextColor(getResources().getColor(R.color.dark_grey));
                        predictionDisagree.setTextColor(getResources().getColor(R.color.teal));
                        predictionNeutral.setTextColor(getResources().getColor(R.color.dark_grey));
                        predictionAgree.setTextColor(getResources().getColor(R.color.dark_grey));
                        predictionStronglyAgree.setTextColor(getResources().getColor(R.color.dark_grey));
                        break;
                    case (2):
                        predictionStronglyDisagree.setTextColor(getResources().getColor(R.color.dark_grey));
                        predictionDisagree.setTextColor(getResources().getColor(R.color.dark_grey));
                        predictionNeutral.setTextColor(getResources().getColor(R.color.teal));
                        predictionAgree.setTextColor(getResources().getColor(R.color.dark_grey));
                        predictionStronglyAgree.setTextColor(getResources().getColor(R.color.dark_grey));
                        break;
                    case (3):
                        predictionStronglyDisagree.setTextColor(getResources().getColor(R.color.dark_grey));
                        predictionDisagree.setTextColor(getResources().getColor(R.color.dark_grey));
                        predictionNeutral.setTextColor(getResources().getColor(R.color.dark_grey));
                        predictionAgree.setTextColor(getResources().getColor(R.color.teal));
                        predictionStronglyAgree.setTextColor(getResources().getColor(R.color.dark_grey));
                        break;
                    case (4):
                        predictionStronglyDisagree.setTextColor(getResources().getColor(R.color.dark_grey));
                        predictionDisagree.setTextColor(getResources().getColor(R.color.dark_grey));
                        predictionNeutral.setTextColor(getResources().getColor(R.color.dark_grey));
                        predictionAgree.setTextColor(getResources().getColor(R.color.dark_grey));
                        predictionStronglyAgree.setTextColor(getResources().getColor(R.color.teal));
                        break;
                    default:
                        //This code is executed when value of variable 'day'
                        //doesn't match with any of case above
                        predictionStronglyDisagree.setTextColor(getResources().getColor(R.color.dark_grey));
                        predictionDisagree.setTextColor(getResources().getColor(R.color.dark_grey));
                        predictionNeutral.setTextColor(getResources().getColor(R.color.dark_grey));
                        predictionAgree.setTextColor(getResources().getColor(R.color.dark_grey));
                        predictionStronglyAgree.setTextColor(getResources().getColor(R.color.dark_grey));
                        break;
                }


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekBar.getThumb().setColorFilter(getResources().getColor(R.color.teal), PorterDuff.Mode.SRC);

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    public void onPredictionRadioButtonClicked(View view) {
    }

    private void initViewElements() {
        seekbarContainer = findViewById(R.id.predictionSeekbarContainer);
        checkBoxContainer = findViewById(R.id.reasonCheckboxContainer);

        predictionSeekbar = findViewById(R.id.seekBarPrediction);
        reasonableSeekbar = findViewById(R.id.seekBarReasonable);
        annoyanceSeekbar = findViewById(R.id.seekBarAnnoyance);

        predictionStronglyDisagree = findViewById(R.id.prediction_strongly_disagree);
        predictionDisagree = findViewById(R.id.prediction_disagree);
        predictionNeutral = findViewById(R.id.prediction_neural);
        predictionAgree = findViewById(R.id.prediction_agree);
        predictionStronglyAgree = findViewById(R.id.prediction_strongly_agree);

        annoyanceStronglyDisagree = findViewById(R.id.annoyance_strongly_disagree);
        annoyanceDisagree = findViewById(R.id.annoyance_disagree);
        annoyanceNeutral = findViewById(R.id.annoyance_neutral);
        annoyanceAgree = findViewById(R.id.annoyance_agree);
        annoyanceStronglyAgree = findViewById(R.id.annoyance_strongly_agree);


        //sendButton = findViewById(R.id.send_button);
        reasonEditext = findViewById(R.id.reasonEdittext);
        reasonEditextVersionA = findViewById(R.id.reasonDefaultEdittext);
    }

    private void insertToFirebase() {
        mDatabaseReference.child("logEvents").child(version).child(userid).child(currenttime.toString()).child("notNowButton").setValue("clicked");
    }

    private void initNotNowButton() {
        notNowButton = findViewById(R.id.not_now_button);
        notNowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertToFirebase();
                NotificationHelper.cancelNotification(ExperienceSamplingActivity.this, Constants.NOTIFICATION_ID);
                finishAffinity();
            }
        });
    }


    public void onSwitchButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        RadioButton trueRadioButton = findViewById(R.id.lockswitch_true);
        RadioButton falseRadioButton = findViewById(R.id.lockswitch_false);
        RadioButton dontRememberRadioButton = findViewById(R.id.lockswitch_dontremember);
        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.lockswitch_true:
                if (checked)
                    switchValue = trueRadioButton.getText().toString();

                break;
            case R.id.lockswitch_false:
                if (checked)
                    switchValue = falseRadioButton.getText().toString();
                break;
            case R.id.lockswitch_dontremember:
                if(checked)
                    switchValue = dontRememberRadioButton.getText().toString();
        }
    }

    public void onFingerErrorRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        RadioButton trueRadioButton = findViewById(R.id.fingerprint_error_true);
        RadioButton falseRadioButton = findViewById(R.id.fingerprint_error_false);
        RadioButton notSureRadioButton = findViewById(R.id.fingerprint_error_not_sure);


        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.fingerprint_error_true:
                if (checked)
                    fingerErrorValue = trueRadioButton.getText().toString();

                break;
            case R.id.fingerprint_error_false:
                if (checked)
                    fingerErrorValue = falseRadioButton.getText().toString();
                break;
            case R.id.fingerprint_error_not_sure:
                if (checked)
                    fingerErrorValue = notSureRadioButton.getText().toString();
                break;
        }
    }

    public void onCheckboxClicked(View view) {
        boolean checked = ((CheckBox) view).isChecked();

        switch(view.getId()) {
            case R.id.rain_checkbox:
                if (checked) {
                    reasonValue = ((CheckBox) view).getText().toString();
                    Log.e("checkbox", reasonValue);
                }

                break;
            case R.id.snow_checkbox:
                if (checked) {
                    reasonValue = ((CheckBox) view).getText().toString();
                }
                break;
        }
    }

    /*send intermediate data to firebase when user closes the app (interrupts survey)
    so not all data are gone
    */
    @Override
    protected void onDestroy() {
        Log.e("ExperienceSampling", "onDestroy");
        //String version = SharedPreferencesStorage.readSharedPreference(this, Constants.PREFERENCES, Constants.VERSION_KEY);
        //mDatabaseReference.child(version).child(userid).child(currenttime.toString()).child("v1values").setValue(extras.toString());
        mDatabaseReference.child(version).child(userid).child(currenttime.toString()).child("lockscreen-switch-value").setValue(switchValue);
        mDatabaseReference.child(version).child(userid).child(currenttime.toString()).child("prediction-rate").setValue(predictionValue);
        mDatabaseReference.child(version).child(userid).child(currenttime.toString()).child("annoyance-rate").setValue(annoyanceValue);
        mDatabaseReference.child(version).child(userid).child(currenttime.toString()).child("fingererror-value").setValue(fingerErrorValue);
        NotificationHelper.cancelNotification(this, Constants.NOTIFICATION_ID);
        super.onDestroy();
    }

    /*ask the user really wants to exit the app to make it a bit harder to leave the app
    disabling the home button is not allowed/ a good solution (we can't/shouldn't prevent the user from leaving the app)
    */
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ExperienceSamplingActivity.this.finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
}
