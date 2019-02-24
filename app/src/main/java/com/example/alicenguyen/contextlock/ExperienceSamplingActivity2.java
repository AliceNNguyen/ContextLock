package com.example.alicenguyen.contextlock;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Date;

public class ExperienceSamplingActivity2 extends AppCompatActivity {

    private static final String PREFERENCES = "com.example.alicenguyen.contextlock";

    private ImageView sendButton;
    private EditText locationEditText;
    private String locationValue;
    private Date currenttime;
    private DatabaseReference mDatabaseReference;
    private SeekBar reasonableSeekbar;
    private TextView reasonableStronglyAgree, reasonableAgree, reasonableNeutral, reasonableDisagree, reasonableStronglyDisagree;
    private String userid, switchValue, fingerErrorValue, locationFreeText, reasonFreeText;
    private int predictionValue, annoyanceValue, reasonableValue;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_experience_sampling2);
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();


        Intent intent = getIntent();
        Bundle b = intent.getExtras();

        if(b!=null)
        {
            predictionValue = b.getInt("predictionValue");
            //reasonableValue = b.getInt("reasonableValue");
            fingerErrorValue = b.getString("fingerErrorValue");
            annoyanceValue = b.getInt("annoyanceValue");
            switchValue = b.getString("switchValue");
            reasonFreeText = b.getString("reasonFreeText");


            Log.e("extra", String.valueOf(predictionValue));
            Log.e("extra", String.valueOf(reasonableValue));
            Log.e("extra", String.valueOf(annoyanceValue));
            //Textv.setText(j);
        }

        SharedPreferences pref = getApplicationContext().getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        userid = pref.getString("user_id", "no id");
        Log.e("ExperienceSampling", userid);
        initViewElements();
        initSendButton();
        setReasonableSeekbarListener();



    }




    private void initViewElements() {
        sendButton = findViewById(R.id.send_button);
        locationEditText = findViewById(R.id.location_freetext);

        reasonableSeekbar = findViewById(R.id.seekBarReasonable);
        reasonableStronglyDisagree = findViewById(R.id.reasonable_strongly_disagree);
        reasonableDisagree = findViewById(R.id.reasonable_disagree);
        reasonableNeutral = findViewById(R.id.reasonable_neutral);
        reasonableAgree = findViewById(R.id.reasonable_agree);
        reasonableStronglyAgree = findViewById(R.id.reasonable_strongly_agree);
    }


    private void initSendButton(){
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            currenttime = Calendar.getInstance().getTime();

            //TODO set locationValue to database
            mDatabaseReference.child("users").child(userid).child(currenttime.toString()).child("lockscreen-switch-value").setValue(switchValue);
            mDatabaseReference.child("users").child(userid).child(currenttime.toString()).child("prediction-rate").setValue(predictionValue);
            mDatabaseReference.child("users").child(userid).child(currenttime.toString()).child("annoyance-rate").setValue(annoyanceValue);
            mDatabaseReference.child("users").child(userid).child(currenttime.toString()).child("reasonable-rate").setValue(reasonableValue);

            mDatabaseReference.child("users").child(userid).child(currenttime.toString()).child("fingererror-value").setValue(fingerErrorValue);
            mDatabaseReference.child("users").child(userid).child(currenttime.toString()).child("user-current-location").setValue(locationValue);
            mDatabaseReference.child("users").child(userid).child(currenttime.toString()).child("location-free-text").setValue(locationEditText.getText().toString());
            mDatabaseReference.child("users").child(userid).child(currenttime.toString()).child("reason-free-text").setValue(reasonFreeText);

            Toast.makeText(ExperienceSamplingActivity2.this, "gesendet", Toast.LENGTH_SHORT).show();
            finish();

            }
        });
    }

    private void setReasonableSeekbarListener() {
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
    }


    //TODO
    public void onLocationRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        RadioButton invehicleRadioButton = (RadioButton) findViewById(R.id.in_vehicle_radio_button);
        RadioButton atworkRadioButton = findViewById(R.id.at_work_radio_button);
        RadioButton atuniRadioButton = findViewById(R.id.at_university_radio_button);
        RadioButton workoutRadioButton = findViewById(R.id.workout_radio_button);
        RadioButton outdoorRadioButton = findViewById(R.id.outdoor_radio_button);
        RadioButton somethgelsekRadioButton = findViewById(R.id.something_else__radio_button);



        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.in_vehicle_radio_button:
                if (checked)
                    locationValue = invehicleRadioButton.getText().toString();

                break;
            case R.id.at_work_radio_button:
                if (checked)
                    locationValue = atworkRadioButton.getText().toString();
                break;
            case R.id.at_university_radio_button:
                if (checked)
                    locationValue = atuniRadioButton.getText().toString();
                break;
            case R.id.workout_radio_button:
                if (checked)
                    locationValue = workoutRadioButton.getText().toString();
                break;
            case R.id.outdoor_radio_button:
                if (checked)
                    locationValue = outdoorRadioButton.getText().toString();
                break;
            case R.id.something_else__radio_button:
                if (checked)
                    locationValue = somethgelsekRadioButton.getText().toString();
                break;
        }
    }

   /* public void onFingerErrorRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        RadioButton trueRadioButton = findViewById(R.id.fingerprint_error_true);
        RadioButton falseRadioButton = findViewById(R.id.fingerprint_error_false);


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
        }
    }*/
}

