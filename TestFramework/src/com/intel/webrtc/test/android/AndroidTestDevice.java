package com.intel.webrtc.test.android;

import android.app.Activity;
import android.util.Log;

import com.intel.webrtc.test.ClientTestController;
import com.intel.webrtc.test.Logger;
import com.intel.webrtc.test.TestCase;
import com.intel.webrtc.test.TestDevice;
import com.intel.webrtc.test.TestSuite;

import java.lang.reflect.Method;
import java.util.Hashtable;

import org.junit.Assert;

/**
 * An abstract of all test logic that run on a single Android device. <br>
 * It is the base class for users to write their own test cases. <br>
 * AndroidTestDevice is a subclass of org.junit.Assert, so users can use <br>
 * assertions in this class.
 * For andrid, it contains the test cases.
 * Note: the test function name should start with prefix "test"
 *
 * @author xianglai
 *
 */
public class AndroidTestDevice extends Assert implements TestDevice {

    // AndroidTestDevice take advantage of the Instrumentation via this
    // interface.
    // TODO extends this interface to add more functions, i.e. runOnUiThread().
    interface InstrumentationWrapper {
        Activity getActivity();
    }

    private final static String TAG = "Woogeen-AndroidTestDevice";

    private String deviceName = "";
    private ClientTestController controller;
    private InstrumentationWrapper testEntry;
    public Activity testActivity;

    // TODO add wait-notify mechanism

    /**
     *
     */
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
     * @param entry
     */
    // TODO Should the name of this method be changed to
    // setInstrumetationWrapper?
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
    // TODO When the dependency issue is solved, amend this method and return
    // the activity directly
    public void getActivity() {
        if (testEntry != null && testActivity == null)
            testActivity = testEntry.getActivity();
        return;
    }

    @Override
    public String getName() {
        return deviceName;
    }

    @Override
    public void setName(String name) {
        deviceName = name;
    }

    /**
     * Set the AndroidTestController to enable the communication with the server.
     * @param controller
     */
    //TODO: this should move to TestDevice
    @Override
    public void setController(ClientTestController controller) {
        this.controller = controller;
    }

    public void setUp() {
        // TODO
    }

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
        controller.waitForObject(objectId);
    }

    @Override
    /**
     * Notify one device that waiting for the specific remote Object.
     * @param objectId
     *    The id of the remote object
     */
    public void notifyObject(String objectId) {
        assertNotNull(controller);
        controller.notifyObject(objectId);
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

	@Override
	public void addDeviceToSuite(TestSuite testSuite) {
		Method[] methods = this.getClass().getMethods();
        int methodNum = methods.length;
        Hashtable<String, TestCase> testCases=testSuite.getTestCases();
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
