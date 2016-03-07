package com.intel.webrtc.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Abstract class of all client test controller which takes charge of communicating with
 * the server, sends heart beat, and provides APIs related to wait-notify.
 * Extend this class when you expand the test client to new platform.<br>
 * For android, this controller is running on cell phone, but for javascript, it
 * runs on the computer. There is a special client for clients behind socket.io
 * server: {@link .LockServer LockServer}.
 * @author bean
 *
 */
public abstract class ClientTestController {
    private static String TAG = "ClientTestController";
    // The static counting down port number of ClientTestController from 10085
    public static int port = 10085;
    // Record the local port of this client test controller. this is the socket
    // server port to communicate with test controller of test server.
    public int localport = port--;
    // Store the accepted socket to socket client on test server.
    protected Socket socketToServer;
    // Buffer of socket to test server
    protected BufferedReader bufferedReader;
    // Writer of socket to test server
    protected PrintWriter printWriter;
    // Flag of whether the printWriter has been initialized(handle sendMessage
    // error)
    private boolean printWriterInited;
    // To keep HeartBeatThread and listening thread to test server alive.
    // set to false whiling close ClientTestController and stop these threads.
    protected boolean alive = true;
    // Store the HeartBeatThread
    protected HeartBeatThread heartBeatThread;

    public ClientTestController() {
        socketToServer = null;
    }

    /**
     * Send a heartBeat message to server.
     */
    protected void heartBeat() {
        JSONObject message = new JSONObject();
        try {
            message.put(MessageProtocol.MESSAGE_TYPE, MessageProtocol.HEARTBEAT);
            sendMessage(message.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            Logger.e(TAG, "Error occured when creating heart beat message.");
        }
    }

    /**
     * Start a socket server to wait for the test server to connect.
     * This method should be called by all the subclass after setting the localPort.
     */
    protected synchronized void startServer() {
        new Thread() {
            public void run() {
                Logger.d(TAG, "creating socket start:");
                ServerSocket server = null;
                synchronized (ClientTestController.class) {
                    try {
                        Logger.d(TAG, "port:" + localport + ", on waiting");
                        server = new ServerSocket(localport);
                    } catch (UnknownHostException e) {
                        Logger.e(TAG, "Unknown host!");
                        e.printStackTrace();
                    } catch (IOException e) {
                        Logger.e(TAG, "Error occured while creating the socket.");
                        e.printStackTrace();
                        System.exit(1);
                    }
                }
                if (server != null) {
                    try {
                        socketToServer = server.accept();
                    } catch (IOException e) {
                        Logger.e(TAG, "Error occured while accepting new connection.");
                        e.printStackTrace();
                    }
                }
                if (socketToServer != null) {
                    try {
                        server.close();
                    } catch (IOException e) {
                        Logger.e(TAG, "Error occured while closing serverSocket.");
                        e.printStackTrace();
                    }
                    try {
                        bufferedReader = new BufferedReader(new InputStreamReader(socketToServer.getInputStream()));
                        printWriter = new PrintWriter(
                                new BufferedWriter(new OutputStreamWriter(socketToServer.getOutputStream())));
                        printWriterInited = true;
                    } catch (IOException e) {
                        Logger.e(TAG, "Error occured while creating reader and writer.");
                        e.printStackTrace();
                    }
                    heartBeatThread = new HeartBeatThread();
                    heartBeatThread.start();
                    listenToServer();
                    Logger.d(TAG, "creating socket end.");
                }
            }
        }.start();
    }

    /**
     * Listening to the socket buffer, after setting up a socket to the test server.
     */
    protected void listenToServer() {
        new Thread() {
            public void run() {
                while (alive) {
                    try {
                        String messageReceived = bufferedReader.readLine();
                        if (messageReceived == null) {
                            continue;
                        }
                        // strange
                        if (messageReceived.equals("null")) {
                            continue;
                        }
                        if (messageReceived.equals("End")) {
                            break;
                        }
                        dispatchMessage(messageReceived);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * Dispatch json formatted message from the socket to test server.
     * @param msg
     */
    protected void dispatchMessage(String msg) {
        JSONObject message;
        String messageType;
        try {
            message = new JSONObject(msg);
        } catch (JSONException e) {
            Logger.e(TAG, "Message [" + msg + "] is not a legal JSONObject.");
            e.printStackTrace();
            return;
        }
        try {
            messageType = message.getString(MessageProtocol.MESSAGE_TYPE);
            // TODO whether it is necessary to tell clients that the test ended,
            // and ask them to clean up.
            if (messageType.equals(MessageProtocol.NOTIFY)) {
                String notifyWhat = "";
                notifyWhat = message.getString(MessageProtocol.WHAT);
                if ("".equals(notifyWhat)) {
                    Logger.e(TAG, "Can not wait or notify an empty string.");
                    return;
                }
                notifyLocalObject(notifyWhat);
            } else if (messageType.equals(MessageProtocol.TEST_START)) {
                handleStartMessage(msg);
            } else {
                Logger.e(TAG, "Unknown message type: " + messageType);
            }
        } catch (JSONException e) {
            Logger.e(TAG, "message[ " + msg + " ] has illegal arguments!");
            e.printStackTrace();
        }
    }

    /**
     * The thread that regularly sends heart beat messages to server.
     */
    protected class HeartBeatThread extends Thread {
        @Override
        public void run() {
            while (alive) {
                try {
                    // TODO read from xml
                    Thread.sleep(4900);
                    heartBeat();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Close the socket and related buffers communicating with test server.
     */
    public void close() {
        alive = false;
        if (socketToServer != null) {
            try {
                printWriter.close();
                socketToServer.close();
            } catch (IOException e) {
                Logger.e(TAG, "Error occured when closing the socket.");
                e.printStackTrace();
                socketToServer = null;
            }
            socketToServer = null;
        }
    }

    /**
     * Send a message to the test server.
     * @param msg
     *     the message
     */
    protected void sendMessage(final String msg) {
        new Thread() {
            public void run() {
                if (printWriterInited) {
                    synchronized (printWriter) {
                        printWriter.println(msg);
                        printWriter.flush();
                    }
                }
            }
        }.start();
    }

    /**
     * Inform the server that the test case running on this device has finished.
     */
    public void testFinished() {
        JSONObject message = new JSONObject();
        try {
            message.put(MessageProtocol.MESSAGE_TYPE, MessageProtocol.TEST_FINISH);
            sendMessage(message.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            Logger.e(TAG, "Error occured when creating message.");
        }
    }

    /**
     * Inform the server that the test case running on this device is waiting for a remote object
     * @param objectId
     */
    public void informWaitForObject(String objectId) {
        JSONObject message = new JSONObject();
        try {
            message.put(MessageProtocol.MESSAGE_TYPE, MessageProtocol.WAIT);
            message.put(MessageProtocol.WHAT, objectId);
            sendMessage(message.toString());
            localWaitOperations(objectId);
        } catch (JSONException e) {
            e.printStackTrace();
            Logger.e(TAG, "Error occured when creating message.");
            RuntimeException re = new RuntimeException("JSONException");
            re.setStackTrace(e.getStackTrace());
            throw re;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Inform the server that the test case running on this device is notifying a client that waiting
     * for this remote object.
     * @param objectId
     *     the id of the object.
     */
    public void informNotifyObject(String objectId) {
        JSONObject message = new JSONObject();
        try {
            message.put(MessageProtocol.MESSAGE_TYPE, MessageProtocol.NOTIFY);
            message.put(MessageProtocol.WHAT, objectId);
            sendMessage(message.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            Logger.e(TAG, "Error occured when creating message.");
        }
    }

    /**
     * Inform the server that the test case running on this device is notifying
     * all client that waiting for this remote object.
     * @param objectId
     *     the id of the object.
     */
    public void notifyObjectForAll(String objectId) {
        JSONObject message = new JSONObject();
        try {
            message.put(MessageProtocol.MESSAGE_TYPE, MessageProtocol.NOTIFY_ALL);
            message.put(MessageProtocol.WHAT, objectId);
            sendMessage(message.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            Logger.e(TAG, "Error occured when creating message.");
        }
    }

    /**
     * Platform specific operations achieve waiting, called after send wait message to server.
     * @param objectId
     */
    public abstract void localWaitOperations(String objectId) throws InterruptedException;

    /**
     * Notify a local object. Called after receiving a notify message from the server.
     * @param objectId
     */
    public abstract void notifyLocalObject(String objectId);

    /**
     * Handle the start message
     * @param messageReceived
     */
    public abstract void handleStartMessage(String messageReceived);
}
