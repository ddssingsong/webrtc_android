package com.dds;

import android.app.Activity;
import android.util.Log;

import com.dds.webrtclib.ChatRoomActivity;
import com.dds.webrtclib.ChatSingleActivity;
import com.dds.webrtclib.MyIceServer;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by dds on 2019/1/7.
 * android_shuai@163.com
 */
public class WebrtcUtil {
    private final static String TAG = "WebrtcUtil";
    private static WebSocketClient mWebSocketClient;

    private static MyIceServer[] iceServers = {
            new MyIceServer("stun:stun.l.google.com:19302")
//            new MyIceServer("stun:47.254.34.146"),
//            new MyIceServer("turn:47.254.34.146?transport=udp", "dds", "123456"),
//            new MyIceServer("turn:47.254.34.146?transport=tcp", "dds", "123456"),
    };
    private static String signal = "ws://192.168.1.122:3000";


    public static void call(Activity activity, String roomId) {
        ChatRoomActivity.openActivity(activity, signal, iceServers, roomId);
    }

    public static void callSingle(Activity activity, String roomId, boolean videoEnable) {
        ChatSingleActivity.openActivity(activity, signal, iceServers, roomId, videoEnable);
    }

    public static void testWs(String wss) {
        URI uri;
        try {
            uri = new URI(wss);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }
        mWebSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake handshake) {
                Log.e(TAG, "onOpen:");
                mWebSocketClient.send("hello");
            }

            @Override
            public void onMessage(String message) {
                Log.e(TAG, "onMessage:" + message);
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                Log.e(TAG, "onClose:" + reason);
            }

            @Override
            public void onError(Exception ex) {
                Log.e(TAG, "onError:");
                Log.e(TAG, ex.toString());
            }
        };
        mWebSocketClient.connect();
    }


}
