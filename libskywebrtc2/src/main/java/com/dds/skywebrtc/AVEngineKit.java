package com.dds.skywebrtc;

import android.content.Context;

import org.webrtc.EglBase;
import org.webrtc.PeerConnection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by dds on 2019/8/19.
 * android_shuai@163.com
 */
public class AVEngineKit {


    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    public Context _context;
    public EglBase _rootEglBase;


    private static AVEngineKit avEngineKit;
    private CallSession currentCallSession;
    private EnumType.CallState callState;


    private AVEngineCallback _engineCallback;
    private ISocketEvent _iSocketEvent;


    public static AVEngineKit Instance() {
        AVEngineKit var0;
        if ((var0 = avEngineKit) != null) {
            return var0;
        } else {
            throw new NotInitializedExecption();
        }
    }


    public static void init(Context context, ISocketEvent iSocketEvent) {
        avEngineKit = new AVEngineKit();
        avEngineKit._context = context;
        avEngineKit._iSocketEvent = iSocketEvent;
    }


    // 创建会话
    public void createSession() {
        if (avEngineKit != null) {
            currentCallSession = new CallSession(avEngineKit);
        }
    }


    // 创建房间并进入房间
    public void createRoom(String room, int roomSize) {
        if (_iSocketEvent != null) {
            // 创建房间
            _iSocketEvent.createRoom(room, roomSize);
            if (currentCallSession != null) {
                // state --> Outgoing
                currentCallSession.setCallState(EnumType.CallState.Outgoing);
            }
        }

    }

    // 邀请好友
    public void invite(String targetId, boolean isAudio) {
        // 发送邀请
        if (_iSocketEvent != null) {
            // state-->Outgoing
            callState = EnumType.CallState.Outgoing;
            // 发送邀请
            _iSocketEvent.sendInvite(targetId, isAudio);
        }
    }


    public CallSession getCurrentSession() {
        return this.currentCallSession;
    }


    // -----------iceServers---------------------
    private List<PeerConnection.IceServer> iceServers = new ArrayList<>();

    public void addIceServer(String host, String username, String pwd) {
        AVEngineKit var = this;
        PeerConnection.IceServer var4 = PeerConnection.IceServer.builder(host)
                .setUsername(username)
                .setPassword(pwd)
                .createIceServer();
        var.iceServers.add(var4);
    }

    public List<PeerConnection.IceServer> getIceServers() {
        return iceServers;
    }


}
