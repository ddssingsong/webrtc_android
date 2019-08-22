package com.dds.skywebrtc;

import android.content.Context;

import org.webrtc.DataChannel;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RtpReceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.VideoDecoderFactory;
import org.webrtc.VideoEncoderFactory;
import org.webrtc.audio.JavaAudioDeviceModule;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dds on 2019/8/19.
 * android_shuai@163.com
 */
public class CallSession {

    private CallSessionCallback sessionCallback;
    private PeerConnectionFactory _factory;
    private Context _context;
    private EglBase _rootEglBase;
    private AVEngineKit avEngineKit;
    private EnumType.CallState callState = EnumType.CallState.Idle;

    private Map<String, Peer> _connectionPeerDic;

    public CallSession(AVEngineKit avEngineKit) {
        this.avEngineKit = avEngineKit;
        _context = avEngineKit._context;
        _rootEglBase = avEngineKit._rootEglBase;
        this._connectionPeerDic = new HashMap<>();
    }


    private PeerConnectionFactory createConnectionFactory() {
        PeerConnectionFactory.initialize(PeerConnectionFactory
                .InitializationOptions
                .builder(_context)
                .createInitializationOptions());

        final VideoEncoderFactory encoderFactory;
        final VideoDecoderFactory decoderFactory;

        encoderFactory = new DefaultVideoEncoderFactory(
                _rootEglBase.getEglBaseContext(),
                true,
                true);
        decoderFactory = new DefaultVideoDecoderFactory(_rootEglBase.getEglBaseContext());

        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();

        return PeerConnectionFactory.builder()
                .setOptions(options)
                .setAudioDeviceModule(JavaAudioDeviceModule.builder(_context).createAudioDeviceModule())
                .setVideoEncoderFactory(encoderFactory)
                .setVideoDecoderFactory(decoderFactory)
                .createPeerConnectionFactory();
    }


    // 每一个Session 可包含多个PeerConnection
    private class Peer implements SdpObserver, PeerConnection.Observer {
        private PeerConnection pc;
        private String userId;

        public Peer(String userId) {
            this.pc = createPeerConnection();
            this.userId = userId;

        }

        private PeerConnection createPeerConnection() {
            if (_factory == null) {
                _factory = createConnectionFactory();
            }
            // 管道连接抽象类实现方法
            PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(avEngineKit.getIceServers());
            return _factory.createPeerConnection(rtcConfig, this);
        }

        //-------------Observer--------------------
        @Override
        public void onSignalingChange(PeerConnection.SignalingState newState) {

        }

        @Override
        public void onIceConnectionChange(PeerConnection.IceConnectionState newState) {

        }

        @Override
        public void onIceConnectionReceivingChange(boolean receiving) {

        }

        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState newState) {

        }

        @Override
        public void onIceCandidate(IceCandidate candidate) {

        }

        @Override
        public void onIceCandidatesRemoved(IceCandidate[] candidates) {

        }

        @Override
        public void onAddStream(MediaStream stream) {

        }

        @Override
        public void onRemoveStream(MediaStream stream) {

        }

        @Override
        public void onDataChannel(DataChannel dataChannel) {

        }

        @Override
        public void onRenegotiationNeeded() {

        }

        @Override
        public void onAddTrack(RtpReceiver receiver, MediaStream[] mediaStreams) {

        }


        //-------------SdpObserver--------------------
        @Override
        public void onCreateSuccess(SessionDescription sdp) {

        }

        @Override
        public void onSetSuccess() {

        }

        @Override
        public void onCreateFailure(String error) {

        }

        @Override
        public void onSetFailure(String error) {

        }
    }


    public void setCallState(EnumType.CallState callState) {
        this.callState = callState;
    }

    public EnumType.CallState getCallState() {
        return callState;
    }

    public void setSessionCallback(CallSessionCallback sessionCallback) {
        this.sessionCallback = sessionCallback;
    }

    public interface CallSessionCallback {
        void didCallEndWithReason(EnumType.CallEndReason var1);

        void didChangeState(EnumType.CallState var1);

        void didChangeMode(boolean isAudio);

        void didCreateLocalVideoTrack();

        void didReceiveRemoteVideoTrack();

        void didError(String error);

    }
}
