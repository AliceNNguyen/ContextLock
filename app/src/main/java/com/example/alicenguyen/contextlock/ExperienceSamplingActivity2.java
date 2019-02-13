package com.example.alicenguyen.contextlock;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
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
    private String locationFreeText = "";
    private Date currenttime;
    private DatabaseReference mDatabaseReference;
    private String userid;
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
            predictionValue = b.getInt("predicationValue");
            reasonableValue = b.getInt("reasonableValue");
            annoyanceValue = b.getInt("annoyanceValue");
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



    }




    private void initViewElements() {
        sendButton = findViewById(R.id.send_button);
        locationEditText = findViewById(R.id.location_freetext);
    }


    private void initSendButton(){
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                currenttime = Calendar.getInstance().getTime();

                //TODO set locationValue to database
                mDatabaseReference.child("users").child(userid).child(currenttime.toString()).child("prediction-rate").setValue(predictionValue);
                mDatabaseReference.child("users").child(userid).child(currenttime.toString()).child("annoyance-rate").setValue(annoyanceValue);
                mDatabaseReference.child("users").child(userid).child(currenttime.toString()).child("reasonable-rate").setValue(reasonableValue);

                mDatabaseReference.child("users").child(userid).child(currenttime.toString()).child("user-current-location").setValue(locationValue);
                mDatabaseReference.child("users").child(userid).child(currenttime.toString()).child("location-free-text").setValue(locationFreeText);

                Toast.makeText(ExperienceSamplingActivity2.this, "gesendet", Toast.LENGTH_SHORT).show();
                finishAffinity();

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
}
