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
    private String name;

    private LinkedList<TestDevice> testDevices;
    //store the platform device number
    private HashMap<String, Integer> platformCount;

    /**
     * This constructor is called by TestSuite to create a TestCase.
     * The name of the TestCase is equal to the test method's name.
     * @param name
     */
    public TestCase(String name) {
        this.name = name;
        testDevices = new LinkedList<TestDevice>();
        platformCount = new HashMap<String, Integer>();
    }

    public String getName() {
        return name;
    }

    public void addDevice(TestDevice device) {
        testDevices.add(device);
        String platformName=device.getClass().getName();
        if(platformCount.containsKey(platformName)){
            int newCount=platformCount.get(platformName)+1;
            platformCount.put(platformName, newCount);
        }else{
            platformCount.put(platformName, 1);
        }
    }

    protected void removeDevice(TestDevice device) {
        testDevices.remove(device);
    }

    public LinkedList<TestDevice> getDevices() {
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
