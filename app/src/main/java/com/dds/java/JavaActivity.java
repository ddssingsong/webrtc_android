package com.dds.java;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.dds.java.socket.IUserState;
import com.dds.java.socket.SocketManager;
import com.dds.skywebrtc.SkyEngineKit;
import com.dds.java.voip.CallSingleActivity;
import com.dds.java.voip.VoipEvent;
import com.dds.webrtc.R;

/**
 * 拨打电话界面
 */
public class JavaActivity extends AppCompatActivity implements IUserState {

    private EditText wss;
    private EditText et_name;
    private TextView user_state;

    // 0 phone 1 pc
    private int device;

    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_java);
        initView();
        initData();

    }


    private void initView() {
        wss = findViewById(R.id.et_wss);
        et_name = findViewById(R.id.et_name);
        user_state = findViewById(R.id.user_state);
        RadioGroup deviceRadioGroup = findViewById(R.id.device);
        deviceRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.phone) {
                device = 0;
            } else if (checkedId == R.id.pc) {
                device = 1;
            }

        });
    }

    private void initData() {
        wss.setText("ws://47.93.186.97:5000/ws");
//        wss.setText("ws://192.168.1.138:5000/ws");
        SocketManager.getInstance().addUserStateCallback(this);
        int userState = SocketManager.getInstance().getUserState();
        if (userState == 1) {
            loginState();
        } else {
            logoutState();
        }


    }

    // 登录
    public void connect(View view) {
        SocketManager.getInstance().connect(
                wss.getText().toString().trim(),
                et_name.getText().toString().trim(),
                device);

    }

    // 退出
    public void unConnect(View view) {
        SocketManager.getInstance().unConnect();
    }

    // 拨打语音
    public void call(View view) {
        String phone = ((TextView) findViewById(R.id.et_phone)).getText().toString().trim();
        SkyEngineKit.init(new VoipEvent());
        CallSingleActivity.openActivity(this, phone, true, true);

    }

    // 拨打视频
    public void callVideo(View view) {
        String phone = ((TextView) findViewById(R.id.et_phone)).getText().toString().trim();
        SkyEngineKit.init(new VoipEvent());
        CallSingleActivity.openActivity(this, phone, true, false);
    }

    @Override
    public void userLogin() {
        handler.post(this::loginState);
    }

    @Override
    public void userLogout() {
        handler.post(this::logoutState);
    }

    //--------------------------------------------------------------------------------------

    public void loginState() {
        user_state.setText("用户登录状态：已登录");
        user_state.setTextColor(ContextCompat.getColor(JavaActivity.this, android.R.color.holo_red_light));
    }

    public void logoutState() {
        user_state.setText("用户登录状态：未登录");
        user_state.setTextColor(ContextCompat.getColor(JavaActivity.this, android.R.color.darker_gray));
    }


}
