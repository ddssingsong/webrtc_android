package com.dds.voip;

import com.dds.java.SocketManager;
import com.dds.skywebrtc.ISendEvent;

/**
 * Created by dds on 2019/8/25.
 * android_shuai@163.com
 */
public class WebSocketEvent implements ISendEvent {


    @Override
    public void createRoom(String room, int roomSize) {
        SocketManager.getInstance().createRoom(room, roomSize);
    }

    @Override
    public void sendInvite(String room, String userId, boolean audioOnly) {
        SocketManager.getInstance().sendInvite(room, userId, audioOnly);
    }

    @Override
    public void sendMeetingInvite(String userList) {
        SocketManager.getInstance().sendMeetingInvite(userList);

    }

    @Override
    public void sendRefuse(String inviteId, int refuseType) {
        SocketManager.getInstance().sendRefuse(inviteId, refuseType);
    }

    @Override
    public void sendJoin(String room) {
        SocketManager.getInstance().sendJoin(room);
    }


    @Override
    public void sendOffer(String userId, String sdp) {
        SocketManager.getInstance().sendOffer(userId, sdp);
    }

    @Override
    public void sendAnswer(String userId, String sdp) {
        SocketManager.getInstance().sendAnswer(userId, sdp);

    }

    @Override
    public void sendIceCandidate(String userId, String id, int label, String candidate) {
        SocketManager.getInstance().sendIceCandidate(userId, id, label, candidate);
    }
}
