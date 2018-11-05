#!/usr/bin/python
###################
#mode 0:  js to js
#mode 1:  js to android
#mode 2:  android to android
#mode 3:  android to ios
#mode 4:  js to ios
#####################
'''
Created on Jan,16 2016

@author: Yanbin
'''
from sys import platform
import sys, getopt, time
from socketIO_client import SocketIO, LoggingNamespace
import subprocess
from com.deploy import Deploy
from com.deploy_node import DeployNode
from com.serverresult import JSResultParse
from com.cleanEnv import CleanEnv
from com.getAndroidDevicesInfo import getAndroidDevice
from com.config.config import Config
from com.config.config import ConfigKeys as Keys
import psutil
import commands
from pexpect import pxssh
import re
import os
import json
print 'Number of arguments:', len(sys.argv), 'arguments.'
print 'Argument List:', str(sys.argv)

caselistfile = ''
mode = ''
install = ''
number=1
connectedNode = False
connectedNodeNumber = 0
deployAndroid = ''
deployAndroid1 = ''
deployAndroid2 = ''
deployAndroid3 = ''
androidTestDevices = ''
deployiOS_result = ''
try:
   opts, args = getopt.getopt(sys.argv[1:],"h:c:m:i",["help", "caselistfile=","mode=","install"])
except getopt.GetoptError:
   print 'error : run.py -c <caselistfile> -m <mode>'
   sys.exit(2)
for opt, arg in opts:
   if opt in ("-h", "--help"):
      print 'run.py -c <caselistfile> -m <mode> -i\n mode 0: JS to JS \n mode 1: JS to Android \n mode 2: Android to Android \n install:will re-install test application use the latest one, without this tag , we will not install package.'
      sys.exit()
   elif opt == "-c":
      caselistfile = arg
      print"getcaselist"
   elif opt == '-m':
      mode = arg
      print "getmode"
   elif opt == '-i':
      install = 'true'
   else:
      assert False, "unhandled option"
if caselistfile == ''or mode == '':
   print 'Error !!! \n Please use: run.py -c <caselistfile> -m <mode> \n mode 0: JS to JS \n mode 1: JS to Android \n mode 2: Android to Android'
   sys.exit()

print 'caselistfile is:', caselistfile
print 'mode is:', mode
socketServer = Config.getConfig(Keys.SOCKET_SERVER)
socketServerPort = Config.getConfig(Keys.SOCKET_SERVER_PORT)
socketServerPort_control = Config.getConfig(Keys.SOCKET_SERVER_PORT_control)
print "socketServer is " ,socketServer
print "socketServerPort is ",socketServerPort

socketIO_control = SocketIO(str(socketServer), int(socketServerPort_control),wait_for_connection=False)
socketIO_action = SocketIO(str(socketServer), int(socketServerPort),wait_for_connection=False)
def start_test(filename, mode):
    global androidTestDevices
    global deployAndroid
    global deployAndroid1
    global deployAndroid2
    global deployAndroid3
    global deployiOS_result
    JS_P2P_CONFIG_FOLDER = Config.getConfig(Keys.JS_P2P_CONFIG_FOLDER)
    ANDROID_P2P_CONFIG_FOLDER=Config.getConfig(Keys.ANDROID_P2P_CONFIG_FOLDER)
    jsresultparse = JSResultParse()
    target = open("TestResult.txt", 'w');
    lines = [line.rstrip('\n') for line in open(filename)]
    cleanEnv = CleanEnv();
    cleanEnv.kill_karmaRun()
    emitmessage("controlevent",{"lock":"InitLock"})
    #####################################
    ##deploy test cases #################
    ######################################
    if int(mode) == 0:
        cleanEnv.rm_TestResult(os.path.join(JS_P2P_CONFIG_FOLDER,'report/*'))
    elif int(mode) == 1 or int(mode) == 3:
        cleanEnv.rm_TestResult(os.path.join(JS_P2P_CONFIG_FOLDER,'report/*'))
        cleanEnv.rm_TestResult(os.path.join(ANDROID_P2P_CONFIG_FOLDER,'log/*'))
        androidTestDevices=getAndroidDevice.get_devices();
        print androidTestDevices
        if install == 'true':
          deployAndroid=Deploy.deploy_android(androidTestDevices[0],"P2P")
        else:
          deployAndroid=0
    elif int(mode) == 2:
        cleanEnv.rm_TestResult(os.path.join(ANDROID_P2P_CONFIG_FOLDER,'log/*'))
        print "start Android to Android"
        androidTestDevices=getAndroidDevice.get_devices();
        print androidTestDevices
        if install == 'true':
          deployAndroid1=Deploy.deploy_android(androidTestDevices[0],"P2P")
          deployAndroid2=Deploy.deploy_android(androidTestDevices[1],"P2P")
        else:
          deployAndroid1 = 0;
          deployAndroid2 = 0;
####################################################################################
# begin testing #
#####################################################################################
    for index in range(len(lines)):
      interval=10
      caseinfo=split_line(lines[index]);
      print "case is", caseinfo[0];
      print "classname is", caseinfo[1];
      ######clean enviroment befor start test suits#########
      if platform == "darwin":
        cleanEnv.kill_Safari()
      cleanEnv.kill_karmaStart()
      cleanEnv.kill_karmaPort()
      cleanEnv.kill_Chrome()
      cleanEnv.kill_Firefox()
      time.sleep(3)
      socket_connect()
      emitmessagetocontrolserver("controlevent",{"lock":"InitLock"})
      emitmessage("lockevent",{"lock":"InitLock"})
      if int(mode) == 0:
        print "start test js to js "  #  begining js to js test
        cleanEnv.rm_TestResult(os.path.join(JS_P2P_CONFIG_FOLDER,'report/test-results-*'))
        deployjs1=Deploy.deploy_js("testclient1.conf.js","P2P")
        deployjs2=Deploy.deploy_js("testclient2.conf.js","P2P")
        time.sleep(3)
        if (deployjs1 == 0) and (deployjs2 == 0):
          emitmessagetocontrolserver("controlevent",{"lock":"STARTTEST"})
          startjs1=Deploy.start_js("testclient1.conf.js",caseinfo[0],"P2P")
          startjs2=Deploy.start_js("testclient2.conf.js",caseinfo[0],"P2P")
          print "startjs1 PID is: ", startjs1
          print "startjs2 PID is: ", startjs2
          waitProcess(10, startjs1,startjs2)
          print "wait test-results-client1.xml"
          waitreport(50,os.path.join(JS_P2P_CONFIG_FOLDER,"report/test-results-client1.xml"))
          print "wait test-results-client2.xml"
          waitreport(50,os.path.join(JS_P2P_CONFIG_FOLDER,"report/test-results-client2.xml"))
          case1result=jsresultparse.parseJSResult("test-results-client1.xml","P2P")
          case2result=jsresultparse.parseJSResult("test-results-client2.xml","P2P")
          print "case1result is ", case1result;
          print "case2result is ", case2result;
          jsresultparse.copyJSResult("test-results-client1.xml", caseinfo[0]+'_1',"P2P")
          jsresultparse.copyJSResult("test-results-client2.xml", caseinfo[0]+'_2',"P2P")
          if (case1result == 0) and (case2result == 0):
            target.write("JS-JS case: "+caseinfo[0]+": pass");
            target.write('\n');
            print "JS-JS case: ",caseinfo[0],": pass";
          else:
            target.write("JS-JS case: "+caseinfo[0]+": fail");
            target.write('\n');
            print "JS-JS case: ",caseinfo[0],": fail"
          emitmessagetocontrolserver("controlevent",{"lock":"InitLock"})
        else:
          print("startBrowser error: ");
    ########################################################################################
    # mode 1: JS to Android #
    ########################################################################################
      elif int(mode) == 1:
        print "start test JS to Android"
        cleanEnv.rm_TestResult(os.path.join(JS_P2P_CONFIG_FOLDER,'report/test-results-*'))
        deployjs1=Deploy.deploy_js("testclient1.conf.js","P2P")
        if (deployjs1 == 0) and (deployAndroid == 0):
          emitmessagetocontrolserver("controlevent",{"lock":"STARTTEST"})
          startjs=Deploy.start_js("testclient1.conf.js",caseinfo[0],"P2P")
          print "startjs PID is: ", startjs;
          startAndorid=Deploy.start_android_sync(androidTestDevices[0],caseinfo[0],caseinfo[2],"P2P")
          #waitProcess(10, startjs,startAndorid)
          waitProcess(15, startjs)
          time.sleep(10)
          startAndorid.communicate()
          waitreport(50,os.path.join(JS_P2P_CONFIG_FOLDER,"report/test-results-client1.xml"))
          waitreport(50,os.path.join(ANDROID_P2P_CONFIG_FOLDER,"log/"+caseinfo[2]+"-"+caseinfo[0]+'.txt'))
          case1result=jsresultparse.parseJSResult("test-results-client1.xml","P2P")
          jsresultparse.copyJSResult("test-results-client1.xml", caseinfo[0],"P2P")
          AndroidResult = getAndroidDevice.read_caselist(caseinfo[2],caseinfo[0],"P2P");
          if (case1result == 0) and (AndroidResult == 0):
            target.write("JS-Android case:: "+caseinfo[0]+": pass");
            target.write('\n');
            print "JS-Andorid case: ",caseinfo[0],": pass"
          else:
            print "JS-Android case: ",caseinfo[0],": fail"
            target.write("JS-Android case: "+caseinfo[0]+" : fail");
            target.write('\n');
        emitmessage("controlevent",{"lock":"InitLock"})
    ########################################################################################
    # mode 2 : Android to Android #
    ########################################################################################
      elif int(mode) == 2:
        print "start Android to Android"
        if (deployAndroid1 == 0) and (deployAndroid2 == 0):
          emitmessagetocontrolserver("controlevent",{"lock":"STARTTEST"})
          startAndroid1=Deploy.start_android_sync(androidTestDevices[0],caseinfo[0],caseinfo[1],"P2P");
          startAndroid2=Deploy.start_android_sync(androidTestDevices[1],caseinfo[0],caseinfo[2],"P2P");
          startAndroid1.communicate()
          startAndroid2.communicate()
          #waitProcess(10, startAndroid1.pid,startAndroid2.pid)
          waitreport(50,os.path.join(ANDROID_P2P_CONFIG_FOLDER,"log/"+caseinfo[1]+"-"+caseinfo[0]+'.txt'))
          waitreport(50,os.path.join(ANDROID_P2P_CONFIG_FOLDER,"log/"+caseinfo[2]+"-"+caseinfo[0]+'.txt'))
          print "check Android1Result"
          Android1Result = getAndroidDevice.read_caselist(caseinfo[1],caseinfo[0],"P2P");
          print "check Android2Result"
          Android2Result = getAndroidDevice.read_caselist(caseinfo[2],caseinfo[0],"P2P");
          print "Android1Result",Android1Result
          print "Android2Result",Android2Result
          if (Android1Result == 0) and (Android2Result == 0):
            target.write("Android-Android case:: "+caseinfo[0]+": pass");
            target.write('\n');
            print "Android-Andorid case: ",caseinfo[0],": pass"
          else:
            print "Android-Android case: ",caseinfo[0],": fail"
            target.write("Android-Android case: "+caseinfo[0]+" : fail");
            target.write('\n');
        emitmessage("controlevent",{"lock":"InitLock"})
      elif int(mode) == 3:
        print "start test JS to Android"
        cleanEnv.rm_TestResult(os.path.join(JS_P2P_CONFIG_FOLDER,'report/test-results-*'))
        deployjs1=Deploy.deploy_js("testclient2.conf.js","P2P")
        if (deployjs1 == 0) and (deployAndroid == 0):
          emitmessagetocontrolserver("controlevent",{"lock":"STARTTEST"})
          startjs=Deploy.start_js("testclient2.conf.js",caseinfo[0],"P2P")
          print "startjs PID is: ", startjs;
          startAndorid=Deploy.start_android_sync(androidTestDevices[0],caseinfo[0],caseinfo[1],"P2P")
          #waitProcess(10, startjs,startAndorid)
          waitProcess(15, startjs)
          time.sleep(10)
          startAndorid.communicate()
          waitreport(50,os.path.join(JS_P2P_CONFIG_FOLDER,"report/test-results-client2.xml"))
          waitreport(50,os.path.join(ANDROID_P2P_CONFIG_FOLDER,"log/"+caseinfo[1]+"-"+caseinfo[0]+'.txt'))
          case1result=jsresultparse.parseJSResult("test-results-client2.xml","P2P")
          jsresultparse.copyJSResult("test-results-client2.xml", caseinfo[0],"P2P")
          AndroidResult = getAndroidDevice.read_caselist(caseinfo[1],caseinfo[0],"P2P");
          if (case1result == 0) and (AndroidResult == 0):
            target.write("JS-Android case:: "+caseinfo[0]+": pass");
            target.write('\n');
            print "JS-Andorid case: ",caseinfo[0],": pass"
          else:
            print "JS-Android case: ",caseinfo[0],": fail"
            target.write("JS-Android case: "+caseinfo[0]+" : fail");
            target.write('\n');
        emitmessage("controlevent",{"lock":"InitLock"})
      cleanEnv.kill_karmaStart()
      cleanEnv.kill_karmaPort()
      cleanEnv.kill_Chrome()
      cleanEnv.kill_Firefox()
      if platform == "darwin":
        cleanEnv.kill_Safari()
      target.close()

def split_line(text):
    casenumber, casename = text.split("=")
    if casename:
       caseInfoList = casename.split(";");
       caseInfoList[0] = caseInfoList[0].replace("\"", "");
       caseInfoList[-1] = caseInfoList[-1].replace("\"", "");
       return caseInfoList

def on_aaa_response(*args):
    print('connected', args)
    return 0;
def socket_connect():
    socketIO_action.on('connect', on_aaa_response)
    socketIO_action.wait(seconds=3)
    socketIO_control.on('connect', on_aaa_response)
    socketIO_control.wait(seconds=3)


def emitmessage(message,data):
    socketIO_action.emit(message,data)

def emitmessagetocontrolserver(message,data):
    socketIO_control.emit(message,data)

def print_ts(message):
    print "[%s] %s"%(time.strftime("%Y-%m-%d %H:%M:%S", time.localtime()), message)

def waitreport(n,filename):
  flag = False
  while n >0:
    if os.path.exists(filename):
      flag = True  
      break
    else:
      time.sleep(1)
    n -=1
  return flag


def waitProcess(interval, processnumber1,processnumber2=None):
    print_ts("-"*100)
    print_ts("Starting every %s seconds."%interval)
    print_ts("-"*100)
    global number
    while number < 10:
      print "****waiting time is:", number*interval, "s";
      time_remaining = interval-time.time()%interval
      print_ts("Sleeping until %s (%s seconds)..."%((time.ctime(time.time()+time_remaining)), time_remaining))
      time.sleep(time_remaining)
      print_ts("Starting command.")
      p=psutil.pids();
      #print p
      if processnumber2 is not None:
        if (processnumber1 in p) or (processnumber2 in p):
          print_ts("-"*100)
          print("process ",processnumber1,',',processnumber2," still running");
          print("process ",processnumber1,"status is, ",psutil.Process(processnumber1).status());
          print("process ",processnumber2,"status is, ",psutil.Process(processnumber2).status());
          if (psutil.Process(processnumber1).status() == 'zombie') and (psutil.Process(processnumber2).status() == 'zombie'):
            break
        else:
          break
      else:
        print "*****only need detect single process", processnumber1
        if (processnumber1 in p):
          print_ts("-"*100)
          print("process ",processnumber1," still running");
          print("process ",processnumber1,"status is, ",psutil.Process(processnumber1).status());
          if (psutil.Process(processnumber1).status() == 'zombie'):
            break
        else:
          break
      number=number+1
#test#
#if __name__ == "__main__":
#    start_test(caselist,0)
if __name__ == "__main__":
   start_test(caselistfile,mode)
