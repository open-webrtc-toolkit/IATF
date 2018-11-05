'''
Created on Jan,20 2016

@author: Yanbin
'''
import os
import time
import datetime
import sys
import getopt
import subprocess
import commands
import pxssh

class Deploy(object):

    def deploy_iOS(self):
        try:
          s = pxssh.pxssh()
          s.login ("10.239.158.129", "webrtc", "intel123")
          print "SSH session login successful"
          s.sendline('cd /Users/webrtc/workspace/yanbin_work/webrtc-ios-sdk/test/p2p_interactiveTest_sockettest')
             #s.sendline ('cd /Users/neilyou/Documents/webrtc-ios-sdk/test/p2psample')
          s.prompt()
          s.sendline('xctool -project WoogeenChatTest.xcodeproj -scheme WoogeenChatTest build-tests')
          s.logfile = sys.stdout
          
         # print s.before
          print s.logfile
          s.prompt()
          return s
        except pxssh.ExceptionPxssh as e:
          print ("%^^^^^^^^^^^^^^^)$E&FFJUGVBVHBB")
          print (e)
#test
if __name__ == '__main__':
   deploy = Deploy()
   result=deploy.deploy_iOS()
   #print result
   result.close
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
