##########
#getDevices function is use to read current connected android devices, only return authenticated devices.
##########################
'''
Created on Jan, 21 2016

@author: Yanbin
'''

import os
import time
import datetime
import sys
import getopt
import subprocess
from config.config import Config
from config.config import ConfigKeys as Keys
devicesArray=[]
devicesIPDic={}

class getAndroidDevice(object):
    @staticmethod
    def get_devices():
        output = subprocess.check_output('adb devices', shell=True,)
        print '\tstdout:', output
        if len(output) == 0:
           return 1
        else:
            devicesArray=[];
            results=output.split("\n");
            for index in range(len(results)):
                #print 'Current results :', results[index]
                if "device" in results[index]:
                	if not "devices" in results[index]:
                	    deviceName=results[index].split('\t')
                	    devicesArray.append(deviceName[0]);
        return devicesArray
    @staticmethod
    def getDevicesIp():
    	androidDevices=get_devices()
    	for index in range(len(androidDevices)):
    		command="adb -s " + androidDevices[index] + " shell netcfg"
    		commandOutput = subprocess.check_output(command, shell=True,)
    		results=commandOutput.split('\n')
    		for index2 in range(len(results)):
    			if ("eth0" in results[index2]) or ("wlan0" in results[index2]):
    				if "UP" in results[index2]:
    					detail = results[index2].split();
    					print "detail[2]", detail[2];
    					devicesIPDic[androidDevices[index]]=detail[2];
    	return devicesIPDic;
    @staticmethod
    def read_caselist(devices, casename, mode):
        arrays = []
        n = 20
        result = 1;
        isTestEnd = False
        if mode == 'P2P':
          AndroidPath=Config.getConfig(Keys.ANDROID_P2P_CONFIG_FOLDER)
        else:
         AndroidPath=Config.getConfig(Keys.ANDROID_CONFERENCE_CONFIG_FOLDER)
        #filename_base = "p2p-android-test-result--com.intel.webrtc.test."+devices+'-'+casename+'.txt';
        filename_base = devices+'-'+casename+'.txt';
        filename = AndroidPath+'/log'+'/'+filename_base;
        print "filename is :" + filename;
        while n>0:
            with open(filename) as f:
                arrays = [line.rstrip('\n') for line in f.readlines()]
            #arrays = [line.rstrip('\n') for line in open(filename)]
                for index in range(len(arrays)):
                    if ("All: 1" in arrays[index]):
                        isTestEnd = True
                        break
            if isTestEnd :
                print "Test End"
                break
            time.sleep(1)
            n -=1
        if isTestEnd:
            for index in range(len(arrays)):
                searchString = "OK: 1";
                if ("OK: 1" in arrays[index]):
                    print "get OK number"
                    result = 0;
        return result;
#for test
