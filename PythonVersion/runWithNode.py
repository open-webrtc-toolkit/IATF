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
import pxssh
import re
import os
from threading import Thread

print 'Number of arguments:', len(sys.argv), 'arguments.'
print 'Argument List:', str(sys.argv)

caselistfile = ''
mode = ''
install = ''
number=1
connectedNode = False
connectedNodeNumber = 0;
nodeStatus={};
nodeResult={};
deployAndroid='';
remoteConnect='';
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
    global deployAndroid
    global nodeStatus
    global remoteConnect
    target = open("TestResult.txt", 'w');
    lines = [line.rstrip('\n') for line in open(filename)]
    cleanEnv = CleanEnv();
    cleanEnv.kill_karmaRun()
    emitmessage("lockevent",{"lock":"InitLock"})
    socket_connect
    for index in range(len(lines)):
      interval=10
      caseinfo=split_line(lines[index]);
      print "case is", caseinfo[0];
      print "classname is", caseinfo[1];
      ######clean enviroment befor start test suits#########
      cleanEnv.kill_karmaStart()
      cleanEnv.kill_Firefox()
      time.sleep(5)
    ########################################################################################
    # Local side is js, remote side is js #
    # Local side android is client2#
    # Remote side js is client1 #
    ########################################################################################
      if int(mode) == 0:
        print "start test js to js "  #  begining js to js test
        print "node connection start"
        #ssh connect to difference node and start node.py 
        deployNode=DeployNode.connect_node(Config.getConfig(Keys.NODE1_ADDR),Config.getConfig(Keys.NODE1_USER),Config.getConfig(Keys.NODE1_PASSD),Config.getConfig(Keys.NODE1_WORKFOLDER1),"node1", "jsp2p_1")
        
        t = Thread(target=MyThreadWaitmessage,args=(3,))
        t.start()
        time.sleep(10)
        length_connectedNode = len(nodeStatus.keys())
        if length_connectedNode < 1:
          time.sleep(10)
          length_connectedNode = len(nodeStatus.keys())
        else:
          print('All node connected to Server')
          emitmessage("lockevent",{"lock":"beginTest/"+caseinfo[0]+"/"+caseinfo[2]})
          # should adjust beginTest is resolved 
          #emitmessage("lockevent",{"lock":"STARTTEST"})
          # start local testing or new node #
          deployjs2=Deploy.deploy_js("testclient2.conf.js","P2P")
          if (deployjs2 == 0):
            t2 = Thread(target=WaitingPeerResult,args=(10,))
            t2.start()
            startjs2=Deploy.start_js("testclient2.conf.js",caseinfo[0],"P2P")

            print "startjs2 PID is: ", startjs2
            waitProcess(10,startjs2,"")

            case2result=JSResultParse.parseJSResult("test-results-client2.xml","P2P")
            print "case2result is ", case2result;
            JSResultParse.copyJSResult("test-results-client2.xml", caseinfo[0]+'_2',"P2P")

            if(case2result == 0):
              length_dict = len(nodeResult.keys())
              if length_dict == 1:
                if (nodeResult["node1"]) == "pass":
                  target.write("JS-JS case: "+caseinfo[0]+": pass");
                  target.write('\n');
                  print "JS-JS case: ",caseinfo[0],": pass";
                else:
                   target.write("JS-JS case: "+caseinfo[0]+": fail");
                   target.write('\n');
                   print "JS-JS case: ",caseinfo[0],": fail"
            else:
              target.write("JS-JS case: "+caseinfo[0]+": fail");
              target.write('\n');
              print "JS-JS case: ",caseinfo[0],": fail"
            cleanEnv.kill_karmaStart() # only need make sure karma start command is killed.
            emitmessage("lockevent",{"lock":"InitLock"})
          else:
            print("startBrowser error: "); 
        cleanEnv.kill_karmaStart() # only need make sure karma start command is killed.
        emitmessage("lockevent",{"lock":"InitLock"})
    ########################################################################################
    # Local side is android, remote side is js #
    # Local side android is client2#
    # Remote side js is client1 #
    ########################################################################################
      elif int(mode) == 1:
        print "local side is  Android"
        print "node connection start"
        #ssh connect to difference node and start node.py
        deployNode=DeployNode.connect_node(Config.getConfig(Keys.NODE1_ADDR),Config.getConfig(Keys.NODE1_USER),Config.getConfig(Keys.NODE1_PASSD),Config.getConfig(Keys.NODE1_WORKFOLDER1),"node1", "jsp2p_1")
        t = Thread(target=MyThreadWaitmessage,args=(3,))
        t.start()
        time.sleep(10)
        length_connectedNode = len(nodeStatus.keys())
        if length_connectedNode < 1:
          time.sleep(10)
          length_connectedNode = len(nodeStatus.keys())
        else:
          print('All node connected to Server')
          emitmessage("lockevent",{"lock":"beginTest/"+caseinfo[0]+"/"+caseinfo[2]})
          androidTestDevices=getAndroidDevice.getDevices();
          print androidTestDevices
          deployAndroid = 0
          if (deployAndroid != 0):
            deployAndroid=Deploy.deploy_android(androidTestDevices[0],"P2P")
          if (deployAndroid == 0):
            t2 = Thread(target=WaitingPeerResult,args=(10,))
            t2.start()
            emitmessage("lockevent",{"lock":"STARTTEST"})
            time.sleep(15)
            startAndorid=Deploy.start_android_sync(androidTestDevices[0],caseinfo[0],caseinfo[2],"P2P")
            waitProcess(10,startAndorid,'')
            AndroidResult = getAndroidDevice.read_caselist(caseinfo[2],caseinfo[0],"P2P");
            if (AndroidResult == 0):
              length_dict = len(nodeResult.keys())
              if length_dict == 1:
                if (nodeResult["node1"]) == "pass":
                  target.write("JS-Android case: "+caseinfo[0]+": pass");
                  target.write('\n');
                  print "JS-Android case: ",caseinfo[0],": pass";
                else:
                   target.write("JS-Android case: "+caseinfo[0]+": fail");
                   target.write('\n');
                   print "JS-Android case: ",caseinfo[0],": fail"
            else:
              print "JS-Android case: ",caseinfo[0],": fail"
              target.write("JS-Android case: "+caseinfo[0]+" : fail");
              target.write('\n');
          cleanEnv.kill_karmaStart() # only need make sure karma start command is killed.
          emitmessage("lockevent",{"lock":"InitLock"})
    ########################################################################################
    # Local side is android, remote side is iOS #
    # Local android is  is client1#
    # Remote side  iOS is client2 #
    ########################################################################################
      elif int(mode) == 2:
        print "local side is  Android"
        print "node connection start"
        deployNode=DeployNode.connect_node(Config.getConfig(Keys.NODE2_ADDR),Config.getConfig(Keys.NODE2_USER),Config.getConfig(Keys.NODE2_PASSD),Config.getConfig(Keys.NODE2_WORKFOLDER1),"node2", "iosp2p_2")
       # remoteConnect = 1
        t = Thread(target=MyThreadWaitmessage,args=(3,))
        t.start()
        time.sleep(10)
        length_connectedNode = len(nodeStatus.keys())
        if length_connectedNode < 1:
          time.sleep(10)
          length_connectedNode = len(nodeStatus.keys())
        else:
          #print nodeStatus["node2"]
          nodeStatus={}
          print('All node connected to Server')
        emitmessage("lockevent",{"lock":"beginTest/"+caseinfo[0]+"/"+caseinfo[2]})
        androidTestDevices=getAndroidDevice.getDevices();
        print androidTestDevices
        deployAndroid = 0
        if (deployAndroid != 0):
          deployAndroid=Deploy.deploy_android(androidTestDevices[0],"P2P")
        if (deployAndroid == 0):
          t2 = Thread(target=WaitingPeerResult,args=(10,))
          t2.start()
          emitmessage("lockevent",{"lock":"STARTTEST"})
          time.sleep(10)
          startAndorid=Deploy.start_android_sync(androidTestDevices[0],caseinfo[0],caseinfo[1],"P2P")
          waitProcess(10,startAndorid,'')
          AndroidResult = getAndroidDevice.read_caselist(caseinfo[1],caseinfo[0],"P2P");
          if (AndroidResult == 0):
            length_dict = len(nodeResult.keys())
            if length_dict == 1:
              if (nodeResult["node2"]) == "pass":
                target.write("JS-Android case: "+caseinfo[0]+": pass");
                target.write('\n');
                print "iOS-Android case: ",caseinfo[0],": pass";
              else:
                 target.write("iOS-Android case: "+caseinfo[0]+": fail");
                 target.write('\n');
                 print "iOS-Android case: ",caseinfo[0],": fail"
          else:
            print "iOS-Android case: ",caseinfo[0],": fail"
            target.write("iOS-Android case: "+caseinfo[0]+" : fail");
            target.write('\n');
          deployNode.close
          emitmessage("lockevent",{"lock":"InitLock"})
      #########################################################################################
       # Local is JS , remote is iOS #
       # Local android is  is client1 #
       # Remote side JS is client2 #
      #############################################################################################          
      elif int(mode) == 3:
        print "start test js to iOS"  #  begining js to js test
        print "node connection start"
        #ssh connect to difference node and start node.py 
        deployNode=DeployNode.connect_node(Config.getConfig(Keys.NODE2_ADDR),Config.getConfig(Keys.NODE2_USER),Config.getConfig(Keys.NODE2_PASSD),Config.getConfig(Keys.NODE2_WORKFOLDER1),"node2", "iosp2p_2")
        t = Thread(target=MyThreadWaitmessage,args=(3,))
        t.start()
        time.sleep(10)
        length_connectedNode = len(nodeStatus.keys())
        if length_connectedNode < 1:
          time.sleep(10)
          length_connectedNode = len(nodeStatus.keys())
        else:
          print('All node connected to Server')
          emitmessage("lockevent",{"lock":"beginTest/"+caseinfo[0]+"/"+caseinfo[2]})
          # should adjust beginTest is resolved 
          #emitmessage("lockevent",{"lock":"STARTTEST"})
          # start local testing or new node #
          deployjs2=Deploy.deploy_js("testclient1.conf.js","P2P")
          if (deployjs2 == 0):
            t2 = Thread(target=WaitingPeerResult,args=(10,))
            t2.start()
            emitmessage("lockevent",{"lock":"STARTTEST"})
            startjs2=Deploy.start_js("testclient1.conf.js",caseinfo[0],"P2P")

            print "startjs2 PID is: ", startjs2
            waitProcess(10,startjs2,"")

            case2result=JSResultParse.parseJSResult("test-results-client1.xml","P2P")
            print "case2result is ", case2result;
            JSResultParse.copyJSResult("test-results-client1.xml", caseinfo[0]+'_1',"P2P")

            if(case2result == 0):
              length_dict = len(nodeResult.keys())
              if length_dict == 1:
                if (nodeResult["node2"]) == "pass":
                  target.write("JS-iOS case: "+caseinfo[0]+": pass");
                  target.write('\n');
                  print "JS-iOS case: ",caseinfo[0],": pass";
                else:
                   target.write("JS-iOS  case: "+caseinfo[0]+": fail");
                   target.write('\n');
                   print "JS-iOS  case: ",caseinfo[0],": fail"
            else:
              target.write("JS-iOS case: "+caseinfo[0]+": fail");
              target.write('\n');
              print "JS-iOS  case: ",caseinfo[0],": fail"
            cleanEnv.kill_karmaStart() # only need make sure karma start command is killed.
            emitmessage("lockevent",{"lock":"InitLock"})
          else:
            print("startBrowser error: "); 
        deployNode.close
        cleanEnv.kill_karmaStart() # only need make sure karma start command is killed.
        emitmessage("lockevent",{"lock":"InitLock"})
    ########################################################################################
    # Conference mode #
    ########################################################################################

      elif int(mode) == 5:
        print "start conference test"
        #deployjs2=Deploy.deploy_js("testacular.conf2.js","CONFERENCE")
        #deployjs1=Deploy.deploy_js("testacular.conf1.js","CONFERENCE")
        t = Thread(target=MyThreadWaitmessage,args=(3,))
        t.start()

        deployNode=DeployNode.connect_node(Config.getConfig(Keys.NODE1_ADDR),Config.getConfig(Keys.NODE1_USER),Config.getConfig(Keys.NODE1_PASSD),Config.getConfig(Keys.NODE1_WORKFOLDER1),"node1", mode)
        deployNode2=DeployNode.connect_node(Config.getConfig(Keys.NODE2_ADDR),Config.getConfig(Keys.NODE2_USER),Config.getConfig(Keys.NODE2_PASSD),Config.getConfig(Keys.NODE2_WORKFOLDER1),"node2", mode)
        deployNode3=DeployNode.connect_node(Config.getConfig(Keys.NODE3_ADDR),Config.getConfig(Keys.NODE3_USER),Config.getConfig(Keys.NODE3_PASSD),Config.getConfig(Keys.NODE3_WORKFOLDER1),"node3", mode)
        time.sleep(10)
        length_connectedNode = len(nodeStatus.keys())
        if nodeStatus < 3:
          time.sleep(10)
          length_connectedNode = len(nodeStatus.keys())
        else:
          length_connectedNode = len(nodeStatus.keys())
          if(lenght_connectedNode == 3):
            print('All node connected to Server')
        emitmessage("lockevent",{"lock":"beginTest/"+caseinfo[0]+"/"+caseinfo[2]})
        t2 = Thread(target=WaitingPeerResult,args=(10,))
        t2.start()
        time.sleep(100)
        length_dict = len(nodeResult.keys())
        if length_dict == 3:
          if (nodeResult["node1"]) == "pass" and (nodeResult["node2"]) == "pass" and (nodeResult["node3"]) == "pass":
            target.write("Conference case: "+caseinfo[0]+": pass");
            target.write('\n');
            print "Conference case: ",caseinfo[0],": pass";
          else:
             target.write("Conference case: "+caseinfo[0]+": fail");
             target.write('\n');
             print "Conference case: ",caseinfo[0],": fail"
          #case1result=JSResultParse.parseJSResult("test-results-client1.xml","CONFERENCE")
          #case1result=JSResultParse.parseJSResult("test-results-client2.xml","CONFERENCE")
          #JSResultParse.copyJSResult("test-results-client1.xml", caseinfo[0],"CONFERENCE")
          #JSResultParse.copyJSResult("test-results-client2.xml", caseinfo[0],"CONFERENCE")
         # AndroidResult = getAndroidDevice.read_caselist(caseinfo[1],caseinfo[0],"CONFERENCE");

          #if (case1result == 0) and (AndroidResult == 0):
          #  target.write("JS-Android case:: "+caseinfo[0]+": pass");
          #  target.write('\n');
          #  print "JS-Andorid case: ",caseinfo[0],": pass"
          #else:
          #  print "JS-Android case: ",caseinfo[0],": fail"
          #  target.write("JS-Android case: "+caseinfo[0]+" : fail");
          #  target.write('\n');
        cleanEnv.kill_karmaStart() # only need make sure karma start command is killed.
        cleanEnv.kill_Firefox()
        emitmessage("lockevent",{"lock":"InitLock"})
    ###########################################################################################
    ###########################################################################################
    # close result file #
    ###########################################################################################
 
    target.close()

def WaitingPeerResult(n):
  # this function is start new thread will start monitor node is connected 
    while n > 0:
      print('T-minus', n)    
      socketIO.on('lockevent', waitingPeerResultCallBack);
      print "waiting ...."      
      n-=1
      socketIO.wait(seconds=10)
def waitingPeerResultCallBack(*args):
    lockValue = args[0]['lock']
    global connectedNode 
    if re.search("pass",lockValue) != None or re.search("fail",lockValue) != None :
      print "lockValue is ", lockValue;
      nodeName, nodeAction = lockValue.split("_");
      global nodeResult
      nodeResult[nodeName] = nodeAction
      print "nodeName is ", nodeName
      print "nodeAction is ",nodeAction
      print "nodeStatus is ", nodeResult[nodeName]
    return 0 

def MyThreadWaitmessage(n):
  # this function is start new thread will start monitor node is connected 
    while n > 0:
      print('T-minus', n)    
      socketIO.on('lockevent', waitMessageCallback);
      print "waiting ...."      
      n-=1
      socketIO.wait(seconds=2)

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
def emitmessage(message,data):
    socketIO.emit(message,data)

def waitMessageCallback(*args):
    #print('nodeStarted',args)
    #print "value is _____________________***********"
    #print args[0]['lock']
    lockValue = args[0]['lock']
    global connectedNode 
    if re.search("connected",lockValue) != None:
      print "lockValue is ", lockValue;
      nodeName, nodeAction = lockValue.split("_");
      global nodeStatus
      nodeStatus[nodeName] = nodeAction
      print "nodeName is ", nodeName
      print "nodeAction is ",nodeAction
      print "nodeStatus is ", nodeStatus[nodeName]
    return 0 

def waitmessage(message):
    socketIO.on('lockevent', waitMessageCallback);
    socketIO.wait(seconds=5);
def waitNodeProcess(interval, processgroup):
    print_ts("-"*100)
    print_ts("Starting every %s seconds."%interval)
    print_ts("-"*100)
    global number
    total_finished = 0;
    while number < 10:
      print "****waiting time is:", number*interval, "s";
      time_remaining = interval-time.time()%interval
      print_ts("Sleeping until %s (%s seconds)..."%((time.ctime(time.time()+time_remaining)), time_remaining))
      time.sleep(time_remaining)
      print_ts("Starting command.")
      for element in processgroup:
         if element == finished :
             total_finished = total_finished + 1
      if len(my_list) == total_finished:
        total_finished = 0;
        break
      number=number+1
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

