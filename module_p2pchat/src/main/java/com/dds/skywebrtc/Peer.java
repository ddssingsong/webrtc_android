package com.dds.skywebrtc;

import android.util.Log;

import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.RtpReceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dds on 2020/3/11.
 * android_shuai@163.com
 */
public class Peer implements SdpObserver, PeerConnection.Observer {
    private final static String TAG = "dds_Peer";
    private PeerConnection pc;
    private String userId;
    private List<IceCandidate> queuedRemoteCandidates;
    private SessionDescription localSdp;
    private CallSession mSession;


    private boolean isOffer;

    public Peer(CallSession session, String userId) {
        this.mSession = session;
        this.pc = createPeerConnection();
        this.userId = userId;
        queuedRemoteCandidates = new ArrayList<>();


    }

    public PeerConnection createPeerConnection() {
        // 管道连接抽象类实现方法
        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(mSession.avEngineKit.getIceServers());
        return mSession._factory.createPeerConnection(rtcConfig, this);
    }

    public void setOffer(boolean isOffer) {
        this.isOffer = isOffer;
    }


    // 创建offer
    public void createOffer() {
        if (pc == null) return;
        pc.createOffer(this, offerOrAnswerConstraint());
    }

    // 创建answer
    public void createAnswer() {
        if (pc == null) return;
        pc.createAnswer(this, offerOrAnswerConstraint());

    }

    public void setRemoteDescription(SessionDescription sdp) {
        if (pc == null) return;
        pc.setRemoteDescription(this, sdp);
    }

    public void addLocalStream(MediaStream stream) {
        if (pc == null) return;
        pc.addStream(stream);
    }


    public void addRemoteIceCandidate(final IceCandidate candidate) {
        if (pc != null) {
            if (queuedRemoteCandidates != null) {
                queuedRemoteCandidates.add(candidate);
            } else {
                pc.addIceCandidate(candidate);
            }
        }
    }

    public void removeRemoteIceCandidates(final IceCandidate[] candidates) {
        if (pc == null) {
            return;
        }
        drainCandidates();
        pc.removeIceCandidates(candidates);
    }


    public void close() {
        if (pc != null) {
            pc.close();
        }
    }

    //------------------------------Observer-------------------------------------
    @Override
    public void onSignalingChange(PeerConnection.SignalingState signalingState) {
        Log.i(TAG, "onSignalingChange: " + signalingState);
    }

    @Override
    public void onIceConnectionChange(PeerConnection.IceConnectionState newState) {
        Log.i(TAG, "onIceConnectionChange: " + newState.toString());
        if (mSession._callState != EnumType.CallState.Connected) return;
        if (newState == PeerConnection.IceConnectionState.DISCONNECTED) {

        }
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
        mSession.executor.execute(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            mSession.avEngineKit.mEvent.sendIceCandidate(userId, candidate.sdpMid, candidate.sdpMLineIndex, candidate.sdp);
        });


    }

    @Override
    public void onIceCandidatesRemoved(IceCandidate[] candidates) {
        Log.i(TAG, "onIceCandidatesRemoved:");
    }

    @Override
    public void onAddStream(MediaStream stream) {
        mSession._remoteStream = stream;
        Log.i(TAG, "onAddStream:");
        if (stream.audioTracks.size() > 0) {
            stream.audioTracks.get(0).setEnabled(true);
        }
        if (mSession.sessionCallback.get() != null) {
            mSession.sessionCallback.get().didReceiveRemoteVideoTrack();
        }


    }

    @Override
    public void onRemoveStream(MediaStream stream) {
        Log.i(TAG, "onRemoveStream:");
    }

    @Override
    public void onDataChannel(DataChannel dataChannel) {
        Log.i(TAG, "onDataChannel:");
    }

    @Override
    public void onRenegotiationNeeded() {
        Log.i(TAG, "onRenegotiationNeeded:");
    }

    @Override
    public void onAddTrack(RtpReceiver receiver, MediaStream[] mediaStreams) {
        Log.i(TAG, "onAddTrack:");
    }


    //-------------SdpObserver--------------------
    @Override
    public void onCreateSuccess(SessionDescription origSdp) {
        Log.d(TAG, "sdp创建成功       " + origSdp.type);
        String sdpString = origSdp.description;
        final SessionDescription sdp = new SessionDescription(origSdp.type, sdpString);
        localSdp = sdp;
        mSession.executor.execute(() -> pc.setLocalDescription(this, sdp));
    }

    @Override
    public void onSetSuccess() {
        mSession.executor.execute(() -> {
            Log.d(TAG, "sdp连接成功   " + pc.signalingState().toString());
            if (pc == null) return;
            // 发送者
            if (isOffer) {
                if (pc.getRemoteDescription() == null) {
                    Log.d(TAG, "Local SDP set succesfully");
                    if (!isOffer) {
                        //接收者，发送Answer
                        mSession.avEngineKit.mEvent.sendAnswer(userId, localSdp.description);
                    } else {
                        //发送者,发送自己的offer
                        mSession.avEngineKit.mEvent.sendOffer(userId, localSdp.description);
                    }
                } else {
                    Log.d(TAG, "Remote SDP set succesfully");

                    drainCandidates();
                }

            } else {
                if (pc.getLocalDescription() != null) {
                    Log.d(TAG, "Local SDP set succesfully");
                    if (!isOffer) {
                        //接收者，发送Answer
                        mSession.avEngineKit.mEvent.sendAnswer(userId, localSdp.description);
                    } else {
                        //发送者,发送自己的offer
                        mSession.avEngineKit.mEvent.sendOffer(userId, localSdp.description);
                    }

                    drainCandidates();
                } else {
                    Log.d(TAG, "Remote SDP set succesfully");
                }
            }
        });


    }

    @Override
    public void onCreateFailure(String error) {
        Log.i(TAG, " SdpObserver onCreateFailure:" + error);
    }

    @Override
    public void onSetFailure(String error) {
        Log.i(TAG, "SdpObserver onSetFailure:" + error);
    }


    private void drainCandidates() {
        if (queuedRemoteCandidates != null) {
            Log.d(TAG, "Add " + queuedRemoteCandidates.size() + " remote candidates");
            for (IceCandidate candidate : queuedRemoteCandidates) {
                pc.addIceCandidate(candidate);
            }
            queuedRemoteCandidates = null;
        }
    }

    private MediaConstraints offerOrAnswerConstraint() {
        MediaConstraints mediaConstraints = new MediaConstraints();
        ArrayList<MediaConstraints.KeyValuePair> keyValuePairs = new ArrayList<>();
        keyValuePairs.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        keyValuePairs.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        mediaConstraints.mandatory.addAll(keyValuePairs);
        return mediaConstraints;
    }

}
