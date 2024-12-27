package com.dds.temple2.socket.utils;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;


public class ContextVal {

    public static final String ACTION_APP_BOOT_COMPLETED = "com.fighter.common.action.app.boot.completed";

    @SuppressLint("StaticFieldLeak")
    private static Context sContext;

    public static void setContext(Context context) {
        sContext = context;
    }

    public static @NonNull
    Context getContext() {
        return sContext;
    }
}
