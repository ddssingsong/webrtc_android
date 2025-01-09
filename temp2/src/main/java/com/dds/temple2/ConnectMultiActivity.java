package com.dds.temple2;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.dds.temple2.lancomm.LanCommManager;
import com.dds.temple2.lancomm.data.Device;
import com.dds.temple2.lancomm.inter.DeviceListener;

public class ConnectMultiActivity extends AppCompatActivity {
    private static final String TAG = "ConnectMultiActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_connect);

    }



    private void initSocket(){
        LanCommManager.getBroadcaster().broadcast("dddd".getBytes());
        LanCommManager.getReceiver().addDeviceListener(new DeviceListener() {
            @Override
            public void onDeviceAdd(Device device) {

            }

            @Override
            public void onDeviceRemove(Device device) {

            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}
