package com.dds.java;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.dds.webrtc.R;

public class JavaActivity extends AppCompatActivity {

    private final static String TAG = "JavaActivity";
    private EditText editText;
    private EditText et_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_java);
        editText = findViewById(R.id.et_wss);
        et_name = findViewById(R.id.et_name);
    }


    /*----------------------------java版本服务器测试--------------------------------------------*/

    // 登录
    public void connect(View view) {
        SocketManager.getInstance().connect(editText.getText().toString().trim(), et_name.getText().toString().trim());

    }

}
