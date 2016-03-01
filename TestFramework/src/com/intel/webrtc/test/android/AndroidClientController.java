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

import com.intel.webrtc.test.ClientTestController;
import com.intel.webrtc.test.Logger;
import com.intel.webrtc.test.MessageProtocol;

import android.util.Log;

public class AndroidClientController extends ClientTestController{
    private final static String TAG = "AndroidClientController";
    private AndroidTestEntry androidTestEntry;
    private Hashtable<String, String> locks;
    public static int androidLocalPort=10086;
    
    public AndroidClientController(AndroidTestEntry androidTestEntry) {
        super();
        startServer();
        locks = new Hashtable<String, String>();
        this.androidTestEntry = androidTestEntry;
    }

    @Override
    public void localWaitOperations(String objectId) throws InterruptedException {
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
    }

    @Override
    public void notifyLocalObject(String objectId) {
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

    @Override
    public void handleStartMessage(String msg) {
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
            if (messageType.equals(MessageProtocol.TEST_START)) {
                String className = null, methodName = null, testActivity = null, testPackage = null;
                className = message.getString(MessageProtocol.CLASS_NAME);
                if ("".equals(className)) {
                    Log.e(TAG, "Class name should not be null.");
                }
                methodName = message.getString(MessageProtocol.METHOD_NAME);
                if ("".equals(methodName)) {
                    Log.e(TAG, "Class name should not be null.");
                }
                testActivity = message.getString(MessageProtocol.TEST_ACTIVITY);
                testPackage = message.getString(MessageProtocol.TEST_PACKAGE);
                androidTestEntry.setTestClassAndMethod(className, methodName);
                androidTestEntry.setTestPackageAndActivity(testPackage,
                        testActivity);
            }
        } catch (JSONException e) {
            Log.e(TAG, "message[ " + msg + " ] has illegal arguments!");
            e.printStackTrace();
        }
        synchronized (androidTestEntry) {
            androidTestEntry.notify();
        }
    }
    @Override
    protected synchronized void startServer() {
        Log.d(TAG, "startServer called.");
        localport=androidLocalPort;
        super.startServer();
    }
}
