package com.huawang.dongxiangjun.myapplication;


import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.VideoCapturerAndroid;
import org.webrtc.VideoRendererGui;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by dongxiangjun on 2017/10/20.
 */

enum Role {
    //发送者
    Caller,
    //被发送者
    Callee,
}


public class WebRTCHelper {

    private WebSocketClient mWebSocketClient;
    // 通道工厂
    private PeerConnectionFactory _factory;
    //本地视频流
    private MediaStream _localStream;


    private ArrayList<String> _connectionIdArray;
    private Map<String, Peer> _connectionPeerDic;

    // 我在这个房间的id
    private String _myId;
    // 接口
    private IWebRTCHelper IHelper;

    private ArrayList<PeerConnection.IceServer> ICEServers;

    // stun服务器
    final private String RTCSTUNServerURL = "stun:stun.l.google.com:19302";
    final private String RTCSTUNServerURL2 = "stun:47.254.34.146";

    final private String TAG = "dds_webrtc";

    // socket 服务器地址
    private URI uri;

    //切换摄像头
    private VideoCapturerAndroid capturerAndroid;

    private VideoSource videoSource;

    // 用户角色
    Role _role;


    // 构造器
    public WebRTCHelper(IWebRTCHelper IHelper) {
        this.IHelper = IHelper;
        this._connectionPeerDic = new HashMap<>();
        this._connectionIdArray = new ArrayList();
    }


    /// 初始化WebSocket
    public void initSocket(String ws, String point) {

        try {
            uri = new URI(ws);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        if (null == mWebSocketClient) {
            mWebSocketClient = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    Log.i(TAG, "onOpen: ");
                    joinTheRoom();
                }

                @Override
                public void onMessage(String s) {
                    Log.i(TAG, "onMessage: " + s);


                    Map map = JSON.parseObject(s, Map.class);

                    socketGetMessage(map);
                }

                @Override
                public void onClose(int i, String s, boolean b) {
                    Log.i(TAG, "onClose: ");
                }

                @Override
                public void onError(Exception e) {
                    Log.i(TAG, "onError: ");
                }
            };
            SSLContext sslContext = null;
            try {
                sslContext = SSLContext.getInstance("TLS");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

            try {
                if (sslContext != null) {
                    sslContext.init(null, new TrustManager[]{
                            new X509TrustManager() {

                                @Override
                                public void checkClientTrusted(X509Certificate[] chain, String authType) {

                                }


                                @Override
                                public void checkServerTrusted(X509Certificate[] chain, String authType) {

                                }

                                @Override
                                public X509Certificate[] getAcceptedIssuers() {
                                    return new X509Certificate[0];
                                }
                            }
                    }, new SecureRandom());
                }
            } catch (KeyManagementException e) {
                e.printStackTrace();
            }
            SSLSocketFactory factory = null;
            if (sslContext != null) {
                factory = sslContext.getSocketFactory();
            }
            try {
                if (factory != null) {
                    mWebSocketClient.setSocket(factory.createSocket());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            mWebSocketClient.connect();
        }
    }

    //加入房间
    private void joinTheRoom() {

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("eventName", "__join");
        Map<String, String> childMap = new HashMap<String, String>();
        childMap.put("room", "100");
        map.put("data", childMap);

        JSONObject object = new JSONObject(map);
        final String jsonString = object.toString();
        //将jsonString发送到服务器解析即可

        mWebSocketClient.send(jsonString);

    }


    // 调整摄像头前置后置
    public void switchCamera() {

        capturerAndroid.switchCamera(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "切换摄像头");
            }
        });

    }


    //socket接收到数据之后的数据处理
    private void socketGetMessage(Map map) {


        String eventName = (String) map.get("eventName");


        //1.0这条消息类型是_peers，意思为房间新用户
        if (eventName.equals("_peers")) {

            Log.v(TAG, "收到socket数据------1.0");

            Map data = (Map) map.get("data");
            JSONArray arr = (JSONArray) data.get("connections");
            //将array数组转换成字符串
            String js = JSONObject.toJSONString(arr, SerializerFeature.WriteClassName);
            //把字符串转换成集合
            ArrayList<String> connections = (ArrayList<String>) JSONObject.parseArray(js, String.class);


            _connectionIdArray.addAll(connections);

            _myId = (String) data.get("you");

            if (_factory == null) {
                /// 初始化WebRTC
                PeerConnectionFactory.initializeAndroidGlobals(IHelper, true, true, true, VideoRendererGui.getEGLContext());
                _factory = new PeerConnectionFactory();
            }
            if (_localStream == null) {
                createLocalStream();
            }

            createPeerConnections();

            //创建点对点的管道peerConnection之后 将自己的视频画面添加进管道中
            //本地的 RTCMediaStream 的视频和音频轨道,现在是则需要添加到peer connection.
            addStreams();

            createOffers();

        } else if (eventName.equals("_new_peer")) {


            Log.v(TAG, "收到socket数据------2.0");

            //2.其他新人加入房间的信息
            Map data = (Map) map.get("data");
            String socketId = (String) data.get("socketId");

            if (_localStream == null) {
                createLocalStream();
            }
            Peer mpeer = new Peer(socketId);
            mpeer.pc.addStream(_localStream);

            _connectionIdArray.add(socketId);
            _connectionPeerDic.put(socketId, mpeer);

        } else if (eventName.equals("_ice_candidate")) {

            Log.i(TAG, "收到socket数据------3.0");

            //3.接收到新加入的人发了ICE候选，（即经过ICEServer而获取到的地址）
            Map data = (Map) map.get("data");
            String socketId = (String) data.get("socketId");
            String sdpMid = (String) data.get("id");

            sdpMid = null == sdpMid ? "video" : sdpMid;

            Integer sdpMLineIndex = (Integer) data.get("label");
            String sdp = (String) data.get("candidate");

            IceCandidate iceCandidate = new IceCandidate(sdpMid, sdpMLineIndex, sdp);

            Peer peer = _connectionPeerDic.get(socketId);

            peer.pc.addIceCandidate(iceCandidate);


        } else if (eventName.equals("_remove_peer")) {

            Log.v(TAG, "收到socket数据------4.0");

            //4.有人离开房间的事件
            Map data = (Map) map.get("data");
            String socketId = (String) data.get("socketId");
            closePeerConnection(socketId);


        } else if (eventName.equals("_offer")) {
            //这个新加入的人发了个offer

            Log.i(TAG, "收到socket数据------5.0");

            //5.这个新加入的人发了个offer
            Map data = (Map) map.get("data");
            Map sdpDic = (Map) data.get("sdp");
            //拿到SDP
            String socketId = (String) data.get("socketId");
            String sdp = (String) sdpDic.get("sdp");

            //设置当前角色状态为被呼叫，（被发offer）
            _role = Role.Callee;

            Peer mPeer = _connectionPeerDic.get(socketId);
            SessionDescription sessionDescription = new SessionDescription(SessionDescription.Type.OFFER, sdp);
            mPeer.pc.setRemoteDescription(mPeer, sessionDescription);


        } else if (eventName.equals("_answer")) {

            Log.v(TAG, "收到socket数据------6.0");

            //6.回应offer的应答
            Map data = (Map) map.get("data");
            Map sdpDic = (Map) data.get("sdp");
            //拿到SDP
            String socketId = (String) data.get("socketId");
            String sdp = (String) sdpDic.get("sdp");

            Peer mPeer = _connectionPeerDic.get(socketId);
            SessionDescription sessionDescription = new SessionDescription(SessionDescription.Type.ANSWER, sdp);
            mPeer.pc.setRemoteDescription(mPeer, sessionDescription);
        }
    }


    //创建本地流
    private void createLocalStream() {


        _localStream = _factory.createLocalMediaStream("ARDAMS");

        // 音频
        AudioSource audioSource = _factory.createAudioSource(new MediaConstraints());
        AudioTrack audioTrack = _factory.createAudioTrack("ARDAMSa0", audioSource);
        _localStream.addTrack(audioTrack);

        String frontFacingDevice = VideoCapturerAndroid.getNameOfFrontFacingDevice();
        //创建需要传入设备的名称
        capturerAndroid = VideoCapturerAndroid.create(frontFacingDevice);

        // 视频
        MediaConstraints audioConstraints = localVideoConstraints();
        videoSource =
                _factory.createVideoSource(capturerAndroid, audioConstraints);
        VideoTrack localVideoTrack =
                _factory.createVideoTrack("ARDAMSv0", videoSource);

        _localStream.addTrack(localVideoTrack);

        if (IHelper != null) {
            IHelper.webRTCHelper_SetLocalStream(_localStream, _myId);
        }

    }

    //创建所有连接
    private void createPeerConnections() {

        for (String str : _connectionIdArray) {
            Peer peer = new Peer(str);
            _connectionPeerDic.put(str, peer);
        }
    }


    //为所有连接添加流
    private void addStreams() {

        Log.v(TAG, "为所有连接添加流");

        for (Map.Entry<String, Peer> entry : _connectionPeerDic.entrySet()) {

            if (_localStream == null) {
                createLocalStream();
            }
            entry.getValue().pc.addStream(_localStream);
        }

    }

    //为所有连接创建offer
    private void createOffers() {
        Log.v(TAG, "为所有连接创建offer");

        for (Map.Entry<String, Peer> entry : _connectionPeerDic.entrySet()) {
            _role = Role.Caller;
            Peer mPeer = entry.getValue();
            mPeer.pc.createOffer(mPeer, offerOranswerConstraint());
        }

    }

    /**
     * 退出房间
     */
    public void exitRoom() {

        if (videoSource != null) {
            videoSource.stop();
        }
        ArrayList<String> mycopy;
        mycopy = (ArrayList) _connectionIdArray.clone();

        for (String Id : mycopy) {
            closePeerConnection(Id);
        }

        mWebSocketClient.close();

        _localStream = null;
    }

    // 关闭通道流
    private void closePeerConnection(String connectionId) {

        Log.v(TAG, "关闭通道流");

        Peer mPeer = _connectionPeerDic.get(connectionId);
        if (mPeer != null) {
            mPeer.pc.close();
        }
        _connectionPeerDic.remove(connectionId);
        _connectionIdArray.remove(connectionId);

        IHelper.webRTCHelper_CloseWithUserId(connectionId);
    }


    /**************************************各种约束******************************************/

    private MediaConstraints localVideoConstraints() {


        MediaConstraints mediaConstraints = new MediaConstraints();
        ArrayList<MediaConstraints.KeyValuePair> keyValuePairs = new ArrayList<>();
        keyValuePairs.add(new MediaConstraints.KeyValuePair("maxWidth", "320"));
        keyValuePairs.add(new MediaConstraints.KeyValuePair("minWidth", "160"));
        keyValuePairs.add(new MediaConstraints.KeyValuePair("maxHeight", "240"));
        keyValuePairs.add(new MediaConstraints.KeyValuePair("minHeight", "120"));
        keyValuePairs.add(new MediaConstraints.KeyValuePair("minFrameRate", "1"));
        keyValuePairs.add(new MediaConstraints.KeyValuePair("maxFrameRate", "5"));

        mediaConstraints.mandatory.addAll(keyValuePairs);
        return mediaConstraints;
    }


    private MediaConstraints peerConnectionConstraints() {

        MediaConstraints mediaConstraints = new MediaConstraints();
        ArrayList<MediaConstraints.KeyValuePair> keyValuePairs = new ArrayList<>();
        keyValuePairs.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));
        keyValuePairs.add(new MediaConstraints.KeyValuePair("minFrameRate", "1"));
        keyValuePairs.add(new MediaConstraints.KeyValuePair("maxFrameRate", "5"));

        mediaConstraints.optional.addAll(keyValuePairs);
        return mediaConstraints;
    }

    private MediaConstraints offerOranswerConstraint() {


        MediaConstraints mediaConstraints = new MediaConstraints();
        ArrayList<MediaConstraints.KeyValuePair> keyValuePairs = new ArrayList<>();
        keyValuePairs.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        keyValuePairs.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        mediaConstraints.mandatory.addAll(keyValuePairs);
        return mediaConstraints;
    }


    /**************************************内部类******************************************/


/// 内部类
    private class Peer implements SdpObserver, PeerConnection.Observer {
        private PeerConnection pc;
        private String socketId;

        public Peer(String socketId) {
            this.pc = createPeerConnection();
            this.socketId = socketId;

        }


        /****************************PeerConnection.Observer****************************/


        @Override
        public void onSignalingChange(PeerConnection.SignalingState signalingState) {
            Log.v(TAG, "ice 状态改变 " + signalingState);
        }

        @Override
        public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
        }

        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
        }


        @Override
        public void onIceCandidate(IceCandidate iceCandidate) {

            Log.v(TAG, "ice候选人许可");

            Map childMap = new HashMap();
            childMap.put("id", iceCandidate.sdpMid);
            childMap.put("label", iceCandidate.sdpMLineIndex);
            childMap.put("candidate", iceCandidate.sdp);
            childMap.put("socketId", socketId);

            Map map = new HashMap();
            map.put("eventName", "__ice_candidate");
            map.put("data", childMap);

            JSONObject object = new JSONObject(map);
            String jsonString = object.toString();


            mWebSocketClient.send(jsonString);
        }

        @Override
        public void onAddStream(MediaStream mediaStream) {
            if (IHelper != null) {
                IHelper.webRTCHelper_AddRemoteStream(mediaStream, socketId);
            }
        }

        @Override
        public void onRemoveStream(MediaStream mediaStream) {
            if (IHelper != null) {
                IHelper.webRTCHelper_CloseWithUserId(socketId);
            }
        }

        @Override
        public void onDataChannel(DataChannel dataChannel) {

        }

        @Override
        public void onRenegotiationNeeded() {

        }


        /****************************SdpObserver****************************/

        @Override
        public void onCreateSuccess(SessionDescription sessionDescription) {

            Log.v(TAG, "sdp创建成功       " + sessionDescription.type);

            //设置本地的SDP
            pc.setLocalDescription(Peer.this, sessionDescription);
        }

        @Override
        public void onSetSuccess() {

            Log.v(TAG, "sdp连接成功        " + pc.signalingState().toString());


            //判断，当前连接状态为，收到了远程点发来的offer，这个是进入房间的时候，尚且没人，来人就调到这里
            if (pc.signalingState() == PeerConnection.SignalingState.HAVE_REMOTE_OFFER) {

                //创建一个answer,会把自己的SDP信息返回出去
                pc.createAnswer(Peer.this, offerOranswerConstraint());

            }
            //判断连接状态为本地发送offer
            else if (pc.signalingState() == PeerConnection.SignalingState.HAVE_LOCAL_OFFER) {
                if (_role == Role.Callee) {

                    Map<String, Object> childMap1 = new HashMap();
                    childMap1.put("type", "answer");
                    childMap1.put("sdp", pc.getLocalDescription().description);

                    Map childMap2 = new HashMap();
                    childMap2.put("socketId", socketId);
                    childMap2.put("sdp", childMap1);

                    Map map = new HashMap();
                    map.put("eventName", "__answer");
                    map.put("data", childMap2);

                    JSONObject object = new JSONObject(map);
                    String jsonString = object.toString();

                    mWebSocketClient.send(jsonString);

                }
                //发送者,发送自己的offer
                else if (_role == Role.Caller) {

                    Map childMap1 = new HashMap();
                    childMap1.put("type", "offer");
                    childMap1.put("sdp", pc.getLocalDescription().description);

                    Map childMap2 = new HashMap();
                    childMap2.put("socketId", socketId);
                    childMap2.put("sdp", childMap1);


                    Map map = new HashMap();
                    map.put("eventName", "__offer");
                    map.put("data", childMap2);

                    JSONObject object = new JSONObject(map);
                    String jsonString = object.toString();

                    mWebSocketClient.send(jsonString);

                }

            }
            // Stable 稳定的
            else if (pc.signalingState() == PeerConnection.SignalingState.STABLE) {

                if (_role == Role.Callee) {

                    Map childMap1 = new HashMap();
                    childMap1.put("type", "answer");
                    childMap1.put("sdp", pc.getLocalDescription().description);

                    Map childMap2 = new HashMap();
                    childMap2.put("socketId", socketId);
                    childMap2.put("sdp", childMap1);

                    Map map = new HashMap();
                    map.put("data", childMap2);
                    map.put("eventName", "__answer");

                    JSONObject object = new JSONObject(map);
                    String jsonString = object.toString();

                    mWebSocketClient.send(jsonString);

                }
            }

        }

        @Override
        public void onCreateFailure(String s) {

        }

        @Override
        public void onSetFailure(String s) {

        }


        //初始化 RTCPeerConnection 连接管道
        private PeerConnection createPeerConnection() {

            if (_factory == null) {
                PeerConnectionFactory.initializeAndroidGlobals(IHelper, true, true, true, VideoRendererGui.getEGLContext());
                _factory = new PeerConnectionFactory();
            }

            if (ICEServers == null) {
                ICEServers = new ArrayList<>();


                PeerConnection.IceServer iceServer1 = new PeerConnection.IceServer(RTCSTUNServerURL, "", "");
                PeerConnection.IceServer iceServer2 = new PeerConnection.IceServer(RTCSTUNServerURL2, "", "");
                ICEServers.add(iceServer1);
                ICEServers.add(iceServer2);
            }
            // 管道连接抽象类实现方法

            return _factory.createPeerConnection(ICEServers, peerConnectionConstraints(), this);
        }

    }


}



