package com.intel.webrtc.test.android;

import android.app.Activity;
import java.lang.reflect.Method;
import java.util.Hashtable;
import org.junit.Assert;

import com.intel.webrtc.test.ClientTestController;
import com.intel.webrtc.test.Logger;
import com.intel.webrtc.test.TestCase;
import com.intel.webrtc.test.TestDevice;
import com.intel.webrtc.test.TestSuite;
import com.intel.webrtc.test.WaitNotifyLocalSupporter;

/**
 * An abstract of all test logic that run on a single Android device. <br>
 * It is the base class for users to write their own test cases.
 * AndroidTestDevice is a subclass of org.junit.Assert, so users can use
 * assertions in this class.<br>
 * For android, it contains the test cases.
 * Note: the test function name should start with prefix "test"
 * @author xianglai
 */
public class AndroidTestDevice extends Assert implements TestDevice, WaitNotifyLocalSupporter {
    // Debug TAG
    private final static String TAG = "Woogeen-AndroidTestDevice";

    // AndroidTestDevice take advantage of the Instrumentation via this
    // interface.
    // TODO extends this interface to add more functions, i.e. runOnUiThread().
    interface InstrumentationWrapper {
        Activity getActivity();
    }

    // logic devic name
    private String deviceName = "";
    // client test controller, to offer wait-notify support
    private ClientTestController controller;
    // android instrument entry
    private InstrumentationWrapper testEntry;
    // test Activity
    public Activity testActivity;

    public AndroidTestDevice() {
        super();
        Logger.d(TAG, "Created successfully.");
        controller = null;
        testEntry = null;
    }

    /**
     * Set the TestEntry, in order to enable AndroidTestDevice to use
     * Instrumentation.
     *
     * @param entry AndroidTestEntry
     */
    public void setTestEntry(InstrumentationWrapper entry) {
        testEntry = entry;
    }

    /**
     * Get the test activity set in the configuration.<p>
     *
     * If the test activity has not been launched, it will be launched by
     *  the Instrumentation.<br>
     *  Because of the dependency issue, the activity will be assigned to the
     *   filed 'testActivity', rather than returned by this method.<br>
     *
     */
    public void getActivity() {
        // call android test entry to start the test activity
        if (testEntry != null && testActivity == null)
            testActivity = testEntry.getActivity();
        return;
    }

    /**
     * Get the device name
     */
    @Override
    public String getName() {
        return deviceName;
    }

    /**
     * Set the device name
     */
    @Override
    public void setName(String name) {
        deviceName = name;
    }

    /**
     * Set the AndroidTestController to enable the communication with the server.
     * @param controller
     */
    // TODO: this should move to TestDevice
    @Override
    public void setController(ClientTestController controller) {
        this.controller = controller;
    }

    /**
     * Device setUp, called after setting the test params by testEntry.
     */
    public void setUp() {
    }

    /**
     * Device tearDown, called after the test has finished.
     */
    public void tearDown() {
    }

    @Override
    /**
     * Wait for a remote Object, until notified.
     * @param objectId
     *    The id of the remote object
     */
    public void waitForObject(String objectId) {
        assertNotNull(controller);
        controller.informWaitForObject(objectId);
    }

    @Override
    /**
     * Notify one device that waiting for the specific remote Object.
     * @param objectId
     *    The id of the remote object
     */
    public void notifyObject(String objectId) {
        assertNotNull(controller);
        controller.informNotifyObject(objectId);
    }

    @Override
    /**
     * Notify all devices that waiting for the specific remote Object.
     * @param objectId
     *    The id of the remote object
     */
    public void notifyObjectForAll(String objectId) {
        assertNotNull(controller);
        controller.notifyObjectForAll(objectId);
    }

    /**
     * Add an android logic test device to testSuite.
     * It will register all the methods that starts with 'test' as a case.
     * testSuite will conbine all the devices under same testCase.
     */
    @Override
    public void addDeviceToSuite(TestSuite testSuite) {
        Method[] methods = this.getClass().getMethods();
        int methodNum = methods.length;
        Hashtable<String, TestCase> testCases = testSuite.getTestCases();
        for (int i = 0; i < methodNum; i++) {
            // If the method is a test method, add the device into
            // related TestCase
            String methodName = methods[i].getName();
            if (methodName.startsWith("test")) {
                // If the TestCase already exists, add the device in.
                if (testCases.containsKey(methodName)) {
                    testCases.get(methodName).addDevice(this);
                } else {
                    TestCase newTestCase = new TestCase(methodName);
                    newTestCase.addDevice(this);
                    testCases.put(methodName, newTestCase);
                }
            }
        }
    }
}
