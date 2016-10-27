'''
Created on Jan,20 2016

@author: Yanbin
this is used for controller server connect to node server 
'''
import os
import time
import datetime
import sys
import getopt
from config.config import Config
from config.config import ConfigKeys as Keys
import subprocess
import commands
import pxssh

class DeployNode(object):

    @staticmethod
    def connect_node(nodeAddress, nodeUser, nodePassd,projectFolder,nodeMachine, mode): 
        #print AndroidPath
        s = pxssh.pxssh()
        if not s.login (nodeAddress, nodeUser, nodePassd):
           print "SSH session failed on login."
           print str(s)
           return 1
        else:
           print "SSH session login successful"
           s.sendline('cd ' + projectFolder)
           #s.sendline ('cd /Users/neilyou/Documents/webrtc-ios-sdk/test/p2psample')
           s.prompt()
           s.sendline('nohup python ./node.py -n ' + nodeMachine + " -m " + mode + " > log.txt &")
           s.prompt()
           print s.before
           return s           


#test
#if __name__ == '__main__':
#   DeployNode.connect_node("yanbin-12.sh.intel.com","yanbin","yanbin","~/workspace/webrtc-webrtc-qa_new/webrtc-webrtc-qa/InteractiveTestFramework/PythonVersion","node1")
   #deploy = Deploy()
   #result=Deploy.deploy_iOS('/Users/neilyou/Documents/webrtc-ios-sdk/test/p2p_checkin','WoogeenChatTest.xcodeproj','WoogeenChatTest')
   #result.close
   #time.sleep(100);
   #result=Deploy.start_iOS('/Users/neilyou/Documents/webrtc-ios-sdk/test/p2p_checkin','WoogeenChatTest.xcodeproj','WoogeenChatTest','','')
#    result.prompt()
#    result.sendline('cd /Users/neilyou/Documents/webrtc-ios-sdk/test/p2psample')
#    result.prompt()
#    print result.before
#    print result.pid
#    result.sendline('xctool -project WoogeenChatTest.xcodeproj -scheme WoogeenChatTest test')
#    time.sleep(10); 
#    result.prompt()
#    result.expect (pexpect.EOF)
#    print result.before
   # Deploy.start_Android('emulator-5554','test_demo','PeerClientFrameworkTest');

#test
#result=start_js("testclient1.conf.temp.js","test_demo")
#print result