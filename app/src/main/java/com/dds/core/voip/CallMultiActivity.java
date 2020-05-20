package com.dds.core.voip;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.dds.webrtc.R;

/**
 * Created by dds on 2018/7/26.
 * 多人通话界面
 */
public class CallMultiActivity extends AppCompatActivity {

    public static void openActivity(Activity activity, String room) {
        Intent intent = new Intent(activity, CallMultiActivity.class);
        intent.putExtra("room", room);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_call);
        initView();
        initData();
    }

    private void initData() {
        Intent intent = getIntent();
        String room = intent.getStringExtra("room");

    }

    private void initView() {

    }
}
