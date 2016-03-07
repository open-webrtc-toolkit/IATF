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
    /**
     * In order to call the ClientTestController to communicate with TestController
     * @param controller client test controller.
     */
    void setController(ClientTestController controller);

    /**
     * Fill local wait operations and call waitForObject in ClientTestController.
     * @param objectId
     */
    public void waitForObject(String objectId);

    /**
     * Fill local notify operations and call waitForObject in ClientTestController.
     * @param objectId
     */
    public void notifyObject(String objectId);

    /**
     * Fill local notify all operations and call waitForObject in ClientTestController.
     * @param objectId
     */
    public void notifyObjectForAll(String objectId);
}
