package cn.wildfirechat.avenginekit;

import android.telecom.VideoProfile;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;
import org.webrtc.StatsReport;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoTrack;


/**
 * Created by dds on 2019/7/24.
 * android_shuai@163.com
 */
public class f implements c {
    f(AVEngineKit var1) {
        this.a = var1;
    }

    public void a(SessionDescription var1) {
        j var2;
        var2 = new j. < init > (AVEngineKit.CallSession.a(AVEngineKit.a(this.a)));
        JSONObject var3;
        JSONObject var10001 = var3 = new JSONObject;
        var3.<init> ();
        AVEngineKit.a(var10001, "sdp", var1.description);
        if (AVEngineKit.h(this.a)) {
            AVEngineKit.a(var3, "type", "offer");
        } else {
            AVEngineKit.a(var3, "type", "answer");
        }

        f var10000 = this;
        var2.a(var3.toString().getBytes());
        AVEngineKit var10002 = this.a;
        AVEngineKit.a(var10002, var2, CallSession.e(AVEngineKit.a(var10002)), true);
        VideoProfile var4 = VideoProfile.getVideoProfile(AVEngineKit.i(this.a), AVEngineKit.j(this.a));
        Log.d("CallRTCClient", "Set video maximum bitrate: " + var4.bitrate);
        AVEngineKit.k(var10000.a).a(var4.bitrate);
    }

    public void onIceCandidate(IceCandidate var1) {
        f var10000 = this;
        j var2;
        j var10001 = var2 = new j;
        var10001.<init> (CallSession.a(AVEngineKit.a(this.a)));
        JSONObject var3;
        JSONObject var10002 = var3 = new JSONObject;
        var3.<init> ();
        AVEngineKit.a(var3, "type", "candidate");
        AVEngineKit.a(var3, "label", var1.sdpMLineIndex);
        AVEngineKit.a(var3, "id", var1.sdpMid);
        AVEngineKit.a(var10002, "candidate", var1.sdp);
        var10001.a(var10002.toString().getBytes());
        AVEngineKit var4 = var10000.a;
        AVEngineKit.a(var4, var2, CallSession.e(AVEngineKit.a(var4)), false);
    }

    public void onIceCandidatesRemoved(IceCandidate[] var1) {
        JSONObject var2;
        JSONObject var10001 = var2 = new JSONObject;
        var10001.<init> ();
        AVEngineKit.a(var10001, "type", "remove-candidates");
        JSONArray var3;
        var3 = new JSONArray. < init > ();
        int var4 = var1.length;

        for (int var5 = 0; var5 < var4; ++var5) {
            var3.put(AVEngineKit.a(var1[var5]));
        }

        AVEngineKit.a(var2, "candidates", var3);
        j var6;
        j var7 = var6 = new j;
        var6.<init> (CallSession.a(AVEngineKit.a(this.a)));
        var7.a(var2.toString().getBytes());
        AVEngineKit var10000 = this.a;
        AVEngineKit.a(var10000, var6, CallSession.e(AVEngineKit.a(var10000)), false);
    }

    public void d() {
        AVEngineKit.b(this.a).submit(() -> {
            if (AVEngineKit.k(this.a) != null && AVEngineKit.a(this.a) != null && AVEngineKit.a(this.a).getState() != CallState.Idle) {
                CallSession.a(AVEngineKit.a(this.a), System.currentTimeMillis());
                AVEngineKit.k(this.a).a(true, 1000);
                CallSession.a(AVEngineKit.a(this.a), CallState.Connected);
            } else {
                Log.w("CallRTCClient", "Call is connected in closed or error state");
            }
        });
    }

    public void b() {
        AVEngineKit.b(this.a).submit(() -> {
            if (AVEngineKit.a(this.a) != null && AVEngineKit.a(this.a).getState() != CallState.Idle) {
                CallSession.a(AVEngineKit.a(this.a), CallEndReason.MediaError);
            }

        });
    }

    public void c(VideoTrack var1) {
        AVEngineKit.b(this.a).submit(() -> {
            AVEngineKit.b(this.a, var1);
            CallSession.b(AVEngineKit.a(this.a)).didReceiveRemoteVideoTrack();
        });
    }

    public void b(VideoTrack var1) {
        AVEngineKit.b(this.a).submit(() -> {
            AVEngineKit.b(this.a, (VideoTrack) null);
            if (AVEngineKit.a(this.a) != null) {
                CallSession.a(AVEngineKit.a(this.a), (VideoRenderer) null);
            }

        });
    }

    public void a(VideoTrack var1) {
        AVEngineKit.b(this.a).submit(() -> {
            AVEngineKit.a(this.a, var1);
            CallSession.b(AVEngineKit.a(this.a)).didCreateLocalVideoTrack();
        });
    }

    public void a() {
        AVEngineKit.b(this.a).submit(() -> {
            AVEngineKit.a(this.a, (VideoTrack) null);
        });
    }

    public void c() {
    }

    public void a(StatsReport[] var1) {
        CallSession.b(AVEngineKit.a(this.a)).didGetStats(var1);
    }

    public void a(String var1) {
        AVEngineKit.b(this.a).submit(() -> {
            CallSession.b(AVEngineKit.a(this.a)).didError(var1);
            CallSession.a(AVEngineKit.a(this.a), CallEndReason.MediaError);
        });
    }
}