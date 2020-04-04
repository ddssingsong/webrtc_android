package com.dds.skywebrtc;


/**
 * rtc基类
 */
public interface IEngine {


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




}
