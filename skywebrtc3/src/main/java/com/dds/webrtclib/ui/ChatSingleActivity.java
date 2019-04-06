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

/**
 * 单聊界面
 * 1. 一对一视频通话
 * 2. 一对一语音通话
 */
public class ChatSingleActivity extends AppCompatActivity {
    private SurfaceViewRenderer local_view;
    private SurfaceViewRenderer remote_view;
    private ProxyVideoSink localRender;
    private ProxyVideoSink remoteRender;

    private WebRTCManager manager;
    private ChatSingleFragment chatSingleFragment;

    private boolean videoEnable;
    private boolean isSwappedFeeds;

    private EglBase rootEglBase;

    public static void openActivity(Activity activity, boolean videoEnable) {
        Intent intent = new Intent(activity, ChatSingleActivity.class);
        intent.putExtra("videoEnable", videoEnable);
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
        setContentView(R.layout.wr_activity_chat_single);
        initVar();
    }


    private void initVar() {
        Intent intent = getIntent();
        videoEnable = intent.getBooleanExtra("videoEnable", false);
        chatSingleFragment = new ChatSingleFragment();
        replaceFragment(chatSingleFragment, videoEnable);
        rootEglBase = EglBase.create();
        if (videoEnable) {
            local_view = findViewById(R.id.local_view_render);
            remote_view = findViewById(R.id.remote_view_render);


            // 本地图像初始化
            local_view.init(rootEglBase.getEglBaseContext(), null);
            local_view.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
            local_view.setZOrderMediaOverlay(true);
            local_view.setMirror(true);
            localRender = new ProxyVideoSink();
            //远端图像初始化
            remote_view.init(rootEglBase.getEglBaseContext(), null);
            remote_view.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_BALANCED);
            remote_view.setMirror(true);
            remoteRender = new ProxyVideoSink();
            setSwappedFeeds(true);

            local_view.setOnClickListener(v -> setSwappedFeeds(!isSwappedFeeds));
        }

        startCall();

    }

    private void setSwappedFeeds(boolean isSwappedFeeds) {
        this.isSwappedFeeds = isSwappedFeeds;
        localRender.setTarget(isSwappedFeeds ? remote_view : local_view);
        remoteRender.setTarget(isSwappedFeeds ? local_view : remote_view);
    }

    private void startCall() {
        manager = WebRTCManager.getInstance();
        manager.setCallback(new IViewCallback() {
            @Override
            public void onSetLocalStream(MediaStream stream, String socketId) {
                if (videoEnable) {
                    stream.videoTracks.get(0).setEnabled(true);
                    stream.videoTracks.get(0).addSink(localRender);
                }
            }

            @Override
            public void onAddRemoteStream(MediaStream stream, String socketId) {
                if (videoEnable) {
                    setSwappedFeeds(false);
                    stream.videoTracks.get(0).setEnabled(true);
                    stream.videoTracks.get(0).addSink(remoteRender);
                }
            }

            @Override
            public void onCloseWithId(String socketId) {
                runOnUiThread(() -> {
                    disConnect();
                    ChatSingleActivity.this.finish();
                });

            }
        });
        if (!PermissionUtil.isNeedRequestPermission(ChatSingleActivity.this)) {
            manager.joinRoom(getApplicationContext(), rootEglBase);
        }

    }

    private void replaceFragment(Fragment fragment, boolean videoEnable) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("videoEnable", videoEnable);
        fragment.setArguments(bundle);
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction()
                .replace(R.id.wr_container, fragment)
                .commit();

    }


    @Override  // 屏蔽返回键
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return keyCode == KeyEvent.KEYCODE_BACK || super.onKeyDown(keyCode, event);
    }


    // 切换摄像头
    public void switchCamera() {
        manager.switchCamera();
    }

    // 挂断
    public void hangUp() {
        disConnect();
        this.finish();
    }

    // 静音
    public void toggleMic(boolean enable) {
        manager.toggleMute(enable);
    }

    // 扬声器
    public void toggleSpeaker(boolean enable) {
        manager.toggleSpeaker(enable);

    }

    @Override
    protected void onDestroy() {
        disConnect();
        super.onDestroy();

    }

    private void disConnect() {
        manager.exitRoom();
        if (localRender != null) {
            localRender.setTarget(null);
            localRender = null;
        }
        if (remoteRender != null) {
            remoteRender.setTarget(null);
            remoteRender = null;
        }

        if (local_view != null) {
            local_view.release();
            local_view = null;
        }
        if (remote_view != null) {
            remote_view.release();
            remote_view = null;
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
