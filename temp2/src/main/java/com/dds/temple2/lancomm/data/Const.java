package com.dds.temple2.lancomm.data;


public class Const {

    /**
     * udp数据包前缀
     */
    public static final int PACKET_PREFIX = '#';

    /**
     * udp数据包类型：广播消息
     */
    public static final int PACKET_TYPE_BROADCAST = 0x10;

    /**
     * udp数据包类型：搜索类型
     */
    public static final int PACKET_TYPE_SEARCH_DEVICE = 0x11;

    /**
     * udp数据包类型：搜索应答类型
     */
    public static final int PACKET_TYPE_SEARCH_DEVICE_RSP = 0x12;

    /**
     * p2p消息类型
     */
    public static final int PACKET_TYPE_COMMAND = 0x13;

    /**
     * p2p消息类型响应
     */
    public static final int PACKET_TYPE_COMMAND_RSP = 0x14;

    /**
     * 局域网内有设备新增
     */
    public static final int PACKET_TYPE_DEVICE_ADD = 0x15;

    /**
     * 局域网内有设备移除
     */
    public static final int PACKET_TYPE_DEVICE_REMOVE = 0x6;

    /**
     * 用于设备broadcast的端口
     */
    public static final int DEVICE_BROADCAST_PORT = 8100;

    /**
     * 用于设备搜索的端口
     */
    public static final int DEVICE_SEARCH_PORT = 8101;

    /**
     * 用于point to point 数据传输的端口
     */
    public static final int COMMAND_PORT = 8102;

    /**
     * 接收超时时间
     */
    public static final int RECEIVE_TIME_OUT = 1000;

    /**
     * 搜索的最大设备数量
     */
    public static final int SEARCH_DEVICE_MAX = 250;

    public static final String SEND_SYMBOL = "====>>";
    public static final String RECEIVE_SYMBOL = "<<====";
}
