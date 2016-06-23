'''
Created on Jan,20 2016

@author: Yanbin
'''
import os
import time
import datetime
import sys
import getopt
from config.config import Config
from config.config import ConfigKeys as Keys
import subprocess
import commands

class Deploy(object):

    @staticmethod
    def deploy_js(testResultFile):
        BasePath=Config.getConfig(Keys.JS_CONFIG_FOLDER)
        #print BasePath
        JSTestFileConfig=[BasePath+"/"+testResultFile]
        KarmaPath=Config.getConfig(Keys.KARMA)
        print "startBrowserCommandIs: " + KarmaPath + ' start ' + JSTestFileConfig[0] ;
        startBrowser=subprocess.Popen(KarmaPath + ' start ' + JSTestFileConfig[0] + ">"+JSTestFileConfig[0]+".log", shell=True)
        time.sleep(10);
        lines = [line.rstrip('\n') for line in open(JSTestFileConfig[0]+".log")]
        for index in range(len(lines)):
            if ("Cannot start" in lines[index]) or ("crashed" in lines[index]):
                return 1
            else:
                return 0
    @staticmethod
    def deploy_android(androidDevice):
        AndroidPath=Config.getConfig(Keys.ANDROID_CONFIG_FOLDER)
        print "run command to install android apk "+ AndroidPath + '/runTest.sh --install -s ' + androidDevice;
        (status, output) = commands.getstatusoutput(AndroidPath + '/runTest.sh --install -s ' + androidDevice)
        print 'status is: ',status;        
        print "output is :",output;
        if status == 0 :
           return 0
        else:
           print "Install APK failed"
           return 1 

    @staticmethod
    def start_js(testResultFile, caseName):
        BasePath=Config.getConfig(Keys.JS_CONFIG_FOLDER)
        #print BasePath
        JSTestFileConfig=[BasePath+"/"+testResultFile]
        KarmaPath=Config.getConfig(Keys.KARMA)
        print "startJSTestIs: "+KarmaPath + ' run ' + JSTestFileConfig[0] + " -- --grep " + caseName ;
        runJSCase=subprocess.Popen(KarmaPath + ' run ' + JSTestFileConfig[0] + " -- --grep " + caseName,  shell=True)
        return runJSCase.pid
        #print "stdout is", runJSCase.stdout.read()
    @staticmethod
    def start_android_withResult(androidDevice, casename, classname):
        AndroidPath=Config.getConfig(Keys.ANDROID_CONFIG_FOLDER)
        #print AndroidPath
        print "run command to run test case "+ AndroidPath + '/runTest.sh --runcase -s ' + androidDevice + ' -n ' + casename + ' -c ' + classname;
        (status, output) = commands.getstatusoutput( AndroidPath + '/runTest.sh --runcase -s ' + androidDevice + ' -n ' + casename + ' -c ' + classname)
        print 'status is: ',status;        
        print "output is :",output;
        if status == 0 :
           return 0
        else:
           print "Run test case failed"
           return 1 
    @staticmethod
    def start_android_sync(androidDevice, casename, classname):
        AndroidPath=Config.getConfig(Keys.ANDROID_CONFIG_FOLDER)
        #print AndroidPath
        print "run command to run test case "+ AndroidPath + '/runTest.sh --runcase -s ' + androidDevice + ' -n ' + casename + ' -c ' + classname;
        runAndriodCase=subprocess.Popen(AndroidPath + '/runTest.sh --runcase -s ' + androidDevice + ' -n ' + casename + ' -c ' + classname, shell=True)
        return runAndriodCase.pid
#test
#if __name__ == '__main__':
#    deploy = Deploy()
#    deploy.deploy_android("emulator-5554")
   # Deploy.start_Android('emulator-5554','test_demo','PeerClientFrameworkTest');

#test
#result=start_js("testclient1.conf.temp.js","test_demo")
#print result
