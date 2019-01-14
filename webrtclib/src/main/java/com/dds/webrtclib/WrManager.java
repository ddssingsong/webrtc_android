package com.dds.webrtclib;

import android.content.Context;

import com.dds.webrtclib.ws.EnumMsg;
import com.dds.webrtclib.ws.ISignalingEvents;
import com.dds.webrtclib.ws.IWebSocket;
import com.dds.webrtclib.ws.MxWebSocket;

import org.webrtc.IceCandidate;

import java.util.ArrayList;

/**
 * Created by dds on 2019/1/11.
 * android_shuai@163.com
 */
public class WrManager implements ISignalingEvents {

    private IWebSocket webSocket;
    private WebRTCHelper.Role _role;
    private EnumMsg.Type _type;
    private String _sessionId;
    private String _myUserId;
    private String _ids;
    private Context _context;


    private WebRTCHelper webRTCHelper;


    public static WrManager getInstance() {
        return Holder.wrManager;
    }

    private static class Holder {
        private static WrManager wrManager = new WrManager();
    }

    public void setCallback(IWebrtcViewCallback callback) {
        if (webRTCHelper != null) {
            webRTCHelper.setViewCallback(callback);
        }
    }


    public void init(Context context, String ids, String myUserId, String sessionId, EnumMsg.Type type) {
        _context = context;
        _myUserId = myUserId;
        _ids = ids;
        _sessionId = sessionId;
        _type = type;
        this.webSocket = new MxWebSocket(this);
        webRTCHelper = new WebRTCHelper(context, this.webSocket);
    }

    public void connectSocket(String wss, WebRTCHelper.Role role) {
        _role = role;
        if (webSocket != null) {
            webSocket.connect(wss);
        }
    }

    @Override
    public void onWebSocketOpen() {
        // webSocket打开成功
        webSocket.login(_sessionId);
    }

    @Override
    public void onLoginSuccess(ArrayList<MyIceServer> iceServers, String socketId) {
        if (_role == WebRTCHelper.Role.Caller) {
            //createRoom
            webSocket.createRoom(_ids, _type.value);

        } else {
            // 发送回执
            webSocket.sendAck(_ids);
        }

    }

    @Override
    public void onCreateRoomSuccess(String room) {
        // 创建房间成功加入房间
        webSocket.joinRoom(room);

    }

    @Override
    public void onJoinToRoom(ArrayList<String> connections, String myId) {
        if (webRTCHelper != null) {
            webRTCHelper.onJoinToRoom(connections, myId);
        }


    }

    @Override
    public void onUserAck(String userId) {
        webSocket.sendInvite(userId);
    }

    @Override
    public void onUserInvite(String socketId, String room) {
        //显示来电界面  开始响铃，


    }

    @Override
    public void onRemoteJoinToRoom(String socketId) {
        if (webRTCHelper != null) {
            webRTCHelper.onRemoteJoinToRoom(socketId);
        }

    }

    @Override
    public void onReceiveOffer(String socketId, String sdp) {
        if (webRTCHelper != null) {
            webRTCHelper.onReceiveOffer(socketId, sdp);
        }
    }

    @Override
    public void onReceiverAnswer(String socketId, String sdp) {
        if (webRTCHelper != null) {
            webRTCHelper.onReceiverAnswer(socketId, sdp);
        }
    }

    @Override
    public void onRemoteIceCandidate(String socketId, IceCandidate iceCandidate) {
        if (webRTCHelper != null) {
            webRTCHelper.onRemoteIceCandidate(socketId, iceCandidate);
        }
    }

    @Override
    public void onRemoteOutRoom(String socketId) {
        if (webRTCHelper != null) {
            webRTCHelper.onRemoteOutRoom(socketId);
        }
    }

    @Override
    public void onDecline(EnumMsg.Decline decline) {


    }

    @Override
    public void onError(String msg) {

    }


}
