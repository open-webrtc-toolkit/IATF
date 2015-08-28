package com.intel.webrtc.test.demo;

import com.intel.webrtc.test.android.AndroidTestDevice;
import android.os.SystemClock;
import android.util.Log;

public class DemoTestDevice1 extends AndroidTestDevice {
    String lock = "lock", TAG = "DemoTestDevice1";
    private final int waitingTime = 4000;

    public void testWaitNotify() {
        SystemClock.sleep(waitingTime);
        notifyObject(lock);
        Log.d(TAG, "Wait-Notify Test Finished");
    }

    public void testLoadActivity() {
        getActivity();
        Log.d(TAG, "Load activity success!");
    }
}
