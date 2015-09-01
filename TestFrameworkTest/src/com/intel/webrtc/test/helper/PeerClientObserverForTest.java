package com.intel.webrtc.test.helper;

import java.util.LinkedList;
import java.util.List;

import com.intel.webrtc.base.RemoteStream;
import com.intel.webrtc.p2p.PeerClient.PeerClientObserver;

import android.util.Log;

public class PeerClientObserverForTest implements PeerClientObserver {

    public int onAcceptedCalledTimes, onChatStartedCalledTimes,
            onChatStoppedCalledTimes, onInvitedCalledTimes,
            onDeniedCalledTimes, onStreamAddedCalledTimes,
            onDataReceivedCalledTimes, onStreamRemovedCalledTimes,
            onServerDisconnectedCalledTimes;
    public int getOnAcceptedCalledTimes() {
        return onAcceptedCalledTimes;
    }

    public int getOnChatStartedCalledTimes() {
        return onChatStartedCalledTimes;
    }

    public int getOnChatStoppedCalledTimes() {
        return onChatStoppedCalledTimes;
    }

    public int getOnInvitedCalledTimes() {
        return onInvitedCalledTimes;
    }

    public int getOnDeniedCalledTimes() {
        return onDeniedCalledTimes;
    }

    public int getOnStreamAddedCalledTimes() {
        return onStreamAddedCalledTimes;
    }

    public int getOnDataReceivedCalledTimes() {
        return onDataReceivedCalledTimes;
    }

    public int getOnStreamRemovedCalledTimes() {
        return onStreamRemovedCalledTimes;
    }

    public int getOnServerDisconnectedCalledTimes() {
        return onServerDisconnectedCalledTimes;
    }

    public List<String> dataReceived, dataSenders;
    public List<RemoteStream> removedStream, addedStream;
    public String roomMember = "";
    public PeerClientObserverForTest() {
        onAcceptedCalledTimes = 0;
        onChatStartedCalledTimes = 0;
        onChatStoppedCalledTimes = 0;
        onInvitedCalledTimes = 0;
        onDeniedCalledTimes = 0;
        onStreamAddedCalledTimes = 0;
        onDataReceivedCalledTimes = 0;
        onStreamRemovedCalledTimes = 0;
        onServerDisconnectedCalledTimes = 0;
        dataReceived = new LinkedList<String>();
        dataSenders = new LinkedList<String>();
        removedStream = new LinkedList<RemoteStream>();
        addedStream = new LinkedList<RemoteStream>();
    }

    @Override
    public void onAccepted(String peerId) {
        Log.d("Observer", "call accepted."+onAcceptedCalledTimes);
        onAcceptedCalledTimes++;
        Log.d("Observer", "call accepted "+onAcceptedCalledTimes);
    }

    @Override
    public void onChatStarted(String peerId) {
        Log.d("Observer", "call onChatStarted.");
        onChatStartedCalledTimes++;
    }

    @Override
    public void onChatStopped(String peerId) {
        Log.d("Observer", "call onChatStopped.");
        onChatStoppedCalledTimes++;
    }

    @Override
    public void onInvited(String peerId) {
        Log.d("Observer", "call onInvited.");
        onInvitedCalledTimes++;
    }

    @Override
    public void onDenied(String peerId) {
        Log.d("Observer", "call onDenied.");
        onDeniedCalledTimes++;
    }

    @Override
    public void onStreamAdded(RemoteStream stream) {
        onStreamAddedCalledTimes++;
        addedStream.add(stream);
    }

    @Override
    public void onDataReceived(String peerId, String msg) {
        onDataReceivedCalledTimes++;
        if (msg != null) {
            dataReceived.add(msg);
        } else {
            dataReceived.add("NullPointer");
        }
        if (peerId != null) {
            dataSenders.add(peerId);
        } else {
            dataSenders.add("NullPointer");
        }
    }

    @Override
    public void onServerDisconnected() {
        Log.d("Observer", "call onServerDisconnected.");
        onServerDisconnectedCalledTimes++;
    }

    @Override
    public void onStreamRemoved(RemoteStream stream) {
        onStreamRemovedCalledTimes++;
        removedStream.add(stream);
    }

    @Override
    public String toString() {
        String ret;
        ret="onAcceptedCalledTimes="+onAcceptedCalledTimes+
            ", onChatStartedCalledTimes="+onChatStartedCalledTimes+
            ", onChatStoppedCalledTimes="+onChatStoppedCalledTimes+
            ", onInvitedCalledTimes"+onInvitedCalledTimes+
            ", onDeniedCalledTimes"+onDeniedCalledTimes+
            ", onStreamAddedCalledTimes"+onStreamAddedCalledTimes+
            ", onDataReceivedCalledTimes"+onDataReceivedCalledTimes+
            ", onStreamRemovedCalledTimes"+onStreamRemovedCalledTimes+
            ", onServerDisconnectedCalledTimes"+onServerDisconnectedCalledTimes+
            ", dataReceived"+dataReceived.size()+
            ", dataSenders"+dataSenders.size()+
            ", removedStream"+removedStream+
            ", addedStream"+addedStream;
        return ret;
    }
}
