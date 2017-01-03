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
import com.util as util
from threading import Thread

print 'Number of arguments:', len(sys.argv), 'arguments.'
print 'Argument List:', str(sys.argv)

casetest = ''
mode = ''
install = ''
number=1
connectedNode = False
connectedNodeNumber = 0;
nodeStatus={};
nodeResult={};
deployAndroid='';
remoteConnect='';
threadflag = True
try:
   opts, args = getopt.getopt(sys.argv[1:],"h:c:m:i",["help", "casetest=","mode=","install"])
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
      install = 'true'
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
socketIO_control = SocketIO(str(socketServer), int(socketServerPort_control))
socketIO_action = SocketIO(str(socketServer), int(socketServerPort))
#emitmessagetocontrolserver("controlevent",{"lock":"connect_server"})
def start_test(casetest, mode):
    global deployAndroid
    global nodeStatus
    global nodeResult
    global remoteConnect
    target = open("TestResult.txt", 'a');
    jsresultParse = JSResultParse();
    caseinfo=split_line(casetest);
    print "case is", caseinfo[0];
    print "classname is", caseinfo[1];
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
        
      t = Thread(target=MyThreadWaitmessage,args=(10,))
      t.daemon = True
      t.start()
      i = 40
      while i>0:
        if len(nodeStatus.keys()) == 1:
            print "node is connect"
            break
        time.sleep(1)
        i-=1

      length_connectedNode = len(nodeStatus.keys())
      print length_connectedNode
      if length_connectedNode < 1:
        target.write("JS-JS case: "+caseinfo[0]+": fail , failreason is: can not receive node1 message");
        target.write('\n');
      else:
        print('All node connected to Server')
        emitmessagetocontrolserver("controlevent",{"lock":"beginTest/"+caseinfo[0]+"/"+caseinfo[2]})
        # should adjust beginTest is resolved 
        #emitmessagetocontrolserver("controlevent",{"lock":"STARTTEST"})
        # start local testing or new node #
        deployjs2=Deploy.deploy_js("testclient2.conf.js","P2P")
        if (deployjs2 == 0):
          emitmessagetocontrolserver("controlevent",{"lock":"STARTTEST"})
          emitmessage("lockevent",{"lock":"STARTTEST"})
          t2 = Thread(target=WaitingPeerResult,args=(100,))
          t2.daemon = True
          t2.start()
          startjs2=Deploy.start_js("testclient2.conf.js",caseinfo[0],"P2P")

          print "startjs2 PID is: ", startjs2
          waitProcess(10,startjs2,"")
          print "test end"
          i = 100
          while i>0:
            if len(nodeResult.keys()) == 1:
                break
            time.sleep(1)
            i-=1

          case2result=jsresultParse.parseJSResult("test-results-client2.xml","P2P")
          print "case2result is ", case2result;
          jsresultParse.copyJSResult("test-results-client2.xml", caseinfo[0]+'_2',"P2P")
          
          if(case2result == 0):
            length_dict = len(nodeResult.keys())
            print length_dict
            if length_dict == 1:
              if (nodeResult["node1"]) == "pass":
                target.write("JS-JS case: "+caseinfo[0]+": pass");
                target.write('\n');
                print "JS-JS case: ",caseinfo[0],": pass";
              else:
                  target.write("JS-JS case: "+caseinfo[0]+": fail , failreason is: node1 test fasle");
                  target.write('\n');
                  print "JS-JS case: ",caseinfo[0],": fail"
            else:
              target.write("JS-Android case: "+caseinfo[0]+": fali ,failreason is : node1 not receive result");
              target.write('\n');
          else:
            reason =''
            if case2result == 1:
              reason = 'peer2 is fail'
            else:
              reason = 'can not read TestResult.xml'
            target.write("JS-JS case: "+caseinfo[0]+": fail , failreason is: "+ reason);
            target.write('\n');
            print "JS-JS case: ",caseinfo[0],": fail"
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
        target.write("Android-Android case: "+caseinfo[0]+": fail , failreason is: no android device");
        target.write('\n');
        return
      #ssh connect to difference node and start node.py
      deployNode=DeployNode.connect_node(Config.getConfig(Keys.NODE1_ADDR),Config.getConfig(Keys.NODE1_USER),Config.getConfig(Keys.NODE1_PASSD),Config.getConfig(Keys.NODE1_WORKFOLDER1),"node1", "jsp2p_1",caseinfo[0])
      t = Thread(target=MyThreadWaitmessage,args=(10,))
      t.daemon = True
      t.start()
      i = 40
      while i>0:
        if len(nodeStatus.keys()) == 1:
          print "node is connect"
          break
        time.sleep(1)
        i-=1

      length_connectedNode = len(nodeStatus.keys())
      print length_connectedNode
      if length_connectedNode < 1:
        target.write("JS-Android case: "+caseinfo[0]+": fail , failreason is: can not receive node1 message");
        target.write('\n');
      else:
        print('All node connected to Server')
        emitmessagetocontrolserver("controlevent",{"lock":"beginTest/"+caseinfo[0]+"/"+caseinfo[2]})
        deployAndroid = 0
        if (deployAndroid != 0):
          deployAndroid=Deploy.deploy_android(androidTestDevices[0],"P2P")
        if (deployAndroid == 0):
          emitmessagetocontrolserver("controlevent",{"lock":"STARTTEST"})
          startAndorid=Deploy.start_android_sync(androidTestDevices[0],caseinfo[0],caseinfo[2],"P2P")
          waitProcess(10,startAndorid,'')
          print "test end"
          t2 = Thread(target=WaitingPeerResult,args=(100,))
          t2.daemon = True
          t2.start()
          i = 100
          while i>0:
            if len(nodeResult.keys()) == 1:
              break
            time.sleep(1)
            i-=1

          AndroidResult = getAndroidDevice.read_caselist(caseinfo[2],caseinfo[0],"P2P");
          if (AndroidResult == 0):
            length_dict = len(nodeResult.keys()) 
            if length_dict == 1:
              print "node1 is receive"
              if (nodeResult["node1"]) == "pass":
                target.write("JS-Android case: "+caseinfo[0]+": pass");
                target.write('\n');
                print "JS-Android case: ",caseinfo[0],": pass";
              else:
                target.write("JS-Android case: "+caseinfo[0]+": fail");
                target.write('\n');
                print "JS-Android case: ",caseinfo[0],": fail"
            else:
              target.write("JS-Android case: "+caseinfo[0]+": fali ,failreason is : node1 not receive result");
              target.write('\n');
          else:
            print "JS-Android case: ",caseinfo[0],": fail"
            target.write("JS-Android case: "+caseinfo[0]+" : fail,failreason is : Android test fail");
            target.write('\n');
      deployNode.close
    ########################################################################################
    # Local side is android, remote side is iOS #
    # Local android is  is client1#
    # Remote side  iOS is client2 #
    ########################################################################################
    elif int(mode) == 2:
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

      print "local side is  Android"
      print "node connection start"
      deployNode=DeployNode.connect_node(Config.getConfig(Keys.NODE1_ADDR),Config.getConfig(Keys.NODE1_USER),Config.getConfig(Keys.NODE1_PASSD),Config.getConfig(Keys.NODE1_WORKFOLDER1),"node1", "androidp2p_1",caseinfo[0])
      # remoteConnect = 1
      t = Thread(target=MyThreadWaitmessage,args=(10,))
      t.daemon = True
      t.start()
      i = 40
      while i>0:
        if len(nodeStatus.keys()) == 1:
          print "node is connect"
          break
        time.sleep(1)
        i-=1

      length_connectedNode = len(nodeStatus.keys())
      print length_connectedNode
      if length_connectedNode < 1:
        target.write("Android-Android case: "+caseinfo[0]+": fail , failreason is: can not receive node1 message");
        target.write('\n');
      else:
        print('All node connected to Server')
        emitmessagetocontrolserver("controlevent",{"lock":"beginTest/"+caseinfo[0]+"/"+caseinfo[2]})
        deployAndroid = 0
        if (deployAndroid != 0):
          deployAndroid=Deploy.deploy_android(androidTestDevices[useDevices],"P2P")
        if (deployAndroid == 0):
          emitmessagetocontrolserver("controlevent",{"lock":"STARTTEST"})
          startAndorid=Deploy.start_android_sync(androidTestDevices[useDevices],caseinfo[0],caseinfo[1],"P2P")
          waitProcess(10,startAndorid,'')
          t2 = Thread(target=WaitingPeerResult,args=(100,))
          t2.daemon = True
          t2.start()
          i = 100
          while i>0:
            if len(nodeResult.keys()) == 1:
              break
            time.sleep(1)
            i-=1

          AndroidResult = getAndroidDevice.read_caselist(caseinfo[1],caseinfo[0],"P2P");
          if (AndroidResult == 0):
            length_dict = len(nodeResult.keys())
            if length_dict == 1:
              if (nodeResult["node1"]) == "pass":
                target.write("Android-Android case: "+caseinfo[0]+": pass");
                target.write('\n');
                print "Android-Android case: ",caseinfo[0],": pass";
              else:
                  target.write("Android-Android case: "+caseinfo[0]+": fail");
                  target.write('\n');
                  print "Android-Android case: ",caseinfo[0],": fail"
            else:
              target.write("Android-Android case: "+caseinfo[0]+": fail,failreason is : node1 not receive result");
              target.write('\n');
          else:
            print "Android-Android case: ",caseinfo[0],": fail"
            target.write("Android-Android case: "+caseinfo[0]+" : fail");
            target.write('\n');
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
          emitmessagetocontrolserver("controlevent",{"lock":"beginTest/"+caseinfo[0]+"/"+caseinfo[2]})
          # should adjust beginTest is resolved 
          #emitmessage("lockevent",{"lock":"STARTTEST"})
          # start local testing or new node #
          deployjs2=Deploy.deploy_js("testclient1.conf.js","P2P")
          if (deployjs2 == 0):
            t2 = Thread(target=WaitingPeerResult,args=(10,))
            t2.start()
            emitmessagetocontrolserver("controlevent",{"lock":"STARTTEST"})
            startjs2=Deploy.start_js("testclient1.conf.js",caseinfo[0],"P2P")

            print "startjs2 PID is: ", startjs2
            waitProcess(10,startjs2,"")

            case2result=jsresultParse.parseJSResult("test-results-client1.xml","P2P")
            print "case2result is ", case2result;
            jsresultParse.copyJSResult("test-results-client1.xml", caseinfo[0]+'_1',"P2P")

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
            emitmessage("lockevent",{"lock":"Init_action_Lock"})
            emitmessagetocontrolserver("controlevent",{"lock":"Init_control_Lock"})
          else:
            print("startBrowser error: "); 
        deployNode.close
        cleanEnv.kill_karmaStart() # only need make sure karma start command is killed.
        emitmessage("lockevent",{"lock":"Init_action_Lock"})
        emitmessagetocontrolserver("controlevent",{"lock":"Init_control_Lock"})
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

def WaitingPeerResult(n):
    print "WaitingPeerResult"
  # this function is start new thread will start monitor node is connected 
    try:
      while n > 0: 
        print('T-minus', n)  
        socketIO_control.on('waitcontrollock', waitingPeerResultCallBack);
        print "waiting ...."      
        if len(nodeResult.keys()) == 1:
          print "receive node result"
          break
        else:
          socketIO_control.wait(seconds=10)
        n-=1
    except:
      pass

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
    try:
      while n > 0:
        print('T-minus', n)
        socketIO_control.on('waitcontrollock', waitMessageCallback);
        print "waiting ...."      
        if len(nodeStatus.keys()) == 1:
          print "node is connect"
          break
        else:
          socketIO_control.wait(seconds=2)
        n-=1
    except:
      pass

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
    socketIO_control.on('waitcontrollock', waitMessageCallback);
    socketIO_control.wait(seconds=5);

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
start_test(casetest,mode)
#test#
#if __name__ == "__main__":
#    start_test(caselist,0)

