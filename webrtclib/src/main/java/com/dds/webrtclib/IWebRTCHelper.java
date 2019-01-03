package com.dds.webrtclib;

import org.webrtc.MediaStream;

/**
 * Created by dds on 2017/10/23.
 */

public interface IWebRTCHelper {

    void webRTCHelper_SetLocalStream(MediaStream stream, String userId);

    void webRTCHelper_AddRemoteStream(MediaStream stream, String userId);

    void webRTCHelper_CloseWithUserId(String userId);

}
