package com.dds.skywebrtc;

import android.content.Context;
import android.util.Log;

import org.webrtc.PeerConnection;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dds on 2019/8/19.
 * android_shuai@163.com
 */
public class AVEngineKit {
    private final static String TAG = "dds_AVEngineKit";
    private static AVEngineKit avEngineKit;
    private CallSession mCurrentCallSession;
    public IBusinessEvent mEvent;
    private List<PeerConnection.IceServer> iceServers = new ArrayList<>();

    // 初始化
    public static void init(IBusinessEvent iSocketEvent) {
        if (avEngineKit == null) {
            avEngineKit = new AVEngineKit();
            avEngineKit.mEvent = iSocketEvent;

            // 初始化一些stun和turn的地址
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

            PeerConnection.IceServer var11 = PeerConnection.IceServer.builder("stun:118.25.25.147:3478?transport=udp")
                    .createIceServer();
            PeerConnection.IceServer var12 = PeerConnection.IceServer.builder("turn:118.25.25.147:3478?transport=udp")
                    .setUsername("ddssingsong")
                    .setPassword("123456")
                    .createIceServer();
            PeerConnection.IceServer var13 = PeerConnection.IceServer.builder("turn:118.25.25.147:3478?transport=tcp")
                    .setUsername("ddssingsong")
                    .setPassword("123456")
                    .createIceServer();
            avEngineKit.iceServers.add(var11);
            avEngineKit.iceServers.add(var12);
            avEngineKit.iceServers.add(var13);


            PeerConnection.IceServer var21 = PeerConnection.IceServer.builder("stun:157.255.51.168:3478?transport=udp")
                    .createIceServer();
            PeerConnection.IceServer var22 = PeerConnection.IceServer.builder("turn:157.255.51.168:3478?transport=udp")
                    .setUsername("ddssingsong")
                    .setPassword("123456")
                    .createIceServer();
            PeerConnection.IceServer var23 = PeerConnection.IceServer.builder("turn:157.255.51.168:3478?transport=tcp")
                    .setUsername("ddssingsong")
                    .setPassword("123456")
                    .createIceServer();
            avEngineKit.iceServers.add(var21);
            avEngineKit.iceServers.add(var22);
            avEngineKit.iceServers.add(var23);
        }
    }


    public static AVEngineKit Instance() {
        AVEngineKit var;
        if ((var = avEngineKit) != null) {
            return var;
        } else {
            throw new NotInitializedExecption();
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
        // 忙线中
        if (mCurrentCallSession != null && mCurrentCallSession.getState() != EnumType.CallState.Idle) {
            if (isComing) {
                if (mEvent != null) {
                    // 发送->忙线中...
                    Log.e(TAG, "startCall error,mCurrentCallSession is exist," +
                            "start sendRefuse!");
                    mEvent.sendRefuse(targetId, EnumType.RefuseType.Busy.ordinal());
                }
            } else {
                Log.e(TAG, "startCall error,mCurrentCallSession is exist");
            }
            return false;
        }
        // 初始化会话
        mCurrentCallSession = new CallSession(avEngineKit);
        mCurrentCallSession.setIsAudioOnly(audioOnly);
        mCurrentCallSession.setRoom(room);
        mCurrentCallSession.setTargetId(targetId);
        mCurrentCallSession.setContext(context);
        mCurrentCallSession.setIsComing(isComing);
        mCurrentCallSession.setCallState(isComing ? EnumType.CallState.Incoming : EnumType.CallState.Outgoing);
        // 响铃并回复
        if (isComing) {
            mCurrentCallSession.shouldStartRing();
            mCurrentCallSession.sendRingBack(targetId);
        }
        // 创建房间
        else {
            mCurrentCallSession.createHome(room, roomSize);
        }
        return true;

    }


    // 挂断会话
    public void endCall() {
        if (mCurrentCallSession != null) {
            // 停止响铃
            mCurrentCallSession.shouldStopRing();
            // 有人进入房间
            if (mCurrentCallSession.isComing) {
                if (mCurrentCallSession.getState() == EnumType.CallState.Incoming) {
                    // 接收到邀请，还没同意，发送拒绝
                    mCurrentCallSession.sendRefuse();
                } else {
                    // 已经接通，挂断电话
                    mCurrentCallSession.leave();
                }
            } else {
                if (mCurrentCallSession.getState() == EnumType.CallState.Outgoing) {
                    mCurrentCallSession.sendCancel();
                } else {
                    // 已经接通，挂断电话
                    mCurrentCallSession.leave();
                }
            }
            mCurrentCallSession.setCallState(EnumType.CallState.Idle);
        }

    }

    public CallSession getCurrentSession() {
        return this.mCurrentCallSession;
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
