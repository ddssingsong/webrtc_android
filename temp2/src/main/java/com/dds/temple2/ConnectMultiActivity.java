package com.dds.temple2;

import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.dds.temple2.nsd.FinderManager;
import com.dds.temple2.nsd.IDiscoveryCallBack;
import com.dds.temple2.nsd.NSDServer;

public class ConnectMultiActivity extends AppCompatActivity {
    private static final String TAG = "ConnectMultiActivity";
    private FinderManager finderManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_connect);
        // start broadcast
        FinderManager.getInstance().init(this);
        FinderManager.getInstance().startServer(this);
        // start discovery
        FinderManager.getInstance().startClient(this, new IDiscoveryCallBack() {
            @Override
            public void onServiceFound(NsdServiceInfo nsdServiceInfo) {
                Log.d(TAG, "onServiceFound: " + nsdServiceInfo.toString());
            }

            @Override
            public void onServiceLost(NsdServiceInfo nsdServiceInfo) {
                Log.d(TAG, "onServiceLost: " + nsdServiceInfo.toString());
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // start broadcast
        FinderManager.getInstance().stopServer();
        // stop discovery
        FinderManager.getInstance().stopClient();
    }
}
