package com.dds.skywebrtc.engine.webrtc;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.media.projection.MediaProjection;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.dds.skywebrtc.EnumType;
import com.dds.skywebrtc.engine.EngineCallback;
import com.dds.skywebrtc.engine.IEngine;
import com.dds.skywebrtc.render.ProxyVideoSink;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RendererCommon;
import org.webrtc.ScreenCapturerAndroid;
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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WebRTCEngine implements IEngine, Peer.IPeerEvent {
    private static final String TAG = "WebRTCEngine";
    private PeerConnectionFactory _factory;
    private EglBase mRootEglBase;
    private MediaStream _localStream;
    private VideoSource videoSource;
    private AudioSource audioSource;
    private AudioTrack _localAudioTrack;
    private VideoCapturer captureAndroid;
    private SurfaceTextureHelper surfaceTextureHelper;

    private ProxyVideoSink localSink;
    private SurfaceViewRenderer localRenderer;


    private static final String VIDEO_TRACK_ID = "ARDAMSv0";
    private static final String AUDIO_TRACK_ID = "ARDAMSa0";
    public static final String VIDEO_CODEC_H264 = "H264";
    private static final int VIDEO_RESOLUTION_WIDTH = 640;
    private static final int VIDEO_RESOLUTION_HEIGHT = 480;
    private static final int FPS = 20;

    // 对话实例列表
    private ConcurrentHashMap<String, Peer> peers = new ConcurrentHashMap<>();
    // 服务器实例列表
    private List<PeerConnection.IceServer> iceServers = new ArrayList<>();

    private EngineCallback mCallback;

    public boolean mIsAudioOnly;
    private Context mContext;
    private AudioManager audioManager;
    private boolean isSpeakerOn = true;

    public WebRTCEngine(boolean mIsAudioOnly, Context mContext) {
        this.mIsAudioOnly = mIsAudioOnly;
        this.mContext = mContext;
        audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        // 初始化ice地址
        initIceServer();
    }


    // -----------------------------------对外方法------------------------------------------
    @Override
    public void init(EngineCallback callback) {
        mCallback = callback;

        if (mRootEglBase == null) {
            mRootEglBase = EglBase.create();
        }
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

            Peer peer = new Peer(_factory, iceServers, id, this);
            peer.setOffer(false);
            // add localStream
            peer.addLocalStream(_localStream);
            // 添加列表
            peers.put(id, peer);
        }
        if (mCallback != null) {
            mCallback.joinRoomSucc();
        }

        if (isHeadphonesPlugged()) {
            toggleHeadset(true);
        } else {
            if (mIsAudioOnly)
                toggleSpeaker(false);
            else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                } else {
                    audioManager.setMode(AudioManager.MODE_IN_CALL);
                }
            }
        }
    }

    @Override
    public void userIn(String userId) {
        // create Peer
        Peer peer = new Peer(_factory, iceServers, userId, this);
        peer.setOffer(true);
        // add localStream
        peer.addLocalStream(_localStream);
        // 添加列表
        peers.put(userId, peer);
        // createOffer
        peer.createOffer();
    }

    @Override
    public void userReject(String userId, int type) {
        //拒绝接听userId应该是没有添加进peers里去不需要remove
//       Peer peer = peers.get(userId);
//        if (peer != null) {
//            peer.close();
//            peers.remove(userId);
//        }
//        if (peers.size() == 0) {

        if (mCallback != null) {
            mCallback.reject(type);
        }
//        }
    }

    @Override
    public void disconnected(String userId, EnumType.CallEndReason reason) {
        if (mCallback != null) {
            mCallback.disconnected(reason);
        }
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
        Log.d("dds_test", "receiveAnswer--" + userId);
        Peer peer = peers.get(userId);
        if (peer != null) {
            SessionDescription sessionDescription = new SessionDescription(SessionDescription.Type.ANSWER, sdp);
            peer.setRemoteDescription(sessionDescription);
        }


    }

    @Override
    public void receiveIceCandidate(String userId, String id, int label, String candidate) {
        Log.d("dds_test", "receiveIceCandidate--" + userId);
        Peer peer = peers.get(userId);
        if (peer != null) {
            IceCandidate iceCandidate = new IceCandidate(id, label, candidate);
            peer.addRemoteIceCandidate(iceCandidate);

        }
    }

    @Override
    public void leaveRoom(String userId) {
        Peer peer = peers.get(userId);
        if (peer != null) {
            peer.close();
            peers.remove(userId);
        }
       Log.d(TAG, "leaveRoom peers.size() = " + peers.size() + "; mCallback = " + mCallback);
        if (peers.size() <= 1) {

            if (mCallback != null) {
                mCallback.exitRoom();
            }
            if (peers.size() == 1) {
                for (Map.Entry<String, Peer> set : peers.entrySet()) {
                    set.getValue().close();
                }
                peers.clear();
            }
        }


    }

    @Override
    public View startPreview(boolean isOverlay) {
        if (mRootEglBase == null) {
            return null;
        }
        localRenderer = new SurfaceViewRenderer(mContext);
        localRenderer.init(mRootEglBase.getEglBaseContext(), null);
        localRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        localRenderer.setMirror(true);
        localRenderer.setZOrderMediaOverlay(isOverlay);

        localSink = new ProxyVideoSink();
        localSink.setTarget(localRenderer);
        if (_localStream.videoTracks.size() > 0) {
            _localStream.videoTracks.get(0).addSink(localSink);
        }
        return localRenderer;
    }

    @Override
    public void stopPreview() {
        if (localSink != null) {
            localSink.setTarget(null);
            localSink = null;
        }
        if (audioSource != null) {
            audioSource.dispose();
            audioSource = null;
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
        // 释放画布
        if (surfaceTextureHelper != null) {
            surfaceTextureHelper.dispose();
            surfaceTextureHelper = null;
        }

        if (videoSource != null) {
            videoSource.dispose();
            videoSource = null;
        }
        if (_localStream != null) {
            _localStream = null;
        }
        if (localRenderer != null) {
            localRenderer.release();
        }


    }

    @Override
    public void startStream() {

    }

    @Override
    public void stopStream() {

    }


    @Override
    public View setupRemoteVideo(String userId, boolean isO) {
        if (TextUtils.isEmpty(userId)) {
            Log.e(TAG, "setupRemoteVideo userId is null ");
            return null;
        }
        Peer peer = peers.get(userId);
        if (peer == null) return null;

        if (peer.renderer == null) {
            peer.createRender(mRootEglBase, mContext, isO);
        }

        return peer.renderer;

    }

    @Override
    public void stopRemoteVideo() {

    }

    private boolean isSwitch = false; // 是否正在切换摄像头

    @Override
    public void switchCamera() {
        if (isSwitch) return;
        isSwitch = true;
        if (captureAndroid == null) return;
        if (captureAndroid instanceof CameraVideoCapturer) {
            CameraVideoCapturer cameraVideoCapturer = (CameraVideoCapturer) captureAndroid;
            try {
                cameraVideoCapturer.switchCamera(new CameraVideoCapturer.CameraSwitchHandler() {
                    @Override
                    public void onCameraSwitchDone(boolean isFrontCamera) {
                        isSwitch = false;
                    }

                    @Override
                    public void onCameraSwitchError(String errorDescription) {
                        isSwitch = false;
                    }
                });
            } catch (Exception e) {
                isSwitch = false;
            }
        } else {
            Log.d(TAG, "Will not switch camera, video caputurer is not a camera");
        }
    }

    @Override
    public boolean muteAudio(boolean enable) {
        if (_localAudioTrack != null) {
            _localAudioTrack.setEnabled(!enable);
            return true;
        }
        return false;
    }

    @Override
    public boolean toggleSpeaker(boolean enable) {
        if (audioManager != null) {
            isSpeakerOn = enable;
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            if (enable) {
                audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                        audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL),
                        AudioManager.FX_KEY_CLICK);
                audioManager.setSpeakerphoneOn(true);
            } else {
                //5.0以上
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    //设置mode
                    audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                } else {
                    //设置mode
                    audioManager.setMode(AudioManager.MODE_IN_CALL);
                }
                //设置音量，解决有些机型切换后没声音或者声音突然变大的问题
                audioManager.setStreamVolume(
                        AudioManager.STREAM_VOICE_CALL,
                        audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL),
                        AudioManager.FX_KEY_CLICK
                );
                audioManager.setSpeakerphoneOn(false);
            }
            return true;
        }
        return false;

    }

    @Override
    public boolean toggleHeadset(boolean isHeadset) {
        if (audioManager != null) {
            if (isHeadset) {
                //5.0以上
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    //设置mode
                    audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                } else {
                    //设置mode
                    audioManager.setMode(AudioManager.MODE_IN_CALL);
                }
                audioManager.setSpeakerphoneOn(false);
            } else {
                if (mIsAudioOnly) {
                    toggleSpeaker(isSpeakerOn);
                }
            }
        }
        return false;
    }

    private boolean isHeadphonesPlugged() {
        if (audioManager == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AudioDeviceInfo[] audioDevices = audioManager.getDevices(AudioManager.GET_DEVICES_ALL);
            for (AudioDeviceInfo deviceInfo : audioDevices) {
                if (deviceInfo.getType() == AudioDeviceInfo.TYPE_WIRED_HEADPHONES
                        || deviceInfo.getType() == AudioDeviceInfo.TYPE_WIRED_HEADSET) {
                    return true;
                }
            }
            return false;
        } else {
            return audioManager.isWiredHeadsetOn();
        }
    }

    @Override
    public void release() {
        if (audioManager != null) {
            audioManager.setMode(AudioManager.MODE_NORMAL);
        }
        // 清空peer
        if (peers != null) {
            for (Peer peer : peers.values()) {
                peer.close();
            }
            peers.clear();
        }


        // 停止预览
        stopPreview();

        if (_factory != null) {
            _factory.dispose();
            _factory = null;
        }

        if (mRootEglBase != null) {
            mRootEglBase.release();
            mRootEglBase = null;
        }


    }

    // -----------------------------其他方法--------------------------------

    private void initIceServer() {
        // 初始化一些stun和turn的地址
        PeerConnection.IceServer var1 = PeerConnection.IceServer.builder("stun:stun.l.google.com:19302")
                .createIceServer();
        iceServers.add(var1);

        PeerConnection.IceServer var11 = PeerConnection.IceServer.builder("stun:42.192.40.58:3478?transport=udp")
                .createIceServer();
        PeerConnection.IceServer var12 = PeerConnection.IceServer.builder("turn:42.192.40.58:3478?transport=udp")
                .setUsername("ddssingsong")
                .setPassword("123456")
                .createIceServer();
        PeerConnection.IceServer var13 = PeerConnection.IceServer.builder("turn:42.192.40.58:3478?transport=tcp")
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


            VideoTrack _localVideoTrack = _factory.createVideoTrack(VIDEO_TRACK_ID, videoSource);
            _localStream.addTrack(_localVideoTrack);
        }

    }


    // 是否使用录屏
    private boolean screencaptureEnabled = false;

    /**
     * 创建媒体方式
     *
     * @return VideoCapturer
     */
    private VideoCapturer createVideoCapture() {
        VideoCapturer videoCapturer;


        if (screencaptureEnabled) {
            return createScreenCapturer();
        }

        if (Camera2Enumerator.isSupported(mContext)) {
            videoCapturer = createCameraCapture(new Camera2Enumerator(mContext));
        } else {
            videoCapturer = createCameraCapture(new Camera1Enumerator(true));
        }
        return videoCapturer;
    }

    /**
     * 创建相机媒体流
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


    private static Intent mediaProjectionPermissionResultData;
    private static int mediaProjectionPermissionResultCode;

    @TargetApi(21)
    private VideoCapturer createScreenCapturer() {
        if (mediaProjectionPermissionResultCode != Activity.RESULT_OK) {
            return null;
        }
        return new ScreenCapturerAndroid(
                mediaProjectionPermissionResultData, new MediaProjection.Callback() {
            @Override
            public void onStop() {
                Log.e(TAG, "User revoked permission to capture the screen.");
            }
        });
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
                new MediaConstraints.KeyValuePair(AUDIO_ECHO_CANCELLATION_CONSTRAINT, "true"));
        audioConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair(AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT, "false"));
        audioConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair(AUDIO_HIGH_PASS_FILTER_CONSTRAINT, "false"));
        audioConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair(AUDIO_NOISE_SUPPRESSION_CONSTRAINT, "true"));
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
            mCallback.onRemoteStream(userId);
        }
    }

    @Override
    public void onRemoveStream(String userId, MediaStream stream) {
        leaveRoom(userId);
    }

    @Override
    public void onDisconnected(String userId) {
        if (mCallback != null) {
           Log.d(TAG, "onDisconnected mCallback != null");
            mCallback.onDisconnected(userId);
        } else {
           Log.d(TAG, "onDisconnected mCallback == null");
        }
    }

}
