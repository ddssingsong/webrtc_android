package com.dds;

import android.app.Activity;

import com.dds.webrtclib.ChatRoomActivity;
import com.dds.webrtclib.MyIceServer;

/**
 * Created by dds on 2019/1/7.
 * android_shuai@163.com
 */
public class WebrtcUtil {


    private static MyIceServer[] iceServers = {
            new MyIceServer("stun:47.254.34.146"),
            new MyIceServer("turn:47.254.34.146?transport=udp", "dds", "123456"),
            new MyIceServer("turn:47.254.34.146?transport=tcp", "dds", "123456")
    };
    private static String signal = "wss://47.254.34.146/wss";


    public static void call(Activity activity, String roomId) {
        ChatRoomActivity.openActivity(activity, signal, iceServers, roomId);
    }

    public static void callSingle(Activity activity, String roomId, boolean videoEnable) {
       // ChatSingleActivity.openActivity(activity, signal, iceServers, roomId, videoEnable);
    }


}
