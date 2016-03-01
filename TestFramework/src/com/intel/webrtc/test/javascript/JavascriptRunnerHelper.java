package com.intel.webrtc.test.javascript;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.validator.PublicClassValidator;

import com.intel.webrtc.test.Config;
import com.intel.webrtc.test.Logger;
import com.intel.webrtc.test.MessageProtocol;
import com.intel.webrtc.test.RunnerPlatformHelper;
import com.intel.webrtc.test.TestCase;
import com.intel.webrtc.test.TestController;
import com.intel.webrtc.test.TestDevice;
import com.intel.webrtc.test.TestSuite;

public class JavascriptRunnerHelper implements RunnerPlatformHelper{
    private String shellPath;
    private String karmaPath;
    private static String TAG="JavascriptRunnerHelper";
    @Override
    public void initParameters(Config config) {
        // TODO read karmaPath from config
        shellPath = config.getShellPath();
        karmaPath = "karma";
    }

    @Override
    public void deployTests(TestSuite testSuite) {
        // TODO Run karma start, to start all the test-conf environment
        LinkedList<TestDevice> devices=testSuite.getTestDevices();
        for(TestDevice device:devices){
            if(device instanceof JavascriptTestDevice){
                JavascriptTestDevice dev=(JavascriptTestDevice)device;
                String startCmd="cd "+dev.jsConfFile.substring(0, dev.jsConfFile.lastIndexOf("/"))+"\n"+karmaPath+" start "+dev.jsConfFile;
                Logger.d(TAG, "startcmd:"+startCmd);
                try{
                    Process p=executeByShell(startCmd);
                    dev.karmaStartProcess=p;
                    BufferedReader sio=new BufferedReader(new InputStreamReader(p.getInputStream()));
                    BufferedReader seo=new BufferedReader(new InputStreamReader(p.getErrorStream()));
                    String resultLine;
                    boolean startFailed = false;
                    while (true) {
                        resultLine = sio.readLine();
                        if (resultLine == null)
                            break;
                        if (resultLine.contains("Cannot start")||resultLine.contains("crashed")) {
                            Logger.e(TAG, "Karma Error! cmd:["+startCmd+"].");
                            startFailed = true;
                        }
                        if(resultLine.contains("captured in")){
                            //success
                            break;
                        }
                        Logger.d(TAG, resultLine);
                    }
//                    while (true) {
//                        resultLine = seo.readLine();
//                        if (resultLine == null)
//                            break;
//                        Logger.d(TAG, resultLine);
//                    }
                    if (startFailed) {
                        throw new RuntimeException("Karma start failed!!");
                    }
                    //Bug: this will cause disconnect of karma
//                    sio.close();
//                    seo.close();
                }catch(Exception e){
                    Logger.e(TAG, "Error occured when karma start:"+startCmd);
                    System.exit(1);
                }
                Logger.d(TAG, "JS instances start successfully!");
            }
        }
    }

    @Override
    public void startTestDevices(TestCase testCase, Hashtable<String, String> addressTable,
            Hashtable<String, String> startMessageTable, Hashtable<String, String> addressDeviceType,
            CountDownLatch startTestCountDownLatch,LinkedList<ExcuteEnv> ret) {
        LinkedList<TestDevice> devices=testCase.getDevices();
        for(TestDevice device:devices){
            if(device instanceof JavascriptTestDevice){
                ExcuteEnv env = startJSTestDevice(testCase, (JavascriptTestDevice) device, addressTable,
                        startMessageTable,addressDeviceType, startTestCountDownLatch);
                ret.add(env);
            }
        }
    }

    private ExcuteEnv startJSTestDevice(TestCase testCase, JavascriptTestDevice device, Hashtable<String, String> addressTable,
            Hashtable<String, String> startMessageTable,Hashtable<String, String> addressDeviceType, CountDownLatch startTestCountDownLatch) {
      //ExcuteEnv(Process process, BufferedReader sio, BufferedReader seo, String 
        //deviceName, String deviceId,RunnerPlatformHelper resultPaser)
        // TODO Run karma run, to start a test case
        String cmd="cd "+device.jsConfFile.substring(0, device.jsConfFile.lastIndexOf("/"))+"\n"+karmaPath+" run "+device.jsConfFile +" -- --grep="+testCase.getName();
        Logger.d(TAG,"run cmd:"+cmd);
        Process p;
        try {
            p = executeByShell(cmd);
            BufferedReader sio=new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader seo=new BufferedReader(new InputStreamReader(p.getErrorStream()));
            ExcuteEnv env=new ExcuteEnv(p, sio, seo, device.getName(), device.deviceInfo.id, this);
            addressTable.put(device.deviceInfo.id, device.deviceInfo.localPort);
            addressDeviceType.put(device.deviceInfo.id, this.getClass().getName());
            generateStartMessage(testCase, device, startMessageTable,addressTable);
            startTestCountDownLatch.countDown();
            return env;
        } catch (IOException e) {
            Logger.e(TAG,"Error occured when karma run:"+cmd);
            System.exit(1);
            e.printStackTrace();
        }
        return null;
    }

    private void generateStartMessage(TestCase testCase, JavascriptTestDevice device,
            Hashtable<String, String> startMessageTable, Hashtable<String, String> addressTable) {
        JSONObject message = new JSONObject();
        try {
            message.put(MessageProtocol.MESSAGE_TYPE, MessageProtocol.TEST_START);
            //TODO: tell lockserver to broadcast the message, case name needed ?
            startMessageTable.put(device.deviceInfo.id, message.toString());
            addressTable.put(device.deviceInfo.id, device.deviceInfo.localPort+"");
        } catch (JSONException e) {
            Logger.e(TAG, "Error occured when generate start message for lock server ");
        }
    }

    @Override
    public void clearBeforeCase() {
        // TODO need to restart?
        
    }

    @Override
    public void clearAfterCase() {
        // TODO how to stop running instance
        
    }

    @Override
    public void parseResult(LinkedList<String> resultLines, String deviceId, TestController testController) {
        // TODO 
        //testController.deviceCrashed, devicePassed, deviceErred, deviceFailed
        if (testController == null) {
            Logger.e(TAG, "parseAndroidResult: testController is null!");
            System.exit(1);
        }
        if (resultLines == null || resultLines.isEmpty()) {
            testController.deviceCrashed(deviceId);
            return;
        }
        Pattern p=Pattern.compile("Executed 1 of [\\d]( \\(skipped \\d\\))? ([^\\(]*)");
        for (int i = 0; i < resultLines.size(); i++) {
            String resultLine = resultLines.get(i);
            Matcher m=p.matcher(resultLine);
            if(m.find()){
                String result=m.group(2).trim();
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
                if(result.contains("ERROR")){
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
