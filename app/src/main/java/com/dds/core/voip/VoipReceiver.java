package com.dds.core.voip;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.dds.App;
import com.dds.core.util.ActivityStackManager;
import com.dds.permission.Permissions;
import com.dds.skywebrtc.SkyEngineKit;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnCancelListener;
import com.lxj.xpopup.interfaces.OnConfirmListener;

/**
 * Created by dds on 2019/8/25.
 * android_shuai@163.com
 */
public class VoipReceiver extends BroadcastReceiver {
    private static final String TAG = "VoipReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Utils.ACTION_VOIP_RECEIVER.equals(action)) {
            String room = intent.getStringExtra("room");
            boolean audioOnly = intent.getBooleanExtra("audioOnly", true);
            String inviteId = intent.getStringExtra("inviteId");
            String userList = intent.getStringExtra("userList");
            String[] list = userList.split(",");
            SkyEngineKit.init(new VoipEvent());
            // 权限检测
            String[] per;
            if (audioOnly) {
                per = new String[]{Manifest.permission.RECORD_AUDIO};
            } else {
                per = new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA};
            }
            Activity activity = ActivityStackManager.getInstance().getTopActivity();
            if (Permissions.has(activity, per)) {
                onHasPermission(activity, room, list, inviteId, false, audioOnly);
            } else {
                new XPopup.Builder(activity).asConfirm("来电通知", "您收到" + inviteId + "的来电邀请，请允许"
                        + (audioOnly ? "录音" : "录音和相机") + "权限来通话", () -> {
                    Permissions.request(activity, per, integer -> {
                        Log.d(TAG, "Permissions.request integer = " + integer);
                        if (integer == 0) {
                            // 权限同意
                            onHasPermission(activity, room, list, inviteId, false, audioOnly);
                        } else {
                            onPermissionDenied(room, inviteId);
                        }
                    });
                }, () -> {
                    onPermissionDenied(room, inviteId);
                }).show();

            }


        }

    }

    private void onHasPermission(Context context, String room, String[] list, String inviteId, boolean isOutgoing, boolean audioOnly) {
        boolean b = SkyEngineKit.Instance().startInCall(App.getInstance(), room, inviteId, audioOnly);
        if (b) {
            App.getInstance().setRoomId(room);
            App.getInstance().setOtherUserId(inviteId);
            if (list.length == 1) {
                CallSingleActivity.openActivity(context, inviteId, isOutgoing, audioOnly);
            } else {
                // 群聊
            }
        }
    }

    // 权限拒绝
    private void onPermissionDenied(String room, String inviteId) {
        SkyEngineKit.Instance().sendRefuseOnPermissionDenied(room, inviteId);//通知对方结束
        Toast.makeText(App.getInstance(), "权限被拒绝，无法通话", Toast.LENGTH_SHORT).show();
    }
}
