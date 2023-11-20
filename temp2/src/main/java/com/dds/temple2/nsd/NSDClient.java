package com.dds.temple2.nsd;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.dds.base.utils.NetworkUtils;


public class NSDClient {
    private static final String TAG = "NSDClient";

    private static Handler mSocketReceiveHandler;
    private static HandlerThread socketReceiveThread;
    private IDiscoveryCallBack discoveryCallBack;
    private NsdManager mNsdManager;
    private NsdManager.DiscoveryListener mNSDDiscoveryListener = null;
    private boolean mStop = false;

    private NSDClient() {
    }

    private static final class MNSDClientHolder {
        static final NSDClient mNSDClient = new NSDClient();
    }

    public static NSDClient getInstance() {
        return MNSDClientHolder.mNSDClient;
    }

    private void initNSDDiscoveryListener() {
        this.mNSDDiscoveryListener = new NsdManager.DiscoveryListener() {
            @Override
            public void onDiscoveryStarted(String str) {
                Log.d(NSDClient.TAG, "onDiscoveryStarted--> " + str);
            }

            @Override
            public void onDiscoveryStopped(String str) {
                Log.d(NSDClient.TAG, "onDiscoveryStopped--> " + str);
            }

            @Override
            public void onServiceFound(NsdServiceInfo nsdServiceInfo) {
                Log.d(NSDClient.TAG, "onServiceFound Info: --> " + nsdServiceInfo);
                NSDClient.this.resoleServer(nsdServiceInfo);
            }

            @Override
            public void onServiceLost(NsdServiceInfo nsdServiceInfo) {
                Log.d(NSDClient.TAG, "onServiceLost--> " + nsdServiceInfo);
                if (NSDClient.this.discoveryCallBack != null) {
                    if (isNetworkAvailable(FinderManager.getInstance().getAppContext())) {
                        NSDClient.this.resoleLostServer(nsdServiceInfo);
                    } else {
                        NSDClient.this.discoveryCallBack.onServiceLost(null);
                    }
                }
            }

            @Override
            public void onStartDiscoveryFailed(String str, int i) {
                Log.d(NSDClient.TAG, "onStartDiscoveryFailed--> " + str + ":" + i);
            }

            @Override
            public void onStopDiscoveryFailed(String str, int i) {
                Log.d(NSDClient.TAG, "onStopDiscoveryFailed--> " + str + ":" + i);
            }
        };
    }

    public void resoleLostServer(NsdServiceInfo nsdServiceInfo) {
        try {
            this.mNsdManager.resolveService(nsdServiceInfo, new NsdManager.ResolveListener() {
                @Override
                public void onResolveFailed(NsdServiceInfo nsdServiceInfo2, int i) {
                    Log.d(NSDClient.TAG, " lost onResolveFailed--> " + i + " serviceInfo:" + nsdServiceInfo2);
                }

                @Override
                public void onServiceResolved(NsdServiceInfo nsdServiceInfo2) {
                    Log.d(NSDClient.TAG, "resolution : " + nsdServiceInfo2.getServiceName() + " \t host_from_server: " + nsdServiceInfo2.getHost() + "\t port from server: " + nsdServiceInfo2.getPort());
                    String hostAddress = nsdServiceInfo2.getHost().getHostAddress();
                    String sb = " lost hostAddress ip--> " + hostAddress;
                    Log.d(NSDClient.TAG, sb);
                    if (NSDClient.this.discoveryCallBack != null) {
                        NSDClient.this.discoveryCallBack.onServiceLost(nsdServiceInfo2);
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "resoleLostServer: " + e);
        }
    }

    public void resoleServer(final NsdServiceInfo nsdServiceInfo) {
        NsdManager nsdManager = this.mNsdManager;
        if (nsdManager == null) {
            return;
        }
        nsdManager.resolveService(nsdServiceInfo, new NsdManager.ResolveListener() {
            @Override
            public void onResolveFailed(NsdServiceInfo nsdServiceInfo2, int errorCode) {
                Log.d(NSDClient.TAG, "onResolveFailed code:" + errorCode + " " + nsdServiceInfo2);
                if (NSDClient.this.mNsdManager != null && errorCode == NsdManager.FAILURE_ALREADY_ACTIVE && !NSDClient.this.mStop && NSDClient.mSocketReceiveHandler != null) {
                    Log.d(TAG, "onResolveFailed retry");
                    NSDClient.mSocketReceiveHandler.postDelayed(() -> NSDClient.this.resoleServer(nsdServiceInfo), 30L);
                }
            }

            @Override
            public void onServiceResolved(NsdServiceInfo nsdServiceInfo2) {
                String ipAddress = NetworkUtils.getIPAddress(true);
                if (ipAddress != null && ipAddress.equals(nsdServiceInfo2.getHost().getHostAddress())) {
                    Log.d(TAG, "onServiceResolved: self ip");
                    return;
                }
                Log.d(NSDClient.TAG, "resolution : " + nsdServiceInfo2.getServiceName() + " \t host_from_server: " + nsdServiceInfo2.getHost() + "\t port from server: " + nsdServiceInfo2.getPort());
                String hostAddress = nsdServiceInfo2.getHost().getHostAddress();
                String sb = "found hostAddress ip--> " + hostAddress;
                Log.d(NSDClient.TAG, sb);
                Log.d(NSDClient.TAG, "serviceInfo message--> " + nsdServiceInfo2);
                if (NSDClient.this.discoveryCallBack != null) {
                    NSDClient.this.discoveryCallBack.onServiceFound(nsdServiceInfo2);
                }
            }
        });
    }

    public void discoveryNSDServer(Context context) {
        this.mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        initNSDDiscoveryListener();
        this.mNsdManager.discoverServices(FinderManager.SERVER_TYPE, 1, this.mNSDDiscoveryListener);
    }

    public void setOnDiscoverCallBack(IDiscoveryCallBack iDiscoveryCallBack) {
        this.discoveryCallBack = iDiscoveryCallBack;
    }

    public void startDiscovery(final Context context) {
        this.mStop = false;
        HandlerThread handlerThread = new HandlerThread("NSDClient");
        socketReceiveThread = handlerThread;
        handlerThread.setPriority(1);
        socketReceiveThread.start();
        Handler handler = new Handler(socketReceiveThread.getLooper());
        mSocketReceiveHandler = handler;
        handler.post(() -> NSDClient.this.discoveryNSDServer(context));
    }

    public void stopDiscovery() {
        this.mStop = true;
        NsdManager nsdManager = this.mNsdManager;
        if (nsdManager != null) {
            nsdManager.stopServiceDiscovery(this.mNSDDiscoveryListener);
        }
        Handler handler = mSocketReceiveHandler;
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        HandlerThread handlerThread = socketReceiveThread;
        if (handlerThread != null) {
            this.mNsdManager = null;
            handlerThread.quit();
            socketReceiveThread = null;
            mSocketReceiveHandler = null;
        }
        this.discoveryCallBack = null;
        Log.d(TAG, "stopDiscovery");
    }

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isAvailable();
    }

}