package com.dds.skywebrtc;

import android.content.Context;
import android.util.Log;

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
    private final static String TAG = "dds_AVEngineKit";
    final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static AVEngineKit avEngineKit;
    private CallSession currentCallSession;
    public IBusinessEvent _iSocketEvent;
    private List<PeerConnection.IceServer> iceServers = new ArrayList<>();

    public static AVEngineKit Instance() {
        AVEngineKit var0;
        if ((var0 = avEngineKit) != null) {
            return var0;
        } else {
            throw new NotInitializedExecption();
        }
    }


    public static void init(IBusinessEvent iSocketEvent) {
        if (avEngineKit == null) {
            avEngineKit = new AVEngineKit();
            avEngineKit._iSocketEvent = iSocketEvent;


            PeerConnection.IceServer var1 = PeerConnection.IceServer.builder("stun:stun.l.google.com:19302")
                    .createIceServer();
            PeerConnection.IceServer var4 = PeerConnection.IceServer.builder("stun:global.stun.twilio.com:3478?transport=udp")
                    .createIceServer();
            PeerConnection.IceServer var2 = PeerConnection.IceServer.builder("turn:global.turn.twilio.com:3478?transport=udp")
                    .setUsername("79fdd6b3c57147c5cc44944344c69d85624b63ec30624b8674ddc67b145e3f3c")
                    .setPassword("xjfTOLkVmDtvFDrDKvpacXU7YofAwPg6P6TXKiztVGw")
                    .createIceServer();
            PeerConnection.IceServer var3 = PeerConnection.IceServer.builder("turn:global.turn.twilio.com:3478?transport=tcp")
                    .setUsername("79fdd6b3c57147c5cc44944344c69d85624b63ec30624b8674ddc67b145e3f3c")
                    .setPassword("xjfTOLkVmDtvFDrDKvpacXU7YofAwPg6P6TXKiztVGw")
                    .createIceServer();
            avEngineKit.iceServers.add(var1);
            avEngineKit.iceServers.add(var4);
            avEngineKit.iceServers.add(var2);
            avEngineKit.iceServers.add(var3);
        }
    }


    // 发起会话
    public boolean startCall(Context context,
                             final String room, final int roomSize,
                             final String targetId,
                             final boolean audioOnly,
                             boolean isComing) {
        if (avEngineKit == null) {
            Log.e(TAG, "receiveCall error,init is not set");
            return false;
        }
        if (currentCallSession != null &&
                currentCallSession.getState() != EnumType.CallState.Idle) {
            if (isComing) {
                // 来电忙线中
                if (_iSocketEvent != null) {
                    // 发送->忙线中...
                    Log.e(TAG, "startCall error,currentCallSession is exist," +
                            "start sendRefuse!");
                    _iSocketEvent.sendRefuse(targetId, EnumType.RefuseType.Busy.ordinal());
                }
            } else {
                Log.e(TAG, "startCall error,currentCallSession is exist");
            }
            return false;
        }
        // new Session
        currentCallSession = new CallSession(avEngineKit);
        currentCallSession.setIsAudioOnly(audioOnly);
        currentCallSession.setRoom(room);
        currentCallSession.setTargetId(targetId);
        currentCallSession.setContext(context);
        currentCallSession.setIsComing(isComing);
        currentCallSession.setCallState(isComing ? EnumType.CallState.Incoming : EnumType.CallState.Outgoing);
        if (isComing) {
            // 开始响铃
            if (_iSocketEvent != null) {
                _iSocketEvent.shouldStartRing(true);
            }
            // 发送响铃回复
            executor.execute(() -> {
                if (_iSocketEvent != null) {
                    _iSocketEvent.sendRingBack(targetId);
                }

            });
        } else {
            executor.execute(() -> {
                if (_iSocketEvent != null) {
                    // 创建房间
                    _iSocketEvent.createRoom(room, roomSize);
                }
            });

        }
        return true;

    }


    // 挂断会话
    public void endCall() {
        // 停止响铃
        if (avEngineKit._iSocketEvent != null) {
            avEngineKit._iSocketEvent.shouldStopRing();
        }
        // 有人进入房间
        if (currentCallSession.isComing) {
            if (currentCallSession.getState() == EnumType.CallState.Incoming) {
                // 接收到邀请，还没同意，发送拒绝
                if (_iSocketEvent != null) {
                    _iSocketEvent.sendRefuse(currentCallSession._targetIds, EnumType.RefuseType.Hangup.ordinal());
                }
            } else {
                // 已经接通，挂断电话
                currentCallSession.leave();
            }
        } else {
            if (currentCallSession.getState() == EnumType.CallState.Outgoing) {
                if (_iSocketEvent != null) {
                    // 取消拨出
                    _iSocketEvent.sendCancel(currentCallSession._targetIds);
                }
            } else {
                // 已经接通，挂断电话
                currentCallSession.leave();
            }
        }
        currentCallSession.setCallState(EnumType.CallState.Idle);
    }

    public CallSession getCurrentSession() {
        return this.currentCallSession;
    }


    // -----------iceServers---------------------

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
