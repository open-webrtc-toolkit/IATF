package com.intel.webrtc.test.p2ptest;

import java.io.File;

import com.intel.webrtc.test.Config;
import com.intel.webrtc.test.TestRunner;
import com.intel.webrtc.test.TestSuite;

public class P2PMain {
    /*
     * For every test case on different devices should use the same name, and
     * start with "test". To define a case, follow the steps below:
     * 1. Init test activity and p2p context.
     * 2. Define variables for the test(userNames,serverIP and locks);
     * 3. Take actions
     *      Action Loop:
     *      (waitForStartLock ->)
     *      (checkWaitActionObserverCallback ->)
     *      callStaticAction ->
     *      checkActionObserverCallback(only for onChatStarted after accept,
     *      onChatStopped after stop, onServerDisconnected, onServerConnected)->
     *      (notifyFinishLock)
     */
    public static void main(String args[]) {
        String path = "test.cfg";
        if (args.length > 0)
            path = args[0];
        File file = new File(path);
        Config config = new Config(file);
        TestRunner testRunner = new TestRunner(config);
        TestSuite testSuite = new TestSuite();
        TestDevice1 device1 = new TestDevice1();
        device1.setName("device1");
        TestDevice2 device2 = new TestDevice2();
        device2.setName("device2");
        TestDevice3 device3 = new TestDevice3();
        device3.setName("device3");
        testSuite.addTestDevice(device1);
        testSuite.addTestDevice(device2);
        testSuite.addTestDevice(device3);
//        TestDevice4 device4=new TestDevice4();
//        device4.setName("device1");
//        TestDevice5 device5=new TestDevice5();
//        device5.setName("device2");
//        testSuite.addTestDevice(device4);
//        testSuite.addTestDevice(device5);
        System.out.println(testSuite.toString());
        testRunner.runTest(testSuite);
    }
}
