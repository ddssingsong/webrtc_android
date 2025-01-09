package com.dds.temple2.lancomm.data;


public class Device {

    /**
     * ip地址
     */
    private String ip;

    public Device(String ip) {
        this.ip = ip;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    @Override
    public String toString() {
        return "Device{" +
                "ip='" + ip + '\'' +
                '}';
    }
}
