ant clean debug
adb install -r bin/TestFrameworkTest-debug.apk
adb shell am instrument -r -e class com.intel.webrtc.test.android.AndroidTestEntry#testEntry -w com.intel.webrtc.test.demo/android.test.InstrumentationTestRunner

