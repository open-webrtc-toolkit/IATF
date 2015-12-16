package com.intel.webrtc.test;

/**
 * An abstract of all test cases that run on a single device.
 * @author xianglai
 *
 */
public interface TestDevice {
    //Logic device ID
    public String getName();

    public void setName(String name);

    public void waitForObject(String objectId);

    public void notifyObject(String objectId);

    public void notifyObjectForAll(String objectId);
    
    public void addDeviceToSuite(TestSuite testSuite);

    void setController(ClientTestController controller);
}
