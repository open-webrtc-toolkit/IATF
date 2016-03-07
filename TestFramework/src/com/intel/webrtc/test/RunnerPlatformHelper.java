package com.intel.webrtc.test;

import java.io.BufferedReader;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;

/**
 * For one specific platform, help TestRunner.
 * there should be a field to store the device info.
 * @author bean
 *
 */
public interface RunnerPlatformHelper {
    /**
     * init the parameter that platform needed.
     * For example, androidRunnerHelper could read some settings in config file.
     * @param config
     */
    void initParameters(Config config);

    /**
     * according to the testSuite, init test enviroment on devices and store the device info.
     * For example, installing the test apk for android or starting the karma runner for javascript.
     * @param testSuite
     */
    public void deployTests(TestSuite testSuite);

    /**
     * before running a testCase, start test devices according to the testCase.
     * Task:
     * 1. set up required number of devices;
     * 2. add the local port of the test devices in addressTable;
     * 3. add start message to the startMessageTable.
     * Note: Address table stores the <deviceID, serverPort>, serverPort is the socket
     * server port on test client. The TestController will connect to the socket to get
     * the test result.
     * start successfully, then count down the latch.
     * @param testCase
     *              current test case.
     * @param addressTable
     *              addressTable for test controller to connect when test starts
     * @param startMessageTable
     *              table that stores start messages
     * @param startTestCountDownLatch
     *              starting test latch
     */
    public void startTestDevices(TestCase testCase, Hashtable<String, String> addressTable,
            Hashtable<String, String> startMessageTable, Hashtable<String, String> addressDeviceType,
            CountDownLatch startTestCountDownLatch, LinkedList<ExcuteEnv> retenv);

    /**
     * do some cleaning work before starting a suite.
     * For example, close all the explorer windows.
     */
    void clearBeforeSuite();

    /**
     * do some cleaning work after finishing a suite.
     * For example, close all the testing windows of explorer.
     */
    void clearAfterSuite(TestSuite testSuite);

    /**
     * do some cleaning work before starting a case.
     * For example, clear deviceInfoTable in android.
     */
    void clearBeforeCase();

    /**
     * do some cleaning work after excuting a case.
     * For example, clear forwardRulds in android.
     */
    void clearAfterCase();

    /**
     * parse the excuting command line results and inform TestController the result.
     * @param resultLines
     *              command line output of test excuting.
     * @param deviceId
     *              device ID.
     * @param testController
     *              test controller on server side.
     */
    void parseResult(LinkedList<String> resultLines, String deviceId, TestController testController);

    // TODO: pass RunnerPlatformHelper is not good
    /**
     * Generate a instance of this class when starting a physical device to store the necessary infos for
     * testRunner to get test result.
     * @author bean
     *
     */
    class ExcuteEnv {
        // The executing process that start a physical device.
        // e.g. Android: the process that runs 'adb instrument', JS: the process
        // that runs 'karma start'.
        public Process process;
        // The output buffer of executing process
        public BufferedReader sio;
        // The error output buffer of executing process
        public BufferedReader seo;
        // The deviceName of the logic device
        public String deviceName;
        // The deviceName of the physical device
        public String deviceId;
        // The RunnerPlatformHelper this physical device is under
        // In order to call the parseResult method in RunnerPlatformHelper
        public RunnerPlatformHelper resultPaser;

        public ExcuteEnv(Process process, BufferedReader sio, BufferedReader seo, String deviceName, String deviceId,
                RunnerPlatformHelper resultPaser) {
            this.process = process;
            this.sio = sio;
            this.seo = seo;
            this.deviceName = deviceName;
            this.deviceId = deviceId;
            this.resultPaser = resultPaser;
        }
    }
}
