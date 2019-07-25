package com.dds;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.dds.webrtc.R;

public class JavaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_java);
    }


    /*----------------------------java版本服务器测试--------------------------------------------*/

    // 测试连接webSocket
    public void connect(View view) {

    }
}
