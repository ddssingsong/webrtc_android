package com.dds.temple2.socket.utils;

import android.os.Handler;
import android.os.Looper;

/**
 * UI线程管理工具
 * 1. 持有主线程handler
 * 2. 判断某个线程是否是主线程
 * 3. 向主线程post()事件
 * Created by fighter_lee on 19/08/21.
 */
public class UIThreadUtil {

    private static final Handler handler;

    static {
        handler = new Handler(Looper.getMainLooper());
    }

    public static boolean isMainThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }

    /**
     * 回调到主线程
     */
    public static void postUI(Runnable run) {
        postUI(0, run);
    }

    /**
     * 回调到主线程
     */
    public static void postUI(long delay, Runnable run) {
        handler.postDelayed(run, delay);
    }


}
