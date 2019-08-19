package com.dds.skywebrtc;

/**
 * Created by dds on 2019/8/19.
 * android_shuai@163.com
 */
public class CallSession {

    private CallSessionCallback sessionCallback;





    public interface CallSessionCallback {
        void didCallEndWithReason(AVEngineKit.CallEndReason var1);

        void didChangeState(AVEngineKit.CallState var1);

        void didChangeMode(boolean isAudio);

        void didCreateLocalVideoTrack();

        void didReceiveRemoteVideoTrack();

        void didError(String error);

    }
}
