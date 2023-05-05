package com.dds.skywebrtc.engine.webrtc;


/**
 * event callback for WebRTC adaptation
 *
 */
public interface IWebRTCEvent {

    // sendOffer
    void sendOffer(String userId, String sdp);

    // sendAnswer
    void sendAnswer(String userId, String sdp);

    // sendIceCandidate
    void sendIceCandidate(String userId, String id, int label, String candidate);
}
