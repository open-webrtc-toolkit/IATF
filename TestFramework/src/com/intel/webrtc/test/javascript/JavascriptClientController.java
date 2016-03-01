package com.intel.webrtc.test.javascript;

import java.util.LinkedList;

import com.intel.webrtc.test.ClientTestController;

public class JavascriptClientController extends ClientTestController{
    public static LinkedList<JavascriptClientController> jsControllers=new LinkedList<JavascriptClientController>();
//only used to send heartbeat to test server.
    public JavascriptClientController() {
        super();
        jsControllers.add(this);
        startServer();
    }
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
