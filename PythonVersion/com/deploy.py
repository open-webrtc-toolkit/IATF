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
from pexpect import pxssh

class Deploy(object):
    @staticmethod
    def deploy_js(testResultFile,mode):
        if mode == 'P2P':
          BasePath=Config.getConfig(Keys.JS_P2P_CONFIG_FOLDER)
        else:
          BasePath=Config.getConfig(Keys.JS_CONFERENCE_CONFIG_FOLDER)
        #print BasePath
        JSTestFileConfig=[BasePath+"/"+testResultFile]
        KarmaPath=Config.getConfig(Keys.KARMA)
        print "startBrowserCommandIs: " + KarmaPath + ' start ' + JSTestFileConfig[0] ;
        startBrowser=subprocess.Popen("cd "+BasePath+"; " + KarmaPath + ' start ' + JSTestFileConfig[0] + ">"+JSTestFileConfig[0]+".log", shell=True)
        time.sleep(10);
        lines = [line.rstrip('\n') for line in open(JSTestFileConfig[0]+".log")]
        for index in range(len(lines)):
            if ("Cannot start" in lines[index]) or ("crashed" in lines[index]):
                return 1
            else:
                return 0

    @staticmethod
    def deploy_android(androidDevice,mode):
        if mode == 'P2P':
          AndroidPath=Config.getConfig(Keys.ANDROID_P2P_CONFIG_FOLDER)
        else:
          AndroidPath=Config.getConfig(Keys.ANDROID_CONFERENCE_CONFIG_FOLDER)
        print "run command to install android apk "+ AndroidPath + '/runTest.sh --buildlib --install -s ' + androidDevice;
        (status, output) = commands.getstatusoutput(AndroidPath + '/runTest.sh --buildlib --install -s ' + androidDevice)
        print 'status is: ',status;
        print "output is :",output;
        if status == 0 :
           return 0
        else:
           print "Install APK failed"
           return 1

    @staticmethod
    def deploy_runtest(casetest,mode,install,log):
        webrtc_webrtc_qa=Config.getConfig(Keys.WEBRTC_WEBRTC_QA)
        runtest=subprocess.Popen('python '+ webrtc_webrtc_qa +'/runTest.py -c ' + casetest + " -m " + mode + " -i "+ install +" >> "+log+".txt ", shell=True)
        #runtest.wait()
        return runtest
    @staticmethod
    def start_js(testResultFile, caseName, mode):
        if mode == 'P2P':
          BasePath=Config.getConfig(Keys.JS_P2P_CONFIG_FOLDER)
        else:
          BasePath=Config.getConfig(Keys.JS_CONFERENCE_CONFIG_FOLDER)
        #print BasePath
        JSTestFileConfig=[BasePath+"/"+testResultFile]
        KarmaPath=Config.getConfig(Keys.KARMA)
        print "startJSTestIs: "+KarmaPath + ' run ' + JSTestFileConfig[0] + " -- --grep " + caseName ;
        runJSCase=subprocess.Popen("cd "+BasePath+"; " + KarmaPath + ' run ' + JSTestFileConfig[0] + " -- --grep " + caseName,  shell=True)
        return runJSCase.pid

    @staticmethod
    def start_android_withResult(androidDevice, casename, classname,mode):
        if mode == 'P2P':
          AndroidPath=Config.getConfig(Keys.ANDROID_P2P_CONFIG_FOLDER)
        else:
           AndroidPath=Config.getConfig(Keys.ANDROID_CONFERENCE_CONFIG_FOLDER)
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
    def start_android_sync(androidDevice, casename, classname, mode):
        if mode == 'P2P':
          AndroidPath=Config.getConfig(Keys.ANDROID_P2P_CONFIG_FOLDER)
        else:
           AndroidPath=Config.getConfig(Keys.ANDROID_CONFERENCE_CONFIG_FOLDER)
        #print AndroidPath
        print "run command to run test case "+ AndroidPath + '/runTest.sh --runcase -s ' + androidDevice + ' -n ' + casename + ' -c ' + classname;
        runAndriodCase=subprocess.Popen(AndroidPath + '/runTest.sh --runcase -s ' + androidDevice + ' -n ' + casename + ' -c ' + classname, shell=True)
        return runAndriodCase.pid
    @staticmethod
    def deploy_iOS(YourWorkspace,YourScheme,YourSimulator,YourPhone,mode):
        if mode == 'P2P':
          IOSPath=Config.getConfig(Keys.IOS_P2P_CONFIG_FOLDER)
        else:
          IOSPath=Config.getConfig(Keys.IOS_CONFERENCE_CONFIG_FOLDER)
        print 'xcodebuild  -project ' + YourWorkspace + ' -scheme ' + YourScheme + ' -sdk '+ YourSimulator +' clean build-for-testing -destination \'platform=iOS Simulator,name='+ YourPhone + '\'' 
        iOSDeploy=subprocess.Popen('cd '+IOSPath+'; ' + 'xcodebuild  -project ' + YourWorkspace + ' -scheme ' + YourScheme + ' -sdk '+ YourSimulator +' clean build-for-testing -destination \'platform=iOS Simulator,name='+ YourPhone + '\''+ ">deployiOS.log", shell=True)
        #time.sleep(10);
        iOSDeploy.wait();
        lines = [line.rstrip('\n') for line in open(IOSPath+"/deployiOS.log")]
        for index in range(len(lines)):
            print lines[index]
            if ("FAILED" in lines[index]) or ("1 errored" in lines[index]):
                return 1
            else:
                return 0
    @staticmethod
    def start_iOS_sync(YourWorkspace,YourScheme,TestTarget,YourSimulator,YourPhone,casename,classname,mode):
        if mode == 'P2P':
          IOSPath=Config.getConfig(Keys.IOS_P2P_CONFIG_FOLDER)
        else:
          IOSPath=Config.getConfig(Keys.IOS_CONFERENCE_CONFIG_FOLDER)
        #print AndroidPath
        print "run command to run test case "+ 'xctool -project ' + YourWorkspace + ' -scheme ' + YourScheme + ' run-tests -only '+TestTarget+':'+classname+'/'+casename +' -sdk '+ YourSimulator +' -destination \'platform=iOS Simulator,name='+ YourPhone + '\'';
        runiOSCase=subprocess.Popen('cd '+IOSPath+'; ' + 'xctool -project ' + YourWorkspace + ' -scheme ' + YourScheme + ' run-tests -only '+TestTarget+':'+classname+'/'+casename +' -sdk '+ YourSimulator +' -destination \'platform=iOS Simulator,name='+ YourPhone + '\'' + '>result/'+classname+'-'+casename+'.log', shell=True)
        return runiOSCase.pid
    @staticmethod
    def deploy_iOS_remote(YourWorkspace,YourScheme):
        iOSAddress=Config.getConfig(Keys.MAC_ADD)
        iOSUser=Config.getConfig(Keys.MAC_USER)
        iOSPassd=Config.getConfig(Keys.MAC_PASSD)
        projectFolder=Config.getConfig(Keys.MAC_FOLDER)
        print iOSAddress
        s = pxssh.pxssh()
        if not s.login (iOSAddress, iOSUser, iOSPassd):
           print "SSH session failed on login."
           print str(s)
           return 1
        else:
           print "SSH session login successful"
           s.sendline('cd ' + projectFolder)
           #s.sendline ('cd /Users/neilyou/Documents/webrtc-ios-sdk/test/p2psample')
           s.prompt()
           s.sendline('security unlock-keychain /Users/neilyou/Library/Keychains/login.keychain')
           s.prompt()
           s.sendline('intel123')
           s.prompt()
           s.sendline('xctool -project ' + YourWorkspace + ' -scheme ' + YourScheme + ' build-tests -sdk iphonesimulator9.2 -destination \'platform=iOS Simulator,name=iPhone 6s Plus\'')
           s.logfile = sys.stdout
           print s.logfile
           return s
    @staticmethod
    def start_iOS_remote(YourWorkspace,YourScheme,TestTarget,casename,classname):
        iOSAddress=Config.getConfig(Keys.MAC_ADD)
        iOSUser=Config.getConfig(Keys.MAC_USER)
        iOSPassd=Config.getConfig(Keys.MAC_PASSD)
        projectFolder=Config.getConfig(Keys.MAC_FOLDER)
        print iOSAddress
        s = pxssh.pxssh()
        if not s.login (iOSAddress, iOSUser, iOSPassd):
           print "SSH session failed on login."
           print str(s)
           return 1
        else:
           print "SSH session login successful"
           print "cd " + projectFolder;
           s.sendline('cd ' + projectFolder)
           #s.sendline ('cd /Users/neilyou/Documents/webrtc-ios-sdk/test/p2psample')
           s.prompt()
           print s.before
           s.sendline('security unlock-keychain -p intel123 ~/Library/Keychains/login.keychain')
           s.prompt()
           print s.before
           print 'xctool -project ' + YourWorkspace + ' -scheme ' + YourScheme + ' run-tests -only '+TestTarget+':'+classname+'/'+casename +' -sdk iphonesimulator9.2 -destination \'platform=iOS Simulator,name=iPhone 6s Plus\''
           s.sendline('xctool -project ' + YourWorkspace + ' -scheme ' + YourScheme + ' run-tests -only '+TestTarget+':'+classname+'/'+casename +' -sdk iphonesimulator9.2 -destination \'platform=iOS Simulator,name=iPhone 6s Plus\'')
           s.logfile = sys.stdout
           print s.logfile
           #s.sendline('xctool -project WoogeenChatTest.xcodeproj -scheme WoogeenChatTest build-tests')
           # xctool -project WoogeenChatTest.xcodeproj -scheme WoogeenChatTest run-tests -only WoogeenChatTestTests:TestDevice2/test04_Peer1InviteAndPeer2Accept -sdk iphonesimulator9.3 -destination 'platform=iOS Simulator,name=iPhone 5s'
           #s.prompt()
           #print s.before
           return s
#test
#if __name__ == '__main__':
   #deploy = Deploy()
   #result=Deploy.deploy_iOS('/Users/neilyou/Documents/webrtc-ios-sdk/test/p2p_checkin','WoogeenChatTest.xcodeproj','WoogeenChatTest')
   #result.close
   #time.sleep(100);
   #result=Deploy.start_iOS('/Users/neilyou/Documents/webrtc-ios-sdk/test/p2p_checkin','WoogeenChatTest.xcodeproj','WoogeenChatTest','','')
#    result.prompt()
#    result.sendline('cd /Users/neilyou/Documents/webrtc-ios-sdk/test/p2psample')
#    result.prompt()
#    print result.before
#    print result.pid
#    result.sendline('xctool -project WoogeenChatTest.xcodeproj -scheme WoogeenChatTest test')
#    time.sleep(10);
#    result.prompt()
#    result.expect (pexpect.EOF)
#    print result.before
   # Deploy.start_Android('emulator-5554','test_demo','PeerClientFrameworkTest');

#test
#result=start_js("testclient1.conf.temp.js","test_demo")
#print result
