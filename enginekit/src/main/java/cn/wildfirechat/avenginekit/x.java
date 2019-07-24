package cn.wildfirechat.avenginekit;

import android.util.Log;

import org.webrtc.DataChannel;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Created by dds on 2019/7/24.
 * android_shuai@163.com
 */
public class x implements DataChannel.Observer {
    x(w.b var1, DataChannel var2) {
        this.b = var1;
        this.a = var2;
    }

    public void onBufferedAmountChange(long var1) {
        Log.d("PCRTCClient", "Data channel buffered amount changed: " + this.a.label() + ": " + this.a.state());
    }

    public void onStateChange() {
        Log.d("PCRTCClient", "Data channel state changed: " + this.a.label() + ": " + this.a.state());
    }

    public void onMessage(DataChannel.Buffer var1) {
        if (var1.binary) {
            Log.d("PCRTCClient", "Received binary msg over " + this.a);
        } else {
            ByteBuffer var10000 = var1.data;
            byte[] var3;
            var10000.get(var3 = new byte[var10000.capacity()]);
            String var2;
            var2 = new String.<init>(var3, Charset.forName("UTF-8"));
            Log.d("PCRTCClient", "Got msg: " + var2 + " over " + this.a);
        }
    }
}

