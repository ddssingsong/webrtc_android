package com.dds.temple0;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.dds.temple0.socket.SocketManager;
import com.dds.temple0.socket.Urls;
import com.dds.temple0.ui.UserHomeActivity;

public class Temple0Activity extends AppCompatActivity implements SocketManager.IUserStateEvent {
    private EditText etUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temple0);
        initView();

        if (SocketManager.getInstance().isLogin()) {
            launchHome();
        }
    }


    @Override
    protected void onDestroy() {
        SocketManager.getInstance().removeUserStateCallback(this);
        super.onDestroy();

    }

    private void initView() {
        etUser = findViewById(R.id.et_user);
    }


    public void login(View view) {
        String username = etUser.getText().toString().trim();
        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, "please input your name", Toast.LENGTH_LONG).show();
            return;
        }

        // 添加登录回调
        SocketManager.getInstance().addUserStateCallback(this, this);
        // 连接socket:登录
        SocketManager.getInstance().login(Urls.WS, username, 0);
    }


    private void launchHome() {
        startActivity(new Intent(this, UserHomeActivity.class));
        finish();
    }

    @Override
    public void userLogin(String userId) {
        launchHome();
    }

    @Override
    public void userLogout(String info) {
    }

}