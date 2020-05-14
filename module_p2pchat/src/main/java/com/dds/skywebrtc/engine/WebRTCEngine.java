package com.dds.skywebrtc.engine;

import android.content.Context;
import android.view.View;

import com.dds.skywebrtc.render.ProxyVideoSink;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RendererCommon;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoDecoderFactory;
import org.webrtc.VideoEncoderFactory;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;
import org.webrtc.audio.AudioDeviceModule;
import org.webrtc.audio.JavaAudioDeviceModule;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WebRTCEngine implements IEngine, Peer.IPeerEvent {
    private PeerConnectionFactory _factory;
    private EglBase mRootEglBase;
    private MediaStream _localStream;
    private VideoSource videoSource;
    private AudioSource audioSource;
    private VideoTrack _localVideoTrack;
    private AudioTrack _localAudioTrack;
    private VideoCapturer captureAndroid;
    private SurfaceTextureHelper surfaceTextureHelper;

    private static final String VIDEO_TRACK_ID = "ARDAMSv0";
    private static final String AUDIO_TRACK_ID = "ARDAMSa0";
    public static final String VIDEO_CODEC_H264 = "H264";
    private static final int VIDEO_RESOLUTION_WIDTH = 1280;
    private static final int VIDEO_RESOLUTION_HEIGHT = 720;
    private static final int FPS = 20;

    // 对话实例列表
    private ConcurrentHashMap<String, Peer> peers = new ConcurrentHashMap<>();
    // 服务器实例列表
    private List<PeerConnection.IceServer> iceServers = new ArrayList<>();
    public ExecutorService executor;

    private EngineCallback mCallback;

    public boolean mIsAudioOnly;
    private Context mContext;

    public WebRTCEngine(boolean mIsAudioOnly, Context mContext) {
        this.mIsAudioOnly = mIsAudioOnly;
        this.mContext = mContext;

        mRootEglBase = EglBase.create();
        // 初始化ice地址
        initIceServer();

        executor = Executors.newSingleThreadExecutor();
    }


    // -----------------------------------对外方法------------------------------------------
    @Override
    public void init(EngineCallback callback) {
        mCallback = callback;
        if (_factory == null) {
            _factory = createConnectionFactory();
        }
        if (_localStream == null) {
            createLocalStream();
        }
    }

    @Override
    public void joinRoom(List<String> userIds) {
        for (String id : userIds) {
            // create Peer
            Peer peer = new Peer(this,_factory, iceServers, id, this);
            peer.setOffer(false);
            // add localStream
            peer.addLocalStream(_localStream);
            // 添加列表
            peers.put(id, peer);
        }
        if (mCallback != null) {
            mCallback.joinRoomSucc();
        }


    }

    @Override
    public void userIn(String userId) {
        // create Peer
        Peer peer = new Peer(this,_factory, iceServers, userId, this);
        peer.setOffer(true);
        // add localStream
        peer.addLocalStream(_localStream);
        // 添加列表
        peers.put(userId, peer);
        // createOffer
        peer.createOffer();
    }

    @Override
    public void userReject(String userId) {

    }

    @Override
    public void receiveOffer(String userId, String description) {
        Peer peer = peers.get(userId);
        if (peer != null) {
            SessionDescription sdp = new SessionDescription(SessionDescription.Type.OFFER, description);
            peer.setOffer(false);
            peer.setRemoteDescription(sdp);
            peer.createAnswer();
        }


    }

    @Override
    public void receiveAnswer(String userId, String sdp) {
        Peer peer = peers.get(userId);
        if (peer != null) {
            SessionDescription sessionDescription = new SessionDescription(SessionDescription.Type.ANSWER, sdp);
            peer.setRemoteDescription(sessionDescription);
        }


    }

    @Override
    public void receiveIceCandidate(String userId, String id, int label, String candidate) {
        Peer peer = peers.get(userId);
        if (peer != null) {
            IceCandidate iceCandidate = new IceCandidate(id, label, candidate);
            peer.addRemoteIceCandidate(iceCandidate);

        }
    }

    @Override
    public void leaveRoom() {

    }

    @Override
    public View startPreview(boolean isOverlay) {
        SurfaceViewRenderer renderer = new SurfaceViewRenderer(mContext);
        renderer.init(mRootEglBase.getEglBaseContext(), null);
        renderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        renderer.setMirror(true);
        renderer.setZOrderMediaOverlay(isOverlay);

        ProxyVideoSink sink = new ProxyVideoSink();
        sink.setTarget(renderer);
        if (_localStream.videoTracks.size() > 0) {
            _localStream.videoTracks.get(0).addSink(sink);
        }
        return renderer;
    }

    @Override
    public void stopPreview() {

    }

    @Override
    public void startStream() {

    }

    @Override
    public void stopStream() {

    }


    @Override
    public View setupRemoteVideo(Context context, String userId, boolean isO) {
        Peer peer = peers.get(userId);
        if (peer == null) return null;
        SurfaceViewRenderer renderer = new SurfaceViewRenderer(context);
        renderer.init(mRootEglBase.getEglBaseContext(), null);
        renderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        renderer.setMirror(true);
        renderer.setZOrderMediaOverlay(isO);

        ProxyVideoSink sink = new ProxyVideoSink();
        sink.setTarget(renderer);
        if (peer._remoteStream != null && peer._remoteStream.videoTracks.size() > 0) {
            peer._remoteStream.videoTracks.get(0).addSink(sink);
        }
        return renderer;

    }

    @Override
    public void stopRemoteVideo() {

    }

    @Override
    public void release() {
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

        // 本地流释放
        _localStream.dispose();

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
    }


    // -----------------------------其他方法--------------------------------

    private void initIceServer() {
        // 初始化一些stun和turn的地址
        PeerConnection.IceServer var1 = PeerConnection.IceServer.builder("stun:stun.l.google.com:19302")
                .createIceServer();
        iceServers.add(var1);


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
        iceServers.add(var11);
        iceServers.add(var12);
        iceServers.add(var13);
    }

    /**
     * 构造PeerConnectionFactory
     *
     * @return PeerConnectionFactory
     */
    public PeerConnectionFactory createConnectionFactory() {

        // 1. 初始化的方法，必须在开始之前调用
        PeerConnectionFactory.initialize(PeerConnectionFactory
                .InitializationOptions
                .builder(mContext)
                .createInitializationOptions());

        // 2. 设置编解码方式：默认方法
        final VideoEncoderFactory encoderFactory;
        final VideoDecoderFactory decoderFactory;

        encoderFactory = new DefaultVideoEncoderFactory(
                mRootEglBase.getEglBaseContext(),
                true,
                true);
        decoderFactory = new DefaultVideoDecoderFactory(mRootEglBase.getEglBaseContext());

        // 构造Factory
        AudioDeviceModule audioDeviceModule = JavaAudioDeviceModule.builder(mContext).createAudioDeviceModule();
        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        return PeerConnectionFactory.builder()
                .setOptions(options)
                .setAudioDeviceModule(audioDeviceModule)
                .setVideoEncoderFactory(encoderFactory)
                .setVideoDecoderFactory(decoderFactory)
                .createPeerConnectionFactory();
    }

    /**
     * 创建本地流
     */
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

    /**
     * 创建媒体方式
     *
     * @return VideoCapturer
     */
    private VideoCapturer createVideoCapture() {
        VideoCapturer videoCapturer;
        if (Camera2Enumerator.isSupported(mContext)) {
            videoCapturer = createCameraCapture(new Camera2Enumerator(mContext));
        } else {
            videoCapturer = createCameraCapture(new Camera1Enumerator(true));
        }
        return videoCapturer;
    }

    /**
     * 创建相机媒体流
     *
     * @param enumerator
     * @return VideoCapturer
     */
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


    //**************************************各种约束******************************************/
    private static final String AUDIO_ECHO_CANCELLATION_CONSTRAINT = "googEchoCancellation";
    private static final String AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT = "googAutoGainControl";
    private static final String AUDIO_HIGH_PASS_FILTER_CONSTRAINT = "googHighpassFilter";
    private static final String AUDIO_NOISE_SUPPRESSION_CONSTRAINT = "googNoiseSuppression";

    // 配置音频参数
    private MediaConstraints createAudioConstraints() {
        MediaConstraints audioConstraints = new MediaConstraints();
        audioConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair(AUDIO_ECHO_CANCELLATION_CONSTRAINT, "false"));
        audioConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair(AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT, "false"));
        audioConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair(AUDIO_HIGH_PASS_FILTER_CONSTRAINT, "false"));
        audioConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair(AUDIO_NOISE_SUPPRESSION_CONSTRAINT, "false"));
        return audioConstraints;
    }

    //------------------------------------回调---------------------------------------------
    @Override
    public void onSendIceCandidate(String userId, IceCandidate candidate) {
        if (mCallback != null) {
            mCallback.onSendIceCandidate(userId, candidate);
        }

    }

    @Override
    public void onSendOffer(String userId, SessionDescription description) {
        if (mCallback != null) {
            mCallback.onSendOffer(userId, description);
        }
    }

    @Override
    public void onSendAnswer(String userId, SessionDescription description) {
        if (mCallback != null) {
            mCallback.onSendAnswer(userId, description);
        }
    }

    @Override
    public void onRemoteStream(String userId, MediaStream stream) {
        if (mCallback != null) {
            mCallback.onRemoteStream(userId,stream);
        }
    }


}
