package com.intel.webrtc.test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Every TestCase object stands for one test case. It contains all TestDevice
 * objects involved in this test case.
 * TestCase should be instantiated automatically by the TestSuite.
 * @author xianglai
 *
 */
public class TestCase {
    // Case Name
    private String name;
    // Device list in this case
    private LinkedList<TestDevice> testDevices;

    /**
     * This constructor is called by TestSuite to create a TestCase.
     * The name of the TestCase is equal to the test method's name.
     * @param name
     */
    public TestCase(String name) {
        this.name = name;
        testDevices = new LinkedList<TestDevice>();
    }

    /**
     * Return the case name.
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Get the devices in this TestCase.
     * @return testDevices devices in case.
     */
    public LinkedList<TestDevice> getDevices() {
        return testDevices;
    }

    /**
     * Add a device in a testCase and count devices under every platform.
     * @param device
     */
    public void addDevice(TestDevice device) {
        testDevices.add(device);
        String platformName = device.getClass().getName();
    }

    /**
     * Convert a TestCase to String, so that you can print it for debugging.
     */
    public String toString() {
        String returnValue = "TestCase name: " + name + " , Devices:";
        Iterator<TestDevice> iterator = testDevices.iterator();
        while (iterator.hasNext()) {
            returnValue += (" " + iterator.next().getName());
        }
        return returnValue;
    }
}
