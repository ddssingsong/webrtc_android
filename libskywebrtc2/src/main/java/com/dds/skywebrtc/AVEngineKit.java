package com.dds.skywebrtc;

import android.content.Context;
import android.util.Log;

import org.webrtc.PeerConnection;

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


    // 发起会话
    public boolean startCall(Context context,
                             final String room, final int roomSize,
                             final String targetId, final boolean audioOnly,
                             boolean isComing) {
        if (avEngineKit == null) {
            Log.e(TAG, "receiveCall error,init is not set");
            return false;
        }
        if (currentCallSession != null &&
                currentCallSession.getState() != EnumType.CallState.Idle) {
            if (isComing) {
                // 来电忙线中
                if (_iSocketEvent != null) {
                    // 发送->忙线中...
                    Log.e(TAG, "startCall error,currentCallSession is exist," +
                            "start sendRefuse!");
                    _iSocketEvent.sendRefuse(targetId, EnumType.RefuseType.Busy.ordinal());
                }
            } else {
                Log.e(TAG, "startCall error,currentCallSession is exist");
            }
            return false;
        }
        // new Session
        currentCallSession = new CallSession(avEngineKit);
        currentCallSession.setIsAudioOnly(audioOnly);
        currentCallSession.setRoom(room);
        currentCallSession.setTargetId(targetId);
        currentCallSession.setContext(context);
        currentCallSession.setIsComing(isComing);
        currentCallSession.setCallState(isComing ? EnumType.CallState.Incoming : EnumType.CallState.Outgoing);
        if (isComing) {
            // 开始响铃
            if (_iSocketEvent != null) {
                _iSocketEvent.shouldStartRing(true);
            }
            // 发送响铃回复
            executor.execute(() -> {
                if (_iSocketEvent != null) {
                    _iSocketEvent.sendRingBack(targetId);
                }

            });
        } else {
            executor.execute(() -> {
                if (_iSocketEvent != null) {
                    // 创建房间
                    _iSocketEvent.createRoom(room, roomSize);
                }
            });

        }
        return true;

    }



    public void endCall() {
        if (currentCallSession.isComing) {
            if (currentCallSession.getState() == EnumType.CallState.Incoming) {
                // 接收到邀请，还没同意，发送拒绝
                if (_iSocketEvent != null) {
                    _iSocketEvent.sendRefuse(currentCallSession._targetId, EnumType.RefuseType.Hangup.ordinal());
                }
            } else {
                // 已经接通，挂断电话
                if (_iSocketEvent != null) {

                }
            }


        } else {
            if (currentCallSession.getState() == EnumType.CallState.Outgoing) {
                if (_iSocketEvent != null) {
                    // 取消拨出
                    _iSocketEvent.sendCancel(currentCallSession._targetId);
                }
            } else {
                // 已经接通，挂断电话

            }
        }
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
