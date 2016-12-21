package com.timarcher.robotcontrolsystemng;

import android.app.Activity;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.timarcher.robotcontrolsystemng.activities.DiagnosticsActivity;
import com.timarcher.robotcontrolsystemng.services.RobotInterfaceService;

/**
 * The main activity/application entry point for the app. Called from the
 * splash screen.
 */
public class MainActivity extends Activity {

    private RobotInterfaceService s;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Thread testThread = new Thread(new Runnable() {
            public void run() {
                try {
                    Looper.prepare();
                    for (;;) {
                        Thread.sleep(2000);
                        Log.d("app", "Service: " + s);
                        if (s != null) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    //Toast.makeText(getApplicationContext(), "Loop Count: " + s.getLoopCount(),
                                    //        Toast.LENGTH_SHORT).show();
                                }
                            });

                        }

                    }
                } catch (Exception e) {
                    //Need to figure out how to handle
                    Log.e("app", "Error calling service", e);
                }

            }
        });
        testThread.start();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case (R.id.action_diagnostics):
                this.startActivity(new Intent(this, DiagnosticsActivity.class));
                return true;
            case (R.id.action_settings):
                // this.startActivity(new Intent(this, MySecondActivity.class));
                // return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this, RobotInterfaceService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        startService(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(mConnection);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
            RobotInterfaceService.RobotInterfaceServiceBinder b = (RobotInterfaceService.RobotInterfaceServiceBinder) binder;
            s = b.getService();
            Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT)
                    .show();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            s = null;
        }
    };

}