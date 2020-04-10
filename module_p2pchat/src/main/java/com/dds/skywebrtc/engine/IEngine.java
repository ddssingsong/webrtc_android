package com.dds.skywebrtc.engine;


/**
 * rtc基类
 */
public interface IEngine {


    /**
     * 初始化
     */
    void init();

    /**
     * 加入房間
     */
    void joinRoom();

    /**
     * 离开房间
     */
    void leaveRoom();

    /**
     * 开启本地预览
     */
    void startPreview();

    /**
     * 关闭本地预览
     */
    void stopPreview();

    /**
     * 开始远端推流
     */
    void startStream();

    /**
     * 停止远端推流
     */
    void stopStream();

    /**
     * 开始远端预览
     */
    void setupRemoteVideo();

    /**
     * 关闭远端预览
     */
    void stopRemoteVideo();

}
