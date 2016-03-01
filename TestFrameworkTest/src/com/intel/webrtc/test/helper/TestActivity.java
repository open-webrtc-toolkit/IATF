package com.intel.webrtc.test.helper;

import com.intel.webrtc.base.ClientContext;
import com.intel.webrtc.base.EglBase;
import com.intel.webrtc.base.Stream;
import com.intel.webrtc.base.VideoStreamsView;
import com.intel.webrtc.base.WoogeenException;
import com.intel.webrtc.test.demo.R;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class TestActivity extends Activity {

    private final static String TAG = "WooGeenP2PTest-TestActivity";
    private EglBase rootEglBase;
    Integer lock = null;

    private TextView waitNotifyTextView, testTitleTextView, curActionTextView;

    boolean hasDrawn1 = false, hasDrawn2 = false, hasDrawn3 = false, hasDrawn4 = false;
    LinearLayout StreamContainer1, StreamContainer2, StreamContainer3, StreamContainer4;

    VideoStreamsView renderer1, renderer2, renderer3, renderer4;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        waitNotifyTextView = (TextView) findViewById(R.id.waitNotifyState);
        testTitleTextView = (TextView) findViewById(R.id.testTitleTextView);
        curActionTextView = (TextView) findViewById(R.id.curActionTextViesw);
        initViews();
        // handler=new UIHandler();
    }

    void setLock(Integer lock) {
        this.lock = lock;
    }

    public void mNotify() {
        Log.d(TAG, "act-mNotify");
        synchronized (lock) {
            try {
                lock.notify();
            } catch (Exception e) {

            }
        }
    }

    void initViews() {
//        StreamContainer1 = (LinearLayout) findViewById(R.id.streamViewLayout1);
//        StreamContainer2 = (LinearLayout) findViewById(R.id.streamViewLayout2);
//        StreamContainer3 = (LinearLayout) findViewById(R.id.streamViewLayout3);
//        StreamContainer4 = (LinearLayout) findViewById(R.id.streamViewLayout4);
        this.rootEglBase = new EglBase();
        renderer1 = (VideoStreamsView) findViewById(R.id.streamViewLayout1);
        renderer2 = (VideoStreamsView) findViewById(R.id.streamViewLayout2);
        renderer3 = (VideoStreamsView) findViewById(R.id.streamViewLayout3);
        renderer4 = (VideoStreamsView) findViewById(R.id.streamViewLayout4);
        renderer1.init(rootEglBase.getContext(), null);
        renderer2.init(rootEglBase.getContext(), null);
        renderer3.init(rootEglBase.getContext(), null);
        renderer4.init(rootEglBase.getContext(), null);
        renderer1.setZOrderMediaOverlay(true);
        renderer2.setZOrderMediaOverlay(true);
        renderer3.setZOrderMediaOverlay(true);
        renderer4.setZOrderMediaOverlay(true);
        // Add listeners to initiate the video renderer
        ClientContext.setApplicationContext(this, rootEglBase.getContext());
    }

    public void setTitle(final String titleStr) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                testTitleTextView.setText(titleStr);
            }
        });
    }

    public void setWaitNotifyStateText(final String wfstate) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                waitNotifyTextView.setText(wfstate);
            }
        });
    }

    public void setCurActionText(final String curact) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                curActionTextView.setText(curact);
            }
        });
    }

    public void attachRender1(Stream stream) {
        try {
            stream.attach(renderer1);
        } catch (WoogeenException e) {
            e.printStackTrace();
            Log.e(TAG, "Error occured when attach local stream to render.");
        }
    }

    public void attachRender2(Stream stream) {
        try {
            stream.attach(renderer2);
        } catch (WoogeenException e) {
            e.printStackTrace();
            Log.e(TAG, "Error occured when attach local stream to render.");
        }
    }

    public void attachRender3(Stream stream) {
        try {
            stream.attach(renderer3);
        } catch (WoogeenException e) {
            e.printStackTrace();
            Log.e(TAG, "Error occured when attach local stream to render.");
        }
    }

    public void attachRender4(Stream stream) {
        try {
            stream.attach(renderer4);
        } catch (WoogeenException e) {
            e.printStackTrace();
            Log.e(TAG, "Error occured when attach local stream to render.");
        }
    }

    public void makeToast(final String toastStr) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), toastStr, Toast.LENGTH_LONG).show();
            }
        });
    }
}
