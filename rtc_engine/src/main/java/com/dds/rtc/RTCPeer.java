package com.dds.rtc;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.StringDef;

import org.webrtc.AudioTrack;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RTCStatsReport;
import org.webrtc.RtpReceiver;
import org.webrtc.RtpSender;
import org.webrtc.RtpTransceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.VideoTrack;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RTCPeer implements SdpObserver, PeerConnection.Observer {
    private static final String TAG = "RTCPeer";
    private final PeerConnectionFactory mFactory;
    private final PeerConnection pc;
    private List<IceCandidate> queuedRemoteCandidates;
    private SessionDescription localDescription;
    private final ExecutorService mExecutor;
    private boolean isInitiator;
    private final PeerConnectionEvents mEvents;
    private boolean isError;

    private static final String VIDEO_TRACK_TYPE = "video";
    public static final String VIDEO_CODEC_VP8 = "VP8";
    public static final String VIDEO_CODEC_VP9 = "VP9";
    public static final String VIDEO_CODEC_H264 = "H264";
    public static final String VIDEO_CODEC_H264_BASELINE = "H264 Baseline";
    public static final String VIDEO_CODEC_H264_HIGH = "H264 High";
    public static final String VIDEO_CODEC_AV1 = "AV1";

    @StringDef({VIDEO_CODEC_VP8, VIDEO_CODEC_VP9, VIDEO_CODEC_H264, VIDEO_CODEC_H264_BASELINE, VIDEO_CODEC_H264_HIGH, VIDEO_CODEC_AV1})
    @Retention(RetentionPolicy.SOURCE)
    @interface VideoCodeType {

    }

    private String videoCodecType = VIDEO_CODEC_VP8;


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

    public void setRemoteDescription(SessionDescription desc) {
        if (pc == null) return;
        String sdp = desc.description;
        sdp = preferCodec(sdp, videoCodecType, false);
        SessionDescription sdpRemote = new SessionDescription(desc.type, sdp);
        pc.setRemoteDescription(this, sdpRemote);
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
        mediaConstraints.optional.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement","true"));
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

    public RtpSender findVideoSender() {
        for (RtpSender sender : pc.getSenders()) {
            if (sender.track() != null) {
                String trackType = sender.track().kind();
                if (trackType.equals(VIDEO_TRACK_TYPE)) {
                    Log.d(TAG, "Found video sender.");
                    return sender;
                }
            }
        }
        return null;
    }

    public void setVideoCodecType(@VideoCodeType String videoCodecType) {
        this.videoCodecType = videoCodecType;
    }

    private static String preferCodec(String sdp, String codec, boolean isAudio) {
        final String[] lines = sdp.split("\r\n");
        final int mLineIndex = findMediaDescriptionLine(isAudio, lines);
        if (mLineIndex == -1) {
            Log.w(TAG, "No mediaDescription line, so can't prefer " + codec);
            return sdp;
        }
        // A list with all the payload types with name `codec`. The payload types are integers in the
        // range 96-127, but they are stored as strings here.
        final List<String> codecPayloadTypes = new ArrayList<>();
        // a=rtpmap:<payload type> <encoding name>/<clock rate> [/<encoding parameters>]
        final Pattern codecPattern = Pattern.compile("^a=rtpmap:(\\d+) " + codec + "(/\\d+)+[\r]?$");
        for (String line : lines) {
            Matcher codecMatcher = codecPattern.matcher(line);
            if (codecMatcher.matches()) {
                codecPayloadTypes.add(codecMatcher.group(1));
            }
        }
        if (codecPayloadTypes.isEmpty()) {
            Log.w(TAG, "No payload types with name " + codec);
            return sdp;
        }

        final String newMLine = movePayloadTypesToFront(codecPayloadTypes, lines[mLineIndex]);
        if (newMLine == null) {
            return sdp;
        }
        Log.d(TAG, "Change media description from: " + lines[mLineIndex] + " to " + newMLine);
        lines[mLineIndex] = newMLine;
        return joinString(Arrays.asList(lines), "\r\n", true /* delimiterAtEnd */);
    }

    /**
     * Returns the line number containing "m=audio|video", or -1 if no such line exists.
     */
    private static int findMediaDescriptionLine(boolean isAudio, String[] sdpLines) {
        final String mediaDescription = isAudio ? "m=audio " : "m=video ";
        for (int i = 0; i < sdpLines.length; ++i) {
            if (sdpLines[i].startsWith(mediaDescription)) {
                return i;
            }
        }
        return -1;
    }

    private static @Nullable String movePayloadTypesToFront(
            List<String> preferredPayloadTypes, String mLine) {
        // The format of the media description line should be: m=<media> <port> <proto> <fmt> ...
        final List<String> origLineParts = Arrays.asList(mLine.split(" "));
        if (origLineParts.size() <= 3) {
            Log.e(TAG, "Wrong SDP media description format: " + mLine);
            return null;
        }
        final List<String> header = origLineParts.subList(0, 3);
        final List<String> unpreferredPayloadTypes =
                new ArrayList<>(origLineParts.subList(3, origLineParts.size()));
        unpreferredPayloadTypes.removeAll(preferredPayloadTypes);
        // Reconstruct the line with `preferredPayloadTypes` moved to the beginning of the payload
        // types.
        final List<String> newLineParts = new ArrayList<>();
        newLineParts.addAll(header);
        newLineParts.addAll(preferredPayloadTypes);
        newLineParts.addAll(unpreferredPayloadTypes);
        return joinString(newLineParts, " ", false /* delimiterAtEnd */);
    }

    private static String joinString(
            Iterable<? extends CharSequence> s, String delimiter, boolean delimiterAtEnd) {
        Iterator<? extends CharSequence> iter = s.iterator();
        if (!iter.hasNext()) {
            return "";
        }
        StringBuilder buffer = new StringBuilder(iter.next());
        while (iter.hasNext()) {
            buffer.append(delimiter).append(iter.next());
        }
        if (delimiterAtEnd) {
            buffer.append(delimiter);
        }
        return buffer.toString();
    }

    // region ------------------------------SdpObserver------------------------
    @Override
    public void onCreateSuccess(SessionDescription desc) {
        if (localDescription != null) {
            Log.d(TAG, "onCreateSuccess: Multiple SDP create.");
            return;
        }
        String sdp = desc.description;
        sdp = preferCodec(sdp, videoCodecType, false);
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
