package com.intel.webrtc.test.javascript;

import java.util.LinkedList;
import com.intel.webrtc.test.ClientTestController;

/**
 * Client test Controller on Javascript(for JS, only used to send heartbeat to test server).
 * @author bean
 *
 */
// TODO: may remove this class when local wait-notify and disconnect checking is
// not needed. Karma provides the reconnect after disconnect.
public class JavascriptClientController extends ClientTestController {
    public static LinkedList<JavascriptClientController> jsControllers = new LinkedList<JavascriptClientController>();

    public JavascriptClientController() {
        super();
        jsControllers.add(this);
        // start socket server to communicate with test server.
        startServer();
    }

    // all the three operations are empty for JS, because the wait-notify is
    // supported in the js test code.
    @Override
    public void localWaitOperations(String objectId) throws InterruptedException {
    }

    @Override
    public void notifyLocalObject(String objectId) {
    }

    @Override
    public void handleStartMessage(String messageReceived) {
    }
}
