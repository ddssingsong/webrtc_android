package com.dds.skywebrtc;

import android.content.Context;
import android.util.Log;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoDecoderFactory;
import org.webrtc.VideoEncoderFactory;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;
import org.webrtc.audio.JavaAudioDeviceModule;

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
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    public Context _context;
    public EglBase _rootEglBase;
    public PeerConnectionFactory _factory;
    private MediaStream _localStream;
    private VideoTrack _localVideoTrack;
    private AudioTrack _localAudioTrack;
    private VideoCapturer captureAndroid;
    private VideoSource videoSource;
    private AudioSource audioSource;
    public static final String VIDEO_TRACK_ID = "ARDAMSv0";
    public static final String AUDIO_TRACK_ID = "ARDAMSa0";
    public static final int VIDEO_RESOLUTION_WIDTH = 320;
    public static final int VIDEO_RESOLUTION_HEIGHT = 240;
    public static final int FPS = 10;
    public boolean _isAudioOnly;
    public String _targetId;
    public String _room;

    private static AVEngineKit avEngineKit;
    private CallSession currentCallSession;
    public EnumType.CallState _callState = EnumType.CallState.Idle;
    public ISendEvent _iSocketEvent;


    public static AVEngineKit Instance() {
        AVEngineKit var0;
        if ((var0 = avEngineKit) != null) {
            return var0;
        } else {
            throw new NotInitializedExecption();
        }
    }


    public static void init(ISendEvent iSocketEvent) {
        if (avEngineKit == null) {
            avEngineKit = new AVEngineKit();
            avEngineKit._iSocketEvent = iSocketEvent;
        }
    }


    public boolean receiveCall(Context context, String room, final String inviteId, final boolean audioOnly) {
        if (avEngineKit == null) {
            Log.e(TAG, "receiveCall error,init is not set");
            return false;
        }
        // 忙线中
        if (currentCallSession != null && _callState != EnumType.CallState.Idle) {
            if (_iSocketEvent != null) {
                _iSocketEvent.sendRefuse(inviteId, EnumType.RefuseType.Busy.ordinal());
                currentCallSession.callEnd(EnumType.CallEndReason.Busy);
            }
            return false;
        } else {
            _context = context;
            // audioOnly
            _isAudioOnly = audioOnly;
            // state --> Outgoing
            _callState = EnumType.CallState.Outgoing;
            _room = room;
            _targetId = inviteId;
            // new Session
            currentCallSession = new CallSession(avEngineKit);

            createFactoryAndLocalStream();

            return true;

        }


    }

    // 发起会话
    public void startCall(Context context, final String room, final int roomSize,
                          final String targetId, final boolean isAudio) {
        if (avEngineKit == null) {
            Log.e(TAG, "receiveCall error,init is not set");
            return;
        }
        if (currentCallSession != null && _callState != EnumType.CallState.Idle) {
            Log.e(TAG, "startCall error,currentCallSession is exist");
            return;
        }

        _context = context;
        // audioOnly
        _isAudioOnly = isAudio;
        // state --> Outgoing
        _callState = EnumType.CallState.Outgoing;
        _room = room;
        _targetId = targetId;

        createFactoryAndLocalStream();

        executor.execute(() -> {
            if (_iSocketEvent != null) {
                // 创建房间
                _iSocketEvent.createRoom(room, roomSize);
                // 发送邀请
                _iSocketEvent.sendInvite(room, targetId, isAudio);
            }


        });
    }

    private void createFactoryAndLocalStream() {
        executor.execute(() -> {
            // 创建factory
            if (_factory == null) {
                _factory = createConnectionFactory();
            }
            // 创建本地流
            if (_localStream == null) {
                createLocalStream();
            }
        });
    }

    // 预览视频
    public void startPreview() {
        if (_isAudioOnly) return;
        executor.execute(() -> {
            //创建需要传入设备的名称
            captureAndroid = createVideoCapture();
            // 视频
            surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", _rootEglBase.getEglBaseContext());
            videoSource = _factory.createVideoSource(captureAndroid.isScreencast());
            captureAndroid.initialize(surfaceTextureHelper, _context, videoSource.getCapturerObserver());
            captureAndroid.startCapture(VIDEO_RESOLUTION_WIDTH, VIDEO_RESOLUTION_HEIGHT, FPS);
            _localVideoTrack = _factory.createVideoTrack(VIDEO_TRACK_ID, videoSource);
            _localStream.addTrack(_localVideoTrack);


        });

    }


    private SurfaceTextureHelper surfaceTextureHelper;

    private void createLocalStream() {
        _localStream = _factory.createLocalMediaStream("ARDAMS");
        // 音频
        audioSource = _factory.createAudioSource(createAudioConstraints());
        _localAudioTrack = _factory.createAudioTrack(AUDIO_TRACK_ID, audioSource);
        _localStream.addTrack(_localAudioTrack);
    }

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

    private PeerConnectionFactory createConnectionFactory() {
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


}
