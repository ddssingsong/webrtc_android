package com.dds.java;

/**
 * Created by dds on 2019/7/26.
 * android_shuai@163.com
 */
public interface IEvent {

    void onOpen();

    void loginSuccess(String json);


    void onInvite(String room, int roomSize, int mediaType, String inviteId, String userList);


    void onCancel(String inviteId);

    void onRing(String userId);

    void onNewPeer(String myId, String userList);


    void onReject(String userId, int type);


    // onOffer

    void onOffer(String userId, String sdp);

    // onAnswer
    void onAnswer(String userId, String sdp);

    // ice-candidate
    void onIceCandidate(String userId, String id, String label, String candidate);


    void logout(String str);

}
