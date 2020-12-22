package com.dds.webrtclib;

/**
 * Created by dds on 2019/7/24.
 * android_shuai@163.com
 */
public enum CallState {
    Idle,
    Outgoing,
    Incoming,
    Connecting,
    Connected;

    private CallState() {
    }
}