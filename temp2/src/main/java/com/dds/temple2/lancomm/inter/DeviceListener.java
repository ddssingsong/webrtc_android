package com.dds.temple2.lancomm.inter;

import com.dds.temple2.lancomm.data.Device;


public interface DeviceListener {

    void onDeviceAdd(Device device);

    void onDeviceRemove(Device device);

}
