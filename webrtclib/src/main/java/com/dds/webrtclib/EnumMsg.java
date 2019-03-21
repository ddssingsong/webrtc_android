package com.dds.webrtclib;

/**
 * Created by dds on 2019/1/11.
 * android_shuai@163.com
 */
public class EnumMsg {

    //挂断类型
    public enum Decline {
        Busy, Refuse, Cancel
    }

    //是拨出方还是接听方
    public enum Direction {
        Outgoing, Incoming
    }

    //通话类型
    public enum MediaType {
        //1=语音，2=视频
        Audio("1"),
        Video("2"),
        Meeting("3");

        public final String value;

        MediaType(String value) {
            this.value = value;
        }
    }

    //通话状态
    public enum CallState {
        Outgoing(0),
        Incoming(1),
        Calling(2);
        public final int value;

        CallState(int value) {
            this.value = value;
        }
    }

}