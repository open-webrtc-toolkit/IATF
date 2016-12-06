import os
import time
import datetime
import sys
import getopt
from config.config import Config
from config.config import ConfigKeys as Keys
import subprocess

class CleanEnv(object):
    def kill_karmaRun(self):
        kill_karmaRun=subprocess.Popen(' ps aux | grep \'karma run\' | grep -v \'grep\' | awk \'{print $2}\'|xargs kill -9 >/dev/null 2>&1 ', shell=True)
        kill_karmaRun.wait()
    def kill_karmaStart(self):
    	kill_karmaStart=subprocess.Popen('ps aux | grep \'karma start\' | grep -v \'grep\' | awk \'{print $2}\'|xargs kill -9 >/dev/null 2>&1', shell=True)
        kill_karmaStart.wait()
        kill_karmaStart=subprocess.Popen('ps aux | grep chrome | grep -v \'grep\' | awk \'{print $2}\' |xargs kill -9 >/dev/null 2>&1', shell=True)
        kill_karmaStart.wait()
    def kill_Firefox(self):
        kill_karmaStart=subprocess.Popen('ps aux | grep firefox | grep -v \'grep\' | awk \'{print $2}\' |xargs kill -9 >/dev/null 2>&1', shell=True)
        kill_karmaStart.wait()
    def kill_lockServer(self):
        kill_karmaStart=subprocess.Popen('ps aux | grep \'lockserver.jar\' | grep -v \'grep\' | awk \'{print $2}\'|xargs kill -9 >/dev/null 2>&1', shell=True)
        kill_karmaStart.wait()
    def kill_runTest(self):
        kill_runtest=subprocess.Popen('ps aux | grep \'runTest.py\' | grep -v \'grep\' | awk \'{print $2}\'|xargs kill -9 >/dev/null 2>&1', shell=True)
        kill_runtest.wait()    
    def rm_JsTestResult(self,testResultFile,mode):
        if mode == "P2P":
            BasePath=Config.getConfig(Keys.JS_P2P_CONFIG_FOLDER)
        else:
            BasePath=Config.getConfig(Keys.JS_CONFERENCE_CONFIG_FOLDER)
        print BasePath
        JSTestResultPath=BasePath+"/report/"+testResultFile
        RenameCase=subprocess.Popen("rm -f "+ JSTestResultPath, shell=True)
    def rm_log(self,testlogFile):
        RenameCase=subprocess.Popen("rm "+ testlogFile+"/*", shell=True)
    def rm_TestResult(self,testresultFile):
        RenameCase=subprocess.Popen("rm "+ testresultFile, shell=True)


