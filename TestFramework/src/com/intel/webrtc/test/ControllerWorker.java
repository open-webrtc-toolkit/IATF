package com.intel.webrtc.test;

import java.io.BufferedReader;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class is responsible for listening messages from a specific test client.
 * In most cases, it waits for the inputed BufferedReader, which is connected
 * with the outputStream of the corresponding Socket.
 * @author xianglai
 *
 */
class ControllerWorker extends Thread {

    final private static String TAG = "ControllerWorker";

    private String deviceId;
    private ControllerWorkerObserver observer = null;
    private volatile boolean alive = true;

    private BufferedReader bufferedReader = null;

    /**
     * An interface used to observe the received messages.
     * @author xianglai
     *
     */
    interface ControllerWorkerObserver {
        /**
         * This method will be called if the testDevice has finished its test.
         * @param deviceId
         *     The id of the device
         */
        void onDeviceFinished(String deviceId);

        /**
         * This method will be called if the testDevice sent a message to wait
         * for an object.
         * @param deviceId
         *     The id of the device.
         * @param objectId
         *     The objectId that the device is waiting for.
         */
        void onWait(String deviceId, String objectId);

        /**
         * This method will be called if the testDevice sent a message to notify
         * an object.
         * @param deviceId
         *     The id of the device.
         * @param objectId
         *     The objectId that the device is notifying.
         */
        void onNotify(String deviceId, String objectId);

        /**
         * This method will be called if the testDevice sent a message to notify
         * all devices that waiting for the object.
         * @param deviceId
         *     The id of the device.
         * @param objectId
         *     The objectId that the device is notifying.
         */
        void onNotifyAll(String deviceId, String objectId);

        /**
         * This method will be called if the testDevice sent a heart-beat-message.
         * @param deviceId
         *     The id of the device.
         */
        void onHeartBeat(String deviceId);
    }

    /**
     * Create an instance of ControllerWorker. Then, start a new thread to listen
     * the bufferdReader.
     * @param observer
     *    The event observer.
     * @param deviceId
     *    The id of the device.
     * @param bufferedReader
     *    the bufferedReader of a inputStream, often from a socket.
     */
    ControllerWorker(ControllerWorkerObserver observer, String deviceId,
            BufferedReader bufferedReader) {
        if (observer == null) {
            Logger.e(TAG, "Error: controllerWorkerObserver is null!");
        }
        if (bufferedReader == null) {
            Logger.e(TAG, "Error: bufferedReader is null!");
        }
        if (deviceId == null) {
            Logger.e(TAG, "Error: deviceId is null!");
        }
        this.observer = observer;
        this.deviceId = deviceId;
        this.bufferedReader = bufferedReader;
        start();
    }

    /**
     * The main logic of this class.
     * It always reads messages from the BufferedReader, until the 'alive' flag
     * is set to false.
     */
    public void run() {
        String message = "";
        while (alive) {
            try { // Read the message from socket.
                message = bufferedReader.readLine();
            } catch (IOException e) {
                Logger.e(TAG+":"+deviceId, "Error occured when read message from socket!message:"+message);
//                e.printStackTrace();
                break;
            }
            if (message == null)
                continue;
            dispatchMessage(message);
        }
        tearDown();
        Logger.d(TAG, "Exit, deviceId = " + deviceId);
    }

    /**
     * Parse the received message, and take corresponding actions.
     * @param message
     *     the message received
     */
    private void dispatchMessage(String message) {
        JSONObject jsonObject = null;
        String messageType = "";
        try { // Forge the message to a jsonObject
            jsonObject = new JSONObject(message);
        } catch (JSONException e) {
            Logger.d(TAG, "message[ " + message
                    + " ] is not a legal JSONObject!");
            e.printStackTrace();
            return;
        }
        try { // Get the message type from the jsonObject
              // Logger.d(TAG, message);
            messageType = jsonObject.getString(MessageProtocol.MESSAGE_TYPE);
            if (messageType.equals(MessageProtocol.TEST_FINISH)) {
                observer.onDeviceFinished(deviceId);
            } else if (messageType.equals(MessageProtocol.NOTIFY)) {
                String notifyWhat = "";
                notifyWhat = jsonObject.getString(MessageProtocol.WHAT);
                if ("".equals(notifyWhat)) {
                    Logger.e(TAG, "Can not wait or notify an empty string.");
                    return;
                }
                observer.onNotify(deviceId, notifyWhat);
            } else if (messageType.equals(MessageProtocol.NOTIFY_ALL)) {
                String notifyWhat = "";
                notifyWhat = jsonObject.getString(MessageProtocol.WHAT);
                if ("".equals(notifyWhat)) {
                    Logger.e(TAG, "Can not wait or notify an empty string.");
                    return;
                }
                observer.onNotifyAll(deviceId, notifyWhat);
            } else if (messageType.equals(MessageProtocol.WAIT)) {
                String waitForWhat = "";
                waitForWhat = jsonObject.getString(MessageProtocol.WHAT);
                if ("".equals(waitForWhat)) {
                    Logger.e(TAG, "Can not wait or notify an empty string.");
                    return;
                }
                observer.onWait(deviceId, waitForWhat);
            } else if (messageType.equals(MessageProtocol.HEARTBEAT)) {
                observer.onHeartBeat(deviceId);
            } else {
                Logger.e(TAG, "Unknown message type: " + messageType);
            }
        } catch (JSONException e) {
            Logger.e(TAG, "message[ " + message + " ] has illegal arguments!");
            e.printStackTrace();
        }
    }

    /**
     * To clean up the resources.
     */
    private void tearDown() {
        if (bufferedReader != null) {
            try {
                bufferedReader.close();
            } catch (IOException e) {
                Logger.e(TAG, "Error occured when close BufferedReader!");
                e.printStackTrace();
            }
            bufferedReader = null;
        }
    }

    /**
     * Set the 'alive' flag to false, and end the circle.
     */
    public void close() {
        alive = false;
    }
}
