package com.dds.webrtclib.ws;

import org.webrtc.IceCandidate;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dds on 2019/1/3.
 * android_shuai@163.com
 */
public interface ISignalingEvents {

    // webSocket连接成功
    void onWebSocketOpen();

    // webSocket连接失败
    void onWebSocketOpenFailed(String msg);

    // 进入房间
    void onJoinToRoom(ArrayList<String> connections, String myId);

    // 有新人进入房间
    void onRemoteJoinToRoom(String socketId);

    void onRemoteIceCandidate(String socketId, IceCandidate iceCandidate);

    void onRemoteIceCandidateRemove(String socketId, List<IceCandidate> iceCandidates);


    void onRemoteOutRoom(String socketId);

    void onReceiveOffer(String socketId, String sdp);

    void onReceiverAnswer(String socketId, String sdp);

}
