package com.intel.webrtc.test.helper;

import org.webrtc.EglBase;
import org.webrtc.RendererCommon.ScalingType;

import com.intel.webrtc.base.ClientContext;
import com.intel.webrtc.base.Stream;
import com.intel.webrtc.base.Stream.VideoRendererInterface;
import com.intel.webrtc.base.WoogeenException;
import com.intel.webrtc.base.WoogeenSurfaceRenderer;
import com.intel.webrtc.test.demo.R;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
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

    // VideoStreamsView renderer2, renderer3, renderer4;
    WoogeenSurfaceRenderer surfaceRenderer1, surfaceRenderer2, surfaceRenderer3, surfaceRenderer4;
    VideoRendererInterface videoRenderer1, videoRenderer2, videoRenderer3, videoRenderer4;
    WoogeenSampleView view1, view2, view3, view4;
    LinearLayout rendererContainer1, rendererContainer2, rendererContainer3, rendererContainer4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        waitNotifyTextView = (TextView) findViewById(R.id.waitNotifyState);
        testTitleTextView = (TextView) findViewById(R.id.testTitleTextView);
        curActionTextView = (TextView) findViewById(R.id.curActionTextViesw);
        initViews();
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
        this.rootEglBase = new EglBase();
        view1=new WoogeenSampleView(this);
        view2=new WoogeenSampleView(this);
        view3=new WoogeenSampleView(this);
        view4=new WoogeenSampleView(this);
        rendererContainer1 = (LinearLayout) findViewById(R.id.rendererContainer1);
        rendererContainer2 = (LinearLayout) findViewById(R.id.rendererContainer2);
        rendererContainer3 = (LinearLayout) findViewById(R.id.rendererContainer3);
        rendererContainer4 = (LinearLayout) findViewById(R.id.rendererContainer4);

        rendererContainer1.addView(view1);
        rendererContainer2.addView(view2);
        rendererContainer3.addView(view3);
        rendererContainer4.addView(view4);

        surfaceRenderer1 = new WoogeenSurfaceRenderer(view1);
        surfaceRenderer2 = new WoogeenSurfaceRenderer(view2);
        surfaceRenderer3 = new WoogeenSurfaceRenderer(view3);
        surfaceRenderer4 = new WoogeenSurfaceRenderer(view4);

        videoRenderer1 = surfaceRenderer1.createVideoRenderer(0, 0, 100, 100, ScalingType.SCALE_ASPECT_FIT, true);
        videoRenderer2 = surfaceRenderer2.createVideoRenderer(0, 0, 100, 100, ScalingType.SCALE_ASPECT_FIT, false);
        videoRenderer3 = surfaceRenderer3.createVideoRenderer(0, 0, 100, 100, ScalingType.SCALE_ASPECT_FIT, false);
        videoRenderer4 = surfaceRenderer4.createVideoRenderer(0, 0, 100, 100, ScalingType.SCALE_ASPECT_FIT, false);
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
            stream.attach(videoRenderer1);
        } catch (WoogeenException e) {
            e.printStackTrace();
            Log.e(TAG, "Error occured when attach local stream to render.");
        }
    }

    public void attachRender2(Stream stream) {
        try {
            stream.attach(videoRenderer2);
        } catch (WoogeenException e) {
            e.printStackTrace();
            Log.e(TAG, "Error occured when attach local stream to render.");
        }
    }

    public void attachRender3(Stream stream) {
        try {
            stream.attach(videoRenderer3);
        } catch (WoogeenException e) {
            e.printStackTrace();
            Log.e(TAG, "Error occured when attach local stream to render.");
        }
    }

    public void attachRender4(Stream stream) {
        try {
            stream.attach(videoRenderer4);
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
