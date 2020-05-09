package com.dds.skywebrtc;

import android.content.Context;
import android.media.AudioManager;
import android.text.TextUtils;
import android.view.View;

import com.dds.skywebrtc.engine.EngineCallback;
import com.dds.skywebrtc.engine.WebRTCEngine;

import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
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
    public WeakReference<CallSessionCallback> sessionCallback;
    public SkyEngineKit avEngineKit;
    public ExecutorService executor;
    private AudioManager audioManager;
    // session参数
    public boolean mIsAudioOnly;
    public List<String> mUserIDList;
    public String mTargetId;
    public String mRoomId;
    public String mMyId;
    public boolean mIsComing;
    public EnumType.CallState _callState = EnumType.CallState.Idle;
    private long startTime;

    private AVEngine iEngine;


    public CallSession(SkyEngineKit avEngineKit, Context context, boolean audioOnly) {
        this.avEngineKit = avEngineKit;
        executor = Executors.newSingleThreadExecutor();
        this.mIsAudioOnly = audioOnly;
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);


        iEngine = AVEngine.createEngine(new WebRTCEngine(audioOnly, context));
        iEngine.init(this);
    }


    // ----------------------------------------各种控制--------------------------------------------

    // 创建房间
    public void createHome(String room, int roomSize) {
        executor.execute(() -> {
            if (avEngineKit.mEvent != null) {
                avEngineKit.mEvent.createRoom(room, roomSize);
            }
        });
    }

    // 加入房间
    public void joinHome(String roomId) {
        executor.execute(() -> {
            _callState = EnumType.CallState.Connecting;
            if (avEngineKit.mEvent != null) {
                avEngineKit.mEvent.sendJoin(roomId);
            }
        });

    }

    //开始响铃
    public void shouldStartRing() {
        if (avEngineKit.mEvent != null) {
            avEngineKit.mEvent.shouldStartRing(true);
        }
    }

    // 关闭响铃
    public void shouldStopRing() {
        if (avEngineKit.mEvent != null) {
            avEngineKit.mEvent.shouldStopRing();
        }
    }

    // 发送响铃回复
    public void sendRingBack(String targetId, String room) {
        executor.execute(() -> {
            if (avEngineKit.mEvent != null) {
                avEngineKit.mEvent.sendRingBack(targetId, room);
            }
        });
    }

    // 发送拒绝信令
    public void sendRefuse() {
        executor.execute(() -> {
            if (avEngineKit.mEvent != null) {
                // 取消拨出
                avEngineKit.mEvent.sendRefuse(mRoomId, mTargetId, EnumType.RefuseType.Hangup.ordinal());
            }
        });

    }

    // 自動发送忙时拒绝
    public void sendRefuse(String room, String targetId, EnumType.RefuseType refuseType) {
        executor.execute(() -> {
            if (avEngineKit.mEvent != null) {
                // 取消拨出
                avEngineKit.mEvent.sendRefuse(room, targetId, refuseType.ordinal());
            }
        });

    }

    // 发送取消信令
    public void sendCancel() {
        executor.execute(() -> {
            if (avEngineKit.mEvent != null) {
                // 取消拨出
                avEngineKit.mEvent.sendCancel(mRoomId, mUserIDList);
            }
        });

    }

    // 离开房间
    public void leave() {
        executor.execute(() -> {
            if (avEngineKit.mEvent != null) {
                avEngineKit.mEvent.sendLeave(mRoomId, mMyId);
            }
        });
        release();

    }

    public void sendTransAudio() {
        executor.execute(() -> {
            if (avEngineKit.mEvent != null) {
                // 发送到对面，切换到语音
                avEngineKit.mEvent.sendTransAudio(mTargetId);
            }
        });
    }

    // 设置静音
    public boolean muteAudio(boolean enable) {
        return true;
    }

    // 设置扬声器
    public boolean toggleSpeaker(boolean enable) {
        if (audioManager != null) {
            if (enable) {
                audioManager.setSpeakerphoneOn(true);
                audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                        audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL),
                        AudioManager.STREAM_VOICE_CALL);
            } else {
                audioManager.setSpeakerphoneOn(false);
                audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                        audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), AudioManager.STREAM_VOICE_CALL);
            }

            return true;
        }

        return false;
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

    private boolean isSwitch = false; // 是否正在切换摄像头

    // 调整摄像头前置后置
    public void switchCamera() {
//        if (isSwitch) return;
//        isSwitch = true;
//        if (captureAndroid == null) return;
//        if (captureAndroid instanceof CameraVideoCapturer) {
//            CameraVideoCapturer cameraVideoCapturer = (CameraVideoCapturer) captureAndroid;
//            try {
//                cameraVideoCapturer.switchCamera(new CameraVideoCapturer.CameraSwitchHandler() {
//                    @Override
//                    public void onCameraSwitchDone(boolean isFrontCamera) {
//                        isSwitch = false;
//                    }
//
//                    @Override
//                    public void onCameraSwitchError(String errorDescription) {
//                        isSwitch = false;
//                    }
//                });
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        } else {
//            Log.d(TAG, "Will not switch camera, video caputurer is not a camera");
//        }

    }

    private void release() {
        executor.execute(() -> {
            if (audioManager != null) {
                audioManager.setMode(AudioManager.MODE_NORMAL);
            }
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
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        executor.execute(() -> {
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
                avEngineKit.mEvent.sendInvite(mRoomId, inviteList, mIsAudioOnly);
            } else {
                iEngine.joinRoom(mUserIDList);
            }

            if (!isAudioOnly()) {
                // debug测试视频，关闭语音以防杂音
//                if (BuildConfig.DEBUG) {
//                    muteAudio(false);
//                }
                // 画面预览
                if (sessionCallback.get() != null) {
                    sessionCallback.get().didCreateLocalVideoTrack();
                }

            }


        });
    }

    // 新成员进入
    public void newPeer(String userId) {
        executor.execute(() -> {
            // 其他人加入房间
            iEngine.userIn(userId);

            // 关闭响铃
            if (avEngineKit.mEvent != null) {
                avEngineKit.mEvent.shouldStopRing();
            }
            // 更换界面
            _callState = EnumType.CallState.Connected;
            if (sessionCallback.get() != null) {
                startTime = System.currentTimeMillis();
                sessionCallback.get().didChangeState(_callState);

            }
        });
    }

    // 对方已拒绝
    public void onRefuse(String userId) {
        iEngine.userReject(userId);
    }

    // 对方已响铃
    public void onRingBack(String userId) {
        if (avEngineKit.mEvent != null) {
            avEngineKit.mEvent.shouldStartRing(false);
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

    // 切换到语音
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
        release();
    }


    // --------------------------------界面显示相关-------------------------------------------------

    public long getStartTime() {
        return startTime;
    }

    public View setupLocalVideo(boolean isOverlay) {
        return iEngine.startPreview(isOverlay);
    }


    public View setupRemoteVideo(Context context, String userId, boolean isOverlay) {
        return iEngine.setupRemoteVideo(context, userId, isOverlay);
    }


    // ***********************************各种参数******************************************/
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
        if (avEngineKit.mEvent != null) {
            avEngineKit.mEvent.shouldStopRing();
        }
        // 更换界面
        _callState = EnumType.CallState.Connected;
        if (sessionCallback.get() != null) {
            startTime = System.currentTimeMillis();
            sessionCallback.get().didChangeState(_callState);

        }
    }

    @Override
    public void onSendIceCandidate(String userId, IceCandidate candidate) {
        if (avEngineKit.mEvent != null) {
            avEngineKit.mEvent.sendIceCandidate(userId, candidate.sdpMid, candidate.sdpMLineIndex, candidate.sdp);
        }
    }

    @Override
    public void onSendOffer(String userId, SessionDescription description) {
        if (avEngineKit.mEvent != null) {
            avEngineKit.mEvent.sendOffer(userId, description.description);
        }
    }

    @Override
    public void onSendAnswer(String userId, SessionDescription description) {
        if (avEngineKit.mEvent != null) {
            avEngineKit.mEvent.sendAnswer(userId, description.description);
        }
    }

    @Override
    public void onRemoteStream(String userId, MediaStream stream) {
        // 画面预览
        if (sessionCallback.get() != null) {
            sessionCallback.get().didReceiveRemoteVideoTrack(userId, stream);
        }
    }

    public interface CallSessionCallback {
        void didCallEndWithReason(EnumType.CallEndReason var1);

        void didChangeState(EnumType.CallState var1);

        void didChangeMode(boolean isAudioOnly);

        void didCreateLocalVideoTrack();

        void didReceiveRemoteVideoTrack(String userId, MediaStream stream);

        void didError(String error);

    }
}
