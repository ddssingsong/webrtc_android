package com.dds.webrtclib;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

/**
 * 来电显示界面
 */
public class IncomingActivity extends AppCompatActivity {

    private TextView wr_hang_up;
    private TextView wr_accept;


    public static void openActivity(Context activity) {
        Intent intent = new Intent(activity, IncomingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.wr_activity_incoming);
        initView();
        initListener();
    }


    private void initView() {
        wr_accept = (TextView) findViewById(R.id.wr_accept);
        wr_hang_up = (TextView) findViewById(R.id.wr_hang_up);

    }

    private void initListener() {
        wr_accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WrManager.getInstance().acceptCall(IncomingActivity.this);
                IncomingActivity.this.finish();
            }
        });
        wr_hang_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WrManager.getInstance().refuseCall();
                IncomingActivity.this.finish();
            }
        });
    }

}
