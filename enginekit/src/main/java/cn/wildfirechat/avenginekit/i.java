package cn.wildfirechat.avenginekit;

import java.util.concurrent.Callable;

/**
 * Created by dds on 2019/7/24.
 * android_shuai@163.com
 */
public class i implements Callable<Boolean> {
    i(AVEngineKit.CallSession var1, boolean var2) {
        this.b = var1;
        this.a = var2;
    }

    public Boolean call() {
        if (AVEngineKit.k(this.b.n) != null) {
            AVEngineKit.k(this.b.n).a(this.a ^ true);
            return true;
        } else {
            return false;
        }
    }
}
