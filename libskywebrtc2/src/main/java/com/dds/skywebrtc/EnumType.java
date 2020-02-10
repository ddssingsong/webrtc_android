package com.dds.skywebrtc;

/**
 * Created by dds on 2019/8/22.
 * android_shuai@163.com
 */
public class EnumType {

    public enum CallState {
        Idle,
        Outgoing,
        Incoming,
        Connecting,
        Connected;

        CallState() {
        }
    }

    public enum CallEndReason {
        Busy,
        SignalError,
        Hangup,
        MediaError,
        RemoteHangup,
        OpenCameraFailure,
        Timeout,
        AcceptByOtherClient;

        CallEndReason() {
        }
    }

    public enum RefuseType {
        Busy,
        Hangup,
    }


}
