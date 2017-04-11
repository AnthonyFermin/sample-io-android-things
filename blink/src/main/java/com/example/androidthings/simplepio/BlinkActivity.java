/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.androidthings.simplepio;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.things.contrib.driver.apa102.Apa102;
import com.google.android.things.contrib.driver.ht16k33.AlphanumericDisplay;
import com.google.android.things.contrib.driver.pwmspeaker.Speaker;
import com.google.android.things.contrib.driver.rainbowhat.RainbowHat;
import com.google.android.things.pio.Gpio;

import java.io.IOException;

/**
 * Sample usage of the Gpio API that blinks an LED at a fixed interval defined in
 * {@link #intervalBetweenBlinksMs}.
 * <p>
 * Some boards, like Intel Edison, have onboard LEDs linked to specific GPIO pins.
 * The preferred GPIO pin to use on each board is in the {@link BoardDefaults} class.
 */
public class BlinkActivity extends Activity {
    private static final String TAG = BlinkActivity.class.getSimpleName();
    private static final int MINIMUM = 1000;
    private static final long DURATION = 10000;
    private static final int QUARTER = 250;
    public int intervalBetweenBlinksMs = 1000;
    public int counter = 60;

    private Handler mHandler = new Handler();
    private Gpio green;
    private Gpio red;
    private Gpio blue;
    private boolean ledState = false;
    private boolean piezoState = false;
    private AlphanumericDisplay display;
    private Speaker piezo;
    private Apa102 strip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
//        cleanup();
        countdown();
    }

    private void init() {
        try {
            green = RainbowHat.openLed(RainbowHat.LED_GREEN);
            red = RainbowHat.openLed(RainbowHat.LED_RED);
            blue = RainbowHat.openLed(RainbowHat.LED_BLUE);
            piezo = RainbowHat.openPiezo();
            display = RainbowHat.openDisplay();
            strip = RainbowHat.openLedStrip();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void cleanup() {
        try {
            green.close();
            red.close();
            blue.close();
            piezo.close();
            display.close();
            strip.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove pending blink Runnable from the handler.
        mHandler.removeCallbacks(mBlinkRunnable);
        // Close the Gpio pin.
        Log.i(TAG, "Closing LED GPIO pin");
        try {
            green.close();
            red.close();
            blue.close();
            piezo.close();
            display.close();
            strip.close();
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        } finally {
            green = null;
            red = null;
            blue = null;
            piezo = null;
            display = null;
            strip = null;
        }
    }

    private void defaultBlink() {
        try {
            green.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            red.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            blue.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            int[] rainbow = new int[7];
            for (int i = 0; i < rainbow.length; i++) {
                rainbow[i] = Color.HSVToColor(255, new float[]{i * 360.f / rainbow.length, 1.0f, 1.0f});
            }
            strip.write(rainbow);
            Log.i(TAG, "Start blinking LED GPIO pin");
            // Post a Runnable that continuously switch the state of the GPIO, blinking the
            // corresponding LED
            mHandler.post(mBlinkRunnable);
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        }
    }

    private Runnable mBlinkRunnable = new Runnable() {
        @Override
        public void run() {
            // Exit Runnable if the GPIO is already closed
            if (green == null || red == null || blue == null) {
                return;
            }

            try {
                // Toggle the GPIO state
                ledState = !ledState;
                green.setValue(ledState);
                red.setValue(ledState);
                blue.setValue(ledState);

                Log.d(TAG, "State set to " + ledState);

                // Reschedule the same runnable in {#intervalBetweenBlinksMs} milliseconds
                mHandler.postDelayed(mBlinkRunnable, intervalBetweenBlinksMs);
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
        }
    };

    private void countdown() {
        mHandler.post(countdownRunnable);
        mHandler.post(piezoRunnable);
    }

    private Runnable countdownRunnable = new Runnable() {
        @Override
        public void run() {
            if (display == null || piezo == null) {
                return;
            }

            try {
                display.display(counter);
                display.setEnabled(true);

                if (counter == 0) {
                    defaultBlink();
                } else {
                    mHandler.postDelayed(countdownRunnable, MINIMUM);
                }
                counter--;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    private Runnable piezoRunnable = new Runnable() {
        @Override
        public void run() {
            if (piezo == null) {
                return;
            }

            int delay;
            try {
                piezoState = !piezoState;
                if (piezoState) {
                    piezo.play(784.3);
                    delay = QUARTER;
                } else {
                    piezo.stop();
                    delay = QUARTER * 3;
                }
                if (counter < 0) {
                    piezo.stop();
                } else {
                    mHandler.postDelayed(piezoRunnable, delay);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };
}
