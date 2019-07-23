package cn.wildfirechat.avenginekit.b;

import android.os.Build;
import android.os.Build.VERSION;
import android.util.Log;

;

/**
 * Created by dds on 2019/7/23.
 * android_shuai@163.com
 */
public final class a {
    private a() {
    }

    public static void a(boolean var0) {
        if (!var0) {
            throw new AssertionError("Expected condition to be true");
        }
    }

    public static String a() {
        return "@[name=" + Thread.currentThread().getName() + ", id=" + Thread.currentThread().getId() + "]";
    }

    public static void a(String var0) {
        Log.d(var0, "Android SDK: " + Build.VERSION.SDK_INT + ", Release: " + VERSION.RELEASE + ", Brand: " + Build.BRAND + ", Device: " + Build.DEVICE + ", Id: " + Build.ID + ", Hardware: " + Build.HARDWARE + ", Manufacturer: " + Build.MANUFACTURER + ", Model: " + Build.MODEL + ", Product: " + Build.PRODUCT);
    }
}

