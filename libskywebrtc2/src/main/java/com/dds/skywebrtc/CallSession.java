package com.dds.skywebrtc;

import android.app.Application;
import android.content.Context;
import android.media.AudioManager;
import android.text.TextUtils;
import android.util.Log;

import com.dds.skywebrtc.render.ProxyVideoSink;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.DataChannel;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RendererCommon;
import org.webrtc.RtpReceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoDecoderFactory;
import org.webrtc.VideoEncoderFactory;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;
import org.webrtc.audio.JavaAudioDeviceModule;

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
public class CallSession {
    public final static String TAG = "dds_CallSession";
    private WeakReference<CallSessionCallback> sessionCallback;
    private SkyEngineKit avEngineKit;
    public ExecutorService executor;

    public static final String VIDEO_TRACK_ID = "ARDAMSv0";
    public static final String AUDIO_TRACK_ID = "ARDAMSa0";
    public static final String VIDEO_CODEC_H264 = "H264";
    public static final int VIDEO_RESOLUTION_WIDTH = 320;
    public static final int VIDEO_RESOLUTION_HEIGHT = 240;
    public static final int FPS = 10;

    public PeerConnectionFactory _factory;
    public MediaStream _localStream;
    public VideoTrack _localVideoTrack;
    public AudioTrack _localAudioTrack;
    public VideoSource videoSource;
    public AudioSource audioSource;
    public VideoCapturer captureAndroid;
    public EglBase mRootEglBase;
    private Context mContext;
    private AudioManager audioManager;

    private Peer mPeer;
    // session参数
    public boolean mIsAudioOnly;
    public String mTargetId;
    public String mRoom;
    public String mMyId;
    public boolean mIsComing;
    public EnumType.CallState _callState = EnumType.CallState.Idle;


    private enum Role {Caller, Receiver,}

    private Role _role;

    public CallSession(SkyEngineKit avEngineKit) {
        this.avEngineKit = avEngineKit;
        mRootEglBase = EglBase.create();
        executor = Executors.newSingleThreadExecutor();
        audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
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
    public void joinHome() {
        executor.execute(() -> {
            _callState = EnumType.CallState.Connecting;
            if (avEngineKit.mEvent != null) {
                avEngineKit.mEvent.sendJoin(mRoom);
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
    public void sendRingBack(String targetId) {
        executor.execute(() -> {
            if (avEngineKit.mEvent != null) {
                avEngineKit.mEvent.sendRingBack(targetId);
            }
        });
    }

    // 发送拒绝信令
    public void sendRefuse() {
        executor.execute(() -> {
            if (avEngineKit.mEvent != null) {
                // 取消拨出
                avEngineKit.mEvent.sendRefuse(mTargetId, EnumType.RefuseType.Hangup.ordinal());
            }
        });

    }

    // 发送取消信令
    public void sendCancel() {
        executor.execute(() -> {
            if (avEngineKit.mEvent != null) {
                // 取消拨出
                avEngineKit.mEvent.sendCancel(mTargetId);
            }
        });

    }

    // 离开房间
    public void leave() {
        executor.execute(() -> {
            if (avEngineKit.mEvent != null) {
                avEngineKit.mEvent.sendLeave(mRoom, mMyId);
            }
        });
        release();

    }

    // 设置自己静音
    public boolean muteAudio(boolean enable) {
        if (_localAudioTrack != null) {
            _localAudioTrack.setEnabled(enable);
            return true;
        }
        return false;

    }

    // 设置扬声器
    public boolean toggleSpeaker(boolean enable) {
        if (audioManager != null) {
            audioManager.setSpeakerphoneOn(enable);
            return true;
        }
        return false;
    }

    private void release() {
        executor.execute(() -> {
            // audio释放
            if (audioSource != null) {
                audioSource.dispose();
                audioSource = null;
            }
            // video释放
            if (videoSource != null) {
                videoSource.dispose();
                videoSource = null;
            }
            // 释放摄像头
            if (captureAndroid != null) {
                try {
                    captureAndroid.stopCapture();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                captureAndroid.dispose();
                captureAndroid = null;
            }

            // 关闭peer
            if (mPeer != null && mPeer.pc != null) {
                mPeer.pc.close();
            }

            // 释放画布
            if (surfaceTextureHelper != null) {
                surfaceTextureHelper.dispose();
                surfaceTextureHelper = null;
            }
            // 释放factory
            if (_factory != null) {
                _factory.dispose();
                _factory = null;
            }
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
        executor.execute(() -> {
            mMyId = myId;
            // todo 多人会议
            if (!TextUtils.isEmpty(users)) {
                String[] split = users.split(",");
                List<String> strings = Arrays.asList(split);
                mTargetId = strings.get(0);
            }
            if (_factory == null) {
                _factory = createConnectionFactory();
            }
            if (_localStream == null) {
                createLocalStream();
            }
            if (mIsComing) {
                // 接电话一方
                _role = Role.Caller;
                // 创建Peer
                mPeer = new Peer(mTargetId);
                // 添加本地流
                mPeer.pc.addStream(_localStream);
                // 创建offer
                mPeer.pc.createOffer(mPeer, offerOrAnswerConstraint());
                // 关闭响铃
                if (avEngineKit.mEvent != null) {
                    avEngineKit.mEvent.shouldStopRing();
                }
                // 更换界面
                _callState = EnumType.CallState.Connected;

                if (sessionCallback.get() != null) {
                    sessionCallback.get().didChangeState(_callState);
                }
            } else {
                avEngineKit.mEvent.sendInvite(mRoom, mTargetId, mIsAudioOnly);
            }

            // 开始显示本地画面
            if (!isAudioOnly()) {
                if (sessionCallback.get() != null) {
                    sessionCallback.get().didCreateLocalVideoTrack();
                }

            }


        });
    }

    // 新成员进入
    public void newPeer(String userId) {
        executor.execute(() -> {
            if (_localStream == null) {
                createLocalStream();
            }
            try {
                mPeer = new Peer(userId);
                mPeer.pc.addStream(_localStream);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            // 关闭响铃
            if (avEngineKit.mEvent != null) {
                avEngineKit.mEvent.shouldStopRing();
            }

            // 切换界面
            _callState = EnumType.CallState.Connected;
            if (sessionCallback.get() != null) {
                sessionCallback.get().didChangeState(EnumType.CallState.Connected);
            }

        });
    }

    public void onRefuse(String userId) {
        release();
    }

    // 对方已响铃
    public void onRingBack(String fromId) {
        if (avEngineKit.mEvent != null) {
            avEngineKit.mEvent.shouldStartRing(false);
        }
    }

    public void onReceiveOffer(String socketId, String description) {
        Log.e("dds_test", "onReceiveOffer:" + socketId);
        executor.execute(() -> {
            _role = Role.Receiver;
            SessionDescription sdp = new SessionDescription(SessionDescription.Type.OFFER, description);
            if (mPeer != null) {
                Log.e("dds_test", "onReceiveOffer setRemoteDescription");
                mPeer.pc.setRemoteDescription(mPeer, sdp);
            }

        });

    }

    public void onReceiverAnswer(String socketId, String sdp) {
        Log.e("dds_test", "onReceiverAnswer:" + socketId);
        executor.execute(() -> {
            SessionDescription sessionDescription = new SessionDescription(SessionDescription.Type.ANSWER, sdp);
            if (mPeer != null && mPeer.pc != null) {
                Log.e("dds_test", "onReceiverAnswer setRemoteDescription");
                mPeer.pc.setRemoteDescription(mPeer, sessionDescription);
            }
        });

    }

    public void onRemoteIceCandidate(String userId, String id, int label, String candidate) {
        executor.execute(() -> {
            if (mPeer != null && mPeer.pc != null) {
                IceCandidate iceCandidate = new IceCandidate(id, label, candidate);
                mPeer.pc.addIceCandidate(iceCandidate);
            }
        });

    }

    // 对方离开房间
    public void onLeave(String userId) {
        release();
    }


    private MediaStream _remoteStream;

    // 每一个Session 可包含多个PeerConnection
    private class Peer implements SdpObserver, PeerConnection.Observer {
        private PeerConnection pc;
        private String userId;

        public Peer(String userId) {
            this.pc = createPeerConnection();
            this.userId = userId;

        }

        private PeerConnection createPeerConnection() {
            // 管道连接抽象类实现方法
            PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(avEngineKit.getIceServers());
            return _factory.createPeerConnection(rtcConfig, this);
        }

        //-------------Observer--------------------
        @Override
        public void onSignalingChange(PeerConnection.SignalingState signalingState) {
            Log.i(TAG, "onSignalingChange: " + signalingState);
        }

        @Override
        public void onIceConnectionChange(PeerConnection.IceConnectionState newState) {
            Log.i(TAG, "onIceConnectionChange: " + newState.toString());

        }

        @Override
        public void onIceConnectionReceivingChange(boolean receiving) {
            Log.i(TAG, "onIceConnectionReceivingChange:" + receiving);
        }

        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState newState) {
            Log.i(TAG, "onIceGatheringChange:" + newState.toString());
        }

        @Override
        public void onIceCandidate(IceCandidate candidate) {
            Log.i(TAG, "onIceCandidate:");
            // 发送IceCandidate
            executor.execute(() -> {
                try {
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                avEngineKit.mEvent.sendIceCandidate(userId, candidate.sdpMid, candidate.sdpMLineIndex, candidate.sdp);
            });


        }

        @Override
        public void onIceCandidatesRemoved(IceCandidate[] candidates) {
            Log.i(TAG, "onIceCandidatesRemoved:");
        }

        @Override
        public void onAddStream(MediaStream stream) {
            _remoteStream = stream;
            Log.i(TAG, "onAddStream:");
            if (stream.audioTracks.size() > 0) {
                Log.e("dds_test", "onAddStream audioTracks ");
                stream.audioTracks.get(0).setEnabled(true);
            }
            if (sessionCallback.get() != null) {
                sessionCallback.get().didReceiveRemoteVideoTrack();
            }


        }

        @Override
        public void onRemoveStream(MediaStream stream) {
            Log.i(TAG, "onRemoveStream:");
        }

        @Override
        public void onDataChannel(DataChannel dataChannel) {
            Log.i(TAG, "onDataChannel:");
        }

        @Override
        public void onRenegotiationNeeded() {
            Log.i(TAG, "onRenegotiationNeeded:");
        }

        @Override
        public void onAddTrack(RtpReceiver receiver, MediaStream[] mediaStreams) {
            Log.i(TAG, "onAddTrack:");
        }


        //-------------SdpObserver--------------------
        @Override
        public void onCreateSuccess(SessionDescription origSdp) {
            Log.d(TAG, "sdp创建成功       " + origSdp.type);
            String sdpDescription = origSdp.description;
            final SessionDescription sdp = new SessionDescription(origSdp.type, sdpDescription);
            executor.execute(() -> pc.setLocalDescription(Peer.this, sdp));


        }

        @Override
        public void onSetSuccess() {
            executor.execute(() -> {
                Log.d(TAG, "sdp连接成功        " + pc.signalingState().toString());
                if (pc.signalingState() == PeerConnection.SignalingState.HAVE_REMOTE_OFFER) {
                    pc.createAnswer(Peer.this, offerOrAnswerConstraint());
                } else if (pc.signalingState() == PeerConnection.SignalingState.HAVE_LOCAL_OFFER) {
                    //判断连接状态为本地发送offer
                    if (_role == Role.Receiver) {
                        //接收者，发送Answer

                        avEngineKit.mEvent.sendAnswer(userId, pc.getLocalDescription().description);

                    } else if (_role == Role.Caller) {
                        //发送者,发送自己的offer
                        avEngineKit.mEvent.sendOffer(userId, pc.getLocalDescription().description);
                    }

                } else if (pc.signalingState() == PeerConnection.SignalingState.STABLE) {
                    // Stable 稳定的
                    if (_role == Role.Receiver) {
                        avEngineKit.mEvent.sendAnswer(userId, pc.getLocalDescription().description);

                    }
                }
            });


        }

        @Override
        public void onCreateFailure(String error) {
            Log.i(TAG, " SdpObserver onCreateFailure:" + error);
        }

        @Override
        public void onSetFailure(String error) {
            Log.i(TAG, "SdpObserver onSetFailure:");
        }
    }


    // --------------------------------界面显示相关-------------------------------------------------

    public long getStartTime() {
        return 0;
    }

    public SurfaceViewRenderer createRendererView() {
        SurfaceViewRenderer renderer = new SurfaceViewRenderer(mContext);
        renderer.init(mRootEglBase.getEglBaseContext(), null);
        renderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        renderer.setMirror(true);
        return renderer;
    }

    public void setupRemoteVideo(SurfaceViewRenderer surfaceView) {
        ProxyVideoSink sink = new ProxyVideoSink();
        sink.setTarget(surfaceView);
        if (_remoteStream != null && _remoteStream.videoTracks.size() > 0) {
            _remoteStream.videoTracks.get(0).addSink(sink);

        }


    }

    public void setupLocalVideo(SurfaceViewRenderer SurfaceViewRenderer) {
        ProxyVideoSink sink = new ProxyVideoSink();
        sink.setTarget(SurfaceViewRenderer);
        if (_localStream.videoTracks.size() > 0) {
            _localStream.videoTracks.get(0).addSink(sink);
        }
    }

    //------------------------------------各种初始化---------------------------------------------

    public void createLocalStream() {
        _localStream = _factory.createLocalMediaStream("ARDAMS");
        // 音频
        audioSource = _factory.createAudioSource(createAudioConstraints());
        _localAudioTrack = _factory.createAudioTrack(AUDIO_TRACK_ID, audioSource);
        _localStream.addTrack(_localAudioTrack);

        // 视频
        if (!mIsAudioOnly) {
            captureAndroid = createVideoCapture();
            surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", mRootEglBase.getEglBaseContext());
            videoSource = _factory.createVideoSource(captureAndroid.isScreencast());
            captureAndroid.initialize(surfaceTextureHelper, mContext, videoSource.getCapturerObserver());
            captureAndroid.startCapture(VIDEO_RESOLUTION_WIDTH, VIDEO_RESOLUTION_HEIGHT, FPS);
            _localVideoTrack = _factory.createVideoTrack(VIDEO_TRACK_ID, videoSource);
            _localStream.addTrack(_localVideoTrack);
        }

    }

    public PeerConnectionFactory createConnectionFactory() {
        PeerConnectionFactory.initialize(PeerConnectionFactory
                .InitializationOptions
                .builder(mContext)
                .createInitializationOptions());

        final VideoEncoderFactory encoderFactory;
        final VideoDecoderFactory decoderFactory;

        encoderFactory = new DefaultVideoEncoderFactory(
                mRootEglBase.getEglBaseContext(),
                true,
                true);
        decoderFactory = new DefaultVideoDecoderFactory(mRootEglBase.getEglBaseContext());
        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        return PeerConnectionFactory.builder()
                .setOptions(options)
                .setAudioDeviceModule(JavaAudioDeviceModule.builder(mContext).createAudioDeviceModule())
                .setVideoEncoderFactory(encoderFactory)
                .setVideoDecoderFactory(decoderFactory)
                .createPeerConnectionFactory();
    }

    private SurfaceTextureHelper surfaceTextureHelper;

    private VideoCapturer createVideoCapture() {
        VideoCapturer videoCapturer;
        if (useCamera2()) {
            videoCapturer = createCameraCapture(new Camera2Enumerator(mContext));
        } else {
            videoCapturer = createCameraCapture(new Camera1Enumerator(true));
        }
        return videoCapturer;
    }

    private VideoCapturer createCameraCapture(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();

        // First, try to find front facing camera
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        // Front facing camera not found, try something else
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        return null;
    }

    private boolean useCamera2() {
        return Camera2Enumerator.isSupported(mContext);
    }

    //**************************************各种约束******************************************/
    private static final String AUDIO_ECHO_CANCELLATION_CONSTRAINT = "googEchoCancellation";
    private static final String AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT = "googAutoGainControl";
    private static final String AUDIO_HIGH_PASS_FILTER_CONSTRAINT = "googHighpassFilter";
    private static final String AUDIO_NOISE_SUPPRESSION_CONSTRAINT = "googNoiseSuppression";

    private MediaConstraints createAudioConstraints() {
        MediaConstraints audioConstraints = new MediaConstraints();
        audioConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair(AUDIO_ECHO_CANCELLATION_CONSTRAINT, "true"));
        audioConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair(AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT, "false"));
        audioConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair(AUDIO_HIGH_PASS_FILTER_CONSTRAINT, "true"));
        audioConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair(AUDIO_NOISE_SUPPRESSION_CONSTRAINT, "true"));
        return audioConstraints;
    }

    private MediaConstraints offerOrAnswerConstraint() {
        MediaConstraints mediaConstraints = new MediaConstraints();
        ArrayList<MediaConstraints.KeyValuePair> keyValuePairs = new ArrayList<>();
        keyValuePairs.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        keyValuePairs.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        mediaConstraints.mandatory.addAll(keyValuePairs);
        return mediaConstraints;
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

    public void setContext(Context context) {
        if (context instanceof Application) {
            this.mContext = context;
        } else {
            this.mContext = context.getApplicationContext();
        }

    }

    public void setIsComing(boolean isComing) {
        this.mIsComing = isComing;
    }

    public void setRoom(String _room) {
        this.mRoom = _room;
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

    public interface CallSessionCallback {
        void didCallEndWithReason(EnumType.CallEndReason var1);

        void didChangeState(EnumType.CallState var1);

        void didChangeMode(boolean isAudio);

        void didCreateLocalVideoTrack();

        void didReceiveRemoteVideoTrack();

        void didError(String error);

    }
}
