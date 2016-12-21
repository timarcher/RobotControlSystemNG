package com.timarcher.robotcontrolsystemng.activities;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.timarcher.robotcontrolsystemng.MainActivity;
import com.timarcher.robotcontrolsystemng.R;

public class SplashScreenActivity extends AppCompatActivity {

    // Splash screen timer
    private static int SPLASH_TIME_OUT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
		//
		//Make the app full screen, turn off the title bar
		//
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		//getWindow().setIcon(0);
		//requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

		//
		//Remove the icon and title from the action bar
		//
		if (getActionBar() != null) {
			getActionBar().setDisplayShowHomeEnabled(false);	//icon
			getActionBar().setDisplayShowTitleEnabled(false);	//title text
		}

		//
		//Prevent the screen from going to sleep
		//
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        */


        setContentView(R.layout.activity_splash_screen);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity
                Intent i = new Intent(SplashScreenActivity.this, MainActivity.class);
                startActivity(i);

                // close this activity
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}
