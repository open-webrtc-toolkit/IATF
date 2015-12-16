package com.intel.webrtc.test.android;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.intel.webrtc.test.ClientTestController;

import android.app.Activity;
import android.os.Bundle;
import android.test.InstrumentationTestCase;
import android.util.Log;

/**
 * The wrapper class for user-write test cases.<p>
 * AndroidTestEntry will be started by InstrumentationTestRunner as a JUnit
 * test case, and it will initialize the environment for test client, set up the
 * network, and instantiate the specific user-defined AndroidTestDevice according
 * to server's command, and finally run the test.<p>
 * It also provides some APIs related to Instrumentation.
 *
 * @author xianglai
 *
 */
public class AndroidTestEntry extends InstrumentationTestCase implements
        AndroidTestDevice.InstrumentationWrapper {
    // TODO change all log.e() to fail();

    private ClientTestController testController = null;
    private AndroidTestDevice testDevice;

    private Method testMethod;
    private final static String TAG = "AndroidTestEntry";
    private String className, methodName, testActivityName, testPackage;
    private Activity testActivity;

    /**
     * called by InstrumentTestRunner.
     */
    public AndroidTestEntry() {
        // TODO How to define a default constructor for JUnit?
        super();
        className = null;
        methodName = null;
        testPackage = null;
        testActivityName = null;
        testDevice = null;
        testMethod = null;

    }

    /**
     * Listen to test controller, and get test identity.
     * Invoke Devices's setUp();
     */
    public void setUp() {
        try {
            super.setUp();
        } catch (Exception e1) {
            e1.printStackTrace();
            fail("Error occured in super.setUp().");
        }
        Log.d(TAG, "Super.setUp() finished.");
        testController = new AndroidClientController(this);
        synchronized (this) {
            // Waiting for the start message;
            try {
                wait();
            } catch (InterruptedException e) {
                Log.w(TAG, "Interrupted while waiting for server.");
                e.printStackTrace();
            }
        }
        initTestClassAndMethod();
        testDevice.setUp();
    }

    /**
     * Instantiate the specific AndroidTestDevice, and get current test method.
     */
    private void initTestClassAndMethod() {
        Class<?> testClass = null;

        try {
            testClass = Class.forName(className);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Class " + className + " is not found!");
            e.printStackTrace();
            return;
        }
        Object object = null;
        try {
            object = testClass.newInstance();
        } catch (InstantiationException e) {
            Log.e(TAG,
                    "Class "
                            + className
                            + " doesn't have a defalut constructor, or it is not visable!");
            e.printStackTrace();
            return;
        } catch (IllegalAccessException e) {
            Log.e(TAG,
                    "Class "
                            + className
                            + " doesn't have a defalut constructor, or it is not visable!");
            e.printStackTrace();
            return;
        }
        if (object instanceof AndroidTestDevice) {
            testDevice = (AndroidTestDevice) object;
        }
        assertNotNull("Error occured when initializing test.", testDevice);
        testDevice.setTestEntry(this);
        testDevice.setController(testController);
        try {
            testMethod = testClass.getMethod(methodName);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            fail("Class " + className + " doesn't have a method named "
                    + methodName + ".");
        }
    }

    /**
     * Called by AndroidTestController to set the test identity.
     * @param className
     *      the name of the user-defined test case class.
     * @param methodName
     *      the name of the test method going to run.
     */
    //TODO put the following two methods into a new interface.
    void setTestClassAndMethod(String className, String methodName) {
        this.className = className;
        this.methodName = methodName;
        synchronized (this) {
            notify();
        }
    }

    /**
     * Called by AndroidTestController to set the test package and the test
     * activity.
     * @param testPackage
     *      the name of the test target package, which is set in AndroidManifest.
     * @param testActivity
     *      the qualified name of the activity.
     */
    void setTestPackageAndActivity(String testPackage,
            String testActivity) {
        this.testPackage = testPackage;
        this.testActivityName = testActivity;
    }

    /**
     * get the test activity set in configure.<p>
     *
     * if the activity has not been launch, it will be launch with an empty
     * Bundle.
     *
     */
    @SuppressWarnings("unchecked")
    public Activity getActivity() {
        if (testActivity == null) {
            Class<Activity> activityClass=null;
            try {
                // TODO find a better way to solve this conversion.
                // Do not suppress the warning.
                activityClass = (Class<Activity>) Class
                        .forName(testActivityName);
                assertNotNull(activityClass);
                testActivity=super.launchActivity(testPackage, activityClass, new Bundle());
                Log.d(TAG, "testActivity:"+testActivity);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                Log.e(TAG, activityClass.toString());
                Log.e(TAG, testActivityName);
                e.printStackTrace();
            }
        }
        return testActivity;
    }

    public void tearDown() {
        testDevice.tearDown();
        testController.testFinished();
        testController.close();
    }

    /**
     * The test method, will be called by Instrumentation test runner.<p>
     *
     * It runs the prepared test method. If there is any uncaught Exceptions in
     * user's test method, this method will throw it out as a RuntimeException.
     */
    public void testEntry() {
        try {
            testMethod.invoke(testDevice);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            fail("Test method is not asscessible.");
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            fail("Test method should not have any argument.");
        } catch (InvocationTargetException e) {
            Throwable throwable = e.getTargetException();
            e.getTargetException().printStackTrace();
            if (throwable instanceof AssertionError)
                throw (AssertionError) throwable;
            else {
                RuntimeException re = new RuntimeException(throwable.getClass()
                        .getName() + " msg:" + throwable.getMessage());
                re.setStackTrace(throwable.getStackTrace());
                throw re;
            }
        }
    }
}
