package com.lmu.alicenguyen.contextlock;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

/*enable offline method for Firebase*/
public class FirebaseHandler extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
