package com.dds.webrtclib.ws;

import com.dds.webrtclib.MyIceServer;

import org.webrtc.IceCandidate;

import java.util.ArrayList;

/**
 * Created by dds on 2019/1/3.
 * android_shuai@163.com
 */
public interface ISignalingEvents {

    void onWebSocketOpen();

    void onLoginSuccess(ArrayList<MyIceServer> iceServers, String socketId);

    void onCreateRoomSuccess(String room);

    // 进入房间
    void onJoinToRoom(ArrayList<String> connections, String myId);

    void onUserAck(String userId);

    void onUserInvite(String socketId);

    // 有新人进入房间
    void onRemoteJoinToRoom(String socketId);

    void onReceiveOffer(String socketId, String sdp);

    void onReceiverAnswer(String socketId, String sdp);

    void onRemoteIceCandidate(String socketId, IceCandidate iceCandidate);

    void onRemoteOutRoom(String socketId);

    void onDecline(EnumMsg.Decline decline);

    void onError(String msg);

}
