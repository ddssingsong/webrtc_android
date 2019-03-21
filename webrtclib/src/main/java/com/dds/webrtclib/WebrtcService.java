package com.dds.webrtclib;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Chronometer;

import com.dds.webrtclib.ui.ChatRoomActivity;
import com.dds.webrtclib.ui.ChatSingleActivity;
import com.dds.webrtclib.ui.IncomingActivity;
import com.dds.webrtclib.utils.SettingsCompat;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

/**
 * Created by dds on 2019/2/26.
 * android_shuai@163.com
 */
public class WebrtcService extends Service {


    private static final String INCOMING = "incoming";
    private static final String CALLING = "person_calling";
    private static final String MEETING_CALLING = "meeting_calling";


    public static void incomingNotification(Context context) {
        Intent intent = new Intent(context, WebrtcService.class);
        intent.putExtra("type", INCOMING);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }


    public static void callingNotification(Context context) {
        Intent intent = new Intent(context, WebrtcService.class);
        intent.putExtra("type", CALLING);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    public static void callingMeetingNotification(Context context) {
        Intent intent = new Intent(context, WebrtcService.class);
        intent.putExtra("type", MEETING_CALLING);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    public static void destory(Context context) {
        Intent intent = new Intent(context, WebrtcService.class);
        context.stopService(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String type = intent.getStringExtra("type");
        if (type.equals(INCOMING)) {
            sendNotification(NOTIFY_INCOMING, "来电...");
        } else if (type.equals(CALLING)) {
            sendNotification(NOTIFY_CALL, "通话中...");
        } else if (type.equals(MEETING_CALLING)) {
            sendNotification(NOTIFY_MEETING_CALL, "会议通话中...");
        }
        return super.onStartCommand(intent, flags, startId);
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

    @Override
    public void onDestroy() {
        cancelNotification(NOTIFY_INCOMING);
        cancelNotification(NOTIFY_CALL);
        super.onDestroy();
    }


    // ============================================================================================
    private NotificationManager mNM;
    private static final String id = "channel_voip";
    private static final String name = "语音通话";
    private static final int NotifyId = 10000;
    public static final int NOTIFY_INCOMING = 100;
    public static final int NOTIFY_CALL = 300;
    public static final int NOTIFY_MEETING_CALL = 200;
    private static final String CHAT_TYPE = "chatType";

    //添加通知
    public void sendNotification(int type, String content) {
        mNM.cancel(NotifyId);
        createNotificationChannel();
        Notification notification = getNotification(type, getString(R.string.webrtc_chat_notify), content).build();
        mNM.notify(NotifyId, notification);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(NotifyId, notification);
        }
    }

    //消除通知
    public void cancelNotification(int type) {
        mNM.cancel(NotifyId);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            mNM.deleteNotificationChannel(id);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(Service.STOP_FOREGROUND_REMOVE);
        }
    }

    public void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH);
            mNM.deleteNotificationChannel(id);
            mNM.createNotificationChannel(channel);
        }

    }

    public NotificationCompat.Builder getNotification(int type, String title, String content) {
        Intent resultIntent = new Intent();
        if (type == NOTIFY_INCOMING) {
            resultIntent.setClass(this, IncomingActivity.class);
        } else if (type == NOTIFY_CALL) {
            resultIntent.setClass(this, ChatSingleActivity.class);
        } else if (type == NOTIFY_MEETING_CALL) {
            resultIntent.setClass(this, ChatRoomActivity.class);
        }
        resultIntent.putExtra(CHAT_TYPE, type);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), id)
                .setContentTitle(title)
                .setContentText(content)
                .setOngoing(true)
                .setAutoCancel(false)
                .setContentIntent(resultPendingIntent);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            builder.setSmallIcon(R.drawable.webrtc_answer);
        } else {
            builder.setSmallIcon(R.drawable.webrtc_answer);
        }
        return builder;

    }

}
