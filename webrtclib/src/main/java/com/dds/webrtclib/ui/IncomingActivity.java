package com.dds.webrtclib.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.dds.webrtclib.EnumMsg;
import com.dds.webrtclib.R;
import com.dds.webrtclib.WebRTCHelper;
import com.dds.webrtclib.WebRTCManager;
import com.dds.webrtclib.WebrtcService;
import com.dds.webrtclib.utils.PermissionUtil;

/**
 * 来电显示界面
 */
public class IncomingActivity extends AppCompatActivity {
    private TextView wr_hang_up;
    private TextView wr_accept;
    private String mediaType;
    private String userId;

    public static IncomingActivity incomingActivity;

    public static void openActivity(Context activity, EnumMsg.MediaType mediaType, String userId) {
        Intent intent = new Intent(activity, IncomingActivity.class);
        intent.putExtra("mediaType", mediaType.value);
        intent.putExtra("userId", userId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置锁屏状态下也能亮屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.systemUiVisibility = View.SYSTEM_UI_FLAG_LOW_PROFILE;
        setContentView(R.layout.wr_activity_incoming);
        incomingActivity = this;
        initView();
        initVar();
        initListener();

        WebrtcService.incomingNotification(this);

    }


    private void initView() {
        wr_accept = findViewById(R.id.wr_accept);
        wr_hang_up = findViewById(R.id.wr_hang_up);

    }

    private void initVar() {
        Intent intent = getIntent();
        if (intent.hasExtra("mediaType")) {
            mediaType = intent.getStringExtra("mediaType");
        } else {
            mediaType = WebRTCManager.getInstance().get_mediaType().value;
        }
        if (intent.hasExtra("userId")) {
            userId = intent.getStringExtra("userId");
        } else {
            userId = WebRTCManager.getInstance().get_userId();
        }


        if (EnumMsg.MediaType.Meeting.value.equals(mediaType)) {
            if (!PermissionUtil.isNeedRequestPermission(IncomingActivity.this)) {
                IncomingMeetingFragment fragment = new IncomingMeetingFragment();
                replaceFragment(fragment, userId);
            }
        } else if (EnumMsg.MediaType.Video.value.equals(mediaType)) {
            if (!PermissionUtil.isNeedRequestPermission(IncomingActivity.this)) {
                IncomingVideoFragment fragment = new IncomingVideoFragment();
                replaceFragment(fragment, userId);
            }

        } else if (EnumMsg.MediaType.Audio.value.equals(mediaType)) {
            if (!PermissionUtil.isNeedRequestAudioPermission(IncomingActivity.this)) {
                IncomingAudioFragment fragment = new IncomingAudioFragment();
                replaceFragment(fragment, userId);
            }

        }

    }


    private void initListener() {
        wr_accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WebRTCManager.getInstance().acceptCall(IncomingActivity.this);
                IncomingActivity.this.finish();
            }
        });
        wr_hang_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WebRTCManager.getInstance().refuseCall();
                IncomingActivity.this.finish();

            }
        });
    }


    private void replaceFragment(Fragment fragment, String userId) {
        Bundle bundle = new Bundle();
        bundle.putString("userId", userId);
        fragment.setArguments(bundle);
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction()
                .replace(R.id.wr_incoming_container, fragment)
                .commit();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        incomingActivity = null;


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (int i = 0; i < permissions.length; i++) {
            Log.i(WebRTCHelper.TAG, "[Permission] " + permissions[i] + " is " + (grantResults[i] == PackageManager.PERMISSION_GRANTED ? "granted" : "denied"));
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                WebRTCManager.getInstance().exitRoom();
                finish();
                break;
            }
        }
        if (EnumMsg.MediaType.Meeting.value.equals(mediaType)) {
            IncomingMeetingFragment fragment = new IncomingMeetingFragment();
            replaceFragment(fragment, userId);
        } else if (EnumMsg.MediaType.Video.value.equals(mediaType)) {
            IncomingVideoFragment fragment = new IncomingVideoFragment();
            replaceFragment(fragment, userId);

        } else if (EnumMsg.MediaType.Audio.value.equals(mediaType)) {
            IncomingAudioFragment fragment = new IncomingAudioFragment();
            replaceFragment(fragment, userId);

        }

    }


}
