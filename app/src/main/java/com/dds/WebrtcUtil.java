package com.dds;

import android.app.Activity;

import com.dds.webrtclib.ChatRoomActivity;
import com.dds.webrtclib.ChatSingleActivity;

/**
 * Created by dds on 2019/1/7.
 * android_shuai@163.com
 */
public class WebrtcUtil {

    private static String stun = "stun:47.254.34.146:3478";
    private static String signal = "wss://47.254.34.146/wss";


    public static void call(Activity activity, String roomId) {
        ChatRoomActivity.openActivity(activity, signal, stun, roomId);
    }

    public static void callSingle(Activity activity, String roomId) {
        ChatSingleActivity.openActivity(activity, signal, stun, roomId);
    }

}
