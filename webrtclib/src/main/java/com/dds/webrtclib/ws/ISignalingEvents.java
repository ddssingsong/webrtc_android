package com.dds.webrtclib.ws;

import org.webrtc.IceCandidate;

import java.util.ArrayList;

/**
 * Created by dds on 2019/1/3.
 * android_shuai@163.com
 */
public interface ISignalingEvents {

    // 进入房间
    void onJoinToRoom(ArrayList<String> connections, String myId);

    // 有新人进入房间
    void onRemoteJoinToRoom(String socketId);

    void onRemoteIceCandidate(String socketId, IceCandidate iceCandidate);

    void onRemoteOutRoom(String socketId);

    void onReceiveOffer(String socketId, String sdp);

    void onReceiverAnswer(String socketId, String sdp);

}
