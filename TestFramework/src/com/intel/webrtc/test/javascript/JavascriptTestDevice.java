package com.intel.webrtc.test.javascript;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.intel.webrtc.test.Logger;
import com.intel.webrtc.test.TestCase;
import com.intel.webrtc.test.TestDevice;
import com.intel.webrtc.test.TestSuite;

import junit.framework.Assert;

/**
 * Logic device of javascript. Init by the test case file and karma config file.
 * @author bean
 *
 */
public class JavascriptTestDevice extends Assert implements TestDevice {
    // Debug TAG
    public static String TAG = "JavascriptTestDevice";
    // Device name
    private String deviceName = "";
    // All the test methods scaned from the js test file, describe block which
    // starts with prefix 'test'
    private TreeSet<String> testMethods;
    // Karma config file
    public String jsConfFile;
    // Physical device info, mainly stores the config infos
    public JavascriptDeviceInfo deviceInfo;
    // The process runs 'karma start' to start test
    public Process karmaStartProcess = null;

    public JavascriptTestDevice(String jsTestFile, String jsConfFile) throws IOException {
        this.jsConfFile = jsConfFile;
        this.deviceInfo = new JavascriptDeviceInfo(jsConfFile);
        testMethods = new TreeSet<String>(new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                String name1=s1.substring(0,6);
                String name2=s2.substring(0,6);
                return name1.compareTo(name2);
            }
        });
        String deviceName;
        try {
            deviceName = scanJsTestCaseFile(jsTestFile);
            setName(deviceName);
        } catch (IOException e) {
            Logger.e(TAG, "Error when init JavascritpTestDevice from the source file.");
            e.printStackTrace();
        }
    }

    /**
     * Scan the test file, get the following infos:
     * 1. DeviceName, and call setName();
     * 2. Methods, add into testMethods.
     * there should be only one TestCase and several TestMethods.
     * @param jsfile the javascript test case source file
     * @return
     * @throws IOException
     */
    private String scanJsTestCaseFile(String jsfile) throws IOException {
        String deviceName = null;
        String suitePatStr = "\\s*describe\\(\\s*['\"]([^'\"]+)['\"]\\s*,\\s*function\\(.*";
        Pattern suitePat = Pattern.compile(suitePatStr);
        String casePatStr = "\\s*[^x]it\\(\\s*['\"]([^'\"]+)['\"]\\s*,\\s*function\\(.*";
        Pattern casePat = Pattern.compile(casePatStr);
        BufferedReader reader = new BufferedReader(new FileReader(jsfile));
        String line;
        Matcher m1, m2;
        while ((line = reader.readLine()) != null) {
            m1 = suitePat.matcher(line);
            if (m1.find()) {
                // this line contains the TestDevice name. 'describe' block.
                if (deviceName == null) {
                    // not assigned yet
                    deviceName = m1.group(1);
                } else {
                    // error, multiple 'describe' block in one test file.
                    Logger.e(TAG, "multiple 'describe' block in test file:" + jsfile);
                }
            }
            m2 = casePat.matcher(line);
            if (m2.find()) {
                // this line contains the testMethod
                testMethods.add(m2.group(1));
            }
        }
        reader.close();
        return deviceName;
    }

    /**
     * Get device name.
     */
    @Override
    public String getName() {
        return deviceName;
    }

    /**
     * Set device name.
     */
    @Override
    public void setName(String name) {
        this.deviceName = name;
    }

    /**
     * Scan the javascript test file, and add test case into testSuite.
     * @param TestSuite the test suite.
     */
    @Override
    public void addDeviceToSuite(TestSuite testSuite) {
        TreeMap<String, TestCase> testCases = testSuite.getTestCases();
        for (String method : testMethods) {
            if (testCases.containsKey(method)) {
                // the method have already been added
                testCases.get(method).addDevice(this);
            } else {
                TestCase newTestCase = new TestCase(method);
                newTestCase.addDevice(this);
                testCases.put(method, newTestCase);
            }
        }
    }
    @Override
    public void addDeviceToSuite(TestSuite testSuite,String caseName) {
        TreeMap<String, TestCase> testCases = testSuite.getTestCases();
        // If the TestCase already exists, add the device in.
        if (testCases.containsKey(caseName)) {
            testCases.get(caseName).addDevice(this);
        } else {
            TestCase newTestCase = new TestCase(caseName);
            newTestCase.addDevice(this);
            testCases.put(caseName, newTestCase);
        }
    }
    @Override
    public String toString() {
        String ret = "JSDevice:" + deviceName + "\nTest Methods:";
        for (String method : testMethods) {
            ret += method + "\t";
        }
        ret += "\n";
        return ret;
    }


}
