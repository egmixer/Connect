package com.example.connectapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.example.connectapp.R;
import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends BaseActivity {
    private static final int SPLASH_TIME_MS = 1000;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mAuth = FirebaseAuth.getInstance();
        Handler mHandler = new Handler();
        Runnable mRunnable = new Runnable() {
            @Override
            public void run() {
                // check if user is already logged in or not
                if (mAuth.getCurrentUser() != null) {
                    // if logged in redirect the user to user listing activity
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                } else {
                    // otherwise redirect the user to login activity
                    startActivity(new Intent(SplashActivity.this, SignInActivity.class));
                }
                finish();
            }
        };
        mHandler.postDelayed(mRunnable, SPLASH_TIME_MS);
    }
}
