package com.dds.temple2.socket.inter;

import com.dds.temple2.socket.data.Device;


public interface DeviceListener {

    void onDeviceAdd(Device device);

    void onDeviceRemove(Device device);

}
