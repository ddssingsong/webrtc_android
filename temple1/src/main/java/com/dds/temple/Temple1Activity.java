package com.dds.temple;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.dds.base.permission.Permissions;


public class Temple1Activity extends AppCompatActivity {
    private static final String TAG = "Temple1Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temple1);
        String[] per = new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA};
        Permissions.request(this, per, integer -> {
            Log.d(TAG, "Permissions.request integer = " + integer);
            if (integer != 0) {
                finish();
            }
        });
    }

    // client start
    public void client(View view) {
        EditText editText = findViewById(R.id.editTextText);
        Editable text = editText.getText();
        String ip = text.toString();
        ConnectActivity.launchActivity(this, ConnectActivity.TYPE_CLIENT, ip);
    }

    // server start
    public void server(View view) {
        ConnectActivity.launchActivity(this, ConnectActivity.TYPE_SERVER, "0.0.0.0");
    }
}