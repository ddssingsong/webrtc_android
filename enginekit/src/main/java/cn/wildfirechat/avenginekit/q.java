package cn.wildfirechat.avenginekit;

import android.util.Log;

import org.webrtc.voiceengine.WebRtcAudioTrack;

/**
 * Created by dds on 2019/7/24.
 * android_shuai@163.com
 */
public class q implements WebRtcAudioTrack.ErrorCallback {
    q(w var1) {
        this.a = var1;
    }

    public void onWebRtcAudioTrackInitError(String var1) {
        Log.e("PCRTCClient", "onWebRtcAudioTrackInitError: " + var1);
        w.a(this.a, var1);
    }

    public void onWebRtcAudioTrackStartError(WebRtcAudioTrack.AudioTrackStartErrorCode var1, String var2) {
        Log.e("PCRTCClient", "onWebRtcAudioTrackStartError: " + var1 + ". " + var2);
        w.a(this.a, var2);
    }

    public void onWebRtcAudioTrackError(String var1) {
        Log.e("PCRTCClient", "onWebRtcAudioTrackError: " + var1);
        w.a(this.a, var1);
    }
}
