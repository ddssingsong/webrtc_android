package com.dds.core.util;

import android.app.ActivityManager;
import android.app.Application;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.dds.App;

import java.util.List;

public class Utils {

    public static boolean isAppRunningForeground() {
        ActivityManager activityManager =
                (ActivityManager) App.getInstance().getSystemService(Application.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcessInfo : runningAppProcesses) {
            if (appProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                if (appProcessInfo.processName.equals(App.getInstance().getApplicationInfo().processName))
                    return true;
            }
        }
        return false;
    }
}
