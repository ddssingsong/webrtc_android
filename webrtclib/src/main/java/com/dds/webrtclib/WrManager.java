package com.dds.webrtclib;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;

import com.dds.webrtclib.ws.EnumMsg;
import com.dds.webrtclib.ws.ISignalingEvents;
import com.dds.webrtclib.ws.MxWebSocket;

import org.webrtc.IceCandidate;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import static android.media.AudioManager.MODE_RINGTONE;
import static android.media.AudioManager.STREAM_RING;

/**
 * Created by dds on 2019/1/11.
 * android_shuai@163.com
 */
public class WrManager implements ISignalingEvents {

    private final static String TAG = WrManager.class.getSimpleName();
    private MxWebSocket webSocket;
    private EnumMsg.Direction _direction;
    private boolean _videoEnable;
    private String _sessionId;
    private String _myUserId;
    private String _ids;
    private Context _context;
    private String _room;


    private WebRTCHelper webRTCHelper;
    private AudioManager mAudioManager;
    private MediaPlayer mRingerPlayer;
    private Vibrator mVibrator;

    public static WrManager getInstance() {
        return Holder.wrManager;
    }

    private static class Holder {
        private static WrManager wrManager = new WrManager();
    }

    public void setCallback(IWebrtcViewCallback callback) {
        if (webRTCHelper != null) {
            webRTCHelper.setViewCallback(callback);
        }
    }


    public void init(Context context, String ids, String myUserId, String sessionId,
                     boolean videoEnable, EnumMsg.Direction direction, String room) {
        _context = context;
        _myUserId = myUserId;
        _ids = ids;
        _sessionId = sessionId;
        _videoEnable = videoEnable;
        _direction = direction;
        _room = room;
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    public void connectSocket(String wss) {
        if (this.webSocket == null) {
            this.webSocket = new MxWebSocket(this);
            this.webRTCHelper = new WebRTCHelper(_context, this.webSocket);
            webSocket.connect(wss);
        }
    }

    public void joinRoom() {
        if (webSocket != null) {
            webSocket.joinRoom(_room);
        }

    }

    // 接听
    public void acceptCall(Activity activity) {
        stopRinging();
        ChatSingleActivity.openActivity(activity, _videoEnable, true);
    }

    // 拒绝接听
    public void refuseCall() {
        stopRinging();
        if (webSocket != null) {
            webSocket.decline(EnumMsg.Decline.Refuse);
        }

    }

    // 调整摄像头前置后置
    public void switchCamera() {
        if (webRTCHelper != null) {
            webRTCHelper.switchCamera();
        }


    }

    // 设置自己静音
    public void toggleMute(boolean enable) {
        if (webRTCHelper != null) {
            webRTCHelper.toggleMute(enable);
        }

    }

    //扬声器
    public void toggleSpeaker(boolean enable) {
        if (mAudioManager != null) {
            mAudioManager.setSpeakerphoneOn(enable);
        }
    }

    // 退出房间
    public void exitRoom() {
        if (webRTCHelper != null) {
            webRTCHelper.exitRoom();
        }

    }

    //==============================================================================================
    @Override
    public void onWebSocketOpen() {
        // webSocket打开成功
        webSocket.login(_sessionId);
    }

    @Override
    public void onLoginSuccess(ArrayList<MyIceServer> iceServers, String socketId) {
        if (webRTCHelper != null) {
            webRTCHelper.onLoginSuccess(iceServers, _videoEnable);
        }
        if (_direction == EnumMsg.Direction.Outgoing) {
            //进入通话界面
            webSocket.createRoom(_ids, _videoEnable);

        } else {
            // 发送回执
            webSocket.sendAck(_ids);
        }

    }

    @Override
    public void onCreateRoomSuccess(String room) {
        _room = room;
        // 进入房间
        ChatSingleActivity.openActivity(_context, _videoEnable, false);

    }

    @Override
    public void onJoinToRoom(ArrayList<String> connections, String myId) {
        if (webRTCHelper != null) {
            webRTCHelper.onJoinToRoom(connections, myId);
        }
    }

    @Override
    public void onUserAck(String userId) {
        if (webSocket != null) {
            webSocket.sendInvite(userId);
        }

    }

    @Override
    public void onUserInvite(String socketId) {
        //显示来电界面  开始响铃，
        IncomingActivity.openActivity(_context);
        startRinging();


    }

    @Override
    public void onRemoteJoinToRoom(String socketId) {
        if (webRTCHelper != null) {
            webRTCHelper.onRemoteJoinToRoom(socketId);
        }

    }

    @Override
    public void onReceiveOffer(String socketId, String sdp) {
        if (webRTCHelper != null) {
            webRTCHelper.onReceiveOffer(socketId, sdp);
        }
    }

    @Override
    public void onReceiverAnswer(String socketId, String sdp) {
        if (webRTCHelper != null) {
            webRTCHelper.onReceiverAnswer(socketId, sdp);
        }
    }

    @Override
    public void onRemoteIceCandidate(String socketId, IceCandidate iceCandidate) {
        if (webRTCHelper != null) {
            webRTCHelper.onRemoteIceCandidate(socketId, iceCandidate);
        }
    }

    @Override
    public void onRemoteOutRoom(String socketId) {
        if (webRTCHelper != null) {
            webRTCHelper.onRemoteOutRoom(socketId);
        }
    }

    @Override
    public void onDecline(EnumMsg.Decline decline) {
        if (webRTCHelper != null) {
            webRTCHelper.exitRoom();
        }
        stopRinging();
    }

    @Override
    public void onError(String msg) {
        this.webSocket = null;
        this.webRTCHelper = null;
        stopRinging();
    }


    //============================================================================================
    private boolean isRinging;
    private boolean mAudioFocused;

    public synchronized void startRinging() {
        int readExternalStorage = PackageManager.PERMISSION_DENIED;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            readExternalStorage = _context.getPackageManager().checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, _context.getPackageName());
        }
        if (readExternalStorage != PackageManager.PERMISSION_GRANTED) {
            mAudioManager.setSpeakerphoneOn(true);
            return;
        }
        mAudioManager.setSpeakerphoneOn(true);
        mAudioManager.setMode(MODE_RINGTONE);
        try {
            if ((mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE || mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) && mVibrator != null) {
                long[] patern = {0, 1000, 1000};
                mVibrator.vibrate(patern, 1);
            }
            if (mRingerPlayer == null) {
                requestAudioFocus(STREAM_RING);
                mRingerPlayer = new MediaPlayer();
                mRingerPlayer.setAudioStreamType(STREAM_RING);
                String ringtone = Settings.System.DEFAULT_RINGTONE_URI.toString();
                try {
                    if (ringtone.startsWith("content://")) {
                        mRingerPlayer.setDataSource(_context, Uri.parse(ringtone));
                    } else {
                        FileInputStream fis = new FileInputStream(ringtone);
                        mRingerPlayer.setDataSource(fis.getFD());
                        fis.close();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Cannot set ringtone");
                }

                mRingerPlayer.prepare();
                mRingerPlayer.setLooping(true);
                mRingerPlayer.start();
            } else {
                Log.w(TAG, "already ringing");
            }
        } catch (Exception e) {
            Log.e(TAG, "cannot handle incoming call");
        }
        isRinging = true;
    }

    public synchronized void stopRinging() {
        if (mRingerPlayer != null) {
            mRingerPlayer.stop();
            mRingerPlayer.release();
            mRingerPlayer = null;
        }
        if (mVibrator != null) {
            mVibrator.cancel();
        }

        isRinging = false;
        if (mAudioManager != null) {
            mAudioManager.setSpeakerphoneOn(false);
        }

    }

    private void requestAudioFocus(int stream) {
        if (!mAudioFocused) {
            int res = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                res = mAudioManager.requestAudioFocus(null, stream, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE);
            }
            if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) mAudioFocused = true;
        }
    }
    //=============================================================================================


}
