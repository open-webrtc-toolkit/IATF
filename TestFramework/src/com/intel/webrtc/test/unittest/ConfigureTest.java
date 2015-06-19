package com.intel.webrtc.test.unittest;

import java.io.File;

import com.intel.webrtc.test.Config;

public class ConfigureTest {
    public static void main(String args[]) {
        System.out.println("sss= ".split("=").length);
        File cfgFile = new File(
                "test.cfg");
        Config config = new Config(cfgFile);
        System.out.println(config.getAdbPath());
        System.out.println(config.getAndroidTestClass());
        System.out.println(config.getAndroidTestPackage());
        System.out.println(config.getAntPath());
        System.out.println(config.getApkName());
        System.out.println(config.getPort());
        System.out.println(config.getShellPath());

    }
}
