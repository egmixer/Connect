package com.example.connectapp;

import android.app.Application;

import com.example.connectapp.utils.PrefUtils;
import com.google.firebase.FirebaseApp;


public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        PrefUtils.with(this);
        FirebaseApp.initializeApp(this);
    }
}
