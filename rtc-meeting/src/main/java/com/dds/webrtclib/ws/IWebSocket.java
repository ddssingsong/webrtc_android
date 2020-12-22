package com.dds.webrtclib.ws;

import org.webrtc.IceCandidate;

/**
 * Created by dds on 2019/1/3.
 * android_shuai@163.com
 */
public interface IWebSocket {


    void connect(String wss);

    boolean isOpen();

    void close();

    // 加入房间
    void joinRoom(String room);

    //处理回调消息
    void handleMessage(String message);

    void sendIceCandidate(String socketId, IceCandidate iceCandidate);

    void sendAnswer(String socketId, String sdp);

    void sendOffer(String socketId, String sdp);
}
