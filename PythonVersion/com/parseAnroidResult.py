#!/usr/bin/python
'''
Created on Jan,16 2016

@author: Yanbin

this file is used for analaysis android scripts
'''
import sys, getopt, time
from config.config import Config
from config.config import ConfigKeys as Keys
class parseResult(object):
	@staticmethod
	def read_caselist(devices, casename):
		AndroidPath=Config.getConfig(Keys.ANDROID_CONFIG_FOLDER)
		filename_base = "p2p-android-test-result--com.intel.webrtc.test."+device+'-'+casename+text;
		filename = AndroidPath+'\log'+'\\'+filename_base;
		print "filename is :" + filename;
	    lines = [line.rstrip('\n') for line in open(filename)]
	    for index in range(len(lines)):
	    	if('OK \(1 test\)' in lines[index]):
	    		return 0
	    	else:
	    		return 1



