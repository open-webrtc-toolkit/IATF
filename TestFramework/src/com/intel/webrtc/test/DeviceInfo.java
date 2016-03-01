package com.intel.webrtc.test;

/**
 * A general description of an available device.
 * TODO Add more common device parameters into this class.
 * @author xianglai
 *
 */
public class DeviceInfo {
    //physical device ID
    public String id;
    //Store the binded localPort to communicate with android socket server
    //TestController will send message to this port
    public String localPort;
    /**
     * @param deviceType
     *      the device's class name. For example, AndroidTestDevice.
     */
    protected DeviceInfo(String id, String string)
    {
        this.id = id;
        this.localPort = string;
    } 
}
