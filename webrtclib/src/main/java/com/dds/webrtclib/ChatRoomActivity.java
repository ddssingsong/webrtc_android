package com.dds.webrtclib;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

import com.dds.webrtclib.utils.PermissionUtil;

import org.webrtc.MediaStream;
import org.webrtc.RendererCommon;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;
import org.webrtc.VideoTrack;

import java.util.HashMap;
import java.util.Map;

/**
 * 视频会议
 */
public class ChatRoomActivity extends AppCompatActivity implements IWebRTCHelper {

    private WebRTCHelper helper;
    private Map<String, VideoTrack> _remoteVideoTracks = new HashMap();
    private Map<String, VideoRenderer.Callbacks> _remoteVideoView = new HashMap();
    private static int x;
    private static int y;
    private GLSurfaceView vsv;
    private VideoRenderer.Callbacks localRender;
    private double width = 480;
    private double height = 640;
    private RendererCommon.ScalingType scalingType = RendererCommon.ScalingType.SCALE_ASPECT_FILL;

    private ChatRoomFragment chatRoomFragment;

    private String signal;
    private String stun;
    private String room;

    public static void openActivity(Activity activity, String signal, String stun, String room) {
        Intent intent = new Intent(activity, ChatRoomActivity.class);
        intent.putExtra("signal", signal);
        intent.putExtra("stun", stun);
        intent.putExtra("room", room);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wr_activity_chat_room);
        initView();
        initVar();
        chatRoomFragment = new ChatRoomFragment();
        replaceFragment(chatRoomFragment);


        startCall();

    }


    private void initView() {
        vsv = findViewById(R.id.wr_glview_call);
        vsv.setPreserveEGLContextOnPause(true);
        vsv.setKeepScreenOn(true);
    }

    private void initVar() {
        Intent intent = getIntent();
        signal = intent.getStringExtra("signal");
        stun = intent.getStringExtra("stun");
        room = intent.getStringExtra("room");

        // 设置宽高比例
        WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        if (manager != null) {
            width = manager.getDefaultDisplay().getWidth() / 3.0;
        }
        height = width * 32.0 / 24.0;
        x = 0;
        y = 70;


    }

    private void startCall() {
        VideoRendererGui.setView(vsv, new Runnable() {
            @Override
            public void run() {
                Log.i("dds_webrtc", "surfaceView准备完毕");
                if (!PermissionUtil.isNeedRequestPermission(ChatRoomActivity.this)) {
                    helper = new WebRTCHelper(ChatRoomActivity.this, stun);
                    helper.initSocket(signal, room);
                }

            }
        });


        localRender = VideoRendererGui.create(0, 0,
                100, 100, scalingType, true);


    }

    @Override
    public void onSetLocalStream(MediaStream stream, String userId) {

        Log.i("dds_webrtc", "在本地添加视频");

        stream.videoTracks.get(0).addRenderer(new VideoRenderer(localRender));
        VideoRendererGui.update(localRender,
                0, 0,
                100, 100,
                scalingType, false);
    }

    @Override
    public void onAddRemoteStream(MediaStream stream, String userId) {

        Log.i("dds_webrtc", "接受到远端视频流     " + userId);

        _remoteVideoTracks.put(userId, stream.videoTracks.get(0));


        VideoRenderer.Callbacks vr = VideoRendererGui.create(
                0, 0,
                0, 0, scalingType, false);


        _remoteVideoView.put(userId, vr);


        stream.videoTracks.get(0).addRenderer(new VideoRenderer(vr));
        VideoRendererGui.update(vr,
                x, y,
                30, x + 30,
                scalingType, false);

        x += 30;
    }


    @Override
    public void onCloseWithId(String userId) {
        Log.i("dds_webrtc", "有用户离开    " + userId);
        VideoRenderer.Callbacks callbacks = _remoteVideoView.get(userId);
        VideoRendererGui.remove(callbacks);

        _remoteVideoTracks.remove(userId);
        _remoteVideoView.remove(userId);

        if (_remoteVideoTracks.size() == 0) {
            x = 0;
        }


    }

    @Override  // 屏蔽返回键
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return keyCode == KeyEvent.KEYCODE_BACK || super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        exit();
        super.onDestroy();
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction()
                .replace(R.id.wr_container, fragment)
                .commit();

    }

    // 切换摄像头
    public void switchCamera() {
        helper.switchCamera();
    }

    // 挂断
    public void hangUp() {
        exit();
        this.finish();
    }

    // 静音
    public void toggleMic(boolean enable) {
        helper.toggleMute(enable);
    }

    private void exit() {
        helper.exitRoom();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (int i = 0; i < permissions.length; i++) {
            Log.i(WebRTCHelper.TAG, "[Permission] " + permissions[i] + " is " + (grantResults[i] == PackageManager.PERMISSION_GRANTED ? "granted" : "denied"));
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                finish();
                break;
            }
        }

        helper = new WebRTCHelper(ChatRoomActivity.this, stun);
        helper.initSocket(signal, room);


    }
}
