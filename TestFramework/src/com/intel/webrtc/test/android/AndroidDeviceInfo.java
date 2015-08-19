package com.intel.webrtc.test.android;

import com.intel.webrtc.test.DeviceInfo;

/**
 * An abstract of an Android Device.
 *
 * @author xianglai
 *
 */
public class AndroidDeviceInfo extends DeviceInfo {
    public String ip;
    public AndroidDeviceType deviceType;
    private static int portStart=10087;
    //Store the binded localPort to communicate with android socket server
    public int localPort;
    //TODO android-version?

    public enum AndroidDeviceType {
        DEVICE, EMULATOR, OFFLINE, UNAUTHORIZED
    };

    public AndroidDeviceInfo(String serialId, AndroidDeviceType deviceType,
            String ip) {
        super(serialId);
        this.deviceType = deviceType;
        this.ip = ip;
        this.localPort = portStart++;
    }

    public String toString() {
        return "Serial Id: " + id + ", Type:" + deviceType.name()
                + ", Ip: " + ip+ ", localPort: " + localPort;
    }
}
