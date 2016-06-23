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
devicesArray=[]
devicesIPDic={}

class getAndroidDevice(object):
    @staticmethod
    def getDevices():
        output = subprocess.check_output('adb devices', shell=True,)
        print '\tstdout:', output
        if len(output) == 0:
           return 1
        else:
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
    	androidDevices=getDevices()
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
#for test
