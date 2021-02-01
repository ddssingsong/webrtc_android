package com.dds.skywebrtc.engine;

import com.dds.skywebrtc.EnumType;

import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

/**
 * Created by dds on 2020/4/12.
 * 框架回调
 */
public interface EngineCallback {


    /**
     * 加入房间成功
     */
    void joinRoomSucc();

    /**
     * 退出房间成功
     */
    void exitRoom();

    /**
     * 拒绝连接
     * @param type type
     */
    void reject(int type);

    void disconnected(EnumType.CallEndReason reason);

    void onSendIceCandidate(String userId, IceCandidate candidate);

    void onSendOffer(String userId, SessionDescription description);

    void onSendAnswer(String userId, SessionDescription description);

    void onRemoteStream(String userId);

    void onDisconnected(String userId);

}
