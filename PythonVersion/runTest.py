#!/usr/bin/python
# -*- coding: utf-8 -*-
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
import com.util as util
from threading import Thread

print 'Number of arguments:', len(sys.argv), 'arguments.'
print 'Argument List:', str(sys.argv)


casetest = ''
mode = ''
install = 'false'
number=1
nodeStatus={}
nodeResult={}
nodeReady={}

try:
   opts, args = getopt.getopt(sys.argv[1:],"hc:m:i:",["help", "casetest=","mode=","install"])
except getopt.GetoptError:
   print 'error : run.py -c <casetest> -m <mode>'
   sys.exit(2)
for opt, arg in opts:
   if opt in ("-h", "--help"):
      print 'run.py -c <casetest> -m <mode> -i\n mode 0: JS to JS \n mode 1: JS to Android \n mode 2: Android to Android \n install:will re-install test application use the latest one, without this tag , we will not install package.'
      sys.exit()
   elif opt == "-c":
      casetest = arg
      print"getcaselist"
   elif opt == '-m':
      mode = arg
      print "getmode"
   elif opt == '-i':
      install = arg
   else:
      assert False, "unhandled option"
if casetest == ''or mode == '':
   print 'Error !!! \n Please use: run.py -c <casetest> -m <mode> \n mode 0: JS to JS \n mode 1: JS to Android \n mode 2: Android to Android'
   sys.exit()


print 'casetest is:', casetest
print 'mode is:', mode
socketServer = Config.getConfig(Keys.SOCKET_SERVER)
socketServerPort = Config.getConfig(Keys.SOCKET_SERVER_PORT)
socketServerPort_control = Config.getConfig(Keys.SOCKET_SERVER_PORT_control)
print "socketServer is " ,socketServer
print "socketServerPort is ",socketServerPort

socketIO_control = SocketIO(str(socketServer), int(socketServerPort_control),wait_for_connection=False)
socketIO_action = SocketIO(str(socketServer), int(socketServerPort),wait_for_connection=False)


#emitmessagetocontrolserver("controlevent",{"lock":"connect_server"})
def start_test(casetest, mode):
    target = open("TestResult.txt", 'a');
    jsresultParse = JSResultParse();
    caseinfo=split_line(casetest);
    print "case is", caseinfo[0];
    print "classname is", caseinfo[1];
    emitmessagetocontrolserver("controlevent",{"lock":"start_test_"+caseinfo[0]})
    ########################################################################################
    # Local side is js, remote side is js #
    # Local side android is client2#
    # Remote side js is client1 #
    ########################################################################################
    if int(mode) == 0:
      print "start test js to js "  #  begining js to js test
      print "node connection start"
      #ssh connect to difference node and start node.py
      deployNode=DeployNode.connect_node(Config.getConfig(Keys.NODE1_ADDR),Config.getConfig(Keys.NODE1_USER),Config.getConfig(Keys.NODE1_PASSD),Config.getConfig(Keys.NODE1_WORKFOLDER1),"node1", "jsp2p_1",caseinfo[0])
      #wait node is connect
      isconnect = waitnodeconnect(int(Config.getConfig(Keys.WAITNODECONNECTTIME)))
      if not isconnect:
        target.write("JS-JS case: "+caseinfo[0]+": fail , failreason is: can not receive node1 is not connect");
        target.write('\n');
      else:
        #run test start
        print 'All node connected to Server'
        emitmessagetocontrolserver("controlevent",{"lock":"beginTest/"+caseinfo[0]+"/"+caseinfo[2]})
        # should adjust beginTest is resolved
        # start local testing or new node #
        deployjs2=Deploy.deploy_js("testclient2.conf.js","P2P")
        #listen node result
        if (deployjs2 == 0):
          isready = waitnodeready(30)
          if not isready:
            target.write("Android-JS case: "+caseinfo[0]+": fail , failreason is: node is not ready");
            target.write('\n');
            return
          emitmessagetocontrolserver("controlevent",{"lock":"STARTTEST"})
          emitmessage("lockevent",{"lock":"STARTTEST"})
          startjs2=Deploy.start_js("testclient2.conf.js",caseinfo[0],"P2P")
          print "startjs2 PID is: ", startjs2      
          waitProcess(10,startjs2,False)
          print_ts("test end")
          #check and wait node result
          waitnoderesult(int(Config.getConfig(Keys.WAITNODERESULTTIME)))
          #chack result and write to test
          case2result=jsresultParse.parseJSResult("test-results-client2.xml","P2P")
          print "case2result is ", case2result;
          jsresultParse.copyJSResult("test-results-client2.xml", caseinfo[0]+'_2',"P2P")
          #write reslut
          writeResult("JS-JS",target,case2result,caseinfo[0])
        else:
          print("startBrowser error: ");
      deployNode.close
    ########################################################################################
    # Local side is android, remote side is js #
    # Local side android is client2#
    # Remote side js is client1 #
    ########################################################################################
    elif int(mode) == 1:
      print "local side is  Android"
      print "node connection start"
      androidTestDevices=getAndroidDevice.get_devices();
      print androidTestDevices
      if len(androidTestDevices) <1:
        print "this computer has not android device"
        target.write("Android-JS case: "+caseinfo[0]+": fail , failreason is: no android device");
        target.write('\n');
        return
      if install == 'true':
        deployAndroid=Deploy.deploy_android(androidTestDevices[0],"P2P")
        if deployAndroid ==1 :
          target.write("Android-JS case: "+caseinfo[0]+": fail , failreason is: android install apk failed");
          target.write('\n');
          return
      #ssh connect to difference node and start node.py
      deployNode=DeployNode.connect_node(Config.getConfig(Keys.NODE1_ADDR),Config.getConfig(Keys.NODE1_USER),Config.getConfig(Keys.NODE1_PASSD),Config.getConfig(Keys.NODE1_WORKFOLDER1),"node1", "jsp2p_1",caseinfo[0])
      #wait node is connect
      isconnect = waitnodeconnect(int(Config.getConfig(Keys.WAITNODECONNECTTIME)))
      if not isconnect:
        target.write("Android-JS case: "+caseinfo[0]+": fail , failreason is: can not receive node1 is not connect");
        target.write('\n');
      else:
        print 'All node connected to Server'
        emitmessagetocontrolserver("controlevent",{"lock":"beginTest/"+caseinfo[0]+"/"+caseinfo[2]})
        isready = waitnodeready(100)
        if not isready:
          target.write("Android-JS case: "+caseinfo[0]+": fail , failreason is: node is not ready");
          target.write('\n');
          return
        #wait node result
        emitmessagetocontrolserver("controlevent",{"lock":"STARTTEST"})
        emitmessage("lockevent",{"lock":"STARTTEST"})
        startAndorid=Deploy.start_android_sync(androidTestDevices[0],caseinfo[0],caseinfo[2],"P2P")
        waitProcess(10,startAndorid,False)
        print "test end"

        waitnoderesult(int(Config.getConfig(Keys.WAITNODERESULTTIME))*2)
        # check test result
        AndroidResult = getAndroidDevice.read_caselist(caseinfo[2],caseinfo[0],"P2P");
        #write result
        writeResult("Android-js",target,AndroidResult,caseinfo[0])
      deployNode.close
    ########################################################################################
    # Local side is android, remote side is iOS #
    # Local android is  is client1#
    # Remote side  iOS is client2 #
    ########################################################################################
    elif int(mode) == 2:
      print_ts("start_test")
      useDevices = 0
      androidTestDevices=getAndroidDevice.get_devices();
      print androidTestDevices
      issamenode = util.getcomputerIp(Config.getConfig(Keys.NODE1_ADDR))
      if issamenode:
        if len(androidTestDevices) <2:
          print "android devices must have two devices"
          target.write("Android-Android case: "+caseinfo[0]+": fail , failreason is: android devices must have two devices");
          target.write('\n');
          return
        else:
          useDevices =1
      print_ts("run test")
      print "local side is  Android"
      print "node connection start"
      if issamenode:
        deployNode=DeployNode.connect_node_withAndroid(Config.getConfig(Keys.NODE1_ADDR),Config.getConfig(Keys.NODE1_USER),Config.getConfig(Keys.NODE1_PASSD),Config.getConfig(Keys.NODE1_WORKFOLDER1),"node1", "androidp2p_1",caseinfo[0],androidTestDevices[0],install)
      else:
        deployNode=DeployNode.connect_node_withAndroid(Config.getConfig(Keys.NODE1_ADDR),Config.getConfig(Keys.NODE1_USER),Config.getConfig(Keys.NODE1_PASSD),Config.getConfig(Keys.NODE1_WORKFOLDER1),"node1", "androidp2p_1",caseinfo[0],install=install)

      print_ts("wait node connect")
      isconnect = waitnodeconnect(int(Config.getConfig(Keys.WAITNODECONNECTTIME)))
      if not isconnect:
        target.write("Android-Android case: "+caseinfo[0]+": fail , failreason is: can not receive node1 is not connect");
        target.write('\n');
      else:
        print 'All node connected to Server'
        emitmessagetocontrolserver("controlevent",{"lock":"beginTest/"+caseinfo[0]+"/"+caseinfo[2]})
        if install == 'true':
          deployAndroid=Deploy.deploy_android(androidTestDevices[useDevices],"P2P")
        #wait noded is ready
        isready = waitnodeready(150)
        if not isready:
          target.write("Android-Android case: "+caseinfo[0]+": fail , failreason is: node is not ready");
          target.write('\n');
          return
        #listen node result
        emitmessagetocontrolserver("controlevent",{"lock":"STARTTEST"})
        emitmessage("lockevent",{"lock":"STARTTEST"})
        startAndorid=Deploy.start_android_sync(androidTestDevices[useDevices],caseinfo[0],caseinfo[1],"P2P")
        waitProcess(10,startAndorid,False)

        waitnoderesult(int(Config.getConfig(Keys.WAITNODERESULTTIME)))

        AndroidResult = getAndroidDevice.read_caselist(caseinfo[1],caseinfo[0],"P2P");
        #write result
        writeResult("Android-Android",target,AndroidResult,caseinfo[0])

      deployNode.close
    #########################################################################################
    # Local is JS , remote is iOS #
    # Local android is  is client1 #
    # Remote side JS is client2 #
    #############################################################################################          
    elif int(mode) == 3:
        print "start test js to iOS"  #  begining js to js test
        print "node connection start"
        #ssh connect to difference node and start node.py
        deployNode=DeployNode.connect_node(Config.getConfig(Keys.NODE2_ADDR),Config.getConfig(Keys.NODE2_USER),Config.getConfig(Keys.NODE2_PASSD),Config.getConfig(Keys.NODE2_WORKFOLDER1),"node1", "iosp2p_1",caseinfo[0])
        isconnect = waitnodeconnect(int(Config.getConfig(Keys.WAITNODECONNECTTIME)))
        if not isconnect:
          target.write("JS-IOS case: "+caseinfo[0]+": fail , failreason is: can not receive node1 is not connect");
          target.write('\n');
        else:
          print 'All node connected to Server'
          emitmessagetocontrolserver("controlevent",{"lock":"beginTest/"+caseinfo[0]+"/"+caseinfo[2]})
          # start local testing or new node #
          deployjs2=Deploy.deploy_js("testclient1.conf.js","P2P")
          if (deployjs2 == 0):
            time.sleep(25)
            emitmessagetocontrolserver("controlevent",{"lock":"STARTTEST"})
            emitmessage("lockevent",{"lock":"STARTTEST"})
            startjs2=Deploy.start_js("testclient1.conf.js",caseinfo[0],"P2P")

            print "startjs2 PID is: ", startjs2
            waitProcess(10,startjs2,False)

            waitnoderesult(int(Config.getConfig(Keys.WAITNODERESULTTIME)))

            case2result=jsresultParse.parseJSResult("test-results-client1.xml","P2P")
            print "case2result is ", case2result;
            jsresultParse.copyJSResult("test-results-client1.xml", caseinfo[0]+'_1',"P2P")
            # write result
            writeResult("JS-IOS",target,case2result,caseinfo[0])
          else:
            print("startBrowser error: ");
        deployNode.close

    #########################################################################################
    # Local is JS , remote is iOS #
    # Local android is  is client1 #
    # Remote side JS is client2 #
    #############################################################################################          
    elif int(mode) == 4:
        print "start test android to iOS"  #  begining js to js test
        androidTestDevices=getAndroidDevice.get_devices();
        print androidTestDevices
        if len(androidTestDevices) <1:
          print "this computer has not android device"
          target.write("Android-Android case: "+caseinfo[0]+": fail , failreason is: no android device");
          target.write('\n');
          return
        if install == 'true':
          deployAndroid=Deploy.deploy_android(androidTestDevices[0],"P2P")
        print "node connection start"
        #ssh connect to difference node and start node.py
        deployNode=DeployNode.connect_node(Config.getConfig(Keys.NODE2_ADDR),Config.getConfig(Keys.NODE2_USER),Config.getConfig(Keys.NODE2_PASSD),Config.getConfig(Keys.NODE2_WORKFOLDER1),"node1", "iosp2p_1",caseinfo[0])
        isconnect = waitnodeconnect(int(Config.getConfig(Keys.WAITNODECONNECTTIME)))
        if not isconnect:
          target.write("Android-IOS case: "+caseinfo[0]+": fail , failreason is: can not receive node1 is not connect");
          target.write('\n');
        else:
          print 'All node connected to Server'
          emitmessagetocontrolserver("controlevent",{"lock":"beginTest/"+caseinfo[0]+"/"+caseinfo[2]})
          # start local testing or new node #
          time.sleep(25)
          emitmessagetocontrolserver("controlevent",{"lock":"STARTTEST"})
          emitmessage("lockevent",{"lock":"STARTTEST"})
          startAndorid=Deploy.start_android_sync(androidTestDevices[0],caseinfo[0],caseinfo[1],"P2P")

          waitProcess(10,startAndorid,False)

          waitnoderesult(int(Config.getConfig(Keys.WAITNODERESULTTIME)))

          AndroidResult = getAndroidDevice.read_caselist(caseinfo[1],caseinfo[0],"P2P");

          #write result
          writeResult("Android-IOS",target,AndroidResult,caseinfo[0])
        deployNode.close
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
        emitmessagetocontrolserver("controlevent",{"lock":"beginTest/"+caseinfo[0]+"/"+caseinfo[2]})
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
        emitmessage("lockevent",{"lock":"Init_action_Lock"})
        emitmessagetocontrolserver("controlevent",{"lock":"Init_control_Lock"})
    ###########################################################################################
    ###########################################################################################
    # close result file #
    ###########################################################################################

    target.close()



def ThreadWaitLock(n):
    global nodeResult
    try:
      while n > 0:
        print('wait-T-minus', n)
        socketIO_control.on('waitcontrollock', waitingLockCallBack); 
        if len(nodeResult.keys()) == 1:
          print "receive node result"
          break
        else:
          socketIO_control.wait(seconds=2)
        n-=1
    except Exception,e:
      print Exception,":",e

def waitingLockCallBack(*args):
    global nodeStatus
    global nodeReady
    global nodeResult
    lockValue = args[0]['lock']
    if re.search("pass",lockValue) != None or re.search("fail",lockValue) != None :
      print_ts("node test end")
      nodeName, nodeAction = lockValue.split("_");
      nodeResult[nodeName] = nodeAction
      print "nodeName is ", nodeName
      print "nodeAction is ",nodeAction
      print "nodeResult is ", nodeResult[nodeName]
    elif re.search("connected",lockValue) != None:
      nodeName, nodeAction = lockValue.split("_");
      nodeStatus[nodeName] = nodeAction
      print "nodeName is ", nodeName
      print "nodeAction is ",nodeAction
      print "nodeStatus is ", nodeStatus[nodeName]
    elif re.search("ready",lockValue) != None:
      print "ready"
      nodeName, nodeAction = lockValue.split("_");
      nodeReady[nodeName] = nodeAction
      print "nodeName is ", nodeName
      print "nodeAction is ",nodeAction
      print "nodeReady is ", nodeReady[nodeName]
    return 0

def waitnodeconnect(n):
    global nodeStatus
    flag = False
    while n>0:
      if len(nodeStatus.keys()) == 1:
          print "node is connect"
          flag = True
          break
      time.sleep(1)
      n-=1
    return flag

def waitnoderesult(n):
    global nodeResult
    while n>0:
      if len(nodeResult.keys()) == 1:
        break
      time.sleep(1)
      n-=1


def waitnodeready(n):
    global nodeReady
    flag = False
    while n>0:
      if len(nodeReady.keys()) == 1:
        print "node is ready"
        flag = True
        break
      time.sleep(1)
      n-=1
    return flag


def split_line(text):
    return text.split("/")

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
            time.sleep(1)
            break
        else:
          break
      number=number+1


def writeResult(tag,target,localresult,casename):
  global nodeResult
  length_dict = len(nodeResult.keys())
  print "-------------------:" + str(length_dict)
  if (localresult == 0):
      if length_dict == 1:
        if (nodeResult["node1"]) == "pass":
          target.write(tag+": "+casename+": pass");
          target.write('\n');
          print tag+": ",casename,": pass";
        else:
            target.write(tag+": "+casename+": fail");
            target.write('\n');
            print tag+": ",casename,": fail"
      else:
        target.write(tag+": "+casename+": fail,failreason is : node1 not receive result");
        target.write('\n');
  else:
      print tag+": ",casename,": fail"
      target.write(tag+": "+casename+" : fail");
      target.write('\n');



t = Thread(target=ThreadWaitLock,args=(300,))
t.daemon = True
t.start()

start_test(casetest,mode)
#test#
#if __name__ == "__main__":
#    start_test(caselist,0)

