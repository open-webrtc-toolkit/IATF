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
    //TODO android-version?

    public enum AndroidDeviceType {
        DEVICE, EMULATOR, OFFLINE, UNAUTHORIZED
    };

    public AndroidDeviceInfo(String serialId, AndroidDeviceType deviceType,
            String ip) {
        super(serialId, portStart++);
        this.deviceType = deviceType;
        this.ip = ip;
    }

    public String toString() {
        return "Serial Id: " + id + ", Type:" + deviceType.name()
                + ", Ip: " + ip+ ", localPort: " + localPort;
    }
}
