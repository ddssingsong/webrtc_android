package com.dds;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import com.dds.webrtc.R;


/**
 * Created by dds on 2018/11/7.
 * android_shuai@163.com
 */
public class MainActivity extends AppCompatActivity {
    private EditText et_signal;
    private EditText et_stun;
    private EditText et_room;
    private EditText et_wss;

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
        et_wss = findViewById(R.id.et_wss);
    }

    private void initVar() {
        et_room.setText("room123456");
        et_wss.setText("ws://192.168.1.122:3000");
    }


    // 单人视频
    public void JoinRoomSingleVideo(View view) {
        WebrtcUtil.callSingle(this,
                et_signal.getText().toString(),
                et_room.getText().toString().trim(),
                true);
    }

    // 单人语音
    public void JoinRoomSingleAudio(View view) {
        WebrtcUtil.callSingle(this,
                et_signal.getText().toString(),
                et_room.getText().toString().trim(),
                false);
    }

    // 群聊
    public void JoinRoom(View view) {
        WebrtcUtil.call(this, et_signal.getText().toString(), et_room.getText().toString().trim());

    }

    //测试wss
    public void wss(View view) {
        WebrtcUtil.testWs(et_wss.getText().toString());
    }
}
