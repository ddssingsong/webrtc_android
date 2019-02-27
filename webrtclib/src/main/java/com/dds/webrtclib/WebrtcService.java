package com.dds.webrtclib;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Chronometer;

import com.dds.webrtclib.utils.SettingsCompat;

/**
 * Created by dds on 2019/2/26.
 * android_shuai@163.com
 */
public class WebrtcService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    //悬浮窗
    private Chronometer nominator_tv;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayout;
    private View narrowView;

    //缩小悬浮框的设置
    public void createNarrowView() {
        if (SettingsCompat.canDrawOverlays(this)) {
            try {
                narrowView = LayoutInflater.from(this).inflate(R.layout.wr_netmonitor, null);
                mLayout = new WindowManager.LayoutParams();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mLayout.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mLayout.type = WindowManager.LayoutParams.TYPE_TOAST;
            } else {
                mLayout.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
            }
            mLayout.format = PixelFormat.RGBA_8888;
            mLayout.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_FULLSCREEN;
            mLayout.gravity = Gravity.LEFT | Gravity.TOP;
            mLayout.width = WindowManager.LayoutParams.WRAP_CONTENT;
            mLayout.height = WindowManager.LayoutParams.WRAP_CONTENT;
            mLayout.x = this.getResources().getDisplayMetrics().widthPixels;
            mLayout.y = 0;
            nominator_tv = narrowView.findViewById(R.id.chronometer);
            narrowView.setOnTouchListener(new View.OnTouchListener() {
                float downX = 0;
                float downY = 0;
                int oddOffsetX = 0;
                int oddOffsetY = 0;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            downX = event.getX();
                            downY = event.getY();
                            oddOffsetX = mLayout.x;
                            oddOffsetY = mLayout.y;
                            break;
                        case MotionEvent.ACTION_MOVE:
                            float moveX = event.getX();
                            float moveY = event.getY();
                            mLayout.x += (moveX - downX) / 3;
                            mLayout.y += (moveY - downY) / 3;
                            if (narrowView != null) {
                                mWindowManager.updateViewLayout(narrowView, mLayout);
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            int newOffsetX = mLayout.x;
                            int newOffsetY = mLayout.y;
                            if (Math.abs(newOffsetX - oddOffsetX) <= 20 && Math.abs(newOffsetY - oddOffsetY) <= 20) {
                                //开启Activity
                                openActivity();
                            }

                            break;
                    }

                    return true;

                }
            });
            addNarrow();
        } else {
            //如果没有开启悬浮窗权限


        }


    }

    // 开启聊天界面
    private void openActivity() {

    }

    public synchronized void addNarrow() {
        try {
            if (mWindowManager != null && narrowView != null && mLayout != null) {
                mWindowManager.addView(narrowView, mLayout);
                WebRTCManager.getInstance().toggleSpeaker(true);
                registerCallDurationTimer(null);
            }
        } catch (Exception e) {
            removeNarrow();

        }


    }

    public synchronized void removeNarrow() {
        if (mWindowManager != null && narrowView != null) {
            try {
                mWindowManager.removeView(narrowView);
                narrowView = null;
            } catch (Exception e) {
                //
            }

        }

    }

    private void registerCallDurationTimer(View v) {
        int callDuration = (int) WebRTCManager.getInstance().getTime();
        Chronometer timer = null;
        if (v == null) {
            timer = narrowView.findViewById(R.id.chronometer);
        }
        if (timer == null) {
            return;
        }
        timer.setBase(SystemClock.elapsedRealtime() - 1000 * callDuration);
        timer.start();
    }

}
