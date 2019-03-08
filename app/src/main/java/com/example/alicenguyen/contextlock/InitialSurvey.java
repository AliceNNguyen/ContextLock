package com.example.alicenguyen.contextlock;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

public class InitialSurvey extends AppCompatActivity {

    public static String SEX = "sex";
    public static String AGE = "age";
    public static String PROFESSION = "profession";

    private static final String TAG = "InitialSurvey";
    private static final String PROFESSION_ID_KEY = "checkedProfessionButtonId";
    private static final String SEX_ID_KEY = "checkedSexButtonId";
    private static final String AGE_ID_KEY = "agedInputStringKey";
    private boolean checkedSex, checkedProfession;
    private String sex, profession, age;
    private RadioGroup radioGroupProfession, radioGroupSex;
    private Bundle extras = new Bundle();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_survey);
        Log.e(TAG, "onCreate");
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

    private void sendResultsToFirebase() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE);
        String userid = pref.getString("user_id", "no id");
        DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mDatabaseReference.child("initialSurvey").child(userid).child("sex").setValue(sex);
        mDatabaseReference.child("initialSurvey").child(userid).child("age").setValue(age);
        mDatabaseReference.child("initialSurvey").child(userid).child("profession").setValue(profession);
    }

    public void openNext(View view) {
        getAge();
        if(checkedProfession && checkedSex && !age.equals("")) {
            Intent i = new Intent(this, InitialSurvey2.class);
            /*extras.putString(SEX, sex);
            extras.putString(AGE, age);
            extras.putString(PROFESSION, profession);
            i.putExtras(extras);*/
            sendResultsToFirebase();
            startActivity(i);
        }else {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        radioGroupProfession = findViewById(R.id.radioGroupProfession);
        int idProfession = radioGroupProfession.getCheckedRadioButtonId();
        Log.e(TAG, "onPause");
        Log.e(TAG, String.valueOf(idProfession));
        SharedPreferencesStorage.writeSharedPreference(this, Constants.PREFERENCES, PROFESSION_ID_KEY, String.valueOf(idProfession));

        radioGroupSex = findViewById(R.id.sexRadioButtonGroup);
        int idSex = radioGroupSex.getCheckedRadioButtonId();
        SharedPreferencesStorage.writeSharedPreference(this, Constants.PREFERENCES, SEX_ID_KEY, String.valueOf(idSex));

        getAge();
        SharedPreferencesStorage.writeSharedPreference(this, Constants.PREFERENCES, AGE_ID_KEY, age);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
        int professionButtonId = Integer.parseInt(SharedPreferencesStorage.readSharedPreference(this, Constants.PREFERENCES, PROFESSION_ID_KEY));
        Log.e(TAG, String.valueOf(professionButtonId));
        if(professionButtonId > 0) {
            Log.e(TAG, String.valueOf(professionButtonId));
            RadioButton checkedProfessionButton = findViewById(professionButtonId);
            if(checkedProfessionButton != null) {
                checkedProfessionButton.setChecked(true);
                checkedProfession = true;
                profession = checkedProfessionButton.getText().toString();
            }

        }

        int sexButtonId = Integer.parseInt(SharedPreferencesStorage.readSharedPreference(this, Constants.PREFERENCES, SEX_ID_KEY));
        if(sexButtonId > 0) {
            RadioButton checkedSexButton = findViewById(sexButtonId);
            if(checkedSexButton != null) {
                checkedSexButton.setChecked(true);
                sex = checkedSexButton.getText().toString();
                checkedSex = true;
            }
        }

        String ageInput = SharedPreferencesStorage.readSharedPreference(this, Constants.PREFERENCES, AGE_ID_KEY);
        if(!ageInput.equals("0")) {
            EditText ageEditText = findViewById(R.id.age);
            if(ageEditText != null) {
                ageEditText.setText(ageInput);
                age = ageEditText.getText().toString();
            }
        }
        Log.e(TAG, ageInput);
    }
}