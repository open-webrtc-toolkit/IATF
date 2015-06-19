package com.intel.webrtc.test;

/**
 * This class defines several keywords used in the communication between
 * clients and the server.
 *
 * Message Format:<p>
 *   Test Start: <br>
 *   {"type":"teststart","class":"AndroidDevice1","method":"testXXX"}<p>
 *   Test Finish:<br>{"type":"testfinish"}<p>
 *   Wait:<br> {"type":"wait","what":"lock1"}<p>
 *   Notify:<br> {"type":"notify","what":"lock1"}<p>
 *   Notify All:<br> {"type":"notifyall","what":"lock1"}<p>
 * @author xianglai
 *
 */
public class MessageProtocol {

    final public static String TEST_START = "teststart";
    final public static String TEST_FINISH = "testfinish";
    final public static String NOTIFY_ALL = "notifyall";
    final public static String NOTIFY = "notify";
    final public static String WAIT = "wait";
    final public static String WHAT = "what";
    final public static String MESSAGE_TYPE = "type";
    final public static String CLASS_NAME = "class";
    final public static String METHOD_NAME = "method";
    final public static String HEARTBEAT = "heartbeat";
    final public static String TEST_ACTIVITY = "testactivity";
    final public static String TEST_PACKAGE = "testpackage";
    // TODO add result type attribute when test finished.
}
