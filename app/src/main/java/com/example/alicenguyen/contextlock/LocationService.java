package com.example.alicenguyen.contextlock;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

public class LocationService extends Service {


    private static final String TAG = "LocationService";
    private LocationManager mLocationManager = null;

    //flag for gps status
    boolean isGPSEnabled = false;
    // flag for network status
    boolean isNetworkEnabled = false;
    //private LocationListener mLocationListener;

    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;

        public LocationListener(String provider) {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);

        }

        @Override
        public void onLocationChanged(Location location) {
            Log.e(TAG, "onLocationChanged: " + location);
            Toast.makeText(getApplicationContext(),
                    "Successfully requested location updates",
                    Toast.LENGTH_SHORT)
                    .show();
            mLastLocation.set(location);
            broadcastActivity(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }



    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.NETWORK_PROVIDER),
            new LocationListener(LocationManager.GPS_PROVIDER),

    };

    /*LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.PASSIVE_PROVIDER)
    };*/




    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }


    @Override
    public void onCreate() {

        Log.e(TAG, "onCreate");

        initializeLocationManager();



        try {
            Log.e("provider network", String.valueOf(isNetworkEnabled));
            Log.e("provider gps", String.valueOf(isGPSEnabled));
            if(isNetworkEnabled) {

                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Constants.LOCATION_INTERVAL, Constants.LOCATION_DISTANCE, mLocationListeners[0]);
            }else if(isGPSEnabled)  {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Constants.LOCATION_INTERVAL, Constants.LOCATION_DISTANCE, mLocationListeners[1]);
            }
            /*mLocationManager.requestLocationUpdates(
                    LocationManager.PASSIVE_PROVIDER,
                    Constants.LOCATION_INTERVAL,
                    Constants.LOCATION_DISTANCE,
                    mLocationListeners[0]
            );*/
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                        return;
                    }
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listener, ignore", ex);
                }
            }
        }
    }

    private void broadcastActivity(Location location) {
        Intent intent = new Intent(Constants.BROADCAST_DETECTED_LOCATION);
        intent.putExtra("longitude", location.getLongitude());
        intent.putExtra("latitude", location.getLatitude());
        intent.putExtra("provider", location.getProvider());
        Log.e("lontitude", String.valueOf(location.getLongitude()));
        //sendBroadcast(intent);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager - LOCATION_INTERVAL: "+ Constants.LOCATION_INTERVAL + " LOCATION_DISTANCE: " + Constants.LOCATION_DISTANCE);
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
            // getting GPS status
            isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            // getting network status
            isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            Log.e("provider network abc", String.valueOf(isNetworkEnabled));
            Log.e("provider gps abc", String.valueOf(isGPSEnabled));

        }
    }
}
