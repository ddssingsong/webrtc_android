package cn.wildfirechat.avenginekit;

/**
 * Created by dds on 2019/7/24.
 * android_shuai@163.com
 */
public class u implements Runnable {
    u(w var1, boolean var2) {
        this.b = var1;
        this.a = var2;
    }

    public void run() {
        w.b(this.b, this.a);
        if (w.a(this.b) != null) {
            w.a(this.b).setEnabled(w.s(this.b));
        }

    }
}
