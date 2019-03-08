package com.example.alicenguyen.contextlock;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class InitialSurvey4 extends AppCompatActivity {
    private static final String TAG = "InitialSurvey4";
    private static final String ERROR_HANDLING_KEY = "error_handling_id_key";
    private static final String ERROR_HANDLING_FREE_KEY = "error_handling_free_id_key";
    private TextView ignore, switchAfterFirstAttempt, switchAfterMultipleAttempt, tryLater, notSure;
    private DatabaseReference mDatabaseReference;
    private String value, freeTextValue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_survey4);
        initButtons();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

    }

    @Override
    protected void onResume() {
        super.onResume();
        int pressedButtonId = Integer.parseInt(SharedPreferencesStorage.readSharedPreference(this, Constants.PREFERENCES, ERROR_HANDLING_KEY));
        if(pressedButtonId > 0) {
            TextView pressedButton = findViewById(pressedButtonId);
            if(pressedButton != null) {
                pressedButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.checkmark, 0);
                pressedButton.setBackground(getDrawable(R.drawable.survey_btn_border_active));
                value = pressedButton.getText().toString();
            }
        }
        freeTextValue = SharedPreferencesStorage.readSharedPreference(this, Constants.PREFERENCES, ERROR_HANDLING_FREE_KEY);
        if(!freeTextValue.equals("0")) {
            EditText editText = findViewById(R.id.errorHandlingEditText);
            if(editText != null) {
                editText.setText(freeTextValue);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        EditText errorHandlingEditText= findViewById(R.id.errorHandlingEditText);
        freeTextValue = errorHandlingEditText.getText().toString();
        SharedPreferencesStorage.writeSharedPreference(this, Constants.PREFERENCES, ERROR_HANDLING_FREE_KEY , freeTextValue);

    }

    private void initButtons() {
        ignore = findViewById(R.id.handle_ignore);
        switchAfterFirstAttempt = findViewById(R.id.handle_switch_first_attempt);
        switchAfterMultipleAttempt = findViewById(R.id.handle_switch_multiple_attempts);
        tryLater = findViewById(R.id.handle_later);
        notSure = findViewById(R.id.handle_not_sure);
    }

    private void savedPressedButtonToPreferences(int buttonId) {
        SharedPreferencesStorage.writeSharedPreference(this, Constants.PREFERENCES, ERROR_HANDLING_KEY, String.valueOf(buttonId));
    }

    public void sendResults(View view) {
       sendResultsToFirebase();
       Intent i = new Intent(this, MainActivity.class);
       startActivity(i);
       setResult(Activity.RESULT_OK);
    }

    private void sendResultsToFirebase() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE);
        String userid = pref.getString("user_id", "no id");
        DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mDatabaseReference.child("initialSurvey").child(userid).child("errorHandling").setValue(value);
        EditText errorHandlingEditText= findViewById(R.id.errorHandlingEditText);
        freeTextValue = errorHandlingEditText.getText().toString();
        mDatabaseReference.child("initialSurvey").child(userid).child("errorHandlingFreeText").setValue(freeTextValue);
    }



    public void openPrevious(View view) {
        Intent i = new Intent(this, InitialSurvey3.class);
        //i.putExtras(extras);
        //Log.e("bundle", extras.toString());
        startActivity(i);
    }

    public void onButtonClicked(View view) {
        switch (view.getId()) {
            case (R.id.handle_ignore):
                ignore.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.checkmark, 0);
                ignore.setBackground(getDrawable(R.drawable.survey_btn_border_active));

                switchAfterFirstAttempt.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                switchAfterFirstAttempt.setBackground(getDrawable(R.drawable.survey_btn_border));

                switchAfterMultipleAttempt.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                switchAfterMultipleAttempt.setBackground(getDrawable(R.drawable.survey_btn_border));

                tryLater.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                tryLater.setBackground(getDrawable(R.drawable.survey_btn_border));

                notSure.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                notSure.setBackground(getDrawable(R.drawable.survey_btn_border));
                break;
            case (R.id.handle_switch_first_attempt):
                ignore.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                ignore.setBackground(getDrawable(R.drawable.survey_btn_border));

                switchAfterFirstAttempt.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.checkmark, 0);
                switchAfterFirstAttempt.setBackground(getDrawable(R.drawable.survey_btn_border_active));

                switchAfterMultipleAttempt.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                switchAfterMultipleAttempt.setBackground(getDrawable(R.drawable.survey_btn_border));

                tryLater.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                tryLater.setBackground(getDrawable(R.drawable.survey_btn_border));

                notSure.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                notSure.setBackground(getDrawable(R.drawable.survey_btn_border));

                break;
            case (R.id.handle_switch_multiple_attempts):
                ignore.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                ignore.setBackground(getDrawable(R.drawable.survey_btn_border));

                switchAfterFirstAttempt.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                switchAfterFirstAttempt.setBackground(getDrawable(R.drawable.survey_btn_border));

                switchAfterMultipleAttempt.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.checkmark, 0);
                switchAfterMultipleAttempt.setBackground(getDrawable(R.drawable.survey_btn_border_active));

                tryLater.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                tryLater.setBackground(getDrawable(R.drawable.survey_btn_border));

                notSure.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                notSure.setBackground(getDrawable(R.drawable.survey_btn_border));

                break;
            case (R.id.handle_later):
                ignore.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                ignore.setBackground(getDrawable(R.drawable.survey_btn_border));

                switchAfterFirstAttempt.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                switchAfterFirstAttempt.setBackground(getDrawable(R.drawable.survey_btn_border));

                switchAfterMultipleAttempt.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                switchAfterMultipleAttempt.setBackground(getDrawable(R.drawable.survey_btn_border));

                tryLater.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.checkmark, 0);
                tryLater.setBackground(getDrawable(R.drawable.survey_btn_border_active));

                notSure.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                notSure.setBackground(getDrawable(R.drawable.survey_btn_border));

                break;
            case (R.id.handle_not_sure):
                ignore.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                ignore.setBackground(getDrawable(R.drawable.survey_btn_border));

                switchAfterFirstAttempt.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                switchAfterFirstAttempt.setBackground(getDrawable(R.drawable.survey_btn_border));

                switchAfterMultipleAttempt.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                switchAfterMultipleAttempt.setBackground(getDrawable(R.drawable.survey_btn_border));

                tryLater.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                tryLater.setBackground(getDrawable(R.drawable.survey_btn_border));

                notSure.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.checkmark, 0);
                notSure.setBackground(getDrawable(R.drawable.survey_btn_border_active));

                break;
        }
        value = ((TextView) view).getText().toString();
        savedPressedButtonToPreferences(view.getId());
    }
}
