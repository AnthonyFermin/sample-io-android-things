package com.example.androidthings.simplepio;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.TextView;

/**
 * @author Anthony Fermin (Fuzz)
 */

public class DeviceInfoActivity extends Activity {

    private static final String TAG = DeviceInfoActivity.class.getSimpleName();
    private static final long FIVE_SECONDS = 5000;
    private TextView tvIpAddress;
    private TextView tvSsid;
    private TextView tvMac;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_info);
        bindViews();
        tvIpAddress.post(deviceInfoRunnable);
    }

    private void bindViews() {
        tvIpAddress = (TextView) findViewById(R.id.ipAddress);
        tvSsid = (TextView) findViewById(R.id.networkName);
        tvMac = (TextView) findViewById(R.id.macAddress);
    }


    private final Runnable deviceInfoRunnable = new Runnable() {

        @SuppressLint("HardwareIds")
        @Override
        public void run() {
            String ip = "";
            String ssid = "";
            String macAddress = "";
            try {
                WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
                ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
                ssid = wm.getConnectionInfo().getSSID();
                macAddress = wm.getConnectionInfo().getBSSID();
                Log.d(TAG, wm.getConnectionInfo().toString());
            } catch (Exception exception) {
                exception.printStackTrace();
            } finally {
                if (macAddress == null) macAddress = "";
                if (ip == null) ip = "";
                if (ssid == null) ssid = "";
            }

            tvMac.setText(macAddress);
            if (!ip.isEmpty() &&
                    !ssid.isEmpty()) {
                tvIpAddress.setText(ip);
                tvSsid.setText(ssid);
                Log.d(TAG, "Ip: " + ip);
            } else {
                tvIpAddress.postDelayed(this, FIVE_SECONDS);
            }
        }
    };
}
