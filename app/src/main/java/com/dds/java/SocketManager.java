package com.dds.java;

import android.util.Log;

import com.dds.webrtclib.ws.JavaWebSocket;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

/**
 * Created by dds on 2019/7/26.
 * android_shuai@163.com
 */
public class SocketManager implements IEvent {
    private final static String TAG = "dds_SocketManager";
    private TestWebSocket webSocket;

    private SocketManager() {

    }

    private static class Holder {
        private static SocketManager socketManager = new SocketManager();
    }

    public static SocketManager getInstance() {
        return Holder.socketManager;
    }

    public void connect(String url, String userId, int device) {
        URI uri;
        try {
            String urls = url + "/" + userId + "/" + device;
            uri = new URI(urls);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }
        webSocket = new TestWebSocket(uri, this);
        // 设置wss
        if (url.startsWith("wss")) {
            try {
                SSLContext sslContext = SSLContext.getInstance("TLS");
                if (sslContext != null) {
                    sslContext.init(null, new TrustManager[]{new JavaWebSocket.TrustManagerTest()}, new SecureRandom());
                }

                SSLSocketFactory factory = null;
                if (sslContext != null) {
                    factory = sslContext.getSocketFactory();
                }

                if (factory != null) {
                    webSocket.setSocket(factory.createSocket());
                }
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // 开始connect
        webSocket.connect();

    }

    @Override
    public void onOpen() {
        Log.i(TAG, "socket is open!");

    }


    @Override
    public void loginSuccess(String json) {
        Log.i(TAG, "loginSuccess:" + json);
    }

    @Override
    public void logout(String str) {
        Log.i(TAG, "logout:" + str);
    }

}
