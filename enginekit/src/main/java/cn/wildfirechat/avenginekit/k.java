package cn.wildfirechat.avenginekit;

import java.util.TimerTask;

/**
 * Created by dds on 2019/7/24.
 * android_shuai@163.com
 */
public class k extends TimerTask {
    k(AVEngineKit.CallSession var1) {
        this.a = var1;
    }

    public void run() {
        if (AVEngineKit.a(this.a.n) != null && CallSession.c(AVEngineKit.a(this.a.n)) != CallState.Connected) {
            AVEngineKit.b(this.a.n).submit(() -> {
                if (AVEngineKit.a(this.a.n) != null && CallSession.c(AVEngineKit.a(this.a.n)) != CallState.Connected) {
                    if (CallSession.b(AVEngineKit.a(this.a.n)) != null) {
                        CallSession.b(AVEngineKit.a(this.a.n)).didError("Wait anwser timeout");
                    }

                    CallSession.a(AVEngineKit.a(this.a.n), CallEndReason.Timeout);
                }

            });
        }

    }
}

