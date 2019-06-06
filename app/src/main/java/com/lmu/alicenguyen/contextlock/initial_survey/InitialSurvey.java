package com.lmu.alicenguyen.contextlock.initial_survey;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.lmu.alicenguyen.contextlock.Constants;
import com.lmu.alicenguyen.contextlock.R;
import com.lmu.alicenguyen.contextlock.SharedPreferencesStorage;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class InitialSurvey extends AppCompatActivity {

    private static final String TAG = "InitialSurvey";
    private static final String PROFESSION_ID_KEY = "checkedProfessionButtonId";
    private static final String SEX_ID_KEY = "checkedSexButtonId";
    private static final String AGE_ID_KEY = "agedInputStringKey";
    public static final String FALLBACK_ID_KEY = "fallbackButtonId";
    private static final String FALLBACK_OTHER_ID_KEY = "fallbackOtherId";
    private boolean checkedSex, checkedProfession, checkedFallback;
    private String sex, profession, age, fallbackUnlock, fallbackUnlockOther;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_survey);
    }


    public void getProfession(View view) {
        checkedProfession = ((RadioButton) view).isChecked();
        Log.e(TAG, String.valueOf(checkedProfession));
        switch (view.getId()) {
            case R.id.profession_student:
                if (checkedProfession) {
                    Log.e("survey", String.valueOf(checkedProfession));
                    profession = ((RadioButton) view).getText().toString();
                    PreferenceManager.getDefaultSharedPreferences(this).edit()
                            .putBoolean("checkedStudent", checkedProfession).apply();
                }
                break;
            case R.id.profession_postgraduate:
                if (checkedProfession) {
                    profession = ((RadioButton) view).getText().toString();
                    PreferenceManager.getDefaultSharedPreferences(this).edit()
                            .putBoolean("checkedPostgraduate", checkedProfession).apply();
                }
            case R.id.profession_employee:
                if (checkedProfession) {
                    profession = ((RadioButton) view).getText().toString();
                    PreferenceManager.getDefaultSharedPreferences(this).edit()
                            .putBoolean("checkedEmployee", checkedProfession).apply();
                }
            case R.id.profession_self_employed:
                if (checkedProfession) {
                    profession = ((RadioButton) view).getText().toString();
                    PreferenceManager.getDefaultSharedPreferences(this).edit()
                            .putBoolean("checkedSelfEmployed", checkedProfession).apply();
                }
            case R.id.profession_other:
                if (checkedProfession) {
                    profession = ((RadioButton) view).getText().toString();
                    PreferenceManager.getDefaultSharedPreferences(this).edit()
                            .putBoolean("checkedOther", checkedProfession).apply();
                }
        }
    }

    private void getAge() {
        EditText ageEditText = findViewById(R.id.age);
        age = ageEditText.getText().toString();
    }

    private void getFallBackOther() {
        EditText fallbackEditText = findViewById(R.id.unlock_other_edittext);
        fallbackUnlockOther = fallbackEditText.getText().toString();
    }

    public void onSexClick(View view) {
        checkedSex = ((RadioButton) view).isChecked();
        switch (view.getId()) {
            case R.id.female:
                if (checkedSex) {
                    sex = ((RadioButton) view).getText().toString();
                }
                break;
            case R.id.male:
                if (checkedSex) {
                    sex = ((RadioButton) view).getText().toString();
                }
        }
    }

    public void getFallbackMethod(View view) {
        checkedFallback = ((RadioButton) view).isChecked();
        switch (view.getId()) {
            case R.id.pattern:
                if (checkedSex) {
                    fallbackUnlock = ((RadioButton) view).getText().toString();
                }
                break;
            case R.id.pin:
                if (checkedSex) {
                    fallbackUnlock= ((RadioButton) view).getText().toString();
                }
            case R.id.unlock_other:
                if (checkedSex) {
                    fallbackUnlock = ((RadioButton) view).getText().toString();
                }
        }
        //Log.e(TAG, fallbackUnlock);
        SharedPreferencesStorage.writeSharedPreference(this, Constants.PREFERENCES, Constants.UNLOCK_METHOD_KEY, fallbackUnlock);


    }

    private void sendResultsToFirebase() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE);
        String userid = pref.getString("user_id", "no id");
        DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mDatabaseReference.child("initialSurvey").child(userid).child("sex").setValue(sex);
        mDatabaseReference.child("initialSurvey").child(userid).child("age").setValue(age);
        mDatabaseReference.child("initialSurvey").child(userid).child("profession").setValue(profession);
        mDatabaseReference.child("initialSurvey").child(userid).child("unlock_fallback").setValue(fallbackUnlock);
        mDatabaseReference.child("initialSurvey").child(userid).child("unlock_fallback_other").setValue(fallbackUnlockOther);

    }

    public void openNext(View view) {
        getAge();
        getFallBackOther();
        Log.e(TAG, String.valueOf(checkedFallback));
        if (checkedProfession && checkedSex && !age.equals("") && checkedFallback) {
            Intent i = new Intent(this, InitialSurvey2.class);
            sendResultsToFirebase();
            startActivity(i);
        } else {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_LONG).show();
        }
    }

    /*Save state of survey input, so that the user doesn't have to enter the data again if he jumps back and forth*/
    @Override
    protected void onPause() {
        super.onPause();
         RadioGroup radioGroupProfession = findViewById(R.id.radioGroupProfession);
        int idProfession = radioGroupProfession.getCheckedRadioButtonId();
        SharedPreferencesStorage.writeSharedPreference(this, Constants.PREFERENCES, PROFESSION_ID_KEY, String.valueOf(idProfession));

        RadioGroup radioGroupSex = findViewById(R.id.sexRadioButtonGroup);
        int idSex = radioGroupSex.getCheckedRadioButtonId();
        SharedPreferencesStorage.writeSharedPreference(this, Constants.PREFERENCES, SEX_ID_KEY, String.valueOf(idSex));

        RadioGroup radioGroupFallback = findViewById(R.id.radioGroupFallback);
        int idFallback = radioGroupFallback.getCheckedRadioButtonId();
        SharedPreferencesStorage.writeSharedPreference(this, Constants.PREFERENCES, FALLBACK_ID_KEY, String.valueOf(idFallback));

        getAge();
        SharedPreferencesStorage.writeSharedPreference(this, Constants.PREFERENCES, AGE_ID_KEY, age);

        getFallBackOther();
        SharedPreferencesStorage.writeSharedPreference(this, Constants.PREFERENCES, FALLBACK_OTHER_ID_KEY, fallbackUnlockOther);
    }

    /*Display state of previous survey input in view*/
    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
        int professionButtonId = Integer.parseInt(SharedPreferencesStorage.readSharedPreference(this, Constants.PREFERENCES, PROFESSION_ID_KEY));
        Log.e(TAG, String.valueOf(professionButtonId));
        if (professionButtonId > 0) {
            Log.e(TAG, String.valueOf(professionButtonId));
            RadioButton checkedProfessionButton = findViewById(professionButtonId);
            if (checkedProfessionButton != null) {
                checkedProfessionButton.setChecked(true);
                checkedProfession = true;
                profession = checkedProfessionButton.getText().toString();
            }
        }
        int sexButtonId = Integer.parseInt(SharedPreferencesStorage.readSharedPreference(this, Constants.PREFERENCES, SEX_ID_KEY));
        if (sexButtonId > 0) {
            RadioButton checkedSexButton = findViewById(sexButtonId);
            if (checkedSexButton != null) {
                checkedSexButton.setChecked(true);
                sex = checkedSexButton.getText().toString();
                checkedSex = true;
            }
        }
        int fallbackButtonId = Integer.parseInt(SharedPreferencesStorage.readSharedPreference(this, Constants.PREFERENCES, FALLBACK_ID_KEY ));
        if(fallbackButtonId > 0) {
            RadioButton checkedFallbackButton = findViewById(fallbackButtonId);
            if(checkedFallbackButton != null) {
                checkedFallbackButton.setChecked(true);
                fallbackUnlock = checkedFallbackButton.getText().toString();
                checkedFallback = true;
            }
        }
        String ageInput = SharedPreferencesStorage.readSharedPreference(this, Constants.PREFERENCES, AGE_ID_KEY);
        if (!ageInput.equals("0")) {
            EditText ageEditText = findViewById(R.id.age);
            if (ageEditText != null) {
                ageEditText.setText(ageInput);
                age = ageEditText.getText().toString();
            }
        }
        String fallbackInput = SharedPreferencesStorage.readSharedPreference(this, Constants.PREFERENCES, FALLBACK_OTHER_ID_KEY);
        if(!fallbackInput.equals("0")) {
            EditText fallbackEditText = findViewById(R.id.unlock_other_edittext);
            if(fallbackEditText != null) {
                fallbackEditText.setText(fallbackInput);
                fallbackUnlockOther = fallbackEditText.getText().toString();
            }
        }
        Log.e(TAG, ageInput);
    }

    @Override
    public void onBackPressed() { }


}