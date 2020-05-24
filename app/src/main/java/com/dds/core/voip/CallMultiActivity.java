package com.dds.core.voip;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.dds.permission.Permissions;
import com.dds.skywebrtc.CallSession;
import com.dds.skywebrtc.EnumType;
import com.dds.skywebrtc.SkyEngineKit;
import com.dds.skywebrtc.except.NotInitializedException;
import com.dds.webrtc.R;

/**
 * Created by dds on 2018/7/26.
 * 多人通话界面
 */
public class CallMultiActivity extends AppCompatActivity implements CallSession.CallSessionCallback {
    private SkyEngineKit gEngineKit;
    private Handler handler = new Handler(Looper.getMainLooper());
    private ImageView meetingHangupImageView;
    private CallSession.CallSessionCallback currentFragment;

    public static void openActivity(Activity activity, String room) {
        Intent intent = new Intent(activity, CallMultiActivity.class);
        intent.putExtra("room", room);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(getSystemUiVisibility());
        setContentView(R.layout.activity_multi_call);
        initView();
        initData();
    }

    private void initView() {
        meetingHangupImageView = findViewById(R.id.meetingHangupImageView);

        Fragment fragment = new FragmentMeeting();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .add(R.id.meeting_container, fragment)
                .commit();
        currentFragment = (CallSession.CallSessionCallback) fragment;
    }

    private void initData() {
        Intent intent = getIntent();
        String room = intent.getStringExtra("room");
        try {
            gEngineKit = SkyEngineKit.Instance();
        } catch (NotInitializedException e) {
            finish();
        }
        String[] per = new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA};
        Permissions.request(this, per, integer -> {
            if (integer == 0) {
                // 权限同意
                init(room);
            } else {
                // 权限拒绝
                CallMultiActivity.this.finish();
            }
        });


    }

    private void init(String room) {
        SkyEngineKit.init(new VoipEvent());
        // 加入房间
        gEngineKit.joinRoom(this, room);

        CallSession session = gEngineKit.getCurrentSession();
        if (session == null) {
            this.finish();
        } else {
            session.setSessionCallback(this);

        }


    }


    public SkyEngineKit getEngineKit() {
        return gEngineKit;
    }


    @TargetApi(19)
    private static int getSystemUiVisibility() {
        int flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            flags |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }
        return flags;
    }


    //-------------------------------------------------回调相关------------------------------------
    @Override
    public void didCallEndWithReason(EnumType.CallEndReason var1) {
        finish();
    }

    @Override
    public void didChangeState(EnumType.CallState callState) {
        handler.post(() -> currentFragment.didChangeState(callState));
    }

    @Override
    public void didChangeMode(boolean var1) {
        handler.post(() -> currentFragment.didChangeMode(var1));
    }

    @Override
    public void didCreateLocalVideoTrack() {
        handler.post(() -> currentFragment.didCreateLocalVideoTrack());
    }

    @Override
    public void didReceiveRemoteVideoTrack(String userId) {
        handler.post(() -> currentFragment.didReceiveRemoteVideoTrack(userId));
    }

    @Override
    public void didError(String var1) {
        finish();
    }
}
