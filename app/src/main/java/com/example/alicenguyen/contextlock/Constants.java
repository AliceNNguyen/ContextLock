package com.example.alicenguyen.contextlock;

public class Constants {
    public static final String BROADCAST_DETECTED_ACTIVITY = "activity_intent";

    static final long DETECTION_INTERVAL_IN_MILLISECONDS = 60 * 1000 * 60;//30 * 1000;

    public static final int CONFIDENCE = 70;

    public static final int LOCATION_INTERVAL = 60 * 1000 * 60;
    public  static final float LOCATION_DISTANCE = 10f;

    public static final String BROADCAST_DETECTED_LOCATION = "location_intent";

    //public static final String BROADCAST_DETECTED_GOOGLE_LOCATION = "google_location_intent";

    public static final int JOB_SERVICE_INTERVAL = 60 * 60 * 1000;
    public static final int NOTIFICATION_TIMEOUT = 5 * 60 * 1000;

    public static final String CHANNEL_ID = "context-notification-id";
    public static final String CHANNEL_ID_DEFAULT = "notification-default-id";

    public static final String KEY_ID = "user_id";
    public static final String COUNTER_KEY = "counter_key";
    public static final String DATE_KEY = "date_key";
    public static final String COOLDOWN_KEY = "cooldown_key";
    public static final String SWITCH_VERSION_KEY = "switch_version_key";
    public static final String VERSION_KEY = "version_key";

    public static final String VERSION_A = "version_A";
    public static final String VERSION_B = "version_B";

    public static final String UNLOCK_COUNTER_KEY = "unlock_counter_key";


    public static final int NOTIFICATION_SEND_MAX_NUMBER = 5;
    public static final String NOTIFICATION_SEND_KEY = "notification_send_key";
    public static final int SURVEY_OPEN_NUMBER = 6;
    public static final int STUDY_LENGTH = 1;
    public static final String PREFERENCES = "com.example.alicenguyen.contextlock";
    public static final String FIRST_OPEN = "first_open_key";
    public static final String FIRST_OPEN_SURVEY = "first_open_survey_key";
    public static final String PERMISSION_AGREE = "permission_agree_key";

    public static final int NOTIFICATION_ID = 1;
}
