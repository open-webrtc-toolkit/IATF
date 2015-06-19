package com.intel.webrtc.test.demo;

import android.app.Activity;
import android.os.SystemClock;
import android.util.Log;

import com.intel.webrtc.test.android.AndroidTestDevice;

public class DemoTestDevice1 extends AndroidTestDevice {
    String lock = "lock", TAG = "DemoTestDevice1";

    public void testWaitNotify() {
        SystemClock.sleep(10000);
        notifyObject(lock);
        Log.d(TAG, "Test Finished");
    }

    public void testMethod1() {
    }
    //
    // public void testMethod_longName() {
    // }
    //
    // public void testMethod_longTestMethodName() {
    // }
    //
    // public void testAssertion() {
    // assertEquals("Assertion1", 1, 1);
    // assertEquals("Assertion2", 1, 2);
    // }
}
