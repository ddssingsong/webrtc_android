package com.dds.skywebrtc;

import android.content.Context;
import android.util.Log;

import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.PeerConnection;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.VideoCapturer;

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
    public final ExecutorService executor = Executors.newSingleThreadExecutor();
    public Context _context;

    public static AVEngineKit avEngineKit;
    private CallSession currentCallSession;
    public IBusinessEvent _iSocketEvent;


    public static AVEngineKit Instance() {
        AVEngineKit var0;
        if ((var0 = avEngineKit) != null) {
            return var0;
        } else {
            throw new NotInitializedExecption();
        }
    }


    public static void init(IBusinessEvent iSocketEvent) {
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
        if (currentCallSession != null && currentCallSession.getCallState() != EnumType.CallState.Idle) {
            if (_iSocketEvent != null) {
                // 发送->忙线中...
                _iSocketEvent.sendRefuse(inviteId, EnumType.RefuseType.Busy.ordinal());
            }
            return false;
        } else {
            _context = context;
            // new Session
            currentCallSession = new CallSession(avEngineKit);
            currentCallSession.setIsAudioOnly(audioOnly);
            currentCallSession.setRoom(room);
            currentCallSession.setTargetId(inviteId);
            currentCallSession.createFactoryAndLocalStream();
            return true;

        }


    }

    // 发起会话
    public void startCall(Context context, final String room, final int roomSize,
                          final String targetId, final boolean audioOnly) {
        if (avEngineKit == null) {
            Log.e(TAG, "receiveCall error,init is not set");
            return;
        }
        if (currentCallSession != null && currentCallSession.getCallState() != EnumType.CallState.Idle) {
            Log.e(TAG, "startCall error,currentCallSession is exist");
            return;
        }

        _context = context;
        // new Session
        currentCallSession = new CallSession(avEngineKit);
        currentCallSession.setIsAudioOnly(audioOnly);
        currentCallSession.setRoom(room);
        currentCallSession.setTargetId(targetId);
        currentCallSession.createFactoryAndLocalStream();

        executor.execute(() -> {
            if (_iSocketEvent != null) {
                // 创建房间
                _iSocketEvent.createRoom(room, roomSize);
                // 发送邀请
                _iSocketEvent.sendInvite(room, targetId, audioOnly);
            }


        });
    }


    // 预览视频
    public void startPreview() {
//        if (_isAudioOnly) return;
//        executor.execute(() -> {
//            //创建需要传入设备的名称
//            captureAndroid = createVideoCapture();
//            // 视频
//            surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", _rootEglBase.getEglBaseContext());
//            videoSource = _factory.createVideoSource(captureAndroid.isScreencast());
//            captureAndroid.initialize(surfaceTextureHelper, _context, videoSource.getCapturerObserver());
//            captureAndroid.startCapture(VIDEO_RESOLUTION_WIDTH, VIDEO_RESOLUTION_HEIGHT, FPS);
//            _localVideoTrack = _factory.createVideoTrack(VIDEO_TRACK_ID, videoSource);
//            _localStream.addTrack(_localVideoTrack);
//
//
//        });

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


}
