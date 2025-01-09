package com.dds.temple2.lancomm.receive;

import android.app.Service;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import com.dds.temple2.lancomm.data.CommData;
import com.dds.temple2.lancomm.data.Const;
import com.dds.temple2.lancomm.data.Device;
import com.dds.temple2.lancomm.inter.DataListener;
import com.dds.temple2.lancomm.inter.DeviceListener;
import com.dds.temple2.lancomm.ContextVal;
import com.dds.temple2.lancomm.utils.ConvertUtils;
import com.dds.temple2.lancomm.utils.Utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;


public class BroadcastReceiverThread extends Thread {

    private static final String TAG = "ReceiverThread";
    private static BroadcastReceiverThread receiverThread;
    private WakeLock wakelock;

    /**
     * 启动接收线程
     */
    public synchronized static void open() {
        if (receiverThread == null) {
            receiverThread = new BroadcastReceiverThread();
            receiverThread.start();
        }
    }

    /**
     * 关闭接收线程
     */
    public synchronized static void close() {
        if (receiverThread != null) {
            receiverThread.destory();
            receiverThread = null;
        }
    }

    DatagramSocket socket;
    volatile boolean openFlag;

    public void destory() {
        if (socket != null) {
            socket.close();
            socket = null;
        }
        openFlag = false;
        releaseLock();
    }

    @Override
    public void run() {
        wakeLock();
        try {
            //指定接收数据包的端口
            socket = new DatagramSocket(Const.DEVICE_BROADCAST_PORT);
            byte[] buf = new byte[1024];
            DatagramPacket recePacket = new DatagramPacket(buf, buf.length);
            openFlag = true;
            while (openFlag) {
                socket.receive(recePacket);
                parseRespData(recePacket);
            }
        } catch (IOException e) {
            destory();
        }
    }

    private void parseRespData(DatagramPacket recePacket) throws IOException {
        if (recePacket.getLength() < 2) {
            return;
        }
        byte[] data = recePacket.getData();
        if (data[0] != Const.PACKET_PREFIX) {
            return;
        }
        switch (data[1]) {
            case Const.PACKET_TYPE_BROADCAST:
                parseBroadcastData(data);
                break;
            case Const.PACKET_TYPE_DEVICE_ADD:
                parseDeviceAddData(data, true);
                break;
            case Const.PACKET_TYPE_DEVICE_REMOVE:
                parseDeviceAddData(data, false);
                break;
            default:
                break;
        }
    }

    private void parseBroadcastData(byte[] data) throws UnknownHostException {
        CommData commData = new CommData();
        byte[] ipData = getIp(data);
        String ipString = Utils.ipbyteToString(ipData);
        Log.v(TAG, Utils.getDeviceIp() + Const.RECEIVE_SYMBOL + ipString + " 局域网广播");
        //广播消息
        byte[] lengthData = new byte[4];
        System.arraycopy(data, 6, lengthData, 0, 4);
        int dataLength = ConvertUtils.byteArrayToInt(lengthData);
        byte[] msgData = new byte[dataLength];
        System.arraycopy(data, 10, msgData, 0, dataLength);
        Log.v(TAG, Utils.getDeviceIp() + Const.RECEIVE_SYMBOL + ipString + "局域网广播，package data:\r\n" + Arrays.toString(msgData));
        Device device = new Device(Utils.ipbyteToString(ipData));
        commData.setDevice(device);
        commData.setData(msgData);
        List<DataListener> dataListeners = ReceiverImpl.getImpl().getReceivers();
        for (DataListener listener : dataListeners) {
            listener.onBroadcastArrive(commData);
        }
    }

    private void parseDeviceAddData(byte[] data, boolean isAdd) throws UnknownHostException {
        byte[] ip = getIp(data);
        List<DeviceListener> deviceListeners = ReceiverImpl.getImpl().getDeviceListeners();
        if (!deviceListeners.isEmpty()) {
            Device device = new Device(Utils.ipbyteToString(ip));
            for (DeviceListener deviceListener : deviceListeners) {
                if (isAdd) {
                    Log.v(TAG, Utils.getDeviceIp() + Const.RECEIVE_SYMBOL + "设备上线" + device.getIp());
                    deviceListener.onDeviceAdd(device);
                } else {
                    Log.v(TAG, Utils.getDeviceIp() + Const.RECEIVE_SYMBOL + "设备下线" + device.getIp());
                    deviceListener.onDeviceRemove(device);
                }
            }
        }
    }

    private byte[] getIp(byte[] data) {
        byte[] ipData = new byte[4];
        System.arraycopy(data, 2, ipData, 0, 4);
        return ipData;
    }

    public void wakeLock() {
        PowerManager pm = (PowerManager) ContextVal.getContext()
                .getSystemService(Service.POWER_SERVICE);
        wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this.getClass().getName());
        wakelock.acquire(10 * 60 * 1000L /*10 minutes*/);
    }

    public void releaseLock() {
        if (wakelock != null && wakelock.isHeld()) {
            wakelock.release();
        }
    }

}
