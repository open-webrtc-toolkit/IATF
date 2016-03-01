package com.intel.webrtc.test;

/**
 * Mainly for android to achieve wait-notify on test device.
 * If(the clientController provide the wait-notify support){
 *      implement this interface to make use of the mechanism.
 * }else{
 *      leave out this interface.
 * }
 * @author bean
 *
 */
public interface WaitNotifyLocalSupporter {
    void setController(ClientTestController controller);
    public void waitForObject(String objectId);
    public void notifyObject(String objectId);
    public void notifyObjectForAll(String objectId);
}
