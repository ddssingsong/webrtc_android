package com.dds.java;

import android.util.Log;

import com.dds.webrtclib.ws.JavaWebSocket;

import java.io.IOException;
import java.lang.ref.WeakReference;
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
    private DWebSocket webSocket;
    private int userState;

    private SocketManager() {

    }

    private static class Holder {
        private static SocketManager socketManager = new SocketManager();
    }

    public static SocketManager getInstance() {
        return Holder.socketManager;
    }

    public void connect(String url, String userId, int device) {
        if (webSocket == null || !webSocket.isOpen()) {
            URI uri;
            try {
                String urls = url + "/" + userId + "/" + device;
                uri = new URI(urls);
            } catch (URISyntaxException e) {
                e.printStackTrace();
                return;
            }
            webSocket = new DWebSocket(uri, this);
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


    }

    public void unConnect() {
        if (webSocket != null) {
            webSocket.close();
            webSocket = null;
        }

    }

    @Override
    public void onOpen() {
        Log.i(TAG, "socket is open!");

    }


    @Override
    public void loginSuccess(String json) {
        Log.i(TAG, "loginSuccess:" + json);
        userState = 1;
        if (iUserState != null && iUserState.get() != null) {
            iUserState.get().userLogin();
        }
    }

    @Override
    public void onInvite(String room, int roomSize, int mediaType, String inviteId, String userList) {

    }

    @Override
    public void onCancel(String inviteId) {

    }

    @Override
    public void onRing(String userId) {

    }

    @Override
    public void onNewPeer(String myId, String userList) {

    }

    @Override
    public void onReject(String userId, int type) {

    }

    @Override
    public void onOffer(String userId, String sdp) {

    }

    @Override
    public void onAnswer(String userId, String sdp) {

    }

    @Override
    public void onIceCandidate(String userId, String id, String label, String candidate) {

    }

    @Override
    public void logout(String str) {
        Log.i(TAG, "logout:" + str);
        userState = 0;
        if (iUserState != null && iUserState.get() != null) {
            iUserState.get().userLogout();
        }
    }

    public int getUserState() {
        return userState;
    }


    private WeakReference<IUserState> iUserState;

    public void addUserStateCallback(IUserState userState) {
        iUserState = new WeakReference<>(userState);
    }

}
