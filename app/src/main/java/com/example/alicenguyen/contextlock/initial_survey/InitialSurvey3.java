package com.example.alicenguyen.contextlock.initial_survey;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.alicenguyen.contextlock.Constants;
import com.example.alicenguyen.contextlock.R;
import com.example.alicenguyen.contextlock.SharedPreferencesStorage;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class InitialSurvey3 extends AppCompatActivity {
    private static final String LOCKOUT_FREQUENCY_KEY = "lockout_frequency_id_key";
    private TextView never, onceADay, onceAWeek, onceAMonth, multipeADay, mulipleAWeek, lessAMonth;
    private String value = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_survey3);
        initButtons();
    }

    /*display button state if user select button previously*/
    @Override
    protected void onResume() {
        super.onResume();
        int pressedButtonId = Integer.parseInt(SharedPreferencesStorage.readSharedPreference(this, Constants.PREFERENCES, LOCKOUT_FREQUENCY_KEY));
        if (pressedButtonId > 0) {
            TextView pressedButton = findViewById(pressedButtonId);
            if (pressedButton != null) {
                pressedButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.checkmark, 0);
                pressedButton.setBackground(getDrawable(R.drawable.survey_btn_border_active));
                value = pressedButton.getText().toString();
            }
        }
    }

    /*save button state*/
    private void savedPressedButtonToPreferences(int buttonId) {
        SharedPreferencesStorage.writeSharedPreference(this, Constants.PREFERENCES, LOCKOUT_FREQUENCY_KEY, String.valueOf(buttonId));
    }

    private void initButtons() {
        never = findViewById(R.id.never);
        onceADay = findViewById(R.id.once_a_day);
        onceAWeek = findViewById(R.id.once_a_week);
        onceAMonth = findViewById(R.id.once_a_month);
        multipeADay = findViewById(R.id.more_a_day);
        mulipleAWeek = findViewById(R.id.more_a_week);
        lessAMonth = findViewById(R.id.less_a_month);
    }

    private void sendResultsToFirebase() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE);
        String userid = pref.getString("user_id", "no id");
        DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mDatabaseReference.child("initialSurvey").child(userid).child("lockoutFrequency").setValue(value);
    }


    public void openNext(View view) {
        sendResultsToFirebase();
        Intent i = new Intent(this, InitialSurvey4.class);
        startActivity(i);
    }

    public void openPrevious(View view) {
        Intent i = new Intent(this, InitialSurvey2.class);
        startActivity(i);
    }

    /*get selected button value and handle UI of buttons*/
    public void onButtonClicked(View view) {
        switch (view.getId()) {
            case (R.id.never):
                never.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.checkmark, 0);
                never.setBackground(getDrawable(R.drawable.survey_btn_border_active));

                onceADay.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                onceADay.setBackground(getDrawable(R.drawable.survey_btn_border));

                onceAWeek.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                onceAWeek.setBackground(getDrawable(R.drawable.survey_btn_border));

                onceAMonth.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                onceAMonth.setBackground(getDrawable(R.drawable.survey_btn_border));

                multipeADay.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                multipeADay.setBackground(getDrawable(R.drawable.survey_btn_border));

                mulipleAWeek.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                mulipleAWeek.setBackground(getDrawable(R.drawable.survey_btn_border));

                lessAMonth.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                lessAMonth.setBackground(getDrawable(R.drawable.survey_btn_border));

                break;
            case (R.id.once_a_day):
                never.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                never.setBackground(getDrawable(R.drawable.survey_btn_border));

                onceADay.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.checkmark, 0);
                onceADay.setBackground(getDrawable(R.drawable.survey_btn_border_active));

                onceAWeek.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                onceAWeek.setBackground(getDrawable(R.drawable.survey_btn_border));

                onceAMonth.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                onceAMonth.setBackground(getDrawable(R.drawable.survey_btn_border));

                multipeADay.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                multipeADay.setBackground(getDrawable(R.drawable.survey_btn_border));

                mulipleAWeek.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                mulipleAWeek.setBackground(getDrawable(R.drawable.survey_btn_border));

                lessAMonth.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                lessAMonth.setBackground(getDrawable(R.drawable.survey_btn_border));
                break;
            case (R.id.once_a_week):
                never.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                never.setBackground(getDrawable(R.drawable.survey_btn_border));

                onceADay.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                onceADay.setBackground(getDrawable(R.drawable.survey_btn_border));

                onceAWeek.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.checkmark, 0);
                onceAWeek.setBackground(getDrawable(R.drawable.survey_btn_border_active));

                onceAMonth.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                onceAMonth.setBackground(getDrawable(R.drawable.survey_btn_border));

                multipeADay.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                multipeADay.setBackground(getDrawable(R.drawable.survey_btn_border));

                mulipleAWeek.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                mulipleAWeek.setBackground(getDrawable(R.drawable.survey_btn_border));

                lessAMonth.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                lessAMonth.setBackground(getDrawable(R.drawable.survey_btn_border));
                break;
            case (R.id.once_a_month):
                never.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                never.setBackground(getDrawable(R.drawable.survey_btn_border));

                onceADay.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                onceADay.setBackground(getDrawable(R.drawable.survey_btn_border));

                onceAWeek.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                onceAWeek.setBackground(getDrawable(R.drawable.survey_btn_border));

                onceAMonth.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.checkmark, 0);
                onceAMonth.setBackground(getDrawable(R.drawable.survey_btn_border_active));

                multipeADay.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                multipeADay.setBackground(getDrawable(R.drawable.survey_btn_border));

                mulipleAWeek.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                mulipleAWeek.setBackground(getDrawable(R.drawable.survey_btn_border));

                lessAMonth.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                lessAMonth.setBackground(getDrawable(R.drawable.survey_btn_border));
                break;
            case (R.id.more_a_day):
                never.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                never.setBackground(getDrawable(R.drawable.survey_btn_border));

                onceADay.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                onceADay.setBackground(getDrawable(R.drawable.survey_btn_border));

                onceAWeek.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                onceAWeek.setBackground(getDrawable(R.drawable.survey_btn_border));

                onceAMonth.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                onceAMonth.setBackground(getDrawable(R.drawable.survey_btn_border));

                multipeADay.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.checkmark, 0);
                multipeADay.setBackground(getDrawable(R.drawable.survey_btn_border_active));

                mulipleAWeek.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                mulipleAWeek.setBackground(getDrawable(R.drawable.survey_btn_border));

                lessAMonth.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                lessAMonth.setBackground(getDrawable(R.drawable.survey_btn_border));
                break;
            case (R.id.more_a_week):
                never.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                never.setBackground(getDrawable(R.drawable.survey_btn_border));

                onceADay.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                onceADay.setBackground(getDrawable(R.drawable.survey_btn_border));

                onceAWeek.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                onceAWeek.setBackground(getDrawable(R.drawable.survey_btn_border));

                onceAMonth.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                onceAMonth.setBackground(getDrawable(R.drawable.survey_btn_border));

                multipeADay.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                multipeADay.setBackground(getDrawable(R.drawable.survey_btn_border));

                mulipleAWeek.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.checkmark, 0);
                mulipleAWeek.setBackground(getDrawable(R.drawable.survey_btn_border_active));

                lessAMonth.setBackgroundColor(ContextCompat.getColor(this, R.color.teal));
                lessAMonth.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                lessAMonth.setBackground(getDrawable(R.drawable.survey_btn_border));
                break;
            case (R.id.less_a_month):
                never.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                never.setBackground(getDrawable(R.drawable.survey_btn_border));

                onceADay.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                onceADay.setBackground(getDrawable(R.drawable.survey_btn_border));

                onceAWeek.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                onceAWeek.setBackground(getDrawable(R.drawable.survey_btn_border));

                onceAMonth.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                onceAMonth.setBackground(getDrawable(R.drawable.survey_btn_border));

                multipeADay.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                multipeADay.setBackground(getDrawable(R.drawable.survey_btn_border));

                mulipleAWeek.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                mulipleAWeek.setBackground(getDrawable(R.drawable.survey_btn_border));

                lessAMonth.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.checkmark, 0);
                lessAMonth.setBackground(getDrawable(R.drawable.survey_btn_border_active));
                break;
        }
        value = ((TextView) view).getText().toString();
        savedPressedButtonToPreferences(view.getId());
    }
}
