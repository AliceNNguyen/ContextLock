package com.example.alicenguyen.contextlock;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

public class InitialSurvey2 extends AppCompatActivity {
    private static final String FINGER_ERROR_FREQUENCY_KEY = "error_frequency_id_key";
    private TextView onceADay, onceAWeek, onceAMonth, multipeADay, mulipleAWeek, lessAMonth;
    private String value;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_survey2);
        initButtons();
    }

    @Override
    protected void onResume() {
        super.onResume();
        int pressedButtonId = Integer.parseInt(SharedPreferencesStorage.readSharedPreference(this, Constants.PREFERENCES, FINGER_ERROR_FREQUENCY_KEY));
        if(pressedButtonId > 0) {
            TextView pressedButton = findViewById(pressedButtonId);
            if(pressedButton != null) {
                pressedButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.checkmark, 0);
                pressedButton.setBackground(getDrawable(R.drawable.survey_btn_border_active));
                value = pressedButton.getText().toString();
            }

        }
    }

    private void initButtons() {
        onceADay = findViewById(R.id.once_a_day);
        onceAWeek = findViewById(R.id.once_a_week);
        onceAMonth = findViewById(R.id.once_a_month);
        multipeADay = findViewById(R.id.more_a_day);
        mulipleAWeek = findViewById(R.id.more_a_week);
        lessAMonth = findViewById(R.id.less_a_month);
    }

    private void savedPressedButtonToPreferences(int buttonId) {
        SharedPreferencesStorage.writeSharedPreference(this, Constants.PREFERENCES, FINGER_ERROR_FREQUENCY_KEY, String.valueOf(buttonId));
    }

    private void sendResultsToFirebase() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE);
        String userid = pref.getString("user_id", "no id");
        DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mDatabaseReference.child("initialSurvey").child(userid).child("errorFrequency").setValue(value);
    }


    public void openNext(View view) {
        Intent i = new Intent(this, InitialSurvey3.class);
        //i.putExtras(extras);
        //Log.e("bundle", extras.toString());
        sendResultsToFirebase();
        startActivity(i);
    }

    public void openPrevious(View view) {
        Intent i = new Intent(this, InitialSurvey.class);
        //i.putExtras(extras);
        //Log.e("bundle", extras.toString());
        startActivity(i);
    }

    public void onButtonClicked(View view) {
        switch (view.getId()) {
            case (R.id.once_a_day):
                //onceADay.setBackgroundColor(ContextCompat.getColor(this, R.color.button_active_color));
                onceADay.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.checkmark, 0);
                onceADay.setBackground(getDrawable(R.drawable.survey_btn_border_active));

                //onceAWeek.setBackgroundColor(ContextCompat.getColor(this, R.color.teal));
                onceAWeek.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                onceAWeek.setBackground(getDrawable(R.drawable.survey_btn_border));

                //onceAMonth.setBackgroundColor(ContextCompat.getColor(this, R.color.teal));
                onceAMonth.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                onceAMonth.setBackground(getDrawable(R.drawable.survey_btn_border));

                //multipeADay.setBackgroundColor(ContextCompat.getColor(this, R.color.teal));
                multipeADay.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                multipeADay.setBackground(getDrawable(R.drawable.survey_btn_border));

                //mulipleAWeek.setBackgroundColor(ContextCompat.getColor(this, R.color.teal));
                mulipleAWeek.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                mulipleAWeek.setBackground(getDrawable(R.drawable.survey_btn_border));

                //lessAMonth.setBackgroundColor(ContextCompat.getColor(this, R.color.teal));
                lessAMonth.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                lessAMonth.setBackground(getDrawable(R.drawable.survey_btn_border));

                break;
            case (R.id.once_a_week):
                //onceADay.setBackgroundColor(ContextCompat.getColor(this, R.color.teal));
                onceADay.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                onceADay.setBackground(getDrawable(R.drawable.survey_btn_border));

                //onceAWeek.setBackgroundColor(ContextCompat.getColor(this, R.color.button_active_color));
                onceAWeek.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.checkmark, 0);
                onceAWeek.setBackground(getDrawable(R.drawable.survey_btn_border_active));

                //onceAMonth.setBackgroundColor(ContextCompat.getColor(this, R.color.teal));
                onceAMonth.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                onceAMonth.setBackground(getDrawable(R.drawable.survey_btn_border));

                //multipeADay.setBackgroundColor(ContextCompat.getColor(this, R.color.teal));
                multipeADay.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                multipeADay.setBackground(getDrawable(R.drawable.survey_btn_border));

                //mulipleAWeek.setBackgroundColor(ContextCompat.getColor(this, R.color.teal));
                mulipleAWeek.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                mulipleAWeek.setBackground(getDrawable(R.drawable.survey_btn_border));

                //lessAMonth.setBackgroundColor(ContextCompat.getColor(this, R.color.teal));
                lessAMonth.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                lessAMonth.setBackground(getDrawable(R.drawable.survey_btn_border));
                break;
            case (R.id.once_a_month):
                //onceADay.setBackgroundColor(ContextCompat.getColor(this, R.color.teal));
                onceADay.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                onceADay.setBackground(getDrawable(R.drawable.survey_btn_border));

                //onceAWeek.setBackgroundColor(ContextCompat.getColor(this, R.color.teal));
                onceAWeek.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                onceAWeek.setBackground(getDrawable(R.drawable.survey_btn_border));

                //onceAMonth.setBackgroundColor(ContextCompat.getColor(this, R.color.button_active_color));
                onceAMonth.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.checkmark, 0);
                onceAMonth.setBackground(getDrawable(R.drawable.survey_btn_border_active));

                //multipeADay.setBackgroundColor(ContextCompat.getColor(this, R.color.teal));
                multipeADay.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                multipeADay.setBackground(getDrawable(R.drawable.survey_btn_border));

                //mulipleAWeek.setBackgroundColor(ContextCompat.getColor(this, R.color.teal));
                mulipleAWeek.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                mulipleAWeek.setBackground(getDrawable(R.drawable.survey_btn_border));

                //lessAMonth.setBackgroundColor(ContextCompat.getColor(this, R.color.teal));
                lessAMonth.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                lessAMonth.setBackground(getDrawable(R.drawable.survey_btn_border));
                break;
            case (R.id.more_a_day):
                //onceADay.setBackgroundColor(ContextCompat.getColor(this, R.color.teal));
                onceADay.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                onceADay.setBackground(getDrawable(R.drawable.survey_btn_border));

                //onceAWeek.setBackgroundColor(ContextCompat.getColor(this, R.color.teal));
                onceAWeek.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                onceAWeek.setBackground(getDrawable(R.drawable.survey_btn_border));

                //onceAMonth.setBackgroundColor(ContextCompat.getColor(this, R.color.teal));
                onceAMonth.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                onceAMonth.setBackground(getDrawable(R.drawable.survey_btn_border));

                //multipeADay.setBackgroundColor(ContextCompat.getColor(this, R.color.button_active_color));
                multipeADay.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.checkmark, 0);
                multipeADay.setBackground(getDrawable(R.drawable.survey_btn_border_active));

                //mulipleAWeek.setBackgroundColor(ContextCompat.getColor(this, R.color.teal));
                mulipleAWeek.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                mulipleAWeek.setBackground(getDrawable(R.drawable.survey_btn_border));

                //lessAMonth.setBackgroundColor(ContextCompat.getColor(this, R.color.teal));
                lessAMonth.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                lessAMonth.setBackground(getDrawable(R.drawable.survey_btn_border));
                break;
            case (R.id.more_a_week):
                //onceADay.setBackgroundColor(ContextCompat.getColor(this, R.color.teal));
                onceADay.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                onceADay.setBackground(getDrawable(R.drawable.survey_btn_border));

                //onceAWeek.setBackgroundColor(ContextCompat.getColor(this, R.color.teal));
                onceAWeek.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                onceAWeek.setBackground(getDrawable(R.drawable.survey_btn_border));

                //onceAMonth.setBackgroundColor(ContextCompat.getColor(this, R.color.teal));
                onceAMonth.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                onceAMonth.setBackground(getDrawable(R.drawable.survey_btn_border));

                //multipeADay.setBackgroundColor(ContextCompat.getColor(this, R.color.teal));
                multipeADay.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                multipeADay.setBackground(getDrawable(R.drawable.survey_btn_border));

                //mulipleAWeek.setBackgroundColor(ContextCompat.getColor(this, R.color.button_active_color));
                mulipleAWeek.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.checkmark, 0);
                mulipleAWeek.setBackground(getDrawable(R.drawable.survey_btn_border_active));

                lessAMonth.setBackgroundColor(ContextCompat.getColor(this, R.color.teal));
                lessAMonth.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                lessAMonth.setBackground(getDrawable(R.drawable.survey_btn_border));
                break;
            case (R.id.less_a_month):
                //onceADay.setBackgroundColor(ContextCompat.getColor(this, R.color.teal));
                onceADay.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                onceADay.setBackground(getDrawable(R.drawable.survey_btn_border));

                //onceAWeek.setBackgroundColor(ContextCompat.getColor(this, R.color.teal));
                onceAWeek.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                onceAWeek.setBackground(getDrawable(R.drawable.survey_btn_border));

                //onceAMonth.setBackgroundColor(ContextCompat.getColor(this, R.color.teal));
                onceAMonth.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                onceAMonth.setBackground(getDrawable(R.drawable.survey_btn_border));

                //multipeADay.setBackgroundColor(ContextCompat.getColor(this, R.color.teal));
                multipeADay.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                multipeADay.setBackground(getDrawable(R.drawable.survey_btn_border));

                //mulipleAWeek.setBackgroundColor(ContextCompat.getColor(this, R.color.teal));
                mulipleAWeek.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                mulipleAWeek.setBackground(getDrawable(R.drawable.survey_btn_border));

                //lessAMonth.setBackgroundColor(ContextCompat.getColor(this, R.color.button_active_color));
                lessAMonth.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.checkmark, 0);
                lessAMonth.setBackground(getDrawable(R.drawable.survey_btn_border_active));
                break;
        }
        value = ((TextView) view).getText().toString();
        savedPressedButtonToPreferences(view.getId());
    }
}
