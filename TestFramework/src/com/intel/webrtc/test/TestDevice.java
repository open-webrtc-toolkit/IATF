package com.intel.webrtc.test;

/**
 * An abstract of all test cases that run on a single device.
 * @author xianglai
 *
 */
public interface TestDevice {
    //Get logic device ID name
    public String getName();
    //Set logic device ID name
    public void setName(String name);
    //Register test methods in this logic device into testSuite or testCase
    public void addDeviceToSuite(TestSuite testSuite);
  //Register test defined method in this logic device into testSuite or testCase
    public void addDeviceToSuite(TestSuite testSuite,String caseName);
}
