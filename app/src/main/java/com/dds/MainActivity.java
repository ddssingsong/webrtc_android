package com.dds;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import com.dds.webrtclib.ChatRoomActivity;
import com.huawang.dongxiangjun.myapplication.R;

public class MainActivity extends AppCompatActivity {
    private EditText et_signal;
    private EditText et_stun;
    private EditText et_room;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initView();
        initVar();

    }

    private void initView() {
        et_signal = findViewById(R.id.et_signal);
        et_stun = findViewById(R.id.et_stun);
        et_room = findViewById(R.id.et_room);
    }

    private void initVar() {
        //这里配置服务器
        et_signal.setText("wss://47.254.34.146/wss");
        et_stun.setText("stun:47.254.34.146:3478");
        et_room.setText("dds123456");
    }


    public void WebRTCHelper(View view) {
        ChatRoomActivity.openActivity(this,
                et_signal.getText().toString().trim(),
                et_stun.getText().toString().trim(),
                et_room.getText().toString().trim()
        );

    }
}
