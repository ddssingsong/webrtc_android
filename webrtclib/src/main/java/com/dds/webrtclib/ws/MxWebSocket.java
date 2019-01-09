package com.dds.webrtclib.ws;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.webrtc.IceCandidate;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

/**
 * Created by dds on 2019/1/9.
 * android_shuai@163.com
 * 自定义协议
 */


public class MxWebSocket implements IWebSocket {
    private final static String TAG = "dds_MxWebSocket";

    private WebSocketClient mWebSocketClient;

    @Override
    public void connect(String wss, final String room) {
        URI uri;
        try {
            uri = new URI(wss);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }
        if (mWebSocketClient == null) {
            mWebSocketClient = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    joinRoom(room);
                }

                @Override
                public void onMessage(String message) {
                    handleMessage(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Log.e(TAG, "onClose:" + reason);
                }

                @Override
                public void onError(Exception ex) {
                    Log.e(TAG, ex.toString());
                }
            };
        }
        mWebSocketClient.connect();
    }

    @Override
    public void close() {

    }

    @Override
    public void joinRoom(String room) {

    }

    @Override
    public void handleMessage(String message) {

    }

    @Override
    public void sendIceCandidate(String socketId, IceCandidate iceCandidate) {

    }

    @Override
    public void sendAnswer(String socketId, String sdp) {

    }

    @Override
    public void sendOffer(String socketId, String sdp) {

    }

    // 忽略证书
    class TrustManagerTest implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

}
