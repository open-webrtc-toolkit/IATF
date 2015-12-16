package com.intel.webrtc.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.intel.webrtc.test.RunnerPlatformHelper.ExcuteEnv;
import com.intel.webrtc.test.TestController.TestStatus;
import com.intel.webrtc.test.android.AndroidRunnerHelper;

public class TestRunner {
    final static private String TAG = "TestRunner";

    private Config config;
    private TestController testController;
    //deviceID-IP(communication socket server of the test client )
    private Hashtable<String, String> addressTable;
    //deviceID-startMsg
    private Hashtable<String, String> startMessageTable;
    //TODO: change to DeviceInfo
    private LinkedList<DeviceInfo> deviceInfos;
    private LinkedList<TestResult> testResults;
    private LinkedList<RunnerPlatformHelper> runnerHelpers;

    // Get the port from the configuration, and pass it to the TestController
    int port;
    private TestCase currentTestCase;

    public TestRunner(Config config, TestSuite testSuite) {
        if (config == null) {
            this.config = new Config(null);
        } else {
            this.config = config;
        }
      //TODO: how to init runnerHelper properly
        runnerHelpers=new LinkedList<RunnerPlatformHelper>();
        runnerHelpers.add(new AndroidRunnerHelper());
        initParameters();
        testController = null;
        deviceInfos = new LinkedList<DeviceInfo>();
        addressTable = new Hashtable<String, String>();
        startMessageTable = new Hashtable<String, String>();
        testResults = new LinkedList<TestResult>();
    }
    private void initParameters() {
        //keep in TestRunner
        port = config.getPort();
        for(RunnerPlatformHelper helper:runnerHelpers){
            helper.initParameters(config);
        }
    }

    /**
     * run TestCases in the TestSuite one by one.
     *
     * @param tests
     *            the test suite.
     */
    public void runTestSuite(TestSuite tests) {
        for(RunnerPlatformHelper helper:runnerHelpers){
            helper.deployTests(tests);
        }
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
        //reset the test environment
        currentTestCase = testCase;
        clearBeforeCase();
        //initialize the test devices
        CountDownLatch startTestCountDownLatch = new CountDownLatch(testCase.getDevices().size());
        //calling helpers to start the case
        for(RunnerPlatformHelper helper:runnerHelpers){
        	LinkedList<ExcuteEnv> envs=helper.startTestDevices(testCase, addressTable, startMessageTable, startTestCountDownLatch);
        	for(ExcuteEnv env:envs){
        		this.waitTestResult(env, startTestCountDownLatch);
        	}
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
        //TODO: this is not a good waiting!
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Logger.e(TAG, "Interrupted when waiting for test cases ready.");
            e.printStackTrace();
        }
        testController = new TestController(addressTable, startMessageTable);
        //TODO: no need?
//        for(RunnerPlatformHelper helper:runnerHelpers){
//            helper.setTestController(testController);
//        }
        TestStatus testCaseReslut = testController.start();// TODO result?
        testResults.add(new TestResult(testCase.getName(), testCaseReslut));
        //calling helpers to clear after finishing a case
        for(RunnerPlatformHelper helper:runnerHelpers){
            helper.clearAfterCase();
        }
    }
    private void clearBeforeCase(){
        deviceInfos.clear();
        addressTable.clear();
        startMessageTable.clear();
        for(RunnerPlatformHelper helper:runnerHelpers){
            helper.clearBeforeCase();
        }
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
    
    private void waitTestResult(final ExcuteEnv env, final CountDownLatch latch) {
        new Thread() {
            public void run() {
                LinkedList<String> resultLines = new LinkedList<String>();
                String resultLine;
                BufferedReader standardIOReader=env.sio;
                BufferedReader standardErrorStreamReader=env.seo;
                String deviceName=env.deviceName;
                String deviceId=env.deviceId;
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
                    env.resultPaser.parseResult(resultLines, deviceId, testController);
                } catch (IOException e) {
                    Logger.e(TAG, "Error occured when reading test result.");
                    countDown(latch);
                    e.printStackTrace();
                }
            }
        }.start();
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
}
