package com.dds.skywebrtc;

import org.webrtc.PeerConnection;
import org.webrtc.StatsReport;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dds on 2019/8/19.
 * android_shuai@163.com
 */
public class AVEngineKit {

    private static AVEngineKit avEngineKit;

    private List<PeerConnection.IceServer> iceServers = new ArrayList<>();

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


    public class CallSession {

    }

    public interface CallSessionCallback {
        void didCallEndWithReason(AVEngineKit.CallEndReason var1);

        void didChangeState(AVEngineKit.CallState var1);

        void didChangeMode(boolean var1);

        void didCreateLocalVideoTrack();

        void didReceiveRemoteVideoTrack();

        void didError(String var1);

        void didGetStats(StatsReport[] var1);
    }

    public interface AVEngineCallback {
        void onReceiveCall(AVEngineKit.CallSession var1);

        void shouldStartRing(boolean var1);

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
