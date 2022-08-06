package com.buttless.client;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.facebook.AccessToken;
import com.facebook.Profile;

public class ActivitySplashScreen extends AppCompatActivity {

    private Boolean ItsTruth;
    private Intent i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

        ItsTruth = Profile.getCurrentProfile() != null && AccessToken.getCurrentAccessToken() != null;

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        Handler handler = new Handler();
        handler.postDelayed(() -> {
            if (!ItsTruth) {
                load(1);
            } else {
                load(0);
            }
        }, 3500);
    }

    private void load(int page){
        PreferenceManager prefManager = new PreferenceManager(this);
        if (!prefManager.isFirstTimeLaunch()) {
            if(page == 0){
                i = new Intent(getApplicationContext(), ActivityHome.class);
            } else if(page == 1){
                i = new Intent(getApplicationContext(), ActivityLogin.class);
            }
            startActivity(i);
            finish();
        } else {
            Intent i = new Intent(getApplicationContext(), ActivityWelcome.class);
            startActivity(i);
            finish();
        }
    }
}