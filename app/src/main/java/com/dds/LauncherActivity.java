package com.dds;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.dds.java.JavaActivity;
import com.dds.nodejs.NodejsActivity;
import com.dds.webrtc.R;

public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
    }

    public void nodejs(View view) {
        startActivity(new Intent(this, NodejsActivity.class));
    }

    public void java(View view) {
        startActivity(new Intent(this, JavaActivity.class));
    }
}
