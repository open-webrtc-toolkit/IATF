package com.intel.webrtc.test.demo;

import android.app.Activity;
import android.os.SystemClock;
import android.util.Log;

import com.intel.webrtc.test.android.AndroidTestDevice;

public class DemoTestDevice2 extends AndroidTestDevice {
    String lock = "lock", TAG = "DemoTestDevice2";

    public void testWaitNotify() {
        SystemClock.sleep(5000);
        getActivity();
        waitForObject(lock);
        Log.d(TAG, "Test Finished");
    }
}
