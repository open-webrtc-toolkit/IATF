package com.intel.webrtc.test.android;

import com.intel.webrtc.test.PhysicalDeviceInfo;

/**
 * An abstract of an Android Device.
 *
 * @author xianglai
 *
 */
public class AndroidDeviceInfo extends PhysicalDeviceInfo {
    //Currently, ip is not used after enhance the socket communication
    //between testController and clientTestController.
    public String ip;
    //The state of android device
    public AndroidDeviceType deviceType;
    //Starting port number of forward socket
    //Adb forward the message to clientTestController communicating port on android
    //from this port. (Check the details in android forward rules)
    private static int portStart=10087;
    //The enum of android device state
    public enum AndroidDeviceType {
        DEVICE, EMULATOR, OFFLINE, UNAUTHORIZED
    };

    public AndroidDeviceInfo(String serialId, AndroidDeviceType deviceType,
            String ip) {
        super(serialId, ""+portStart++);
        this.deviceType = deviceType;
        this.ip = ip;
    }

    public String toString() {
        return "Serial Id: " + id + ", Type:" + deviceType.name()
                + ", Ip: " + ip+ ", localPort: " + localPort;
    }
}
