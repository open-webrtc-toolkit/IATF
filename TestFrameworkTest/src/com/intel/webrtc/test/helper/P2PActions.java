package com.intel.webrtc.test.helper;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;

import com.intel.webrtc.base.LocalCameraStream;
import com.intel.webrtc.base.LocalCameraStreamParameters;
import com.intel.webrtc.base.WoogeenException;
import com.intel.webrtc.p2p.PeerClient;

import android.app.Activity;
import android.os.SystemClock;
import android.util.Log;

/**
 * This class provides basic actions on P2PSDK. Atom action is defined and
 * asserts the action callback state.
 * @author bean
 */
public class P2PActions extends Assert {
    public static long waitingTime = 4000;
    public static final String TAG = "P2PActions";
    private Activity testAct = null;

    private static String generateLoginToken(String serverIp, String userName) {
        JSONObject loginObject = new JSONObject();
        try {
            loginObject.put("host", serverIp);
            loginObject.put("token", userName);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "Error occured when generate login token.");
        }
        Log.d(TAG, "Generate token for " + userName);
        return loginObject.toString();
    }

    /**
     * Login to P2P server.
     * @param actorUser The peer client user to take a test action.
     * @param actorUserName The name of the peer client user to take an action.
     * @param serverIP P2P server IP.
     */
    public static void connect(PeerClient actorUser, String actorUserName, String serverIP) {
        String token = generateLoginToken(serverIP, actorUserName);
        CustomizedActionCallBack<String> stringCallBack = new CustomizedActionCallBack<String>();
        actorUser.connect(token, stringCallBack);
        assertActionSucceeded(stringCallBack, actorUserName + " connect to server");
        Log.d(TAG, actorUserName + " connect to server");
    }

    /**
     * ActorUser invite targetUser.
     * @param actorUser The peer client user to take a test action.
     * @param actorUserName The name of the peer client user to take an action.
     * @param targetUserName The name of the action target user.
     */
    public static void invite(PeerClient actorUser, String actorUserName, String targetUserName) {
        CustomizedActionCallBack<Void> callBack = new CustomizedActionCallBack<Void>();
        actorUser.invite(targetUserName, callBack);
        assertActionSucceeded(callBack, actorUserName + " invite " + targetUserName);
        Log.d(TAG, actorUserName + " invite " + targetUserName);
    }

    /**
     * ActorUser accept targetUser.
     * @param actorUser The peer client user to take a test action.
     * @param actorUserName The name of the peer client user to take an action.
     * @param targetUserName The name of the action target user.
     */
    public static void accept(PeerClient actorUser, String actorUserName, String targetUserName) {
        CustomizedActionCallBack<Void> callBack = new CustomizedActionCallBack<Void>();
        actorUser.accept(targetUserName, callBack);
        assertActionSucceeded(callBack, actorUserName + " accept " + targetUserName);
        Log.d(TAG, actorUserName + " accept " + targetUserName);
    }

    /**
     * ActorUser deny targetUser.
     * @param actorUser The peer client user to take a test action.
     * @param actorUserName The name of the peer client user to take an action.
     * @param targetUserName The name of the action target user.
     */
    public static void deny(PeerClient actorUser, String actorUserName, String targetUserName) {
        CustomizedActionCallBack<Void> callBack = new CustomizedActionCallBack<Void>();
        actorUser.deny(targetUserName, callBack);
        assertActionSucceeded(callBack, actorUserName + " deny " + targetUserName);
        Log.d(TAG, actorUserName + " deny " + targetUserName);
    }

    /**
     * ActorUser stop chat to targetUser.
     * @param actorUser The peer client user to take a test action.
     * @param actorUserName The name of the peer client user to take an action.
     * @param targetUserName The name of the action target user.
     */
    public static void stop(PeerClient actorUser, String actorUserName, String targetUserName) {
        CustomizedActionCallBack<Void> callBack = new CustomizedActionCallBack<Void>();
        actorUser.stop(targetUserName, callBack);
        assertActionSucceeded(callBack, actorUserName + " stop chat to " + targetUserName);
        Log.d(TAG, actorUserName + " stop chat to " + targetUserName);
    }

    /**
     * Create local camera stream with parameters.
     * @param actorUserName The name of the peer client user to take an action.
     * @param enableVideo Local stream setting to enable video.
     * @param enableAudio Local stream setting to enable audio.
     * @return local stream created.
     */
    public static LocalCameraStream createLocalCameraStream(String actorUserName, boolean enableVideo,
            boolean enableAudio) {
        LocalCameraStreamParameters lcsp = null;
        try {
            lcsp = new LocalCameraStreamParameters(enableVideo, enableAudio);
        } catch (WoogeenException e1) {
            fail("Error occured when creating LocalCameraStreamParameters.");
            e1.printStackTrace();
        }
        assertNotNull(lcsp);
        LocalCameraStream localCameraStream = null;
        try {
            localCameraStream = new LocalCameraStream(lcsp);
        } catch (WoogeenException e) {
            fail("Error occured when creating local stream.");
            e.printStackTrace();
        }
        assertNotNull(localCameraStream);
        assertNotNull(actorUserName + " MediaStream is null.", localCameraStream.getMediaStream());
        Log.d(TAG, actorUserName + " create local stream.");
        return localCameraStream;
    }

    /**
     * ActorUser publish local camera stream to targetUser.
     * @param actorUser The peer client user to take a test action.
     * @param actorUserName The name of the peer client user to take an action.
     * @param targetUserName The name of the action target user.
     * @param actorLocalCameraStream Local stream of the test actor peer client.
     */
    public static void publish(PeerClient actorUser, String actorUserName, String targetUserName,
            LocalCameraStream actorLocalCameraStream) {
        CustomizedActionCallBack<Void> callBack = new CustomizedActionCallBack<Void>();
        actorUser.publish(actorLocalCameraStream, targetUserName, callBack);
        assertActionSucceeded(callBack, actorUserName + " publish local camera stream to " + targetUserName);
        Log.d(TAG, actorUserName + " publish local camera stream to " + targetUserName);
    }

    /**
     * ActorUser unpublish local camera stream to targetUser.
     * @param actorUser The peer client user to take a test action.
     * @param actorUserName The name of the peer client user to take an action.
     * @param targetUserName The name of the action target user.
     * @param actorLocalCameraStream Local stream of the test actor peer client.
     */
    public static void unpublish(PeerClient actorUser, String actorUserName, String targetUserName,
            LocalCameraStream actorLocalCameraStream) {
        CustomizedActionCallBack<Void> callBack = new CustomizedActionCallBack<Void>();
        actorUser.unpublish(actorLocalCameraStream, targetUserName, callBack);
        assertActionSucceeded(callBack, actorUserName + " unpublish local camera stream to " + targetUserName);
        Log.d(TAG, actorUserName + " unpublish local camera stream to " + targetUserName);
    }

    /**
     * ActorUser send message to targetUser.
     * @param actorUser The peer client user to take a test action.
     * @param actorUserName The name of the peer client user to take an action.
     * @param targetUserName The name of the action target user.
     * @param message The content to be sent.
     */
    public static void send(PeerClient actorUser, String actorUserName, String targetUserName, String message) {
        CustomizedActionCallBack<Void> callBack = new CustomizedActionCallBack<Void>();
        actorUser.send(message, targetUserName, callBack);
        assertActionSucceeded(callBack, actorUserName + " send message to " + targetUserName);
        Log.d(TAG, actorUserName + " send message to " + targetUserName);
    }

    /**
     * ActorUser disconnect from server.
     * @param actorUser The peer client user to take a test action.
     * @param actorUserName The name of the peer client user to take an action.
     */
    public static void disconnect(PeerClient actorUser, String actorUserName) {
        CustomizedActionCallBack<Void> callBack = new CustomizedActionCallBack<Void>();
        actorUser.disconnect(callBack);
        assertActionSucceeded(callBack, actorUserName + " disconnect");
        Log.d(TAG, actorUserName + " disconnect");
    }

    /**
     * Call after wait for a connet action.
     */
    public static void afterWaitConnect() {
        // do nothing
    }

    /**
     * Call after wait for an invite action.
     * @param actorUserName The name of the peer client user to take an action.
     * @param pcObserver Peer client callback observer.
     * @param expectInviteTimes Expect number in peer client observer.
     */
    public static void afterWaitInvite(String actorUserName, PeerClientObserverForTest pcObserver,
            int expectInviteTimes) {
        SystemClock.sleep(waitingTime);
        assertEquals(" should be invited once!", expectInviteTimes, pcObserver.onInvitedCalledTimes);
    }

    /**
     * Call after wait for an accept action.
     * @param actorUserName The name of the peer client user to take an action.
     * @param pcObserver Peer client callback observer.
     * @param expectAcceptTimes Expect number in peer client observer.
     * @param expectCharStartTimes Expect number in peer client observer.
     */
    public static void afterWaitAccept(String actorUserName, PeerClientObserverForTest pcObserver,
            int expectAcceptTimes, int expectCharStartTimes) {
        SystemClock.sleep(waitingTime);
        assertEquals(actorUserName + " didn't receive the accepted message!", expectAcceptTimes,
                pcObserver.onAcceptedCalledTimes);
        assertEquals(actorUserName + " didn't receive the chatStarted message!", expectCharStartTimes,
                pcObserver.onChatStartedCalledTimes);
    }

    /**
     * Call after wait for a deny action.
     * @param actorUserName The name of the peer client user to take an action.
     * @param pcObserver Peer client callback observer.
     * @param expectDenyTimes Expect number in peer client observer.
     */
    public static void afterWaitDeny(String actorUserName, PeerClientObserverForTest pcObserver, int expectDenyTimes) {
        SystemClock.sleep(waitingTime);
        assertEquals(actorUserName + " didn't receive the onDeny message!", expectDenyTimes,
                pcObserver.onDeniedCalledTimes);
    }

    /**
     * Call after wait for a stop action.
     * @param actorUserName The name of the peer client user to take an action.
     * @param pcObserver Peer client callback observer.
     * @param expectiChatStopTimes Expect number in peer client observer.
     */
    public static void afterWaitStop(String actorUserName, PeerClientObserverForTest pcObserver,
            int expectiChatStopTimes) {
        SystemClock.sleep(waitingTime);
        assertEquals(actorUserName + " didn't receive the onStop message!", expectiChatStopTimes,
                pcObserver.onChatStoppedCalledTimes);
    }

    /**
     * Call after wait for a CreateLocalCameraStream action.
     */
    public static void afterWaitCreateLocalCameraStream() {
        // do nothing
    }

    /**
     * Call after wait for a publish action.
     * @param actorUserName The name of the peer client user to take an action.
     * @param pcObserver Peer client callback observer.
     * @param expectStreamAddedTimes Expect number in peer client observer.
     */
    public static void afterWaitPublish(String actorUserName, PeerClientObserverForTest pcObserver,
            int expectStreamAddedTimes) {
        SystemClock.sleep(waitingTime);
        assertEquals(actorUserName + " should receive the streamAdded message once!", expectStreamAddedTimes,
                pcObserver.onStreamAddedCalledTimes);
        assertNotNull(actorUserName + " received a null stream!", pcObserver.addedStream);
    }

    /**
     * Call after wait for an unpublish action.
     * @param actorUserName The name of the peer client user to take an action.
     * @param pcObserver Peer client callback observer.
     * @param expectStreamRemovedTimes Expect number in peer client observer.
     */
    public static void afterWaitUnpublish(String actorUserName, PeerClientObserverForTest pcObserver,
            int expectStreamRemovedTimes) {
        SystemClock.sleep(waitingTime);
        assertEquals(actorUserName + " should receive the streamAdded message once!", expectStreamRemovedTimes,
                pcObserver.onStreamRemovedCalledTimes);
        assertNotNull(actorUserName + " the stream removed was null!", pcObserver.removedStream);
    }

    /**
     * Call after wait for a sendTwoMessage action.
     * @param actorUserName The name of the peer client user to take an action.
     * @param pcObserver Peer client callback observer.
     * @param numOfMsg Expect message number in peer client observer.
     * @param numOfSender Expect sender number in peer client observer.
     */
    public static void afterWaitSend(String actorUserName, PeerClientObserverForTest pcObserver, int numOfMsg,
            int numOfSender) {
        SystemClock.sleep(waitingTime);
        assertEquals(actorUserName + " should receive " + numOfMsg + " messages from ", numOfMsg,
                pcObserver.dataReceived.size());
        Log.d(TAG, "senders:");
        for (String peer : pcObserver.dataSenders) {
            Log.d(TAG, peer);
        }
        assertEquals(actorUserName + " should receive messages from " + numOfSender + " peer!", numOfSender,
                pcObserver.dataSenders.size());
    }

    /**
     * Call after wait for a disconnect action.
     */
    public static void afterWaitDisconnect() {
        // do nothing
    }

    // Call after an action
    public static void afterConnect() {
        // do nothing
    }

    public static void afterInvite() {
        // do nothing
    }

    /**
     * Call after taking an accept action.
     * @param actorUserName The name of the peer client user to take an action.
     * @param pcObserver Peer client callback observer.
     * @param expectChatStartedTimes Expect number in peer client observer.
     */
    public static void afterAccept(String actorUserName, PeerClientObserverForTest pcObserver,
            int expectChatStartedTimes) {
        SystemClock.sleep(waitingTime);
        assertEquals(actorUserName + " didn't receive the chatStarted message!", expectChatStartedTimes,
                pcObserver.onChatStartedCalledTimes);
    }

    public static void afterDeny() {
        // do nothing
    }
    /**
     * Call after taking a stop action
     * @param actorUserName The name of the peer client user to take an action.
     * @param pcObserver Peer client callback observer.
     * @param expectChatStopTimes Expect number in peer client observer.
     */
    public static void afterStop(String actorUserName, PeerClientObserverForTest pcObserver, int expectChatStopTimes) {
        SystemClock.sleep(waitingTime);
        assertEquals(actorUserName + " didn't receive the chatStopped message!", expectChatStopTimes,
                pcObserver.onChatStoppedCalledTimes);
    }
    /**
     * Call after taking a CreateLocalStream action
     * @param actorUserName The name of the peer client user to take an action.
     * @param lcs Created local stream.
     */
    public static void afterCreateLocalStream(String actorUserName, LocalCameraStream lcs) {
        assertNotNull(actorUserName + " shouldn't create a null stream!", lcs);
    }

    public static void afterPublish() {
        // do nothing
    }

    public static void afterUnpublish() {
        // do nothing
    }

    public static void afterSend() {
        // do nothing
    }

    /**
     * Call after taking a disconnet action
     * @param actorUserName The name of the peer client user to take an action.
     * @param pcObserver Peer client callback observer.
     * @param expectDisconnetTimes Expect number in peer client observer.
     */
    public static void afterDisconnect(String actorUserName, PeerClientObserverForTest pcObserver,
            int expectDisconnetTimes) {
        SystemClock.sleep(waitingTime);
        assertEquals(actorUserName + " should receive a message of server disconnected!", expectDisconnetTimes,
                pcObserver.onServerDisconnectedCalledTimes);
    }

    public static void startStory(String storyName) {
        Log.d(TAG, "=======================" + storyName + "=======================");
    }

    public static void endStory() {
        Log.d(TAG, "===========================================================");
    }

    public static void assertActionSucceeded(CustomizedActionCallBack callBack, String actionName) {
        // waiting for the action to finish
        SystemClock.sleep(waitingTime);
        assertEquals("Action <" + ":" + actionName + "> failed!", 0, callBack.onFailureCalled);
        assertTrue("OnSuccess hasn't been called!", callBack.onSuccessCalled > 0);
        assertEquals("OnSuccess has been called more than once in action <" + actionName + ">", 1,
                callBack.onSuccessCalled);
    }
}
