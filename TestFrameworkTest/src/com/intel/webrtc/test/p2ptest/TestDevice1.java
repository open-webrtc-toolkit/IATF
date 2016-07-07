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
import com.intel.webrtc.test.helper.SocketSignalingChannel;
import com.intel.webrtc.test.helper.TestActivity;

import android.os.SystemClock;
import android.util.Log;

public class TestDevice1 extends AndroidTestDevice {
    String TAG = "P2PTestDevice1";
    TestActivity act = null;
    private long waitingTime = 3000;
    private String serverIP = "http://10.239.44.74:8095/";

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

        String actorUserName = "User1";
        String targetUserName = "User2";
        long waitingTime = 3000;
        PeerClientObserverForTest pcObserver = new PeerClientObserverForTest();
        PeerClientConfiguration peerClientConfiguration = new PeerClientConfiguration();
        PeerClient actorUser = new PeerClient(peerClientConfiguration, new SocketSignalingChannel());
        actorUser.addObserver(pcObserver);

        P2PActions.startStory("testTwoUserInteraction");
        act.makeToast("Test start, actor:" + actorUserName);
        act.setTitle("Actor:" + actorUserName);

        // Action 1: User1Connect
        act.setCurActionText("Action 1: User1Connect");
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User1Connect");
        // Action 2: User2Connect
        act.setCurActionText("Action 2: User2Connect");
        // Action 3: User1InviteUser2
        waitLock("User2Connect");
        act.setCurActionText("Action 3: User1InviteUser2");
        P2PActions.afterWaitConnect();
        P2PActions.invite(actorUser, actorUserName, targetUserName,true);
        P2PActions.afterInvite();
        notifyLock("User1InviteUser2");
        // Action 4: User2AcceptUser1
        act.setCurActionText("Action 4: User2AcceptUser1");
        // Action 5: User1CreateLocalStream
        waitLock("User2AcceptUser1");
        act.setCurActionText("Action 5: User1CreateLocalStream");
        P2PActions.afterWaitAccept(actorUserName, pcObserver, 1, 1);
        LocalCameraStream lcs = P2PActions.createLocalCameraStream(actorUserName, true, true);
        Log.d(TAG, "lcs:" + lcs);
        ((TestActivity) testActivity).attachRender1(lcs);
        notifyLock("User1CreateLocalStream");
        // Action 6: User2CreateLocalStream
        act.setCurActionText("Action 6: User2CreateLocalStream");
        // Action 7: User1PublishToUser2
        waitLock("User2CreateLocalStream");
        act.setCurActionText("Action 7: User1PublishToUser2");
        P2PActions.afterWaitCreateLocalCameraStream();
        P2PActions.publish(actorUser, actorUserName, targetUserName, lcs,true);
        P2PActions.afterPublish();
        notifyLock("User1PublishToUser2");
        // Action 8: User2PublishToUser1
        act.setCurActionText("Action 8: User2PublishToUser1");
        // Action 9: User1SendTwoMessagesToUser2
        waitLock("User2PublishToUser1");
        act.setCurActionText("Action 9: User1SendTwoMessagesToUser2");
        Log.d(TAG, "OBSERVER:" + pcObserver);
        P2PActions.afterWaitPublish(actorUserName, pcObserver, 1);
        List<RemoteStream> rslist = pcObserver.addedStream;
        act.attachRender2(rslist.get(0));
        P2PActions.send(actorUser, actorUserName, targetUserName,
                actorUserName + " send message1 to " + targetUserName,true);
        P2PActions.send(actorUser, actorUserName, targetUserName,
                actorUserName + " send message2 to " + targetUserName,true);
        P2PActions.afterSend();
        notifyLock("User1SendTwoMessagesToUser2");
        // Action 10: User2SendTwoMessagesToUser1
        act.setCurActionText("Action 10: User2SendTwoMessagesToUser1");
        // Action 11: User1UnpublishToUser2
        waitLock("User2SendTwoMessagesToUser1");
        act.setCurActionText("Action 11: User1UnpublishToUser2");
        P2PActions.afterWaitSend(actorUserName, pcObserver, 2, 2);
        P2PActions.unpublish(actorUser, actorUserName, targetUserName, lcs,true);
        P2PActions.afterUnpublish();
        notifyLock("User1UnpublishToUser2");
        // Action 12: User2UnpublishToUser1
        act.setCurActionText("Action 12: User2UnpublishToUser1");
        // Action 13: User1StopChatWithUser2
        waitLock("User2UnpublishToUser1");
        act.setCurActionText("Action 13: User1StopChatWithUser2");
        P2PActions.afterWaitUnpublish(actorUserName, pcObserver, 1);
        P2PActions.stop(actorUser, actorUserName, targetUserName,true);
        P2PActions.afterStop(actorUserName, pcObserver, 1);
        notifyLock("User1StopChatWithUser2");
        // Action 14:User2InviteUser1
        act.setCurActionText("Action 14:User2InviteUser1");
        // Action 15:User1DenyUser2
        waitLock("User2InviteUser1");
        act.setCurActionText("Action 15:User1DenyUser2");
        P2PActions.afterWaitInvite(actorUserName, pcObserver, 1);
        P2PActions.deny(actorUser, actorUserName, targetUserName,true);
        P2PActions.afterDeny();
        notifyLock("User1DenyUser2");
        // Action 16:User2Disconnect
        act.setCurActionText("Action 16:User2Disconnect");
        // Action 17:User1Disconnect
        waitLock("User2Disconnect");
        act.setCurActionText("Action 17:User1Disconnect");
        P2PActions.afterWaitDisconnect();
        P2PActions.disconnect(actorUser, actorUserName,true);
        P2PActions.afterDisconnect(actorUserName, pcObserver, 1);

        P2PActions.endStory();
        act.makeToast("Test end!");
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

        String actorUserName = "User1";
        String targetUserName = "User2";
        long waitingTime = 3000;
        PeerClientObserverForTest pcObserver = new PeerClientObserverForTest();
        PeerClientConfiguration peerClientConfiguration = new PeerClientConfiguration();
        PeerClient actorUser = new PeerClient(peerClientConfiguration, new SocketSignalingChannel());
        actorUser.addObserver(pcObserver);

        P2PActions.startStory("testThreeUserInteraction");
        act.makeToast("Test start, actor:" + actorUserName);
        act.setTitle("Actor:" + actorUserName);

        // Action 1: User1Connect
        act.setCurActionText("Action 1: User1Connect");
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User1Connect");
        // Action 2: User2Connect
        act.setCurActionText("Action 2: User2Connect");
        // Action 3: User1InviteUser2
        waitLock("User2Connect");
        act.setCurActionText("Action 3: User1InviteUser2");
        P2PActions.afterWaitConnect();
        P2PActions.invite(actorUser, actorUserName, targetUserName,true);
        P2PActions.afterInvite();
        notifyLock("User1InviteUser2");
        // Action 4: User2AcceptUser1
        act.setCurActionText("Action 4: User2AcceptUser1");
        // Action 5: User1CreateLocalStream
        waitLock("User2AcceptUser1");
        act.setCurActionText("Action 5: User1CreateLocalStream");
        P2PActions.afterWaitAccept(actorUserName, pcObserver, 1, 1);
        LocalCameraStream lcs = P2PActions.createLocalCameraStream(actorUserName, true, true);
        Log.d(TAG, "lcs:" + lcs);
        ((TestActivity) testActivity).attachRender1(lcs);
        notifyLock("User1CreateLocalStream");
        // Action 6: User2CreateLocalStream
        act.setCurActionText("Action 6: User2CreateLocalStream");
        // Action 7: User1PublishToUser2
        waitLock("User2CreateLocalStream");
        act.setCurActionText("Action 7: User1PublishToUser2");
        P2PActions.afterWaitCreateLocalCameraStream();
        P2PActions.publish(actorUser, actorUserName, targetUserName, lcs,true);
        P2PActions.afterPublish();
        notifyLock("User1PublishToUser2");
        // Action 8: User2PublishToUser1
        SystemClock.sleep(waitingTime);

        act.setCurActionText("Action 8: User2PublishToUser1");
        P2PActions.endStory();
        act.makeToast("Test end!");
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

        String actorUserName = "User1";
        String targetUserName = "User2";
        long waitingTime = 3000;
        PeerClientObserverForTest pcObserver = new PeerClientObserverForTest();
        PeerClientConfiguration peerClientConfiguration = new PeerClientConfiguration();
        PeerClient actorUser = new PeerClient(peerClientConfiguration, new SocketSignalingChannel());
        actorUser.addObserver(pcObserver);

        P2PActions.startStory("testThreeUserInteraction");
        act.makeToast("Test start, actor:" + actorUserName);
        act.setTitle("Actor:" + actorUserName);

        // Action 1: User1Connect
        act.setCurActionText("Action 1: User1Connect");
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User1Connect");
        // Action 2: User2Connect
        act.setCurActionText("Action 2: User2Connect");
        // Action 3: User1InviteUser2
        waitLock("User2Connect");
        act.setCurActionText("Action 3: User1InviteUser2");
        P2PActions.afterWaitConnect();
        P2PActions.invite(actorUser, actorUserName, targetUserName,true);
        P2PActions.afterInvite();
        notifyLock("User1InviteUser2");
        // Action 4: User2AcceptUser1
        act.setCurActionText("Action 4: User2AcceptUser1");
        // Action 5: User1CreateVideoOnlyLocalStream
        waitLock("User2AcceptUser1");
        act.setCurActionText("Action 5: User1CreateVideoOnlyLocalStream");
        P2PActions.afterWaitAccept(actorUserName, pcObserver, 1, 1);
        LocalCameraStream lcs = P2PActions.createLocalCameraStream(actorUserName, true, false);
        Log.d(TAG, "lcs:" + lcs);
        ((TestActivity) testActivity).attachRender1(lcs);
        notifyLock("User1CreateVideoOnlyLocalStream");
        // Action 6: User2CreateVideoOnlyLocalStream
        act.setCurActionText("Action 6: User2CreateVideoOnlyLocalStream");
        // Action 7: User1PublishToUser2
        waitLock("User2CreateVideoOnlyLocalStream");
        act.setCurActionText("Action 7: User1PublishToUser2");
        P2PActions.afterWaitCreateLocalCameraStream();
        P2PActions.publish(actorUser, actorUserName, targetUserName, lcs,true);
        P2PActions.afterPublish();
        notifyLock("User1PublishToUser2");
        // Action 8: User2PublishToUser1
        SystemClock.sleep(waitingTime);

        act.setCurActionText("Action 8: User2PublishToUser1");
        P2PActions.endStory();
        act.makeToast("Test end!");
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

        String actorUserName = "User1";
        String targetUserName = "User2";
        long waitingTime = 3000;
        PeerClientObserverForTest pcObserver = new PeerClientObserverForTest();
        PeerClientConfiguration peerClientConfiguration = new PeerClientConfiguration();
        PeerClient actorUser = new PeerClient(peerClientConfiguration, new SocketSignalingChannel());
        actorUser.addObserver(pcObserver);

        P2PActions.startStory("testThreeUserInteraction");
        act.makeToast("Test start, actor:" + actorUserName);
        act.setTitle("Actor:" + actorUserName);

        // Action 1: User1Connect
        act.setCurActionText("Action 1: User1Connect");
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User1Connect");
        // Action 2: User2Connect
        act.setCurActionText("Action 2: User2Connect");
        // Action 3: User1InviteUser2
        waitLock("User2Connect");
        act.setCurActionText("Action 3: User1InviteUser2");
        P2PActions.afterWaitConnect();
        P2PActions.invite(actorUser, actorUserName, targetUserName,true);
        P2PActions.afterInvite();
        notifyLock("User1InviteUser2");
        // Action 4: User2AcceptUser1
        act.setCurActionText("Action 4: User2AcceptUser1");
        // Action 5: User1CreateAudioOnlyLocalStream
        waitLock("User2AcceptUser1");
        act.setCurActionText("Action 5: User1CreateAudioOnlyLocalStream");
        P2PActions.afterWaitAccept(actorUserName, pcObserver, 1, 1);
        LocalCameraStream lcs = P2PActions.createLocalCameraStream(actorUserName, false, true);
        Log.d(TAG, "lcs:" + lcs);
        ((TestActivity) testActivity).attachRender1(lcs);
        notifyLock("User1CreateAudioOnlyLocalStream");
        // Action 6: User2CreateAudioOnlyLocalStream
        act.setCurActionText("Action 6: User2CreateAudioOnlyLocalStream");
        // Action 7: User1PublishToUser2
        waitLock("User2CreateAudioOnlyLocalStream");
        act.setCurActionText("Action 7: User1PublishToUser2");
        P2PActions.afterWaitCreateLocalCameraStream();
        P2PActions.publish(actorUser, actorUserName, targetUserName, lcs,true);
        P2PActions.afterPublish();
        notifyLock("User1PublishToUser2");
        // Action 8: User2PublishToUser1
        SystemClock.sleep(waitingTime);

        act.setCurActionText("Action 8: User2PublishToUser1");
        P2PActions.endStory();
        act.makeToast("Test end!");
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

        String actorUserName = "User1";
        String targetUserName = "User2";
        long waitingTime = 3000;
        PeerClientObserverForTest pcObserver = new PeerClientObserverForTest();
        PeerClientConfiguration peerClientConfiguration = new PeerClientConfiguration();
        PeerClient actorUser = new PeerClient(peerClientConfiguration, new SocketSignalingChannel());
        actorUser.addObserver(pcObserver);

        P2PActions.startStory("testThreeUserInteraction");
        act.makeToast("Test start, actor:" + actorUserName);
        act.setTitle("Actor:" + actorUserName);

        // Action 1: User1Connect
        act.setCurActionText("Action 1: User1Connect");
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User1Connect");
        // Action 2: User2Connect
        act.setCurActionText("Action 2: User2Connect");
        // Action 3: User1InviteUser2
        waitLock("User2Connect");
        act.setCurActionText("Action 3: User1InviteUser2");
        P2PActions.afterWaitConnect();
        P2PActions.invite(actorUser, actorUserName, targetUserName,true);
        P2PActions.afterInvite();
        notifyLock("User1InviteUser2");
        // Action 4: User2AcceptUser1
        act.setCurActionText("Action 4: User2AcceptUser1");
        // Action 5: User1CreateLocalStream
        waitLock("User2AcceptUser1");
        act.setCurActionText("Action 5: User1CreateLocalStream");
        P2PActions.afterWaitAccept(actorUserName, pcObserver, 1, 1);
        LocalCameraStream lcs = P2PActions.createLocalCameraStream(actorUserName, true, true);
        Log.d(TAG, "lcs:" + lcs);
        ((TestActivity) testActivity).attachRender1(lcs);
        notifyLock("User1CreateLocalStream");
        // Action 6: User2CreatevideoOnlyLocalStream
        act.setCurActionText("Action 6: User2CreatevideoOnlyLocalStream");
        // Action 7: User1PublishToUser2
        waitLock("User2CreatevideoOnlyLocalStream");
        act.setCurActionText("Action 7: User1PublishToUser2");
        P2PActions.afterWaitCreateLocalCameraStream();
        P2PActions.publish(actorUser, actorUserName, targetUserName, lcs,true);
        P2PActions.afterPublish();
        notifyLock("User1PublishToUser2");
        // Action 8: User2PublishToUser1
        SystemClock.sleep(waitingTime);

        act.setCurActionText("Action 8: User2PublishToUser1");
        P2PActions.endStory();
        act.makeToast("Test end!");
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

        String actorUserName = "User1";
        String targetUserName = "User2";
        long waitingTime = 3000;
        PeerClientObserverForTest pcObserver = new PeerClientObserverForTest();
        PeerClientConfiguration peerClientConfiguration = new PeerClientConfiguration();
        PeerClient actorUser = new PeerClient(peerClientConfiguration, new SocketSignalingChannel());
        actorUser.addObserver(pcObserver);

        P2PActions.startStory("testThreeUserInteraction");
        act.makeToast("Test start, actor:" + actorUserName);
        act.setTitle("Actor:" + actorUserName);

        // Action 1: User1Connect
        act.setCurActionText("Action 1: User1Connect");
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User1Connect");
        // Action 2: User2Connect
        act.setCurActionText("Action 2: User2Connect");
        // Action 3: User1InviteUser2
        waitLock("User2Connect");
        act.setCurActionText("Action 3: User1InviteUser2");
        P2PActions.afterWaitConnect();
        P2PActions.invite(actorUser, actorUserName, targetUserName,true);
        P2PActions.afterInvite();
        notifyLock("User1InviteUser2");
        // Action 4: User2AcceptUser1
        act.setCurActionText("Action 4: User2AcceptUser1");
        // Action 5: User1CreateLocalStream
        waitLock("User2AcceptUser1");
        act.setCurActionText("Action 5: User1CreateLocalStream");
        P2PActions.afterWaitAccept(actorUserName, pcObserver, 1, 1);
        LocalCameraStream lcs = P2PActions.createLocalCameraStream(actorUserName, true, true);
        Log.d(TAG, "lcs:" + lcs);
        ((TestActivity) testActivity).attachRender1(lcs);
        notifyLock("User1CreateLocalStream");
        // Action 6: User2CreateAudioOnlyLocalStream
        act.setCurActionText("Action 6: User2CreateAudioOnlyLocalStream");
        // Action 7: User1PublishToUser2
        waitLock("User2CreateAudioOnlyLocalStream");
        act.setCurActionText("Action 7: User1PublishToUser2");
        P2PActions.afterWaitCreateLocalCameraStream();
        P2PActions.publish(actorUser, actorUserName, targetUserName, lcs,true);
        P2PActions.afterPublish();
        notifyLock("User1PublishToUser2");
        // Action 8: User2PublishToUser1
        SystemClock.sleep(waitingTime);

        act.setCurActionText("Action 8: User2PublishToUser1");
        P2PActions.endStory();
        act.makeToast("Test end!");
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

        String actorUserName = "User1";
        String targetUserName = "User2";
        long waitingTime = 3000;
        PeerClientObserverForTest pcObserver = new PeerClientObserverForTest();
        PeerClientConfiguration peerClientConfiguration = new PeerClientConfiguration();
        PeerClient actorUser = new PeerClient(peerClientConfiguration, new SocketSignalingChannel());
        actorUser.addObserver(pcObserver);

        P2PActions.startStory("testThreeUserInteraction");
        act.makeToast("Test start, actor:" + actorUserName);
        act.setTitle("Actor:" + actorUserName);

        // Action 1: User1Connect
        act.setCurActionText("Action 1: User1Connect");
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User1Connect");
        // Action 2: User2Connect
        act.setCurActionText("Action 2: User2Connect");
        // Action 3: User1InviteUser2
        waitLock("User2Connect");
        act.setCurActionText("Action 3: User1InviteUser2");
        P2PActions.afterWaitConnect();
        P2PActions.invite(actorUser, actorUserName, targetUserName,true);
        P2PActions.afterInvite();
        notifyLock("User1InviteUser2");
        // Action 4: User2AcceptUser1
        act.setCurActionText("Action 4: User2AcceptUser1");
        // Action 5: User1CreatevideoOnlyLocalStream
        waitLock("User2AcceptUser1");
        act.setCurActionText("Action 5: User1CreatevideoOnlyLocalStream");
        P2PActions.afterWaitAccept(actorUserName, pcObserver, 1, 1);
        LocalCameraStream lcs = P2PActions.createLocalCameraStream(actorUserName, true, false);
        Log.d(TAG, "lcs:" + lcs);
        ((TestActivity) testActivity).attachRender1(lcs);
        notifyLock("User1CreatevideoOnlyLocalStream");
        // Action 6: User2CreateAudioOnlyLocalStream
        act.setCurActionText("Action 6: User2CreateAudioOnlyLocalStream");
        // Action 7: User1PublishToUser2
        waitLock("User2CreateAudioOnlyLocalStream");
        act.setCurActionText("Action 7: User1PublishToUser2");
        P2PActions.afterWaitCreateLocalCameraStream();
        P2PActions.publish(actorUser, actorUserName, targetUserName, lcs,true);
        P2PActions.afterPublish();
        notifyLock("User1PublishToUser2");
        // Action 8: User2PublishToUser1
        SystemClock.sleep(waitingTime);

        act.setCurActionText("Action 8: User2PublishToUser1");
        P2PActions.endStory();
        act.makeToast("Test end!");
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

        String actorUserName = "User1";
        String targetUserName = "User2";
        long waitingTime = 3000;
        PeerClientObserverForTest pcObserver = new PeerClientObserverForTest();
        PeerClientConfiguration peerClientConfiguration = new PeerClientConfiguration();
        PeerClient actorUser = new PeerClient(peerClientConfiguration, new SocketSignalingChannel());
        actorUser.addObserver(pcObserver);

        P2PActions.startStory("testTwoUserInteraction");
        act.makeToast("Test start, actor:" + actorUserName);
        act.setTitle("Actor:" + actorUserName);

        // Action 1: User1Connect
        act.setCurActionText("Action 1: User1Connect");
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User1Connect");
        // Action 2: User2Connect
        act.setCurActionText("Action 2: User2Connect");
        // Action 3: User1InviteUser2
        waitLock("User2Connect");
        act.setCurActionText("Action 3: User1InviteUser2");
        P2PActions.afterWaitConnect();
        P2PActions.invite(actorUser, actorUserName, targetUserName,true);
        P2PActions.afterInvite();
        notifyLock("User1InviteUser2");
        // Action 4: User2AcceptUser1
        act.setCurActionText("Action 4: User2AcceptUser1");
        // Action 5: User1CreateLocalStream
        waitLock("User2AcceptUser1");
        act.setCurActionText("Action 5: User1CreateLocalStream");
        P2PActions.afterWaitAccept(actorUserName, pcObserver, 1, 1);
        LocalCameraStream lcs = P2PActions.createLocalCameraStream(actorUserName, true, true);
        Log.d(TAG, "lcs:" + lcs);
        ((TestActivity) testActivity).attachRender1(lcs);
        notifyLock("User1CreateLocalStream");
        // Action 6: User2CreateLocalStream
        act.setCurActionText("Action 6: User2CreateLocalStream");
        // Action 7: User1PublishToUser2
        waitLock("User2CreateLocalStream");
        act.setCurActionText("Action 7: User1PublishToUser2");
        P2PActions.afterWaitCreateLocalCameraStream();
        P2PActions.publish(actorUser, actorUserName, targetUserName, lcs,true);
        P2PActions.afterPublish();
        notifyLock("User1PublishToUser2");
        // Action 8: User2PublishToUser1
        act.setCurActionText("Action 8: User2PublishToUser1");
        // Action 9: User1UnpublishToUser2
        waitLock("User2PublishToUser1");
        act.setCurActionText("Action 9: User1UnpublishToUser2");
        P2PActions.afterWaitPublish(actorUserName, pcObserver, 1);
        P2PActions.unpublish(actorUser, actorUserName, targetUserName, lcs,true);
        P2PActions.afterUnpublish();
        notifyLock("User1UnpublishToUser2");
        // Action 10: User2UnpublishToUser1
        act.setCurActionText("Action 10: User2UnpublishToUser1");
        SystemClock.sleep(waitingTime);

        P2PActions.endStory();
        act.makeToast("Test end!");
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

        String actorUserName = "User1";
        String targetUserName = "User2";
        long waitingTime = 3000;
        PeerClientObserverForTest pcObserver = new PeerClientObserverForTest();
        PeerClientConfiguration peerClientConfiguration = new PeerClientConfiguration();
        PeerClient actorUser = new PeerClient(peerClientConfiguration, new SocketSignalingChannel());
        actorUser.addObserver(pcObserver);

        P2PActions.startStory("testTwoUserInteraction");
        act.makeToast("Test start, actor:" + actorUserName);
        act.setTitle("Actor:" + actorUserName);

        // Action 1: User1Connect
        act.setCurActionText("Action 1: User1Connect");
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User1Connect");
        // Action 2: User2Connect
        act.setCurActionText("Action 2: User2Connect");
        // Action 3: User1InviteUser2
        waitLock("User2Connect");
        act.setCurActionText("Action 3: User1InviteUser2");
        P2PActions.afterWaitConnect();
        P2PActions.invite(actorUser, actorUserName, targetUserName,true);
        P2PActions.afterInvite();
        notifyLock("User1InviteUser2");
        // Action 4: User2AcceptUser1
        act.setCurActionText("Action 4: User2AcceptUser1");
        // Action 5: User1CreateLocalStream
        waitLock("User2AcceptUser1");
        act.setCurActionText("Action 5: User1CreateLocalStream");
        P2PActions.afterWaitAccept(actorUserName, pcObserver, 1, 1);
        LocalCameraStream lcs = P2PActions.createLocalCameraStream(actorUserName, true, true);
        Log.d(TAG, "lcs:" + lcs);
        ((TestActivity) testActivity).attachRender1(lcs);
        notifyLock("User1CreateLocalStream");
        // Action 6: User2CreateLocalStream
        act.setCurActionText("Action 6: User2CreateLocalStream");
        // Action 7: User1PublishToUser2
        waitLock("User2CreateLocalStream");
        act.setCurActionText("Action 7: User1PublishToUser2");
        P2PActions.afterWaitCreateLocalCameraStream();
        P2PActions.publish(actorUser, actorUserName, targetUserName, lcs,true);
        P2PActions.afterPublish();
        notifyLock("User1PublishToUser2");
        // Action 8: User2PublishToUser1
        act.setCurActionText("Action 8: User2PublishToUser1");
        // Action 9: User1SendTwoMessagesToUser2
        waitLock("User2PublishToUser1");
        act.setCurActionText("Action 9: User1SendTwoMessagesToUser2");
        Log.d(TAG, "OBSERVER:" + pcObserver);
        P2PActions.afterWaitPublish(actorUserName, pcObserver, 1);
        List<RemoteStream> rslist = pcObserver.addedStream;
        act.attachRender2(rslist.get(0));
        P2PActions.send(actorUser, actorUserName, targetUserName,
                actorUserName + " send message1 to " + targetUserName,true);
        P2PActions.send(actorUser, actorUserName, targetUserName,
                actorUserName + " send message2 to " + targetUserName,true);
        P2PActions.afterSend();
        notifyLock("User1SendTwoMessagesToUser2");
        // Action 10: User2SendTwoMessagesToUser1
        act.setCurActionText("Action 10: User2SendTwoMessagesToUser1");
        SystemClock.sleep(waitingTime);
        P2PActions.endStory();
        act.makeToast("Test end!");
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

        String actorUserName = "User1";
        String targetUserName = "User2";
        long waitingTime = 3000;
        PeerClientObserverForTest pcObserver = new PeerClientObserverForTest();
        PeerClientConfiguration peerClientConfiguration = new PeerClientConfiguration();
        PeerClient actorUser = new PeerClient(peerClientConfiguration, new SocketSignalingChannel());
        actorUser.addObserver(pcObserver);

        P2PActions.startStory("testTwoUserInteraction");
        act.makeToast("Test start, actor:" + actorUserName);
        act.setTitle("Actor:" + actorUserName);

        // Action 1: User1Connect
        act.setCurActionText("Action 1: User1Connect");
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User1Connect");
        // Action 2: User2Connect
        act.setCurActionText("Action 2: User2Connect");
        // Action 3: User1InviteUser2
        waitLock("User2Connect");
        act.setCurActionText("Action 3: User1InviteUser2");
        P2PActions.afterWaitConnect();
        P2PActions.invite(actorUser, actorUserName, targetUserName,true);
        P2PActions.afterInvite();
        notifyLock("User1InviteUser2");
        // Action 4: User2AcceptUser1
        act.setCurActionText("Action 4: User2AcceptUser1");
        // Action 5: User1CreateLocalStream
        waitLock("User2AcceptUser1");
        act.setCurActionText("Action 5: User1CreateLocalStream");
        P2PActions.afterWaitAccept(actorUserName, pcObserver, 1, 1);
        LocalCameraStream lcs = P2PActions.createLocalCameraStream(actorUserName, true, true);
        Log.d(TAG, "lcs:" + lcs);
        ((TestActivity) testActivity).attachRender1(lcs);
        notifyLock("User1CreateLocalStream");
        // Action 6: User2CreateLocalStream
        act.setCurActionText("Action 6: User2CreateLocalStream");
        // Action 7: User1PublishToUser2
        waitLock("User2CreateLocalStream");
        act.setCurActionText("Action 7: User1PublishToUser2");
        P2PActions.afterWaitCreateLocalCameraStream();
        P2PActions.publish(actorUser, actorUserName, targetUserName, lcs,true);
        P2PActions.afterPublish();
        notifyLock("User1PublishToUser2");
        // Action 8: User2PublishToUser1
        act.setCurActionText("Action 8: User2PublishToUser1");
        // Action 9: User1UnpublishToUser2
        waitLock("User2PublishToUser1");
        List<RemoteStream> rslist = pcObserver.addedStream;
        act.attachRender2(rslist.get(0));
        act.setCurActionText("Action 9: User1UnpublishToUser2");
        P2PActions.afterWaitPublish(actorUserName, pcObserver, 1);
        P2PActions.unpublish(actorUser, actorUserName, targetUserName, lcs,true);
        P2PActions.afterUnpublish();
        notifyLock("User1UnpublishToUser2");
        // Action 10: User2UnpublishToUser1
        act.setCurActionText("Action 10: User2UnpublishToUser1");
        // Action 11: User1SendTwoMessagesToUser2
        waitLock("User2UnpublishToUser1");
        act.setCurActionText("Action 9: User1SendTwoMessagesToUser2");
        Log.d(TAG, "OBSERVER:" + pcObserver);
        P2PActions.afterWaitUnpublish(actorUserName, pcObserver, 1);
        P2PActions.send(actorUser, actorUserName, targetUserName,
                actorUserName + " send message1 to " + targetUserName,true);
        P2PActions.send(actorUser, actorUserName, targetUserName,
                actorUserName + " send message2 to " + targetUserName,true);
        P2PActions.afterSend();
        notifyLock("User1SendTwoMessagesToUser2");
        // Action 12: User2SendTwoMessagesToUser1
        act.setCurActionText("Action 10: User2SendTwoMessagesToUser1");
        SystemClock.sleep(waitingTime);
        P2PActions.endStory();
        act.makeToast("Test end!");
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

        String actorUserName = "User1";
        String targetUserName = "User2";
        long waitingTime = 3000;
        PeerClientObserverForTest pcObserver = new PeerClientObserverForTest();
        PeerClientConfiguration peerClientConfiguration = new PeerClientConfiguration();
        PeerClient actorUser = new PeerClient(peerClientConfiguration, new SocketSignalingChannel());
        actorUser.addObserver(pcObserver);

        P2PActions.startStory("testTwoUserInteraction");
        act.makeToast("Test start, actor:" + actorUserName);
        act.setTitle("Actor:" + actorUserName);

        // Action 1: User1Connect
        act.setCurActionText("Action 1: User1Connect");
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User1Connect");
        // Action 2: User2Connect
        act.setCurActionText("Action 2: User2Connect");
        // Action 3: User1InviteUser2
        waitLock("User2Connect");
        act.setCurActionText("Action 3: User1InviteUser2");
        P2PActions.afterWaitConnect();
        P2PActions.invite(actorUser, actorUserName, targetUserName,true);
        P2PActions.afterInvite();
        notifyLock("User1InviteUser2");
        // Action 4: User2AcceptUser1
        act.setCurActionText("Action 4: User2AcceptUser1");
        // Action 5: User1CreateLocalStream
        waitLock("User2AcceptUser1");
        act.setCurActionText("Action 5: User1CreateLocalStream");
        P2PActions.afterWaitAccept(actorUserName, pcObserver, 1, 1);
        LocalCameraStream lcs = P2PActions.createLocalCameraStream(actorUserName, true, true);
        Log.d(TAG, "lcs:" + lcs);
        ((TestActivity) testActivity).attachRender1(lcs);
        notifyLock("User1CreateLocalStream");
        // Action 6: User2CreateLocalStream
        act.setCurActionText("Action 6: User2CreateLocalStream");
        // Action 7: User1PublishToUser2
        waitLock("User2CreateLocalStream");
        act.setCurActionText("Action 7: User1PublishToUser2");
        P2PActions.afterWaitCreateLocalCameraStream();
        P2PActions.publish(actorUser, actorUserName, targetUserName, lcs,true);
        P2PActions.afterPublish();
        notifyLock("User1PublishToUser2");
        // Action 8: User2PublishToUser1
        act.setCurActionText("Action 8: User2PublishToUser1");
        // Action 9: User1CloseLocalStream
        waitLock("User2PublishToUser1");
        List<RemoteStream> rslist = pcObserver.addedStream;
        act.attachRender2(rslist.get(0));
        act.setCurActionText("Action 9: User1UnpublishToUser2");
        P2PActions.afterWaitPublish(actorUserName, pcObserver, 1);
        P2PActions.closeLocalCameraStream(actorUserName, lcs);
        notifyLock("User1CloseLocalStream");
        // Action 10: User2CloseLocalStream
        act.setCurActionText("Action 10: User2CloseLocalStream");
        // Action 11: User1CreateLocalStream
        waitLock("User2CloseLocalStream");
        act.setCurActionText("Action 11: User1CreateLocalStream");
        lcs = P2PActions.createLocalCameraStream(actorUserName, true, true);
        Log.d(TAG, "lcs:" + lcs);
        ((TestActivity) testActivity).attachRender1(lcs);
        notifyLock("User1CreateLocalStream");
        // Action 12: User2CreateLocalStream
        act.setCurActionText("Action 12: User2CreateLocalStream");
        // Action 13: User1RepublishToUser2
        waitLock("User2CreateLocalStream");
        act.setCurActionText("Action 13: User1RepublishToUser2");
        P2PActions.afterWaitCreateLocalCameraStream();
        P2PActions.publish(actorUser, actorUserName, targetUserName, lcs,true);
        P2PActions.afterPublish();
        notifyLock("User1RepublishToUser2");
        // Action 14: User2RepublishToUser1
        act.setCurActionText("Action 14: User2RepublishToUser1");
        waitLock("User2RepublishToUser1");
        rslist = pcObserver.addedStream;
        act.attachRender2(rslist.get(1));
        P2PActions.afterWaitPublish(actorUserName, pcObserver, 2);
        P2PActions.endStory();
        act.makeToast("Test end!");
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

        String actorUserName = "User1";
        String targetUserName = "User2";
        long waitingTime = 3000;
        PeerClientObserverForTest pcObserver = new PeerClientObserverForTest();
        PeerClientConfiguration peerClientConfiguration = new PeerClientConfiguration();
        PeerClient actorUser = new PeerClient(peerClientConfiguration, new SocketSignalingChannel());
        actorUser.addObserver(pcObserver);

        P2PActions.startStory("testTwoUserInteraction");
        act.makeToast("Test start, actor:" + actorUserName);
        act.setTitle("Actor:" + actorUserName);

        // Action 1: User1Connect
        act.setCurActionText("Action 1: User1Connect");
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User1Connect");
        // Action 2: User2Connect
        act.setCurActionText("Action 2: User2Connect");
        // Action 3: User1InviteUser2
        waitLock("User2Connect");
        act.setCurActionText("Action 3: User1InviteUser2");
        P2PActions.afterWaitConnect();
        P2PActions.invite(actorUser, actorUserName, targetUserName,true);
        P2PActions.afterInvite();
        notifyLock("User1InviteUser2");
        // Action 4: User2AcceptUser1
        act.setCurActionText("Action 4: User2AcceptUser1");
        // Action 5: User1CreateLocalStream
        waitLock("User2AcceptUser1");
        act.setCurActionText("Action 5: User1CreateLocalStream");
        P2PActions.afterWaitAccept(actorUserName, pcObserver, 1, 1);
        LocalCameraStream lcs = P2PActions.createLocalCameraStream(actorUserName, true, true);
        Log.d(TAG, "lcs:" + lcs);
        ((TestActivity) testActivity).attachRender1(lcs);
        notifyLock("User1CreateLocalStream");
        // Action 6: User2CreateLocalStream
        act.setCurActionText("Action 6: User2CreateLocalStream");
        // Action 7: User1PublishToUser2
        waitLock("User2CreateLocalStream");
        act.setCurActionText("Action 7: User1PublishToUser2");
        P2PActions.afterWaitCreateLocalCameraStream();
        P2PActions.publish(actorUser, actorUserName, targetUserName, lcs,true);
        P2PActions.afterPublish();
        notifyLock("User1PublishToUser2");
        // Action 8: User2PublishToUser1
        act.setCurActionText("Action 8: User2PublishToUser1");
        // Action 9: User1CloseLocalStream
        waitLock("User2PublishToUser1");
        List<RemoteStream> rslist = pcObserver.addedStream;
        act.attachRender2(rslist.get(0));
        act.setCurActionText("Action 9: User1UnpublishToUser2");
        P2PActions.afterWaitPublish(actorUserName, pcObserver, 1);
        P2PActions.closeLocalCameraStream(actorUserName, lcs);
        notifyLock("User1CloseLocalStream");
        // Action 10: User2CloseLocalStream
        act.setCurActionText("Action 10: User2CloseLocalStream");
        // Action 11: User1SendTwoMessagesToUser2
        waitLock("User2CloseLocalStream");
        act.setCurActionText("Action 11: User1CreateLocalStream");
        P2PActions.send(actorUser, actorUserName, targetUserName,
                actorUserName + " send message1 to " + targetUserName,true);
        P2PActions.send(actorUser, actorUserName, targetUserName,
                actorUserName + " send message2 to " + targetUserName,true);
        P2PActions.afterSend();
        notifyLock("User1SendTwoMessagesToUser2");
        // Action 12: User2SendTwoMessagesToUser1
        act.setCurActionText("Action 12: User2SendTwoMessagesToUser1");
        waitLock("User2SendTwoMessagesToUser1");
        act.setCurActionText("Action 11: User1UnpublishToUser2");
        P2PActions.afterWaitSend(actorUserName, pcObserver, 2, 2);
        P2PActions.endStory();
        act.makeToast("Test end!");
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

        String actorUserName = "User1";
        String targetUserName = "User2";
        long waitingTime = 3000;
        PeerClientObserverForTest pcObserver = new PeerClientObserverForTest();
        PeerClientConfiguration peerClientConfiguration = new PeerClientConfiguration();
        PeerClient actorUser = new PeerClient(peerClientConfiguration, new SocketSignalingChannel());
        actorUser.addObserver(pcObserver);

        P2PActions.startStory("testTwoUserInteraction");
        act.makeToast("Test start, actor:" + actorUserName);
        act.setTitle("Actor:" + actorUserName);

        // Action 1: User1Connect
        act.setCurActionText("Action 1: User1Connect");
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User1Connect");
        // Action 2: User2Connect
        act.setCurActionText("Action 2: User2Connect");
        // Action 3: User1InviteUser2
        waitLock("User2Connect");
        act.setCurActionText("Action 3: User1InviteUser2");
        P2PActions.afterWaitConnect();
        P2PActions.invite(actorUser, actorUserName, targetUserName,true);
        P2PActions.afterInvite();
        notifyLock("User1InviteUser2");
        // Action 4: User2AcceptUser1
        act.setCurActionText("Action 4: User2AcceptUser1");
        // Action 5: User1CreateLocalStream
        waitLock("User2AcceptUser1");
        act.setCurActionText("Action 5: User1CreateLocalStream");
        P2PActions.afterWaitAccept(actorUserName, pcObserver, 1, 1);
        LocalCameraStream lcs = P2PActions.createLocalCameraStream(actorUserName, true, true);
        Log.d(TAG, "lcs:" + lcs);
        ((TestActivity) testActivity).attachRender1(lcs);
        notifyLock("User1CreateLocalStream");
        // Action 6: User2CreateLocalStream
        act.setCurActionText("Action 6: User2CreateLocalStream");
        // Action 7: User1PublishToUser2
        waitLock("User2CreateLocalStream");
        act.setCurActionText("Action 7: User1PublishToUser2");
        P2PActions.afterWaitCreateLocalCameraStream();
        P2PActions.publish(actorUser, actorUserName, targetUserName, lcs,true);
        P2PActions.afterPublish();
        notifyLock("User1PublishToUser2");
        // Action 8: User2PublishToUser1
        act.setCurActionText("Action 8: User2PublishToUser1");
        // Action 9: User1StopChatWithUser2
        waitLock("User2PublishToUser1");
        List<RemoteStream> rslist = pcObserver.addedStream;
        act.attachRender2(rslist.get(0));
        P2PActions.afterWaitPublish(actorUserName, pcObserver, 1);
        P2PActions.stop(actorUser, actorUserName, targetUserName,true);
        P2PActions.afterStop(actorUserName, pcObserver, 1);
        notifyLock("User1StopChatWithUser2");
        // Action 10: User2InviteUser1
        act.setCurActionText("Action 10: User2InviteUser1");
        // Action 11: User1AcceptUser2
        waitLock("User2InviteUser1");
        act.setCurActionText("Action 11: User1AcceptUser2");
        P2PActions.afterWaitInvite(actorUserName, pcObserver, 1);
        P2PActions.accept(actorUser, actorUserName, targetUserName,true);
        P2PActions.afterAccept(actorUserName, pcObserver, 2);
        notifyLock("User1AcceptUser2");
        // Action 12: User2PublishToUser1
        act.setCurActionText("Action 12: User2PublishToUser1");
        // Action 13: User1PublishToUser2
        waitLock("User2PublishToUser1");
        P2PActions.afterWaitPublish(actorUserName, pcObserver, 2);
        rslist = pcObserver.addedStream;
        act.attachRender2(rslist.get(1));
        P2PActions.publish(actorUser, actorUserName, targetUserName, lcs,true);
        notifyLock("User1PublishToUser2");
        SystemClock.sleep(waitingTime);
        P2PActions.endStory();
        act.makeToast("Test end!");
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

        String actorUserName = "User1";
        String targetUserName = "User2";
        long waitingTime = 3000;
        PeerClientObserverForTest pcObserver = new PeerClientObserverForTest();
        PeerClientConfiguration peerClientConfiguration = new PeerClientConfiguration();
        PeerClient actorUser = new PeerClient(peerClientConfiguration, new SocketSignalingChannel());
        actorUser.addObserver(pcObserver);

        P2PActions.startStory("testTwoUserInteraction");
        act.makeToast("Test start, actor:" + actorUserName);
        act.setTitle("Actor:" + actorUserName);

        // Action 1: User1Connect
        act.setCurActionText("Action 1: User1Connect");
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User1Connect");
        // Action 2: User2Connect
        act.setCurActionText("Action 2: User2Connect");
        // Action 3: User1InviteUser2
        waitLock("User2Connect");
        act.setCurActionText("Action 3: User1InviteUser2");
        P2PActions.afterWaitConnect();
        P2PActions.invite(actorUser, actorUserName, targetUserName,true);
        P2PActions.afterInvite();
        notifyLock("User1InviteUser2");
        // Action 4: User2AcceptUser1
        act.setCurActionText("Action 4: User2AcceptUser1");
        // Action 5: User1CreateLocalStream
        waitLock("User2AcceptUser1");
        act.setCurActionText("Action 5: User1CreateLocalStream");
        P2PActions.afterWaitAccept(actorUserName, pcObserver, 1, 1);
        LocalCameraStream lcs = P2PActions.createLocalCameraStream(actorUserName, true, true);
        Log.d(TAG, "lcs:" + lcs);
        ((TestActivity) testActivity).attachRender1(lcs);
        notifyLock("User1CreateLocalStream");
        // Action 6: User2CreateLocalStream
        act.setCurActionText("Action 6: User2CreateLocalStream");
        // Action 7: User1PublishToUser2
        waitLock("User2CreateLocalStream");
        act.setCurActionText("Action 7: User1PublishToUser2");
        P2PActions.afterWaitCreateLocalCameraStream();
        P2PActions.publish(actorUser, actorUserName, targetUserName, lcs,true);
        P2PActions.afterPublish();
        notifyLock("User1PublishToUser2");
        // Action 8: User2PublishToUser1
        act.setCurActionText("Action 8: User2PublishToUser1");
        // Action 9: User1Disconnect
        waitLock("User2PublishToUser1");
        List<RemoteStream> rslist = pcObserver.addedStream;
        act.attachRender2(rslist.get(0));
        act.setCurActionText("Action 9: User1Disconnect");
        P2PActions.afterWaitPublish(actorUserName, pcObserver, 1);
        P2PActions.disconnect(actorUser, actorUserName,true);
        P2PActions.afterStop(actorUserName, pcObserver, 1);
        P2PActions.afterDisconnect(actorUserName, pcObserver, 1);
        notifyLock("User1Disconnect");
        // Action 10: User2CheckChatStopped
        act.setCurActionText("Action 10: User2CheckChatStopped");
        // Action 11: User1LoginAndInviteUser2
        waitLock("User2CheckChatStopped");
        act.setCurActionText("Action 11: User1LoginAndInviteUser2");
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.invite(actorUser, actorUserName, targetUserName,true);
        notifyLock("User1LoginAndInviteUser2");
        // Action 12: User2AcceptUser1
        act.setCurActionText("Action 12: User2AcceptUser1");
        // Action 13: User1PublishToUser2
        waitLock("User2AcceptUser1");
        act.setCurActionText("Action 13: User1PublishToUser2");
        P2PActions.afterWaitAccept(actorUserName, pcObserver, 2, 2);
        P2PActions.publish(actorUser, actorUserName, targetUserName, lcs,true);
        notifyLock("User1PublishToUser2");
        // Action 14: User2PublishToUser1
        waitLock("User2PublishToUser1");
        act.setCurActionText("Action 14: User2PublishToUser1");
        rslist = pcObserver.addedStream;
        act.attachRender2(rslist.get(1));
        P2PActions.afterWaitPublish(actorUserName, pcObserver, 2);
        P2PActions.endStory();
        act.makeToast("Test end!");
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

        String actorUserName = "User1";
        String targetUserName = "User2";
        long waitingTime = 3000;
        PeerClientObserverForTest pcObserver = new PeerClientObserverForTest();
        PeerClientConfiguration peerClientConfiguration = new PeerClientConfiguration();
        PeerClient actorUser = new PeerClient(peerClientConfiguration, new SocketSignalingChannel());
        actorUser.addObserver(pcObserver);

        P2PActions.startStory("testTwoUserInteraction");
        act.makeToast("Test start, actor:" + actorUserName);
        act.setTitle("Actor:" + actorUserName);

        // Action 1: User1Connect
        act.setCurActionText("Action 1: User1Connect");
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User1Connect");
        // Action 2: User2Connect
        act.setCurActionText("Action 2: User2Connect");
        // Action 3: User1InviteUser2
        waitLock("User2Connect");
        act.setCurActionText("Action 3: User1InviteUser2");
        P2PActions.afterWaitConnect();
        P2PActions.invite(actorUser, actorUserName, targetUserName,true);
        P2PActions.afterInvite();
        notifyLock("User1InviteUser2");
        // Action 4: User2AcceptUser1
        act.setCurActionText("Action 4: User2AcceptUser1");
        // Action 5: User1CreateLocalStream
        waitLock("User2AcceptUser1");
        act.setCurActionText("Action 5: User1CreateLocalStream");
        P2PActions.afterWaitAccept(actorUserName, pcObserver, 1, 1);
        LocalCameraStream lcs = P2PActions.createLocalCameraStream(actorUserName, true, true);
        Log.d(TAG, "lcs:" + lcs);
        ((TestActivity) testActivity).attachRender1(lcs);
        notifyLock("User1CreateLocalStream");
        // Action 6: User2CreateLocalStream
        act.setCurActionText("Action 6: User2CreateLocalStream");
        // Action 7: User1PublishToUser2
        waitLock("User2CreateLocalStream");
        act.setCurActionText("Action 7: User1PublishToUser2");
        P2PActions.afterWaitCreateLocalCameraStream();
        P2PActions.publish(actorUser, actorUserName, targetUserName, lcs,true);
        P2PActions.afterPublish();
        notifyLock("User1PublishToUser2");
        // Action 8: User2PublishToUser1
        act.setCurActionText("Action 8: User2PublishToUser1");
        // Action 9: User1CheckStreamAdded
        waitLock("User2PublishToUser1");
        P2PActions.afterWaitPublish(actorUserName, pcObserver, 1);
        List<RemoteStream> rslist = pcObserver.addedStream;
        act.attachRender2(rslist.get(0));
        notifyLock("User1CheckStreamAdded");
        // Action 10: User2Disconnect
        act.setCurActionText("Action 10: User2Disconnect");
        // Action 11: User1CheckChatStopped
        waitLock("User2Disconnect");
        act.setCurActionText("User1CheckChatStopped");
        P2PActions.afterStop(actorUserName, pcObserver, 1);
        notifyLock("User1CheckChatStopped");
        // Action 12: User2Connect
        act.setCurActionText("Action 12: User2Connect");
        // Action 13:User1InviteUser2
        waitLock("User2Connect");
        act.setCurActionText("Action 13:User1InviteUser2");
        P2PActions.invite(actorUser, actorUserName, targetUserName,true);
        notifyLock("User1InviteUser2");
        // Action 14: User2AcceptUser1
        act.setCurActionText("Action 14: User2AcceptUser1");
        // Action 15: User1PublishToUser2
        waitLock("User2AcceptUser1");
        act.setCurActionText("Action 15: User1PublishToUser2");
        P2PActions.afterWaitAccept(actorUserName, pcObserver, 2, 2);
        P2PActions.publish(actorUser, actorUserName, targetUserName, lcs,true);
        notifyLock("User1PublishToUser2");
        // Action 16: User2PublishToUser1
        act.setCurActionText("Action 16: User2PublishToUser1");
        waitLock("User2PublishToUser1");
        P2PActions.afterWaitPublish(actorUserName, pcObserver, 2);
        rslist = pcObserver.addedStream;
        act.attachRender2(rslist.get(1));
        P2PActions.endStory();
        act.makeToast("Test end!");
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

        String actorUser1Name = "User1";
        String targetUser2Name = "User2";
        String targetUser3Name = "User3";
        PeerClientObserverForTest pcObserver = new PeerClientObserverForTest();
        PeerClientConfiguration peerClientConfiguration = new PeerClientConfiguration();
        PeerClient actorUser1 = new PeerClient(peerClientConfiguration, new SocketSignalingChannel());
        actorUser1.addObserver(pcObserver);
        // Start notice
        P2PActions.startStory("testInteraction");
        act.makeToast("Test start, actor:" + actorUser1Name);
        act.setTitle("Actor:" + actorUser1Name);
        // Action 1. User1ConnectAndCreateLocalStream
        P2PActions.connect(actorUser1, actorUser1Name, serverIP,true);
        LocalCameraStream lcs = P2PActions.createLocalCameraStream(actorUser1Name, true, true);
        act.attachRender1(lcs);
        notifyLock("User1ConnectAndCreateLocalStream");
        // Action 2. User2ConnectAndCreateLocalStream
        // Action 3. User3ConnectAndCreateLocalStream
        // Action 4. User1InviteUser2
        waitLock("User3ConnectAndCreateLocalStream");
        P2PActions.invite(actorUser1, actorUser1Name, targetUser2Name,true);
        notifyLock("User1InviteUser2");
        // Action 5. User2AcceptUser1
        // Action 6. User3InviteUser1
        // Action 7. User1AcceptUser3
        waitLock("User3InviteUser1");
        P2PActions.afterWaitAccept(actorUser1Name, pcObserver, 1, 1);
        P2PActions.afterWaitInvite(actorUser1Name, pcObserver, 1);
        P2PActions.accept(actorUser1, actorUser1Name, targetUser3Name,true);
        P2PActions.afterAccept(actorUser1Name, pcObserver, 2);
        notifyLock("User1AcceptUser3");
        // Action 8. User2InivteUser3
        // Action 9. User3AcceptUser2
        // Action 10. User1PublishToUser2AndUser3
        waitLock("User3AcceptUser2");
        P2PActions.publish(actorUser1, actorUser1Name, targetUser2Name, lcs,true);
        P2PActions.publish(actorUser1, actorUser1Name, targetUser3Name, lcs,true);
        notifyLock("User1PublishToUser2AndUser3");
        // Action 11. User2PublishToUser1AndUser3
        // Action 12. User3PublishToUser1AndUser2
        // Action 13. User1SendMessageToUser2AndUser3
        waitLock("User3PublishToUser1AndUser2");
        P2PActions.afterWaitPublish(actorUser1Name, pcObserver, 2);
        List<RemoteStream> rslist = pcObserver.addedStream;
        act.attachRender2(rslist.get(rslist.size() - 2));
        act.attachRender3(rslist.get(rslist.size() - 1));
        P2PActions.send(actorUser1, actorUser1Name, targetUser2Name, "Message:User1ToUser2",true);
        P2PActions.send(actorUser1, actorUser1Name, targetUser3Name, "Message:User1ToUser3",true);
        notifyLock("User1SendMessageToUser2AndUser3");
        // Action 14. User2SendMessageToUser1AndUser3
        // Action 15. User3SendMessageToUser1AndUser2
        // Action 16. User1UnpublishToUser2AndUser3
        waitLock("User3SendMessageToUser1AndUser2");
        P2PActions.afterWaitSend(actorUser1Name, pcObserver, 2, 2);
        P2PActions.unpublish(actorUser1, actorUser1Name, targetUser2Name, lcs,true);
        P2PActions.unpublish(actorUser1, actorUser1Name, targetUser3Name, lcs,true);
        notifyLock("User1UnpublishToUser2AndUser3");
        // Action 17. User2UnpublishToUser1AndUser3
        // Action 18. User3UnpublishToUser1AndUser2
        // Action 19. User1StopChatWithUser2
        waitLock("User3UnpublishToUser1AndUser2");
        P2PActions.afterWaitUnpublish(actorUser1Name, pcObserver, 2);
        P2PActions.stop(actorUser1, actorUser1Name, targetUser2Name,true);
        P2PActions.afterStop(actorUser1Name, pcObserver, 1);
        notifyLock("User1StopChatWithUser2");
        // Action 20. User2StopChatWithUser3
        // Action 21. User3StopChatWithUser1
        // Action 22. User1Disconnect
        waitLock("User3StopChatWithUser1");
        P2PActions.afterWaitStop(actorUser1Name, pcObserver, 2);
        P2PActions.disconnect(actorUser1, actorUser1Name,true);
        P2PActions.afterDisconnect(actorUser1Name, pcObserver, 1);
        notifyLock("User1Disconnect");
        // sleep to make sure the notify message is sent out
        SystemClock.sleep(waitingTime);
        // Action 23. User2Disconnect
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
