package com.example.alicenguyen.contextlock;

public class Constants {
    public static final String BROADCAST_DETECTED_ACTIVITY = "activity_intent";

    static final long DETECTION_INTERVAL_IN_MILLISECONDS = 10 * 1000 * 60;//30 * 1000;

    public static final int CONFIDENCE = 70;

    public static final int LOCATION_INTERVAL = 10 * 1000 * 60;
    public  static final float LOCATION_DISTANCE = 10f;

    public static final String BROADCAST_DETECTED_LOCATION = "location_intent";

    public static final String BROADCAST_DETECTED_GOOGLE_LOCATION = "google_location_intent";

    public static final int JOB_SERVICE_INTERVAL = 15 * 60 * 1000;

    public static final String CHANNEL_ID = "context-notification-id";

}
