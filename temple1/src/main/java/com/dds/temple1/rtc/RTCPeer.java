package com.dds.temple1.rtc;

import android.util.Log;

import org.webrtc.AudioTrack;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RTCStatsReport;
import org.webrtc.RtpReceiver;
import org.webrtc.RtpTransceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class RTCPeer implements SdpObserver, PeerConnection.Observer {
    private static final String TAG = "RTCPeer";
    private final PeerConnectionFactory mFactory;
    private PeerConnection pc;
    private List<IceCandidate> queuedRemoteCandidates;
    private SessionDescription localDescription;
    private final ExecutorService mExecutor;
    private boolean isInitiator;
    private final PeerConnectionEvents mEvents;
    private boolean isError;

    public RTCPeer(PeerConnectionFactory factory, ExecutorService executor, PeerConnectionEvents events) {
        this.mFactory = factory;
        mExecutor = executor;
        mEvents = events;
        pc = createPeerConnection();
        queuedRemoteCandidates = new ArrayList<>();
        isInitiator = false;
    }

    public PeerConnection createPeerConnection() {
        return mFactory.createPeerConnection(new ArrayList<>(), this);
    }

    public void createOffer() {
        if (pc == null) return;
        isInitiator = true;
        pc.createOffer(this, offerOrAnswerConstraint());
    }

    public void createAnswer() {
        if (pc == null) return;
        isInitiator = false;
        pc.createAnswer(this, offerOrAnswerConstraint());

    }

    public void addVideoTrack(VideoTrack videoTrack, List<String> mediaStreamLabels) {
        if (pc == null) return;
        pc.addTrack(videoTrack, mediaStreamLabels);
    }

    public void addAudioTrack(AudioTrack audioTrack, List<String> mediaStreamLabels) {
        if (pc == null) return;
        pc.addTrack(audioTrack, mediaStreamLabels);
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

    public List<RtpTransceiver> getTransceivers() {
        return pc.getTransceivers();
    }


    public void dispose() {
        if (pc == null) return;
        pc.dispose();
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

    public void getStats() {
        Log.d(TAG, "getStats: ");
        pc.getStats(mEvents::onPeerConnectionStatsReady);
    }



    // region ------------------------------SdpObserver------------------------
    @Override
    public void onCreateSuccess(SessionDescription desc) {
        if (localDescription != null) {
            Log.d(TAG, "onCreateSuccess: Multiple SDP create.");
            return;
        }
        String sdp = desc.description;
        final SessionDescription newDesc = new SessionDescription(desc.type, sdp);
        localDescription = newDesc;
        mExecutor.execute(() -> {
            if (pc == null || isError) {
                return;
            }
            Log.d(TAG, "onCreateSuccess: setLocalDescription");
            pc.setLocalDescription(RTCPeer.this, newDesc);
        });
    }

    @Override
    public void onSetSuccess() {
        mExecutor.execute(() -> {
            if (pc == null || isError) {
                return;
            }
            if (isInitiator) {
                if (pc.getRemoteDescription() == null) {
                    Log.d(TAG, "onSetSuccess Local SDP set successfully");
                    mEvents.onLocalDescription(localDescription);
                } else {
                    Log.d(TAG, "onSetSuccess Remote SDP set successfully");
                    drainCandidates();
                }
            } else {
                if (pc.getLocalDescription() != null) {
                    Log.d(TAG, "onSetSuccess Local SDP set successfully");
                    mEvents.onLocalDescription(localDescription);
                    drainCandidates();
                } else {
                    Log.d(TAG, "onSetSuccess Remote SDP set successfully");
                }
            }

        });

    }

    @Override
    public void onCreateFailure(String error) {
        reportError("createSDP error: " + error);
    }

    @Override
    public void onSetFailure(String error) {
        reportError("setSDP error: " + error);
    }

    // endregion

    // region ----------------------------PeerConnection.Observer--------------------

    @Override
    public void onSignalingChange(PeerConnection.SignalingState newState) {
        Log.d(TAG, "onSignalingChange: " + newState);
    }

    @Override
    public void onIceConnectionChange(PeerConnection.IceConnectionState newState) {
        mExecutor.execute(() -> {
            Log.d(TAG, "IceConnectionState: " + newState);
            if (newState == PeerConnection.IceConnectionState.CONNECTED) {
                mEvents.onIceConnected();
            } else if (newState == PeerConnection.IceConnectionState.DISCONNECTED) {
                mEvents.onIceDisconnected();
            } else if (newState == PeerConnection.IceConnectionState.FAILED) {
                reportError("ICE connection failed.");
            }
        });
    }

    @Override
    public void onConnectionChange(PeerConnection.PeerConnectionState newState) {
        mExecutor.execute(() -> {
            Log.d(TAG, "PeerConnectionState: " + newState);
            if (newState == PeerConnection.PeerConnectionState.CONNECTED) {
                mEvents.onConnected();
            } else if (newState == PeerConnection.PeerConnectionState.DISCONNECTED) {
                mEvents.onDisconnected();
            } else if (newState == PeerConnection.PeerConnectionState.FAILED) {
                reportError("DTLS connection failed.");
            }
        });
    }

    @Override
    public void onIceConnectionReceivingChange(boolean receiving) {
        Log.d(TAG, "onIceConnectionReceivingChange: " + receiving);
    }

    @Override
    public void onIceGatheringChange(PeerConnection.IceGatheringState newState) {
        Log.d(TAG, "onIceGatheringChange: " + newState);
    }

    @Override
    public void onIceCandidate(IceCandidate candidate) {
        mExecutor.execute(() -> mEvents.onIceCandidate(candidate));

    }

    @Override
    public void onIceCandidatesRemoved(IceCandidate[] candidates) {
        mExecutor.execute(() -> mEvents.onIceCandidatesRemoved(candidates));
    }

    @Override
    public void onAddStream(MediaStream stream) {
        Log.d(TAG, "onAddStream: " + stream);
    }

    @Override
    public void onRemoveStream(MediaStream stream) {
        Log.d(TAG, "onRemoveStream: " + stream);
    }

    @Override
    public void onDataChannel(DataChannel dataChannel) {
        Log.d(TAG, "onDataChannel: " + dataChannel);

    }

    @Override
    public void onRenegotiationNeeded() {

    }

    @Override
    public void onAddTrack(RtpReceiver receiver, MediaStream[] mediaStreams) {
        Log.d(TAG, "onAddTrack: RtpReceiver = " + receiver + ", MediaStream[] = " + Arrays.toString(mediaStreams));
    }

    @Override
    public void onTrack(RtpTransceiver transceiver) {
        PeerConnection.Observer.super.onTrack(transceiver);
        Log.d(TAG, "onTrack: " + transceiver);
    }



    // endregion


    public interface PeerConnectionEvents {
        /**
         * Callback fired once local SDP is created and set.
         */
        void onLocalDescription(final SessionDescription sdp);

        /**
         * Callback fired once local Ice candidate is generated.
         */
        void onIceCandidate(final IceCandidate candidate);

        /**
         * Callback fired once local ICE candidates are removed.
         */
        void onIceCandidatesRemoved(final IceCandidate[] candidates);

        /**
         * Callback fired once connection is established (IceConnectionState is
         * CONNECTED).
         */
        void onIceConnected();

        /**
         * Callback fired once connection is disconnected (IceConnectionState is
         * DISCONNECTED).
         */
        void onIceDisconnected();

        /**
         * Callback fired once DTLS connection is established (PeerConnectionState
         * is CONNECTED).
         */
        void onConnected();

        /**
         * Callback fired once DTLS connection is disconnected (PeerConnectionState
         * is DISCONNECTED).
         */
        void onDisconnected();

        /**
         * Callback fired once peer connection error happened.
         */
        void onPeerConnectionError(final String description);

        /**
         * Callback fired once peer connection statistics is ready.
         */
        void onPeerConnectionStatsReady(RTCStatsReport report);
    }

    private void reportError(final String errorMessage) {
        Log.e(TAG, "reportError error: " + errorMessage);
        mExecutor.execute(() -> {
            if (!isError) {
                mEvents.onPeerConnectionError(errorMessage);
                isError = true;
            }
        });
    }

}
