package com.dds.temple2.nsd;

import android.app.Activity;
import android.content.Context;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;

public class FinderManager {
    private static final String TAG = "FinderManager";
    public static final String SERVER_TYPE = "_nsd-rtc._udp";

    public static final int SERVER_PORT = 3002;

    private int localPort = SERVER_PORT;
    private NsdServiceInfoEntity mLocalNsdServiceInfoEntity;

    private Context appContext;

    private FinderManager() {

    }

    private static final class InstanceHolder {
        public static final FinderManager instance = new FinderManager();
    }

    public static FinderManager getInstance() {
        return InstanceHolder.instance;
    }

    public void updateLocalNsdServiceInfo(NsdServiceInfoEntity nsdServiceInfoEntity) {
        mLocalNsdServiceInfoEntity = nsdServiceInfoEntity;
    }

    public NsdServiceInfoEntity getLocalNsdServiceInfoEntity() {
        return mLocalNsdServiceInfoEntity;
    }

    public Context getAppContext() {
        return appContext;
    }

    public void init(Context context) {
        if (context instanceof Activity) {
            appContext = context.getApplicationContext();
        } else {
            appContext = context;
        }
        initializeServerSocket();
    }

    private void initializeServerSocket() {
        // Initialize a server socket on the next available port.
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(0);
            // Store the chosen port.
            localPort = serverSocket.getLocalPort();
            serverSocket.close();
        } catch (IOException e) {
            Log.d(TAG, "initializeServerSocket: " + e);
        }


    }

    public void startServer(Context context) {
        String deviceName = Build.PRODUCT + " " + Build.DEVICE;
        String bluetooth_name = Settings.Secure.getString(context.getContentResolver(), "bluetooth_name");
        NSDServer.getInstance().setRegisterCallBack(new NSDServer.IRegisterCallBack() {
            @Override
            public void onRegistrationFailed(NsdServiceInfo nsdServiceInfo, int i) {
                Log.d(TAG, "onRegistrationFailed: " + nsdServiceInfo.getServiceName());
            }

            @Override
            public void onServiceRegistered(NsdServiceInfo nsdServiceInfo) {
                Log.d(TAG, "onServiceRegistered: " + nsdServiceInfo.getServiceName());
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo nsdServiceInfo) {
                Log.d(TAG, "onServiceUnregistered: " + nsdServiceInfo.getServiceName());
            }

            @Override
            public void onUnRegistrationFailed(NsdServiceInfo nsdServiceInfo, int i) {
                Log.d(TAG, "onUnRegistrationFailed: " + nsdServiceInfo.getServiceName());
            }
        });
        NSDServer.getInstance().startNSDServer(context, "device_" + (bluetooth_name == null ? deviceName : bluetooth_name), localPort);

    }

    public void stopServer() {
        NSDServer.getInstance().stopNSDServer();
    }

    public void startClient(Context context, IDiscoveryCallBack iDiscoveryCallBack) {
        NSDClient.getInstance().setOnDiscoverCallBack(iDiscoveryCallBack);
        NSDClient.getInstance().startDiscovery(context);

    }

    public void stopClient() {
        NSDClient.getInstance().stopDiscovery();
    }


}
