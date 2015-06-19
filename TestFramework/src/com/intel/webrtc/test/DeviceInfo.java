package com.intel.webrtc.test;

/**
 * A general description of an available device.
 * TODO Add more common device parameters into this class.
 * @author xianglai
 *
 */
public class DeviceInfo {
    public String id;

    /**
     * @param deviceType
     *      the device's class name. For example, AndroidTestDevice.
     */
    protected DeviceInfo(String id)
    {
        this.id = id;
    }
}
