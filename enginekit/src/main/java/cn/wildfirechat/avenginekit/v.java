package cn.wildfirechat.avenginekit;

/**
 * Created by dds on 2019/7/24.
 * android_shuai@163.com
 */
public class v implements Runnable {
    v(w var1, boolean var2) {
        this.b = var1;
        this.a = var2;
    }

    public void run() {
        w.a(this.b, this.a);
        if (w.c(this.b) != null) {
            w.c(this.b).setEnabled(w.b(this.b));
        }

        if (w.d(this.b) != null) {
            w.d(this.b).setEnabled(w.b(this.b));
        }

    }
}
