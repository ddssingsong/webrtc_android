package cn.wildfirechat.avenginekit;

import android.util.Log;

/**
 * Created by dds on 2019/7/23.
 * android_shuai@163.com
 */
public class z implements Runnable {
    z(e var1) {
        this.a = var1;
    }

    public void run() {
        if (w.f(this.a.a) != null && !w.g(this.a.a)) {
            if (w.m(this.a.a)) {
                if (w.f(this.a.a).getRemoteDescription() == null) {
                    Log.d("PCRTCClient", "Local SDP set succesfully");
                    w.q(this.a.a).a(w.h(this.a.a));
                } else {
                    Log.d("PCRTCClient", "Remote SDP set succesfully");
                    w.n(this.a.a);
                }
            } else if (w.f(this.a.a).getLocalDescription() != null) {
                Log.d("PCRTCClient", "Local SDP set succesfully");
                w.q(this.a.a).a(w.h(this.a.a));
                w.n(this.a.a);
            } else {
                Log.d("PCRTCClient", "Remote SDP set succesfully");
            }

        }
    }
}
