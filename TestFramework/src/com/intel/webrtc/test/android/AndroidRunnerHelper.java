package com.intel.webrtc.test.android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.json.JSONException;
import org.json.JSONObject;

import com.intel.webrtc.test.Config;
import com.intel.webrtc.test.Logger;
import com.intel.webrtc.test.MessageProtocol;
import com.intel.webrtc.test.RunnerPlatformHelper;
import com.intel.webrtc.test.TestCase;
import com.intel.webrtc.test.TestController;
import com.intel.webrtc.test.TestDevice;
import com.intel.webrtc.test.TestSuite;
import com.intel.webrtc.test.android.AndroidDeviceInfo.AndroidDeviceType;

/**
 * RunnerHelper on android platform. Including deploy apk, manage android devices and parse Instrument result.
 * @author bean
 *
 */
public class AndroidRunnerHelper implements RunnerPlatformHelper {
    // Debug TAG
    public static String TAG = "AndroidRunnerHelper";
    // Configurations
    private String adbPath, antPath, shellPath, apkName, androidTestPackage, androidTestClass, androidTestActivity;
    // Store information of all the android devices
    private LinkedList<AndroidDeviceInfo> androidDeviceInfos;

    /**
     * Init params from config
     */
    @Override
    public void initParameters(Config config) {
        adbPath = config.getAdbPath();
        antPath = config.getAntPath();
        shellPath = config.getShellPath();
        apkName = null;
        androidTestPackage = config.getAndroidTestPackage();
        androidTestClass = config.getAndroidTestClass();
        androidTestActivity = config.getAndroidTestActivity();
        androidDeviceInfos = new LinkedList<AndroidDeviceInfo>();
    }

    /**
     * Deploy test apk on all the accessable android devices.
     */
    // TODO: but cannot make use of newly connected devices, because the
    // apk is deployed only once before a suite test started.
    @Override
    public void deployTests(TestSuite testSuite) {
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

    /**
     * Refresh the device info before every case to avoid start a case on
     * an offline device.
     */
    @Override
    public void clearBeforeCase() {
        androidDeviceInfos.clear();
        getTestDeviceInfo();
    }

    /**
     * Start all the android test devices in testCase.
     */
    @Override
    public void startTestDevices(TestCase testCase, Hashtable<String, String> addressTable,
            Hashtable<String, String> startMessageTable, Hashtable<String, String> addressDeviceType,
            CountDownLatch startTestCountDownLatch, LinkedList<ExcuteEnv> ret) {
        for (TestDevice testDevice : testCase.getDevices()) {
            if (testDevice instanceof AndroidTestDevice) {
                ExcuteEnv env = startAndroidTestDevice(testCase, (TestDevice) testDevice, addressTable,
                        startMessageTable, addressDeviceType, startTestCountDownLatch);
                ret.add(env);
            }
        }
    }

    /**
     * Remove all the forward rules after the case done.
     */
    @Override
    public void clearAfterCase() {
        removeForwardRules();
    }

    /**
     * parse the test result of a single Android device, and report it to the
     * TestController
     *
     * @param resultLines
     *            the test result lines
     * @param deviceId
     *            the device id of the device.
     * @param testController
     *             the server controller to receive the test result.
     */
    @Override
    public void parseResult(LinkedList<String> resultLines, String deviceId, TestController testController) {
        // report to controller
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
                        String systemName = System.getProperties().getProperty("os.name");
                        if (systemName.contains("Windows")) {
                            apkName = "bin/TestFrameworkTest-debug-unaligned.apk";
                        }
                        else{
                        String[] pathItems = resultLine.split("/");
                         apkName = "bin/" + pathItems[pathItems.length - 1];
                        }
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
                // GET CLIENT IP
                try {
                    process = executeByShell(adbPath + " -s " + serialId + " shell netcfg");
                    sio1 = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    process.waitFor();
                    while (true) {
                        // TODO add some fault tolerance related code.
                        resultLine = sio1.readLine();
                        if (resultLine == null)
                            break;
                        if (resultLine.startsWith("wlan0")|| resultLine.startsWith("eth0")) {
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
     * This method is used to deploy the test APK to a single device.
     *
     * @param info
     *            the AndroidTestInfo of a device.
     * @param latch
     *            the CountDownLatch to control the deploy process.
     */
    private void deployApk(AndroidDeviceInfo info, CountDownLatch latch) {
        Process process;
        BufferedReader sio, seo;
        String cmd = adbPath + " -s " + info.id + " install -r " + apkName;
        try {
            process = executeByShell(cmd);
            sio = new BufferedReader(new InputStreamReader(process.getInputStream()));
            seo = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        } catch (IOException e) {
            e.printStackTrace();
            Logger.e(TAG, "Error occured when building test Apk.");
            return;
        }
        waitForResultAndLog(process, sio, seo, latch);
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
        String systemName = System.getProperties().getProperty("os.name");
        if (systemName.contains("Windows")) {
        return Runtime.getRuntime().exec(new String[] {"cmd.exe", "/c", cmd});
         }
         else{
         return Runtime.getRuntime().exec(new String[] { shellPath, "-c", cmd });
         }
    }

    /**
     * Count down latch and print the call stack info.
     * @param latch
     */
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
     * Start an android test device, put the informations needed in table and
     * return the {@link com.intel.webrtc.test.RunnerPlatformHelper.ExcuteEnv Excute environment}
     * to read the test result. Finally count down the starting latch.
     * @param curTestCase
     *              current test case
     * @param device
     *              test device
     * @param addressTable
     *              addressTable for test controller to connect when test starts
     * @param startMessageTable
     *              table that stores start messages
     * @param addressDeviceType
     *              table that stores the device type of every communication address
     * @param latch
     *              starting latch
     * @return Excute environment
     */
    private ExcuteEnv startAndroidTestDevice(TestCase curTestCase, TestDevice device,
            Hashtable<String, String> addressTable, Hashtable<String, String> startMessageTable,
            Hashtable<String, String> addressDeviceType, CountDownLatch latch) {
        // TODO this solution of device assigning is the simplest one.
        AndroidDeviceInfo info;
        if (!androidDeviceInfos.isEmpty()) {
            info = androidDeviceInfos.removeFirst();
            addressTable.put(info.id, "" + info.localPort);
            // set forward rule
            Process process;
            String cmd = adbPath + " -s " + info.id + " forward tcp:" + info.localPort + " tcp:"
                    + AndroidClientController.androidLocalPort;
            Logger.d(TAG, "forward command:" + cmd);
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
            generateStartMessage(curTestCase, device, startMessageTable, info);
        } else {
            Logger.e(TAG, "There is no Android device available!");
            System.exit(1);
            return null;
        }
        try { // run Android Instrumentation Test via ADB
            Process process;
            BufferedReader sio, seo;
            String cmd = adbPath + " -s " + info.id + " shell am instrument -r -e class " + androidTestClass
                    + "#testEntry -w " + androidTestPackage + "/android.test.InstrumentationTestRunner";
            process = executeByShell(cmd);
            sio = new BufferedReader(new InputStreamReader(process.getInputStream()));
            seo = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            countDown(latch);
            return new ExcuteEnv(process, sio, seo, device.getName(), info.id, this);
        } catch (IOException e) {
            Logger.e(TAG, "Error occured when start instrument on " + device.getName());
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }

    /**
     * This method is used to generate a start message for one android device.
     *
     * @param device
     *            the AndroidTestDevice going to run.
     * @param info
     *            the AndroidDeviceInfo of the device.
     */
    private void generateStartMessage(TestCase currentTestCase, TestDevice device,
            Hashtable<String, String> startMessageTable, AndroidDeviceInfo info) {
        JSONObject message = new JSONObject();
        try {
            message.put(MessageProtocol.MESSAGE_TYPE, MessageProtocol.TEST_START);
            message.put(MessageProtocol.CLASS_NAME, device.getClass().getName());
            message.put(MessageProtocol.METHOD_NAME, currentTestCase.getName());
            message.put(MessageProtocol.TEST_ACTIVITY, androidTestActivity);
            message.put(MessageProtocol.TEST_PACKAGE, androidTestPackage);
            Logger.d(TAG, "Start message of device " + device.getName() + " is " + message.toString());
            startMessageTable.put(info.id, message.toString());
        } catch (JSONException e) {
            Logger.e(TAG, "Error occured when generate start message for device " + device.getName());
        }
    }

    /**
     * Clear operations before testSuite starts.
     */
    @Override
    public void clearBeforeSuite() {
    }

    /**
     * Clear opearations after testSuite ends.
     */
    @Override
    public void clearAfterSuite(TestSuite testSuite) {
    }
}
