package com.dds.skywebrtc.engine;

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

    void onSendIceCandidate(String userId, IceCandidate candidate);

    void onSendOffer(String userId, SessionDescription description);

    void onSendAnswer(String userId, SessionDescription description);

    void onRemoteStream(String userId);

}
