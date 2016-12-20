package com.timarcher.robotcontrolsystemng;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import ioio.lib.api.DigitalOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;

public class MainActivity extends IOIOActivity {
    TextView txtIoioStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtIoioStatus = (TextView) findViewById(R.id.txtIoioStatus);
    }

    /**
     * A method to create our IOIO thread.
     *
     * @see ioio.lib.util.AbstractIOIOActivity#createIOIOThread()
     */
    @Override
    protected IOIOLooper createIOIOLooper() {
        return new Looper();
    }


    /**
     * This is the thread on which all the IOIO activity happens. It will be run
     * every time the application is resumed and aborted when it is paused. The
     * method setup() will be called right after a connection with the IOIO has
     * been established (which might happen several times!). Then, loop() will
     * be called repetitively until the IOIO gets disconnected.
     */
    class Looper extends BaseIOIOLooper {
        long loopCount = 0;

        /** The on-board LED. */
        private DigitalOutput led_;

        /**
         * Called every time a connection with IOIO has been established.
         * Typically used to open pins.
         *
         * @throws ConnectionLostException
         *             When IOIO connection is lost.
         *
         * @see ioio.lib.util.AbstractIOIOActivity.IOIOThread#setup()
         */
        @Override
        protected void setup() throws ConnectionLostException {
            runOnUiThread(new Runnable() {
                public void run() {
                    txtIoioStatus.setText("Setting up.");
                    txtIoioStatus.setBackgroundColor(Color.GREEN);
                }
            });

            loopCount = 0;
            led_ = ioio_.openDigitalOutput(0, true);
        }


        /**
         * Called when the IOIO has been disconnected.
         *
         */
        @Override
        public void disconnected() {
            runOnUiThread(new Runnable() {
                public void run() {
                    txtIoioStatus.setText("Disconnected.");
                    txtIoioStatus.setBackgroundColor(Color.RED);
                }
            });
        }

        /**
         * Called repetitively while the IOIO is connected.
         *
         * @throws ConnectionLostException
         *             When IOIO connection is lost.
         *
         * @see ioio.lib.util.AbstractIOIOActivity.IOIOThread#loop()
         */
        @Override
        public void loop() throws ConnectionLostException, InterruptedException {
            loopCount ++;
            led_.write(false);	//Gotta write a 0 to the onboard led to get it to turn on.

            //
            //Since the IOIO runs in its own thread, need to update the view from the UI thread
            //
            runOnUiThread(new Runnable() {
                public void run() {
                    //Just some dummy status code
                    txtIoioStatus.setText("Looping: " + String.valueOf(loopCount));
                }
            });

            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
            }
        }

    }
}
