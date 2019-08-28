package com.dds.skywebrtc;

import android.util.Log;

import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.RtpReceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dds on 2019/8/19.
 * android_shuai@163.com
 */
public class CallSession {
    public final static String TAG = "dds_CallSession";
    private CallSessionCallback sessionCallback;
    private AVEngineKit avEngineKit;

    private ProxyVideoSink localRender;
    private ProxyVideoSink remoteRender;
    private Map<String, Peer> _connectionPeerDic;

    public CallSession(AVEngineKit avEngineKit) {
        this.avEngineKit = avEngineKit;
        this._connectionPeerDic = new HashMap<>();
    }

    public void callEnd(EnumType.CallEndReason callEndReason) {
        if (sessionCallback != null) {
            sessionCallback.didCallEndWithReason(callEndReason);
        }

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
            // 管道连接抽象类实现方法
            PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(avEngineKit.getIceServers());
            return avEngineKit._factory.createPeerConnection(rtcConfig, this);
        }

        //-------------Observer--------------------
        @Override
        public void onSignalingChange(PeerConnection.SignalingState signalingState) {
            Log.i(TAG, "onSignalingChange: " + signalingState);
        }

        @Override
        public void onIceConnectionChange(PeerConnection.IceConnectionState newState) {
            Log.i(TAG, "onIceConnectionChange: " + newState.toString());

        }

        @Override
        public void onIceConnectionReceivingChange(boolean receiving) {
            Log.i(TAG, "onIceConnectionReceivingChange:" + receiving);
        }

        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState newState) {
            Log.i(TAG, "onIceGatheringChange:" + newState.toString());
        }

        @Override
        public void onIceCandidate(IceCandidate candidate) {
            Log.i(TAG, "onIceCandidate:");
            // 发送IceCandidate
            avEngineKit._iSocketEvent.sendIceCandidate(userId, candidate.sdpMid, candidate.sdpMLineIndex, candidate.sdp);
        }

        @Override
        public void onIceCandidatesRemoved(IceCandidate[] candidates) {
            Log.i(TAG, "onIceCandidatesRemoved:");
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


    public EnumType.CallState getCallState() {
        return avEngineKit._callState;
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
