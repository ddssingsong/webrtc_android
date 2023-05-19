package com.dds.temple.rtc;

import android.util.Log;

import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.RtpReceiver;
import org.webrtc.RtpTransceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

import java.util.ArrayList;
import java.util.List;

public class RTCPeer implements SdpObserver, PeerConnection.Observer {
    private static final String TAG = "RTCPeer";
    private final PeerConnection pc;
    private List<IceCandidate> queuedRemoteCandidates;

    public RTCPeer(PeerConnection pc) {
        this.pc = pc;
        queuedRemoteCandidates = new ArrayList<>();
    }

    public void createOffer() {
        if (pc == null) return;
        pc.createOffer(this, offerOrAnswerConstraint());
    }

    public void createAnswer() {
        if (pc == null) return;
        pc.createAnswer(this, offerOrAnswerConstraint());

    }

    public void setRemoteDescription(SessionDescription sdp) {
        if (pc == null) return;
        pc.setRemoteDescription(this, sdp);
    }

    public void addRemoteIceCandidate(final IceCandidate candidate) {
        if (pc == null) return;
        if (queuedRemoteCandidates != null) {
            queuedRemoteCandidates.add(candidate);
        } else {
            pc.addIceCandidate(candidate);
        }

    }

    public void removeRemoteIceCandidates(final IceCandidate[] candidates) {
        if (pc == null) {
            return;
        }
        drainCandidates();
        pc.removeIceCandidates(candidates);
    }

    private MediaConstraints offerOrAnswerConstraint() {
        MediaConstraints mediaConstraints = new MediaConstraints();
        ArrayList<MediaConstraints.KeyValuePair> keyValuePairs = new ArrayList<>();
        keyValuePairs.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        keyValuePairs.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        mediaConstraints.mandatory.addAll(keyValuePairs);
        return mediaConstraints;
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


    // region ------------------------------SdpObserver------------------------
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

    // endregion

    // region ----------------------------PeerConnection.Observer--------------------

    @Override
    public void onSignalingChange(PeerConnection.SignalingState newState) {

    }

    @Override
    public void onIceConnectionChange(PeerConnection.IceConnectionState newState) {

    }

    @Override
    public void onConnectionChange(PeerConnection.PeerConnectionState newState) {
        PeerConnection.Observer.super.onConnectionChange(newState);
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

    @Override
    public void onTrack(RtpTransceiver transceiver) {
        PeerConnection.Observer.super.onTrack(transceiver);
    }

    // endregion
}
