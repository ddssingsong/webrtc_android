package com.dds.java.socket;

/**
 * Created by dds on 2019/7/26.
 * android_shuai@163.com
 */
public interface IEvent {

    void onOpen();

    void loginSuccess(String userId, String avatar);


    void onInvite(String room, boolean audioOnly, String inviteId, String userList);


    void onCancel(String inviteId);

    void onRing(String userId);


    void onPeers(String myId, String userList);

    void onNewPeer(String myId);

    void onReject(String userId, int type);

    // onOffer
    void onOffer(String userId, String sdp);

    // onAnswer
    void onAnswer(String userId, String sdp);

    // ice-candidate
    void onIceCandidate(String userId, String id, int label, String candidate);

    void onLeave(String userId);

    void logout(String str);

    void onTransAudio(String userId);

    void onDisConnect(String userId);

    void reConnect();

}
