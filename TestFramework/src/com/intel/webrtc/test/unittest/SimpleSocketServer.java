package com.intel.webrtc.test.unittest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.intel.webrtc.test.MessageProtocol;

public class SimpleSocketServer {
    static Random random = new Random();
    private int port = 10086;
    private Hashtable<String, String> locks;

    public SimpleSocketServer(int port) {
        if (port > 65535 | port < 1024) {
            throw new RuntimeException("Wrong port number!");
        }
        this.port = port;
        locks = new Hashtable<String, String>();
    }

    public void start() {
        ServerSocket server;
        Socket socket;
        try {
            server = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(
                    "Error occured when create ServerSocket at port " + port);
        }
        System.out.println("Server started at port " + port);
        while (true) {
            try {
                socket = server.accept();
                listenToSocket(socket);
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    server.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                throw new RuntimeException(
                        "Error occured when accept request at port " + port);
            }
        }
    }

    private void listenToSocket(final Socket socket) {
        new Thread() {
            public void run() {
                try {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(socket.getInputStream()));
                    PrintWriter printWriter = new PrintWriter(
                            new BufferedWriter(new OutputStreamWriter(
                                    socket.getOutputStream())));
                    String line;
                    int port = socket.getPort();
                    String id;
                    line = reader.readLine();
                    new HeartBeatThread(printWriter).start();
                    while (true) {
                        line = reader.readLine();
                        System.out.println("Server@port\t" + "\t"
                                + SimpleSocketServer.this.port + "\tPort: "
                                + port + "\tsays\t" + line);
                        if ("Exit".equals(line)) {
                            break;
                        }
                    }
                    // if ("StartMessage1".equals(line)) {
                    // id = "Client1";
                    // System.out.println("Server@port\t" + id + "\t"
                    // + SimpleSocketServer.this.port + "\tPort: "
                    // + port + "\tsays\t" + line);
                    // waitForObject("lock1", printWriter);
                    // while (true) {
                    // line = reader.readLine();
                    // System.out.println("Server@port\t" + id + "\t"
                    // + SimpleSocketServer.this.port + "\tPort: "
                    // + port + "\tsays\t" + line);
                    // if ("Exit".equals(line)) {
                    // break;
                    // }
                    // }
                    // } else {
                    // id = "Client2";
                    // System.out.println("Server@port\t" + id + "\t"
                    // + SimpleSocketServer.this.port + "\tPort: "
                    // + port + "\tsays\t" + line);
                    // sleep(1000);
                    // notifyObject("lock1", printWriter);
                    // while (true) {
                    // line = reader.readLine();
                    // System.out.println("Server@port\t" + id + "\t"
                    // + SimpleSocketServer.this.port + "\tPort: "
                    // + port + "\tsays\t" + line);
                    // if ("Exit".equals(line)) {
                    // break;
                    // }
                    // }
                    // }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    private class HeartBeatThread extends Thread {
        private PrintWriter printWriter;

        public HeartBeatThread(PrintWriter printWriter) {
            this.printWriter = printWriter;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    // TODO read from xml
                    Thread.sleep(Math.abs(random.nextInt()) % 7000 + 1000);
                    heartBeat(printWriter);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Wait for a remote object
     * @param objectId
     */
    void waitForObject(String objectId, PrintWriter writer) {
        JSONObject message = new JSONObject();
        try {
            message.put(MessageProtocol.MESSAGE_TYPE, MessageProtocol.WAIT);
            message.put(MessageProtocol.WHAT, objectId);
            sendMessage(message.toString(), writer);
        } catch (JSONException e) {
            e.printStackTrace();
            System.out.println("Error occured when creating message.");
        }
    }

    /**
     * Notify a client that waiting for the remote object.
     * @param objectId
     *     the id of the object.
     */
    void notifyObject(String objectId, PrintWriter writer) {
        JSONObject message = new JSONObject();
        try {
            message.put(MessageProtocol.MESSAGE_TYPE, MessageProtocol.NOTIFY);
            message.put(MessageProtocol.WHAT, objectId);
            sendMessage(message.toString(), writer);
        } catch (JSONException e) {
            e.printStackTrace();
            System.out.println("Error occured when creating message.");
        }
    }

    /**
     * Notify all client that waiting for the remote object.
     * @param objectId
     *     the id of the object.
     */
    void notifyObjectForAll(String objectId, PrintWriter writer) {
        JSONObject message = new JSONObject();
        try {
            message.put(MessageProtocol.MESSAGE_TYPE,
                    MessageProtocol.NOTIFY_ALL);
            message.put(MessageProtocol.WHAT, objectId);
            sendMessage(message.toString(), writer);
        } catch (JSONException e) {
            e.printStackTrace();
            System.out.println("Error occured when creating message.");
        }
    }

    void heartBeat(PrintWriter writer) {
        JSONObject message = new JSONObject();
        try {
            message.put(MessageProtocol.MESSAGE_TYPE, MessageProtocol.HEARTBEAT);
            sendMessage(message.toString(), writer);
        } catch (JSONException e) {
            e.printStackTrace();
            System.out
                    .println("Error occured when creating heart beat message.");
        }
    }

    private void dispatchMessage(String msg) {
        JSONObject message;
        String messageType;
        try {
            message = new JSONObject(msg);
        } catch (JSONException e) {
            System.out.println("Message [" + msg
                    + "] is not a legal JSONObject.");
            e.printStackTrace();
            return;
        }
        try { // Get the message type from the jsonObject
            messageType = message.getString(MessageProtocol.MESSAGE_TYPE);
            // TODO whether it is necessary to tell clients that the test ended,
            // and ask them to clean up.
            if (messageType.equals(MessageProtocol.NOTIFY)) {
                String notifyWhat = "";
                notifyWhat = message.getString(MessageProtocol.WHAT);
                if ("".equals(notifyWhat)) {
                    System.out
                            .println("Can not wait or notify an empty string.");
                    return;
                }
                // observer.onNotified(notifyWhat);
            } else {
                System.out.println("Unknown message type: " + messageType);
            }
        } catch (JSONException e) {
            System.out.println("message[ " + msg + " ] has illegal arguments!");
            e.printStackTrace();
        }
    }

    /**
     * send a message to the test controller
     * @param msg
     *     the message
     */
    private void sendMessage(final String msg, final PrintWriter printWriter) {
        new Thread() {
            public void run() {
                synchronized (printWriter) {
                    printWriter.println(msg);
                    printWriter.flush();
                }
            }
        }.start();
    }
}
