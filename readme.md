# IATF - Interactive API Testing Framework

## Introduction

IATF is a test framework handles cross-platform and multi-device API test automation.

Links: [Video](https://youtu.be/mHJspt6BgZU?list=PLSIUOFhnxEiAeGHYoBZCvEMY5wCOIpyOM), [Slides](https://docs.google.com/presentation/d/1iVf-TogkdoIcvs8OpRMMWx76s9Zk4_f0JJ-e1sZIxog/edit#slide=id.p490)

Different from traditional test tools, the test roles can be deployed under different OSes and different platforms.

The advantages of this framework are as followings:

- **Cross-platform**: In particular occasions, we have to test on devices under different platforms and they may have a sequence of interactive operations. Though we can employee mock things to do the testing, it cannot reflect the real circumstance.
- **Minimize Code Labor**: The test code is divided into test roles and combined into test case according to the test method name. So the code can be reused event other roles in this test case changes to another platform.
- **Wait-Notify Mechanism**: In order to control the execution order under different platforms, we implement a cross-platform lock events and wait-notify mechanism.

## Architecture

The following figure shows the architecture of this framework.
![Architecture of IATF](docs/images/arch.jpg)

### Logical Structure

1. **Offline Detection**: Employ heartbeat mechanism to detect whether the test devices are offline.
    - HeartBeatRecorder: Manage the received heart beat messages from all test clients automatically.
    - HeartBeatReceiver: Run on distributed test node, collect heartbeat from test device and send to test server.
    - HeartBeatSender: Run on test device, Start a thread that regularly sends heart beat messages to server.
2. **Status Control**: Send device test status and messages according to message protocol
    - TestController: Communicate with ClientController, mainly collect device test status (and  other messages).
    - ControllerWorker: Receive the messages from ClientController on all the devices of that test node.
    - ClientController: Send out device test status and messages
3. **Test Executing**: Manage the executing, mainly utilizing the OS and third-party test tools
    - TestRunner: Control the test process . Utilizing the RunnerHelper in different platform to deploy test start test and access the third-party test tools. Managing the LockServer to pass lock between test server and test devices. Collecting the test result for the TestSuite.
    - AdapterRunner: For one specific platform, help TestRunner to direct the test execution. Help clean the test environment , start a test on a device and return the running environment.
    - Deployer: Wrapper of third-party test tool that deploy test case on device and start test on that single platform test tool.
    + Third-party Test Tool:  Android instrumentation & karma for javascript.
4. **Wait Notify Management**: Implement a  cross-platform wait-notify mechanism between all the test devices.
    - WaitNotifyManager: Be responsible for the management of the remote wait-notify mechanism. Maintain the waiting list &notify list and send wait-notify messages to testdevices.
    - LockServer: passing locking message between WaitNotifyManager and local WaitNotifySupport with socket.io, in order to unify the communication between  the two component and reduce the relatedness to specific platform.
    - WaitNotifySupport: Mainly wrap local wait-notify operations on test device.
5. **Result Management**: report the result of given TestSuite from user.
    - ReportGenerator: generate the test result of TestSuite after all TestCase s have done.
    - ReportParser: Send all the device results on test node to TestServer.
    - TestSuite: User defined test logic.

## Physical Structure

1. **Test Server**
    + Runs the server side logic of  test framework
    + Send deploy files or command to distributed test nodes by STAF framework
2. **Test Node**
    + Distributed host computers of physical test devices(browsers or android phones)
    + Directly manage the devices and call the third-party test tools
    + Intermediate communication component between test server and test devices
3. **Test Client**
    + Executing devices of TestCases.

## Sample

A sample is included to demostrate how to add and run a task for two web endpoints. Task info will be uploaded by a REST request, and both test devices will be launched by controller and WebDriver.

1. Start IATF server.
```
node src/server/server.js --certificate_file <cert> --key_file <key>
```

2. Host test pages.

As current sample is a html page, you'll need a web server to host it and its resources. If you don't have such a web server, http-server might be a choice for development or evaluation. Simplely install it by `npm install -g http-server`. Then `http-server src\client`. It will listen HTTP request on 8081 port because the default 8080 is occupied by IATF server.

3. Add a task to IATF server by a REST request (PUT) to `https://<server>/rest/v1/tasks`. An example could be
```
{
    "roles": [{
        "name": "role1",
        "type": "JavaScript",
        "config":{"url":"http://localhost:8081/javascript/test.html"}
    },{
        "name": "role2",
        "type": "JavaScript",
        "config":{"url":"http://localhost:8081/javascript/test.html"}
    }]
}
```
4. Start client controller.
```
python src/controller/controller.py --server https://localhost:8080 --no_ssl_verification --task <taskId>
```