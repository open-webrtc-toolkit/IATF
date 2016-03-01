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
     * @param addressTable 
     * @param startMessageTable 
     * @param startTestCountDownLatch
     */
    public void startTestDevices(TestCase testCase, Hashtable<String, String> addressTable,
            Hashtable<String, String> startMessageTable,Hashtable<String, String> addressDeviceType, CountDownLatch startTestCountDownLatch,LinkedList<ExcuteEnv> retenv);

    /**
     * pass testController to runnerHelpers to enable test results to be passed back.
     * @param testController
     */
    // void setTestController(TestController testController);

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
     * @param deviceId
     * @param testController 
     */
    void parseResult(LinkedList<String> resultLines, String deviceId, TestController testController);

    // TODO: pass RunnerPlatformHelper is not good
    class ExcuteEnv {
        public Process process;
        public BufferedReader sio;
        public BufferedReader seo;
        public String deviceName;
        public String deviceId;
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
