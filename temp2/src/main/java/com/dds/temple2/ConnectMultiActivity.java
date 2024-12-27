package com.dds.temple2;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ConnectMultiActivity extends AppCompatActivity {
    private static final String TAG = "ConnectMultiActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_connect);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}
