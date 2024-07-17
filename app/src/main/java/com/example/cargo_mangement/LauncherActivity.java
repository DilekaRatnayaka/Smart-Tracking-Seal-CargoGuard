package com.example.cargo_mangement;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.content.Intent;
import android.os.Handler;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

public class LauncherActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 3000; // 3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        // Find the logo ImageView
        //ImageView logoImageView = findViewById(R.id.logoImageView);

        // Create animation
        //Animation fadeIn = new AlphaAnimation(0, 1);
        //fadeIn.setDuration(SPLASH_DURATION); // Set duration for the animation

        // Apply animation to ImageView
        //logoImageView.startAnimation(fadeIn);

        // Set a delay to transition to the next activity
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Start your main activity here
                startActivity(new Intent(LauncherActivity.this, LoginActivity.class));
                finish(); // Close the launcher activity
            }
        }, SPLASH_DURATION);
    }
}