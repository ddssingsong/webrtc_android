package com.dds.temple;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.dds.temple.socket.AppRTCClient;
import com.dds.temple.socket.DirectRTCClient;

import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.RendererCommon;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceViewRenderer;

public class ConnectActivity extends AppCompatActivity implements AppRTCClient.SignalingEvents {
    private static final String TAG = "ConnectActivity";
    private SurfaceViewRenderer mFullView;
    private SurfaceViewRenderer mPipView;
    private int mRoleType;
    private String mIpAddress;
    private DirectRTCClient mDirectRTCClient;
    private boolean isServer;

    public static final String ARG_ROLE_TYPE = "roleType";
    public static final String ARG_IP_ADDRESS = "ipAddress";
    public static final int TYPE_SERVER = 0;
    public static final int TYPE_CLIENT = 1;

    public static void launchActivity(Activity activity, int roleType, String ip) {
        Intent intent = new Intent(activity, ConnectActivity.class);
        intent.putExtra(ARG_ROLE_TYPE, roleType);
        intent.putExtra(ARG_IP_ADDRESS, ip);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarOrScreenStatus(this);
        setContentView(R.layout.activity_connect2);
        Intent intent = getIntent();
        mRoleType = intent.getIntExtra(ARG_ROLE_TYPE, 0);
        mIpAddress = intent.getStringExtra(ARG_IP_ADDRESS);
        isServer = mRoleType == TYPE_SERVER;
        initView();
        initSocket();
    }


    private void initView() {
        mFullView = findViewById(R.id.full_surface_render);
        mPipView = findViewById(R.id.pip_surface_render);

        // start init render
        final EglBase eglBase = EglBase.create();
        // full
        mFullView.init(eglBase.getEglBaseContext(), null);
        mFullView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        // pip
        mPipView.init(eglBase.getEglBaseContext(), null);
        mPipView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
    }

    private void initSocket() {
        mDirectRTCClient = new DirectRTCClient(this);
        AppRTCClient.RoomConnectionParameters parameters = new AppRTCClient.RoomConnectionParameters(mIpAddress);
        mDirectRTCClient.connectToRoom(parameters);
    }

    public void hungUp(View view) {
        if (mDirectRTCClient != null) {
            mDirectRTCClient.disconnectFromRoom();
        }
    }


    // region -------------------------------socket event-------------------------------------------
    @Override
    public void onConnectedToRoom(AppRTCClient.SignalingParameters params) {
        boolean initiator = params.initiator;
        if (initiator) {
            // create offer
            logAndToast("create offer");
        } else {
            if (params.offerSdp != null) {

            }

        }


    }

    @Override
    public void onRemoteDescription(SessionDescription sdp) {

    }

    @Override
    public void onRemoteIceCandidate(IceCandidate candidate) {

    }

    @Override
    public void onRemoteIceCandidatesRemoved(IceCandidate[] candidates) {

    }

    @Override
    public void onChannelClose() {

    }

    @Override
    public void onChannelError(String description) {

    }

    // endregion

    private Toast logToast;

    private void logAndToast(String msg) {
        Log.d(TAG, msg);
        if (logToast != null) {
            logToast.cancel();
        }
        logToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        logToast.show();
    }

    private static int getSystemUiVisibility() {
        int flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            flags |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }
        return flags;
    }

    /**
     * 设置状态栏透明
     */
    public void setStatusBarOrScreenStatus(Activity activity) {
        Window window = activity.getWindow();
        //全屏+锁屏+常亮显示
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        window.getDecorView().setSystemUiVisibility(getSystemUiVisibility());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
            layoutParams.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            window.setAttributes(layoutParams);
        }
        // 5.0以上系统状态栏透明
        //清除透明状态栏
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //设置状态栏颜色必须添加
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);//设置透明
    }

}