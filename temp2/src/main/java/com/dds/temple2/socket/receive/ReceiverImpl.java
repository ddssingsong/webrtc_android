package com.dds.temple2.socket.receive;

import com.dds.temple2.socket.inter.DataListener;
import com.dds.temple2.socket.inter.DeviceListener;

import java.util.ArrayList;
import java.util.List;


public class ReceiverImpl implements Receiver {

    private final List<DataListener> receiverList = new ArrayList<>();

    public List<DeviceListener> getDeviceListeners() {
        return deviceListenerList;
    }

    private final List<DeviceListener> deviceListenerList = new ArrayList<>();

    private ReceiverImpl() {
    }

    public static ReceiverImpl getImpl() {
        return ReceiverImplHolder.sInstance;
    }

    private static class ReceiverImplHolder {
        private static final ReceiverImpl sInstance = new ReceiverImpl();
    }

    public List<DataListener> getReceivers() {
        return receiverList;
    }

    @Override
    public void addDataListener(DataListener dataListener) {
        if (!receiverList.contains(dataListener)) {
            receiverList.add(dataListener);
        }
        openReceiverThread();
    }

    @Override
    public void addDeviceListener(DeviceListener deviceListener) {
        if (!deviceListenerList.contains(deviceListener)) {
            deviceListenerList.add(deviceListener);
        }
        openReceiverThread();
    }

    @Override
    public void removeDataListener(DataListener dataListener) {
        receiverList.remove(dataListener);
        closeReceiverThread();
    }

    @Override
    public void removeDeviceListener(DeviceListener deviceListener) {
        deviceListenerList.remove(deviceListener);
        closeReceiverThread();
    }

    private void openReceiverThread() {
        if (!receiverList.isEmpty() || !deviceListenerList.isEmpty()) {
            //开启broadcast数据接收
            BroadcastReceiverThread.open();
        }
        if (!receiverList.isEmpty()) {
            //开启点对点数据接收
            CommandReceiverThread.open();
        }
    }

    private void closeReceiverThread() {
        if (receiverList.isEmpty() && deviceListenerList.isEmpty()) {
            //关闭broadcast数据接收
            BroadcastReceiverThread.close();
        }
        if (receiverList.isEmpty()) {
            //关闭点对点数据接收
            CommandReceiverThread.close();
        }
    }
}
