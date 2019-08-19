package com.dds.voip;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.dds.skywebrtc.AVEngineKit;
import com.dds.skywebrtc.CallSession;
import com.dds.skywebrtc.NotInitializedExecption;
import com.dds.webrtc.R;

public class SingleCallActivity extends AppCompatActivity implements CallSession.CallSessionCallback {

    public static final String EXTRA_TARGET = "targetId";
    public static final String EXTRA_MO = "isOutGoing";
    public static final String EXTRA_AUDIO_ONLY = "audioOnly";
    public static final String EXTRA_FROM_FLOATING_VIEW = "fromFloatingView";


    private boolean isOutgoing;
    private String targetId;
    private boolean isAudioOnly;
    private boolean isFromFloatingView;

    private AVEngineKit gEngineKit;


    public static void openActivity(Context context, String targetId, boolean isMo, boolean isAudioOnly) {
        Intent voip = new Intent(context, SingleCallActivity.class);
        voip.putExtra(SingleCallActivity.EXTRA_MO, isMo);
        voip.putExtra(SingleCallActivity.EXTRA_TARGET, targetId);
        voip.putExtra(SingleCallActivity.EXTRA_AUDIO_ONLY, isAudioOnly);

        if (context instanceof Activity) {
            context.startActivity(voip);
        } else {
            voip.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(voip);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //全屏显示
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(getSystemUiVisibility());
        setContentView(R.layout.activity_single_call);

        try {
            gEngineKit = AVEngineKit.Instance();
        } catch (NotInitializedExecption notInitializedExecption) {
            finish();
        }
        final Intent intent = getIntent();
        targetId = intent.getStringExtra(EXTRA_TARGET);
        isFromFloatingView = intent.getBooleanExtra(EXTRA_FROM_FLOATING_VIEW, false);

        if (isFromFloatingView) {
            Intent serviceIntent = new Intent(this, FloatingVoipService.class);
            stopService(serviceIntent);
        } else {
            isOutgoing = intent.getBooleanExtra(EXTRA_MO, false);
            isAudioOnly = intent.getBooleanExtra(EXTRA_AUDIO_ONLY, false);
            init(targetId, isOutgoing, isAudioOnly);
        }


    }

    private void init(String targetId, boolean outgoing, boolean audioOnly) {


        Fragment fragment;
        if (audioOnly) {
            fragment = new AudioFragment();
        } else {
            fragment = new VideoFragment();
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .add(android.R.id.content, fragment)
                .commit();

        if (isOutgoing) {
            // 发起会话
            gEngineKit.startCall(targetId, audioOnly, this);
            // 画面预览
            gEngineKit.startPreview();

        } else {

        }
    }

    @TargetApi(19)
    private static int getSystemUiVisibility() {
        int flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            flags |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }
        return flags;
    }


    // =========================================================================
    @Override
    public void didCallEndWithReason(AVEngineKit.CallEndReason var1) {

    }

    @Override
    public void didChangeState(AVEngineKit.CallState var1) {

    }

    @Override
    public void didChangeMode(boolean var1) {

    }

    @Override
    public void didCreateLocalVideoTrack() {

    }

    @Override
    public void didReceiveRemoteVideoTrack() {

    }

    @Override
    public void didError(String var1) {

    }


}
