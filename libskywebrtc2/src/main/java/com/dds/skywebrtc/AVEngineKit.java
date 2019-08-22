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


    private static AVEngineKit avEngineKit;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Context _context;
    private EglBase _rootEglBase;

    private CallSession currentCallSession;
    private AVEngineCallback engineCallback;


    public static AVEngineKit Instance() {
        AVEngineKit var0;
        if ((var0 = avEngineKit) != null) {
            return var0;
        } else {
            throw new NotInitializedExecption();
        }
    }


    public void init(Context context) {
        this._context = context;
    }


    // 拨打电话
    public CallSession invite(String targetId, boolean isAudio) {
        // 发送邀请
        return null;
    }


    public void joinHome(EglBase eglBase) {
        _rootEglBase = eglBase;
    }

    // 画面预览
    public void startPreview() {

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
