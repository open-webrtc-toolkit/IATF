package com.intel.webrtc.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.json.JSONObject;

import com.intel.webrtc.test.TestController.TestStatus;
import com.intel.webrtc.test.android.AndroidDeviceInfo;
import com.intel.webrtc.test.android.AndroidTestDevice;
import com.intel.webrtc.test.android.AndroidDeviceInfo.AndroidDeviceType;

import android.R.integer;

/**
 * This class is the core of the test framework. Its main job is collaborating
 * all modules together. It is also responsible for interacting with Linux
 * System and ADB, in order to get device info and start test on each device.
 *
 * @author xianglai
 *
 */
public class TestRunner {

    final static private String TAG = "TestRunner";

    private Config config;
    private TestController testController;
    private Hashtable<String, String> addressTable;
    private Hashtable<String, String> startMessageTable;
    private LinkedList<AndroidDeviceInfo> androidDeviceInfos;
    private LinkedList<TestResult> testResults;

    // androidHome and antHome should be read from configure files.
    private String adbPath, antPath, shellPath, apkName, androidTestPackage, androidTestClass;
    // Get the port from the configuration, and pass it to the TestController
    int port;
    private TestCase currentTestCase;

    // TODO maybe a singleton is better?
    public TestRunner(Config config) {

        if (config == null) {
            this.config = new Config(null);
        } else {
            this.config = config;
        }
        initPeremeters();
        testController = null;
        androidDeviceInfos = new LinkedList<AndroidDeviceInfo>();
        addressTable = new Hashtable<String, String>();
        startMessageTable = new Hashtable<String, String>();
        testResults = new LinkedList<TestResult>();
    }

    private void initPeremeters() {
        port = config.getPort();
        adbPath = config.getAdbPath();
        antPath = config.getAntPath();
        shellPath = config.getShellPath();
        apkName = null;
        androidTestPackage = config.getAndroidTestPackage();
        androidTestClass = config.getAndroidTestClass();
    }

    /**
     * run TestCases in the TestSuite one by one.
     *
     * @param tests
     *            the test suite.
     */
    public void runTest(TestSuite tests) {
        deployTests();
        Hashtable<String, TestCase> testCases = tests.getTestCases();
        Iterator<Entry<String, TestCase>> iterator = testCases.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, TestCase> entry = iterator.next();
            runTestCase(entry.getValue());
        }
        printResult(tests.getTestCases().size());
        // TODO generate test result.
    }

    /**
     * Run single test case.
     *
     * @param testCase
     *            the test case which is going to run.
     */
    private void runTestCase(TestCase testCase) {
        if (testController != null) {
            testController.close();
            testController = null;
        }
        currentTestCase = testCase;
        androidDeviceInfos.clear();
        addressTable.clear();
        startMessageTable.clear();
        // TODO reset the test environment
        getTestDeviceInfo();
        // TODO initialize the test devices
        CountDownLatch startTestCountDownLatch = new CountDownLatch(testCase.getDevices().size());
        Iterator<TestDevice> iterator = testCase.getDevices().iterator();
        while (iterator.hasNext()) {
            TestDevice device = iterator.next();
            startTestDevice(device, startTestCountDownLatch);
        }
        try {
            if (!startTestCountDownLatch.await(100000, TimeUnit.MILLISECONDS))
                Logger.e(TAG, "Time out when start test cases.");
            else {
                Logger.d(TAG, "Test cases started successfully.");
            }
        } catch (InterruptedException e) {
            Logger.e(TAG, "Interrupted when start test cases.");
            e.printStackTrace();
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Logger.e(TAG, "Interrupted when waiting for test cases ready.");
            e.printStackTrace();
        }
        testController = new TestController(addressTable, startMessageTable);
        TestStatus testCaseReslut = testController.start();// TODO result?
        removeForwardRules();
        testResults.add(new TestResult(testCase.getName(), testCaseReslut));
    }

    /**
     * Remove the forward rules on all the devices after a testCase is finished.
     */
    private void removeForwardRules() {
        // Clean forward rules
        String cmd = adbPath + " forward --remove-all";
        try {
            Process process = executeByShell(cmd);
            process.waitFor();
        } catch (IOException e) {
            Logger.e(TAG, "Forward setting error!");
            e.printStackTrace();
        } catch (InterruptedException e) {
            Logger.e(TAG, "Wait for cmdline process error!");
            e.printStackTrace();
        }
        Logger.d(TAG, "Remove all the forward rules.");
    }

    /**
     * Get device info. Now, this method will get serial Id, device type and IP
     * address of all Android device that attached to the computer.
     */
    private void getTestDeviceInfo() {
        Process process = null;
        BufferedReader sio0, sio1 = null;
        String resultLine = null, serialId, ipAddr;
        AndroidDeviceType type;
        try {
            process = executeByShell(adbPath + " devices");
            sio0 = new BufferedReader(new InputStreamReader(process.getInputStream()));
            process.waitFor();
            while (true) {
                serialId = null;
                ipAddr = null;
                type = AndroidDeviceType.OFFLINE;
                resultLine = sio0.readLine();
                if (resultLine == null)
                    break;
                if ("".equals(resultLine))
                    continue;
                if (resultLine.startsWith("List of devices attached"))
                    continue;
                String[] deviceInfo = resultLine.split("\t");
                if (deviceInfo.length != 2)
                    continue;// TODO the format is not correct!
                serialId = deviceInfo[0];
                if ("device".equals(deviceInfo[1]))
                    type = AndroidDeviceType.DEVICE;
                else if ("emulator".equals(deviceInfo[1])) {
                    type = AndroidDeviceType.EMULATOR;
                } else if ("unauthorized".equals(deviceInfo[1])) {
                    type = AndroidDeviceType.UNAUTHORIZED;
                    Logger.e(TAG, "Device " + serialId + " is unauthorized!");
                } else {
                    Logger.e(TAG, "The type of Device " + serialId + " is unknown!");
                }
                /**
                 * GET CLIENT IP
                 */
                try {
                    process = executeByShell(adbPath + " -s " + serialId + " shell netcfg");
                    sio1 = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    process.waitFor();
                    while (true) {
                        // TODO add some fault tolerance related code.
                        resultLine = sio1.readLine();
                        if (resultLine == null)
                            break;
                        if (resultLine.startsWith("wlan0")) {
                            int i, offset = resultLine.indexOf("/");
                            for (i = offset; i > 0; i--) {
                                if (resultLine.charAt(i) == ' ') {
                                    i = i + 1;
                                    break;
                                }
                            }
                            if (i != 0) {
                                ipAddr = resultLine.substring(i, offset);
                                Logger.d(TAG, "Serial Id: " + serialId + ", Type:" + type.name() + ", Ip: " + ipAddr);
                                androidDeviceInfos.push(new AndroidDeviceInfo(serialId, type, ipAddr));
                                Logger.d(TAG, "Push back into Infos, size = " + androidDeviceInfos.size());
                            } else {
                                Logger.d(TAG, "Serial Id: " + serialId + ", Type:" + type.name() + ", Ip:-----------");
                            }
                        }
                    }
                } catch (IOException e) {
                    Logger.e(TAG, "Error occured when get ip address of " + serialId);
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    Logger.e(TAG, "Error occured when get ip address of " + serialId);
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            Logger.d(TAG, "Error occured when get device list via ADB ");
            e.printStackTrace();
        } catch (InterruptedException e) {
            Logger.d(TAG, "Error occured when get device list via ADB ");
            e.printStackTrace();
        }
    }

    /**
     * This method is a common entry of starting test device
     *
     * @param device
     *            device name
     * @param latch
     *            to guarantee all TestDevices have been started before the next
     *            step. if the action finishes, it will call latch.countDown().
     */
    private void startTestDevice(TestDevice device, CountDownLatch latch) {
        if (device instanceof AndroidTestDevice) {
            startAndroidTestDevice((AndroidTestDevice) device, latch);
            return;
        }
        // if (device instanceof BrowserTab) {
        // startBrowserTab((BrowserTab) device);
        // return;
        // }
    }

    /**
     * This method will initialize an Android device for testing. It will start
     * an instrumentation test case and run the test named testEntry. Then, it
     * will pause and wait for commands from the test controller, in setUp();
     *
     * @param device
     *            device name
     * @param latch
     *            to guarantee all TestDevices have been started before the next
     *            step. if the action finishes, it will call latch.countDown().
     */

    private void startAndroidTestDevice(AndroidTestDevice device, CountDownLatch latch) {
        // TODO this solution of device assigning is the simplest one.
        AndroidDeviceInfo info;
        if (!androidDeviceInfos.isEmpty()) {
            info = androidDeviceInfos.removeFirst();
            addressTable.put(info.id, ""+info.localPort);
            // set forward rule
            Process process;
            String cmd = adbPath + " -s " + info.id + " forward tcp:" + info.localPort + " tcp:10086";
            try {
                process = executeByShell(cmd);
                process.waitFor();
            } catch (IOException e) {
                Logger.e(TAG, "Forward setting error!");
                e.printStackTrace();
            } catch (InterruptedException e) {
                Logger.e(TAG, "Wait for cmdline process error!");
                e.printStackTrace();
            }

            generateStartMessage(device, info);
        } else {
            // TODO: logic error
            Logger.e(TAG, "There is no Android device available!");
            countDown(latch);
            return;
        }
        try { // run Android Instrumentation Test via ADB
            Process process;
            BufferedReader sio, seo;
            String cmd = adbPath + " -s " + info.id + " shell am instrument -r -e class " + androidTestClass
                    + "#testEntry -w " + androidTestPackage + "/android.test.InstrumentationTestRunner";
            process = executeByShell(cmd);
            sio = new BufferedReader(new InputStreamReader(process.getInputStream()));
            seo = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            waitForAndroidTestResult(process, sio, seo, latch, device.getName(), info.id);
        } catch (IOException e) {
            Logger.e(TAG, "Error occured when get device list via ADB ");
            countDown(latch);
            e.printStackTrace();
        }

    }

    /**
     * This method is used internally to build a new thread to wait for a
     * process and read its result when it finishes.
     *
     * @param process
     *            The process that the new thread is going to wait.
     * @param standardIOReader
     *            the BufferedReader of the standard I/O stream of the process.
     * @param standardErrorStreamReader
     *            the BufferedReader of the standard error stream of the
     *            process.
     * @param latch
     *            if the latch is not null, its countDown() method will be
     *            called when the thread finished.
     */
    private void waitForResultAndLog(final Process process, final BufferedReader standardIOReader,
            final BufferedReader standardErrorStreamReader, final CountDownLatch latch) {
        new Thread() {
            public void run() {
                String resultLine;
                try {
                    while (true) {
                        resultLine = standardIOReader.readLine();
                        if (resultLine == null)
                            break;
                        Logger.d(TAG, resultLine);
                    }
                    while (true) {
                        resultLine = standardErrorStreamReader.readLine();
                        if (resultLine == null)
                            break;
                        Logger.d(TAG, resultLine);
                    }
                } catch (IOException e) {
                    Logger.e(TAG, "Error occured when reading test result.");
                    e.printStackTrace();
                } finally {
                    if (latch != null)
                        countDown(latch);
                }
            }
        }.start();
    }

    private void waitForAndroidTestResult(final Process process, final BufferedReader standardIOReader,
            final BufferedReader standardErrorStreamReader, final CountDownLatch latch, final String deviceName,
            final String deviceId) {
        new Thread() {
            public void run() {
                LinkedList<String> resultLines = new LinkedList<String>();
                String resultLine;
                try {
                    while (true) {
                        resultLine = standardIOReader.readLine();
                        if (resultLine == null)
                            break;
                        resultLines.add(resultLine);
                        if (resultLine.equals("INSTRUMENTATION_STATUS_CODE: 1"))
                            countDown(latch);
                    }
                    while (true) {
                        resultLine = standardErrorStreamReader.readLine();
                        if (resultLine == null)
                            break;
                        Logger.e(TAG,
                                "Test Device:\t" + deviceName + "\tReal Device:\t" + deviceId + "\n" + resultLine);
                    }
                    Logger.d(TAG, "Test Device:\t" + deviceName + "\tReal Device:\t" + deviceId);
                    for (int i = 0; i < resultLines.size(); i++) {
                        Logger.d(TAG, resultLines.get(i));
                    }
                    parseAndroidResult(resultLines, deviceId);
                } catch (IOException e) {
                    Logger.e(TAG, "Error occured when reading test result.");
                    countDown(latch);
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * This method will start a new process, and execute the inputed command in
     * it, with shell (the default path is /bin/sh)
     *
     * @param cmd
     *            The inputed command.
     * @return The created process.
     * @throws IOException
     *             When error occurs during the execution, it may throw an
     *             IOException.
     */
    private Process executeByShell(String cmd) throws IOException {
        return Runtime.getRuntime().exec(new String[] { shellPath, "-c", cmd });
    }

    /**
     * This method is used to generate a start message for android device.
     *
     * @param device
     *            the AndroidTestDevice going to run.
     * @param info
     *            the AndroidDeviceInfo of the device.
     */
    private void generateStartMessage(AndroidTestDevice device, AndroidDeviceInfo info) {
        JSONObject message = new JSONObject();
        try {
            message.put(MessageProtocol.MESSAGE_TYPE, MessageProtocol.TEST_START);
            message.put(MessageProtocol.CLASS_NAME, device.getClass().getName());
            message.put(MessageProtocol.METHOD_NAME, currentTestCase.getName());
            message.put(MessageProtocol.TEST_ACTIVITY, config.getAndroidTestActivity());
            message.put(MessageProtocol.TEST_PACKAGE, config.getAndroidTestPackage());
            Logger.d(TAG, "Start message of device " + device.getName() + " is " + message.toString());
            startMessageTable.put(info.id, message.toString());
        } catch (JSONException e) {
            Logger.e(TAG, "Error occured when generate start message for device " + device.getName());
        }
    }

    /**
     * This method is used to build the test APK.
     */
    private void buildApk() {
        Process process;
        BufferedReader sio, seo;
        try {
            String cmd = antPath + " clean debug";
            process = executeByShell(cmd);
            sio = new BufferedReader(new InputStreamReader(process.getInputStream()));
            seo = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String resultLine;
            boolean buildFailed = false;
            while (true) {
                resultLine = sio.readLine();
                if (resultLine == null)
                    break;
                if (resultLine.endsWith(".apk")) {
                    if (resultLine.endsWith("-debug.apk")) {
                        String[] pathItems = resultLine.split("/");
                        apkName = "bin/" + pathItems[pathItems.length - 1];
                        Logger.d(TAG, "apk name is " + apkName);
                    }
                }
                if (resultLine.contains("BUILD FAILED")) {
                    Logger.e(TAG, "Apk Build Failed!");
                    buildFailed = true;
                }
                Logger.d(TAG, resultLine);
            }
            while (true) {
                resultLine = seo.readLine();
                if (resultLine == null)
                    break;
                Logger.d(TAG, resultLine);
            }
            if (buildFailed) {
                throw new RuntimeException("APK build failed!!");
            }
        } catch (IOException e) {
            e.printStackTrace();
            Logger.e(TAG, "Error occured when building test Apk.");
        }
        // catch (InterruptedException e) {
        // e.printStackTrace();
        // Logger.e(TAG, "Interrupted when building test Apk.");
        // }

    }

    /**
     * This method is used to deploy the test APK to a single device.
     *
     * @param info
     *            the AndroidTestInfo of a device.
     * @param latch
     *            the CountDownLatch to control the deploy process.
     */
    private void deployApk(AndroidDeviceInfo info, CountDownLatch latch) {
        // TODO if info == null?
        // TODO if error occurs, do what?
        Process process;
        BufferedReader sio, seo;
        if (apkName == null)
            apkName = config.getApkName();
        String cmd = adbPath + " -s " + info.id + " install -r " + apkName;
        try {
            process = executeByShell(cmd);
            sio = new BufferedReader(new InputStreamReader(process.getInputStream()));
            seo = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        } catch (IOException e) {
            e.printStackTrace();
            Logger.e(TAG, "Error occured when building test Apk.");
            countDown(latch);
            return;
        }
        waitForResultAndLog(process, sio, seo, latch);
    }

    /**
     * This method is used to deploy all test programs to real test devices. It
     * can be extended to support more types of device
     */
    private void deployTests() {
        // deploy Android Test
        buildApk();
        getTestDeviceInfo();
        int i, n = androidDeviceInfos.size();
        Logger.d(TAG, "Info number: " + n);
        CountDownLatch deployApkLatch = new CountDownLatch(n);
        Logger.d(TAG, "Deploying APKs to devices.");
        for (i = 0; i < n; i++) {
            deployApk(androidDeviceInfos.get(i), deployApkLatch);
        }
        try {
            if (!deployApkLatch.await(30000, TimeUnit.MILLISECONDS))
                Logger.e(TAG, "Deploy time out.");
        } catch (InterruptedException e) {
            Logger.e(TAG, "Interrupted when deploying.");
            e.printStackTrace();
        }
        Logger.d(TAG, "APKs have been deployed successfully.");
    }

    static private void countDown(final CountDownLatch latch) {
        Exception exception = new Exception();
        StackTraceElement[] elements = exception.getStackTrace();
        Logger.d(TAG, "CountDown, remains " + latch.getCount());
        for (int i = 1; i < elements.length; i++) {
            Logger.d(TAG, "at " + elements[i].toString());
        }
        latch.countDown();
    }

    /**
     * parse the test result of a single Android device, and report it to the
     * TestController
     *
     * @param resultLines
     *            the test result lines
     * @param deviceId
     *            the device id of the device.
     */
    private void parseAndroidResult(LinkedList<String> resultLines, String deviceId) {
        if (testController == null) {
            Logger.e(TAG, "parseAndroidResult: testController is null!");
        }
        if (resultLines == null || resultLines.isEmpty()) {
            testController.deviceCrashed(deviceId);
            return;
        }
        for (int i = 0; i < resultLines.size(); i++) {
            String resultLine = resultLines.get(i);
            if (resultLine.startsWith("OK")) {
                // Test passed
                testController.devicePassed(deviceId);
                return;
            }
            if (resultLine.startsWith("Test results for InstrumentationTestRunner=.E")) {
                // Test erred
                testController.deviceErred(deviceId);
                return;
            }
            if (resultLine.startsWith("Test results for InstrumentationTestRunner=.F")) {
                // Test failed
                testController.deviceFailed(deviceId);
                return;
            }
        }
        // Test crashed
        testController.deviceCrashed(deviceId);
    }

    private void printResult(int totalCaseNum) {
        Logger.d(TAG, "=============================");
        Logger.d(TAG, "Test Case\t\tResult");
        int passed = 0, crashed = 0, erred = 0, failed = 0, timeout = 0;
        for (int i = 0; i < testResults.size(); i++) {
            TestResult element = testResults.get(i);
            String testCaseName = element.testCaseName, result = element.status.name();
            switch (element.status) {
            case Passed:
                passed++;
                break;
            case Timeout:
                timeout++;
                break;
            case Failed:
                failed++;
                break;
            case Erred:
                erred++;
                break;
            case Crashed:
                crashed++;
                break;
            default:
                result = "Unknown";
                break;
            }
            String text = testCaseName + "\t";
            if (testCaseName.length() < 16)
                text += "\t";
            text += result;
            Logger.d(TAG, text);
        }
        Logger.d(TAG, "=============================");
        Logger.d(TAG, "Total case:\t" + totalCaseNum);
        Logger.d(TAG, "Passed:\t\t" + passed + "\tFailed:\t\t" + failed);
        Logger.d(TAG, "Erred:\t\t" + erred + "\tCrashed:\t" + crashed);
        Logger.d(TAG, "Timeout:\t" + timeout + "\tNo result:\t"
                + (totalCaseNum - passed - failed - erred - crashed - timeout));
        Logger.d(TAG, "=============================");
    }

    private class TestResult {
        String testCaseName;
        TestStatus status;

        public TestResult(String testCaseName, TestStatus status) {
            this.testCaseName = testCaseName;
            this.status = status;
        }
    }
}
