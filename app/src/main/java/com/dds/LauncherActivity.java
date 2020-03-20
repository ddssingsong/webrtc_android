package com.dds;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.dds.java.MainActivity;
import com.dds.webrtc.R;

public class LauncherActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private EditText etUser;
    private Button button8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        initView();
    }

    private void initView() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar = findViewById(R.id.toolbar);
        etUser = findViewById(R.id.et_user);
        button8 = findViewById(R.id.button8);

    }

    public void java(View view) {
        String username = etUser.getText().toString().trim();
        App.getInstance().setUsername(username);
        startActivity(new Intent(this, MainActivity.class));
    }
}
