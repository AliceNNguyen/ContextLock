package com.example.alicenguyen.contextlock;

import android.content.Context;
import android.content.SharedPreferences;


/*Helper class to store data in SharedPreferences*/

public class SharedPreferencesStorage {
    /*get stored data from SharedPreferences*/
    public static String readSharedPreference(Context context, String spName, String key){
        SharedPreferences sharedPreferences = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
        return sharedPreferences.getString(key,"0");
    }

    /*write data to SharedPreferences*/
    public static void writeSharedPreference(Context context,String spName,String key, String value ){
        SharedPreferences sharedPreferences = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value );
        editor.commit();
    }
}
