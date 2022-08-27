package com.buttless.client;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.Profile;
import com.facebook.ProfileTracker;

import java.util.Objects;

public class ActivitySplashScreen extends AppCompatActivity {

    public static final String PREFS_NAME = "FB_DTLHS";

    private Boolean ItsTruth;
    private Intent i;
    private ProfileTracker mProfileTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

        ItsTruth = Profile.getCurrentProfile() != null && AccessToken.getCurrentAccessToken() != null;
//        Log.d("API123 PROFILE", String.valueOf(Profile.getCurrentProfile()));
//        Log.d("API123 PROFILE2", String.valueOf(AccessToken.getCurrentAccessToken()));
//        Log.v("API123 facebook", Profile.getCurrentProfile().getFirstName());
//        Log.v("API123 facebook", String.valueOf(ItsTruth));

        if(Profile.getCurrentProfile() == null) {
            mProfileTracker = new ProfileTracker() {
                @Override
                protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                    Log.v("facebook - profile", currentProfile.getFirstName());
                    mProfileTracker.stopTracking();
                }
            };
            // no need to call startTracking() on mProfileTracker
            // because it is called by its constructor, internally.
        }
        else {
            Profile profile = Profile.getCurrentProfile();
            Log.v("facebook - profile", profile.getFirstName());
        }
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
        SharedPreferences shaPrefHome = Objects.requireNonNull(ActivitySplashScreen.this.getSharedPreferences(PREFS_NAME, MODE_PRIVATE));
        String storageUser = shaPrefHome.getString("fb_id", "");
        Profile.getCurrentProfile();

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