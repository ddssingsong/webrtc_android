package cn.wildfirechat.avenginekit;

import cn.wildfirechat.remote.SendMessageCallback;

/**
 * Created by dds on 2019/7/24.
 * android_shuai@163.com
 */
public class g implements SendMessageCallback {
    g(h var1) {
        this.a = var1;
    }

    public void onSuccess(long var1, long var3) {
        long var10000 = this.a.c.messageId;
    }

    public void onFail(int var1) {
    }

    public void onPrepare(long var1, long var3) {
    }

    public void onMediaUpload(String var1) {
    }
}
