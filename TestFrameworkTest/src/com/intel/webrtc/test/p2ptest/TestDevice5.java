package com.intel.webrtc.test.p2ptest;

import java.util.List;

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

public class TestDevice5 extends AndroidTestDevice {
    String TAG = "P2PTestDevice2";
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
    public void test01_TwoUserInteraction() {
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
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        // Action 3:User1InviteUser2
        act.setCurActionText("Action 3: User1InviteUser2");
        // Action 4:User2AcceptUser1
        waitLock("User1InviteUser2");
        act.setCurActionText("Action 4: User2AcceptUser1");
        P2PActions.afterWaitInvite(actorUserName, pcObserver, 1);
        P2PActions.accept(actorUser, actorUserName, targetUserName,true);
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
        P2PActions.publish(actorUser, actorUserName, targetUserName, lcs,true);
        P2PActions.afterPublish();
        notifyLock("User2PublishToUser1");
        Log.d(TAG, "OBSERVER:" + pcObserver);
        // Action 9:User1SendTwoMessagesToUser2
        act.setCurActionText("Action 9: User1SendTwoMessagesToUser2");
        // Action 10:User2SendTwoMessagesToUser1
        waitLock("User1SendTwoMessagesToUser2");
        act.setCurActionText("Action 10: User2SendTwoMessagesToUser1");
        P2PActions.afterWaitSend(actorUserName, pcObserver, 1, 1);
        P2PActions.send(actorUser, actorUserName, targetUserName,
                actorUserName + " send message1 to " + targetUserName,true);
//        P2PActions.send(actorUser, actorUserName, targetUserName,
//                actorUserName + " send message2 to " + targetUserName);
        P2PActions.afterSend();
        notifyLock("User2SendTwoMessagesToUser1");
        // Action 11:User1UnpublishToUser2
        act.setCurActionText("Action 11: User1UnpublishToUser2");
        // Action 12:User2UnpublishToUser1
        waitLock("User1UnpublishToUser2");
        act.setCurActionText("Action 12: User2UnpublishToUser1");
        P2PActions.afterWaitUnpublish(actorUserName, pcObserver, 1);
        P2PActions.unpublish(actorUser, actorUserName, targetUserName, lcs,true);
        P2PActions.afterUnpublish();
        notifyLock("User2UnpublishToUser1");
        // Action 13:User1StopChatWithUser2
        act.setCurActionText("Action 13: User1StopChatWithUser2");
        // Action 14:User2InviteUser1
        waitLock("User1StopChatWithUser2");
        act.setCurActionText("Action 14:User2InviteUser1");
        P2PActions.afterWaitStop(actorUserName, pcObserver, 1);
        P2PActions.invite(actorUser, actorUserName, targetUserName,true);
        P2PActions.afterInvite();
        notifyLock("User2InviteUser1");
        // Action 15:User1DenyUser2
        act.setCurActionText("Action 15:User1DenyUser2");
        // Action 16:User2Disconnect
        waitLock("User1DenyUser2");
        act.setCurActionText("Action 16:User2Disconnect");
        P2PActions.afterWaitDeny(actorUserName, pcObserver, 1);
        P2PActions.disconnect(actorUser, actorUserName,true);
        P2PActions.afterDisconnect(actorUserName, pcObserver, 1);
        notifyLock("User2Disconnect");
        // Action 17:User1Disconnect
        act.setCurActionText("Action 17:User1Disconnect");
        SystemClock.sleep(waitingTime);
        P2PActions.endStory();
        act.makeToast("Test End!");
    }
    /**
     * Test a normal interaction process between two users.
     * Actors: User1 and User2
     * Story:
     * 1. User1Connect
     * 2. User2Connect
     * 3. User1InviteUser2
     */
    public void test02_Peer1InvitePeer2(){
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
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        // Action 3:User1InviteUser2
        act.setCurActionText("Action 3: User1InviteUser2");
        // Action 4:User2AcceptUser1
        waitLock("User1InviteUser2");
        P2PActions.afterWaitInvite(actorUserName, pcObserver, 1);
        SystemClock.sleep(waitingTime);
        P2PActions.endStory();
        act.makeToast("Test End!");
    }
    /**
     * Test a normal interaction process between two users.
     * Actors: User1 and User2
     * Story:
     * 1. User1Connect
     * 2. User2Connect
     * 3. User2InviteUser1
     */
    public void test03_Peer2InvitePeer1(){
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
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        act.setCurActionText("Action 3: User2InviteUser1");
        P2PActions.invite(actorUser, actorUserName, targetUserName, true);
        P2PActions.afterInvite();
        notifyLock("User2InviteUser1");
        SystemClock.sleep(waitingTime);
        P2PActions.endStory();
        act.makeToast("Test End!");
    }
    /**
     * Test a normal interaction process between two users.
     * Actors: User1 and User2
     * Story:
     * 1. User1Connect
     * 2. User2Connect
     * 3. User1InviteUser2
     * 4. User2AcceptUser1
     */
    public void test04_Peer1InviteAndPeer2Accept(){
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
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        // Action 3:User1InviteUser2
        act.setCurActionText("Action 3: User1InviteUser2");
        // Action 4:User2AcceptUser1
        waitLock("User1InviteUser2");
        act.setCurActionText("Action 4: User2AcceptUser1");
        P2PActions.afterWaitInvite(actorUserName, pcObserver, 1);
        P2PActions.accept(actorUser, actorUserName, targetUserName,true);
        P2PActions.afterAccept(actorUserName, pcObserver, 1);
        notifyLock("User2AcceptUser1");
        SystemClock.sleep(waitingTime);
        P2PActions.endStory();
        act.makeToast("Test End!");
    }
    /**
     * Test a normal interaction process between two users.
     * Actors: User1 and User2
     * Story:
     * 1. User1Connect
     * 2. User2Connect
     * 3. User2InviteUser1
     * 4. User1AcceptUser2
     */
    public void test05_Peer2InviteAndPeer1Accept(){
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
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        act.setCurActionText("Action 3: User2InviteUse1");
        P2PActions.invite(actorUser, actorUserName, targetUserName, true);
        P2PActions.afterInvite();
        notifyLock("User2InviteUse1");
        act.setCurActionText("Action 4: User1AcceptUser2");
        waitLock("User1AcceptUser2");
        SystemClock.sleep(waitingTime);
        P2PActions.endStory();
        act.makeToast("Test End!");
    }
    /**
     * Test a normal interaction process between two users.
     * Actors: User1 and User2
     * Story:
     * 1. User1Connect
     * 2. User2Connect
     * 3. User2InviteUser1
     * 4. User1AcceptUser2
     * 5. User2InviteUser1Again
     */
    public void test06_Peer2InviteAndAcceptThenPeer2InviteAgain(){
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
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        // Action 3:User1InviteUser2
        act.setCurActionText("Action 3: User1InviteUser2");
        // Action 4:User2AcceptUser1
        waitLock("User1InviteUser2");
        act.setCurActionText("Action 4: User2AcceptUser1");
        P2PActions.afterWaitInvite(actorUserName, pcObserver, 1);
        P2PActions.accept(actorUser, actorUserName, targetUserName,true);
        P2PActions.afterAccept(actorUserName, pcObserver, 1);
        notifyLock("User2AcceptUser1");
        //Action 5:User1InviteUser2Again
        waitLock("User1InviteUser2Again");
        P2PActions.afterWaitInvite(actorUserName, pcObserver, 1);
        SystemClock.sleep(waitingTime);
        P2PActions.endStory();
        act.makeToast("Test End!");
    }
    /**
     * Test a normal interaction process between two users.
     * Actors: User1 and User2
     * Story:
     * 1. User1Connect
     * 2. User2Connect
     * 3. User1InviteUser2
     * 4. User2AcceptUser1
     * 5. User1InviteUser2Again
     */
    public void test07_Peer1InviteAndAcceptThenPeer1InviteAgain(){
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
          P2PActions.connect(actorUser, actorUserName, serverIP,true);
          P2PActions.afterConnect();
          notifyLock("User2Connect");
          waitLock("User1InviteUser2");
          act.setCurActionText("Action 3: User2AcceptUser1");
          P2PActions.accept(actorUser, actorUserName, targetUserName, true);
          P2PActions.afterAccept(actorUserName, pcObserver, 1);
          notifyLock("User2AcceptUser1");
          act.setCurActionText("Action 4: User2InviteUser1Again");
          P2PActions.accept(actorUser, actorUserName, targetUserName, false);
          P2PActions.afterAccept(actorUserName, pcObserver, 1);
          SystemClock.sleep(waitingTime);
          P2PActions.endStory();
          act.makeToast("Test End!");
    }
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
     */
    public void test08_PublishEachOther(){
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
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        // Action 3:User1InviteUser2
        act.setCurActionText("Action 3: User1InviteUser2");
        // Action 4:User2AcceptUser1
        waitLock("User1InviteUser2");
        act.setCurActionText("Action 4: User2AcceptUser1");
        P2PActions.afterWaitInvite(actorUserName, pcObserver, 1);
        P2PActions.accept(actorUser, actorUserName, targetUserName,true);
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
        P2PActions.publish(actorUser, actorUserName, targetUserName, lcs,true);
        P2PActions.afterPublish();
        notifyLock("User2PublishToUser1");
        Log.d(TAG, "OBSERVER:" + pcObserver);
        SystemClock.sleep(waitingTime);
        P2PActions.endStory();
        act.makeToast("Test End!");
    }
    /**
     * Test a normal interaction process between two users.
     * Actors: User1 and User2
     * Story:
     * 1. User1Connect
     * 2. User2Connect
     * 3. User1InviteUser2
     * 4. User2AcceptUser1
     * 5. User1CreateLocalStream
     * 6. User1PublishToUser2
     */
    public void test09_Peer1PublishToPeer2(){
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
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        // Action 3:User1InviteUser2
        act.setCurActionText("Action 3: User1InviteUser2");
        // Action 4:User2AcceptUser1
        waitLock("User1InviteUser2");
        act.setCurActionText("Action 4: User2AcceptUser1");
        P2PActions.afterWaitInvite(actorUserName, pcObserver, 1);
        P2PActions.accept(actorUser, actorUserName, targetUserName,true);
        P2PActions.afterAccept(actorUserName, pcObserver, 1);
        notifyLock("User2AcceptUser1");
        // Action 5:User1CreateLocalStream
        act.setCurActionText("Action 5: User1CreateLocalStream");
        // Action 6:User2CreateLocalStream
        waitLock("User1CreateLocalStream");
        act.setCurActionText("Action 6: User1PublishToUser2");
        // Action 8:User2PublishToUser1
        waitLock("User1PublishToUser2");
        act.setCurActionText("Action 7: User2PublishToUser1");
        P2PActions.afterWaitPublish(actorUserName, pcObserver, 1);
        List<RemoteStream> rslist = pcObserver.addedStream;
        act.attachRender2(rslist.get(0));
        SystemClock.sleep(waitingTime);
        P2PActions.endStory();
        act.makeToast("Test End!");
    }
    /**
     * Test a normal interaction process between two users.
     * Actors: User1 and User2
     * Story:
     * 1. User1Connect
     * 2. User2Connect
     * 3. User1InviteUser2
     * 4. User2AcceptUser1
     * 5. User2CreateLocalStream
     * 6. User2PublishToUser1
     */
    public void test10_Peer2PublishToPeer1(){
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
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        // Action 3:User1InviteUser2
        act.setCurActionText("Action 3: User1InviteUser2");
        // Action 4:User2AcceptUser1
        waitLock("User1InviteUser2");
        act.setCurActionText("Action 4: User2AcceptUser1");
        P2PActions.afterWaitInvite(actorUserName, pcObserver, 1);
        P2PActions.accept(actorUser, actorUserName, targetUserName,true);
        P2PActions.afterAccept(actorUserName, pcObserver, 1);
        notifyLock("User2AcceptUser1");
        // Action 5:User1CreateLocalStream
        act.setCurActionText("Action 5: User1CreateLocalStream");
        act.setCurActionText("Action 6: User2CreateLocalStream");
        P2PActions.afterWaitCreateLocalCameraStream();
        LocalCameraStream lcs = P2PActions.createLocalCameraStream(actorUserName, true, true);
        Log.d(TAG, "lcs:" + lcs);
        act.attachRender1(lcs);
        notifyLock("User2CreateLocalStream");
        act.setCurActionText("Action 7: User2PublishToUser1");
        P2PActions.afterWaitPublish(actorUserName, pcObserver, 1);
        List<RemoteStream> rslist = pcObserver.addedStream;
        act.attachRender2(rslist.get(0));
        P2PActions.publish(actorUser, actorUserName, targetUserName, lcs,true);
        P2PActions.afterPublish();
        notifyLock("User2PublishToUser1");
        Log.d(TAG, "OBSERVER:" + pcObserver);
        SystemClock.sleep(waitingTime);
        P2PActions.endStory();
        act.makeToast("Test End!");
    }
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
     * 9. User1PublishToUser2Again
     */
    public void test11_Peer1PublishAgain(){
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
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        // Action 3:User1InviteUser2
        act.setCurActionText("Action 3: User1InviteUser2");
        // Action 4:User2AcceptUser1
        waitLock("User1InviteUser2");
        act.setCurActionText("Action 4: User2AcceptUser1");
        P2PActions.afterWaitInvite(actorUserName, pcObserver, 1);
        P2PActions.accept(actorUser, actorUserName, targetUserName,true);
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
        waitLock("User1PublishToUser2");
        P2PActions.afterWaitPublish(actorUserName, pcObserver, 1);
        //Action 8:User2PublishToUser1
        P2PActions.publish(actorUser, actorUserName, targetUserName, lcs,true);
        P2PActions.afterPublish();
        notifyLock("User2PublishToUser1");
        //Action 9: User1PublishToUser2Again
        waitLock("User1PublishToUser2Again");
        P2PActions.afterWaitPublish(actorUserName, pcObserver, 1);
        SystemClock.sleep(waitingTime);
        P2PActions.endStory();
        act.makeToast("Test End!");
    }
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
     * 9. User2PublishToUser1Again
     */
    public void test12_Peer2PublishAgain(){
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
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        // Action 3:User1InviteUser2
        act.setCurActionText("Action 3: User1InviteUser2");
        // Action 4:User2AcceptUser1
        waitLock("User1InviteUser2");
        act.setCurActionText("Action 4: User2AcceptUser1");
        P2PActions.afterWaitInvite(actorUserName, pcObserver, 1);
        P2PActions.accept(actorUser, actorUserName, targetUserName,true);
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
        waitLock("User1PublishToUser2");
        P2PActions.afterWaitPublish(actorUserName, pcObserver, 1);
        act.setCurActionText("Action 8: User2PublishToUser1");
        //Action 8:User2PublishToUser1
        P2PActions.publish(actorUser, actorUserName, targetUserName, lcs,true);
        P2PActions.afterPublish();
        notifyLock("User2PublishToUser1");
        act.setCurActionText("Action 9: User2PublishToUser1Again");
        P2PActions.publish(actorUser, actorUserName, targetUserName, lcs,false);
        P2PActions.afterPublish();
        notifyLock("User2PublishToUser1Again");
    }
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
     * 9. User1UnpublishToUser2
     * 10. User2UnpublishToUser1
     */
    public void test13_PublishUnpublishEachOther(){
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
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        // Action 3:User1InviteUser2
        act.setCurActionText("Action 3: User1InviteUser2");
        // Action 4:User2AcceptUser1
        waitLock("User1InviteUser2");
        act.setCurActionText("Action 4: User2AcceptUser1");
        P2PActions.afterWaitInvite(actorUserName, pcObserver, 1);
        P2PActions.accept(actorUser, actorUserName, targetUserName,true);
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
        P2PActions.publish(actorUser, actorUserName, targetUserName, lcs,true);
        P2PActions.afterPublish();
        notifyLock("User2PublishToUser1");
        Log.d(TAG, "OBSERVER:" + pcObserver);
        // Action 9:User1UnpublishToUser2
        act.setCurActionText("Action 9: User1UnpublishToUser2");
        // Action 10:User2UnpublishToUser1
        waitLock("User1UnpublishToUser2");
        P2PActions.afterWaitUnpublish(actorUserName, pcObserver, 1);
        P2PActions.unpublish(actorUser, actorUserName, targetUserName, lcs,true);
        P2PActions.afterUnpublish();
        notifyLock("User2UnpublishToUser1");
        SystemClock.sleep(waitingTime);
        P2PActions.endStory();
        act.makeToast("Test End!");
    }
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
     * 9. User1UnpublishToUser2
     */
    public void test14_PublishPeer1Unpublish(){
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
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        // Action 3:User1InviteUser2
        act.setCurActionText("Action 3: User1InviteUser2");
        // Action 4:User2AcceptUser1
        waitLock("User1InviteUser2");
        act.setCurActionText("Action 4: User2AcceptUser1");
        P2PActions.afterWaitInvite(actorUserName, pcObserver, 1);
        P2PActions.accept(actorUser, actorUserName, targetUserName,true);
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
        P2PActions.publish(actorUser, actorUserName, targetUserName, lcs,true);
        P2PActions.afterPublish();
        notifyLock("User2PublishToUser1");
        Log.d(TAG, "OBSERVER:" + pcObserver);
        // Action 9:User1UnpublishToUser2
        act.setCurActionText("Action 9: User1UnpublishToUser2");
        // Action 10:User2UnpublishToUser1
        waitLock("User1UnpublishToUser2");
        SystemClock.sleep(waitingTime);
        P2PActions.endStory();
        act.makeToast("Test End!");
    }
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
     * 9. User2UnpublishToUser1
     */
    public void test15_PublishPeer2Unpublish(){
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
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        // Action 3:User1InviteUser2
        act.setCurActionText("Action 3: User1InviteUser2");
        // Action 4:User2AcceptUser1
        waitLock("User1InviteUser2");
        act.setCurActionText("Action 4: User2AcceptUser1");
        P2PActions.afterWaitInvite(actorUserName, pcObserver, 1);
        P2PActions.accept(actorUser, actorUserName, targetUserName,true);
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
        P2PActions.publish(actorUser, actorUserName, targetUserName, lcs,true);
        P2PActions.afterPublish();
        notifyLock("User2PublishToUser1");
        Log.d(TAG, "OBSERVER:" + pcObserver);
        P2PActions.afterWaitUnpublish(actorUserName, pcObserver, 1);
        P2PActions.unpublish(actorUser, actorUserName, targetUserName, lcs,true);
        P2PActions.afterUnpublish();
        notifyLock("User2UnpublishToUser1");
        SystemClock.sleep(waitingTime);
        P2PActions.endStory();
        act.makeToast("Test End!");
    }
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
     * 9. User1UnpublishToUser2
     * 10. User2UnpublishToUser1
     * 11. User1PublishToUser2Again
     */
    public void test16_PublishUnpublishThenPeer1RePublish(){
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
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        // Action 3:User1InviteUser2
        act.setCurActionText("Action 3: User1InviteUser2");
        // Action 4:User2AcceptUser1
        waitLock("User1InviteUser2");
        act.setCurActionText("Action 4: User2AcceptUser1");
        P2PActions.afterWaitInvite(actorUserName, pcObserver, 1);
        P2PActions.accept(actorUser, actorUserName, targetUserName,true);
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
        P2PActions.publish(actorUser, actorUserName, targetUserName, lcs,true);
        P2PActions.afterPublish();
        notifyLock("User2PublishToUser1");
        Log.d(TAG, "OBSERVER:" + pcObserver);
        // Action 9:User1UnpublishToUser2
        act.setCurActionText("Action 9: User1UnpublishToUser2");
        // Action 10:User2UnpublishToUser1
        waitLock("User1UnpublishToUser2");
        P2PActions.afterWaitUnpublish(actorUserName, pcObserver, 1);
        act.setCurActionText("Action 10: User2UnpublishToUser1");
        P2PActions.unpublish(actorUser, actorUserName, targetUserName, lcs,true);
        P2PActions.afterUnpublish();
        notifyLock("User2UnpublishToUser1");
        act.setCurActionText("Action 11: Wait User1RePublishToUser2");
        waitLock("User1RePublishToUser2");
        P2PActions.afterWaitPublish(actorUserName, pcObserver, 2);
        SystemClock.sleep(waitingTime);
        P2PActions.endStory();
        act.makeToast("Test End!");
    }
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
     * 9. User1UnpublishToUser2
     * 10. User2UnpublishToUser1
     * 11. User2PublishToUser1Again
     */
    public void test17_PublishUnpublishThenPeer2RePublish(){
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
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        // Action 3:User1InviteUser2
        act.setCurActionText("Action 3: User1InviteUser2");
        // Action 4:User2AcceptUser1
        waitLock("User1InviteUser2");
        act.setCurActionText("Action 4: User2AcceptUser1");
        P2PActions.afterWaitInvite(actorUserName, pcObserver, 1);
        P2PActions.accept(actorUser, actorUserName, targetUserName,true);
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
        P2PActions.publish(actorUser, actorUserName, targetUserName, lcs,true);
        P2PActions.afterPublish();
        notifyLock("User2PublishToUser1");
        Log.d(TAG, "OBSERVER:" + pcObserver);
        // Action 9:User1UnpublishToUser2
        act.setCurActionText("Action 9: User1UnpublishToUser2");
        // Action 10:User2UnpublishToUser1
        waitLock("User1UnpublishToUser2");
        P2PActions.afterWaitUnpublish(actorUserName, pcObserver, 1);
        act.setCurActionText("Action 10: User2UnpublishToUser1");
        P2PActions.unpublish(actorUser, actorUserName, targetUserName, lcs,true);
        P2PActions.afterUnpublish();
        notifyLock("User2UnpublishToUser1");
        act.setCurActionText("Action 11: User2RePublishToUser1");
        P2PActions.publish(actorUser, actorUserName, targetUserName, lcs,true);
        P2PActions.afterPublish();
        notifyLock("User2RePublishToUser1");
        SystemClock.sleep(waitingTime);
        P2PActions.endStory();
        act.makeToast("Test End!");
    }
    /**
     * Test a normal interaction process between two users.
     * Actors: User1 and User2
     * Story:
     * 1. User1Connect
     * 2. User2Connect
     * 3. User1InviteUser2
     * 4. User2AcceptUser1
     * 5. User1CreateLocalStreamAudioOnly
     * 6. User2CreateLocalStreamAudioOnly
     * 7. User1PublishToUser2
     * 8. User2PublishToUser1
     */
    public void test18_PublishAudioOnlyEachOther(){
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
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        // Action 3:User1InviteUser2
        act.setCurActionText("Action 3: User1InviteUser2");
        // Action 4:User2AcceptUser1
        waitLock("User1InviteUser2");
        act.setCurActionText("Action 4: User2AcceptUser1");
        P2PActions.afterWaitInvite(actorUserName, pcObserver, 1);
        P2PActions.accept(actorUser, actorUserName, targetUserName,true);
        P2PActions.afterAccept(actorUserName, pcObserver, 1);
        notifyLock("User2AcceptUser1");
        // Action 5:User1CreateLocalStream
        act.setCurActionText("Action 5: User1CreateLocalStream");
        // Action 6:User2CreateLocalStream
        waitLock("User1CreateLocalStream");
        act.setCurActionText("Action 6: User2CreateLocalStream");
        P2PActions.afterWaitCreateLocalCameraStream();
        LocalCameraStream lcs = P2PActions.createLocalCameraStream(actorUserName, false, true);
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
        P2PActions.publish(actorUser, actorUserName, targetUserName, lcs,true);
        P2PActions.afterPublish();
        notifyLock("User2PublishToUser1");
        Log.d(TAG, "OBSERVER:" + pcObserver);
        SystemClock.sleep(waitingTime);
        P2PActions.endStory();
        act.makeToast("Test End!");
    }
    /**
     * Test a normal interaction process between two users.
     * Actors: User1 and User2
     * Story:
     * 1. User1Connect
     * 2. User2Connect
     * 3. User1InviteUser2
     * 4. User2AcceptUser1
     * 5. User1CreateLocalStreamAudio
     * 6. User1PublishToUser2
     */
    public void test19_Peer1PublishAudioOnly(){
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
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        // Action 3:User1InviteUser2
        act.setCurActionText("Action 3: User1InviteUser2");
        // Action 4:User2AcceptUser1
        waitLock("User1InviteUser2");
        act.setCurActionText("Action 4: User2AcceptUser1");
        P2PActions.afterWaitInvite(actorUserName, pcObserver, 1);
        P2PActions.accept(actorUser, actorUserName, targetUserName,true);
        P2PActions.afterAccept(actorUserName, pcObserver, 1);
        notifyLock("User2AcceptUser1");
        // Action 5:User1CreateLocalStream
        act.setCurActionText("Action 5: User1CreateLocalStream");
        // Action 6:User2CreateLocalStream
        waitLock("User1CreateLocalStream");
        act.setCurActionText("Action 7: User1PublishToUser2");
        // Action 8:User2PublishToUser1
        waitLock("User1PublishToUser2");
        act.setCurActionText("Action 8: User2PublishToUser1");
        P2PActions.afterWaitPublish(actorUserName, pcObserver, 1);
        SystemClock.sleep(waitingTime);
        P2PActions.endStory();
        act.makeToast("Test End!");
    }
    /**
     * Test a normal interaction process between two users.
     * Actors: User1 and User2
     * Story:
     * 1. User1Connect
     * 2. User2Connect
     * 3. User1InviteUser2
     * 4. User2AcceptUser1
     * 5. User2CreateLocalStreamAudio
     * 6. User2PublishToUser1
     */
    public void test20_Peer2PublishAudioOnly(){
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
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        // Action 3:User1InviteUser2
        act.setCurActionText("Action 3: User1InviteUser2");
        // Action 4:User2AcceptUser1
        waitLock("User1InviteUser2");
        act.setCurActionText("Action 4: User2AcceptUser1");
        P2PActions.afterWaitInvite(actorUserName, pcObserver, 1);
        P2PActions.accept(actorUser, actorUserName, targetUserName,true);
        P2PActions.afterAccept(actorUserName, pcObserver, 1);
        notifyLock("User2AcceptUser1");
        act.setCurActionText("Action 5: User2CreateLocalStream");
        P2PActions.afterWaitCreateLocalCameraStream();
        LocalCameraStream lcs = P2PActions.createLocalCameraStream(actorUserName, false, true);
        Log.d(TAG, "lcs:" + lcs);
        act.attachRender1(lcs);
        notifyLock("User2CreateLocalStream");
        P2PActions.publish(actorUser, actorUserName, targetUserName, lcs,true);
        P2PActions.afterPublish();
        notifyLock("User2PublishToUser1");
        Log.d(TAG, "OBSERVER:" + pcObserver);
        SystemClock.sleep(waitingTime);
        P2PActions.endStory();
        act.makeToast("Test End!");
    }
    /**
     * Test a normal interaction process between two users.
     * Actors: User1 and User2
     * Story:
     * 1. User1Connect
     * 2. User2Connect
     * 3. User1InviteUser2
     * 4. User2AcceptUser1
     * 5. User1CreateLocalStream
     * 6. User1UnpublishToUser2
     */
    public void test21_Peer1UnpublishBeforePublish(){
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
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        // Action 3:User1InviteUser2
        act.setCurActionText("Action 3: User1InviteUser2");
        // Action 4:User2AcceptUser1
        waitLock("User1InviteUser2");
        act.setCurActionText("Action 4: User2AcceptUser1");
        P2PActions.afterWaitInvite(actorUserName, pcObserver, 1);
        P2PActions.accept(actorUser, actorUserName, targetUserName,true);
        P2PActions.afterAccept(actorUserName, pcObserver, 1);
        notifyLock("User2AcceptUser1");
        // Action 5:User1UnpublishToUser2
        act.setCurActionText("Action 5: Wait User1UnpublishToUser2");
        waitLock("User1UnpublishToUser2");
        P2PActions.afterWaitUnpublish(actorUserName, pcObserver, 0);
        SystemClock.sleep(waitingTime);
        P2PActions.endStory();
        act.makeToast("Test End!");
    }
    /**
     * Test a normal interaction process between two users.
     * Actors: User1 and User2
     * Story:
     * 1. User1Connect
     * 2. User2Connect
     * 3. User2CreateLocalStream
     * 4. User2PublishToUser1
     */
    public void test22_Peer2PublishBeforeInvite(){
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
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        waitLock("User1CreateLocalStream");
        act.setCurActionText("Action 3: User2CreateLocalStream");
        P2PActions.afterWaitCreateLocalCameraStream();
        LocalCameraStream lcs = P2PActions.createLocalCameraStream(actorUserName, true, true);
        Log.d(TAG, "lcs:" + lcs);
        act.attachRender1(lcs);
        notifyLock("User2CreateLocalStream");
        act.setCurActionText("Action 4: User2PublishToUser1");
        P2PActions.publish(actorUser, actorUserName, targetUserName, lcs,false);
        P2PActions.afterPublish();
        notifyLock("User2PublishToUser1");
        SystemClock.sleep(waitingTime);
        P2PActions.endStory();
        act.makeToast("Test End!");
    }
    /**
     * Test a normal interaction process between two users.
     * Actors: User1 and User2
     * Story:
     * 1. User1Connect
     * 2. User2Connect
     * 3. User1CreateLocalStream
     * 4. User1PublishToUser2
     */
    public void test23_Peer1PublishBeforeInvite(){
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
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        waitLock("User1CreateLocalStream");
        waitLock("User1PublishToUser2");
        P2PActions.afterWaitUnpublish(actorUserName, pcObserver, 0);
        SystemClock.sleep(waitingTime);
        P2PActions.endStory();
        act.makeToast("Test End!");
    }
    /**
     * Test a normal interaction process between two users.
     * Actors: User1 and User2
     * Story:
     * 1. User1Connect
     * 2. User2CreateLocalStream
     * 3. User2PublishToUser1
     */
    public void test24_Peer2PublishBeforeLogin(){
        initTestActivity();
        // Init variables
        String actorUserName = "User2";
        String targetUserName = "User1";
        PeerClientObserverForTest pcObserver = new PeerClientObserverForTest();
        PeerClientConfiguration peerClientConfiguration = new PeerClientConfiguration();
        PeerClient actorUser = new PeerClient(peerClientConfiguration, new SocketSignalingChannel());
        actorUser.addObserver(pcObserver);
        P2PActions.afterWaitCreateLocalCameraStream();
        LocalCameraStream lcs = P2PActions.createLocalCameraStream(actorUserName, true, true);
        Log.d(TAG, "lcs:" + lcs);
        act.attachRender1(lcs);
        act.setCurActionText("Action 1: User1Connect");
        // Action 8:User2PublishToUser1
        waitLock("User1Connect");
        act.setCurActionText("Action 2: User2PublishToUser1");
        P2PActions.afterWaitPublish(actorUserName, pcObserver, 0);
        P2PActions.publish(actorUser, actorUserName, targetUserName, lcs,false);
        P2PActions.afterPublish();
        notifyLock("User2PublishToUser1");
        SystemClock.sleep(waitingTime);
        P2PActions.endStory();
        act.makeToast("Test End!");
    }
    /**
     * Test a normal interaction process between two users.
     * Actors: User1 and User2
     * Story:
     * 1. User2Connect
     * 2. User1CreateLocalStream
     * 3. User1PublishToUser2
     */
    public void test25_Peer1PublishBeforeLogin(){
        initTestActivity();
        // Init variables
        String actorUserName = "User2";
        String targetUserName = "User1";
        PeerClientObserverForTest pcObserver = new PeerClientObserverForTest();
        PeerClientConfiguration peerClientConfiguration = new PeerClientConfiguration();
        PeerClient actorUser = new PeerClient(peerClientConfiguration, new SocketSignalingChannel());
        actorUser.addObserver(pcObserver);
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        waitLock("User1CreateLocalStream");
        waitLock("User1PublishToUser2");
        act.setCurActionText("Action 2: User2PublishToUser1");
        P2PActions.afterWaitPublish(actorUserName, pcObserver, 0);
        SystemClock.sleep(waitingTime);
        P2PActions.endStory();
        act.makeToast("Test End!");
    }
    /**
     * Test a normal interaction process between two users.
     * Actors: User1 and User2
     * Story:
     * 1. User1Connect
     * 2. User2Connect
     * 3. User1InviteUser2
     * 4. User2CreateLocalStream
     * 5. User2PublishToUser1
     */
    public void test26_Peer2PublishBeforeAccept(){
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
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        // Action 3:User1InviteUser2
        act.setCurActionText("Action 3: User1InviteUser2");
        // Action 4:User2AcceptUser1
        waitLock("User1InviteUser2");
        act.setCurActionText("Action 4: User2AcceptUser1");
        P2PActions.afterWaitInvite(actorUserName, pcObserver, 1);
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
        act.setCurActionText("Action 8: User2PublishToUser1");
        P2PActions.publish(actorUser, actorUserName, targetUserName, lcs,false);
        P2PActions.afterPublish();
        notifyLock("User2PublishToUser1");
        SystemClock.sleep(waitingTime);
        P2PActions.endStory();
        act.makeToast("Test End!");
    }
    /**
     * Test a normal interaction process between two users.
     * Actors: User1 and User2
     * Story:
     * 1. User1Connect
     * 2. User2Connect
     * 3. User1InviteUser2
     * 4. User1CreateLocalStream
     * 5. User1PublishToUser2
     */
    public void test27_Peer1PublishBeforeAccept(){
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
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        // Action 3:User1InviteUser2
        act.setCurActionText("Action 3: User1InviteUser2");
        // Action 4:User2AcceptUser1
        waitLock("User1InviteUser2");
        act.setCurActionText("Action 4: User2AcceptUser1");
        P2PActions.afterWaitInvite(actorUserName, pcObserver, 1);
        act.setCurActionText("Action 7: User1PublishToUser2");
        // Action 8:User2PublishToUser1
        waitLock("User1PublishToUser2");
        P2PActions.afterWaitPublish(actorUserName, pcObserver, 0);
        SystemClock.sleep(waitingTime);
        P2PActions.endStory();
        act.makeToast("Test End!");
    }
    /**
     * Test a normal interaction process between two users.
     * Actors: User1 and User2
     * Story:
     * 1. User1Connect
     * 2. User2Connect
     * 3. User2CreateLocalStream
     * 4. User2UnpublishToUser1
     */
    public void test28_Peer2UnpublishBeforeInvite(){
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
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        act.setCurActionText("Action 5: User1CreateLocalStream");
        // Action 6:User2CreateLocalStream
        waitLock("User1CreateLocalStream");
        act.setCurActionText("Action 6: User2CreateLocalStream");
        P2PActions.afterWaitCreateLocalCameraStream();
        LocalCameraStream lcs = P2PActions.createLocalCameraStream(actorUserName, true, true);
        Log.d(TAG, "lcs:" + lcs);
        act.attachRender1(lcs);
        notifyLock("User2CreateLocalStream");
        act.setCurActionText("Action 8: User2UnpublishToUser1");
        P2PActions.unpublish(actorUser, actorUserName, targetUserName, lcs,false);
        P2PActions.afterUnpublish();
        notifyLock("User2UnpublishToUser1");
        SystemClock.sleep(waitingTime);
        P2PActions.endStory();
        act.makeToast("Test End!");
    }
    /**
     * Test a normal interaction process between two users.
     * Actors: User1 and User2
     * Story:
     * 1. User1Connect
     * 2. User2Connect
     * 3. User1CreateLocalStream
     * 4. User1UnpublishToUser2
     */
    public void test29_Peer1UnpublishBeforeInvite(){
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
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        act.setCurActionText("Action 5: User1CreateLocalStream");
        // Action 6:User2CreateLocalStream
        waitLock("User1CreateLocalStream");
        waitLock("User1UnpublishToUser2");
        P2PActions.afterWaitPublish(actorUserName, pcObserver, 0);
        notifyLock("User2UnpublishToUser1");
        SystemClock.sleep(waitingTime);
        P2PActions.endStory();
        act.makeToast("Test End!");
    }
    /**
     * Test a normal interaction process between two users.
     * Actors: User1 and User2
     * Story:
     * 1. User1Connect
     * 2. User2Connect
     * 3. User2InviteUser1
     * 4. User1DenyUser2
     */
    public void test30_Peer2InviteAndDeny(){
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
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        P2PActions.invite(actorUser, actorUserName, targetUserName,true);
        P2PActions.afterInvite();
        notifyLock("User2InviteUser1");
        // Action 15:User1DenyUser2
        act.setCurActionText("Action 3:User1DenyUser2");
        // Action 16:User2Disconnect
        waitLock("User1DenyUser2");
        act.setCurActionText("Action 4:User2Disconnect");
        P2PActions.afterWaitDeny(actorUserName, pcObserver, 1);
        SystemClock.sleep(waitingTime);
        P2PActions.endStory();
        act.makeToast("Test End!");
    }
    /**
     * Test a normal interaction process between two users.
     * Actors: User1 and User2
     * Story:
     * 1. User1Connect
     * 2. User2Connect
     * 3. User1InviteUser2
     * 4. User2DenyUser1
     */
    public void test31_Peer1InviteAndDeny(){
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
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        waitLock("User1InviteUser2");
        P2PActions.deny(actorUser, actorUserName, targetUserName,true);
        P2PActions.afterDeny();
        notifyLock("User2DenyUser1");
        SystemClock.sleep(waitingTime);
        P2PActions.endStory();
        act.makeToast("Test End!");
    }
    /**
     * Test a normal interaction process between two users.
     * Actors: User1 and User2
     * Story:
     * 1. User1Connect
     * 2. User2Connect
     * 3. User2InviteUser1
     * 4. User1DenyUser2
     * 5. User2CreateLocalStream
     * 6. User2PublishToUser1
     */
    public void test32_Peer2InviteAndPeer1DenyPeer2Publish(){
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
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        P2PActions.invite(actorUser, actorUserName, targetUserName,true);
        P2PActions.afterInvite();
        notifyLock("User2InviteUser1");
        // Action 15:User1DenyUser2
        act.setCurActionText("Action 3:User1DenyUser2");
        // Action 16:User2Disconnect
        waitLock("User1DenyUser2");
        act.setCurActionText("Action 4:User2Disconnect");
        P2PActions.afterWaitDeny(actorUserName, pcObserver, 1);
        act.setCurActionText("Action 5: User1CreateLocalStream");
        // Action 6:User2CreateLocalStream
        waitLock("User1CreateLocalStream");
        act.setCurActionText("Action 6: User2CreateLocalStream");
        P2PActions.afterWaitCreateLocalCameraStream();
        LocalCameraStream lcs = P2PActions.createLocalCameraStream(actorUserName, true, true);
        Log.d(TAG, "lcs:" + lcs);
        act.attachRender1(lcs);
        notifyLock("User2CreateLocalStream");
        act.setCurActionText("Action 7: User2PublishToUser1");
        P2PActions.publish(actorUser, actorUserName, targetUserName, lcs,false);
        P2PActions.afterPublish();
        notifyLock("User2PublishToUser1");
        Log.d(TAG, "OBSERVER:" + pcObserver);
        SystemClock.sleep(waitingTime);
        P2PActions.endStory();
        act.makeToast("Test End!");
    }
    /**
     * Test a normal interaction process between two users.
     * Actors: User1 and User2
     * Story:
     * 1. User1Connect
     * 2. User2Connect
     * 3. User1InviteUser2
     * 4. User2DenyUser1
     * 5. User1CreateLocalStreaM
     * 6. User1PublishToUser2
     */
    public void test33_Peer1InviteAndPeer2DenyPeer1Publish(){
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
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        P2PActions.invite(actorUser, actorUserName, targetUserName,true);
        P2PActions.afterInvite();
        notifyLock("User2InviteUser1");
        P2PActions.deny(actorUser, actorUserName, targetUserName,true);
        P2PActions.afterDeny();
        notifyLock("User2DenyUser1");
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
        P2PActions.afterWaitPublish(actorUserName, pcObserver, 0);
        SystemClock.sleep(waitingTime);
        P2PActions.endStory();
        act.makeToast("Test End!");
    }
    /**
     * Test a normal interaction process between two users.
     * Actors: User1 and User2
     * Story:
     * 1. User1Connect
     * 2. User2Connect
     * 3. User2AcceptUser1
     */
    public void test34_Peer2AcceptBeforeInvite(){
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
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        P2PActions.accept(actorUser, actorUserName, targetUserName,false);
        P2PActions.afterAccept(actorUserName, pcObserver, 0);
        notifyLock("User2AcceptUser1");
        SystemClock.sleep(waitingTime);
        P2PActions.endStory();
        act.makeToast("Test End!");
    }
    /**
     * Test a normal interaction process between two users.
     * Actors: User1 and User2
     * Story:
     * 1. User1Connect
     * 2. User2Connect
     * 3. User1AcceptUser2
     */
    public void test35_Peer1AcceptBeforeInvite(){
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
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        waitLock("User1AcceptUser2");
        P2PActions.afterWaitAccept(actorUserName, pcObserver, 0, 0);
        SystemClock.sleep(waitingTime);
        P2PActions.endStory();
        act.makeToast("Test End!");
    }
    /**
     * Test a normal interaction process between two users.
     * Actors: User1 and User2
     * Story:
     * 1. User1Connect
     * 2. User2Connect
     * 3. User2AcceptUser1
     * 4. User1InviteUser2
     * 5. User2AcceptUser1
     */
    public void test36_Peer2AcceptBeforeInviteThenInviteAndAccept(){
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
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        P2PActions.accept(actorUser, actorUserName, targetUserName,false);
        P2PActions.afterAccept(actorUserName, pcObserver, 0);
        notifyLock("User2AcceptUser1");
        waitLock("User1InviteUser2");
        act.setCurActionText("Action 3: User2AcceptUser1");
        P2PActions.afterWaitInvite(actorUserName, pcObserver, 1);
        P2PActions.accept(actorUser, actorUserName, targetUserName,true);
        P2PActions.afterAccept(actorUserName, pcObserver, 1);
        notifyLock("User2AcceptUser1");
        SystemClock.sleep(waitingTime);
        P2PActions.endStory();
        act.makeToast("Test End!");
    }
    /**
     * Test a normal interaction process between two users.
     * Actors: User1 and User2
     * Story:
     * 1. User2Connect
     * 2. User1StopChatWithUser2
     */
    public void test37_Peer1StopBeforeConnect(){
        initTestActivity();
        // Init variables

        String actorUserName = "User2";
        String targetUserName = "User1";
        PeerClientObserverForTest pcObserver = new PeerClientObserverForTest();
        PeerClientConfiguration peerClientConfiguration = new PeerClientConfiguration();
        PeerClient actorUser = new PeerClient(peerClientConfiguration, new SocketSignalingChannel());
        actorUser.addObserver(pcObserver);
        act.makeToast("Test start, actor:" + actorUserName);
        waitLock("User1StopChatWithUser2");
        act.setCurActionText("Action 1:User2InviteUser1");
        P2PActions.afterWaitStop(actorUserName, pcObserver, 0);
        act.setTitle("Actor:" + actorUserName);
        // Action 1:User1Connect
        act.setCurActionText("Action 2: User1Connect");
        // Action 2:User2Connect
        waitLock("User1Connect");
        act.setCurActionText("Action 3: User2Connect");
        P2PActions.afterWaitConnect();
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        SystemClock.sleep(waitingTime);
        P2PActions.endStory();
        act.makeToast("Test End!");
    }
    /**
     * Test a normal interaction process between two users.
     * Actors: User1 and User2
     * Story:
     * 1. User1Connect
     * 2. User2StopChatWithUser1
     */
    public void test38_Peer2StopBeforeConnect(){
        initTestActivity();
        // Init variables

        String actorUserName = "User2";
        String targetUserName = "User1";
        PeerClientObserverForTest pcObserver = new PeerClientObserverForTest();
        PeerClientConfiguration peerClientConfiguration = new PeerClientConfiguration();
        PeerClient actorUser = new PeerClient(peerClientConfiguration, new SocketSignalingChannel());
        actorUser.addObserver(pcObserver);
        act.makeToast("Test start, actor:" + actorUserName);

        P2PActions.stop(actorUser, actorUserName, targetUserName,false);
        P2PActions.afterStop(actorUserName, pcObserver, 0);
        notifyLock("User2StopUser1");
        act.setTitle("Actor:" + actorUserName);
        // Action 1:User1Connect
        act.setCurActionText("Action 1: User1Connect");
        // Action 2:User2Connect
        waitLock("User1Connect");
        act.setCurActionText("Action 2: User2Connect");
        P2PActions.afterWaitConnect();
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        SystemClock.sleep(waitingTime);
        P2PActions.endStory();
        act.makeToast("Test End!");
    }
    /**
     * Test a normal interaction process between two users.
     * Actors: User1 and User2
     * Story:
     * 1. User1Connect
     * 2. User2Connect
     * 3. User1InviteUser2
     * 4. User2AcceptUser1
     * 5. User1StopChatWithUser2
     */
    public void test39_Peer1StopAfterAccept(){
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
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        // Action 3:User1InviteUser2
        act.setCurActionText("Action 3: User1InviteUser2");
        // Action 4:User2AcceptUser1
        waitLock("User1InviteUser2");
        act.setCurActionText("Action 4: User2AcceptUser1");
        P2PActions.afterWaitInvite(actorUserName, pcObserver, 1);
        P2PActions.accept(actorUser, actorUserName, targetUserName,true);
        P2PActions.afterAccept(actorUserName, pcObserver, 1);
        notifyLock("User2AcceptUser1");
        // Action 13:User1StopChatWithUser2
        act.setCurActionText("Action 5: User1StopChatWithUser2");
        // Action 14:User2InviteUser1
        waitLock("User1StopChatWithUser2");
        act.setCurActionText("Action 6:User2InviteUser1");
        P2PActions.afterWaitStop(actorUserName, pcObserver, 1);
        SystemClock.sleep(waitingTime);
        P2PActions.endStory();
        act.makeToast("Test End!");
    }
    /**
     * Test a normal interaction process between two users.
     * Actors: User1 and User2
     * Story:
     * 1. User1Connect
     * 2. User2Connect
     * 3. User1InviteUser2
     * 4. User2AcceptUser1
     * 5. User2StopChatWithUser1
     */
    public void test40_Peer2StopAfterAccept(){
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
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        // Action 3:User1InviteUser2
        act.setCurActionText("Action 3: User1InviteUser2");
        // Action 4:User2AcceptUser1
        waitLock("User1InviteUser2");
        act.setCurActionText("Action 4: User2AcceptUser1");
        P2PActions.afterWaitInvite(actorUserName, pcObserver, 1);
        P2PActions.accept(actorUser, actorUserName, targetUserName,true);
        P2PActions.afterAccept(actorUserName, pcObserver, 1);
        notifyLock("User2AcceptUser1");
        act.setCurActionText("Action 5: User2StopChatWithUser1");
        P2PActions.stop(actorUser, actorUserName, targetUserName,true);
        P2PActions.afterStop(actorUserName, pcObserver, 1);
        notifyLock("User2StopChatWithUser1");
        SystemClock.sleep(waitingTime);
        P2PActions.endStory();
        act.makeToast("Test End!");
    }
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
     * 9. User1StopChatWithUser2
     */
    public void test41_Peer1StopAfterPublish(){
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
         P2PActions.connect(actorUser, actorUserName, serverIP,true);
         P2PActions.afterConnect();
         notifyLock("User2Connect");
         // Action 3:User1InviteUser2
         act.setCurActionText("Action 3: User1InviteUser2");
         // Action 4:User2AcceptUser1
         waitLock("User1InviteUser2");
         act.setCurActionText("Action 4: User2AcceptUser1");
         P2PActions.afterWaitInvite(actorUserName, pcObserver, 1);
         P2PActions.accept(actorUser, actorUserName, targetUserName,true);
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
         P2PActions.publish(actorUser, actorUserName, targetUserName, lcs,true);
         P2PActions.afterPublish();
         notifyLock("User2PublishToUser1");
         Log.d(TAG, "OBSERVER:" + pcObserver);
         act.setCurActionText("Action 9: User1StopChatWithUser2");
         SystemClock.sleep(waitingTime);
         P2PActions.afterWaitStop(actorUserName, pcObserver, 1);
         P2PActions.endStory();
         act.makeToast("Test End!");
    }
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
     * 9. User2StopChatWithUser1
     */
    public void test42_Peer2StopAfterPublish(){
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
         P2PActions.connect(actorUser, actorUserName, serverIP,true);
         P2PActions.afterConnect();
         notifyLock("User2Connect");
         // Action 3:User1InviteUser2
         act.setCurActionText("Action 3: User1InviteUser2");
         // Action 4:User2AcceptUser1
         waitLock("User1InviteUser2");
         act.setCurActionText("Action 4: User2AcceptUser1");
         P2PActions.afterWaitInvite(actorUserName, pcObserver, 1);
         P2PActions.accept(actorUser, actorUserName, targetUserName,true);
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
         P2PActions.publish(actorUser, actorUserName, targetUserName, lcs,true);
         P2PActions.afterPublish();
         notifyLock("User2PublishToUser1");
         Log.d(TAG, "OBSERVER:" + pcObserver);
         // Action 13:User1StopChatWithUser2
         act.setCurActionText("Action 9: User2StopChatWithUser1");
         P2PActions.stop(actorUser, actorUserName, targetUserName,true);
         P2PActions.afterStop(actorUserName, pcObserver, 0);
         notifyLock("User2StopChatWithUser1");
         SystemClock.sleep(waitingTime);
         P2PActions.afterWaitStop(actorUserName, pcObserver, 1);
         P2PActions.endStory();
         act.makeToast("Test End!");
    }
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
     * 9. User1UnpublishToUser2
     * 10. User2UnpublishToUser1
     * 11. User1StopChatWithUser2
     */
    public void test43_Peer1StopAfterUnpublish(){
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
           P2PActions.connect(actorUser, actorUserName, serverIP,true);
           P2PActions.afterConnect();
           notifyLock("User2Connect");
           // Action 3:User1InviteUser2
           act.setCurActionText("Action 3: User1InviteUser2");
           // Action 4:User2AcceptUser1
           waitLock("User1InviteUser2");
           act.setCurActionText("Action 4: User2AcceptUser1");
           P2PActions.afterWaitInvite(actorUserName, pcObserver, 1);
           P2PActions.accept(actorUser, actorUserName, targetUserName,true);
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
           P2PActions.publish(actorUser, actorUserName, targetUserName, lcs,true);
           P2PActions.afterPublish();
           notifyLock("User2PublishToUser1");
           act.setCurActionText("Action 9: User1UnpublishToUser2");
           // Action 12:User2UnpublishToUser1
           waitLock("User1UnpublishToUser2");
           act.setCurActionText("Action 10: User2UnpublishToUser1");
           P2PActions.afterWaitUnpublish(actorUserName, pcObserver, 1);
           P2PActions.unpublish(actorUser, actorUserName, targetUserName, lcs,true);
           P2PActions.afterUnpublish();
           notifyLock("User2UnpublishToUser1");
           // Action 13:User1StopChatWithUser2
           act.setCurActionText("Action 11: User1StopChatWithUser2");
           // Action 14:User2InviteUser1
           SystemClock.sleep(waitingTime);
           P2PActions.afterWaitStop(actorUserName, pcObserver, 1);
           P2PActions.endStory();
           act.makeToast("Test End!");
    }
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
     * 9. User1UnpublishToUser2
     * 10. User2UnpublishToUser1
     * 11. User2StopChatWithUser1
     */
    public void test44_Peer2StopAfterUnpublish(){
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
           P2PActions.connect(actorUser, actorUserName, serverIP,true);
           P2PActions.afterConnect();
           notifyLock("User2Connect");
           // Action 3:User1InviteUser2
           act.setCurActionText("Action 3: User1InviteUser2");
           // Action 4:User2AcceptUser1
           waitLock("User1InviteUser2");
           act.setCurActionText("Action 4: User2AcceptUser1");
           P2PActions.afterWaitInvite(actorUserName, pcObserver, 1);
           P2PActions.accept(actorUser, actorUserName, targetUserName,true);
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
           P2PActions.publish(actorUser, actorUserName, targetUserName, lcs,true);
           P2PActions.afterPublish();
           notifyLock("User2PublishToUser1");
           act.setCurActionText("Action 9: User1UnpublishToUser2");
           // Action 12:User2UnpublishToUser1
           waitLock("User1UnpublishToUser2");
           act.setCurActionText("Action 10: User2UnpublishToUser1");
           P2PActions.afterWaitUnpublish(actorUserName, pcObserver, 1);
           P2PActions.unpublish(actorUser, actorUserName, targetUserName, lcs,true);
           P2PActions.afterUnpublish();
           notifyLock("User2UnpublishToUser1");
           // Action 13:User1StopChatWithUser2
           act.setCurActionText("Action 11: User2StopChatWithUser1");
           P2PActions.stop(actorUser, actorUserName, targetUserName,true);
           P2PActions.afterStop(actorUserName, pcObserver, 0);
           notifyLock("User2StopChatWithUser1");
           // Action 14:User2InviteUser1
           SystemClock.sleep(waitingTime);
           P2PActions.afterWaitStop(actorUserName, pcObserver, 1);
           P2PActions.endStory();
           act.makeToast("Test End!");
    }
    /**
     * Test a normal interaction process between two users.
     * Actors: User1 and User2
     * Story:
     * 1. User1Connect
     * 2. User2Connect
     * 3. User1InviteUser2
     * 4. User2AcceptUser1
     * 5. User1SendTwoMessagesToUser2
     * 6. User2SendTwoMessagesToUser1
     * 7. User1StopChatWithUser2
     */
    public void test45_Peer1StopAfterSend(){
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
           P2PActions.connect(actorUser, actorUserName, serverIP,true);
           P2PActions.afterConnect();
           notifyLock("User2Connect");
           // Action 3:User1InviteUser2
           act.setCurActionText("Action 3: User1InviteUser2");
           // Action 4:User2AcceptUser1
           waitLock("User1InviteUser2");
           act.setCurActionText("Action 4: User2AcceptUser1");
           P2PActions.afterWaitInvite(actorUserName, pcObserver, 1);
           P2PActions.accept(actorUser, actorUserName, targetUserName,true);
           P2PActions.afterAccept(actorUserName, pcObserver, 1);
           notifyLock("User2AcceptUser1");
           // Action 9:User1SendTwoMessagesToUser2
           act.setCurActionText("Action 5: User1SendTwoMessagesToUser2");
           // Action 10:User2SendTwoMessagesToUser1
           waitLock("User1SendTwoMessagesToUser2");
           act.setCurActionText("Action 6: User2SendTwoMessagesToUser1");
           P2PActions.afterWaitSend(actorUserName, pcObserver, 1, 1);
           P2PActions.send(actorUser, actorUserName, targetUserName,
                   actorUserName + " send message1 to " + targetUserName,true);
           P2PActions.afterSend();
           notifyLock("User2SendTwoMessagesToUser1");
           // Action 14:User2InviteUser1
           SystemClock.sleep(waitingTime);
           act.setCurActionText("Action 7:User2InviteUser1");
           P2PActions.afterWaitStop(actorUserName, pcObserver, 1);
           SystemClock.sleep(waitingTime);
           P2PActions.endStory();
           act.makeToast("Test End!");
    }
    /**
     * Test a normal interaction process between two users.
     * Actors: User1 and User2
     * Story:
     * 1. User1Connect
     * 2. User2Connect
     * 3. User1InviteUser2
     * 4. User2AcceptUser1
     * 5. User1SendTwoMessagesToUser2
     * 6. User2SendTwoMessagesToUser1
     * 7. User2StopChatWithUser1
     */
    public void test46_Peer2StopAfterSend(){
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
           P2PActions.connect(actorUser, actorUserName, serverIP,true);
           P2PActions.afterConnect();
           notifyLock("User2Connect");
           // Action 3:User1InviteUser2
           act.setCurActionText("Action 3: User1InviteUser2");
           // Action 4:User2AcceptUser1
           waitLock("User1InviteUser2");
           act.setCurActionText("Action 4: User2AcceptUser1");
           P2PActions.afterWaitInvite(actorUserName, pcObserver, 1);
           P2PActions.accept(actorUser, actorUserName, targetUserName,true);
           P2PActions.afterAccept(actorUserName, pcObserver, 1);
           notifyLock("User2AcceptUser1");
           // Action 9:User1SendTwoMessagesToUser2
           act.setCurActionText("Action 5: User1SendTwoMessagesToUser2");
           // Action 10:User2SendTwoMessagesToUser1
           waitLock("User1SendTwoMessagesToUser2");
           act.setCurActionText("Action 6: User2SendTwoMessagesToUser1");
           P2PActions.afterWaitSend(actorUserName, pcObserver, 1, 1);
           P2PActions.send(actorUser, actorUserName, targetUserName,
                   actorUserName + " send message1 to " + targetUserName,true);
           P2PActions.afterSend();
           notifyLock("User2SendTwoMessagesToUser1");
           // Action 14:User2InviteUser1
           SystemClock.sleep(waitingTime);
           act.setCurActionText("Action 7:User2StopChatWithUser1");
           P2PActions.stop(actorUser, actorUserName, targetUserName,true);
           P2PActions.afterStop(actorUserName, pcObserver, 0);
           notifyLock("User2StopChatWithUser1");
           SystemClock.sleep(waitingTime);
           P2PActions.endStory();
           act.makeToast("Test End!");
    }
    /**
     * Test a normal interaction process between two users.
     * Actors: User1 and User2
     * Story:
     * 1. User1Connect
     * 2. User2Connect
     * 3. User1InviteUser2
     * 4. User2AcceptUser1
     * 5. User1SendTwoMessagesToUser2
     * 6. User2SendTwoMessagesToUser1
     */
    public void test47_SendEachOther(){
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
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        // Action 3:User1InviteUser2
        act.setCurActionText("Action 3: User1InviteUser2");
        // Action 4:User2AcceptUser1
        waitLock("User1InviteUser2");
        act.setCurActionText("Action 4: User2AcceptUser1");
        P2PActions.afterWaitInvite(actorUserName, pcObserver, 1);
        P2PActions.accept(actorUser, actorUserName, targetUserName,true);
        P2PActions.afterAccept(actorUserName, pcObserver, 1);
        notifyLock("User2AcceptUser1");
        // Action 9:User1SendTwoMessagesToUser2
        act.setCurActionText("Action 5: User1SendTwoMessagesToUser2");
        // Action 10:User2SendTwoMessagesToUser1
        waitLock("User1SendTwoMessagesToUser2");
        act.setCurActionText("Action 6: User2SendTwoMessagesToUser1");
        P2PActions.afterWaitSend(actorUserName, pcObserver, 1, 1);
        P2PActions.send(actorUser, actorUserName, targetUserName,
                actorUserName + " send message1 to " + targetUserName,true);
        P2PActions.afterSend();
        notifyLock("User2SendTwoMessagesToUser1");
        SystemClock.sleep(waitingTime);
        P2PActions.endStory();
        act.makeToast("Test End!");
    }
    /**
     * Test a normal interaction process between two users.
     * Actors: User1 and User2
     * Story:
     * 1. User1Connect
     * 2. User2Connect
     * 3. User1InviteUser2
     * 4. User2AcceptUser1
     * 5. User1SendTwoMessagesToUser2
     */
    public void test48_Peer1Send(){
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
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        // Action 3:User1InviteUser2
        act.setCurActionText("Action 3: User1InviteUser2");
        // Action 4:User2AcceptUser1
        waitLock("User1InviteUser2");
        act.setCurActionText("Action 4: User2AcceptUser1");
        P2PActions.afterWaitInvite(actorUserName, pcObserver, 1);
        P2PActions.accept(actorUser, actorUserName, targetUserName,true);
        P2PActions.afterAccept(actorUserName, pcObserver, 1);
        notifyLock("User2AcceptUser1");
        act.setCurActionText("Action 5: User1SendTwoMessagesToUser2");
        waitLock("User1SendTwoMessagesToUser2");
        SystemClock.sleep(waitingTime);
        P2PActions.endStory();
        act.makeToast("Test End!");
    }
    /**
     * Test a normal interaction process between two users.
     * Actors: User1 and User2
     * Story:
     * 1. User1Connect
     * 2. User2Connect
     * 3. User1InviteUser2
     * 4. User2AcceptUser1
     * 5. User2SendTwoMessagesToUser1
     */
    public void test49_Peer2Send(){
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
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        // Action 3:User1InviteUser2
        act.setCurActionText("Action 3: User1InviteUser2");
        // Action 4:User2AcceptUser1
        waitLock("User1InviteUser2");
        act.setCurActionText("Action 4: User2AcceptUser1");
        P2PActions.afterWaitInvite(actorUserName, pcObserver, 1);
        P2PActions.accept(actorUser, actorUserName, targetUserName,true);
        P2PActions.afterAccept(actorUserName, pcObserver, 1);
        notifyLock("User2AcceptUser1");
        act.setCurActionText("Action 6: User2SendTwoMessagesToUser1");
        P2PActions.afterWaitSend(actorUserName, pcObserver, 1, 1);
        P2PActions.send(actorUser, actorUserName, targetUserName,
                actorUserName + " send message1 to " + targetUserName,true);
        P2PActions.afterSend();
        notifyLock("User2SendTwoMessagesToUser1");
        SystemClock.sleep(waitingTime);
        P2PActions.endStory();
        act.makeToast("Test End!");
    }
    /**
     * Test a normal interaction process between two users.
     * Actors: User1 and User2
     * Story:
     * 1. User1Connect
     * 2. User2SendTwoMessagesToUser1
     */
    public void test50_Peer2SendBeforeConnect(){
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
        P2PActions.send(actorUser, actorUserName, targetUserName,
                actorUserName + " send message1 to " + targetUserName,false);
        P2PActions.afterSend();
        notifyLock("User2SendTwoMessagesToUser1");
        SystemClock.sleep(waitingTime);
        P2PActions.endStory();
        act.makeToast("Test End!");
    }
    /**
     * Test a normal interaction process between two users.
     * Actors: User1 and User2
     * Story:
     * 1. User2Connect
     * 2. User1SendTwoMessagesToUser2
     */
    public void test51_Peer1SendBeforeConnect(){
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
        act.setCurActionText("Action 1: User1SendTwoMessagesToUser2");
        waitLock("User1SendTwoMessagesToUser2");
        act.setCurActionText("Action 2: User2SendTwoMessagesToUser1");
        P2PActions.afterWaitSend(actorUserName, pcObserver, 0, 0);
        SystemClock.sleep(waitingTime);
        P2PActions.endStory();
        act.makeToast("Test End!");
    }
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
     * 9. User2SendTwoMessagesToUser1
     */
    public void test52_Peer2SendAfterPublish(){
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
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        // Action 3:User1InviteUser2
        act.setCurActionText("Action 3: User1InviteUser2");
        // Action 4:User2AcceptUser1
        waitLock("User1InviteUser2");
        act.setCurActionText("Action 4: User2AcceptUser1");
        P2PActions.afterWaitInvite(actorUserName, pcObserver, 1);
        P2PActions.accept(actorUser, actorUserName, targetUserName,true);
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
        P2PActions.publish(actorUser, actorUserName, targetUserName, lcs,true);
        P2PActions.afterPublish();
        notifyLock("User2PublishToUser1");
        Log.d(TAG, "OBSERVER:" + pcObserver);
        // Action 9:User1SendTwoMessagesToUser2
        P2PActions.send(actorUser, actorUserName, targetUserName,
                actorUserName + " send message1 to " + targetUserName,true);
        P2PActions.afterSend();
        notifyLock("User2SendTwoMessagesToUser1");
        SystemClock.sleep(waitingTime);
        P2PActions.endStory();
        act.makeToast("Test End!");
    }
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
     */
    public void test53_Peer1SendAfterPublish(){
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
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        // Action 3:User1InviteUser2
        act.setCurActionText("Action 3: User1InviteUser2");
        // Action 4:User2AcceptUser1
        waitLock("User1InviteUser2");
        act.setCurActionText("Action 4: User2AcceptUser1");
        P2PActions.afterWaitInvite(actorUserName, pcObserver, 1);
        P2PActions.accept(actorUser, actorUserName, targetUserName,true);
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
        P2PActions.publish(actorUser, actorUserName, targetUserName, lcs,true);
        P2PActions.afterPublish();
        notifyLock("User2PublishToUser1");
        Log.d(TAG, "OBSERVER:" + pcObserver);
        act.setCurActionText("Action 9: User1SendTwoMessagesToUser2");
        waitLock("User1SendTwoMessagesToUser2");
        act.setCurActionText("Action 10: User2SendTwoMessagesToUser1");
        P2PActions.afterWaitSend(actorUserName, pcObserver, 1, 1);
        SystemClock.sleep(waitingTime);
        P2PActions.endStory();
        act.makeToast("Test End!");
    }
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
     * 9. User1UnpublishToUser2
     * 10. User2UnpublishToUser1
     * 11. User2SendTwoMessagesToUser1
     */
    public void test54_Peer2SendAfterUnPublish(){
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
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        // Action 3:User1InviteUser2
        act.setCurActionText("Action 3: User1InviteUser2");
        // Action 4:User2AcceptUser1
        waitLock("User1InviteUser2");
        act.setCurActionText("Action 4: User2AcceptUser1");
        P2PActions.afterWaitInvite(actorUserName, pcObserver, 1);
        P2PActions.accept(actorUser, actorUserName, targetUserName,true);
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
        P2PActions.publish(actorUser, actorUserName, targetUserName, lcs,true);
        P2PActions.afterPublish();
        notifyLock("User2PublishToUser1");
        Log.d(TAG, "OBSERVER:" + pcObserver);
        // Action 11:User1UnpublishToUser2
        act.setCurActionText("Action 9: User1UnpublishToUser2");
        // Action 12:User2UnpublishToUser1
        waitLock("User1UnpublishToUser2");
        act.setCurActionText("Action 10: User2UnpublishToUser1");
        P2PActions.afterWaitUnpublish(actorUserName, pcObserver, 1);
        P2PActions.unpublish(actorUser, actorUserName, targetUserName, lcs,true);
        P2PActions.afterUnpublish();
        notifyLock("User2UnpublishToUser1");
        P2PActions.send(actorUser, actorUserName, targetUserName,
                actorUserName + " send message1 to " + targetUserName,true);
        P2PActions.afterSend();
        notifyLock("User2SendTwoMessagesToUser1");
        SystemClock.sleep(waitingTime);
        P2PActions.endStory();
        act.makeToast("Test End!");
    }
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
     * 9. User1UnpublishToUser2
     * 10. User2UnpublishToUser1
     * 11. User1SendTwoMessagesToUser2
     */
    public void test55_Peer1SendAfterUnPublish(){
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
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        // Action 3:User1InviteUser2
        act.setCurActionText("Action 3: User1InviteUser2");
        // Action 4:User2AcceptUser1
        waitLock("User1InviteUser2");
        act.setCurActionText("Action 4: User2AcceptUser1");
        P2PActions.afterWaitInvite(actorUserName, pcObserver, 1);
        P2PActions.accept(actorUser, actorUserName, targetUserName,true);
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
        P2PActions.publish(actorUser, actorUserName, targetUserName, lcs,true);
        P2PActions.afterPublish();
        notifyLock("User2PublishToUser1");
        Log.d(TAG, "OBSERVER:" + pcObserver);
        // Action 11:User1UnpublishToUser2
        act.setCurActionText("Action 9: User1UnpublishToUser2");
        // Action 12:User2UnpublishToUser1
        waitLock("User1UnpublishToUser2");
        act.setCurActionText("Action 10: User2UnpublishToUser1");
        P2PActions.afterWaitUnpublish(actorUserName, pcObserver, 1);
        P2PActions.unpublish(actorUser, actorUserName, targetUserName, lcs,true);
        P2PActions.afterUnpublish();
        notifyLock("User2UnpublishToUser1");
        act.setCurActionText("Action 11: User1SendTwoMessagesToUser2");
        waitLock("User1SendTwoMessagesToUser2");
        act.setCurActionText("Action 12: User2SendTwoMessagesToUser1");
        P2PActions.afterWaitSend(actorUserName, pcObserver, 1, 1);
        SystemClock.sleep(waitingTime);
        P2PActions.endStory();
        act.makeToast("Test End!");
    }
    /**
     * Test a normal interaction process between two users.
     * Actors: User1 and User2
     * Story:
     * 1. User1Connect
     * 2. User2Connect
     * 3. User1InviteUser2
     * 4. User2AcceptUser1
     * 5. User1StopChatWithUser2
     * 6. User2SendTwoMessagesToUser1
     */
    public void test56_Peer2SendAfterStop(){
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
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        // Action 3:User1InviteUser2
        act.setCurActionText("Action 3: User1InviteUser2");
        // Action 4:User2AcceptUser1
        waitLock("User1InviteUser2");
        act.setCurActionText("Action 4: User2AcceptUser1");
        P2PActions.afterWaitInvite(actorUserName, pcObserver, 1);
        P2PActions.accept(actorUser, actorUserName, targetUserName,true);
        P2PActions.afterAccept(actorUserName, pcObserver, 1);
        notifyLock("User2AcceptUser1");
        act.setCurActionText("Action 5: User1StopChatWithUser2");
        waitLock("User1StopChatWithUser2");
        act.setCurActionText("Action 6:User2InviteUser1");
        P2PActions.afterWaitStop(actorUserName, pcObserver, 1);
        P2PActions.send(actorUser, actorUserName, targetUserName,
                actorUserName + " send message1 to " + targetUserName,false);
        P2PActions.afterSend();
        notifyLock("User2SendTwoMessagesToUser1");
        SystemClock.sleep(waitingTime);
        P2PActions.endStory();
        act.makeToast("Test End!");
    }
    /**
     * Test a normal interaction process between two users.
     * Actors: User1 and User2
     * Story:
     * 1. User1Connect
     * 2. User2Connect
     * 3. User1InviteUser2
     * 4. User2AcceptUser1
     * 5. User1StopChatWithUser2
     * 6. User1SendTwoMessagesToUser2
     */
    public void test57_Peer1SendAfterStop(){
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
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        // Action 3:User1InviteUser2
        act.setCurActionText("Action 3: User1InviteUser2");
        // Action 4:User2AcceptUser1
        waitLock("User1InviteUser2");
        act.setCurActionText("Action 4: User2AcceptUser1");
        P2PActions.afterWaitInvite(actorUserName, pcObserver, 1);
        P2PActions.accept(actorUser, actorUserName, targetUserName,true);
        P2PActions.afterAccept(actorUserName, pcObserver, 1);
        notifyLock("User2AcceptUser1");
        // Action 13:User1StopChatWithUser2
        act.setCurActionText("Action 5: User1StopChatWithUser2");
        // Action 14:User2InviteUser1
        waitLock("User1StopChatWithUser2");
        act.setCurActionText("Action 6:User2InviteUser1");
        P2PActions.afterWaitStop(actorUserName, pcObserver, 1);
        // Action 9:User1SendTwoMessagesToUser2
        act.setCurActionText("Action 7: User1SendTwoMessagesToUser2");
        // Action 10:User2SendTwoMessagesToUser1
        waitLock("User1SendTwoMessagesToUser2");
        act.setCurActionText("Action 8: User2SendTwoMessagesToUser1");
        P2PActions.afterWaitSend(actorUserName, pcObserver, 0, 0);
        SystemClock.sleep(waitingTime);
        P2PActions.endStory();
        act.makeToast("Test End!");
    }
    /**
     * Test a normal interaction process between two users.
     * Actors: User1 and User2
     * Story:
     * 1. User1Connect
     * 2. User2Connect
     * 3. User1InviteUser2
     * 4. User2SendTwoMessagesToUser1
     */
    public void test58_Peer2SendBeforeAccept(){
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
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        // Action 3:User1InviteUser2
        act.setCurActionText("Action 3: User1InviteUser2");
        // Action 4:User2AcceptUser1
        waitLock("User1InviteUser2");
        act.setCurActionText("Action 4: User2AcceptUser1");
        P2PActions.afterWaitInvite(actorUserName, pcObserver, 1);
        P2PActions.send(actorUser, actorUserName, targetUserName,
                actorUserName + " send message1 to " + targetUserName,false);
        P2PActions.afterSend();
        notifyLock("User2SendTwoMessagesToUser1");
        SystemClock.sleep(waitingTime);
        P2PActions.endStory();
        act.makeToast("Test End!");
    }
    /**
     * Test a normal interaction process between two users.
     * Actors: User1 and User2
     * Story:
     * 1. User1Connect
     * 2. User2Connect
     * 3. User1InviteUser2
     * 4. User1SendTwoMessagesToUser2
     */
    public void test59_Peer1SendBeforeAccept(){
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
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        // Action 3:User1InviteUser2
        act.setCurActionText("Action 3: User1InviteUser2");
        // Action 4:User2AcceptUser1
        waitLock("User1InviteUser2");
        act.setCurActionText("Action 4: User2AcceptUser1");
        P2PActions.afterWaitInvite(actorUserName, pcObserver, 1);
        act.setCurActionText("Action 5: User1SendTwoMessagesToUser2");
        waitLock("User1SendTwoMessagesToUser2");
        act.setCurActionText("Action 6: User2SendTwoMessagesToUser1");
        P2PActions.afterWaitSend(actorUserName, pcObserver, 1, 1);
        SystemClock.sleep(waitingTime);
        P2PActions.endStory();
        act.makeToast("Test End!");
    }
    /**
     * Test a normal interaction process between two users.
     * Actors: User1 and User2
     * Story:
     * 1. User1Connect
     * 2. User2Connect
     * 3. User1InviteUser2
     * 4. User2InviteUser1
     * 5. User2DenyUser1
     * 6. User2SendTwoMessagesToUser1
     */
    public void test60_Peer2SendAfterDeny(){
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
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        // Action 3:User1InviteUser2
        act.setCurActionText("Action 3: User1InviteUser2");
        waitLock("User1InviteUser2");
        act.setCurActionText("Action 4: User2AcceptUser1");
        P2PActions.afterWaitInvite(actorUserName, pcObserver, 1);
        // Action 4:User2DenyUser1
        P2PActions.deny(actorUser, actorUserName, targetUserName,true);
        P2PActions.afterDeny();
        notifyLock("User2DenyUser1");
        P2PActions.send(actorUser, actorUserName, targetUserName,
                actorUserName + " send message1 to " + targetUserName,false);
        P2PActions.afterSend();
        notifyLock("User2SendTwoMessagesToUser1");
        SystemClock.sleep(waitingTime);
        P2PActions.endStory();
        act.makeToast("Test End!");
    }
    /**
     * Test a normal interaction process between two users.
     * Actors: User1 and User2
     * Story:
     * 1. User1Connect
     * 2. User2Connect
     * 3. User1InviteUser2
     * 4. User2InviteUser1
     * 5. User2DenyUser1
     * 6. User1SendTwoMessagesToUser2
     */
    public void test61_Peer1SendAfterDeny(){
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
        P2PActions.connect(actorUser, actorUserName, serverIP,true);
        P2PActions.afterConnect();
        notifyLock("User2Connect");
        // Action 3:User1InviteUser2
        act.setCurActionText("Action 3: User1InviteUser2");
        waitLock("User1InviteUser2");
        act.setCurActionText("Action 4: User2AcceptUser1");
        P2PActions.afterWaitInvite(actorUserName, pcObserver, 1);
        // Action 4:User2DenyUser1
        P2PActions.deny(actorUser, actorUserName, targetUserName,true);
        P2PActions.afterDeny();
        notifyLock("User2DenyUser1");
        //Action 5: User1SendMessageToUser2
        act.setCurActionText("Action 5: Wait User1SendTwoMessagesToUser2");
        waitLock("User1SendTwoMessagesToUser2");
        P2PActions.afterWaitSend(actorUserName, pcObserver, 0, 0);
        SystemClock.sleep(waitingTime);
        P2PActions.endStory();
        act.makeToast("Test End!");
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
