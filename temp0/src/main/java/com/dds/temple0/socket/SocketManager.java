package com.dds.temple0.socket;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class SocketManager {
    private static final String TAG = "SocketManager";

    private WebSocketClient webSocket;

    private CopyOnWriteArrayList<IUserStateEvent> userStateEvents= new CopyOnWriteArrayList<>();
    private IUserStateEvent iUserState;
    private IEvent iEvent;


    public interface SocketState {
        int IDLE = 0;
        int CONNECTED = 1;
        int LOGIN = 2;
    }

    private int socketState = SocketState.IDLE;

    private String myId;

    private SocketManager() {

    }

    private static class Holder {
        private static final SocketManager socketManager = new SocketManager();
    }

    public static SocketManager getInstance() {
        return Holder.socketManager;
    }

    public void addUserStateCallback(IUserStateEvent event) {
        this.iUserState = event;
    }

    public void removeUserStateCallback(IUserStateEvent event){

    }


    // region -------------------------public-------------------------------

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
            webSocket = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    Log.i(TAG, "socket is open!");
                    socketState = SocketState.CONNECTED;
                }

                @Override
                public void onMessage(String message) {
                    Log.d(TAG, "onMessage: " + message);
                    handleMessage(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Log.d(TAG, "onClose: ");
                }

                @Override
                public void onError(Exception ex) {
                    Log.d(TAG, "onError: " + ex);
                }
            };
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

    public void logout() {
        if (webSocket != null) {
            webSocket.close();
            webSocket = null;
        }

    }

    public boolean isLogin() {
        return socketState == SocketState.LOGIN;
    }

    // endregion

    // region -------------------------private-------------------------------

    private void handleMessage(String message) {
        try {
            JSONObject map = new JSONObject(message);
            String eventName = map.optString("eventName");
            if (TextUtils.isEmpty(eventName)) return;
            // 登录成功
            if (eventName.equals("__login_success")) {
                handleLogin(map);
                return;
            }
            // 被邀请
            if (eventName.equals("__invite")) {
                handleInvite(map);
                return;
            }
            // 取消拨出
            if (eventName.equals("__cancel")) {
                handleCancel(map);
                return;
            }
            // 响铃
            if (eventName.equals("__ring")) {
                handleRing(map);
                return;
            }
            // 进入房间
            if (eventName.equals("__peers")) {
                handlePeers(map);
                return;
            }
            // 新人入房间
            if (eventName.equals("__new_peer")) {
                handleNewPeer(map);
                return;
            }
            // 拒绝接听
            if (eventName.equals("__reject")) {
                handleReject(map);
                return;
            }
            // offer
            if (eventName.equals("__offer")) {
                handleOffer(map);
                return;
            }
            // answer
            if (eventName.equals("__answer")) {
                handleAnswer(map);
                return;
            }
            // ice-candidate
            if (eventName.equals("__ice_candidate")) {
                handleIceCandidate(map);
            }
            // 离开房间
            if (eventName.equals("__leave")) {
                handleLeave(map);
            }
            // 切换到语音
            if (eventName.equals("__audio")) {
                handleTransAudio(map);
            }
            // 意外断开
            if (eventName.equals("__disconnect")) {
                handleDisConnect(map);
            }

        } catch (JSONException e) {
            Log.e(TAG, "handleMessage: " + e);
        }
    }

    private void handleLogin(JSONObject map) {
        try {
            JSONObject data = map.optJSONObject("data");
            if (data != null) {
                String userID = (String) data.get("userID");
                this.iUserState.userLogin(userID);
            }

        } catch (JSONException e) {
            Log.e(TAG, "handleLogin: " + e);
        }


    }

    private void handleDisConnect(JSONObject map) {
        try {
            JSONObject data = map.optJSONObject("data");
            if (data != null) {
                String fromId = (String) data.get("fromID");
                this.iEvent.onDisConnect(fromId);
            }
        } catch (JSONException e) {
            Log.d(TAG, "handleDisConnect: " + e);
        }

    }

    private void handleTransAudio(JSONObject map) {
        try {
            JSONObject data = map.optJSONObject("data");
            if (data != null) {
                String fromId = (String) data.get("fromID");
                this.iEvent.onTransAudio(fromId);
            }
        } catch (JSONException e) {
            Log.d(TAG, "handleTransAudio: " + e);
        }
    }

    private void handleReject(JSONObject map) {
        try {
            JSONObject data = map.optJSONObject("data");
            if (data != null) {
                String fromID = (String) data.get("fromID");
                int rejectType = Integer.parseInt(String.valueOf(data.get("refuseType")));
                this.iEvent.onReject(fromID, rejectType);
            }
        } catch (JSONException e) {
            Log.d(TAG, "handleReject: " + e);
        }
    }

    private void handlePeers(JSONObject map) {
        try {
            JSONObject data = map.optJSONObject("data");
            if (data != null) {
                String you = (String) data.get("you");
                String connections = (String) data.get("connections");
                int roomSize = (int) data.get("roomSize");
                this.iEvent.onPeers(you, connections, roomSize);
            }
        } catch (JSONException e) {
            Log.d(TAG, "handleTransAudio: " + e);
        }
    }

    private void handleNewPeer(JSONObject map) {
        try {
            JSONObject data = map.optJSONObject("data");
            if (data != null) {
                String userID = (String) data.get("userID");
                this.iEvent.onNewPeer(userID);
            }
        } catch (JSONException e) {
            Log.d(TAG, "handleTransAudio: " + e);
        }

    }

    private void handleRing(JSONObject map) {
        try {
            JSONObject data = map.optJSONObject("data");
            if (data != null) {
                String fromId = (String) data.get("fromID");
                this.iEvent.onRing(fromId);
            }
        } catch (JSONException e) {
            Log.d(TAG, "handleTransAudio: " + e);
        }

    }

    private void handleCancel(JSONObject map) {
        try {
            JSONObject data = map.optJSONObject("data");
            if (data != null) {
                String inviteID = (String) data.get("inviteID");
                this.iEvent.onCancel(inviteID);
            }
        } catch (JSONException e) {
            Log.d(TAG, "handleTransAudio: " + e);
        }
    }

    private void handleInvite(JSONObject map) {
        try {
            JSONObject data = map.optJSONObject("data");
            if (data != null) {
                String room = (String) data.get("room");
                boolean audioOnly = (boolean) data.get("audioOnly");
                String inviteID = (String) data.get("inviteID");
                String userList = (String) data.get("userList");
                this.iEvent.onInvite(room, audioOnly, inviteID, userList);
            }
        } catch (JSONException e) {
            Log.d(TAG, "handleTransAudio: " + e);
        }
    }

    private void handleLeave(JSONObject map) {
        try {
            JSONObject data = map.optJSONObject("data");
            if (data != null) {
                String fromID = (String) data.get("fromID");
                this.iEvent.onLeave(fromID);
            }
        } catch (JSONException e) {
            Log.d(TAG, "handleTransAudio: " + e);
        }
    }

    private void handleIceCandidate(JSONObject map) {
        try {
            JSONObject data = map.optJSONObject("data");
            if (data != null) {
                String userID = (String) data.get("fromID");
                String id = (String) data.get("id");
                int label = (int) data.get("label");
                String candidate = (String) data.get("candidate");
                this.iEvent.onIceCandidate(userID, id, label, candidate);
            }
        } catch (JSONException e) {
            Log.d(TAG, "handleTransAudio: " + e);
        }

    }

    private void handleAnswer(JSONObject map) {
        try {
            JSONObject data = map.optJSONObject("data");
            if (data != null) {
                String sdp = (String) data.get("sdp");
                String userID = (String) data.get("fromID");
                this.iEvent.onAnswer(userID, sdp);
            }
        } catch (JSONException e) {
            Log.d(TAG, "handleTransAudio: " + e);
        }

    }

    private void handleOffer(JSONObject map) {
        try {
            JSONObject data = map.optJSONObject("data");
            if (data != null) {
                String sdp = (String) data.get("sdp");
                String userID = (String) data.get("fromID");
                this.iEvent.onOffer(userID, sdp);
            }
        } catch (JSONException e) {
            Log.d(TAG, "handleTransAudio: " + e);
        }
    }

    // endregion

    public interface IUserStateEvent {

        void userLogin(String userId);

        void userLogout();
    }

    private interface IEvent {

        /**
         * The other party invites you to join the room, which may be a double room or multiple people
         * @param room roomId
         * @param audioOnly audioOnly
         * @param inviteId  Invite's ID
         * @param userList  ID of other people in the room
         */
        void onInvite(String room, boolean audioOnly, String inviteId, String userList);

        /**
         * The other party cancel invited
         * @param inviteId inviteId
         */
        void onCancel(String inviteId);

        void onRing(String userId);

        void onPeers(String myId, String userList, int roomSize);

        void onNewPeer(String myId);

        void onReject(String userId, int type);

        void onLeave(String userId);

        void onTransAudio(String userId);

        void onDisConnect(String userId);

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
