package com.dds.core.ui.event;

public class MsgEvent<T> {
    private int code;
    private T data;
    public static final int CODE_ON_CALL_ENDED = 0X01;//语音视频通话结束
    public static final int CODE_ON_REMOTE_RING = 0X02;//对方已响铃


    public MsgEvent(int code) {
        this.code = code;
    }

    public MsgEvent(int code, T data) {
        this.code = code;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}

