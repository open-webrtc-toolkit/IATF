package com.intel.webrtc.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;

import com.intel.webrtc.test.ControllerWorker.ControllerWorkerObserver;

import android.R.integer;

/**
 * This class takes charge of the executing process of a test case.
 * It communicates with test devices via Socket, and do the following things:
 *   Initializing test devices with start messages.
 *   Providing a service of remote Wait-Notify.
 *   Monitoring the status of the test.
 *   Detecting device timeout via the heart-beat mechanism.
 *
 * @author xianglai
 *
 */
public class TestController implements ControllerWorkerObserver {
    // TODO

    /**
     * Stands for both device and test status.
     * @author xianglai
     *
     */
    enum TestStatus {
        Ready, Running, Passed, Timeout, Failed, Erred, Crashed
    };

    private static final String TAG = "TestController";

    private Hashtable<String, String> addressTable;
    private Hashtable<String, String> startMessageTable;

    private WaitNotifyManager wnManager;
    private String serverAddress;

    private HeartBeatRecorder heartBeatRecorder;
    private HeartBeatThread heartBeatThread;
//    private int port = 10086;
    private String ip="127.0.0.1";

    private TestStatus testStatus;

    // TODO Enhancement: Make a new class to hold these attributes?
    // TODO Enhancement: To save system resources, create printWriters when it
    // is necessary, and delete it after using.
    private Hashtable<String, TestStatus> deviceStatus = null;
    private Hashtable<String, ControllerWorker> controllerWorkers = null;
    private Hashtable<String, Socket> sockets = null;
    private Hashtable<String, PrintWriter> printWriters = null;

    /**
     * create a new TestController instance with a device-address mapping and
     * a device-startMessage mapping.<p>
     * Both of these two mapping are created by TestRunner.
     *
     * @param addressTable
     * @param startMessageTable
     */
    public TestController(Hashtable<String, String> addressTable,
            Hashtable<String, String> startMessageTable) {
        this.addressTable = addressTable;
        this.startMessageTable = startMessageTable;
        this.wnManager = new WaitNotifyManager();

        deviceStatus = new Hashtable<String, TestStatus>();
        controllerWorkers = new Hashtable<String, ControllerWorker>();
        sockets = new Hashtable<String, Socket>();
        printWriters = new Hashtable<String, PrintWriter>();
        testStatus = TestStatus.Ready;
    }

    /**
     * This method will start the test by sending the start command.
     * @return
     * It will return the test result when the test ended.
     */
    public TestStatus start() {
        this.serverAddress = "";
        // getServerAddressFromSystem();
        initTestEnv();
        heartBeatThread = new HeartBeatThread();
        heartBeatThread.start();
        startTest();
        synchronized (deviceStatus) {
            try {
                // TODO this time should be read from the configuration.
                deviceStatus.wait(300000);
                Logger.d(TAG, "Test Ended!");
                if (testStatus == TestStatus.Running)// Time out
                {
                    Logger.e(TAG, "Test case has been time out.");
                    testStatus = TestStatus.Timeout;
                }
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        Logger.d(TAG, "The result was:" + testStatus.name());
        heartBeatThread.setTestEnd(true);
        close();
        return testStatus;
    }

    Hashtable<String, String> getAddressTable() {
        return addressTable;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    /**
     * Initialize the test environment.<p>
     *
     * It establishes the Socket connection to test devices, prepare the
     * corresponding BufferedReader and PrintWriter, and build new listening
     * Thread for every test device.
     */
    private void initTestEnv() {
        // TODO getConfig (port)
        // TODO need verification

        Iterator<Entry<String, String>> iterator = addressTable.entrySet()
                .iterator();
        Entry<String, String> element;
        String deviceId;
        String localPort;
        String[] deviceIds = new String[addressTable.size()];
        int index = 0;
        while (iterator.hasNext()) {
            Socket socket = null;
            BufferedReader bufferedReader = null;
            PrintWriter printWriter = null;
            ControllerWorker controllerWorker = null;
            element = iterator.next();
            deviceId = element.getKey();
            localPort = element.getValue();
            deviceIds[index++] = deviceId;
            try {
                Logger.d(TAG, "Create connection to ip " + deviceId + " port "
                        + localPort);
                socket = new Socket(ip, Integer.parseInt(localPort));
                bufferedReader = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));
                printWriter = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream())));
                controllerWorker = new ControllerWorker(this, deviceId,
                        bufferedReader);
                deviceStatus.put(deviceId, TestStatus.Ready);
                sockets.put(deviceId, socket);
                printWriters.put(deviceId, printWriter);
                controllerWorkers.put(deviceId, controllerWorker);
            } catch (IOException e) {
                // TODO call api of testRunner, and tell it that error occurred
                Logger.e(TAG, "Error occured when creating socket to "
                        + deviceId);
                e.printStackTrace();
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e1) {
                        Logger.e(TAG, "Error occured when closing socket.");
                        e1.printStackTrace();
                    }
                }
                close();
                return;
            }
        }
        heartBeatRecorder = new HeartBeatRecorder(deviceIds);
    }

    /**
     * Send start message to test devices to start the test.
     */
    private void startTest() {
        Iterator<Entry<String, PrintWriter>> iterator = printWriters.entrySet()
                .iterator();
        Entry<String, PrintWriter> element;
        while (iterator.hasNext()) {
            element = iterator.next();
            String startTestMessage = startMessageTable.get(element.getKey());
            sendMessage(element.getValue(), startTestMessage);
            deviceStatus.put(element.getKey(), TestStatus.Running);
        }
        testStatus = TestStatus.Running;
    }

    /**
     * Send a message in a new Thread
     * @param pw
     *      The corresponding PrintWriter of the device.
     * @param msg
     *      The message going to send.
     */
    private void sendMessage(final PrintWriter pw, final String msg) {
        new Thread() {
            public void run() {
                synchronized (pw) {
                    pw.println(msg);
                    Logger.d(TAG, "Sent message [ " + msg + " ]");
                    pw.flush();
                }
            }
        }.start();
    }

    // private void getServerAddressFromSystem() {
    // try {
    // Enumeration<NetworkInterface> netInterfaces = NetworkInterface
    // .getNetworkInterfaces();
    // while (netInterfaces.hasMoreElements()) {
    // NetworkInterface networkInterface = netInterfaces.nextElement();
    // Enumeration<InetAddress> inetAddresses = networkInterface
    // .getInetAddresses();
    // while (inetAddresses.hasMoreElements()) {
    // InetAddress inetAddress = inetAddresses.nextElement();
    // if (inetAddress.isLoopbackAddress())
    // continue;
    // if (inetAddress instanceof Inet4Address) {
    // // TODO how to choose network card when
    // // the computer has multiple cards?
    // serverAddress = inetAddress.getHostAddress();
    // return;
    // }
    // }
    // }
    // } catch (Exception e) {
    // Logger.d(TAG, e.getMessage());
    // }
    // }

    private boolean haveAllTestsFinished() {

        return !(deviceStatus.containsValue(TestStatus.Running));
    }

    /**
     * Clean up the TestController.
     */
    public void close() {
        Iterator<Entry<String, PrintWriter>> iterator = printWriters.entrySet()
                .iterator();
        Entry<String, PrintWriter> element;
        while (iterator.hasNext()) {
            element = iterator.next();
            String deviceId = element.getKey();
            // Close ControllerWorkers
            controllerWorkers.get(deviceId).close();
            // Close PrintWriters
            printWriters.get(deviceId).close();
            // Close Sockets
            try {
                sockets.get(deviceId).close();
            } catch (IOException e) {
                e.printStackTrace();
                Logger.e(TAG, "Error occured when close the socket of device "
                        + deviceId);
            }
        }
        Logger.d(TAG, "TestController closed.");
    }

    @Override
    public void onDeviceFinished(String deviceId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onWait(String deviceId, String lockId) {
        Logger.d(TAG, "onWait(): deviceId:\t" + deviceId + "\tlockId:\t"
                + lockId);
        boolean CallNotify=wnManager.waitForObject(deviceId, lockId);
        if(CallNotify){
            Logger.d(TAG, "Use Stored notify !");
            notifyDevice(deviceId, lockId);
        }
    }

    @Override
    public void onNotify(String deviceId, String lockId) {
        Logger.d(TAG, "onNotify(): deviceId:\t" + deviceId + "\tlockId:\t"
                + lockId);
        String notifiedDevice = wnManager.notifyObject(deviceId,lockId);
        if (notifiedDevice != null) {
            notifyDevice(notifiedDevice, lockId);
        }
    }

    @Override
    public void onNotifyAll(String deviceId, String lockId) {
        String[] notifiedDevices = wnManager.notifyObjectForAll(lockId);
        for (int l = notifiedDevices.length, i = 0; i < l; i++) {
            notifyDevice(notifiedDevices[i], lockId);
        }
    }

    @Override
    public void onHeartBeat(String deviceId) {
        if (heartBeatRecorder == null)
            return;
        Date date = new Date();
        heartBeatRecorder.onHeartBeat(deviceId, date.getTime());
    }

    private void notifyDevice(String notifiedDevice, String lockId) {
        Logger.d(TAG, "NotifyDevice(): \tnotifyedDevice:" + notifiedDevice
                + "\t lockId:" + lockId);
        JSONObject messageObject = new JSONObject();
        try {
            messageObject.put(MessageProtocol.MESSAGE_TYPE,
                    MessageProtocol.NOTIFY);
            messageObject.put(MessageProtocol.WHAT, lockId);
            PrintWriter printWriter = printWriters.get(notifiedDevice);
            Logger.d(TAG,
                    "NotifyDevice(): \tmessage:" + messageObject.toString());
            sendMessage(printWriter, messageObject.toString());
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private class HeartBeatThread extends Thread {
        private volatile boolean testEnd=false;
        public void setTestEnd(boolean end) {
            this.testEnd = end;
            Logger.d(TAG, "set testEnd flag:"+testEnd);
        }
        @Override
        public void run() {
            try {
                // TODO Set this variable from configure files.
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // TODO Replace this condition with "Test hasn't finished"
            while (!testEnd) {
                try {
                    Thread.sleep(5000);
                    long earlestTime = heartBeatRecorder.getEarliest()
                            .getTime();
                    if (new Date().getTime() - earlestTime > 60000) {
                        // TODO TIMEOUT
                    }

                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    public void deviceCrashed(String deviceId) {
        if (deviceStatus.containsKey(deviceId)) {
            deviceStatus.put(deviceId, TestStatus.Crashed);
            Logger.d(TAG, "The test on [" + deviceId + "] crashed.");
        } else {
            Logger.d(TAG, "There's no device named [" + deviceId + "].");
        }
        testStatus = TestStatus.Crashed;
        synchronized (deviceStatus) {
            deviceStatus.notify();
        }
    }

    public void deviceErred(String deviceId) {
        if (deviceStatus.containsKey(deviceId)) {
            deviceStatus.put(deviceId, TestStatus.Crashed);
            Logger.d(TAG, "The test on [" + deviceId + "] erred.");
        } else {
            Logger.d(TAG, "There's no device named [" + deviceId + "].");
        }
        testStatus = TestStatus.Erred;
        synchronized (deviceStatus) {
            deviceStatus.notify();
        }
    }

    public void deviceFailed(String deviceId) {
        if (deviceStatus.containsKey(deviceId)) {
            deviceStatus.put(deviceId, TestStatus.Failed);
            Logger.d(TAG, "The test on [" + deviceId + "] failed.");
        } else {
            Logger.d(TAG, "There's no device named [" + deviceId + "].");
        }
        testStatus = TestStatus.Failed;
        synchronized (deviceStatus) {
            deviceStatus.notify();
        }
    }

    public void devicePassed(String deviceId) {
        if (deviceStatus.containsKey(deviceId)) {
            deviceStatus.put(deviceId, TestStatus.Passed);
            // Set the time stamp of the finished device to prevent it from
            // being recognized as time out.
            heartBeatRecorder.onHeartBeat(deviceId, Long.MAX_VALUE);
            Logger.d(TAG, "The test on [" + deviceId + "] finished.");
        } else {
            Logger.d(TAG, "There's no device named [" + deviceId + "].");
        }
        if (haveAllTestsFinished()) {
            testStatus = TestStatus.Passed;
            synchronized (deviceStatus) {
                deviceStatus.notify();
            }
        }
    }

    public void deviceTimeOut(String deviceId) {
        if (deviceStatus.containsKey(deviceId)) {
            deviceStatus.put(deviceId, TestStatus.Timeout);
            Logger.d(TAG, "The test on [" + deviceId + "] has been time out.");
        } else {
            Logger.d(TAG, "There's no device named [" + deviceId + "].");
        }
        testStatus = TestStatus.Timeout;
        synchronized (deviceStatus) {
            deviceStatus.notify();
        }
    }

    // private void debug_printDeviceStatus() {
    // Logger.d(TAG,"Print Device Status:");
    // }
}
