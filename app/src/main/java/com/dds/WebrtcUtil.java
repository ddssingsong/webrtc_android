package com.dds;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.dds.webrtclib.bean.MyIceServer;
import com.dds.webrtclib.WebRTCManager;
import com.dds.webrtclib.ui.ChatSingleActivity;
import com.dds.webrtclib.ws.IConnectEvent;

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

    // 转发和穿透服务器
    private static MyIceServer[] iceServers = {
            new MyIceServer("stun:stun.l.google.com:19302")
//            new MyIceServer("stun:47.254.34.146"),
//            new MyIceServer("turn:47.254.34.146?transport=udp", "dds", "123456"),
//            new MyIceServer("turn:47.254.34.146?transport=tcp", "dds", "123456"),
    };

    // 信令服务器
    private static String WSS = "wss://47.254.34.146/wss";


    // one to one
    public static void callSingle(Activity activity, String wss, String roomId, boolean videoEnable) {
        WebRTCManager.getInstance().init(
                videoEnable ? WebRTCManager.MediaType.Video : WebRTCManager.MediaType.Audio,
                roomId,
                new IConnectEvent() {
                    @Override
                    public void onSuccess() {
                        ChatSingleActivity.openActivity(activity, videoEnable);
                    }

                    @Override
                    public void onFailed(String msg) {
                        // 打开失败弹出失败原因
                        Toast.makeText(activity, "连接sokect失败", Toast.LENGTH_LONG).show();
                    }
                }
        );
        WebRTCManager.getInstance().connect(TextUtils.isEmpty(wss) ? WSS : wss, iceServers);
    }

    // meeting
    public static void call(Activity activity, String roomId) {
        // WebRTCManager.getInstance().init();
        //  ChatRoomActivity.openActivity(activity, signal, iceServers, roomId);
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
