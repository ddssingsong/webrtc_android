package com.dds.rtc_chat;

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
        RemoteSignalError,
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
