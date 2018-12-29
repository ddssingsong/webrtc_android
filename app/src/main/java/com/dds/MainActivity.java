package com.dds;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.dds.webrtclib.ChatRoomActivity;
import com.huawang.dongxiangjun.myapplication.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

    }


    public void WebRTCHelper(View view) {
        Intent intent = new Intent(MainActivity.this, ChatRoomActivity.class);
        startActivity(intent);

    }
}
