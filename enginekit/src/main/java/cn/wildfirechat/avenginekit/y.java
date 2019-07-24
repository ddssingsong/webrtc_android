package cn.wildfirechat.avenginekit;

import android.util.Log;

import org.webrtc.SessionDescription;

/**
 * Created by dds on 2019/7/24.
 * android_shuai@163.com
 */
public class y implements Runnable {
    y(e var1, SessionDescription var2) {
        this.b = var1;
        this.a = var2;
    }

    public void run() {
        if (w.f(this.b.a) != null && !w.g(this.b.a)) {
            Log.d("PCRTCClient", "Set local SDP from " + this.a.type);
            w.f(this.b.a).setLocalDescription(w.l(this.b.a), this.a);
        }

    }
}

