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

import android.os.Bundle;
import android.util.Log;

import com.google.android.things.contrib.driver.button.Button;
import com.google.android.things.contrib.driver.pwmservo.Servo;
import com.google.android.things.contrib.driver.rainbowhat.RainbowHat;

import java.io.IOException;

/**
 * Sample usage of the Gpio API that logs when a button is pressed.
 *
 */
public class ButtonActivity extends DeviceInfoActivity {
    private static final String TAG = ButtonActivity.class.getSimpleName();

    private Button mButtonA;
    private Button mButtonB;
    private Button mButtonC;
    private Servo servo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Starting ButtonActivity");

        connectButtonA();
        connectButtonB();
        connectButtonC();
        connectServo();
    }

    private void connectServo() {
        try {
            servo = RainbowHat.openServo();
            servo.setEnabled(false);
            servo.setAngleRange(0, 360.0);
            servo.setPulseDurationRange(0.9, 2.1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void connectButtonA() {
        try {
            String pinName = "BCM21";
            mButtonA = RainbowHat.openButton(pinName);
            mButtonA.setOnButtonEventListener(new Button.OnButtonEventListener() {
                @Override
                public void onButtonEvent(Button button, boolean pressed) {
                    try {
                        if (pressed) {
                            servo.setEnabled(true);
                            servo.setAngle(servo.getMaximumAngle());
                        } else {
                            servo.setEnabled(false);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        }
    }

    private void connectButtonB() {
        try {
            String pinName = "BCM20";
            mButtonC = RainbowHat.openButton(pinName);
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        }
    }

    private void connectButtonC() {
        try {
            String pinName = "BCM16";
            mButtonB = RainbowHat.openButton(pinName);
            mButtonB.setOnButtonEventListener(new Button.OnButtonEventListener() {
                @Override
                public void onButtonEvent(Button button, boolean pressed) {
                    try {
                        if (pressed) {
                            servo.setEnabled(true);
                            servo.setAngle(servo.getMinimumAngle());
                        } else {
                            servo.setEnabled(false);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mButtonA != null) {
            // Close the Gpio pin
            Log.i(TAG, "Closing Button GPIO pin");
            try {
                mButtonA.close();
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            } finally {
                mButtonA = null;
            }
        }
    }
}
