package com.intel.webrtc.test.demo;

import android.util.Log;
import com.intel.webrtc.test.android.AndroidTestDevice;

public class DemoTestDevice2 extends AndroidTestDevice {
    String lock = "lock", TAG = "DemoTestDevice2";
    private final int waitingTime = 4000;

    public void testWaitNotify() {
        waitForObject(lock);
        Log.d(TAG, "Wait-Notify Test Finished");
    }

    public void testLoadActivity() {
        getActivity();
        Log.d(TAG, "Load activity success!");
    }
    public void testAssert(){
        assertEquals("Assert equal failed!", 1, 1);
    }
}
