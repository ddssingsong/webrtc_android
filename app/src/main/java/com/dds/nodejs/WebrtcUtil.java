package com.dds.nodejs;

import android.app.Activity;
import android.text.TextUtils;

import com.dds.webrtclib.WebRTCManager;
import com.dds.webrtclib.bean.MediaType;
import com.dds.webrtclib.bean.MyIceServer;
import com.dds.webrtclib.ui.ChatRoomActivity;
import com.dds.webrtclib.ui.ChatSingleActivity;
import com.dds.webrtclib.ws.IConnectEvent;

/**
 * Created by dds on 2019/1/7.
 * android_shuai@163.com
 */
public class WebrtcUtil {


    // turn and stun
    private static MyIceServer[] iceServers = {

            new MyIceServer("stun:stun.l.google.com:19302"),

            // 测试地址0
            new MyIceServer("stun:global.stun.twilio.com:3478?transport=udp"),
            new MyIceServer("turn:global.turn.twilio.com:3478?transport=udp",
                    "79fdd6b3c57147c5cc44944344c69d85624b63ec30624b8674ddc67b145e3f3c",
                    "xjfTOLkVmDtvFDrDKvpacXU7YofAwPg6P6TXKiztVGw"),
            new MyIceServer("turn:global.turn.twilio.com:3478?transport=tcp",
                    "79fdd6b3c57147c5cc44944344c69d85624b63ec30624b8674ddc67b145e3f3c",
                    "xjfTOLkVmDtvFDrDKvpacXU7YofAwPg6P6TXKiztVGw"),

            // 测试地址1
            new MyIceServer("stun:118.25.25.147:3478?transport=udp"),
            new MyIceServer("turn:118.25.25.147:3478?transport=udp",
                    "ddssingsong",
                    "123456"),
            new MyIceServer("turn:118.25.25.147:3478?transport=tcp",
                    "ddssingsong",
                    "123456"),
            // 测试地址2
            new MyIceServer("turn:157.255.51.168:3478?transport=udp",
                    "ddssingsong",
                    "123456"),
            new MyIceServer("turn:157.255.51.168:3478?transport=tcp",
                    "ddssingsong",
                    "123456")

    };

    // signalling
    private static String WSS = "wss://157.255.51.168/wss";
    //本地测试信令地址
    // private static String WSS = "ws://192.168.1.138:3000";

    // one to one
    public static void callSingle(Activity activity, String wss, String roomId, boolean videoEnable) {
        if (TextUtils.isEmpty(wss)) {
            wss = WSS;
        }
        WebRTCManager.getInstance().init(wss, iceServers, new IConnectEvent() {
            @Override
            public void onSuccess() {
                ChatSingleActivity.openActivity(activity, videoEnable);
            }

            @Override
            public void onFailed(String msg) {

            }
        });
        WebRTCManager.getInstance().connect(videoEnable ? MediaType.TYPE_VIDEO : MediaType.TYPE_AUDIO, roomId);
    }

    // Videoconferencing
    public static void call(Activity activity, String wss, String roomId) {
        if (TextUtils.isEmpty(wss)) {
            wss = WSS;
        }
        WebRTCManager.getInstance().init(wss, iceServers, new IConnectEvent() {
            @Override
            public void onSuccess() {
                ChatRoomActivity.openActivity(activity);
            }

            @Override
            public void onFailed(String msg) {

            }
        });
        WebRTCManager.getInstance().connect(MediaType.TYPE_MEETING, roomId);
    }


}
