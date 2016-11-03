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
  NODE1_ADDR = 12
  NODE1_USER = 13
  NODE1_PASSD = 14
  NODE2_ADDR = 15
  NODE2_USER = 16
  NODE2_PASSD = 17
  NODE3_ADDR = 18
  NODE3_USER = 19
  NODE3_PASSD = 20
  NODE1_WORKFOLDER1 = 21
  IOS_P2P_CONFIG_FOLDER = 22
  IOS_CONFERENCE_CONFIG_FOLDER = 23
  IOS_SIMULATOR = 24
  IOS_PHONE = 25
  NODE2_WORKFOLDER1 = 26
class Config(object):
  config = {
            ConfigKeys.KARMA: r"/usr/lib/node_modules/karma/bin/karma",
            ConfigKeys.ADB:r"adb",
            ConfigKeys.JS_P2P_CONFIG_FOLDER:r"/home/yanbin/workspace/project/webrtc-javascript-sdk/test/p2pInteractionTest",
            ConfigKeys.JS_CONFERENCE_CONFIG_FOLDER:r"/home/yanbin/workspace/project/webrtc-javascript-sdk/test/mcuJSTestCases/test-api",
            ConfigKeys.ANDROID_P2P_CONFIG_FOLDER:r"/home/yanbin/workspace/project/webrtc-android-sdk/test/p2pFrameworkTest",
            ConfigKeys.ANDROID_CONFERENCE_CONFIG_FOLDER:r"/home/yanbin/workspace/project/webrtc-android-sdk/test/conferenceframeworktest2",
            ConfigKeys.IOS_P2P_CONFIG_FOLDER:r"/Users/webrtc/workspace/yanbin_work/webrtc-ios-sdk/test/p2p_interactiveTest",
            ConfigKeys.IOS_CONFERENCE_CONFIG_FOLDER:r"/Users/webrtc/workspace/yanbin_work/webrtc-ios-sdk/test/conference_interactiveTest",
            ConfigKeys.IOS_SIMULATOR:r"iphonesimulator9.2",
            ConfigKeys.IOS_PHONE:r"iPhone 6s Plus",
            ConfigKeys.MAC_ADD:r"10.239.158.146",
            ConfigKeys.MAC_USER:r"yanbin",
            ConfigKeys.MAC_PASSD:r"yanbin",
            ConfigKeys.MAC_FOLDER:r"/Users/webrtc/workspace/yanbin_work/webrtc-ios-sdk/test/p2p_interactiveTest",
            ConfigKeys.SOCKET_SERVER:r"10.239.10.102",
            ConfigKeys.SOCKET_SERVER_PORT:r"9092",
            ConfigKeys.NODE1_ADDR:r"yanbin-12.sh.intel.com",
            ConfigKeys.NODE1_USER:r"yanbin",
            ConfigKeys.NODE1_PASSD:r"yanbin",
            ConfigKeys.NODE1_WORKFOLDER1:r"~/workspace/webrtc-webrtc-qa_new/webrtc-webrtc-qa/InteractiveTestFramework/PythonVersion",
            ConfigKeys.NODE2_ADDR:r"10.239.158.146",
            ConfigKeys.NODE2_USER:r"webrtc",
            ConfigKeys.NODE2_PASSD:r"intel123",
            ConfigKeys.NODE2_WORKFOLDER1:r"/Users/webrtc/workspace/yanbin_work/webrtc-webrtc-qa/InteractiveTestFramework/PythonVersion",
            ConfigKeys.NODE3_ADDR:r"xxx",
            ConfigKeys.NODE3_USER:r"XXX",
            ConfigKeys.NODE3_PASSD:r"XXX"
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
