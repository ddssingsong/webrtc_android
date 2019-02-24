package com.dds.webrtclib.callback;

import org.webrtc.MediaStream;

/**
 * 界面回调类
 * Created by dds on 2017/10/23.
 */

public interface IViewCallback {

    void onSetLocalStream(MediaStream stream, String socketId);

    void onAddRemoteStream(MediaStream stream, String socketId);

    void onReceiveAck();

    void onCloseWithId(String socketId);

    void onDecline();

    void onError(String msg);
}
