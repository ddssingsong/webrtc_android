package com.dds.webrtclib;

/**
 * Created by dds on 2019/7/24.
 * android_shuai@163.com
 */
public enum CallEndReason {
    Busy,
    SignalError,
    Hangup,
    MediaError,
    RemoteHangup,
    OpenCameraFailure,
    Timeout,
    AcceptByOtherClient;

    private CallEndReason() {
    }
}
