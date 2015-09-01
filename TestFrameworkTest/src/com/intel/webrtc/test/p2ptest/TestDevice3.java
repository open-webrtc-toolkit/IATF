package com.intel.webrtc.test.p2ptest;

import java.util.List;

import com.intel.webrtc.base.ClientContext;
import com.intel.webrtc.base.LocalCameraStream;
import com.intel.webrtc.base.RemoteStream;
import com.intel.webrtc.p2p.PeerClient;
import com.intel.webrtc.p2p.PeerClientConfiguration;
import com.intel.webrtc.p2p.SocketSignalingChannel;
import com.intel.webrtc.test.android.AndroidTestDevice;
import com.intel.webrtc.test.helper.P2PActions;
import com.intel.webrtc.test.helper.PeerClientObserverForTest;
import com.intel.webrtc.test.helper.TestActivity;

import android.os.SystemClock;
import android.util.Log;

public class TestDevice3 extends AndroidTestDevice{
    String TAG = "P2PTestDevice2";
    TestActivity act = null;
    private long waitingTime =3000;
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
    public void testThreeUserInteraction(){
        initTestActivity();
        // Init variables
        String serverIP = "http://10.239.61.104:8095/";
        String actorUser3Name = "User3";
        String targetUser1Name = "User1";
        String targetUser2Name = "User2";
        PeerClientObserverForTest pcObserver = new PeerClientObserverForTest();
        PeerClientConfiguration peerClientConfiguration = new PeerClientConfiguration();
        PeerClient actorUser3 = new PeerClient(peerClientConfiguration, new SocketSignalingChannel());
        actorUser3.addObserver(pcObserver);
        //Start notice
        P2PActions.startStory("testThreeUserInteraction");
        act.makeToast("Test start, actor:" + actorUser3Name);
        act.setTitle("Actor:" + actorUser3Name);
      //Action 1. User1ConnectAndCreateLocalStream
      //Action 2. User2ConnectAndCreateLocalStream
      //Action 3. User3ConnectAndCreateLocalStream
        waitLock("User2ConnectAndCreateLocalStream");
        P2PActions.connect(actorUser3, actorUser3Name, serverIP);
        LocalCameraStream lcs = P2PActions.createLocalCameraStream(actorUser3Name, true, true);
        act.attachRender1(lcs);
        notifyLock("User3ConnectAndCreateLocalStream");
      //Action 4. User1InviteUser2
      //Action 5. User2AcceptUser1
      //Action 6. User3InviteUser1
        waitLock("User2AcceptUser1");
        P2PActions.invite(actorUser3, actorUser3Name, targetUser1Name);
        notifyLock("User3InviteUser1");
      //Action 7. User1AcceptUser3
      //Action 8. User2InivteUser3
      //Action 9. User3AcceptUser2
        waitLock("User2InivteUser3");
        P2PActions.afterWaitAccept(actorUser3Name, pcObserver, 1, 1);
        P2PActions.afterWaitInvite(actorUser3Name, pcObserver, 1);
        P2PActions.accept(actorUser3, actorUser3Name, targetUser2Name);
        P2PActions.afterAccept(actorUser3Name, pcObserver, 2);
        notifyLock("User3AcceptUser2");
      //Action 10. User1PublishToUser2AndUser3
      //Action 11. User2PublishToUser1AndUser3
      //Action 12. User3PublishToUser1AndUser2
        waitLock("User2PublishToUser1AndUser3");
        P2PActions.afterWaitPublish(actorUser3Name, pcObserver, 2);
        List<RemoteStream> rslist=pcObserver.addedStream;
        act.attachRender2(rslist.get(rslist.size()-2));
        act.attachRender3(rslist.get(rslist.size()-1));
        P2PActions.publish(actorUser3, actorUser3Name, targetUser1Name, lcs);
        P2PActions.publish(actorUser3, actorUser3Name, targetUser2Name, lcs);
        notifyLock("User3PublishToUser1AndUser2");
      //Action 13. User1SendMessageToUser2AndUser3
      //Action 14. User2SendMessageToUser1AndUser3
      //Action 15. User3SendMessageToUser1AndUser2
        waitLock("User2SendMessageToUser1AndUser3");
        P2PActions.afterWaitSend(actorUser3Name, pcObserver, 2, 2);
        P2PActions.send(actorUser3, actorUser3Name, targetUser1Name, "Message:User3ToUser1");
        P2PActions.send(actorUser3, actorUser3Name, targetUser2Name, "Message:User3ToUser2");
        notifyLock("User3SendMessageToUser1AndUser2");
      //Action 16. User1UnpublishToUser2AndUser3
      //Action 17. User2UnpublishToUser1AndUser3
      //Action 18. User3UnpublishToUser1AndUser2
        waitLock("User2UnpublishToUser1AndUser3");
        P2PActions.afterWaitUnpublish(actorUser3Name, pcObserver, 2);
        P2PActions.unpublish(actorUser3, actorUser3Name, targetUser1Name, lcs);
        P2PActions.unpublish(actorUser3, actorUser3Name, targetUser2Name, lcs);
        notifyLock("User3UnpublishToUser1AndUser2");
      //Action 19. User1StopChatWithUser2
      //Action 20. User2StopChatWithUser3
      //Action 21. User3StopChatWithUser1
        waitLock("User2StopChatWithUser3");
        P2PActions.afterWaitStop(actorUser3Name, pcObserver, 1);
        P2PActions.stop(actorUser3, actorUser3Name, targetUser1Name);
        P2PActions.afterStop(actorUser3Name, pcObserver, 2);
        notifyLock("User3StopChatWithUser1");
      //Action 22. User1Disconnect
      //Action 23. User2Disconnect
      //Action 24. User3Disconnect
        waitLock("User2Disconnect");
        P2PActions.disconnect(actorUser3, actorUser3Name);
      //sleep to make sure the notify message is sent out
        SystemClock.sleep(waitingTime);
        P2PActions.endStory();
    }

    private void initTestActivity() {
        while(true){
            try{
                getActivity();
                break;
            }catch(Exception e){
                Log.d(TAG, "activity get error, retry!");
            }
        }
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
