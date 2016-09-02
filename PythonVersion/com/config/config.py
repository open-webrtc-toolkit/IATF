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
class Config(object):
  config = {
            ConfigKeys.KARMA: r"/usr/lib/node_modules/karma/bin/karma",
            ConfigKeys.ADB:r"adb",
            ConfigKeys.JS_CONFIG_FOLDER:r"/home/yanbin/workspace/project/webrtc-javascript-sdk/test/p2pInteractionTest",
            ConfigKeys.ANDROID_CONFIG_FOLDER:r"/home/yanbin/workspace/project/webrtc-android-sdk/test/p2pFrameworkTest",
            ConfigKeys.MAC_ADD:r"webrtctest25.sh.intel.com",
            ConfigKeys.MAC_USER:r"webrtctest25",
            ConfigKeys.MAC_PASSD:r"intel123",
            ConfigKeys.MAC_FOLDER:r"/Users/neilyou/Documents/webrtc-ios-sdk/test/p2p_interactiveTest"
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
