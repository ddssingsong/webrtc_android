package com.dds.core.voip;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.BounceInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.dds.core.ui.event.MsgEvent;
import com.dds.skywebrtc.CallSession;
import com.dds.skywebrtc.CallSession.CallSessionCallback;
import com.dds.skywebrtc.EnumType.CallEndReason;
import com.dds.skywebrtc.EnumType.CallState;
import com.dds.skywebrtc.SkyEngineKit;
import com.dds.webrtc.BuildConfig;
import com.dds.webrtc.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Locale;
import java.util.Objects;

/**
 * 悬浮窗界面
 */
public class FloatingVoipService extends Service {
    private CallSession session;
    private Intent resumeActivityIntent;
    private Handler handler = new Handler();
    private WindowManager wm;
    private View view;
    private WindowManager.LayoutParams params;
    private int touchSlop = 0;
    private LinearLayout audioView;
    private FrameLayout videoView;
    private int margin = 0;
    final int statusBarHeight = BarUtils.getStatusBarHeight();
    private int screenWidth = 0;
    private int screenHeight = 0;
    private PowerManager.WakeLock wakeLock;
    HeadsetPlugReceiver headsetPlugReceiver;
    private final static String TAG = "FloatingVoipService";
    private static boolean isStarted = false;
    private final static int NOTIFICATION_ID = 1;
    private ViewGroup floatingView;

    @Override
    public void onCreate() {
        super.onCreate();
        touchSlop = ViewConfiguration.get(this).getScaledTouchSlop();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        headsetPlugReceiver = new HeadsetPlugReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(headsetPlugReceiver, filter);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (isStarted) {
            return START_NOT_STICKY;
        }
        isStarted = true;
        session = SkyEngineKit.Instance().getCurrentSession();
        if (session == null || CallState.Idle.equals(session.getState())) {
            stopSelf();
        }
        resumeActivityIntent = new Intent(this, CallSingleActivity.class);
        resumeActivityIntent = new Intent(this, CallSingleActivity.class);
        resumeActivityIntent.putExtra(CallSingleActivity.EXTRA_FROM_FLOATING_VIEW, true);
        resumeActivityIntent.putExtra(CallSingleActivity.EXTRA_MO, intent.getBooleanExtra(CallSingleActivity.EXTRA_MO, false));
        resumeActivityIntent.putExtra(CallSingleActivity.EXTRA_AUDIO_ONLY, intent.getBooleanExtra(CallSingleActivity.EXTRA_AUDIO_ONLY, false));
        resumeActivityIntent.putExtra(CallSingleActivity.EXTRA_TARGET, intent.getStringExtra(CallSingleActivity.EXTRA_TARGET));
        resumeActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, resumeActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        String channelId = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelId = BuildConfig.APPLICATION_ID + ".voip";
            String channelName = "voip";
            NotificationChannel chan = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(chan);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("通话中...")
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
        startForeground(NOTIFICATION_ID, builder.build());
        try {
            showFloatingWindow();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return START_NOT_STICKY;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MsgEvent<Object> messageEvent) {
        int code = messageEvent.getCode();
        if (code == MsgEvent.CODE_ON_CALL_ENDED) {
            hideFloatBox();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        unregisterReceiver(headsetPlugReceiver);  //注销监听
        releaseWakeLock();
        super.onDestroy();
        super.onDestroy();
        try {
            wm.removeView(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
        isStarted = false;
    }

    private void showFloatingWindow() {
        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        params = new WindowManager.LayoutParams();
        margin = SizeUtils.dp2px(10f);
        screenWidth = ScreenUtils.getScreenWidth();
        screenHeight = ScreenUtils.getScreenHeight();
        int type;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        params.type = type;
        params.flags = WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;

        params.format = PixelFormat.TRANSLUCENT;
        params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.END | Gravity.TOP;
        view = LayoutInflater.from(this).inflate(R.layout.av_voip_float_view, null);
        view.setOnTouchListener(onTouchListener);
        params.x = margin;
        params.y = statusBarHeight;
        wm.addView(view, params);
        if (session.isAudioOnly()) {
            showAudioInfo();
        } else {
            showVideoInfo();
        }
        if (session == null) return;
        session.setSessionCallback(new CallSessionCallback() {
            @Override
            public void didCallEndWithReason(CallEndReason var1) {
                Log.d(TAG, "didCallEndWithReason");
                hideFloatBox();
            }

            @Override
            public void didChangeState(CallState var1) {

            }

            @Override
            public void didChangeMode(boolean isAudioOnly) {
                handler.post(() -> showAudioInfo());
            }

            @Override
            public void didCreateLocalVideoTrack() {

            }

            @Override
            public void didReceiveRemoteVideoTrack(String userId) {

            }

            @Override
            public void didUserLeave(String userId) {
                hideFloatBox();
            }

            @Override
            public void didError(String error) {
                hideFloatBox();
            }

            @Override
            public void didDisconnected(String userId) {
                hideFloatBox();
            }
        });

    }

    private void hideFloatBox() {
        stopSelf();
    }

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        int startX = 0;
        int startY = 0;//起始点 = 0
        boolean isPerformClick = false;//是否点击
        int finalMoveX = 0;//最后通过动画将v的X轴坐标移动到finalMoveX


        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            Log.d("click", "onTouch: " + event.getAction());
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    startX = (int) event.getX();
                    startY = (int) event.getY();
                    isPerformClick = true;
                    return true;
                }
                case MotionEvent.ACTION_MOVE: {
                    //判断是CLICK还是MOVE
                    //只要移动过，就认为不是点击
                    if (Math.abs(startX - event.getX()) >= touchSlop || Math.abs(startY - event.getY()) >= touchSlop) {
                        isPerformClick = false;
                    }
//                    LogUtil.d(TAG, "event.rawX = " + event.rawX + "; startX = " + startX)
                    params.x = screenWidth - (int) (event.getRawX() - startX) - view.getWidth();
                    //这里修复了刚开始移动的时候，悬浮窗的y坐标是不正确的，要减去状态栏的高度
                    params.y = (int) (event.getRawY() - startY - statusBarHeight);
                    if (params.x < margin) params.x = margin;
                    if (params.x > screenWidth - margin) params.x = screenWidth - margin;

                    if (params.y + view.getHeight() + statusBarHeight > screenHeight - margin)
                        params.y =
                                screenHeight - statusBarHeight - view.getHeight();

//                    LogUtil.d(TAG, "x---->" + params.x)
//                    LogUtil.d(TAG, "y---->" + params.y)
                    updateViewLayout(); //更新v 的位置
                    return true;
                }
                case MotionEvent.ACTION_UP: {
                    if (isPerformClick) {
                        v.performClick();
                        clickToResume();
                    }

                    //判断v是在Window中的位置，以中间为界
                    if (params.x + v.getMeasuredWidth() / 2 >= wm.getDefaultDisplay().getWidth() / 2) {
                        finalMoveX = wm.getDefaultDisplay().getWidth() - v.getMeasuredWidth() - margin;
                    } else {
                        finalMoveX = margin;
                    }
                    stickToSide();
                    return !isPerformClick;
                }
            }
            return false;
        }

        private void stickToSide() {
            ValueAnimator animator =
                    ValueAnimator.ofInt(params.x, finalMoveX).setDuration(Math.abs(params.x - finalMoveX));
            animator.setInterpolator(new BounceInterpolator());
            animator.addUpdateListener(animation -> {
                params.x = (int) animation.getAnimatedValue();
                updateViewLayout();
            });
            animator.start();
        }
    };

    private void clickToResume() {
        startActivity(resumeActivityIntent);
        hideFloatBox();
    }

    private void updateViewLayout() {
        if (wm != null && view != null) {
            wm.updateViewLayout(view, params);
        }
    }

    private void refreshCallDurationInfo(TextView timeView) {
        CallSession session = SkyEngineKit.Instance().getCurrentSession();
        if (session == null || !session.isAudioOnly()) {
            return;
        }
        long duration = (System.currentTimeMillis() - session.getStartTime()) / 1000;
        if (duration >= 3600) {
            timeView.setText(String.format(
                    Locale.getDefault(), "%d:%02d:%02d",
                    duration / 3600, duration % 3600 / 60, duration % 60
            ));
        } else {
            timeView.setText(String.format(
                    Locale.getDefault(), "%02d:%02d",
                    duration % 3600 / 60, duration % 60
            ));
        }
        handler.postDelayed(() -> refreshCallDurationInfo(timeView), 1000);
    }


    private ViewGroup getFloatingView() {
        if (session == null) {
            return null;
        }
        LogUtils.dTag(TAG, "getFloatingView session.isAudioOnly() = " + session.isAudioOnly());
        if (session.isAudioOnly()) {
            if (audioView == null) {
                audioView = view.findViewById(R.id.audioLinearLayout);
            }
            return audioView;
        } else {
            if (videoView == null) {
                videoView = view.findViewById(R.id.remoteVideoFrameLayout);
            }
            return videoView;
        }
    }

    private void showAudioInfo() {
        floatingView = Objects.requireNonNull(getFloatingView());
        FrameLayout remoteVideoFrameLayout = view.findViewById(R.id.remoteVideoFrameLayout);
        if (remoteVideoFrameLayout.getVisibility() == View.VISIBLE) {
            remoteVideoFrameLayout.setVisibility(View.GONE);
            wm.removeView(view);
            wm.addView(view, params);
        }
        floatingView.setVisibility(View.VISIBLE);
        TextView timeV = view.findViewById(R.id.durationTextView);
        ImageView mediaIconV = view.findViewById(R.id.av_media_type);
        mediaIconV.setImageResource(R.drawable.av_float_audio);
        refreshCallDurationInfo(timeV);
        releaseWakeLock();
    }

    private void showVideoInfo() {
        newWakeLock();
        view.findViewById(R.id.audioLinearLayout).setVisibility(View.GONE);
        floatingView = Objects.requireNonNull(getFloatingView());
        floatingView.setVisibility(View.VISIBLE);
        View surfaceView = session.setupRemoteVideo(session.mTargetId, true);
        if (surfaceView != null) {
            if (surfaceView.getParent() != null) {
                ((ViewGroup) (surfaceView.getParent())).removeView(surfaceView);
            }
            floatingView.removeAllViews();
            floatingView.addView(surfaceView);
        }
    }


    private void newWakeLock() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Log.i(TAG, "setScreenOff: 熄灭屏幕");
            if (wakeLock == null) {
                wakeLock = ((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(
                        PowerManager.PARTIAL_WAKE_LOCK,
                        "$TAG:mywakelocktag"
                );
            }
            wakeLock.acquire(1200 * 60 * 1000L /*20 hours*/);
        }
    }

    private void releaseWakeLock() {
        if (wakeLock != null) {
            wakeLock.setReferenceCounted(false);
            wakeLock.release();
            wakeLock = null;
        }
    }

    class HeadsetPlugReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("state")) {
                if (session != null) {
                    if (intent.getIntExtra("state", 0) == 0) { //拔出耳机
                        session.toggleHeadset(false);
                    } else if (intent.getIntExtra("state", 0) == 1) { //插入耳机
                        session.toggleHeadset(true);
                    }
                }
            }
        }
    }
}