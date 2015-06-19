package com.intel.webrtc.test;

import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;

import com.intel.webrtc.test.android.AndroidTestDevice;

import android.util.Pair;

/**
 * TestSuite is a combination of several single TestDevice.
 *
 * @author xianglai
 *
 */
public class TestSuite {
    private final static String TAG = "TestSuite";

    // TODO private LinkedList<Pair<String, Integer>> deviceRequired;
    private LinkedList<TestDevice> testDevices;

    private Hashtable<String, TestCase> testCases;

    public TestSuite() {
        testCases = new Hashtable<String, TestCase>();
        testDevices = new LinkedList<TestDevice>();
    }

    Hashtable<String, TestCase> getTestCases() {
        return testCases;
    }

    /**
     * Add a TestDevice into the TestSuite.
     * The TestSuite will scan the test methods of the new-coming TestDevice,
     * and add the TestDevice into or create corresponding TestCases.
     * @param testDevice
     */
    public void addTestDevice(TestDevice testDevice) {
        if (testDevice instanceof AndroidTestDevice) {
            Method[] methods = testDevice.getClass().getMethods();
            int methodNum = methods.length;
            for (int i = 0; i < methodNum; i++) {
                // If the method is a test method, add the device into
                // related TestCase
                String methodName = methods[i].getName();
                if (methodName.startsWith("test")) {
                    // If the TestCase already exists, add the device in.
                    if (testCases.containsKey(methodName)) {
                        testCases.get(methodName).addDevice(testDevice);
                    } else {
                        TestCase newTestCase = new TestCase(methodName);
                        newTestCase.addDevice(testDevice);
                        testCases.put(methodName, newTestCase);
                    }
                }
            }
        }
    }

    /**
     * Remove a TestDevice from the TestSuite.
     * @param testDeviceName
     *      The name of the TestDevice.
     */
    public void removeTestDevice(String testDeviceName) {
        // TODO
    }

    /**
     * Remove a TestDevice from the TestSuite.
     * @param testDevice
     *      The TestDevice going to remove.
     */
    public void removeTestDevice(TestDevice testDevice) {
        // TODO
    }

    /**
     * Remote a TestCase from the TestSuite.
     * @param testCaseName
     *      The name of the TestCase.
     */
    public void removeTestCase(String testCaseName) {
        // TODO
    }

    /**
     * Convert a TestSuite to String, so that you can print it for debugging.
     */
    public String toString() {
        // TODO need verification
        String resultString = "Testsuite:\n";
        Iterator<Entry<String, TestCase>> iterator = testCases.entrySet()
                .iterator();
        for (int i = 0; iterator.hasNext(); i++) {
            resultString += ("TestCase " + i + ": "
                    + iterator.next().getValue().toString() + "\n");
        }
        return resultString;
    }
}
