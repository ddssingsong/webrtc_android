package com.dds.temple0.socket;

import android.text.TextUtils;
import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class MyWebSocket extends WebSocketClient {
    private static final String TAG = "MyWebSocket";
    private SocketManager mSocketManager;
    private int socketState = SocketState.IDLE;

    public interface SocketState {
        int IDLE = 0;
        int CONNECTED = 1;
    }


    public MyWebSocket(URI serverUri, SocketManager socketManager) {
        super(serverUri);
        mSocketManager = socketManager;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        Log.i(TAG, "socket is open!");
        socketState = SocketState.CONNECTED;
    }

    @Override
    public void onMessage(String message) {
        handleMessage(message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.d(TAG, "onClose: code = " + code + ",reason = " + reason + ",isRemote = " + remote);
        socketState = SocketState.IDLE;
        forEachUserStateEventCallback(iUserStateEvent -> iUserStateEvent.userLogout(reason));

    }

    @Override
    public void onError(Exception ex) {
        Log.d(TAG, "onError: " + ex);
        socketState = SocketState.IDLE;
        forEachUserStateEventCallback(iUserStateEvent -> iUserStateEvent.userLogout(ex.getMessage()));
    }


    public int getSocketState() {
        return socketState;
    }


    // region ------------------------------receive message-------------------------------------

    private void forEachUserStateEventCallback(Consumer<SocketManager.IUserStateEvent> consumer) {
        Collection<WeakReference<SocketManager.IUserStateEvent>> values = mSocketManager.userStateEvents.values();
        for (WeakReference<SocketManager.IUserStateEvent> value : values) {
            if (value.get() != null) {
                consumer.accept(value.get());
            }

        }

    }

    private void forEachEventCallback(Consumer<SocketManager.IEvent> consumer) {
        for (HashMap.Entry<Object, WeakReference<SocketManager.IEvent>> entry : mSocketManager.iEventMap.entrySet()) {
            WeakReference<SocketManager.IEvent> value = entry.getValue();
            if (value.get() != null) {
                consumer.accept(value.get());
            }

        }
    }

    private void handleMessage(String message) {
        JSONObject map = null;
        String eventName = null;
        try {
            map = new JSONObject(message);
            eventName = map.optString("eventName");
        } catch (JSONException e) {
            Log.e(TAG, "handleMessage: " + e);
        }
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


    }

    private void handleLogin(JSONObject map) {
        try {
            JSONObject data = map.optJSONObject("data");
            if (data != null) {
                String userID = (String) data.get("userID");
                forEachUserStateEventCallback(iUserStateEvent -> iUserStateEvent.userLogin(userID));
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
                forEachEventCallback(event -> event.onDisConnect(fromId));

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
                forEachEventCallback(event -> event.onTransAudio(fromId));
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
                forEachEventCallback(event -> event.onReject(fromID, rejectType));
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
                forEachEventCallback(event -> event.onPeers(you, connections, roomSize));
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
                forEachEventCallback(event -> event.onNewPeer(userID));
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
                forEachEventCallback(event -> event.onRing(fromId));
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
                forEachEventCallback(event -> event.onCancel(inviteID));
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
                forEachEventCallback(event -> event.onInvite(room, audioOnly, inviteID, userList));
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
                forEachEventCallback(event -> event.onLeave(fromID));
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
                forEachEventCallback(event -> event.onIceCandidate(userID, id, label, candidate));
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
                forEachEventCallback(event -> event.onAnswer(userID, sdp));
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
                forEachEventCallback(event -> event.onOffer(userID, sdp));
            }
        } catch (JSONException e) {
            Log.d(TAG, "handleTransAudio: " + e);
        }
    }

    // endregion

    // region ------------------------------send message----------------------------------------
    public void createRoom(String room, int roomSize, String myId) {
        Map<String, Object> map = new HashMap<>();
        map.put("eventName", "__create");

        Map<String, Object> childMap = new HashMap<>();
        childMap.put("room", room);
        childMap.put("roomSize", roomSize);
        childMap.put("userID", myId);

        map.put("data", childMap);
        JSONObject object = new JSONObject(map);
        final String jsonString = object.toString();
        Log.d(TAG, "send-->" + jsonString);
        send(jsonString);
    }

    // 发送邀请
    public void sendInvite(String room, String myId, List<String> users, boolean audioOnly) {
        Map<String, Object> map = new HashMap<>();
        map.put("eventName", "__invite");

        Map<String, Object> childMap = new HashMap<>();
        childMap.put("room", room);
        childMap.put("audioOnly", audioOnly);
        childMap.put("inviteID", myId);

        String join = listToString(users);
        childMap.put("userList", join);

        map.put("data", childMap);
        JSONObject object = new JSONObject(map);
        final String jsonString = object.toString();
        Log.d(TAG, "send-->" + jsonString);
        send(jsonString);
    }

    // 取消邀请
    public void sendCancel(String mRoomId, String useId, List<String> users) {
        Map<String, Object> map = new HashMap<>();
        map.put("eventName", "__cancel");

        Map<String, Object> childMap = new HashMap<>();
        childMap.put("inviteID", useId);
        childMap.put("room", mRoomId);

        String join = listToString(users);
        childMap.put("userList", join);


        map.put("data", childMap);
        JSONObject object = new JSONObject(map);
        final String jsonString = object.toString();
        Log.d(TAG, "send-->" + jsonString);
        send(jsonString);
    }

    // 发送响铃通知
    public void sendRing(String myId, String toId, String room) {
        Map<String, Object> map = new HashMap<>();
        map.put("eventName", "__ring");

        Map<String, Object> childMap = new HashMap<>();
        childMap.put("fromID", myId);
        childMap.put("toID", toId);
        childMap.put("room", room);


        map.put("data", childMap);
        JSONObject object = new JSONObject(map);
        final String jsonString = object.toString();
        Log.d(TAG, "send-->" + jsonString);
        send(jsonString);
    }

    //加入房间
    public void sendJoin(String room, String myId) {
        Map<String, Object> map = new HashMap<>();
        map.put("eventName", "__join");

        Map<String, String> childMap = new HashMap<>();
        childMap.put("room", room);
        childMap.put("userID", myId);


        map.put("data", childMap);
        JSONObject object = new JSONObject(map);
        final String jsonString = object.toString();
        Log.d(TAG, "send-->" + jsonString);
        send(jsonString);
    }

    // 拒接接听
    public void sendRefuse(String room, String inviteID, String myId, int refuseType) {
        Map<String, Object> map = new HashMap<>();
        map.put("eventName", "__reject");

        Map<String, Object> childMap = new HashMap<>();
        childMap.put("room", room);
        childMap.put("toID", inviteID);
        childMap.put("fromID", myId);
        childMap.put("refuseType", String.valueOf(refuseType));

        map.put("data", childMap);
        JSONObject object = new JSONObject(map);
        final String jsonString = object.toString();
        Log.d(TAG, "send-->" + jsonString);
        send(jsonString);
    }

    // 离开房间
    public void sendLeave(String myId, String room, String userId) {
        Map<String, Object> map = new HashMap<>();
        map.put("eventName", "__leave");

        Map<String, Object> childMap = new HashMap<>();
        childMap.put("room", room);
        childMap.put("fromID", myId);
        childMap.put("userID", userId);

        map.put("data", childMap);
        JSONObject object = new JSONObject(map);
        final String jsonString = object.toString();
        Log.d(TAG, "send-->" + jsonString);
        if (isOpen()) {
            send(jsonString);
        }
    }

    // 切换到语音
    public void sendTransAudio(String myId, String userId) {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> childMap = new HashMap<>();
        childMap.put("fromID", myId);
        childMap.put("userID", userId);
        map.put("data", childMap);
        map.put("eventName", "__audio");
        JSONObject object = new JSONObject(map);
        final String jsonString = object.toString();
        Log.d(TAG, "send-->" + jsonString);
        send(jsonString);
    }

    // 断开重连
    public void sendDisconnect(String room, String myId, String userId) {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> childMap = new HashMap<>();
        childMap.put("fromID", myId);
        childMap.put("userID", userId);
        childMap.put("room", room);
        map.put("data", childMap);
        map.put("eventName", "__disconnect");
        JSONObject object = new JSONObject(map);
        final String jsonString = object.toString();
        Log.d(TAG, "send-->" + jsonString);
        send(jsonString);
    }

    // send offer
    public void sendOffer(String myId, String userId, String sdp) {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> childMap = new HashMap<>();
        childMap.put("sdp", sdp);
        childMap.put("userID", userId);
        childMap.put("fromID", myId);
        map.put("data", childMap);
        map.put("eventName", "__offer");
        JSONObject object = new JSONObject(map);
        final String jsonString = object.toString();
        Log.d(TAG, "send-->" + jsonString);
        send(jsonString);
    }

    // send answer
    public void sendAnswer(String myId, String userId, String sdp) {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> childMap = new HashMap<>();
        childMap.put("sdp", sdp);
        childMap.put("fromID", myId);
        childMap.put("userID", userId);
        map.put("data", childMap);
        map.put("eventName", "__answer");
        JSONObject object = new JSONObject(map);
        final String jsonString = object.toString();
        Log.d(TAG, "send-->" + jsonString);
        send(jsonString);
    }

    // send ice-candidate
    public void sendIceCandidate(String myId, String userId, String id, int label, String candidate) {
        Map<String, Object> map = new HashMap<>();
        map.put("eventName", "__ice_candidate");

        Map<String, Object> childMap = new HashMap<>();
        childMap.put("userID", userId);
        childMap.put("fromID", myId);
        childMap.put("id", id);
        childMap.put("label", label);
        childMap.put("candidate", candidate);

        map.put("data", childMap);
        JSONObject object = new JSONObject(map);
        final String jsonString = object.toString();
        Log.d(TAG, "send-->" + jsonString);
        if (isOpen()) {
            send(jsonString);
        }
    }

    //endregion

    private String listToString(List<String> mList) {
        final String SEPARATOR = ",";
        StringBuilder sb = new StringBuilder();
        String convertedListStr;
        if (null != mList && mList.size() > 0) {
            for (String item : mList) {
                sb.append(item);
                sb.append(SEPARATOR);
            }
            convertedListStr = sb.toString();
            convertedListStr = convertedListStr.substring(0, convertedListStr.length() - SEPARATOR.length());
            return convertedListStr;
        } else return "";
    }

    public interface Consumer<T> {
        void accept(T t);
    }


}
