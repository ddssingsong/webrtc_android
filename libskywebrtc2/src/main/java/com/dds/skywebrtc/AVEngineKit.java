package com.dds.skywebrtc;

import org.webrtc.PeerConnection;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dds on 2019/8/19.
 * android_shuai@163.com
 */
public class AVEngineKit {

    private static AVEngineKit avEngineKit;
    private List<PeerConnection.IceServer> iceServers = new ArrayList<>();

    private CallSession currentCallSession;


    public static AVEngineKit Instance() {
        AVEngineKit var0;
        if ((var0 = avEngineKit) != null) {
            return var0;
        } else {
            throw new NotInitializedExecption();
        }
    }

    public void addIceServer(String host, String username, String pwd) {
        AVEngineKit var = this;
        PeerConnection.IceServer var4 = PeerConnection.IceServer.builder(host)
                .setUsername(username)
                .setPassword(pwd)
                .createIceServer();
        var.iceServers.add(var4);
    }


    // 拨打电话
    public CallSession startCall(String targetId, boolean isAudio, CallSession.CallSessionCallback callback) {
        // 发送invite 同时设置定时器
        // 呼出中 -->对方已响铃


        return null;
    }

    // 画面预览
    public void startPreview() {

    }

    public CallSession getCurrentSession() {
        return this.currentCallSession;
    }


    public interface AVEngineCallback {
        void onReceiveCall(CallSession session);

        void shouldStartRing(boolean isComing);

        void shouldSopRing();
    }

    public static enum CallEndReason {
        Busy,
        SignalError,
        Hangup,
        MediaError,
        RemoteHangup,
        OpenCameraFailure,
        Timeout,
        AcceptByOtherClient;

        private CallEndReason() {
        }
    }

    public static enum CallState {
        Idle,
        Outgoing,
        Incoming,
        Connecting,
        Connected;

        private CallState() {
        }
    }
}
