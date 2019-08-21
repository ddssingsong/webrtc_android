package com.dds.skywebrtc;

/**
 * Created by dds on 2019/8/20.
 * android_shuai@163.com
 */
public interface AVEngineCallback {
    void onReceiveCall(CallSession session);

    void shouldStartRing(boolean isComing);

    void shouldSopRing();
}
