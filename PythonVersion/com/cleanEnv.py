import os
import time
import datetime
import sys
import getopt
from config.config import Config
from config.config import ConfigKeys as Keys
import subprocess

class CleanEnv:
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



