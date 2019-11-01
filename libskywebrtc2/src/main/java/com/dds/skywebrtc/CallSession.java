package com.dds.skywebrtc;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;

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
import org.webrtc.RtpReceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoDecoderFactory;
import org.webrtc.VideoEncoderFactory;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;
import org.webrtc.audio.JavaAudioDeviceModule;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by dds on 2019/8/19.
 * android_shuai@163.com
 */
public class CallSession {
    public final static String TAG = "dds_CallSession";
    private WeakReference<CallSessionCallback> sessionCallback;
    private AVEngineKit avEngineKit;
    public ExecutorService executor;
    public ArrayList<String> _connectionIdArray;
    private Map<String, Peer> _connectionPeerDic;

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
    public EglBase _rootEglBase;
    private Context _context;

    public boolean _isAudioOnly;
    public String _targetIds;
    public String _room;
    public String _myId;
    public boolean isComing;


    enum Role {Caller, Receiver,}

    private Role _role;

    public EnumType.CallState _callState = EnumType.CallState.Idle;

    public CallSession(AVEngineKit avEngineKit) {
        this.avEngineKit = avEngineKit;
        _connectionIdArray = new ArrayList<>();
        this._connectionPeerDic = new HashMap<>();
        _rootEglBase = EglBase.create();
        executor = Executors.newSingleThreadExecutor();
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
        _callState = EnumType.CallState.Connecting;
        if (avEngineKit.mEvent != null) {
            avEngineKit.mEvent.sendJoin(_room);
        }
    }

    // 设置静音
    public boolean muteAudio(boolean b) {
        return false;
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
                avEngineKit.mEvent.sendRefuse(_targetIds, EnumType.RefuseType.Hangup.ordinal());
            }
        });

    }

    // 发送取消信令
    public void sendCancel() {
        executor.execute(() -> {
            if (avEngineKit.mEvent != null) {
                // 取消拨出
                avEngineKit.mEvent.sendCancel(_targetIds);
            }
        });

    }

    // 离开房间
    public void leave() {
        executor.execute(() -> {
            if (avEngineKit.mEvent != null) {
                avEngineKit.mEvent.sendLeave(_room, _myId);
            }
        });
        release();

    }

    private void release() {
        executor.execute(() -> {
            // 释放资源
            ArrayList myCopy;
            myCopy = (ArrayList) _connectionIdArray.clone();
            for (Object Id : myCopy) {
                closePeerConnection((String) Id);
            }
            if (_connectionIdArray != null) {
                _connectionIdArray.clear();
            }
            if (audioSource != null) {
                audioSource.dispose();
                audioSource = null;
            }
            if (videoSource != null) {
                videoSource.dispose();
                videoSource = null;
            }

            if (captureAndroid != null) {
                try {
                    captureAndroid.stopCapture();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                captureAndroid.dispose();
                captureAndroid = null;
            }

            if (surfaceTextureHelper != null) {
                surfaceTextureHelper.dispose();
                surfaceTextureHelper = null;
            }

            if (_factory != null) {
                _factory.dispose();
                _factory = null;
            }
            _callState = EnumType.CallState.Idle;
            if (sessionCallback.get() != null) {
                sessionCallback.get().didCallEndWithReason(null);
            }
        });
    }

    private void closePeerConnection(String connectionId) {
        Peer mPeer = _connectionPeerDic.get(connectionId);
        if (mPeer != null) {
            mPeer.pc.close();
        }
        _connectionPeerDic.remove(connectionId);
        _connectionIdArray.remove(connectionId);
    }
    //------------------------------------receive---------------------------------------------------

    // 加入房间成功
    public void onJoinHome(String myId, String users) {
        executor.execute(() -> {
            _myId = myId;
            if (!TextUtils.isEmpty(users)) {
                String[] split = users.split(",");
                List<String> strings = Arrays.asList(split);
                _connectionIdArray.addAll(strings);
            }
            if (_factory == null) {
                _factory = createConnectionFactory();
            }
            if (_localStream == null) {
                createLocalStream();
            }

            createPeerConnections();
            addStreams();
            createOffers();

            // 如果是发起人，发送邀请
            if (!isComing) {
                avEngineKit.mEvent.sendInvite(_room, _targetIds, _isAudioOnly);
            } else {

                // 关闭响铃
                if (avEngineKit.mEvent != null) {
                    avEngineKit.mEvent.shouldStopRing();
                }

                // 更换界面
                _callState = EnumType.CallState.Connected;
                if (sessionCallback.get() != null) {
                    sessionCallback.get().didChangeState(EnumType.CallState.Connected);
                }
            }

        });
    }

    // 新成员进入
    public void newPeer(String userId) {
        Log.e("dds_test", "newPeer");
        executor.execute(() -> {
            if (_localStream == null) {
                createLocalStream();
            }
            try {
                Peer mPeer = new Peer(userId);
                mPeer.pc.addStream(_localStream);
                _connectionIdArray.add(userId);
                _connectionPeerDic.put(userId, mPeer);
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
            Peer mPeer = _connectionPeerDic.get(socketId);
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
            Peer mPeer = _connectionPeerDic.get(socketId);
            SessionDescription sessionDescription = new SessionDescription(SessionDescription.Type.ANSWER, sdp);
            if (mPeer != null) {
                Log.e("dds_test", "onReceiverAnswer setRemoteDescription");
                mPeer.pc.setRemoteDescription(mPeer, sessionDescription);
            }
        });

    }

    public void onRemoteIceCandidate(String userId, String id, int label, String candidate) {
        executor.execute(() -> {
            Peer peer = _connectionPeerDic.get(userId);
            if (peer != null) {
                IceCandidate iceCandidate = new IceCandidate(id, label, candidate);
                peer.pc.addIceCandidate(iceCandidate);
            }
        });

    }


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
            avEngineKit.mEvent.sendIceCandidate(userId, candidate.sdpMid, candidate.sdpMLineIndex, candidate.sdp);
        }

        @Override
        public void onIceCandidatesRemoved(IceCandidate[] candidates) {
            Log.i(TAG, "onIceCandidatesRemoved:");
        }

        @Override
        public void onAddStream(MediaStream stream) {
            Log.i(TAG, "onAddStream:");
            if (stream.videoTracks.size() > 0) {
                Log.e("dds_test", "onAddStream videoTracks ");
                stream.videoTracks.get(0).setEnabled(true);
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

            pc.setLocalDescription(Peer.this, sdp);

        }

        @Override
        public void onSetSuccess() {
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

    public SurfaceView createRendererView() {
        return null;
    }

    public void setupRemoteVideo(SurfaceView surfaceView) {

    }

    public void setupLocalVideo(SurfaceView surfaceView) {

    }

    //------------------------------------各种初始化---------------------------------------------
    private void createPeerConnections() {
        for (Object str : _connectionIdArray) {
            Log.d("dds_test", "创建Peer:" + str);
            Peer peer = new Peer((String) str);
            _connectionPeerDic.put((String) str, peer);
        }
    }

    // 为所有连接添加流
    private void addStreams() {
        if (_localStream == null) return;
        for (Map.Entry<String, Peer> entry : _connectionPeerDic.entrySet()) {
            entry.getValue().pc.addStream(_localStream);
        }

    }

    // 为所有连接创建offer
    private void createOffers() {
        for (Map.Entry<String, Peer> entry : _connectionPeerDic.entrySet()) {
            _role = Role.Caller;
            Peer mPeer = entry.getValue();
            if (mPeer.pc == null) {
                break;
            }
            mPeer.pc.createOffer(mPeer, offerOrAnswerConstraint());
        }

    }

    public void createLocalStream() {
        _localStream = _factory.createLocalMediaStream("ARDAMS");
        // 音频
        audioSource = _factory.createAudioSource(createAudioConstraints());
        _localAudioTrack = _factory.createAudioTrack(AUDIO_TRACK_ID, audioSource);
        _localStream.addTrack(_localAudioTrack);

        // 视频
        if (!_isAudioOnly) {
            captureAndroid = createVideoCapture();
            surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", _rootEglBase.getEglBaseContext());
            videoSource = _factory.createVideoSource(captureAndroid.isScreencast());
            captureAndroid.initialize(surfaceTextureHelper, _context, videoSource.getCapturerObserver());
            captureAndroid.startCapture(VIDEO_RESOLUTION_WIDTH, VIDEO_RESOLUTION_HEIGHT, FPS);
            _localVideoTrack = _factory.createVideoTrack(VIDEO_TRACK_ID, videoSource);
            _localStream.addTrack(_localVideoTrack);
        }

    }

    public PeerConnectionFactory createConnectionFactory() {
        PeerConnectionFactory.initialize(PeerConnectionFactory
                .InitializationOptions
                .builder(_context)
                .createInitializationOptions());

        final VideoEncoderFactory encoderFactory;
        final VideoDecoderFactory decoderFactory;

        encoderFactory = new DefaultVideoEncoderFactory(
                _rootEglBase.getEglBaseContext(),
                true,
                true);
        decoderFactory = new DefaultVideoDecoderFactory(_rootEglBase.getEglBaseContext());
        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        return PeerConnectionFactory.builder()
                .setOptions(options)
                .setAudioDeviceModule(JavaAudioDeviceModule.builder(_context).createAudioDeviceModule())
                .setVideoEncoderFactory(encoderFactory)
                .setVideoDecoderFactory(decoderFactory)
                .createPeerConnectionFactory();
    }

    private SurfaceTextureHelper surfaceTextureHelper;

    private VideoCapturer createVideoCapture() {
        VideoCapturer videoCapturer;
        if (useCamera2()) {
            videoCapturer = createCameraCapture(new Camera2Enumerator(_context));
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
        return Camera2Enumerator.isSupported(_context);
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
        this._isAudioOnly = _isAudioOnly;
    }

    public boolean isAudioOnly() {
        return _isAudioOnly;
    }


    public void setTargetId(String targetIds) {
        this._targetIds = targetIds;
    }

    public void setContext(Context context) {
        if (context instanceof Application) {
            this._context = context;
        } else {
            this._context = context.getApplicationContext();
        }

    }

    public void setIsComing(boolean isComing) {
        this.isComing = isComing;
    }

    public void setRoom(String _room) {
        this._room = _room;
    }

    public EnumType.CallState getState() {
        return _callState;
    }

    public void setCallState(EnumType.CallState callState) {
        this._callState = callState;
    }

    public boolean isComing() {
        return isComing;
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
