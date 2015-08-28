package com.intel.webrtc.test.p2ptest;

import com.intel.webrtc.base.ClientContext;
import com.intel.webrtc.base.LocalCameraStream;
import com.intel.webrtc.p2p.PeerClient;
import com.intel.webrtc.p2p.PeerClientConfiguration;
import com.intel.webrtc.p2p.SocketSignalingChannel;
import com.intel.webrtc.test.android.AndroidTestDevice;
import com.intel.webrtc.test.helper.P2PActions;
import com.intel.webrtc.test.helper.PeerClientObserverForTest;
import com.intel.webrtc.test.helper.TestActivity;

import android.util.Log;

public class TestDevice2 extends AndroidTestDevice {
    String TAG = "P2PTestDevice2";
    TestActivity act = null;

    /**
     * Test a normal interaction process.
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
    public void testInteraction() {
        initTestActivity();
        // Init variables
        String serverIP = "http://10.239.61.104:8095/";
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
        P2PActions.afterWaitInvite(actorUserName, pcObserver,1);
        P2PActions.accept(actorUser, actorUserName, targetUserName);
        P2PActions.afterAccept(actorUserName, pcObserver,1);
        notifyLock("User2AcceptUser1");
        // Action 5:User1CreateLocalStream
        act.setCurActionText("Action 5: User1CreateLocalStream");
        // Action 6:User2CreateLocalStream
        waitLock("User1CreateLocalStream");
        act.setCurActionText("Action 6: User2CreateLocalStream");
        P2PActions.afterWaitCreateLocalCameraStream();
        LocalCameraStream lcs = P2PActions.createLocalCameraStream(actorUserName, true, true);
        Log.d(TAG, "lcs:" + lcs);
        P2PActions.afterCreateLocalStream(actorUserName, lcs);
        ((TestActivity) testActivity).attachRender1(lcs);
        notifyLock("User2CreateLocalStream");
        // Action 7:User1PublishToUser2
        act.setCurActionText("Action 7: User1PublishToUser2");
        // Action 8:User2PublishToUser1
        waitLock("User1PublishToUser2");
        act.setCurActionText("Action 8: User2PublishToUser1");
        P2PActions.afterWaitPublish(actorUserName, pcObserver,1);
        ((TestActivity) testActivity).attachRender2(pcObserver.addedStream);
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
        P2PActions.afterWaitUnpublish(actorUserName, pcObserver,1);
        P2PActions.unpublish(actorUser, actorUserName, targetUserName, lcs);
        P2PActions.afterUnpublish();
        notifyLock("User2UnpublishToUser1");
        // Action 13:User1StopChatWithUser2
        act.setCurActionText("Action 13: User1StopChatWithUser2");
        // Action 14:User2InviteUser1
        waitLock("User1StopChatWithUser2");
        act.setCurActionText("Action 14:User2InviteUser1");
        P2PActions.afterWaitStop(actorUserName, pcObserver,1);
        P2PActions.invite(actorUser, actorUserName, targetUserName);
        P2PActions.afterInvite();
        notifyLock("User2InviteUser1");
        // Action 15:User1DenyUser2
        act.setCurActionText("Action 15:User1DenyUser2");
        // Action 16:User2Disconnect
        waitLock("User1DenyUser2");
        act.setCurActionText("Action 16:User2Disconnect");
        P2PActions.afterWaitDeny(actorUserName, pcObserver,1);
        P2PActions.disconnect(actorUser, actorUserName);
        P2PActions.afterDisconnect(actorUserName, pcObserver,1);
        notifyLock("User2Disconnect");
        // Action 17:User1Disconnect
        act.setCurActionText("Action 17:User1Disconnect");
        act.makeToast("Test End!");
    }

    private void initTestActivity() {
        getActivity();
        Log.d(TAG, testActivity.toString());
        ClientContext.setApplicationContext(testActivity);
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
