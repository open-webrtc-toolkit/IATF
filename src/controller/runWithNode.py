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
from threading import Thread

print 'Number of arguments:', len(sys.argv), 'arguments.'
print 'Argument List:', str(sys.argv)

caselistfile = ''
mode = ''
install = 'false'
number=1
isfinish = False
istimeout = True

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
socketIO = SocketIO(str(socketServer), int(socketServerPort),wait_for_connection=False)
socketIO_control = SocketIO(str(socketServer), int(socketServerPort_control),wait_for_connection=False)
def start_test(filename, mode):
    global isfinish
    global istimeout
    tag = ""
    if mode == 0:
      tag = "JS-JS"
    elif mode == 1:
      tag = "Android-js"
    elif mode == 2:
      tag = "Android-Android"
    elif mode == 3:
      tag = "JS-IOS"
    elif mode == 4:
      tag = "Android-IOS"
    elif mode == 5:
      tag = "IOS-IOS"
    lines = [line.rstrip('\n') for line in open(filename)]
    #cleanEnv = CleanEnv();
    CleanEnv.kill_karmaRun()
    basepath = os.path.join(os.getcwd(),os.path.split(sys.argv[0])[0])
    logpath = os.path.join(basepath,'log')
    resultpath = os.path.join(basepath,"TestResult.txt")
    reportpath = os.path.join(basepath,"report")
    print logpath
    if not os.path.exists(logpath):
      os.mkdir(logpath)
    else:
      CleanEnv.rm_log(logpath)
    if os.path.exists(resultpath):
      CleanEnv.rm_TestResult(resultpath)
    runshell("touch " + resultpath)
    DeployNode.init_node(Config.getConfig(Keys.NODE1_ADDR),Config.getConfig(Keys.NODE1_USER),Config.getConfig(Keys.NODE1_PASSD),Config.getConfig(Keys.NODE1_WORKFOLDER1))
    if mode ==  5:
       DeployNode.init_node(Config.getConfig(Keys.NODE2_ADDR),Config.getConfig(Keys.NODE2_USER),Config.getConfig(Keys.NODE2_PASSD),Config.getConfig(Keys.NODE2_WORKFOLDER1))
    #socket_connect
    print len(lines)
    for index in range(len(lines)):
      isfinish = False
      istimeout = True
      print "start_test"
      emitmessage("lockevent",{"lock":"Init_action_Lock"})
      emitmessagetocontrolserver("controlevent",{"lock":"Init_control_Lock"})
      ######clean enviroment befor start test suits#########
      CleanEnv.kill_karmaStart()
      CleanEnv.kill_Firefox()
      CleanEnv.rm_JsTestResult('test-results-client2.xml','P2P')
      CleanEnv.rm_JsTestResult('test-results-client1.xml','P2P')
      interval=10
      caseinfo=split_line(lines[index]);
      print "case is", caseinfo[0];
      print "classname is", caseinfo[1];
      casetest = caseinfo[0] +"/"+caseinfo[1]+"/"+caseinfo[2]
      testlogpath = os.path.join(logpath,caseinfo[0])
      if install == 'true' and index == 0:
        pid = Deploy.deploy_runtest(casetest,mode,'true',testlogpath)
      else:
        pid = Deploy.deploy_runtest(casetest,mode,'false',testlogpath)
      print "runtest :" +caseinfo[0]
      if runtesttimeout(pid,200):
        print "test is timeout "
        target = open("TestResult.txt", 'a');
        target.write(tag+": "+caseinfo[0] +" : test is timeout");
        target.write('\n');
        target.close()
      else:
        print "test run is finish"
      print "cleanEnv"
      CleanEnv.kill_runTest()
      CleanEnv.kill_karmaStart()
      CleanEnv.kill_Firefox()
      time.sleep(2)

    #if not os.path.exists(reportpath):
    #    os.mkdir(reportpath)
    #test=subprocess.Popen(basepath+"/lib/testscp.sh "+Config.getConfig(Keys.NODE1_USER)+" " + Config.getConfig(Keys.NODE1_ADDR) +" " +Config.getConfig(Keys.NODE1_WORKFOLDER1)+"/node.py " + reportpath +" "+ Config.getConfig(Keys.NODE1_PASSD) ,shell=True)
    #test.wait()


def runshell(cmd):
    runcmd=subprocess.Popen(cmd, shell=True)
    runcmd.wait()

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
    socketIO.on('connect', on_aaa_response)
    socketIO.wait(seconds=3)
    socketIO_control.on('connect', on_aaa_response)
    socketIO_control.wait(seconds=3)


def emitmessage(message,data):
    socketIO.emit(message,data)

def emitmessagetocontrolserver(message,data):
    socketIO_control.emit(message,data)


def runtesttimeout(runTest,n):
    global isfinish
    global istimeout
    print runTest.pid
    t = Thread(target=threadwaittestrun,args=(n,))
    t.daemon = True
    t.start()
    runTest.wait()
    isfinish = True
    time.sleep(1)
    return istimeout

def threadwaittestrun(n):
    global isfinish
    global istimeout
    while n > 0:
      if isfinish:
        istimeout = False
        break;
      n-=1
      time.sleep(1)
    CleanEnv.kill_runTest()



# def waitMessageCallback(*args):
#     #print('nodeStarted',args)
#     #print "value is _____________________***********"
#     #print args[0]['lock']
#     lockValue = args[0]['lock']
#     global connectedNode
#     if re.search("connected",lockValue) != None:
#       print "lockValue is ", lockValue;
#       nodeName, nodeAction = lockValue.split("_");
#       global nodeStatus
#       nodeStatus[nodeName] = nodeAction
#       print "nodeName is ", nodeName
#       print "nodeAction is ",nodeAction
#       print "nodeStatus is ", nodeStatus[nodeName]
#     return 0 

# def waitmessage(message):
#     socketIO_control.on('waitcontrollock', waitMessageCallback);
#     socketIO_control.wait(seconds=5);

start_test(caselistfile,mode)
#test#
#if __name__ == "__main__":
#    start_test(caselist,0)

