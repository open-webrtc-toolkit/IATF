package com.intel.webrtc.test.unittest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;

import android.R.integer;

import com.intel.webrtc.test.android.AndroidDeviceInfo.AndroidDeviceType;

public class Test {

    static String androidHome = "/home/xianglai/develop/ADT/sdk/";
    static String adbHome = androidHome + "platform-tools/";
    static String antPath = "/home/xianglai/tools/apache-ant-1.9.4/bin/ant";
    static String adb = "adb";
    Process process = null;
    BufferedReader sio0, seo = null;
    String resultLine = null;

    final static public void main(String args[]) {
        byte a, b;
        a='z';
        b = 'a';
        long c, d;
        c= a;
        d = b;
        System.out.println(c);
        System.out.println(d);
        // TestRunner tr = new TestRunner();
        // String user0 = "user0", user1 = "user1", user2 = "user2";
        // String lock0 = "lock0", lock1 = "lock1", lock2 = "lock2";
        // WaitNotifyManager wnManager = new WaitNotifyManager();
        // wnManager.waitForObject(user0, lock0);
        // wnManager.waitForObject(user0, lock0);
        // System.out.println(wnManager.notifyObject(lock0));
        // wnManager.waitForObject(user1, lock0);
        // wnManager.waitForObject(user2, lock0);
        // String[] s = wnManager.notifyObjectForAll(lock0);
        // for (int i = 0; i < s.length; i++)
        // System.out.print(s[i] + " ");
        // System.out.print('\n');
        // wnManager.waitForObject(user0, lock1);
        // wnManager.waitForObject(user2, lock1);
        // System.out.println(wnManager.notifyObject(lock1));
        // wnManager.waitForObject(user0, lock1);
        // s = wnManager.notifyObjectForAll(lock1);
        // for (int i = 0; i < s.length; i++)
        // System.out.print(s[i] + " ");
        // System.out.print('\n');
        // wnManager.waitForObject(user0, lock2);
        // System.out.println(wnManager.notifyObject(lock2));
        // System.out.println(wnManager.notifyObject(lock2));
        // System.out.println("End");
    }

    void m() {
        // }
        //
        // void main2(String args[]) {

        // Hashtable<String, String> addressTable = new Hashtable<String,
        // String>();
        // addressTable.put("d1", "127.0.0.1:10777");
        // addressTable.put("d1", "127.0.0.1:10999");
        // TestController tc = new TestController(addressTable);
        //

        try {
            process = Runtime.getRuntime().exec(
                    new String[] {
                            "/bin/sh",
                            "-c",
                            adbHome + adb + " -s ZTEU930HD "
                                    + " install -r TestActivity-debug.apk" });
            sio0 = new BufferedReader(new InputStreamReader(
                    process.getInputStream()));
            // process.waitFor();
            while (true) {
                resultLine = sio0.readLine();
                if (resultLine == null)
                    break;
                System.out.println(resultLine);
            }
        } catch (IOException e) {
            System.out.println("Error occured when get device list via ADB ");
            e.printStackTrace();
        }

        try {

            process = Runtime
                    .getRuntime()
                    .exec(new String[] {
                            "/bin/sh",
                            "-c",
                            adbHome
                                    + adb
                                    + " -s ZTEU930HD "
                                    + " shell am instrument -r -e class com.intel.webrtc.test.AutoTest#testLcsp_getCameraList -w com.intel.webrtc.test/android.test.InstrumentationTestRunner" });
            sio0 = new BufferedReader(new InputStreamReader(
                    process.getInputStream()));
            seo = new BufferedReader(new InputStreamReader(
                    process.getErrorStream()));
            // process.waitFor();
            while (true) {
                resultLine = sio0.readLine();
                if (resultLine == null)
                    break;
                System.out.println(resultLine);
            }
            while (true) {
                resultLine = seo.readLine();
                if (resultLine == null)
                    break;
                System.out.println(resultLine);
            }
        } catch (IOException e) {
            System.out.println("Error occured when get device list via ADB ");
            e.printStackTrace();
        }

        // try {

        // Properties properties = System.getProperties();
        // Iterator<Entry<Object, Object>> it =
        // properties.entrySet().iterator();
        // while (it.hasNext()) {
        // Entry<Object, Object> ele = it.next();
        // System.out.println(ele.getKey() + "   =====   " + ele.getValue());
        // }

        // System.out.println(System.getProperties().toString());
        // Map<String, String> env = System.getenv();
        // Iterator<Entry<String, String>> iterator = env.entrySet().iterator();
        // while (iterator.hasNext()) {
        // Entry<String, String> ele = iterator.next();
        // System.out.println(ele.getKey() + " " + ele.getValue());
        // }
    }
}
