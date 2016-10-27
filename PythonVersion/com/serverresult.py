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
import subprocess
import commands
import cStringIO
class JSResultParse:

    currentDate = datetime.datetime.now() 
    delta = datetime.timedelta(days=1)
    codeDate=currentDate-delta
    formatCodeDate = codeDate.strftime('%Y%m%d')
    @staticmethod
    def copyJSResult(testResultFile,casename,mode):
        if mode == "P2P":
            BasePath=Config.getConfig(Keys.JS_P2P_CONFIG_FOLDER)
        else:
            BasePath=Config.getConfig(Keys.JS_CONFERENCE_CONFIG_FOLDER)
        print BasePath
        JSTestResultPath=BasePath+"/report/"+testResultFile
        ResultPath=BasePath+"/report/"+casename+".xml"
        print JSTestResultPath
        RenameCase=subprocess.Popen("cp "+ JSTestResultPath + " " + ResultPath, shell=True)
    @staticmethod
    def parseJSResult(testResultFile,mode):
        if mode == "P2P":
            BasePath=Config.getConfig(Keys.JS_P2P_CONFIG_FOLDER)
        else:
            BasePath=Config.getConfig(Keys.JS_CONFERENCE_CONFIG_FOLDER)
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
