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

import android.util.Log;

/**
 * Take charge of communicating with the server, sends heart beat, and provides
 * APIs related to wait-notify.
 * Extend this class when you expand the test client to new platform.
 * @author bean
 *
 */
/*
 * TODO: for android, this controller is running on one device,
 * but for javascript, this controller can handle all javascript test client.
 */
public abstract class ClientTestController{
    private final static String TAG = "ClientTestController";
    //TODO: read from config file
    protected static final int port = 10086;
    protected Socket socketToServer;
    protected BufferedReader bufferedReader;
    protected PrintWriter printWriter;
    protected boolean alive = true;
    protected HeartBeatThread heartBeatThread;
    public ClientTestController() {
        socketToServer = null;
        startServer();
    }
    /**
     * send a heartBeat message to server.
     */
    protected void heartBeat() {
        JSONObject message = new JSONObject();
        try {
            message.put(MessageProtocol.MESSAGE_TYPE, MessageProtocol.HEARTBEAT);
            sendMessage(message.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "Error occured when creating heart beat message.");
        }
    }
    protected void startServer() {
        new Thread() {
            public void run() {
                Log.d(TAG, "creating socket start:");
                ServerSocket server = null;
                try {
                    server = new ServerSocket(port);
                } catch (UnknownHostException e) {
                    Log.e(TAG, "Unknown host!");
                    e.printStackTrace();
                } catch (IOException e) {
                    Log.e(TAG, "Error occured while creating the socket.");
                    e.printStackTrace();
                }
                if (server != null) {
                    try {
                        socketToServer = server.accept();
                    } catch (IOException e) {
                        Log.e(TAG,
                                "Error occured while accepting new connection.");
                        e.printStackTrace();
                    }
                }
                if (socketToServer != null) {
                    try {
                        server.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Error occured while closing serverSocket.");
                        e.printStackTrace();
                    }
                    try {
                        bufferedReader = new BufferedReader(
                                new InputStreamReader(
                                        socketToServer.getInputStream()));
                        printWriter = new PrintWriter(new BufferedWriter(
                                new OutputStreamWriter(
                                        socketToServer.getOutputStream())));
                    } catch (IOException e) {
                        Log.e(TAG,
                                "Error occured while creating reader and writer.");
                        e.printStackTrace();
                    }
                    heartBeatThread = new HeartBeatThread();
                    heartBeatThread.start();
                    listenToServer();
                    Log.d(TAG, "creating socket end.");
                }
            }
        }.start();
    }

    protected void listenToServer() {
        new Thread() {
            public void run() {
                while (alive) {
                    try {
                        String messageReceived = bufferedReader.readLine();
                        Log.d(TAG, "Received [" + messageReceived + "].");
                        // TODO safe?
                        if(messageReceived==null){
                            continue;
                        }
                        if (messageReceived.equals("End")) {
                            break;
                        }
                        dispatchMessage(messageReceived);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    protected void dispatchMessage(String msg) {
        JSONObject message;
        String messageType;
        try {
            message = new JSONObject(msg);
        } catch (JSONException e) {
            Log.e(TAG, "Message [" + msg + "] is not a legal JSONObject.");
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
                    Log.e(TAG, "Can not wait or notify an empty string.");
                    return;
                }
                notifyLocalObject(notifyWhat);
            } else if (messageType.equals(MessageProtocol.TEST_START)) {
                handleStartMessage(msg);
            }else {
                Log.e(TAG, "Unknown message type: " + messageType);
            }
        } catch (JSONException e) {
            Log.e(TAG, "message[ " + msg + " ] has illegal arguments!");
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
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    public void close() {
        alive = false;
        if (socketToServer != null) {
            try {
                socketToServer.close();
            } catch (IOException e) {
                Log.e(TAG, "Error occured when closing the socket.");
                e.printStackTrace();
                socketToServer = null;
            }
            socketToServer = null;
        }
    }

    /**
     * send a message to the test controller
     * @param msg
     *     the message
     */
    protected void sendMessage(final String msg) {
        new Thread() {
            public void run() {
                synchronized (printWriter) {
                    printWriter.println(msg);
                    printWriter.flush();
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
            message.put(MessageProtocol.MESSAGE_TYPE,
                    MessageProtocol.TEST_FINISH);
            sendMessage(message.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "Error occured when creating message.");
        }
    }

    /**
     * Inform the server that the test case running on this device is waiting for a remote object
     * @param objectId
     */
    public void waitForObject(String objectId) {
        JSONObject message = new JSONObject();
        try {
            message.put(MessageProtocol.MESSAGE_TYPE, MessageProtocol.WAIT);
            message.put(MessageProtocol.WHAT, objectId);
            sendMessage(message.toString());
            localWaitOperations(objectId);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "Error occured when creating message.");
            RuntimeException re = new RuntimeException("JSONException");
            re.setStackTrace(e.getStackTrace());
            throw re;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Notify a client that waiting for the remote object.
     * @param objectId
     *     the id of the object.
     */
    public void notifyObject(String objectId) {
        JSONObject message = new JSONObject();
        try {
            message.put(MessageProtocol.MESSAGE_TYPE, MessageProtocol.NOTIFY);
            message.put(MessageProtocol.WHAT, objectId);
            sendMessage(message.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "Error occured when creating message.");
        }
    }

    /**
     * Notify all client that waiting for the remote object.
     * @param objectId
     *     the id of the object.
     */
    public void notifyObjectForAll(String objectId) {
        JSONObject message = new JSONObject();
        try {
            message.put(MessageProtocol.MESSAGE_TYPE,
                    MessageProtocol.NOTIFY_ALL);
            message.put(MessageProtocol.WHAT, objectId);
            sendMessage(message.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "Error occured when creating message.");
        }
    }

    /**
     * Platform specific operations achieve waiting, called after send wait message to server.
     * @param objectId
     */
    public abstract void localWaitOperations(String objectId) throws InterruptedException ;
    /**
     * notify a local object<br>
     * called when AndroidTestController receive a notify message from the server. 
     * @param objectId
     */
    public abstract void notifyLocalObject(String objectId);
    /**
     * handle the start message
     * @param messageReceived
     */
    public abstract void handleStartMessage(String messageReceived);
}
