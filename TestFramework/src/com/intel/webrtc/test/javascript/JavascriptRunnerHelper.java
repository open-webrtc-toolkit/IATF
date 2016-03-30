package com.intel.webrtc.test.javascript;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
/**
 * RunnerHelper on JS. Including start test, clear environment and parse result.
 * @author bean
 *
 */
public class JavascriptRunnerHelper implements RunnerPlatformHelper {
    // Debug TAG
    private static String TAG = "JavascriptRunnerHelper";
    // Shell path init from the test framework config file
    private String shellPath;
    // Karma path init from the test framework config file
    private String karmaPath;

    @Override
    public void initParameters(Config config) {
        // TODO read karmaPath from config
        shellPath = config.getShellPath();
        karmaPath = "karma";
    }

    /**
     * Start all the karma config files in testSuite. Run before testSuite starts. CMD: "karma start <karmaConfigFile>"
     */
    @Override
    public void deployTests(TestSuite testSuite) {
        LinkedList<TestDevice> devices = testSuite.getTestDevices();
        for (TestDevice device : devices) {
            if (device instanceof JavascriptTestDevice) {
                JavascriptTestDevice dev = (JavascriptTestDevice) device;
                String startCmd = "cd " + dev.jsConfFile.substring(0, dev.jsConfFile.lastIndexOf("/")) + "\n"
                        + karmaPath + " start " + dev.jsConfFile;
                Logger.d(TAG, "startcmd:" + startCmd);
                try {
                    Process p = executeByShell(startCmd);
                    dev.karmaStartProcess = p;
                    BufferedReader sio = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String resultLine;
                    boolean startFailed = false;
                    while (true) {
                        resultLine = sio.readLine();
                        if (resultLine == null)
                            break;
                        if (resultLine.contains("Cannot start") || resultLine.contains("crashed")) {
                            Logger.e(TAG, "Karma Error! cmd:[" + startCmd + "].");
                            startFailed = true;
                        }
                        if (resultLine.contains("captured in")) {
                            // success
                            break;
                        }
                        Logger.d(TAG, resultLine);
                    }
                    if (startFailed) {
                        throw new RuntimeException("Karma start failed!!");
                    }
                    // Bug: this will cause disconnect of karma
                    // sio.close();
                    // seo.close();
                } catch (Exception e) {
                    Logger.e(TAG, "Error occured when karma start:" + startCmd);
                    System.exit(1);
                }
                Logger.d(TAG, "JS instances start successfully!");
            }
        }
    }

    /**
     * Starts all the JS test devices in the testCase.
     */
    @Override
    public void startTestDevices(TestCase testCase, Hashtable<String, String> addressTable,
            Hashtable<String, String> startMessageTable, Hashtable<String, String> addressDeviceType,
            CountDownLatch startTestCountDownLatch, LinkedList<ExcuteEnv> ret) {
        LinkedList<TestDevice> devices = testCase.getDevices();
        for (TestDevice device : devices) {
            if (device instanceof JavascriptTestDevice) {
                ExcuteEnv env = startJSTestDevice(testCase, (JavascriptTestDevice) device, addressTable,
                        startMessageTable, addressDeviceType, startTestCountDownLatch);
                ret.add(env);
            }
        }
    }

    /**
     * Start JS test devices , put the informations needed in table and
     * return the {@link com.intel.webrtc.test.RunnerPlatformHelper.ExcuteEnv Excute environment}
     * to read the test result. Finally count down the starting latch.
     * @param testCase
     *              current test case.
     * @param device
     *              JS test device
     * @param addressTable
     *              addressTable for test controller to connect when test starts
     * @param startMessageTable
     *              table that stores start messages
     * @param addressDeviceType
     *              table that stores the device type of every communication address
     * @param startTestCountDownLatch
     *              starting test latch
     * @return Excute environment
     */
    private ExcuteEnv startJSTestDevice(TestCase testCase, JavascriptTestDevice device,
            Hashtable<String, String> addressTable, Hashtable<String, String> startMessageTable,
            Hashtable<String, String> addressDeviceType, CountDownLatch startTestCountDownLatch) {
        String cmd = "cd " + device.jsConfFile.substring(0, device.jsConfFile.lastIndexOf("/")) + "\n" + karmaPath
                + " run " + device.jsConfFile + " -- --grep=" + testCase.getName();
        Logger.d(TAG, "run cmd:" + cmd);
        Process p;
        try {
            p = executeByShell(cmd);
            BufferedReader sio = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader seo = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            ExcuteEnv env = new ExcuteEnv(p, sio, seo, device.getName(), device.deviceInfo.id, this);
            addressTable.put(device.deviceInfo.id, device.deviceInfo.localPort);
            addressDeviceType.put(device.deviceInfo.id, this.getClass().getName());
            generateStartMessage(testCase, device, startMessageTable, addressTable);
            startTestCountDownLatch.countDown();
            return env;
        } catch (IOException e) {
            Logger.e(TAG, "Error occured when karma run:" + cmd);
            System.exit(1);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Generate start message for JS devices and put it into startMessageTable.
     * Also, addrees info is put in the addressTable.
     * @param testCase
     *              current test case.
     * @param device
     *              JS test device.
     * @param startMessageTable
     *              table that stores start messages
     * @param addressTable
     *              table that stores the device type of every communication address
     */
    private void generateStartMessage(TestCase testCase, JavascriptTestDevice device,
            Hashtable<String, String> startMessageTable, Hashtable<String, String> addressTable) {
        JSONObject message = new JSONObject();
        try {
            message.put(MessageProtocol.MESSAGE_TYPE, MessageProtocol.TEST_START);
            startMessageTable.put(device.deviceInfo.id, message.toString());
            addressTable.put(device.deviceInfo.id, device.deviceInfo.localPort + "");
        } catch (JSONException e) {
            Logger.e(TAG, "Error occured when generate start message for lock server ");
        }
    }

    @Override
    public void clearBeforeCase() {
    }

    @Override
    public void clearAfterCase() {
    }

    /**
     * Parse the output from JS platform(command line output of 'karma run' process)
     * and inform the testController about the result.
     */
    @Override
    public void parseResult(LinkedList<String> resultLines, String deviceId, TestController testController) {
        if (testController == null) {
            Logger.e(TAG, "parseAndroidResult: testController is null!");
            System.exit(1);
        }
        if (resultLines == null || resultLines.isEmpty()) {
            testController.deviceCrashed(deviceId);
            return;
        }
        Pattern p = Pattern.compile("Executed 1 of [\\d]( \\(skipped \\d\\))? ([^\\(]*)");
        for (int i = 0; i < resultLines.size(); i++) {
            String resultLine = resultLines.get(i);
            Matcher m = p.matcher(resultLine);
            if (m.find()) {
                String result = m.group(2).trim();
                if (result.contains("SUCCESS")) {
                    // Test passed
                    testController.devicePassed(deviceId);
                    return;
                }
                if (result.contains("FAILED")) {
                    // Test failed
                    testController.deviceFailed(deviceId);
                    return;
                }
                if (result.contains("ERROR")) {
                    // Test erred
                    testController.deviceErred(deviceId);
                    return;
                }
            }
        }
        // Test crashed
        testController.deviceCrashed(deviceId);
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
     * Stop all the chrome processes.
     */
    private void stopChrome() {
        String closeChromeCmd = "ps -e | grep chrome | awk '{print $1}' | xargs kill -9";
        Process p;
        try {
            p = executeByShell(closeChromeCmd);
            p.waitFor();
        } catch (IOException e) {
            Logger.e(TAG, "Error occured while closing chrome.");
            e.printStackTrace();
        } catch (InterruptedException e) {
            Logger.e(TAG, "Error occured while waiting for chrome to close.");
            e.printStackTrace();
        }
    }

    /**
     * Stop all the chrome windows before test suite starts.
     */
    @Override
    public void clearBeforeSuite() {
        System.out.println("stopChrome before suite");
        stopChrome();
    }

    /**
     * Clear operations after testSuite finishes.<br>
     *      1. Kill the 'karma start' process;
     *      2. Close the clientTestController
     *      2. Stop all the chrome windows after test suite ends.
     */
    @Override
    public void clearAfterSuite(TestSuite testSuite) {
        for (TestDevice device : testSuite.getTestDevices()) {
            if (device instanceof JavascriptTestDevice) {
                ((JavascriptTestDevice) device).karmaStartProcess.destroy();
            }
        }
        for (JavascriptClientController jscc : JavascriptClientController.jsControllers) {
            jscc.close();
        }
        System.out.println("stopChrome after suite");
        stopChrome();
    }
}
