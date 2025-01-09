package com.dds.temple2.lancomm.receive;

import com.dds.temple2.lancomm.inter.DataListener;
import com.dds.temple2.lancomm.inter.DeviceListener;


public interface Receiver {

    /**
     * 添加数据监听
     *
     * @param dataListener
     */
    void addDataListener(DataListener dataListener);

    /**
     * 添加设备监听
     *
     * @param deviceListener
     */
    void addDeviceListener(DeviceListener deviceListener);

    /**
     * 移除数据监听
     *
     * @param dataListener
     */
    void removeDataListener(DataListener dataListener);

    /**
     * 移除设备监听
     *
     * @param deviceListener
     */
    void removeDeviceListener(DeviceListener deviceListener);

}
