package com.dds.skywebrtc.render;

import org.webrtc.Logging;
import org.webrtc.VideoFrame;
import org.webrtc.VideoSink;

/**
 * Created by dds on 2019/4/4.
 * android_shuai@163.com
 */
public class ProxyVideoSink implements VideoSink {
    private static final String TAG = "dds_ProxyVideoSink";
    private VideoSink target;

    @Override
    synchronized public void onFrame(VideoFrame frame) {
        if (target == null) {
            Logging.d(TAG, "Dropping frame in proxy because target is null.");
            return;
        }
        target.onFrame(frame);
    }

    synchronized public void setTarget(VideoSink target) {
        this.target = target;
    }

}