package com.elgo.android.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.elgo.lego.R;
import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * Created by Abhish3k on 7/25/2016.
 */

public class SplashActivity extends Activity {

    public FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        final Boolean secretCodeEntered = getSharedPreferences("userData", MODE_PRIVATE).getBoolean("secret_code_entered", false);

        Thread timerThread = new Thread() {
            public void run() {
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    if (secretCodeEntered) {
                        Intent preLogin = new Intent(SplashActivity.this, PreLoginActivity.class);
                        startActivity(preLogin);
                        finish();
                    } else {
                        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }
        };
        timerThread.start();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        finish();
    }
}
