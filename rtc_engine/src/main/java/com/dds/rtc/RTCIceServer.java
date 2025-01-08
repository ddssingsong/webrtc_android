package com.dds.rtc;

public class RTCIceServer {
    public final String url;
    public final String username;
    public final String password;
    public final boolean isStun;
    public final boolean isEnableTls;


    public RTCIceServer(String url, String username, String password, boolean isStun, boolean isEnableTls) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.isEnableTls = isEnableTls;
        this.isStun = isStun;
    }
}
