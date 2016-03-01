package com.intel.webrtc.test;

import com.corundumstudio.socketio.listener.*;
import com.corundumstudio.socketio.*;

/**
 * This class is responsible for passing lock between WaitNotifyManager and all platform clients.
 * For WaitNotifyManager, it acts as a socket client. Send wait lock message and 
 * @author bean
 *
 */
public class LockServer extends ClientTestController {
    private SocketIOServer server;
    private static String TAG="LockServer";
    // TODO: this should be read in config file
    public static String startJsLock = "STARTTEST";
    public static int lockServerPort = 9092;
    public static String infoid="lockserver";
    public static String lockserverIp="127.0.0.1";

    public LockServer(){
        super();
        startServer();
    }
    public void start() {
        Configuration config = new Configuration();
        config.setHostname("localhost");
        config.setPort(lockServerPort);

        server = new SocketIOServer(config);
        server.addConnectListener(new ConnectListener() {
            @Override
            public void onConnect(SocketIOClient client) {
                LockObject data = new LockObject("server received connect!");
                client.sendEvent("lockevent", data);
            }
        });
        server.addEventListener("lockevent", LockObject.class, new DataListener<LockObject>() {
            @Override
            public void onData(SocketIOClient client, LockObject data, AckRequest ackRequest) {
                // TODO: Send notify message to WNManager
                notifyObject(data.getLock());
            }
        });
        server.addEventListener("waitlock", LockObject.class, new DataListener<LockObject>() {
            @Override
            public void onData(SocketIOClient client, LockObject data, AckRequest ackRequest) {
                // TODO: Send wait message to WNManager
                waitForObject(data.getLock());
            }
        });
        server.start();
        try {
            Thread.sleep(Integer.MAX_VALUE);
        } catch (Exception e) {
            server.stop();
        }
    }

    public static void main(String args[]) {
        LockServer ls = new LockServer();
        ls.start();
    }

    @Override
    public void localWaitOperations(String objectId) throws InterruptedException {
        // TODO No action is needed, unlike android
    }

    @Override
    public void notifyLocalObject(String objectId) {
        // TODO lockevent is the notify lock message
        server.getBroadcastOperations().sendEvent("lockevent", new LockObject(objectId));
    }

    @Override
    public void handleStartMessage(String messageReceived) {
        // TODO all the js clients should call waitsFor() to wait lock
        // startJsLock
        Logger.d(TAG,"LockServer received start message");
        server.getBroadcastOperations().sendEvent("lockevent", new LockObject(startJsLock));
    }
    @Override
    public void close() {
        super.close();
        Thread.interrupted();
    }
}
