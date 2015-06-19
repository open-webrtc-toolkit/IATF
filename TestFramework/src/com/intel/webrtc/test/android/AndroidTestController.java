package com.intel.webrtc.test.android;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Hashtable;

import org.json.JSONException;
import org.json.JSONObject;

import com.intel.webrtc.test.MessageProtocol;

import android.util.Log;

/**
 * An Android client of TestClient.
 * Take charge of communicating with the server, sends heart beat, and provides
 * APIs related to wait-notify.
 * @author xianglai
 *
 */
public class AndroidTestController {

    private final static String TAG = "AndroidTestController";
    private Socket socketToServer;
    private int port = 10086;
    private BufferedReader bufferedReader;
    private PrintWriter printWriter;
    private AndroidTestEntry androidTestEntry;
    private HeartBeatThread heartBeatThread;
    private Hashtable<String, String> locks;
    private boolean alive = true;

//    class TestInfo {
//        public final String ip, className, methodName;
//
//        public TestInfo(String ip, String className, String methodName) {
//            this.ip = ip;
//            this.className = className;
//            this.methodName = methodName;
//        }
//    }

    //TODO change AndroidTestEntry to an interface
    public AndroidTestController(AndroidTestEntry androidTestEntry) {
        socketToServer = null;
        locks = new Hashtable<String, String>();
        this.androidTestEntry = androidTestEntry;
        startServer();
    }

    private void startServer() {
        new Thread() {
            public void run() {
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
                }
            }
        }.start();
    }

    private void listenToServer() {
        new Thread() {
            public void run() {
                int time = 5;
                while (time > 0) {
                    try {
                        time--;
                        String messageReceived = bufferedReader.readLine();
                        Log.d(TAG, "Received [" + messageReceived + "].");
                        // TODO safe?
                        if (messageReceived != null) {
                            if (handleStartMessage(messageReceived))
                                break;
                        }
                    } catch (IOException e) {
                        Log.e(TAG,
                                "Error occured when listening to the start message.");
                        e.printStackTrace();
                    }
                }

                synchronized (androidTestEntry) {
                    androidTestEntry.notify();
                }

                while (alive) {
                    try {
                        String messageReceived = bufferedReader.readLine();
                        Log.d(TAG, "Received [" + messageReceived + "].");
                        // TODO safe?
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

    private boolean handleStartMessage(String msg) {
        JSONObject message;
        String messageType;
        try {
            message = new JSONObject(msg);
        } catch (JSONException e) {
            Log.e(TAG, "Message [" + msg + "] is not a legal JSONObject.");
            e.printStackTrace();
            return false;
        }
        try { // Get the message type from the jsonObject
            messageType = message.getString(MessageProtocol.MESSAGE_TYPE);
            // TODO whether it is necessary to tell clients that the test ended,
            // and ask them to clean up.
            if (messageType.equals(MessageProtocol.TEST_START)) {
                String className = null, methodName = null, testActivity = null, testPackage = null;
                className = message.getString(MessageProtocol.CLASS_NAME);
                if ("".equals(className)) {
                    Log.e(TAG, "Class name should not be null.");
                    return false;
                }
                methodName = message.getString(MessageProtocol.METHOD_NAME);
                if ("".equals(methodName)) {
                    Log.e(TAG, "Class name should not be null.");
                    return false;
                }
                testActivity = message.getString(MessageProtocol.TEST_ACTIVITY);
                testPackage = message.getString(MessageProtocol.TEST_PACKAGE);
                androidTestEntry.setTestClassAndMethod(className, methodName);
                androidTestEntry.setTestPackageAndActivity(testPackage,
                        testActivity);
                return true;
            }
        } catch (JSONException e) {
            Log.e(TAG, "message[ " + msg + " ] has illegal arguments!");
            e.printStackTrace();
        }
        return false;
    }

    private void dispatchMessage(String msg) {
        JSONObject message;
        String messageType;
        try {
            message = new JSONObject(msg);
        } catch (JSONException e) {
            Log.e(TAG, "Message [" + msg + "] is not a legal JSONObject.");
            e.printStackTrace();
            return;
        }
        try { // Get the message type from the jsonObject
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
            } else {
                Log.e(TAG, "Unknown message type: " + messageType);
            }
        } catch (JSONException e) {
            Log.e(TAG, "message[ " + msg + " ] has illegal arguments!");
            e.printStackTrace();
        }
    }

    /**
     * send a message to the test controller
     * @param msg
     *     the message
     */
    private void sendMessage(final String msg) {
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
    void testFinished() {
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
     * Wait for a remote object
     * @param objectId
     */
    void waitForObject(String objectId) {
        JSONObject message = new JSONObject();
        try {
            message.put(MessageProtocol.MESSAGE_TYPE, MessageProtocol.WAIT);
            message.put(MessageProtocol.WHAT, objectId);
            sendMessage(message.toString());
            String lock = null;
            synchronized (locks) {
                if (locks.containsKey(objectId)) {
                    lock = locks.get(objectId);
                } else {
                    lock = new String(objectId);
                    locks.put(objectId, lock);
                }
            }
            synchronized (lock) {
                lock.wait();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "Error occured when creating message.");
            RuntimeException re = new RuntimeException("JSONException");
            re.setStackTrace(e.getStackTrace());
            throw re;
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Notify a client that waiting for the remote object.
     * @param objectId
     *     the id of the object.
     */
    void notifyObject(String objectId) {
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
    void notifyObjectForAll(String objectId) {
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
     * notify a local object<br>
     * called when AndroidTestController receive a notify message from the server. 
     * @param objectId
     */
    void notifyLocalObject(String objectId) {
        String lock = null;
        synchronized (locks) {
            if (locks.containsKey(objectId)) {
                lock = locks.get(objectId);
            } else {
                Log.e(TAG, "The local object [" + objectId
                        + "] does not exist!");
                return;
            }
        }
        synchronized (lock) {
            lock.notify();
        }
    }

    /**
     * send a heartBeat message to server.
     */
    void heartBeat() {
        JSONObject message = new JSONObject();
        try {
            message.put(MessageProtocol.MESSAGE_TYPE, MessageProtocol.HEARTBEAT);
            sendMessage(message.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "Error occured when creating heart beat message.");
        }
    }

    /**
     * The thread that regularly sends heart beat messages to server.
     * @author xianglai
     *
     */
    private class HeartBeatThread extends Thread {
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

    void close() {
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
}
