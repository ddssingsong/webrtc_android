package com.dds;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import com.huawang.dongxiangjun.myapplication.R;

/**
 * Created by dds on 2018/11/7.
 * android_shuai@163.com
 */
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
        et_room.setText("room123456");
    }


    public void JoinRoom(View view) {
        WebrtcUtil.call(this, et_room.getText().toString().trim());

    }


    public void JoinRoomSingleVideo(View view) {
        WebrtcUtil.callSingle(this, et_room.getText().toString().trim(), true);
    }

    public void JoinRoomSingleAudio(View view) {
        WebrtcUtil.callSingle(this, et_room.getText().toString().trim(), false);
    }
}
