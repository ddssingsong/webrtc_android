package com.dds.webrtclib.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.dds.webrtclib.EnumMsg;
import com.dds.webrtclib.R;
import com.dds.webrtclib.WebRTCManager;

/**
 * 来电显示界面
 */
public class IncomingActivity extends AppCompatActivity {

    private TextView wr_hang_up;
    private TextView wr_accept;
    private String mediaType;


    public static void openActivity(Context activity, EnumMsg.MediaType mediaType) {
        Intent intent = new Intent(activity, IncomingActivity.class);
        intent.putExtra("mediaType", mediaType.value);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.wr_activity_incoming);
        initView();
        initVar();
        initListener();
    }


    private void initView() {
        wr_accept = (TextView) findViewById(R.id.wr_accept);
        wr_hang_up = (TextView) findViewById(R.id.wr_hang_up);

    }

    private void initVar() {
        Intent intent = getIntent();
        mediaType = intent.getStringExtra("mediaType");
        if (EnumMsg.MediaType.Meeting.value.equals(mediaType)) {
            IncomingMeetingFragment fragment = new IncomingMeetingFragment();
            replaceFragment(fragment);

        } else if (EnumMsg.MediaType.Video.value.equals(mediaType)) {
            IncomingVideoFragment fragment = new IncomingVideoFragment();
            replaceFragment(fragment);
        } else if (EnumMsg.MediaType.Audio.value.equals(mediaType)) {
            IncomingAudioFragment fragment = new IncomingAudioFragment();
            replaceFragment(fragment);
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


    private void replaceFragment(Fragment fragment) {
        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction()
                .replace(R.id.wr_incoming_container, fragment)
                .commit();

    }


}
