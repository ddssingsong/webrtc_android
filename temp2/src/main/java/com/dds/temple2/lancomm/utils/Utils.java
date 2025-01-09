package com.dds.temple2.lancomm.utils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;


public class Utils {

    /**
     * 获取内网IP地址
     *
     * @return
     * @throws SocketException
     */
    public static byte[] getLocalIPAddress() throws SocketException {
        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
            NetworkInterface intf = en.nextElement();
            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                InetAddress inetAddress = enumIpAddr.nextElement();
                if (!inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address)) {
                    return inetAddress.getAddress();
                }
            }
        }
        return new byte[4];
    }

    public static String ipbyteToString(byte[] ip) throws UnknownHostException {
        return InetAddress.getByAddress(ip).getHostAddress();
    }

    public static String getDeviceIp() {
        try {
            byte[] localIPAddress = getLocalIPAddress();
            return Utils.ipbyteToString(localIPAddress);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return "";
    }
}
