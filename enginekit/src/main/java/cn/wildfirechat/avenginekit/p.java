package cn.wildfirechat.avenginekit;

import android.util.Log;

import org.webrtc.voiceengine.WebRtcAudioRecord;

/**
 * Created by dds on 2019/7/24.
 * android_shuai@163.com
 */
public class p implements WebRtcAudioRecord.WebRtcAudioRecordErrorCallback {
    p(w var1) {
        this.a = var1;
    }

    public void onWebRtcAudioRecordInitError(String var1) {
        Log.e("PCRTCClient", "onWebRtcAudioRecordInitError: " + var1);
        w.a(this.a, var1);
    }

    public void onWebRtcAudioRecordStartError(WebRtcAudioRecord.AudioRecordStartErrorCode var1, String var2) {
        Log.e("PCRTCClient", "onWebRtcAudioRecordStartError: " + var1 + ". " + var2);
        w.a(this.a, var2);
    }

    public void onWebRtcAudioRecordError(String var1) {
        Log.e("PCRTCClient", "onWebRtcAudioRecordError: " + var1);
        w.a(this.a, var1);
    }
}
