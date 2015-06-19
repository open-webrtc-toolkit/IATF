package com.intel.webrtc.test;

import java.io.File;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Logger {

    // TODO add more log levels
    // TODO These two flags are used to determine whether this module
    // print the output into a log file, and they should be set through
    // the configuration.
    private static boolean printToStandardIO = true;
    private static boolean printToLogFile = false;
    private File logFile = null;
    private static DateFormat simpleDateFormat = new SimpleDateFormat(
            "MM-dd HH:mm:ss");

    /**
     * Print a log in the Error level.
     *
     */
    public static void e(String tag, String message) {
        System.err.println(generateFinalMessage(tag, message));
    }

    /**
     * Print a log in the Debug level.
     *
     */
    public static void d(String tag, String msg) {
        System.out.println(generateFinalMessage(tag, msg));
    }

    /**
     * Format the log message like this
     * Time     Tag     message
     *
     */
    private static String generateFinalMessage(String tag, String msg) {
        String finalMessage = getTimeStamp() + "\t" + tag + "\t";
        if (tag.length() < 16)
            finalMessage += "\t";
        finalMessage += msg;
        return finalMessage;
    }

    /**
     * get current system time
     * @return
     */
    private static String getTimeStamp() {
        Date date = new Date();
        String timeStamp = simpleDateFormat.format(date);
        timeStamp += "." + date.getTime() % 1000;
        return timeStamp;
    }
}
