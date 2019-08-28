package com.dds.voip;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.dds.skywebrtc.AVEngineKit;

/**
 * Created by dds on 2019/8/25.
 * android_shuai@163.com
 */
public class VoipReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Utils.ACTION_VOIP_RECEIVER.equals(action)) {
            String room = intent.getStringExtra("room");
            int roomSize = intent.getIntExtra("roomSize", 2);
            int mediaType = intent.getIntExtra("mediaType", 0);
            String inviteId = intent.getStringExtra("inviteId");
            String userId = intent.getStringExtra("userList");


            AVEngineKit.init(new WebSocketEvent());

            AVEngineKit.Instance().receiveCall(context, room, roomSize, inviteId, mediaType == 0);


            SingleCallActivity.openActivity(context, inviteId, false, mediaType == 0);


        }

    }
}
