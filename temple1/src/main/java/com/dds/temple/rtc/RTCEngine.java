package com.dds.temple.rtc;

import android.content.Context;
import android.util.Log;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.MediaConstraints;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoDecoderFactory;
import org.webrtc.VideoEncoderFactory;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;
import org.webrtc.audio.AudioDeviceModule;
import org.webrtc.audio.JavaAudioDeviceModule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RTCEngine {
    private static final String TAG = "RTCEngine";
    private EglBase mRootEglBase;
    private PeerConnectionFactory mConnectionFactory;
    // video
    private VideoTrack mVideoTrack;
    private VideoSource mVideoSource;
    private VideoCapturer mVideoCapturer;
    private SurfaceTextureHelper mSurfaceTextureHelper;
    // audio
    private AudioTrack mAudioTrack;
    private AudioSource mAudioSource;

    private RTCPeer mPeer;
    // config
    private static final String VIDEO_TRACK_ID = "ARDAMSv0";
    private static final String AUDIO_TRACK_ID = "ARDAMSa0";
    private static final int VIDEO_RESOLUTION_WIDTH = 720;
    private static final int VIDEO_RESOLUTION_HEIGHT = 1280;
    private static final int FPS = 30;
    private static final String AUDIO_ECHO_CANCELLATION_CONSTRAINT = "googEchoCancellation";
    private static final String AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT = "googAutoGainControl";
    private static final String AUDIO_HIGH_PASS_FILTER_CONSTRAINT = "googHighpassFilter";
    private static final String AUDIO_NOISE_SUPPRESSION_CONSTRAINT = "googNoiseSuppression";

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public RTCEngine(Context context, EglBase eglBase) {
        mRootEglBase = eglBase;
        executor.execute(() -> {
            mConnectionFactory = createConnectionFactory(context);
            mVideoTrack = createVideoTrack(context);
            mAudioTrack = createAudioTrack();
        });

    }

    private PeerConnectionFactory createConnectionFactory(Context context) {
        // 1. init factory
        PeerConnectionFactory.InitializationOptions.Builder builder = PeerConnectionFactory.InitializationOptions.builder(context);
        PeerConnectionFactory.InitializationOptions initializationOptions = builder.createInitializationOptions();
        PeerConnectionFactory.initialize(initializationOptions);

        // 2. video encode decode method
        final VideoEncoderFactory encoderFactory = new DefaultVideoEncoderFactory(mRootEglBase.getEglBaseContext(), true, true);
        final VideoDecoderFactory decoderFactory = new DefaultVideoDecoderFactory(mRootEglBase.getEglBaseContext());

        // 3. audio deal
        AudioDeviceModule audioDeviceModule = JavaAudioDeviceModule.builder(context).createAudioDeviceModule();

        // 4. create connectFactory
        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        PeerConnectionFactory.Builder builder1 = PeerConnectionFactory.builder();
        builder1.setOptions(options)
                .setAudioDeviceModule(audioDeviceModule)
                .setVideoEncoderFactory(encoderFactory)
                .setVideoDecoderFactory(decoderFactory);
        return builder1.createPeerConnectionFactory();
    }

    private VideoTrack createVideoTrack(Context context) {

        // 1. create video source
        mVideoSource = mConnectionFactory.createVideoSource(false);
        // 2. create video capture
        mVideoCapturer = createVideoCapture(context);
        // 3. start capture
        mSurfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", mRootEglBase.getEglBaseContext());
        mVideoCapturer.initialize(mSurfaceTextureHelper, context, mVideoSource.getCapturerObserver());
        mVideoCapturer.startCapture(VIDEO_RESOLUTION_WIDTH, VIDEO_RESOLUTION_HEIGHT, FPS);
        // 4. create videoTrack
        VideoTrack videoTrack = mConnectionFactory.createVideoTrack(VIDEO_TRACK_ID, mVideoSource);
        videoTrack.setEnabled(true);
        return videoTrack;
    }

    private VideoCapturer createVideoCapture(Context context) {
        VideoCapturer videoCapturer = createCameraCapture(new Camera2Enumerator(context));
        Log.d(TAG, "createVideoCapture: " + videoCapturer);
        // You can implement various captures here, such as screen recording and file recording
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

    private AudioTrack createAudioTrack() {
        mAudioSource = mConnectionFactory.createAudioSource(createAudioConstraints());
        mAudioTrack = mConnectionFactory.createAudioTrack(AUDIO_TRACK_ID, mAudioSource);
        mAudioTrack.setEnabled(true);
        return mAudioTrack;
    }

    private MediaConstraints createAudioConstraints() {
        MediaConstraints audioConstraints = new MediaConstraints();
        audioConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair(AUDIO_ECHO_CANCELLATION_CONSTRAINT, "true"));
        audioConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair(AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT, "true"));
        audioConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair(AUDIO_HIGH_PASS_FILTER_CONSTRAINT, "true"));
        audioConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair(AUDIO_NOISE_SUPPRESSION_CONSTRAINT, "true"));
        return audioConstraints;
    }

    public void createPeerConnection(RTCPeer.PeerConnectionEvents events) {
        executor.execute(() -> {
            mPeer = new RTCPeer(mConnectionFactory,executor,events);
            List<String> mediaStreamLabels = Collections.singletonList("ARDAMS");
            if (mVideoTrack != null) {
                mPeer.addVideoTrack(mVideoTrack, mediaStreamLabels);
            }
            if (mAudioTrack != null) {
                mPeer.addVideoTrack(mAudioTrack, mediaStreamLabels);
            }
        });
    }

    public void createOffer() {
        if (mPeer != null) {
            mPeer.createOffer();
        }
    }

    public void createAnswer() {
        if (mPeer != null) {
            mPeer.createAnswer();
        }
    }


}
