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
from com.serverresult import JSResultParse
from com.cleanEnv import CleanEnv
from com.getAndroidDevicesInfo import getAndroidDevice
from com.config.config import Config
from com.config.config import ConfigKeys as Keys
import psutil
import commands
import pxssh
import re
import os
print 'Number of arguments:', len(sys.argv), 'arguments.'
print 'Argument List:', str(sys.argv)

caselistfile = ''
mode = ''
install = ''
number=1
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
print "socketServer is " ,socketServer
print "socketServerPort is ",socketServerPort
socketIO = SocketIO(str(socketServer), int(socketServerPort))
def start_test(filename, mode):
    target = open("TestResult.txt", 'w');
    lines = [line.rstrip('\n') for line in open(filename)]
    cleanEnv = CleanEnv();
    cleanEnv.kill_karmaRun()
    emitmessage("lockevent",{"lock":"InitLock"})

    for index in range(len(lines)):
      interval=10
      caseinfo=split_line(lines[index]);
      print "case is", caseinfo[0];
      print "classname is", caseinfo[1];
      ######clean enviroment befor start test suits#########
      cleanEnv.kill_karmaStart()
      cleanEnv.kill_Firefox()
      time.sleep(5)
      socket_connect()
      if int(mode) == 0:
        print "start test js to js "  #  begining js to js test
        deployjs1=Deploy.deploy_js("testclient1.conf.js")
        deployjs2=Deploy.deploy_js("testclient2.conf.js")
        if (deployjs1 == 0) and (deployjs2 == 0):
          emitmessage("lockevent",{"lock":"STARTTEST"})
          startjs1=Deploy.start_js("testclient1.conf.js",caseinfo[0])
          startjs2=Deploy.start_js("testclient2.conf.js",caseinfo[0])
          print "startjs1 PID is: ", startjs1
          print "startjs2 PID is: ", startjs2
          waitProcess(10, startjs1,startjs2)
          case1result=JSResultParse.parseJSResult("test-results-client1.xml")
          case2result=JSResultParse.parseJSResult("test-results-client2.xml")
          print "case1result is ", case1result;
          print "case2result is ", case2result;
          JSResultParse.copyJSResult("test-results-client1.xml", caseinfo[0]+'_1')
          JSResultParse.copyJSResult("test-results-client2.xml", caseinfo[0]+'_2')
          if (case1result == 0) and (case2result == 0):
            target.write("JS-JS case: "+caseinfo[0]+": pass");
            target.write('\n');
            print "JS-JS case: ",caseinfo[0],": pass";
          else:
            target.write("JS-JS case: "+caseinfo[0]+": fail");
            target.write('\n');
            print "JS-JS case: ",caseinfo[0],": fail"
          cleanEnv.kill_karmaStart() # only need make sure karma start command is killed.
          emitmessage("lockevent",{"lock":"InitLock"})
        else:
          print("startBrowser error: ");
    ########################################################################################
    # JS to Android #
    ########################################################################################
      elif int(mode) == 1:
        print "start test JS to Android"
        deployjs1=Deploy.deploy_js("testclient1.conf.js")
        androidTestDevices=getAndroidDevice.getDevices();
        print androidTestDevices
        if install == 'true':
          deployAndroid=Deploy.deploy_android(androidTestDevices[0])
        else:
          deployAndroid=0
        if (deployjs1 == 0) and (deployAndroid == 0):
          emitmessage("lockevent",{"lock":"STARTTEST"})
          startjs=Deploy.start_js("testclient1.conf.js",caseinfo[0])
          print "startjs PID is: ", startjs;
          startAndorid=Deploy.start_android_sync(androidTestDevices[0],caseinfo[0],caseinfo[2])
          waitProcess(10, startjs,startAndorid)
          case1result=JSResultParse.parseJSResult("test-results-client1.xml")
          JSResultParse.copyJSResult("test-results-client1.xml", caseinfo[0])
          AndroidResult = getAndroidDevice.read_caselist(caseinfo[2],caseinfo[0]);
          if (case1result == 0) and (AndroidResult == 0):
            target.write("JS-Android case:: "+caseinfo[0]+": pass");
            target.write('\n');
            print "JS-Andorid case: ",caseinfo[0],": pass"
          else:
            print "JS-Android case: ",caseinfo[0],": fail"
            target.write("JS-Android case: "+caseinfo[0]+" : fail");
            target.write('\n');
        cleanEnv.kill_karmaStart() # only need make sure karma start command is killed.
        emitmessage("lockevent",{"lock":"InitLock"})
    ########################################################################################
    # Android to Android #
    ########################################################################################
      elif int(mode) == 2:
        print "start Android to Android"
        androidTestDevices=getAndroidDevice.getDevices();
        print androidTestDevices
        if install == 'true':
          deployAndroid1=Deploy.deploy_android(androidTestDevices[0])
          deployAndroid2=Deploy.deploy_android(androidTestDevices[1])
        else:
          deployAndroid1 = 0;
          deployAndroid2 = 0;
        if (deployAndroid1 == 0) and (deployAndroid2 == 0):
          emitmessage("lockevent",{"lock":"STARTTEST"})
          startAndroid1=Deploy.start_android_sync(androidTestDevices[0],caseinfo[0],caseinfo[1]);
          startAndroid2=Deploy.start_android_sync(androidTestDevices[1],caseinfo[0],caseinfo[2]);
          waitProcess(10, startAndroid1,startAndroid2)
          Android1Result = getAndroidDevice.read_caselist(caseinfo[1],caseinfo[0]);
          Android2Result = getAndroidDevice.read_caselist(caseinfo[2],caseinfo[0]);
          if (Android1Result == 0) and (Android2Result == 0):
            target.write("Android-Android case:: "+caseinfo[0]+": pass");
            target.write('\n');
            print "Android-Andorid case: ",caseinfo[0],": pass"
          else:
            print "Android-Android case: ",caseinfo[0],": fail"
            target.write("Android-Android case: "+caseinfo[0]+" : fail");
            target.write('\n');
        emitmessage("lockevent",{"lock":"InitLock"})

    ########################################################################################
    # Android to iOS #
    ########################################################################################
      elif int(mode) == 3:
        print "start Android to iOS"
        androidTestDevices=getAndroidDevice.getDevices();
        print androidTestDevices
        if install == 'true':
          deployiOS=Deploy.deploy_iOS('WoogeenChatTest.xcodeproj','WoogeenChatTest')
          deployAndroid1=Deploy.deploy_android(androidTestDevices[0])
          deployiOS.prompt()
          print deployiOS.before
          deployiOS.sendline('echo $?')
          deployiOS.prompt()
          deployiOS_result=deployiOS.before.strip()
          print "$? ......"
          print deployiOS_result
          print "deployiOS pid is"
          print deployiOS.pid
          #close ssh connection
          deployiOS.close
        else:
          deployAndroid1 = 0;
          deployiOS_result = 0;
        if (deployAndroid1 == 0) and (deployiOS_result == 0):
          path="iOSResult"
          if not os.path.exists(path):
             os.mkdir(path)
          iOSResultFile = open("iOSResult/"+caseinfo[0]+'_'+caseinfo[2]+'.txt', 'w');
          print "start testing "
          emitmessage("lockevent",{"lock":"STARTTEST"})
          startAndroid1=Deploy.start_android_sync(androidTestDevices[0],caseinfo[0],caseinfo[1]);
          startiOS=Deploy.start_iOS('WoogeenChatTest.xcodeproj','WoogeenChatTest','WoogeenChatTestTests',caseinfo[0],caseinfo[2]);
          print startiOS.pid;
          # following code only use to check the android running process
          waitProcess(10,startAndroid1,"");         
          ##################################################################################
          # check iOS process
          startiOS.prompt()
          deployiOS_result=startiOS.before
          print deployiOS_result
          iOSResultFile.write(deployiOS_result);
          iOSResultFile.close()
          if(re.search("1 failed",deployiOS_result) and re.search("1 total",deployiOS_result)):
            print "match failed"
            iOSResult = 1
          elif(re.search("1 passed",deployiOS_result) and re.search("1 total",deployiOS_result)):
            print "match passed"
            iOSResult = 0
          #####################################################################################
          # compare result
          Android1Result = getAndroidDevice.read_caselist(caseinfo[1],caseinfo[0]);
          if (Android1Result == 0) and (iOSResult == 0):
            target.write("Android-iOS case:: "+caseinfo[0]+": pass");
            target.write('\n');
            print "Android-iOS case: ",caseinfo[0],": pass"
          else:
            print "Android-iOS case: ",caseinfo[0],": fail"
            target.write("Android-iOS case: "+caseinfo[0]+" : fail");
            target.write('\n');
          #########close ssh process ###########
          startiOS.close
          emitmessage("lockevent",{"lock":"InitLock"})
    ########################################################################################
    # JS to iOS #
    ########################################################################################
      elif int(mode) == 4:
        print "start JS to iOS"
        deployjs1=Deploy.deploy_js("testclient1.conf.js")
        if install == 'true':
          deployiOS=Deploy.deploy_iOS('WoogeenChatTest.xcodeproj','WoogeenChatTest')
          deployiOS.prompt()
          print deployiOS.before
          deployiOS.sendline('echo $?')
          deployiOS.prompt()
          deployiOS_result=deployiOS.before.strip()
          print "$? ......"
          print deployiOS_result
          print "deployiOS pid is"
          print deployiOS.pid
          #close ssh connection
          deployiOS.close
        else:
          deployiOS_result = 0;
        if (deployjs1 == 0) and (deployiOS_result == 0):
          iOSResultFile = open("iOSResult/"+caseinfo[0]+'_'+caseinfo[2]+'.txt', 'w');
          print "start testing "
          emitmessage("lockevent",{"lock":"STARTTEST"})
          startiOS=Deploy.start_iOS('WoogeenChatTest.xcodeproj','WoogeenChatTest','WoogeenChatTestTests',caseinfo[0],caseinfo[2]);
          print startiOS.pid;
          startjs1=Deploy.start_js("testclient1.conf.js",caseinfo[0])
          print "startjs1 PID is: ", startjs1;
          # following code only use to check the JS running process
          #############################################################################
          waitProcess(10,startjs1,"");
          ##################################################################################
          # check iOS process
          startiOS.prompt()
          deployiOS_result=startiOS.before
          print deployiOS_result
          iOSResultFile.write(deployiOS_result);
          iOSResultFile.close()
          if(re.search("1 failed",deployiOS_result) and re.search("1 total",deployiOS_result)):
            print "match failed"
            iOSResult = 1
          elif(re.search("1 passed",deployiOS_result) and re.search("1 total",deployiOS_result)):
            print "match passed"
            iOSResult = 0
          #####################################################################################
          # compare result
          case1result=JSResultParse.parseJSResult("test-results-client1.xml")
          JSResultParse.copyJSResult("test-results-client1.xml", caseinfo[0])
          if (case1result == 0) and (iOSResult == 0):
            target.write("JS-iOS case:: "+caseinfo[0]+": pass");
            target.write('\n');
            print "JS-iOS case: ",caseinfo[0],": pass"
          else:
            print "JS-iOS case: ",caseinfo[0],": fail"
            target.write("JS-iOS case: "+caseinfo[0]+" : fail");
            target.write('\n');
          #########close ssh process ###########
          startiOS.close
          cleanEnv.kill_karmaStart() # only need make sure karma start command is killed.
          emitmessage("lockevent",{"lock":"InitLock"})
    ###########################################################################################
    # close result file #
    ###########################################################################################
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
    socketIO.on('connect', on_aaa_response)
    socketIO.wait(seconds=3)
    return socketIO;
    socketIO.emit("lockevent",{"lock":"STARTTEST"})
def emitmessage(message,data):
    socketIO.emit(message,data)
    
def print_ts(message):
    print "[%s] %s"%(time.strftime("%Y-%m-%d %H:%M:%S", time.localtime()), message)
def waitProcess(interval, processnumber1,processnumber2):
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
      print p
      if processnumber2:
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
start_test(caselistfile,mode)
#test#
#if __name__ == "__main__":
#    start_test(caselist,0)

