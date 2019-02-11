package com.dds.webrtclib.callback;

import org.webrtc.VideoRenderer;

/**
 * Video界面渲染类
 * Created by dds on 2018/11/7.
 */
public class ProxyRenderer implements VideoRenderer.Callbacks {
    private VideoRenderer.Callbacks target;

    synchronized public void renderFrame(VideoRenderer.I420Frame frame) {
        if (target == null) {
            VideoRenderer.renderFrameDone(frame);
            return;
        }
        target.renderFrame(frame);
    }

    synchronized public void setTarget(VideoRenderer.Callbacks target) {
        this.target = target;
    }
}
