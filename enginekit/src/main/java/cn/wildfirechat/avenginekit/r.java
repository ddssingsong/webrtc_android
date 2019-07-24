package cn.wildfirechat.avenginekit;

import org.webrtc.StatsObserver;
import org.webrtc.StatsReport;

/**
 * Created by dds on 2019/7/24.
 * android_shuai@163.com
 */
public class r implements StatsObserver {
    r(w var1) {
        this.a = var1;
    }

    public void onComplete(StatsReport[] var1) {
        w.q(this.a).a(var1);
    }
}
