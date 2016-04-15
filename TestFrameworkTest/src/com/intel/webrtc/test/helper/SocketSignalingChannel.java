/*
 * Copyright © 2015 Intel Corporation. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.intel.webrtc.test.helper;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.github.nkzawa.emitter.Emitter.Listener;
import com.github.nkzawa.socketio.client.Ack;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.intel.webrtc.base.ActionCallback;
import com.intel.webrtc.base.WoogeenException;
import com.intel.webrtc.base.WoogeenIllegalArgumentException;
import com.intel.webrtc.p2p.SignalingChannelInterface;
import com.intel.webrtc.p2p.WoogeenP2PException;

/**
 * Socket.IO implementation of P2P signaling channel.
 */
public class SocketSignalingChannel implements SignalingChannelInterface {
    private static final String TAG = "WooGeen-SocketClient";
    private final String CLIENT_CHAT_TYPE = "woogeen-message";
    private final String SERVERAUTHENTICATED = "server-authenticated";
    private final String FORCE_DISCONNECT = "server-disconnect";
    private Socket socketIOClient;
    private List<SignalingChannelObserver> signalingChannelObservers;
    private ActionCallback<String> connectCallback;
    private final String CLIENT_TYPE = "&clientType=";
    private final String CLIENT_TYPE_VALUE = "Android";
    private final String CLIENT_VERSION = "&clientVersion=";
    private final String CLIENT_VERSION_VALUE = "2.8";

    /**
     * Initialize the socket client.
     */
    public SocketSignalingChannel() {
        socketIOClient = null;
        connectCallback = null;
        this.signalingChannelObservers = new ArrayList<SignalingChannelObserver>();
    }

    public void addObserver(SignalingChannelObserver observer) {
        this.signalingChannelObservers.add(observer);
    }

    public void removeObserver(SignalingChannelObserver observer) {
        this.signalingChannelObservers.remove(observer);
    }

    /**
     * Asynchronously connect to WooGeen socket server and listen on messages.
     */
    public void connect(String userInfo, ActionCallback<String> callback) {
        JSONObject loginObject;
        String token;
        String url;
        try {
            connectCallback = callback;
            loginObject = new JSONObject(userInfo);
            token = loginObject.getString("token");
            url = loginObject.getString("host");
            if (isValid(url) && !token.equals("")) {
                url += "?token=" + token + CLIENT_TYPE + CLIENT_TYPE_VALUE
                        + CLIENT_VERSION + CLIENT_VERSION_VALUE;
                IO.Options opt = new IO.Options();
                opt.forceNew = true;
                opt.reconnection = true;
                if(socketIOClient != null){
                    Log.d(TAG, "stop reconnecting the former url");
                    socketIOClient.disconnect();
                }
                socketIOClient = IO.socket(url, opt);
                bindCallbacks();
                socketIOClient.connect();
            } else {
                if (callback != null)
                    callback.onFailure(new WoogeenIllegalArgumentException(
                            "URL is invalid."));
            }
        } catch (JSONException e) {
            if (callback != null)
                callback.onFailure(new WoogeenIllegalArgumentException(e
                        .getMessage()));
        } catch (WoogeenIllegalArgumentException e) {
            if (callback != null)
                callback.onFailure(new WoogeenIllegalArgumentException(e
                        .getMessage()));
        } catch (URISyntaxException e) {
            if (callback != null)
                callback.onFailure(new WoogeenIllegalArgumentException(e
                        .getMessage()));
        }
    }

    boolean isValid(String urlString) throws WoogeenIllegalArgumentException {
        try {
            URL url = new URL(urlString);
            if (url.getPort() > 65535) {
                throw new WoogeenIllegalArgumentException(
                        "port cannot be larger than 65535.");
            } else
                return true;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new WoogeenIllegalArgumentException("Wrong URL.");
        }
    }

    /**
     * Disconnect from server.
     */
    public void disconnect(ActionCallback<Void> callback) {
        if (socketIOClient != null) {
            Log.d(TAG, "Socket IO Disconnect.");
            socketIOClient.disconnect();
            socketIOClient = null;
        }
        if(callback!=null)
            callback.onSuccess(null);
    }

    /**
     * Send message to peer over WooGeen service.
     * @param message data which will be sent.
     * @param peerId peer id
     * @param callback callback function of sendMessage.
     */
    public void sendMessage(String message, String peerId,
            final ActionCallback<Void> callback) {
        if (socketIOClient == null) {
            Log.d(TAG, "socketIOClient is not established.");
            return;
        }
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("to", peerId);
            jsonObject.put("data", message);
            Ack ack = new Ack() {
                @Override
                public void call(Object... arg0) {
                    if (callback != null) {
                        if ((arg0 == null) || (arg0.length != 0)) {
                            callback.onFailure(new WoogeenException(
                                    "Errors occored during sending message."));
                        } else {
                            callback.onSuccess(null);
                        }
                    }
                }
            };
            socketIOClient.emit(CLIENT_CHAT_TYPE, jsonObject, ack);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Bind callback to the socket IO client.
     */
    private void bindCallbacks() {
        socketIOClient.on(CLIENT_CHAT_TYPE, onMessageCallback)
                .on(SERVERAUTHENTICATED, onServerAuthenticatedCallback)
                .on(FORCE_DISCONNECT, onForceDisconnectCallback)
                .on(Socket.EVENT_CONNECT_ERROR, onConnectFailedCallback)
                .on(Socket.EVENT_DISCONNECT, onDisconnectCallback)
                .on(Socket.EVENT_ERROR, onServerErrorCallback);
    };

    private Listener onServerErrorCallback = new Listener(){

        @Override
        public void call(Object... arg0) {
            if(connectCallback != null){
                connectCallback.onFailure(new WoogeenP2PException(
                                WoogeenP2PException.Code.valueOf(Integer.parseInt(arg0[0].toString()))));
                connectCallback = null;
            }
        }

    };
    private Listener onMessageCallback = new Listener() {
        @Override
        public void call(Object... arg0) {
            JSONObject argumentJsonObject = (JSONObject) arg0[0];
            for (SignalingChannelObserver observer : signalingChannelObservers)
                try {
                    observer.onMessage(argumentJsonObject.getString("from"),
                            argumentJsonObject.getString("data"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
        }
    };

    private Listener onServerAuthenticatedCallback = new Listener() {
        @Override
        public void call(Object... arg0) {
            if(connectCallback!=null){
                connectCallback.onSuccess(arg0[0].toString());
                connectCallback = null;
            }
        }
    };

    private Listener onConnectFailedCallback = new Listener() {

        @Override
        public void call(Object... arg0) {
            if (connectCallback != null) {
                connectCallback.onFailure(new WoogeenP2PException(
                                "Socket.IO reports connect to signaling server failed.",
                                WoogeenP2PException.Code.P2P_CONN_SERVER_UNKNOWN));
                connectCallback = null;
            }
        }
    };

    private Listener onForceDisconnectCallback = new Listener() {
        @Override
        public void call(Object... arg0) {
           if (socketIOClient != null) {
               socketIOClient.io().reconnection(false);
           }
        }
    };

    private Listener onDisconnectCallback = new Listener() {
        @Override
        public void call(Object... arg0) {
            for (SignalingChannelObserver observer : signalingChannelObservers)
                observer.onServerDisconnected();
        }
    };

}
