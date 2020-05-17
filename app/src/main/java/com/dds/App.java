package com.dds;

import android.app.Application;

import com.dds.core.voip.VoipEvent;
import com.dds.net.HttpRequestPresenter;
import com.dds.net.urlconn.UrlConnRequest;
import com.dds.skywebrtc.SkyEngineKit;
import com.squareup.leakcanary.LeakCanary;

/**
 * Created by dds on 2019/8/25.
 * android_shuai@163.com
 */
public class App extends Application {

    private static App app;
    private String username = "";

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;

        // initLeakCanary();
        // 初始化网络请求
        HttpRequestPresenter.init(new UrlConnRequest());

        SkyEngineKit.init(new VoipEvent());

    }

    public static App getInstance() {
        return app;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    private void initLeakCanary() {
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        LeakCanary.install(this);
    }
}
