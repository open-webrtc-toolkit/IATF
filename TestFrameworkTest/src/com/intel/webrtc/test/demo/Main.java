package com.intel.webrtc.test.demo;

import java.io.File;

import com.intel.webrtc.test.Config;
import com.intel.webrtc.test.TestRunner;
import com.intel.webrtc.test.TestSuite;

public class Main {
    public static void main(String args[]) {
        String path = "test.cfg";
        if (args.length > 0)
            path = args[0];
        File file = new File(path);
        Config config = new Config(file);
        TestRunner testRunner = new TestRunner(config);
        TestSuite testSuite = new TestSuite();
        DemoTestDevice1 device1 = new DemoTestDevice1();
        device1.setName("device1");
        DemoTestDevice2 device2 = new DemoTestDevice2();
        device2.setName("device2");
        testSuite.addTestDevice(device1);
        testSuite.addTestDevice(device2);
        System.out.println(testSuite.toString());
        testRunner.runTest(testSuite);
    }
}
