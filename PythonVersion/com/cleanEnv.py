import os
import time
import datetime
import sys
import getopt
from config.config import Config
from config.config import ConfigKeys as Keys
import subprocess

class CleanEnv(object):
    @staticmethod
    def kill_karmaRun():
        kill_karmaRun=subprocess.Popen(' ps aux | grep \'karma run\' | grep -v \'grep\' | awk \'{print $2}\'|xargs kill -9 >/dev/null 2>&1 ', shell=True)
        kill_karmaRun.wait()
    @staticmethod
    def kill_karmaStart():
    	kill_karmaStart=subprocess.Popen('ps aux | grep \'karma start\' | grep -v \'grep\' | awk \'{print $2}\'|xargs kill -9 >/dev/null 2>&1', shell=True)
        kill_karmaStart.wait()
        kill_karmaStart=subprocess.Popen('ps aux | grep chrome | grep -v \'grep\' | awk \'{print $2}\' |xargs kill -9 >/dev/null 2>&1', shell=True)
        kill_karmaStart.wait()
    @staticmethod
    def kill_Firefox():
        kill_karmaStart=subprocess.Popen('ps aux | grep firefox | grep -v \'grep\' | awk \'{print $2}\' |xargs kill -9 >/dev/null 2>&1', shell=True)
        kill_karmaStart.wait()
    @staticmethod
    def kill_lockServer():
        kill_karmaStart=subprocess.Popen('ps aux | grep \'lockserver.jar\' | grep -v \'grep\' | awk \'{print $2}\'|xargs kill -9 >/dev/null 2>&1', shell=True)
        kill_karmaStart.wait()
    @staticmethod
    def kill_runTest():
        kill_runtest=subprocess.Popen('ps aux | grep \'runTest.py\' | grep -v \'grep\' | awk \'{print $2}\'|xargs kill -9 >/dev/null 2>&1', shell=True)
        kill_runtest.wait()
    @staticmethod
    def rm_JsTestResult(testResultFile,mode):
        if mode == "P2P":
            BasePath=Config.getConfig(Keys.JS_P2P_CONFIG_FOLDER)
        else:
            BasePath=Config.getConfig(Keys.JS_CONFERENCE_CONFIG_FOLDER)
        print BasePath
        JSTestResultPath=BasePath+"/report/"+testResultFile
        RenameCase=subprocess.Popen("rm -f "+ JSTestResultPath, shell=True)
    @staticmethod
    def rm_log(testlogFile):
        RenameCase=subprocess.Popen("rm "+ testlogFile+"/*", shell=True)
    @staticmethod
    def rm_TestResult(testresultFile):
        RenameCase=subprocess.Popen("rm "+ testresultFile, shell=True)


