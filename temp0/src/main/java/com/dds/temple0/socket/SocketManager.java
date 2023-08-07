package com.dds.temple0.socket;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class SocketManager {
    private static final String TAG = "SocketManager";

    private MyWebSocket webSocket;

    public final Map<Object, WeakReference<SocketManager.IUserStateEvent>> userStateEvents = new Hashtable<>();

    public final Map<Object, WeakReference<SocketManager.IEvent>> iEventMap = new Hashtable<>();

    private String myId;


    private SocketManager() {

    }

    private static class Holder {
        private static final SocketManager socketManager = new SocketManager();
    }

    public static SocketManager getInstance() {
        return Holder.socketManager;
    }


    // region -------------------------public-------------------------------


    public void addUserStateCallback(Object object, SocketManager.IUserStateEvent event) {
        if (!userStateEvents.containsKey(object)) {
            userStateEvents.put(object, new WeakReference<>(event));
        }

    }

    public void removeUserStateCallback(Object object) {
        userStateEvents.remove(object);

    }

    public void login(String url, String userId, int device) {
        if (webSocket == null || !webSocket.isOpen()) {
            URI uri;
            try {
                String urls = url + "/" + userId + "/" + device;
                uri = new URI(urls);
            } catch (URISyntaxException e) {
                e.printStackTrace();
                return;
            }
            webSocket = new MyWebSocket(uri, this);
            // 设置wss
            if (url.startsWith("wss")) {
                try {
                    SSLContext sslContext = SSLContext.getInstance("TLS");
                    if (sslContext != null) {
                        sslContext.init(null, new TrustManager[]{new TrustManagerTest()}, new SecureRandom());
                    }

                    SSLSocketFactory factory = null;
                    if (sslContext != null) {
                        factory = sslContext.getSocketFactory();
                    }

                    if (factory != null) {
                        webSocket.setSocket(factory.createSocket());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // 开始connect
            webSocket.connect();
        }
    }

    public boolean isLogin() {
        return webSocket != null && webSocket.isOpen() && webSocket.getSocketState() == MyWebSocket.SocketState.CONNECTED;
    }

    public void logout() {
        if (webSocket != null) {
            webSocket.close();
            webSocket = null;
        }

    }


    public void addEventCallback(Object object, SocketManager.IEvent event) {
        if (!iEventMap.containsKey(object)) {
            iEventMap.put(object, new WeakReference<>(event));
        }

    }

    public void removeEventCallback(Object object) {
        iEventMap.remove(object);
    }

    public void createRoom(String room, int roomSize) {
        if (webSocket != null) {
            webSocket.createRoom(room, roomSize, myId);
        }

    }

    public void sendInvite(String room, List<String> users, boolean audioOnly) {
        if (webSocket != null) {
            webSocket.sendInvite(room, myId, users, audioOnly);
        }
    }

    public void sendLeave(String room, String userId) {
        if (webSocket != null) {
            webSocket.sendLeave(myId, room, userId);
        }
    }

    public void sendRingBack(String targetId, String room) {
        if (webSocket != null) {
            webSocket.sendRing(myId, targetId, room);
        }
    }

    public void sendRefuse(String room, String inviteId, int refuseType) {
        if (webSocket != null) {
            webSocket.sendRefuse(room, inviteId, myId, refuseType);
        }
    }

    public void sendCancel(String mRoomId, List<String> userIds) {
        if (webSocket != null) {
            webSocket.sendCancel(mRoomId, myId, userIds);
        }
    }

    public void sendJoin(String room) {
        if (webSocket != null) {
            webSocket.sendJoin(room, myId);
        }
    }

    public void sendOffer(String userId, String sdp) {
        if (webSocket != null) {
            webSocket.sendOffer(myId, userId, sdp);
        }
    }

    public void sendAnswer(String userId, String sdp) {
        if (webSocket != null) {
            webSocket.sendAnswer(myId, userId, sdp);
        }
    }

    public void sendIceCandidate(String userId, String id, int label, String candidate) {
        if (webSocket != null) {
            webSocket.sendIceCandidate(myId, userId, id, label, candidate);
        }
    }

    public void sendTransAudio(String userId) {
        if (webSocket != null) {
            webSocket.sendTransAudio(myId, userId);
        }
    }

    public void sendDisconnect(String room, String userId) {
        if (webSocket != null) {
            webSocket.sendDisconnect(room, myId, userId);
        }
    }

    // endregion

    public interface IUserStateEvent {

        void userLogin(String userId);

        void userLogout(String info);
    }

    public interface IEvent {
        void onPeers(String userId, String userList, int roomSize);

        void onNewPeer(String userId);

        // receiver

        /**
         * The other party invites you to join the room, which may be a double room or multiple people
         *
         * @param room      roomId
         * @param audioOnly audioOnly
         * @param inviteId  Invite's ID
         * @param userList  ID of other people in the room
         */
        void onInvite(String room, boolean audioOnly, String inviteId, String userList);

        /**
         * The other party cancel invited
         *
         * @param inviteId inviteId
         */
        void onCancel(String inviteId);

        void onLeave(String userId);

        void onTransAudio(String userId);

        void onDisConnect(String userId);

        void onReject(String userId, int type);

        void onRing(String userId);

        // onOffer
        void onOffer(String userId, String sdp);

        // onAnswer
        void onAnswer(String userId, String sdp);

        // ice-candidate
        void onIceCandidate(String userId, String id, int label, String candidate);

    }

    // 忽略证书
    @SuppressLint("CustomX509TrustManager")
    public static class TrustManagerTest implements X509TrustManager {

        @SuppressLint("TrustAllX509TrustManager")
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

        }

        @SuppressLint("TrustAllX509TrustManager")
        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

}
