package com.dds.rtc_demo;

import android.app.Application;

import com.dds.rtc_demo.core.util.CrashHandler;
import com.dds.rtc_demo.core.voip.VoipEvent;
import com.dds.base.net.HttpRequestPresenter;
import com.dds.base.net.urlconn.UrlConnRequest;
import com.dds.rtc_chat.SkyEngineKit;

/**
 * Created by dds on 2019/8/25.
 * android_shuai@163.com
 */
public class App extends Application {

    private static App app;
    private String username = "";
    private String roomId = "";
    private String otherUserId = "";

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler());
        // init http request
        HttpRequestPresenter.init(new UrlConnRequest());
        // init SkyEngineKit
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

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getOtherUserId() {
        return otherUserId;
    }

    public void setOtherUserId(String otherUserId) {
        this.otherUserId = otherUserId;
    }
}
