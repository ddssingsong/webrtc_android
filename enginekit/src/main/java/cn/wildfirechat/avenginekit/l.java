package cn.wildfirechat.avenginekit;

import java.util.TimerTask;

/**
 * Created by dds on 2019/7/24.
 * android_shuai@163.com
 */
public class l  extends TimerTask {
    l(CallSession var1) {
        this.a = var1;
    }

    public void run() {
        if (AVEngineKit.a(this.a.n) != null && CallSession.c(AVEngineKit.a(this.a.n)) != CallState.Connected) {
            AVEngineKit.b(this.a.n).submit(() -> {
                if (AVEngineKit.a(this.a.n) != null && CallSession.c(AVEngineKit.a(this.a.n)) != CallState.Connected) {
                    CallSession.b(AVEngineKit.a(this.a.n)).didError("Connect timeout");
                    CallSession.a(AVEngineKit.a(this.a.n), CallEndReason.Timeout);
                }

            });
        }

    }
}