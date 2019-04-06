package com.dds.webrtclib.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.dds.webrtclib.IViewCallback;
import com.dds.webrtclib.PeerConnectionHelper;
import com.dds.webrtclib.ProxyVideoSink;
import com.dds.webrtclib.R;
import com.dds.webrtclib.WebRTCManager;
import com.dds.webrtclib.utils.PermissionUtil;

import org.webrtc.EglBase;
import org.webrtc.MediaStream;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoTrack;

import java.util.HashMap;
import java.util.Map;

/**
 * 群聊界面
 * 1. 一对一视频通话
 * 2. 一对一语音通话
 */


public class ChatRoomActivity extends AppCompatActivity implements IViewCallback {

    private FrameLayout wr_video_view;

    private WebRTCManager manager;
    private Map<String, VideoTrack> _remoteVideoTracks = new HashMap();
    private Map<String, SurfaceViewRenderer> _remoteVideoViews = new HashMap();
    private Map<String, ProxyVideoSink> _remoteSinks = new HashMap();

    private SurfaceViewRenderer localRender;
    private ProxyVideoSink localSink;


    private static int x;
    private static int y;

    private int width = 480;
    private int height = 640;

    private ChatRoomFragment chatRoomFragment;

    private EglBase rootEglBase;


    public static void openActivity(Activity activity) {
        Intent intent = new Intent(activity, ChatRoomActivity.class);
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
        wr_video_view = findViewById(R.id.wr_video_view);
    }

    private void initVar() {
        // 设置宽高比例
        WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        if (manager != null) {
            width = manager.getDefaultDisplay().getWidth() / 3 - 12;
        }
        height = width * 32 / 24;
        x = 9;
        y = 10;
        rootEglBase = EglBase.create();

    }

    private void startCall() {
        manager = WebRTCManager.getInstance();
        manager.setCallback(this);

        localRender = new SurfaceViewRenderer(this);
        localRender.init(rootEglBase.getEglBaseContext(), null);
        localRender.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        localRender.setZOrderMediaOverlay(true);
        localRender.setMirror(true);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(width, height);
        layoutParams.leftMargin = x;
        layoutParams.topMargin = y;
        localRender.setLayoutParams(layoutParams);
        localSink = new ProxyVideoSink();
        localSink.setTarget(localRender);
        wr_video_view.addView(localRender);

        x = x + width + 9;


        if (!PermissionUtil.isNeedRequestPermission(ChatRoomActivity.this)) {
            manager.joinRoom(getApplicationContext(), rootEglBase);
        }

    }

    @Override
    public void onSetLocalStream(MediaStream stream, String userId) {
        Log.i("dds_webrtc", "在本地添加视频");
        stream.videoTracks.get(0).addSink(localSink);
    }

    @Override
    public void onAddRemoteStream(MediaStream stream, String userId) {
        _remoteVideoTracks.put(userId, stream.videoTracks.get(0));
        runOnUiThread(() -> {
            SurfaceViewRenderer renderer = new SurfaceViewRenderer(ChatRoomActivity.this);
            renderer.init(rootEglBase.getEglBaseContext(), null);
            renderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
            renderer.setZOrderMediaOverlay(true);
            renderer.setMirror(true);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(width, height);
            layoutParams.leftMargin = x;
            layoutParams.topMargin = y;
            renderer.setLayoutParams(layoutParams);
            wr_video_view.addView(renderer);
            _remoteVideoViews.put(userId, renderer);
            ProxyVideoSink sink = new ProxyVideoSink();
            sink.setTarget(renderer);
            _remoteSinks.put(userId, sink);


            stream.videoTracks.get(0).addSink(sink);

            int size = _remoteVideoTracks.size();
            x = (width + 9) * (size % 3 + 1) + 9;
            y = ((size + 1) / 3) * (height + 10) + 10;

        });


    }


    @Override
    public void onCloseWithId(String userId) {
        _remoteVideoTracks.remove(userId);
        runOnUiThread(() -> {
            ProxyVideoSink sink = _remoteSinks.get(userId);
            SurfaceViewRenderer renderer = _remoteVideoViews.get(userId);
            if (sink != null) {
                sink.setTarget(null);
            }
            if (renderer != null) {
                renderer.release();
            }

            _remoteSinks.remove(userId);
            _remoteVideoViews.remove(userId);

            wr_video_view.removeView(renderer);


        });


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
        manager.switchCamera();
    }

    // 挂断
    public void hangUp() {
        exit();
        this.finish();
    }

    // 静音
    public void toggleMic(boolean enable) {
        manager.toggleMute(enable);
    }

    private void exit() {
        manager.exitRoom();

        localSink.setTarget(null);
        if (localRender != null) {
            localRender.release();
            localRender = null;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (int i = 0; i < permissions.length; i++) {
            Log.i(PeerConnectionHelper.TAG, "[Permission] " + permissions[i] + " is " + (grantResults[i] == PackageManager.PERMISSION_GRANTED ? "granted" : "denied"));
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                finish();
                break;
            }
        }
        manager.joinRoom(getApplicationContext(), rootEglBase);


    }
}
