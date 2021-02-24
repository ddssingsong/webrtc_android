package com.dds.core.voip;

import android.media.AudioManager;
import android.net.Uri;
import android.util.Log;

import com.dds.App;
import com.dds.core.socket.SocketManager;
import com.dds.skywebrtc.inter.ISkyEvent;
import com.dds.webrtc.R;

import java.util.List;

/**
 * Created by dds on 2019/8/25.
 * android_shuai@163.com
 */
public class VoipEvent implements ISkyEvent {
    private static final String TAG = "VoipEvent";
    private AsyncPlayer ringPlayer;

    public VoipEvent() {
        ringPlayer = new AsyncPlayer(null);
    }

    @Override
    public void createRoom(String room, int roomSize) {
        SocketManager.getInstance().createRoom(room, roomSize);
    }

    @Override
    public void sendInvite(String room, List<String> userIds, boolean audioOnly) {
        SocketManager.getInstance().sendInvite(room, userIds, audioOnly);
    }

    @Override
    public void sendRefuse(String room, String inviteId, int refuseType) {
        SocketManager.getInstance().sendRefuse(room, inviteId, refuseType);
    }

    @Override
    public void sendTransAudio(String toId) {
        SocketManager.getInstance().sendTransAudio(toId);
    }

    @Override
    public void sendDisConnect(String room, String toId, boolean isCrashed) {
        SocketManager.getInstance().sendDisconnect(room, toId);
    }

    @Override
    public void sendCancel(String mRoomId, List<String> toIds) {
        SocketManager.getInstance().sendCancel(mRoomId, toIds);
    }


    @Override
    public void sendJoin(String room) {
        SocketManager.getInstance().sendJoin(room);
    }

    @Override
    public void sendRingBack(String targetId, String room) {
        SocketManager.getInstance().sendRingBack(targetId, room);
    }

    @Override
    public void sendLeave(String room, String userId) {
        SocketManager.getInstance().sendLeave(room, userId);
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

    @Override
    public void onRemoteRing() {

    }


    //==============================================================================
    @Override
    public void shouldStartRing(boolean isComing) {
        if (isComing) {
            Uri uri = Uri.parse("android.resource://" + App.getInstance().getPackageName() + "/" + R.raw.incoming_call_ring);
            ringPlayer.play(App.getInstance(), uri, true, AudioManager.STREAM_RING);
        } else {
            Uri uri = Uri.parse("android.resource://" + App.getInstance().getPackageName() + "/" + R.raw.wr_ringback);
            ringPlayer.play(App.getInstance(), uri, true, AudioManager.STREAM_RING);
        }
    }

    @Override
    public void shouldStopRing() {
        Log.d(TAG, "shouldStopRing begin");
        ringPlayer.stop();
    }
}
