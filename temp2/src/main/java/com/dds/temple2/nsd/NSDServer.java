package com.dds.temple2.nsd;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;


/**
 * see <a href="https://developer.android.com/training/connect-devices-wirelessly/nsd?hl=zh-cn">...</a>
 */
public class NSDServer {
    public static final String TAG = "NSDServer";
    private static NSDServer mNSDServer;
    private static HandlerThread socketSendThread;
    private NsdManager mNsdManager;
    private NsdManager.RegistrationListener mRegistrationListener;
    private String mServerName;
    private IRegisterCallBack registerCallBack;
    private NsdServiceInfo serviceInfo;
    private boolean mHasRegistered = false;

    public interface IRegisterCallBack {
        void onRegistrationFailed(NsdServiceInfo nsdServiceInfo, int i);

        void onServiceRegistered(NsdServiceInfo nsdServiceInfo);

        void onServiceUnregistered(NsdServiceInfo nsdServiceInfo);

        void onUnregistrationFailed(NsdServiceInfo nsdServiceInfo, int i);
    }

    private NSDServer() {
    }

    public static NSDServer getInstance() {
        if (mNSDServer == null) {
            synchronized (NSDServer.class) {
                if (mNSDServer == null) {
                    mNSDServer = new NSDServer();
                }
            }
        }
        return mNSDServer;
    }

    public void initializeRegistrationListener() {
        this.mRegistrationListener = new NsdManager.RegistrationListener() {
            @Override
            public void onRegistrationFailed(NsdServiceInfo nsdServiceInfo, int i) {
                Log.d(NSDServer.TAG, "NsdServiceInfo onRegistrationFailed");
                if (NSDServer.this.registerCallBack != null) {
                    NSDServer.this.registerCallBack.onRegistrationFailed(nsdServiceInfo, i);
                }
            }

            @Override
            public void onServiceRegistered(NsdServiceInfo nsdServiceInfo) {
                NSDServer.this.mServerName = nsdServiceInfo.getServiceName();
                Log.d(NSDServer.TAG, "onServiceRegistered: " + nsdServiceInfo);
                Log.d(NSDServer.TAG, "mServerName onServiceRegistered: " + NSDServer.this.mServerName);
                if (NSDServer.this.registerCallBack != null) {
                    NSDServer.this.registerCallBack.onServiceRegistered(nsdServiceInfo);
                }

            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo nsdServiceInfo) {
                Log.d(NSDServer.TAG, "onServiceUnregistered serviceInfo: " + nsdServiceInfo);
                if (NSDServer.this.registerCallBack != null) {
                    NSDServer.this.registerCallBack.onServiceUnregistered(nsdServiceInfo);
                }
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo nsdServiceInfo, int i) {
                Log.d(NSDServer.TAG, "onUnregistrationFailed serviceInfo: " + nsdServiceInfo + " ,errorCode:" + i);
                if (NSDServer.this.registerCallBack != null) {
                    NSDServer.this.registerCallBack.onUnregistrationFailed(nsdServiceInfo, i);
                }
            }
        };
    }

    public void registerService(Context context, String serverName, int port) {
        this.mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);

        NsdServiceInfo nsdServiceInfo = new NsdServiceInfo();
        this.serviceInfo = nsdServiceInfo;
        nsdServiceInfo.setServiceName(serverName);
        this.serviceInfo.setPort(port);
        this.serviceInfo.setServiceType(FinderManager.SERVER_TYPE);

        NsdServiceInfoEntity nsdServiceInfoEntity = new NsdServiceInfoEntity();
        nsdServiceInfoEntity.setNsdServiceInfo(this.serviceInfo);

        ServiceInfoBean serviceInfoBean = new ServiceInfoBean();

        serviceInfoBean.setTime(((System.currentTimeMillis() / 1000.0) + ""));
        serviceInfoBean.setUuid("server001");
        serviceInfoBean.setV("1");
        nsdServiceInfoEntity.setServiceInfoBean(serviceInfoBean);
        this.serviceInfo.setAttribute("info", serviceInfoBean.toJsonString());

        FinderManager.getInstance().updateLocalNsdServiceInfo(nsdServiceInfoEntity);
        this.mNsdManager.registerService(this.serviceInfo, NsdManager.PROTOCOL_DNS_SD, this.mRegistrationListener);
        this.mHasRegistered = true;
    }


    public void setRegisterCallBack(IRegisterCallBack iRegisterCallBack) {
        this.registerCallBack = iRegisterCallBack;
    }

    public void stopNSDServer() {
        NsdManager nsdManager = this.mNsdManager;
        if (nsdManager != null) {
            try {
                if (this.mHasRegistered) {
                    NsdManager.RegistrationListener registrationListener = this.mRegistrationListener;
                    if (registrationListener != null) {
                        nsdManager.unregisterService(registrationListener);
                    }
                    this.mHasRegistered = false;
                    FinderManager.getInstance().updateLocalNsdServiceInfo(null);
                }
            } catch (Exception e) {
                Log.e(TAG, "stopNSDServer" + e);
            }
        }
        HandlerThread handlerThread = socketSendThread;
        if (handlerThread != null) {
            handlerThread.quit();
            socketSendThread = null;
        }
    }

    public void startNSDServer(final Context context, final String serverName, final int port) {
        HandlerThread handlerThread = new HandlerThread("NSDServer");
        socketSendThread = handlerThread;
        handlerThread.setPriority(1);
        socketSendThread.start();
        Handler handler = new Handler(socketSendThread.getLooper());
        handler.post(() -> {
            NSDServer.this.initializeRegistrationListener();
            NSDServer.this.registerService(context, serverName, port);
        });
    }


}