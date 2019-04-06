package com.dds;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

/**
 * Created by dds on 2019/4/5.
 * android_shuai@163.com
 */
public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
    }
}


