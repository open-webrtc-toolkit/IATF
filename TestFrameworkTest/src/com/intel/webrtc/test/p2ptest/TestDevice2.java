package com.intel.webrtc.test.p2ptest;

import java.util.List;

import com.intel.webrtc.base.ClientContext;
import com.intel.webrtc.base.LocalCameraStream;
import com.intel.webrtc.base.RemoteStream;
import com.intel.webrtc.p2p.PeerClient;
import com.intel.webrtc.p2p.PeerClientConfiguration;
import com.intel.webrtc.test.android.AndroidTestDevice;
import com.intel.webrtc.test.helper.P2PActions;
import com.intel.webrtc.test.helper.PeerClientObserverForTest;
import com.intel.webrtc.test.helper.TestActivity;

import android.os.SystemClock;
import android.util.Log;

public class TestDevice2 extends AndroidTestDevice {
    String TAG = "P2PTestDevice2";
    TestActivity act = null;
    private long waitingTime = 3000;
    private String serverIP = "http://10.239.44.33:8095/";

    /**
     * Test a normal interaction process between two users.
     * Actors: User1 and User2
     * Story:
     * 1. User1Connect
     * 2. User2Connect
     * 3. User1InviteUser2
     * 4. User2AcceptUser1
     * 5. User1CreateLocalStream
     * 6. User2CreateLocalStream
     * 7. User1PublishToUser2
     * 8. User2PublishToUser1
     * 9. User1SendTwoMessagesToUser2
     * 10. User2SendTwoMessagesToUser1
     * 11. User1UnpublishToUser2
     * 12. User2UnpublishToUser1
     * 13. User1StopChatWithUser2
     * 14. User2InviteUser1
     * 15. User1DenyUser2
     * 16. User2Disconnect
     * 17. User1Disconnect
     */
    public void testTwoUserInteraction() {
        initTestActivity();
        // Init variables

        String actorUserName = "User2";
        String targetUserName = "User1";
        PeerClientObserverForTest pcObserver = new PeerClientObserverForTest();
        PeerClientConfiguration peerClientConfiguration = new PeerClientConfiguration();
        PeerClient actorUser = new PeerClient(peerClientConfiguration, new SocketSignalingChannel());
        actorUser.addObserver(pcObserver);
        act.makeToast("Test start, actor:" + actorUserName);
        act.setTitle("Actor:" + actorUserName);
        // Action 1:User1Connect
        act.setCurActionText("Action 1: User1Connect");
        // Action 2:User2Connect
        waitLock("User1Connect");
        act.setCurActionText("Action 2: User2Connect");
        P2PActions.afterWaitConnect();
        P2PActions.connect(actorUser, actorUserName, serverIP);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        // Action 3:User1InviteUser2
        act.setCurActionText("Action 3: User1InviteUser2");
        // Action 4:User2AcceptUser1
        waitLock("User1InviteUser2");
        act.setCurActionText("Action 4: User2AcceptUser1");
        P2PActions.afterWaitInvite(actorUserName, pcObserver, 1);
        P2PActions.accept(actorUser, actorUserName, targetUserName);
        P2PActions.afterAccept(actorUserName, pcObserver, 1);
        notifyLock("User2AcceptUser1");
        // Action 5:User1CreateLocalStream
        act.setCurActionText("Action 5: User1CreateLocalStream");
        // Action 6:User2CreateLocalStream
        waitLock("User1CreateLocalStream");
        act.setCurActionText("Action 6: User2CreateLocalStream");
        P2PActions.afterWaitCreateLocalCameraStream();
        LocalCameraStream lcs = P2PActions.createLocalCameraStream(actorUserName, true, true);
        Log.d(TAG, "lcs:" + lcs);
        act.attachRender1(lcs);
        notifyLock("User2CreateLocalStream");
        // Action 7:User1PublishToUser2
        act.setCurActionText("Action 7: User1PublishToUser2");
        // Action 8:User2PublishToUser1
        waitLock("User1PublishToUser2");
        act.setCurActionText("Action 8: User2PublishToUser1");
        P2PActions.afterWaitPublish(actorUserName, pcObserver, 1);
        List<RemoteStream> rslist = pcObserver.addedStream;
        act.attachRender2(rslist.get(0));
        P2PActions.publish(actorUser, actorUserName, targetUserName, lcs);
        P2PActions.afterPublish();
        notifyLock("User2PublishToUser1");
        Log.d(TAG, "OBSERVER:" + pcObserver);
        // Action 9:User1SendTwoMessagesToUser2
        act.setCurActionText("Action 9: User1SendTwoMessagesToUser2");
        // Action 10:User2SendTwoMessagesToUser1
        waitLock("User1SendTwoMessagesToUser2");
        act.setCurActionText("Action 10: User2SendTwoMessagesToUser1");
        P2PActions.afterWaitSend(actorUserName, pcObserver, 2, 2);
        P2PActions.send(actorUser, actorUserName, targetUserName,
                actorUserName + " send message1 to " + targetUserName);
        P2PActions.send(actorUser, actorUserName, targetUserName,
                actorUserName + " send message2 to " + targetUserName);
        P2PActions.afterSend();
        notifyLock("User2SendTwoMessagesToUser1");
        // Action 11:User1UnpublishToUser2
        act.setCurActionText("Action 11: User1UnpublishToUser2");
        // Action 12:User2UnpublishToUser1
        waitLock("User1UnpublishToUser2");
        act.setCurActionText("Action 12: User2UnpublishToUser1");
        P2PActions.afterWaitUnpublish(actorUserName, pcObserver, 1);
        P2PActions.unpublish(actorUser, actorUserName, targetUserName, lcs);
        P2PActions.afterUnpublish();
        notifyLock("User2UnpublishToUser1");
        // Action 13:User1StopChatWithUser2
        act.setCurActionText("Action 13: User1StopChatWithUser2");
        // Action 14:User2InviteUser1
        waitLock("User1StopChatWithUser2");
        act.setCurActionText("Action 14:User2InviteUser1");
        P2PActions.afterWaitStop(actorUserName, pcObserver, 1);
        P2PActions.invite(actorUser, actorUserName, targetUserName);
        P2PActions.afterInvite();
        notifyLock("User2InviteUser1");
        // Action 15:User1DenyUser2
        act.setCurActionText("Action 15:User1DenyUser2");
        // Action 16:User2Disconnect
        waitLock("User1DenyUser2");
        act.setCurActionText("Action 16:User2Disconnect");
        P2PActions.afterWaitDeny(actorUserName, pcObserver, 1);
        P2PActions.disconnect(actorUser, actorUserName);
        P2PActions.afterDisconnect(actorUserName, pcObserver, 1);
        notifyLock("User2Disconnect");
        // Action 17:User1Disconnect
        act.setCurActionText("Action 17:User1Disconnect");
        SystemClock.sleep(waitingTime);
        P2PActions.endStory();
        act.makeToast("Test End!");
    }

    /**
     * Case 1: Test bidirection publish between two users.
     * Actors: User1 and User2
     * Story:
     * 1. User1Connect
     * 2. User2Connect
     * 3. User1InviteUser2
     * 4. User2AcceptUser1
     * 5. User1CreateLocalStream
     * 6. User2CreateLocalStream
     * 7. User1PublishToUser2
     * 8. User2PublishToUser1
     */
    public void test_TwoUser_bipublish() {
        initTestActivity();
        // Init variables

        String actorUserName = "User2";
        String targetUserName = "User1";
        PeerClientObserverForTest pcObserver = new PeerClientObserverForTest();
        PeerClientConfiguration peerClientConfiguration = new PeerClientConfiguration();
        PeerClient actorUser = new PeerClient(peerClientConfiguration, new SocketSignalingChannel());
        actorUser.addObserver(pcObserver);

        act.makeToast("Test start, actor:" + actorUserName);
        P2PActions.startStory("testTwoUserInteraction");
        act.setTitle("Actor:" + actorUserName);

        // Action 1:User1Connect
        act.setCurActionText("Action 1: User1Connect");
        // Action 2:User2Connect
        waitLock("User1Connect");
        act.setCurActionText("Action 2: User2Connect");
        P2PActions.afterWaitConnect();
        P2PActions.connect(actorUser, actorUserName, serverIP);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        // Action 3:User1InviteUser2
        act.setCurActionText("Action 3: User1InviteUser2");
        // Action 4:User2AcceptUser1
        waitLock("User1InviteUser2");
        act.setCurActionText("Action 4: User2AcceptUser1");
        P2PActions.afterWaitInvite(actorUserName, pcObserver, 1);
        P2PActions.accept(actorUser, actorUserName, targetUserName);
        P2PActions.afterAccept(actorUserName, pcObserver, 1);
        notifyLock("User2AcceptUser1");
        // Action 5:User1CreateLocalStream
        act.setCurActionText("Action 5: User1CreateLocalStream");
        // Action 6:User2CreateLocalStream
        waitLock("User1CreateLocalStream");
        act.setCurActionText("Action 6: User2CreateLocalStream");
        P2PActions.afterWaitCreateLocalCameraStream();
        LocalCameraStream lcs = P2PActions.createLocalCameraStream(actorUserName, true, true);
        Log.d(TAG, "lcs:" + lcs);
        act.attachRender1(lcs);
        notifyLock("User2CreateLocalStream");
        // Action 7:User1PublishToUser2
        act.setCurActionText("Action 7: User1PublishToUser2");
        // Action 8:User2PublishToUser1
        waitLock("User1PublishToUser2");
        act.setCurActionText("Action 8: User2PublishToUser1");
        P2PActions.afterWaitPublish(actorUserName, pcObserver, 1);
        List<RemoteStream> rslist = pcObserver.addedStream;
        act.attachRender2(rslist.get(0));
        P2PActions.publish(actorUser, actorUserName, targetUserName, lcs);
        P2PActions.afterPublish();

        P2PActions.endStory();
        act.makeToast("Test End!");
    }

    /**
     * Case 2: Test bidirection publish video-only stream between two users.
     * Actors: User1 and User2
     * Story:
     * 1. User1Connect
     * 2. User2Connect
     * 3. User1InviteUser2
     * 4. User2AcceptUser1
     * 5. User1CreateVideoOnlyLocalStream
     * 6. User2CreateVideoOnlyLocalStream
     * 7. User1PublishToUser2
     * 8. User2PublishToUser1
     */
    public void test_TwoUser_bipublishVideoOnly() {
        initTestActivity();
        // Init variables

        String actorUserName = "User2";
        String targetUserName = "User1";
        PeerClientObserverForTest pcObserver = new PeerClientObserverForTest();
        PeerClientConfiguration peerClientConfiguration = new PeerClientConfiguration();
        PeerClient actorUser = new PeerClient(peerClientConfiguration, new SocketSignalingChannel());
        actorUser.addObserver(pcObserver);

        act.makeToast("Test start, actor:" + actorUserName);
        P2PActions.startStory("testTwoUserInteraction");
        act.setTitle("Actor:" + actorUserName);

        // Action 1:User1Connect
        act.setCurActionText("Action 1: User1Connect");
        // Action 2:User2Connect
        waitLock("User1Connect");
        act.setCurActionText("Action 2: User2Connect");
        P2PActions.afterWaitConnect();
        P2PActions.connect(actorUser, actorUserName, serverIP);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        // Action 3:User1InviteUser2
        act.setCurActionText("Action 3: User1InviteUser2");
        // Action 4:User2AcceptUser1
        waitLock("User1InviteUser2");
        act.setCurActionText("Action 4: User2AcceptUser1");
        P2PActions.afterWaitInvite(actorUserName, pcObserver, 1);
        P2PActions.accept(actorUser, actorUserName, targetUserName);
        P2PActions.afterAccept(actorUserName, pcObserver, 1);
        notifyLock("User2AcceptUser1");
        // Action 5:User1CreateVideoOnlyLocalStream
        act.setCurActionText("Action 5: User1CreateVideoOnlyLocalStream");
        // Action 6:User2CreateVideoOnlyLocalStream
        waitLock("User1CreateVideoOnlyLocalStream");
        act.setCurActionText("Action 6: User2CreateVideoOnlyLocalStream");
        P2PActions.afterWaitCreateLocalCameraStream();
        LocalCameraStream lcs = P2PActions.createLocalCameraStream(actorUserName, true, false);
        Log.d(TAG, "lcs:" + lcs);
        act.attachRender1(lcs);
        notifyLock("User2CreateVideoOnlyLocalStream");
        // Action 7:User1PublishToUser2
        act.setCurActionText("Action 7: User1PublishToUser2");
        // Action 8:User2PublishToUser1
        waitLock("User1PublishToUser2");
        act.setCurActionText("Action 8: User2PublishToUser1");
        P2PActions.afterWaitPublish(actorUserName, pcObserver, 1);
        List<RemoteStream> rslist = pcObserver.addedStream;
        act.attachRender2(rslist.get(0));
        P2PActions.publish(actorUser, actorUserName, targetUserName, lcs);
        P2PActions.afterPublish();

        P2PActions.endStory();
        act.makeToast("Test End!");
    }

    /**
     * Case 3: Test bidirection publish audio-only stream between two users.
     * Actors: User1 and User2
     * Story:
     * 1. User1Connect
     * 2. User2Connect
     * 3. User1InviteUser2
     * 4. User2AcceptUser1
     * 5. User1CreateAudioOnlyLocalStream
     * 6. User2CreateAudioOnlyLocalStream
     * 7. User1PublishToUser2
     * 8. User2PublishToUser1
     */
    public void test_TwoUser_bipublishAudioOnly() {
        initTestActivity();
        // Init variables

        String actorUserName = "User2";
        String targetUserName = "User1";
        PeerClientObserverForTest pcObserver = new PeerClientObserverForTest();
        PeerClientConfiguration peerClientConfiguration = new PeerClientConfiguration();
        PeerClient actorUser = new PeerClient(peerClientConfiguration, new SocketSignalingChannel());
        actorUser.addObserver(pcObserver);

        act.makeToast("Test start, actor:" + actorUserName);
        P2PActions.startStory("testTwoUserInteraction");
        act.setTitle("Actor:" + actorUserName);

        // Action 1:User1Connect
        act.setCurActionText("Action 1: User1Connect");
        // Action 2:User2Connect
        waitLock("User1Connect");
        act.setCurActionText("Action 2: User2Connect");
        P2PActions.afterWaitConnect();
        P2PActions.connect(actorUser, actorUserName, serverIP);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        // Action 3:User1InviteUser2
        act.setCurActionText("Action 3: User1InviteUser2");
        // Action 4:User2AcceptUser1
        waitLock("User1InviteUser2");
        act.setCurActionText("Action 4: User2AcceptUser1");
        P2PActions.afterWaitInvite(actorUserName, pcObserver, 1);
        P2PActions.accept(actorUser, actorUserName, targetUserName);
        P2PActions.afterAccept(actorUserName, pcObserver, 1);
        notifyLock("User2AcceptUser1");
        // Action 5:User1CreateAudioOnlyLocalStream
        act.setCurActionText("Action 5: User1CreateAudioOnlyLocalStream");
        // Action 6:User2CreateAudioOnlyLocalStream
        waitLock("User1CreateAudioOnlyLocalStream");
        act.setCurActionText("Action 6: User2CreateAudioOnlyLocalStream");
        P2PActions.afterWaitCreateLocalCameraStream();
        LocalCameraStream lcs = P2PActions.createLocalCameraStream(actorUserName, false, true);
        Log.d(TAG, "lcs:" + lcs);
        act.attachRender1(lcs);
        notifyLock("User2CreateAudioOnlyLocalStream");
        // Action 7:User1PublishToUser2
        act.setCurActionText("Action 7: User1PublishToUser2");
        // Action 8:User2PublishToUser1
        waitLock("User1PublishToUser2");
        act.setCurActionText("Action 8: User2PublishToUser1");
        P2PActions.afterWaitPublish(actorUserName, pcObserver, 1);
        List<RemoteStream> rslist = pcObserver.addedStream;
        act.attachRender2(rslist.get(0));
        P2PActions.publish(actorUser, actorUserName, targetUserName, lcs);
        P2PActions.afterPublish();

        P2PActions.endStory();
        act.makeToast("Test End!");
    }

    /**
     * Case 4: Test user1 publish normal stream to user2, user2 publish video-only stream to user1.
     * Actors: User1 and User2
     * Story:
     * 1. User1Connect
     * 2. User2Connect
     * 3. User1InviteUser2
     * 4. User2AcceptUser1
     * 5. User1CreateLocalStream
     * 6. User2CreatevideoOnlyLocalStream
     * 7. User1PublishToUser2
     * 8. User2PublishToUser1
     */
    public void test_TwoUser_publishNormalTovideoOnly() {
        initTestActivity();
        // Init variables

        String actorUserName = "User2";
        String targetUserName = "User1";
        PeerClientObserverForTest pcObserver = new PeerClientObserverForTest();
        PeerClientConfiguration peerClientConfiguration = new PeerClientConfiguration();
        PeerClient actorUser = new PeerClient(peerClientConfiguration, new SocketSignalingChannel());
        actorUser.addObserver(pcObserver);

        act.makeToast("Test start, actor:" + actorUserName);
        P2PActions.startStory("testTwoUserInteraction");
        act.setTitle("Actor:" + actorUserName);

        // Action 1:User1Connect
        act.setCurActionText("Action 1: User1Connect");
        // Action 2:User2Connect
        waitLock("User1Connect");
        act.setCurActionText("Action 2: User2Connect");
        P2PActions.afterWaitConnect();
        P2PActions.connect(actorUser, actorUserName, serverIP);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        // Action 3:User1InviteUser2
        act.setCurActionText("Action 3: User1InviteUser2");
        // Action 4:User2AcceptUser1
        waitLock("User1InviteUser2");
        act.setCurActionText("Action 4: User2AcceptUser1");
        P2PActions.afterWaitInvite(actorUserName, pcObserver, 1);
        P2PActions.accept(actorUser, actorUserName, targetUserName);
        P2PActions.afterAccept(actorUserName, pcObserver, 1);
        notifyLock("User2AcceptUser1");
        // Action 5:User1CreateLocalStream
        act.setCurActionText("Action 5: User1CreateLocalStream");
        // Action 6:User2CreatevideoOnlyLocalStream
        waitLock("User1CreateLocalStream");
        act.setCurActionText("Action 6: User2CreatevideoOnlyLocalStream");
        P2PActions.afterWaitCreateLocalCameraStream();
        LocalCameraStream lcs = P2PActions.createLocalCameraStream(actorUserName, true, false);
        Log.d(TAG, "lcs:" + lcs);
        act.attachRender1(lcs);
        notifyLock("User2CreatevideoOnlyLocalStream");
        // Action 7:User1PublishToUser2
        act.setCurActionText("Action 7: User1PublishToUser2");
        // Action 8:User2PublishToUser1
        waitLock("User1PublishToUser2");
        act.setCurActionText("Action 8: User2PublishToUser1");
        P2PActions.afterWaitPublish(actorUserName, pcObserver, 1);
        List<RemoteStream> rslist = pcObserver.addedStream;
        act.attachRender2(rslist.get(0));
        P2PActions.publish(actorUser, actorUserName, targetUserName, lcs);
        P2PActions.afterPublish();

        P2PActions.endStory();
        act.makeToast("Test End!");
    }

    /**
     * Case 5: Test user1 publish normal stream to user2, user2 publish audio-only stream to user1.
     * Actors: User1 and User2
     * Story:
     * 1. User1Connect
     * 2. User2Connect
     * 3. User1InviteUser2
     * 4. User2AcceptUser1
     * 5. User1CreateLocalStream
     * 6. User2CreateAudioOnlyLocalStream
     * 7. User1PublishToUser2
     * 8. User2PublishToUser1
     */
    public void test_TwoUser_publishNormalToAudioOnly() {
        initTestActivity();
        // Init variables

        String actorUserName = "User2";
        String targetUserName = "User1";
        PeerClientObserverForTest pcObserver = new PeerClientObserverForTest();
        PeerClientConfiguration peerClientConfiguration = new PeerClientConfiguration();
        PeerClient actorUser = new PeerClient(peerClientConfiguration, new SocketSignalingChannel());
        actorUser.addObserver(pcObserver);

        act.makeToast("Test start, actor:" + actorUserName);
        P2PActions.startStory("testTwoUserInteraction");
        act.setTitle("Actor:" + actorUserName);

        // Action 1:User1Connect
        act.setCurActionText("Action 1: User1Connect");
        // Action 2:User2Connect
        waitLock("User1Connect");
        act.setCurActionText("Action 2: User2Connect");
        P2PActions.afterWaitConnect();
        P2PActions.connect(actorUser, actorUserName, serverIP);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        // Action 3:User1InviteUser2
        act.setCurActionText("Action 3: User1InviteUser2");
        // Action 4:User2AcceptUser1
        waitLock("User1InviteUser2");
        act.setCurActionText("Action 4: User2AcceptUser1");
        P2PActions.afterWaitInvite(actorUserName, pcObserver, 1);
        P2PActions.accept(actorUser, actorUserName, targetUserName);
        P2PActions.afterAccept(actorUserName, pcObserver, 1);
        notifyLock("User2AcceptUser1");
        // Action 5:User1CreateLocalStream
        act.setCurActionText("Action 5: User1CreateLocalStream");
        // Action 6:User2CreateAudioOnlyLocalStream
        waitLock("User1CreateLocalStream");
        act.setCurActionText("Action 6: User2CreateAudioOnlyLocalStream");
        P2PActions.afterWaitCreateLocalCameraStream();
        LocalCameraStream lcs = P2PActions.createLocalCameraStream(actorUserName, false, true);
        Log.d(TAG, "lcs:" + lcs);
        act.attachRender1(lcs);
        notifyLock("User2CreateAudioOnlyLocalStream");
        // Action 7:User1PublishToUser2
        act.setCurActionText("Action 7: User1PublishToUser2");
        // Action 8:User2PublishToUser1
        waitLock("User1PublishToUser2");
        act.setCurActionText("Action 8: User2PublishToUser1");
        P2PActions.afterWaitPublish(actorUserName, pcObserver, 1);
        List<RemoteStream> rslist = pcObserver.addedStream;
        act.attachRender2(rslist.get(0));
        P2PActions.publish(actorUser, actorUserName, targetUserName, lcs);
        P2PActions.afterPublish();

        P2PActions.endStory();
        act.makeToast("Test End!");
    }

    /**
     * Case 6: Test user1 publish video-only stream to user2, user2 publish audio-only stream to user1.
     * Actors: User1 and User2
     * Story:
     * 1. User1Connect
     * 2. User2Connect
     * 3. User1InviteUser2
     * 4. User2AcceptUser1
     * 5. User1CreatevideoOnlyLocalStream
     * 6. User2CreateAudioOnlyLocalStream
     * 7. User1PublishToUser2
     * 8. User2PublishToUser1
     */
    public void test_TwoUser_publishvideoOnlyToAudioOnly() {
        initTestActivity();
        // Init variables

        String actorUserName = "User2";
        String targetUserName = "User1";
        PeerClientObserverForTest pcObserver = new PeerClientObserverForTest();
        PeerClientConfiguration peerClientConfiguration = new PeerClientConfiguration();
        PeerClient actorUser = new PeerClient(peerClientConfiguration, new SocketSignalingChannel());
        actorUser.addObserver(pcObserver);

        act.makeToast("Test start, actor:" + actorUserName);
        P2PActions.startStory("testTwoUserInteraction");
        act.setTitle("Actor:" + actorUserName);

        // Action 1:User1Connect
        act.setCurActionText("Action 1: User1Connect");
        // Action 2:User2Connect
        waitLock("User1Connect");
        act.setCurActionText("Action 2: User2Connect");
        P2PActions.afterWaitConnect();
        P2PActions.connect(actorUser, actorUserName, serverIP);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        // Action 3:User1InviteUser2
        act.setCurActionText("Action 3: User1InviteUser2");
        // Action 4:User2AcceptUser1
        waitLock("User1InviteUser2");
        act.setCurActionText("Action 4: User2AcceptUser1");
        P2PActions.afterWaitInvite(actorUserName, pcObserver, 1);
        P2PActions.accept(actorUser, actorUserName, targetUserName);
        P2PActions.afterAccept(actorUserName, pcObserver, 1);
        notifyLock("User2AcceptUser1");
        // Action 5:User1CreatevideoOnlyLocalStream
        act.setCurActionText("Action 5: User1CreatevideoOnlyLocalStream");
        // Action 6:User2CreateAudioOnlyLocalStream
        waitLock("User1CreatevideoOnlyLocalStream");
        act.setCurActionText("Action 6: User2CreateAudioOnlyLocalStream");
        P2PActions.afterWaitCreateLocalCameraStream();
        LocalCameraStream lcs = P2PActions.createLocalCameraStream(actorUserName, false, true);
        Log.d(TAG, "lcs:" + lcs);
        act.attachRender1(lcs);
        notifyLock("User2CreateAudioOnlyLocalStream");
        // Action 7:User1PublishToUser2
        act.setCurActionText("Action 7: User1PublishToUser2");
        // Action 8:User2PublishToUser1
        waitLock("User1PublishToUser2");
        act.setCurActionText("Action 8: User2PublishToUser1");
        P2PActions.afterWaitPublish(actorUserName, pcObserver, 1);
        List<RemoteStream> rslist = pcObserver.addedStream;
        act.attachRender2(rslist.get(0));
        P2PActions.publish(actorUser, actorUserName, targetUserName, lcs);
        P2PActions.afterPublish();

        P2PActions.endStory();
        act.makeToast("Test End!");
    }

    /**
     * Case 7: Test bidirection unpublish between two users.
     * Actors: User1 and User2
     * Story:
     * 1. User1Connect
     * 2. User2Connect
     * 3. User1InviteUser2
     * 4. User2AcceptUser1
     * 5. User1CreateLocalStream
     * 6. User2CreateLocalStream
     * 7. User1PublishToUser2
     * 8. User2PublishToUser1
     * 9. User1UnpublishToUser2
     * 10. User2UnpublishToUser1
     */
    public void test_TwoUser_biunpublish() {
        initTestActivity();
        // Init variables

        String actorUserName = "User2";
        String targetUserName = "User1";
        PeerClientObserverForTest pcObserver = new PeerClientObserverForTest();
        PeerClientConfiguration peerClientConfiguration = new PeerClientConfiguration();
        PeerClient actorUser = new PeerClient(peerClientConfiguration, new SocketSignalingChannel());
        actorUser.addObserver(pcObserver);
        act.makeToast("Test start, actor:" + actorUserName);
        act.setTitle("Actor:" + actorUserName);
        // Action 1:User1Connect
        act.setCurActionText("Action 1: User1Connect");
        // Action 2:User2Connect
        waitLock("User1Connect");
        act.setCurActionText("Action 2: User2Connect");
        P2PActions.afterWaitConnect();
        P2PActions.connect(actorUser, actorUserName, serverIP);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        // Action 3:User1InviteUser2
        act.setCurActionText("Action 3: User1InviteUser2");
        // Action 4:User2AcceptUser1
        waitLock("User1InviteUser2");
        act.setCurActionText("Action 4: User2AcceptUser1");
        P2PActions.afterWaitInvite(actorUserName, pcObserver, 1);
        P2PActions.accept(actorUser, actorUserName, targetUserName);
        P2PActions.afterAccept(actorUserName, pcObserver, 1);
        notifyLock("User2AcceptUser1");
        // Action 5:User1CreateLocalStream
        act.setCurActionText("Action 5: User1CreateLocalStream");
        // Action 6:User2CreateLocalStream
        waitLock("User1CreateLocalStream");
        act.setCurActionText("Action 6: User2CreateLocalStream");
        P2PActions.afterWaitCreateLocalCameraStream();
        LocalCameraStream lcs = P2PActions.createLocalCameraStream(actorUserName, true, true);
        Log.d(TAG, "lcs:" + lcs);
        act.attachRender1(lcs);
        notifyLock("User2CreateLocalStream");
        // Action 7:User1PublishToUser2
        act.setCurActionText("Action 7: User1PublishToUser2");
        // Action 8:User2PublishToUser1
        waitLock("User1PublishToUser2");
        act.setCurActionText("Action 8: User2PublishToUser1");
        P2PActions.afterWaitPublish(actorUserName, pcObserver, 1);
        List<RemoteStream> rslist = pcObserver.addedStream;
        act.attachRender2(rslist.get(0));
        P2PActions.publish(actorUser, actorUserName, targetUserName, lcs);
        P2PActions.afterPublish();
        notifyLock("User2PublishToUser1");
        // Action 9:User1UnpublishToUser2
        act.setCurActionText("Action 9: User1UnpublishToUser2");
        // Action 10:User2UnpublishToUser1
        waitLock("User1UnpublishToUser2");
        act.setCurActionText("Action 10: User2UnpublishToUser1");
        P2PActions.afterWaitUnpublish(actorUserName, pcObserver, 1);
        P2PActions.unpublish(actorUser, actorUserName, targetUserName, lcs);
        P2PActions.afterUnpublish();
        P2PActions.endStory();
        act.makeToast("Test End!");
    }

    /**
     * Case 8: Test bipublish-bisend between two users.
     * Actors: User1 and User2
     * Story:
     * 1. User1Connect
     * 2. User2Connect
     * 3. User1InviteUser2
     * 4. User2AcceptUser1
     * 5. User1CreateLocalStream
     * 6. User2CreateLocalStream
     * 7. User1PublishToUser2
     * 8. User2PublishToUser1
     * 9. User1SendTwoMessagesToUser2
     * 10. User2SendTwoMessagesToUser1
     */
    public void test_TwoUser_bipublishBisend() {
        initTestActivity();
        // Init variables

        String actorUserName = "User2";
        String targetUserName = "User1";
        PeerClientObserverForTest pcObserver = new PeerClientObserverForTest();
        PeerClientConfiguration peerClientConfiguration = new PeerClientConfiguration();
        PeerClient actorUser = new PeerClient(peerClientConfiguration, new SocketSignalingChannel());
        actorUser.addObserver(pcObserver);
        act.makeToast("Test start, actor:" + actorUserName);
        act.setTitle("Actor:" + actorUserName);
        // Action 1:User1Connect
        act.setCurActionText("Action 1: User1Connect");
        // Action 2:User2Connect
        waitLock("User1Connect");
        act.setCurActionText("Action 2: User2Connect");
        P2PActions.afterWaitConnect();
        P2PActions.connect(actorUser, actorUserName, serverIP);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        // Action 3:User1InviteUser2
        act.setCurActionText("Action 3: User1InviteUser2");
        // Action 4:User2AcceptUser1
        waitLock("User1InviteUser2");
        act.setCurActionText("Action 4: User2AcceptUser1");
        P2PActions.afterWaitInvite(actorUserName, pcObserver, 1);
        P2PActions.accept(actorUser, actorUserName, targetUserName);
        P2PActions.afterAccept(actorUserName, pcObserver, 1);
        notifyLock("User2AcceptUser1");
        // Action 5:User1CreateLocalStream
        act.setCurActionText("Action 5: User1CreateLocalStream");
        // Action 6:User2CreateLocalStream
        waitLock("User1CreateLocalStream");
        act.setCurActionText("Action 6: User2CreateLocalStream");
        P2PActions.afterWaitCreateLocalCameraStream();
        LocalCameraStream lcs = P2PActions.createLocalCameraStream(actorUserName, true, true);
        Log.d(TAG, "lcs:" + lcs);
        act.attachRender1(lcs);
        notifyLock("User2CreateLocalStream");
        // Action 7:User1PublishToUser2
        act.setCurActionText("Action 7: User1PublishToUser2");
        // Action 8:User2PublishToUser1
        waitLock("User1PublishToUser2");
        act.setCurActionText("Action 8: User2PublishToUser1");
        P2PActions.afterWaitPublish(actorUserName, pcObserver, 1);
        List<RemoteStream> rslist = pcObserver.addedStream;
        act.attachRender2(rslist.get(0));
        P2PActions.publish(actorUser, actorUserName, targetUserName, lcs);
        P2PActions.afterPublish();
        notifyLock("User2PublishToUser1");
        Log.d(TAG, "OBSERVER:" + pcObserver);
        // Action 9:User1SendTwoMessagesToUser2
        act.setCurActionText("Action 9: User1SendTwoMessagesToUser2");
        // Action 10:User2SendTwoMessagesToUser1
        waitLock("User1SendTwoMessagesToUser2");
        act.setCurActionText("Action 10: User2SendTwoMessagesToUser1");
        P2PActions.afterWaitSend(actorUserName, pcObserver, 2, 2);
        P2PActions.send(actorUser, actorUserName, targetUserName,
                actorUserName + " send message1 to " + targetUserName);
        P2PActions.send(actorUser, actorUserName, targetUserName,
                actorUserName + " send message2 to " + targetUserName);
        P2PActions.afterSend();
        P2PActions.endStory();
        act.makeToast("Test End!");
    }

    /**
    * Case 9: Test bidirection unpublish, then bidirection send.
    * Actors: User1 and User2
    * Story:
    * 1. User1Connect
    * 2. User2Connect
    * 3. User1InviteUser2
    * 4. User2AcceptUser1
    * 5. User1CreateLocalStream
    * 6. User2CreateLocalStream
    * 7. User1PublishToUser2
    * 8. User2PublishToUser1
    * 9. User1UnpublishToUser2
    * 10. User2UnpublishToUser1
    * 11. User1SendTwoMessagesToUser2
    * 12. User2SendTwoMessagesToUser1
    */
    public void test_TwoUser_biunpublishBisend() {
        initTestActivity();
        // Init variables

        String actorUserName = "User2";
        String targetUserName = "User1";
        PeerClientObserverForTest pcObserver = new PeerClientObserverForTest();
        PeerClientConfiguration peerClientConfiguration = new PeerClientConfiguration();
        PeerClient actorUser = new PeerClient(peerClientConfiguration, new SocketSignalingChannel());
        actorUser.addObserver(pcObserver);
        act.makeToast("Test start, actor:" + actorUserName);
        act.setTitle("Actor:" + actorUserName);
        // Action 1:User1Connect
        act.setCurActionText("Action 1: User1Connect");
        // Action 2:User2Connect
        waitLock("User1Connect");
        act.setCurActionText("Action 2: User2Connect");
        P2PActions.afterWaitConnect();
        P2PActions.connect(actorUser, actorUserName, serverIP);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        // Action 3:User1InviteUser2
        act.setCurActionText("Action 3: User1InviteUser2");
        // Action 4:User2AcceptUser1
        waitLock("User1InviteUser2");
        act.setCurActionText("Action 4: User2AcceptUser1");
        P2PActions.afterWaitInvite(actorUserName, pcObserver, 1);
        P2PActions.accept(actorUser, actorUserName, targetUserName);
        P2PActions.afterAccept(actorUserName, pcObserver, 1);
        notifyLock("User2AcceptUser1");
        // Action 5:User1CreateLocalStream
        act.setCurActionText("Action 5: User1CreateLocalStream");
        // Action 6:User2CreateLocalStream
        waitLock("User1CreateLocalStream");
        act.setCurActionText("Action 6: User2CreateLocalStream");
        P2PActions.afterWaitCreateLocalCameraStream();
        LocalCameraStream lcs = P2PActions.createLocalCameraStream(actorUserName, true, true);
        Log.d(TAG, "lcs:" + lcs);
        act.attachRender1(lcs);
        notifyLock("User2CreateLocalStream");
        // Action 7:User1PublishToUser2
        act.setCurActionText("Action 7: User1PublishToUser2");
        // Action 8:User2PublishToUser1
        waitLock("User1PublishToUser2");
        act.setCurActionText("Action 8: User2PublishToUser1");
        P2PActions.afterWaitPublish(actorUserName, pcObserver, 1);
        List<RemoteStream> rslist = pcObserver.addedStream;
        act.attachRender2(rslist.get(0));
        P2PActions.publish(actorUser, actorUserName, targetUserName, lcs);
        P2PActions.afterPublish();
        notifyLock("User2PublishToUser1");
        // Action 9:User1UnpublishToUser2
        act.setCurActionText("Action 9: User1UnpublishToUser2");
        // Action 10:User2UnpublishToUser1
        waitLock("User1UnpublishToUser2");
        act.setCurActionText("Action 10: User2UnpublishToUser1");
        P2PActions.afterWaitUnpublish(actorUserName, pcObserver, 1);
        P2PActions.unpublish(actorUser, actorUserName, targetUserName, lcs);
        P2PActions.afterUnpublish();
        notifyLock("User2UnpublishToUser1");
        // Action 11:User1SendTwoMessagesToUser2
        act.setCurActionText("Action 11: User1SendTwoMessagesToUser2");
        // Action 10:User2SendTwoMessagesToUser1
        waitLock("User1SendTwoMessagesToUser2");
        act.setCurActionText("Action 12: User2SendTwoMessagesToUser1");
        P2PActions.afterWaitSend(actorUserName, pcObserver, 2, 2);
        P2PActions.send(actorUser, actorUserName, targetUserName,
                actorUserName + " send message1 to " + targetUserName);
        P2PActions.send(actorUser, actorUserName, targetUserName,
                actorUserName + " send message2 to " + targetUserName);
        P2PActions.afterSend();
        P2PActions.endStory();
        act.makeToast("Test End!");
    }

    /**
    * Case 10: Test bidirection close stream then republish.
    * Actors: User1 and User2
    * Story:
    * 1. User1Connect
    * 2. User2Connect
    * 3. User1InviteUser2
    * 4. User2AcceptUser1
    * 5. User1CreateLocalStream
    * 6. User2CreateLocalStream
    * 7. User1PublishToUser2
    * 8. User2PublishToUser1
    * 9. User1CloseLocalStream
    * 10. User2CloseLocalStream
    * 11. User1CreateLocalStream
    * 12. User2CreateLocalStream
    * 13. User1RepublishToUser2
    * 14. User2RepublishToUser1
    */
    public void test_TwoUser_biclosestreamRepublish() {
        initTestActivity();
        // Init variables

        String actorUserName = "User2";
        String targetUserName = "User1";
        PeerClientObserverForTest pcObserver = new PeerClientObserverForTest();
        PeerClientConfiguration peerClientConfiguration = new PeerClientConfiguration();
        PeerClient actorUser = new PeerClient(peerClientConfiguration, new SocketSignalingChannel());
        actorUser.addObserver(pcObserver);
        act.makeToast("Test start, actor:" + actorUserName);
        act.setTitle("Actor:" + actorUserName);
        // Action 1:User1Connect
        act.setCurActionText("Action 1: User1Connect");
        // Action 2:User2Connect
        waitLock("User1Connect");
        act.setCurActionText("Action 2: User2Connect");
        P2PActions.afterWaitConnect();
        P2PActions.connect(actorUser, actorUserName, serverIP);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        // Action 3:User1InviteUser2
        act.setCurActionText("Action 3: User1InviteUser2");
        // Action 4:User2AcceptUser1
        waitLock("User1InviteUser2");
        act.setCurActionText("Action 4: User2AcceptUser1");
        P2PActions.afterWaitInvite(actorUserName, pcObserver, 1);
        P2PActions.accept(actorUser, actorUserName, targetUserName);
        P2PActions.afterAccept(actorUserName, pcObserver, 1);
        notifyLock("User2AcceptUser1");
        // Action 5:User1CreateLocalStream
        act.setCurActionText("Action 5: User1CreateLocalStream");
        // Action 6:User2CreateLocalStream
        waitLock("User1CreateLocalStream");
        act.setCurActionText("Action 6: User2CreateLocalStream");
        P2PActions.afterWaitCreateLocalCameraStream();
        LocalCameraStream lcs = P2PActions.createLocalCameraStream(actorUserName, true, true);
        Log.d(TAG, "lcs:" + lcs);
        act.attachRender1(lcs);
        notifyLock("User2CreateLocalStream");
        // Action 7:User1PublishToUser2
        act.setCurActionText("Action 7: User1PublishToUser2");
        // Action 8:User2PublishToUser1
        waitLock("User1PublishToUser2");
        act.setCurActionText("Action 8: User2PublishToUser1");
        P2PActions.afterWaitPublish(actorUserName, pcObserver, 1);
        List<RemoteStream> rslist = pcObserver.addedStream;
        act.attachRender2(rslist.get(0));
        P2PActions.publish(actorUser, actorUserName, targetUserName, lcs);
        P2PActions.afterPublish();
        notifyLock("User2PublishToUser1");
        // Action 9:User1CloseLocalStream
        act.setCurActionText("Action 9: User1CloseLocalStream");
        // Action 10:User2CloseLocalStream
        waitLock("User1CloseLocalStream");
        act.setCurActionText("Action 10: User2CloseLocalStream");
        P2PActions.closeLocalCameraStream(actorUserName, lcs);
        notifyLock("User2CloseLocalStream");
        // Action 11:User1CreateLocalStream
        act.setCurActionText("Action 11: User1CreateLocalStream");
        // Action 12:User2CreateLocalStream
        waitLock("User1CreateLocalStream");
        act.setCurActionText("Action 12: User2CreateLocalStream");
        P2PActions.afterWaitCreateLocalCameraStream();
        lcs = P2PActions.createLocalCameraStream(actorUserName, true, true);
        Log.d(TAG, "lcs:" + lcs);
        act.attachRender1(lcs);
        notifyLock("User2CreateLocalStream");
        // Action 13:User1RepublishToUser2
        act.setCurActionText("Action 13: User1RepublishToUser2");
        // Action 14:User2RepublishToUser1
        waitLock("User1RepublishToUser2");
        act.setCurActionText("Action 14: User2RepublishToUser1");
        P2PActions.afterWaitPublish(actorUserName, pcObserver, 2);
        rslist = pcObserver.addedStream;
        act.attachRender2(rslist.get(1));
        P2PActions.publish(actorUser, actorUserName, targetUserName, lcs);
        P2PActions.afterPublish();
        notifyLock("User2RepublishToUser1");
        P2PActions.endStory();
        act.makeToast("Test End!");
    }

    /**
    * Case 11: Test bidirection close stream then send messages.
    * Actors: User1 and User2
    * Story:
    * 1. User1Connect
    * 2. User2Connect
    * 3. User1InviteUser2
    * 4. User2AcceptUser1
    * 5. User1CreateLocalStream
    * 6. User2CreateLocalStream
    * 7. User1PublishToUser2
    * 8. User2PublishToUser1
    * 9. User1CloseLocalStream
    * 10. User2CloseLocalStream
    * 11. User1SendTwoMessagesToUser2
    * 12. User2SendTwoMessagesToUser1
    */
    public void test_TwoUser_biclosestreamSend() {
        initTestActivity();
        // Init variables

        String actorUserName = "User2";
        String targetUserName = "User1";
        PeerClientObserverForTest pcObserver = new PeerClientObserverForTest();
        PeerClientConfiguration peerClientConfiguration = new PeerClientConfiguration();
        PeerClient actorUser = new PeerClient(peerClientConfiguration, new SocketSignalingChannel());
        actorUser.addObserver(pcObserver);
        act.makeToast("Test start, actor:" + actorUserName);
        act.setTitle("Actor:" + actorUserName);
        // Action 1:User1Connect
        act.setCurActionText("Action 1: User1Connect");
        // Action 2:User2Connect
        waitLock("User1Connect");
        act.setCurActionText("Action 2: User2Connect");
        P2PActions.afterWaitConnect();
        P2PActions.connect(actorUser, actorUserName, serverIP);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        // Action 3:User1InviteUser2
        act.setCurActionText("Action 3: User1InviteUser2");
        // Action 4:User2AcceptUser1
        waitLock("User1InviteUser2");
        act.setCurActionText("Action 4: User2AcceptUser1");
        P2PActions.afterWaitInvite(actorUserName, pcObserver, 1);
        P2PActions.accept(actorUser, actorUserName, targetUserName);
        P2PActions.afterAccept(actorUserName, pcObserver, 1);
        notifyLock("User2AcceptUser1");
        // Action 5:User1CreateLocalStream
        act.setCurActionText("Action 5: User1CreateLocalStream");
        // Action 6:User2CreateLocalStream
        waitLock("User1CreateLocalStream");
        act.setCurActionText("Action 6: User2CreateLocalStream");
        P2PActions.afterWaitCreateLocalCameraStream();
        LocalCameraStream lcs = P2PActions.createLocalCameraStream(actorUserName, true, true);
        Log.d(TAG, "lcs:" + lcs);
        act.attachRender1(lcs);
        notifyLock("User2CreateLocalStream");
        // Action 7:User1PublishToUser2
        act.setCurActionText("Action 7: User1PublishToUser2");
        // Action 8:User2PublishToUser1
        waitLock("User1PublishToUser2");
        act.setCurActionText("Action 8: User2PublishToUser1");
        P2PActions.afterWaitPublish(actorUserName, pcObserver, 1);
        List<RemoteStream> rslist = pcObserver.addedStream;
        act.attachRender2(rslist.get(0));
        P2PActions.publish(actorUser, actorUserName, targetUserName, lcs);
        P2PActions.afterPublish();
        notifyLock("User2PublishToUser1");
        // Action 9:User1CloseLocalStream
        act.setCurActionText("Action 9: User1CloseLocalStream");
        // Action 10:User2CloseLocalStream
        waitLock("User1CloseLocalStream");
        act.setCurActionText("Action 10: User2CloseLocalStream");
        P2PActions.closeLocalCameraStream(actorUserName, lcs);
        notifyLock("User2CloseLocalStream");
        // Action 11:User1SendTwoMessagesToUser2
        act.setCurActionText("Action 11: User1SendTwoMessagesToUser2");
        // Action 12:User2SendTwoMessagesToUser1
        waitLock("User1SendTwoMessagesToUser2");
        act.setCurActionText("Action 10: User2SendTwoMessagesToUser1");
        P2PActions.afterWaitSend(actorUserName, pcObserver, 2, 2);
        P2PActions.send(actorUser, actorUserName, targetUserName,
                actorUserName + " send message1 to " + targetUserName);
        P2PActions.send(actorUser, actorUserName, targetUserName,
                actorUserName + " send message2 to " + targetUserName);
        notifyLock("User2SendTwoMessagesToUser1");
        SystemClock.sleep(waitingTime);
        P2PActions.endStory();
        act.makeToast("Test End!");
    }

    /**
    * Case 12: Test stop by inviter and former accepter reinvite.
    * Actors: User1 and User2
    * Story:
    * 1. User1Connect
    * 2. User2Connect
    * 3. User1InviteUser2
    * 4. User2AcceptUser1
    * 5. User1CreateLocalStream
    * 6. User2CreateLocalStream
    * 7. User1PublishToUser2
    * 8. User2PublishToUser1
    * 9. User1StopChatWithUser2
    * 10. User2InviteUser1
    * 11. User1AcceptUser2
    * 12. User2PublishToUser1
    * 13. User1PublishToUser2
    */
    public void test_TwoUser_stopReinvite() {
        initTestActivity();
        // Init variables

        String actorUserName = "User2";
        String targetUserName = "User1";
        PeerClientObserverForTest pcObserver = new PeerClientObserverForTest();
        PeerClientConfiguration peerClientConfiguration = new PeerClientConfiguration();
        PeerClient actorUser = new PeerClient(peerClientConfiguration, new SocketSignalingChannel());
        actorUser.addObserver(pcObserver);
        act.makeToast("Test start, actor:" + actorUserName);
        act.setTitle("Actor:" + actorUserName);
        // Action 1:User1Connect
        act.setCurActionText("Action 1: User1Connect");
        // Action 2:User2Connect
        waitLock("User1Connect");
        act.setCurActionText("Action 2: User2Connect");
        P2PActions.afterWaitConnect();
        P2PActions.connect(actorUser, actorUserName, serverIP);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        // Action 3:User1InviteUser2
        act.setCurActionText("Action 3: User1InviteUser2");
        // Action 4:User2AcceptUser1
        waitLock("User1InviteUser2");
        act.setCurActionText("Action 4: User2AcceptUser1");
        P2PActions.afterWaitInvite(actorUserName, pcObserver, 1);
        P2PActions.accept(actorUser, actorUserName, targetUserName);
        P2PActions.afterAccept(actorUserName, pcObserver, 1);
        notifyLock("User2AcceptUser1");
        // Action 5:User1CreateLocalStream
        act.setCurActionText("Action 5: User1CreateLocalStream");
        // Action 6:User2CreateLocalStream
        waitLock("User1CreateLocalStream");
        act.setCurActionText("Action 6: User2CreateLocalStream");
        P2PActions.afterWaitCreateLocalCameraStream();
        LocalCameraStream lcs = P2PActions.createLocalCameraStream(actorUserName, true, true);
        Log.d(TAG, "lcs:" + lcs);
        act.attachRender1(lcs);
        notifyLock("User2CreateLocalStream");
        // Action 7:User1PublishToUser2
        act.setCurActionText("Action 7: User1PublishToUser2");
        // Action 8:User2PublishToUser1
        waitLock("User1PublishToUser2");
        act.setCurActionText("Action 8: User2PublishToUser1");
        P2PActions.afterWaitPublish(actorUserName, pcObserver, 1);
        List<RemoteStream> rslist = pcObserver.addedStream;
        act.attachRender2(rslist.get(0));
        P2PActions.publish(actorUser, actorUserName, targetUserName, lcs);
        P2PActions.afterPublish();
        notifyLock("User2PublishToUser1");
        // Action 9:User1StopChatWithUser2
        act.setCurActionText("Action 9:User1StopChatWithUser2");
        // Action 10:User2InviteUser1
        waitLock("User1StopChatWithUser2");
        act.setCurActionText("Action 10:User2InviteUser1");
        P2PActions.afterWaitStop(actorUserName, pcObserver, 1);
        P2PActions.invite(actorUser, actorUserName, targetUserName);
        notifyLock("User2InviteUser1");
        // Action 11:User1AcceptUser2
        act.setCurActionText("Action 11:User1AcceptUser2");
        // Action 12:User2PublishToUser1
        waitLock("User1AcceptUser2");
        act.setCurActionText("Action 12:User2PublishToUser1");
        P2PActions.afterWaitAccept(actorUserName, pcObserver, 1, 2);
        P2PActions.publish(actorUser, actorUserName, targetUserName, lcs);
        notifyLock("User2PublishToUser1");
        // Action 13:User1PublishToUser2
        act.setCurActionText("Action 13:User1PublishToUser2");
        waitLock("User1PublishToUser2");
        rslist = pcObserver.addedStream;
        act.attachRender2(rslist.get(1));
        P2PActions.afterWaitPublish(actorUserName, pcObserver, 2);
        P2PActions.endStory();
        act.makeToast("Test End!");
    }

    /**
     * Case 13: Test inviter disconnect then rejoin.
     * Actors: User1 and User2
     * Story:
     * 1. User1Connect
     * 2. User2Connect
     * 3. User1InviteUser2
     * 4. User2AcceptUser1
     * 5. User1CreateLocalStream
     * 6. User2CreateLocalStream
     * 7. User1PublishToUser2
     * 8. User2PublishToUser1
     * 9. User1Disconnect
     * 10. User2CheckChatStopped
     * 11. User1LoginAndInviteUser2
     * 12. User2AcceptUser1
     * 13. User1PublishToUser2
     * 14. User2PublishToUser1
     */
    public void test_TwoUser_inviterdisconnectReinvite() {
        initTestActivity();
        // Init variables

        String actorUserName = "User2";
        String targetUserName = "User1";
        PeerClientObserverForTest pcObserver = new PeerClientObserverForTest();
        PeerClientConfiguration peerClientConfiguration = new PeerClientConfiguration();
        PeerClient actorUser = new PeerClient(peerClientConfiguration, new SocketSignalingChannel());
        actorUser.addObserver(pcObserver);
        act.makeToast("Test start, actor:" + actorUserName);
        act.setTitle("Actor:" + actorUserName);
        // Action 1:User1Connect
        act.setCurActionText("Action 1: User1Connect");
        // Action 2:User2Connect
        waitLock("User1Connect");
        act.setCurActionText("Action 2: User2Connect");
        P2PActions.afterWaitConnect();
        P2PActions.connect(actorUser, actorUserName, serverIP);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        // Action 3:User1InviteUser2
        act.setCurActionText("Action 3: User1InviteUser2");
        // Action 4:User2AcceptUser1
        waitLock("User1InviteUser2");
        act.setCurActionText("Action 4: User2AcceptUser1");
        P2PActions.afterWaitInvite(actorUserName, pcObserver, 1);
        P2PActions.accept(actorUser, actorUserName, targetUserName);
        P2PActions.afterAccept(actorUserName, pcObserver, 1);
        notifyLock("User2AcceptUser1");
        // Action 5:User1CreateLocalStream
        act.setCurActionText("Action 5: User1CreateLocalStream");
        // Action 6:User2CreateLocalStream
        waitLock("User1CreateLocalStream");
        act.setCurActionText("Action 6: User2CreateLocalStream");
        P2PActions.afterWaitCreateLocalCameraStream();
        LocalCameraStream lcs = P2PActions.createLocalCameraStream(actorUserName, true, true);
        Log.d(TAG, "lcs:" + lcs);
        act.attachRender1(lcs);
        notifyLock("User2CreateLocalStream");
        // Action 7:User1PublishToUser2
        act.setCurActionText("Action 7: User1PublishToUser2");
        // Action 8:User2PublishToUser1
        waitLock("User1PublishToUser2");
        act.setCurActionText("Action 8: User2PublishToUser1");
        P2PActions.afterWaitPublish(actorUserName, pcObserver, 1);
        List<RemoteStream> rslist = pcObserver.addedStream;
        act.attachRender2(rslist.get(0));
        P2PActions.publish(actorUser, actorUserName, targetUserName, lcs);
        P2PActions.afterPublish();
        notifyLock("User2PublishToUser1");
        // Action 9:User1Disconnect
        act.setCurActionText("Action 9:User1Disconnect");
        // Action 10:User2CheckChatStopped
        waitLock("User1Disconnect");
        act.setCurActionText("Action 10:User2CheckChatStopped");
        P2PActions.afterWaitStop(actorUserName, pcObserver, 1);
        notifyLock("User2CheckChatStopped");
        // Action 11:User1LoginAndInviteUser2
        act.setCurActionText("Action 11:User1LoginAndInviteUser2");
        // Action 12:User2AcceptUser1
        waitLock("User1LoginAndInviteUser2");
        act.setCurActionText("Action 12:User2AcceptUser1");
        P2PActions.afterWaitInvite(actorUserName, pcObserver, 2);
        P2PActions.accept(actorUser, actorUserName, targetUserName);
        P2PActions.afterAccept(actorUserName, pcObserver, 2);
        notifyLock("User2AcceptUser1");
        // Action 13:User1PublishToUser2
        act.setCurActionText("Action 13:User1PublishToUser2");
        // Action 14:User2PublishToUser1
        waitLock("User1PublishToUser2");
        rslist = pcObserver.addedStream;
        act.attachRender2(rslist.get(1));
        P2PActions.afterWaitPublish(actorUserName, pcObserver, 2);
        P2PActions.publish(actorUser, actorUserName, targetUserName, lcs);
        notifyLock("User2PublishToUser1");
        SystemClock.sleep(waitingTime);
        P2PActions.endStory();
        act.makeToast("Test End!");
    }

    /**
     * Case 14: Test accepter disconnect then rejoin.
     * Actors: User1 and User2
     * Story:
     * 1. User1Connect
     * 2. User2Connect
     * 3. User1InviteUser2
     * 4. User2AcceptUser1
     * 5. User1CreateLocalStream
     * 6. User2CreateLocalStream
     * 7. User1PublishToUser2
     * 8. User2PublishToUser1
     * 9. User1CheckStreamAdded
     * 10. User2Disconnect
     * 11. User1CheckChatStopped
     * 12. User2Connect
     * 13. User1InviteUser2
     * 14. User2AcceptUser1
     * 15. User1PublishToUser2
     * 16. User2PublishToUser1
     */
    public void test_TwoUser_accepterdisconnectReinvite() {
        initTestActivity();
        // Init variables

        String actorUserName = "User2";
        String targetUserName = "User1";
        PeerClientObserverForTest pcObserver = new PeerClientObserverForTest();
        PeerClientConfiguration peerClientConfiguration = new PeerClientConfiguration();
        PeerClient actorUser = new PeerClient(peerClientConfiguration, new SocketSignalingChannel());
        actorUser.addObserver(pcObserver);
        act.makeToast("Test start, actor:" + actorUserName);
        act.setTitle("Actor:" + actorUserName);
        // Action 1:User1Connect
        act.setCurActionText("Action 1: User1Connect");
        // Action 2:User2Connect
        waitLock("User1Connect");
        act.setCurActionText("Action 2: User2Connect");
        P2PActions.afterWaitConnect();
        P2PActions.connect(actorUser, actorUserName, serverIP);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        // Action 3:User1InviteUser2
        act.setCurActionText("Action 3: User1InviteUser2");
        // Action 4:User2AcceptUser1
        waitLock("User1InviteUser2");
        act.setCurActionText("Action 4: User2AcceptUser1");
        P2PActions.afterWaitInvite(actorUserName, pcObserver, 1);
        P2PActions.accept(actorUser, actorUserName, targetUserName);
        P2PActions.afterAccept(actorUserName, pcObserver, 1);
        notifyLock("User2AcceptUser1");
        // Action 5:User1CreateLocalStream
        act.setCurActionText("Action 5: User1CreateLocalStream");
        // Action 6:User2CreateLocalStream
        waitLock("User1CreateLocalStream");
        act.setCurActionText("Action 6: User2CreateLocalStream");
        P2PActions.afterWaitCreateLocalCameraStream();
        LocalCameraStream lcs = P2PActions.createLocalCameraStream(actorUserName, true, true);
        Log.d(TAG, "lcs:" + lcs);
        act.attachRender1(lcs);
        notifyLock("User2CreateLocalStream");
        // Action 7:User1PublishToUser2
        act.setCurActionText("Action 7: User1PublishToUser2");
        // Action 8:User2PublishToUser1
        waitLock("User1PublishToUser2");
        act.setCurActionText("Action 8: User2PublishToUser1");
        P2PActions.afterWaitPublish(actorUserName, pcObserver, 1);
        List<RemoteStream> rslist = pcObserver.addedStream;
        act.attachRender2(rslist.get(0));
        P2PActions.publish(actorUser, actorUserName, targetUserName, lcs);
        P2PActions.afterPublish();
        notifyLock("User2PublishToUser1");
        // Action 9: User1CheckStreamAdded
        act.setCurActionText("Action 9: User1CheckStreamAdded");
        // Action 10: User2Disconnect
        waitLock("User1CheckStreamAdded");
        act.setCurActionText("Action 10: User2Disconnect");
        P2PActions.disconnect(actorUser, actorUserName);
        P2PActions.afterDisconnect(actorUserName, pcObserver, 1);
        notifyLock("User2Disconnect");
        // Action 11: User1CheckChatStopped
        act.setCurActionText("Action 11: User1CheckChatStopped");
        // Action 12:User2Connect
        waitLock("User1CheckChatStopped");
        P2PActions.connect(actorUser, actorUserName, serverIP);
        notifyLock("User2Connect");
        // Action 13:User1InviteUser2
        act.setCurActionText("Action 13:User1InviteUser2");
        // Action 14: User2AcceptUser1
        waitLock("User1InviteUser2");
        act.setCurActionText("Action 14: User2AcceptUser1");
        P2PActions.afterWaitInvite(actorUserName, pcObserver, 2);
        P2PActions.accept(actorUser, actorUserName, targetUserName);
        P2PActions.afterAccept(actorUserName, pcObserver, 2);
        notifyLock("User2AcceptUser1");
        // Action 15: User1PublishToUser2
        act.setCurActionText("Action 15: User1PublishToUser2");
        // Action 16: User2PublishToUser1
        waitLock("User1PublishToUser2");
        act.setCurActionText("Action 16: User2PublishToUser1");
        P2PActions.afterWaitPublish(actorUserName, pcObserver, 2);
        rslist = pcObserver.addedStream;
        act.attachRender2(rslist.get(1));
        P2PActions.publish(actorUser, actorUserName, targetUserName, lcs);
        notifyLock("User2PublishToUser1");
        SystemClock.sleep(waitingTime);
        P2PActions.endStory();
        act.makeToast("Test End!");
    }

    /**
     * Test a normal interaction process between three users.
     * Actors: User1, User2 and User3
     * Story:
     * 1. User1ConnectAndCreateLocalStream
     * 2. User2ConnectAndCreateLocalStream
     * 3. User3ConnectAndCreateLocalStream
     * 4. User1InviteUser2
     * 5. User2AcceptUser1
     * 6. User3InviteUser1
     * 7. User1AcceptUser3
     * 8. User2InivteUser3
     * 9. User3AcceptUser2
     * 10. User1PublishToUser2AndUser3
     * 11. User2PublishToUser1AndUser3
     * 12. User3PublishToUser1AndUser2
     * 13. User1SendMessageToUser2AndUser3
     * 14. User2SendMessageToUser1AndUser3
     * 15. User3SendMessageToUser1AndUser2
     * 16. User1UnpublishToUser2AndUser3
     * 17. User2UnpublishToUser1AndUser3
     * 18. User3UnpublishToUser1AndUser2
     * 19. User1StopChatWithUser2
     * 20. User2StopChatWithUser3
     * 21. User3StopChatWithUser1
     * 22. User1Disconnect
     * 23. User2Disconnect
     * 24. User3Disconnect
     */
    public void testThreeUserInteraction() {
        initTestActivity();
        // Init variables

        String actorUser2Name = "User2";
        String targetUser1Name = "User1";
        String targetUser3Name = "User3";
        PeerClientObserverForTest pcObserver = new PeerClientObserverForTest();
        PeerClientConfiguration peerClientConfiguration = new PeerClientConfiguration();
        PeerClient actorUser2 = new PeerClient(peerClientConfiguration, new SocketSignalingChannel());
        actorUser2.addObserver(pcObserver);
        // Start notice
        P2PActions.startStory("testTwoUserInteraction");
        act.makeToast("Test start, actor:" + actorUser2Name);
        act.setTitle("Actor:" + actorUser2Name);
        // Action 1. User1ConnectAndCreateLocalStream
        // Action 2. User2ConnectAndCreateLocalStream
        waitLock("User1ConnectAndCreateLocalStream");
        P2PActions.connect(actorUser2, actorUser2Name, serverIP);
        LocalCameraStream lcs = P2PActions.createLocalCameraStream(actorUser2Name, true, true);
        act.attachRender1(lcs);
        notifyLock("User2ConnectAndCreateLocalStream");
        // Action 3. User3ConnectAndCreateLocalStream
        // Action 4. User1InviteUser2
        // Action 5. User2AcceptUser1
        waitLock("User1InviteUser2");
        P2PActions.afterWaitInvite(actorUser2Name, pcObserver, 1);
        P2PActions.accept(actorUser2, actorUser2Name, targetUser1Name);
        P2PActions.afterAccept(actorUser2Name, pcObserver, 1);
        notifyLock("User2AcceptUser1");
        // Action 6. User3InviteUser1
        // Action 7. User1AcceptUser3
        // Action 8. User2InivteUser3
        waitLock("User1AcceptUser3");
        P2PActions.invite(actorUser2, actorUser2Name, targetUser3Name);
        notifyLock("User2InivteUser3");
        // Action 9. User3AcceptUser2
        // Action 10. User1PublishToUser2AndUser3
        // Action 11. User2PublishToUser1AndUser3
        waitLock("User1PublishToUser2AndUser3");
        P2PActions.afterWaitAccept(actorUser2Name, pcObserver, 1, 2);
        P2PActions.afterWaitPublish(actorUser2Name, pcObserver, 1);
        List<RemoteStream> rslist = pcObserver.addedStream;
        act.attachRender2(rslist.get(rslist.size() - 1));
        P2PActions.publish(actorUser2, actorUser2Name, targetUser1Name, lcs);
        P2PActions.publish(actorUser2, actorUser2Name, targetUser3Name, lcs);
        notifyLock("User2PublishToUser1AndUser3");
        // Action 12. User3PublishToUser1AndUser2
        // Action 13. User1SendMessageToUser2AndUser3
        // Action 14. User2SendMessageToUser1AndUser3
        waitLock("User1SendMessageToUser2AndUser3");
        P2PActions.afterWaitPublish(actorUser2Name, pcObserver, 2);
        act.attachRender3(rslist.get(rslist.size() - 1));
        P2PActions.afterWaitSend(actorUser2Name, pcObserver, 1, 1);
        P2PActions.send(actorUser2, actorUser2Name, targetUser1Name, "Message:User2ToUser1");
        P2PActions.send(actorUser2, actorUser2Name, targetUser3Name, "Message:User2ToUser3");
        notifyLock("User2SendMessageToUser1AndUser3");
        // Action 15. User3SendMessageToUser1AndUser2
        // Action 16. User1UnpublishToUser2AndUser3
        // Action 17. User2UnpublishToUser1AndUser3
        waitLock("User1UnpublishToUser2AndUser3");
        P2PActions.afterWaitSend(actorUser2Name, pcObserver, 2, 2);
        P2PActions.afterWaitUnpublish(actorUser2Name, pcObserver, 1);
        P2PActions.unpublish(actorUser2, actorUser2Name, targetUser1Name, lcs);
        P2PActions.unpublish(actorUser2, actorUser2Name, targetUser3Name, lcs);
        notifyLock("User2UnpublishToUser1AndUser3");
        // Action 18. User3UnpublishToUser1AndUser2
        // Action 19. User1StopChatWithUser2
        // Action 20. User2StopChatWithUser3
        waitLock("User1StopChatWithUser2");
        P2PActions.afterWaitUnpublish(actorUser2Name, pcObserver, 2);
        P2PActions.afterWaitStop(actorUser2Name, pcObserver, 1);
        P2PActions.stop(actorUser2, actorUser2Name, targetUser3Name);
        P2PActions.afterStop(actorUser2Name, pcObserver, 2);
        notifyLock("User2StopChatWithUser3");
        // Action 21. User3StopChatWithUser1
        // Action 22. User1Disconnect
        // Action 23. User2Disconnect
        waitLock("User1Disconnect");
        P2PActions.disconnect(actorUser2, actorUser2Name);
        notifyLock("User2Disconnect");
        // sleep to make sure the notify message is sent out
        SystemClock.sleep(waitingTime);
        // Action 24. User3Disconnect
        P2PActions.endStory();
    }

    private void initTestActivity() {
        while (true) {
            try {
                getActivity();
                break;
            } catch (Exception e) {
                Log.d(TAG, "activity get error, retry!");
            }
        }
        Log.d(TAG, testActivity.toString());
//        ClientContext.setApplicationContext(testActivity);
        act = (TestActivity) testActivity;
    }

    private void waitLock(String lock) {
        Log.d(TAG, "wait:" + lock);
        act.setWaitNotifyStateText("wait:" + lock);
        waitForObject(lock);
        Log.d(TAG, "receive notify:" + lock);
    }

    private void notifyLock(String lock) {
        notifyObject(lock);
        Log.d(TAG, "notify:" + lock);
        act.setWaitNotifyStateText("notify:" + lock);
    }
}
