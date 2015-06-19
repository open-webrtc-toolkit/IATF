package com.intel.webrtc.test;

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
    private String name;

    private LinkedList<TestDevice> testDevices;

    /**
     * This constructor is called by TestSuite to create a TestCase.
     * The name of the TestCase is equal to the test method's name.
     * @param name
     */
    TestCase(String name) {
        this.name = name;
        testDevices = new LinkedList<TestDevice>();
    }

    public String getName() {
        return name;
    }

    protected void addDevice(TestDevice device) {
        testDevices.add(device);
    }

    protected void removeDevice(TestDevice device) {
        testDevices.remove(device);
    }

    protected LinkedList<TestDevice> getDevices() {
        return testDevices;
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
