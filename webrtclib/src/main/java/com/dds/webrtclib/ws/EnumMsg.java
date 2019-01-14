package com.dds.webrtclib.ws;

/**
 * Created by dds on 2019/1/11.
 * android_shuai@163.com
 */
public class EnumMsg {

    //挂断类型
    public enum Decline {
        Busy, Refuse, Cancel
    }

    public enum Type {
        //1=语音，2=视频
        AGREE("1"),
        DELETE("2");

        public final String value;

        Type(String value) {
            this.value = value;
        }
    }
}
