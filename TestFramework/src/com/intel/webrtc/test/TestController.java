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
import com.intel.webrtc.test.javascript.JavascriptRunnerHelper;

/**
 * This class takes charge of the executing process of one specific test case.
 * It communicates with test devices via Socket, and do the following things:<br>
 * <li>Initializing test devices with start messages.</li>
 * <li>Providing a service of remote Wait-Notify.</li>
 * <li>Monitoring the status of the test.</li>
 * <li>Detecting device timeout via the heart-beat mechanism.</li>
 * @author xianglai
 *
 */
public class TestController implements ControllerWorkerObserver {
    /**
     * Stands for the status of both single device and test case.
     * @author xianglai
     */
    enum TestStatus {
        Ready, Running, Passed, Timeout, Failed, Erred, Crashed
    };

    // Debug tag
    private static final String TAG = "TestController";
    // <DeviceInfoID-LocalPort>: store the address info of test clients.
    // TestController will connect to the socket server on test client side via
    // the local port.
    // TODO: refactor merge addressTable and startMessage in device and remove
    // platform
    // related operations on server side.
    private Hashtable<String, String> addressTable;
    // <DeviceInfoID-startMessage>: store the start message to test clients
    private Hashtable<String, String> startMessageTable;
    // Wait-notify manager on test server side
    private WaitNotifyManager wnManager;
    // TODO: Useless currently, but may be helpful while expanding this
    // framework to distributed system.
    private String serverAddress;
    // Record the heart beat from test client
    private HeartBeatRecorder heartBeatRecorder;
    // Thread that check whether the test is timeout from HeartBeatRecorder
    private HeartBeatThread heartBeatThread;
    // TODO: Currently, all the client ip is localhost.
    // After expanding this framework to distributed system,
    // different client may be distributed to different ip.
    private String clientSocketIP = "127.0.0.1";
    // Test status of the test case
    private TestStatus testStatus;
    // <DeviceInfoID-TestStatus>: store the status of each test client of
    // current test case.
    private Hashtable<String, TestStatus> deviceStatus = null;
    // <DeviceInfoID-ControllerWorker>: store the communicating ControllerWorker
    // for each test client
    private Hashtable<String, ControllerWorker> controllerWorkers = null;
    // <DeviceInfoID-Socket>: store the communicating Socket to each test client
    private Hashtable<String, Socket> sockets = null;
    // <DeviceInfoID-PrintWriter>: store the PrintWriter of each communicating
    // Socket to each test client
    private Hashtable<String, PrintWriter> printWriters = null;
    // <DeviceInfoID-Socket>: get from TestRunner, store the Sockets which are
    // only init by once.
    // (To keep a long-term communicating socket in javascript clients, instead
    // of rebuilding connection
    // for every test case as Android)
    private Hashtable<String, Socket> storedSockets = null;
    // <DeviceInfoID-ControllerWorker>: mated with storedSockets, but store
    // ControllerWorkers instead
    private Hashtable<String, ControllerWorker> storedControllerWorkers = null;
    // <DeviceInfoID-PrintWriter>: mated with storedSockets, but store
    // PrintWriters instead
    private Hashtable<String, PrintWriter> storedPrintWriters = null;
    // TODO: remove this. <DeviceInfoID-DeviceType>: stores the device type of
    // every test client.(The class name of the device)
    // In order to do some platform specific operations
    private Hashtable<String, String> deviceTypes = null;

    /**
     * Create a new TestController instance with necessary informations.<br>
     * This constructor will be called by TestRunner and get these informations.<br>
     * @param addressTable <DeviceInfoID-LocalPort> Address table.
     * @param startMessageTable <DeviceInfoID-startMessage> Start messages.
     * @param addressDeviceType <DeviceInfoID-DeviceType> Device types(class name of TestDevice).
     * @param storedSockets <DeviceInfoID-Socket> Stored long-term communicating sockets.
     * @param storedPrintWriters <DeviceInfoID-PrintWriter> Stored long-term communicating PrintWriters.
     * @param storedControllerWorkers <DeviceInfoID-ControllerWorker> Stored long-term communicating ControllerWorkers.
     */
    public TestController(Hashtable<String, String> addressTable, Hashtable<String, String> startMessageTable,
            Hashtable<String, String> addressDeviceType, final Hashtable<String, Socket> storedSockets,
            final Hashtable<String, PrintWriter> storedPrintWriters,
            final Hashtable<String, ControllerWorker> storedControllerWorkers) {
        this.addressTable = addressTable;
        this.startMessageTable = startMessageTable;
        this.deviceTypes = addressDeviceType;
        this.storedControllerWorkers = storedControllerWorkers;
        this.storedSockets = storedSockets;
        this.storedPrintWriters = storedPrintWriters;
        this.wnManager = new WaitNotifyManager();

        deviceStatus = new Hashtable<String, TestStatus>();
        controllerWorkers = new Hashtable<String, ControllerWorker>();
        sockets = new Hashtable<String, Socket>();
        printWriters = new Hashtable<String, PrintWriter>();
        testStatus = TestStatus.Ready;
    }

    /**
     * This method will init the connection with test clients,
     * start a HeartBeatRecorder and send the start message to test clients.
     * @return
     *      It will return the test result until the test ended.
     */
    public TestStatus start() {
        this.serverAddress = "";
        // TODO: getServerAddressFromSystem();
        initTestEnv();
        heartBeatThread = new HeartBeatThread();
        heartBeatThread.start();
        startTest();
        synchronized (deviceStatus) {
            try {
                // TODO this time should be read from the configuration.
                deviceStatus.wait(300000);
                Logger.d(TAG, "Test Ended!");
                if (testStatus == TestStatus.Running) {
                    // Time out
                    Logger.e(TAG, "Test case has been time out.");
                    testStatus = TestStatus.Timeout;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Logger.d(TAG, "The result was:" + testStatus.name());
        heartBeatThread.setTestEnd(true);
        close();
        return testStatus;
    }

    /**
     * Initialize the test environment.<p>
     *
     * It establishes the Socket connection to test devices, prepare the
     * corresponding BufferedReader and PrintWriter, and build new listening
     * thread in {@link .ControllerWorker ControllerWorker} for every test device.
     * Additionally, {@link .HeartBeatRecorder HeartBeatRecorder} will be started
     * to check the heartbeat from clients.
     */
    private void initTestEnv() {
        Iterator<Entry<String, String>> iterator = addressTable.entrySet().iterator();
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
                // TODO: How to remove the relateness with specific platform
                String deviceType = deviceTypes.get(deviceId);
                if (storedSockets.containsKey(deviceId)) {
                    Logger.d(TAG, "fetch socket from stored list:" + deviceId);
                    socket = storedSockets.get(deviceId);
                    printWriter = storedPrintWriters.get(deviceId);
                    controllerWorker = storedControllerWorkers.get(deviceId);
                } else {
                    Logger.d(TAG, "Create connection to ip " + deviceId + " port " + localPort);
                    socket = new Socket(clientSocketIP, Integer.parseInt(localPort));
                    bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    printWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
                    controllerWorker = new ControllerWorker(this, deviceId, bufferedReader);
                }
                if ((deviceType != null && deviceType.equals(JavascriptRunnerHelper.class.getName()))
                        || deviceId.equals(LockServer.infoid)) {
                    storedSockets.put(deviceId, socket);
                    storedPrintWriters.put(deviceId, printWriter);
                    storedControllerWorkers.put(deviceId, controllerWorker);
                }
                if (!deviceId.equals(LockServer.infoid)) {
                    deviceStatus.put(deviceId, TestStatus.Ready);
                }
                sockets.put(deviceId, socket);
                printWriters.put(deviceId, printWriter);
                controllerWorkers.put(deviceId, controllerWorker);
            } catch (IOException e) {
                // TODO call api of testRunner, and tell it that error occurred
                Logger.e(TAG, "Error occured when creating socket to " + deviceId);
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
     * Send start messages to test devices to start the test.
     */
    private void startTest() {
        Iterator<Entry<String, PrintWriter>> iterator = printWriters.entrySet().iterator();
        Entry<String, PrintWriter> element;
        while (iterator.hasNext()) {
            element = iterator.next();
            String startTestMessage = startMessageTable.get(element.getKey());
            sendMessage(element.getValue(), startTestMessage);
            if (!element.getKey().equals(LockServer.infoid)) {
                deviceStatus.put(element.getKey(), TestStatus.Running);
            }
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

    /**
     * Check whether all the test clients have finished test.
     * @return true if yes.
     */
    private boolean haveAllTestsFinished() {
        return !(deviceStatus.containsValue(TestStatus.Running));
    }

    /**
     * Clean up the TestController.<br>
     * Close short-term socket connections with test client.(Android platform)
     */
    public void close() {
        Iterator<Entry<String, PrintWriter>> iterator = printWriters.entrySet().iterator();
        Entry<String, PrintWriter> element;
        while (iterator.hasNext()) {
            element = iterator.next();
            String deviceId = element.getKey();
            if (!storedSockets.containsKey(deviceId)) {
                Logger.d(TAG, "close deviceId:" + deviceId);
                // Close ControllerWorkers
                controllerWorkers.get(deviceId).close();
                // Close PrintWriters
                printWriters.get(deviceId).close();
                // Close Sockets
                try {
                    sockets.get(deviceId).close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Logger.e(TAG, "Error occured when close the socket of device " + deviceId);
                }
            }
        }
        Logger.d(TAG, "TestController closed.");
    }

    /**
     * Called by ControllerWorker after receiving test finish message from client.
     */
    @Override
    public void onDeviceFinished(String deviceId) {
        // TODO utilizing this if necessary
    }

    /**
     * Called by ControllerWorker after receiving wait message from client.
     * Calling wait-notify manager to provide the remote wait operations.
     */
    @Override
    public void onWait(String deviceId, String lockId) {
        Logger.d(TAG, "onWait(): deviceId:\t" + deviceId + "\tlockId:\t" + lockId);
        boolean callNotify = wnManager.waitForObject(deviceId, lockId);
        if (callNotify) {
            Logger.d(TAG, "Use Stored notify !");
            notifyDevice(deviceId, lockId);
        }
    }

    /**
     * Called by ControllerWorker after receiving notify message from client.
     * Calling wait-notify manager to provide the remote notify operations.
     */
    @Override
    public void onNotify(String deviceId, String lockId) {
        Logger.d(TAG, "onNotify(): deviceId:\t" + deviceId + "\tlockId:\t" + lockId);
        String notifiedDevice = wnManager.notifyObject(deviceId, lockId);
        Logger.d(TAG, "notifiedDevice:" + notifiedDevice);
        if (notifiedDevice != null) {
            notifyDevice(notifiedDevice, lockId);
        }
    }

    /**
     * Called by ControllerWorker after receiving notifyAll message from client.
     * Calling wait-notify manager to provide the remote notifyAll operations.
     */
    @Override
    public void onNotifyAll(String deviceId, String lockId) {
        String[] notifiedDevices = wnManager.notifyObjectForAll(lockId);
        for (int l = notifiedDevices.length, i = 0; i < l; i++) {
            notifyDevice(notifiedDevices[i], lockId);
        }
    }

    /**
     * Called by ControllerWorker after receiving heart beat message from client.
     * Calling HeartBeatRecorder to record heart beat from client.
     */
    @Override
    public void onHeartBeat(String deviceId) {
        if (heartBeatRecorder == null)
            return;
        Date date = new Date();
        heartBeatRecorder.onHeartBeat(deviceId, date.getTime());
    }

    /**
     * Send notify message to test client in waiting for a specific lock.
     * @param notifiedDevice
     * @param lockId
     */
    private void notifyDevice(String notifiedDevice, String lockId) {
        Logger.d(TAG, "NotifyDevice(): \tnotifyedDevice:" + notifiedDevice + "\t lockId:" + lockId);
        JSONObject messageObject = new JSONObject();
        try {
            messageObject.put(MessageProtocol.MESSAGE_TYPE, MessageProtocol.NOTIFY);
            messageObject.put(MessageProtocol.WHAT, lockId);
            PrintWriter printWriter = printWriters.get(notifiedDevice);
            Logger.d(TAG, "NotifyDevice(): \tmessage:" + messageObject.toString());
            sendMessage(printWriter, messageObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * HeartBeatRecorder work on this thread to check whether test client is timeout.
     * @author bean
     *
     */
    private class HeartBeatThread extends Thread {
        private volatile boolean testEnd = false;

        public void setTestEnd(boolean end) {
            this.testEnd = end;
            Logger.d(TAG, "set testEnd flag:" + testEnd);
        }

        @Override
        public void run() {
            try {
                // TODO Set this variable from configure files.
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            while (!testEnd) {
                try {
                    Thread.sleep(5000);
                    long earlestTime = heartBeatRecorder.getEarliest().getTime();
                    if (new Date().getTime() - earlestTime > 60000) {
                        // TODO TIMEOUT
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Update test case status when a test client crashes.
     * @param deviceId
     */
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

    /**
     * Update test case status when a test client errors.
     * @param deviceId
     */
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

    /**
     * Update test case status when a test client fails.
     * @param deviceId
     */
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

    /**
     * Update test case status when a test client passes.
     * @param deviceId
     */
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

    /**
     * Update test case status when a test client is timeout.
     * @param deviceId
     */
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
}
