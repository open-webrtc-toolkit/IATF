'''
Created on 6,16, 2016

@author: Yanbin
'''
class ConfigKeys(object):
  KARMA = 0
  ADB = 1
  JS_P2P_CONFIG_FOLDER = 2
  JS_CONFERENCE_CONFIG_FOLDER = 3
  ANDROID_P2P_CONFIG_FOLDER = 4
  ANDROID_CONFERENCE_CONFIG_FOLDER = 5
  MAC_ADD = 6
  MAC_USER = 7
  MAC_PASSD = 8
  MAC_FOLDER = 9
  SOCKET_SERVER = 10
  SOCKET_SERVER_PORT = 11
  SOCKET_SERVER_PORT_control = 12
  NODE1_ADDR = 13
  NODE1_USER = 14
  NODE1_PASSD = 15
  NODE2_ADDR = 16
  NODE2_USER = 17
  NODE2_PASSD = 18
  NODE3_ADDR = 19
  NODE3_USER = 20
  NODE3_PASSD = 21
  NODE1_WORKFOLDER1 = 22
  IOS_P2P_CONFIG_FOLDER = 23
  IOS_CONFERENCE_CONFIG_FOLDER = 24
  IOS_SIMULATOR = 25
  IOS_PHONE = 26
  NODE2_WORKFOLDER1 = 27
  WEBRTC_WEBRTC_QA = 28
  WAITNODERESULTTIME = 29
  WAITNODECONNECTTIME = 30
class Config(object):
  config = {
            ConfigKeys.KARMA: r"/usr/lib/node_modules/karma/bin/karma",
            ConfigKeys.ADB:r"adb",
            ConfigKeys.WEBRTC_WEBRTC_QA:r"/home/fengwei/zyh_qa/0322/webrtc-webrtc-qa/InteractiveTestFramework/PythonVersion",
            ConfigKeys.JS_P2P_CONFIG_FOLDER:r"/home/fengwei/zyh_js/webrtc-javascript-sdk/test/p2pInteractionTest",
            ConfigKeys.JS_CONFERENCE_CONFIG_FOLDER:r"//home/fengwei/zyh_js/webrtc-javascript-sdk/test/mcuJSTestCases/test-api",
            ConfigKeys.ANDROID_P2P_CONFIG_FOLDER:r"/home/fengwei/zyh_android/0317/webrtc-android-sdk/test/p2pFrameworkTest",
            ConfigKeys.ANDROID_CONFERENCE_CONFIG_FOLDER:r"/home/fengwei/zyh_android/0216/webrtc-android-sdk/test/conferenceframeworktest2",
            ConfigKeys.IOS_P2P_CONFIG_FOLDER:r"/Users/webrtctest25/Documents/shijincheng/webrtc-ios-sdk/test/p2p_interactiveTest",
            ConfigKeys.IOS_CONFERENCE_CONFIG_FOLDER:r"/Users/webrtc/workspace/yanbin_work/webrtc-ios-sdk/test/conference_interactiveTest",
            ConfigKeys.IOS_SIMULATOR:r"iphonesimulator9.2",
            ConfigKeys.IOS_PHONE:r"iPhone 6s Plus",
            ConfigKeys.MAC_ADD:r"webrtctest25.sh.intel.com",
            ConfigKeys.MAC_USER:r"webrtctest25",
            ConfigKeys.MAC_PASSD:r"intel123",
            ConfigKeys.MAC_FOLDER:r"/Users/webrtctest25/Documents/shijincheng/webrtc-ios-sdk/test/p2p_interactiveTest",
            ConfigKeys.SOCKET_SERVER:r"10.239.44.74",
            ConfigKeys.SOCKET_SERVER_PORT:r"9092",
            ConfigKeys.SOCKET_SERVER_PORT_control:r"9091",
            ConfigKeys.NODE1_ADDR:r"10.239.44.74",
            ConfigKeys.NODE1_USER:r"fengwei",
            ConfigKeys.NODE1_PASSD:r"intel@123",
            ConfigKeys.NODE1_WORKFOLDER1:r"/home/fengwei/zyh_qa/0322/webrtc-webrtc-qa/InteractiveTestFramework/PythonVersion",
            ConfigKeys.NODE2_ADDR:r"webrtctest25.sh.intel.com",
            ConfigKeys.NODE2_USER:r"webrtctest25",
            ConfigKeys.NODE2_PASSD:r"intel123",
            ConfigKeys.NODE2_WORKFOLDER1:r"/Users/webrtctest25/Documents/shijincheng/webrtc-webrtc-qa/InteractiveTestFramework/PythonVersion",
            ConfigKeys.NODE3_ADDR:r"xxx",
            ConfigKeys.NODE3_USER:r"XXX",
            ConfigKeys.NODE3_PASSD:r"XXX",
            ConfigKeys.WAITNODERESULTTIME: r"40",
            ConfigKeys.WAITNODECONNECTTIME: r"40"
            }
  @staticmethod
  def getConfig(key):
    if key in Config.config.keys():
      return Config.config[key]
    else:
      return None
  @staticmethod
  def setConfig(key, value):
    raise Exception("Not Implement")
