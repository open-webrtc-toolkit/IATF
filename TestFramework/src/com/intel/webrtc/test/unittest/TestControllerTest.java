package com.intel.webrtc.test.unittest;

import java.util.Hashtable;

import com.intel.webrtc.test.TestController;

public class TestControllerTest {
    Hashtable<String, String> addressTable = new Hashtable<String, String>();
    Hashtable<String, String> startMessageTable = new Hashtable<String, String>();
    String name = "Client";
    String localHost = "127.0.0.1";

    public static void main(String[] args) {
        System.out.println("test start");
        new TestControllerTest().start();
    }

    public void start() {
        for (int i = 0; i < 10; i++) {
            addressTable.put(name + i, localHost);
            startMessageTable.put(name + i, "StartMessage1");
        }
        startServer();
        TestController controller = new TestController(addressTable,
                startMessageTable);
        controller.start();

    }

    private void startServer() {
        new Thread() {
            public void run() {
                System.out.println("Child thread.");
                SimpleSocketServer server = new SimpleSocketServer(10086);
                server.start();
            }
        }.start();
    }
}
