package com.intel.webrtc.test.helper;

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
        StreamContainer1 = (LinearLayout) findViewById(R.id.streamViewLayout1);
        StreamContainer2 = (LinearLayout) findViewById(R.id.streamViewLayout2);
        StreamContainer3 = (LinearLayout) findViewById(R.id.streamViewLayout3);
        StreamContainer4 = (LinearLayout) findViewById(R.id.streamViewLayout4);

        // Add listeners to initiate the video renderer
        ViewTreeObserver vto = StreamContainer1.getViewTreeObserver();
        vto.addOnPreDrawListener(new OnPreDrawListener() {
            public boolean onPreDraw() {
                Log.d(TAG, "onPreDraw1");
                if (!hasDrawn1) {
                    Point point = new Point(StreamContainer1.getWidth(), StreamContainer1.getHeight());
                    renderer1 = new VideoStreamsView(TestActivity.this, point);
                    StreamContainer1.addView(renderer1);
                    hasDrawn1 = true;
                }
                return true;
            }
        });
        vto = StreamContainer2.getViewTreeObserver();
        vto.addOnPreDrawListener(new OnPreDrawListener() {
            public boolean onPreDraw() {

                Log.d(TAG, "onPreDraw2");
                if (!hasDrawn2) {
                    Point point = new Point(StreamContainer2.getWidth(), StreamContainer2.getHeight());
                    renderer2 = new VideoStreamsView(TestActivity.this, point);
                    StreamContainer2.addView(renderer2);
                    hasDrawn2 = true;
                }
                return true;
            }
        });
        vto = StreamContainer3.getViewTreeObserver();
        vto.addOnPreDrawListener(new OnPreDrawListener() {
            public boolean onPreDraw() {
                Log.d(TAG, "onPreDraw3");
                if (!hasDrawn3) {
                    Point point = new Point(StreamContainer3.getWidth(), StreamContainer3.getHeight());
                    renderer3 = new VideoStreamsView(TestActivity.this, point);
                    StreamContainer3.addView(renderer3);
                    hasDrawn3 = true;
                }
                return true;
            }
        });
        vto = StreamContainer4.getViewTreeObserver();
        vto.addOnPreDrawListener(new OnPreDrawListener() {
            public boolean onPreDraw() {
                Log.d(TAG, "onPreDraw4");
                if (!hasDrawn4) {
                    Point point = new Point(StreamContainer4.getWidth(), StreamContainer4.getHeight());
                    renderer4 = new VideoStreamsView(TestActivity.this, point);
                    StreamContainer4.addView(renderer4);
                    hasDrawn4 = true;
                }
                return true;
            }
        });
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
