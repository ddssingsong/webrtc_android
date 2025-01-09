package com.dds.temple2;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.dds.base.permission.Permissions;

public class Temple2Activity extends AppCompatActivity {
    private static final String TAG = "Temple2Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp2);
        String[] per = new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA};
        Permissions.request(this, per, integer -> {
            Log.d(TAG, "Permissions.request integer = " + integer);
            if (integer != 0) {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void startScan(View view) {
        startActivity(new Intent(this, ConnectMultiActivity.class));
    }
}