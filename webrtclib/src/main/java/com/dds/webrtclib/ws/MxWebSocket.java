package com.dds.webrtclib.ws;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.dds.webrtclib.MyIceServer;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.webrtc.IceCandidate;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
    public void connect(String wss) {
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
        if (mWebSocketClient != null) {
            mWebSocketClient.close();
        }
    }


    //=================================上行数据=================================================
    @Override
    public void login(String sessionId) {
        Map<String, Object> map = new HashMap<>();
        map.put("action", "user_login");
        Map<String, String> childMap = new HashMap<>();
        childMap.put("sessionID", sessionId);
        map.put("data", childMap);
        JSONObject object = new JSONObject(map);
        final String jsonString = object.toString();
        mWebSocketClient.send(jsonString);


    }

    @Override
    public void createRoom(String ids, String type) {
        Map<String, Object> map = new HashMap<>();
        map.put("action", "room_create");
        Map<String, String> childMap = new HashMap<>();
        childMap.put("ids", ids);
        childMap.put("type", type);
        map.put("data", childMap);
        JSONObject object = new JSONObject(map);
        final String jsonString = object.toString();
        mWebSocketClient.send(jsonString);
    }

    @Override
    public void sendInvite(String userId) {
        Map<String, Object> map = new HashMap<>();
        map.put("action", "user_invite");
        Map<String, String> childMap = new HashMap<>();
        childMap.put("toId", userId);
        map.put("data", childMap);
        JSONObject object = new JSONObject(map);
        final String jsonString = object.toString();
        mWebSocketClient.send(jsonString);
    }

    @Override
    public void sendAck(String userId) {
        Map<String, Object> map = new HashMap<>();
        map.put("action", "user_ack");
        Map<String, String> childMap = new HashMap<>();
        childMap.put("fromId", userId);
        map.put("data", childMap);
        JSONObject object = new JSONObject(map);
        final String jsonString = object.toString();
        mWebSocketClient.send(jsonString);
    }

    @Override
    public void joinRoom(String room) {
        Map<String, Object> map = new HashMap<>();
        map.put("action", "room_join");
        Map<String, String> childMap = new HashMap<>();
        childMap.put("room", room);
        map.put("data", childMap);
        JSONObject object = new JSONObject(map);
        final String jsonString = object.toString();
        mWebSocketClient.send(jsonString);
    }

    @Override
    public void sendOffer(String socketId, String sdp) {
        HashMap<String, Object> childMap2 = new HashMap();
        childMap2.put("socketId", socketId);
        childMap2.put("sdp", sdp);
        HashMap<String, Object> map = new HashMap();
        map.put("action", "user_sendOffer");
        map.put("data", childMap2);
        JSONObject object = new JSONObject(map);
        String jsonString = object.toString();
        mWebSocketClient.send(jsonString);
    }

    @Override
    public void sendAnswer(String socketId, String sdp) {
        HashMap<String, Object> childMap2 = new HashMap();
        childMap2.put("socketId", socketId);
        childMap2.put("sdp", sdp);
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
        childMap.put("socketId", socketId);
        map.put("data", childMap);
        JSONObject object = new JSONObject(map);
        String jsonString = object.toString();
        mWebSocketClient.send(jsonString);
    }

    @Override //拒绝接听
    public void decline(EnumMsg.Decline decline) {
        String reason = "refuse";
        if (decline == EnumMsg.Decline.Busy) {
            reason = "busy";
        } else if (decline == EnumMsg.Decline.Cancel) {
            reason = "cancel";
        }
        Map<String, Object> map = new HashMap<>();
        map.put("action", "room_refuse");
        Map<String, String> childMap = new HashMap<>();
        childMap.put("reason", reason);
        map.put("data", childMap);
        JSONObject object = new JSONObject(map);
        final String jsonString = object.toString();
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
            case "user_sendOffer_success":
                handleOffer(map);
                break;
            case "user_sendAnswer_success":
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
        String myId = (String) data.get("socketId");
        JSONArray arr = (JSONArray) data.get("iceServers");
        String js = JSONObject.toJSONString(arr, SerializerFeature.WriteClassName);
        ArrayList<MyIceServer> iceServers = (ArrayList<MyIceServer>) JSONObject.parseArray(js, MyIceServer.class);
        events.onLoginSuccess(iceServers, myId);
    }

    //处理创建房间成功
    private void handleRoomCreateSuccess(Map map) {
        String room = (String) map.get("room");
        events.onCreateRoomSuccess(room);


    }

    // 回应信息
    private void handleAck(Map map) {
        String fromId = (String) map.get("fromId");
        events.onUserAck(fromId);

    }

    // 邀请进入房间
    private void handleInvite(Map map) {
        String socketId = (String) map.get("socketId");
        String room = (String) map.get("room");
        events.onUserInvite(socketId, room);


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
        String socketId = (String) data.get("socketId");
        events.onRemoteJoinToRoom(socketId);
    }

    // 处理Offer
    private void handleOffer(Map map) {
        Map data = (Map) map.get("data");
        String socketId = (String) data.get("socketId");
        String sdp = (String) data.get("sdp");
        events.onReceiveOffer(socketId, sdp);
    }

    // 处理Answer
    private void handleAnswer(Map map) {
        Map data = (Map) map.get("data");
        String socketId = (String) data.get("socketId");
        String sdp = (String) data.get("sdp");
        events.onReceiverAnswer(socketId, sdp);
    }

    // 处理交换信息
    private void handleRemoteCandidate(Map map) {
        Map data = (Map) map.get("data");
        String socketId = (String) data.get("socketId");
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
        String socketId = (String) data.get("socketId");
        events.onRemoteOutRoom(socketId);
    }

    private void handleDecline(Map map) {
        Map data = (Map) map.get("data");
        String reason = (String) data.get("reason");
        EnumMsg.Decline decline;
        if(reason.equals("cancel")){
            decline = EnumMsg.Decline.Cancel;
        }else if(reason.equals("busy")){
            decline = EnumMsg.Decline.Cancel;
        }

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
