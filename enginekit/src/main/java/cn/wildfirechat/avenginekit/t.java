package cn.wildfirechat.avenginekit;

import java.util.TimerTask;

/**
 * Created by dds on 2019/7/24.
 * android_shuai@163.com
 */
public class t extends TimerTask {
    t(w var1) {
        this.a = var1;
    }

    public void run() {
        w.a().execute(new s(this));
    }
}
