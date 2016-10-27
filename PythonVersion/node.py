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
from threading import Thread
print 'Number of arguments:', len(sys.argv), 'arguments.'
print 'Argument List:', str(sys.argv)

nodeName = ''
connected = False;
begin = False;
currentCase = ''
number = 0
try:
   opts, args = getopt.getopt(sys.argv[1:],"h:m:n:i",["help", "m=","n=","install"])
except getopt.GetoptError:
   print 'error : node.py -m <mode> -n <node>'
   sys.exit(2)
for opt, arg in opts:
   if opt in ("-h", "--help"):
      print 'node.py -m <mode> -n <node>'
      sys.exit()
   elif opt == "-n":
      nodeName = arg
      print"nodeName is", nodeName
   elif opt == '-m':
      mode = arg
      print "getmode"
   elif opt == '-i':
      install = 'true'
   else:
      assert False, "unhandled option"
if nodeName == '':
   print 'Error !!! '
   sys.exit()

socketServer = Config.getConfig(Keys.SOCKET_SERVER)
socketServerPort = Config.getConfig(Keys.SOCKET_SERVER_PORT)
print "socketServer is " ,socketServer
print "socketServerPort is ",socketServerPort
socketIO = SocketIO(str(socketServer), int(socketServerPort))


    
def emitmessage(message,data):
    print "emitmessage"
    socketIO.emit(message,data)
    return 0 

def start_node():
    socket_connect()
    socketIO.wait(seconds=5)
    print "connected is", connected;
    n=10
    if(connected == True):
      print "start test node"
      emitmessage("lockevent",{"lock": nodeName+"_connected"})
      while n > 0:
        print('T-minus', n)    
        socketIO.on('lockevent', waitMessageCallback);
        print "waiting ...."  
        if begin == True :
          break;
        else:        
          n-=1
          socketIO.wait(seconds=10)
      if begin == True:
         if int(mode) == 0:
            print "start test JS to JS"
            deployjs1=Deploy.deploy_js("testclient1.conf.js","P2P")         
            if (deployjs1 == 0):
              emitmessage("lockevent",{"lock":"STARTTEST"})
              startjs1=Deploy.start_js("testclient1.conf.js",currentCase,"P2P")
              print "startjs1 PID is: ", startjs1
              waitProcess(10, startjs1,'')
              case1result=JSResultParse.parseJSResult("test-results-client1.xml","P2P")
              print "case1result is ", case1result;
              JSResultParse.copyJSResult("test-results-client1.xml", currentCase+'_1',"P2P")
              if (case1result == 0):
                 emitmessage("lockevent",{"lock":nodeName+"_pass"})
              else:
                emitmessage("lockevent",{"lock":nodeName+"_fail"})
              cleanEnv.kill_karmaStart() # only need make sure karma start command is killed.
              emitmessage("lockevent",{"lock":"node1_finished"})
            else:
              print("startBrowser error: ");
def socket_connect():
    socketIO.on('connect', on_aaa_response)
    return socketIO;


def split_line(text):
    casenumber, casename = text.split("=")
    if casename:
       caseInfoList = casename.split(";");
       caseInfoList[0] = caseInfoList[0].replace("\"", "");
       caseInfoList[-1] = caseInfoList[-1].replace("\"", "");
       return caseInfoList

def on_aaa_response(*args):
    print('connected', args)
    global connected
    connected = True;
    
def node_begin_test(n):
  # this function is start new thread will start monitor node is connected 
    while n > 0:
      print('T-minus', n)    
      socketIO.on('lockevent', waitMessageCallback);
      print "waiting ...."  
      if begin == True :

         break;
      else:        
        n-=1
        socketIO.wait(seconds=10)
def waitMessageCallback(*args):
        lockValue = args[0]['lock']
        if re.search("beginTest",lockValue) != None:
          nodeBegin, caseFile = lockValue.split("/");
          print "lockValue is ", lockValue;
          global begin
          begin = True;
          global currentCase
          print "currentCase is ", currentCase
          currentCase = caseFile
          return 0 


   # socketIO.emit("lockevent",{"lock":"STARTTEST"})

    
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
start_node()
#test#
#if __name__ == "__main__":
#    start_test(caselist,0)

