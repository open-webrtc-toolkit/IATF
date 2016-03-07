package com.intel.webrtc.test;

import com.corundumstudio.socketio.listener.*;
import com.corundumstudio.socketio.*;

/**
 * This class is responsible for passing lock between WaitNotifyManager and all platform clients behind it.<br>
 * It will start a socket.io server to broadcast all the lock messages and act as a proxy client(on behalf of
 * all platform clients behind it) to communicate with WaitNotifyManager.
 * @author bean
 *
 */
public class LockServer extends ClientTestController {
    // Debug tag
    private static String TAG = "LockServer";
    // Socket.io server for all the test clients behind lock server,
    // all the lock messages between these clients and wait-notify manager is
    // sent
    private SocketIOServer server;
    // TODO: these should be read in config file
    // The start message that JS clients will be waiting for, this should be in
    // accordance with
    // the initial waiting lock in beforeEach()
    public static String startJsLock = "STARTTEST";
    // The socket.io server port, all the test clients passing wait-notify
    // message through lock server will
    // connect to this server port.
    public static int lockServerPort = 9092;
    // DeviceInfo for LockServer
    public static String infoid = "lockserver";

    /**
     * Create a ClientTestController socket to test server and
     * start socket.io lock server for test clients, in order
     * to be a lock medium between test server and test clients.
     */
    public LockServer() {
        super();
        startServer();
    }

    public void start() {
        Configuration config = new Configuration();
        config.setHostname("localhost");
        config.setPort(lockServerPort);

        server = new SocketIOServer(config);
        /**
         * When a client connect to socket.io, it will send a lock message(for debug).
         */
        server.addConnectListener(new ConnectListener() {
            @Override
            public void onConnect(SocketIOClient client) {
                LockObject data = new LockObject("server received connect!");
                client.sendEvent("lockevent", data);
            }
        });
        /**
         * When socket.io receives lockevent message from client, it will inform the test server a notify message.
         */
        server.addEventListener("lockevent", LockObject.class, new DataListener<LockObject>() {
            @Override
            public void onData(SocketIOClient client, LockObject data, AckRequest ackRequest) {
                // Send notify message to WNManager
                informNotifyObject(data.getLock());
            }
        });
        /**
         * When socket.io receives waitlock message from client, it will inform the test server a wait message.
         */
        server.addEventListener("waitlock", LockObject.class, new DataListener<LockObject>() {
            @Override
            public void onData(SocketIOClient client, LockObject data, AckRequest ackRequest) {
                // Send wait message to WNManager
                informWaitForObject(data.getLock());
            }
        });
        server.start();
        try {
            Thread.sleep(Integer.MAX_VALUE);
        } catch (Exception e) {
            server.stop();
        }
    }

    @Override
    public void localWaitOperations(String objectId) throws InterruptedException {
        // No action is needed, unlike android
    }

    @Override
    public void notifyLocalObject(String objectId) {
        // lockevent is the notify lock message
        server.getBroadcastOperations().sendEvent("lockevent", new LockObject(objectId));
    }

    /**
     * LockServer will receive start message from test manager,
     * and it will broadcast this start message to all the waiting test clients.
     */
    @Override
    public void handleStartMessage(String messageReceived) {
        // all the js clients should call waitsFor() to wait startJsLock
        Logger.d(TAG, "LockServer received start message");
        server.getBroadcastOperations().sendEvent("lockevent", new LockObject(startJsLock));
    }

    @Override
    public void close() {
        super.close();
        Thread.interrupted();
    }
}

/**
 * Structure to store the lock string.
 * @author bean
 *
 */
class LockObject {
    private String lock;

    public String getLock() {
        return lock;
    }

    public void setLock(String lock) {
        this.lock = lock;
    }

    public LockObject() {
    }

    public LockObject(String lock) {
        super();
        this.lock = lock;
    }
}
