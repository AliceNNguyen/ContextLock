package com.example.alicenguyen.contextlock.experience_sampling;

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

import com.example.alicenguyen.contextlock.Constants;
import com.example.alicenguyen.contextlock.R;
import com.example.alicenguyen.contextlock.SharedPreferencesStorage;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Date;

public class ExperienceSamplingActivity2 extends AppCompatActivity {
    private static final String TAG = "ExperienceSampling2";

    private static final String PREFERENCES = "com.example.alicenguyen.contextlock";

    private ImageView sendButton;
    private EditText locationEditText;
    private String locationValue;
    private Date currenttime;
    private DatabaseReference mDatabaseReference;
    private String userid, version, reasonFreeText, reasonFreeTextVersionA, reasonValueVersionA;
    private int predictionValue, annoyanceValue, reasonableValue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_experience_sampling2);
        Intent intent = getIntent();
        Bundle b = intent.getExtras();

        if (b != null) {
            predictionValue = b.getInt("predictionValue");
            reasonableValue = b.getInt("reasonableValue");
            annoyanceValue = b.getInt("annoyanceValue");
            reasonFreeText = b.getString("reasonFreeText");
            reasonFreeTextVersionA = b.getString("reasonFreeTextVersionA");
            reasonValueVersionA = b.getString("reasonValueVersionA");
        }

        version = SharedPreferencesStorage.readSharedPreference(this, Constants.PREFERENCES, Constants.VERSION_KEY);
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


    private void initSendButton() {
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currenttime = Calendar.getInstance().getTime();
                mDatabaseReference = FirebaseDatabase.getInstance().getReference().child(version).child(userid).child(currenttime.toString());

               // mDatabaseReference.child(version).child(userid).child(currenttime.toString()).child("lockscreen-switch-value").setValue(switchValue);
                mDatabaseReference.child("prediction-rate").setValue(predictionValue);
                mDatabaseReference.child("annoyance-rate").setValue(annoyanceValue);
                mDatabaseReference.child("reasonable-rate").setValue(reasonableValue);
                mDatabaseReference.child("reason-value-A").setValue(reasonValueVersionA);

                //mDatabaseReference.child(version).child(userid).child(currenttime.toString()).child("fingererror-value").setValue(fingerErrorValue);
                mDatabaseReference.child("user-current-location").setValue(locationValue);
                mDatabaseReference.child("location-free-text").setValue(locationEditText.getText().toString());
                mDatabaseReference.child("reason-free-text-B").setValue(reasonFreeText);
                mDatabaseReference.child("reason-free-text-A").setValue(reasonFreeTextVersionA);

                Toast.makeText(ExperienceSamplingActivity2.this, "send", Toast.LENGTH_SHORT).show();
                //NotificationHelper.cancelNotification(ExperienceSamplingActivity2.this, Constants.NOTIFICATION_ID);
                finishAffinity();

            }
        });
    }

    public void onLocationRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        RadioButton invehicleRadioButton = findViewById(R.id.in_vehicle_radio_button);
        RadioButton atworkRadioButton = findViewById(R.id.at_work_radio_button);
        RadioButton atuniRadioButton = findViewById(R.id.at_university_radio_button);
        RadioButton workoutRadioButton = findViewById(R.id.workout_radio_button);
        RadioButton outdoorRadioButton = findViewById(R.id.outdoor_radio_button);
        RadioButton atHomeRadioButton = findViewById(R.id.at_home_radio_button);
        RadioButton somethgelsekRadioButton = findViewById(R.id.something_else__radio_button);

        // Check which radio button was clicked
        switch (view.getId()) {
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
            case R.id.at_home_radio_button:
                if (checked)
                    locationValue = atHomeRadioButton.getText().toString();
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
        Log.e(TAG, locationValue);
    }
}

