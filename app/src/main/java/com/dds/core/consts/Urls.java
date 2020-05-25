package com.dds.core.consts;

/**
 * Created by dds on 2020/4/19.
 * ddssingsong@163.com
 */
public class Urls {

    //    private final static String IP = "192.168.2.111";
    public final static String IP = "47.93.186.97";


    private final static String HOST = "http://" + IP + ":5000/";

    public final static String WS = "ws://" + IP + ":5000/ws";

    public static String getUserList() {
        return HOST + "userList";
    }

    public static String getRoomList() {
        return HOST + "roomList";
    }
}
