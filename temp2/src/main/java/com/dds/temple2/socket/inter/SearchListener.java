package com.dds.temple2.socket.inter;

import com.dds.temple2.socket.data.Device;

import java.util.HashMap;


public interface SearchListener {

    /**
     * 开始搜索
     */
    void onSearchStart();

    /**
     * 发现新设备
     *
     * @param device
     */
    void onSearchedNewOne(Device device);

    /**
     * 搜索结束
     */
    void onSearchFinish(HashMap<String, Device> devices);

}
