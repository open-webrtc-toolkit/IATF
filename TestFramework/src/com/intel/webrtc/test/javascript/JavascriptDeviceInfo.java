package com.intel.webrtc.test.javascript;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.intel.webrtc.test.PhysicalDeviceInfo;
import com.intel.webrtc.test.LockServer;
import com.intel.webrtc.test.Logger;

/**
 * Physical device of javascript test client.
 * @author bean
 *
 */
public class JavascriptDeviceInfo extends PhysicalDeviceInfo {
    // Debug TAG
    public static String TAG = "JavascriptDeviceInfo";
    // JS test file prefix
    // TODO: This should be read from config
    public static String testClientFilePrefix = "test-peerwn";
    // broswer type in karma config file
    public String browser;
    // state of the test instance
    private JavascriptDeviceType deviceState = JavascriptDeviceType.DEFAULT;
    // karma config file name
    public String karmaConfigFile;
    // client test controller
    private JavascriptClientController jcc;

    public JavascriptDeviceType getDeviceStatus() {
        return deviceState;
    }

    public void setDeviceStatus(JavascriptDeviceType deviceState) {
        this.deviceState = deviceState;
    }

    // the test port of karma
    public String karmaport;
    // test source file
    public String testClient;

    // Enum of JS device status
    public enum JavascriptDeviceType {
        DISCONNECT, IDLE, EXCUTING, DEFAULT
    };

    /**
     * Scan the karma config file and init the device infos.
     * @param karmaConfigFile
     * @throws IOException
     */
    public JavascriptDeviceInfo(String karmaConfigFile) throws IOException {
        super("JS", "" + LockServer.port);
        this.karmaConfigFile = karmaConfigFile;
        String deviceId = karmaConfigFile.substring(karmaConfigFile.lastIndexOf("/") + 1);
        this.id = deviceId;
        String browser, karmaport, testClient;
        String browserPatStr = "browsers:\\[[\"']([^\"']+)[\"']\\],";
        String kmportPatStr = "port:([\\d]+),";
        String tcPatStr = "(" + testClientFilePrefix + "[^'\"]+)[\"']";
        Pattern brpat = Pattern.compile(browserPatStr);
        Pattern kmpat = Pattern.compile(kmportPatStr);
        Pattern tcpat = Pattern.compile(tcPatStr);
        BufferedReader karmaConfReader = new BufferedReader(new FileReader(karmaConfigFile));
        String line;
        boolean starComment = false;
        StringBuilder sb = new StringBuilder();
        while ((line = karmaConfReader.readLine()) != null) {
            // skip empty line and comments
            String trimline = line.trim().replace(" ", "");
            if (trimline.equals("") || trimline.startsWith("//")) {
                continue;
            }
            if (trimline.startsWith("/*")) {
                starComment = true;
            }
            if (trimline.endsWith("*/")) {
                starComment = false;
                continue;
            }
            if (starComment) {
                continue;
            }
            sb.append(trimline);
        }
        String conf = sb.toString();
        Matcher brmat = brpat.matcher(conf);
        if (brmat.find()) {
            browser = brmat.group(1);
        } else {
            Logger.d(TAG, "No browser option find in file:[" + karmaConfigFile + "], javascript device init failed.");
            return;
        }
        if (brmat.find()) {
            Logger.d(TAG, "Multiple browser options are founded in file:[" + karmaConfigFile
                    + "], javascript device init failed.");
            return;
        }
        Matcher kmmat = kmpat.matcher(conf);
        if (kmmat.find()) {
            karmaport = kmmat.group(1);
        } else {
            Logger.d(TAG, "No port option find in file:[" + karmaConfigFile + "], javascript device init failed.");
            return;
        }
        if (kmmat.find()) {
            Logger.d(TAG, "Multiple port options are founded in file:[" + karmaConfigFile
                    + "], javascript device init failed.");
            return;
        }
        Matcher tcmat = tcpat.matcher(conf);
        if (tcmat.find()) {
            testClient = tcmat.group(1);
        } else {
            Logger.d(TAG, "No test client source file is founded in file:[" + karmaConfigFile
                    + "], javascript device init failed.");
            return;
        }
        if (tcmat.find()) {
            Logger.d(TAG, "Multiple test client source file are founded in file:[" + karmaConfigFile
                    + "], javascript device init failed.");
            return;
        }
        karmaConfReader.close();
        this.karmaport = karmaport;
        this.testClient = testClient;
        this.browser = browser;
        this.jcc = new JavascriptClientController();
        // FIX BUG:socket counter
        this.localPort = "" + (jcc.localport);
    }

    @Override
    public String toString() {
        String ret = "JS test device, on broswer[" + browser + "], state[" + deviceState + "], " + "Logic Instance["
                + id + "], localPort[" + localPort + "], testClient[" + testClient + "].";
        return ret;
    }
}
