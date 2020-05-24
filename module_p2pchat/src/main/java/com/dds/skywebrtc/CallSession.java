package com.dds.skywebrtc;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.dds.skywebrtc.engine.EngineCallback;
import com.dds.skywebrtc.engine.webrtc.WebRTCEngine;
import com.dds.skywebrtc.inter.ISkyEvent;

import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by dds on 2019/8/19.
 * 会话层
 */
public class CallSession implements EngineCallback {
    private WeakReference<CallSessionCallback> sessionCallback;
    private ExecutorService executor;
    private Handler handler = new Handler(Looper.getMainLooper());
    // session参数
    private boolean mIsAudioOnly;
    // 房间人列表
    private List<String> mUserIDList;
    // 单聊对方Id/群聊邀请人
    public String mTargetId;
    // 房间Id
    private String mRoomId;
    // myId
    private String mMyId;

    private boolean mIsComing;
    private EnumType.CallState _callState = EnumType.CallState.Idle;
    private long startTime;

    private AVEngine iEngine;
    private ISkyEvent mEvent;

    public CallSession(Context context, boolean audioOnly, ISkyEvent event) {
        executor = Executors.newSingleThreadExecutor();
        this.mIsAudioOnly = audioOnly;
        this.mEvent = event;

        iEngine = AVEngine.createEngine(new WebRTCEngine(audioOnly, context));
        iEngine.init(this);
    }


    // ----------------------------------------各种控制--------------------------------------------

    // 创建房间
    public void createHome(String room, int roomSize) {
        executor.execute(() -> {
            if (mEvent != null) {
                mEvent.createRoom(room, roomSize);
            }
        });
    }

    // 加入房间
    public void joinHome(String roomId) {
        executor.execute(() -> {
            _callState = EnumType.CallState.Connecting;
            if (mEvent != null) {
                mEvent.sendJoin(roomId);
            }
        });

    }

    //开始响铃
    public void shouldStartRing() {
        if (mEvent != null) {
            mEvent.shouldStartRing(true);
        }
    }

    // 关闭响铃
    public void shouldStopRing() {
        if (mEvent != null) {
            mEvent.shouldStopRing();
        }
    }

    // 发送响铃回复
    public void sendRingBack(String targetId, String room) {
        executor.execute(() -> {
            if (mEvent != null) {
                mEvent.sendRingBack(targetId, room);
            }
        });
    }

    // 发送拒绝信令
    public void sendRefuse() {
        executor.execute(() -> {
            if (mEvent != null) {
                // 取消拨出
                mEvent.sendRefuse(mRoomId, mTargetId, EnumType.RefuseType.Hangup.ordinal());
            }
        });

    }

    // 发送忙时拒绝
    void sendBusyRefuse(String room, String targetId) {
        executor.execute(() -> {
            if (mEvent != null) {
                // 取消拨出
                mEvent.sendRefuse(room, targetId, EnumType.RefuseType.Busy.ordinal());
            }
        });

    }

    // 发送取消信令
    public void sendCancel() {
        executor.execute(() -> {
            if (mEvent != null) {
                // 取消拨出
                mEvent.sendCancel(mRoomId, mUserIDList);
            }
        });

    }

    // 离开房间
    public void leave() {
        executor.execute(() -> {
            if (mEvent != null) {
                mEvent.sendLeave(mRoomId, mMyId);
            }
        });
        // 释放变量
        release();

    }

    // 切换到语音接听
    public void sendTransAudio() {
        executor.execute(() -> {
            if (mEvent != null) {
                // 发送到对面，切换到语音
                mEvent.sendTransAudio(mTargetId);
            }
        });
    }

    // 设置静音
    public boolean toggleMuteAudio(boolean enable) {
        return iEngine.muteAudio(enable);
    }

    // 设置扬声器
    public boolean toggleSpeaker(boolean enable) {

        return iEngine.toggleSpeaker(enable);
    }

    // 切换到语音通话
    public void switchToAudio() {
        mIsAudioOnly = true;
        // 告诉远端
        sendTransAudio();
        // 本地切换
        if (sessionCallback.get() != null) {
            sessionCallback.get().didChangeMode(true);
        }

    }

    // 调整摄像头前置后置
    public void switchCamera() {
        iEngine.switchCamera();
    }

    // 释放资源
    private void release() {
        executor.execute(() -> {
            // 释放内容
            iEngine.release();
            // 状态设置为Idle
            _callState = EnumType.CallState.Idle;

            //界面回调
            if (sessionCallback.get() != null) {
                sessionCallback.get().didCallEndWithReason(null);
            }
        });
    }

    //------------------------------------receive---------------------------------------------------

    // 加入房间成功
    public void onJoinHome(String myId, String users) {
        startTime = 0;
        handler.post(() -> executor.execute(() -> {
            mMyId = myId;
            List<String> strings;
            if (!TextUtils.isEmpty(users)) {
                String[] split = users.split(",");
                strings = Arrays.asList(split);
                mUserIDList = strings;
            }

            // 发送邀请
            if (!mIsComing) {
                List<String> inviteList = new ArrayList<>();
                inviteList.add(mTargetId);
                mEvent.sendInvite(mRoomId, inviteList, mIsAudioOnly);
            } else {
                iEngine.joinRoom(mUserIDList);
            }

            if (!isAudioOnly()) {
                // 画面预览
                if (sessionCallback.get() != null) {
                    sessionCallback.get().didCreateLocalVideoTrack();
                }

            }


        }));


    }

    // 新成员进入
    public void newPeer(String userId) {
        handler.post(() -> executor.execute(() -> {
            // 其他人加入房间
            iEngine.userIn(userId);

            // 关闭响铃
            if (mEvent != null) {
                mEvent.shouldStopRing();
            }
            // 更换界面
            _callState = EnumType.CallState.Connected;
            if (sessionCallback.get() != null) {
                startTime = System.currentTimeMillis();
                sessionCallback.get().didChangeState(_callState);

            }
        }));

    }

    // 对方已拒绝
    public void onRefuse(String userId) {
        iEngine.userReject(userId);
    }

    // 对方已响铃
    public void onRingBack(String userId) {
        if (mEvent != null) {
            mEvent.shouldStartRing(false);
        }
    }

    // 切换到语音
    public void onTransAudio(String userId) {
        mIsAudioOnly = true;
        // 本地切换
        if (sessionCallback.get() != null) {
            sessionCallback.get().didChangeMode(true);
        }
    }

    // 对方网络断开
    public void onDisConnect(String userId) {

    }

    public void onReceiveOffer(String userId, String description) {
        executor.execute(() -> {
            iEngine.receiveOffer(userId, description);
        });

    }

    public void onReceiverAnswer(String userId, String sdp) {
        executor.execute(() -> {
            iEngine.receiveAnswer(userId, sdp);
        });

    }

    public void onRemoteIceCandidate(String userId, String id, int label, String candidate) {
        executor.execute(() -> {
            iEngine.receiveIceCandidate(userId, id, label, candidate);
        });

    }

    // 对方离开房间
    public void onLeave(String userId) {
        executor.execute(() -> iEngine.leaveRoom(userId));

    }


    // --------------------------------界面显示相关--------------------------------------------/

    public long getStartTime() {
        return startTime;
    }

    public View setupLocalVideo(boolean isOverlay) {
        return iEngine.startPreview(isOverlay);
    }


    public View setupRemoteVideo(String userId, boolean isOverlay) {
        return iEngine.setupRemoteVideo(userId, isOverlay);
    }


    //------------------------------------各种参数----------------------------------------------/

    public void setIsAudioOnly(boolean _isAudioOnly) {
        this.mIsAudioOnly = _isAudioOnly;
    }

    public boolean isAudioOnly() {
        return mIsAudioOnly;
    }

    public void setTargetId(String targetIds) {
        this.mTargetId = targetIds;
    }

    public void setIsComing(boolean isComing) {
        this.mIsComing = isComing;
    }

    public boolean isComing() {
        return mIsComing;
    }

    public void setRoom(String _room) {
        this.mRoomId = _room;
    }

    public String getRoomId() {
        return mRoomId;
    }

    public EnumType.CallState getState() {
        return _callState;
    }

    public void setCallState(EnumType.CallState callState) {
        this._callState = callState;
    }

    public void setSessionCallback(CallSessionCallback sessionCallback) {
        this.sessionCallback = new WeakReference<>(sessionCallback);
    }

    //-----------------------------Engine回调-----------------------------------------

    @Override
    public void joinRoomSucc() {
        // 关闭响铃
        if (mEvent != null) {
            mEvent.shouldStopRing();
        }
        // 更换界面
        _callState = EnumType.CallState.Connected;
        if (sessionCallback.get() != null) {
            startTime = System.currentTimeMillis();
            sessionCallback.get().didChangeState(_callState);

        }
    }

    @Override
    public void exitRoom() {
        // 状态设置为Idle
        handler.post(this::release);

    }

    @Override
    public void onSendIceCandidate(String userId, IceCandidate candidate) {
        executor.execute(() -> {
            if (mEvent != null) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.d("dds_test", "onSendIceCandidate");
                mEvent.sendIceCandidate(userId, candidate.sdpMid, candidate.sdpMLineIndex, candidate.sdp);
            }
        });

    }

    @Override
    public void onSendOffer(String userId, SessionDescription description) {
        executor.execute(() -> {
            if (mEvent != null) {
                Log.d("dds_test", "onSendOffer");
                mEvent.sendOffer(userId, description.description);
            }
        });

    }

    @Override
    public void onSendAnswer(String userId, SessionDescription description) {
        executor.execute(() -> {
            if (mEvent != null) {
                Log.d("dds_test", "onSendAnswer");
                mEvent.sendAnswer(userId, description.description);
            }
        });

    }

    @Override
    public void onRemoteStream(String userId) {
        // 画面预览
        if (sessionCallback.get() != null) {
            sessionCallback.get().didReceiveRemoteVideoTrack(userId);
        }
    }

    public interface CallSessionCallback {
        void didCallEndWithReason(EnumType.CallEndReason var1);

        void didChangeState(EnumType.CallState var1);

        void didChangeMode(boolean isAudioOnly);

        void didCreateLocalVideoTrack();

        void didReceiveRemoteVideoTrack(String userId);

        void didError(String error);

    }
}
