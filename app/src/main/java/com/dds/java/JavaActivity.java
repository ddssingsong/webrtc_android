package com.dds.java;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.dds.webrtc.R;

public class JavaActivity extends AppCompatActivity {

    private final static String TAG = "JavaActivity";
    private EditText editText;
    private EditText et_name;
    private RadioGroup deviceRadioGroup;
    private int device; // 0 phone 1 pc

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_java);
        initView();
        editText.setText("ws://192.168.1.138:3000/ws");
    }

    private void initView() {
        editText = findViewById(R.id.et_wss);
        et_name = findViewById(R.id.et_name);
        deviceRadioGroup = findViewById(R.id.device);
        deviceRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.phone) {
                device = 0;
            } else if (checkedId == R.id.pc) {
                device = 1;
            }

        });
    }


    /*----------------------------java版本服务器测试--------------------------------------------*/

    // 登录
    public void connect(View view) {
        SocketManager.getInstance().connect(editText.getText().toString().trim(), et_name.getText().toString().trim(), device);

    }

}
