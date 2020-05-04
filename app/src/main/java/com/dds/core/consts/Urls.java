package com.dds.core.consts;

/**
 * Created by dds on 2020/4/19.
 * ddssingsong@163.com
 */
public class Urls {

    //    public final static String HOST = "http://47.93.186.97:5000/";
    private final static String HOST = "http://192.168.2.2:5000/";

    //    private final static String WS = "ws://47.93.186.97:5000/ws";
    public final static String WS = "ws://192.168.2.2:5000/ws";

    public static String getUserList() {
        return HOST + "userList";
    }

    public static String getRoomList() {
        return HOST + "roomList";
    }
}
