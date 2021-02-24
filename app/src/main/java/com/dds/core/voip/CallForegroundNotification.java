package com.dds.core.voip;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.dds.core.util.ActivityStackManager;
import com.dds.webrtc.R;

import java.util.Random;

/**
 * <pre>
 *     author : Jasper
 *     e-mail : 229605030@qq.com
 *     time   : 2021/02/01
 *     desc   :
 * </pre>
 */
public class CallForegroundNotification extends ContextWrapper {
    private static final String TAG = "CallForegroundNotificat";
    private static final String id = "channel1";
    private static final String name = "voip";
    private NotificationManager manager;

    public CallForegroundNotification(Context base) {
        super(base);
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @RequiresApi(api = 26)
    public void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH);
        channel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
        manager.createNotificationChannel(channel);
    }

    public void sendRequestIncomingPermissionsNotification(
            Context context, String room, String userList, String inviteId, String inviteUserName, Boolean isAudioOnly
    ) {
        clearAllNotification();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }
        //发送广播，调起接听界面
        Intent intent = new Intent(context, ActivityStackManager.getInstance().getBottomActivity().getClass()); //栈底是MainActivity
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("room", room);
        intent.putExtra("isFromCall", true);
        intent.putExtra("audioOnly", isAudioOnly);
        intent.putExtra("inviteUserName", inviteUserName);
        intent.putExtra("inviteId", inviteId);
        intent.putExtra("userList", userList);
        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(this, new Random().nextInt(100), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, id)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setTicker(
                        "您收到" + inviteUserName + "的来电邀请，请允许"
                                + (isAudioOnly ? "录音" :
                                "录音和相机") + "权限来通话"
                )
                .setContentText("您收到" + inviteUserName + "的来电邀请，请允许"
                        + (isAudioOnly ? "录音" :
                        "录音和相机") + "权限来通话"
                )
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(Notification.CATEGORY_CALL);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationBuilder.setFullScreenIntent(fullScreenPendingIntent, true);
        } else {
            notificationBuilder.setContentIntent(fullScreenPendingIntent);
        }
//
        manager.notify(10086, notificationBuilder.build());
    }

    public void sendIncomingCallNotification(
            Context context, String targetId, Boolean isOutgoing, String inviteUserName,
            Boolean isAudioOnly, Boolean isClearTop
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            clearAllNotification();
            createNotificationChannel();
            Notification notification = getChannelNotificationQ(context, targetId, isOutgoing, inviteUserName, isAudioOnly, isClearTop);
            manager.notify(10086, notification);
        }
    }

    private void clearAllNotification() {
        manager.cancelAll();
    }

    private Notification getChannelNotificationQ(
            Context context, String targetId, Boolean isOutgoing, String inviteUserName,
            Boolean isAudioOnly, Boolean isClearTop
    ) {
        Intent fullScreenIntent = CallSingleActivity.getCallIntent(context, targetId, isOutgoing, inviteUserName, isAudioOnly, isClearTop);
        fullScreenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(this, new Random().nextInt(100), fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, id)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setTicker(inviteUserName + "来电")
                .setContentText(inviteUserName + "来电")
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(Notification.CATEGORY_CALL)
                .setFullScreenIntent(fullScreenPendingIntent, true);

        Log.d(TAG, "getChannelNotificationQ");
        return notificationBuilder.build();
    }


}
