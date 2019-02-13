package com.example.alicenguyen.contextlock;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class WeatherListener {
    //private String url;
    private String OPEN_WEATHER_API_KEY = "18d997dfe947e33eb626ce588b9c7510";
    private Context context;

    // Instantiate the RequestQueue.
    RequestQueue queue = Volley.newRequestQueue(context);
    private String url ="http://www.google.com";
    private String weatherUrl = "http://dataservice.accuweather.com/locations/v1/cities/geoposition/search\n" + "\n";

    public WeatherListener (Context mContext) {
        context = mContext;
    }


    private void getWeatherData(){
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // Display the first 500 characters of the response string.
                //mTextView.setText("Response is: "+ response.substring(0,500));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //mTextView.setText("That didn't work!");
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }


    public String getWeatherContext(){
        return url;
    }


}
