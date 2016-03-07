package com.intel.webrtc.test;

/**
 * A general description of an available physical device.
 * @author xianglai
 *
 */
public class PhysicalDeviceInfo {
    // physical device ID
    public String id;
    // Store the binded localPort to communicate with socket server on test
    // clients.
    // TestController will send message to this port
    public String localPort;

    /**
     * @param id physical device ID.
     * @param string local port of socket server for testController to connect and send messages.
     */
    protected PhysicalDeviceInfo(String id, String string) {
        this.id = id;
        this.localPort = string;
    }
}
