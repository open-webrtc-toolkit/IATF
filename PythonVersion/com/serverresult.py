#! /usr/bin/python
# 1 : Pass check for JS result
# 0 : Case Failed for this Result
'''
Created on Jan,16 2016

@author: Yanbin
'''
import os
import time
import datetime
import sys
import getopt
from xml.dom import minidom
from xml.dom.minidom import Document
from config.config import Config
from config.config import ConfigKeys as Keys

import cStringIO
class JSResultParse:

    currentDate = datetime.datetime.now() 
    delta = datetime.timedelta(days=1)
    codeDate=currentDate-delta
    formatCodeDate = codeDate.strftime('%Y%m%d')
    @staticmethod
    def parseJSResult(testResultFile):
        BasePath=Config.getConfig(Keys.JS_CONFIG_FOLDER) 
        print BasePath
        JSTestResultPaths=[BasePath+"/report/"+testResultFile]
        print JSTestResultPaths
        summaryTotal=0
        summaryFailed=0
        summaryError=0
        summaryDisabled=0


   # Check results.
        for xmlTestResultPath in JSTestResultPaths:
            try:
                xmlDoc=minidom.parse(xmlTestResultPath)
            except:
                print "Error occured while reading woogeen server test result."
                return 1
        
            testSuites=xmlDoc.documentElement
            testSuite=testSuites.getElementsByTagName('testsuite')[1]
            summaryTotal+=int(testSuite.attributes["tests"].value)
            summaryFailed+=int(testSuite.attributes["failures"].value)
            summaryError+=int(testSuite.attributes["errors"].value)
    
        if summaryTotal==0:
           return 1
    
        elif summaryError > 0:
           return 1
    
        elif summaryFailed > 0:
           return 1
        else:
           return 0

#test#
#result=passResult("test-results-client1.xml")
#print result
