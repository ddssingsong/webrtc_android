package com.dds.skywebrtc;

import android.content.Context;
import android.util.Log;

import com.dds.skywebrtc.except.NotInitializedException;
import com.dds.skywebrtc.inter.ISkyEvent;

import org.webrtc.PeerConnection;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dds on 2019/8/19.
 */
public class SkyEngineKit {
    private final static String TAG = "dds_AVEngineKit";
    private static SkyEngineKit avEngineKit;
    private CallSession mCurrentCallSession;
    public ISkyEvent mEvent;
    private List<PeerConnection.IceServer> iceServers = new ArrayList<>();

    public static SkyEngineKit Instance() {
        SkyEngineKit var;
        if ((var = avEngineKit) != null) {
            return var;
        } else {
            throw new NotInitializedException();
        }
    }

    // 初始化
    public static void init(ISkyEvent iSocketEvent) {
        if (avEngineKit == null) {
            avEngineKit = new SkyEngineKit();
            avEngineKit.mEvent = iSocketEvent;

            // 初始化一些stun和turn的地址
            PeerConnection.IceServer var1 = PeerConnection.IceServer.builder("stun:stun.l.google.com:19302")
                    .createIceServer();
            avEngineKit.iceServers.add(var1);


            PeerConnection.IceServer var11 = PeerConnection.IceServer.builder("stun:47.93.186.97:3478?transport=udp")
                    .createIceServer();
            PeerConnection.IceServer var12 = PeerConnection.IceServer.builder("turn:47.93.186.97:3478?transport=udp")
                    .setUsername("ddssingsong")
                    .setPassword("123456")
                    .createIceServer();
            PeerConnection.IceServer var13 = PeerConnection.IceServer.builder("turn:47.93.186.97:3478?transport=tcp")
                    .setUsername("ddssingsong")
                    .setPassword("123456")
                    .createIceServer();
            avEngineKit.iceServers.add(var11);
            avEngineKit.iceServers.add(var12);
            avEngineKit.iceServers.add(var13);

        }
    }


    // 拨打电话
    public boolean startOutCall(Context context,
                                final String room,
                                final String targetId,
                                final boolean audioOnly) {
        // 未初始化
        if (avEngineKit == null) {
            Log.e(TAG, "startOutCall error,please init first");
            return false;
        }
        // 忙线中
        if (mCurrentCallSession != null && mCurrentCallSession.getState() != EnumType.CallState.Idle) {
            Log.i(TAG, "startCall error,currentCallSession is exist");
            return false;
        }
        // 初始化会话
        mCurrentCallSession = new CallSession(avEngineKit, context, audioOnly);
        mCurrentCallSession.setContext(context);
        mCurrentCallSession.setIsAudioOnly(audioOnly);
        mCurrentCallSession.setRoom(room);
        mCurrentCallSession.setTargetId(targetId);
        mCurrentCallSession.setIsComing(false);
        mCurrentCallSession.setCallState(EnumType.CallState.Outgoing);
        // 创建房间
        mCurrentCallSession.createHome(room, 2);
        return true;
    }

    // 接听电话
    public boolean startInCall(Context context,
                               final String room,
                               final String targetId,
                               final boolean audioOnly) {
        if (avEngineKit == null) {
            Log.e(TAG, "startInCall error,init is not set");
            return false;
        }
        // 忙线中
        if (mCurrentCallSession != null && mCurrentCallSession.getState() != EnumType.CallState.Idle) {
            if (mEvent != null) {
                // 发送->忙线中...
                Log.i(TAG, "startInCall busy,currentCallSession is exist,start sendRefuse!");
                mEvent.sendRefuse(targetId, EnumType.RefuseType.Busy.ordinal());
            }
            return false;
        }
        // 初始化会话
        mCurrentCallSession = new CallSession(avEngineKit, context, audioOnly);
        mCurrentCallSession.setIsAudioOnly(audioOnly);
        mCurrentCallSession.setRoom(room);
        mCurrentCallSession.setTargetId(targetId);
        mCurrentCallSession.setContext(context);
        mCurrentCallSession.setIsComing(true);
        mCurrentCallSession.setCallState(EnumType.CallState.Incoming);

        // 开始响铃并回复
        mCurrentCallSession.shouldStartRing();
        mCurrentCallSession.sendRingBack(targetId);


        return true;
    }

    // 挂断会话
    public void endCall() {
        if (mCurrentCallSession != null) {
            // 停止响铃
            mCurrentCallSession.shouldStopRing();

            if (mCurrentCallSession.mIsComing) {
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


    // 获取对话实例
    public CallSession getCurrentSession() {
        return this.mCurrentCallSession;
    }


    // --------------------------------iceServers------------------------------------

    // 添加turn和stun
    public void addIceServer(String host, String username, String pwd) {
        SkyEngineKit var = this;
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
