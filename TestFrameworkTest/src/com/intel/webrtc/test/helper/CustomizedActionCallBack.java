package com.intel.webrtc.test.helper;

import com.intel.webrtc.base.ActionCallback;
import com.intel.webrtc.base.WoogeenException;

public class CustomizedActionCallBack<T> implements ActionCallback<T> {
    T result;
    public int onSuccessCalled;
    public int getOnSuccessCalled() {
        return onSuccessCalled;
    }

    public int getOnFailureCalled() {
        return onFailureCalled;
    }

    public int getOnDisconnect() {
        return onDisconnect;
    }

    public int getOnInValidIdOrStream() {
        return onInValidIdOrStream;
    }

    public int getOnNoSuchPeer() {
        return onNoSuchPeer;
    }

    public int onFailureCalled;
    int onDisconnect, onInValidIdOrStream, onNoSuchPeer;
    private boolean isSuccess, isFailure;

    public boolean isSuccess() {
        return isSuccess;
    }

    public boolean isFailure() {
        return isFailure;
    }

    public CustomizedActionCallBack() {
        onSuccessCalled = 0;
        onFailureCalled = 0;
        onDisconnect = 0;
        onInValidIdOrStream = 0;
        onNoSuchPeer = 0;
    }

    public void clear(){
        onSuccessCalled = 0;
        onFailureCalled = 0;
        onDisconnect = 0;
        onInValidIdOrStream = 0;
        onNoSuchPeer = 0;
        result=null;
        isSuccess=false;
        isFailure=false;
    }

    @Override
    public void onSuccess(T result) {
        this.result = result;
        this.isSuccess = true;
        onSuccessCalled++;
    }

    @Override
    public void onFailure(WoogeenException e) {
        this.isFailure = true;
        if (e.toString().contains("PeerClient haven't connect to a signaling server")) {
            onDisconnect++;
        }
        if (e.toString().contains("Peer ID or stream is invalid") || e.toString().contains("Peer ID is invalid")) {
            onInValidIdOrStream++;
        }
        if (e.toString().contains("Connection to remote Peer is not established")) {
            onNoSuchPeer++;
        }
        if (e.toString().contains("Another PeerClient haven't connect to me")) {
            onNoSuchPeer++;
        }
        e.printStackTrace();
        onFailureCalled++;
    }

    public T getResult() {
        return result;
    }
}
