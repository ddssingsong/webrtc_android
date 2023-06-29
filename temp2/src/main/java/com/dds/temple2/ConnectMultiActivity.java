package com.dds.temple2;

import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.dds.temple2.nsd.FinderManager;
import com.dds.temple2.nsd.NSDServer;

public class ConnectMultiActivity extends AppCompatActivity {
    private static final String TAG = "ConnectMultiActivity";
    private FinderManager finderManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_connect);
    }

    @Override
    protected void onResume() {
        super.onResume();
        FinderManager.getInstance().init(this);
        FinderManager.getInstance().startServer(this, new NSDServer.IRegisterCallBack() {
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
            public void onUnregistrationFailed(NsdServiceInfo nsdServiceInfo, int i) {
                Log.d(TAG, "onUnregistrationFailed: " + nsdServiceInfo.getServiceName());
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        finderManager.stopServer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
