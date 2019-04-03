package com.example.alicenguyen.contextlock;

/*Constants accessible from any class*/
public class Constants {
    /*constants for ActivityRecognitionClient*/
    public static final String BROADCAST_DETECTED_ACTIVITY = "activity_intent";
    static final long DETECTION_INTERVAL_IN_MILLISECONDS = 60 * 1000 * 60;//30 * 1000;
    public static final int CONFIDENCE = 70;

    /*constants for LocationListener*/
    public static final int LOCATION_INTERVAL = 1 * 1000 * 60;
    public static final float LOCATION_DISTANCE = 10f;
    public static final String WEATHER_KEY = "weather_key";


    public static final int JOB_SERVICE_INTERVAL = 60 * 60 * 1000;

    public static final String KEY_ID = "user_id";
    public static final String COUNTER_KEY = "counter_key";
    public static final String COOLDOWN_KEY = "cooldown_key";
    public static final String SWITCH_VERSION_KEY = "switch_version_key";
    public static final String STUDY_END_KEY = "study_end_key";
    public static final String VERSION_KEY = "version_key";


    public static final String NOTIFICATION_MESSAGE_KEY = "notification_message_key";
    public static final String NOTIFICATION_ICON_KEY = "notification_icon_key";
    public static final String NOTIFICATION_STORE_KEY = "notification_store_key";

    public static final String VERSION_A = "version_A";
    public static final String VERSION_B = "version_B";

    public static final String UNLOCK_COUNTER_KEY = "unlock_counter_key";


    public static final int NOTIFICATION_SEND_MAX_NUMBER = 5;
    public static final String LOCKSCREEN_SHOW_KEY = "lockscreen_show_key";

    public static final String LOCKSCREEN_STORED_KEY = "lockscreen_stored_key";
    public static final int LOCKSCREEN_SHOW_COUNTER = 50; //TODO to set
    public static final String UNLOCK_FAILURE_COUNTER = "unlock_failure_counter";

    public static final int SURVEY_COOLDOWN = 2; //possibility that survey opens, here 50% chance TODO to set


    public static final String NOTIFICATION_SEND_KEY = "notification_send_key";
    public static final int STUDY_LENGTH = 1;
    public static final String PREFERENCES = "com.example.alicenguyen.contextlock";
    public static final String FIRST_OPEN = "first_open_key";
    public static final String FIRST_OPEN_SURVEY = "first_open_survey_key";
    public static final String PERMISSION_AGREE = "permission_agree_key";
    public static final String FIRST_SETUP = "first_setup";

    public static final int NOTIFICATION_ID = 1;
    public static final String UNLOCK_METHOD_KEY = "unlock_method_key";

    public static final int CHECK_FINGERPRINT_TIMEOUT = 3000;
    public static final String PASSWORD_USED_KEY = "password_used_key";


}
