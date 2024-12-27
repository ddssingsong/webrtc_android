package com.dds.temple2.socket.inter;

import com.dds.temple2.socket.data.CommData;


public interface DataListener {

    /**
     * 接收到广播数据
     *
     * @param commData
     */
    void onBroadcastArrive(CommData commData);

    /**
     * 接收到指定设备发出的点对点数据
     *
     * @param commData
     * @return 响应特定的数据
     */
    void onCommandArrive(CommData commData);

}
