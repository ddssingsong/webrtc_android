package com.dds.webrtclib.ws;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.dds.webrtclib.EnumMsg;
import com.dds.webrtclib.bean.MyIceServer;
import com.dds.webrtclib.bean.OtherInfo;
import com.dds.webrtclib.callback.ConnectCallback;
import com.dds.webrtclib.utils.AppRTCUtils;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.webrtc.IceCandidate;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by dds on 2019/1/9.
 * android_shuai@163.com
 * 自定义协议
 */
public class MxWebSocket implements IWebSocket {
    private final static String TAG = "dds_MxWebSocket";

    private WebSocketClient mWebSocketClient;
    private ISignalingEvents events;

    public MxWebSocket(ISignalingEvents events) {
        this.events = events;
    }

    @Override
    public void connect(String wss, final ConnectCallback callback) {
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
                    events.onWebSocketOpen();
                    if (callback != null) {
                        callback.onSuccess();
                    }
                }

                @Override
                public void onMessage(String message) {
                    Log.d(TAG, "onMessage:" + message);
                    handleMessage(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Log.e(TAG, "onClose:" + reason);
                    events.onError("onClose");
                    if (callback != null) {
                        callback.onFailed();
                    }

                }

                @Override
                public void onError(Exception ex) {
                    Log.e(TAG, ex.toString());
                    events.onError("onError:" + ex.toString());
                    if (callback != null) {
                        callback.onFailed();
                    }


                }
            };
        }
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
                mWebSocketClient.setSocket(factory.createSocket());
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mWebSocketClient.connect();
    }

    @Override
    public void close() {
        if (mWebSocketClient != null) {
            mWebSocketClient.close();
        }
    }


    //=================================上行数据=================================================
    @Override
    public void login(String sessionId) {
        Log.d(TAG, "login:" + AppRTCUtils.getThreadInfo());
        Map<String, Object> map = new HashMap<>();
        map.put("action", "user_login");
        Map<String, String> childMap = new HashMap<>();
        childMap.put("sessionID", sessionId);
        map.put("data", childMap);
        JSONObject object = new JSONObject(map);
        final String jsonString = object.toString();
        Log.d(TAG, "send:" + jsonString);
        mWebSocketClient.send(jsonString);


    }

    @Override
    public void createRoom(String ids, boolean videoEnable) {
        Log.d(TAG, "createRoom:" + AppRTCUtils.getThreadInfo());
        Map<String, Object> map = new HashMap<>();
        map.put("action", "room_create");
        Map<String, String> childMap = new HashMap<>();
        childMap.put("ids", ids);
        childMap.put("type", videoEnable ? "2" : "1");
        map.put("data", childMap);
        JSONObject object = new JSONObject(map);
        final String jsonString = object.toString();
        Log.d(TAG, "send:" + jsonString);
        mWebSocketClient.send(jsonString);
    }

    @Override
    public void createRoom(String ids, String groupID, List<OtherInfo> list) {
        Log.d(TAG, "createRoom:" + AppRTCUtils.getThreadInfo());
        List<Map<String, String>> childList = new ArrayList<>();
        for (OtherInfo info : list) {
            Map<String, String> childMap1 = new HashMap<>();
            childMap1.put("id", info.getId());
            childMap1.put("username", info.getId());
            childMap1.put("avatar", info.getId());
            childList.add(childMap1);
        }

        Map<String, Object> childMap = new HashMap<>();
        childMap.put("ids", ids);
        childMap.put("type", "2");
        childMap.put("groupID", groupID);
        childMap.put("info", childList);

        Map<String, Object> map = new HashMap<>();
        map.put("action", "room_create");
        map.put("data", childMap);
        JSONObject object = new JSONObject(map);
        final String jsonString = object.toString();
        Log.d(TAG, "send:" + jsonString);
        mWebSocketClient.send(jsonString);
    }

    @Override
    public void sendInvite(String userId) {
        Log.d(TAG, "sendInvite:" + AppRTCUtils.getThreadInfo());
        Map<String, Object> map = new HashMap<>();
        map.put("action", "user_invite");
        Map<String, String> childMap = new HashMap<>();
        childMap.put("toID", userId);
        map.put("data", childMap);
        JSONObject object = new JSONObject(map);
        final String jsonString = object.toString();
        Log.d(TAG, "send:" + jsonString);
        mWebSocketClient.send(jsonString);
    }

    @Override
    public void sendAck(String userId) {
        Log.d(TAG, "sendAck:" + AppRTCUtils.getThreadInfo());
        Map<String, Object> map = new HashMap<>();
        map.put("action", "user_ack");
        Map<String, String> childMap = new HashMap<>();
        childMap.put("toID", userId);
        map.put("data", childMap);
        JSONObject object = new JSONObject(map);
        final String jsonString = object.toString();
        Log.d(TAG, "send:" + jsonString);
        mWebSocketClient.send(jsonString);
    }

    @Override
    public void joinRoom(String room) {
        Log.d(TAG, "joinRoom:" + AppRTCUtils.getThreadInfo());
        Map<String, Object> map = new HashMap<>();
        map.put("action", "room_join");
        Map<String, String> childMap = new HashMap<>();
        childMap.put("room", room);
        map.put("data", childMap);
        JSONObject object = new JSONObject(map);
        final String jsonString = object.toString();
        Log.d(TAG, "send:" + jsonString);
        mWebSocketClient.send(jsonString);
    }

    @Override
    public void sendOffer(String socketId, String sdp) {
        HashMap<String, Object> childMap1 = new HashMap();
        childMap1.put("sdp", sdp);
        childMap1.put("type", "offer");

        HashMap<String, Object> childMap2 = new HashMap();
        childMap2.put("socketID", socketId);
        childMap2.put("sdp", childMap1);

        HashMap<String, Object> map = new HashMap();
        map.put("action", "user_sendOffer");
        map.put("data", childMap2);

        JSONObject object = new JSONObject(map);
        String jsonString = object.toString();
        Log.d(TAG, "send:" + jsonString);
        mWebSocketClient.send(jsonString);
    }

    @Override
    public void sendAnswer(String socketId, String sdp) {
        Map<String, Object> childMap1 = new HashMap();
        childMap1.put("sdp", sdp);
        childMap1.put("type", "answer");
        HashMap<String, Object> childMap2 = new HashMap();
        childMap2.put("socketID", socketId);
        childMap2.put("sdp", childMap1);
        HashMap<String, Object> map = new HashMap();
        map.put("action", "user_sendAnswer");
        map.put("data", childMap2);
        JSONObject object = new JSONObject(map);
        String jsonString = object.toString();
        mWebSocketClient.send(jsonString);
    }


    @Override
    public void sendIceCandidate(String socketId, IceCandidate iceCandidate) {
        HashMap<String, Object> childMap = new HashMap();
        HashMap<String, Object> map = new HashMap();
        map.put("action", "user_sendIceCandidate");

        childMap.put("id", iceCandidate.sdpMid);
        childMap.put("label", iceCandidate.sdpMLineIndex);
        childMap.put("candidate", iceCandidate.sdp);
        childMap.put("socketID", socketId);
        map.put("data", childMap);
        JSONObject object = new JSONObject(map);
        String jsonString = object.toString();
        Log.d(TAG, "send:" + jsonString);
        mWebSocketClient.send(jsonString);
    }

    @Override //拒绝接听
    public void decline(String toId, EnumMsg.Decline decline) {
        Map<String, String> childMap = new HashMap<>();
        childMap.put("toID", toId);
        Map<String, Object> map = new HashMap<>();
        map.put("action", "room_refuse");
        map.put("data", childMap);
        JSONObject object = new JSONObject(map);
        final String jsonString = object.toString();
        Log.d(TAG, "send:" + jsonString);
        mWebSocketClient.send(jsonString);
    }

    //=================================下行数据=================================================
    @Override
    public void handleMessage(String message) {
        Map map = JSON.parseObject(message, Map.class);
        String eventName = (String) map.get("action");
        switch (eventName) {
            case "user_login_success":
                handleUserLoginSuccess(map);
                break;
            case "room_create_success":
                handleRoomCreateSuccess(map);
                break;
            case "user_ack":
                handleAck(map);
                break;
            case "user_invite":
                handleInvite(map);
                break;
            case "room_join_success":
                handleJoinToRoom(map);
                break;
            case "new_user_join":
                handleRemoteInRoom(map);
                break;
            case "user_sendOffer":
                handleOffer(map);
                break;
            case "user_sendAnswer":
                handleAnswer(map);
                break;
            case "user_sendIceCandidate":
                handleRemoteCandidate(map);
                break;
            case "user_leave":
                handleRemoteOutRoom(map);
                break;
            case "decline_reason":
                handleDecline(map);
                break;
        }
    }


    // 处理登录成功
    private void handleUserLoginSuccess(Map map) {
        Map data = (Map) map.get("data");
        String myId = (String) data.get("socketID");
        JSONArray arr = (JSONArray) data.get("iceServers");
        String js = JSONObject.toJSONString(arr);
        ArrayList<MyIceServer> iceServers = (ArrayList<MyIceServer>) JSONObject.parseArray(js, MyIceServer.class);
        events.onLoginSuccess(iceServers, myId);
    }

    //处理创建房间成功
    private void handleRoomCreateSuccess(Map map) {
        Map data = (Map) map.get("data");
        String room = (String) data.get("room");
        events.onCreateRoomSuccess(room);


    }

    // 回应信息
    private void handleAck(Map map) {
        Map data = (Map) map.get("data");
        String fromId = (String) data.get("fromID");
        events.onUserAck(fromId);

    }

    // 邀请进入房间
    private void handleInvite(Map map) {
        Map data = (Map) map.get("data");
        String socketId = (String) data.get("socketID");
        events.onUserInvite(socketId);


    }

    // 自己进入房间
    private void handleJoinToRoom(Map map) {
        Map data = (Map) map.get("data");
        JSONArray arr = (JSONArray) data.get("connections");
        String js = JSONObject.toJSONString(arr, SerializerFeature.WriteClassName);
        ArrayList<String> connections = (ArrayList<String>) JSONObject.parseArray(js, String.class);
        String myId = (String) data.get("you");
        events.onJoinToRoom(connections, myId);
    }

    // 自己已经在房间，有人进来
    private void handleRemoteInRoom(Map map) {
        Map data = (Map) map.get("data");
        String socketId = (String) data.get("socketID");
        events.onRemoteJoinToRoom(socketId);
    }

    // 处理Offer
    private void handleOffer(Map map) {
        Map data = (Map) map.get("data");
        Map sdpDic = (Map) data.get("sdp");
        String socketId = (String) data.get("socketID");
        String sdp = (String) sdpDic.get("sdp");
        events.onReceiveOffer(socketId, sdp);
    }

    // 处理Answer
    private void handleAnswer(Map map) {
        Map data = (Map) map.get("data");
        Map sdpDic = (Map) data.get("sdp");
        String socketId = (String) data.get("socketID");
        String sdp = (String) sdpDic.get("sdp");
        events.onReceiverAnswer(socketId, sdp);
    }

    // 处理交换信息
    private void handleRemoteCandidate(Map map) {
        Map data = (Map) map.get("data");
        String socketId = (String) data.get("socketID");
        String sdpMid = (String) data.get("id");
        sdpMid = (null == sdpMid) ? "video" : sdpMid;
        Integer sdpMLineIndex = (Integer) data.get("label");
        String candidate = (String) data.get("candidate");
        IceCandidate iceCandidate = new IceCandidate(sdpMid, sdpMLineIndex, candidate);
        events.onRemoteIceCandidate(socketId, iceCandidate);

    }

    // 有人离开了房间
    private void handleRemoteOutRoom(Map map) {
        Map data = (Map) map.get("data");
        String socketId = (String) data.get("socketID");
        events.onRemoteOutRoom(socketId);
    }

    // 拒絕接聽
    private void handleDecline(Map map) {
        Map data = (Map) map.get("data");
        String reason = (String) data.get("reason");
        EnumMsg.Decline decline = EnumMsg.Decline.Refuse;
        if (reason.equals("cancel")) {
            decline = EnumMsg.Decline.Cancel;
        } else if (reason.equals("busy")) {
            decline = EnumMsg.Decline.Busy;
        }
        events.onDecline(decline);

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
