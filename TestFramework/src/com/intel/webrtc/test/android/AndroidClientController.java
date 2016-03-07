package com.intel.webrtc.test.android;

import java.util.Hashtable;
import org.json.JSONException;
import org.json.JSONObject;
import android.util.Log;

import com.intel.webrtc.test.ClientTestController;
import com.intel.webrtc.test.MessageProtocol;

/**
 * ClientTestController on android platform.
 * @author bean
 *
 */
public class AndroidClientController extends ClientTestController {
    // Debug TAG
    private final static String TAG = "AndroidClientController";
    // Wrapper class of InstrumentationTestCase to enable auto test
    private AndroidTestEntry androidTestEntry;
    // String lock object table, to ensure lock with same String should mapping
    // to same String object.
    private Hashtable<String, String> locks;
    // Socket of ClientController, communicating with testController on server
    // side
    public static int androidLocalPort = 10086;

    /**
     * Start socket to communicate with testController.
     * @param androidTestEntry
     */
    public AndroidClientController(AndroidTestEntry androidTestEntry) {
        super();
        startServer();
        locks = new Hashtable<String, String>();
        this.androidTestEntry = androidTestEntry;
    }

    /**
     * Local wait support on android, wait for a String lock object.
     */
    public void localWaitOperations(String objectId) {
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
            try {
                lock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Notify local object with lock id.
     * Check up in lock object map and notify.
     */
    @Override
    public void notifyLocalObject(String objectId) {
        String lock = null;
        synchronized (locks) {
            if (locks.containsKey(objectId)) {
                lock = locks.get(objectId);
            } else {
                Log.e(TAG, "The local object [" + objectId + "] does not exist!");
                return;
            }
        }
        synchronized (lock) {
            lock.notify();
        }
    }

    /**
     * Waiting for startMessage from testController. Set up the test params in testEntry
     * with the infos in startMessage then notify testEntry to continue running the test
     * method.
     */
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
                androidTestEntry.setTestPackageAndActivity(testPackage, testActivity);
            }
        } catch (JSONException e) {
            Log.e(TAG, "message[ " + msg + " ] has illegal arguments!");
            e.printStackTrace();
        }
        synchronized (androidTestEntry) {
            androidTestEntry.notify();
        }
    }

    /**
     * Set localport then start socket server to communicate with testController.
     */
    @Override
    protected synchronized void startServer() {
        Log.d(TAG, "startServer called.");
        localport = androidLocalPort;
        super.startServer();
    }
}
