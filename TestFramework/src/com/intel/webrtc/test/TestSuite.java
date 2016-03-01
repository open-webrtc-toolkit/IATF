package com.intel.webrtc.test;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;

/**
 * TestSuite is a combination of several single TestDevice.
 *
 * @author xianglai
 *
 */
public class TestSuite {
	private final static String TAG = "TestSuite";

	// TODO private LinkedList<Pair<String, Integer>> deviceRequired;
	private LinkedList<TestDevice> testDevices;

	public LinkedList<TestDevice> getTestDevices() {
        return testDevices;
    }

    private Hashtable<String, TestCase> testCases;

	public TestSuite() {
		testCases = new Hashtable<String, TestCase>();
		testDevices = new LinkedList<TestDevice>();
	}

	public Hashtable<String, TestCase> getTestCases() {
		return testCases;
	}

	/**
	 * Add a TestDevice into the TestSuite. The TestSuite will scan the test
	 * methods of the new-coming TestDevice, and add the TestDevice into or
	 * create corresponding TestCases.
	 * 
	 * @param testDevice
	 */
	public void addTestDevice(TestDevice testDevice) {
		testDevice.addDeviceToSuite(this);
		//for javascript client, this can be used to run 'karma start'
		testDevices.add(testDevice);
	}

	/**
	 * Remove a TestDevice from the TestSuite.
	 * 
	 * @param testDeviceName
	 *            The name of the TestDevice.
	 */
	public void removeTestDevice(String testDeviceName) {
		// TODO
	}

	/**
	 * Remove a TestDevice from the TestSuite.
	 * 
	 * @param testDevice
	 *            The TestDevice going to remove.
	 */
	public void removeTestDevice(TestDevice testDevice) {
		// TODO
	}

	/**
	 * Remote a TestCase from the TestSuite.
	 * 
	 * @param testCaseName
	 *            The name of the TestCase.
	 */
	public void removeTestCase(String testCaseName) {
		// TODO
	}

	/**
	 * Convert a TestSuite to String, so that you can print it for debugging.
	 */
	public String toString() {
		// TODO need verification
		String resultString = "Testsuite:\n";
		Iterator<Entry<String, TestCase>> iterator = testCases.entrySet().iterator();
		for (int i = 0; iterator.hasNext(); i++) {
			resultString += ("TestCase " + i + ": " + iterator.next().getValue().toString() + "\n");
		}
		return resultString;
	}
}
