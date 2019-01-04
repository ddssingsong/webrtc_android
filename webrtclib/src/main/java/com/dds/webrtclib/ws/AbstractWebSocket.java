package com.dds.webrtclib.ws;

/**
 * Created by dds on 2019/1/3.
 * android_shuai@163.com
 */
public abstract class AbstractWebSocket {

    // 连接服务器
    abstract void connect(String wss, String room);

    abstract void close();

    // 加入房间
    abstract void joinRoom(String room);

    //处理回调消息
    abstract void handleMessage(String message);


}
