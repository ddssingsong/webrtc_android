package com.dds.skywebrtc.engine.webrtc;

import android.content.Context;
import android.util.Log;

import com.dds.skywebrtc.render.ProxyVideoSink;

import org.webrtc.DataChannel;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RendererCommon;
import org.webrtc.RtpReceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceViewRenderer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dds on 2020/3/11.
 * android_shuai@163.com
 */
public class Peer implements SdpObserver, PeerConnection.Observer {
    private final static String TAG = "dds_Peer";
    private final PeerConnection pc;
    private final String mUserId;
    private List<IceCandidate> queuedRemoteCandidates;
    private SessionDescription localSdp;
    private final PeerConnectionFactory mFactory;
    private final List<PeerConnection.IceServer> mIceLis;
    private final IPeerEvent mEvent;
    private boolean isOffer;

    public MediaStream _remoteStream;
    public SurfaceViewRenderer renderer;
    public ProxyVideoSink sink;


    public Peer(PeerConnectionFactory factory, List<PeerConnection.IceServer> list, String userId, IPeerEvent event) {
        mFactory = factory;
        mIceLis = list;
        mEvent = event;
        mUserId = userId;
        queuedRemoteCandidates = new ArrayList<>();
        this.pc = createPeerConnection();
        Log.d("dds_test", "create Peer:" + mUserId);

    }

    public PeerConnection createPeerConnection() {
        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(mIceLis);
        if (mFactory != null) {
            return mFactory.createPeerConnection(rtcConfig, this);
        } else {
            return null;
        }
    }

    public void setOffer(boolean isOffer) {
        this.isOffer = isOffer;
    }

    // 创建offer
    public void createOffer() {
        if (pc == null) return;
        Log.d("dds_test", "createOffer");
        pc.createOffer(this, offerOrAnswerConstraint());
    }

    // 创建answer
    public void createAnswer() {
        if (pc == null) return;
        Log.d("dds_test", "createAnswer");
        pc.createAnswer(this, offerOrAnswerConstraint());

    }

    // 设置LocalDescription
    public void setLocalDescription(SessionDescription sdp) {
        Log.d("dds_test", "setLocalDescription");
        if (pc == null) return;
        pc.setLocalDescription(this, sdp);
    }

    // 设置RemoteDescription
    public void setRemoteDescription(SessionDescription sdp) {
        if (pc == null) return;
        Log.d("dds_test", "setRemoteDescription");
        pc.setRemoteDescription(this, sdp);
    }

    //添加本地流
    public void addLocalStream(MediaStream stream) {
        if (pc == null) return;
        Log.d("dds_test", "addLocalStream" + mUserId);
        pc.addStream(stream);
    }

    // 添加RemoteIceCandidate
    public synchronized void addRemoteIceCandidate(final IceCandidate candidate) {
        Log.d("dds_test", "addRemoteIceCandidate");
        if (pc != null) {
            if (queuedRemoteCandidates != null) {
               Log.d("dds_test", "addRemoteIceCandidate  2222");
                synchronized (Peer.class) {
                    if (queuedRemoteCandidates != null) {
                        queuedRemoteCandidates.add(candidate);
                    }
                }

            } else {
               Log.d("dds_test", "addRemoteIceCandidate1111");
                pc.addIceCandidate(candidate);
            }
        }
    }

    // 移除RemoteIceCandidates
    public void removeRemoteIceCandidates(final IceCandidate[] candidates) {
        if (pc == null) {
            return;
        }
        drainCandidates();
        pc.removeIceCandidates(candidates);
    }

    public void createRender(EglBase mRootEglBase, Context context, boolean isOverlay) {
        renderer = new SurfaceViewRenderer(context);
        renderer.init(mRootEglBase.getEglBaseContext(), new RendererCommon.RendererEvents() {
            @Override
            public void onFirstFrameRendered() {
                Log.d(TAG, "createRender onFirstFrameRendered");

            }

            @Override
            public void onFrameResolutionChanged(int videoWidth, int videoHeight, int rotation) {
                Log.d(TAG, "createRender onFrameResolutionChanged");
            }
        });
        renderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        renderer.setMirror(true);
        renderer.setZOrderMediaOverlay(isOverlay);
        sink = new ProxyVideoSink();
        sink.setTarget(renderer);
        if (_remoteStream != null && _remoteStream.videoTracks.size() > 0) {
            _remoteStream.videoTracks.get(0).addSink(sink);
        }

    }

    // 关闭Peer
    public void close() {
        if (renderer != null) {
            renderer.release();
            renderer = null;
        }
        if (sink != null) {
            sink.setTarget(null);
        }
        if (pc != null) {
            try {
                pc.close();
                pc.dispose();
            } catch (Exception e) {

            }


        }


    }

    //------------------------------Observer-------------------------------------
    @Override
    public void onSignalingChange(PeerConnection.SignalingState signalingState) {
        Log.i(TAG, "onSignalingChange: " + signalingState);
    }

    @Override
    public void onIceConnectionChange(PeerConnection.IceConnectionState newState) {
        Log.i(TAG, "onIceConnectionChange: " + newState);
        if (newState == PeerConnection.IceConnectionState.DISCONNECTED || newState == PeerConnection.IceConnectionState.FAILED) {
            mEvent.onDisconnected(mUserId);
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
        // 发送IceCandidate
        mEvent.onSendIceCandidate(mUserId, candidate);
    }

    @Override
    public void onIceCandidatesRemoved(IceCandidate[] candidates) {
        Log.i(TAG, "onIceCandidatesRemoved:");
    }

    @Override
    public void onAddStream(MediaStream stream) {
        Log.i(TAG, "onAddStream:");
        stream.audioTracks.get(0).setEnabled(true);
        _remoteStream = stream;
        if (mEvent != null) {
            mEvent.onRemoteStream(mUserId, stream);
        }
    }

    @Override
    public void onRemoveStream(MediaStream stream) {
        Log.i(TAG, "onRemoveStream:");
        if (mEvent != null) {
            mEvent.onRemoveStream(mUserId, stream);
        }
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
        Log.i(TAG, "onAddTrack:" + mediaStreams.length);
    }


    //-------------SdpObserver--------------------
    @Override
    public void onCreateSuccess(SessionDescription origSdp) {
        Log.d(TAG, "sdp创建成功       " + origSdp.type);
        String sdpString = origSdp.description;
        final SessionDescription sdp = new SessionDescription(origSdp.type, sdpString);
        localSdp = sdp;
        setLocalDescription(sdp);

    }

    @Override
    public void onSetSuccess() {
        Log.d(TAG, "sdp连接成功   " + pc.signalingState().toString());
        if (pc == null) return;
        // 发送者
        if (isOffer) {
            if (pc.getRemoteDescription() == null) {
                Log.d(TAG, "Local SDP set succesfully");
                if (!isOffer) {
                    //接收者，发送Answer
                    mEvent.onSendAnswer(mUserId, localSdp);
                } else {
                    //发送者,发送自己的offer
                    mEvent.onSendOffer(mUserId, localSdp);
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
                    mEvent.onSendAnswer(mUserId, localSdp);
                } else {
                    //发送者,发送自己的offer
                    mEvent.onSendOffer(mUserId, localSdp);
                }

                drainCandidates();
            } else {
                Log.d(TAG, "Remote SDP set succesfully");
            }
        }


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
        Log.i("dds_test", "drainCandidates");
        synchronized (Peer.class) {
            if (queuedRemoteCandidates != null) {
                Log.d(TAG, "Add " + queuedRemoteCandidates.size() + " remote candidates");
                for (IceCandidate candidate : queuedRemoteCandidates) {
                    pc.addIceCandidate(candidate);
                }
                queuedRemoteCandidates = null;
            }

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

    // ----------------------------回调-----------------------------------

    public interface IPeerEvent {


        void onSendIceCandidate(String userId, IceCandidate candidate);

        void onSendOffer(String userId, SessionDescription description);

        void onSendAnswer(String userId, SessionDescription description);

        void onRemoteStream(String userId, MediaStream stream);

        void onRemoveStream(String userId, MediaStream stream);


        void onDisconnected(String userId);
    }

}
