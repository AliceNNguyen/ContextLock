package com.lmu.alicenguyen.contextlock.experience_sampling;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.lmu.alicenguyen.contextlock.Constants;
import com.lmu.alicenguyen.contextlock.R;
import com.lmu.alicenguyen.contextlock.SharedPreferencesStorage;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Date;

public class ExperienceSamplingActivity extends AppCompatActivity {

    private static final String PREFERENCES = "com.example.alicenguyen.contextlock";

    public View seekbarContainer;
    public View checkBoxContainer;

    private SeekBar predictionSeekbar;
    private SeekBar reasonableSeekbar;
    private SeekBar annoyanceSeekbar;

    private TextView predictionStronglyAgree, predictionAgree, predictionNeutral, predictionDisagree, predictionStronglyDisagree;
    private TextView annoyanceStronglyAgree, annoyanceAgree, annoyanceNeutral, annoyanceDisagree, annoyanceStronglyDisagree;
    private TextView reasonableStronglyAgree, reasonableAgree, reasonableNeutral, reasonableDisagree, reasonableStronglyDisagree;


    private EditText reasonEditext, reasonEditextVersionA;

    private int annoyanceValue, predictionValue, reasonableValue;
    private DatabaseReference mDatabaseReference;
    private String userid, version;
    private StringBuffer reasonValue = new StringBuffer();
    private Bundle extras = new Bundle();

    private Date currenttime;
    private CheckBox rain, snow, humidity, heat, wetFingers, dirtyFinger, cooking;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_experience_sampling);
        Log.e("ExperienceSampling", "startActivity");
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        currenttime = Calendar.getInstance().getTime();
        version = SharedPreferencesStorage.readSharedPreference(this, Constants.PREFERENCES, Constants.VERSION_KEY);


        SharedPreferences pref = getApplicationContext().getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        userid = pref.getString("user_id", "no id");

        initNotNowButton();
        initViewElements();
        Log.e("version", version);

        /*handle survey version based on notification version*/
        if (version.equals(Constants.VERSION_A)) {
            seekbarContainer.setVisibility(View.GONE);
            checkBoxContainer.setVisibility(View.VISIBLE);
        } else if (version.equals(Constants.VERSION_B)) {
            seekbarContainer.setVisibility(View.VISIBLE);
            checkBoxContainer.setVisibility(View.GONE);
        }
        setPredictionSeekbarListener();
        setAnnoyanceSeekbarListener();
        setReasonableSeekbarListener();

        initNextButton();
    }

    private void initNextButton() {
        Button nextButton = findViewById(R.id.next_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                extras.putInt("predictionValue", predictionValue);
                extras.putInt("annoyanceValue", annoyanceValue);
                extras.putInt("reasonableValue", reasonableValue);
                //extras.putString("switchValue", switchValue);
                //extras.putString("fingerErrorValue", fingerErrorValue);
                extras.putString("reasonFreeText", reasonEditext.getText().toString());
                extras.putString("reasonFreeTextVersionA", reasonEditextVersionA.getText().toString());
                extras.putString("reasonValueVersionA", reasonValue.toString());
                //startActivity(new Intent(ExperienceSamplingActivity.this, ExperienceSamplingActivity2.class));
                Intent i = new Intent(ExperienceSamplingActivity.this, ExperienceSamplingActivity2.class);
                i.putExtras(extras);
                Log.e("bundle", extras.toString());
                startActivity(i);
            }
        });
    }

    private void setReasonableSeekbarListener() {
        reasonableSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                reasonableValue = progress;
                switch (progress) {
                    case (0):
                        reasonableStronglyDisagree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.teal));
                        reasonableDisagree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        reasonableNeutral.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        reasonableAgree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        reasonableStronglyAgree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        break;
                    case (1):
                        reasonableStronglyDisagree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        reasonableDisagree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.teal));
                        reasonableNeutral.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        reasonableAgree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        reasonableStronglyAgree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        break;
                    case (2):
                        reasonableStronglyDisagree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        reasonableDisagree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        reasonableNeutral.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.teal));
                        reasonableAgree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        reasonableStronglyAgree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        break;
                    case (3):
                        reasonableStronglyDisagree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        reasonableDisagree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        reasonableNeutral.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        reasonableAgree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.teal));
                        reasonableStronglyAgree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        break;
                    case (4):
                        reasonableStronglyDisagree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        reasonableDisagree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        reasonableNeutral.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        reasonableAgree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        reasonableStronglyAgree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.teal));
                        break;
                    default:
                        //This code is executed when value of variable 'day'
                        //doesn't match with any of case above
                        reasonableDisagree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        reasonableDisagree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        reasonableNeutral.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        reasonableAgree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        reasonableStronglyAgree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        break;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekBar.getThumb().setColorFilter(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.teal), PorterDuff.Mode.SRC);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void setAnnoyanceSeekbarListener() {
        annoyanceSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                annoyanceValue = progress;
                switch (progress) {
                    case (0):
                        annoyanceStronglyDisagree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.teal));
                        annoyanceDisagree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        annoyanceNeutral.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        annoyanceAgree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        annoyanceStronglyAgree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        break;
                    case (1):
                        annoyanceStronglyDisagree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        annoyanceDisagree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.teal));
                        annoyanceNeutral.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        annoyanceAgree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        annoyanceStronglyAgree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        break;
                    case (2):
                        annoyanceStronglyDisagree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        annoyanceDisagree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        annoyanceNeutral.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.teal));
                        annoyanceAgree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        annoyanceStronglyAgree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        break;
                    case (3):
                        annoyanceStronglyDisagree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        annoyanceDisagree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        annoyanceNeutral.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        annoyanceAgree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.teal));
                        annoyanceStronglyAgree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        break;
                    case (4):
                        annoyanceStronglyDisagree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        annoyanceDisagree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        annoyanceNeutral.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        annoyanceAgree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        annoyanceStronglyAgree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.teal));
                        break;
                    default:
                        //This code is executed when value of variable 'day'
                        //doesn't match with any of case above
                        annoyanceStronglyDisagree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.teal));
                        annoyanceDisagree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        annoyanceNeutral.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        annoyanceAgree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        annoyanceStronglyAgree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        break;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekBar.getThumb().setColorFilter(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.teal), PorterDuff.Mode.SRC);

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
                        predictionStronglyDisagree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.teal));
                        predictionDisagree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        predictionNeutral.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        predictionAgree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        predictionStronglyAgree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        break;
                    case (1):
                        predictionStronglyDisagree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        predictionDisagree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.teal));
                        predictionNeutral.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        predictionAgree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        predictionStronglyAgree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        break;
                    case (2):
                        predictionStronglyDisagree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        predictionDisagree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        predictionNeutral.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.teal));
                        predictionAgree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        predictionStronglyAgree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        break;
                    case (3):
                        predictionStronglyDisagree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        predictionDisagree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        predictionNeutral.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        predictionAgree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.teal));
                        predictionStronglyAgree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        break;
                    case (4):
                        predictionStronglyDisagree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        predictionDisagree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        predictionNeutral.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        predictionAgree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        predictionStronglyAgree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.teal));
                        break;
                    default:
                        predictionStronglyDisagree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        predictionDisagree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        predictionNeutral.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        predictionAgree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        predictionStronglyAgree.setTextColor(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.dark_grey));
                        break;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekBar.getThumb().setColorFilter(ContextCompat.getColor(ExperienceSamplingActivity.this,R.color.teal), PorterDuff.Mode.SRC);

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

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

        reasonableSeekbar = findViewById(R.id.seekBarReasonable);
        reasonableStronglyDisagree = findViewById(R.id.reasonable_strongly_disagree);
        reasonableDisagree = findViewById(R.id.reasonable_disagree);
        reasonableNeutral = findViewById(R.id.reasonable_neutral);
        reasonableAgree = findViewById(R.id.reasonable_agree);
        reasonableStronglyAgree = findViewById(R.id.reasonable_strongly_agree);

        //sendButton = findViewById(R.id.send_button);
        reasonEditext = findViewById(R.id.reasonEdittext);
        reasonEditextVersionA = findViewById(R.id.reasonDefaultEdittext);

        rain = findViewById(R.id.rain_checkbox);
        snow = findViewById(R.id.snow_checkbox);
        humidity = findViewById(R.id.humidity_checkbox);
        heat = findViewById(R.id.heat_checkbox);
        wetFingers = findViewById(R.id.wetfingers_checkbox);
        dirtyFinger = findViewById(R.id.dirtyfingers_checkbox);
        cooking = findViewById(R.id.cooking_checkbox);
    }

    private void insertToFirebase() {
        //mDatabaseReference.child("logEvents").child(version).child(userid).child(currenttime.toString()).child("notNowButton").setValue("clicked");
        mDatabaseReference.child(version).child(userid).child(currenttime.toString()).child("notNowButton").setValue("clicked");

    }

    private void initNotNowButton() {
        Button notNowButton = findViewById(R.id.not_now_button);
        notNowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertToFirebase();
                finishAffinity();
            }
        });
    }

    //TODO
    public void onCheckboxClicked(View view) {
        //boolean checked = ((CheckBox) view).isChecked();
        //reasonValue = new StringBuffer();
        reasonValue.append("Rain check : ").append(rain.isChecked());
        reasonValue.append(", Snow check : ").append(snow.isChecked());
        reasonValue.append(", Humidity check :").append(humidity.isChecked());
        reasonValue.append(", Heat check :").append(heat.isChecked());
        reasonValue.append(", Wet Fingers check :").append(wetFingers.isChecked());
        reasonValue.append(", Dirty Fingers check :").append(dirtyFinger.isChecked());
        reasonValue.append(", Cooking check :").append(cooking.isChecked());
        Log.e("ExperienceSampling", reasonValue.toString());
    }

    /*send intermediate data to firebase when user closes the app (interrupts survey)
    so not all data are gone
    */
    @Override
    protected void onDestroy() {
        Log.e("ExperienceSampling", "onDestroy");
        mDatabaseReference.child(version).child(userid).child(currenttime.toString()).child("prediction-rate").setValue(predictionValue);
        mDatabaseReference.child(version).child(userid).child(currenttime.toString()).child("annoyance-rate").setValue(annoyanceValue);
        mDatabaseReference.child(version).child(userid).child(currenttime.toString()).child("reason-value-versionA").setValue(reasonValue.toString());

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
