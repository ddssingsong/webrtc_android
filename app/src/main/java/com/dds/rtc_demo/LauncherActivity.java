package com.dds.rtc_demo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.dds.base.activity.BaseActivity;
import com.dds.temple0.Temple0Activity;
import com.dds.temple1.Temple1Activity;
import com.dds.temple2.Temple2Activity;
import com.dds.webrtc.R;

public class LauncherActivity extends BaseActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
    }

    public void temple0(View view) {
        startActivity(new Intent(this, Temple0Activity.class));
    }

    public void temple1(View view) {
        startActivity(new Intent(this, Temple1Activity.class));
    }

    public void temple2(View view) {
        startActivity(new Intent(this, Temple2Activity.class));

    }


}
