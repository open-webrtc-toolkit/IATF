package com.intel.webrtc.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Used to parse and provided parameters for the test framework.
 *
 * @author xianglai
 *
 */
public class Config {

    final static private String TAG = "Configure";

    private int port = 10086;
    private String adbPath = "/home/bean/Lib/android-sdk-linux/platform-tools/adb";
    private String antPath = "/home/bean/Lib/apache-ant-1.9.5/bin/ant";
    private String shellPath = "/bin/sh";
    private String apkName = "test.apk";
    private String androidTestPackage = "com.intel.webrtc.test";
    private String androidTestClass = "com.intel.webrtc.test.AutoTest";
    private String androidTestActivity = "com.intel.webrtc.test.TestActivity";
    private String karmaPath = "/usr/lib/node_modules/karma/bin/karma";
    private boolean buildApk = true;
    private int heartbeatInterval = 5000;
    private int heartbeatTimeout = 30000;

    /**
     * Create an instance of Configure by reading parameters from a file.
     * Format:  Name=Value
     * Example: port=10086
     * @param file
     * The configure file.
     */
    public Config(File configFile) {
        if (!(configFile != null && configFile.exists() && configFile.isFile() && configFile.canRead()))
            return;
        BufferedReader bufferedReader;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(configFile)));
        } catch (FileNotFoundException e1) {
            Logger.e(TAG, "File not found!");
            e1.printStackTrace();
            return;

        }
        String line;
        while (true) {
            try {
                line = bufferedReader.readLine();
                if (line == null || "".equals(line)) {
                    break;
                }
                String[] keyValuePair = line.split("=");
                if (keyValuePair.length != 2)
                    continue;
                saveParameter(keyValuePair[0], keyValuePair[1]);
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    /**
     * Save the parameters read from the configure file into current Config.
     * @param key
     * @param value
     */
    private void saveParameter(String key, String value) {
        if ("".equals(key)) {
            return;
        }
        if (key.equalsIgnoreCase("port")) {
            try {
                int port = Integer.parseInt(value);
                if (port > 1024 && port < 65535) {
                    this.port = port;
                    return;
                }
                Logger.e(TAG, "Port: " + value + " is an illegal port number.");
            } catch (NumberFormatException e) {
                Logger.e(TAG, "Port: " + value + " is an illegal port number.");
            }
            return;
        }
        if (key.equalsIgnoreCase("adbPath")) {
            adbPath = value;
            return;
        }
        if (key.equalsIgnoreCase("antPath")) {
            antPath = value;
            return;
        }
        if (key.equalsIgnoreCase("shellPath")) {
            shellPath = value;
            return;
        }
        if (key.equalsIgnoreCase("karmaPath")) {
        	karmaPath = value;
        	return;
        }
        if (key.equalsIgnoreCase("apkName")) {
            apkName = value;
            return;
        }
        if (key.equalsIgnoreCase("androidTestPackage")) {
            androidTestPackage = value;
            return;
        }
        if (key.equalsIgnoreCase("androidTestClass")) {
            androidTestClass = value;
            return;
        }
        if (key.equalsIgnoreCase("androidTestActivity")) {
            androidTestActivity = value;
            return;
        }
        if (key.equalsIgnoreCase("buildApk")) {
            if (value.equalsIgnoreCase("false") | value.equalsIgnoreCase("f"))
                buildApk = false;
            return;
        }
        if (key.equalsIgnoreCase("heartbeatInterval")) {
            try {
                heartbeatInterval = Math.abs(Integer.parseInt(value));
            } catch (NumberFormatException e) {
                Logger.e(TAG, "HeartbeatInterval: " + value + " is not a number.");
            }
            return;
        }
        if (key.equalsIgnoreCase("heartbeatTimeout")) {
            try {
                heartbeatTimeout = Math.abs(Integer.parseInt(value));
            } catch (NumberFormatException e) {
                Logger.e(TAG, "heartbeatTimeout: " + value + " is not a number.");
            }
            return;
        }
    }

    public int getPort() {
        return port;
    }

    public String getAdbPath() {
        return adbPath;
    }

    public String getAntPath() {
        return antPath;
    }

    public String getShellPath() {
        return shellPath;
    }
    public String getKarmaPath() {
    	return karmaPath;
    }
    public String getApkName() {
        return apkName;
    }

    public String getAndroidTestPackage() {
        return androidTestPackage;
    }

    public String getAndroidTestClass() {
        return androidTestClass;
    }

    public String getAndroidTestActivity() {
        return androidTestActivity;
    }

    public boolean whetherBuildApk() {
        return buildApk;
    }

    public int getHeartbeatTimeout() {
        return heartbeatTimeout;
    }

    public int getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public void setPort(int port) {
        if (port > 1024 && port < 65535) {
            this.port = port;
        }
    }

    public void setAdbPath(String adbPath) {
        if (adbPath != null)
            this.adbPath = adbPath;
    }

    public void setAntPath(String antPath) {
        if (antPath != null)
            this.antPath = antPath;
    }

    public void setShellPath(String shellPath) {
        if (shellPath != null)
            this.shellPath = shellPath;
    }

    public void setApkName(String apkName) {
        if (apkName != null)
            this.apkName = apkName;
    }

    public void setAndroidTestPackage(String androidTestPackage) {
        if (androidTestPackage != null)
            this.androidTestPackage = androidTestPackage;
    }

    public void setAndroidTestClass(String androidTestClass) {
        if (androidTestClass != null)
            this.androidTestClass = androidTestClass;
    }

    public void setAndroidTestActivity(String androidTestActivity) {
        if (androidTestActivity != null)
            this.androidTestActivity = androidTestActivity;
    }

    public void setBuildApk(boolean build) {
        buildApk = build;
    }

    public void setHeartbeatInterval(int millisecond) {
        heartbeatInterval = Math.abs(millisecond);
    }

    public void setHeartbeatTimeout(int millisecond) {
        heartbeatTimeout = Math.abs(millisecond);
    }

}
