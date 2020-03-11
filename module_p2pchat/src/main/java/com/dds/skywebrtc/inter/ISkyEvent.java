package com.dds.skywebrtc.inter;

/**
 * Created by dds on 2019/8/21.
 * android_shuai@163.com
 */
public interface ISkyEvent {

    // 创建房间
    void createRoom(String room, int roomSize);

    // 发送单人邀请
    void sendInvite(String room, String userId, boolean audioOnly);

    // 发起会议邀请
    void sendMeetingInvite(String userList);

    void sendRefuse(String inviteId, int refuseType);

    void sendTransAudio(String toId);

    void sendDisConnect(String toId);

    void sendCancel(String toId);

    void sendJoin(String room);

    void sendRingBack(String targetId);

    void sendLeave(String room, String userId);

    // sendOffer
    void sendOffer(String userId, String sdp);

    // sendAnswer
    void sendAnswer(String userId, String sdp);

    // sendIceCandidate
    void sendIceCandidate(String userId, String id, int label, String candidate);


    void shouldStartRing(boolean isComing);

    void shouldStopRing();


}
