package com.dds.java;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

/**
 * Created by dds on 2019/7/26.
 * android_shuai@163.com
 */
public class TestWebSocket extends WebSocketClient {
    private final static String TAG = "dds_TestWebSocket";
    IEvent iEvent;

    public TestWebSocket(URI serverUri, IEvent event) {
        super(serverUri);
        this.iEvent = event;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        Log.e("dds_test", "onOpen");
        this.iEvent.onOpen();
    }

    @Override
    public void onMessage(String message) {
        Log.d(TAG, message);
        handleMessage(message);


    }

    private void handleMessage(String message) {

    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.e("dds_test", "onClose:" + reason + "remote:" + remote);
        this.iEvent.logout("onClose");

    }

    @Override
    public void onError(Exception ex) {
        Log.e("dds_test", "onError:" + ex.toString());
        this.iEvent.logout("onError");

    }
}
