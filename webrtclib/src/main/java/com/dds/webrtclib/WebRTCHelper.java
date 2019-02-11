package com.dds.webrtclib;


import android.util.Log;

import com.dds.webrtclib.bean.MyIceServer;
import com.dds.webrtclib.callback.IViewCallback;
import com.dds.webrtclib.utils.AppRTCUtils;
import com.dds.webrtclib.ws.IWebSocket;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.CameraEnumerationAndroid;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.VideoCapturerAndroid;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class WebRTCHelper {

    public final static String TAG = "dds_webrtc";

    private PeerConnectionFactory _factory;
    private MediaStream _localStream;
    private AudioTrack _localAudioTrack;
    private VideoCapturerAndroid captureAndroid;
    private VideoSource videoSource;


    private ArrayList<String> _connectionIdArray; // socketId 的集合
    private Map<String, Peer> _connectionPeerDic; // Peer的集合

    private String _myId;
    private IViewCallback IHelper;

    private ArrayList<PeerConnection.IceServer> ICEServers;
    private boolean videoEnable;


    enum Role {Caller, Receiver,}

    private Role _role;// 判断是sendOffer还是sendAnswer

    private IWebSocket webSocket;


    public WebRTCHelper(IWebSocket webSocket) {
        this._connectionPeerDic = new HashMap<>();
        this._connectionIdArray = new ArrayList<>();
        this.ICEServers = new ArrayList<>();
        this.webSocket = webSocket;


    }

    // 设置界面的回调
    public void setViewCallback(IViewCallback callback) {
        IHelper = callback;
    }

    // 添加ice服务器
    public void addIceServer(MyIceServer myIceServer) {
        PeerConnection.IceServer iceServer = new PeerConnection.IceServer(myIceServer.urls,
                myIceServer.username, myIceServer.credential);
        ICEServers.add(iceServer);
    }

    // ===================================webSocket回调信息=======================================

    // 登陆成功：返回stun和turn地址已经是否
    public void onLoginSuccess(ArrayList<MyIceServer> iceServers, String socketId) {
        _myId = socketId;
        for (MyIceServer myIceServer : iceServers) {
            PeerConnection.IceServer iceServer = new PeerConnection.IceServer(myIceServer.urls,
                    myIceServer.username, myIceServer.credential);
            ICEServers.add(iceServer);
        }

    }

    // 加入房间成功
    public void onJoinToRoom(ArrayList<String> connections, String myId, boolean videoEnable) {
        this.videoEnable = videoEnable;
        _connectionIdArray.addAll(connections);
        _myId = myId;
        if (_factory == null) {
            PeerConnectionFactory.initializeAndroidGlobals(IHelper, true, true, true);
            _factory = new PeerConnectionFactory();
        }
        if (_localStream == null) {
            createLocalStream();
        }
        createPeerConnections();
        addStreams();
        createOffers();
    }


    public void onRemoteJoinToRoom(String socketId) {
        Log.d(TAG, "onRemoteJoinToRoom");
        Log.d(TAG, AppRTCUtils.getThreadInfo());
        if (_localStream == null) {
            createLocalStream();
        }
        Peer mPeer = new Peer(socketId);
        mPeer.pc.addStream(_localStream);

        _connectionIdArray.add(socketId);
        _connectionPeerDic.put(socketId, mPeer);

    }

    public void onRemoteIceCandidate(String socketId, IceCandidate iceCandidate) {
        Log.d(TAG, "onRemoteIceCandidate");
        Log.d(TAG, AppRTCUtils.getThreadInfo());
        Peer peer = _connectionPeerDic.get(socketId);
        peer.pc.addIceCandidate(iceCandidate);
    }

    public void onRemoteOutRoom(String socketId) {
        Log.d(TAG, "onRemoteOutRoom");
        Log.d(TAG, AppRTCUtils.getThreadInfo());
        closePeerConnection(socketId);
    }

    public void onReceiveOffer(String socketId, String sdp) {
        Log.d(TAG, "onReceiveOffer");
        Log.d(TAG, AppRTCUtils.getThreadInfo());
        _role = Role.Receiver;
        Peer mPeer = _connectionPeerDic.get(socketId);
        SessionDescription sessionDescription = new SessionDescription(SessionDescription.Type.OFFER, sdp);
        mPeer.pc.setRemoteDescription(mPeer, sessionDescription);
    }

    public void onReceiverAnswer(String socketId, String sdp) {
        Log.d(TAG, "onReceiverAnswer");
        Log.d(TAG, AppRTCUtils.getThreadInfo());
        Peer mPeer = _connectionPeerDic.get(socketId);
        SessionDescription sessionDescription = new SessionDescription(SessionDescription.Type.ANSWER, sdp);
        mPeer.pc.setRemoteDescription(mPeer, sessionDescription);
    }


    //**************************************逻辑控制**************************************
    // 调整摄像头前置后置
    public void switchCamera() {
        captureAndroid.switchCamera(new VideoCapturerAndroid.CameraSwitchHandler() {
            @Override
            public void onCameraSwitchDone(boolean b) {
                Log.i(TAG, "切换摄像头");
            }

            @Override
            public void onCameraSwitchError(String s) {
                Log.i(TAG, "切换摄像头失败");
            }
        });

    }

    // 设置自己静音
    public void toggleMute(boolean enable) {
        if (_localAudioTrack != null) {
            _localAudioTrack.setEnabled(enable);
        }
    }

    // 退出房间
    public void exitRoom() {
        if (videoSource != null) {
            videoSource.stop();
        }
        ArrayList myCopy;
        myCopy = (ArrayList) _connectionIdArray.clone();
        for (Object Id : myCopy) {
            closePeerConnection((String) Id);
        }
        if (_connectionIdArray != null) {
            _connectionIdArray.clear();
        }
        _localStream = null;

    }

    // 创建本地流
    private void createLocalStream() {
        _localStream = _factory.createLocalMediaStream("ARDAMS");
        // 音频
        MediaConstraints audioConstraints = new MediaConstraints();
//        audioConstraints.mandatory.add(
//                new MediaConstraints.KeyValuePair("levelControl", "true"));
        AudioSource audioSource = _factory.createAudioSource(audioConstraints);
        _localAudioTrack = _factory.createAudioTrack("ARDAMSa0", audioSource);
        _localStream.addTrack(_localAudioTrack);

        if (videoEnable) {
            String frontFacingDevice = CameraEnumerationAndroid.getNameOfFrontFacingDevice();
            //创建需要传入设备的名称
            captureAndroid = VideoCapturerAndroid.create(frontFacingDevice, new VideoCapturerAndroid.CameraEventsHandler() {
                @Override
                public void onCameraError(String s) {

                }

                @Override
                public void onCameraFreezed(String s) {

                }

                @Override
                public void onCameraOpening(int i) {

                }

                @Override
                public void onFirstFrameAvailable() {

                }

                @Override
                public void onCameraClosed() {

                }
            });
            // 视频
            MediaConstraints videoConstraints = localVideoConstraints();
            videoSource = _factory.createVideoSource(captureAndroid, videoConstraints);
            VideoTrack localVideoTrack = _factory.createVideoTrack("ARDAMSv0", videoSource);
            _localStream.addTrack(localVideoTrack);
        }
        if (IHelper != null) {
            IHelper.onSetLocalStream(_localStream, _myId);
        }

    }

    // 创建所有连接
    private void createPeerConnections() {
        for (Object str : _connectionIdArray) {
            Peer peer = new Peer((String) str);
            _connectionPeerDic.put((String) str, peer);
        }
    }

    // 为所有连接添加流
    private void addStreams() {
        Log.v(TAG, "为所有连接添加流");
        for (Map.Entry<String, Peer> entry : _connectionPeerDic.entrySet()) {
            if (_localStream == null) {
                createLocalStream();
            }
            entry.getValue().pc.addStream(_localStream);
        }

    }

    // 为所有连接创建offer
    private void createOffers() {
        Log.d(TAG, "为所有连接创建offer");

        for (Map.Entry<String, Peer> entry : _connectionPeerDic.entrySet()) {
            _role = Role.Caller;
            Peer mPeer = entry.getValue();
            mPeer.pc.createOffer(mPeer, offerOrAnswerConstraint());
        }

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

        if (IHelper != null) {
            IHelper.onCloseWithId(connectionId);
        }

    }


    //**************************************各种约束******************************************/
    private MediaConstraints localVideoConstraints() {
        MediaConstraints mediaConstraints = new MediaConstraints();
        ArrayList<MediaConstraints.KeyValuePair> keyValuePairs = new ArrayList<>();
        keyValuePairs.add(new MediaConstraints.KeyValuePair("maxWidth", "360"));
        keyValuePairs.add(new MediaConstraints.KeyValuePair("minWidth", "160"));
        keyValuePairs.add(new MediaConstraints.KeyValuePair("maxHeight", "640"));
        keyValuePairs.add(new MediaConstraints.KeyValuePair("minHeight", "120"));
        keyValuePairs.add(new MediaConstraints.KeyValuePair("minFrameRate", "1"));
        keyValuePairs.add(new MediaConstraints.KeyValuePair("maxFrameRate", "10"));
        mediaConstraints.mandatory.addAll(keyValuePairs);
        return mediaConstraints;
    }

    private MediaConstraints peerConnectionConstraints() {
        MediaConstraints mediaConstraints = new MediaConstraints();
        ArrayList<MediaConstraints.KeyValuePair> keyValuePairs = new ArrayList<>();
        keyValuePairs.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));
        keyValuePairs.add(new MediaConstraints.KeyValuePair("minFrameRate", "1"));
        keyValuePairs.add(new MediaConstraints.KeyValuePair("maxFrameRate", "10"));

        mediaConstraints.optional.addAll(keyValuePairs);
        return mediaConstraints;
    }

    private MediaConstraints offerOrAnswerConstraint() {
        MediaConstraints mediaConstraints = new MediaConstraints();
        ArrayList<MediaConstraints.KeyValuePair> keyValuePairs = new ArrayList<>();
        keyValuePairs.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        keyValuePairs.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        mediaConstraints.mandatory.addAll(keyValuePairs);
        return mediaConstraints;
    }


    //**************************************内部类******************************************/
    private class Peer implements SdpObserver, PeerConnection.Observer {
        private PeerConnection pc;
        private String socketId;

        public Peer(String socketId) {
            this.pc = createPeerConnection();
            this.socketId = socketId;

        }


        //****************************PeerConnection.Observer****************************/
        @Override
        public void onSignalingChange(PeerConnection.SignalingState signalingState) {
            Log.v(TAG, "ice 状态改变 " + signalingState);
        }

        @Override
        public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
        }

        @Override
        public void onIceConnectionReceivingChange(boolean b) {

        }

        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
        }


        @Override
        public void onIceCandidate(IceCandidate iceCandidate) {
            // 发送IceCandidate
            webSocket.sendIceCandidate(socketId, iceCandidate);
        }

        @Override
        public void onAddStream(MediaStream mediaStream) {
            if (IHelper != null) {
                IHelper.onAddRemoteStream(mediaStream, socketId);
            }
        }

        @Override
        public void onRemoveStream(MediaStream mediaStream) {
            if (IHelper != null) {
                IHelper.onCloseWithId(socketId);
            }
        }

        @Override
        public void onDataChannel(DataChannel dataChannel) {

        }

        @Override
        public void onRenegotiationNeeded() {

        }


        //****************************SdpObserver****************************/

        @Override
        public void onCreateSuccess(SessionDescription sessionDescription) {
            Log.v(TAG, "sdp创建成功       " + sessionDescription.type);
            //设置本地的SDP
            pc.setLocalDescription(Peer.this, sessionDescription);
        }

        @Override
        public void onSetSuccess() {
            Log.v(TAG, "sdp连接成功 " + pc.signalingState().toString() + "," + _role);

            if (pc.signalingState() == PeerConnection.SignalingState.HAVE_REMOTE_OFFER) {
                pc.createAnswer(Peer.this, offerOrAnswerConstraint());
            } else if (pc.signalingState() == PeerConnection.SignalingState.HAVE_LOCAL_OFFER) {
                //判断连接状态为本地发送offer
                if (_role == Role.Receiver) {
                    //接收者，发送Answer
                    webSocket.sendAnswer(socketId, pc.getLocalDescription().description);

                } else if (_role == Role.Caller) {
                    //发送者,发送自己的offer
                    webSocket.sendOffer(socketId, pc.getLocalDescription().description);
                }

            } else if (pc.signalingState() == PeerConnection.SignalingState.STABLE) {
                // Stable 稳定的
                if (_role == Role.Receiver) {
                    webSocket.sendAnswer(socketId, pc.getLocalDescription().description);

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
                PeerConnectionFactory.initializeAndroidGlobals(IHelper, true, true, true);
                _factory = new PeerConnectionFactory();
            }
            // 管道连接抽象类实现方法
            return _factory.createPeerConnection(ICEServers, peerConnectionConstraints(), this);
        }

    }


}



