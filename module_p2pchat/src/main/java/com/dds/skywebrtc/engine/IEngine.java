package com.dds.skywebrtc.engine;


/**
 * rtc基类
 */
public interface IEngine {


    /**
     * 加入房間
     */
    void joinChannel();


    /**
     * 离开房间
     */
    void leaveChannel();


    /**
     * 开启本地预览
     */
    void startPreview();

    /**
     * 关闭本地预览
     */
    void stopPreview();


    /**
     * 有其他用戶進入
     */
    void peerIn();




}
