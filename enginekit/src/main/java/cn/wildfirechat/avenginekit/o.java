package cn.wildfirechat.avenginekit;

/**
 * Created by dds on 2019/7/24.
 * android_shuai@163.com
 */
public class o implements Runnable {
    o(w var1) {
        this.a = var1;
    }

    public void run() {
        try {
            w.o(this.a);
            w.p(this.a);
        } catch (Exception var2) {
            w.a(this.a, "Failed to create peer connection: " + var2.getMessage());
            throw var2;
        }
    }
}
