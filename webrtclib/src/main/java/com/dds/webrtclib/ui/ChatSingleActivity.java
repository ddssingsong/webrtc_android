package com.dds.webrtclib.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.dds.webrtclib.R;
import com.dds.webrtclib.WebRTCHelper;
import com.dds.webrtclib.WebRTCManager;
import com.dds.webrtclib.callback.IViewCallback;
import com.dds.webrtclib.callback.ProxyRenderer;
import com.dds.webrtclib.utils.PermissionUtil;

import org.webrtc.EglBase;
import org.webrtc.MediaStream;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoRenderer;

/**
 * 单聊界面
 * 1. 一对一视频通话
 * 2. 一对一语音通话
 */
public class ChatSingleActivity extends AppCompatActivity implements IViewCallback {
    private static final String TAG = "dds_ChatSingle";
    private SurfaceViewRenderer local_view;
    private SurfaceViewRenderer remote_view;
    private ProxyRenderer localRender;
    private ProxyRenderer remoteRender;
    private EglBase rootEglBase;

    private WebRTCManager helper;
    private ChatSingleFragment chatSingleFragment;
    private boolean isSwappedFeeds;

    private boolean videoEnable;
    private String userId;


    public static void openActivity(Context activity, boolean videoEnable, String userId) {
        Intent intent = new Intent(activity, ChatSingleActivity.class);
        intent.putExtra("videoEnable", videoEnable);
        intent.putExtra("userId", userId);
        if (activity instanceof Activity) {
            activity.startActivity(intent);
            ((Activity) activity).overridePendingTransition(0, 0);
        } else {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            activity.startActivity(intent);
        }


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
        userId = intent.getStringExtra("userId");
        chatSingleFragment = new ChatSingleFragment();
        replaceFragment(chatSingleFragment, videoEnable, userId);

        if (videoEnable) {
            local_view = findViewById(R.id.local_view_render);
            remote_view = findViewById(R.id.remote_view_render);
            local_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setSwappedFeeds(!isSwappedFeeds);
                }
            });
            rootEglBase = EglBase.create();
            // 本地图像初始化
            local_view.init(rootEglBase.getEglBaseContext(), null);
            local_view.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
            local_view.setZOrderMediaOverlay(true);
            localRender = new ProxyRenderer();
            //远端图像初始化
            remote_view.init(rootEglBase.getEglBaseContext(), null);
            remote_view.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
            remoteRender = new ProxyRenderer();
            setSwappedFeeds(true);
        }

        startCall();

    }

    private void setSwappedFeeds(boolean isSwappedFeeds) {
        this.isSwappedFeeds = isSwappedFeeds;
        localRender.setTarget(isSwappedFeeds ? remote_view : local_view);
        remoteRender.setTarget(isSwappedFeeds ? local_view : remote_view);
    }

    private void startCall() {
        helper = WebRTCManager.getInstance();
        helper.setCallback(this);
        if (!PermissionUtil.isNeedRequestPermission(ChatSingleActivity.this)) {
            helper.joinRoom();
        }

    }

    private void replaceFragment(Fragment fragment, boolean videoEnable, String userId) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("videoEnable", videoEnable);
        bundle.putString("userId", userId);
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

    @Override
    public void onSetLocalStream(MediaStream stream, String socketId) {
        if (videoEnable) {
            stream.videoTracks.get(0).setEnabled(true);
            stream.videoTracks.get(0).addRenderer(new VideoRenderer(localRender));
        }

    }

    @Override
    public void onAddRemoteStream(MediaStream stream, String socketId) {
        if (videoEnable) {
            stream.videoTracks.get(0).setEnabled(true);
            stream.videoTracks.get(0).addRenderer(new VideoRenderer(remoteRender));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setSwappedFeeds(false);

                    if (chatSingleFragment != null && chatSingleFragment.isAdded()) {
                        chatSingleFragment.hideUserInfo();
                        chatSingleFragment.startTimer();
                    }
                    // 开始计时
                    WebRTCManager.getInstance().startTimer();


                }
            });
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (chatSingleFragment != null && chatSingleFragment.isAdded()) {
                        chatSingleFragment.startTimer();
                    }
                    // 开始计时
                    WebRTCManager.getInstance().startTimer();


                }
            });
        }


    }

    @Override
    public void onReceiveAck() {
        // 等待对方接听
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (chatSingleFragment != null) {
                    chatSingleFragment.setChatTips(getString(R.string.webrtc_invite_waiting));
                }

            }
        });

    }

    @Override
    public void onCloseWithId(String socketId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ChatSingleActivity.this, "对方已挂断", Toast.LENGTH_SHORT).show();
                exit();
            }
        });

    }

    @Override
    public void onDecline() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ChatSingleActivity.this.finish();
            }
        });

    }

    @Override
    public void onError(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "通话结束", Toast.LENGTH_LONG).show();
                ChatSingleActivity.this.finish();
            }
        });
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

    // 扬声器
    public void toggleSpeaker(boolean enable) {
        helper.toggleSpeaker(enable);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        exit();
    }

    private void exit() {
        helper.exitRoom();
        if (localRender != null) {
            localRender.setTarget(null);
        }

        if (local_view != null) {
            local_view.release();
            local_view = null;
        }
        if (remote_view != null) {
            remote_view.release();
            remote_view = null;
        }
        this.finish();

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
        helper.joinRoom();

    }
}
