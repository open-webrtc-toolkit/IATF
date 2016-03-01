package com.intel.webrtc.test.p2ptest;

import java.io.File;
import java.io.IOException;

import com.intel.webrtc.test.Config;
import com.intel.webrtc.test.TestRunner;
import com.intel.webrtc.test.TestSuite;
import com.intel.webrtc.test.javascript.JavascriptTestDevice;

public class P2PJS2Android {
    public static void main(String args[]) throws IOException{
        String path = "test.cfg";
        if (args.length > 0)
            path = args[0];
        File file = new File(path);
        Config config = new Config(file);
        TestSuite testSuite = new TestSuite();

        //You can test testTwoUserInteraction scene with one of the following cases.
        //Case 1: JS as User1, Android as User2
        JavascriptTestDevice jsdevice1=new JavascriptTestDevice("/home/bean/workspace/webrtc-javascript-sdk/test/p2pInteractionTest/test-peerwn-user1.js",
                "/home/bean/workspace/webrtc-javascript-sdk/test/p2pInteractionTest/testclient1.conf.js");
        jsdevice1.setName("device1");
        TestDevice5 device5=new TestDevice5();
        device5.setName("device2");
        testSuite.addTestDevice(jsdevice1);
        testSuite.addTestDevice(device5);

        //Case 2: Android as User1, JS as User2
//        TestDevice4 device4=new TestDevice4();
//        device4.setName("device1");
//        JavascriptTestDevice jsdevice2=new JavascriptTestDevice("/home/bean/workspace/webrtc-javascript-sdk/test/p2pInteractionTest/test-peerwn-user2.js",
//        "/home/bean/workspace/webrtc-javascript-sdk/test/p2pInteractionTest/testclient2.conf.js");
//        jsdevice2.setName("device2");
//        testSuite.addTestDevice(device4);
//        testSuite.addTestDevice(jsdevice2);

        TestRunner testRunner = new TestRunner(config,testSuite);
        System.out.println(testSuite.toString());
        testRunner.runTestSuite(testSuite);
    }
}
