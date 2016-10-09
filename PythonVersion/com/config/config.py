'''
Created on 6,16, 2016

@author: Yanbin
'''
class ConfigKeys(object):
  KARMA = 0
  ADB = 1
  JS_CONFIG_FOLDER = 2
  ANDROID_CONFIG_FOLDER = 3
  MAC_ADD = 4
  MAC_USER = 5
  MAC_PASSD = 6
  MAC_FOLDER = 7
  SOCKET_SERVER = 8
  SOCKET_SERVER_PORT = 9
class Config(object):
  config = {
            ConfigKeys.KARMA: r"/usr/lib/node_modules/karma/bin/karma",
            ConfigKeys.ADB:r"adb",
            ConfigKeys.JS_CONFIG_FOLDER:r"/home/yanbin/workspace/project/webrtc-javascript-sdk/test/p2pInteractionTest",
            ConfigKeys.ANDROID_CONFIG_FOLDER:r"/home/yanbin/workspace/project/webrtc-android-sdk/test/p2pFrameworkTest",
            ConfigKeys.MAC_ADD:r"10.239.158.143",
            ConfigKeys.MAC_USER:r"webrtc",
            ConfigKeys.MAC_PASSD:r"intel123",
            ConfigKeys.MAC_FOLDER:r"/Users/webrtc/workspace/yanbin_work/webrtc-ios-sdk/test/p2p_interactiveTest",
            ConfigKeys.SOCKET_SERVER:r"10.239.10.102",
            ConfigKeys.SOCKET_SERVER_PORT:r"9092"
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
