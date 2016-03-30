package com.intel.webrtc.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.json.JSONException;
import org.json.JSONObject;
import com.intel.webrtc.test.TestController.TestStatus;
import com.intel.webrtc.test.android.AndroidRunnerHelper;
import com.intel.webrtc.test.javascript.JavascriptRunnerHelper;
/**
 * Control the test process. <br>
 * <li>Utilizing the RunnerHelper in different platform to deploy test</li>
 * start test and access the third-party test tools.
 * <li>Managing the LockServer to pass lock between test server and test devices.
 * <li>Collecting the test result for the TestSuite.
 * @author bean
 *
 */
public class TestRunner implements RunnerPlatformHelper {
    // Debug tag
    final static private String TAG = "TestRunner";
    // global config of this application, init from file
    private Config config;
    // controller on test server side for current test case
    private TestController testController;
    // deviceID-IP(communication socket server of the test client )
    private Hashtable<String, String> addressTable;
    // deviceID-startMsg
    private Hashtable<String, String> startMessageTable;
    // deviceID-deviceType(JS, android,etc)
    private Hashtable<String, String> addressDeviceType;
    // store the results of all the cases
    private LinkedList<TestResult> testResults;
    // store all the RunnerPlatformHelpers, help the TestRunner to do some
    // platform-related
    // operations, see {@link .RunnerPlatformHelper RunnerPlatformHelper}.
    // Register all the
    // RunnerPlatformHelper before testing.
    private LinkedList<RunnerPlatformHelper> runnerHelpers;
    // Store long-term socket connection to test clients
    private Hashtable<String, Socket> storedSockets = null;
    // Mated with storedSockets, store printWriters to test clients instead.
    private Hashtable<String, PrintWriter> storedPrintWriters = null;
    // Mate with storedControllerWorkers, store ControllerWorkers for test
    // clients instead.
    private Hashtable<String, ControllerWorker> storedControllerWorkers = null;
    // {@link .LockServer Lock server} for current testing.
    protected LockServer ls = null;
    // Thread which is started to run lock server.
    private Thread lockServerThread = null;

    /**
     * Init a TestRunner with application config and the test suite to run.
     * @param config config file of all the configuration needed.
     * @param testSuite test suite inited from test file.
     */
    public TestRunner(Config config, TestSuite testSuite) {
        if (config == null) {
            this.config = new Config(null);
        } else {
            this.config = config;
        }
        runnerHelpers = new LinkedList<RunnerPlatformHelper>();
        testController = null;
        addressTable = new Hashtable<String, String>();
        startMessageTable = new Hashtable<String, String>();
        addressDeviceType = new Hashtable<String, String>();
        testResults = new LinkedList<TestResult>();
        storedControllerWorkers = new Hashtable<String, ControllerWorker>();
        storedSockets = new Hashtable<String, Socket>();
        storedPrintWriters = new Hashtable<String, PrintWriter>();
        lockServerThread = startLockServerThread();
        // TODO: How to init runnerHelper properly
        // All the RunnerPlatformHelper should be registered here
        runnerHelpers.add(new AndroidRunnerHelper());
        runnerHelpers.add(new JavascriptRunnerHelper());
    }

    /**
     * Run TestCases in the TestSuite one by one, main logic of this class.
     *
     * @param testSuite
     *            the test suite.
     */
    public void runTestSuite(TestSuite testSuite) {
        try {
            initParameters(config);
            clearBeforeSuite();
            deployTests(testSuite);
            Hashtable<String, TestCase> testCases = testSuite.getTestCases();
            Iterator<Entry<String, TestCase>> iterator = testCases.entrySet().iterator();
            while (iterator.hasNext()) {
                Entry<String, TestCase> entry = iterator.next();
                runTestCase(entry.getValue());
            }
            printResult(testSuite.getTestCases().size());
            // TODO generate test result with enhance format.
            clearAfterSuite(testSuite);
        } catch (Exception e) {
            ls.close();
        }
    }

    /**
     * Clear the stored long-term sockets to clients and close the LockServer.
     */
    private void closeAfterSuite() {
        for (String deviceId : storedSockets.keySet()) {
            storedControllerWorkers.get(deviceId).close();
            storedPrintWriters.get(deviceId).close();
            try {
                storedSockets.get(deviceId).close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ls.close();
        lockServerThread.interrupt();
    }

    /**
     * Run single test case.
     *
     * @param testCase
     *            the test case which is going to run.
     */
    private void runTestCase(TestCase testCase) {
        // 1. reset the test environment before case
        clearBeforeCase();
        // 2. initialize and deploy on the physical test devices
        CountDownLatch startTestCountDownLatch = new CountDownLatch(testCase.getDevices().size());
        LinkedList<ExcuteEnv> envs = new LinkedList<ExcuteEnv>();
        startTestDevices(testCase, addressTable, startMessageTable, addressDeviceType, startTestCountDownLatch, envs);
        generateLockServerStartMessage(startMessageTable, addressTable);
        try {
            // TODO: time should be read from config
            if (!startTestCountDownLatch.await(100000, TimeUnit.MILLISECONDS))
                Logger.e(TAG, "Time out when start test cases.");
            else {
                Logger.d(TAG, "Test cases started successfully.");
            }
        } catch (InterruptedException e) {
            Logger.e(TAG, "Interrupted when start test cases.");
            e.printStackTrace();
        }
        // TODO: this is not a good waiting!
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Logger.e(TAG, "Interrupted when waiting for test cases ready.");
            e.printStackTrace();
        }
        // 3. Initialize a testController to test one single case and waiting
        // for result
        testController = new TestController(addressTable, startMessageTable, addressDeviceType, storedSockets,
                storedPrintWriters, storedControllerWorkers);
        for (ExcuteEnv env : envs) {
            this.waitTestResult(env);
        }
        // Controller controls testing one single test case
        TestStatus testCaseReslut = testController.start();
        // 4. Collect the test result
        testResults.add(new TestResult(testCase.getName(), testCaseReslut));
        // 5. reset the test environment after case
        clearAfterCase();
    }

    /**
     * Print the test results collected in testResults.
     * @param totalCaseNum
     */
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

    /**
     * Record for test result.
     * @author bean
     *
     */
    private class TestResult {
        String testCaseName;
        TestStatus status;

        public TestResult(String testCaseName, TestStatus status) {
            this.testCaseName = testCaseName;
            this.status = status;
        }
    }

    /**
     * Start a new thread to read the test results from the {@link .RunnerPlatformHelper.ExcuteEnv ExcuteEnv}.<br>
     * @param env
     *          {@link .RunnerPlatformHelper.ExcuteEnv Excuting environment}
     */
    private void waitTestResult(final ExcuteEnv env) {
        new Thread() {
            public void run() {
                LinkedList<String> resultLines = new LinkedList<String>();
                String resultLine;
                BufferedReader standardIOReader = env.sio;
                BufferedReader standardErrorStreamReader = env.seo;
                String deviceName = env.deviceName;
                String deviceId = env.deviceId;
                try {
                    while (true) {
                        resultLine = standardIOReader.readLine();
                        if (resultLine == null)
                            break;
                        resultLines.add(resultLine);
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
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * Start a thread to create the Lock socket server.
     * @return The thread runs lock socket server.
     */
    private Thread startLockServerThread() {
        Thread ret = new Thread() {
            @Override
            public void run() {
                super.run();
                ls = new LockServer();
                try {
                    ls.start();
                } catch (Exception e) {
                    System.out.println("Error occured while starting LockServer!");
                    System.exit(1);
                }
            }
        };
        ret.start();
        return ret;
    }

    /**
     * This method is used to add lock server information in addressTable & startMessageTable.
     * @param startMessageTable start message table.
     * @param addressTable address table.
     */
    private void generateLockServerStartMessage(Hashtable<String, String> startMessageTable,
            Hashtable<String, String> addressTable) {
        JSONObject message = new JSONObject();
        try {
            message.put(MessageProtocol.MESSAGE_TYPE, MessageProtocol.TEST_START);
            // TODO: tell lockserver to broadcast the message, case name needed
            // ?
            startMessageTable.put(LockServer.infoid, message.toString());
            addressTable.put(LockServer.infoid, ls.localport + "");
        } catch (JSONException e) {
            Logger.e(TAG, "Error occured when generate start message for lock server ");
        }
    }

    /**
     * Init params from the config and pass config to RunnerPlatformHelpers.
     */
    @Override
    public void initParameters(Config config) {
        for (RunnerPlatformHelper helper : runnerHelpers) {
            helper.initParameters(config);
        }
    }

    /**
     * Calling helpers to deploy the test case and starting the client test bed.<br>
     */
    @Override
    public void deployTests(TestSuite testSuite) {
        for (RunnerPlatformHelper helper : runnerHelpers) {
            helper.deployTests(testSuite);
        }
    }

    /**
     * Calling helpers to start the case.<br>
     * Helpers operations see {@link .RunnerPlatformHelper#startTestDevices startTestDevices}.
     */
    @Override
    public void startTestDevices(TestCase testCase, Hashtable<String, String> addressTable,
            Hashtable<String, String> startMessageTable, Hashtable<String, String> addressDeviceType,
            CountDownLatch startTestCountDownLatch, LinkedList<ExcuteEnv> retenv) {
        for (RunnerPlatformHelper helper : runnerHelpers) {
            helper.startTestDevices(testCase, addressTable, startMessageTable, addressDeviceType,
                    startTestCountDownLatch, retenv);
        }
    }

    /**
     * Do some cleaning before running a test suite and call the helper clear method.
     */
    @Override
    public void clearBeforeSuite() {
        for (RunnerPlatformHelper helper : runnerHelpers) {
            helper.clearBeforeSuite();
        }
    }

    /**
     * Do some cleaning after running a test suite and call the helper clear method.
     */
    @Override
    public void clearAfterSuite(TestSuite testSuite) {
        closeAfterSuite();
        for (RunnerPlatformHelper helper : runnerHelpers) {
            helper.clearAfterSuite(testSuite);
        }
    }

    /**
     * Do some cleaning before running a case and call the helper clear method.
     */
    @Override
    public void clearBeforeCase() {
        if (testController != null) {
            testController.close();
            testController = null;
        }
        addressTable.clear();
        addressDeviceType.clear();
        startMessageTable.clear();
        for (RunnerPlatformHelper helper : runnerHelpers) {
            helper.clearBeforeCase();
        }
    }

    /**
     * Calling helpers to clear after finishing a case
     */
    @Override
    public void clearAfterCase() {
        for (RunnerPlatformHelper helper : runnerHelpers) {
            helper.clearAfterCase();
        }
    }

    @Override
    public void parseResult(LinkedList<String> resultLines, String deviceId, TestController testController) {
        // Do nothing
    }
}
